package com.lmj.platformserver.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.lmj.platformserver.exception.FileUploadException;
import com.lmj.platformserver.properties.AliOssProperties;
import com.lmj.platformserver.result.ResultCodeEnum;
import jakarta.annotation.PreDestroy;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AliOssUtil {

    @Autowired
    private OSS ossClient;

    @Autowired
    private AliOssProperties aliOssProperties;

    public String upload(MultipartFile file) {

        String originalFileName = file.getOriginalFilename();
        String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date())
                + "/"
                + UUID.randomUUID().toString().replace("-", "")
                + originalFileName.substring(originalFileName.lastIndexOf("."));

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentDisposition("inline");
        objectMetadata.setContentType(file.getContentType());

        try {
            // 创建PutObject请求。
            ossClient.putObject(aliOssProperties.getBucketName(), fileName, new ByteArrayInputStream(file.getBytes()), objectMetadata);
        } catch (Exception e) {
            log.error("文件上传失败：", e);
            throw new FileUploadException(ResultCodeEnum.FILE_UPLOAD_FAIL);
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(aliOssProperties.getBucketName())
                .append(".")
                .append(aliOssProperties.getEndpoint())
                .append("/")
                .append(fileName);

        log.info("文件上传到: {}", stringBuilder);

        return stringBuilder.toString();
    }

    @Async
    public void deleteFile(String filepath) {

        // https://demoskytakeout.oss-cn-shenzhen.aliyuncs.com/20250515/a505eceed4584061a5f0696f7ea27e18.jpg
        StringBuilder sb = new StringBuilder();
        sb.append("https://");
        sb.append(aliOssProperties.getBucketName());
        sb.append(".");
        sb.append(aliOssProperties.getEndpoint());
        sb.append("/");
        try {
            ossClient.deleteObject(aliOssProperties.getBucketName(), filepath.split(sb.toString())[1]);
            log.info("oss文件删除成功，文件路径：{}", filepath);
        } catch (Exception e) {
            log.error("异常信息：", e);
        }
    }

    @Async
    public void deleteFileBatch(List<String> filepath) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://");
        sb.append(aliOssProperties.getBucketName());
        sb.append(".");
        sb.append(aliOssProperties.getEndpoint());
        sb.append("/");
        try {
            List<String> keys = filepath.stream().map(s -> s.split(sb.toString())[1]).collect(Collectors.toList());
            ossClient.deleteObjects(new DeleteObjectsRequest(aliOssProperties.getBucketName()).withKeys(keys).withQuiet(true));
            for (String s : filepath) {
                log.info("oss文件删除成功，地址为：{}", s);
            }
        } catch (Exception e) {
            log.error("异常信息：", e);
        }
    }

    @SneakyThrows
    public byte[] getFileBytes(String url) {
        String objectName = parseObjectNameFromUrl(url);
        try {
            @Cleanup OSSObject ossObject = ossClient.getObject(aliOssProperties.getBucketName(), objectName);
            @Cleanup InputStream inputStream = ossObject.getObjectContent();
            return inputStream.readAllBytes();
        } catch (OSSException e) {
            return null;
        }
    }

    private String parseObjectNameFromUrl(String url) {
        String bucketUrlPrefix = "https://" + aliOssProperties.getBucketName() + "." + aliOssProperties.getEndpoint() + "/";
        if (!url.startsWith(bucketUrlPrefix)) {
            throw new IllegalArgumentException("提供的URL不是有效的OSS文件路径或与配置不符: " + url);
        }
        return url.substring(bucketUrlPrefix.length());
    }

    // 在对象销毁前关闭客户端连接
    @PreDestroy
    private void closeOSSClient() {
        ossClient.shutdown();
    }

}
