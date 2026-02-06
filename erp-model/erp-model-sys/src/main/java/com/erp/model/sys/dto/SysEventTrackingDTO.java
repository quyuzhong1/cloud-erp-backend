package com.erp.model.sys.dto;

import java.time.LocalDateTime;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 前端埋点事件记录请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-02-19
*/
@Data
@NoArgsConstructor
public class SysEventTrackingDTO implements Serializable {




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
        * 系统环境
        */
        private String env;

        /**
        * 页面标题
        */
        private String title;

        /**
        * 页面的url
        */
        private String url;

        /**
        * 用户名
        */
        private String userName;

        /**
        * 用户ID
        */
        private String userId;

        /**
        * 原部门名称
        */
        private String deptName;

        /**
        * 原部门Id
        */
        private String deptId;

        /**
        * 触发埋点的时间
        */
        private LocalDateTime eventTime;

        /**
        * 客户端的设备信息
        */
        private String ua;

        /**
        * 屏幕信息
        */
        private String screen;

        /**
        * 数据类型，根据触发的不同埋点有不同的类型
        */
        private String type;

        /**
        * 事件数据json
        */
        private String eventData;

        /**
        * sdk相关信息
        */
        private String sdk;

        /**
        * IP地址
        */
        private String ipAddress;


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
        * 系统环境
        */
//        @NotBlank(message = "系统环境不能为空")
        @Size(max = 64,message = "系统环境最大长度不能超过64位")
        private String env;

        /**
        * 页面标题
        */
//        @NotBlank(message = "页面标题不能为空")
//        @Size(max = 64,message = "页面标题最大长度不能超过64位")
        private String title;

        /**
        * 页面的url
        */
        @NotBlank(message = "页面的url不能为空")
        private String url;

        /**
        * 用户名
        */
//        @NotBlank(message = "用户名不能为空")
        @Size(max = 255,message = "用户名最大长度不能超过255位")
        private String userName;

        /**
        * 用户ID
        */
//        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64,message = "用户ID最大长度不能超过64位")
        private String userId;

        /**
        * 原部门名称
        */
//        @Size(max = 255,message = "原部门名称最大长度不能超过255位")
        private String deptName;

        /**
        * 原部门Id
        */
//        @Size(max = 64,message = "原部门Id最大长度不能超过64位")
        private String deptId;

        /**
        * 触发时间
        */
//        @NotNull(message = "触发埋点的时间不能为空")
        private LocalDateTime eventTime;

        /**
        * 客户端的设备信息
        */
//        @NotBlank(message = "客户端的设备信息不能为空")
        @Size(max = 255,message = "客户端的设备信息最大长度不能超过255位")
        private String ua;

        /**
        * 屏幕信息
        */
        @NotBlank(message = "屏幕信息不能为空")
        @Size(max = 64,message = "屏幕信息最大长度不能超过64位")
        private String screen;

        /**
        * 数据类型，根据触发的不同埋点有不同的类型
        */
//        @NotBlank(message = "数据类型，根据触发的不同埋点有不同的类型不能为空")
        @Size(max = 64,message = "数据类型，根据触发的不同埋点有不同的类型最大长度不能超过64位")
        private String type;

        /**
        * 事件数据json
        */
        private JSONObject eventData;

        /**
        * sdk相关信息
        */
//        @NotBlank(message = "sdk相关信息不能为空")
        @Size(max = 64,message = "sdk相关信息最大长度不能超过64位")
        private String sdk;

        /**
         * 数据类型描述
         */
        private String typeDesc;

        /**
         * 设备类型
         */
        private String deviceType;

        /**
         * 时区
         */
        private String timeZone;

        /**
         * sessionId
         */
        private String sessionId;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDataDTO{
        public AddDTO data;
    }
}