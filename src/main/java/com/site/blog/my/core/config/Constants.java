package com.site.blog.my.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author RedAnts
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "blog.constants.config")
public class Constants {
    public final static String FILE_UPLOAD_DIC = "D:\\workspace2019\\myspace\\My-Blog\\static-files\\";//上传文件的默认url前缀，根据部署设置自行修改
    private String fileUpload;
    private String theme;
}
