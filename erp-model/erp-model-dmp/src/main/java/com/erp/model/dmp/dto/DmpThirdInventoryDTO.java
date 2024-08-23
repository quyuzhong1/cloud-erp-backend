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
 * 第三方仓库存请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
*/
@Data
@NoArgsConstructor
public class DmpThirdInventoryDTO implements Serializable {




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
        * 仓库平台类型
        */
        private String warehousePlatformType;

        /**
        * 来源平台（编码）：goodcang、iml
        */
        private String sourcePlatform;

        /**
        * erp授权Id
        */
        private String authId;

        /**
        * 第三方仓仓库代码
        */
        private String platformWarehouseCode;

        /**
        * 第三方仓仓库名称
        */
        private String platformWarehouseName;

        /**
        * 平台sku
        */
        private String productSku;

        /**
        * 尾程在途数量
        */
        private Integer onway;

        /**
        * 总尾程在途数量
        */
        private Integer totalOnway;

        /**
        * 发货在途数量
        */
        private Integer transferOnway;

        /**
        * 待上架数量
        */
        private Integer pending;

        /**
        * 可售数量
        */
        private Integer sellable;

        /**
        * 不合格数量
        */
        private Integer unsellable;

        /**
        * 备货数量
        */
        private Integer stocking;

        /**
        * 缺货数量
        */
        private Integer piNoStock;

        /**
        * 待出库数量
        */
        private Integer reserved;

        /**
        * 历史出库数量
        */
        private Integer shipped;

        /**
        * 待确认数量
        */
        private Integer unconfirmed;

        /**
        * 冻结数量
        */
        private Integer piFreeze;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
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
        * 仓库平台类型
        */
        @NotBlank(message = "仓库平台类型不能为空")
        @Size(max = 32,message = "仓库平台类型最大长度不能超过32位")
        private String warehousePlatformType;

        /**
        * 来源平台（编码）：goodcang、iml
        */
        @NotBlank(message = "来源平台（编码）：goodcang、iml不能为空")
        @Size(max = 32,message = "来源平台（编码）：goodcang、iml最大长度不能超过32位")
        private String sourcePlatform;

        /**
        * erp授权Id
        */
        @NotBlank(message = "erp授权Id不能为空")
        @Size(max = 64,message = "erp授权Id最大长度不能超过64位")
        private String authId;

        /**
        * 第三方仓仓库代码
        */
        @NotBlank(message = "第三方仓仓库代码不能为空")
        @Size(max = 64,message = "第三方仓仓库代码最大长度不能超过64位")
        private String platformWarehouseCode;

        /**
        * 第三方仓仓库名称
        */
        @NotBlank(message = "第三方仓仓库名称不能为空")
        @Size(max = 500,message = "第三方仓仓库名称最大长度不能超过500位")
        private String platformWarehouseName;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 64,message = "平台sku最大长度不能超过64位")
        private String productSku;

        /**
        * 尾程在途数量
        */
        @NotNull(message = "尾程在途数量不能为空")
        private Integer onway;

        /**
        * 总尾程在途数量
        */
        @NotNull(message = "总尾程在途数量不能为空")
        private Integer totalOnway;

        /**
        * 发货在途数量
        */
        @NotNull(message = "发货在途数量不能为空")
        private Integer transferOnway;

        /**
        * 待上架数量
        */
        @NotNull(message = "待上架数量不能为空")
        private Integer pending;

        /**
        * 可售数量
        */
        @NotNull(message = "可售数量不能为空")
        private Integer sellable;

        /**
        * 不合格数量
        */
        @NotNull(message = "不合格数量不能为空")
        private Integer unsellable;

        /**
        * 备货数量
        */
        @NotNull(message = "备货数量不能为空")
        private Integer stocking;

        /**
        * 缺货数量
        */
        @NotNull(message = "缺货数量不能为空")
        private Integer piNoStock;

        /**
        * 待出库数量
        */
        @NotNull(message = "待出库数量不能为空")
        private Integer reserved;

        /**
        * 历史出库数量
        */
        @NotNull(message = "历史出库数量不能为空")
        private Integer shipped;

        /**
        * 待确认数量
        */
        @NotNull(message = "待确认数量不能为空")
        private Integer unconfirmed;

        /**
        * 冻结数量
        */
        @NotNull(message = "冻结数量不能为空")
        private Integer piFreeze;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}