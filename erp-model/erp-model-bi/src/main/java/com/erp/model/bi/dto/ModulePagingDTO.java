package com.erp.model.bi.dto;

import com.erp.model.bi.entity.BiModulePermissionEntity;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname ModulePagingDTO

 * @Date 2022-12-12 11:27
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ModulePagingDTO implements Serializable {

    /**
     * 表id
     */
    private String id;

    /**
     * 模块名
     */
    private String name;

    /**
     * 系统模块名
     */
    private String sysModuleName;

    /**
     * 备注说明
     */
    private String remark;


    /**
     * 缩略图地址
     */
    private String imageUrl;

    /**
     * 状态
     */
    private Boolean state;


    /**
     * 本月使用次数
     */
    private Integer monthUsageCount = 0;


    /**
     * 使用次数
     */
    private Integer usageCount = 0;

    /**
     * 分享标示
     * share 共享
     * role 角色共享
     */
    private String shareFlag = "share";


    /**
     * 分享的身份id列表(用户ID/角色ID)
     */
    private List<String> shareFlagIdList = Collections.emptyList();


    /**
     * 检查和设置权限信息
     */
    public void checkAndSetShareFlagInfo(List<BiModulePermissionEntity> permissionList) {
        if (CollectionUtils.isEmpty(permissionList)){
            return;
        }
        boolean isRole = BiShareIdentityTypeEnum.ROLE.getCode().equalsIgnoreCase(permissionList.get(0).getIdentityType());
        if (isRole){
            this.setShareFlag(BiShareIdentityTypeEnum.ROLE.getCode());
        }
        List<String> identityIds = permissionList.stream().map(BiModulePermissionEntity::getIdentityId).collect(Collectors.toList());
        this.setShareFlagIdList(identityIds);
    }
}
