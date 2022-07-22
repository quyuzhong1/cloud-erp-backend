package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.dto.SysFindMenuDTO;
import com.cloud.erp.admin.modules.sys.dto.SysMenuDTO;
import com.cloud.erp.admin.modules.sys.entity.SysMenuEntity;
import com.cloud.erp.common.modules.sys.vo.SysMenuVO;

import java.util.List;

/**
 * 菜单表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
public interface SysMenuService extends IService<SysMenuEntity> {


    void batchSaveMenu(List<SysMenuDTO> list);

    boolean saveOrUpdateMenu(SysMenuEntity sysMenu);

    List<SysMenuEntity> menuList(SysFindMenuDTO dto);

    List<SysMenuVO> treeList();

    void removeMenuByIds(List<String> menuIds);
}

