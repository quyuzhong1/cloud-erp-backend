package com.erp.server.admin.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.admin.dto.SysFindMenuDTO;
import com.erp.model.admin.dto.SysMenuDTO;
import com.erp.model.admin.entity.MenuEntity;
import com.erp.model.sys.vo.SysMenuVO;
import com.erp.server.admin.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Classname AdminApiController
 * @Description TODO
 * @Date 2022-08-22 10:21
 * @Created by yl
 */
@RestController
@RequestMapping("menu")
public class AdminApiController  extends BaseController {

    @Autowired
    public MenuService menuService;




    /**
     * 菜单列表
     */
    @RequestMapping("/list")
    public ApiResult list(@RequestBody @Validated SysFindMenuDTO dto) {
        List<MenuEntity> list = menuService.menuList(dto);
        return success(list);
    }

    @RequestMapping("/tree")
    public ApiResult tree() {
        List<SysMenuVO> treeList = menuService.treeList();
        return success(treeList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{menuId}")
    public ApiResult info(@PathVariable("menuId") Long menuId) {
        MenuEntity sysMenu = menuService.getById(menuId);
        return success(sysMenu);
    }

    /**
     * 保存或者修改
     */
    @RequestMapping("/saveOrUpdate")
    public ApiResult save(@RequestBody MenuEntity sysMenu) {
        boolean resultFlag = menuService.saveOrUpdateMenu(sysMenu);
        return resultFlag == true ? success() : failure();
    }

    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody @Validated List<SysMenuDTO> list) {
        menuService.batchSaveMenu(list);
        return success();
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    public ApiResult update(@RequestBody MenuEntity sysMenu) {
        menuService.updateById(sysMenu);

        return success();
    }

    /**
     * 删除
     */
    @RequestMapping("/remove")
    public ApiResult delete(@RequestBody List<String> menuIds) {
        menuService.removeMenuByIds(menuIds);
        return success();
    }





}
