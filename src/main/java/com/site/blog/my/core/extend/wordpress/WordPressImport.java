package com.site.blog.my.core.extend.wordpress;

import com.site.blog.my.core.config.Constants;
import com.site.blog.my.core.entity.Blog;
import com.site.blog.my.core.extend.wordpress.data.Article;
import com.site.blog.my.core.service.BlogService;
import com.site.blog.my.core.service.CategoryService;
import com.site.blog.my.core.service.TagService;
import com.site.blog.my.core.util.DateStyle;
import com.site.blog.my.core.util.DateUtils;
import com.site.blog.my.core.util.Dom4jUtils;
import com.site.blog.my.core.util.HttpDownload;
import org.dom4j.Element;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author RedAnts
 */
@Component
public class WordPressImport {

    private static final Logger logger = LoggerFactory.getLogger(WordPressImport.class);

    @Resource
    private CategoryService categoryService;

    @Resource
    private TagService tagService;

    @Resource
    private Constants constants;

    @Resource
    private BlogService blogService;

    /**
     * 默认博客的分类图标
     */
    private static final String DEFAULT_CATEGORY_IMG = "/admin/dist/img/category/06.png";

    /**
     * 默认博客封面
     */
    private static final String DEFAULT_BLOG_IMG = "/admin/dist/img/rand/36.jpg";

    /**
     * 默认分类
     */
    private static final String DEFAULT_BLOG_CATEGORY = "日常随笔";

    /**
     * 默认标签
     */
    private static final String DEFAULT_BLOG_TAG = "读书";

    public void batchSaveBlog(List<Article> articles) throws InterruptedException {
        for (Article article : articles) {

            Blog blog = new Blog();
            //分类
            String categoryName = getOneCategory(article);
            if(StringUtils.isEmpty(categoryName)){
                categoryName = DEFAULT_BLOG_CATEGORY;
            }
            Integer categoryId = categoryService.saveCategoryReturnId(categoryName, DEFAULT_CATEGORY_IMG);
            blog.setBlogCategoryId(categoryId);
            blog.setBlogCategoryName(categoryName);

            //标题
            String title = article.getTitle();
            if(StringUtils.isEmpty(title)){
                continue;
            }
            blog.setBlogTitle(title);

            String subUrl = "";
            blog.setBlogSubUrl(subUrl);

            //博客封面
            String blogCoverImage = DEFAULT_BLOG_IMG;
            blog.setBlogCoverImage(blogCoverImage);

            //博客内容
            String content = disposeContent(article.getContent());
            if(StringUtils.isEmpty(content)){
                continue;
            }
            blog.setBlogContent(content);

            //博客标签
            String tags = article.getTags();
            if(StringUtils.isEmpty(tags)){
                tags = DEFAULT_BLOG_TAG;
            }
            String[] tagArr = tags.split(",");
            for (String tag : tagArr) {
                tagService.saveTag(tag);
            }
            blog.setBlogTags(tags);

            //博客状态
            Byte blogStatus = 1;
            blog.setBlogStatus(blogStatus);

            //博客阅读量
            Long views = 7888L;
            blog.setBlogViews(views);

            //是否允许评论
            Byte enableComment = 0;
            blog.setEnableComment(enableComment);

            //是否删除
            Byte isDeleted = 0;
            blog.setIsDeleted(isDeleted);

            //创建时间
            Date createTime = new Date();
            blog.setCreateTime(createTime);

            //更新时间
            Date updateTime = new Date();
            blog.setUpdateTime(updateTime);

            blogService.saveBlog(blog);
            Thread.sleep(1000);
        }
    }

    public  List<Article> parseArticleByFilePath(String xmlPath) {
        org.dom4j.Document document = Dom4jUtils.getXMLByFilePath(xmlPath);
        return parseArticle(document);
    }

    public  List<Article> parseArticleByInputStream(InputStream inputStream) {
        org.dom4j.Document document = Dom4jUtils.getXMLByInputStream(inputStream);
        return parseArticle(document);
    }


