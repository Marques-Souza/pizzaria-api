package br.com.pizzaria.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloundinaryConfig {

    public Cloudinary cloudinary(@Value("${cloudinary.url}") String cloudinaryUrl) {
        return new Cloudinary(cloudinaryUrl);
    }
}
