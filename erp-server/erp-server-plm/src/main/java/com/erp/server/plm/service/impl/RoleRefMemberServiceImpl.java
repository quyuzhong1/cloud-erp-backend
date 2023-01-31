package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.RoleRefMemberDTO;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.RoleRefMemberEntity;
import com.erp.server.plm.mapper.RoleRefMemberMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.RoleRefMemberService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 */
@Service
public class RoleRefMemberServiceImpl extends ServiceImpl<RoleRefMemberMapper, RoleRefMemberEntity>
        implements RoleRefMemberService {


    @Autowired
    private ProjectMembersService projectMembersService;

    /**
     * 根据角色id 获取到对应的成员
     *
     * @param roleIds
     * @return java.util.List<com.erp.model.plm.dto.RoleRefMemberDTO>
     * @author yl
     * @date 2022-10-10 11:23
     */
    @Override
    public List<RoleRefMemberDTO> getByRoleIds(List<String> roleIds) {
        LambdaQueryWrapper<RoleRefMemberEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(RoleRefMemberEntity::getRoleId, roleIds);
        List<RoleRefMemberEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, RoleRefMemberDTO.class);
    }

    /**
     * 添加 关系表
     *
     * @param membersTableIds 成员表id
     * @param roleId
     * @return void
     * @author yl
     * @date 2022-10-10 15:01
     */
    @Override
    public void saveRef(List<String> membersTableIds, String roleId, String productId) {


        List<RoleRefMemberEntity> addList = new ArrayList<>();
        for (String membersTableId : membersTableIds) {
            RoleRefMemberEntity ref = new RoleRefMemberEntity();
            ref.setRoleId(roleId);
            ref.setProductId(productId);
            ref.setMembersId(membersTableId);
            addList.add(ref);
        }
        this.saveBatch(addList);

    }

    private List<RoleRefMemberEntity> getExistList(String roleId) {
        LambdaQueryWrapper<RoleRefMemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleRefMemberEntity::getRoleId, roleId);
        return this.list(queryWrapper);
    }

    /**
     * 检查角色id 与成员id 是否存在
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-11 11:02
     */
    @Override
    public void checkRoleMember(String id, String roleId, List<String> memberIds, String productId) {

        List<RoleRefMemberEntity> existList = getExistList(roleId);
        List<String> existMemberTableIds = existList.stream().map(RoleRefMemberEntity::getMembersId).collect(Collectors.toList());
        List<ProjectMembersEntity> projectMembersList = projectMembersService.getByMemberIds(memberIds, productId);
        List<String> memberTableIds = projectMembersList.stream().map(ProjectMembersEntity::getId).collect(Collectors.toList());
        List<String> intersection = existMemberTableIds.stream().filter(item -> memberTableIds.contains(item)).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(intersection)) {
            throw new ServiceException(ApiError.ERROR_95021);
        }
    }

    @Override
    public List<RoleRefMemberEntity> getByProductId(String productId) {
        LambdaQueryWrapper<RoleRefMemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleRefMemberEntity::getProductId, productId);
        return this.list(queryWrapper);
    }


    @Override
    public List<String> getUserRole(String userId, String productId) {
        LambdaQueryWrapper<RoleRefMemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(RoleRefMemberEntity::getRoleId);
        queryWrapper.eq(RoleRefMemberEntity::getProductId, productId);
        queryWrapper.eq(RoleRefMemberEntity::getMembersId, userId);
        return this.listObjs(queryWrapper, Object::toString);
    }

    @Override
    public List<RoleRefMemberEntity> listByMembersIds(List<String> membersIds) {
        LambdaQueryWrapper<RoleRefMemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RoleRefMemberEntity::getMembersId,membersIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<RoleRefMemberEntity> getByRoleIdsAndProductId(List<String> roleIdList, String productId) {
        LambdaQueryWrapper<RoleRefMemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RoleRefMemberEntity::getRoleId,roleIdList);
        queryWrapper.eq(RoleRefMemberEntity::getProductId,productId);
        return this.list(queryWrapper);
    }
}




