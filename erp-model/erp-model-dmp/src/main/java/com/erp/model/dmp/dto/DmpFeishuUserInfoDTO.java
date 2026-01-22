package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * DMP飞书用户信息请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-13
*/
@Data
@NoArgsConstructor
public class DmpFeishuUserInfoDTO implements Serializable {



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

        /**
        * 任务转换ID
        */
        private String convertId;

        /**
        * 店铺ID
        */
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 	事件 ID
        */
        private String eventId;

        /**
        * 事件类型
        */
        private String eventType;

        /**
        * 用户的 open_id
        */
        private String openId;

        /**
        * 用户的 union_id
        */
        private String unionId;

        /**
        * 用户的 user_id
        */
        private String userId;

        /**
        * 用户名
        */
        private String name;

        /**
        * 手机号
        */
        private String mobile;

        /**
        * 性别 0=未知 1 = 男 2=女 3=其他
        */
        private Integer gender;

        /**
        * 邮箱
        */
        private String email;

        /**
        * 是否为离职状态 true=是 false=否
        */
        private Boolean isResigned;

        /**
        * 原始json
        */
        private String dataJson;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


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

        /**
        * 任务转换ID
        */
        private String convertId;

        /**
        * 店铺ID
        */
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 	事件 ID
        */
        private String eventId;

        /**
        * 事件类型
        */
        private String eventType;

        /**
        * 用户的 open_id
        */
        private String openId;

        /**
        * 用户的 union_id
        */
        private String unionId;

        /**
        * 用户的 user_id
        */
        private String userId;

        /**
        * 用户名
        */
        private String name;

        /**
        * 手机号
        */
        private String mobile;

        /**
        * 性别 0=未知 1 = 男 2=女 3=其他
        */
        private Integer gender;

        /**
        * 邮箱
        */
        private String email;

        /**
        * 是否为离职状态 true=是 false=否
        */
        private Boolean isResigned;

        /**
        * 原始json
        */
        private String dataJson;


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
    public static class CommonDTO extends SuperDTO {

        /**
        * 任务转换ID
        */
        @NotBlank(message = "任务转换ID不能为空")
        @Size(max = 19,message = "任务转换ID最大长度不能超过19位")
        private String convertId;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 255,message = "店铺ID最大长度不能超过255位")
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        @NotBlank(message = "任务来源唯一加密代号不能为空")
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        @NotBlank(message = "任务数据加密代号不能为空")
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 	事件 ID
        */
        @NotBlank(message = "	事件 ID不能为空")
        @Size(max = 255,message = "	事件 ID最大长度不能超过255位")
        private String eventId;

        /**
        * 事件类型
        */
        @NotBlank(message = "事件类型不能为空")
        @Size(max = 64,message = "事件类型最大长度不能超过64位")
        private String eventType;

        /**
        * 用户的 open_id
        */
        @NotBlank(message = "用户的 open_id不能为空")
        @Size(max = 64,message = "用户的 open_id最大长度不能超过64位")
        private String openId;

        /**
        * 用户的 union_id
        */
        @NotBlank(message = "用户的 union_id不能为空")
        @Size(max = 64,message = "用户的 union_id最大长度不能超过64位")
        private String unionId;

        /**
        * 用户的 user_id
        */
        @NotBlank(message = "用户的 user_id不能为空")
        @Size(max = 64,message = "用户的 user_id最大长度不能超过64位")
        private String userId;

        /**
        * 用户名
        */
        @NotBlank(message = "用户名不能为空")
        @Size(max = 64,message = "用户名最大长度不能超过64位")
        private String name;

        /**
        * 手机号
        */
        @NotBlank(message = "手机号不能为空")
        @Size(max = 64,message = "手机号最大长度不能超过64位")
        private String mobile;

        /**
        * 性别 0=未知 1 = 男 2=女 3=其他
        */
        @NotNull(message = "性别 0=未知 1 = 男 2=女 3=其他不能为空")
        private Integer gender;

        /**
        * 邮箱
        */
        @NotBlank(message = "邮箱不能为空")
        @Size(max = 128,message = "邮箱最大长度不能超过128位")
        private String email;

        /**
        * 是否为离职状态 true=是 false=否
        */
        @NotNull(message = "是否为离职状态 true=是 false=否不能为空")
        private Boolean isResigned;

        /**
        * 原始json
        */
        @NotBlank(message = "原始json不能为空")
        private String dataJson;


    }


}