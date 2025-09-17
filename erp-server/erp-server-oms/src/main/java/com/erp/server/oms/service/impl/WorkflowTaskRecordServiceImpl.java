package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.server.oms.mapper.WorkflowTaskRecordMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * <p>
 * 任务节点记录表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-09-16
 */
@Slf4j
@Service
public class WorkflowTaskRecordServiceImpl extends SuperServiceImpl<WorkflowTaskRecordMapper, WorkflowTaskRecordEntity> implements WorkflowTaskRecordService {


    @Resource
    private DictBasicService dictBasicService;

    /**
     * 添加工作流任务记录
     *
     * @param dto 添加任务的数据传输对象，包含字典类型、源ID和源类型等信息
     * @return 创建的工作流任务记录实体列表
     * @throws ServiceException 当指定类型的工作流任务节点字典不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkflowTaskRecordEntity> addTask(WorkflowTaskRecordDTO.AddTaskDTO dto) {
        //查询字典表 type = exhibitionWorkflowTaskNode
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(dto.getDictBasicTypeEnum().getType());
        if(CollUtil.isEmpty(dictList)){
            throw new ServiceException(ApiError.NOT_EXIST,dto.getDictBasicTypeEnum().getDesc());
        }
        // 根据 sort 字段升序排序
        dictList = dictList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(DictBasicDTO.ViewDTO::getSort))
                .collect(Collectors.toList());

        List<WorkflowTaskRecordEntity> entities = new ArrayList<>();

        // 一次遍历完成实体创建和 nextId 设置
        for (int i = 0; i < dictList.size(); i++) {
            DictBasicDTO.ViewDTO viewDTO = dictList.get(i);
            WorkflowTaskRecordEntity entity = new WorkflowTaskRecordEntity();
            String id = IdWorker.getIdStr();
            entity.setId(id);
            entity.setSourceId(dto.getSourceId());
            entity.setSourceType(dto.getSourceTypeEnum().getCode());
            entity.setClassPath(viewDTO.getValue());
            entity.setIndex(i);
            if (i == 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", dto.getSourceId());
                entity.setInputData(JSON.toJSONString(map));
            }
            entities.add(entity);
        }
        // 批量保存所有实体
        saveBatch(entities);
        return entities;
    }
}