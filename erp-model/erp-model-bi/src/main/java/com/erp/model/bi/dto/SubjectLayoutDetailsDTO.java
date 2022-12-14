package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 专题详情
 *
 * @Classname
 * @Description TODO
 * @Date 2022-12-13 17:11
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SubjectLayoutDetailsDTO implements Serializable {

    /**
     * 专题id
     */
    @NotBlank(message = "专题id不能为空")
    private String subjectId;


    /**
     * 专题名
     */
    @NotBlank(message = "专题名不能为空")
    @Size(max = 20,message = "最大20个字符")
    private String name;


    @Valid
    private List<LayoutDetailsDTO> layoutDetailsList;

}
