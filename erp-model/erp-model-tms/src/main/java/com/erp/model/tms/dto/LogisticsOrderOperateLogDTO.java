package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流平台订单操作记录请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
*/
@Data
@NoArgsConstructor
public class LogisticsOrderOperateLogDTO implements Serializable {




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
        * 业务类型（createOrder,confirmOrder,updateOrder）
        */
        private String businessType;

        /**
        * 配置Id
        */
        private String authId;

        /**
        * 销售订单Id
        */
        private String soId;

        /**
        * 销售订单编码
        */
        private String soCode;

        /**
        * 物流平台类型
        */
        private String logisticsPlatform;

        /**
        * 请求状态（0请求中 1请求成功 2请求失败）
        */
        private String status;

        /**
        * 操作类型
        */
        private String operation;

        /**
        * API请求参数
        */
        private String requestParamJson;

        private String msg;

        /**
        * API响应参数
        */
        private String responseParamJson;


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
        * 业务类型（createOrder,confirmOrder,updateOrder）
        */
        @NotBlank(message = "业务类型（createOrder,confirmOrder,updateOrder）不能为空")
        @Size(max = 255,message = "业务类型（createOrder,confirmOrder,updateOrder）最大长度不能超过255位")
        private String businessType;

        /**
        * 配置Id
        */
        @NotBlank(message = "配置Id不能为空")
        private String authId;

        /**
        * 销售订单Id
        */
        private String soId;

        /**
        * 销售订单编码
        */
        private String soCode;

        /**
        * 物流平台类型
        */
        @NotBlank(message = "物流平台类型不能为空")
        @Size(max = 255,message = "物流平台类型最大长度不能超过255位")
        private String logisticsPlatform;

        /**
        * 请求状态（0请求中 1请求成功 2请求失败）
        */
        private String status;

        /**
        * 操作类型
        */
        @NotBlank(message = "操作类型不能为空")
        @Size(max = 64,message = "操作类型最大长度不能超过64位")
        private String operation;

        /**
        * API请求参数
        */
        private String requestParamJson;

        private String msg;

        /**
        * API响应参数
        */
        private String responseParamJson;


    }


}