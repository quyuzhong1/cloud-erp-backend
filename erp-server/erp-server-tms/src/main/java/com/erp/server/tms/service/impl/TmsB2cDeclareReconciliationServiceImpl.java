package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.LogisticsAddressDTO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDTO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.model.tms.enums.TmsB2cDeclareReconciliationStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.tms.mapper.TmsB2cDeclareReconciliationMapper;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationService;
import com.erp.server.tms.service.TransferLogisticsSupplierService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_B2C_DECLARE_RECONCILIATION;

/**
 * <p>
 * b2c报关对账单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsB2cDeclareReconciliationServiceImpl extends SuperServiceImpl<TmsB2cDeclareReconciliationMapper, TmsB2cDeclareReconciliationEntity> implements TmsB2cDeclareReconciliationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ShopInfoFeign shopInfoFeign;

    @Autowired
    private SysDictFeign sysDictFeign;

    @Autowired
    private ScmTaskFeign scmTaskFeign;

    @Autowired
    private DmpTaskFeign dmpTaskFeign;

    @Autowired
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;

    @Autowired
    private TransferLogisticsSupplierService transferLogisticsSupplierService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsB2cDeclareReconciliationDTO.AddDTO addDTO) {
        TmsB2cDeclareReconciliationEntity tmsB2cDeclareReconciliationEntity = new TmsB2cDeclareReconciliationEntity();
        BeanMapperUtils.copy(addDTO, tmsB2cDeclareReconciliationEntity);

        // 数据处理
        handleData(tmsB2cDeclareReconciliationEntity);

        log.info("开始新增b2c报关对账单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BGZD);
        tmsB2cDeclareReconciliationEntity.setCode(code);
        tmsB2cDeclareReconciliationEntity.setReconciliationDate(LocalDate.now());
        boolean save = super.save(tmsB2cDeclareReconciliationEntity);
        if(!save) {
            throw new ServiceException("b2c报关对账单保存失败");
        }

        //明细添加
        tmsB2cDeclareReconciliationDetailService.update(addDTO.getDetailList(),tmsB2cDeclareReconciliationEntity.getId());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c报关对账单" , tmsB2cDeclareReconciliationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), tmsB2cDeclareReconciliationEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(tmsB2cDeclareReconciliationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsB2cDeclareReconciliationDTO.UpdateDTO updateDTO) {
        TmsB2cDeclareReconciliationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c报关对账单"));

        TmsB2cDeclareReconciliationEntity tmsB2cDeclareReconciliationEntity =  BeanMapperUtils.map(TmsB2cDeclareReconciliationEntity.class, updateDTO);

        // 数据处理
        handleData(tmsB2cDeclareReconciliationEntity);
        log.info("编辑 开始修改b2c报关对账单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(tmsB2cDeclareReconciliationEntity);
        if(!save) {
            throw new ServiceException("b2c报关对账单保存失败");
        }

        //明细更新
        tmsB2cDeclareReconciliationDetailService.update(updateDTO.getDetailList(),tmsB2cDeclareReconciliationEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录b2c报关对账单日志数据，单号：【{}】", tmsB2cDeclareReconciliationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), tmsB2cDeclareReconciliationEntity.getCode(), "b2c报关对账单");
        operateLogService.addModuleOperateLogByObj(old, tmsB2cDeclareReconciliationEntity, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), tmsB2cDeclareReconciliationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> paging(PagingDTO<TmsB2cDeclareReconciliationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsB2cDeclareReconciliationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<TmsB2cDeclareReconciliationDTO.TabListDTO> tabList(PermissionsDTO param) {
        TmsB2cDeclareReconciliationDTO.PagingParamDTO searchParam = new TmsB2cDeclareReconciliationDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<TmsB2cDeclareReconciliationDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        if (CollUtil.isEmpty(list)) {
            list.stream().forEach(obj -> obj.setTabFlagName(ApproveStatusEnum.getName(obj.getTabFlag())));
        }
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(TmsB2cDeclareReconciliationDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new TmsB2cDeclareReconciliationDTO.TabListDTO(status,ApproveStatusEnum.getName(status), 0));
        }
        });
        return list;
    }

    @Override
    public void exportList(TmsB2cDeclareReconciliationDTO.ExportDTO param) {
        downloadTaskFeign.saveDownloadTask("b2c报关对账单导出", EXPORT_TMS_TMS_B2C_DECLARE_RECONCILIATION.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        TmsB2cDeclareReconciliationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到b2c报关对账单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改b2c报关对账单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus(),LocalDate.now());

        log.info("提交 开始启动b2c报关对账单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录b2c报关对账单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "b2c报关对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(TmsB2cDeclareReconciliationDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(TmsB2cDeclareReconciliationDTO.UpdateDTO dto) {
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
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        TmsB2cDeclareReconciliationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "b2c报关对账单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(TmsB2cDeclareReconciliationEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode());
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
        TmsB2cDeclareReconciliationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到b2c报关对账单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "b2c报关对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(TmsB2cDeclareReconciliationEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        TmsB2cDeclareReconciliationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到b2c报关对账单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }

        // 删除主单数据
        log.info("删除 开始删除b2c报关对账单主单数据，id：【{}】", id);
        super.removeById(id);

        //清除明细主表id
        tmsB2cDeclareReconciliationDetailService.cleanDetailMainId(id);

        // 删除日志数据
        log.info("删除 开始删除b2c报关对账单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "b2c报关对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), entity.getCode(), "删除b2c报关对账单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        TmsB2cDeclareReconciliationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到b2c报关对账单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改b2c报关对账单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus(),null);

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "b2c报关对账单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, TmsB2cDeclareReconciliationEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus(),dto.getComment());

        return Boolean.TRUE;
    }

    @Override
    public List<TmsB2cDeclareReconciliationEntity> listEntityByIds(List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.EMPTY_LIST;
        }
        return this.listByIds(idList);
    }

    @Override
    public PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO> exportB2cDeclareReconciliation(PagingDTO<TmsB2cDeclareReconciliationDTO.ExportDTO> dto) {
        Page<TmsB2cDeclareReconciliationDTO.ListDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public TmsB2cDeclareReconciliationDTO.ViewDTO view(String id) {
        TmsB2cDeclareReconciliationEntity tmsB2cDeclareReconciliationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到b2c报关对账单数据"));
        TmsB2cDeclareReconciliationDTO.ViewDTO data = BeanMapperUtils.map(TmsB2cDeclareReconciliationDTO.ViewDTO.class, tmsB2cDeclareReconciliationEntity);
        // 数据填充处理
        fillOne(data);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(TmsB2cDeclareReconciliationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    /**
     * @description: 分页查询调整
     * @author Will
     * @date: 2024/3/26 14:20
     * @param data
     */
    private void fillOne(TmsB2cDeclareReconciliationDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //对账周期
        data.setCycle(StrUtil.format("{}-{}",data.getStartDate(),data.getEndDate()));
        //审核状态名称
        data.setApproveStatusName(data.getApproveStatus().getName());

        //明细
        List<TmsB2cDeclareReconciliationDetailEntity> detailList = tmsB2cDeclareReconciliationDetailService.listMainIdList(Arrays.asList(data.getId()));
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> viewDTOList = BeanMapperUtils.copyList(TmsB2cDeclareReconciliationDetailDTO.ViewDTO.class, detailList);
        //店铺信息
        List<String> shopIdList = viewDTOList.stream().map(TmsB2cDeclareReconciliationDetailDTO.ViewDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = shopInfoFeign.listShopInfoByIds(shopIdList);
        //国家信息
        List<String> countryIdList = viewDTOList.stream().map(TmsB2cDeclareReconciliationDetailDTO.ViewDTO::getCountry).collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIdList);

        //币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(data.getCurrency()));

        //物流费用总金额
        BigDecimal totalCost = detailList.stream().map(obj -> obj.getActualShippingCost().add(obj.getActualDeclareCost()).add(obj.getActualOtherCost())).reduce(BigDecimal.ZERO, BigDecimal::add);
        data.setTotalCost(totalCost);

        for (TmsB2cDeclareReconciliationDetailDTO.ViewDTO viewDTO : viewDTOList) {
            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), viewDTO.getShopId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            viewDTO.setShopName(shopName);
            //国家名称
            String countryName = countryList.stream().filter(obj -> StrUtil.equals(obj.getId(), viewDTO.getCountry()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            viewDTO.setCountryName(countryName);

            //币别符号
            if (CollUtil.isNotEmpty(currencyList)) {
                viewDTO.setCurrencySymbol(currencyList.get(0).getSymbol());
            }
            viewDTO.setStatusName(TmsB2cDeclareReconciliationStatusEnum.getName(viewDTO.getStatus()));

        }
        data.setDetailList(viewDTOList);

    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus,String comment) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(TmsB2cDeclareReconciliationEntity::getId, id)
            .set(TmsB2cDeclareReconciliationEntity::getApproveUserId, userInfo.getUid())
            .set(TmsB2cDeclareReconciliationEntity::getApproveUserName, userInfo.getUserName())
            .set(TmsB2cDeclareReconciliationEntity::getApproveStatus, approveStatus)
            .set(TmsB2cDeclareReconciliationEntity::getApproveDate, LocalDate.now())
            .set(ApproveStatusEnum.REJECT.getCode().equals(approveStatus),TmsB2cDeclareReconciliationEntity::getReason, comment)
            .update();
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(TmsB2cDeclareReconciliationEntity::getId, id)
            .set(TmsB2cDeclareReconciliationEntity::getApproveUserId, "")
            .set(TmsB2cDeclareReconciliationEntity::getApproveUserName, "")
            .set(TmsB2cDeclareReconciliationEntity::getApproveStatus, approveStatus)
            .set(TmsB2cDeclareReconciliationEntity::getApproveDate,null)
            .set(TmsB2cDeclareReconciliationEntity::getSubmitDate,null)
            .update();
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus,LocalDate submitDate) {
        lambdaUpdate().eq(TmsB2cDeclareReconciliationEntity::getId, id)
        .set(TmsB2cDeclareReconciliationEntity::getApproveStatus, approveStatus)
        .set(TmsB2cDeclareReconciliationEntity::getSubmitDate,submitDate)
        .update();
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<TmsB2cDeclareReconciliationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //币别信息
        List<String> currencyIdList = list.stream().map(TmsB2cDeclareReconciliationDTO.ListDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        // 属性赋值
        for(TmsB2cDeclareReconciliationDTO.ListDTO data : list) {
            //审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            //对账周期
            data.setCycle(StrUtil.format("{}-{}",data.getStartDate(),data.getEndDate()));
            //币别符号
            CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(obj -> StrUtil.equals(obj.getId(), data.getCurrency())).findFirst().orElse(null);
            if(ObjectUtil.isNotEmpty(viewDTO)) {
                data.setCurrencySymbol(viewDTO.getSymbol());
                data.setCurrencyName(viewDTO.getName());
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(TmsB2cDeclareReconciliationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        List<TmsB2cDeclareReconciliationDetailEntity> tmsB2cDeclareReconciliationDetailList = tmsB2cDeclareReconciliationDetailService.listMainIdList(Arrays.asList(entity.getId()));
        if (CollectionUtils.isEmpty(tmsB2cDeclareReconciliationDetailList)) {
            throw new ServiceException("对账单下未发现明细不支持提交");
        }
        String soCodes = tmsB2cDeclareReconciliationDetailList.stream()
                .filter(obj -> !StrUtil.equals(obj.getStatus(), TmsB2cDeclareReconciliationStatusEnum.CONFIRM.getCode())
                        && !StrUtil.equals(obj.getStatus(), TmsB2cDeclareReconciliationStatusEnum.DIFF_CONFIRM.getCode()))
                .map(TmsB2cDeclareReconciliationDetailEntity::getSoCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(soCodes)) {
            throw new ServiceException(StrUtil.format("销售订单【{}】对账状态非【已确认/差异确认】不支持提交",soCodes));
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(TmsB2cDeclareReconciliationEntity tmsB2cDeclareReconciliationEntity) {
        //中转报关供应商
        TransferLogisticsSupplierEntity supplierEntity = transferLogisticsSupplierService.getById(tmsB2cDeclareReconciliationEntity.getLogisticsSupplierId());
        if (ObjectUtil.isEmpty(supplierEntity)) {
            return;
        }
        SupplierEntity supplier = scmTaskFeign.getSupplierById(supplierEntity.getSupplierId());
        if (ObjectUtil.isEmpty(supplier)) {
            return;
        }
        tmsB2cDeclareReconciliationEntity.setCurrency(supplier.getPayCurrency());
        //查询汇率
        BigDecimal rate = dmpTaskFeign.getRate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), tmsB2cDeclareReconciliationEntity.getCurrency());
        if(ObjectUtil.isEmpty(rate)){
            log.error("币别【{}】,汇率为空，请维护汇率后再提交",tmsB2cDeclareReconciliationEntity.getCurrency());
            throw new ServiceException("汇率为空，请维护汇率后再提交");
        }
        tmsB2cDeclareReconciliationEntity.setExchangeRate(rate);
    }
}
