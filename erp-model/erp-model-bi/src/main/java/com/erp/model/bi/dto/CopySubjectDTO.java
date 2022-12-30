package com.erp.model.bi.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname CopySubjectDTO
 * @Description TODO
 * @Date 2022-12-29 18:30
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CopySubjectDTO implements Serializable {

    @NotBlank(message = "专题id不能为空")
    private String subjectId;


    /**
     * 专题名
     */
    @NotBlank(message = "专题名不能为空")
    @Size(max = 20,message = "最大20个字符")
    private String name;


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
     * 是否常用
     */
    private Integer isFrequently=0;
}
