package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * <p>
 * 模具明细
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_detail")
public class MouldDetailEntity extends BaseEntity<MouldDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 模具编号
    */
    @TableField("mould_no")
    private String mouldNo;
    /**
    * 外部模具编号(供应商)
    */
    @TableField("third_mould_no")
    private String thirdMouldNo;
    /**
    * 模具类型
    */
    @TableField("type_id")
    private String typeId;
    /**
    * 模具穴数
    */
    @TableField("mold_holes")
    private String moldHoles;
    /**
    * 模具长
    */
    @TableField("length")
    private BigDecimal length;
    /**
    * 模具宽
    */
    @TableField("width")
    private BigDecimal width;
    /**
    * 模具高
    */
    @TableField("height")
    private BigDecimal height;
    /**
    * 模具材质
    */
    @TableField("material")
    private String material;
    /**
    * 模具寿命(万)(啤)
    */
    @TableField("life_cycle")
    private Integer lifeCycle;
    /**
    * 开模周期(自然日)
    */
    @TableField("develop_cycle")
    private Integer developCycle;
    /**
    * 启用时间
    */
    @TableField("enable_date")
    private LocalDate enableDate;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String MOULD_NO = "mould_no";

    public static final String THIRD_MOULD_NO = "third_mould_no";

    public static final String TYPE_ID = "type_id";

    public static final String MOLD_HOLES = "mold_holes";

    public static final String LENGTH = "length";

    public static final String WIDTH = "width";

    public static final String HEIGHT = "height";

    public static final String MATERIAL = "material";

    public static final String LIFE_CYCLE = "life_cycle";

    public static final String DEVELOP_CYCLE = "develop_cycle";

    public static final String ENABLE_DATE = "enable_date";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}