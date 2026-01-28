package com.erp.model.dmp.dto.excel;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;

import lombok.Data;

@Data
public class PlatformInitStockExcelDTO implements Serializable {

	/**
     * 周期
     */
    @ExcelProperty(value = "*核对仓库")
    @FieldValid(fieldName = "核对仓库", isNotBlank = true)
	private String sourceSystemName;
	
	/**
     * 周期
     */
    @ExcelProperty(value = "*周期")
    @FieldValid(fieldName = "周期", isNotBlank = true)
	private String checkMonth;
	
    /**
     * 库存SKU
     */
    @ExcelProperty(value = "*库存SKU")
    @FieldValid(fieldName = "库存SKU", isNotBlank = true)
    private String stockSku;
    /**
     * 仓库名称
     */
    @ExcelProperty(value = "*仓库名称")
    @FieldValid(fieldName = "仓库名称", isNotBlank = true)
    private String platformWarehouseName;
    /**
     * 期初数量
     */
    @ExcelProperty(value = "*期初数量")
    @FieldValid(fieldName = "期初数量",isNotBlank = true)
    private String initQty;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
    
    /**
     * 主键
     */
    private String id;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 账号
     */
    private String accountCode;
    
    /**
     * 仓库编码
     */
    private String platformWarehouseCode;
}
