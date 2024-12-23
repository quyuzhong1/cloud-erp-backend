package com.erp.model.plm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 模具配置请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Data
@NoArgsConstructor
public class CfgMouldSettingDTO implements Serializable {

    /**
     * 新增
     */
    @Getter
    @Setter
    public static class AddDTO {

        @Valid
        private List<ParamDTO> mouldList;

        @Valid
        private List<ParamDTO> docList;
    }

    /**
     * 参数
     */
    @Getter
    @Setter
    public static class ParamDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 名字
         */
        @NotBlank(message = "类型名不能为空")
        private String name;
    }

}