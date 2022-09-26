package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.modules.sys.vo.SysMenuVO;
import com.erp.model.sys.dto.RoleMenuDTO;
import com.erp.model.sys.dto.SysRoleMenuBatchDTO;
import com.erp.model.sys.entity.SysRoleMenuEntity;

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




    boolean batchSaveRoleMenu(SysRoleMenuBatchDTO batchDTO);


    List<SysMenuVO> findMenuByRoleIds(List<String> ids);


    List<String> findMenuCodeByRoleIds(List<String> ids, Integer type);

    void removeRefByRoleIds(List<String> roleIds);

    void copyRoleMenu(String copyRoleId, String newRoleId);

    RoleMenuDTO findRoleMenuTreeByRoleId(String roleId);

    List<SysMenuVO> findLeftMenuByRoleIds(List<String> roleIds);
}

