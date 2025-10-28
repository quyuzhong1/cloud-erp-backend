package com.erp.model.fms.dto;

import java.time.LocalDate;
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
 * 资产盘点明细表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetStocktakingDetailDTO implements Serializable {




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
        * 单位 PCS
        */
        private String unit;

        /**
        * 资产状态（正常使用）
        */
        private String assetStatus;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 账存数量
        */
        private Integer bookQty;

        /**
        * 初盘数量
        */
        private Integer firstCountQty;

        /**
        * 初盘差异
        */
        private Integer firstDiffQty;

        /**
        * 账存资产位置
        */
        private String bookLocation;

        /**
        * 初盘变动位置
        */
        private String firstChangeLocation;

        /**
        * 初盘人ID
        */
        private String firstCountUserId;

        /**
        * 初盘人姓名
        */
        private String firstCountUserName;

        /**
        * 初盘日期
        */
        private LocalDate firstCountDate;

        /**
        * 是否复盘
        */
        private Boolean isRecount;

        /**
        * 复盘数量
        */
        private Integer recountQty;

        /**
        * 复盘差异
        */
        private Integer recountDiffQty;

        /**
        * 复盘变动位置
        */
        private String recountChangeLocation;

        /**
        * 复盘人ID
        */
        private String recountUserId;

        /**
        * 复盘人姓名
        */
        private String recountUserName;

        /**
        * 复盘日期
        */
        private LocalDate recountDate;

        /**
        * 最终差异
        */
        private Integer finalDiffQty;

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
        * 主表ID
        */
        @NotBlank(message = "主表ID不能为空")
        @Size(max = 19,message = "主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * 资产类别（机器设备） /fms/dict/list?key=assetCategory
        */
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
        * 单位 PCS
        */
        @NotBlank(message = "单位 PCS不能为空")
        @Size(max = 20,message = "单位 PCS最大长度不能超过20位")
        private String unit;

        /**
        * 资产状态（正常使用）
        */
        @NotBlank(message = "资产状态（正常使用）不能为空")
        @Size(max = 20,message = "资产状态（正常使用）最大长度不能超过20位")
        private String assetStatus;

        /**
        * 资产编码
        */
        @NotBlank(message = "资产编码不能为空")
        @Size(max = 50,message = "资产编码最大长度不能超过50位")
        private String assetCode;

        /**
        * 账存数量
        */
        @NotNull(message = "账存数量不能为空")
        private Integer bookQty;

        /**
        * 初盘数量
        */
        @NotNull(message = "初盘数量不能为空")
        private Integer firstCountQty;

        /**
        * 初盘差异
        */
        @NotNull(message = "初盘差异不能为空")
        private Integer firstDiffQty;

        /**
        * 账存资产位置
        */
        @NotBlank(message = "账存资产位置不能为空")
        @Size(max = 100,message = "账存资产位置最大长度不能超过100位")
        private String bookLocation;

        /**
        * 初盘变动位置
        */
        @NotBlank(message = "初盘变动位置不能为空")
        @Size(max = 100,message = "初盘变动位置最大长度不能超过100位")
        private String firstChangeLocation;

        /**
        * 初盘人ID
        */
        @NotBlank(message = "初盘人ID不能为空")
        @Size(max = 19,message = "初盘人ID最大长度不能超过19位")
        private String firstCountUserId;

        /**
        * 初盘人姓名
        */
        @NotBlank(message = "初盘人姓名不能为空")
        @Size(max = 50,message = "初盘人姓名最大长度不能超过50位")
        private String firstCountUserName;

        /**
        * 初盘日期
        */
        private LocalDate firstCountDate;

        /**
        * 是否复盘
        */
        @NotNull(message = "是否复盘不能为空")
        private Boolean isRecount;

        /**
        * 复盘数量
        */
        @NotNull(message = "复盘数量不能为空")
        private Integer recountQty;

        /**
        * 复盘差异
        */
        @NotNull(message = "复盘差异不能为空")
        private Integer recountDiffQty;

        /**
        * 复盘变动位置
        */
        @NotBlank(message = "复盘变动位置不能为空")
        @Size(max = 100,message = "复盘变动位置最大长度不能超过100位")
        private String recountChangeLocation;

        /**
        * 复盘人ID
        */
        @NotBlank(message = "复盘人ID不能为空")
        @Size(max = 19,message = "复盘人ID最大长度不能超过19位")
        private String recountUserId;

        /**
        * 复盘人姓名
        */
        @NotBlank(message = "复盘人姓名不能为空")
        @Size(max = 50,message = "复盘人姓名最大长度不能超过50位")
        private String recountUserName;

        /**
        * 复盘日期
        */
        private LocalDate recountDate;

        /**
        * 最终差异
        */
        @NotNull(message = "最终差异不能为空")
        private Integer finalDiffQty;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}