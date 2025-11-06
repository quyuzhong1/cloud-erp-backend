package com.erp.model.fms.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 盘盈盘亏单主表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetProfitLossDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
          * 类型名称
          */
         private String tabFlagName;

         /**
         * 数量
         */
         private Integer count;

     }
     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {

         /**
         * 页面高级查询
         */
         private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
            * sqlMap 默认key default
        */
        private Map<String,String> sqlMap;

     }
    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        private String approveStatus;

        private String approveUserId;

        private String approveUserName;

        private LocalDateTime approveTime;

        private Boolean invalidStatus;

        private String invalidRemark;

        private LocalDateTime invalidTime;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 单据号
        */
        private String code;

        /**
        * 单据类型（盘盈 盘亏）
        */
        private String docType;

        /**
        * 盘点方案ID
        */
        private String planId;

        /**
        * 盘点方案code
        */
        private String planCode;

        /**
        * 资产组织ID
        */
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        private String assetOrgName;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 单据类型名称（盘盈/盘亏）
        */
        private String docTypeName;

        /**
        * 盘点方案名称
        */
        private String planName;

        /**
        * 资产类别
        */
        private String assetCategory;

        /**
        * 资产类别名称
        */
        private String assetCategoryName;

        /**
        * 卡片编码
        */
        private String cardCode;

        /**
        * 卡片ID
        */
        private String cardId;

        /**
        * 资产名称
        */
        private String assetName;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 计量单位
        */
        private String unit;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 创建人
        */
        private String createUserName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }

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

        private String approveStatus;

        private String approveUserId;

        private String approveUserName;

        private LocalDateTime approveTime;

        private Boolean invalidStatus;

        private String invalidRemark;

        private LocalDateTime invalidTime;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 单据号
        */
        private String code;

        /**
        * 单据类型（盘盈 盘亏）
        */
        private String docType;

        /**
        * 盘点方案ID
        */
        private String planId;

        /**
        * 盘点方案code
        */
        private String planCode;

        /**
        * 资产组织ID
        */
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        private String assetOrgName;

        /**
        * 单据类型名称（盘盈/盘亏）
        */
        private String docTypeName;

        /**
        * 盘点方案名称
        */
        private String planName;

        /**
        * 明细列表
        */
        private List<AssetProfitLossDetailDTO.ViewDTO> detailList;


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

        private LocalDateTime invalidTime;

        /**
        * 来源单号
        */
        @Size(max = 100,message = "来源单号最大长度不能超过100位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @Size(max = 200,message = "来源类型最大长度不能超过200位")
        private String sourceType;

        /**
        * 来源ID
        */
        @Size(max = 19,message = "来源ID最大长度不能超过19位")
        private String sourceId;

        /**
        * 单据类型（盘盈 盘亏）
        */
        @NotBlank(message = "单据类型（盘盈 盘亏）不能为空")
        @Size(max = 10,message = "单据类型（盘盈 盘亏）最大长度不能超过10位")
        private String docType;

        /**
        * 盘点方案ID
        */
        @NotBlank(message = "盘点方案ID不能为空")
        @Size(max = 19,message = "盘点方案ID最大长度不能超过19位")
        private String planId;

        /**
        * 盘点方案code
        */
        @Size(max = 32,message = "盘点方案code最大长度不能超过32位")
        private String planCode;

        /**
        * 资产组织ID
        */
        @NotBlank(message = "资产组织ID不能为空")
        @Size(max = 19,message = "资产组织ID最大长度不能超过19位")
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        @Size(max = 50,message = "资产组织名称最大长度不能超过50位")
        private String assetOrgName;


    }

    /**
     * 下推到资产卡片前的列表DTO
     */
    @Data
    @NoArgsConstructor
    public static class PushToCardListDTO {
        
        /**
         * 盘盈盘亏单号
         */
        private String code;
        
        /**
         * 来源单号（资产盘点表单号）
         */
        private String sourceCode;
        
        /**
         * 单据类型（盘盈/盘亏）
         */
        private String docType;
        
        /**
         * 单据类型名称
         */
        private String docTypeName;
        
        /**
         * 资产组织ID
         */
        private String assetOrgId;
        
        /**
         * 资产组织名称
         */
        private String assetOrgName;
        
        /**
         * 明细ID
         */
        private String detailId;
        
        /**
         * 资产类别
         */
        private String assetCategory;
        
        /**
         * 资产类别名称
         */
        private String assetCategoryName;
        
        /**
         * 卡片编码
         */
        private String cardCode;
        
        /**
         * 资产名称
         */
        private String assetName;
        
        /**
         * 资产编码
         */
        private String assetCode;
        
        /**
         * 计量单位
         */
        private String unit;
        
        /**
         * 数量（差异数量）
         */
        private Integer qty;
        
        /**
         * 实际资产位置ID
         */
        private String actualLocation;
        
        /**
         * 实际资产位置名称
         */
        private String actualLocationName;
        
        /**
         * 使用部门ID
         */
        private String useDeptId;
        
        /**
         * 使用部门名称
         */
        private String useDeptName;
        
        /**
         * 费用项目
         */
        private String costType;
        
        /**
         * 费用项目名称
         */
        private String costTypeName;
        
        /**
         * 供应商ID
         */
        private String supplierId;
        
        /**
         * 供应商名称
         */
        private String supplierName;
    }

    /**
     * 下推到资产卡片请求DTO（明细维度的参数列表）
     */
    @Data
    @NoArgsConstructor
    public static class PushToCardDTO {
        
        /**
         * 下推的资产卡片列表（必须是用户已选择并填写完整的数据）
         */
        @NotEmpty(message = "下推资产卡片列表不能为空")
        private List<PushToCardListDTO> detailList;
    }


}