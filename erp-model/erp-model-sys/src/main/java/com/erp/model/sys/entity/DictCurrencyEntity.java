package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 币种字典表
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_currency")
@EqualsAndHashCode
public class DictCurrencyEntity extends BaseEntity<DictCurrencyEntity> {

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 英文名
     */
    @TableField("symbol")
    private String symbol;

    /**
     * 序号
     */
    @TableField("index")
    private Integer index;

    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 金蝶编码
     */
    @TableField("kingdee_code")
    private String kingdeeCode;
    /**
     * 数字代码
     */
    @TableField("currency_num")
    private String currencyNum;


    @Override
    public Serializable pkVal() {
        return null;
    }

}
