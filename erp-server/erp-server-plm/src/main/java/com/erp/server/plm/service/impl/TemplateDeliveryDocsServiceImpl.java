package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;

import com.erp.server.plm.mapper.TemplateDeliveryDocsMapper;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TemplateDeliveryDocsService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Service
public class TemplateDeliveryDocsServiceImpl extends ServiceImpl<TemplateDeliveryDocsMapper, TemplateDeliveryDocsEntity>
        implements TemplateDeliveryDocsService {


    @Autowired
    private TaskDeliveryService taskDeliveryService;

    /**
     * 交付文档
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 16:07
     */
    @Override
    public void saveTemplateDeliveryDocs(String templateId, String productId) {
        List<TaskDeliveryDocsEntity> list = taskDeliveryService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateDeliveryDocsEntity> saveList = new ArrayList<>();
            for (TaskDeliveryDocsEntity item : list) {
                TemplateDeliveryDocsEntity entity = new TemplateDeliveryDocsEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }
}




