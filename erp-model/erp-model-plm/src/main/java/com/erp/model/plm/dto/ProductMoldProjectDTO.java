package com.erp.model.plm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 开模通知单-选择产品弹框返回值：在产品明细基础上附带模具档案项目名称
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductMoldProjectDTO extends ProductDetailShowDTO {

    /**
     * 项目名称
     * 模具档案 mold_info.project_name，按 skuNo = mold_info.code 关联
     */
    private String projectName;
}
