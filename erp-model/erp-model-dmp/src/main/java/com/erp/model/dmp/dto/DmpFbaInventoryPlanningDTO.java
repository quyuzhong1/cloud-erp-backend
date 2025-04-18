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
 * 中台FBA库存库龄信息请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-04-11
*/
@Data
@NoArgsConstructor
public class DmpFbaInventoryPlanningDTO implements Serializable {




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

        /**
        * 库龄 0-30 天的可售商品数量
        */
        private Integer inventoryAge0To30Days;

        /**
        * 库龄 31-60 天的可售商品数量
        */
        private Integer inventoryAge31To60Days;

        /**
        * 库龄 61-90 天的可售商品数量
        */
        private Integer inventoryAge61To90Days;

        /**
        * 库龄 91-180 天的可售商品数量
        */
        private Integer inventoryAge91To180Days;

        /**
        * 库龄 181-270 天的可售商品数量
        */
        private Integer inventoryAge181To270Days;

        /**
        * 库龄 271-365 天的可售商品数量
        */
        private Integer inventoryAge271To365Days;

        /**
        * 库龄 365 天以上的可售商品数量
        */
        private Integer inventoryAge365PlusDays;


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

        /**
        * 库龄 0-30 天的可售商品数量
        */
        @NotNull(message = "库龄 0不能为空")
        private Integer inventoryAge0To30Days;

        /**
        * 库龄 31-60 天的可售商品数量
        */
        @NotNull(message = "库龄 31不能为空")
        private Integer inventoryAge31To60Days;

        /**
        * 库龄 61-90 天的可售商品数量
        */
        @NotNull(message = "库龄 61不能为空")
        private Integer inventoryAge61To90Days;

        /**
        * 库龄 91-180 天的可售商品数量
        */
        @NotNull(message = "库龄 91不能为空")
        private Integer inventoryAge91To180Days;

        /**
        * 库龄 181-270 天的可售商品数量
        */
        @NotNull(message = "库龄 181不能为空")
        private Integer inventoryAge181To270Days;

        /**
        * 库龄 271-365 天的可售商品数量
        */
        @NotNull(message = "库龄 271不能为空")
        private Integer inventoryAge271To365Days;

        /**
        * 库龄 365 天以上的可售商品数量
        */
        @NotNull(message = "库龄 365 天以上的可售商品数量不能为空")
        private Integer inventoryAge365PlusDays;


    }


}