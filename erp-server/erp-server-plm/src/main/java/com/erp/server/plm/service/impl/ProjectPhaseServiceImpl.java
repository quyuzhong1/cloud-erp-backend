package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.BatchTaskPhaseDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.model.plm.entity.ProjectPhaseEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
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
        List<String> nameList = productList.stream().map(TaskPhaseDTO::getName).collect(Collectors.toList());
        //先从系统里面取
        List<TaskPhaseDTO> sysList = sysTaskPhaseService.getSysTaskPhase(nameList);
        if (CollectionUtils.isNotEmpty(sysList)) {
            for (TaskPhaseDTO item : sysList) {
                item.setIfQuote(true);
            }
            resultList.addAll(sysList);
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
            List<String> phaseNames = list.stream().map(TaskPhaseDTO::getName).collect(Collectors.toList());
            List<String> phaseIds = list.stream().map(TaskPhaseDTO::getId).collect(Collectors.toList());
            //获取产品加系统的阶段名 去重后的
            List<String> dbPhaseNames = getDbTaskPhaseNames(productId, phaseIds);
            //获取交集
            List<String> intersections = phaseNames.stream().filter(item -> dbPhaseNames.contains(item)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(intersections)) {
                String intersectionName = String.join(",", intersections);
                throw new ServiceException(1, intersectionName + " 阶段名已存在,不可重复提交");
            }
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

    /**
     * 保存任务阶段
     *
     * @param flagId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-20 15:10
     */
    @Override
    public void savePhase(String flagId, String productId) {
        LambdaQueryWrapper<ProjectPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectPhaseEntity::getProductId, productId);
        List<ProjectPhaseEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectPhaseEntity item : list) {
                item.setProductId(flagId);
                item.setId(IdWorker.getIdStr());
            }
            this.saveBatch(list);
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
     * 是否产品已 引用
     *
     * @param name
     * @return void
     * @author yl
     * @date 2022-10-25 17:46
     */

    @Override
    public void checkTaskQuote(String name) {
        LambdaQueryWrapper<ProjectPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectPhaseEntity::getName, name);
        queryWrapper.eq(ProjectPhaseEntity::getIsSourceSys, IsConstant.YES);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95048);
        }

    }

    /**
     * 方法说明
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-25 19:08
     */
    @Override
    public List<String> getAllSysName() {
        LambdaQueryWrapper<ProjectPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProjectPhaseEntity::getName);
        queryWrapper.eq(ProjectPhaseEntity::getIsSourceSys, IsConstant.YES);
        return this.listObjs(queryWrapper, Object::toString);
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

    private void checkPhaseTask(String id) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getPhaseId, id);
        int count = projectTaskService.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95043);
        }
    }


    /**
     * 根据产品id 获取 产品下任务阶段名 然后在加上 系统的任务阶段名
     *
     * @param productId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-09-14 17:53
     */
    private List<String> getDbTaskPhaseNames(String productId, List<String> phaseIds) {
        LambdaQueryWrapper<ProjectPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProjectPhaseEntity::getName);
        queryWrapper.eq(ProjectPhaseEntity::getProductId, productId);
        queryWrapper.notIn(ProjectPhaseEntity::getId, phaseIds);
        List<String> list = this.listObjs(queryWrapper, Object::toString);
//        List<String> sysList = sysTaskPhaseService.getSysTaskPhaseNames();
        List<String> results = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            results.addAll(list);
        }
//        if (CollectionUtils.isNotEmpty(sysList)) {
//            results.addAll(sysList);
//        }

        return results.stream().distinct().collect(Collectors.toList());
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
        for (TaskPhaseDTO item : list) {
            if (flagName.equals(item.getName())) {
                item.setIsProjectApproval(IsConstant.YES);
            }
        }
        return list;
    }
}
