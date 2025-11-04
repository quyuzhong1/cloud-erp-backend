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
 * 资产盘点表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetStocktakingDTO implements Serializable {


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

         public TabListDTO(String tabFlag, Integer count) {
             this.tabFlag = tabFlag;
             this.count = count;
         }

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
        * 盘点单号
        */
        private String code;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源ID（盘点方案ID）
        */
        private String sourceId;

        /**
        * 盘点方案名称（通过sourceId关联查询）
        */
        private String stocktakingPlanName;

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
        * 盘点单号
        */
        private String code;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源ID（盘点方案ID）
        */
        private String sourceId;

        /**
        * 盘点方案名称（通过sourceId关联查询）
        */
        private String stocktakingPlanName;

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
        * 明细列表
        */
        private List<AssetStocktakingDetailDTO.ViewDTO> detailList;

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
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 100,message = "来源单号最大长度不能超过100位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 200,message = "来源类型最大长度不能超过200位")
        private String sourceType;

        /**
        * 来源ID
        */
        @NotBlank(message = "来源ID不能为空")
        @Size(max = 19,message = "来源ID最大长度不能超过19位")
        private String sourceId;

        /**
        * 资产组织ID
        */
        @NotBlank(message = "资产组织ID不能为空")
        @Size(max = 19,message = "资产组织ID最大长度不能超过19位")
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        @NotBlank(message = "资产组织名称不能为空")
        @Size(max = 50,message = "资产组织名称最大长度不能超过50位")
        private String assetOrgName;

        /**
        * 描述
        */
        @NotBlank(message = "描述不能为空")
        @Size(max = 500,message = "描述最大长度不能超过500位")
        private String remark;

        /**
        * 明细列表
        */
        private List<AssetStocktakingDetailDTO.UpdateDTO> detailList;

    }

    /**
    * 下拉列表DTO
    */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropDownDTO {

        /**
        * 主键id
        */
        private String id;

        /**
        * 盘点单号
        */
        private String code;

        /**
        * 来源单号
        */
        private String sourceCode;

    }


}