package com.erp.model.wms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 发货单箱规信息
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_carton")
public class FirstMileCartonEntity extends BaseEntity<FirstMileCartonEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 箱规编号
    */
    @TableField("box_spec_no")
    private Integer boxSpecNo;
    /**
    * 包装重量
    */
    @TableField("package_weight")
    private BigDecimal packageWeight;
    /**
    * 箱子尺寸（长）
    */
    @TableField("box_length")
    private BigDecimal boxLength;
    /**
    * 箱子尺寸（宽）
    */
    @TableField("box_width")
    private BigDecimal boxWidth;
    /**
    * 箱子尺寸（高）
    */
    @TableField("box_height")
    private BigDecimal boxHeight;
    /**
    * 箱数
    */
    @TableField("box_qty")
    private Integer boxQty;


    public static final String MAIN_ID = "main_id";

    public static final String BOX_SPEC_NO = "box_spec_no";

    public static final String PACKAGE_WEIGHT = "package_weight";

    public static final String BOX_LENGTH = "box_length";

    public static final String BOX_WIDTH = "box_width";

    public static final String BOX_HEIGHT = "box_height";

    public static final String BOX_QTY = "box_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}