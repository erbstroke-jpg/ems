package com.erbol.ems.common.service;

import com.erbol.ems.common.exception.BusinessRuleException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded files on the local filesystem and returns public URLs
 * that can be embedded in HTML pages.
 *
 * <p>For event cover images, the typical flow is:
 * <ol>
 *   <li>Organizer submits a form with a MultipartFile.</li>
 *   <li>{@code storeEventCover()} writes the file and returns "/uploads/events/{uuid}.{ext}".</li>
 *   <li>That URL is saved on the Event entity and rendered by Thymeleaf.</li>
 * </ol>
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif"
    );

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path baseDir;

    @PostConstruct
    void init() throws IOException {
        baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(baseDir.resolve("events"));
        log.info("File storage initialized at: {}", baseDir);
    }

    /**
     * Save the given file under /uploads/events/ and return a public URL
     * suitable for direct use in {@code <img src="...">}.
     *
     * @return public URL like "/uploads/events/{uuid}.jpg"
     */
    public String storeEventCover(MultipartFile file) {
        validate(file);

        String extension = extractExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = baseDir.resolve("events").resolve(storedName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Failed to write uploaded file", ex);
            throw new BusinessRuleException("Failed to save uploaded image");
        }

        String publicUrl = "/uploads/events/" + storedName;
        log.info("Stored event cover: {}", publicUrl);
        return publicUrl;
    }

    /**
     * Delete a previously stored file by its public URL.
     * Silently does nothing if the file does not exist.
     */
    public void deleteIfExists(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
            return;
        }
        String relative = publicUrl.substring("/uploads/".length());
        Path target = baseDir.resolve(relative).normalize();

        if (!target.startsWith(baseDir)) {
            log.warn("Refusing to delete path outside upload dir: {}", publicUrl);
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            log.warn("Failed to delete file {}: {}", publicUrl, ex.getMessage());
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException("File is too large (max 5 MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleException(
                    "Unsupported file type. Allowed: JPEG, PNG, WebP, GIF");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessRuleException("Unsupported file extension");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}