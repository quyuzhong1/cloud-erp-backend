package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.model.workflow.dto.ThirdProcessTaskManagementDTO;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import com.erp.model.workflow.entity.ThirdProcessManagementEntity;
import com.erp.model.workflow.entity.ThirdProcessTaskManagementEntity;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.model.workflow.enums.FsRequestBodyAttributesEnum;
import com.erp.model.workflow.enums.ProcessSourcePlatformEnum;
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
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

import static com.common.business.enums.ApproveTypeEnum.PASS;
import static com.common.business.enums.ApproveTypeEnum.REJECT;

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
public class ThirdProcessManagementServiceImpl extends SuperServiceImpl<ThirdProcessManagementMapper, ThirdProcessManagementEntity> implements ThirdProcessManagementService {

    @Resource
    private ApproveTaskInfoService approveTaskInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ThirdProcessTaskManagementService thirdProcessTaskManagementService;

    @Resource
    private ProcessManagementService processManagementService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdProcessManagementDTO.AddDTO addDTO) {
        ThirdProcessManagementEntity thirdProcessManagementEntity = new ThirdProcessManagementEntity();
        BeanMapperUtils.copy(addDTO, thirdProcessManagementEntity);

        log.info("开始新增主记录。");
        boolean save = super.save(thirdProcessManagementEntity);
        if (!save) {
            throw new ServiceException("保存主记录失败");
        }
        String mainId = thirdProcessManagementEntity.getId();
        if (addDTO.getTaskList() != null && !addDTO.getTaskList().isEmpty()) {
            thirdProcessTaskManagementService.add(addDTO.getTaskList());
        }
        return new BaseResultDTO.AddDTO(mainId, mainId);
    }

    /**
     * 修改一条现有记录及其关联的任务。
     * 现在假定任务列表是 UpdateDTO 的一部分。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdProcessManagementDTO.UpdateDTO updateDTO) {
        ThirdProcessManagementEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, ""));

        ThirdProcessManagementEntity thirdProcessManagementEntity = BeanMapperUtils.map(ThirdProcessManagementEntity.class, updateDTO);

        log.info("开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(thirdProcessManagementEntity);
        if (!save) {
            throw new ServiceException("保存主记录失败");
        }

        if (updateDTO.getTaskList() != null) {
            List<ThirdProcessTaskManagementDTO.UpdateDTO> updateDTOList = BeanUtil.copyToList(updateDTO.getTaskList(), ThirdProcessTaskManagementDTO.UpdateDTO.class);
            for (ThirdProcessTaskManagementDTO.UpdateDTO dto : updateDTOList) {
                dto.setMainId(updateDTO.getId());
            }
            thirdProcessTaskManagementService.update(updateDTOList);
        }
        return Boolean.TRUE;
    }

    /**
     * 处理来自第三方源数据的入口点。
     * 此方法现在作为“编排者”，解析请求并委托给 add() 或 update()。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(JSONObject jsonObject, String sourcePlatform) {
        String instanceCode = jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode());

        // 1. 检查记录是否已存在
        ThirdProcessManagementEntity existingEntity = this.lambdaQuery()
                .eq(ThirdProcessManagementEntity::getProcessInstanceId, instanceCode)
                .one();

        // 2. 从 JSON 载荷构建 DTO
        ThirdProcessManagementDTO.AddDTO addDTO = buildAddDTOFromJson(jsonObject, sourcePlatform);

        // 3. 委托给 add() 或 update()
        if (existingEntity != null) {
            // 这是更新操作
            ThirdProcessManagementDTO.UpdateDTO updateDTO = BeanMapperUtils.map(ThirdProcessManagementDTO.UpdateDTO.class, addDTO);
            updateDTO.setId(existingEntity.getId());
            this.update(updateDTO);
        } else {
            // 这是新增操作
            this.add(addDTO);
        }

        // 4. 处理后置的回调逻辑
        FSApprovalStatusEnum statusEnum = FSApprovalStatusEnum.getByCode(jsonObject.getStr(FsRequestBodyAttributesEnum.STATUS.getCode()));
        if (statusEnum != FSApprovalStatusEnum.PENDING) {
            ApproveTaskInfoEntity one = approveTaskInfoService.getOne(
                    new LambdaQueryWrapper<ApproveTaskInfoEntity>()
                            .eq(ApproveTaskInfoEntity::getThirdInstanceId, instanceCode)
                            .orderByDesc(ApproveTaskInfoEntity::getCreateTime)
            );
            handleCallbackLogic(jsonObject, statusEnum, one);
        }
    }

    /**
     * 从传入的 JSONObject 构建一个完整填充的 AddDTO 的辅助方法。
     * 此方法封装了映射和数据检索逻辑。
     */
    private ThirdProcessManagementDTO.AddDTO buildAddDTOFromJson(JSONObject jsonObject, String sourcePlatform) {
        ThirdProcessManagementDTO.AddDTO processManagementDTO = new ThirdProcessManagementDTO.AddDTO();

        String instanceCode = jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode());

        // 获取关联的业务信息
        ApproveTaskInfoEntity one = approveTaskInfoService.getOne(
                new LambdaQueryWrapper<ApproveTaskInfoEntity>()
                        .eq(ApproveTaskInfoEntity::getThirdInstanceId, instanceCode)
                        .orderByDesc(ApproveTaskInfoEntity::getCreateTime)
        );

        // 映射任务列表明细
        JSONArray taskListJson = jsonObject.getJSONArray(FsRequestBodyAttributesEnum.TASKLIST.getCode());
        List<ThirdProcessTaskManagementDTO.AddDTO> taskManagementDTOList = new ArrayList<>();
        // 为了提高效率，您可能需要一次性获取所有用户
        ArrayList<String> userIds = new ArrayList<>();
        for (JSONObject task : taskListJson.jsonIter()) {
            userIds.add(task.getStr(FsRequestBodyAttributesEnum.USERID.getCode()));
        }
        userIds.add(jsonObject.getStr(FsRequestBodyAttributesEnum.USERID.getCode()));
        List<SysUserThirdEntity> users = sysUserFeign.getUserByThirdIdList(ProcessSourcePlatformEnum.FS.getCode().toUpperCase(), userIds);
        Map<String, String> thirdIdToSysIdMap = users.stream().collect(Collectors.toMap(SysUserThirdEntity::getThirdUserId, SysUserThirdEntity::getUserId));

        // 映射主表字段
        processManagementDTO.setProcessInstanceId(instanceCode);
        processManagementDTO.setProcessDefinitionId(jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALCODE.getCode()));
        processManagementDTO.setSysUserId(thirdIdToSysIdMap.get(jsonObject.getStr(FsRequestBodyAttributesEnum.USERID.getCode())));
        processManagementDTO.setThirdUserId(jsonObject.getStr(FsRequestBodyAttributesEnum.USERID.getCode()));
        processManagementDTO.setBusinessId(one.getBussinessId());
        processManagementDTO.setBusinessCode(one.getBussinessCode());
        processManagementDTO.setBusinessKey(one.getBussinessKey());

        processManagementDTO.setStatus(jsonObject.getStr(FsRequestBodyAttributesEnum.STATUS.getCode()));

        processManagementDTO.setProcessInstanceName(jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALNAME.getCode()));
        processManagementDTO.setSourcePlatform(sourcePlatform);
        processManagementDTO.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonObject.getLong(FsRequestBodyAttributesEnum.STARTTIME.getCode())), ZoneId.systemDefault()));
        processManagementDTO.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonObject.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode())), ZoneId.systemDefault()));

        //处理明细
        taskListJson.jsonIter().forEach(taskJson -> {
            ThirdProcessTaskManagementDTO.AddDTO taskManagementDTO = new ThirdProcessTaskManagementDTO.AddDTO(); // 替换为您实际的任务DTO
            String thirdId = taskJson.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
            taskManagementDTO.setTaskId(taskJson.getStr(FsRequestBodyAttributesEnum.ID.getCode()));
            taskManagementDTO.setNodeId(taskJson.getStr(FsRequestBodyAttributesEnum.NODEID.getCode()));
            taskManagementDTO.setNodeName(taskJson.getStr(FsRequestBodyAttributesEnum.NODENAME.getCode()));
            taskManagementDTO.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(taskJson.getLong(FsRequestBodyAttributesEnum.STARTTIME.getCode())), ZoneId.systemDefault()));
            taskManagementDTO.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(taskJson.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode())), ZoneId.systemDefault()));

            taskManagementDTO.setTaskStatus(taskJson.getStr(FsRequestBodyAttributesEnum.STATUS.getCode()));

            taskManagementDTO.setThirdUserId(taskJson.getStr(FsRequestBodyAttributesEnum.USERID.getCode()));
            taskManagementDTO.setSysUserId(thirdIdToSysIdMap.get(thirdId));
            taskManagementDTOList.add(taskManagementDTO);
        });
        processManagementDTO.setTaskList(taskManagementDTOList);

        return processManagementDTO;
    }

    /**
     * 根据流程状态处理最终的回调逻辑。
     */
    private void handleCallbackLogic(JSONObject jsonObject, FSApprovalStatusEnum statusEnum, ApproveTaskInfoEntity one) {
        JSONArray taskList = jsonObject.getJSONArray(FsRequestBodyAttributesEnum.TASKLIST.getCode());
        JSONObject lastTask = taskList.getJSONObject(taskList.size() - 1);
        String lastUserId = lastTask.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
        Long endTime = lastTask.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode());
        LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

        switch (statusEnum) {
            case APPROVED:
                handleCallback(one, PASS.getStatus(), lastUserId, approveTime);
                break;
            case REJECTED:
                handleCallback(one, REJECT.getStatus(), lastUserId, approveTime);
                break;
            case CANCELED:
                // TODO 缺少统一撤销入口
                break;
            case DELETED:
                // TODO 缺少统一的反审核入口
                break;
            default:
                // PENDING 或其他无需操作的状态
                break;
        }
    }

    /**
     * 原始的回调方法，保持不变。
     */
    @Override
    public void handleCallback(ApproveTaskInfoEntity one, String approveStatus, String userId, LocalDateTime approveTime) {
        EndProcessDTO processDTO = new EndProcessDTO();
        processDTO.setBusinessKey(one.getBussinessKey());
        processDTO.setBusinessId(one.getBussinessId());
        processDTO.setApproveStatus(ApproveTypeEnum.getByCode(approveStatus));
        // 来自第三方系统的用户ID可能需要转换为您系统内部的用户ID
        SysUserThirdEntity user = sysUserFeign.getUserByThird(ProcessSourcePlatformEnum.FS.getCode().toUpperCase(), userId);
        processDTO.setApproveUserId(user.getUserId());
        processDTO.setApproveTime(approveTime);
        processManagementService.callFeign(one.getBussinessKey(), processDTO);
    }
}
