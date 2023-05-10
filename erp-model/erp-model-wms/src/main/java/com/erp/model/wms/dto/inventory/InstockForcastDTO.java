package com.erp.model.wms.dto.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 入库预报请求实体
 * @Classname: InstockForcastDTO
 * @Description: TODO
 * @CreateTime: 2023-05-09  15:28
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class InstockForcastDTO implements Serializable {

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO  {


        /**
         * 采购订单id
         */
        @NotEmpty(message = "采购订单id不能为空")
        private String purchaseOrderId;

        /**
         * 仓库组织id
         */
        @NotEmpty(message = "仓库组织不能为空")
        private String orgId;

        /**
         * 仓库id
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;


        /**
         * 单据日期（暂时不传，等于当前时间）
         */
        private LocalDate billDate;

        /**
         * 入库预报库存产品明细
         */
        @Valid
        @NotNull(message = "产品明细不能为空")
        @Size(min = 1, message = "请至少录入一条产品明细")
        private List<InstockForcastDetailDTO.AddDTO> details;

    }


    /**
     * 结束交货
     */
    @Data
    @NoArgsConstructor
    public static class FinishDeliveryDTO  {

        /**
         * 采购订单id
         */
        @NotEmpty(message = "采购订单id不能为空")
        private String purchaseOrderId;


        /**
         * 结束交货产品明细
         */
        @Valid
        @NotNull(message = "产品明细不能为空")
        @Size(min = 1, message = "请至少录入一条产品明细")
        private List<InventoryFinishDeliveryDetailDTO.AddDTO> members;

    }


}