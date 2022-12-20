package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname SubjectLayoutDTO
 * @Description TODO
 * @Date 2022-12-13 15:57
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SubjectLayoutDTO implements Serializable {


    @NotBlank(message = "专题id不能为空")
    private String subjectId;


   // @NotBlank(message = "分类id不能为空")
    private String categoryId;


    @NotBlank(message = "专题名不能为空")
    private String name;

    /**
     * 分享的用户id
     */
    private List<String> shareUserIdList;

    /**
     * 布局集合
     *
     * @author yl
     * @date 2022-12-09 16:40
     * @param null
     * @return
     */
    @Valid
    private List<LayoutDTO> layoutList;
}
