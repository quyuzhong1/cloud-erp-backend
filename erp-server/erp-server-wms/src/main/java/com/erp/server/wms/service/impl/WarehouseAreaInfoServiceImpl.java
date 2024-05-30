package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.model.wms.entity.WarehouseAreaInfoEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.WarehouseAreaInfoMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseAreaInfoService;
import com.erp.server.wms.service.WarehouseLocationService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 库区管理 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-05-29
 */
@Slf4j
@Service
public class WarehouseAreaInfoServiceImpl extends SuperServiceImpl<WarehouseAreaInfoMapper, WarehouseAreaInfoEntity> implements WarehouseAreaInfoService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(UpdateStateDTO.BatchUpdateDTO dto) {
        update(Wrappers.<WarehouseAreaInfoEntity>lambdaUpdate()
                .set(WarehouseAreaInfoEntity::getDisabled, dto.getDisabled())
                .in(WarehouseAreaInfoEntity::getId, dto.getIds()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        List<WarehouseLocationEntity> warehouseLocations = warehouseLocationService.list(Wrappers.<WarehouseLocationEntity>lambdaQuery()
                .in(WarehouseLocationEntity::getParentId, ids));
        if (!CollectionUtils.isEmpty(warehouseLocations)) {
            List<String> areaIds = warehouseLocations.stream()
                    .map(WarehouseLocationEntity::getParentId)
                    .collect(Collectors.toList());
            List<WarehouseAreaInfoEntity> areaInfos = listByIds(areaIds);
            String codes = areaInfos.stream().map(WarehouseAreaInfoEntity::getCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.POSITION_BINDING_EXIST, codes);
        }
        this.removeByIds(ids);
    }


    @Override
    public WarehouseAreaDTO.View view(String id) {
        WarehouseAreaInfoEntity entity = getById(id);
        return BeanMapperUtils.map(WarehouseAreaDTO.View.class, entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WarehouseAreaDTO.Add dto, String id) {
        existCode(dto.getCode(), id);
        existName(dto.getName(), id);
        WarehouseAreaInfoEntity entity = dto.getWarehouseAreaInfo();
        entity.setId(id);
        updateById(entity);
        operateLogService.addModuleOperateLog(String.format("编辑了库区【%s】", dto.getCode()), ModuleTypeEnum.WAREHOUSE_AREA.getCode(), entity.getId(), "新增操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(WarehouseAreaDTO.Add dto) {
        existCode(dto.getCode(), null);
        existName(dto.getName(), null);
        WarehouseAreaInfoEntity entity = dto.getWarehouseAreaInfo();
        save(entity);
        operateLogService.addModuleOperateLog(String.format("新增了一个库区【%s】", dto.getCode()), ModuleTypeEnum.WAREHOUSE_AREA.getCode(), entity.getId(), "新增操作");
    }

    @Override
    public PagingVO<WarehouseAreaDTO.PagingView> paging(PagingDTO<WarehouseAreaDTO.PagingParam> dto) {
        IPage<WarehouseAreaDTO.PagingView> paging = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(paging);
    }

    public Boolean approveEnd(ApproveOneDTO dto, WarehouseAreaInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus;
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            approveStatus = ApproveStatusEnum.APPROVE;
        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT;
        }
        Boolean result = this.update(Wrappers.<WarehouseAreaInfoEntity>lambdaUpdate()
                .eq(WarehouseAreaInfoEntity::getId, entity.getId())
                .set(WarehouseAreaInfoEntity::getStatus, approveStatus.getStatus()));
        if (Boolean.FALSE.equals(result)) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        return Boolean.TRUE;
    }

    private void startProcess(WarehouseAreaInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.WAREHOUSE_AREA_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        String userId = UserContext.getDefaultLoginUser().getUid();
        startDTO.setUserId(userId);
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    private void approveProcess(WarehouseAreaInfoEntity entity, ApproveOneDTO dto) {
        //无需流程则直接更新状态
        if (ObjectUtil.isNotEmpty(dto.getIsNeedProcess()) && Boolean.FALSE.equals(dto.getIsNeedProcess())) {
            approveEnd(dto, entity);
            return;
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.WAREHOUSE_AREA_INFO.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        String userId = userInfo.getUid();
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(userId)) {
            userId = "0";
        }
        approveDTO.setUserId(userId);
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        if (!approveResult.isSuccess()) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || Boolean.FALSE.equals(data.getIsExistProcess())) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    private void existCode(String code, String id) {
        int count = count(Wrappers.<WarehouseAreaInfoEntity>lambdaQuery()
                .eq(WarehouseAreaInfoEntity::getCode, code)
                .ne(StringUtils.hasText(id), WarehouseAreaInfoEntity::getId, id));
        if (count > 0) {
            throw new ServiceException(ApiError.WAREHOUSE_AREA_EXIST, "编码", code);
        }
    }

    private void existName(String name, String id) {
        int count = count(Wrappers.<WarehouseAreaInfoEntity>lambdaQuery()
                .eq(WarehouseAreaInfoEntity::getName, name)
                .ne(StringUtils.hasText(id), WarehouseAreaInfoEntity::getId, id));
        if (count > 0) {
            throw new ServiceException(ApiError.WAREHOUSE_AREA_EXIST, "名称", name);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id, Boolean isProcess) {
        WarehouseAreaInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST);
        }
        // 待提交或审核不通过并且未作废允许提交
        if (Boolean.FALSE.equals(ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(entity.getStatus())))) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        // 更新单据审核状态
        log.info("提交 开始修改库区状态数据，id：【{}】", id);
        this.update(Wrappers.<WarehouseAreaInfoEntity>lambdaUpdate()
                .eq(WarehouseAreaInfoEntity::getId, id)
                .set(WarehouseAreaInfoEntity::getStatus, ApproveStatusEnum.APPROVE_ING.getCode()));
        log.info("提交 开始启动库区流程，id=：【{}】", entity.getId());
        if (Boolean.TRUE.equals(isProcess)) {
            startProcess(entity);
        }
        // 记录操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "库区");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_AREA.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    public String getCodeById(String id) {
        return getById(id).getCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id) {
        WarehouseAreaInfoEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST);
        }
        // 审核中的数据允许撤销
        if (!Objects.equals(ApproveStatusEnum.APPROVE_ING.getStatus(), entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        revokeProcess(id);
        this.update(Wrappers.<WarehouseAreaInfoEntity>lambdaUpdate()
                .eq(WarehouseAreaInfoEntity::getId, id)
                .set(WarehouseAreaInfoEntity::getStatus, ApproveStatusEnum.WAIT_SUBMIT.getCode()));
        String msg = "库区【{}】撤销流程";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode()), ModuleTypeEnum.WAREHOUSE_AREA.getCode(), id, "撤销流程");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "撤销流程");
    }

    private void revokeProcess(String id) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(id);
        revokeDTO.setBusinessKey(SourceTypeEnum.WAREHOUSE_AREA_INFO.getCode());
        revokeDTO.setUserId(userId);
        workflowFeign.revokeProcess(revokeDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        WarehouseAreaInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        // 调用流程审核
        approveProcess(entity, dto);
        String approveName = ApproveTypeEnum.REJECT.getName();
        if (Objects.nonNull(approveType)) {
            approveName = approveType.getName();
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "库区", approveName, dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_AREA.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(String id) {
        WarehouseAreaInfoEntity entity = getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST);
        }
        // 已审核的数据才可以反审核
        if (!Objects.equals(ApproveStatusEnum.APPROVE.getCode(), entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        this.update(Wrappers.<WarehouseAreaInfoEntity>lambdaUpdate()
                .eq(WarehouseAreaInfoEntity::getId, id)
                .set(WarehouseAreaInfoEntity::getStatus, ApproveStatusEnum.WAIT_SUBMIT.getCode()));
        String msg = "库区【{}】反审核流程";
        operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, entity.getCode()), ModuleTypeEnum.WAREHOUSE_AREA.getCode(), id, "反审核流程");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "反审核流程");
    }
}
