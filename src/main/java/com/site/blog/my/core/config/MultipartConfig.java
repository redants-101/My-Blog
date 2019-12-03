package com.site.blog.my.core.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

/**
 * @author fuchenggang
 * 上传文件大小限制
 */
@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //单个文件最大
        factory.setMaxFileSize(DataSize.ofMegabytes(5L));
        /// 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(50L));
        return factory.createMultipartConfig();
    }
}
