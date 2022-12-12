package com.erp.model.bi.dto;

import com.erp.common.modules.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @Classname SubjectDTO
 * @Description TODO
 * @Date 2022-12-09 15:44
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SubjectDTO  implements Serializable {

    /**
     * 表id
     */
    @NotBlank(message = "id不能为空" ,groups = UpdateGroup.class )
    private String id;


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
     * 是否常用
     * 0 不是 1 是
     */
    private Integer isFrequently;


    /**
     * 分类名
     *
     */
    private String categoryName;


//   /**
//    * 布局集合
//    * @author yl
//    * @date 2022-12-09 16:40
//    * @param null
//    * @return
//    */
//    private List<LayoutDTO> layoutList;


}
