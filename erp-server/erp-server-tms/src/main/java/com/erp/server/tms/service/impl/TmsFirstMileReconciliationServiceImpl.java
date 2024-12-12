package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DetailReconciliationTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.TmsB2cDeclareReconciliationPayStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.tms.convert.TmsFirstMileReconciliationConverter;
import com.erp.server.tms.mapper.TmsFirstMileReconciliationMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_FIRST_MILE_RECONCILIATION;

/**
 * <p>
 * 头程对账单 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@Service
public class TmsFirstMileReconciliationServiceImpl extends SuperServiceImpl<TmsFirstMileReconciliationMapper, TmsFirstMileReconciliationEntity> implements TmsFirstMileReconciliationService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Lazy
    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    @Lazy
    private FirstMileCostAllocationService firstMileCostAllocationService;
    @Resource
    @Lazy
    private FirstMileSkuCostAllocationService firstMileSkuCostAllocationService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDTO.AddDTO addDTO) {
        TmsFirstMileReconciliationEntity tmsFirstMileReconciliationEntity = new TmsFirstMileReconciliationEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileReconciliationEntity);

        // 数据处理
        handleData(tmsFirstMileReconciliationEntity);

        log.info("开始新增头程对账单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        tmsFirstMileReconciliationEntity.setCode(code);
        boolean save = super.save(tmsFirstMileReconciliationEntity);
        if (!save) {
            throw new ServiceException("头程对账单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程对账单", tmsFirstMileReconciliationEntity.getCode());

        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileReconciliationEntity.getId(), "新增操作");


        return new BaseResultDTO.AddDTO(tmsFirstMileReconciliationEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileReconciliationDTO.UpdateDTO updateDTO) {
        TmsFirstMileReconciliationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "头程对账单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(old.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        TmsFirstMileReconciliationEntity tmsFirstMileReconciliationEntity = BeanMapperUtils.map(TmsFirstMileReconciliationEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileReconciliationEntity);
        log.info("编辑 开始修改头程对账单数据，单号：【{}】", old.getCode());
//        boolean save = super.updateById(tmsFirstMileReconciliationEntity);
//        if (!save) {
//            throw new ServiceException("头程对账单保存失败");
//        }
        if (!Objects.equals(old.getReconciliationMonth(), updateDTO.getReconciliationMonth())) {
            //校验明细物流单在这个月份是否已存在
            List<String> sourceIds = updateDTO.getDetailList().stream().map(TmsFirstMileReconciliationDetailDTO.UpdateDTO::getSourceId).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(sourceIds)) {
                List<TmsFirstMileReconciliationDetailEntity> detailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIds(sourceIds, DetailReconciliationTypeEnum.ACTUAL.getCode());
                List<TmsFirstMileReconciliationDetailEntity> detailEntityList1 = detailEntityList.stream().filter(e -> Objects.nonNull(e) && updateDTO.getReconciliationMonth().equals(e.getReconciliationMonth())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(detailEntityList1)) {
                    List<String> sourceCodes = detailEntityList1.stream().map(TmsFirstMileReconciliationDetailEntity::getSourceCode).distinct().collect(Collectors.toList());
                    throw new ServiceException(ApiError.ERROR_92260, String.join(",", sourceCodes), updateDTO.getReconciliationMonth());
                }
            }

            this.lambdaUpdate().set(TmsFirstMileReconciliationEntity::getReconciliationMonth, updateDTO.getReconciliationMonth())
                    .eq(TmsFirstMileReconciliationEntity::getId, updateDTO.getId()).update();
            old.setReconciliationMonth(updateDTO.getReconciliationMonth());
            String msg = "用户【{}】编辑了【{}】对账月份，由【{}】改为【{}】";
            operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, UserContext.getDefaultLoginUser().getUserName(), old.getCode(), old.getReconciliationMonth(), updateDTO.getReconciliationMonth()),
                    ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), old.getId(), "修改对账单");
        }
        // 修改明细数据（包含增删改）
        tmsFirstMileReconciliationDetailService.update(updateDTO.getDetailList(), old);

        // 记录主单操作日志
        log.info("编辑 开始记录头程对账单日志数据，单号：【{}】", old.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), tmsFirstMileReconciliationEntity.getCode(), "头程对账单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileReconciliationEntity, ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), tmsFirstMileReconciliationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<TmsFirstMileReconciliationDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsFirstMileReconciliationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<TmsFirstMileReconciliationDTO.TabListDTO> tabList(PermissionsDTO param) {
        TmsFirstMileReconciliationDTO.PagingParamDTO searchParam = new TmsFirstMileReconciliationDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<TmsFirstMileReconciliationDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        if (!CollUtil.isEmpty(list)) {
            list.forEach(obj -> obj.setTabFlagName(ApproveStatusEnum.getName(obj.getTabFlag())));
        }
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(TmsFirstMileReconciliationDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new TmsFirstMileReconciliationDTO.TabListDTO(status, ApproveStatusEnum.getName(status), 0));
            }
        });
        return list;
    }

    @Override
    public void exportList(TmsFirstMileReconciliationDTO.ExportDTO param) {
        downloadTaskFeign.saveDownloadTask("头程对账单导出", EXPORT_TMS_TMS_FIRST_MILE_RECONCILIATION.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        TmsFirstMileReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到头程对账单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改头程对账单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // 启动流程（如果需要的话）
        log.info("提交 开始启动头程对账单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录头程对账单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "头程对账单");
        // 此处的需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(TmsFirstMileReconciliationDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(TmsFirstMileReconciliationDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        TmsFirstMileReconciliationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "头程对账单", approveType.getName(), dto.getComment());
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(TmsFirstMileReconciliationEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode());
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        TmsFirstMileReconciliationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 反审核恢复已确认
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList = tmsFirstMileReconciliationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (!CollectionUtils.isEmpty(detailEntityList)) {
            List<String> sourceIds = detailEntityList.stream().map(TmsFirstMileReconciliationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
            tmsFirstMileLogisticService.updateReconciliation(sourceIds, ReconciliationStatusEnum.CONFIRMED.getCode(), id);
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "头程对账单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(TmsFirstMileReconciliationEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        //对账单明细已进行费用分摊 不能进行反审核
        List<FirstMileCostAllocationEntity> entityList = firstMileCostAllocationService.listByReconciliationIds(Collections.singletonList(entity.getId()));
        if (!CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.ERROR_92241);
        }
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList = tmsFirstMileReconciliationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (!CollectionUtils.isEmpty(detailEntityList)) {
            List<String> detailIds = detailEntityList.stream().map(TmsFirstMileReconciliationDetailEntity::getId).distinct().collect(Collectors.toList());
            List<FirstMileSkuCostAllocationEntity> detailList = firstMileSkuCostAllocationService.listByReconciliationDetailIds(detailIds);
            if (!CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_92241);
            }
        }
        // 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        TmsFirstMileReconciliationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getCode(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除明细数据（如果有明细数据的话）
        //清除明细
        tmsFirstMileReconciliationDetailService.checkRemoveByMainId(id);

        // 删除主单数据
        log.info("删除 开始删除头程对账单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除头程对账单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "头程对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getCode(), "删除头程对账单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        TmsFirstMileReconciliationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销流程
        log.info("撤销 开始撤销流程，id：【{}】", id);

        log.info("撤销 开始修改头程对账单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "头程对账单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, TmsFirstMileReconciliationEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus(), dto.getComment());
        // 明细数据处理 上下游数据处理

        // 审核通过修改头程物流单已对账
        if (ApproveStatusEnum.APPROVE.equals(approveStatus)) {
            List<TmsFirstMileReconciliationDetailEntity> detailEntityList = tmsFirstMileReconciliationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            if (!CollectionUtils.isEmpty(detailEntityList)) {
                List<String> sourceIds = detailEntityList.stream().map(TmsFirstMileReconciliationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
                tmsFirstMileLogisticService.updateReconciliation(sourceIds, ReconciliationStatusEnum.RECONCILED.getCode(), entity.getId());
            }
        }
        return Boolean.TRUE;
    }


    @Override
    public TmsFirstMileReconciliationDTO.ViewDTO view(String id) {
        TmsFirstMileReconciliationEntity tmsFirstMileReconciliationEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单数据"));
        TmsFirstMileReconciliationDTO.ViewDTO data = BeanMapperUtils.map(TmsFirstMileReconciliationDTO.ViewDTO.class, tmsFirstMileReconciliationEntity);
        // 数据填充处理
        fillOne(data);
        // 明细数据额外分页
        return data;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(TmsFirstMileReconciliationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        //此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    private void fillOne(TmsFirstMileReconciliationDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        CurrencyDTO.ViewDTO currencyView = null;
        String currency = data.getCurrency();
        if (StringUtils.isNotBlank(data.getCurrency())) {
            currencyView = tmsFirstMileReconciliationDetailService.getCurrencyView(currency);
            data.setCurrencySymbol(null == currencyView ? "" : currencyView.getSymbol());
            data.setCurrencyName(null == currencyView ? "" : currencyView.getName());
        }
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(data.getLogisticsSupplierId());
        if (null != supplierEntity) {
            data.setLogisticsSupplierName(supplierEntity.getSupplierName());
        }

        //审核状态名称
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        //对账周期
        data.setCycle(CharSequenceUtil.format("{}-{}", data.getStartDate(), data.getEndDate()));
        // 明细数据
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList = tmsFirstMileReconciliationDetailService.listByMainIdsBySort(Collections.singletonList(data.getId()));

        List<TmsFirstMileReconciliationDetailDTO.ListDTO> viewDTOList = BeanMapperUtils.copyList(TmsFirstMileReconciliationDetailDTO.ListDTO.class, detailEntityList);

        tmsFirstMileReconciliationDetailService.fillDetailList(viewDTOList, currency, currencyView);

        data.setDetailList(viewDTOList);

    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus, String comment) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(TmsFirstMileReconciliationEntity::getId, id)
                .set(TmsFirstMileReconciliationEntity::getApproveUserId, userInfo.getUid())
                .set(TmsFirstMileReconciliationEntity::getApproveUserName, userInfo.getUserName())
                .set(TmsFirstMileReconciliationEntity::getApproveStatus, approveStatus)
                .set(TmsFirstMileReconciliationEntity::getApproveDate, LocalDate.now())
                .set(ApproveStatusEnum.REJECT.getStatus().equalsIgnoreCase(approveStatus) && StringUtils.isNotBlank(comment),
                        TmsFirstMileReconciliationEntity::getReason, comment)
                .update(new TmsFirstMileReconciliationEntity());
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(TmsFirstMileReconciliationEntity::getId, id)
                .set(TmsFirstMileReconciliationEntity::getApproveUserId, "")
                .set(TmsFirstMileReconciliationEntity::getApproveUserName, "")
                .set(TmsFirstMileReconciliationEntity::getApproveStatus, approveStatus)
                .set(TmsFirstMileReconciliationEntity::getSubmitDate, null)
                .set(TmsFirstMileReconciliationEntity::getApproveDate, null)
                .update(new TmsFirstMileReconciliationEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(TmsFirstMileReconciliationEntity::getId, id)
                .set(TmsFirstMileReconciliationEntity::getApproveStatus, approveStatus)
                .set(ApproveStatusEnum.APPROVE_ING.getCode().equals(approveStatus), TmsFirstMileReconciliationEntity::getSubmitDate, LocalDate.now())
                .set(ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(approveStatus), TmsFirstMileReconciliationEntity::getSubmitDate, null)
                .update(new TmsFirstMileReconciliationEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<? extends TmsFirstMileReconciliationDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> mainIds = list.stream().map(TmsFirstMileReconciliationDTO.ListDTO::getId).collect(Collectors.toList());
        // 明细费用
        List<TmsFirstMileReconciliationDetailEntity> detailEntity = tmsFirstMileReconciliationDetailService.listByMainIds(mainIds);
        Map<String, List<TmsFirstMileReconciliationDetailEntity>> detailGroupMap = detailEntity
                .stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailEntity::getMainId));


        //币别信息
        List<String> currencyIdList = list.stream().map(TmsFirstMileReconciliationDTO.ListDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        List<String> supplierIds = list.stream().map(TmsFirstMileReconciliationDTO.ListDTO::getLogisticsSupplierId).collect(Collectors.toList());
        // 物流商
        Map<String, LogisticsSupplierEntity> supplierMap = logisticsSupplierService.mapByIds(supplierIds);

        // 属性赋值
        for (TmsFirstMileReconciliationDTO.ListDTO data : list) {
            //审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            //对账周期
            data.setCycle(CharSequenceUtil.format("{}-{}", data.getStartDate(), data.getEndDate()));
            //币别符号
            CurrencyDTO.ViewDTO viewDTO = currencyList
                    .stream()
                    .filter(obj -> CharSequenceUtil.equals(obj.getId(), data.getCurrency()))
                    .findFirst()
                    .orElse(null);
            data.setCurrencySymbol(null == viewDTO ? "" : viewDTO.getSymbol());
            data.setCurrencyName(null == viewDTO ? "" : viewDTO.getName());
            if (Objects.nonNull(data.getReconciliationMonth())) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
                data.setReconciliationMonthStr(data.getReconciliationMonth().format(dateTimeFormatter));
            }
            LogisticsSupplierEntity supplierEntity = supplierMap.get(data.getLogisticsSupplierId());
            if (null != supplierEntity) {
                data.setLogisticsSupplierName(supplierEntity.getSupplierName());
            }
            if (Objects.nonNull(data.getReconciliationCount())) {
                if (Objects.equals(0, data.getReconciliationCount())) {
                    data.setReconciliationCountName("");
                } else if (Objects.equals(1, data.getReconciliationCount())) {
                    data.setReconciliationCountName("首次对账");
                } else {
                    data.setReconciliationCountName(data.getReconciliationCount() + "次对账");
                }
            }

            data.setPayStatusName(TmsB2cDeclareReconciliationPayStatusEnum.getName(data.getPayStatus()));
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(TmsFirstMileReconciliationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        // 是否所有明细确认
        List<TmsFirstMileReconciliationDetailEntity> detailList = tmsFirstMileReconciliationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("明细为空无法提交");
        }
        List<String> unConfirmNoList = detailList.stream()
                .filter(e -> ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equalsIgnoreCase(e.getStatus()))
                .map(TmsFirstMileReconciliationDetailEntity::getTransportNo)
                .distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unConfirmNoList)) {
            String msg = CharSequenceUtil.format("运单号{}明细处于待确认，无法提交", unConfirmNoList);
            throw new ServiceException(msg);
        }

    }

    /**
     * 新增修改处理数据
     */
    private void handleData(TmsFirstMileReconciliationEntity tmsFirstMileReconciliationEntity) {
        // 验证数据 & 数据赋值
    }

    @Override
    public List<TmsFirstMileLogisticDTO.WaitSubmitListDTO> listByApproveStatus(String status) {
        List<TmsFirstMileReconciliationEntity> list = lambdaQuery()
                .eq(TmsFirstMileReconciliationEntity::getApproveStatus, status)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(e -> new TmsFirstMileLogisticDTO.WaitSubmitListDTO(
                        e.getId(),
                        e.getCode(),
                        e.getStartDate(),
                        e.getEndDate(),
                        CharSequenceUtil.format("{}-{}", e.getStartDate(), e.getEndDate()),
                        e.getLogisticsSupplierId(),
                        e.getLogisticsSupplierName()
                )).collect(Collectors.toList());

    }

    @Override
    public TmsFirstMileReconciliationEntity getByGenerate(String logisticsSupplierId, LocalDate startDate, LocalDate endDate, String currency) {
        return this.lambdaQuery()
                .eq(TmsFirstMileReconciliationEntity::getLogisticsSupplierId, logisticsSupplierId)
                .eq(TmsFirstMileReconciliationEntity::getStartDate, startDate)
                .eq(TmsFirstMileReconciliationEntity::getEndDate, endDate)
                .eq(TmsFirstMileReconciliationEntity::getCurrency, currency)
                .last(" LIMIT 1")
                .one();
    }

    @Override
    public String checkAndGetSupplier(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ServiceException("id不能为空");
        }
        TmsFirstMileReconciliationEntity reconciliationEntity = this.getById(id);
        if (null == reconciliationEntity) {
            throw new ServiceException("头程对账不存在");
        }
        return reconciliationEntity.getLogisticsSupplierId();
    }

    @Override
    public TmsFirstMileReconciliationEntity findByCycleAndSupplier(String supplier, String currency, LocalDate startDate, LocalDate endDate) {
        return lambdaQuery()
                .eq(TmsFirstMileReconciliationEntity::getLogisticsSupplierId, supplier)
                .eq(TmsFirstMileReconciliationEntity::getCurrency, currency)
                .eq(TmsFirstMileReconciliationEntity::getStartDate, startDate)
                .eq(TmsFirstMileReconciliationEntity::getEndDate, endDate)
                .last(" LIMIT 1 ")
                .one();
    }

    @Override
    public TmsFirstMileReconciliationEntity getByCode(String code) {
        return lambdaQuery()
                .eq(TmsFirstMileReconciliationEntity::getCode, code)
                .last(" LIMIT 1 ")
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReconciliation(TmsFirstMileReconciliationDTO.UpdateDTO updateDTO) {
        TmsFirstMileReconciliationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "头程对账单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(ApproveStatusEnum.getByStatus(old.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        //数据转换
        TmsFirstMileReconciliationEntity tmsFirstMileReconciliationEntity = TmsFirstMileReconciliationConverter.INSTANCE.updateDtoToEntity(updateDTO);
        log.info("编辑 开始修改头程对账单数据，单号：【{}】", old.getCode());
        //校验对账月份
        if (!Objects.equals(old.getReconciliationMonth(), updateDTO.getReconciliationMonth())) {
            //校验明细物流单在这个月份是否已存在
            List<String> sourceIds = updateDTO.getDetailList().stream().map(TmsFirstMileReconciliationDetailDTO.UpdateDTO::getSourceId).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(sourceIds)) {
                List<TmsFirstMileReconciliationDetailEntity> detailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIds(sourceIds, DetailReconciliationTypeEnum.ACTUAL.getCode());
                List<TmsFirstMileReconciliationDetailEntity> detailEntityList1 = detailEntityList.stream().filter(e -> Objects.nonNull(e) && updateDTO.getReconciliationMonth().equals(e.getReconciliationMonth())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(detailEntityList1)) {
                    List<String> sourceCodes = detailEntityList1.stream().map(TmsFirstMileReconciliationDetailEntity::getSourceCode).distinct().collect(Collectors.toList());
                    throw new ServiceException(ApiError.ERROR_92260, String.join(",", sourceCodes), updateDTO.getReconciliationMonth());
                }
            }
            this.lambdaUpdate().set(TmsFirstMileReconciliationEntity::getReconciliationMonth, updateDTO.getReconciliationMonth())
                    .eq(TmsFirstMileReconciliationEntity::getId, updateDTO.getId()).update();
            old.setReconciliationMonth(updateDTO.getReconciliationMonth());
            String msg = "用户【{}】编辑了【{}】对账月份，由【{}】改为【{}】";
            operateLogService.addModuleOperateLog(CharSequenceUtil.format(msg, UserContext.getDefaultLoginUser().getUserName(), old.getCode(), old.getReconciliationMonth(), updateDTO.getReconciliationMonth()),
                    ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), old.getId(), "修改对账单");
        }
        // 修改明细数据（包含增删改）
        tmsFirstMileReconciliationDetailService.updateReconciliationDetail(updateDTO.getDetailList(), old);
        // 记录主单操作日志
        log.info("编辑 开始记录头程对账单日志数据，单号：【{}】", old.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), tmsFirstMileReconciliationEntity.getCode(), "头程对账单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileReconciliationEntity, ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), tmsFirstMileReconciliationEntity.getId(), msg);
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDTO.ListDTO> exportFirstMileReconciliation(PagingDTO<TmsFirstMileReconciliationDTO.ExportDTO> dto) {

        Page<TmsFirstMileReconciliationDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        fillList(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public List<TmsFirstMileLogisticDTO.ReconciliationDTO> listReconciliationAndCostByBillIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return baseMapper.listReconciliationAndCostByBillIds(ids);
    }

    @Override
    public List<TmsFirstMileReconciliationEntity> listbyCodes(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(TmsFirstMileReconciliationEntity::getCode, codeList).list();
    }

    @Override
    public BatchResultDTO updatePayStatus(TmsFirstMileReconciliationDTO.UpdatePayStatusDTO dto, String id) {
        if (CollUtil.isEmpty(dto.getIds())) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        TmsFirstMileReconciliationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单数据"));
        if (entity.getPayStatus().equals(dto.getPayStatus())) {
            throw new ServiceException("修改前后支付状态一致");
        }

        if (SoB2cPayStatusEnum.ENUM_PAID.getCode().equals(dto.getPayStatus())) {
            lambdaUpdate()
                    .set(TmsFirstMileReconciliationEntity::getPayStatus, dto.getPayStatus())
                    .set(TmsFirstMileReconciliationEntity::getPayTime, dto.getPayTime())
                    .eq(TmsFirstMileReconciliationEntity::getId, id)
                    .update();
        } else {
            lambdaUpdate()
                    .set(TmsFirstMileReconciliationEntity::getPayStatus, dto.getPayStatus())
                    .set(TmsFirstMileReconciliationEntity::getPayTime, null)
                    .in(TmsFirstMileReconciliationEntity::getId, id)
                    .update();
        }

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }
}
