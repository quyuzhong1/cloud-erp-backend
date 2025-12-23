package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.apache.xpath.operations.Bool;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
*/
@Data
@NoArgsConstructor
public class DeliveryBoxRuleDetailDTO implements Serializable {

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
         * 主表id
         */
        private String  mainId;

        /**
        * 发货skuId
        */
        private String deliverySkuId;

        /**
        * 发货sku编码
        */
        private String deliverySkuNo;

        /**
        * 发货sku名称
        */
        private String deliveryProductName;

        /**
        * 每箱数量
        */
        private Integer perBoxQty;

        /**
        * 优先级
        */
        private Integer sort;

        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;

        /**
         * 作废用户id
         */
        private String invalidUserId;

        /**
         * 作废用户名
         */
        private String invalidUserName;

        /**
         * 作废原因
         */
        private String invalidReason;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 作废状态
         */
        @NotNull(message = "作废状态不能为空")
        private Boolean invalidStatus;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 箱规头id
         */
        private String mainId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        private String deliverySkuId;

        /**
         * skuNo
         */
        @NotBlank(message = "sku编码不能为空")
        private String deliverySkuNo;

        /**
        * sku名称
        */
        @NotBlank(message = "sku名称不能为空")
        private String deliveryProductName;

        /**
        * 每箱数量
        */
        @NotNull(message = "每箱数量不能为空")
        @Min(value = 1)
        private Integer perBoxQty;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer sort;

        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;

        /**
         * 作废用户id
         */
        private String invalidUserId;

        /**
         * 作废用户名
         */
        private String invalidUserName;

        /**
         * 作废原因
         */
        private String invalidReason;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;
    }

    @Data
    @NoArgsConstructor
    public static class DetailImportDTO {
        /**
         * 发货skuId
         */
        private String deliverySkuId;
        /**
         * 发货skuNo
         */
        private String deliverySkuNo;
        /**
         * 发货skuNo
         */
        private String deliveryProductName;

        /**
         * 发货箱规
         */
        private Integer perBoxQty;

        /**
         * 优先级
         */
        private Integer sort;
    }

    @Data
    @NoArgsConstructor
    public static class ListBoxRuleBySkuDetailDTO{

        /**
         * 明细id
         */
        private String id;
        /**
         * 发货skuId
         */
        private String deliverySkuId;

        /**
         * 发货sku编码
         */
        private String deliverySkuNo;

        /**
         * 发货sku名称
         */
        private String deliveryProductName;

        /**
         * 每箱数量
         */
        private Integer perBoxQty;

        /**
         * 优先级
         */
        private Integer sort;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;
    }

}