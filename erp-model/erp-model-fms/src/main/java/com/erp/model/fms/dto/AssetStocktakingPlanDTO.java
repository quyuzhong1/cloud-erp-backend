package com.erp.model.fms.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 资产盘点方案表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetStocktakingPlanDTO implements Serializable {


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
        * 盘点方案编号
        */
        private String code;

        /**
        * 盘点方案名称
        */
        private String planName;

        /**
        * 资产组织ID
        */
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        private String assetOrgName;

        /**
        * 描述
        */
        private String remark;

        /**
        * 资产类别（机器设备）字符串数组
        */
        private String assetCategories;

        /**
        * 使用部门ID字符串数组
        */
        private String useDeptIds;

        /**
        * 资产位置ID字符串数组
        */
        private String assetLocationIds;

        /**
        * 卡片编码-开始
        */
        private String cardCodeStart;

        /**
        * 卡片编码-结束
        */
        private String cardCodeEnd;


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
        * 盘点方案编号
        */
        private String code;

        /**
        * 盘点方案名称
        */
        private String planName;

        /**
        * 资产组织ID
        */
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        private String assetOrgName;

        /**
        * 描述
        */
        private String remark;

        /**
        * 资产类别（机器设备）字符串数组
        */
        private String assetCategories;

        /**
        * 使用部门ID字符串数组
        */
        private String useDeptIds;

        /**
        * 资产位置ID字符串数组
        */
        private String assetLocationIds;

        /**
        * 卡片编码-开始
        */
        private String cardCodeStart;

        /**
        * 卡片编码-结束
        */
        private String cardCodeEnd;


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
        * 盘点方案名称
        */
        @NotBlank(message = "盘点方案名称不能为空")
        @Size(max = 100,message = "盘点方案名称最大长度不能超过100位")
        private String planName;

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

        /**
        * 描述
        */
        @NotBlank(message = "描述不能为空")
        @Size(max = 500,message = "描述最大长度不能超过500位")
        private String remark;

        /**
        * 资产类别（机器设备）字符串数组
        */
        private String assetCategories;

        /**
        * 使用部门ID字符串数组
        */
        private String useDeptIds;

        /**
        * 资产位置ID字符串数组
        */
        private String assetLocationIds;

        /**
        * 卡片编码-开始
        */
        @Size(max = 50,message = "卡片编码最大长度不能超过50位")
        private String cardCodeStart;

        /**
        * 卡片编码-结束
        */
        @Size(max = 50,message = "卡片编码最大长度不能超过50位")
        private String cardCodeEnd;


    }


}