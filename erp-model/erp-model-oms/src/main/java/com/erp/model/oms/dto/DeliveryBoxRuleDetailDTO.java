package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
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
        private Integer priority;


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

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        private String deliverySkuId;

        /**
        * sku名称
        */
        @NotBlank(message = "sku名称不能为空")
        private String deliveryProductName;

        /**
        * 每箱数量
        */
        @NotNull(message = "每箱数量不能为空")
        private Integer perBoxQty;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer priority;


    }


}