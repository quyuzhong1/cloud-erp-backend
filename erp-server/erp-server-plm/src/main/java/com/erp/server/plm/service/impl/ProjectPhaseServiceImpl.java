package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.BatchTaskPhaseDTO;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.model.plm.entity.ProjectPhaseEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.SysTaskPhaseEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProjectPhaseMapper;
import com.erp.server.plm.service.ProjectPhaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.SysTaskPhaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 任务阶段表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectPhaseServiceImpl extends ServiceImpl<ProjectPhaseMapper, ProjectPhaseEntity> implements ProjectPhaseService {


    @Autowired
    private SysTaskPhaseService sysTaskPhaseService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProjectPhaseService projectPhaseService;

    /**
     * 获取 产品任务的阶段名
     *
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskPhaseDTO>
     * @author yl
     * @date 2022-09-13 17:39
     */
    @Override
    public List<TaskPhaseDTO> findList(BasicProductIdDTO dto) {
        List<TaskPhaseDTO> resultList = new ArrayList<>();
        String productId = dto.getProductId();
        //根据产品id 获取到对应的阶段名
        List<TaskPhaseDTO> productList = getTaskPhaseByProductId(productId);
        if (CollectionUtils.isNotEmpty(productList)) {
            resultList.addAll(productList);
        }

        return resultList;
    }

    /**
     * 批量保存或者修改任务阶段
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-14 16:52
     */
    @Override
    public void batchSaveOrUpdate(BatchTaskPhaseDTO dto) {
        List<TaskPhaseDTO> list = dto.getTaskPhases();
        String productId = dto.getProductId();
        //获取到任务阶段的
        if (CollectionUtils.isNotEmpty(list)) {
            //获取不是系统的阶段名 那就是产品的阶段名
            chekPhaseName(list, productId);
            List<ProjectPhaseEntity> updateList = new LinkedList<>();
            for (TaskPhaseDTO item : list) {
                ProjectPhaseEntity entity = new ProjectPhaseEntity();
                entity.setId(item.getId());
                entity.setName(item.getName());
                entity.setProductId(productId);
                updateList.add(entity);
            }
            this.saveOrUpdateBatch(updateList);
        }
    }

    private void chekPhaseName(List<TaskPhaseDTO> list, String productId) {
        List<ProjectPhaseEntity> phaseList = getByProductId(productId);
        //List<SysTaskPhaseEntity> sysTaskPhaseList = sysTaskPhaseService.getSysTaskPhaseNames();
        //List<String> sysTaskPhase = sysTaskPhaseList.stream().map(SysTaskPhaseEntity::getName).collect(Collectors.toList());

        int size = list.stream().map(TaskPhaseDTO::getName).distinct().collect(Collectors.toList()).size();
        if (size != list.size()) {
            throw new ServiceException(ApiError.ERROR_95001);
        }

        for (TaskPhaseDTO phase : list) {
            String id = phase.getId();
            String name = phase.getName();
            List<String> phaseNames = new ArrayList<>();
            if (StringUtils.isNotBlank(id)) {
                phaseNames = phaseList.stream().filter(p -> !p.getId().equals(id)).map(ProjectPhaseEntity::getName).collect(Collectors.toList());
            } else {
                phaseNames = phaseList.stream().map(ProjectPhaseEntity::getName).collect(Collectors.toList());
            }
            if (phaseNames.contains(name)/*||sysTaskPhase.contains(name)*/) {
                throw new ServiceException(ApiError.ERROR_95001);
            }

        }


    }


    /**
     * 添加立项阶段
     *
     * @param productId
     * @param phaseName
     * @return java.lang.String
     * @author yl
     * @date 2022-09-28 16:30
     */
    @Override
    public String saveTaskPhase(String productId, String phaseName, Integer isSourceSys) {
        ProjectPhaseEntity entity = new ProjectPhaseEntity();
        entity.setProductId(productId);
        entity.setName(phaseName);
        entity.setIsSourceSys(isSourceSys);
        this.save(entity);
        return entity.getId();
    }


    /**
     * 删除阶段
     *
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-21 15:35
     */
    @Override
    public Boolean removeTaskPhaseById(String id) {
        ProjectPhaseEntity phaseEntity = this.getById(id);
        if (Objects.isNull(phaseEntity)) {
            throw new ServiceException(ApiError.ERROR_95041);
        }
        String name = phaseEntity.getName();
        String flagName = TaskConstant.APPROVAL_TASK_NAME;
        if (flagName.equals(name)) {
            throw new ServiceException(ApiError.ERROR_95042);
        }
        checkPhaseTask(id);
        return removeById(id);
    }


    /**
     * 根据产品id 获取阶段名
     *
     * @param productId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-26 10:06
     */
    @Override
    public List<String> getPhaseNameName(String productId) {
        LambdaQueryWrapper<ProjectPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProjectPhaseEntity::getName);
        queryWrapper.eq(ProjectPhaseEntity::getProductId, productId);
        return this.listObjs(queryWrapper, Object::toString);
    }

    /**
     * 保存系统的阶段名
     *
     * @param
     * @return java.util.List<com.erp.model.plm.entity.ProjectPhaseEntity>
     * @author yl
     * @date 2022-10-27 14:30
     */
    @Override
    public List<CopySourceDTO> saveSysPhase(String productId) {
        List<SysTaskPhaseEntity> sysPhaseNames = sysTaskPhaseService.getSysTaskPhaseNames();

        if (CollectionUtils.isNotEmpty(sysPhaseNames)) {
            List<ProjectPhaseEntity>  existList=projectPhaseService.getByProductId(productId);
            List<CopySourceDTO> sourceList = new ArrayList<>(sysPhaseNames.size());
            List<ProjectPhaseEntity> saveList = new ArrayList<>();
            for (SysTaskPhaseEntity item : sysPhaseNames) {
                ProjectPhaseEntity exist= existList.stream().filter(e->e.getName().equals(item.getName())).findFirst().orElse(null);
                CopySourceDTO source = new CopySourceDTO();
                if(Objects.isNull(exist)){
                    ProjectPhaseEntity phaseEntity = new ProjectPhaseEntity();
                    phaseEntity.setName(item.getName());
                    phaseEntity.setProductId(productId);
                    phaseEntity.setIsSourceSys(IsConstant.YES);
                    String id = IdWorker.getIdStr();
                    phaseEntity.setId(id);
                    source.setNewCreateId(id);
                    saveList.add(phaseEntity);
                }else{
                    source.setNewCreateId(exist.getId());
                }
                source.setDataId(item.getId());
                sourceList.add(source);
            }
            this.saveBatch(saveList);
            return sourceList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<ProjectPhaseEntity> getByProductId(String productId) {
        LambdaQueryWrapper<ProjectPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectPhaseEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    private void checkPhaseTask(String id) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getPhaseId, id);
        int count = projectTaskService.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95043);
        }
    }


    /**
     * 根据产品id 获取到 产品任务阶段名
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.TaskPhaseDTO>
     * @author yl
     * @date 2022-09-13 18:00
     */

    private List<TaskPhaseDTO> getTaskPhaseByProductId(String productId) {
        List<TaskPhaseDTO> list = baseMapper.getTaskPhaseByProductId(productId);
        String flagName = TaskConstant.APPROVAL_TASK_NAME;
        List<TaskPhaseDTO> resultList = new ArrayList<>();
        List<TaskPhaseDTO> otherList = new ArrayList<>();
        for (TaskPhaseDTO item : list) {
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
