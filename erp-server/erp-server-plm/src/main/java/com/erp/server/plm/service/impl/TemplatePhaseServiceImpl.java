package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.ProjectPhaseEntity;
import com.erp.model.plm.entity.TemplatePhaseEntity;

import com.erp.server.plm.mapper.TemplatePhaseMapper;
import com.erp.server.plm.service.ProjectPhaseService;
import com.erp.server.plm.service.TemplatePhaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Service
public class TemplatePhaseServiceImpl extends ServiceImpl<TemplatePhaseMapper, TemplatePhaseEntity>
        implements TemplatePhaseService {


    @Autowired
    private ProjectPhaseService projectPhaseService;

    /**
     * 保存模板阶段
     *
     * @return void
     * @author yl
     * @date 2022-10-27 15:25
     */
    @Override
    public void saveTemplatePhase(String templateId, String productId) {
        List<ProjectPhaseEntity> list = projectPhaseService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplatePhaseEntity> saveList = new ArrayList<>();
            for (ProjectPhaseEntity item : list) {
                TemplatePhaseEntity entity = new TemplatePhaseEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }

    @Override
    public List<TemplateCopySourceDTO> copyTemplatePhase(String flagId, String productId, String projectId) {
        return null;
    }
}




