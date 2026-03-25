package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.dto.QcNoticeDetailDTO;
import com.erp.model.wms.entity.QcApplicationDetailEntity;
import com.erp.model.wms.entity.QcApplicationEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.QcApplicationMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_APPLICATION;

/**
 * <p>
 * 质检申请单主表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcApplicationServiceImpl extends SuperServiceImpl<QcApplicationMapper, QcApplicationEntity> implements QcApplicationService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private QcApplicationDetailService qcApplicationDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private QcNoticeService qcNoticeService;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(QcApplicationDTO.AddDTO addDTO) {
        QcApplicationEntity qcApplicationEntity = new QcApplicationEntity();
        BeanMapperUtils.copy(addDTO, qcApplicationEntity);

        // 数据处理
        handleData(qcApplicationEntity);

        log.info("开始新增质检申请单主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZJSQ);
        qcApplicationEntity.setCode(code);
        boolean save = super.save(qcApplicationEntity);
        if(!save) {
            throw new ServiceException("质检申请单主单保存失败");
        }
        //添加质检明细信息
        qcApplicationDetailService.add(addDTO.getDetailList(),qcApplicationEntity);

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "质检申请单主单" , qcApplicationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_APPLICATION.getCode(), qcApplicationEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(qcApplicationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(QcApplicationDTO.UpdateDTO addOrUpdateDTO) {
        QcApplicationEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "质检申请单主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_UPDATE_STATUS_NOT_ALLOWED);
        }
        QcApplicationEntity qcApplicationEntity =  BeanMapperUtils.map(QcApplicationEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(qcApplicationEntity);
        log.info("编辑 开始修改质检申请单主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(qcApplicationEntity);
        if(!save) {
            throw new ServiceException("质检申请单主单保存失败");
        }
        //添加质检明细信息
        qcApplicationDetailService.update(addOrUpdateDTO.getDetailList(),qcApplicationEntity);

        // 记录主单操作日志
        log.info("编辑 开始记录质检申请单主单日志数据，单号：【{}】", qcApplicationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), qcApplicationEntity.getCode(), "质检申请单主单");
        operateLogService.addModuleOperateLogByObj(old, qcApplicationEntity, ModuleTypeEnum.QC_APPLICATION.getCode(), qcApplicationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<QcApplicationDTO.ListDTO> paging(PagingDTO<QcApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<QcApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        QcApplicationDTO.PagingParamDTO searchParam = new QcApplicationDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<QcApplicationDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(QcApplicationDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new QcApplicationDTO.TabListDTO(status,ApproveStatusEnum.getName(status), 0));
            }
        });
        return list;
    }

    @Override
    public Boolean exportList(QcApplicationDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("质检申请导出", EXPORT_WMS_QC_APPLICATION.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class ,timeout = 120000)
    public BatchResultDTO generateQcNotice(QcApplicationDTO.GenerateQcNoticeDTO dto) {
        QcApplicationEntity entity = getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.QC_APPLICATION_NOT_EXIST);
        }
        if (!CharSequenceUtil.equals(entity.getApproveStatus().getStatus(),ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.QC_APPLICATION_NOT_APPROVE_PUSH);
        }
        List<QcApplicationDetailEntity> detailList = qcApplicationDetailService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.QC_APPLICATION_DETAIL_NOT_EXIST);
        }

        QcNoticeDTO.AddDTO addDTO = new QcNoticeDTO.AddDTO();
        addDTO.setQcType(entity.getQcType());
        addDTO.setQcWarehouseId(entity.getWarehouseId());
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getSourceCode());
        addDTO.setSourceType(entity.getSourceType());
        List<QcNoticeDetailDTO.AddDTO> addDetailList = new ArrayList<>();
        for (QcApplicationDetailEntity detailEntity : detailList) {
            QcNoticeDetailDTO.AddDTO  addDetailDTO = new QcNoticeDetailDTO.AddDTO();
            addDetailDTO.setSkuId(detailEntity.getSkuId());
            addDetailDTO.setQcNoticeQty(detailEntity.getQty());
            addDetailDTO.setQcUserId(dto.getQcUserId());
            addDetailDTO.setSourceDetailId(detailEntity.getId());
            addDetailList.add(addDetailDTO);
        }
        addDTO.setDetailList(addDetailList);
        qcNoticeService.add(addDTO);

        //添加下推日志
         String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据生成质检通知单 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检申请单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_APPLICATION.getCode(), entity.getId(), "生成质检通知单操作");

        //更新质检申请单中的期望质检日期
        updatePlanQcDate(entity, dto.getPlanQcDate());

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.GENERATE);
    }

    /**
     * 更新质检申请单中的期望质检日期
     * @param entity 质检申请单主单实体
     * @param planQcDate 期望质检日期
     */
    private void updatePlanQcDate(QcApplicationEntity entity, LocalDate planQcDate) {
        if (ObjectUtil.isEmpty(planQcDate) || planQcDate.equals(entity.getPlanQcDate())) {
            return;
        }
        QcApplicationEntity updateEntity = new QcApplicationEntity();
        updateEntity.setId(entity.getId());
        updateEntity.setPlanQcDate(planQcDate);
        super.updateById(updateEntity);

        //更新日志
        String msg = CharSequenceUtil.format("用户【{}】修改单号为【{}】的【{}】单据期望质检日期，原值：【{}】，新值：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检申请单主单", entity.getPlanQcDate(), planQcDate);
        operateLogService.addModuleOperateLogByObj(entity, updateEntity, ModuleTypeEnum.QC_APPLICATION.getCode(), entity.getId(), msg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class ,timeout = 120000)
    public Boolean generatePoRefQcApplication(ValidList<QcApplicationDTO.GeneratePoRefQcApplicationDTO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<QcApplicationDTO.GeneratePoRefQcApplicationDTO> generateList = list.getList();
        Map<String, List<QcApplicationDTO.GeneratePoRefQcApplicationDTO>> map = generateList.stream().collect(Collectors.groupingBy(QcApplicationDTO.GeneratePoRefQcApplicationDTO::getPoId));

        //查询采购订单信息
        List<String> poIdList = generateList.stream().map(QcApplicationDTO.GeneratePoRefQcApplicationDTO::getPoId).distinct().collect(Collectors.toList());
        List<PurchaseOrderEntity> poList = FeignQuery.getByIds(PurchaseOrderEntity.class, poIdList);

        //查询采购订单明细信息
        List<String> podIdList = generateList.stream().map(QcApplicationDTO.GeneratePoRefQcApplicationDTO::getPodId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> podList = FeignQuery.getByIds(PurchaseOrderDetailEntity.class, podIdList);

        //采购供应商信息
        List<PurchaseOrderSupplierEntity> poSupplierList = FeignQuery.create(PurchaseOrderSupplierEntity.class).in(PurchaseOrderSupplierEntity::getPurchaseOrderId, poIdList).list();

        for ( Map.Entry<String, List<QcApplicationDTO.GeneratePoRefQcApplicationDTO>> entry : map.entrySet()) {
            List<QcApplicationDTO.GeneratePoRefQcApplicationDTO> value = entry.getValue();

            //采购订单
            PurchaseOrderEntity purchaseOrderEntity = poList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entry.getKey())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderEntity)) {
                throw new ServiceException(ApiError.PO_NOT_FOUND);
            }
            if (!CharSequenceUtil.equals(purchaseOrderEntity.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())) {
                throw new ServiceException(ApiError.PO_APPROVED_ONLY_CAN_PUSH_QC_APPLICATION);
            }
            //采购供应商信息
            PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = poSupplierList.stream().filter(obj -> CharSequenceUtil.equals(obj.getPurchaseOrderId(), purchaseOrderEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderSupplierEntity)) {
                throw new ServiceException(ApiError.PO_SUPPLIER_INFO_NOT_FOUND);
            }
            QcApplicationDTO.AddDTO addDTO = new QcApplicationDTO.AddDTO();
            addDTO.setSourceId(purchaseOrderEntity.getId());
            addDTO.setSourceCode(purchaseOrderEntity.getCode());
            addDTO.setSourceType(SourceTypeEnum.PURCHASE_ORDER.getCode());
            addDTO.setPlanQcDate(value.get(0).getPlanQcDate());
            addDTO.setWarehouseId(purchaseOrderEntity.getDeliveryWarehouseId());

            List<QcApplicationDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (QcApplicationDTO.GeneratePoRefQcApplicationDTO refDTO : value) {
                //采购订单明细
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = podList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entry.getKey())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                    throw new ServiceException(ApiError.PO_DETAIL_NOT_FOUND);
                }
                QcApplicationDetailDTO.AddDTO detailDTO = new QcApplicationDetailDTO.AddDTO();
                detailDTO.setQty(refDTO.getQty());
                detailDTO.setSourceDetailId(purchaseOrderDetailEntity.getId());
                detailDTO.setSkuId(purchaseOrderDetailEntity.getSkuId());
                detailDTO.setSupplierId(purchaseOrderSupplierEntity.getSupplierId());
                detailList.add(detailDTO);
            }
            addDTO.setDetailList(detailList);
             this.add(addDTO);
        }
        return Boolean.TRUE;
    }


    @Override
    public List<QcApplicationDTO.ListPushQcNoticeDTO> listPushQcNotice(List<String> ids) {
        List<QcApplicationEntity> qcApplicationList = this.listByIds(ids);
        if (CollUtil.isEmpty(qcApplicationList)) {
            throw new ServiceException(ApiError.QC_APPLICATION_NOT_EXIST);
        }
        List<String> warehouseIdList = qcApplicationList.stream().map(QcApplicationEntity::getWarehouseId).distinct().collect(Collectors.toList());
        Map<String, WarehouseEntity> warehouseMap = warehouseService.mapByIds(warehouseIdList);


        List<QcApplicationDTO.ListPushQcNoticeDTO> resultList = new ArrayList<>();
        for (QcApplicationEntity entity :  qcApplicationList) {
            QcApplicationDTO.ListPushQcNoticeDTO dto = new QcApplicationDTO.ListPushQcNoticeDTO();
            dto.setId(entity.getId());
            dto.setCode(entity.getCode());
            dto.setSourceCode(entity.getSourceCode());
            dto.setQcType(entity.getQcType());
            dto.setQcTypeName(QcTypeEnum.getByCode(entity.getQcType()));
            dto.setWarehouseId(entity.getWarehouseId());

            //仓库
            WarehouseEntity warehouseEntity = warehouseMap.get(entity.getWarehouseId());
            if (ObjectUtil.isEmpty(warehouseEntity)) {
                throw new ServiceException(ApiError.WH_NOT_EXIST_OR_NO_PERMISSION);
            }
            dto.setWarehouseName(warehouseEntity.getName());
            dto.setPlanQcDate(entity.getPlanQcDate());
            resultList.add(dto);
        }
        return resultList;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        QcApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到质检申请单主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改质检申请单主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动质检申请单主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录质检申请单主单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检申请单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_APPLICATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        QcApplicationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.WF_APPROVAL_DELETE_FORBIDDEN);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检申请单主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_APPLICATION.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(QcApplicationEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.QC_APPLICATION.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.BILL_APPROVE_FAILED,"质检申请单");
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        QcApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检申请单主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检申请单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_APPLICATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(QcApplicationEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        QcApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检申请单主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }

        // 删除主单数据
        log.info("删除 开始删除质检申请单主单主单数据，id：【{}】", id);
        super.removeById(id);

        //删除明细数据
        log.info("删除 开始删除质检申请单主单明细数据，id：【{}】", id);
        qcApplicationDetailService.removeByMainId(id);

        // 删除日志数据
        log.info("删除 开始删除质检申请单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检申请单主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除质检申请单主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        QcApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检申请单主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改质检申请单主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "质检申请单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_APPLICATION.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setSourcePlatform(dto.getSourcePlatform());
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.QC_APPLICATION.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        revokeDTO.setSourcePlatform(dto.getSourcePlatform());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, QcApplicationEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        return Boolean.TRUE;
    }

    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(QcApplicationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.QC_APPLICATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }


    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //创建人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        this.lambdaUpdate().eq(QcApplicationEntity::getId, id)
            .set(QcApplicationEntity::getApproveUserId, userInfo.getUid())
            .set(QcApplicationEntity::getApproveStatus, approveStatus)
            .set(QcApplicationEntity::getApproveTime, LocalDateTime.now())
            .update(new QcApplicationEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(QcApplicationEntity::getId, id)
            .set(QcApplicationEntity::getApproveUserId, "")
            .set(QcApplicationEntity::getApproveStatus, approveStatus)
            .set(QcApplicationEntity::getApproveTime, null)
            .update(new QcApplicationEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(QcApplicationEntity::getId, id)
        .set(QcApplicationEntity::getApproveStatus, approveStatus)
        .update(new QcApplicationEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(QcApplicationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(QcApplicationEntity qcApplicationEntity) {
        if (qcApplicationEntity.getPlanQcDate().isBefore(LocalDate.now())) {
            throw new ServiceException(ApiError.QC_APPLICATION_PLAN_QC_DATE_NOT_BEFORE_NOW);
        }

    }

    @Override
    public QcApplicationDTO.ViewDTO view(String id) {
    QcApplicationEntity qcApplicationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到质检申请单主单数据"));
    QcApplicationDTO.ViewDTO data = BeanMapperUtils.map(QcApplicationDTO.ViewDTO.class, qcApplicationEntity);
    // 数据填充处理
    fillOne(data);
    return data;
    }

    /**
     * 数据处理
     * @author will
     * @date 2026/3/24 15:31
     * @param data
     */
    private void fillOne(QcApplicationDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
        //来源单号
        data.setSourceTypeName(SourceTypeEnum.getName(data.getSourceType()));
         //审核状态
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        //质检类型
        data.setQcTypeName(QcTypeEnum.getByCode(data.getQcType()));

        //明细
        List<QcApplicationDetailEntity> list = qcApplicationDetailService.listByMainId(data.getId());
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.QC_APPLICATION_DETAIL_NOT_EXIST);
        }
        //SKU
        List<String> skuIdList = list.stream().map(QcApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuPurchaseByIds(skuIdList);
        Map<String, SkuVO> skuMap = CollUtil.isEmpty(skuVOS) ? new HashMap<>() : skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, obj -> obj));
        //供应商
        List<String> supplierIdList = list.stream().map(QcApplicationDetailEntity::getSupplierId).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierList = FeignQuery.getByIds(SupplierEntity.class, supplierIdList);
        Map<String, String> supplierNameMap = CollUtil.isEmpty(supplierList)  ?   new HashMap<>() :  supplierList.stream().collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getName));

        List<QcApplicationDetailDTO.ViewDTO> detailList = new ArrayList<>();
        for (QcApplicationDetailEntity detailEntity : list) {
            QcApplicationDetailDTO.ViewDTO viewDetailEntity = BeanUtil.toBean(detailEntity, QcApplicationDetailDTO.ViewDTO.class);
            //sku信息
            SkuVO skuVO = skuMap.get(detailEntity.getSkuId());
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDetailEntity.setProductName(skuVO.getSkuName());
                viewDetailEntity.setEan(skuVO.getEan());
            }
            //供应商名称
            viewDetailEntity.setSupplierName(supplierNameMap.get(detailEntity.getSupplierId()));
            detailList.add(viewDetailEntity);
        }
        data.setDetailList(detailList);
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<QcApplicationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        //仓库
       List<String> warehouseIdList = list.stream().map(QcApplicationDTO.ListDTO::getWarehouseId).distinct().collect(Collectors.toList());
       Map<String, WarehouseEntity> warehouseEntityMap = warehouseService.mapByIds(warehouseIdList);
        //供应商
       List<String> supplierIdList = list.stream().map(QcApplicationDTO.ListDTO::getSupplierId).distinct().collect(Collectors.toList());
       List<SupplierEntity> supplierList = FeignQuery.getByIds(SupplierEntity.class, supplierIdList);
       Map<String, String> supplierNameMap = supplierList.stream().collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getName));

       // 属性赋值
        for(QcApplicationDTO.ListDTO data : list) {
            //审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            //质检类型名称
            data.setQcTypeName(QcTypeEnum.getByCode(data.getQcType()));
            //质检状态名称
             data.setQcStatusName(QcTypeEnum.getByCode(data.getQcStatus()));
            //质检结果名称
            data.setQcResultName(QcResultEnum.getByCode(data.getQcResult()));
            // 仓库名称
            WarehouseEntity warehouseEntity = warehouseEntityMap.get(data.getWarehouseId());
            if (ObjectUtil.isNotEmpty(warehouseEntity)) {
                data.setWarehouseName(warehouseEntity.getName());
            }
            // 供应商名称
            data.setSupplierName(supplierNameMap.get(data.getSupplierId()));

        }
   }
}
