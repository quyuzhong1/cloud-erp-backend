package com.erp.model.bi.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname AddTotalSubjectDTO
 * @Description TODO
 * @Date 2022-12-19 17:36
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class AddTotalSubjectDTO implements Serializable {

    /**
     * 分类id
     */
    @NotBlank(message = "分类id 不能为空")
    private String categoryId;

    /**
     * 专题名
     */
    @NotBlank(message = "专题名不能为空")
    @Size(max = 20,message = "最大20个字符")
    private String name;




    /**
     * 分类名
     *
     */
    private String categoryName;

    /**
     * 分享标示
     * personal 私人
     * share 共享
     */
    @NotBlank(message = "分享标示不能为空")
    @StateEnumValue(strValues = {"personal","share"},message = "分享标识有误")
    private String shareFlag="personal";

    /**
     * 分享的用户集合
     */
    private List<String> shareUserIdList;



    /**
     * 布局集合
     * @author yl
     * @date 2022-12-09 16:40
     * @param null
     * @return
     */
    @Valid
    private List<LayoutDTO> layoutList;
}
