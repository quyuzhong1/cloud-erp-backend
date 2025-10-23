package com.erp.server.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.context.CreateBillFactory;
import com.erp.server.workflow.handler.CreateBillHandler;
import com.erp.server.workflow.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.common.business.enums.ApproveTypeEnum.PASS;
import static com.common.business.enums.ApproveTypeEnum.REJECT;

/**
 * 飞书审批示例详情拉取接口服务IMPL
 * @author will
 * @date 2025/10/21 19:12
 */
@Service
@Slf4j
public class FsInstancesServiceImpl implements FsInstancesService {

    @Resource
    private ApproveTaskInfoService approveTaskInfoService;

    @Resource
    private CreateBillFactory createBillFactory;

    @Resource
    private CfgThirdProcessService cfgThirdProcessService;

    @Resource
    private CfgProcessFieldMapService cfgProcessFieldMapService;

    @Resource
    private CfgProcessValueMapService  cfgProcessValueMapService;

    @Resource
    private ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    private ProcessManagementService processManagementService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleUpdateStatus(JSONObject jsonObject, String sourcePlatform) {
        //更新或新增thirdProcessMan and taskMan
        thirdProcessManagementService.addOrUpdate(jsonObject,sourcePlatform);

        //回调
        FSApprovalStatusEnum statusEnum = FSApprovalStatusEnum.getByCode(jsonObject.getStr(FsRequestBodyAttributesEnum.STATUS.getCode()));
        if (statusEnum != FSApprovalStatusEnum.PENDING) {
            ApproveTaskInfoEntity one = approveTaskInfoService.getOne(
                    new LambdaQueryWrapper<ApproveTaskInfoEntity>()
                            .eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode()))
                            .orderByDesc(ApproveTaskInfoEntity::getCreateTime)
            );
            handleCallbackLogic(jsonObject, statusEnum, one);
        }
    }

    @Override
    public void handleAddInstance(JSONObject jsonObject) {
        // 从 jsonObject 中获取 instanceCode
        CfgThirdProcessEntity thirdProcessEntity = cfgThirdProcessService.getOne(new LambdaQueryWrapper<CfgThirdProcessEntity>().eq(CfgThirdProcessEntity::getThirdProcessDefinitionCode, jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALCODE.getCode())).eq(CfgThirdProcessEntity::getIsDeleted, false));

        if (ObjectUtil.isEmpty(thirdProcessEntity)) {
            throw new ServiceException("未找到对应的三方审批生成配置");
        }
        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));

        List<String> fieldIdList = fieldMapList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIdList).eq(CfgProcessValueMapEntity::getIsDeleted, false));

        //使用bussniessKey查询出三方审批生成配置，根据oprateType处理instance
        CreateBillHandler createBillHandler = createBillFactory.getCreateBillHandler(thirdProcessEntity.getBussinessKey());
        createBillHandler.createBill(jsonObject, thirdProcessEntity, fieldMapList, valueMapList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void addFreshGenerate(JSONObject jsonObject, Map<String, Object> map, CfgThirdProcessEntity thirdProcessEntity,
                                 List<ApproveTaskDetailDTO.AddDTO> addDTOS) {
        String createUserId = jsonObject.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
        JSONArray taskList = jsonObject.getJSONArray(FsRequestBodyAttributesEnum.TASKLIST.getCode());
        JSONObject lastTask = taskList.getJSONObject(taskList.size() - 1);
        String lastUserId = lastTask.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
        Long endTime = lastTask.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode());
        LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

        //值映射
        SupplierDTO.InsertDTO addDTO = BeanUtil.toBean(map, SupplierDTO.InsertDTO.class);

        //第一条账户设置成默认
        if (CollUtil.isNotEmpty(addDTO.getBankAccountList())) {
            addDTO.getBankAccountList().get(0).setIsDefault(Boolean.TRUE);
        }
        addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE);
        addDTO.setThirdApprovalUserId(lastUserId);
        addDTO.setThirdApproveTime(approveTime);

        //生成三方生成查询主表数据
        ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(jsonObject, addDTOS,thirdProcessEntity.getBussinessKey());
        //查询三方生成查询
        ApproveTaskInfoEntity taskInfoEntity = approveTaskInfoService.getOne(new LambdaQueryWrapper<ApproveTaskInfoEntity>().eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode())).eq(ApproveTaskInfoEntity::getIsDeleted, false));
        if (ObjectUtil.isNotEmpty(taskInfoEntity)) {
            SupplierEntity supplierEntity = FeignQuery.getById(SupplierEntity.class, taskInfoEntity.getBussinessId());
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                log.warn(CharSequenceUtil.format("供应商【{}】已存在，直接标记消费成功",supplierEntity.getCode()));
                return;
            }
            //判断是否存在三方生成查询数据，存在则删除
            approveTaskInfoService.deleteByThird(taskInfo.getType(),taskInfo.getThirdInstanceId(),taskInfo.getThirdApprovalCode());
        }
        // 第二步：保存供应商信息
        BatchResultDTO batchResultDTO = new BatchResultDTO();
        String reason = "";
        String taskStatus = ApproveTaskStatusEnum.SUCCESS.getCode();
        try {
            //查找创建人
            addCreateUser(createUserId,addDTO);
            //添加供应商
            ValidatorUtil.validateEntity(addDTO);
            batchResultDTO = supplierFeign.add(addDTO);
        } catch (Exception e) {
            taskStatus = ApproveTaskStatusEnum.FAIL.getCode();
            reason = e.getMessage();
        }
        taskInfo.setBussinessKey(thirdProcessEntity.getBussinessKey());
        taskInfo.setBussinessCode(batchResultDTO.getCode());
        taskInfo.setBussinessId(batchResultDTO.getId());
        taskInfo.setHappenTime(LocalDateTime.now());
        taskInfo.setStatus(taskStatus);
        taskInfo.setReason(reason);
        approveTaskInfoService.add(taskInfo);
        //添加三方流程记录
        if (ApproveTaskStatusEnum.SUCCESS.getCode().equals(taskStatus)) {
            thirdProcessManagementService.addOrUpdate(jsonObject,thirdProcessEntity.getSourcePlatform());
        }
    }

    /**
     * 创建人
     * @author will
     * @date 2025/9/29 10:19
     * @param createUserId
     * @return FindUserDTO
     */
    @Override
    public void addCreateUser (String createUserId,SupplierDTO.InsertDTO addDTO) {
        SysUserThirdEntity userByThird = sysUserFeign.getUserByThird(ThirdpartyPlatformEnum.FS.getCode(), createUserId);
        if (Objects.isNull(userByThird)) {
            throw new ServiceException("飞书创建人未绑定，请绑定后重新生成,thirdUserId:"+createUserId);
        }
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(userByThird.getUserId());
        if (ObjUtil.isEmpty(findUserDTO)) {
            throw new ServiceException(ApiError.ERROR_1037, userByThird.getUserId());
        }
        addDTO.setCreateUserId(findUserDTO.getUserId());
        addDTO.setCreateUserName(findUserDTO.getUserName());
        addDTO.setUpdateUserId(findUserDTO.getUserId());
        addDTO.setUpdateUserName(findUserDTO.getUserName());
    }

    /**
     * 数据组装
     * @author will
     * @date 2025/10/23 16:04
     * @param jsonObject
     * @param addDTOS
     * @param bussinessKey
     * @return AddDTO
     */
    public ApproveTaskInfoDTO.AddDTO buildApproveTaskInfo(JSONObject jsonObject, List<ApproveTaskDetailDTO.AddDTO> addDTOS,String bussinessKey) {
        ThirdProcessDefinitionEntity thirdProcessDefinition = thirdProcessDefinitionService.getOne(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getApprovalCode, jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALCODE.getCode())).
                eq(ThirdProcessDefinitionEntity::getIsDeleted, false).eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode()));
        ApproveTaskInfoDTO.AddDTO addDTO = new ApproveTaskInfoDTO.AddDTO();
        addDTO.setDetailList(addDTOS);
        addDTO.setType(thirdProcessDefinition.getType());
        addDTO.setThirdDefinniationName(thirdProcessDefinition.getName());
        addDTO.setThirdInstanceId(jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode()));
        addDTO.setThirdApprovalCode(thirdProcessDefinition.getApprovalCode());
        addDTO.setSourcePlatform(thirdProcessDefinition.getSourcePlatform());
        addDTO.setBussinessKey(bussinessKey);
        return addDTO;
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
                ApproveDTO.CancelProcessDTO cancelProcessDTO = new ApproveDTO.CancelProcessDTO();
                cancelProcessDTO.setBusinessKey(one.getBussinessKey());
                cancelProcessDTO.setId(one.getBussinessId());
                processManagementService.cancelProcessFeign(cancelProcessDTO);
                break;
            case DELETED:
                ApproveDTO.DisApproveDTO disApproveDTO = new ApproveDTO.DisApproveDTO();
                disApproveDTO.setId(one.getBussinessId());
                disApproveDTO.setBusinessKey(one.getBussinessKey());
                processManagementService.disApproveFeign(disApproveDTO);
                break;
            default:
                break;
        }
    }

    /**
     * 原始的回调方法，保持不变。
     */
    public void handleCallback(ApproveTaskInfoEntity entity, String approveStatus, String userId, LocalDateTime approveTime) {
        EndProcessDTO processDTO = new EndProcessDTO();
        processDTO.setBusinessKey(entity.getBussinessKey());
        processDTO.setBusinessId(entity.getBussinessId());
        processDTO.setApproveStatus(ApproveTypeEnum.getByCode(approveStatus));
        // 来自第三方系统的用户ID可能需要转换为您系统内部的用户ID
        SysUserThirdEntity user = sysUserFeign.getUserByThird(ProcessSourcePlatformEnum.FS.getCode().toUpperCase(), userId);
        processDTO.setApproveUserId(user.getUserId());
        processDTO.setApproveTime(approveTime);
        processManagementService.callFeign(entity.getBussinessKey(), processDTO);
    }
}
