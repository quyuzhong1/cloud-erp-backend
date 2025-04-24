package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiModulePermissionEntity;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;

import java.util.List;
import java.util.Map;


/**
 * 模块权限表(BiModulePermission)表服务接口
 *
 * @author yl
 * @since 2022-12-12 10:29:35
 */
public interface BiModulePermissionService  extends IService<BiModulePermissionEntity> {


    void addModulePermission(String id, List<String> permissionUserIdList, BiShareIdentityTypeEnum typeEnum);

    void deleteByModuleId(String id);

    List<String> getByModuleId(String moduleId);

    /**
     * @deprecated
     * This method is deprecated and will be removed in future versions.
     * Please use {@link #getUserVisibleModuleIdsNew(String)} instead.
     */
    @Deprecated
    List<String> getModuleIdsByUserId(String userId);

    /**
     * 检查和添加
     */
    void checkAndAddModulePermission(List<String> shareFlagIdList, String mainId, String shareFlag);

    /**
     * 通过ModuleId查询
     */
    List<BiModulePermissionEntity> findByModuleId(String moduleId);

    /**
     * 通过ModuleIds查询
     */
    Map<String, List<BiModulePermissionEntity>> mapByModuleIds(List<String> moduleIds);


    /**
     * 查询当前用户共享的ModuleId
     */
    List<String> findModuleId(String userId, List<String> roleIdList);
}
