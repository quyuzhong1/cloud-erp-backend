package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.ThirdProcessTaskManagementDTO;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import com.erp.model.workflow.entity.ThirdProcessManagementEntity;
import com.erp.model.workflow.entity.ThirdProcessTaskManagementEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.mapper.ThirdProcessManagementMapper;
import com.erp.server.workflow.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ThirdProcessManagementDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-23
 */
@Slf4j
@Service
public class ThirdProcessManagementServiceImpl extends SuperServiceImpl<ThirdProcessManagementMapper, ThirdProcessManagementEntity> implements ThirdProcessManagementService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private ApproveTaskInfoService approveTaskInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ThirdProcessTaskManagementService thirdProcessTaskManagementService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdProcessManagementDTO.AddDTO addDTO) {
        ThirdProcessManagementEntity thirdProcessManagementEntity = new ThirdProcessManagementEntity();
        BeanMapperUtils.copy(addDTO, thirdProcessManagementEntity);

        // 数据处理
        handleData(thirdProcessManagementEntity);

        log.info("开始新增");
        boolean save = super.save(thirdProcessManagementEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , thirdProcessManagementEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, thirdProcessManagementEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(thirdProcessManagementEntity.getId(), thirdProcessManagementEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdProcessManagementDTO.UpdateDTO addOrUpdateDTO) {
        ThirdProcessManagementEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        ThirdProcessManagementEntity thirdProcessManagementEntity =  BeanMapperUtils.map(ThirdProcessManagementEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(thirdProcessManagementEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(thirdProcessManagementEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", thirdProcessManagementEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thirdProcessManagementEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, thirdProcessManagementEntity, null, thirdProcessManagementEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(JSONObject jsonObject, String sourcePlatform) {
        // 1. 通过 instance_code 查询是否已有记录
        String instanceCode = jsonObject.getStr("instance_code");
        ThirdProcessManagementEntity thirdProcessManagementEntity = this.lambdaQuery()
                .eq(ThirdProcessManagementEntity::getProcessInstanceId, instanceCode)
                .one();

        // 2. 组装主表数据
        ApproveTaskInfoEntity one = approveTaskInfoService.getOne(
                new LambdaQueryWrapper<ApproveTaskInfoEntity>()
                        .eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr("instanceCode"))
                        .orderByDesc(ApproveTaskInfoEntity::getCreateTime)
        );
        ThirdProcessManagementDTO.AddDTO addDTO = new ThirdProcessManagementDTO.AddDTO();
        addDTO.setProcessInstanceId(instanceCode);
        addDTO.setProcessDefinitionId(jsonObject.getStr("approval_code"));
        addDTO.setSysUserId(jsonObject.getStr("user_id"));
        addDTO.setBusinessId(one.getBussinessCode());
        addDTO.setBusinessCode(one.getBussinessId());
        addDTO.setBusinessKey(one.getBussinessKey());
        addDTO.setStatus(jsonObject.getStr("status"));
        addDTO.setProcessInstanceName(jsonObject.getStr("approval_name"));
        addDTO.setSourcePlatform(sourcePlatform);
        addDTO.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonObject.getLong("start_time")), ZoneId.systemDefault()));
        addDTO.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonObject.getLong("end_time")), ZoneId.systemDefault()));

        if (thirdProcessManagementEntity != null) {
            // 已有记录，更新
            BeanMapperUtils.copy(addDTO, thirdProcessManagementEntity);
            this.updateById(thirdProcessManagementEntity);
        } else {
            // 没有记录，新增
            thirdProcessManagementEntity = BeanUtil.copyProperties(addDTO, ThirdProcessManagementEntity.class);
            this.save(thirdProcessManagementEntity);
        }

        // 3. 生成 task 明细数据
        JSONArray taskList = jsonObject.getJSONArray("task_list");
        ArrayList<ThirdProcessTaskManagementEntity> arrayList = new ArrayList<>();
        String id = thirdProcessManagementEntity.getId();
        taskList.jsonIter().forEach(task -> {
            ThirdProcessTaskManagementEntity taskDTO = new ThirdProcessTaskManagementEntity();
            taskDTO.setTaskId(task.getStr("id"));
            taskDTO.setNodeId(task.getStr("node_id"));
            taskDTO.setNodeName(task.getStr("node_name"));
            taskDTO.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(task.getLong("start_time")), ZoneId.systemDefault()));
            taskDTO.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(task.getLong("end_time")), ZoneId.systemDefault()));
            taskDTO.setTaskStatus(task.getStr("status"));
            taskDTO.setThirdUserId(task.getStr("user_id"));
            SysUserThirdEntity userByThird = sysUserFeign.getUserByThird("fs", task.getStr("user_id"));
            taskDTO.setSysUserId(userByThird.getUserId());
            taskDTO.setMainId(id);
            arrayList.add(taskDTO);
        });
        //arrayList根据nodeid更新或插入数据
        for (ThirdProcessTaskManagementEntity taskEntity : arrayList) {
            LambdaQueryWrapper<ThirdProcessTaskManagementEntity> queryWrapper = new LambdaQueryWrapper<ThirdProcessTaskManagementEntity>()
                    .eq(ThirdProcessTaskManagementEntity::getMainId, taskEntity.getMainId())
                    .eq(ThirdProcessTaskManagementEntity::getNodeId, taskEntity.getNodeId());
            ThirdProcessTaskManagementEntity exist = thirdProcessTaskManagementService.getOne(queryWrapper);
            if (exist != null) {
                // 更新
                taskEntity.setId(exist.getId());
                thirdProcessTaskManagementService.updateById(taskEntity);
            } else {
                // 插入
                thirdProcessTaskManagementService.save(taskEntity);
            }
        }
        //TODO 根据status执行后续流程

    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdProcessManagementEntity thirdProcessManagementEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
