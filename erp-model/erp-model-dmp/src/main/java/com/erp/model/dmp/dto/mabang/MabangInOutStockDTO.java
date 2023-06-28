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
     * 仓库编码
     */
    @JsonIgnore
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

        @JsonIgnore
        private String productName;

        private String quantity;

        private String gridCode;

        @JsonIgnore
        private String sourceDetailId;

    }

}