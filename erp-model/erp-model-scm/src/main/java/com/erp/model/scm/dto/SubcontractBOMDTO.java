package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: wtr
 * @Date: 2026/1/19 8:40
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@NoArgsConstructor
public class SubcontractBOMDTO {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KingdeeSubcontractBOMDTO{

        private String id;

        /**
         * 委外订单id
         */
        private String sourceId;

        /**
         * 委外订单编码
         */
        private String sourceCode;

        /**
         * 金蝶数据id
         */
        private String syncKingdeeId;

        /**
         * 委外清单分录行-委外订单编号
         */
        private String code;

        /**
         * 明细
         */
        private List<KingdeeSubcontractBOMDetailDTO> details;

    }

    /**
     * 明细
     */
    @Data
    @NoArgsConstructor
    public static class KingdeeSubcontractBOMDetailDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * SKU编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;
        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 币种
         */
        private String currency;


        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 返修数量
         */
        private Integer repairQty;

        /**
         * 返修单价
         */
        private BigDecimal repairPrice;

        /**
         * 返修金额
         */
        private BigDecimal repairAmount;

        /**
         * 采购金额
         */
        private BigDecimal amount;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 即时库存数量
         */
        private Integer curInventoryQty;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;

        /**
         * 仓库库位名称
         */
        private String warehouseLocationName;

        /**
         * 子件集合
         */
        private List<SubcontractOrderDetailDTO.ChildDTO> childList;

        /**
         * 新品首批名称
         */
        private String firstMassProductName;
    }
}
