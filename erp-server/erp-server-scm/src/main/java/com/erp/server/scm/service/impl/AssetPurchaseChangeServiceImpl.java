package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.enums.MoldInfoTagEnum;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseChangeService;
import com.erp.server.scm.mapper.AssetPurchaseChangeMapper;
import com.erp.server.scm.service.*;
import io.seata.common.util.CollectionUtils;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.jfree.util.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ASSET_PURCHASE_ORDER;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AssetPurchaseChangeServiceImpl extends SuperServiceImpl<AssetPurchaseChangeMapper, AssetPurchaseChangeEntity> implements AssetPurchaseChangeService {

    @Autowired
    private AssetPurchaseOrderService assetPurchaseOrderService;

    @Autowired
    private AssetPurchaseChangeDetailService assetPurchaseChangeDetailService;

    @Autowired
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Autowired
    private AssetNoticeDetailService assetNoticeDetailService;

    @Autowired
    private PurchasePriceService purchasePriceService;

    @Autowired
    private ModuleOperateLogService moduleOperateLogService;

    @Autowired
    private AssetPurchaseOrderSupplierService assetPurchaseOrderSupplierService;

    @Autowired
    private SyncKingdeePurchaseChangeService syncKingdeePurchaseChangeService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private DmpMqFeign dmpMqFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetPurchaseChangeDTO.AddDTO addDTO) {
        AssetPurchaseChangeEntity assetPurchaseChangeEntity = new AssetPurchaseChangeEntity();
        BeanMapperUtils.copy(addDTO, assetPurchaseChangeEntity);

        // 数据处理
        handleAddData(addDTO,assetPurchaseChangeEntity);

        log.info("开始新增");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPOCC);
        assetPurchaseChangeEntity.setCode(code);
        boolean save = super.save(assetPurchaseChangeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        List<AssetPurchaseChangeDetailEntity> detailList = handleDetailData(addDTO, assetPurchaseChangeEntity.getId());
        assetPurchaseChangeDetailService.saveBatch(detailList);

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "模具采购变更单" , assetPurchaseChangeEntity.getCode());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_PURCHASE_CHANGE.getCode(), assetPurchaseChangeEntity.getId(), "新增");

        return new BaseResultDTO.AddDTO(assetPurchaseChangeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetPurchaseChangeDTO.UpdateDTO addOrUpdateDTO) {
        AssetPurchaseChangeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetPurchaseChangeEntity assetPurchaseChangeEntity =  BeanMapperUtils.map(AssetPurchaseChangeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleUpdateData(assetPurchaseChangeEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetPurchaseChangeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        List<AssetPurchaseChangeDetailEntity> assetPurchaseChangeDetailEntity = handleUpdateDetailData(addOrUpdateDTO);
        assetPurchaseChangeDetailService.updateBatchById(assetPurchaseChangeDetailEntity);

        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，单号：【{}】", assetPurchaseChangeEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetPurchaseChangeEntity.getCode(), "模具采购单");
        moduleOperateLogService.addModuleOperateLogByObj(old, assetPurchaseChangeEntity, ModuleTypeEnum.ASSET_PURCHASE_CHANGE.getCode(), assetPurchaseChangeEntity.getId(),"模具采购单", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AssetPurchaseChangeDTO.ListDTO> paging(PagingDTO<AssetPurchaseChangeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetPurchaseChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetPurchaseChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetPurchaseChangeDTO.PagingParamDTO searchParam = new AssetPurchaseChangeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AssetPurchaseChangeDTO.TabListDTO> returnList = new ArrayList<>();

        List<AssetPurchaseChangeDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        // 获取状态列表（all最后统计）
        List<String> statusList = AssetApproveStatusEnum.getStatusList();
        statusList.remove("all");

        // 设置状态名称
        list.forEach(tabListDTO ->
                tabListDTO.setTabFlagName(AssetApproveStatusEnum.getName(tabListDTO.getTabFlag()))
        );

        // 补全缺失的状态（确保顺序与枚举一致）
        List<AssetPurchaseChangeDTO.TabListDTO> finalList = new ArrayList<>();
        statusList.forEach(status -> {
            Optional<AssetPurchaseChangeDTO.TabListDTO> existingItem = list.stream()
                    .filter(item -> item.getTabFlag().equals(status))
                    .findFirst();
            if (existingItem.isPresent()) {
                finalList.add(existingItem.get()); // 已存在的状态直接添加
            } else {
                // 缺失的状态补0
                finalList.add(new AssetPurchaseChangeDTO.TabListDTO(
                        status,
                        AssetApproveStatusEnum.getName(status),
                        0
                ));
            }
        });

        // 添加合计项（all）
        returnList.add(new AssetPurchaseChangeDTO.TabListDTO(
                "all",
                AssetNoticeTabListEnum.ALL.getName(),
                finalList.stream().mapToInt(AssetPurchaseChangeDTO.TabListDTO::getCount).sum()
        ));

        // 按枚举顺序添加所有状态
        returnList.addAll(finalList);

        return returnList;
    }

    @Override
    public void exportList(AssetPurchaseChangeDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("模具采购变更单导出", EXPORT_PLM_ASSET_PURCHASE_ORDER.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetPurchaseChangeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_PURCHASE_CHANGE.getCode(), entity.getId(), "提交");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetPurchaseChangeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetPurchaseChangeDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        AssetPurchaseChangeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getCode())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "", approveType.getName(), dto.getComment());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_PURCHASE_CHANGE.getCode(), entity.getId(), "审核");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetPurchaseChangeEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_PURCHASE_CHANGE.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
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
        AssetPurchaseChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_PURCHASE_CHANGE.getCode(), entity.getId(), "反审核");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetPurchaseChangeEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetPurchaseChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }

        List<AssetPurchaseChangeDetailEntity> list = assetPurchaseChangeDetailService.lambdaQuery()
                .eq(AssetPurchaseChangeDetailEntity::getMainId, id)
                .eq(AssetPurchaseChangeDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();

        if (list.isEmpty()) {
            throw new ServiceException(ApiError.ERROR_95318);
        }

        List<String> collect = list.stream().map(obj -> obj.getId()).collect(Collectors.toList());

        //删除明细
        assetPurchaseChangeDetailService.removeByIds(collect);

        // 删除主单数据
        log.info("删除 开始删除主单数据，id：【{}】", id);
        super.removeById(id);

        // 删除日志数据
        log.info("删除 开始删除日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_PURCHASE_CHANGE.getCode(), entity.getCode(), "删除");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetPurchaseChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetPurchaseChangeEntity::getId, id)
            .set(AssetPurchaseChangeEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(AssetPurchaseChangeEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "", remark);
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_PURCHASE_CHANGE.getCode(), entity.getId(), "作废");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetPurchaseChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_PURCHASE_CHANGE.getCode(), entity.getId(), "取消流程");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_PURCHASE_CHANGE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetPurchaseChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        // 查询变更单明细
        List<AssetPurchaseChangeDetailEntity> changeDetails = assetPurchaseChangeDetailService.lambdaQuery()
                .eq(AssetPurchaseChangeDetailEntity::getMainId, entity.getId())
                .eq(AssetPurchaseChangeDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();

        if (CollectionUtils.isEmpty(changeDetails)) {
            return Boolean.TRUE;
        }

        // 批量更新采购订单明细
        List<String> sourceIds = changeDetails.stream()
                .map(AssetPurchaseChangeDetailEntity::getSourceDetailId)
                .collect(Collectors.toList());

        Map<String, AssetPurchaseChangeDetailEntity> changeDetailMap = changeDetails.stream()
                .collect(Collectors.toMap(AssetPurchaseChangeDetailEntity::getSourceDetailId, Function.identity()));

        // 批量查询
        List<AssetPurchaseOrderDetailEntity> orderDetails = assetPurchaseOrderDetailService.lambdaQuery()
                .in(AssetPurchaseOrderDetailEntity::getId, sourceIds)
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();

        // 批量更新
        boolean result = true;
        for (AssetPurchaseOrderDetailEntity orderDetail : orderDetails) {
            AssetPurchaseChangeDetailEntity changeDetail = changeDetailMap.get(orderDetail.getId());
            if (changeDetail != null) {
                boolean updated = assetPurchaseOrderDetailService.lambdaUpdate()
                        .set(AssetPurchaseOrderDetailEntity::getPurchaseQty, changeDetail.getPurchaseQty())
                        .set(AssetPurchaseOrderDetailEntity::getTaxPrice, changeDetail.getTaxPrice())
                        .set(AssetPurchaseOrderDetailEntity::getTotalAmount, changeDetail.getTotalAmount())
                        .set(AssetPurchaseOrderDetailEntity::getTaxRate, changeDetail.getTaxRate())
                        .eq(AssetPurchaseOrderDetailEntity::getId, orderDetail.getId())
                        .update();

                if (!updated) {
                    throw new ServiceException("采购变更单更新失败");
                }
            }
        }
        //推送金蝶
        DmpPushTaskEntity pushTaskEntity = syncKingdeePurchaseChangeService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
            }
        });
        return result;
    }


    @Override
    public AssetPurchaseChangeDTO.ViewDTO view(String id) {
        AssetPurchaseChangeEntity assetPurchaseChangeEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到数据"));
        AssetPurchaseChangeDTO.ViewDTO data = BeanMapperUtils.map(AssetPurchaseChangeDTO.ViewDTO.class, assetPurchaseChangeEntity);
        // 数据填充处理
        fillOne(data);

        //供应商信息
        AssetPurchaseOrderSupplierDTO.ViewDTO supplierViewDTO = new AssetPurchaseOrderSupplierDTO.ViewDTO();
        AssetPurchaseOrderEntity assetPurchaseOrderEntity = assetPurchaseOrderService.lambdaQuery()
                .eq(AssetPurchaseOrderEntity::getId, assetPurchaseChangeEntity.getSourceId())
                .eq(AssetPurchaseOrderEntity::getIsDeleted, Boolean.FALSE)
                .one();

        if (Objects.isNull(assetPurchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_95307);
        }

        AssetPurchaseOrderSupplierEntity assetPurchaseOrderSupplierEntity = assetPurchaseOrderSupplierService.lambdaQuery()
                .eq(AssetPurchaseOrderSupplierEntity::getAssetPurchaseOrderId,assetPurchaseOrderEntity.getId())
                .eq(AssetPurchaseOrderSupplierEntity::getIsDeleted, Boolean.FALSE)
                .one();

        if (Objects.isNull(assetPurchaseOrderSupplierEntity)) {
            throw new ServiceException(ApiError.ERROR_95317);
        }

        BeanUtils.copyProperties(assetPurchaseOrderSupplierEntity,supplierViewDTO);
        data.setAssetPurchaseOrderSupplierDTO(supplierViewDTO);

        //明细
        List<AssetPurchaseChangeDetailEntity> detailList = assetPurchaseChangeDetailService.lambdaQuery()
                .eq(AssetPurchaseChangeDetailEntity::getMainId, data.getId())
                .eq(AssetPurchaseChangeDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();
        List<AssetPurchaseChangeDetailDTO.ViewDTO> detailViewList = BeanMapperUtils.copyList(AssetPurchaseChangeDetailDTO.ViewDTO.class, detailList);
        fillViewList(detailViewList);
        data.setAssetPurchaseChangeDetailDTOList(detailViewList);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(AssetPurchaseChangeEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.ASSET_PURCHASE_CHANGE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetPurchaseChangeDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(AssetPurchaseChangeEntity::getId, id)
            .set(AssetPurchaseChangeEntity::getApproveUserId, userInfo.getUid())
            .set(AssetPurchaseChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetPurchaseChangeEntity::getApproveStatus, approveStatus)
            .set(AssetPurchaseChangeEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetPurchaseChangeEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetPurchaseChangeEntity::getId, id)
            .set(AssetPurchaseChangeEntity::getApproveUserId, "")
            .set(AssetPurchaseChangeEntity::getApproveUserName, "")
            .set(AssetPurchaseChangeEntity::getApproveStatus, approveStatus)
            .set(AssetPurchaseChangeEntity::getApproveTime, null)
            .update(new AssetPurchaseChangeEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetPurchaseChangeEntity::getId, id)
        .set(AssetPurchaseChangeEntity::getApproveStatus, approveStatus)
        .update(new AssetPurchaseChangeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetPurchaseChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(AssetPurchaseChangeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetPurchaseChangeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleAddData(AssetPurchaseChangeDTO.AddDTO addDTO,AssetPurchaseChangeEntity assetPurchaseChangeEntity) {
        AssetPurchaseOrderEntity assetPurchaseOrderEntity = assetPurchaseOrderService.getById(addDTO.getAssetPurchaseOrderId());
        if (Objects.isNull(assetPurchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_95307);
        }

        if (!ApproveStatusEnum.APPROVE.getStatus().equals(assetPurchaseOrderEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_95310);
        }

        //变更日期
        if (assetPurchaseChangeEntity.getChangeDate() != null && !assetPurchaseChangeEntity.getChangeDate().isBefore(LocalDate.now())) {
            assetPurchaseChangeEntity.setChangeDate(assetPurchaseChangeEntity.getChangeDate());
        } else {
            assetPurchaseChangeEntity.setChangeDate(LocalDate.now());
        }

        //变更人
        if (StringUtils.isNotBlank(addDTO.getChangeUserId())) {
            SysUserDTO sysUserById = sysUserFeign.getSysUserById(addDTO.getChangeUserId());
            if (Objects.nonNull(sysUserById)) {
                assetPurchaseChangeEntity.setChangeUserId(addDTO.getChangeUserId());
                assetPurchaseChangeEntity.setChangeUserName(sysUserById.getUserName());
            } else {
                throw new ServiceException(ApiError.ERROR_1037,addDTO.getChangeUserId());
            }
        }

        //变更部门
        if (StringUtils.isNotBlank(addDTO.getChangeDeptId())) {
            SysDepartmentDTO userDept = sysUserFeign.getUserDeptById(addDTO.getChangeDeptId());
            if (Objects.nonNull(userDept)) {
                assetPurchaseChangeEntity.setChangeDeptId(addDTO.getChangeDeptId());
                assetPurchaseChangeEntity.setChangeDeptName(userDept.getName());
            } else {
                throw new ServiceException(ApiError.ERROR_9029);
            }
        }

        //采购组织
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(addDTO.getPurchaseOrgId());
        if (Objects.nonNull(sysAccountingCompanyEntity)) {
            assetPurchaseChangeEntity.setChangeDeptId(addDTO.getChangeDeptId());
            assetPurchaseChangeEntity.setChangeDeptName(sysAccountingCompanyEntity.getCompanyName());
        } else {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORG_NOT_FOUND);
        }

        assetPurchaseChangeEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());
        assetPurchaseChangeEntity.setOrderType(addDTO.getOrderType());
        assetPurchaseChangeEntity.setChangeReason(addDTO.getChangeReason());
        assetPurchaseChangeEntity.setSourceCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MPO));
        assetPurchaseChangeEntity.setSourceId(addDTO.getSourceId());
        assetPurchaseChangeEntity.setSourceType(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
    }

    private List<AssetPurchaseChangeDetailEntity> handleDetailData(AssetPurchaseChangeDTO.AddDTO addDTO,String assetPurchaseChangeId){

        List<AssetPurchaseChangeDetailEntity> list = BeanMapperUtils.copyList(AssetPurchaseChangeDetailEntity.class, addDTO.getAssetPurchaseChangeDetailDTOList());
        //采购变更id赋值
        list.forEach(obj -> obj.setMainId(assetPurchaseChangeId));

        //校验采购订单是否符合下推条件
        checkPoPushDown(addDTO,list,assetPurchaseChangeId);

        //校验价格
        checkPurchasePrice(addDTO,list,assetPurchaseChangeId);

        return list;
    }

    private void checkPoPushDown (AssetPurchaseChangeDTO.AddDTO addDTO,List<AssetPurchaseChangeDetailEntity> list,String assetPurchaseChangeId) {
        List<String> purchaseOrderDetailIds = list.stream().map(AssetPurchaseChangeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<AssetPurchaseOrderDetailEntity> assetPurchaseOrderDetailEntityList = assetPurchaseOrderDetailService.listByIds(purchaseOrderDetailIds);
        if (CollectionUtils.isEmpty(assetPurchaseOrderDetailEntityList)) {
            throw new ServiceException(ApiError.ERROR_95308);
        }

        //采购订单
        AssetPurchaseOrderEntity assetPurchaseOrderEntity = assetPurchaseOrderService
                .lambdaQuery()
                .eq(AssetPurchaseOrderEntity::getId,assetPurchaseOrderDetailEntityList.get(0).getMainId())
                .eq(AssetPurchaseOrderEntity::getIsDeleted,Boolean.FALSE)
                .one();

        if (Objects.isNull(assetPurchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_95307);
        }

        //明细条数不允许增加
        List<AssetPurchaseOrderDetailEntity> detailList = assetPurchaseOrderDetailService.lambdaQuery()
                .eq(AssetPurchaseOrderDetailEntity::getMainId, addDTO.getSourceId())
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();
        if (addDTO.getAssetPurchaseChangeDetailDTOList().size() > detailList.size()) {
            throw new ServiceException(ApiError.ERROR_95311);
        }

        //申请数量不允许超过剩余数量
        for (AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity : detailList) {
            List<AssetPurchaseOrderDetailEntity> entityList = assetPurchaseOrderDetailService.lambdaQuery()
                    .eq(AssetPurchaseOrderDetailEntity::getId, assetPurchaseOrderDetailEntity.getSourceDetailId())
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                    .list();

            BigDecimal purchaseQtySum = entityList.stream()
                    .filter(obj -> !obj.getId().equals(assetPurchaseOrderDetailEntity.getId()))
                    .map(obj -> obj.getPurchaseQty())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            //如果来源开模通知单
            if (StringUtils.isNotBlank(assetPurchaseOrderDetailEntity.getSourceDetailId())) {
                //获取通知单的申请数量
                AssetNoticeDetailEntity assetNoticeDetailEntity = assetNoticeDetailService.lambdaQuery()
                        .eq(AssetNoticeDetailEntity::getId, assetPurchaseOrderDetailEntity.getSourceDetailId())
                        .eq(AssetNoticeDetailEntity::getIsDeleted, Boolean.FALSE)
                        .one();
                //申请数量-除了当前单的已采购数量 > 传入的采购数量 才可以保存
                if (assetNoticeDetailEntity.getApplyQty().subtract(purchaseQtySum).compareTo(assetPurchaseOrderDetailEntity.getPurchaseQty()) < 0) {
                    throw new ServiceException(ApiError.ERROR_95312);
                }
            } else {
                //如果没有开模通知单，数量比对应采购单明细数量少就可以
                for (AssetPurchaseChangeDetailDTO.AddDTO dto : addDTO.getAssetPurchaseChangeDetailDTOList()) {
                    if (dto.getSourceDetailId().equals(assetPurchaseOrderDetailEntity.getId())
                            && dto.getPurchaseQty().compareTo(assetPurchaseOrderDetailEntity.getPurchaseQty()) > 0) {
                        throw new ServiceException(ApiError.ERROR_95312);
                    }
                }
            }

        }
    }


    public void checkPurchasePrice (AssetPurchaseChangeDTO.AddDTO addDTO,List<AssetPurchaseChangeDetailEntity> list,String purchaseChangeId) {
        //采购价目表查询
        List<PurchasePriceDTO.PriceDTO> convertList = convertAssetPurchaseChangeDTOToPriceDTO(addDTO);

        List<PurchasePriceDTO.PriceDTO> priceDTOList = purchasePriceService.batchGetPurchasePrice(convertList);
        if (priceDTOList.isEmpty()) {
            throw new ServiceException(ApiError.ERROR_98024);
        }

        for (AssetPurchaseChangeDetailEntity assetPurchaseChangeDetailEntity : list) {
            for (PurchasePriceDTO.PriceDTO priceDTO : priceDTOList) {
                if (priceDTO.getSkuId().equals(assetPurchaseChangeDetailEntity.getAssetId())) {
                    assetPurchaseChangeDetailEntity.setTaxPrice(priceDTO.getTaxPrice());
                    assetPurchaseChangeDetailEntity.setTotalAmount(new BigDecimal(priceDTO.getAmount()));
                    assetPurchaseChangeDetailEntity.setTaxRate(priceDTO.getTaxRate());
                }
            }

        }

        AssetPurchaseChangeDetailEntity assetPurchaseChangeDetailEntity = list.stream()
                .filter(obj -> obj.getTotalAmount().compareTo(BigDecimal.ZERO) == 0)
                .findFirst()
                .orElse(null);

        if (Objects.nonNull(assetPurchaseChangeDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95313,assetPurchaseChangeDetailEntity.getAssetCode());
        }

        //获取原明细行数据
        for (AssetPurchaseChangeDetailEntity purchaseChangeDetailEntity : list) {
            AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = assetPurchaseOrderDetailService.lambdaQuery()
                    .eq(AssetPurchaseOrderDetailEntity::getId, purchaseChangeDetailEntity.getSourceDetailId())
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                    .one();

            if (Objects.isNull(assetPurchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95308);
            }

            purchaseChangeDetailEntity.setOldPurchaseQty(assetPurchaseOrderDetailEntity.getPurchaseQty());
            purchaseChangeDetailEntity.setOldTaxPrice(assetPurchaseOrderDetailEntity.getTaxPrice());
            purchaseChangeDetailEntity.setOldTotalAmount(assetPurchaseOrderDetailEntity.getTotalAmount());
            purchaseChangeDetailEntity.setOldTaxRate(assetPurchaseOrderDetailEntity.getTaxRate());
        }
    }

    public List<PurchasePriceDTO.PriceDTO> convertAssetPurchaseChangeDTOToPriceDTO(AssetPurchaseChangeDTO.AddDTO addDTO) {
        List<PurchasePriceDTO.PriceDTO> priceDTOList = new ArrayList<>();

        for (AssetPurchaseChangeDetailDTO.AddDTO dto : addDTO.getAssetPurchaseChangeDetailDTOList()) {
            PurchasePriceDTO.PriceDTO priceDTO = new PurchasePriceDTO.PriceDTO();
            priceDTO.setPurchaseOrgId(addDTO.getPurchaseOrgId());
            priceDTO.setSkuId(dto.getAssetId());
            priceDTO.setSupplierId(addDTO.getSupplierId());
            priceDTO.setQty(dto.getPurchaseQty().intValue());
            priceDTOList.add(priceDTO);
        }

        return priceDTOList;
    }

    private AssetPurchaseChangeEntity handleUpdateData(AssetPurchaseChangeEntity entity){
        if (entity.getChangeDate().compareTo(LocalDate.now()) < 0) {
            new ServiceException(ApiError.ERROR_95314);
        }

        AssetPurchaseChangeEntity assetPurchaseChangeEntity = new AssetPurchaseChangeEntity();
        assetPurchaseChangeEntity.setOrderType(assetPurchaseChangeEntity.getOrderType());

        if (StringUtils.isNotBlank(entity.getChangeReason())) {
            assetPurchaseChangeEntity.setChangeReason(entity.getChangeReason());
        }

        if (StringUtils.isNotBlank(entity.getChangeUserId())) {
            SysUserDTO sysUserDTO = sysUserFeign.getSysUserById(entity.getChangeUserId());
            if (Objects.nonNull(sysUserDTO)) {
                assetPurchaseChangeEntity.setChangeUserName(sysUserDTO.getUserName());
            } else {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
        }

        if (StringUtils.isNotBlank(entity.getChangeDeptId())) {
            List<SysDepartmentEntity> deptList = sysUserFeign.getDeptByIds(Arrays.asList(entity.getChangeUserId()));
            if (deptList.isEmpty()) {
                throw new ServiceException(ApiError.ERROR_9029);
            } else {
                assetPurchaseChangeEntity.setChangeDeptName(deptList.get(0).getName());
            }
        }

        return assetPurchaseChangeEntity;
    }

    public List<AssetPurchaseChangeDetailEntity> handleUpdateDetailData(AssetPurchaseChangeDTO.UpdateDTO updateDTO){
        List<AssetPurchaseChangeDetailEntity> detailList = new ArrayList<>();

        //获取新价格
        List<PurchasePriceDTO.PriceDTO> convertList = convertAssetPurchaseChangeDTOToPriceDTO(updateDTO);
        List<PurchasePriceDTO.PriceDTO> priceDTOList = purchasePriceService.batchGetPurchasePrice(convertList);
        if (priceDTOList.isEmpty()) {
            throw new ServiceException(ApiError.ERROR_98024);
        }

        for (AssetPurchaseChangeDetailDTO.UpdateDTO dto : updateDTO.getAssetPurchaseChangeDetailDTOList()) {
            AssetPurchaseChangeDetailEntity detailEntity = new AssetPurchaseChangeDetailEntity();
            BeanUtils.copyProperties(dto,detailEntity);

            //校验数量不能超过通知单待采购数量
            AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = assetPurchaseOrderDetailService.lambdaQuery()
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                    .eq(AssetPurchaseOrderDetailEntity::getId, dto.getSourceDetailId())
                    .one();

            //排除当前来源的采购订单
            if (StringUtils.isNotBlank(assetPurchaseOrderDetailEntity.getSourceDetailId())) {
                AssetNoticeDetailEntity assetNoticeDetailEntity = assetNoticeDetailService.lambdaQuery()
                        .eq(AssetNoticeDetailEntity::getId, assetPurchaseOrderDetailEntity.getSourceDetailId())
                        .eq(AssetNoticeDetailEntity::getIsDeleted, Boolean.FALSE).one();

                List<AssetPurchaseOrderDetailEntity> assetPurchaseOrderDetailEntityList = assetPurchaseOrderDetailService.lambdaQuery()
                        .eq(AssetPurchaseOrderDetailEntity::getSourceDetailId, assetNoticeDetailEntity.getId())
                        .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                        .list();

                BigDecimal pruchaseQtySum = assetPurchaseOrderDetailEntityList.stream()
                        .map(obj -> obj.getPurchaseQty())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (assetNoticeDetailEntity.getApplyQty().compareTo(dto.getPurchaseQty().add(pruchaseQtySum)) < 0) {
                    throw new ServiceException(ApiError.ERROR_95315);
                }
            } else {
                //如果没有开模通知单，数量比对应采购单明细数量少就可以
                for (AssetPurchaseChangeDetailDTO.UpdateDTO updateDTO1 : updateDTO.getAssetPurchaseChangeDetailDTOList()) {
                    if (updateDTO1.getSourceDetailId().equals(assetPurchaseOrderDetailEntity.getId())
                            && updateDTO1.getPurchaseQty().compareTo(assetPurchaseOrderDetailEntity.getPurchaseQty()) > 0) {
                        throw new ServiceException(ApiError.ERROR_95312);
                    }
                }
            }

            for (PurchasePriceDTO.PriceDTO priceDTO : priceDTOList) {
                if (priceDTO.getSkuId().equals(dto.getAssetId())) {
                    detailEntity.setTaxPrice(priceDTO.getTaxPrice());
                    detailEntity.setTotalAmount(new BigDecimal(priceDTO.getAmount()));
                    detailEntity.setTaxRate(priceDTO.getTaxRate());
                }
            }

            detailList.add(detailEntity);
        }

        AssetPurchaseChangeDetailEntity assetPurchaseChangeDetailEntity = detailList.stream()
                .filter(obj -> obj.getTotalAmount().compareTo(BigDecimal.ZERO) == 0)
                .findFirst()
                .orElse(null);

        if (Objects.nonNull(assetPurchaseChangeDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95313,assetPurchaseChangeDetailEntity.getAssetCode());
        }

        return detailList;
    }

    public List<PurchasePriceDTO.PriceDTO> convertAssetPurchaseChangeDTOToPriceDTO(AssetPurchaseChangeDTO.UpdateDTO updateDTO) {
        List<PurchasePriceDTO.PriceDTO> priceDTOList = new ArrayList<>();

        for (AssetPurchaseChangeDetailDTO.UpdateDTO dto : updateDTO.getAssetPurchaseChangeDetailDTOList()) {
            PurchasePriceDTO.PriceDTO priceDTO = new PurchasePriceDTO.PriceDTO();
            priceDTO.setPurchaseOrgId(updateDTO.getPurchaseOrgId());
            priceDTO.setSkuId(dto.getAssetId());
            priceDTO.setSupplierId(updateDTO.getSupplierId());
            priceDTO.setQty(dto.getPurchaseQty().intValue());
            priceDTOList.add(priceDTO);
        }

        return priceDTOList;
    }

    public void fillViewList(List<AssetPurchaseChangeDetailDTO.ViewDTO> dtoList){

        for (AssetPurchaseChangeDetailDTO.ViewDTO detailDTO : dtoList) {
            List<AssetNoticeDetailDTO.AssetDetailRefSkuDTO> assetNoticeDetailRefSkuDTOS = plmTaskFeign.searchMoldRefSkuByAssetId(detailDTO.getAssetId());
            //关联sku信息
            List<AssetPurchaseChangeDetailDTO.AssetDetailRefSkuDTO> assetDetailRefSkuDTOS = BeanMapperUtils.copyList(AssetPurchaseChangeDetailDTO.AssetDetailRefSkuDTO.class, assetNoticeDetailRefSkuDTOS);
            detailDTO.setAssetDetailRefSkuDTOList(assetDetailRefSkuDTOS);
            detailDTO.setTagName(MoldInfoTagEnum.getName(detailDTO.getTag()));
        }

    }
}
