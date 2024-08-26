package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
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
 * 中台FBA库存信息请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-08-26
*/
@Data
@NoArgsConstructor
public class DmpFbaInventoryDTO implements Serializable {




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
        * 平台SPU
        */
        private String asin;

        /**
        * 卖家sku
        */
        private String msku;

        /**
        * FNSKU
        */
        private String fnSku;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 平台最后更新时间
        */
        private LocalDateTime lastPlatformUpdateTime;

        /**
        * 亚马逊库存SKU使用状况
        */
        private String condition;

        /**
        * 亚马逊站点代号
        */
        private String marketplaceId;

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
        * 平台SPU
        */
        @NotBlank(message = "平台SPU不能为空")
        @Size(max = 64,message = "平台SPU最大长度不能超过64位")
        private String asin;

        /**
        * 卖家sku
        */
        @NotBlank(message = "卖家sku不能为空")
        @Size(max = 64,message = "卖家sku最大长度不能超过64位")
        private String msku;

        /**
        * FNSKU
        */
        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 64,message = "FNSKU最大长度不能超过64位")
        private String fnSku;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String productName;

        /**
        * 平台最后更新时间
        */
        private LocalDateTime lastPlatformUpdateTime;

        /**
        * 亚马逊库存SKU使用状况
        */
        @NotBlank(message = "亚马逊库存SKU使用状况不能为空")
        @Size(max = 64,message = "亚马逊库存SKU使用状况最大长度不能超过64位")
        private String condition;

        /**
        * 亚马逊站点代号
        */
        @NotBlank(message = "亚马逊站点代号不能为空")
        @Size(max = 64,message = "亚马逊站点代号最大长度不能超过64位")
        private String marketplaceId;

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