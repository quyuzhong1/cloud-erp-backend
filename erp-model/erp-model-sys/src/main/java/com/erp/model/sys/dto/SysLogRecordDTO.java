package com.erp.model.sys.dto;

import com.common.business.dto.base.SortDTO;
import com.common.core.enums.LogActionEnum;
import com.common.core.enums.LogStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <p>
 * 操作日志请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-08-25
 */
@Data
@NoArgsConstructor
public class SysLogRecordDTO implements Serializable {

    /**
     * 新增
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 请求ID
         */
        @NotBlank(message = "请求ID不能为空")
        private String requestId;

        /**
         * 系统模块
         */
        @NotBlank(message = "系统模块不能为空")
        @Size(max = 50, message = "系统模块最大长度不能超过50位")
        private String systemModule;

        /**
         * 操作路径
         */
        @NotBlank(message = "操作路径不能为空")
        private String path;

        /**
         * 操作类型
         * {@link LogActionEnum}
         */
        @NotBlank(message = "操作类型不能为空")
        @Size(max = 100, message = "操作路径最大长度不能超过100位")
        private String action;

        /**
         * 描述
         */
        @NotBlank(message = "描述不能为空")
        private String description;

        /**
         * 操作人ip
         */
        @NotBlank(message = "操作人ip不能为空")
        @Size(max = 16, message = "操作人ip最大长度不能超过16位")
        private String ip;

        /**
         * 操作的参数json
         */
        @NotBlank(message = "操作的参数json不能为空")
        private String requestParams;

        /**
         * 响应的参数json
         */
        @NotBlank(message = "响应的参数json不能为空")
        private String responseParams;

        /**
         * 修改的类名
         */
        @NotBlank(message = "修改的类名不能为空")
        private String classPath;

        /**
         * 修改的记录id
         */
        @NotBlank(message = "修改的记录id不能为空")
        private String recordId;

        /**
         * 记录单据编号
         */
        @NotNull(message = "记录单据编号不能为NULL")
        private String recordCode;

        /**
         * 异常信息
         */
        @NotNull(message = "异常信息不能为NULL")
        private String errorMsg;

        /**
         * 状态:正常/异常
         * {@link LogStatusEnum}
         */
        @NotBlank(message = "状态不能为空")
        private String status;

    }

    /**
     * 分页列表查询参数
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 操作类型：
         * insert=插入
         * update=更新
         * delete=删除
         * grant=授权
         * import=导入
         * export=导出
         * cancel=撤销
         * submit=提交
         * approve=审核
         * disapprove=反审核
         * invalid=作废
         * addAndSubmit=新增并提交
         * updateAndSubmit=更新并提交
         */
        private List<String> actionList;

        /**
         * 系统模块
         */
        private String systemModule;

        /**
         * 创建时间列表
         */
        private List<LocalDateTime> createTimeList;

        /**
         * 创建人id列表
         */
        private List<String> createUserIdList;

        /**
         * 记录ID列表
         */
        private List<String> recordIdList;

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
        private String id;

        /**
         * 系统模块
         */
        private String systemModule;

        /**
         * 操作类型
         */
        private String action;

        /**
         * 操作类型名称
         */
        private String actionName;

        /**
         * 操作路径
         */
        private String path;

        /**
         * 描述
         */
        private String description;

        /**
         * 操作人ip
         */
        private String ip;

        /**
         * 操作参数json
         */
        private String requestParams;

        /**
         * 响应参数json
         */
        private String responseParams;

        /**
         * 修改的类名
         */
        private String classPath;

        /**
         * 修改的记录id
         */
        private String recordId;

        /**
         * 记录单据编号
         */
        private String recordCode;

        /**
         * 异常信息
         */
        private String errorMsg;

        /**
         * 状态:正常/异常
         */
        private String status;

        /**
         * 请求唯一标识ID
         */
        private String requestId;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 通过Action设置操作类型名称
         */
        public void setActionNameByAction() {
            if (StringUtils.isBlank(this.action)){
                this.setActionName("");
                return;
            }
            LogActionEnum actionEnum = LogActionEnum.getByCode(this.action);
            if (null == actionEnum){
                this.setActionName("");
                return;
            }
            this.setActionName(actionEnum.getName());
        }
    }

}