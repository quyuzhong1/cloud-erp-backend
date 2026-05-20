package com.common.business.mask.protect;

import com.common.business.mask.MaskPermissionResolver;
import com.common.business.mask.cache.CfgMaskFieldLocalCache;
import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ConvertUtils;
import com.common.business.vo.LoginUser;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 写入参脱敏回显保护处理器。
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class MaskProtectInputProcessor {

    private static final String[] FRAMEWORK_PREFIXES = {
            "java.",
            "javax.",
            "jakarta.",
            "sun.",
            "com.sun.",
            "org.springframework.",
            "org.apache.catalina.",
            "org.eclipse.jetty.",
            "io.undertow.",
            "io.netty.",
            "feign.",
            "lombok."
    };

    @Autowired(required = false)
    private CfgMaskFieldLocalCache configCache;

    @Autowired(required = false)
    private MaskPermissionResolver permissionResolver;

    @Autowired(required = false)
    private MaskProtectCurrentValueReader currentValueReader;

    public MaskProtectInputProcessor() {
    }

    MaskProtectInputProcessor(CfgMaskFieldLocalCache configCache, MaskPermissionResolver permissionResolver,
                              MaskProtectCurrentValueReader currentValueReader) {
        this.configCache = configCache;
        this.permissionResolver = permissionResolver;
        this.currentValueReader = currentValueReader;
    }

    public List<FieldRestorePlan> collect(Object[] args) {
        if (args == null || args.length == 0 || configCache == null) {
            return Collections.emptyList();
        }
        LoginUser user = UserContext.getLoginUser();
        Set<String> permissionSet = toPermissionSet(user);
        List<FieldRestorePlan> plans = new ArrayList<>();
        IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>(64);
        for (Object arg : args) {
            collectPlans(arg, user, permissionSet, seen, plans);
        }
        if (plans.isEmpty()) {
            return Collections.emptyList();
        }
        plans.sort(Comparator
                .comparing(FieldRestorePlan::lockGroupKey)
                .thenComparing(FieldRestorePlan::getRecordId)
                .thenComparing(FieldRestorePlan::getFieldName));
        return plans;
    }

    public void process(Object[] args) {
        List<FieldRestorePlan> plans = collect(args);
        validateAndApply(plans);
    }

    public boolean requiresDbCompare(List<FieldRestorePlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return false;
        }
        for (FieldRestorePlan plan : plans) {
            if (plan.isDbRestore()) {
                return true;
            }
        }
        return false;
    }

    public void validateAndApply(List<FieldRestorePlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return;
        }
        validateDbComparePlans(plans);
        for (FieldRestorePlan plan : plans) {
            plan.apply();
        }
    }

    private void validateDbComparePlans(List<FieldRestorePlan> plans) {
        Map<DbCompareGroup, List<FieldRestorePlan>> groups = new LinkedHashMap<>();
        for (FieldRestorePlan plan : plans) {
            if (!plan.isDbRestore()) {
                continue;
            }
            if (currentValueReader == null) {
                throw new MaskProtectException();
            }
            groups.computeIfAbsent(new DbCompareGroup(plan.getEntry()), k -> new ArrayList<>()).add(plan);
        }
        for (Map.Entry<DbCompareGroup, List<FieldRestorePlan>> item : groups.entrySet()) {
            List<String> ids = new ArrayList<>();
            for (FieldRestorePlan plan : item.getValue()) {
                ids.add(plan.getRecordId());
            }
            Map<String, MaskProtectCurrentValue> values =
                    currentValueReader.lockAndRead(item.getValue().get(0).getEntry(), ids);
            if (values == null) {
                throw new MaskProtectException();
            }
            for (FieldRestorePlan plan : item.getValue()) {
                MaskProtectCurrentValue current = values.get(plan.getRecordId());
                if (current == null) {
                    throw new MaskProtectException();
                }
                try {
                    plan.setValue(current.isNullValue()
                            ? null : MaskProtectReflectionUtils.convertString(current.getValue(), plan.getFieldType()));
                } catch (Throwable e) {
                    log.warn("mask protect db current value convert failed, class={}, field={}, msg={}",
                            plan.getTargetClassName(), plan.getFieldName(), e.getMessage());
                    throw new MaskProtectException();
                }
            }
        }
        for (FieldRestorePlan plan : plans) {
            plan.validateAssignableValue();
        }
    }

    private void collectPlans(Object obj, LoginUser user, Set<String> permissionSet,
                              IdentityHashMap<Object, Boolean> seen, List<FieldRestorePlan> plans) {
        if (obj == null || isSimpleValue(obj.getClass())) {
            return;
        }
        if (seen.put(obj, Boolean.TRUE) != null) {
            return;
        }
        if (obj instanceof Collection) {
            for (Object item : (Collection<?>) obj) {
                collectPlans(item, user, permissionSet, seen, plans);
            }
            return;
        }
        if (obj instanceof Map) {
            for (Object value : ((Map<?, ?>) obj).values()) {
                collectPlans(value, user, permissionSet, seen, plans);
            }
            return;
        }
        if (obj.getClass().isArray()) {
            if (!obj.getClass().getComponentType().isPrimitive()) {
                Object[] arr = (Object[]) obj;
                for (Object item : arr) {
                    collectPlans(item, user, permissionSet, seen, plans);
                }
            }
            return;
        }
        if (isFrameworkType(obj.getClass())) {
            return;
        }
        collectPojoPlans(obj, user, permissionSet, seen, plans);
    }

    private void collectPojoPlans(Object pojo, LoginUser user, Set<String> permissionSet,
                                  IdentityHashMap<Object, Boolean> seen, List<FieldRestorePlan> plans) {
        Field[] fields = ConvertUtils.getAllFields(pojo.getClass());
        if (fields == null || fields.length == 0) {
            return;
        }
        for (Field field : fields) {
            if (isSkippable(field)) {
                continue;
            }
            try {
                field.setAccessible(true);
            } catch (Throwable e) {
                continue;
            }
            Object value;
            try {
                value = field.get(pojo);
            } catch (IllegalAccessException e) {
                continue;
            }
            List<CfgMaskFieldSnapshotEntry> rules =
                    configCache.getValueProtectRules(pojo.getClass().getName(), field.getName());
            if (!rules.isEmpty()) {
                collectFieldPlan(pojo, field, value, rules, user, permissionSet, plans);
            }
            if (value != null && shouldRecurse(field.getType(), value)) {
                collectPlans(value, user, permissionSet, seen, plans);
            }
        }
    }

    private void collectFieldPlan(Object pojo, Field field, Object value, List<CfgMaskFieldSnapshotEntry> rules,
                                  LoginUser user, Set<String> permissionSet, List<FieldRestorePlan> plans) {
        for (CfgMaskFieldSnapshotEntry entry : rules) {
            if (hasPlainPermission(user, permissionSet, entry.getPermission())) {
                continue;
            }
            MaskProtectMode protectMode = entry.getProtectMode() == null
                    ? MaskProtectMode.REJECT : entry.getProtectMode();
            if (protectMode == MaskProtectMode.REJECT) {
                log.debug("mask protect reject update, class={}, field={}",
                        pojo.getClass().getName(), field.getName());
                throw new MaskProtectException();
            }
            if (protectMode == MaskProtectMode.SET_NULL) {
                if (field.getType().isPrimitive() || hasNotNullConstraint(field)) {
                    throw new MaskProtectException();
                }
                plans.add(new FieldRestorePlan(pojo, field, null, entry, "", false));
                return;
            }
            if (protectMode != MaskProtectMode.RESTORE_ORIGINAL) {
                throw new MaskProtectException();
            }
            MaskProtectBinding binding = configCache.findProtectBinding(entry, pojo.getClass().getName(),
                    field.getName());
            if (binding == null) {
                throw new MaskProtectException();
            }
            String recordId = stringField(pojo, binding.getParamRecordIdField());
            if (StringUtils.isBlank(recordId)) {
                throw new MaskProtectException();
            }
            plans.add(new FieldRestorePlan(pojo, field, null, entry, recordId, true));
            return;
        }
    }

    private Set<String> toPermissionSet(LoginUser user) {
        if (permissionResolver != null) {
            try {
                Set<String> resolved = permissionResolver.resolve(user);
                return resolved == null ? Collections.emptySet() : resolved;
            } catch (Throwable e) {
                log.warn("MaskProtect permissionResolver threw, fallback to LoginUser.permissionList, msg={}",
                        e.getMessage());
            }
        }
        if (user == null || user.getPermissionList() == null || user.getPermissionList().isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(user.getPermissionList());
    }

    private boolean hasPlainPermission(LoginUser user, Set<String> permissionSet, String permission) {
        if (isSuperAdmin(user)) {
            return true;
        }
        return StringUtils.isNotBlank(permission)
                && permissionSet != null
                && !permissionSet.isEmpty()
                && permissionSet.contains(permission);
    }

    private boolean isSuperAdmin(LoginUser user) {
        if (user == null) {
            return false;
        }
        if (permissionResolver != null) {
            try {
                return permissionResolver.isSuperAdmin(user);
            } catch (Throwable e) {
                log.warn("MaskProtect permissionResolver.isSuperAdmin threw, fallback to LoginUser.isSupper, msg={}",
                        e.getMessage());
            }
        }
        return Boolean.TRUE.equals(user.getIsSupper());
    }

    private String stringField(Object pojo, String fieldName) {
        return MaskProtectReflectionUtils.normalizeValue(
                MaskProtectReflectionUtils.getFieldValue(pojo, fieldName));
    }

    private boolean shouldRecurse(Class<?> type, Object value) {
        if (value == null || isSimpleValue(type)) {
            return false;
        }
        if (value instanceof Collection || value instanceof Map || value.getClass().isArray()) {
            return true;
        }
        return !isFrameworkType(type);
    }

    private boolean isSimpleValue(Class<?> type) {
        return type == null
                || type.isPrimitive()
                || type.isEnum()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Character.class
                || java.util.Date.class.isAssignableFrom(type)
                || type.getName().startsWith("java.time.");
    }

    private boolean isFrameworkType(Class<?> type) {
        if (type == null) {
            return true;
        }
        String name = type.getName();
        for (String prefix : FRAMEWORK_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSkippable(Field field) {
        if (field == null) {
            return true;
        }
        int mod = field.getModifiers();
        return Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isTransient(mod);
    }

    private boolean hasNotNullConstraint(Field field) {
        if (field == null) {
            return false;
        }
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation == null || annotation.annotationType() == null) {
                continue;
            }
            String name = annotation.annotationType().getSimpleName();
            if ("NotNull".equals(name) || "NotBlank".equals(name) || "NotEmpty".equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static class FieldRestorePlan {
        private final Object target;
        private final Field field;
        private Object value;
        private final CfgMaskFieldSnapshotEntry entry;
        private final String recordId;
        private final boolean dbRestore;

        private FieldRestorePlan(Object target, Field field, Object value, CfgMaskFieldSnapshotEntry entry,
                                 String recordId, boolean dbRestore) {
            this.target = target;
            this.field = field;
            this.value = value;
            this.entry = entry;
            this.recordId = recordId;
            this.dbRestore = dbRestore;
        }

        private void apply() {
            try {
                field.set(target, value);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new MaskProtectException();
            }
        }

        private void setValue(Object value) {
            this.value = value;
        }

        private void validateAssignableValue() {
            if (value == null && field.getType().isPrimitive()) {
                throw new MaskProtectException();
            }
        }

        private CfgMaskFieldSnapshotEntry getEntry() {
            return entry;
        }

        private Class<?> getFieldType() {
            return field.getType();
        }

        private String getTargetClassName() {
            return target == null ? "" : target.getClass().getName();
        }

        private String getRecordId() {
            return recordId;
        }

        private String getFieldName() {
            return field.getName();
        }

        private boolean isDbRestore() {
            return dbRestore;
        }

        private String lockGroupKey() {
            return StringUtils.defaultString(entry.getProtectTableName())
                    + "|" + StringUtils.defaultString(entry.getProtectValueColumn())
                    + "|" + StringUtils.defaultString(entry.getProtectRecordIdColumn())
                    + "|" + StringUtils.defaultString(entry.getProtectDeletedColumn());
        }
    }

    private static class DbCompareGroup {
        private final String tableName;
        private final String recordIdColumn;
        private final String valueColumn;
        private final String deletedColumn;

        private DbCompareGroup(CfgMaskFieldSnapshotEntry entry) {
            this.tableName = StringUtils.defaultString(entry.getProtectTableName());
            this.recordIdColumn = StringUtils.defaultString(entry.getProtectRecordIdColumn());
            this.valueColumn = StringUtils.defaultString(entry.getProtectValueColumn());
            this.deletedColumn = StringUtils.defaultString(entry.getProtectDeletedColumn());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DbCompareGroup)) {
                return false;
            }
            DbCompareGroup other = (DbCompareGroup) obj;
            return StringUtils.equals(tableName, other.tableName)
                    && StringUtils.equals(recordIdColumn, other.recordIdColumn)
                    && StringUtils.equals(valueColumn, other.valueColumn)
                    && StringUtils.equals(deletedColumn, other.deletedColumn);
        }

        @Override
        public int hashCode() {
            int result = tableName.hashCode();
            result = 31 * result + recordIdColumn.hashCode();
            result = 31 * result + valueColumn.hashCode();
            result = 31 * result + deletedColumn.hashCode();
            return result;
        }
    }
}
