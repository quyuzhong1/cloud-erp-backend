package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname SupplierPhaseDTO
 * @Date 2023-03-16 12:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierPhaseDTO implements Serializable {


    /**
     * 添加阶段
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends PermissionsDTO implements Serializable {
        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商id不能为空")
        private String supplierId;


        /**
         * 当前阶段
         */
        private String currentPhase;


        /**
         * 目标阶段
         */
        @NotBlank(message = "目标阶段不能为空")
        private String targetPhase;

        /**
         * 说明
         */
        private String description;


        /**
         * 当前等级
         */
        private String currentGradeId;

        /**
         * 目标等级
         */
        private String targetGradeId;



        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;
    }

    /**
     * 添加阶段
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 当前阶段
         */
        @NotBlank(message = "当前阶段不能为空")
        private String currentPhase;


    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        private List<String> attachmentUrlList;

        private List<String> attachmentNameList;

    }


    /**
     * 阶段分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 供应商 阶段表 id
         */
        private String id;


        /**
         * 供应商表id
         */
        private String supplierId;


        /**
         * 供应商表id
         */
        private String supplierName;

        /**
         * 供应商编号
         */
        private String supplierCode;


        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;

        /**
         * 当前阶段
         */
        private String currentPhase;

        /**
         * 当前阶段名
         */
        private String currentPhaseName;


        /**
         * 目标阶段
         */
        private String targetPhase;

        /**
         * 目标阶段名
         */
        private String targetPhaseName;

        /**
         * 说明
         */
        private String description;

        /**
         * 当前等级
         */
        private String currentGradeId;
        /**
         * 当前等级名称
         */
        private String currentGradeName;

        /**
         * 目标等级
         */
        private String targetGradeId;

        /**
         * 目标等级名称
         */
        private String targetGradeName;

        /**
         * 审核状态
         */
        private String approveStatus;


        /**
         * 审核状态名
         */
        private String approveStatusName;

        /**
         * 创建人id
         */
        private String createUserName;

        /**
         * 审核完成人
         */
        private String approveUserName;
        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }


    /**
     * 阶段分页查询参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;


//        @StateEnumValue(strValues = {"all", "waitApprove"}, message = "搜索类型有误")
        private String searchType;


        /**
         * 分类id集合
         */
        private List<String> categoryIdList;

        /**
         * 状态
         */
        private List<String> approveStatusList;

        /**
         * name
         */
        private String name;
        /**
         * 选中导出字段
         */
        private List<SupplierPhaseDTO.ExportField> fieldList;
    }

    /**
     * 导出字段
     */
    @Data
    @NoArgsConstructor
    public static class ExportField {

        /**
         * 字段
         */
        private String field;

        /**
         * 字段名称
         */
        private String fieldName;
    }

    @Data
    @NoArgsConstructor
    public static class TabFlagDTO {

        /**
         * tabFlag
         */
        private String tabFlag;

        /**
         * tabFlagName
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }


    @Data
    @NoArgsConstructor
    public static class ApproveCountDTO {
        /**
         * 类型
         */
        private String approveStatus;

        /**
         * 数量
         */
        private Integer count;
    }

}
