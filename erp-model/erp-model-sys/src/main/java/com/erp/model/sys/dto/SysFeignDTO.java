package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Classname SysFeignDTO
 *
 * @Author Cloud
 * @Date 2023/4/27 16:30
 **/
public class SysFeignDTO {

    private SysFeignDTO() {
    }

    @Data
    @NoArgsConstructor
    public static class ListByRoleIdsDTO{
        /**
         * 角色id集合
         */
        @NotNull(message = "角色id集合不能为空")
        @NotEmpty(message = "角色id集合不能为空")
        private List<String> roleIds;

        /**
         * 用户名
         */
        private String userId;

        public ListByRoleIdsDTO(List<String> roleIds, String startUserId) {
            this.roleIds = roleIds;
            this.userId = startUserId;
        }
    }
}
