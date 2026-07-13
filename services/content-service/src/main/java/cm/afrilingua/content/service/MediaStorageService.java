package cm.afrilingua.content.service;

import cm.afrilingua.content.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

/** Uploads media files (word audio, illustrations) to Supabase Storage via
 * the S3-compatible protocol, and returns the public URL to store on the
 * Word entity (audioUrl / a future imageUrl field). Requires the bucket to
 * be configured as public in Supabase, since these URLs are served directly
 * to the mobile app without any auth. */
@Service
@RequiredArgsConstructor
public class MediaStorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public String uploadAudio(MultipartFile file, String wordId) {
        return upload(file, "audio/" + wordId + fileExtension(file, ".mp3"));
    }

    public String uploadImage(MultipartFile file, String wordId) {
        return upload(file, "images/" + wordId + fileExtension(file, ".png"));
    }

    private String upload(MultipartFile file, String key) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Properties.bucket())
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }

        return publicUrl(key);
    }

    /** Constructs the public URL from the base endpoint. Supabase serves
     * public bucket objects at .../storage/v1/object/public/{bucket}/{key},
     * distinct from the S3 API endpoint used for uploads. */
    private String publicUrl(String key) {
        String base = s3Properties.endpoint()
                .replace("/storage/v1/s3", "/storage/v1/object/public");
        return base + "/" + s3Properties.bucket() + "/" + key;
    }

    private String fileExtension(MultipartFile file, String fallback) {
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            return original.substring(original.lastIndexOf('.'));
        }
        return fallback;
    }
}
