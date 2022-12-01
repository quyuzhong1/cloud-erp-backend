package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.constant.CommonConstants;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.modules.sys.vo.SysMenuVO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysMenuEntity;
import com.erp.model.sys.entity.SysRoleMenuEntity;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.mapper.SysRoleMenuMapper;
import com.erp.server.sys.service.SysMenuService;
import com.erp.server.sys.service.SysRoleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenuEntity> implements SysRoleMenuService {


    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 根据菜单id 删除对应角色菜单绑定的关系
     *
     * @param menuIds
     * @return void
     * @author yl
     * @date 2022-07-19 15:50
     */

    @Override
    @Transactional
    public void removeByMenuIds(List<String> menuIds) {
        LambdaQueryWrapper<SysRoleMenuEntity> wrapper = new LambdaQueryWrapper();
        if (CollectionUtils.isNotEmpty(menuIds)) {
            wrapper.in(SysRoleMenuEntity::getMenuId, menuIds);
            baseMapper.delete(wrapper);
        }
    }


    //根据角色id 获取菜单id
    public List<String> getMenuIdByRoleId(String roleId) {
        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysRoleMenuEntity::getMenuId);
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, roleId);
        List<Object> menuObjs = this.listObjs(queryWrapper);
        if (CollectionUtils.isNotEmpty(menuObjs)) {
            List<String> menuIds = BeanMapperUtils.copyList(String.class, menuObjs);
            return menuIds;
        }
        return new ArrayList<>();
    }

    /**
     * 批量保存 角色与 菜单的关系
     *
     * @param batchDTO
     * @return boolean
     * @author yl
     * @date 2022-07-20 9:20
     */
    @Override
    @Transactional
    public boolean batchSaveRoleMenu(SysRoleMenuBatchDTO batchDTO) {
        Set<SysRoleMenuDataScopeDTO> menuIds = batchDTO.getMenuIdList();
        String roleId = batchDTO.getRoleId();
        List<SysRoleMenuEntity> batchList = new LinkedList<>();
        removeByRoleId(roleId);
        if (CollectionUtils.isNotEmpty(menuIds)) {
            for (SysRoleMenuDataScopeDTO menuId : menuIds) {
                SysRoleMenuEntity entity = new SysRoleMenuEntity();
                entity.setMenuId(menuId.getMenuId());
                entity.setRoleId(roleId);
                entity.setDataScope(menuId.getDataScope());
                batchList.add(entity);
            }
            return this.saveBatch(batchList);
        }
        return true;

    }

    /**
     * 根据角色id 删除
     *
     * @param roleId
     * @return void
     * @author yl
     * @date 2022-07-20 15:12
     */
    private void removeByRoleId(String roleId) {
        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, roleId);
        baseMapper.delete(queryWrapper);
    }


    /**
     * 根据角色id 获取到 所以的菜单code
     *
     * @param roleIds
     * @return java.util.List<java.lang.String>
     * @author yl  1580852739573813249  1580852739573813249
     * @date 2022-07-20 9:55
     */
    @Override
    public List<SysMenuVO> findMenuByRoleIds(List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        List<SysMenuEntity> allList = sysMenuService.list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<String> menuIds;
        if (roleIds.contains(CommonConstants.ADMIN_ROLE_ID)) {
            menuIds = allList.stream().map(s -> s.getMenuId()).collect(Collectors.toList());
        } else {
            menuIds = baseMapper.findMenuIdsByRoleIds(roleIds);
        }
        List<SysMenuVO> resultList = menuList.stream().
                filter(item -> "0".equals(item.getParentId()) && menuIds.contains(item.getMenuId())).
                map(item -> {
                    item.setParentName("");
                    item.setChildrenList(getRoleChildrenList(item, menuList, menuIds));
                    return item;
                }).collect(Collectors.toList());
        return resultList;

    }

    /**
     * 获取所有的菜单code
     *
     * @return java.util.List<com.erp.common.modules.sys.vo.SysMenuVO>
     * @Author Luo_WG
     * @Date 2022/11/1 14:23
     **/
    @Override
    public List<SysMenuVO> findMenuAll() {
        List<SysMenuEntity> allList = sysMenuService.list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<String> menuIds = allList.stream().map(s -> s.getMenuId()).collect(Collectors.toList());
        List<SysMenuVO> resultList = menuList.stream().
                filter(item -> "0".equals(item.getParentId()) && menuIds.contains(item.getMenuId())).
                map(item -> {
                    item.setParentName("");
                    item.setChildrenList(getRoleChildrenList(item, menuList, menuIds));
                    return item;
                }).collect(Collectors.toList());
        return resultList;
    }


    /**
     * 根据角色id 和类型查找对用的code
     *
     * @param roleIds
     * @param functionType
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-07-20 14:19
     */
    @Override
    public List<String> findMenuCodeByRoleIds(List<String> roleIds, Integer functionType) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        //如果有系统管理员显示所有的
        if (roleIds.contains(CommonConstants.ADMIN_ROLE_ID)) {
            return baseMapper.findAllMenuCode(functionType);
        } else {
            return baseMapper.findMenuCodeByRoleIds(roleIds, functionType);
        }
    }

    /**
     * 获取所有菜单code
     *
     * @return java.util.List<java.lang.String>
     * @Author Luo_WG
     * @Date 2022/11/1 14:30
     **/
    @Override
    public List<String> findMenuCodeAll() {
        return baseMapper.findAllMenuCode(null);
    }

    /**
     * 方法说明
     *
     * @param roleIds 角色id
     * @return void
     * @author yl
     * @date 2022-07-29 9:24
     */
    @Override
    public void removeRefByRoleIds(List<String> roleIds) {
        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(SysRoleMenuEntity::getRoleId, roleIds);
        this.remove(queryWrapper);
    }

    /**
     * 复制角色菜单
     *
     * @param copyRoleId
     * @param newRoleId
     * @return void
     * @author yl
     * @date 2022-07-29 14:58
     */

    @Override
    public void copyRoleMenu(String copyRoleId, String newRoleId) {
        List<String> menuIds = getMenuIdByRoleId(copyRoleId);
        if (CollectionUtils.isNotEmpty(menuIds)) {
            List<SysRoleMenuEntity> addList = new LinkedList<>();
            for (String menuId : menuIds) {
                SysRoleMenuEntity entity = new SysRoleMenuEntity();
                entity.setRoleId(newRoleId);
                entity.setMenuId(menuId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }


    }

    @Override
    public RoleMenuDTO findRoleMenuTreeByRoleId(String roleId) {
        RoleMenuDTO roleMenuVO = new RoleMenuDTO();
        List<String> menuIds = getMenuIdByRoleId(roleId);

        List<SysMenuEntity> allList = sysMenuService.list(new LambdaQueryWrapper<SysMenuEntity>().orderByDesc(SysMenuEntity::getType));

        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, roleId);
        List<SysRoleMenuEntity> sysRoleMenuEntityList = this.list(queryWrapper);

        List<RoleMenuTreeDTO> menuList = BeanMapperUtils.copyList(RoleMenuTreeDTO.class, allList);
        List<RoleMenuTreeDTO> treeList = menuList.stream().
                filter(item -> "0".equals(item.getParentId())).
                map(item -> {
                    SysRoleMenuEntity sysRoleMenuEntity = sysRoleMenuEntityList.stream().filter(roleMenu -> item.getMenuId().equals(roleMenu.getMenuId())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(sysRoleMenuEntity)) {
                        item.setDataScope(sysRoleMenuEntity.getDataScope());
                    } else {
                        item.setDataScope(1);
                    }
                    item.setParentName("");
                    item.setSelectState(false);
                    item.setChildrenList(getChildrenList(item, menuList, menuIds, sysRoleMenuEntityList));
                    return item;
                }).collect(Collectors.toList());

        roleMenuVO.setSysRoleMenuTrees(treeList);
        roleMenuVO.setSelectedMenuIds(menuIds);
        roleMenuVO.setTotalMenu(allList.size());
        return roleMenuVO;
    }


    /**
     * 获取到左侧菜单
     *
     * @param roleIds
     * @return java.util.List<com.erp.common.modules.sys.vo.SysMenuVO>
     * @author yl
     * @date 2022-09-26 9:44
     */
    @Override
    public List<SysMenuVO> findLeftMenuByRoleIds(List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        List<SysMenuEntity> allList = sysMenuService.list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<String> menuIds;
        if (roleIds.contains(CommonConstants.ADMIN_ROLE_ID)) {
            menuIds = allList.stream().map(s -> s.getMenuId()).collect(Collectors.toList());
        } else {
            menuIds = baseMapper.findMenuIdsByRoleIds(roleIds);
        }
        List<SysMenuVO> resultList = menuList.stream().
                filter(item -> "0".equals(item.getParentId()) && menuIds.contains(item.getMenuId())).
                map(item -> {
                    item.setParentName("");
                    item.setChildrenList(getRoleChildrenLeftList(item, menuList, menuIds, SysConstant.FUNCTION_TYPE, SysConstant.BUTTON_TYPE));
                    return item;
                }).collect(Collectors.toList());
        return resultList;
    }


    /**
     * 获取左侧菜单所有列表
     *
     * @return java.util.List<com.erp.common.modules.sys.vo.SysMenuVO>
     * @Author Luo_WG
     * @Date 2022/11/1 14:26
     **/
    @Override
    public List<SysMenuVO> findLeftMenuAll() {
        List<SysMenuEntity> allList = sysMenuService.list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<String> menuIds = allList.stream().map(s -> s.getMenuId()).collect(Collectors.toList());
        List<SysMenuVO> resultList = menuList.stream().
                filter(item -> "0".equals(item.getParentId()) && menuIds.contains(item.getMenuId())).
                map(item -> {
                    item.setParentName("");
                    item.setChildrenList(getRoleChildrenLeftList(item, menuList, menuIds, SysConstant.FUNCTION_TYPE, SysConstant.BUTTON_TYPE));
                    return item;
                }).collect(Collectors.toList());
        return resultList;
    }

    /**
     * bao
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-14 16:01
     */
    @Override
    public Boolean saveRoleMenu(SysRoleMenuDTO dto) {
        SysRoleMenuEntity entity = new SysRoleMenuEntity();
        entity.setMenuId(dto.getMenuId());
        entity.setRoleId(dto.getRoleId());
        entity.setDataScope(dto.getDataScope());
        return this.save(entity);

    }

    @Override
    public List<SysRoleMenuEntity> getMenuRefRoleByRoleIds(List<String> roleIdList) {
        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRoleMenuEntity::getRoleId, roleIdList);
        return this.list(queryWrapper);
    }

    /**
     * 获取详情的菜单
     *
     * @param item
     * @param treeList
     * @param menuIds
     * @param functionType
     * @param buttonType
     * @return java.util.List<com.erp.common.modules.sys.vo.SysMenuVO>
     * @author yl
     * @date 2022-09-26 9:54
     */
    private List<SysMenuVO> getRoleChildrenLeftList(SysMenuVO item, List<SysMenuVO> treeList, List<String> menuIds, Integer functionType, Integer buttonType) {
        List<SysMenuVO> collectList = treeList.stream().filter(menu -> (item.getMenuId().equals(menu.getParentId()) && menuIds.contains(menu.getMenuId()) && menu.getType() != functionType && menu.getType() != buttonType)).
                map(m -> {
                    m.setParentName(item.getMenuName());
                    m.setChildrenList(getRoleChildrenLeftList(m, treeList, menuIds, functionType, buttonType));
                    return m;
                }).collect(Collectors.toList());

        return CollectionUtils.isEmpty(collectList) ? null : collectList;
    }


    /**
     * 获取到子菜单
     *
     * @param item
     * @param treeList
     * @param menuIds
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysMenuVO>
     * @author yl
     * @date 2022-07-20 11:30
     */
    private List<SysMenuVO> getRoleChildrenList(SysMenuVO item, List<SysMenuVO> treeList, List<String> menuIds) {
        //menu.getType() != SysConstant.FUNCTION_TYPE
        List<SysMenuVO> collectList = treeList.stream().filter(menu -> (item.getMenuId().equals(menu.getParentId()) && menuIds.contains(menu.getMenuId()))).
                map(m -> {
                    m.setParentName(item.getMenuName());
                    m.setChildrenList(getRoleChildrenList(m, treeList, menuIds));
                    return m;
                }).collect(Collectors.toList());

        return CollectionUtils.isEmpty(collectList) ? null : collectList;
    }

    private List<RoleMenuTreeDTO> getChildrenList(RoleMenuTreeDTO item, List<RoleMenuTreeDTO> menuList, List<String> menuIds, List<SysRoleMenuEntity> sysRoleMenuEntityList) {
        List<RoleMenuTreeDTO> collectList = menuList.stream().filter(menu -> item.getMenuId().equals(menu.getParentId()))
                .map(m -> {
                    m.setParentName(item.getMenuName());
                    String selectFlag = menuIds.stream().filter(r -> r.equals(m.getMenuId())).findFirst().orElse("0");
                    //表示 没有 选中
                    if ("0".equals(selectFlag)) {
                        m.setSelectState(false);
                    } else {
                        m.setSelectState(true);
                    }

                    SysRoleMenuEntity sysRoleMenuEntity = sysRoleMenuEntityList.stream().filter(roleMenu -> m.getMenuId().equals(roleMenu.getMenuId())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(sysRoleMenuEntity)) {
                        m.setDataScope(sysRoleMenuEntity.getDataScope());
                    } else {
                        m.setDataScope(1);
                    }
                    m.setChildrenList(getChildrenList(m, menuList, menuIds, sysRoleMenuEntityList));
                    return m;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collectList) ? null : collectList;
    }


}