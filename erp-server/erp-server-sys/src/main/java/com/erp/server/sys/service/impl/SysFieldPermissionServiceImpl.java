package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.SysFieldPermissionDTO;
import com.erp.model.sys.entity.CfgMaskFieldEntity;
import com.erp.model.sys.entity.SysRoleMenuEntity;
import com.erp.server.sys.mapper.CfgMaskFieldMapper;
import com.erp.server.sys.mapper.SysFieldPermissionMapper;
import com.erp.server.sys.mapper.SysRoleMenuMapper;
import com.erp.server.sys.service.SysFieldPermissionService;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段权限管理服务实现
 *
 * <p>不引入新表：完全基于现有 {@code sys_menu(type=5)} + {@code sys_role_menu} + {@code cfg_mask_field}。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Service
public class SysFieldPermissionServiceImpl implements SysFieldPermissionService {

    @Resource
    private SysFieldPermissionMapper sysFieldPermissionMapper;

    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Resource
    private CfgMaskFieldMapper cfgMaskFieldMapper;

    @Override
    public List<SysFieldPermissionDTO.ListVO> list(SysFieldPermissionDTO.ListSearchDTO dto) {
        List<SysFieldPermissionDTO.ListVO> rows =
                sysFieldPermissionMapper.listFieldPermissionForRole(dto.getRoleId(), dto.getSystem());
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }
        Set<String> codes = rows.stream()
                .map(SysFieldPermissionDTO.ListVO::getMenuCode)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toSet());
        Map<String, List<String>> codeToFields = codes.isEmpty()
                ? Collections.emptyMap()
                : cfgMaskFieldMapper
                    .selectList(new LambdaQueryWrapper<CfgMaskFieldEntity>()
                            .in(CfgMaskFieldEntity::getPermissionCode, codes))
                    .stream()
                    .collect(Collectors.groupingBy(
                            CfgMaskFieldEntity::getPermissionCode,
                            Collectors.mapping(
                                    e -> e.getClassPath() + "#" + e.getFieldName(),
                                    Collectors.toList())));
        rows.forEach(r -> r.setBoundFields(
                codeToFields.getOrDefault(r.getMenuCode(), Collections.emptyList())));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean save(SysFieldPermissionDTO.SaveDTO dto) {
        // ① 圈定本维度全集（所有 type=5 字段权限菜单 id）
        List<String> fieldPermMenuIds = sysFieldPermissionMapper.findFieldPermissionMenuIds();
        if (CollectionUtils.isEmpty(fieldPermMenuIds)) {
            // 系统还没有任何字段权限菜单，无需操作
            return Boolean.TRUE;
        }
        Set<String> fieldPermMenuIdSet = new HashSet<>(fieldPermMenuIds);

        // ② 校验入参 menuId 必须全部属于"字段权限菜单"维度，避免误删功能权限
        List<String> visible = dto.getVisibleMenuIds() == null
                ? Collections.emptyList() : dto.getVisibleMenuIds();
        for (String menuId : visible) {
            if (!fieldPermMenuIdSet.contains(menuId)) {
                throw new ServiceException("非字段权限菜单不可在此接口维护：menuId=" + menuId);
            }
        }

        // ③ 删除该角色在"字段权限"维度的旧关联（逻辑删除，由 BaseMapper.delete 触发 @TableLogic）
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenuEntity>()
                .eq(SysRoleMenuEntity::getRoleId, dto.getRoleId())
                .in(SysRoleMenuEntity::getMenuId, fieldPermMenuIds));

        // ④ 写入新关联
        if (!visible.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            LoginUser loginUser = UserContext.getNonLoginUser();
            List<SysRoleMenuEntity> batch = new LinkedList<>();
            for (String menuId : visible) {
                SysRoleMenuEntity entity = new SysRoleMenuEntity();
                entity.setRoleId(dto.getRoleId());
                entity.setMenuId(menuId);
                entity.setUpdateTime(now);
                entity.setUpdateUserId(loginUser.getUid());
                entity.setUpdateUserName(loginUser.getUserName());
                entity.setCreateTime(now);
                entity.setCreateUserId(loginUser.getUid());
                entity.setCreateUserName(loginUser.getUserName());
                batch.add(entity);
            }
            for (SysRoleMenuEntity entity : batch) {
                sysRoleMenuMapper.insert(entity);
            }
        }
        return Boolean.TRUE;
    }
}
