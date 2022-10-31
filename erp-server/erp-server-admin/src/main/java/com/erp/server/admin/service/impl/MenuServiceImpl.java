package com.erp.server.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.vo.SysMenuVO;
import com.erp.model.admin.dto.SysFindMenuDTO;
import com.erp.model.admin.dto.SysMenuDTO;
import com.erp.model.admin.entity.MenuEntity;
import com.erp.server.admin.mapper.MenuMapper;
import com.erp.server.admin.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname MenuServiceImpl
 * @Description TODO
 * @Date 2022-08-22 10:31
 * @Created by yl
 */
@Slf4j
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, MenuEntity> implements MenuService {



    @Override
    public void batchSaveMenu(List<SysMenuDTO> list) {
        List<MenuEntity> batchList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (SysMenuDTO dto : list) {
                getSaveTree("0", batchList, dto);
            }
        }
        this.saveOrUpdateBatch(batchList);
    }

    /**
     * 保存或者修改系统菜单
     *
     * @param sysMenu 系统餐单 入参
     * @return 返回  是否保存成功
     * @author yl
     * @date 2022-07-19 10:07
     */
    @Override
    public boolean saveOrUpdateMenu(MenuEntity sysMenu) {
        String menuId = sysMenu.getMenuId();
        if (StringUtils.isBlank(menuId)) {
            menuId = IdWorker.getIdStr();
        }
        String parentId = sysMenu.getParentId();
        if (StringUtils.isBlank(parentId)) {
            sysMenu.setParentId("0");
        }
        sysMenu.setMenuId(menuId);
        return this.saveOrUpdate(sysMenu);
    }

    @Override
    public List<MenuEntity> menuList(SysFindMenuDTO dto) {
        LambdaQueryWrapper<MenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuEntity::getType, dto.getMenuType());
        return this.list(queryWrapper);
    }

    @Override
    public List<SysMenuVO> treeList() {
        //获取到餐单的所有列表
        //  List<SysMenuEntity> allList = menuList(dto);
        List<MenuEntity> allList=list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<SysMenuVO> treeList = menuList.stream().
                filter(item -> "0".equals(item.getParentId())).
                map(item -> {
                    item.setParentName("");
                    item.setChildrenList(getChildrenList(item, menuList));

                    return item;
                }).collect(Collectors.toList());
        return treeList;
    }

    @Override
    public void removeMenuByIds(List<String> menuIds) {
        LambdaQueryWrapper<MenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MenuEntity::getParentId, menuIds);
        int count = this.count(queryWrapper);
        //表示有父类的id
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_9013);
        }
        boolean flag = this.removeByIds(menuIds);
        //删除成功就要去移除对应的角色权限
        if (flag) {
            baseMapper.removeRoleMenuByMenuIds(menuIds);
        }
    }


    /**
     * 获取子类的列表
     *
     * @param
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysMenuVO>
     * @author yl
     * @date 2022-07-19 11:11
     */
    public List<SysMenuVO> getChildrenList(SysMenuVO item, List<SysMenuVO> treeList) {
        List<SysMenuVO> collectList = treeList.stream().filter(menu -> item.getMenuId().equals(menu.getParentId()))
                .map(m -> {
                    m.setParentName(item.getMenuName());
                    m.setChildrenList(getChildrenList(m, treeList));
                    return m;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collectList) ? null : collectList;
    }

    /**
     * 获取保存的的集合数据
     *
     * @param parentId
     * @param batchList
     * @param item      传来的参数
     * @return void
     * @author yl
     * @date 2022-07-19 9:52
     */

    private void getSaveTree(String parentId, List<MenuEntity> batchList, SysMenuDTO item) {
        MenuEntity entity = new MenuEntity();
        BeanMapperUtils.copy(item, entity);
        entity.setParentId(parentId);
        String menuId = item.getMenuId();
        if (StringUtils.isBlank(menuId)) {
            menuId = IdWorker.getIdStr();
        }
        entity.setMenuId(menuId);
        batchList.add(entity);
        List<SysMenuDTO> childrenList = item.getChildrenList();
        if (CollectionUtils.isNotEmpty(childrenList)) {
            for (SysMenuDTO item1 : childrenList) {
                this.getSaveTree(menuId, batchList, item1);
            }
        }
    }

}
