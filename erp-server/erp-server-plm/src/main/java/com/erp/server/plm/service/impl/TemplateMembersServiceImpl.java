package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.TemplateMembersEntity;

import com.erp.server.plm.mapper.TemplateMembersMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.TemplateMembersService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Service
public class TemplateMembersServiceImpl extends ServiceImpl<TemplateMembersMapper, TemplateMembersEntity>
        implements TemplateMembersService {

    @Autowired
    private ProjectMembersService projectMembersService;

    /**
     * 保存 模板成员
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 15:12
     */
    @Override
    public void saveMember(String templateId, String productId) {
        List<ProjectMembersEntity> list = projectMembersService.getListByProductId(productId);
        List<TemplateMembersEntity> saveList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity item : list) {
                TemplateMembersEntity entity = new TemplateMembersEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }
}




