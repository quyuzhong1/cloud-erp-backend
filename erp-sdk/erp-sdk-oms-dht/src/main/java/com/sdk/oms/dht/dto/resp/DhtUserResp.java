package com.sdk.oms.dht.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sdk.oms.dht.dto.BaseResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DhtUserResp extends BaseResult {

    @JsonProperty("empList")
    private List<EmpListDTO> empList;
    @NoArgsConstructor
    @Data
    public static class EmpListDTO {
        @JsonProperty("enterpriseId")
        private Integer enterpriseId;
        @JsonProperty("openUserId")
        private String openUserId;
        @JsonProperty("account")
        private String account;
        @JsonProperty("fullName")
        private String fullName;
        @JsonProperty("name")
        private String name;
        @JsonProperty("status")
        private String status;
        @JsonProperty("mobile")
        private String mobile;
        @JsonProperty("telephone")
        private String telephone;
        @JsonProperty("role")
        private String role;
        @JsonProperty("post")
        private String post;
        @JsonProperty("qq")
        private String qq;
        @JsonProperty("email")
        private String email;
        @JsonProperty("gender")
        private String gender;
        @JsonProperty("profileImage")
        private String profileImage;
        @JsonProperty("description")
        private String description;
        @JsonProperty("weixin")
        private String weixin;
        @JsonProperty("msn")
        private String msn;
        @JsonProperty("extensionNumber")
        private String extensionNumber;
        @JsonProperty("mobileSetting")
        private MobileSettingDTO mobileSetting;
        @JsonProperty("workingState")
        private String workingState;
        @JsonProperty("isActive")
        private Boolean isActive;
        @JsonProperty("mainDepartmentIds")
        private List<Integer> mainDepartmentIds;
        @JsonProperty("departmentIds")
        private List<Integer> departmentIds;
        @JsonProperty("departmentAsteriskIds")
        private List<String> departmentAsteriskIds;
        @JsonProperty("employeeAsteriskIds")
        private List<String> employeeAsteriskIds;
        @JsonProperty("birthDate")
        private String birthDate;
        @JsonProperty("hireDate")
        private String hireDate;
        @JsonProperty("empNum")
        private String empNum;
        @JsonProperty("startWorkDate")
        private String startWorkDate;
        @JsonProperty("stopTime")
        private Integer stopTime;
        @JsonProperty("createTime")
        private Long createTime;
        @JsonProperty("updateTime")
        private Long updateTime;
        @JsonProperty("nameSpell")
        private String nameSpell;
        @JsonProperty("nameOrder")
        private String nameOrder;

        @NoArgsConstructor
        @Data
        public static class MobileSettingDTO {
            @JsonProperty("mobileStatus")
            private String mobileStatus;
            @JsonProperty("departmentIds")
            private List<String> departmentIds;
            @JsonProperty("employeeIds")
            private List<String> employeeIds;
        }
    }
}
