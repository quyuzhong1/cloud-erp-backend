package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiModulePermissionEntity;

import java.util.List;


/**
 * 模块权限表(BiModulePermission)表服务接口
 *
 * @author yl
 * @since 2022-12-12 10:29:35
 */
public interface BiModulePermissionService  extends IService<BiModulePermissionEntity> {


    void addModulePermission(String id, List<String> permissionUserIdList);

    void deleteByModuleId(String id);
}
