package com.erp.model.mrp.dto;

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
 * 销量试算表请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@NoArgsConstructor
public class CalcSalesInfoDimDTO implements Serializable {




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
        * skuId
        */
        private String skuId;

        /**
        * sku
        */
        private String skuNo;

        /**
        * 国家
        */
        private String country;

        /**
        * 店铺
        */
        private String shopId;

        /**
        * 平台
        */
        private String platform;

        /**
        * 累计销量(去噪后)
        */
        private String salesQtyJson;

        /**
        * 日均销量(去噪后)
        */
        private String avgSalesQtyJson;

        /**
        * 预估销量
        */
        private String monthSalesEstimateQtyJson;

        /**
        * 真实销量
        */
        private String monthRealSalesQtyJson;

        /**
        * 试算配置id
        */
        private String cfgRuleCalcId;

        /**
        * 备注
        */
        private String remark;


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
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 国家
        */
        @NotBlank(message = "国家不能为空")
        @Size(max = 255,message = "国家最大长度不能超过255位")
        private String country;

        /**
        * 店铺
        */
        @NotBlank(message = "店铺不能为空")
        @Size(max = 19,message = "店铺最大长度不能超过19位")
        private String shopId;

        /**
        * 平台
        */
        @NotBlank(message = "平台不能为空")
        @Size(max = 255,message = "平台最大长度不能超过255位")
        private String platform;

        /**
        * 累计销量(去噪后)
        */
        private String salesQtyJson;

        /**
        * 日均销量(去噪后)
        */
        private String avgSalesQtyJson;

        /**
        * 预估销量
        */
        private String monthSalesEstimateQtyJson;

        /**
        * 真实销量
        */
        private String monthRealSalesQtyJson;

        /**
        * 试算配置id
        */
        @NotBlank(message = "试算配置id不能为空")
        @Size(max = 19,message = "试算配置id最大长度不能超过19位")
        private String cfgRuleCalcId;

        /**
        * 备注
        */
        private String remark;


    }


}