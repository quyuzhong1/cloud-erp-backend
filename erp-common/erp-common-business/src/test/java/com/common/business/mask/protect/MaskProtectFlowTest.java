package com.common.business.mask.protect;

import com.common.business.mask.MaskPermissionResolver;
import com.common.business.mask.MaskStrategy;
import com.common.business.mask.cache.CfgMaskFieldLocalCache;
import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 脱敏值回显保护核心流程单元测试。
 *
 * @author cloud-erp
 */
public class MaskProtectFlowTest {

    private FixedPermissionResolver permissionResolver;

    @Before
    public void setUp() {
        permissionResolver = new FixedPermissionResolver();
        LoginUser user = new LoginUser();
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
        SampleUpdateDTO dto = new SampleUpdateDTO("1", "200.00", "changed");

        processor(reader(), disabledEntry("amount")).process(new Object[]{dto});

        assertEquals("200.00", dto.amount);
    }

    @Test
    public void maskedSubmitShouldRestoreDbCurrentValue() {
        CfgMaskFieldSnapshotEntry entry = entry(SampleUpdateDTO.class, "amount", "id");
        MemoryCurrentValueReader reader = reader();
        reader.put(entry, "1", "101.77", false);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", "***", "changed");

        processor(reader, entry).process(new Object[]{dto});

        assertEquals("101.77", dto.amount);
        assertEquals("changed", dto.note);
    }

    @Test
    public void realNewValueShouldRestoreDbCurrentValueWhenNoPermission() {
        CfgMaskFieldSnapshotEntry entry = entry(SampleUpdateDTO.class, "amount", "id");
        MemoryCurrentValueReader reader = reader();
        reader.put(entry, "1", "101.77", false);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", "200.00", "changed");

        processor(reader, entry).process(new Object[]{dto});

        assertEquals("101.77", dto.amount);
    }

    @Test
    public void differentWriteDtoFieldShouldRestoreByExplicitMapping() {
        CfgMaskFieldSnapshotEntry entry = entry(RenamedUpdateDTO.class, "amountInput", "recordNo");
        MemoryCurrentValueReader reader = reader();
        reader.put(entry, "1", "101.77", false);
        RenamedUpdateDTO dto = new RenamedUpdateDTO("1", "***");

        processor(reader, entry).process(new Object[]{dto});

        assertEquals("101.77", dto.amountInput);
    }

    @Test
    public void oneViewFieldShouldProtectMultipleUpdateDtos() {
        CfgMaskFieldSnapshotEntry entry = entry("amount");
        entry.setProtectParamBindings(bindings(
                binding(SampleUpdateDTO.class, "amount", "id"),
                binding(RenamedUpdateDTO.class, "amountInput", "recordNo")));
        MemoryCurrentValueReader reader = reader();
        reader.put(entry, "1", "101.77", false);
        SampleUpdateDTO dto1 = new SampleUpdateDTO("1", "***", "changed");
        RenamedUpdateDTO dto2 = new RenamedUpdateDTO("1", "***");

        MaskProtectInputProcessor processor = processor(reader, entry);
        processor.process(new Object[]{dto1});
        processor.process(new Object[]{dto2});

        assertEquals("101.77", dto1.amount);
        assertEquals("101.77", dto2.amountInput);
    }

    @Test
    public void setNullModeShouldClearFieldWhenNoPermission() {
        CfgMaskFieldSnapshotEntry entry = entry(SampleUpdateDTO.class, "amount", "id");
        entry.setProtectMode(MaskProtectMode.SET_NULL);
        SampleUpdateDTO dto = new SampleUpdateDTO("", "200.00", "changed");

        processor(null, entry).process(new Object[]{dto});

        assertNull(dto.amount);
    }

    @Test(expected = MaskProtectException.class)
    public void rejectModeShouldRejectWhenNoPermission() {
        CfgMaskFieldSnapshotEntry entry = entry(SampleUpdateDTO.class, "amount", "id");
        entry.setProtectMode(MaskProtectMode.REJECT);

        processor(null, entry).process(new Object[]{new SampleUpdateDTO("1", "200.00", "changed")});
    }

