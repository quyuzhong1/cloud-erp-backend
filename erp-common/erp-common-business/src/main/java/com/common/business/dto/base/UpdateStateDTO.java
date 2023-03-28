package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 * @Classname UpdateStateDTO
 * @Description TODO
 * @Date 2022-11-07 14:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateStateDTO implements Serializable {

    @NotBlank(message = "id不能为空")
    private String id;


    /**
     * true 禁用
     * false 启用
     */
    @NotNull(message = "状态值不能为空")
    private Boolean state;


    /**
     * 批量修改
     * 状态
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateDTO {

        /**
         * ids 不能为空
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;
    }
}
