package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.modules.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.BatchSaveRoleUserDTO;
import com.erp.model.sys.entity.SysRoleUserEntity;

import java.util.List;

/**
 * ${comments}
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-08 11:23:00
 */

public interface SysRoleUserService extends IService<SysRoleUserEntity> {



    /**
     * 批量保存 用户的角色id
     * @param uid
     * @param roleIds
     */
    void batchInsertRef(String uid, List<String> roleIds, boolean ifAdd);


    void removeRefByRoleId(List<String> roleIds);

    List<SysUserDTO> findRoleUser(BaseSearchDTO dto);


    List<String> findRoleIdsByUid(String uid);

    List<SysRoleUserEntity> findRoleIdsByUidList(List<String> userIds);

    boolean saveBatchRoleUser(BatchSaveRoleUserDTO dto);

    void copyRoleUser(String copyRoleId, String newRoleId);

    List<SysRoleUserEntity> roleUserList(String roleId);
}

