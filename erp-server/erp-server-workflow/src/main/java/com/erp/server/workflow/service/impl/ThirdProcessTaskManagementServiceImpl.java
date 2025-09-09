package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.ThirdProcessTaskManagementEntity;
import com.erp.server.workflow.mapper.ThirdProcessTaskManagementMapper;
import com.erp.server.workflow.service.ThirdProcessTaskManagementService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ThirdProcessTaskManagementDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-23
 */
@Slf4j
@Service
public class ThirdProcessTaskManagementServiceImpl extends SuperServiceImpl<ThirdProcessTaskManagementMapper, ThirdProcessTaskManagementEntity> implements ThirdProcessTaskManagementService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(List<ThirdProcessTaskManagementDTO.AddDTO> addDTO) {
        List<ThirdProcessTaskManagementEntity> taskManagementEntityList = BeanMapperUtils.copyList(ThirdProcessTaskManagementEntity.class, addDTO);
        log.info("开始新增");
        boolean save = super.saveBatch(taskManagementEntityList);
        if (!save) {
            throw new ServiceException("保存失败");
        }
        return new BaseResultDTO.AddDTO("", "");
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<ThirdProcessTaskManagementDTO.UpdateDTO> addOrUpdateDTO) {
        List<ThirdProcessTaskManagementEntity> taskEntities = BeanMapperUtils.copyList(ThirdProcessTaskManagementEntity.class, addOrUpdateDTO);
        Set<String> mianids = taskEntities.stream().map(ThirdProcessTaskManagementEntity::getMainId).collect(Collectors.toSet());
        List<ThirdProcessTaskManagementEntity> oldEntityList = super.list(new LambdaQueryWrapper<ThirdProcessTaskManagementEntity>().in(ThirdProcessTaskManagementEntity::getMainId, mianids));
        // 构建 oldEntity 的 map，key 为 mainId + "_" + taskId
        Map<String, ThirdProcessTaskManagementEntity> oldEntityMap = oldEntityList.stream()
                .collect(Collectors.toMap(
                        e -> e.getMainId() + "_" + e.getTaskId(),
                        e -> e
                ));

        // 构建新 entity 的 key 集合
        Set<String> newEntityKeys = taskEntities.stream()
                .map(e -> e.getMainId() + "_" + e.getTaskId())
                .collect(Collectors.toSet());

        // 找出待删除的 oldEntity
        List<ThirdProcessTaskManagementEntity> toDeleteList = oldEntityList.stream()
                .filter(e -> !newEntityKeys.contains(e.getMainId() + "_" + e.getTaskId()))
                .collect(Collectors.toList());
        if (!toDeleteList.isEmpty()){
            List<String> removeIds = toDeleteList.stream().map(ThirdProcessTaskManagementEntity::getId).collect(Collectors.toList());
            super.removeByIds(removeIds);
        }

        // 匹配到的 entity 赋值 id
        for (ThirdProcessTaskManagementEntity entity : taskEntities) {
            String key = entity.getMainId() + "_" + entity.getTaskId();
            if (oldEntityMap.containsKey(key)) {
                entity.setId(oldEntityMap.get(key).getId());
            }
        }
        return super.saveOrUpdateBatch(taskEntities);
    }
}
