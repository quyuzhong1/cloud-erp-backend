package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 本地推送消息表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-08-27
*/
@Data
@NoArgsConstructor
public class SysPushMsgDTO implements Serializable {




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
        * 目标系统
        */
        private String targetPlatform;

        /**
        * 来源系统
        */
        private String sourcePlatform;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编号
        */
        private String sourceCode;

        /**
        * 操作类型
        */
        private String syncOperate;

        /**
        * 推送数据
        */
        private String pushData;

        /**
        * 备注
        */
        private String remark;

        /**
        * 父id
        */
        private String parentId;


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
        * 目标系统
        */
        @NotBlank(message = "目标系统不能为空")
        @Size(max = 31,message = "目标系统最大长度不能超过31位")
        private String targetPlatform;

        /**
        * 来源系统
        */
        @NotBlank(message = "来源系统不能为空")
        @Size(max = 31,message = "来源系统最大长度不能超过31位")
        private String sourcePlatform;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 31,message = "来源类型最大长度不能超过31位")
        private String sourceType;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源编号
        */
        @NotBlank(message = "来源编号不能为空")
        @Size(max = 63,message = "来源编号最大长度不能超过63位")
        private String sourceCode;

        /**
        * 操作类型
        */
        @NotBlank(message = "操作类型不能为空")
        @Size(max = 63,message = "操作类型最大长度不能超过63位")
        private String syncOperate;

        /**
        * 推送数据
        */
        private String pushData;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 500,message = "备注最大长度不能超过500位")
        private String remark;

        /**
        * 父id
        */
        @NotBlank(message = "父id不能为空")
        @Size(max = 19,message = "父id最大长度不能超过19位")
        private String parentId;


    }


}