package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.dto.SysRoleMenuBatchDTO;
import com.cloud.erp.admin.modules.sys.entity.SysRoleMenuEntity;
import com.cloud.erp.admin.modules.sys.vo.SysRoleMenuVO;
import com.erp.common.modules.sys.vo.SysMenuVO;

import java.util.List;

/**
 * ${comments}
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
public interface SysRoleMenuService extends IService<SysRoleMenuEntity> {


    void removeByMenuIds(List<String> menuIds);


    List<SysRoleMenuVO> findRoleMenuTree(String roleId);

    boolean batchSaveRoleMenu(SysRoleMenuBatchDTO batchDTO);


    List<SysMenuVO> findMenuByRoleIds(List<String> ids);


    List<String> findMenuCodeByRoleIds(List<String> ids,Integer type);

    void removeRefByRoleIds(List<String> roleIds);

    void copyRoleMenu(String copyRoleId, String newRoleId);
}

