package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author lambda
 * @since 2023-12-20
*/
@Data
@NoArgsConstructor
public class SoB2cErrorDTO implements Serializable {




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
        * 销售订单id
        */
        private String mainId;

        /**
        * 异常类型
         * submitDelivery 提交发货异常
         * signDelivery 标记发货异常
         * getLogisticsCode 获取物流单异常
        */
        private String type;

        /**
        * 传的json 字符串
        */
        private String paramJson;

        /**
        * 错误信息
        */
        private String message;

        /**
        * 返回的json 字符串
        */
        private String returnJson;


    }

    /**
    * 新增
    */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        public AddDTO(String mainId, String type, String paramJson, String message, String returnJson, String detailId) {
            super.mainId = mainId;
            super.type = type;
            super.paramJson = paramJson;
            super.message = message;
            super.returnJson = returnJson;
            super.detailId = detailId;
        }
    }

    /**
    * 同时新增和删除异常数据
    */
    @Data
    @NoArgsConstructor
    public static class AddAndDeleteDTO{
        /**
         * 新增数据
         */
        private List<SoB2cErrorDTO.AddDTO> addDTOList;

        /**
         * 删除的数据
         */
        private List<SoB2cErrorDTO.DeleteDTO> deleteDTOList;


        /**
         * 订单标记报关单信息
         */
        private List<ShippingDTO> shippingOrderDTO;

    }

    /**
     * 第三方中转服务商发货单
     */
    @Data
    @NoArgsConstructor
    public static class ShippingDTO {
        /**
         * 订单id
         */
        private String soId;

        /**
         * 第三方中转服务商的发货单号
         */
        private String shippingOrderNo;
        /**
         * 异常标识
         */
        private String sign;
    }

    /**
     * 异常订单显示参数
     */
    @Data
    @NoArgsConstructor
    public static class  InfoDTO{

        /**
         * 销售订单id
         */
        private String id;

        /**
         * 类型 来源于订单的异常类型
         *
         */
        private String type;

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
        * 销售订单id
        */
        @NotBlank(message = "销售订单不能为空")
        private String mainId;

        /**
         * 前端显示的异常类型
         * {@link com.erp.model.oms.enums.SoB2cErrorTypeEnum}
         */
        @NotBlank(message = "异常类型 异常不能为空")
        private String type;

        /**
        * 传的json 字符串
        */
        @NotBlank(message = "传的json 字符串不能为空")
        private String paramJson;

        /**
        * 错误信息
        */
        @NotBlank(message = "错误信息不能为空")
        private String message;

        /**
        * 返回的json 字符串
        */
        @NotBlank(message = "返回的json 字符串不能为空")
        private String returnJson;

        /**
         * 订单详情id
         */
        private String detailId;

    }
    @Data
    @NoArgsConstructor
    public static class BatchAdd {

        /**
         * 销售订单id
         */
        @NotNull(message = "销售订单不能为空")
        private List<String> mainIds;

        /**
         * 异常类型
         * submitDelivery 提交发货异常
         * signDelivery 标记发货异常
         * getLogisticsCode 获取物流单异常
         */
        @NotBlank(message = "异常类型 异常不能为空")
        private String type;

        /**
         * 传的json 字符串
         */
        @NotBlank(message = "传的json 字符串不能为空")
        private String paramJson;

        /**
         * 错误信息
         */
        @NotBlank(message = "错误信息不能为空")
        private String message;

        /**
         * 返回的json 字符串
         */
        @NotBlank(message = "返回的json 字符串不能为空")
        private String returnJson;


    }

    @Data
    @NoArgsConstructor
    public static class DeleteDTO{
        /**
         * 订单id
         */
        private String mainId;

        /**
         * 异常类型
         */
        private String type;

    }

    @Data
    @NoArgsConstructor
    public static class DeleteDetailDTO{
        /**
         * 订单id
         */
        @NotNull(message = "订单id不能为空")
        private String mainId;
        /**
         * 异常类型
         */
        @NotNull(message = "异常类型不能为空")
        private String type;
        /**
         * 订单明细id
         */
        @NotNull(message = "明细ID不能为空")
        private List<String> detailIdList;
    }


    @Data
    @NoArgsConstructor
    public static class BatchDeleteDTO{
        /**
         * 订单id
         */
        private List<String> mainIds;

        /**
         * 异常类型
         */
        private String type;

    }


    @Data
    @NoArgsConstructor
    public static class TypeCountDTO{
        /**
         *
         */
        private Integer typeCount = 0;

        /**
         * 异常类型
         */
        private String type;

    }
}