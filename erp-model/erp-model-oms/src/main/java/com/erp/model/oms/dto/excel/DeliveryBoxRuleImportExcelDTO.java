package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import java.io.Serializable;

/**
 * @Author: wtr
 * @Date: 2025/11/27 17:18
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
public class DeliveryBoxRuleImportExcelDTO  implements Serializable {

    @ExcelProperty("*SKU")
    @FieldValid(fieldName = "skuNo",isNotBlank = true)
    private String skuNo;

    /**
     * 申请日期
     */
    @ExcelProperty("*发货SKU")
    @FieldValid(fieldName = "deliverySkuNo",isNotBlank = true)
    private String  deliverySkuNo;

    /**
     * 申请人
     */
    @ExcelProperty("*发货箱规")
    @FieldValid(fieldName = "perBoxQty",isNotBlank = true)
    private String perBoxQty;

    /**
     * 优先级
     */
    @ExcelProperty("*优先级")
    @FieldValid(fieldName = "sort",isNotBlank = true)
    private String sort;

    /**
     * 错误数据
     */
    @ExcelProperty("错误数据")
    private String  errorMsg;
}
