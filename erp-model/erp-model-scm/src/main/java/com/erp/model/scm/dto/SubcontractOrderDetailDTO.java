package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 委外订单明细请求响应实体
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@NoArgsConstructor
public class SubcontractOrderDetailDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends UpdateDTO {

        /**
        * 变体信息
        */
        private String variantProperty;
        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 采购金额
        */
        private BigDecimal amount;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 是否是赠品（false否，true是）
        */
        private Boolean isGift;

        /**
        * bom版本
        */
        private Integer bomVersion;

        /**
         * 子件集合
         */
        private List<ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 子件集合
         */
        private List<AddDTO> childList;

    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

        /**
         * 子件集合
         */
        private List<UpdateDTO> childList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;
        /**
        * sku编码
        */
        @NotBlank(message = "sku编码不能为空")
        @Size(max = 32,message = "sku编码最大长度不能超过32位")
        private String skuNo;

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;
        /**
        * 采购数量
        */
        @NotNull(message = "采购数量不能为空")
        private Integer qty;
        /**
        * 领料数量(发料数量)
        */
        @NotNull(message = "领料数量(发料数量)不能为空")
        private Integer deliveryQty;
        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal price;
        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 20,message = "币别最大长度不能超过20位")
        private String currency;
        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;
        /**
        * 预计交货日期
        */
        private LocalDate planDeliveryDate;
        /**
        * 是否加急（false否，true是）
        */
        @NotNull(message = "是否加急（false否，true是）不能为空")
        private Boolean isUrgent;

        /**
         * 备注
         */
        private String remark;

        /**
        * 是否自动生成采购订单
        */
        @NotNull(message = "是否自动生成采购订单不能为空")
        private Boolean isGeneratePo;

        /**
        * 来源明细id
        */
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

    }


}