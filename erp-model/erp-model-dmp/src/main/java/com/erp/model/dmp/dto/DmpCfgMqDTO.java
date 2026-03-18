package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
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
 * 输入输出mq信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2026-03-17
*/
@Data
@NoArgsConstructor
public class DmpCfgMqDTO implements Serializable {



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
        * 系统id
        */
        private String systemId;

        /**
        * 输入输出类型：input=输入，output=输出
        */
        private String type;

        /**
        * mq类型:rocketmq=rocketmq,kafka=kafka
        */
        private String mqType;

        /**
        * 主机
        */
        private String host;

        /**
        * 端口
        */
        private Integer port;

        /**
        * 用户名
        */
        private String userName;

        /**
        * 密码
        */
        private String passWord;

        /**
        * 主题
        */
        private String topic;

        /**
        * tag信息
        */
        private String tag;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * mq分组
        */
        private String mqGroup;


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
        * 系统id
        */
        private String systemId;

        /**
        * 输入输出类型：input=输入，output=输出
        */
        private String type;

        /**
        * mq类型:rocketmq=rocketmq,kafka=kafka
        */
        private String mqType;

        /**
        * 主机
        */
        private String host;

        /**
        * 端口
        */
        private Integer port;

        /**
        * 用户名
        */
        private String userName;

        /**
        * 密码
        */
        private String passWord;

        /**
        * 主题
        */
        private String topic;

        /**
        * tag信息
        */
        private String tag;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * mq分组
        */
        private String mqGroup;

        /**
         * 输入/输出类
         */
        private String cfgType = "mq";

        /**
         * 输入/输出类明细
         */
        private String cfgTypeName = DmpCfgOutputTypeEnum.getName("mq");
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
        * 系统id
        */
        @NotBlank(message = "系统id不能为空")
        @Size(max = 50,message = "系统id最大长度不能超过50位")
        private String systemId;

        /**
        * 输入输出类型：input=输入，output=输出
        */
        @NotBlank(message = "输入输出类型：input=输入，output=输出不能为空")
        @Size(max = 50,message = "输入输出类型：input=输入，output=输出最大长度不能超过50位")
        private String type;

        /**
        * mq类型:rocketmq=rocketmq,kafka=kafka
        */
        @NotBlank(message = "mq类型:rocketmq=rocketmq,kafka=kafka不能为空")
        @Size(max = 255,message = "mq类型:rocketmq=rocketmq,kafka=kafka最大长度不能超过255位")
        private String mqType;

        /**
        * 主机
        */
        @NotBlank(message = "主机不能为空")
        @Size(max = 50,message = "主机最大长度不能超过50位")
        private String host;

        /**
        * 端口
        */
        @NotNull(message = "端口不能为空")
        private Integer port;

        /**
        * 用户名
        */
        @NotBlank(message = "用户名不能为空")
        @Size(max = 255,message = "用户名最大长度不能超过255位")
        private String userName;

        /**
        * 密码
        */
        @NotBlank(message = "密码不能为空")
        @Size(max = 255,message = "密码最大长度不能超过255位")
        private String passWord;

        /**
        * 主题
        */
        @NotBlank(message = "主题不能为空")
        @Size(max = 255,message = "主题最大长度不能超过255位")
        private String topic;

        /**
        * tag信息
        */
        @NotBlank(message = "tag信息不能为空")
        @Size(max = 255,message = "tag信息最大长度不能超过255位")
        private String tag;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * mq分组
        */
        @NotBlank(message = "mq分组不能为空")
        @Size(max = 255,message = "mq分组最大长度不能超过255位")
        private String mqGroup;


    }


}