package com.erp.model.dmp.dto;

import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 拉取任务请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpInputTaskDTO implements Serializable {




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
        * 拉取数据配置明细id
        */
        private String inputDetailId;

        /**
        * 拉取接口条件的开始时间
        */
        private LocalDateTime startTime;

        /**
        * 拉取接口条件的结束时间
        */
        private LocalDateTime endTime;

        /**
        * 状态：init=待拉取，fds=上传fds，mongo=保存mongo，dmp=保存dmp，finish=完成，error=异常
        */
        private String status;

        /**
        * 异常原因
        */
        private String errorMessage;

        /**
        * 任务类型：normal=正常任务，history=补偿任务，hotfix=及时任务
        */
        private String taskType;


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
        * 拉取数据配置明细id
        */
        @NotBlank(message = "拉取数据配置明细id不能为空")
        @Size(max = 50,message = "拉取数据配置明细id最大长度不能超过50位")
        private String inputDetailId;

        /**
        * 拉取接口条件的开始时间
        */
        private LocalDateTime startTime;

        /**
        * 拉取接口条件的结束时间
        */
        private LocalDateTime endTime;

        /**
        * 状态：init=待拉取，fds=上传fds，mongo=保存mongo，dmp=保存dmp，finish=完成，error=异常
        */
        @NotBlank(message = "状态：init=待拉取，fds=上传fds，mongo=保存mongo，dmp=保存dmp，finish=完成，error=异常不能为空")
        @Size(max = 50,message = "状态：init=待拉取，fds=上传fds，mongo=保存mongo，dmp=保存dmp，finish=完成，error=异常最大长度不能超过50位")
        private String status;

        /**
        * 异常原因
        */
        private String errorMessage;

        /**
        * 任务类型：normal=正常任务，history=补偿任务，hotfix=及时任务
        */
        @NotBlank(message = "任务类型：normal=正常任务，history=补偿任务，hotfix=及时任务不能为空")
        @Size(max = 50,message = "任务类型：normal=正常任务，history=补偿任务，hotfix=及时任务最大长度不能超过50位")
        private String taskType;


    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型，all全部、0无需同步、同步中、2同步中、3同步成功、4同步失败
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 分页返回值
     */
    @Data
    @NoArgsConstructor
    public class PagingDTO {
        /**
         * 来源系统名称
         */
        private String sourcePlatformName;

        /**
         * 目标平台名称
         */
        private String targetPlatformName;

        /**
         * 同步类型
         */
        private String syncTypeName;

        /**
         * 单据类型
         */
        private String sourceTypeName;

        /**
         * 单据编号
         */
        private String sourceCode;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 最新同步时间
         */
        private LocalDateTime lastSyncTime;

        /**
         * 操作节点
         */
        private String syncOperateName;

        /**
         * 同步状态
         */
        private String status;

        /**
         * 同步状态中文
         */
        private String statusName;

        /**
         * 推送失败原因
         */
        private String returnMsg;

        /**
         * 推送数据
         */
        private String pushData;

    }


    /**
     * 分页查询条件
     */
    @Data
    @NoArgsConstructor
    public class PagingParamDTO extends PermissionsDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }
}