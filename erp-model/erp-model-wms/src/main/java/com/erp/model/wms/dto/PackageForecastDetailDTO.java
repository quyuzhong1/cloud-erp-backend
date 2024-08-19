package com.erp.model.wms.dto;

import java.math.BigDecimal;
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
 * 组包预报详情请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
*/
@Data
@NoArgsConstructor
public class PackageForecastDetailDTO implements Serializable {




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
        * 销售订单code
        */
        private String soCode;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道名
        */
        private String logisticsChannelName;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 运输单号
        */
        private String transportNo;

        /**
        * 重量
        */
        private BigDecimal weight;

        /**
        * 重量单位 
        */
        private String weightUnit;

        /**
        * 交接状态
        */
        private String handoverStatus;

        /**
         * 交接状态名
         */
        private String handoverStatusName;

        /**
        * 出库状态名
        */
        private String outstockStatusName;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 是否存在发货拦截单
         */
        private boolean hasDeliveryIntercept;

        /**
         * 销售订单单据状态
         */
        private String billStatus;

        private Boolean isIntercept;
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
        * 主表id
        */
        private String mainId;

        /**
        * 销售订单code
        */
        @NotBlank(message = "销售订单code不能为空")
        @Size(max = 32,message = "销售订单code最大长度不能超过32位")
        private String soCode;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单不能为空")
        private String soId;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        private String logisticsChannelId;

        private String logisticsChannelName;



        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 运输单号
        */
        private String transportNo;

        /**
        * 重量
        */
        @NotNull(message = "重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal weight;

        /**
        * 重量单位 
        */
        @NotBlank(message = "重量单位 不能为空")
        @Size(max = 10,message = "重量单位 最大长度不能超过10位")
        private String weightUnit;

        /**
        * 交接状态
        */
        private String handoverStatus;




    }


}