package com.shop.donationtip.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class DonationTipImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private final Path uploadDirectory =
            Paths.get(
                            "uploads",
                            "donation-tips"
                    )
                    .toAbsolutePath()
                    .normalize();


    public String save(
            MultipartFile file
    ) {
        if (
                file == null
                        || file.isEmpty()
        ) {
            return null;
        }

        if (
                file.getContentType() == null
                        || !ALLOWED_CONTENT_TYPES.contains(
                        file.getContentType()
                )
        ) {
            throw new IllegalArgumentException(
                    "이미지는 JPG, PNG, WEBP 형식만 등록할 수 있습니다."
            );
        }

        if (
                file.getSize()
                        > 5L * 1024 * 1024
        ) {
            throw new IllegalArgumentException(
                    "이미지 파일은 5MB 이하만 등록할 수 있습니다."
            );
        }

        try {
            Files.createDirectories(
                    uploadDirectory
            );

            String extension =
                    getExtension(
                            file.getOriginalFilename()
                    );

            String fileName =
                    UUID.randomUUID()
                            + extension;

            Path target =
                    uploadDirectory.resolve(
                            fileName
                    );

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/donation-tips/"
                    + fileName;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "헌혈팁 이미지를 저장하지 못했습니다.",
                    e
            );
        }
    }


    private String getExtension(
            String originalFileName
    ) {
        if (
                originalFileName == null
                        || !originalFileName.contains(".")
        ) {
            return "";
        }

        String extension =
                originalFileName.substring(
                                originalFileName
                                        .lastIndexOf(".")
                        )
                        .toLowerCase();

        return switch (extension) {
            case ".jpg",
                 ".jpeg",
                 ".png",
                 ".webp"
                    -> extension;

            default
                    -> "";
        };
    }
}