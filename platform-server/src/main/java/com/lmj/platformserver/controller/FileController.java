package com.lmj.platformserver.controller;

import com.lmj.platformserver.annotation.validation.File;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.utils.AliOssUtil;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@Validated
@RequestMapping("/file")
public class FileController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/uploadImage")
    public Response<String> uploadImage(
            @File(maxSize = 2, allowFileTypes = {"jpg", "jpeg", "png"})
            @NotNull(message = "文件不能为空")
            MultipartFile file) {
        log.info("图片上传: {}", file.getOriginalFilename());
        String path = aliOssUtil.upload(file);
        return Response.success(path);
    }

    @PostMapping("/uploadTestcaseFile")
    public Response<String> uploadTestcaseFile(
            @File(maxSize = 10, allowFileTypes = {})
            @NotNull(message = "文件不能为空")
            MultipartFile file
    ) {
        log.info("测试用例文件上传：{}", file.getOriginalFilename());
        String path = aliOssUtil.upload(file);
        return Response.success(path);
    }
}
