package com.site.blog.my.core.util;

import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author fuchenggang
 * @description
 * @createtime 2019/11/11
 */
public class HttpDownload {

    private static final Logger logger = LoggerFactory.getLogger(HttpDownload.class);

    public static String imageDownload(String imageUrl,String fileUpload){
        String imagePath = "";
        try {
            imageUrl = getCorrectImageUrl(imageUrl);
            //自定义配置
            RequestConfig globalConfig = getRequestConfig();

            CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
            HttpGet httpGet = new HttpGet(imageUrl);
            CloseableHttpResponse response = httpClient.execute(httpGet);

            //图片类型
            String imageType = getImageType(response);

            //生成文件名称通用方法
            String imageName = getImageName(imageType);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                File fileDirectory = new File(fileUpload);
                if (!fileDirectory.exists()) {
                    if (!fileDirectory.mkdir()) {
                        throw new IOException("文件夹创建失败,路径为：" + fileDirectory);
                    }
                }
                String fullFile = fileUpload + imageName;
                logger.info("文件上传全路径：{}",fullFile);
                //创建文件
                File destFile = new File(fullFile);
                byte[] b = EntityUtils.toByteArray(response.getEntity());
                FileOutputStream fos = new FileOutputStream(destFile);
                fos.write(b);
                fos.close();
                imagePath = "../upload/" + imageName;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return imagePath;
    }

    /**
     * 获取正确的Url
     * @param imageUrl
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String getCorrectImageUrl(String imageUrl) throws UnsupportedEncodingException {
        imageUrl = imageUrl.trim();
        if(imageUrl.contains("?")){
            String parameterTemp = imageUrl.substring(imageUrl.indexOf("?")+1);
            String parameter= URLEncoder.encode(parameterTemp,"utf-8");
            String imageUrlTemp = imageUrl.substring(0,imageUrl.indexOf("?")+1);
            imageUrl = imageUrlTemp+parameter;
        }
        if(!imageUrl.contains("http://") && !imageUrl.contains("https://")){
            imageUrl = "http://"+imageUrl;
        }
        return imageUrl;
    }

    private static String getImageName(String imageType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Random r = new Random();
        return sdf.format(new Date()) + r.nextInt(100) + "." + imageType;
    }

    private static String getImageType(CloseableHttpResponse response) {
        Header[] contentTypeArr = response.getHeaders("Content-Type");
        String value = contentTypeArr[0].getValue();
        return value.substring(value.indexOf("/") + 1);
    }

    private static RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .setConnectionRequestTimeout(6000)
                .setConnectTimeout(6000)
                .build();
    }
}


