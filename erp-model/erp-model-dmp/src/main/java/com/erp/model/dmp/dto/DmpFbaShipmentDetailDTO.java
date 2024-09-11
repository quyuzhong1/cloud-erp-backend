package com.erp.model.dmp.dto;

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
 * FBA拣货明细表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-09-07
*/
@Data
@NoArgsConstructor
public class DmpFbaShipmentDetailDTO implements Serializable {




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
        * 平台产品id（ASIN）
        */
        private String asin;

        /**
        * 平台sku（msku）
        */
        private String msku;

        /**
        * FNSKU
        */
        private String fnSku;

        /**
        * 申报数量
        */
        private Integer declareQty;

        /**
        * 收发差异
        */
        private Integer diffQty;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * FBA货件ID
        */
        private String fbaShipmentId;

        /**
        * 输入任务ID
        */
        private String inputTaskId;

        /**
        * 亚马逊账号代号
        */
        private String platformShopCode;

        /**
        * 任务转换ID
        */
        private String convertId;

        /**
        * 店铺ID
        */
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        private String dataEncrypt;


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
        * 平台产品id（ASIN）
        */
        @NotBlank(message = "平台产品id（ASIN）不能为空")
        @Size(max = 64,message = "平台产品id（ASIN）最大长度不能超过64位")
        private String asin;

        /**
        * 平台sku（msku）
        */
        @NotBlank(message = "平台sku（msku）不能为空")
        @Size(max = 64,message = "平台sku（msku）最大长度不能超过64位")
        private String msku;

        /**
        * FNSKU
        */
        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 64,message = "FNSKU最大长度不能超过64位")
        private String fnSku;

        /**
        * 申报数量
        */
        @NotNull(message = "申报数量不能为空")
        private Integer declareQty;

        /**
        * 收发差异
        */
        @NotNull(message = "收发差异不能为空")
        private Integer diffQty;

        /**
        * 收货数量
        */
        @NotNull(message = "收货数量不能为空")
        private Integer receiveQty;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * FBA货件ID
        */
        @NotBlank(message = "FBA货件ID不能为空")
        @Size(max = 255,message = "FBA货件ID最大长度不能超过255位")
        private String fbaShipmentId;

        /**
        * 输入任务ID
        */
        @NotBlank(message = "输入任务ID不能为空")
        @Size(max = 19,message = "输入任务ID最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 亚马逊账号代号
        */
        @NotBlank(message = "亚马逊账号代号不能为空")
        @Size(max = 100,message = "亚马逊账号代号最大长度不能超过100位")
        private String platformShopCode;

        /**
        * 任务转换ID
        */
        @NotBlank(message = "任务转换ID不能为空")
        @Size(max = 19,message = "任务转换ID最大长度不能超过19位")
        private String convertId;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 19,message = "店铺ID最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        @NotBlank(message = "任务来源唯一加密代号不能为空")
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        @NotBlank(message = "任务数据加密代号不能为空")
        private String dataEncrypt;


    }


}