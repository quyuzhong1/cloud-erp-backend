package com.common.business.mask;

/**
 * 脱敏上下文，承载单次字段处理所需的所有信息
 *
 * <p>由 {@link com.common.business.mask.core.MaskCore} 在调用 {@link MaskHandler#handle}
 * 前构造，{@link MaskHandler} 实现仅消费、不修改。</p>
 *
 * @author cloud-erp
 */
public class MaskContext {

    /** 当前字段所在的 POJO 实例（可能用于跨字段联动） */
    private final Object owner;

    /** 当前字段的 Java 字段名 */
    private final String fieldName;

    /** 当前字段所属类的全限定名 */
    private final String classPath;

    /** 自定义正则（仅 CUSTOM 策略） */
    private final String regex;

    /** 自定义替换串（CUSTOM 策略），其它策略的兜底占位也会取这个 */
    private final String replacement;

    /** 字段为 null/empty 时是否保持原样 */
    private final boolean keepEmpty;

    private MaskContext(Builder b) {
        this.owner = b.owner;
        this.fieldName = b.fieldName;
        this.classPath = b.classPath;
        this.regex = b.regex;
        this.replacement = b.replacement;
        this.keepEmpty = b.keepEmpty;
    }

    public Object getOwner() {
        return owner;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getClassPath() {
        return classPath;
    }

    public String getRegex() {
        return regex;
    }

    public String getReplacement() {
        return replacement;
    }

    public boolean isKeepEmpty() {
        return keepEmpty;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Object owner;
        private String fieldName;
        private String classPath;
        private String regex = "";
        private String replacement = "***";
        private boolean keepEmpty = true;

        public Builder owner(Object owner) {
            this.owner = owner;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder classPath(String classPath) {
            this.classPath = classPath;
            return this;
        }

        public Builder regex(String regex) {
            this.regex = regex == null ? "" : regex;
            return this;
        }

        public Builder replacement(String replacement) {
            this.replacement = replacement == null ? "***" : replacement;
            return this;
        }

        public Builder keepEmpty(boolean keepEmpty) {
            this.keepEmpty = keepEmpty;
            return this;
        }

        public MaskContext build() {
            return new MaskContext(this);
        }
    }
}
