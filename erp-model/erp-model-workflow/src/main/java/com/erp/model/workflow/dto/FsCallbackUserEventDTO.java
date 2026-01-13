package com.erp.model.workflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FsCallbackUserEventDTO implements Serializable {

    /**
     * 员工离职事件 - contact.user.deleted_v3
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserDeletedDTO {
        @JsonProperty("header")
        private EventHeaderDTO header;

        @JsonProperty("event")
        private UserDeletedEventDTO event;
    }

    /**
     * 事件头部信息
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventHeaderDTO {
        @JsonProperty("event_id")
        private String eventId;

        @JsonProperty("event_type")
        private String eventType;

        @JsonProperty("create_time")
        private String createTime;

        @JsonProperty("token")
        private String token;

        @JsonProperty("app_id")
        private String appId;

        @JsonProperty("tenant_key")
        private String tenantKey;
    }

    /**
     * 用户删除事件数据
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserDeletedEventDTO {
        @JsonProperty("object")
        private UserDeletedEventObjectDTO object;

        @JsonProperty("old_object")
        private UserDeletedOldObjectDTO oldObject;
    }

    /**
     * 删除前的对象数据
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserDeletedOldObjectDTO {
        @JsonProperty("department_ids")
        private List<String> departmentIds;

        @JsonProperty("open_id")
        private String openId;
    }

    /**
     * 用户详细信息对象
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserDeletedEventObjectDTO {
        @JsonProperty("open_id")
        private String openId;

        @JsonProperty("union_id")
        private String unionId;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("en_name")
        private String enName;

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("email")
        private String email;

        @JsonProperty("enterprise_email")
        private String enterpriseEmail;

        @JsonProperty("job_title")
        private String jobTitle;

        @JsonProperty("mobile")
        private String mobile;

        @JsonProperty("gender")
        private Integer gender;

        @JsonProperty("avatar")
        private AvatarDTO avatar;

        @JsonProperty("status")
        private StatusDTO status;

        @JsonProperty("department_ids")
        private List<String> departmentIds;

        @JsonProperty("leader_user_id")
        private String leaderUserId;

        @JsonProperty("city")
        private String city;

        @JsonProperty("country")
        private String country;

        @JsonProperty("work_station")
        private String workStation;

        @JsonProperty("join_time")
        private Long joinTime;

        @JsonProperty("employee_no")
        private String employeeNo;

        @JsonProperty("employee_type")
        private Integer employeeType;

        @JsonProperty("orders")
        private List<OrderDTO> orders;

        @JsonProperty("custom_attrs")
        private List<CustomAttrDTO> customAttrs;

        @JsonProperty("job_level_id")
        private String jobLevelId;

        @JsonProperty("job_family_id")
        private String jobFamilyId;

        @JsonProperty("dotted_line_leader_user_ids")
        private List<String> dottedLineLeaderUserIds;
    }

    /**
     * 头像信息
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvatarDTO {
        @JsonProperty("avatar_72")
        private String avatar72;

        @JsonProperty("avatar_240")
        private String avatar240;

        @JsonProperty("avatar_640")
        private String avatar640;

        @JsonProperty("avatar_origin")
        private String avatarOrigin;
    }

    /**
     * 用户状态信息
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusDTO {
        @JsonProperty("is_frozen")
        private Boolean isFrozen;

        @JsonProperty("is_resigned")
        private Boolean isResigned;

        @JsonProperty("is_activated")
        private Boolean isActivated;

        @JsonProperty("is_exited")
        private Boolean isExited;

        @JsonProperty("is_unjoin")
        private Boolean isUnjoin;
    }

    /**
     * 部门排序信息
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderDTO {
        @JsonProperty("department_id")
        private String departmentId;

        @JsonProperty("user_order")
        private Integer userOrder;

        @JsonProperty("department_order")
        private Integer departmentOrder;

        @JsonProperty("is_primary_dept")
        private Boolean isPrimaryDept;
    }

    /**
     * 自定义属性
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomAttrDTO {
        @JsonProperty("type")
        private String type;

        @JsonProperty("id")
        private String id;

        @JsonProperty("value")
        private CustomAttrValueDTO value;
    }

    /**
     * 自定义属性值
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomAttrValueDTO {
        @JsonProperty("text")
        private String text;

        @JsonProperty("url")
        private String url;

        @JsonProperty("pc_url")
        private String pcUrl;

        @JsonProperty("option_id")
        private String optionId;

        @JsonProperty("option_value")
        private String optionValue;

        @JsonProperty("name")
        private String name;

        @JsonProperty("picture_url")
        private String pictureUrl;

        @JsonProperty("generic_user")
        private GenericUserDTO genericUser;
    }

    /**
     * 通用用户信息
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenericUserDTO {
        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private Integer type;
    }
}
