package com.erp.server.admin.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.admin.dto.SysFindMenuDTO;
import com.erp.model.admin.dto.SysMenuDTO;
import com.erp.model.admin.entity.MenuEntity;
import com.erp.model.sys.vo.SysMenuVO;
import com.erp.server.admin.mapper.MenuMapper;
import com.erp.server.admin.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname MenuServiceImpl

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
    @Transactional(rollbackFor = Exception.class)
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
        boolean save = this.saveOrUpdate(sysMenu);
        if (save) {
            //排序
            sortMenu(sysMenu);
        }
       return save;
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
        List<MenuEntity> allList=list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        return menuList.stream().
                filter(item -> "0".equals(item.getParentId()))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .peek(item -> {
                    item.setParentName("");
                    item.setChildrenList(getChildrenList(item, menuList));
                }).collect(Collectors.toList());
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
     * @description: 菜单排序
     * @author Will
     * @date: 2024/3/12 15:40
     * @param sysMenu
     */
    private void sortMenu (MenuEntity sysMenu) {
        //更新菜单排序
        List<MenuEntity> menuList = this.listByParentId(sysMenu.getParentId());
        //大于等于当前序号的同级别的菜单重新排序
        List<MenuEntity> levelMenuList = menuList.stream().filter(obj -> MathUtil.compareTo(obj.getIndex(), sysMenu.getIndex()) >= MathUtil.ZERO
                        && !CharSequenceUtil.equals(sysMenu.getMenuId(),obj.getMenuId()))
                .sorted(Comparator.comparing(MenuEntity::getIndex)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(levelMenuList)) {
            return ;
        }
        if (ObjectUtil.isEmpty(sysMenu.getIndex())) {
            throw new ServiceException("排序字段必填");
        }
        Integer index = sysMenu.getIndex() + 1;
        for (MenuEntity menuEntity : levelMenuList) {
            menuEntity.setIndex(index);
            index ++;
        }
        this.saveOrUpdateBatch(levelMenuList);
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
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .peek(m -> {
                    m.setParentName(item.getMenuName());
                    m.setChildrenList(getChildrenList(m, treeList));
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collectList) ? null : collectList;
    }

    /**
     * @description: 根据父级id查询
     * @author Will
     * @date: 2024/3/12 15:29
     * @param parentId
     * @return List<MenuEntity>
     */
    private List<MenuEntity> listByParentId(String parentId) {
        return lambdaQuery().eq(MenuEntity::getParentId, parentId)
                .orderByAsc(MenuEntity::getIndex)
                .list();
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
