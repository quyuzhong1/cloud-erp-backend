package com.erp.model.bi.dto;

import com.common.business.validator.UpdateGroup;
import com.common.core.anno.StateEnumValue;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.entity.BiModulePermissionEntity;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模块
 *
 * @Classname ModuleDTO

 * @Date 2022-12-12 9:46
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ModuleDTO implements Serializable {


    /**
     * 表id
     */
    @NotBlank(message = "id不能为空",  groups = {UpdateGroup.class} )
    private String id;

    /**
     *系统模块id
     */
    private String sysModuleId;

    /**
     * 分类id
     * 来源于字典表
     */
    private String categoryId;


    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空")
    private String name;


    /**
     * 模块说明
     */
    private String remark;


    /**
     * 图片地址
     */
    private Object imageFile=null;

    /**
     * 图片地址
     */
    private String imageUrl;


    /**
     * 编辑的上传表示
     * 如果为true 表示
     * 改变了  如果为false 就是没有
     */
    @NotNull(message = "上传标识不能为空",  groups = {UpdateGroup.class} )
    private Boolean uploadFlag=false;


    /**
     * 前端组件名不能为空
     */
    @NotBlank(message = "前端组件名不能为空")
    private String viewCode;

    @NotBlank(message = "编码不能为空")
    private String code;

    /**
     * 分享的ID, 用户ID/角色ID
     */
    private List<String> shareFlagIdList;

    /**
     * 分享标示
     * personal 私人
     * share 按多用户ID共享
     * role 按多角色ID
     */
    @StateEnumValue(strValues = {"personal","share","role"},message = "分享类型有误")
    private String shareFlag;

    /**
     * 原用户ID列表
     */
    @Deprecated
    private List<String> permissionUserIdList;

    public List<String> checkAndGetShareFlagIdList() {
        if ("personal".equalsIgnoreCase(this.shareFlag)){
            return shareFlagIdList;
        }
        if (CollectionUtils.isEmpty(this.shareFlagIdList)){
            throw new ServiceException("shareFlagIdList分享的标识ID列表不能为空");
        }
        return shareFlagIdList;
    }

    /**
     * 设置权限信息
     */
    public void checkAndSetFlagInfo(List<BiModulePermissionEntity> permissionList) {
        String shareFlag = "personal";
        List<String> shareFlagIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(permissionList)){
            shareFlag = BiShareIdentityTypeEnum.getShareFlag(permissionList.get(0).getIdentityType());

            shareFlagIdList = permissionList
                    .stream()
                    .map(BiModulePermissionEntity::getIdentityId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        this.setShareFlag(shareFlag);
        this.setShareFlagIdList(shareFlagIdList);
        this.setPermissionUserIdList(shareFlagIdList);
    }
}
