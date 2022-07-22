package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.dto.SysFindMenuDTO;
import com.cloud.erp.admin.modules.sys.dto.SysMenuDTO;
import com.cloud.erp.admin.modules.sys.entity.SysMenuEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysMenuMapper;
import com.cloud.erp.admin.modules.sys.service.SysMenuService;
import com.cloud.erp.admin.modules.sys.service.SysRoleMenuService;
import com.cloud.erp.common.common.ApiError;
import com.cloud.erp.common.common.exception.ServiceException;
import com.cloud.erp.common.modules.sys.vo.SysMenuVO;
import com.cloud.erp.common.utils.BeanMapperUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;


    /**
     * 批量保存菜单树结构
     *
     * @param list
     * @return void
     * @author yl
     * @date 2022-07-19 9:45
     */

    @Override
    public void batchSaveMenu(List<SysMenuDTO> list) {
        List<SysMenuEntity> batchList = new LinkedList<>();
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
    public boolean saveOrUpdateMenu(SysMenuEntity sysMenu) {
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

    /**
     * 根据菜单类型 显示菜单
     *
     * @param dto
     * @return java.util.List<com.cloud.erp.admin.modules.sys.entity.SysMenuEntity>
     * @author yl
     * @date 2022-07-19 10:34
     */
    @Override
    public List<SysMenuEntity> menuList(SysFindMenuDTO dto) {
        LambdaQueryWrapper<SysMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenuEntity::getType, dto.getMenuType());
        return this.list(queryWrapper);
    }

    /**
     * 菜单树结构
     *
     * @param
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysMenuVO>
     * @author yl
     * @date 2022-07-19 10:58
     */
    @Override
    public List<SysMenuVO> treeList() {
        //获取到餐单的所有列表
      //  List<SysMenuEntity> allList = menuList(dto);
        List<SysMenuEntity> allList=list();
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


    /**
     * 删除菜单列表
     * @author yl
     * @date 2022-07-19 15:43
     * @param menuIds
     * @return void
     */

    @Override
    public void removeMenuByIds(List<String> menuIds) {
        LambdaQueryWrapper<SysMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysMenuEntity::getParentId, menuIds);
        int count = this.count(queryWrapper);
        //表示有父类的id
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_9013);
        }
        boolean flag = this.removeByIds(menuIds);
        //删除成功就要去移除对应的角色权限
        if (flag) {
            sysRoleMenuService.removeByMenuIds(menuIds);
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

    private void getSaveTree(String parentId, List<SysMenuEntity> batchList, SysMenuDTO item) {
        SysMenuEntity entity = new SysMenuEntity();
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