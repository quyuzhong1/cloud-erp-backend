package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.vo.SysMenuVO;
import com.erp.model.sys.dto.RoleMenuDTO;
import com.erp.model.sys.dto.SysRoleMenuBatchDTO;
import com.erp.model.sys.dto.SysRoleMenuDTO;
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

    /**
     * 获取所有的菜单code
     * @Author Luo_WG
     * @Date 2022/11/1 14:23
     * @return java.util.List<com.erp.model.sys.vo.SysMenuVO>
     **/
    List<SysMenuVO> findMenuAll();

    List<String> findMenuCodeByRoleIds(List<String> ids, Integer type);

    /**
     * 获取所有菜单code
     * @Author Luo_WG
     * @Date 2022/11/1 14:30
     * @return java.util.List<java.lang.String>
     **/
    List<String> findMenuCodeAll();

    void removeRefByRoleIds(List<String> roleIds);

    void copyRoleMenu(String copyRoleId, String newRoleId);

    RoleMenuDTO findRoleMenuTreeByRoleId(String roleId);

    List<SysMenuVO> findLeftMenuByRoleIds(List<String> roleIds);

    /**
     * 获取左侧菜单所有列表
     * @Author Luo_WG
     * @Date 2022/11/1 14:26
     * @return java.util.List<com.erp.model.sys.vo.SysMenuVO>
     **/
    List<SysMenuVO> findLeftMenuAll();

    Boolean saveRoleMenu(SysRoleMenuDTO dto);

    List<SysRoleMenuEntity> getMenuRefRoleByRoleIds(List<String> roleIdList);
}

