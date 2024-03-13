package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

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
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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
}