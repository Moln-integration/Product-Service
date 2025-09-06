package se.moln.productservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) throws IOException {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public StoredFile store(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IOException("Empty file");
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) ext = original.substring(dot);
        String savedName = UUID.randomUUID() + ext;

        Path target = root.resolve(savedName);
        file.transferTo(target);

        String url = "/uploads/" + savedName;
        return new StoredFile(url, original, file.getContentType(), file.getSize());
    }

    public record StoredFile(String url, String originalName, String contentType, long size) {
    }
}