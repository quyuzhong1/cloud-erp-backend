package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 中转报关详情请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class TransferDeclareDetailDTO implements Serializable {




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
        private String mainId;

        /**
        * 销售单id
        */
        private String soId;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道中文
        */
        private String logisticsChannelName;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 包裹重量
        */
        private BigDecimal packageWeight;

        /**
        * 包裹重量单位
        */
        private String weightUnit;

        /**
        * 出库状态
        */
        private String outstockStatus;

        /**
        * 出库状态中文
        */
        private String outstockStatusName;

        /**
        * 中转状态
        */
        private String transferStatus;

        /**
        * 中转状态中文
        */
        private String transferStatusName;

        /**
         * 上传状态(订单)
         */
        private String orderUploadStatus;

        /**
         * 上传状态中文(订单)
         */
        private String orderUploadStatusName;

        /**
         * 失败原因
         */
        private String failureReason;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        private String orderUploadStatus;
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
        * 销售单id
        */
        @NotBlank(message = "销售单id不能为空")
        @Size(max = 19,message = "销售单id最大长度不能超过19位")
        private String soId;

        /**
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 64,message = "销售单号最大长度不能超过64位")
        private String soCode;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 包裹重量
        */
        private BigDecimal packageWeight;

        /**
        * 包裹重量单位
        */
        private String weightUnit;

        /**
        * 第三方中转服务商的发货单号
        */
        private String shippingOrderNo;

    }


}