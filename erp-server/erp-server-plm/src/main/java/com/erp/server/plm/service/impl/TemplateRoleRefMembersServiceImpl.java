package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.entity.RoleRefMemberEntity;
import com.erp.model.plm.entity.TemplateRoleRefMembersEntity;

import com.erp.server.plm.mapper.TemplateRoleRefMembersMapper;
import com.erp.server.plm.service.RoleRefMemberService;
import com.erp.server.plm.service.TemplateRoleRefMembersService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Service
public class TemplateRoleRefMembersServiceImpl extends ServiceImpl<TemplateRoleRefMembersMapper, TemplateRoleRefMembersEntity>
        implements TemplateRoleRefMembersService {

    @Autowired
    private RoleRefMemberService roleRefMemberService;

    @Override
    public void saveRoleRefMembers(String templateId, String productId) {
        List<RoleRefMemberEntity> list = roleRefMemberService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateRoleRefMembersEntity> saveList = new ArrayList<>();
            for (RoleRefMemberEntity item : list) {
                TemplateRoleRefMembersEntity entity = new TemplateRoleRefMembersEntity();
                BeanMapper.copy(item, entity);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }
}




