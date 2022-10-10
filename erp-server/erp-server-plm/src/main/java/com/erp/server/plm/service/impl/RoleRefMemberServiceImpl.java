package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.RoleRefMemberDTO;
import com.erp.model.plm.entity.RoleRefMemberEntity;
import com.erp.server.plm.mapper.RoleRefMemberMapper;
import com.erp.server.plm.service.RoleRefMemberService;
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
        return BeanMapper.copyList(list,RoleRefMemberDTO.class);
    }

    /**
     * 添加或者修改 关系表
     * @author yl
     * @date 2022-10-10 15:01
     * @param roleRefMemberId
     * @param memberId
     * @param roleId
     * @return void
     */
    @Override
    public void saveOrUpdateRef(String roleRefMemberId, String memberId, String roleId) {
        
    }
}




