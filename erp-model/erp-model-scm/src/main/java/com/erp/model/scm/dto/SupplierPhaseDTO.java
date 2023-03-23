package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname SupplierPhaseDTO
 * @Description TODO
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
    public static class AddDTO {
        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商id不能为空")
        private String supplierId;

        /**
         * 操作类型
         */
        @NotBlank(message = "操作类型不能为空")
        @StateEnumValue(strValues = {"upgrade", "degrade"}, message = "操作类型有误")
        private String type;

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
         * 附件地址
         */
        private List<String> attachmentUrlList;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO  extends AddDTO{

        @NotBlank(message = "id不能为空")
        private String id;

        private List<String> attachmentUrl;

    }


    /**
     * 阶段分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{
        /**
         * 供应商 阶段表 id
         */
        private String id;


        /**
         * 供应商表id
         */
        private String SupplierId;


        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;

        /**
         * 操作类型
         */
        private String type;

        /**
         * 当前阶段
         */
        private String currentPhase;


        /**
         * 目标阶段
         */
        private String targetPhase;

        /**
         * 说明
         */
        private String description;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 创建人id
         */
        private String createUserName;

        /**
         * 审核人
         */
        private String approvedBy;

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
    public static class PagingParamDTO extends SortDTO {

        @StateEnumValue(strValues = {"all","waitApprove"}, message = "搜索类型有误")
        private String searchType;

        /**
         * 供应商名
         */
        private String name;


        /**
         * 分类id集合
         */
        private List<String> categoryIdList;


        /**
         * 阶段列表
         */
        private List<String> phaseList;



        /**
         * 联系人名
         */
        private String contactPerson;


        /**
         * 联系人
         */
        private String contactTelNumber;

    }


}
