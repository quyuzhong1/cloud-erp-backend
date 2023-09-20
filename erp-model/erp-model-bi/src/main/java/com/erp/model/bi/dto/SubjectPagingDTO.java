package com.erp.model.bi.dto;

import com.common.business.validator.UpdateGroup;
import com.erp.model.bi.entity.BiSubjectShareEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname SubjectPagingDTO

 * @Date 2022-12-13 12:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SubjectPagingDTO implements Serializable {

    /**
     * 表id
     */
    @NotBlank(message = "id不能为空" ,groups = UpdateGroup.class )
    private String id;


    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 分类名
     */
    private String categoryName;

    /**
     * 专题名
     */
    private String name;

    /**
     * 是否常用
     * 0 不是 1 是
     */
    private Integer isFrequently;

    /**
     * 是否启用
     * true 启用
     * false 没有
     */
    private Boolean state;

    /**
     * 分享标示
     * personal 私人
     * share 共享
     * role 角色共享
     */
    private String shareFlag;

    /**
     * 创建人id
     */
    private String createUserId;


    /**
     * 创建人名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 更改人id
     */
    private String updateUserId;

    /**
     * 更改人
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 分享的身份id列表(用户ID/角色ID)
     */
    private List<String> shareFlagIdList = Collections.emptyList();

    /**
     * 检查和设置分享的身份id列表
     */
    public void checkAndSetShareFlagIdList(List<BiSubjectShareEntity> shareList) {
        if (CollectionUtils.isEmpty(shareList)){
            return;
        }
        List<String> identityIds = shareList.stream().map(BiSubjectShareEntity::getIdentityId).collect(Collectors.toList());
        this.setShareFlagIdList(identityIds);
    }
}
