package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.ProductRoleDTO;
import com.erp.model.plm.dto.ProductRoleMemberDTO;
import com.erp.model.plm.dto.ProjectRoleDTO;
import com.erp.model.plm.dto.RoleRefMemberDTO;
import com.erp.model.plm.entity.ProjectRoleEntity;
import com.erp.server.plm.mapper.ProjectRoleMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.ProjectRoleService;
import com.erp.server.plm.service.RoleRefMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname RoleServiceImpl
 * @Description TODO
 * @Date 2022-10-09 19:48
 * @Created by yl
 */
@Service
public class ProjectRoleServiceImpl extends ServiceImpl<ProjectRoleMapper, ProjectRoleEntity> implements ProjectRoleService {

    @Autowired
    private RoleRefMemberService roleRefMemberService;

    @Autowired
    private ProjectMembersService projectMembersService;


    /**
     * 保存项目角色名
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-26 16:52
     */
    @Override
    public Boolean saveRole(ProjectRoleDTO dto) {
        String name = dto.getName();
        String productId = dto.getProductId();
        checkRoleName(name, productId);
        ProjectRoleEntity entity = new ProjectRoleEntity();
        entity.setName(name);
        entity.setProductId(dto.getProductId());
        entity.setProjectId(dto.getProjectId());
        return this.save(entity);
    }

    /**
     * 根据产品id 获取角色列表
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.entity.ProjectRoleEntity>
     * @author yl
     * @date 2022-09-26 17:29
     */
    @Override
    public List<ProjectRoleEntity> listByProductId(String productId) {
        LambdaQueryWrapper<ProjectRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectRoleEntity::getProductId, productId);
        return this.list(queryWrapper);
    }


    /**
     * 人员分类
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.ProductRoleDTO>
     * @author yl
     * @date 2022-10-10 10:35
     */
    @Override
    public List<ProductRoleDTO> roleSortList() {
        List<ProductRoleDTO> resultList = new ArrayList<>();
        List<ProjectRoleEntity> allRoleList = this.list();
        //以角色名分组
        Map<String, List<ProjectRoleEntity>> roleNameMap = allRoleList.parallelStream().collect(Collectors.groupingBy(ProjectRoleEntity::getName));
        for (Map.Entry<String, List<ProjectRoleEntity>> item : roleNameMap.entrySet()) {
            ProductRoleDTO roleDTO = new ProductRoleDTO();
            //角色名
            String roleName = item.getKey();
            //对应的是集合
            List<ProjectRoleEntity> roleList = item.getValue();
            //获取到对应的角色id
            List<String> roleIds = roleList.stream().map(ProjectRoleEntity::getId).collect(Collectors.toList());
            roleDTO.setRoleName(roleName);
            //根据角色id 集合 获取到对应的人
            List<RoleRefMemberDTO> roleRefList = roleRefMemberService.getByRoleIds(roleIds);
            roleDTO.setRoleMemberCount(roleRefList.size());
            List<String> memberList=roleRefList.stream().map(RoleRefMemberDTO::getMembersId).collect(Collectors.toList());
            //根据成员id 获取到参与了多少项目
            List<ProductRoleMemberDTO> productMemberList=projectMembersService.getProductCountByMemberList(memberList);
            roleDTO.setProductRoleMembers(productMemberList);
            resultList.add(roleDTO);
        }
        return resultList;
    }


    /**
     * 检查角色名是否重复
     *
     * @param name
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-26 17:00
     */
    private void checkRoleName(String name, String productId) {
        LambdaQueryWrapper<ProjectRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectRoleEntity::getName, name);
        queryWrapper.eq(ProjectRoleEntity::getProductId, productId);
        queryWrapper.last("LIMIT 1");
        int count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95016);
        }
    }
}