    @Test(expected = MaskProtectException.class)
    public void dbCurrentMissingShouldReject() {
        CfgMaskFieldSnapshotEntry entry = entry(SampleUpdateDTO.class, "amount", "id");

        processor(reader(), entry).process(new Object[]{new SampleUpdateDTO("1", "***", "changed")});
    }

    @Test(expected = MaskProtectException.class)
    public void missingRecordIdShouldRejectForRestore() {
        CfgMaskFieldSnapshotEntry entry = entry(SampleNoIdDTO.class, "amount", "id");

        processor(reader(), entry).process(new Object[]{new SampleNoIdDTO("***")});
    }

    @Test
    public void plainPermissionShouldSkipProtect() {
        permissionResolver.permissions = Collections.singleton("demo:view");
        SampleUpdateDTO dto = new SampleUpdateDTO("1", "200.00", "changed");

        processor(reader(), entry(SampleUpdateDTO.class, "amount", "id")).process(new Object[]{dto});

        assertEquals("200.00", dto.amount);
    }

    @Test
    public void multipleFieldsShouldRestoreTogether() {
        CfgMaskFieldSnapshotEntry amount = entry(SampleUpdateDTO.class, "amount", "id");
        CfgMaskFieldSnapshotEntry note = entry(SampleUpdateDTO.class, "note", "id");
        MemoryCurrentValueReader reader = reader();
        reader.put(amount, "1", "101.77", false);
        reader.put(note, "1", "secret-note", false);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", "***", "已隐藏");

        processor(reader, amount, note).process(new Object[]{dto});

        assertEquals("101.77", dto.amount);
        assertEquals("secret-note", dto.note);
    }

    @Test
    public void oneFieldFailureShouldNotPartiallyRestore() {
        CfgMaskFieldSnapshotEntry amount = entry(SampleUpdateDTO.class, "amount", "id");
        CfgMaskFieldSnapshotEntry note = entry(SampleUpdateDTO.class, "note", "id");
        MemoryCurrentValueReader reader = reader();
        reader.put(amount, "1", "101.77", false);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", "***", "已隐藏");

        try {
            processor(reader, amount, note).process(new Object[]{dto});
        } catch (MaskProtectException e) {
            assertEquals("***", dto.amount);
            assertEquals("已隐藏", dto.note);
            return;
        }
        throw new AssertionError("should reject");
    }

    @Test
    public void bigDecimalFieldShouldRestoreNumber() {
        CfgMaskFieldSnapshotEntry entry = entry(BigDecimalUpdateDTO.class, "amount", "id");
        entry.setStrategy(MaskStrategy.AMOUNT);
        MemoryCurrentValueReader reader = reader();
        reader.put(entry, "1", "101.77", false);
        BigDecimalUpdateDTO dto = new BigDecimalUpdateDTO("1", BigDecimal.ZERO);

        processor(reader, entry).process(new Object[]{dto});

        assertEquals(new BigDecimal("101.77"), dto.amount);
    }

    @Test
    public void dbCurrentNullShouldRestoreNull() {
        CfgMaskFieldSnapshotEntry entry = entry(SampleUpdateDTO.class, "amount", "id");
        MemoryCurrentValueReader reader = reader();
        reader.put(entry, "1", null, true);
        SampleUpdateDTO dto = new SampleUpdateDTO("1", "***", "changed");

        processor(reader, entry).process(new Object[]{dto});

        assertNull(dto.amount);
    }

    @Test(expected = MaskProtectException.class)
    public void dbCurrentConvertFailureShouldReject() {
        CfgMaskFieldSnapshotEntry entry = entry(BigDecimalUpdateDTO.class, "amount", "id");
        MemoryCurrentValueReader reader = reader();
        reader.put(entry, "1", "not-number", false);

        processor(reader, entry).process(new Object[]{new BigDecimalUpdateDTO("1", BigDecimal.ZERO)});
    }

