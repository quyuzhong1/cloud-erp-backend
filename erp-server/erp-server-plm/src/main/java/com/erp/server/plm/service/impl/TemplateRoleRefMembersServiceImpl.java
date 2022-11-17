package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CopySourceDTO;
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
                entity.setTemplateId(templateId);
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
    public void copyTemplateRoleRefMembers(String templateId, String productId, String projectId,List<CopySourceDTO> copyRoleSourceList) {
        List<TemplateRoleRefMembersEntity> list = getByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<RoleRefMemberEntity> copyList = new ArrayList<>();
            for (TemplateRoleRefMembersEntity item : list) {
                CopySourceDTO source=  copyRoleSourceList.stream().filter(c->c.getDataId()
                        .equals(item.getRoleId())).findFirst().orElse(null);
                RoleRefMemberEntity entity = new RoleRefMemberEntity();
                BeanMapper.copy(item, entity);
                entity.setProductId(productId);
                if(!Objects.isNull(source)){
                    entity.setRoleId(source.getNewCreateId());
                }
                entity.setId(IdWorker.getIdStr());
                copyList.add(entity);
            }
            roleRefMemberService.saveBatch(copyList);
        }
    }

    @Override
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRoleRefMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleRefMembersEntity::getTemplateId,templateId);
        this.remove(queryWrapper);
    }

    @Override
    public List<String> getUserRole(String userId, String templateId) {
        LambdaQueryWrapper<TemplateRoleRefMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(TemplateRoleRefMembersEntity::getRoleId);
        queryWrapper.eq(TemplateRoleRefMembersEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateRoleRefMembersEntity::getMembersId, userId);
        return this.listObjs(queryWrapper,Object::toString);
    }

    @Override
    public TemplateRoleRefMembersEntity getByIdAndTemplateId(String id, String templateId) {
        LambdaQueryWrapper<TemplateRoleRefMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleRefMembersEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateRoleRefMembersEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public Boolean updateByTemplateId(TemplateRoleRefMembersEntity templateRoleRefMembersEntity) {
        LambdaUpdateWrapper<TemplateRoleRefMembersEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TemplateRoleRefMembersEntity::getTemplateId, templateRoleRefMembersEntity.getTemplateId());
        updateWrapper.eq(TemplateRoleRefMembersEntity::getId, templateRoleRefMembersEntity.getId());
        updateWrapper.set(TemplateRoleRefMembersEntity::getMembersId,templateRoleRefMembersEntity.getMembersId());
        updateWrapper.set(TemplateRoleRefMembersEntity::getRoleId,templateRoleRefMembersEntity.getRoleId());
        updateWrapper.set(TemplateRoleRefMembersEntity::getUpdateUserId,templateRoleRefMembersEntity.getUpdateUserId());
        updateWrapper.set(TemplateRoleRefMembersEntity::getUpdateUserName,templateRoleRefMembersEntity.getUpdateUserName());
        return this.update(updateWrapper);
    }

    @Override
    public List<TemplateRoleRefMembersEntity> getByRoleIdAndTemplateId(String roleId, String templateId) {
        LambdaQueryWrapper<TemplateRoleRefMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleRefMembersEntity::getRoleId,roleId);
        queryWrapper.eq(TemplateRoleRefMembersEntity::getTemplateId,templateId);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean removeByIdAndTemplateId(String roleRefMembersId, String templateId) {
        LambdaQueryWrapper<TemplateRoleRefMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleRefMembersEntity::getTemplateId,templateId);
        queryWrapper.eq(TemplateRoleRefMembersEntity::getId,roleRefMembersId);
        return this.remove(queryWrapper);
    }


    public List<TemplateRoleRefMembersEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRoleRefMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleRefMembersEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }
}




