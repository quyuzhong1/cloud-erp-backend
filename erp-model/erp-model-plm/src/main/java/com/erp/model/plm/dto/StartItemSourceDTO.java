package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname StartItemSourceDTO
 * @Description TODO
 * @Date 2022-10-11 18:42
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StartItemSourceDTO implements Serializable {


    /**
     * 来源名
     */
    private String sourceName;

    /**
     * 来源类型
     * 0 新建
     * 1 项目
     * 2 模板
     */
    private  Integer sourceType;

    /**
     * 对应的id
     */
    private String flagId;

    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    List<StartItemSourceDTO> childrenList;
}
