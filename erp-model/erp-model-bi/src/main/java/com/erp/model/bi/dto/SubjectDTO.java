package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import com.common.business.validator.UpdateGroup;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiSubjectShareEntity;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname SubjectDTO

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
     * share 按多用户ID共享
     * role 按多角色ID
     */
    @NotBlank(message = "分享类型不能为空")
    @StateEnumValue(strValues = {"personal","share","role"},message = "分享类型有误")
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
    @Deprecated
    private List<String> shareUserIdList;


    /**
     * 分享的标识ID集合
     */
    private List<String> shareFlagIdList;

    public List<String> checkAndGetShareFlagIdList() {
        if ("personal".equalsIgnoreCase(this.shareFlag)){
            return shareFlagIdList;
        }
        if (CollectionUtils.isEmpty(this.shareFlagIdList)){
            throw new ServiceException("shareFlagIdList分享的标识ID列表不能为空");
        }
        return shareFlagIdList;
    }


    public void checkAndSetFlagInfo(List<BiSubjectShareEntity> shareList) {
        String shareFlag = "personal";
        List<String> shareFlagIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(shareList)){
            shareFlag = BiShareIdentityTypeEnum.getShareFlag(shareList.get(0).getIdentityType());

            shareFlagIdList = shareList
                    .stream()
                    .map(BiSubjectShareEntity::getIdentityId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        this.setShareFlag(shareFlag);
        this.setShareFlagIdList(shareFlagIdList);
        this.setShareUserIdList(shareFlagIdList);
    }
}
