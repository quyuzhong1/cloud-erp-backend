package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * b2c发货拦截单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
*/
@Data
@NoArgsConstructor
public class SoB2cDeliveryInterceptDTO implements Serializable {




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
        * 单据编号
        */
        private String code;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 处理状态 waitHandle:待处理 handle:已处理 cancel:已取消
        */
        private String handleStatus;

        /**
        * 处理结果 success：拦截成功  failure：拦截失败
        */
        private String handleResult;

        /**
        * 处理备注
        */
        private String handleRemark;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 发货单号
        */
        private String soDeliveryCode;

        /**
        * 销售出库单号
        */
        private String soOutstockCode;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道名称
        */
        private String logisticsChannelName;

        /**
        * 运单号
        */
        private String transportNo;

        /**
        * 备注
        */
        private String remark;

        /**
        * 处理人id
        */
        private String handleUserId;

        /**
        * 处理人名称
        */
        private String handleUserName;

        /**
        * 处理时间
        */
        private LocalDateTime handleTime;


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
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50,message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 64,message = "来源类型最大长度不能超过64位")
        private String sourceType;

        /**
        * 处理状态 waitHandle:待处理 handle:已处理 cancel:已取消
        */
        @NotBlank(message = "处理状态 waitHandle:待处理 handle:已处理 cancel:已取消不能为空")
        @Size(max = 64,message = "处理状态 waitHandle:待处理 handle:已处理 cancel:已取消最大长度不能超过64位")
        private String handleStatus;

        /**
        * 处理结果 success：拦截成功  failure：拦截失败
        */
        @NotBlank(message = "处理结果 success：拦截成功  failure：拦截失败不能为空")
        @Size(max = 64,message = "处理结果 success：拦截成功  failure：拦截失败最大长度不能超过64位")
        private String handleResult;

        /**
        * 处理备注
        */
        @NotBlank(message = "处理备注不能为空")
        @Size(max = 255,message = "处理备注最大长度不能超过255位")
        private String handleRemark;

        /**
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 50,message = "销售单号最大长度不能超过50位")
        private String soCode;

        /**
        * 发货单号
        */
        @NotBlank(message = "发货单号不能为空")
        @Size(max = 50,message = "发货单号最大长度不能超过50位")
        private String soDeliveryCode;

        /**
        * 销售出库单号
        */
        @NotBlank(message = "销售出库单号不能为空")
        @Size(max = 50,message = "销售出库单号最大长度不能超过50位")
        private String soOutstockCode;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 物流渠道名称
        */
        @NotBlank(message = "物流渠道名称不能为空")
        @Size(max = 255,message = "物流渠道名称最大长度不能超过255位")
        private String logisticsChannelName;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 64,message = "运单号最大长度不能超过64位")
        private String transportNo;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 处理人id
        */
        @NotBlank(message = "处理人id不能为空")
        @Size(max = 19,message = "处理人id最大长度不能超过19位")
        private String handleUserId;

        /**
        * 处理人名称
        */
        @NotBlank(message = "处理人名称不能为空")
        @Size(max = 50,message = "处理人名称最大长度不能超过50位")
        private String handleUserName;

        /**
        * 处理时间
        */
        private LocalDateTime handleTime;


    }


}