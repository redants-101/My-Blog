package com.site.blog.my.core.extend.wordpress.data;

import lombok.Data;

/**
 * @author fuchenggang
 * @description
 * @createtime 2019/11/3
 */
@Data
public class Article {
    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 分类集合
     */
    private String categorys;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签集合
     */
    private String tags;

    /**
     * 创建时间
     */
    private String createTime;
}
