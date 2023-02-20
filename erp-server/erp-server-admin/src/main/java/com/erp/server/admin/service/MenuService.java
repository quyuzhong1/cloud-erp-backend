package com.erp.server.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.admin.dto.SysFindMenuDTO;
import com.erp.model.admin.dto.SysMenuDTO;
import com.erp.model.admin.entity.MenuEntity;
import com.erp.model.sys.vo.SysMenuVO;

import java.util.List;

/**
 * @Classname MenuService
 * @Description TODO
 * @Date 2022-08-22 10:30
 * @Created by yl
 */
public interface MenuService  extends IService<MenuEntity> {


    void batchSaveMenu(List<SysMenuDTO> list);

    boolean saveOrUpdateMenu(MenuEntity sysMenu);

    List<MenuEntity> menuList(SysFindMenuDTO dto);

    List<SysMenuVO> treeList();

    void removeMenuByIds(List<String> menuIds);
}
