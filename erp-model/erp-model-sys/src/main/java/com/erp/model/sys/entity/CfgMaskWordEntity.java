package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 脱敏词典表
 *
 * <p>{@link com.erp.model.sys.entity.CfgMaskFieldEntity} 解决"哪些字段需要脱敏"，
 * 本表解决"哪些值需要脱敏"。两表互补，配合 {@code MaskStrategy.AUTO} 策略使用：</p>
 * <ul>
 *   <li>{@code word_type=0} 黑名单：业务自定义敏感词，命中即整体替换；</li>
 *   <li>{@code word_type=1} 白名单：豁免（用于"长得像 PII 但其实不是"的特殊串）。</li>
 * </ul>
 *
 * <p>(word_type, word) 唯一索引；变更后删除 Redis 全量缓存，业务节点按 cache-aside
 * 回源最新词典并同步 {@code SensitiveWordBs} 引擎。</p>
 *
 * @author cloud-erp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "cfg_mask_word")
public class CfgMaskWordEntity extends BaseEntity<CfgMaskWordEntity> {

    private static final long serialVersionUID = 1L;

    /** 黑名单 */
    public static final int WORD_TYPE_DENY = 0;

    /** 白名单 */
    public static final int WORD_TYPE_ALLOW = 1;

    /**
     * 词类型：0=黑名单（敏感词），1=白名单（豁免）
     */
    @TableField("word_type")
    private Integer wordType;

    /**
     * 分类标签，仅业务分组用，引擎不解析
     * 例：phone / idcard / internal_code / partner_code
     */
    @TableField("category")
    private String category;

    /**
     * 词内容；区分大小写，按 sensitive-word DFA 整体匹配
     */
    @TableField("word")
    private String word;

    /**
     * 是否禁用：true 时不灌入引擎
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 词典灌入顺序，越小越先处理
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    public static final String WORD_TYPE = "word_type";
    public static final String CATEGORY = "category";
    public static final String WORD = "word";
    public static final String DISABLED = "disabled";
    public static final String SORT = "sort";
}
