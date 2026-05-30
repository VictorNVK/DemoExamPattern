package ru.demoexam.backend.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.demoexam.backend.storage.StoragePaths;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final StoragePaths storagePaths;

    public ResponseEntity<String> storeProductImage(MultipartFile file, Integer productId, String existingStoredPath) throws IOException {
        storagePaths.ensureCreated();
        if (existingStoredPath != null && !existingStoredPath.isBlank()) {
            deleteStoredImage(existingStoredPath);
        }

        String targetFileName = "product_" + productId + ".png";
        Path targetFile = storagePaths.imagesDirectory().resolve(targetFileName);

        BufferedImage sourceImage = ImageIO.read(file.getInputStream());
        if (sourceImage == null) {
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(targetFile.getFileName().toString());
        }

        BufferedImage resizedImage = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resizedImage.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(sourceImage, 0, 0, 300, 200, null);
        } finally {
            graphics.dispose();
        }

        ImageIO.write(resizedImage, "png", targetFile.toFile());
        return ResponseEntity.ok(targetFileName);
    }

    public ResponseEntity<Void> deleteStoredImage(String storedPath) throws IOException {
        if (storedPath == null || storedPath.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        Path file = storagePaths.imagesDirectory().resolve(storedPath);
        Files.deleteIfExists(file);
        return ResponseEntity.noContent().build();
    }
}
