package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.TaskDocsNameEntity;
import com.erp.model.plm.entity.TemplateTaskDocsNameEntity;

import com.erp.server.plm.mapper.TemplateTaskDocsNameMapper;
import com.erp.server.plm.service.TaskDocsNameService;
import com.erp.server.plm.service.TemplateTaskDocsNameService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 *
 */
@Service
public class TemplateTaskDocsNameServiceImpl extends ServiceImpl<TemplateTaskDocsNameMapper, TemplateTaskDocsNameEntity>
        implements TemplateTaskDocsNameService {

    @Autowired
    private TaskDocsNameService taskDocsNameService;

    /**
     * 保存模板 文档名
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 15:51
     */
    @Override
    public void saveTemplateDocsName(String templateId, String productId) {
        List<TaskDocsNameEntity> list = taskDocsNameService.getDocsNameByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateTaskDocsNameEntity> saveList = new ArrayList<>();
            for (TaskDocsNameEntity item : list) {
                TemplateTaskDocsNameEntity entity = new TemplateTaskDocsNameEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }


    /**
     * 复制文档名
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return java.util.List<com.erp.model.plm.dto.TemplateCopySourceDTO>
     * @author yl
     * @date 2022-10-28 14:06
     */
    @Override
    public List<CopySourceDTO> copyTemplateDocsName(String templateId, String productId, String projectId) {
        List<TemplateTaskDocsNameEntity> list = getByTemplateId(templateId);
        List<TaskDocsNameEntity> existDocsNameList = taskDocsNameService.getDocsNameByProductId(productId);
        List<CopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<TaskDocsNameEntity> copyList = new ArrayList<>();
            for (TemplateTaskDocsNameEntity item : list) {
                TaskDocsNameEntity exist = existDocsNameList.stream().
                        filter(e -> e.getName().equals(item.getName())).findFirst().orElse(null);
                CopySourceDTO source = new CopySourceDTO();
                //可能数据库存在 要排除数据库里面的数据
                if (Objects.isNull(exist)) {
                    TaskDocsNameEntity entity = new TaskDocsNameEntity();
                    BeanMapper.copy(item, entity);
                    entity.setProductId(productId);
                    String id = IdWorker.getIdStr();
                    entity.setId(id);
                    copyList.add(entity);
                    source.setNewCreateId(id);
                } else {
                    source.setNewCreateId(exist.getId());
                }
                source.setDataId(item.getId());
                sourceList.add(source);
            }
            if (CollectionUtils.isNotEmpty(copyList)) {
                taskDocsNameService.saveBatch(copyList);
            }

        }

        return sourceList;
    }


    public List<TemplateTaskDocsNameEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskDocsNameEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskDocsNameEntity::getTemplateId, templateId);
        return list(queryWrapper);
    }
}




