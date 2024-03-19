package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 头程物流单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
*/
@Data
@NoArgsConstructor
public class TmsFirstMileLogisticDTO implements Serializable {




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
        * 运单号
        */
        private String transportNo;

        /**
        * 柜号
        */
        private String counterNo;

        /**
        * 对账状态
        */
        private String reconciliationStatus;

        /**
        * 发票状态
        */
        private String invoicesStatus;

        /**
        * 物流状态
        */
        private String logisticsStatus;

        /**
        * 运输方式
        */
        private String shippingMethod;

        /**
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 备注
        */
        private String remark;

        /**
        * 物流下单时间
        */
        private LocalDateTime logisticsOrderTime;

        /**
        * 开船时间
        */
        private LocalDateTime shipTime;

        /**
        * 运输时间
        */
        private LocalDateTime transitTime;

        /**
        * 到达时间
        */
        private LocalDateTime arrivalTime;

        /**
        * 签收时间
        */
        private LocalDateTime signTime;

        /**
        * 实际重量
        */
        private BigDecimal actualWeight;

        /**
        * 实际重量单位
        */
        private String actualWeightUnit;

        /**
        * 实际体积重
        */
        private BigDecimal actualVolumeWeight;

        /**
        * 实际体积中单位
        */
        private String actualVolumeWeightUnit;

        /**
        * 币种
        */
        private String currency;

        /**
        * 汇率表id
        */
        private String exchangeRateId;

        /**
        * 汇率
        */
        private String exchangeRate;


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
        * 运单号
        */
        private String transportNo;

        /**
        * 柜号
        */
        private String counterNo;

        /**
        * 对账状态
        */
        private String reconciliationStatus;

        /**
        * 发票状态
        */
        private String invoicesStatus;

        /**
        * 物流状态
        */
        private String logisticsStatus;

        /**
        * 运输方式
        */
        private String shippingMethod;

        /**
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 备注
        */
        private String remark;

        /**
        * 物流下单时间
        */
        private LocalDateTime logisticsOrderTime;

        /**
        * 开船时间
        */
        private LocalDateTime shipTime;

        /**
        * 运输时间
        */
        private LocalDateTime transitTime;

        /**
        * 到达时间
        */
        private LocalDateTime arrivalTime;

        /**
        * 签收时间
        */
        private LocalDateTime signTime;

        /**
        * 实际重量
        */
        private BigDecimal actualWeight;

        /**
        * 实际重量单位
        */
        private String actualWeightUnit;

        /**
        * 实际体积重
        */
        private BigDecimal actualVolumeWeight;

        /**
        * 实际体积中单位
        */
        private String actualVolumeWeightUnit;

        /**
        * 币种
        */
        private String currency;

        /**
        * 汇率表id
        */
        private String exchangeRateId;

        /**
        * 汇率
        */
        private String exchangeRate;


    }


}