package com.erp.model.dmp.dto.mabang;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 马帮手工出入库请求参数实体
 * @CreateTime: 2023-06-27  19:31
 * @Author: zhangchunlin
 */
@NoArgsConstructor
@Data
public class MabangInOutStockDTO {

    /**
     * erp单据编号
     */
    private String erpSourceCode;

    /**
     * 审核类型
     */
    private String approveType;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 员工名称
     */
    private String employeeName;

    /**
     * 出入库类型
     */
    private String type;

    /**
     * 入库类型
     */
    private String typeName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 商品
     */
    private List<SkuItem> data;

    /**
     * 商品
     */
    @Data
    public static class SkuItem {

        private String stockSku;

        private String productName;

        private String quantity;

        private String gridCode;

        private String sourceDetailId;

    }

}