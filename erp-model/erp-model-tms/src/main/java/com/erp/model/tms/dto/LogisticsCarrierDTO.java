package com.erp.model.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 物流快递/海运/空运公司列表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
*/
@Data
@NoArgsConstructor
public class LogisticsCarrierDTO implements Serializable {




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
        * 物流类型
        */
        private String logisticsType;

        /**
        * 物流商编码
        */
        private String carrierCode;

        /**
        * 物流商中文名称
        */
        private String carrierCn;

        /**
        * 物流商英文名称
        */
        private String carrierEn;


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
        * 物流类型
        */
        @NotBlank(message = "物流类型不能为空")
        @Size(max = 50,message = "物流类型最大长度不能超过50位")
        private String logisticsType;

        /**
        * 物流商编码
        */
        @NotBlank(message = "物流商编码不能为空")
        @Size(max = 200,message = "物流商编码最大长度不能超过200位")
        private String carrierCode;

        /**
        * 物流商中文名称
        */
        @NotBlank(message = "物流商中文名称不能为空")
        @Size(max = 255,message = "物流商中文名称最大长度不能超过255位")
        private String carrierCn;

        /**
        * 物流商英文名称
        */
        @NotBlank(message = "物流商英文名称不能为空")
        @Size(max = 255,message = "物流商英文名称最大长度不能超过255位")
        private String carrierEn;


    }

    @Data
    @NoArgsConstructor
    public static class SearchDTO {
        /**
         * 物流类型 expressDelivery 快递 ocean 海运 aviation  空运
         */
        private String logisticsType;
        /**
         * 搜素名称
         */
        private String searchKeyword;
    }

    @Data
    @AllArgsConstructor
    public static class PagingVO {
        private String id;
        /**
         * 主键id
         */
        private String logisticsType;
        /**
         * 物流商编码
         */
        private String carrierCode;
        /**
         * 物流商中文名称
         */
        private String carrierCn;
        /**
         * 物流商英文名称
         */
        private String carrierEn;
    }
}