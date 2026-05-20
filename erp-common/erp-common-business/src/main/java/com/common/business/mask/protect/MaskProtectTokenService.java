package com.common.business.mask.protect;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;
import com.common.business.mask.core.MaskFieldDescriptor;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏回显保护 token 服务。
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class MaskProtectTokenService {

    private static final int DEFAULT_TTL_SECONDS = 300;
    private static final int MIN_TTL_SECONDS = 30;
    private static final int MAX_TTL_SECONDS = 3600;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Value("${mask.protect.encrypt-key:}")
    private String encryptKey;

    public MaskProtectTokenService() {
    }

    MaskProtectTokenService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void save(Object owner, MaskFieldDescriptor fd, Object originalValue, LoginUser user) {
        if (owner == null || fd == null || !fd.isValueProtectEnabled() || user == null
                || StringUtils.isBlank(user.getUid()) || redissonClient == null) {
            return;
        }
        String recordId = fieldValue(owner, fd.getProtectRecordIdField());
        String version = fieldValue(owner, fd.getProtectVersionField());
        MaskProtectVerifyMode verifyMode = fd.getProtectVerifyMode() == null
                ? MaskProtectVerifyMode.DB_VALUE_COMPARE : fd.getProtectVerifyMode();
        if (fd.getProtectMode() == MaskProtectMode.REJECT || verifyMode == MaskProtectVerifyMode.REJECT) {
            return;
        }
        if (StringUtils.isBlank(recordId)
                || (verifyMode == MaskProtectVerifyMode.PARAM_VERSION && StringUtils.isBlank(version))) {
            log.debug("skip mask protect cache, class={}, field={}, recordIdBlank={}, versionBlank={}, verifyMode={}",
                    owner.getClass().getName(), fd.getField().getName(), StringUtils.isBlank(recordId),
                    StringUtils.isBlank(version), verifyMode);
            return;
        }
        int ttl = normalizeTtl(fd.getProtectTtlSeconds());
        if (fd.getProtectParamBindings() == null || fd.getProtectParamBindings().isEmpty()) {
            log.debug("skip mask protect cache, no param binding, class={}, field={}",
                    owner.getClass().getName(), fd.getField().getName());
            return;
        }
        String nonce = randomTokenPart();
        String token = buildToken(user.getUid(), owner.getClass().getName(), fd.getField().getName(),
                recordId, fd.getPermission(), nonce);

        MaskProtectContext context = new MaskProtectContext();
        context.setToken(token);
        context.setUserId(user.getUid());
        context.setClassPath(owner.getClass().getName());
        context.setFieldName(fd.getField().getName());
        context.setParamBindings(copyBindings(fd.getProtectParamBindings()));
        context.setRecordId(recordId);
        context.setVersionFieldName(fd.getProtectVersionField());
        context.setVersionValue(version);
        context.setVerifyMode(verifyMode);
        context.setTableName(fd.getProtectTableName());
        context.setRecordIdColumn(fd.getProtectRecordIdColumn());
        context.setValueColumn(fd.getProtectValueColumn());
        context.setDeletedColumn(fd.getProtectDeletedColumn());
        context.setPermissionCode(fd.getPermission());
        context.setOriginalNull(originalValue == null);
        context.setOriginalValue(originalValue == null
                ? null : encodeOriginalValue(MaskProtectReflectionUtils.normalizeValue(originalValue)));
        context.setCreateTimeMillis(System.currentTimeMillis());
        context.setExpireTimeMillis(context.getCreateTimeMillis() + ttl * 1000L);
        context.setNonce(nonce);

        String ctxKey = contextKey(token);
        String body = JSON.toJSONString(context);
        redissonClient.<String>getBucket(ctxKey).set(body, ttl, TimeUnit.SECONDS);
        for (MaskProtectBinding binding : fd.getProtectParamBindings()) {
            String idxKey = indexKey(user.getUid(), owner.getClass().getName(), fd.getField().getName(),
                    binding.getParamClassPath(), binding.getParamFieldName(), recordId, fd.getPermission());
            redissonClient.<String>getBucket(idxKey).set(token, ttl, TimeUnit.SECONDS);
        }
    }

    public MaskProtectContext loadAndValidate(CfgMaskFieldSnapshotEntry entry, MaskProtectBinding binding,
                                              String recordId, String versionValue, LoginUser user) {
        if (entry == null || binding == null || user == null
                || StringUtils.isBlank(user.getUid()) || redissonClient == null) {
            throw new MaskProtectException();
        }
        MaskProtectVerifyMode verifyMode = entry.getProtectVerifyMode() == null
                ? MaskProtectVerifyMode.DB_VALUE_COMPARE : entry.getProtectVerifyMode();
        if (StringUtils.isBlank(recordId)
                || (verifyMode == MaskProtectVerifyMode.PARAM_VERSION && StringUtils.isBlank(versionValue))) {
            throw new MaskProtectException();
        }
        String idxKey = indexKey(user.getUid(), entry.getClassPath(), entry.getFieldName(),
                binding.getParamClassPath(), binding.getParamFieldName(), recordId, entry.getPermission());
        String token = redissonClient.<String>getBucket(idxKey).get();
        if (StringUtils.isBlank(token)) {
            throw new MaskProtectException();
        }
        MaskProtectContext context = loadContext(token);
        validateContext(context, user.getUid(), entry, binding, recordId, versionValue);
        context.setOriginalValue(Boolean.TRUE.equals(context.getOriginalNull())
                ? null : decodeOriginalValue(context.getOriginalValue()));
        return context;
    }

    public String currentUserId() {
        LoginUser user = UserContext.getLoginUser();
        return user == null ? "" : user.getUid();
    }

    void setEncryptKeyForTest(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    private MaskProtectContext loadContext(String token) {
        RBucket<String> bucket = redissonClient.getBucket(contextKey(token));
        String body = bucket.get();
        if (StringUtils.isBlank(body)) {
            throw new MaskProtectException();
        }
        try {
            return JSON.parseObject(body, MaskProtectContext.class);
        } catch (Throwable e) {
            throw new MaskProtectException();
        }
    }

    private void validateContext(MaskProtectContext context, String userId, CfgMaskFieldSnapshotEntry entry,
                                 MaskProtectBinding binding, String recordId, String versionValue) {
        if (context == null) {
            throw new MaskProtectException();
        }
        long now = System.currentTimeMillis();
        if (context.getExpireTimeMillis() == null || context.getExpireTimeMillis() < now) {
            throw new MaskProtectException();
        }
        MaskProtectVerifyMode verifyMode = entry.getProtectVerifyMode() == null
                ? MaskProtectVerifyMode.DB_VALUE_COMPARE : entry.getProtectVerifyMode();
        if (!StringUtils.equals(userId, context.getUserId())
                || !StringUtils.equals(entry.getClassPath(), context.getClassPath())
                || !StringUtils.equals(entry.getFieldName(), context.getFieldName())
                || !containsBinding(context.getParamBindings(), binding)
                || !StringUtils.equals(recordId, context.getRecordId())
                || (verifyMode == MaskProtectVerifyMode.PARAM_VERSION
                && !StringUtils.equals(versionValue, context.getVersionValue()))
                || context.getVerifyMode() != verifyMode
                || (verifyMode == MaskProtectVerifyMode.DB_VALUE_COMPARE
                && !sameDbCompareConfig(entry, context))
                || !StringUtils.equals(StringUtils.defaultString(entry.getPermission()),
                StringUtils.defaultString(context.getPermissionCode()))) {
            throw new MaskProtectException();
        }
    }

    private boolean sameDbCompareConfig(CfgMaskFieldSnapshotEntry entry, MaskProtectContext context) {
        return StringUtils.equals(StringUtils.defaultString(entry.getProtectTableName()),
                StringUtils.defaultString(context.getTableName()))
                && StringUtils.equals(StringUtils.defaultString(entry.getProtectRecordIdColumn()),
                StringUtils.defaultString(context.getRecordIdColumn()))
                && StringUtils.equals(StringUtils.defaultString(entry.getProtectValueColumn()),
                StringUtils.defaultString(context.getValueColumn()))
                && StringUtils.equals(StringUtils.defaultString(entry.getProtectDeletedColumn()),
                StringUtils.defaultString(context.getDeletedColumn()));
    }

    private String fieldValue(Object owner, String fieldName) {
        Object value = MaskProtectReflectionUtils.getFieldValue(owner, fieldName);
        return MaskProtectReflectionUtils.normalizeValue(value);
    }

    private int normalizeTtl(Integer ttlSeconds) {
        int ttl = ttlSeconds == null ? DEFAULT_TTL_SECONDS : ttlSeconds;
        if (ttl < MIN_TTL_SECONDS) {
            return MIN_TTL_SECONDS;
        }
        return Math.min(ttl, MAX_TTL_SECONDS);
    }

    private String buildToken(String userId, String classPath, String fieldName, String recordId,
                              String permission, String nonce) {
        return sha256(userId + "|" + classPath + "|" + fieldName + "|" + recordId + "|"
                + StringUtils.defaultString(permission) + "|" + nonce);
    }

    private String indexKey(String userId, String classPath, String fieldName, String paramClassPath,
                            String paramFieldName, String recordId, String permission) {
        String digest = sha256(userId + "|" + classPath + "|" + fieldName + "|" + recordId
                + "|" + StringUtils.defaultString(paramClassPath)
                + "|" + StringUtils.defaultString(paramFieldName)
                + "|" + StringUtils.defaultString(permission));
        return RedisCacheConstants.MASK_PROTECT_INDEX_PREFIX + digest;
    }

    private boolean containsBinding(List<MaskProtectBinding> bindings, MaskProtectBinding expected) {
        if (bindings == null || bindings.isEmpty() || expected == null) {
            return false;
        }
        for (MaskProtectBinding binding : bindings) {
            if (binding == null) {
                continue;
            }
            if (StringUtils.equals(binding.getParamClassPath(), expected.getParamClassPath())
                    && StringUtils.equals(binding.getParamFieldName(), expected.getParamFieldName())
                    && StringUtils.equals(binding.getParamRecordIdField(), expected.getParamRecordIdField())
                    && StringUtils.equals(binding.getParamVersionField(), expected.getParamVersionField())) {
                return true;
            }
        }
        return false;
    }

    private List<MaskProtectBinding> copyBindings(List<MaskProtectBinding> bindings) {
        List<MaskProtectBinding> result = new ArrayList<>();
        if (bindings == null) {
            return result;
        }
        for (MaskProtectBinding source : bindings) {
            if (source == null) {
                continue;
            }
            MaskProtectBinding target = new MaskProtectBinding();
            target.setParamClassPath(source.getParamClassPath());
            target.setParamFieldName(source.getParamFieldName());
            target.setParamRecordIdField(source.getParamRecordIdField());
            target.setParamVersionField(source.getParamVersionField());
            result.add(target);
        }
        return result;
    }

    private String contextKey(String token) {
        return RedisCacheConstants.MASK_PROTECT_CONTEXT_PREFIX + token;
    }

    private static String randomTokenPart() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String encodeOriginalValue(String value) {
        if (StringUtils.isBlank(encryptKey) || value == null) {
            return value;
        }
        try {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return "ENC:v1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(iv)
                    + ":" + Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception e) {
            throw new MaskProtectException();
        }
    }

    private String decodeOriginalValue(String value) {
        if (value == null || !value.startsWith("ENC:v1:")) {
            return value;
        }
        if (StringUtils.isBlank(encryptKey)) {
            throw new MaskProtectException();
        }
        try {
            String[] parts = value.split(":", 4);
            if (parts.length != 4) {
                throw new MaskProtectException();
            }
            byte[] iv = Base64.getUrlDecoder().decode(parts[2]);
            byte[] encrypted = Base64.getUrlDecoder().decode(parts[3]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (MaskProtectException e) {
            throw e;
        } catch (Exception e) {
            throw new MaskProtectException();
        }
    }

    private SecretKeySpec aesKey() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(encryptKey.getBytes(StandardCharsets.UTF_8));
        byte[] key = new byte[16];
        System.arraycopy(digest, 0, key, 0, key.length);
        return new SecretKeySpec(key, "AES");
    }
}
