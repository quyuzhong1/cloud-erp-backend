package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.TemplatePreTaskEntity;
import com.erp.server.plm.mapper.TemplatePreTaskMapper;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.TemplatePreTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class TemplatePreTaskServiceImpl extends ServiceImpl<TemplatePreTaskMapper, TemplatePreTaskEntity>
        implements TemplatePreTaskService {


    @Autowired
    private PreTaskService preTaskService;

    @Override
    public void saveTemplatePreTask(String templateId, String productId) {
        List<PreTaskEntity> list = preTaskService.getPreTaskByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplatePreTaskEntity> saveList = new ArrayList<>();
            for (PreTaskEntity item : list) {
                TemplatePreTaskEntity entity = new TemplatePreTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }
}