    @Test
    public void batchShouldValidateBeforeApply() {
        CfgMaskFieldSnapshotEntry entry = entry(SampleUpdateDTO.class, "amount", "id");
        MemoryCurrentValueReader reader = reader();
        reader.put(entry, "1", "101.77", false);
        SampleUpdateDTO dto1 = new SampleUpdateDTO("1", "***", "a");
        SampleUpdateDTO dto2 = new SampleUpdateDTO("2", "***", "b");

        try {
            processor(reader, entry).process(new Object[]{Arrays.asList(dto1, dto2)});
        } catch (MaskProtectException e) {
            assertEquals("***", dto1.amount);
            assertEquals("***", dto2.amount);
            return;
        }
        throw new AssertionError("should reject");
    }

    private MaskProtectInputProcessor processor(MaskProtectCurrentValueReader reader,
                                                CfgMaskFieldSnapshotEntry... entries) {
        CfgMaskFieldLocalCache cache = mock(CfgMaskFieldLocalCache.class);
        when(cache.getValueProtectRules(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());
        for (CfgMaskFieldSnapshotEntry entry : entries) {
            if (entry.isValueProtectEnabled() && entry.getProtectParamBindings() != null) {
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
        return new MaskProtectInputProcessor(cache, permissionResolver, reader);
    }

    private MemoryCurrentValueReader reader() {
        return new MemoryCurrentValueReader();
    }

    private CfgMaskFieldSnapshotEntry disabledEntry(String fieldName) {
        CfgMaskFieldSnapshotEntry entry = entry(SampleUpdateDTO.class, fieldName, "id");
        entry.setValueProtectEnabled(false);
        return entry;
    }

    private CfgMaskFieldSnapshotEntry entry(String fieldName) {
        return entry(SampleUpdateDTO.class, fieldName, "id");
    }

    private CfgMaskFieldSnapshotEntry entry(Class<?> writeClass, String writeFieldName, String recordIdField) {
        CfgMaskFieldSnapshotEntry entry = new CfgMaskFieldSnapshotEntry();
        entry.setClassPath("read.vo.SampleViewDTO");
        entry.setFieldName(writeFieldName);
        entry.setStrategy(MaskStrategy.ALL);
        entry.setReplacement("***");
        entry.setPermission("demo:view");
        entry.setValueProtectEnabled(true);
        entry.setProtectTableName("sample_table");
        entry.setProtectRecordIdColumn("id");
        entry.setProtectValueColumn(writeFieldName);
        entry.setProtectDeletedColumn("is_deleted");
        entry.setProtectParamBindings(Collections.singletonList(
                binding(writeClass, writeFieldName, recordIdField)));
        entry.setProtectMode(MaskProtectMode.RESTORE_ORIGINAL);
        return entry;
    }

    private static MaskProtectBinding binding(Class<?> paramClass, String fieldName, String recordIdField) {
        MaskProtectBinding binding = new MaskProtectBinding();
        binding.setParamClassPath(paramClass.getName());
        binding.setParamFieldName(fieldName);
        binding.setParamRecordIdField(recordIdField);
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

    public static class SampleUpdateDTO {
        public String id;
        public String amount;
        public String note;

        public SampleUpdateDTO(String id, String amount, String note) {
            this.id = id;
            this.amount = amount;
            this.note = note;
        }
    }

    public static class SampleNoIdDTO {
        public String amount;

        public SampleNoIdDTO(String amount) {
            this.amount = amount;
        }
    }

    public static class RenamedUpdateDTO {
        public String recordNo;
        public String amountInput;

        public RenamedUpdateDTO(String recordNo, String amountInput) {
            this.recordNo = recordNo;
            this.amountInput = amountInput;
        }
    }

    public static class BigDecimalUpdateDTO {
        public String id;
        public BigDecimal amount;

        public BigDecimalUpdateDTO(String id, BigDecimal amount) {
            this.id = id;
            this.amount = amount;
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
