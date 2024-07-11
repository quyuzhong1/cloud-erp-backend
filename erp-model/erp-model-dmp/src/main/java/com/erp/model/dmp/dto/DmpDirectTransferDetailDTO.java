package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 中台直接调拨单详情表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-07-02
*/
@Data
@NoArgsConstructor
public class DmpDirectTransferDetailDTO implements Serializable {




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
        * 来源详情id
        */
        private String thirdDetailId;

        /**
        * 平台原始详情id
        */
        private String platformDetailId;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编码
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String skuName;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 单位
        */
        private String productUnit;

        /**
        * 调出仓库编码
        */
        private String outWarehouseCode;

        /**
        * 调出仓库名称
        */
        private String outWarehouseName;

        /**
        * 调出仓位
        */
        private String outWarehouseLocation;

        /**
        * 调入仓库编码
        */
        private String inWarehouseCode;

        /**
        * 调入仓库名称
        */
        private String inWarehouseName;

        /**
        * 调入仓位
        */
        private String inWarehouseLocation;

        /**
        * 备注
        */
        private String remark;

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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 64,message = "主表id最大长度不能超过64位")
        private String mainId;

        /**
        * 来源详情id
        */
        @NotBlank(message = "来源详情id不能为空")
        @Size(max = 64,message = "来源详情id最大长度不能超过64位")
        private String thirdDetailId;

        /**
        * 平台原始详情id
        */
        @NotBlank(message = "平台原始详情id不能为空")
        @Size(max = 64,message = "平台原始详情id最大长度不能超过64位")
        private String platformDetailId;

        /**
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 64,message = "产品id最大长度不能超过64位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 255,message = "产品名称最大长度不能超过255位")
        private String skuName;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 255,message = "单位最大长度不能超过255位")
        private String productUnit;

        /**
        * 调出仓库编码
        */
        @NotBlank(message = "调出仓库编码不能为空")
        @Size(max = 32,message = "调出仓库编码最大长度不能超过32位")
        private String outWarehouseCode;

        /**
        * 调出仓库名称
        */
        @NotBlank(message = "调出仓库名称不能为空")
        @Size(max = 255,message = "调出仓库名称最大长度不能超过255位")
        private String outWarehouseName;

        /**
        * 调出仓位
        */
        @NotBlank(message = "调出仓位不能为空")
        @Size(max = 32,message = "调出仓位最大长度不能超过32位")
        private String outWarehouseLocation;

        /**
        * 调入仓库编码
        */
        @NotBlank(message = "调入仓库编码不能为空")
        @Size(max = 32,message = "调入仓库编码最大长度不能超过32位")
        private String inWarehouseCode;

        /**
        * 调入仓库名称
        */
        @NotBlank(message = "调入仓库名称不能为空")
        @Size(max = 255,message = "调入仓库名称最大长度不能超过255位")
        private String inWarehouseName;

        /**
        * 调入仓位
        */
        @NotBlank(message = "调入仓位不能为空")
        @Size(max = 32,message = "调入仓位最大长度不能超过32位")
        private String inWarehouseLocation;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

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