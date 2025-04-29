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
 * 第三方库存流水表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-04-25
*/
@Data
@NoArgsConstructor
public class DmpThirdInventoryTransFlowDTO implements Serializable {




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

        /**
        * 库存流水ID
        */
        private String thirdId;

        /**
        * 关联单号
        */
        private String referenceNo;

        /**
        * 应用编码
        */
        private String applicationCode;

        /**
        * 应用编码描述
        */
        private String applicationCodeDesc;

        /**
        * 商品SKU
        */
        private String productSku;

        /**
        * 仓库编码
        */
        private String warehouseCode;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 库存变更类型
        */
        private Integer inventoryChangeType;

        /**
        * 库存变更类型描述
        */
        private String inventoryChangeTypeDesc;

        /**
        * 商品品质:0=全部,1=良品,2=不良品
        */
        private Integer productType;

        /**
        * 库存变更数量
        */
        private Integer changeQty;

        /**
        * 备注
        */
        private String remark;

        /**
        * 库存变更时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 授权ID
        */
        private String authId;


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
        @NotBlank(message = "唯一字段md5值不能为空")
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        @NotBlank(message = "数据字段md5值不能为空")
        private String dataEncrypt;

        /**
        * 库存流水ID
        */
        @NotBlank(message = "库存流水ID不能为空")
        @Size(max = 64,message = "库存流水ID最大长度不能超过64位")
        private String thirdId;

        /**
        * 关联单号
        */
        @NotBlank(message = "关联单号不能为空")
        @Size(max = 64,message = "关联单号最大长度不能超过64位")
        private String referenceNo;

        /**
        * 应用编码
        */
        @NotBlank(message = "应用编码不能为空")
        @Size(max = 10,message = "应用编码最大长度不能超过10位")
        private String applicationCode;

        /**
        * 应用编码描述
        */
        @NotBlank(message = "应用编码描述不能为空")
        @Size(max = 50,message = "应用编码描述最大长度不能超过50位")
        private String applicationCodeDesc;

        /**
        * 商品SKU
        */
        @NotBlank(message = "商品SKU不能为空")
        @Size(max = 64,message = "商品SKU最大长度不能超过64位")
        private String productSku;

        /**
        * 仓库编码
        */
        @NotBlank(message = "仓库编码不能为空")
        @Size(max = 32,message = "仓库编码最大长度不能超过32位")
        private String warehouseCode;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 50,message = "仓库名称最大长度不能超过50位")
        private String warehouseName;

        /**
        * 库存变更类型
        */
        @NotNull(message = "库存变更类型不能为空")
        private Integer inventoryChangeType;

        /**
        * 库存变更类型描述
        */
        @NotBlank(message = "库存变更类型描述不能为空")
        @Size(max = 50,message = "库存变更类型描述最大长度不能超过50位")
        private String inventoryChangeTypeDesc;

        /**
        * 商品品质:0=全部,1=良品,2=不良品
        */
        @NotNull(message = "商品品质:0=全部,1=良品,2=不良品不能为空")
        private Integer productType;

        /**
        * 库存变更数量
        */
        @NotNull(message = "库存变更数量不能为空")
        private Integer changeQty;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        private String remark;

        /**
        * 库存变更时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 来源ID
        */
        @NotBlank(message = "来源ID不能为空")
        @Size(max = 64,message = "来源ID最大长度不能超过64位")
        private String sourceId;

        /**
        * 授权ID
        */
        @NotBlank(message = "授权ID不能为空")
        @Size(max = 19,message = "授权ID最大长度不能超过19位")
        private String authId;


    }


}