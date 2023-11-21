package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.dto.JobTaskDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.dmp.entity.PlatformApiEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.server.dmp.mapper.PlatformApiTaskMapper;
import com.erp.server.dmp.service.PlatformApiService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Cloud
 */
@Service
public class PlatformApiTaskServiceImpl extends SuperServiceImpl<PlatformApiTaskMapper, PlatformApiTaskEntity>
        implements PlatformApiTaskService {

    @Resource
    private PlatformApiService platformApiService;

    /**
     * 修改任务下次执行
     *
     * @param jobTaskDTO jobTaskDTO
     * @param type
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/15 10:24
     **/
    @Override
    public Boolean updateTaskStateById(JobTaskDTO jobTaskDTO, Integer type) {
        LambdaUpdateWrapper<PlatformApiTaskEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        if (1 != type) {
            Integer interval = jobTaskDTO.getIntervalTime();
            LocalDateTime nextTime = jobTaskDTO.getNextTime();
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getLastTime, nextTime);
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getNextTime, nextTime.plusSeconds(interval));
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getRetryTimes, 0);
        } else {
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getRetryTimes, jobTaskDTO.getRetryTimes() + 1);
        }
        if (3 != type) {
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getStatus, 1);
        }
        lambdaUpdateWrapper.set(PlatformApiTaskEntity::getUpdateTime, LocalDateTime.now());
        lambdaUpdateWrapper.eq(PlatformApiTaskEntity::getId, jobTaskDTO.getId());
        return this.update(lambdaUpdateWrapper);
    }

    @Override
    public PlatformApiTaskEntity getByApiCode(String taskName) {
        return lambdaQuery()
                .eq(PlatformApiTaskEntity::getApiCode, taskName)
                .le(PlatformApiTaskEntity::getNextTime, LocalDateTime.now())
                .eq(PlatformApiTaskEntity::getStatus, 3)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createPlatformTask(PlatformTaskDTO.AddDTO dto) {
        List<PlatformApiEntity> entityList = platformApiService.listByPlatform(dto.getDictPlatform());
        if (CollectionUtil.isEmpty(entityList)) {
            return Boolean.TRUE;
        }
        // 根据店铺id查询是否已经存在任务
        List<PlatformApiTaskEntity> taskEntity = lambdaQuery()
                .eq(PlatformApiTaskEntity::getShopId, dto.getShopId())
                .eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform())
                .list();
        // 对比当前店铺不存在的任务
        Set<String> existApiIds = taskEntity.stream().map(PlatformApiTaskEntity::getPlatformApiId).collect(Collectors.toSet());
        // 需要添加的任务
        List<PlatformApiEntity> notExistApiList = entityList
                .stream()
                .filter(item -> !existApiIds.contains(item.getId()))
                .collect(Collectors.toList());
        // 对比当前店铺不存在的任务
        Set<String> allApiList = entityList.stream().map(PlatformApiEntity::getId).collect(Collectors.toSet());
        // 需要删除的任务
        List<String> taskIds = taskEntity
                .stream()
                .filter(item -> !allApiList.contains(item.getPlatformApiId()))
                .map(PlatformApiTaskEntity::getId)
                .collect(Collectors.toList());
        List<PlatformApiTaskEntity> insertEntityList = notExistApiList.stream()
                .map(task -> getPlatformApiTaskEntity(dto, task))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(insertEntityList)) {
            this.saveBatch(insertEntityList);
        }
        if (CollectionUtil.isNotEmpty(taskIds)) {
            this.removeByIds(taskIds);
        }
        return Boolean.TRUE;
    }

    private static PlatformApiTaskEntity getPlatformApiTaskEntity(PlatformTaskDTO.AddDTO dto, PlatformApiEntity task) {
        PlatformApiTaskEntity entity = new PlatformApiTaskEntity();
        entity.setShopId(dto.getShopId());
        entity.setShopName(dto.getShopName());
        entity.setDictPlatform(dto.getDictPlatform());
        entity.setApiCode(task.getApiCode());
        entity.setApiName(task.getApiName());
        entity.setIntervalTime(task.getIntervalTime());
        entity.setLastTime(LocalDateTime.now());
        entity.setNextTime(LocalDateTime.now().plusSeconds(task.getIntervalTime()));
        entity.setStatus(1);
        entity.setRetryTimes(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setPlatformApiId(task.getId());
        entity.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        entity.setSyncOperate(task.getSyncOperate());
        entity.setBillType(task.getBillType());
        entity.setOperateType(task.getOperateType());
        return entity;
    }

    @Override
    public List<PlatformApiTaskEntity> listByPlatformAndShop(String dictPlatform, String shopId) {
        return lambdaQuery()
                .eq(PlatformApiTaskEntity::getDictPlatform, dictPlatform)
                .eq(PlatformApiTaskEntity::getShopId, shopId)
                .list();
    }

    @Override
    public List<JobTaskDTO> listApiTask(LocalDateTime localTime, String operateType) {
        return baseMapper.selectApiTask(localTime, operateType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskTypeState(List<JobTaskDTO> timeoutList, int type) {
        baseMapper.updateTaskTypeState(timeoutList, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removePlatformTask(PlatformTaskDTO.AddDTO dto) {
        LambdaQueryWrapper<PlatformApiTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform());
        queryWrapper.eq(PlatformApiTaskEntity::getShopId, dto.getShopId());
        return this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disabledPlatformTask(PlatformTaskDTO.DisabledDTO dto) {
        return lambdaUpdate().eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform())
                .eq(PlatformApiTaskEntity::getShopId, dto.getShopId())
                .ne(PlatformApiTaskEntity::getDisabled, dto.getDisabled())
                .set(PlatformApiTaskEntity::getDisabled, dto.getDisabled())
                .update(new PlatformApiTaskEntity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createThirdWarehouseTask(ThirdWarehouseTaskDTO.AddDTO dto) {
        List<PlatformApiEntity> entityList = platformApiService.listByPlatform(dto.getDictPlatform());
        if (CollectionUtil.isEmpty(entityList)) {
            return Boolean.TRUE;
        }
        // 根据授权id查询
        List<PlatformApiTaskEntity> taskEntity = lambdaQuery()
                .eq(PlatformApiTaskEntity::getShopId, dto.getAuthKey())
                .eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform())
                .list();
        Set<String> existApiIds = taskEntity.stream().map(PlatformApiTaskEntity::getPlatformApiId).collect(Collectors.toSet());
        // 需要添加的任务
        List<PlatformApiEntity> notExistApiList = entityList
                .stream()
                .filter(item -> !existApiIds.contains(item.getId()))
                .collect(Collectors.toList());

        List<PlatformApiTaskEntity> insertEntityList = notExistApiList.stream()
                .map(task -> getThirdWarehouseApiTaskEntity(dto, task))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(insertEntityList)) {
            this.saveBatch(insertEntityList);
        }
        return Boolean.TRUE;
    }

    private static PlatformApiTaskEntity getThirdWarehouseApiTaskEntity(ThirdWarehouseTaskDTO.AddDTO dto, PlatformApiEntity task) {
        PlatformApiTaskEntity entity = new PlatformApiTaskEntity();
        entity.setShopId(dto.getAuthKey());
        entity.setShopName(task.getDictPlatform());
        entity.setApiParam(dto.getAuthInfo());
        entity.setDictPlatform(dto.getDictPlatform());
        entity.setApiCode(task.getApiCode());
        entity.setApiName(task.getApiName());
        entity.setIntervalTime(task.getIntervalTime());
        entity.setLastTime(LocalDateTime.now());
        entity.setNextTime(LocalDateTime.now().plusSeconds(task.getIntervalTime()));
        entity.setStatus(1);
        entity.setRetryTimes(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setPlatformApiId(task.getId());
        entity.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        entity.setSyncOperate(task.getSyncOperate());
        entity.setBillType(task.getBillType());
        entity.setOperateType(task.getOperateType());
        return entity;
    }

}
