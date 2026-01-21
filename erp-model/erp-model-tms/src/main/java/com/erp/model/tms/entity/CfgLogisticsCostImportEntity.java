package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 费用项配置
 * </p>
 *
 * @author jack
 * @since 2026-01-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_logistics_cost_import")
public class CfgLogisticsCostImportEntity extends BaseEntity<CfgLogisticsCostImportEntity> {

    /**
    * 单据编码
    */
    @TableField("code")
    private String code;
    /**
    * 配置单据
    */
    @TableField("bussiness_type")
    private String bussinessType;
    /**
    * 配置类型：logistics_supplier=物流商,platform=平台  枚举：CfgLogisticsCostImportCfgTypeEnum
    */
    @TableField("cfg_type")
    private String cfgType;
    /**
    * 配置平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 识别名称
    */
    @TableField("name")
    private String name;
    /**
    * sheet名称
    */
    @TableField("sheet_name")
    private String sheetName;
    /**
    * 行开始
    */
    @TableField("header_row")
    private Integer headerRow;
    /**
    * 费用来源：api=API,excel=线下表格  枚举：CfgLogisticsCostImportCostTypeEnum
    */
    @TableField("cost_type")
    private String costType;
    /**
    * 导入处理：import_update=导入更新,import_add_old=导入新增(按原单),import_add_new=导入新增(按新单)  枚举：CfgLogisticsCostImportImportTypeEnum
    */
    @TableField("import_type")
    private String importType;
    /**
    * 启用状态
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String BUSSINESS_TYPE = "bussiness_type";

    public static final String CFG_TYPE = "cfg_type";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String NAME = "name";

    public static final String SHEET_NAME = "sheet_name";

    public static final String HEADER_ROW = "header_row";

    public static final String COST_TYPE = "cost_type";

    public static final String IMPORT_TYPE = "import_type";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
