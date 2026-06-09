package com.hotel.controller;

import com.hotel.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class UploadController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:5242880}")
    private long maxSize;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/pjpeg",
        "image/png", "image/x-png",
        "image/gif",
        "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final int MAX_FILENAME_LENGTH = 100;

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }

        boolean typeOk = contentType != null && ALLOWED_TYPES.contains(contentType.toLowerCase());
        boolean extOk = ALLOWED_EXTENSIONS.contains(ext);
        if (!typeOk && !extOk) {
            return Result.error("不支持的图片格式，仅支持 JPG / PNG / GIF / WEBP");
        }

        if (file.getSize() > maxSize) {
            return Result.error("图片大小不能超过 " + (maxSize / 1024 / 1024) + " MB");
        }

        if (originalName != null && originalName.length() > MAX_FILENAME_LENGTH) {
            return Result.error("文件名过长");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

            Path targetPath = uploadPath.resolve(savedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/" + savedName;

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", originalName);
            log.info("图片上传成功: {} -> {}", originalName, savedName);

            return Result.success("上传成功", data);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败，请稍后重试");
        }
    }
}
