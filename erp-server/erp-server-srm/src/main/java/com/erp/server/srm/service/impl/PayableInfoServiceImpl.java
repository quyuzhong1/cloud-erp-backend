package com.erp.server.srm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseOrderTypeEnum;
import com.erp.model.srm.dto.PayableDetailDTO;
import com.erp.model.srm.dto.PayableInfoDTO;
import com.erp.model.srm.entity.PayableDetailEntity;
import com.erp.model.srm.entity.PayableInfoEntity;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.enums.PayableTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.srm.convert.PayableInfoConverter;
import com.erp.server.srm.kingdee.SyncKingdeePayableInfoService;
import com.erp.server.srm.mapper.PayableInfoMapper;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.PayableDetailService;
import com.erp.server.srm.service.PayableInfoService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2025-09-25
 */
@Slf4j
@Service
public class PayableInfoServiceImpl extends SuperServiceImpl<PayableInfoMapper, PayableInfoEntity> implements PayableInfoService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private PayableDetailService payableDetailService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SyncKingdeePayableInfoService syncKingdeePayableInfoService;

    @Resource
    @Lazy
    private PayableInfoService self;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PayableInfoDTO.AddDTO addDTO) {
        PayableInfoEntity payableInfoEntity = new PayableInfoEntity();
        BeanMapperUtils.copy(addDTO, payableInfoEntity);

        // 数据处理
        handleData(payableInfoEntity);

        log.info("开始新增");

        boolean save = super.save(payableInfoEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        //添加明细信息
        payableDetailService.batchAdd(addDTO.getDetailList(),payableInfoEntity.getId());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , payableInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PAYABLE_INFO.getCode(), payableInfoEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(payableInfoEntity.getId(), payableInfoEntity.getCode());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PayableInfoDTO.UpdateDTO addOrUpdateDTO) {
        PayableInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        PayableInfoEntity payableInfoEntity =  BeanMapperUtils.map(PayableInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(payableInfoEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(payableInfoEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，单号：【{}】", payableInfoEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), payableInfoEntity.getCode(), "");
        operateLogService.addModuleOperateLogByObj(old, payableInfoEntity, ModuleTypeEnum.PAYABLE_INFO.getCode(), payableInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<PayableInfoDTO.ListDTO> paging(PagingDTO<PayableInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PayableInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<PayableInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        PayableInfoDTO.PagingParamDTO searchParam = new PayableInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<PayableInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(PayableInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new PayableInfoDTO.TabListDTO(status, 0));
        }
        });
        list.add(new PayableInfoDTO.TabListDTO("all", list.stream().mapToInt(PayableInfoDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id,Boolean isProcess) {
        PayableInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动流程，id=：【{}】", entity.getId());
        if (isProcess) {
            startProcess(entity);
        }
        // 记录操作日志
        log.info("提交 开始记录日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PAYABLE_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(PayableInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId(),Boolean.TRUE);
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(PayableInfoDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId(),Boolean.TRUE);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        PayableInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PAYABLE_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(PayableInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PAYABLE_INFO.getCode());
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
        PayableInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //推送金蝶
        syncApproveInfoToKingdee(entity, SyncOperateEnum.OPERATE_DELETE);

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PAYABLE_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(PayableInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        PayableInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }

        // 删除主单数据
        log.info("删除 开始删除主单数据，id：【{}】", id);
        boolean remove = super.removeById(id);
        if (!remove) {
            throw new ServiceException(ApiError.ERROR_DATA_DELETE);
        }

        //删除明细信息
        Boolean detailRemove = payableDetailService.removeByMainId(id);
        if (!detailRemove) {
            throw new ServiceException(ApiError.ERROR_DATA_DELETE);
        }
        // 删除日志数据
        log.info("删除 开始删除日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PAYABLE_INFO.getCode(), entity.getCode(), "删除数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        PayableInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PAYABLE_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PAYABLE_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, PayableInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        if (CharSequenceUtil.equals(dto.getType(),ApproveTypeEnum.PASS.getStatus())) {
            //推送金蝶
            syncApproveInfoToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE);
        }
        return Boolean.TRUE;
    }

    @Override
    public void generatePayableInfo(PoReconciliationEntity entity,List<PoReconciliationDetailEntity> poReconciliationDetailList) {
        //采购订单id集合
        List<String> poIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getPoId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<PurchaseOrderEntity> poList = FeignQuery.getByIds(PurchaseOrderEntity.class, poIdList);
        Map<String, PurchaseOrderEntity> poMap = CollUtil.isEmpty(poList) ? new HashMap<>() : poList.stream().collect(Collectors.toMap(PurchaseOrderEntity::getId, Function.identity()));
        for (PoReconciliationDetailEntity detailEntity : poReconciliationDetailList) {
            PurchaseOrderEntity purchaseOrderEntity = poMap.get(detailEntity.getPoId());
            if (ObjectUtil.isEmpty(purchaseOrderEntity) || CharSequenceUtil.equals(purchaseOrderEntity.getType(), PurchaseOrderTypeEnum.ENUM_PURCHASE.getCode())) {
                if (CharSequenceUtil.equals(detailEntity.getSourceType(),SourceTypeEnum.PO_INSTOCK.getCode())) {
                    detailEntity.setPayableType(PayableTypeEnum.PURCHASE_INSTOCK.getCode());
                } else if (CharSequenceUtil.equals(detailEntity.getSourceType(),SourceTypeEnum.PO_RETURN.getCode())) {
                    detailEntity.setPayableType(PayableTypeEnum.PURCHASE_RETURN.getCode());
                }
            } else if (CharSequenceUtil.equals(purchaseOrderEntity.getType(), PurchaseOrderTypeEnum.ENUM_SUBCONTRACT.getCode())) {
                if (CharSequenceUtil.equals(detailEntity.getSourceType(),SourceTypeEnum.PO_INSTOCK.getCode())) {
                    detailEntity.setPayableType(PayableTypeEnum.SUBCONTRACT_INSTOCK.getCode());
                } else if (CharSequenceUtil.equals(detailEntity.getSourceType(),SourceTypeEnum.PO_RETURN.getCode())) {
                    detailEntity.setPayableType(PayableTypeEnum.SUBCONTRACT_RETURN.getCode());
                }
            } else {
                log.error("采购订单类型异常，对账单明细id：{}", detailEntity.getId());
                throw new ServiceException("采购订单类型异常，请检查");
            }
        }
        //根据类型分组生成数据
        Map<String, List<PoReconciliationDetailEntity>> payableMap = poReconciliationDetailList.stream().collect(Collectors.groupingBy(obj -> obj.getPayableType()));
        for (Map.Entry<String, List<PoReconciliationDetailEntity>> entry : payableMap.entrySet()) {
            List<PoReconciliationDetailEntity> value = entry.getValue();
            PayableInfoDTO.AddDTO addDTO = PayableInfoConverter.INSTANCE.poReconciliationToPayableEntity(entity);
            addDTO.setType(value.get(0).getPayableType());
            List<PayableDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (PoReconciliationDetailEntity detailEntity : value) {
                PayableDetailDTO.AddDTO detailAddDTO = PayableInfoConverter.INSTANCE.poReconciliationDetailToPayableDetailEntity(detailEntity);
                detailList.add(detailAddDTO);
            }
            addDTO.setDetailList(detailList);
            //新增
            BaseResultDTO.AddDTO result = self.add(addDTO);
           //提交
            self.submit(result.getId(),Boolean.FALSE);
            //审核
            ApproveOneDTO approveOneDTO = new ApproveOneDTO();
            approveOneDTO.setId(result.getId());
            approveOneDTO.setType(ApproveType.PASS);
            self.approve(approveOneDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySourceId(String sourceId) {
        List<PayableInfoEntity> payableInfoList = listBySourceId(sourceId);
        if (CollUtil.isEmpty(payableInfoList)) {
            return;
        }
        long count = payableInfoList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getApproveStatus().getCode(), ApproveStatusEnum.APPROVE.getCode())).count();
        if (count > 0) {
            throw new ServiceException("存在未审核的应付单，无法删除");
        }
        List<String> payableIdList = payableInfoList.stream().map(PayableInfoEntity::getId).distinct().collect(Collectors.toList());
        //反审核
        for (String id : payableIdList) {
            BatchResultDTO disApproveResult = self.disApprove(id);
            if (!disApproveResult.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_DATA_DISAPPROVE);
            }
            BatchResultDTO deleteResult = self.delete(id);
            if (!deleteResult.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_DATA_DELETE);
            }
        }
    }

    @Override
    public Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(PayableInfoEntity::getId,businessId)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId),PayableInfoEntity::getThirdPayableId,syncKingdeeId)
                .update();
    }

    @Override
    public PayableInfoDTO.ViewDTO view(String id) {
        PayableInfoEntity payableInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到数据"));
        PayableInfoDTO.ViewDTO data = BeanMapperUtils.map(PayableInfoDTO.ViewDTO.class, payableInfoEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    /**
     * 根据来源id查询
     * @author will
     * @date 2025/9/30 16:26
     * @param sourceId
     * @return List<PayableInfoEntity>
     */
    private List<PayableInfoEntity> listBySourceId(String sourceId) {
      return   lambdaQuery().eq(PayableInfoEntity::getSourceId,sourceId).list();
    }

    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(PayableInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.PAYABLE_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(PayableInfoDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(PayableInfoEntity::getId, id)
            .set(PayableInfoEntity::getApproveUserId, userInfo.getUid())
            .set(PayableInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(PayableInfoEntity::getApproveStatus, approveStatus)
            .set(PayableInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new PayableInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(PayableInfoEntity::getId, id)
            .set(PayableInfoEntity::getApproveUserId, "")
            .set(PayableInfoEntity::getApproveUserName, "")
            .set(PayableInfoEntity::getApproveStatus, approveStatus)
            .set(PayableInfoEntity::getApproveTime, null)
            .update(new PayableInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(PayableInfoEntity::getId, id)
        .set(PayableInfoEntity::getApproveStatus, approveStatus)
        .update(new PayableInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<PayableInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(PayableInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(PayableInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(PayableInfoEntity payableInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 推送金蝶
     * @author will
     * @date 2025/4/22 16:34
     * @param entity
     * @param syncOperateEnum
     * @return void
     */
    private void syncApproveInfoToKingdee(PayableInfoEntity entity, SyncOperateEnum syncOperateEnum) {

        //应付单明细
        List<PayableDetailEntity> payableDetailList = payableDetailService.listMainIdList(Collections.singletonList(entity.getId()));
        //服务sku
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(noInventorySku) ?
                noInventorySku.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : Collections.emptyList();
        payableDetailList = CollUtil.isNotEmpty(payableDetailList) ? payableDetailList.stream().filter(e -> !ignoreInventorySkuIds.contains(e.getSkuId())).collect(Collectors.toList()) : Collections.emptyList();
        //删除或者非服务sku不为空时推金蝶
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(syncOperateEnum.getCode()) || CollUtil.isNotEmpty(payableDetailList)){
            syncKingdeePayableInfoService.syncDataToKingdee(entity,payableDetailList, syncOperateEnum.getCode());
        }
    }
}
