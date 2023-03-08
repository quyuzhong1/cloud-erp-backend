package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.BasicTemplateIdDTO;
import com.erp.model.plm.dto.BatchTemplatePhaseDTO;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TemplatePhaseDTO;
import com.erp.model.plm.entity.ProjectPhaseEntity;
import com.erp.model.plm.entity.TemplatePhaseEntity;
import com.erp.model.plm.entity.TemplateTaskEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.TemplatePhaseMapper;
import com.erp.server.plm.service.ProjectPhaseService;
import com.erp.server.plm.service.TemplatePhaseService;
import com.erp.server.plm.service.TemplateTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 *
 */
@Service
public class TemplatePhaseServiceImpl extends ServiceImpl<TemplatePhaseMapper, TemplatePhaseEntity>
        implements TemplatePhaseService {


    @Autowired
    private ProjectPhaseService projectPhaseService;

    @Autowired
    private TemplateTaskService templateTaskService;

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
    public List<CopySourceDTO> copyTemplatePhase(String templateId, String productId, String projectId) {
        List<TemplatePhaseEntity> list = getByTemplateId(templateId);
        List<ProjectPhaseEntity> existList = projectPhaseService.getByProductId(productId);
        List<CopySourceDTO> sourceList = new ArrayList<>();
        Integer seq = 0;
        if (CollectionUtils.isNotEmpty(existList)) {
            seq = existList.stream().max(Comparator.comparingInt(ProjectPhaseEntity::getSeq)).map(ProjectPhaseEntity::getSeq).get();
        }
        if (CollectionUtils.isNotEmpty(list)) {
            List<ProjectPhaseEntity> copyList = new ArrayList<>();
            for (TemplatePhaseEntity item : list) {
                ProjectPhaseEntity exist = existList.stream().filter(e -> e.getName().equals(item.getName())).
                        findFirst().orElse(null);
                CopySourceDTO source = new CopySourceDTO();

                if (Objects.isNull(exist)) {
                    ProjectPhaseEntity entity = new ProjectPhaseEntity();
                    BeanMapper.copy(item, entity);
                    entity.setProductId(productId);
                    String id = IdWorker.getIdStr();
                    entity.setId(id);
                    seq++;
                    entity.setSeq(seq);
                    source.setNewCreateId(id);
                    copyList.add(entity);
                } else {
                    source.setNewCreateId(exist.getId());
                }
                source.setDataId(item.getId());
                sourceList.add(source);
            }
            if (CollectionUtils.isNotEmpty(copyList)) {
                projectPhaseService.saveBatch(copyList);
            }

        }
        return sourceList;
    }

    @Override
    public void batchSaveOrUpdate(BatchTemplatePhaseDTO dto) {
        List<TemplatePhaseDTO> list = dto.getTemplatePhases();
        String templateId = dto.getTemplateId();
        //查询模板下是否已存在该阶段名称
        chekPhaseName(list, templateId);
        List<TemplatePhaseEntity> updateList = new LinkedList<>();
        for (TemplatePhaseDTO item : list) {
            TemplatePhaseEntity entity = new TemplatePhaseEntity();
            entity.setId(item.getId());
            entity.setName(item.getName());
            entity.setTemplateId(templateId);
            updateList.add(entity);
        }
        this.saveOrUpdateBatch(updateList);
    }

    @Override
    public Boolean removeTemplatePhase(String id, String templateId) {
        TemplatePhaseEntity phaseEntity = this.getByIdAndTemplateId(id, templateId);
        if (Objects.isNull(phaseEntity)) {
            throw new ServiceException(ApiError.ERROR_95041);
        }
        String name = phaseEntity.getName();
        String flagName = TaskConstant.APPROVAL_TASK_NAME;
        if (flagName.equals(name)) {
            throw new ServiceException(ApiError.ERROR_95042);
        }
        checkPhaseTask(id, templateId);
        LambdaQueryWrapper<TemplatePhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplatePhaseEntity::getId, id);
        queryWrapper.eq(TemplatePhaseEntity::getTemplateId, templateId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<TemplatePhaseDTO> findList(BasicTemplateIdDTO dto) {
        List<TemplatePhaseDTO> resultList = new ArrayList<>();
        String templateId = dto.getTemplateId();
        //根据模板id 获取到对应的阶段名
        List<TemplatePhaseDTO> phaseList = getTaskPhaseByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(phaseList)) {
            resultList.addAll(phaseList);
        }
        return resultList;
    }

    /**
     * @param templateId
     * @return List<TemplatePhaseEntity>
     * @description: 查询模板下所有阶段
     * @author Will
     * @date: 2022/11/17 10:29
     */
    public List<TemplatePhaseEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplatePhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplatePhaseEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }

    /**
     * @param id
     * @param templateId
     * @return TemplatePhaseEntity
     * @description: 根据id和模板查询阶段
     * @author Will
     * @date: 2022/11/17 10:29
     */
    @Override
    public TemplatePhaseEntity getByIdAndTemplateId(String id, String templateId) {
        LambdaQueryWrapper<TemplatePhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplatePhaseEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplatePhaseEntity::getId, id);
        return this.getOne(queryWrapper);
    }


    /**
     * 根据模板id 获取
     *
     * @param templateIds
     * @return java.util.List<com.erp.model.plm.entity.TemplatePhaseEntity>
     * @author yl
     * @date 2023-03-07 20:13
     */
    @Override
    public List<TemplatePhaseEntity> getByTemplateIds(List<String> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TemplatePhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TemplatePhaseEntity::getTemplateId, templateIds);
        return this.list(queryWrapper);
    }

    /**
     * @param list
     * @param templateId
     * @description: 验证是否已存在阶段名称
     * @author Will
     * @date: 2022/11/17 10:14
     */
    private void chekPhaseName(List<TemplatePhaseDTO> list, String templateId) {
        List<TemplatePhaseEntity> phaseList = getByTemplateId(templateId);
        int size = list.stream().map(TemplatePhaseDTO::getName).distinct().collect(Collectors.toList()).size();
        if (size != list.size()) {
            throw new ServiceException(ApiError.ERROR_95001);
        }
        for (TemplatePhaseDTO phase : list) {
            String id = phase.getId();
            String name = phase.getName();
            List<String> phaseNames = new ArrayList<>();
            if (StringUtils.isNotBlank(id)) {
                phaseNames = phaseList.stream().filter(p -> !p.getId().equals(id)).map(TemplatePhaseEntity::getName).collect(Collectors.toList());
            } else {
                phaseNames = phaseList.stream().map(TemplatePhaseEntity::getName).collect(Collectors.toList());
            }
            if (phaseNames.contains(name)) {
                throw new ServiceException(ApiError.ERROR_95001);
            }

        }
    }

    /**
     * @param id
     * @param templateId
     * @description: 验证该阶段下是否存在任务
     * @author Will
     * @date: 2022/11/17 10:25
     */
    private void checkPhaseTask(String id, String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getPhaseId, id);
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        int count = templateTaskService.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95043);
        }
    }

    private List<TemplatePhaseDTO> getTaskPhaseByTemplateId(String templateId) {
        List<TemplatePhaseDTO> list = baseMapper.getTemplatePhaseByTemplateId(templateId);
        String flagName = TaskConstant.APPROVAL_TASK_NAME;
        if (CollectionUtils.isEmpty(list)) {
            TemplatePhaseEntity entry = new TemplatePhaseEntity();
            entry.setName(flagName);
            entry.setTemplateId(templateId);
            boolean flag = this.save(entry);
            if (flag) {
                TemplatePhaseDTO dto = new TemplatePhaseDTO();
                BeanMapperUtils.copy(entry, dto);
                list.add(dto);
            }
        }
        List<TemplatePhaseDTO> resultList = new ArrayList<>();
        List<TemplatePhaseDTO> otherList = new ArrayList<>();
        for (TemplatePhaseDTO item : list) {
            if (flagName.equals(item.getName())) {
                item.setIsProjectApproval(IsConstant.YES);
                item.setIfQuote(true);
                resultList.add(item);
            } else {
                otherList.add(item);
            }

        }
        resultList.addAll(otherList);
        return resultList;
    }
}




