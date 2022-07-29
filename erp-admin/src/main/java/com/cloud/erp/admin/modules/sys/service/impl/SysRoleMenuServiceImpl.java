package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.constant.SysConstant;
import com.cloud.erp.admin.modules.sys.dto.SysRoleMenuBatchDTO;
import com.cloud.erp.admin.modules.sys.entity.SysMenuEntity;
import com.cloud.erp.admin.modules.sys.entity.SysRoleMenuEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysRoleMenuMapper;
import com.cloud.erp.admin.modules.sys.service.SysMenuService;
import com.cloud.erp.admin.modules.sys.service.SysRoleMenuService;
import com.cloud.erp.admin.modules.sys.vo.SysRoleMenuVO;
import com.commm.core.constant.CommonConstants;
import com.commm.core.utils.BeanMapperUtils;
import com.erp.common.modules.sys.vo.SysMenuVO;
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
        if(CollectionUtils.isNotEmpty(menuIds)){
            wrapper.in(SysRoleMenuEntity::getMenuId, menuIds);
            baseMapper.delete(wrapper);
        }



    }


    /**
     * 根据角色id 查询到对应的权限列表
     *
     * @param roleId
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysRoleMenuVO>
     * @author yl
     * @date 2022-07-19 17:38
     */

    @Override
    public List<SysRoleMenuVO> findRoleMenuTree(String roleId) {
        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysRoleMenuEntity::getMenuId);
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, roleId);
        List<Object> menuObjs = this.listObjs(queryWrapper);
        List<String> menuIds = BeanMapperUtils.copyList(String.class, menuObjs);
        List<SysMenuEntity> allList = sysMenuService.list();
        List<SysRoleMenuVO> menuList = BeanMapperUtils.copyList(SysRoleMenuVO.class, allList);
        List<SysRoleMenuVO> treeList = menuList.stream().
                filter(item -> "0".equals(item.getParentId())).
                map(item -> {
                    item.setParentName("");
                    item.setSelectState(false);
                    item.setChildrenList(getChildrenList(item, menuList, menuIds));
                    return item;
                }).collect(Collectors.toList());
        return treeList;
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
        Set<String> menuIds = batchDTO.getMenuIdList();
        String roleId = batchDTO.getRoleId();
        List<SysRoleMenuEntity> batchList = new LinkedList<>();
        for (String menuId : menuIds) {
            SysRoleMenuEntity entity = new SysRoleMenuEntity();
            entity.setMenuId(menuId);
            entity.setRoleId(roleId);
            batchList.add(entity);
        }
        removeByRoleId(roleId);
        return this.saveBatch(batchList);
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
     * @author yl
     * @date 2022-07-20 9:55
     */
    @Override
    public List<SysMenuVO> findMenuByRoleIds(List<String> roleIds) {
        if(CollectionUtils.isEmpty(roleIds)){
            return  new ArrayList<>();
        }
        List<SysMenuEntity> allList = sysMenuService.list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<String> menuIds;
        if (roleIds.contains(CommonConstants.ADMIN_ROLE_ID)) {
            menuIds=allList.stream().map(s->s.getMenuId()).collect(Collectors.toList());
        }else{
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
        if(CollectionUtils.isEmpty(roleIds)){
           return new ArrayList<>();
        }
        //如果有系统管理员显示所有的
        if(roleIds.contains(CommonConstants.ADMIN_ROLE_ID)){
             return baseMapper.findAllMenuCode(functionType);
        }else{
            return baseMapper.findMenuCodeByRoleIds(roleIds, functionType);
        }

    }

    /**
     * 方法说明
     * @author yl
     * @date 2022-07-29 9:24
     * @param roleIds 角色id
     * @return void
     */
    @Override
    public void removeRefByRoleIds(List<String> roleIds) {
        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.in(SysRoleMenuEntity::getRoleId,roleIds);
        this.remove(queryWrapper);

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
        List<SysMenuVO> collectList = treeList.stream().filter(menu -> (item.getMenuId().equals(menu.getParentId()) && menuIds.contains(menu.getMenuId()) && menu.getType() != SysConstant.FUNCTION_TYPE)).
                map(m -> {
                    m.setParentName(item.getMenuName());
                    m.setChildrenList(getRoleChildrenList(m, treeList, menuIds));
                    return m;
                }).collect(Collectors.toList());

        return CollectionUtils.isEmpty(collectList) ? null : collectList;
    }

    private List<SysRoleMenuVO> getChildrenList(SysRoleMenuVO item, List<SysRoleMenuVO> menuList, List<String> menuIds) {
        List<SysRoleMenuVO> collectList = menuList.stream().filter(menu -> item.getMenuId().equals(menu.getParentId()))
                .map(m -> {
                    m.setParentName(item.getMenuName());
                    String selectFlag = menuIds.stream().filter(r -> r.equals(m.getMenuId())).findFirst().orElse("0");
                    //表示 没有 选中
                    if ("0".equals(selectFlag)) {
                        m.setSelectState(false);
                    } else {
                        m.setSelectState(true);
                    }
                    m.setChildrenList(getChildrenList(m, menuList, menuIds));
                    return m;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collectList) ? null : collectList;
    }


}