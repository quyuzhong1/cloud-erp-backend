package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.RoleRefMemberDTO;
import com.erp.model.plm.entity.RoleRefMemberEntity;
import com.erp.server.plm.mapper.RoleRefMemberMapper;
import com.erp.server.plm.service.RoleRefMemberService;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 *
 */
@Service
public class RoleRefMemberServiceImpl extends ServiceImpl<RoleRefMemberMapper, RoleRefMemberEntity>
        implements RoleRefMemberService {


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
     * 添加或者修改 关系表
     *
     * @param roleRefMemberId
     * @param memberId
     * @param roleId
     * @return void
     * @author yl
     * @date 2022-10-10 15:01
     */
    @Override
    public void saveOrUpdateRef(String roleRefMemberId, String memberId, String roleId, String productId) {
        //表示 是修改
        if (StringUtils.isNotBlank(roleRefMemberId)) {
            //如果修改  先查出来原来用没有
            LambdaUpdateWrapper<RoleRefMemberEntity> queryWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.eq(RoleRefMemberEntity::getId, roleRefMemberId);
            queryWrapper.set(RoleRefMemberEntity::getRoleId, roleId);
            queryWrapper.set(RoleRefMemberEntity::getMembersId, memberId);
            queryWrapper.set(RoleRefMemberEntity::getProductId, productId);
            this.update(queryWrapper);
        } else {
            RoleRefMemberEntity ref = new RoleRefMemberEntity();
            ref.setMembersId(memberId);
            ref.setRoleId(roleId);
            ref.setProductId(productId);
            this.save(ref);
        }

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
    public void checkRoleMember(String id, String roleId, String memberId) {
        LambdaQueryWrapper<RoleRefMemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleRefMemberEntity::getRoleId, roleId);
        queryWrapper.eq(RoleRefMemberEntity::getMembersId, memberId);
        queryWrapper.ne(RoleRefMemberEntity::getId, id);
        RoleRefMemberEntity entity = this.getOne(queryWrapper);
        if (entity != null) {
            throw new ServiceException(ApiError.ERROR_95021);
        }
    }

    @Override
    public List<RoleRefMemberEntity> getByProductId(String productId) {
        LambdaQueryWrapper<RoleRefMemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleRefMemberEntity::getProductId, productId);
        return this.list(queryWrapper);
    }
}




