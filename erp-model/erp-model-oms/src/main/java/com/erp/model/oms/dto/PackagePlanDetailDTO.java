package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 组包计划明细请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
*/
@Data
@NoArgsConstructor
public class PackagePlanDetailDTO implements Serializable {




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
         * 条码
         */
        private String barcode;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
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
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 19,message = "销售订单id最大长度不能超过19位")
        private String soId;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 物流渠道名
        */
        @NotBlank(message = "物流渠道名不能为空")
        @Size(max = 50,message = "物流渠道名最大长度不能超过50位")
        private String logisticsChannelName;

        /**
        * 物流跟踪号
        */
        @NotBlank(message = "物流跟踪号不能为空")
        @Size(max = 32,message = "物流跟踪号最大长度不能超过32位")
        private String trackNo;

        /**
        * 运输单号
        */
        @NotBlank(message = "运输单号不能为空")
        @Size(max = 32,message = "运输单号最大长度不能超过32位")
        private String transportNo;


    }


}