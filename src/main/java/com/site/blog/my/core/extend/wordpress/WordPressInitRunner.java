package com.site.blog.my.core.extend.wordpress;

import com.site.blog.my.core.extend.wordpress.data.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fuchenggang
 * @description
 * @createtime 2019/11/11
 */
@Component
@Order(value = 1)
public class WordPressInitRunner implements ApplicationRunner {

    @Autowired
    private WordPressImport wordPressImport;

    @Autowired
    private WordPressConfig wordPressConfig;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        if(!wordPressConfig.getIsOpenImport()){
           return;
        }

        List<Article> articles = wordPressImport.parseArticleByFilePath(wordPressConfig.getXmlPath());
        wordPressImport.batchSaveBlog(articles);
    }
}
