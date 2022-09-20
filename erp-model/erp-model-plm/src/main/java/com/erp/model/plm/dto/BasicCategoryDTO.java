package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Classname BasicCategoryDTO
 * @Description TODO
 * @Date 2022-09-13 14:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BasicCategoryDTO {

    private String id;

    private String name;

    private String pid;

    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    private List<BasicCategoryDTO> childrenList;
}
