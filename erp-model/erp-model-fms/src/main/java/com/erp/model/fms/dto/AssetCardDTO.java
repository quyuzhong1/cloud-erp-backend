package com.erp.model.fms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 资产卡片主表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetCardDTO implements Serializable {


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
        * 卡片来源（采购收货 手工建卡 盘盈建卡）
        */
        private String sourceType;
        
        /**
        * 卡片来源名称
        */
        private String sourceTypeName;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 资产组织ID
        */
        private String orgId;

        /**
        * 资产组织名称
        */
        private String orgName;

        /**
        * 资产类型（机器设备）
        */
        private String type;
        
        /**
        * 资产类型名称
        */
        private String typeName;

        /**
        * 卡片编码
        */
        private String code;

        /**
        * 资产状态（正常使用）
        */
        private String status;
        
        /**
        * 资产状态名称
        */
        private String statusName;

        /**
        * 变动方式（购入 盘盈）
        */
        private String changeMethod;
        
        /**
        * 变动方式名称
        */
        private String changeMethodName;

        /**
        * 资产名称
        */
        private String name;

        /**
        * 计量单位
        */
        private String unit;
        
        /**
        * 计量单位名称
        */
        private String unitName;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 开始使用日期
        */
        private LocalDate startUseDate;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 资产位置ID
        */
        private String assetLocationId;

        /**
        * 处置情况（空 部分处置 完全清理）
        */
        private String disposalStatus;

        /**
        * 备注
        */
        private String remark;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;
        
        // 明细表字段
        /**
        * 明细ID
        */
        private String detailId;
        
        /**
        * 来源明细ID
        */
        private String sourceDetailId;
        
        /**
        * 明细资产编码
        */
        private String detailAssetCode;
        
        /**
        * 明细资产位置ID
        */
        private String detailAssetLocationId;
        
        /**
        * 明细数量
        */
        private Integer detailQty;
        
        /**
        * 供应商ID
        */
        private String supplierId;
        
        /**
        * 供应商名称
        */
        private String supplierName;
        
        /**
        * 使用部门名称
        */
        private String useDeptName;
        
        /**
        * 使用部门ID
        */
        private String useDeptId;
        
        /**
        * 费用项目
        */
        private String costType;
        
        /**
        * 费用项目名称
        */
        private String costTypeName;
        
        /**
        * 明细备注
        */
        private String detailRemark;
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
        * 卡片来源（采购收货 手工建卡 盘盈建卡）
        */
        private String sourceType;
        
        /**
        * 卡片来源名称
        */
        private String sourceTypeName;

        /**
        * 来源ID
        */
        private String sourceId;

        /**
        * 资产组织ID
        */
        private String orgId;

        /**
        * 资产组织名称
        */
        private String orgName;

        /**
        * 资产类型（机器设备）
        */
        private String type;
        
        /**
        * 资产类型名称
        */
        private String typeName;

        /**
        * 卡片编码
        */
        private String code;

        /**
        * 资产状态（正常使用）
        */
        private String status;
        
        /**
        * 资产状态名称
        */
        private String statusName;

        /**
        * 变动方式（购入 盘盈）
        */
        private String changeMethod;
        
        /**
        * 变动方式名称
        */
        private String changeMethodName;

        /**
        * 资产名称
        */
        private String name;

        /**
        * 计量单位
        */
        private String unit;
        
        /**
        * 计量单位名称
        */
        private String unitName;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 开始使用日期
        */
        private LocalDate startUseDate;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 资产位置ID
        */
        private String assetLocationId;

        /**
        * 处置情况（空 部分处置 完全清理）
        */
        private String disposalStatus;

        /**
        * 备注
        */
        private String remark;

        /**
        * 明细列表
        */
        private List<AssetCardDetailDTO.ViewDTO> detailList;

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
        * 卡片来源（采购收货 手工建卡 盘盈建卡） /fms/dict/list?key=cardSource
        */
        @Size(max = 200,message = "卡片来源（采购收货 手工建卡 盘盈建卡）最大长度不能超过200位")
        private String sourceType;

        /**
        * 来源ID
        */
        @Size(max = 19,message = "来源ID最大长度不能超过19位")
        private String sourceId;

        /**
        * 资产组织ID /sys/company/list
        */
        @NotBlank(message = "资产组织ID不能为空")
        @Size(max = 19,message = "资产组织ID最大长度不能超过19位")
        private String orgId;

        /**
        * 资产组织名称 /sys/company/list
        */
        @Size(max = 50,message = "资产组织名称最大长度不能超过50位")
        private String orgName;

        /**
        * 资产类型（机器设备）
        */
        @NotBlank(message = "资产类型（机器设备）不能为空")
        @Size(max = 50,message = "资产类型（机器设备）最大长度不能超过50位")
        private String type;

        /**
        * 资产状态（正常使用）
        */
        @NotBlank(message = "资产状态（正常使用）不能为空")
        @Size(max = 20,message = "资产状态（正常使用）最大长度不能超过20位")
        private String status;

        /**
        * 变动方式（购入 盘盈）
        */
        @NotBlank(message = "变动方式（购入 盘盈）不能为空")
        @Size(max = 20,message = "变动方式（购入 盘盈）最大长度不能超过20位")
        private String changeMethod;

        /**
        * 资产名称
        */
        @NotBlank(message = "资产名称不能为空")
        @Size(max = 200,message = "资产名称最大长度不能超过200位")
        private String name;

        /**
        * 计量单位
        */
        @NotBlank(message = "计量单位不能为空")
        @Size(max = 20,message = "计量单位最大长度不能超过20位")
        private String unit;

        /**
         * 备注
         */
        private String remark;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 开始使用日期
        */
        @NotNull(message = "开始使用日期不能为空")
        private LocalDate startUseDate;

        /**
         * 明细
         */
        private List<AssetCardDetailDTO.AddDTO>detailList;

    }

    /**
    * 获取已审核资产卡片查询参数
    */
    @Data
    @NoArgsConstructor
    public static class QueryApprovedDTO {
        /**
        * 资产卡片编码（模糊查询）
        */
        private String code;

        /**
        * 开始编码（可空）
        */
        private String startCode;
    }

    /**
    * 已审核资产卡片返回结果
    */
    @Data
    @NoArgsConstructor
    public static class ApprovedCardDTO {
        /**
         * 资产卡片ID
         */
        private String id;

        /**
         * 资产卡片编码
         */
        private String code;

        /**
         * 资产名称
         */
        private String name;

        /**
         * 资产类型
         */
        private String type;

        /**
         * 资产类型名称
         */
        private String typeName;
        /**
         * 资产组织ID
         */
        private String orgId;
        /**
         * 资产组织名称
         */
        private String orgName;
        /**
         * 资产状态（正常使用）
         */
        private String status;
        private String statusName;
        /**
         * 变动方式（购入 盘盈）
         */
        private String changeMethod;
        private String changeMethodName;
        /**
         * 计量单位
         */
        private String unit;
        private String unitName;
        /**
         * 数量
         */
        private Integer qty;
    }
}