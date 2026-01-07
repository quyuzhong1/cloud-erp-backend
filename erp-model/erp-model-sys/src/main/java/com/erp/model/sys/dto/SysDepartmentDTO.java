package com.erp.model.sys.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Classname SysDepartmentDTO

 * @Date 2022-07-11 17:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysDepartmentDTO {


    private String id;

    private String parentId;
    /**
     * $column.comments
     */
    private String name;

    private String code;

    //1 部门  2  小组
    private Integer type;

    /**
     * 备注
     */
    private String remark;


    @JsonInclude(value= JsonInclude.Include.NON_NULL)
    private List<SysDepartmentDTO> childrenList;


    /**
     * 更新启禁用DTO
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDisabledDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 是否禁用 ： true 禁用  false 启用
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

    }

}
