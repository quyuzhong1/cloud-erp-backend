package com.common.business.mask;

/**
 * 字段脱敏策略枚举
 *
 * <p>开发期通过 {@link Mask} 注解的 {@code strategy} 属性指定，
 * 运维期可通过 {@code cfg_mask_field.strategy} 列覆盖。</p>
 *
 * <p>每个策略对应一个 {@link MaskHandler} Bean 实现，引擎按 strategy 分发。
 * 业务方可通过实现 {@link MaskHandler} 接口注册自定义策略。</p>
 *
 * @author cloud-erp
 */
public enum MaskStrategy {

    /**
     * 默认占位：表示"用配置表里的策略"。
     * 字段注解写 {@code @Mask}（不指定 strategy）时取该值，
     * 仅在配置表存在记录且 strategy != AUTO_FROM_CONFIG 时生效，
     * 否则保持原值不脱。
     */
    AUTO_FROM_CONFIG,

    /**
     * 显式不脱敏：与 {@link #AUTO_FROM_CONFIG} 不同，NONE 是"明确表达不脱"的强语义。
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>{@code cfg_mask_field.strategy='NONE'} 临时禁用某字段的脱敏（保留配置行便于审计与后续启用）</li>
     *   <li>未来扩展点（{@link com.common.business.mask.MaskPermissionEvaluator}）显式表达"该主体看明文"</li>
     * </ul>
     */
    NONE,

    /**
     * 自动按"值的形态"识别脱敏：跑 PII 引擎扫描，覆盖
     * 手机号 / 邮箱 / URL / IPv4（sensitive-word DFA）+ 身份证 / 银行卡（自定义正则 + Luhn 校验）+
     * 业务自定义关键词（{@code cfg_mask_word.word_type=0}）+ 白名单豁免（{@code word_type=1}）。
     *
     * <p>适用：备注 / 标题 / content 等"自由文本"字段，业务侧无法预先穷举字段语义时兜底使用。</p>
     */
    AUTO,

    /**
     * 手机号：保留前 3 后 4，中间 4 位 *。
     * 例：13812345678 → 138****5678
     */
    PHONE,

    /**
     * 身份证号：18 位保留前 3 后 4，15 位保留前 3 后 4，中间全 *。
     * 例：110101199001011234 → 110***********1234
     */
    ID_CARD,

    /**
     * 银行卡号：保留前 4 后 4，中间全 *。
     * 例：6222024200012345678 → 6222***********5678
     */
    BANK_CARD,

    /**
     * 邮箱：本地部分保留首字符 + 域名原样。
     * 例：jack@xxx.com → j***@xxx.com
     */
    EMAIL,

    /**
     * 中文姓名：≥ 2 字保留姓 + 名首字 *，单字保留首 + *。
     * 例：张三 → 张*；李四光 → 李*光
     */
    NAME,

    /**
     * 地址：保留前 6 个字符（一般是省市），其余 ****。
     * 例：北京市朝阳区建国路100号 → 北京市朝阳区****
     */
    ADDRESS,

    /**
     * 金额：整体替换为 ***；适用于 BigDecimal/Number/String 字段。
     */
    AMOUNT,

    /**
     * 密码：整体 ******（无论原长度）。
     */
    PASSWORD,

    /**
     * 全部遮蔽：整体替换为 ***。
     */
    ALL,

    /**
     * 自定义：使用 {@link Mask#regex()} 和 {@link Mask#replacement()}。
     */
    CUSTOM,
}
