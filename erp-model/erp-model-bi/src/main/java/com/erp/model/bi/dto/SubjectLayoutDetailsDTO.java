package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import com.common.core.exception.ServiceException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 专题详情
 *
 * @Classname

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

    /**
     * 分享标示 personal 私人  share 共享
     */
    @StateEnumValue(strValues = {"personal","share","role"},message = "分享类型有误")
    private String shareFlag;


    /**
     * 是否常用 1 是   0  不是
     */
    private Integer isFrequently=0;

    /**
     * 分类名
     *
     */
    private String categoryName;

    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 分享的用户集合
     */
//    @Deprecated
    private List<String> shareUserIdList;

    /**
     * 分享的用户/角色集合
     */
    private List<String> shareFlagIdList;


    @Valid
    private List<LayoutDetailsDTO> layoutDetailsList;

    public List<String> checkAndGetShareFlagIdList() {
        if ("personal".equalsIgnoreCase(this.shareFlag)){
            return shareFlagIdList;
        }
        if (CollectionUtils.isEmpty(this.shareFlagIdList)){
            throw new ServiceException("shareFlagIdList分享的标识ID列表不能为空");
        }
        return shareFlagIdList;
    }

}
