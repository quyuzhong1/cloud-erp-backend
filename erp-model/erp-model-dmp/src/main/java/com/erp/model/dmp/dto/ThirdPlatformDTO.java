package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 第三方系统平台表请求响应实体
 * </p>
 *
 */
@Data
@NoArgsConstructor
public class ThirdPlatformDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PageSelectDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        private String sysType;
        /**
         * 名称
         */
        private String name;
        /**
         * 编号
         */
        private String code;
        /**
         * 是否可选
         */
        private Boolean canCheck = true;
        /**
         * disabled
         */
        private Boolean disabled;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        @NotBlank(message = "系统类型不能为空")
        @Size(max = 19, message = "系统类型：lingxing领星，wangdian旺店通 最大长度不能超过19位")
        private String sysType;
        /**
         * 名称
         */
        @Size(max = 200, message = "名称最大长度不能超过200位")
        private String name;
    }

    /**
     * 远程搜索
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 系统类型
         */
        @NotBlank(message = "系统类型不能为空")
        private String sysType;

        /**
         * 关键词
         */
        private String searchKeyword;
    }
}