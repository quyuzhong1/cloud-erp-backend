package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
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
import java.util.Objects;


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

    /**
     * 复制角色成员关系表
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return void
     * @author yl
     * @date 2022-10-28 11:33
     */
    @Override
    public void copyTemplateRoleRefMembers(String templateId, String productId, String projectId,List<TemplateCopySourceDTO> copyRoleSourceList) {
        List<TemplateRoleRefMembersEntity> list = getByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<RoleRefMemberEntity> copyList = new ArrayList<>();
            for (TemplateRoleRefMembersEntity item : list) {
                TemplateCopySourceDTO source=  copyRoleSourceList.stream().filter(c->c.getTemplateDataId()
                        .equals(item.getRoleId())).findFirst().orElse(null);
                if(Objects.isNull(source)){
                    RoleRefMemberEntity entity = new RoleRefMemberEntity();
                    BeanMapper.copy(item, entity);
                    entity.setProductId(productId);
                    entity.setRoleId(source.getNewCreateId());
                    entity.setId(IdWorker.getIdStr());
                    copyList.add(entity);
                }
            }
            roleRefMemberService.saveBatch(copyList);
        }
    }


    public List<TemplateRoleRefMembersEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRoleRefMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleRefMembersEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }
}




