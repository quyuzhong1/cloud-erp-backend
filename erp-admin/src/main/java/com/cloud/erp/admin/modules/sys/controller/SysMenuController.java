package com.cloud.erp.admin.modules.sys.controller;


import com.cloud.erp.admin.modules.sys.dto.SysFindMenuDTO;
import com.cloud.erp.admin.modules.sys.dto.SysMenuDTO;
import com.cloud.erp.admin.modules.sys.entity.SysMenuEntity;
import com.cloud.erp.admin.modules.sys.service.SysMenuService;
import com.cloud.erp.common.common.ApiResult;
import com.cloud.erp.common.common.BaseController;
import com.cloud.erp.common.modules.sys.vo.SysMenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 菜单表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@RestController
@RequestMapping("sys/menu")
public class SysMenuController extends BaseController {
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 菜单列表
     */
    @RequestMapping("/list")
    public ApiResult list(@RequestBody @Validated SysFindMenuDTO dto) {
        List<SysMenuEntity> list = sysMenuService.menuList(dto);
        return success(list);
    }

    @RequestMapping("/tree")
    public ApiResult tree() {
        List<SysMenuVO> treeList = sysMenuService.treeList();
        return success(treeList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{menuId}")
    public ApiResult info(@PathVariable("menuId") Long menuId) {
        SysMenuEntity sysMenu = sysMenuService.getById(menuId);

        return success(sysMenu);
    }

    /**
     * 保存或者修改
     */
    @RequestMapping("/saveOrUpdate")
    public ApiResult save(@RequestBody SysMenuEntity sysMenu) {
        boolean resultFlag = sysMenuService.saveOrUpdateMenu(sysMenu);
        return resultFlag == true ? success() : failure();
    }

    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody @Validated List<SysMenuDTO> list) {
        sysMenuService.batchSaveMenu(list);
        return success();
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    public ApiResult update(@RequestBody SysMenuEntity sysMenu) {
        sysMenuService.updateById(sysMenu);

        return success();
    }

    /**
     * 删除
     */
    @RequestMapping("/remove")
    public ApiResult delete(@RequestBody List<String> menuIds) {
        sysMenuService.removeMenuByIds(menuIds);
        return success();
    }

}
