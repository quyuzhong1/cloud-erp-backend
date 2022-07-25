package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.entity.SysRoleUserEntity;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.cloud.erp.common.common.dto.BaseSearchDTO;
import com.cloud.erp.common.utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * ${comments}
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-08 11:23:00
 */

public interface SysRoleUserService extends IService<SysRoleUserEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 批量保存 用户的角色id
     * @param uid
     * @param roleIds
     */
    void batchInsertRef(String uid, List<String> roleIds,boolean ifAdd);


    void removeRefByRoleId(List<String> roleIds);

    List<SysUserVO> findRoleUser(BaseSearchDTO dto);


    List<String> findRoleIdsByUid(String uid);

    List<SysRoleUserEntity> findRoleIdsByUidList(List<String> userIds);
}

