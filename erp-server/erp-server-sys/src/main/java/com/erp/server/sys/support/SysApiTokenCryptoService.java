package com.erp.server.sys.support;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 完整 API Token 加解密。认证只走 hash，解密仅用于用户主动复制。
 */
@Component
public class SysApiTokenCryptoService {

    private static final String ENV_KEY = "ERP_API_TOKEN_AES_KEY";

    private static final String AES = "AES";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int GCM_TAG_LENGTH = 128;

    private static final int IV_LENGTH = 12;

    private static final int AES_128_BYTES = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${erp.api-token.aes-key:}")
    private String configuredKey;

    public String encrypt(String plainToken) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getKeySpec(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(e, ApiError.AUTH_API_TOKEN_ENCRYPT_FAILED);
        }
    }

    public String decrypt(String encryptedToken) {
        try {
            byte[] allBytes = Base64.getUrlDecoder().decode(encryptedToken);
            if (allBytes.length <= IV_LENGTH) {
                throw new ServiceException(ApiError.AUTH_API_TOKEN_CIPHERTEXT_INVALID);
            }
            byte[] iv = Arrays.copyOfRange(allBytes, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(allBytes, IV_LENGTH, allBytes.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getKeySpec(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(e, ApiError.AUTH_API_TOKEN_DECRYPT_FAILED);
        }
    }

    private SecretKeySpec getKeySpec() {
        String key = StringUtils.defaultIfBlank(configuredKey, System.getenv(ENV_KEY));
        if (StringUtils.isBlank(key)) {
            throw new ServiceException(ApiError.AUTH_API_TOKEN_AES_KEY_REQUIRED);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(Arrays.copyOf(keyBytes, AES_128_BYTES), AES);
        } catch (Exception e) {
            throw new ServiceException(e, ApiError.AUTH_API_TOKEN_AES_KEY_INIT_FAILED);
        }
    }
}
