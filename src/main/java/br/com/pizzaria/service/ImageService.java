package br.com.pizzaria.service;

import br.com.pizzaria.dtos.ImageUploadResult;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp");

    private final Cloudinary cloudinary;

    public ImageUploadResult store(MultipartFile file, String folder){
        if (file == null || file.isEmpty()){
            throw new InvalidImageException("Image is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)){
            throw new InvalidImageException("Unsupported image type. Use JPEG, PNG or WEBP");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "resource_type", "image"));

            return new ImageUploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"));

        }catch (IOException ex){
            throw new ImageStorageException("Could not upload image", ex);
        }
    }

    public void delete(String publicId){
        try {

        }catch (Exception ex){
            log.warn("Could not delete image", ex);
        }
    }
}
