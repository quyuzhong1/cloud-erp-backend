package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: InitStockExportExcelDTO
 * @Description: TODO
 * @CreateTime: 2023-05-11  12:11
 * @Author: zhangchunlin
 */
@Data
public class InitStockExportExcelDTO implements Serializable {

    /**
     * 单据编号
     */
    private String code;

    /**
     * sku编号
     */
    private String skuNo;


    /**
     * 产品名称
     */
    private String productName;

    /**
     * 单据状态
     */
    private String approveStatusName;

    /**
     * 作废状态（0未作废，1已作废）
     */
    @ExcelProperty(value = "作废状态", index = 3)
    private String invalidStatusName;

    /**
     * spu型号
     */
    private String spuNo;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 库存组织名称
     */
    private String orgName;

    /**
     * 销售状态编码
     */
    private String saleStatus;

    /**
     * 销售状态名称
     */
    private String saleStatusName;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 期初数量
     */
    private Integer qty;


}