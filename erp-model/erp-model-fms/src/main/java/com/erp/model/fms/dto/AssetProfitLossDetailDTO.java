package com.erp.model.fms.dto;

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
 * 盘盈盘亏单明细表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetProfitLossDetailDTO implements Serializable {




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
        * 来源明细ID
        */
        private String sourceDetailId;

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * 资产类别（机器设备）
        */
        private String assetCategory;

        /**
        * 卡片ID
        */
        private String cardId;

        /**
        * 卡片明细ID
        */
        private String cardDetailId;

        /**
        * 卡片编码
        */
        private String cardCode;

        /**
        * 资产ID
        */
        private String assetId;

        /**
        * 资产名称
        */
        private String assetName;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 计量单位 PCS
        */
        private String unit;

        /**
        * 账存数量
        */
        private Integer bookQty;

        /**
        * 资产实际数量
        */
        private Integer actualQty;

        /**
        * 差异数量
        */
        private Integer diffQty;

        /**
        * 账存资产位置
        */
        private String bookLocation;

        /**
        * 实际资产位置
        */
        private String actualLocation;


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
        * 来源明细ID
        */
        @NotBlank(message = "来源明细ID不能为空")
        @Size(max = 19,message = "来源明细ID最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 主表ID
        */
        @NotBlank(message = "主表ID不能为空")
        @Size(max = 19,message = "主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * 资产类别（机器设备）
        */
        @NotBlank(message = "资产类别（机器设备）不能为空")
        @Size(max = 50,message = "资产类别（机器设备）最大长度不能超过50位")
        private String assetCategory;

        /**
        * 卡片ID
        */
        @NotBlank(message = "卡片ID不能为空")
        @Size(max = 19,message = "卡片ID最大长度不能超过19位")
        private String cardId;

        /**
        * 卡片明细ID
        */
        @NotBlank(message = "卡片明细ID不能为空")
        @Size(max = 19,message = "卡片明细ID最大长度不能超过19位")
        private String cardDetailId;

        /**
        * 卡片编码
        */
        @NotBlank(message = "卡片编码不能为空")
        @Size(max = 50,message = "卡片编码最大长度不能超过50位")
        private String cardCode;

        /**
        * 资产ID
        */
        @NotBlank(message = "资产ID不能为空")
        @Size(max = 19,message = "资产ID最大长度不能超过19位")
        private String assetId;

        /**
        * 资产名称
        */
        @NotBlank(message = "资产名称不能为空")
        @Size(max = 200,message = "资产名称最大长度不能超过200位")
        private String assetName;

        /**
        * 资产编码
        */
        @NotBlank(message = "资产编码不能为空")
        @Size(max = 50,message = "资产编码最大长度不能超过50位")
        private String assetCode;

        /**
        * 计量单位 PCS
        */
        @NotBlank(message = "计量单位 PCS不能为空")
        @Size(max = 20,message = "计量单位 PCS最大长度不能超过20位")
        private String unit;

        /**
        * 账存数量
        */
        @NotNull(message = "账存数量不能为空")
        private Integer bookQty;

        /**
        * 资产实际数量
        */
        @NotNull(message = "资产实际数量不能为空")
        private Integer actualQty;

        /**
        * 差异数量
        */
        @NotNull(message = "差异数量不能为空")
        private Integer diffQty;

        /**
        * 账存资产位置
        */
        @NotBlank(message = "账存资产位置不能为空")
        @Size(max = 100,message = "账存资产位置最大长度不能超过100位")
        private String bookLocation;

        /**
        * 实际资产位置
        */
        @NotBlank(message = "实际资产位置不能为空")
        @Size(max = 100,message = "实际资产位置最大长度不能超过100位")
        private String actualLocation;


    }


}