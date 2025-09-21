package com.axis.bank.auth.security;

import com.axis.bank.exception.AxisBankException;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@AllArgsConstructor
public class RsaKeyLoader {

    private final ResourceLoader resourceLoader;

    public PrivateKey loadPrivateKey(String resourceLocation) throws Exception {
        Resource resource = resourceLoader.getResource(resourceLocation);
        try (InputStream inputStream = resource.getInputStream()) {
            String privateKey = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            privateKey = privateKey.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decode = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decode);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception exception) {
            throw new AxisBankException(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public PublicKey loadPublicKey(String resourceLocation) throws Exception {
        Resource resource = resourceLoader.getResource(resourceLocation);
        try (InputStream inputStream = resource.getInputStream()) {
            String publicKey = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            publicKey = publicKey.replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decode = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decode);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        }
    }
}
