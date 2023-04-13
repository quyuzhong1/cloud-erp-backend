package com.erp.model.workflow.dto;

import com.common.business.dto.base.SortDTO;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 收货明细DTO
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
@NoArgsConstructor
public class WorkOptionDTO extends BaseEntity<WorkOptionDTO> {

    /**
     * 代办列表
     */
    @Data
    @NoArgsConstructor
    public static class PendingViewDTO {
        /**
         * 模块名称
         */
        private String name;

        /**
         * 单据数量
         */
        private String count;

        /**
         * 状态名称
         */
        private String statusName;
    }

    /**
     * 常用列表
     */
    @Data
    @NoArgsConstructor
    public static class FrequentlyViewDTO {
        /**
         * 模块名称
         */
        private String name;

        /**
         * 状态名称
         */
        private String statusName;
    }

    /**
     * 立项阶段列表
     */
    @Data
    @NoArgsConstructor
    public static class StageViewDTO {
        /**
         * 阶段名称
         */
        private String stageName;

        /**
         * 单据数量
         */
        private String count;


    }

    /**
     * 审批中心
     */
    @Data
    @NoArgsConstructor
    public static class ApproveViewDTO {
        /**
         * 单据来源
         */
        private String source;

        /**
         * 单据名称
         */
        private String name;

        /**
         * 已审核时长
         */
        private String approveDuration;

        /**
         * 申请人
         */
        private String createUserName;

        /**
         * 申请时间
         */
        private String createTime;

        /**
         * 审核状态
         */
        private String status;
    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class ApproveViewParamDTO extends SortDTO {
        /**
         * 单据名称
         */
        private String name;

        /**
         * 模块名称
         */
        private String  moduleName;

    }

    /**
     * 审批中心-下拉搜索选项
     */
    @Data
    @NoArgsConstructor
    public static class ApproveSearchOptionDTO {
        /**
         * 数据审核状态（代办、已办、已发送）
         */
        private String status;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 模块表名集合
         */
        private List<Module> moduleList;
    }


    @Data
    @NoArgsConstructor
    public static class Module {
        /**
         * 模块表名
         */
        private String tableName;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 数据表id集合
         */
        private List<String> ids;
    }
}
