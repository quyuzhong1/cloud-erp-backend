package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import com.common.business.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

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
   // @NotBlank(message = "分类id 不能为空")
    private String categoryId;


    private Integer isFrequently=0;

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
     * 创建人id
     */
    private String createUserId;


    /**
     * 创建人
     */
    private String createUserName;


    /**
     * 分享的用户集合
     */
    private List<String> shareUserIdList;


}
