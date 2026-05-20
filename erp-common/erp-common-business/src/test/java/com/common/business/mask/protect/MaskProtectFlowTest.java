package com.common.business.mask.protect;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.mask.MaskPermissionResolver;
import com.common.business.mask.MaskStrategy;
import com.common.business.mask.cache.CfgMaskFieldFullCacheDTO;
import com.common.business.mask.cache.CfgMaskFieldLocalCache;
import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;
import com.common.business.mask.core.MaskFieldDescriptor;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 脱敏值回显保护核心流程单元测试。
 *
 * @author cloud-erp
 */
public class MaskProtectFlowTest {

    private MemoryRedisson redisson;
    private MaskProtectTokenService tokenService;
    private MaskedValueDetector detector;
    private FixedPermissionResolver permissionResolver;
    private LoginUser user;

    @Before
    public void setUp() {
        redisson = new MemoryRedisson();
        tokenService = new MaskProtectTokenService(redisson.client());
        detector = new MaskedValueDetector();
        permissionResolver = new FixedPermissionResolver();
        user = new LoginUser();
        user.setUid("u1");
        user.setUserName("tester");
        UserContext.setLoginUser(user);
    }

    @After
    public void tearDown() {
        UserContext.clear();
    }

    @Test
    public void disabledProtectShouldKeepOriginalLogic() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "secret", "other");
        MaskFieldDescriptor fd = descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", false, "id", "version");

        tokenService.save(view, fd, view.amount, user);

        assertEquals(0, redisson.size());
    }

    @Test
    public void queryMaskedShouldWriteRedis() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        MaskFieldDescriptor fd = descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version");

        tokenService.save(view, fd, view.amount, user);

        assertEquals(2, redisson.size());
    }

    @Test
    public void maskedSubmitShouldRestoreOriginalWhenContextMatches() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", 1, "***", "changed");
        MaskProtectInputProcessor processor = processor(entry("amount"));

        processor.process(new Object[]{dto});

        assertEquals("101.77", dto.amount);
        assertEquals("changed", dto.other);
    }

    @Test
    public void differentWriteDtoFieldShouldRestoreByExplicitMapping() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                RenamedUpdateDTO.class, "amountInput", true, "id", "version"), view.amount, user);
        RenamedUpdateDTO dto = new RenamedUpdateDTO("1", 1, "***");
        CfgMaskFieldSnapshotEntry entry = entry(SampleViewDTO.class, "amount",
                RenamedUpdateDTO.class, "amountInput", "recordNo", "revision");

        processor(entry).process(new Object[]{dto});

        assertEquals("101.77", dto.amountInput);
    }

    @Test
    public void oneViewFieldShouldProtectMultipleUpdateDtos() {
        CfgMaskFieldSnapshotEntry entry = entry("amount");
        entry.setProtectParamBindings(bindings(
                binding(SampleUpdateDTO.class, "amount", "id", "version"),
                binding(RenamedUpdateDTO.class, "amountInput", "recordNo", "revision")));
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount", true,
                "id", "version", entry.getProtectParamBindings()), view.amount, user);
        assertEquals(3, redisson.size());

        SampleUpdateDTO dto1 = new SampleUpdateDTO("1", 1, "***", "changed");
        RenamedUpdateDTO dto2 = new RenamedUpdateDTO("1", 1, "***");
        MaskProtectInputProcessor processor = processor(entry);

        processor.process(new Object[]{dto1});
        processor.process(new Object[]{dto2});

        assertEquals("101.77", dto1.amount);
        assertEquals("101.77", dto2.amountInput);
    }

    @Test
    public void missingParamClassPathShouldNotGuessBySameOwnerDto() {
        CfgMaskFieldSnapshotEntry entry = entry("amount");
        entry.setProtectParamClassPath("");
        entry.setProtectParamBindings(Collections.emptyList());
        CfgMaskFieldFullCacheDTO payload = new CfgMaskFieldFullCacheDTO();
        payload.setVersion(1L);
        payload.setData(Collections.singletonList(entry));
        RedissonClient client = redisson.client();
        client.<String>getBucket(RedisCacheConstants.MASK_FIELD_CFG_FULL_KEY).set(JSON.toJSONString(payload));
        CfgMaskFieldLocalCache cache = new CfgMaskFieldLocalCache();
        ReflectionTestUtils.setField(cache, "redissonClient", client);

        assertEquals(0, cache.getValueProtectRules(SampleUpdateDTO.class.getName(), "amount").size());
    }

    @Test
    public void realNewValueShouldRestoreOriginalWhenNoPermission() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", 1, "200.00", "changed");
        MaskProtectInputProcessor processor = processor(entry("amount"));

        processor.process(new Object[]{dto});

        assertEquals("101.77", dto.amount);
    }

    @Test
    public void setNullModeShouldClearFieldWhenNoPermission() {
        CfgMaskFieldSnapshotEntry entry = entry("amount");
        entry.setProtectMode(MaskProtectMode.SET_NULL);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", 1, "200.00", "changed");

        processor(entry).process(new Object[]{dto});

        assertEquals(null, dto.amount);
    }

    @Test(expected = MaskProtectException.class)
    public void rejectModeShouldRejectWhenNoPermission() {
        CfgMaskFieldSnapshotEntry entry = entry("amount");
        entry.setProtectMode(MaskProtectMode.REJECT);

        processor(entry).process(new Object[]{new SampleUpdateDTO("1", 1, "200.00", "changed")});
    }

    @Test(expected = MaskProtectException.class)
    public void redisExpiredShouldReject() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);
        redisson.expireAll();
        SampleUpdateDTO dto = new SampleUpdateDTO("1", 1, "***", "changed");
        processor(entry("amount")).process(new Object[]{dto});
    }

    @Test(expected = MaskProtectException.class)
    public void userMismatchShouldReject() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);
        user.setUid("u2");

        processor(entry("amount")).process(new Object[]{new SampleUpdateDTO("1", 1, "***", "changed")});
    }

    @Test(expected = MaskProtectException.class)
    public void fieldMismatchShouldReject() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "secret-note");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);

        processor(entry("note")).process(new Object[]{new SampleUpdateDTO("1", 1, "changed", "***")});
    }

    @Test(expected = MaskProtectException.class)
    public void recordIdMismatchShouldReject() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);

        processor(entry("amount")).process(new Object[]{new SampleUpdateDTO("2", 1, "***", "changed")});
    }

    @Test(expected = MaskProtectException.class)
    public void versionMismatchShouldReject() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "other");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);

        processor(entry("amount")).process(new Object[]{new SampleUpdateDTO("1", 2, "***", "changed")});
    }

    @Test(expected = MaskProtectException.class)
    public void missingRecordIdShouldReject() {
        SampleNoIdDTO dto = new SampleNoIdDTO(1, "***");

        processor(entry(SampleViewDTO.class, "amount", SampleNoIdDTO.class, "amount",
                "id", "version")).process(new Object[]{dto});
    }

    @Test
    public void plainPermissionShouldSkipProtect() {
        permissionResolver.permissions = Collections.singleton("demo:view");
        SampleUpdateDTO dto = new SampleUpdateDTO("1", 1, "***", "changed");

        processor(entry("amount")).process(new Object[]{dto});

        assertEquals("***", dto.amount);
    }

    @Test
    public void multipleFieldsShouldRestoreTogether() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "secret-note");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);
        tokenService.save(view, descriptor(SampleViewDTO.class, "note",
                SampleUpdateDTO.class, "note", true, "id", "version"), view.note, user);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", 1, "***", "已隐藏");

        processor(entry("amount"), entry("note")).process(new Object[]{dto});

        assertEquals("101.77", dto.amount);
        assertEquals("secret-note", dto.note);
    }

    @Test
    public void oneFieldFailureShouldNotPartiallyRestore() {
        SampleViewDTO view = new SampleViewDTO("1", 1, "101.77", "secret-note");
        tokenService.save(view, descriptor(SampleViewDTO.class, "amount",
                SampleUpdateDTO.class, "amount", true, "id", "version"), view.amount, user);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", 1, "***", "已隐藏");

        try {
            processor(entry("amount"), entry("note")).process(new Object[]{dto});
        } catch (MaskProtectException e) {
            assertEquals("***", dto.amount);
            assertEquals("已隐藏", dto.note);
            return;
        }
        throw new AssertionError("should reject");
    }

    @Test
    public void amountZeroPlaceholderShouldRestoreNumber() {
        BigDecimalViewDTO view = new BigDecimalViewDTO("1", 1, new BigDecimal("101.77"));
        tokenService.save(view, descriptor(BigDecimalViewDTO.class, "amount",
                BigDecimalUpdateDTO.class, "amount", true, "id", "version"),
                view.amount, user);
        BigDecimalUpdateDTO dto = new BigDecimalUpdateDTO("1", 1, BigDecimal.ZERO);

        processor(amountEntry()).process(new Object[]{dto});

        assertEquals(new BigDecimal("101.77"), dto.amount);
    }

    @Test
    public void dbValueCompareShouldRestoreWithoutParamVersion() {
        CfgMaskFieldSnapshotEntry entry = dbEntry("amount");
        SampleViewDTO view = new SampleViewDTO("1", null, "101.77", "other");
        tokenService.save(view, dbDescriptor(SampleViewDTO.class, "amount",
                Collections.singletonList(binding(SampleUpdateNoVersionDTO.class, "amount", "id", ""))),
                view.amount, user);
        SampleUpdateNoVersionDTO dto = new SampleUpdateNoVersionDTO("1", "***");
        MemoryCurrentValueReader reader = new MemoryCurrentValueReader();
        reader.put(entry, "1", "101.77", false);

        processor(reader, entry).process(new Object[]{dto});

        assertEquals("101.77", dto.amount);
    }

    @Test(expected = MaskProtectException.class)
    public void dbValueChangedShouldReject() {
        CfgMaskFieldSnapshotEntry entry = dbEntry("amount");
        SampleViewDTO view = new SampleViewDTO("1", null, "101.77", "other");
        tokenService.save(view, dbDescriptor(SampleViewDTO.class, "amount",
                Collections.singletonList(binding(SampleUpdateNoVersionDTO.class, "amount", "id", ""))),
                view.amount, user);
        SampleUpdateNoVersionDTO dto = new SampleUpdateNoVersionDTO("1", "***");
        MemoryCurrentValueReader reader = new MemoryCurrentValueReader();
        reader.put(entry, "1", "200.00", false);

        processor(reader, entry).process(new Object[]{dto});
    }

    @Test(expected = MaskProtectException.class)
    public void dbCompareConfigMismatchShouldReject() {
        CfgMaskFieldSnapshotEntry entry = dbEntry("amount");
        SampleViewDTO view = new SampleViewDTO("1", null, "101.77", "other");
        tokenService.save(view, dbDescriptor(SampleViewDTO.class, "amount",
                Collections.singletonList(binding(SampleUpdateNoVersionDTO.class, "amount", "id", ""))),
                view.amount, user);
        entry.setProtectValueColumn("other_amount");
        SampleUpdateNoVersionDTO dto = new SampleUpdateNoVersionDTO("1", "***");
        MemoryCurrentValueReader reader = new MemoryCurrentValueReader();
        reader.put(entry, "1", "101.77", false);

        processor(reader, entry).process(new Object[]{dto});
    }

    @Test(expected = MaskProtectException.class)
    public void dbCurrentNullShouldRejectWhenOriginalNotNull() {
        CfgMaskFieldSnapshotEntry entry = dbEntry("amount");
        SampleViewDTO view = new SampleViewDTO("1", null, "101.77", "other");
        tokenService.save(view, dbDescriptor(SampleViewDTO.class, "amount",
                Collections.singletonList(binding(SampleUpdateNoVersionDTO.class, "amount", "id", ""))),
                view.amount, user);
        SampleUpdateNoVersionDTO dto = new SampleUpdateNoVersionDTO("1", "***");
        MemoryCurrentValueReader reader = new MemoryCurrentValueReader();
        reader.put(entry, "1", null, true);

        processor(reader, entry).process(new Object[]{dto});
    }

    @Test
    public void dbValueCompareBatchShouldValidateBeforeApply() {
        CfgMaskFieldSnapshotEntry entry = dbEntry("amount");
        tokenService.save(new SampleViewDTO("1", null, "101.77", "other"),
                dbDescriptor(SampleViewDTO.class, "amount",
                        Collections.singletonList(binding(SampleUpdateNoVersionDTO.class, "amount", "id", ""))),
                "101.77", user);
        tokenService.save(new SampleViewDTO("2", null, "202.88", "other"),
                dbDescriptor(SampleViewDTO.class, "amount",
                        Collections.singletonList(binding(SampleUpdateNoVersionDTO.class, "amount", "id", ""))),
                "202.88", user);
        SampleUpdateNoVersionDTO dto1 = new SampleUpdateNoVersionDTO("1", "***");
        SampleUpdateNoVersionDTO dto2 = new SampleUpdateNoVersionDTO("2", "***");
        MemoryCurrentValueReader reader = new MemoryCurrentValueReader();
        reader.put(entry, "1", "101.77", false);
        reader.put(entry, "2", "999.99", false);

        try {
            processor(reader, entry).process(new Object[]{java.util.Arrays.asList(dto1, dto2)});
        } catch (MaskProtectException e) {
            assertEquals("***", dto1.amount);
            assertEquals("***", dto2.amount);
            return;
        }
        throw new AssertionError("should reject");
    }

    private MaskProtectInputProcessor processor(CfgMaskFieldSnapshotEntry... entries) {
        return processor(null, entries);
    }

    private MaskProtectInputProcessor processor(MaskProtectCurrentValueReader reader,
                                                CfgMaskFieldSnapshotEntry... entries) {
        CfgMaskFieldLocalCache cache = mock(CfgMaskFieldLocalCache.class);
        when(cache.getValueProtectRules(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());
        for (CfgMaskFieldSnapshotEntry entry : entries) {
            if (entry.getProtectParamBindings() != null) {
                for (MaskProtectBinding binding : entry.getProtectParamBindings()) {
                    when(cache.getValueProtectRules(ArgumentMatchers.eq(binding.getParamClassPath()),
                            ArgumentMatchers.eq(binding.getParamFieldName())))
                            .thenReturn(Collections.singletonList(entry));
                    when(cache.findProtectBinding(ArgumentMatchers.eq(entry),
                            ArgumentMatchers.eq(binding.getParamClassPath()),
                            ArgumentMatchers.eq(binding.getParamFieldName())))
                            .thenReturn(binding);
                }
            }
        }
        return new MaskProtectInputProcessor(cache, permissionResolver, tokenService, detector, reader);
    }

    private CfgMaskFieldSnapshotEntry entry(String fieldName) {
        return entry(SampleViewDTO.class, fieldName, SampleUpdateDTO.class, fieldName, "id", "version");
    }

    private CfgMaskFieldSnapshotEntry entry(Class<?> readClass, String readFieldName,
                                           Class<?> writeClass, String writeFieldName,
                                           String writeRecordIdField, String writeVersionField) {
        CfgMaskFieldSnapshotEntry entry = new CfgMaskFieldSnapshotEntry();
        entry.setClassPath(readClass.getName());
        entry.setFieldName(readFieldName);
        entry.setStrategy(MaskStrategy.ALL);
        entry.setReplacement("***");
        entry.setPermission("demo:view");
        entry.setValueProtectEnabled(true);
        entry.setProtectParamClassPath(writeClass.getName());
        entry.setProtectParamFieldName(writeFieldName);
        entry.setProtectRecordIdField("id");
        entry.setProtectParamRecordIdField(writeRecordIdField);
        entry.setProtectVersionField("version");
        entry.setProtectParamVersionField(writeVersionField);
        entry.setProtectVerifyMode(MaskProtectVerifyMode.PARAM_VERSION);
        entry.setProtectParamBindings(Collections.singletonList(
                binding(writeClass, writeFieldName, writeRecordIdField, writeVersionField)));
        entry.setProtectTtlSeconds(300);
        entry.setProtectMode(MaskProtectMode.RESTORE_ORIGINAL);
        return entry;
    }

    private CfgMaskFieldSnapshotEntry dbEntry(String fieldName) {
        CfgMaskFieldSnapshotEntry entry = new CfgMaskFieldSnapshotEntry();
        entry.setClassPath(SampleViewDTO.class.getName());
        entry.setFieldName(fieldName);
        entry.setStrategy(MaskStrategy.ALL);
        entry.setReplacement("***");
        entry.setPermission("demo:view");
        entry.setValueProtectEnabled(true);
        entry.setProtectRecordIdField("id");
        entry.setProtectVerifyMode(MaskProtectVerifyMode.DB_VALUE_COMPARE);
        entry.setProtectTableName("sample_table");
        entry.setProtectRecordIdColumn("id");
        entry.setProtectValueColumn(fieldName);
        entry.setProtectDeletedColumn("is_deleted");
        entry.setProtectParamBindings(Collections.singletonList(
                binding(SampleUpdateNoVersionDTO.class, fieldName, "id", "")));
        entry.setProtectTtlSeconds(300);
        entry.setProtectMode(MaskProtectMode.RESTORE_ORIGINAL);
        return entry;
    }

    private CfgMaskFieldSnapshotEntry amountEntry() {
        CfgMaskFieldSnapshotEntry entry = entry(BigDecimalViewDTO.class, "amount",
                BigDecimalUpdateDTO.class, "amount", "id", "version");
        entry.setStrategy(MaskStrategy.AMOUNT);
        return entry;
    }

    private MaskFieldDescriptor descriptor(Class<?> clazz, String fieldName, Class<?> paramClass,
                                           String paramFieldName, boolean enabled,
                                           String idField, String versionField) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return new MaskFieldDescriptor(field, MaskStrategy.ALL, "", "***", "demo:view",
                    true, true, false, false, 0, enabled,
                    paramClass.getName(), paramFieldName, idField, versionField,
                    300, "", MaskProtectMode.RESTORE_ORIGINAL,
                    Collections.singletonList(binding(paramClass, paramFieldName, idField, versionField)));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private MaskFieldDescriptor descriptor(Class<?> clazz, String fieldName, boolean enabled,
                                           String idField, String versionField,
                                           List<MaskProtectBinding> bindings) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return new MaskFieldDescriptor(field, MaskStrategy.ALL, "", "***", "demo:view",
                    true, true, false, false, 0, enabled,
                    "", "", idField, versionField, 300, "",
                    MaskProtectMode.RESTORE_ORIGINAL, bindings);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private MaskFieldDescriptor dbDescriptor(Class<?> clazz, String fieldName,
                                             List<MaskProtectBinding> bindings) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return new MaskFieldDescriptor(field, MaskStrategy.ALL, "", "***", "demo:view",
                    true, true, false, false, 0, true,
                    "", "", "id", "", MaskProtectVerifyMode.DB_VALUE_COMPARE,
                    "sample_table", "id", fieldName, "is_deleted",
                    300, "", MaskProtectMode.RESTORE_ORIGINAL, bindings);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static MaskProtectBinding binding(Class<?> paramClass, String fieldName,
                                              String recordIdField, String versionField) {
        MaskProtectBinding binding = new MaskProtectBinding();
        binding.setParamClassPath(paramClass.getName());
        binding.setParamFieldName(fieldName);
        binding.setParamRecordIdField(recordIdField);
        binding.setParamVersionField(versionField);
        return binding;
    }

    private static List<MaskProtectBinding> bindings(MaskProtectBinding... bindings) {
        List<MaskProtectBinding> result = new ArrayList<>();
        if (bindings != null) {
            Collections.addAll(result, bindings);
        }
        return result;
    }

    private static class FixedPermissionResolver implements MaskPermissionResolver {
        private Set<String> permissions = Collections.emptySet();

        @Override
        public Set<String> resolve(LoginUser user) {
            return permissions;
        }
    }

    public static class SampleViewDTO {
        public String id;
        public Integer version;
        public String amount;
        public String note;

        public SampleViewDTO(String id, Integer version, String amount, String note) {
            this.id = id;
            this.version = version;
            this.amount = amount;
            this.note = note;
        }
    }

    public static class SampleUpdateDTO {
        public String id;
        public Integer version;
        public String amount;
        public String note;

        public SampleUpdateDTO(String id, Integer version, String amount, String note) {
            this.id = id;
            this.version = version;
            this.amount = amount;
            this.note = note;
        }
    }

    public static class SampleNoIdDTO {
        public Integer version;
        public String amount;

        public SampleNoIdDTO(Integer version, String amount) {
            this.version = version;
            this.amount = amount;
        }
    }

    public static class SampleUpdateNoVersionDTO {
        public String id;
        public String amount;

        public SampleUpdateNoVersionDTO(String id, String amount) {
            this.id = id;
            this.amount = amount;
        }
    }

    public static class RenamedUpdateDTO {
        public String recordNo;
        public Integer revision;
        public String amountInput;

        public RenamedUpdateDTO(String recordNo, Integer revision, String amountInput) {
            this.recordNo = recordNo;
            this.revision = revision;
            this.amountInput = amountInput;
        }
    }

    public static class BigDecimalViewDTO {
        public String id;
        public Integer version;
        public BigDecimal amount;

        public BigDecimalViewDTO(String id, Integer version, BigDecimal amount) {
            this.id = id;
            this.version = version;
            this.amount = amount;
        }
    }

    public static class BigDecimalUpdateDTO {
        public String id;
        public Integer version;
        public BigDecimal amount;

        public BigDecimalUpdateDTO(String id, Integer version, BigDecimal amount) {
            this.id = id;
            this.version = version;
            this.amount = amount;
        }
    }

    private static class MemoryRedisson {
        private final ConcurrentMap<String, Object> values = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, Long> expireAt = new ConcurrentHashMap<>();

        RedissonClient client() {
            RedissonClient client = mock(RedissonClient.class);
            when(client.getBucket(ArgumentMatchers.anyString())).thenAnswer(invocation -> {
                String key = invocation.getArgument(0);
                return bucket(key);
            });
            return client;
        }

        int size() {
            return values.size();
        }

        void expireAll() {
            long expired = System.currentTimeMillis() - 1;
            for (String key : values.keySet()) {
                expireAt.put(key, expired);
            }
        }

        private RBucket<String> bucket(String key) {
            RBucket<String> bucket = mock(RBucket.class);
            when(bucket.get()).thenAnswer(invocation -> {
                Long expireTime = expireAt.get(key);
                if (expireTime != null && expireTime < System.currentTimeMillis()) {
                    values.remove(key);
                    expireAt.remove(key);
                    return null;
                }
                return (String) values.get(key);
            });
            org.mockito.Mockito.doAnswer(invocation -> {
                values.put(key, invocation.getArgument(0));
                expireAt.remove(key);
                return null;
            }).when(bucket).set(ArgumentMatchers.anyString());
            org.mockito.Mockito.doAnswer(invocation -> {
                values.put(key, invocation.getArgument(0));
                Long ttl = invocation.getArgument(1);
                TimeUnit unit = invocation.getArgument(2);
                expireAt.put(key, System.currentTimeMillis() + unit.toMillis(ttl));
                return null;
            }).when(bucket).set(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class));
            return bucket;
        }
    }

    private static class MemoryCurrentValueReader implements MaskProtectCurrentValueReader {
        private final Map<String, Map<String, MaskProtectCurrentValue>> values = new HashMap<>();

        void put(CfgMaskFieldSnapshotEntry entry, String recordId, String value, boolean nullValue) {
            values.computeIfAbsent(key(entry), k -> new HashMap<>())
                    .put(recordId, new MaskProtectCurrentValue(value, nullValue));
        }

        @Override
        public Map<String, MaskProtectCurrentValue> lockAndRead(CfgMaskFieldSnapshotEntry entry,
                                                                List<String> recordIds) {
            Map<String, MaskProtectCurrentValue> source = values.getOrDefault(key(entry), Collections.emptyMap());
            Map<String, MaskProtectCurrentValue> result = new HashMap<>();
            for (String recordId : recordIds) {
                if (source.containsKey(recordId)) {
                    result.put(recordId, source.get(recordId));
                }
            }
            return result;
        }

        private String key(CfgMaskFieldSnapshotEntry entry) {
            return entry.getProtectTableName() + "|" + entry.getProtectRecordIdColumn()
                    + "|" + entry.getProtectValueColumn();
        }
    }
}
