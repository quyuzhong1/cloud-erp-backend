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
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 菜单id
         */
        private String workMenuId;
        /**
         * 地址（预留）
         */
        private String moduleUrl;
        /**
         * 入参（预留）
         */
        private String moduleParam;
        /**
         * 类型  1：常用模块  2：代办模块
         */
        private String type;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * id
         */
        private String id;
        /**
         * 菜单id
         */
        private String workMenuId;
        /**
         * 地址（预留）
         */
        private String moduleUrl;
        /**
         * 入参（预留）
         */
        private String moduleParam;
    }

    /**
     * 模块分类下拉
     */
    @Data
    @NoArgsConstructor
    public static class WaitDoMenu {
        /**
         * id
         */
        private String id;
        /**
         * name
         */
        private String name;
        /**
         * 是否已添加  0 否 1 是
         */
        private Integer sign;
        /**
         * 模块分类
         */
        private String moduleClassify;
        /**
         * 模块状态名称
         */
        private String moduleStatusName;
    }

    @Data
    @NoArgsConstructor
    public static class MyWorkOptionDTO {
        /**
         * id
         */
        private String id;

        /**
         * 模块状态id
         */
        private String moduleStatusId;

        /**
         * 菜单id
         */
        private String menuId;

        /**
         * 系统分类 plm wms scm
         */
        private String sysClassify;

        /**
         * 模块分类
         */
        private String moduleClassify;

        /**
         * 模块编码
         */
        private String moduleCode;

        /**
         * 模块状态名称
         */
        private String moduleStatusName;

        /**
         * 模块状态
         */
        private String moduleStatus;

        /**
         * 地址（预留）
         */
        private String moduleUrl;
        /**
         * 入参（预留）
         */
        private String moduleParam;
    }

    /**
     * 代办列表
     */
    @Data
    @NoArgsConstructor
    public static class PendingViewDTO {
        /**
         * 模块分类 plm wms scm
         */
        public String sysClassify;

        /**
         * 数据集
         */
        public List<PendingViewDetailDTO> list;
    }
    /**
     * 代办列表
     */
    @Data
    @NoArgsConstructor
    public static class PendingViewDetailDTO {
        /**
         * id
         */
        private String id;

        /**
         * 模块状态id
         */
        private String moduleStatusId;

        /**
         * 菜单id
         */
        private String menuId;

        /**
         * 模块名称
         */
        private String name;

        /**
         * 单据数量
         */
        private Integer count;

        /**
         * 状态编码
         */
        private String moduleStatus;

        /**
         * 状态名称
         */
        private String moduleStatusName;

        /**
         * 地址（预留）
         */
        private String moduleUrl;

        /**
         * 入参（预留）
         */
        private String moduleParam;
    }

    /**
     * 常用列表
     */
    @Data
    @NoArgsConstructor
    public static class FrequentlyViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 模块状态id
         */
        private String moduleStatusId;

        /**
         * 模块分类 plm wms scm
         */
        public String sysClassify;

        /**
         * 菜单id
         */
        private String menuId;

        /**
         * 模块名称
         */
        private String name;

        /**
         * 地址（预留）
         */
        private String moduleUrl;

        /**
         * 入参（预留）
         */
        private String moduleParam;
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

    /**
     * 工作台查询表单数量参数
     */
    @Data
    @NoArgsConstructor
    public static class TableNumDTO {
        /**
         * 表名称
         */
        private String tableName;

        /**
         * 状态
         */
        private String approveStatus;
    }

}
