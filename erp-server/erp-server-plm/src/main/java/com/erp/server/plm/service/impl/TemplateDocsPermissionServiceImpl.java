package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.DocsPermissionEntity;
import com.erp.model.plm.entity.TemplateDocsPermissionEntity;
import com.erp.server.plm.mapper.TemplateDocsPermissionMapper;
import com.erp.server.plm.service.DocsPermissionService;
import com.erp.server.plm.service.TemplateDocsPermissionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Classname TemplateDocsPermissionServiceiMPL
 * @Description TODO
 * @Date 2022-10-31 9:46
 * @Created by yl
 */
@Service
public class TemplateDocsPermissionServiceImpl extends ServiceImpl<TemplateDocsPermissionMapper, TemplateDocsPermissionEntity>
        implements TemplateDocsPermissionService {


    @Autowired
    private DocsPermissionService docsPermissionService;

    /**
     * 保存模板的 文档权限
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-31 9:51
     */
    @Override
    public void saveTemplateDocsPermission(String templateId, String productId, List<CopySourceDTO> sourceDeliveryList, List<CopySourceDTO> taskSourceList, List<CopySourceDTO> sourceRoleList) {
        List<DocsPermissionEntity> list = docsPermissionService.getDocsPermissionByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateDocsPermissionEntity> saveList = new ArrayList<>();
            for (DocsPermissionEntity item : list) {
                TemplateDocsPermissionEntity entity = new TemplateDocsPermissionEntity();
                entity.setTemplateId(templateId);
                String deliveryDocsId = sourceDeliveryList.stream().filter(d -> d.getDataId().equals(item.getDeliveryDocsId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getNewCreateId())).orElse("");
                entity.setDeliveryDocsId(deliveryDocsId);
                String taskId = taskSourceList.stream().filter(t -> t.getDataId().equals(item.getTaskId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getNewCreateId())).orElse("");
                entity.setTaskId(taskId);
                String queryRoleId = item.getQueryRoleId();
                if(StringUtils.isNotBlank(queryRoleId)){
                    String roleId=sourceRoleList.stream().filter(r -> r.getDataId().equals(item.getQueryRoleId())).
                            findFirst().flatMap(obj -> Optional.ofNullable(obj.getNewCreateId())).orElse("");
                    item.setQueryRoleId(roleId);
                }else{
                    item.setQueryRoleId("");
                }
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }


    /**
     * 保存文档权限
     *
     * @param templateId
     * @param productId
     * @param taskSourceList
     * @param deliveryDocsSourceList
     * @return void
     * @author yl
     * @date 2022-10-31 10:18
     */
    @Override
    public void copyTemplateDeliveryDocs(String templateId, String productId, List<CopySourceDTO> taskSourceList, List<CopySourceDTO> deliveryDocsSourceList) {
        List<TemplateDocsPermissionEntity> list = this.getByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<DocsPermissionEntity> saveList = new ArrayList<>();
            for (TemplateDocsPermissionEntity item : list) {
                CopySourceDTO source = deliveryDocsSourceList.stream().filter(d -> d.getDataId().
                        equals(item.getDeliveryDocsId())).findFirst().orElse(null);
                if (!Objects.isNull(source)) {
                    DocsPermissionEntity entity = new DocsPermissionEntity();
                    BeanMapper.copy(item, entity);
                    entity.setProductId(productId);
                    entity.setDeliveryDocsId(source.getNewCreateId());
                    entity.setId(IdWorker.getIdStr());
                    saveList.add(entity);
                }
            }
            docsPermissionService.saveBatch(saveList);
        }

    }

    @Override
    public List<String> getDocsIdsByRoleIds(List<String> userRoleIds, String templateId) {
        LambdaQueryWrapper<TemplateDocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(TemplateDocsPermissionEntity::getDeliveryDocsId);
        queryWrapper.eq(TemplateDocsPermissionEntity::getTemplateId, templateId);
        queryWrapper.in(TemplateDocsPermissionEntity::getQueryRoleId, userRoleIds);
        return this.listObjs(queryWrapper, Object::toString);
    }

    @Override
    public List<String> getAllDeliveryDocsIds(String templateId) {
        LambdaQueryWrapper<TemplateDocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(TemplateDocsPermissionEntity::getDeliveryDocsId);
        queryWrapper.eq(TemplateDocsPermissionEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateDocsPermissionEntity::getQueryRoleId, "");
        return this.listObjs(queryWrapper, Object::toString);
    }

    /**
     * 方法说明
     *
     * @param
     * @return java.util.List<com.erp.model.plm.entity.TemplateDocsPermissionEntity>
     * @author yl
     * @date 2022-10-31 10:21
     */
    public List<TemplateDocsPermissionEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateDocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDocsPermissionEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }
}