    public List<Article> parseArticle(org.dom4j.Document document) {
        List<Article> articles = new ArrayList<>();
        try {
            if (null == document) {
                return articles;
            }

            Element root = document.getRootElement();
            Element channel = root.element("channel");
            List<Element> elements = Dom4jUtils.getChildElementsByChildnode(channel, "item");
            for (Element item : elements) {
                Article article = new Article();

                //标题
                Element title = item.element("title");
                if (null == title) {
                    continue;
                }
                article.setTitle(title.getStringValue());

                //内容
                String content = item.elementText("encoded");
                if (null == content || content.length() <= 500) {
                    continue;
                }
                article.setContent(content);

                item.elements("postmeta");
                List<Element> postmetas = Dom4jUtils.getChildElementsByChildnode(item, "postmeta");
                for (Element postmeta : postmetas) {
                    //标签（关键字）
                    if ("_aioseop_keywords".equals(postmeta.elementText("meta_key"))) {
                        String keyWords = postmeta.elementText("meta_value");
                        article.setTags(keyWords);
                    }
                    //描述
                    if ("_aioseop_description".equals(postmeta.elementText("meta_key"))) {
                        String description = postmeta.elementText("meta_value");
                        article.setDescription(description);
                    }
                }

                //对获取标签的补充逻辑
                if (StringUtils.isEmpty(article.getTags())) {
                    List<Element> categorys = Dom4jUtils.getChildElementsByChildnode(item, "category");
                    String tags = "";
                    for (int i = 0; i < categorys.size(); i++) {
                        Element category = categorys.get(i);
                        String domain = Dom4jUtils.getAttribute(category, "domain");
                        if ("post_tag".equals(domain)) {
                            if (i == (categorys.size() - 1)) {
                                tags = tags + category.getStringValue();
                            } else {
                                tags = tags + (category.getStringValue() + ",");
                            }
                        }
                    }
                    article.setTags(tags);
                }

                //分类
                setCategorys(item, article);

                //创建时间
                setCreateTime(item, article);

                articles.add(article);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articles;
    }

    /**
     * 替换博客内容中的图片地址
     *
     * @param content
     * @return
     */
    private String disposeContent(String content) {
        content = content.replace("<![CDATA[","");
        StringBuilder sb1 = new StringBuilder(content);
        content = sb1.reverse().toString();
        content = content.replace("[[","");
        StringBuilder sb2 = new StringBuilder(content);
        content = sb2.reverse().toString();
        Document doc = Jsoup.parse(content);
        org.jsoup.nodes.Element body = doc.body();
        Elements imgs = body.getElementsByTag("img");
        logger.info("图片上传路径：{}",constants.getFileUpload());
        for (org.jsoup.nodes.Element img : imgs) {
            String src = img.attr("src");
            String imageUrl = HttpDownload.imageDownload(src, constants.getFileUpload());
            System.out.println(src + "----" + imageUrl);
            if(StringUtils.isEmpty(imageUrl)){
                imageUrl = src;
            }
            img.attr("src", imageUrl);
        }
        return body.html();
    }

    private String getOneCategory(Article article) {
        String categorys = article.getCategorys();
        String[] categoryArr = categorys.split(",");
        if (categoryArr.length == 0) {
            return null;
        }
        return categoryArr[0];
    }


    private void setCategorys(Element item, Article article) {
        List<Element> categorys = Dom4jUtils.getChildElementsByChildnode(item, "category");
        String categorysStr = "";
        for (int i = 0; i < categorys.size(); i++) {
            Element category = categorys.get(i);
            String domain = Dom4jUtils.getAttribute(category, "domain");
            if ("category".equals(domain)) {
                if (i == (categorys.size() - 1)) {
                    categorysStr = categorysStr + category.getStringValue();
                } else {
                    categorysStr = categorysStr + (category.getStringValue() + ",");
                }
            }
        }
        article.setCategorys(categorysStr);
    }

    private void setCreateTime(Element item, Article article) {
        Element postDate = Dom4jUtils.getChildElement(item, "post_date");
        if (null == postDate) {
            article.setCreateTime(DateUtils.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
        } else {
            String dateStr = postDate.getStringValue();
            if (StringUtils.isEmpty(dateStr)) {
                article.setCreateTime(DateUtils.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
            } else {
                article.setCreateTime(dateStr);
            }
        }
    }

}
