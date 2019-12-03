package com.site.blog.my.core.extend.wordpress;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author fuchenggang
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wordpress.config")
public class WordPressConfig {
    private String xmlPath;

    private Boolean isOpenImport = false;

}
