package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.mask.resolver.MaskPermissionEvictPublisher;
import com.common.business.dataperm.DataPermissionContextEvictPublisher;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.constant.BusinessCommonConstants;
import com.common.core.constant.CommonConstants;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysMenuEntity;
import com.erp.model.sys.entity.SysRoleMenuEntity;
import com.erp.model.sys.vo.SysMenuVO;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.mapper.SysRoleMenuMapper;
import com.erp.server.sys.service.SysMenuService;
import com.erp.server.sys.service.SysRoleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenuEntity> implements SysRoleMenuService {


    @Autowired
    private SysMenuService sysMenuService;

    @Autowired(required = false)
    private MaskPermissionEvictPublisher maskPermissionEvictPublisher;

    @Autowired(required = false)
    private DataPermissionContextEvictPublisher dataPermissionContextEvictPublisher;

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
            publishMaskPermEvictAll("SysRoleMenuService.removeByMenuIds");
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
        LocalDateTime now = LocalDateTime.now();
        LoginUser loginUser = UserContext.getNonLoginUser();
        try {
            if (CollectionUtils.isNotEmpty(menuIds)) {
                for (SysRoleMenuDataScopeDTO menuId : menuIds) {
                    SysRoleMenuEntity entity = new SysRoleMenuEntity();
                    entity.setMenuId(menuId.getMenuId());
                    entity.setRoleId(roleId);
                    entity.setDataScope(menuId.getDataScope());
                    // 处理公共字段
                    handleCommonField(entity, now, loginUser);
                    batchList.add(entity);
                }
                return this.saveBatch(batchList);
            }
            return true;
        } finally {
            // role 改动影响所有持有该 role 的用户；本服务不存"role→users"反向索引，统一 ALL 清除
            publishMaskPermEvictAll("SysRoleMenuService.batchSaveRoleMenu");
        }

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
    public List<SysMenuVO> findMenuByRoleIds(List<String> roleIds, String userType) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        List<SysMenuEntity> allMenuList = sysMenuService
                .lambdaQuery()
                .eq(SysMenuEntity::getDisabled, Boolean.FALSE)
                .eq(StringUtils.isNotBlank(userType), SysMenuEntity::getSystem, userType)
                .eq(BusinessCommonConstants.isArchive(), SysMenuEntity::getIsArchiveDisplay , Boolean.TRUE)
                .list();
        if (CollectionUtils.isEmpty(allMenuList)) {
            return Collections.emptyList();
        }
        // 2. Entity → VO
        List<SysMenuVO> menuVOList = BeanMapperUtils.copyList(SysMenuVO.class, allMenuList);
        // 3. 计算【最终可用 menuId 集合】（Set，关键优化）
        Set<String> menuIdSet;
        if (roleIds.contains(CommonConstants.ADMIN_ROLE_ID)) {
            menuIdSet = allMenuList.stream().map(s -> s.getMenuId()).collect(Collectors.toSet());
        } else {
            // 普通角色：角色菜单 + 所有父级
            List<String> roleMenuIds = baseMapper.findMenuIdsByRoleIds(roleIds);
            menuIdSet = getSelfAndParentMenuIds(roleMenuIds, allMenuList);
        }
        if (CollectionUtils.isEmpty(menuIdSet)) {
            return Collections.emptyList();
        }
        // 4. 只在这里做一次 Set → List 的转换
        List<String> menuIdList = new ArrayList<>(menuIdSet);
        // 5. 构建左侧菜单树（只处理根节点）
        return menuVOList.stream().
                filter(item -> "0".equals(item.getParentId()) && menuIdList.contains(item.getMenuId()))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .map(item -> {
                    item.setParentName("");
                    item.setChildrenList(getRoleChildrenList(item, menuVOList, menuIdList));
                    return item;
                }).collect(Collectors.toList());
    }

    /**
     * 获取所有的菜单code
     *
     * @return java.util.List<com.erp.model.sys.vo.SysMenuVO>
     * @Author Luo_WG
     * @Date 2022/11/1 14:23
     **/
    @Override
    public List<SysMenuVO> findMenuAll(String userType) {
        List<SysMenuEntity> allList = sysMenuService
                .lambdaQuery()
                .eq(SysMenuEntity::getDisabled, Boolean.FALSE)
                .eq(StringUtils.isNotBlank(userType), SysMenuEntity::getSystem, userType)
                .list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<String> menuIds = allList.stream().map(s -> s.getMenuId()).collect(Collectors.toList());
        List<SysMenuVO> resultList = menuList.stream().
                filter(item -> "0".equals(item.getParentId()) && menuIds.contains(item.getMenuId()))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .map(item -> {
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
    public List<String> findMenuCodeByRoleIds(List<String> roleIds, Integer functionType, String userType) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        boolean archive = BusinessCommonConstants.isArchive();
        //如果有系统管理员显示所有的
        if (roleIds.contains(CommonConstants.ADMIN_ROLE_ID)) {
            return baseMapper.findAllMenuCode(functionType,userType , archive);
        } else {
            return baseMapper.findMenuCodeByRoleIds(roleIds, functionType,userType , archive);
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
    public List<String> findMenuCodeAll(String userType) {
        return baseMapper.findAllMenuCode(null, userType , BusinessCommonConstants.isArchive());
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
        publishMaskPermEvictAll("SysRoleMenuService.removeRefByRoleIds");
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
            LocalDateTime now = LocalDateTime.now();
            LoginUser loginUser = UserContext.getNonLoginUser();
            for (String menuId : menuIds) {
                SysRoleMenuEntity entity = new SysRoleMenuEntity();
                entity.setRoleId(newRoleId);
                entity.setMenuId(menuId);
                // 处理公共字段
                handleCommonField(entity, now, loginUser);
                addList.add(entity);
            }
            this.saveBatch(addList);
            publishMaskPermEvictAll("SysRoleMenuService.copyRoleMenu");
        }


    }

    @Override
    public RoleMenuDTO findRoleMenuTreeByRoleId(String roleId) {
        RoleMenuDTO roleMenuVO = new RoleMenuDTO();
        List<String> menuIds = getMenuIdByRoleId(roleId);

        List<SysMenuEntity> allList = sysMenuService.list(new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getDisabled, Boolean.FALSE).orderByDesc(SysMenuEntity::getType));

        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, roleId);
        List<SysRoleMenuEntity> sysRoleMenuEntityList = this.list(queryWrapper);

        List<RoleMenuTreeDTO> menuList = BeanMapperUtils.copyList(RoleMenuTreeDTO.class, allList);
        List<RoleMenuTreeDTO> treeList = menuList.stream().
                filter(item -> "0".equals(item.getParentId()))
                .sorted(Comparator.comparing(RoleMenuTreeDTO::getIndex))
                .peek(item -> {
                    SysRoleMenuEntity sysRoleMenuEntity = sysRoleMenuEntityList.stream().filter(roleMenu -> item.getMenuId().equals(roleMenu.getMenuId())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(sysRoleMenuEntity)) {
                        item.setDataScope(sysRoleMenuEntity.getDataScope());
                    } else {
                        item.setDataScope(1);
                    }
                    item.setParentName("");
                    item.setSelectState(false);
                    item.setChildrenList(getChildrenList(item, menuList, menuIds, sysRoleMenuEntityList));
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
     * @return java.util.List<com.erp.model.sys.vo.SysMenuVO>
     * @author yl
     * @date 2022-09-26 9:44
     */
    @Override
    public List<SysMenuVO> findLeftMenuByRoleIds(List<String> roleIds, String userType) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        // 1. 查询所有可用菜单（一次）
        List<SysMenuEntity> allMenuList = sysMenuService.lambdaQuery()
                .eq(SysMenuEntity::getDisabled, Boolean.FALSE)
                .eq(StringUtils.isNotBlank(userType), SysMenuEntity::getSystem, userType)
                .list();

        if (CollectionUtils.isEmpty(allMenuList)) {
            return Collections.emptyList();
        }
        // 2. Entity → VO
        List<SysMenuVO> menuVOList = BeanMapperUtils.copyList(SysMenuVO.class, allMenuList);

        // 3. 计算最终可用 menuId（Set，性能 & 正确性关键）
        Set<String> menuIdSet;
        if (roleIds.contains(CommonConstants.ADMIN_ROLE_ID)) {
            // 管理员：拥有全部菜单
            menuIdSet = allMenuList.stream()
                    .map(SysMenuEntity::getMenuId)
                    .collect(Collectors.toSet());
        } else {
            // 普通角色：角色菜单 + 所有父级菜单
            List<String> roleMenuIds = baseMapper.findMenuIdsByRoleIds(roleIds);
            menuIdSet = getSelfAndParentMenuIds(roleMenuIds, allMenuList);
        }

        if (CollectionUtils.isEmpty(menuIdSet)) {
            return Collections.emptyList();
        }

        // 4. 只在这里做一次 Set → List，兼容旧方法签名
        List<String> menuIdList = new ArrayList<>(menuIdSet);

        // 5. 构建左侧菜单树（从根节点开始）
        return menuVOList.stream()
                .filter(menu -> "0".equals(menu.getParentId()) && menuIdSet.contains(menu.getMenuId()))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .map(menu -> {
                    menu.setParentName("");
                    menu.setChildrenList(
                            getRoleChildrenLeftList(menu, menuVOList, menuIdList, SysConstant.FUNCTION_TYPE, SysConstant.BUTTON_TYPE)
                    );
                    return menu;
                }).collect(Collectors.toList());
    }

    @Override
    public List<SysMenuVO> findLeftMenuByRoleIds(List<String> roleIds, Integer type, String userType) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        // 1. 查询所有可用菜单（一次）
        List<SysMenuEntity> allMenuList = sysMenuService
                .lambdaQuery()
                .eq(SysMenuEntity::getDisabled, Boolean.FALSE)
                .eq(SysMenuEntity::getType,type)
                .eq(SysMenuEntity::getSystem, userType)
                .eq(BusinessCommonConstants.isArchive(), SysMenuEntity::getIsArchiveDisplay , Boolean.TRUE)
                .list();
        if (CollectionUtils.isEmpty(allMenuList)) {
            return Collections.emptyList();
        }
        // 2. Entity → VO
        List<SysMenuVO> menuVOList = BeanMapperUtils.copyList(SysMenuVO.class, allMenuList);
        // 3. 计算【最终可用 menuId 集合】（Set，关键优化）
        Set<String> menuIdSet;
        if (roleIds.contains(CommonConstants.ADMIN_ROLE_ID)) {
            // 管理员：直接拥有全部
            menuIdSet = allMenuList.stream()
                    .map(SysMenuEntity::getMenuId)
                    .collect(Collectors.toSet());
        } else {
            // 普通角色：角色菜单 + 所有父级
            List<String> roleMenuIds = baseMapper.findMenuIdsByRoleIdsAndType(roleIds, type, userType);
            menuIdSet = getSelfAndParentMenuIds(roleMenuIds, allMenuList);
        }

        if (CollectionUtils.isEmpty(menuIdSet)) {
            return Collections.emptyList();
        }

        // 4. 只在这里做一次 Set → List 的转换
        List<String> menuIdList = new ArrayList<>(menuIdSet);
        // 5. 构建左侧菜单树（只处理根节点）
        return menuVOList.stream()
                .filter(menu -> "0".equals(menu.getParentId()) && menuIdSet.contains(menu.getMenuId()))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .map(menu -> {
                    menu.setParentName("");
                    menu.setChildrenList(
                         getRoleChildrenLeftList(menu, menuVOList, menuIdList, SysConstant.FUNCTION_TYPE, SysConstant.BUTTON_TYPE)
                    );
                    return menu;
                }).collect(Collectors.toList());
    }

    /**
     * 获取自身及所有父级菜单ID
     *
     * @param menuIds  菜单ID列表
     * @param allMenus 所有菜单列表
     * @return 包含自身及所有父级菜单ID的集合
     */
    private static Set<String> getSelfAndParentMenuIds(
            List<String> menuIds,
            List<SysMenuEntity> allMenus
    ) {
        if (menuIds == null || menuIds.isEmpty() || allMenus == null || allMenus.isEmpty()) {
            return Collections.emptySet();
        }

        // 1. 构建 menuId -> SysMenuEntity 的 Map，方便 O(1) 查找
        Map<String, SysMenuEntity> menuMap = allMenus.stream()
                .filter(m -> m.getMenuId() != null)
                .collect(Collectors.toMap(
                        SysMenuEntity::getMenuId,
                        Function.identity(),
                        (a, b) -> a
                ));

        Set<String> result = new HashSet<>();

        // 2. 对每一个 menuId 向上递归 / 迭代查找父级
        for (String menuId : menuIds) {
            String currentId = menuId;

            while (currentId != null && !currentId.isEmpty()) {
                // 已处理过则跳出，避免死循环
                if (!result.add(currentId)) {
                    break;
                }

                SysMenuEntity currentMenu = menuMap.get(currentId);
                if (currentMenu == null) {
                    break;
                }

                currentId = currentMenu.getParentId();
            }
        }

        return result;
    }


    /**
     * 获取左侧菜单所有列表
     *
     * @return java.util.List<com.erp.model.sys.vo.SysMenuVO>
     * @Author Luo_WG
     * @Date 2022/11/1 14:26
     **/
    @Override
    public List<SysMenuVO> findLeftMenuAll(String userType) {
        List<SysMenuEntity> allList = sysMenuService
                .lambdaQuery()
                .eq(SysMenuEntity::getDisabled, Boolean.FALSE)
                .eq(StringUtils.isNotBlank(userType), SysMenuEntity::getSystem, userType)
                .list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<String> menuIds = allList.stream().map(s -> s.getMenuId()).collect(Collectors.toList());
        List<SysMenuVO> resultList = menuList.stream().
                filter(item -> "0".equals(item.getParentId()) && menuIds.contains(item.getMenuId()))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .map(item -> {
                    item.setParentName("");
                    item.setChildrenList(getRoleChildrenLeftList(item, menuList, menuIds, SysConstant.FUNCTION_TYPE, SysConstant.BUTTON_TYPE));
                    return item;
                }).collect(Collectors.toList());
        return resultList;
    }

    /**
     * 获取左侧菜单所有列表
     *
     * @return java.util.List<com.erp.model.sys.vo.SysMenuVO>
     * @Author Luo_WG
     * @Date 2022/11/1 14:26
     **/
    @Override
    public List<SysMenuVO> findLeftMenuAll(Integer type, String userType) {
        List<SysMenuEntity> allList = sysMenuService
                .lambdaQuery()
                .eq(SysMenuEntity::getDisabled, Boolean.FALSE)
                .eq(SysMenuEntity::getType,type)
                .eq(StringUtils.isNotBlank(userType), SysMenuEntity::getSystem, userType)
                .list();
        List<SysMenuVO> menuList = BeanMapperUtils.copyList(SysMenuVO.class, allList);
        List<String> menuIds = allList.stream().map(s -> s.getMenuId()).collect(Collectors.toList());
        List<SysMenuVO> resultList = menuList.stream().
                filter(item -> "0".equals(item.getParentId()) && menuIds.contains(item.getMenuId()))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .map(item -> {
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
        LocalDateTime now = LocalDateTime.now();
        LoginUser loginUser = UserContext.getNonLoginUser();
        // 处理公共字段
        handleCommonField(entity, now, loginUser);
        try {
            return this.save(entity);
        } finally {
            publishMaskPermEvictAll("SysRoleMenuService.saveRoleMenu");
        }

    }

    private static void handleCommonField(SysRoleMenuEntity entity, LocalDateTime now, LoginUser loginUser) {
        entity.setUpdateTime(now);
        entity.setUpdateUserId(loginUser.getUid());
        entity.setUpdateUserName(loginUser.getUserName());
        entity.setCreateTime(now);
        entity.setCreateUserId(loginUser.getUid());
        entity.setCreateUserName(loginUser.getUserName());
    }

    /**
     * 安全调用脱敏权限失效广播；publisher 未注入或 Redis 异常都不影响主业务事务
     *
     * <p>{@code sys_role_menu} 改动同时会让"用户的菜单权限码集合"和"数据权限上下文里 permissionsList"
     * 失效，因此 mask 与 dataPerm 两个 publisher 都打 publishAll。两者独立 channel，
     * 任一 publisher 故障互不影响。</p>
     */
    private void publishMaskPermEvictAll(String source) {
        if (maskPermissionEvictPublisher != null) {
            try {
                maskPermissionEvictPublisher.publishAll(source);
            } catch (Throwable ignore) {
                // publisher 内部已经容错，这里再吞一次保证 service 主流程不受影响
            }
        }
        if (dataPermissionContextEvictPublisher != null) {
            try {
                dataPermissionContextEvictPublisher.publishAll(source);
            } catch (Throwable ignore) {
                // 同上，dataPerm 失效广播失败不阻塞主业务事务
            }
        }
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
     * @return java.util.List<com.erp.model.sys.vo.SysMenuVO>
     * @author yl
     * @date 2022-09-26 9:54
     */
    private List<SysMenuVO> getRoleChildrenLeftList(SysMenuVO item, List<SysMenuVO> treeList, List<String> menuIds, Integer functionType, Integer buttonType) {
        List<SysMenuVO> collectList = treeList.stream().filter(menu -> (item.getMenuId().equals(menu.getParentId()) && menuIds.contains(menu.getMenuId()) && !menu.getType().equals(functionType) && !menu.getType().equals(buttonType)))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .map(m -> {
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
        List<SysMenuVO> collectList = treeList.stream().filter(menu -> (item.getMenuId().equals(menu.getParentId()) && menuIds.contains(menu.getMenuId())))
                .sorted(Comparator.comparing(SysMenuVO::getIndex))
                .map(m -> {
                    m.setParentName(item.getMenuName());
                    m.setChildrenList(getRoleChildrenList(m, treeList, menuIds));
                    return m;
                }).collect(Collectors.toList());

        return CollectionUtils.isEmpty(collectList) ? null : collectList;
    }

    private List<RoleMenuTreeDTO> getChildrenList(RoleMenuTreeDTO item, List<RoleMenuTreeDTO> menuList, List<String> menuIds, List<SysRoleMenuEntity> sysRoleMenuEntityList) {
        List<RoleMenuTreeDTO> collectList = menuList.stream().filter(menu -> item.getMenuId().equals(menu.getParentId()))
                .sorted(Comparator.comparing(RoleMenuTreeDTO::getIndex))
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