package com.erp.server.fms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogStatusEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.fms.dto.AssetProfitLossDTO;
import com.erp.model.fms.dto.AssetProfitLossDetailDTO;
import com.erp.model.fms.dto.AssetStocktakingPlanDTO;
import com.erp.model.fms.dto.DictBasicDTO;
import com.erp.model.fms.entity.AssetProfitLossDetailEntity;
import com.erp.model.fms.entity.AssetProfitLossEntity;
import com.erp.model.fms.entity.AssetStocktakingPlanEntity;
import com.erp.model.fms.enums.AssetProfitLossTypeEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.fms.mapper.AssetProfitLossMapper;
import com.erp.server.fms.service.AssetProfitLossDetailService;
import com.erp.server.fms.service.AssetProfitLossService;
import com.erp.server.fms.service.AssetStocktakingPlanService;
import com.erp.server.fms.service.DictBasicService;
import com.erp.server.fms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 盘盈盘亏单主表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetProfitLossServiceImpl extends SuperServiceImpl<AssetProfitLossMapper, AssetProfitLossEntity> implements AssetProfitLossService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private DictBasicService dictBasicService;
    @Autowired
    private AssetProfitLossDetailService assetProfitLossDetailService;
    @Autowired
    private AssetStocktakingPlanService assetStocktakingPlanService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetProfitLossDTO.AddDTO addDTO) {
        AssetProfitLossEntity assetProfitLossEntity = new AssetProfitLossEntity();
        BeanMapperUtils.copy(addDTO, assetProfitLossEntity);

        // 数据处理
        handleData(assetProfitLossEntity);

        log.info("开始新增盘盈盘亏单主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.STOCKTAKING_LOSS);
        assetProfitLossEntity.setCode(code);
        boolean save = super.save(assetProfitLossEntity);
        if(!save) {
            throw new ServiceException("盘盈盘亏单主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "盘盈盘亏单主单" , assetProfitLossEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), assetProfitLossEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetProfitLossEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetProfitLossDTO.UpdateDTO addOrUpdateDTO) {
        AssetProfitLossEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "盘盈盘亏单主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetProfitLossEntity assetProfitLossEntity =  BeanMapperUtils.map(AssetProfitLossEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetProfitLossEntity);
        log.info("编辑 开始修改盘盈盘亏单主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetProfitLossEntity);
        if(!save) {
            throw new ServiceException("盘盈盘亏单主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录盘盈盘亏单主单日志数据，单号：【{}】", assetProfitLossEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetProfitLossEntity.getCode(), "盘盈盘亏单主单");
        operateLogService.addModuleOperateLogByObj(old, assetProfitLossEntity, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), assetProfitLossEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AssetProfitLossDTO.ListDTO> paging(PagingDTO<AssetProfitLossDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetProfitLossDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetProfitLossDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetProfitLossDTO.PagingParamDTO searchParam = new AssetProfitLossDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<AssetProfitLossDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 设置tabFlagName
        list.stream().forEach(e -> {
            e.setTabFlagName(ApproveStatusEnum.getTableName(e.getTabFlag()));
        }); 
        
        // 获取状态列表，确保所有状态都存在
        List<String> statusList = ApproveStatusEnum.getStatusList();
        List<String> existStatusList = list.stream().map(AssetProfitLossDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        
        // 不存在的状态赋值为0
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                AssetProfitLossDTO.TabListDTO newTab = new AssetProfitLossDTO.TabListDTO(status, ApproveStatusEnum.getTableName(status), 0);
                list.add(newTab);
            }
        });
        
        // 按照指定顺序排序：待提交、审核中、已审核、不通过
        List<String> orderList = Arrays.asList("waitSubmit", "approveIng", "approve", "reject");
        list.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });
        
        // 计算合计数量并添加"全部"标签
        int totalCount = list.stream().mapToInt(AssetProfitLossDTO.TabListDTO::getCount).sum();
        AssetProfitLossDTO.TabListDTO allTab = new AssetProfitLossDTO.TabListDTO("all", "全部", totalCount);
        list.add(0, allTab); // 添加到第一位
        
        return list;
    }

    @Override
    public void exportList(AssetProfitLossDTO.ExportDTO param, HttpServletResponse response) {
        // 使用异步导出，不再直接导出
        // 该方法保留是为了向后兼容，但实际应该调用 Controller 中的异步导出
        log.warn("exportList 方法已废弃，请使用异步导出方式");
    }

    @Override
    public PagingVO<AssetProfitLossDTO.ListDTO> getAssetProfitLossPageData(PagingDTO<AssetProfitLossDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<AssetProfitLossDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetProfitLossEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到盘盈盘亏单主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改盘盈盘亏单主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动盘盈盘亏单主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录盘盈盘亏单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘盈盘亏单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetProfitLossDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetProfitLossDTO.UpdateDTO dto) {
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
        AssetProfitLossEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘盈盘亏单主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetProfitLossEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(null);
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
        AssetProfitLossEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到盘盈盘亏单主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘盈盘亏单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetProfitLossEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetProfitLossEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到盘盈盘亏单主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除盘盈盘亏单主单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除盘盈盘亏单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘盈盘亏单主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除盘盈盘亏单主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetProfitLossEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到盘盈盘亏单主单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改盘盈盘亏单主单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetProfitLossEntity::getId, id)
            .set(AssetProfitLossEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(AssetProfitLossEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘盈盘亏单主单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetProfitLossEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到盘盈盘亏单主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改盘盈盘亏单主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘盈盘亏单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(null);
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetProfitLossEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public AssetProfitLossDTO.ViewDTO view(String id) {
        AssetProfitLossEntity assetProfitLossEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到盘盈盘亏单主单数据"));
        AssetProfitLossDTO.ViewDTO data = BeanMapperUtils.map(AssetProfitLossDTO.ViewDTO.class, assetProfitLossEntity);
        
        // 数据填充处理
        fillOne(data);
        
        // 查询明细数据
        List<AssetProfitLossDetailEntity> detailEntities = assetProfitLossDetailService.lambdaQuery()
                .eq(AssetProfitLossDetailEntity::getMainId, id)
                .eq(AssetProfitLossDetailEntity::getIsDeleted, false)
                .list();
        
        if (CollUtil.isNotEmpty(detailEntities)) {
            List<AssetProfitLossDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(AssetProfitLossDetailDTO.ViewDTO.class,detailEntities);
            data.setDetailList(detailList);
        }
        
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(AssetProfitLossEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(null);
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetProfitLossDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        
        // 填充单据类型名称
        if (StringUtils.isNotBlank(data.getDocType())) {
            data.setDocTypeName(AssetProfitLossTypeEnum.getName(data.getDocType()));
        }
        
        // 填充盘点方案名称
        if (StringUtils.isNotBlank(data.getPlanId())) {
            AssetStocktakingPlanEntity planEntity = assetStocktakingPlanService.getById(data.getPlanId());
            if (planEntity != null) {
                data.setPlanName(planEntity.getPlanName());
            }
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
        this.lambdaUpdate().eq(AssetProfitLossEntity::getId, id)
            .set(AssetProfitLossEntity::getApproveUserId, userInfo.getUid())
            .set(AssetProfitLossEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetProfitLossEntity::getApproveStatus, approveStatus)
            .set(AssetProfitLossEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetProfitLossEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetProfitLossEntity::getId, id)
            .set(AssetProfitLossEntity::getApproveUserId, "")
            .set(AssetProfitLossEntity::getApproveUserName, "")
            .set(AssetProfitLossEntity::getApproveStatus, approveStatus)
            .set(AssetProfitLossEntity::getApproveTime, null)
            .update(new AssetProfitLossEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetProfitLossEntity::getId, id)
        .set(AssetProfitLossEntity::getApproveStatus, approveStatus)
        .update(new AssetProfitLossEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetProfitLossDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 获取资产类别字典数据
        Map<String, String> assetCategoryMap = new HashMap<>();
        List<String> assetCategoryList = list.stream()
                .map(AssetProfitLossDTO.ListDTO::getAssetCategory)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        if (CollUtil.isNotEmpty(assetCategoryList)) {
            // 调用字典服务获取资产类别名称
            List<DictBasicDTO.DropDownDTO> assetCategory = dictBasicService.listByType("assetCategory", null);
            assetCategoryMap=assetCategory.stream().collect(Collectors.toMap(DictBasicDTO.DropDownDTO::getCode, DictBasicDTO.DropDownDTO::getName,(v1,v2)->v1));
        }
        
        final Map<String, String> finalAssetCategoryMap = assetCategoryMap;
        
        // 属性赋值
        for(AssetProfitLossDTO.ListDTO data : list) {
            // 审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // 作废状态名称
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            
            // 单据类型名称（盘盈/盘亏）
            if (StringUtils.isNotBlank(data.getDocType())) {
                data.setDocTypeName(AssetProfitLossTypeEnum.getName(data.getDocType()));
            }
            
            // 资产类别名称
            if (StringUtils.isNotBlank(data.getAssetCategory())) {
                data.setAssetCategoryName(finalAssetCategoryMap.get(data.getAssetCategory()));
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetProfitLossEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetProfitLossEntity assetProfitLossEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<AssetProfitLossDTO.PushToCardListDTO> getPushToCardList(String id) {
        // 查询主单信息
        AssetProfitLossEntity mainEntity = super.getById(id);
        if (mainEntity == null) {
            throw new ServiceException("盘盈盘亏单不存在");
        }
        
        // 校验单据状态：只有已审核的单据才能下推
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(mainEntity.getApproveStatus().getStatus())) {
            throw new ServiceException("只有已审核的盘盈单才能下推到资产卡片");
        }

        // 校验单据状态：只有已审核的单据才能下推
        if (!AssetProfitLossTypeEnum.PROFIT.getCode().equals(mainEntity.getDocType())) {
            throw new ServiceException("只有盘盈类型才允许下推资产卡片");
        }
        
        // 查询明细列表
        List<AssetProfitLossDetailEntity> detailList = assetProfitLossDetailService.lambdaQuery()
                .eq(AssetProfitLossDetailEntity::getMainId, id)
                .eq(AssetProfitLossDetailEntity::getIsDeleted, false)
                .list();
        
        if (CollUtil.isEmpty(detailList)) {
            log.warn("盘盈盘亏单【{}】没有明细数据", mainEntity.getCode());
            return Collections.emptyList();
        }
        
        // 获取资产类别字典
        Map<String, String> assetCategoryMap = new HashMap<>();
        List<DictBasicDTO.DropDownDTO> assetCategoryList = dictBasicService.listByType("assetCategory","");
        if (CollUtil.isNotEmpty(assetCategoryList)) {
            assetCategoryMap = assetCategoryList.stream()
                    .collect(Collectors.toMap(DictBasicDTO.DropDownDTO::getCode, DictBasicDTO.DropDownDTO::getName, (v1, v2) -> v1));
        }
        
        // 组装返回数据
        List<AssetProfitLossDTO.PushToCardListDTO> resultList = new ArrayList<>(detailList.size());
        Map<String, String> finalAssetCategoryMap = assetCategoryMap;
        
        for (AssetProfitLossDetailEntity detail : detailList) {
            AssetProfitLossDTO.PushToCardListDTO dto = new AssetProfitLossDTO.PushToCardListDTO();
            
            // 主单信息
            dto.setCode(mainEntity.getCode());
            dto.setSourceCode(mainEntity.getSourceCode());
            dto.setDocType(mainEntity.getDocType());
            dto.setDocTypeName(AssetProfitLossTypeEnum.getName(mainEntity.getDocType()));
            dto.setAssetOrgId(mainEntity.getAssetOrgId());
            dto.setAssetOrgName(mainEntity.getAssetOrgName());
            
            // 明细信息
            dto.setDetailId(detail.getId());
            dto.setAssetCategory(detail.getAssetCategory());
            dto.setAssetCategoryName(finalAssetCategoryMap.get(detail.getAssetCategory()));
            dto.setCardCode(detail.getCardCode());
            dto.setAssetName(detail.getAssetName());
            dto.setAssetCode(detail.getAssetCode());
            dto.setUnit(detail.getUnit());
            dto.setQty(detail.getDiffQty());  // 使用差异数量
            dto.setActualLocation(detail.getActualLocation());
            // actualLocationName 需要通过位置服务查询，暂时不设置
            
            // 使用部门和费用项目留空，等待用户选择
            dto.setUseDeptId(null);
            dto.setUseDeptName(null);
            dto.setCostType(null);
            dto.setCostTypeName(null);
            
            resultList.add(dto);
        }
        
        log.info("获取盘盈盘亏单【{}】下推列表成功，明细数量：{}", mainEntity.getCode(), resultList.size());
        return resultList;
    }
}
