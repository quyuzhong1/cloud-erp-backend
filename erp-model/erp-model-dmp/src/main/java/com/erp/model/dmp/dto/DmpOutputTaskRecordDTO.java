package com.erp.model.dmp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
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
    public class PagingDTO {
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class  AddOutputBlackDTO {
        /**
         * 输出任务id
         */
        private String outputId;

        /**
         * 单据编码
         */
        private String billCode;
    }

    public class ExpotParamDTO extends PagingParamDTO {
        /**
         * 主键id
         */
        private List<String> ids;
    }
}