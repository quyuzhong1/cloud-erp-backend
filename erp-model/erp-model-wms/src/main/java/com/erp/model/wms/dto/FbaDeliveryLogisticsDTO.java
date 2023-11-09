package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * FBI发货单物流信息表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FbaDeliveryLogisticsDTO implements Serializable {

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
        * 发货单号
        */
        private String deliveryCode;

        /**
         * 物流方式:/wms/common/enumDropDown?type=LogisticsMethod
         * 描述：airfreight:空运, express:快递, oceanFreightBulk:海运散装
         * , oceanFreightFCL:海运整箱, railwayTransportationBulk:铁运散装
         * , railwayTransportationFCL:铁运整箱
         */
        private String logisticsMethod;

        /**
        * 物流方式名称
        */
        private String logisticsMethodName;

        /**
        * 物流渠道
        */
        private String logisticsChannel;

        /**
        * 物流渠道名称
        */
        private String logisticsChannelName;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 备注
        */
        private String logisticsRemark;

        /**
         * 物流跟踪号
         */
        private List<String> trackingNoList;
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
        * 发货单id
        */
        private String mainId;

        /**
        * 发货单号
        */
        private String deliveryCode;

        /**
        * 物流方式
        */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 64,message = "物流方式最大长度不能超过64位")
        private String logisticsMethod;

        /**
        * 物流渠道
        */
        private String logisticsChannel;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
         * 物流跟踪号
         */
        private List<String> trackingNoList;

        /**
         * 备注
         */
        private String logisticsRemark;
    }


    /**
     * 更新物流信息列表查询
     */
    @Data
    @NoArgsConstructor
    public static class DeliveryLogisticsView {
        /**
         * 主键id
         */
        private String id;
        /**
         * 发货单id
         */
        private String mainId;
        /**
         * 发货单号
         */
        private String deliveryCode;
        /**
         * 物流方式
         */
        private String logisticsMethod;
        /**
         * 物流方式
         */
        private String logisticsMethodName;
        /**
         * 物流渠道
         */
        private String logisticsChannel;
        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;
        /**
         * 物流跟踪号
         */
        private List<String> trackingNoList;
        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;
        /**
         * 备注
         */
        private String logisticsRemark;
    }

    /**
     * 更新物流信息列表保存
     */
    @Data
    @NoArgsConstructor
    public static class DeliveryLogisticsSave {
        /**
         * 主键id
         */
        private String id;
        /**
         * 发货单id
         */
        private String mainId;
        /**
         * 物流方式
         */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 64,message = "物流方式最大长度不能超过64位")
        private String logisticsMethod;
        /**
         * 物流渠道
         */
        private String logisticsChannel;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;
        /**
         * 物流跟踪号
         */
        private List<String> trackingNoList;
        /**
         * 备注
         */
        private String logisticsRemark;
    }
}