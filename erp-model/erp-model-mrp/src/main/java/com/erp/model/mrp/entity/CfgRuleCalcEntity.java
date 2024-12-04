package com.erp.model.mrp.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;


/**
 * <p>
 * 试算配置
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_calc")
public class CfgRuleCalcEntity extends BaseEntity<CfgRuleCalcEntity> {

    /**
    * sku_id
    */
    @TableField("sku_json")
    private JSONArray skuJson;
    /**
     * 试算日期
     */
    @NotNull(message = "开始试算日期不能为空")
    @TableField("start_calc_date")
    private LocalDate startCalcDate;

    /**
     * 试算日期
     */
    @NotNull(message = "结束试算日期不能为空")
    @TableField("end_calc_date")
    private LocalDate endCalcDate;
    /**
    * 店铺json
    */
    @TableField("shop_json")
    private JSONArray shopJson;
    /**
    * 历史销量类型
    */
    @TableField("sale_type")
    private String saleType;
    /**
    * 文件地址
    */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件名字
     */
    @TableField("file_name")
    private String fileName;
    /**
    * 试算配置名称
    */
    @TableField("name")
    private String name;

    /**
     * 试算配置编号
     */
    @TableField("code")
    private String code;

    public static final String SKU_JSON = "sku_json";

    public static final String CALC_DATE = "calc_date";

    public static final String SHOP_JSON = "shop_json";

    public static final String SALE_TYPE = "sale_type";

    public static final String FILE_URL = "file_url";

    public static final String NAME = "name";


    @Override
    public Serializable pkVal() {
        return null;
    }

}