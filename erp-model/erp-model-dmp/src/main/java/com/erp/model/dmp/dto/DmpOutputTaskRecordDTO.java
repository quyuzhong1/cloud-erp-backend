package com.erp.model.dmp.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 推送任务记录请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpOutputTaskRecordDTO implements Serializable {

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
        * 输入任务存储状态（冗余）
        */
        private String inputStatus;

        /**
        * 数据的存储名，fds为文件夹路径，mongo为集合名,pg为表名（冗余）
        */
        private String storageName;

        /**
        * 数据id
        */
        private String dataId;

        /**
        * 推送状态：init=待推送,finish=推送成功,error=推送失败
        */
        private String status;

        /**
        * 异常原因
        */
        private String responseData;


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
        * 推送状态：init=待推送,finish=推送成功,error=推送失败
        */
        @NotBlank(message = "推送状态：init=待推送,finish=推送成功,error=推送失败不能为空")
        @Size(max = 50,message = "推送状态：init=待推送,finish=推送成功,error=推送失败最大长度不能超过50位")
        private String status;

        /**
        * 异常原因
        */
        private String responseData;

        /**
         * 返回信息
         */
        private String message;
    }


    /**
     * 分页返回值
     */
    @Data
    @NoArgsConstructor
    public static class PagingDTO {
        /**
         * 主键id
         */
        private String id;
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
         * 是否需要同步
         */
        private Boolean isNeedSync;

    }


    /**
     * 分页查询条件
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
        private Map<String,String> sqlMap;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型：/dmp/common/enumDropDown?type=DmpOutputTaskRecordStatus
         * all全部、init:待推送、mqsuccess:mq推送成功、mqerror:mq推送失败、cosumererror:消费失败、finish:推送成功、error:推送失败、0:无需同步
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddOutputBlackDTO {
        /**
         * 输出任务id
         */
        private List<String> ids;

        /**
         * 定义条件
         */
        private CustomizeBlackParam params;

        /**
         * 备注
         */
        private String remark;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomizeBlackParam {
        /**
         * 来源系统
         * 接口：/dmp/dmpBasicSystem/listDmpBasicSystem
         */
        private String sourcePlatformCode;
        /**
         * 单据类型（级联：关联来源系统）
         * 接口：/dmp/dmpCfgInput/listDmpCfgInput?id = 来源系统code
         * 接口入参：来源系统code
         */
        private String billTypeId;

        /**
         * 目标平台
         * 接口：/dmp/dmpBasicSystem/listDmpBasicSystem
         */
        private String targetPlatformCode;

        /**
         * 单据编码
         */
        private List<String> sourceCodeList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpotParamDTO extends PagingParamDTO {
        /**
         * 主键id
         */
        private List<String> ids;
    }
}