package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
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
import com.common.core.utils.ValidatorUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.dto.StocktakingPlanDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.model.wms.entity.StocktakingPlanEntity;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import com.erp.model.wms.enums.StocktakingTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.StocktakingPlanMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘点计划表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
 */
@Slf4j
@Service
public class StocktakingPlanServiceImpl extends SuperServiceImpl<StocktakingPlanMapper, StocktakingPlanEntity> implements StocktakingPlanService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private StocktakingPlanDetailService stocktakingPlanDetailService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private StocktakingTaskService stocktakingTaskService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Override
    public PagingVO<StocktakingPlanDTO.ListDTO> paging(PagingDTO<StocktakingPlanDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<StocktakingPlanDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<StocktakingPlanDTO.TabListDTO> tabList(PermissionsDTO param) {
        StocktakingPlanDTO.PagingParamDTO searchParam = new StocktakingPlanDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<StocktakingPlanDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(StocktakingPlanDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
               list.add(new StocktakingPlanDTO.TabListDTO(status, 0));
            }
        });
        list.add(new StocktakingPlanDTO.TabListDTO("all", list.stream().mapToInt(StocktakingPlanDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(StocktakingPlanDTO.ExportDTO param, HttpServletResponse response) {
        List<StocktakingPlanDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/stocktakingPlan.xlsx";
        String name = "盘点计划单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(StocktakingPlanDTO.AddDTO addDTO) {
        // 数据处理
        handleData(addDTO);
        log.debug("开始新增盘点计划单 param = {}", JSONUtil.toJsonStr(addDTO));
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.STOCKTAKING_PLAN);
        StocktakingPlanEntity stocktakingPlanEntity = new StocktakingPlanEntity(addDTO, code);
        // 保存主数据
        boolean save = super.save(stocktakingPlanEntity);
        if(!save) {
           throw new ServiceException(ApiError.SAVE_BILL_FAIL, "盘点计划");
        }
        // 保存明细数据
        stocktakingPlanDetailService.saveList(addDTO.getDetailList(), stocktakingPlanEntity.getId());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "盘点计划" , stocktakingPlanEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), stocktakingPlanEntity.getId(), "新增单据");
        return stocktakingPlanEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(StocktakingPlanDTO.UpdateDTO updateDTO) {
        StocktakingPlanEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "盘点计划单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        // 数据处理
        handleData(updateDTO);
        // 修改主数据
        StocktakingPlanEntity stocktakingPlanEntity = new StocktakingPlanEntity(updateDTO);
        log.info("编辑 开始修改盘点计划单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(stocktakingPlanEntity);
        if(!save) {
           throw new ServiceException("盘点计划单保存失败");
        }
        // 修改明细数据
        stocktakingPlanDetailService.updateList(updateDTO.getDetailList(), stocktakingPlanEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录盘点计划单日志数据，单号：【{}】", stocktakingPlanEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "盘点计划");
        operateLogService.addModuleOperateLogByObj(old, stocktakingPlanEntity, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), stocktakingPlanEntity.getId(), msg);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        StocktakingPlanEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到盘点计划单数据");
        }
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改盘点计划单状态数据，id=：【{}】", JSONObject.toJSONString(entity.getId()));
        this.updateApproveStatus(entity.getId(), ApproveStatusEnum.APPROVE_ING.getStatus());
        // 启动流程
        log.info("提交 开始启动盘点计划单流程，id=：【{}】", JSONObject.toJSONString(entity.getId()));
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录盘点计划单日志数据，id集合：【{}】", JSONObject.toJSONString(entity));
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    private void validateSubmit(StocktakingPlanEntity entity) {
        // 检验头数据
        // 动销时间校验
        if (!Objects.equals(StocktakingTypeEnum.BY_SKU, entity.getType())) {
           if (ObjectUtil.isEmpty(entity.getStartTime()) || ObjectUtil.isEmpty(entity.getEndTime())) {
                throw new ServiceException("动销时间不能为空");
            }
        }
        // 查询详情
        List<StocktakingPlanDetailEntity> detailList = stocktakingPlanDetailService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException("盘点计划单明细数据为空");
        }
        detailList.stream().forEach( item -> {
            // 校验明细数据
            // 按仓库盘点必须有仓库id
            ValidatorUtil.isNotBlank(item.getWarehouseId(), ApiError.ERROR_99001);
            // 按照仓位盘点必须有仓库id和仓位和库区
            if (Objects.equals(StocktakingTypeEnum.BY_LOCATION, entity.getType())) {
                ValidatorUtil.isNotNull(item.getWarehouseLocation(), ApiError.WAREHOUSE_LOCATION_IS_NULL);
                ValidatorUtil.isNotNull(item.getWarehouseArea(), ApiError.WAREHOUSE_AREA_IS_NULL);
            }
            // 按照sku盘点必须有仓库id和sku
            if (Objects.equals(StocktakingTypeEnum.BY_SKU, entity.getType())) {
                ValidatorUtil.isNotBlank(item.getSkuId(), ApiError.ERROR_95198);
                ValidatorUtil.isNotNull(item.getWarehouseLocation(), ApiError.WAREHOUSE_LOCATION_IS_NULL);
                ValidatorUtil.isNotNull(item.getWarehouseArea(), ApiError.WAREHOUSE_AREA_IS_NULL);
            }
        });
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(StocktakingPlanEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_PLAN.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(StocktakingPlanDTO.AddDTO dto) {
        // 新增
        String id = this.add(dto);
        // 提交
        this.submit(id);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(StocktakingPlanDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            // 审核不通过必须填写审核意见
           throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        StocktakingPlanEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     * @param entity
     * @param dto
     */
    private void approveProcess(StocktakingPlanEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_PLAN.getCode());
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
        if (ObjectUtils.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            this.approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        StocktakingPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到盘点计划单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // 删除盘点任务及明细
        stocktakingTaskService.removeBySourceId(id);
        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(StocktakingPlanEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // 下游盘点计划单全部为未开始时允许反审核
        List<StocktakingTaskEntity> taskEntityList = stocktakingTaskService.listBySourceId(entity.getId());
        if(CollUtil.isEmpty(taskEntityList)){
            return true;
        }
        Optional<StocktakingTaskEntity> first = taskEntityList.stream().filter(item -> !Objects.equals(item.getStatus(), StocktakingStatusEnum.NOT_STARTED)).findFirst();
        if(first.isPresent()){
            throw new ServiceException(ApiError.STOCKTAKING_TASK_STARTED);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        StocktakingPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到盘点计划单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除日志数据
        log.info("删除 开始删除盘点计划单日志数据，id集合：【{}】", JSONObject.toJSONString(id));
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getCode(), "删除盘点计划单数据");
        // 删除明细数据
        stocktakingPlanDetailService.removeByMainId(id);
        // 删除主单数据
        removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id) {
        StocktakingPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到盘点计划单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // 撤销流程
        log.info("撤销 开始修改盘点计划单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(id));
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_PLAN.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    public StocktakingPlanDTO.ViewDTO view(String id) {
        StocktakingPlanEntity stocktakingPlanEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到盘点计划单数据"));
        StocktakingPlanDTO.ViewDTO data = BeanMapperUtils.map(StocktakingPlanDTO.ViewDTO.class, stocktakingPlanEntity);
        // 数据填充处理
        fillOne(data);
        // 查询明细数据
        List<StocktakingPlanDetailDTO.ViewDTO> detailEntityList = stocktakingPlanDetailService.listByMainIdAndType(id);
        if (CollUtil.isNotEmpty(detailEntityList)) {
            List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = detailEntityList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
            List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIdAndCode(paramList);
            List<StocktakingPlanDetailDTO.ViewDTO> detailDTOList = BeanUtil.copyToList(detailEntityList, StocktakingPlanDetailDTO.ViewDTO.class);
            detailDTOList.forEach(viewDTO -> {
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> e.getWarehouseId().equals(viewDTO.getWarehouseId()) && e.getCode().equals(viewDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
                viewDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
            });
            data.setDetailList(detailDTOList);
        }
        return data;
    }

    private void fillOne(StocktakingPlanDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        data.setApproveStatusName(data.getApproveStatus().getName());
        data.setModeName(data.getMode().getName());
        data.setTypeName(data.getType().getName());
        data.setStatusName(data.getStatus().getName());
        data.setSeparateRuleName(data.getSeparateRule().getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, StocktakingPlanEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // 明细
        List<StocktakingPlanDetailEntity> detailEntityList = stocktakingPlanDetailService.listByMainId(entity.getId());
        // 生成盘点任务
        stocktakingTaskService.createTaskList(entity, detailEntityList);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateForStocktakingStatus(String sourceId, StocktakingStatusEnum stocktakingStatus) {
        StocktakingPlanEntity entity = lambdaQuery().eq(StocktakingPlanEntity::getId, sourceId)
                .one();
        if (ObjectUtil.isEmpty(entity) || Objects.equals(entity.getStatus(), stocktakingStatus)){
            return;
        }
        this.lambdaUpdate().eq(StocktakingPlanEntity::getId, sourceId)
                .set(StocktakingPlanEntity::getStatus, stocktakingStatus)
                .update(new StocktakingPlanEntity());
    }

    /**
     * 审核更新审核信息
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(StocktakingPlanEntity::getId, id)
                .set(StocktakingPlanEntity::getApproveUserId, userInfo.getUid())
                .set(StocktakingPlanEntity::getApproveUserName, userInfo.getUserName())
                .set(StocktakingPlanEntity::getApproveStatus, approveStatus)
                .set(StocktakingPlanEntity::getApproveTime, LocalDateTime.now())
                .update(new StocktakingPlanEntity());
    }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().in(StocktakingPlanEntity::getId, id)
            .set(StocktakingPlanEntity::getApproveUserId, "")
            .set(StocktakingPlanEntity::getApproveUserName, "")
            .set(StocktakingPlanEntity::getApproveStatus, approveStatus)
            .set(StocktakingPlanEntity::getApproveTime, null)
            .update();
        }

    /**
    * 更新审核状态
    */
    public void updateApproveStatus(List<String> ids, String approveStatus) {
        lambdaUpdate().in(StocktakingPlanEntity::getId, ids)
        .set(StocktakingPlanEntity::getApproveStatus, approveStatus)
        .set(StocktakingPlanEntity::getSubmitTime, LocalDateTime.now())
        .set(StocktakingPlanEntity::getSubmitUserId, UserContext.getDefaultLoginUser().getUid())
        .set(StocktakingPlanEntity::getSubmitUserName, UserContext.getDefaultLoginUser().getUserName())
        .update(new StocktakingPlanEntity());
    }
    /**
     * 更新审核状态
     */
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(StocktakingPlanEntity::getId, id)
                .set(StocktakingPlanEntity::getApproveStatus, approveStatus)
                .set(StocktakingPlanEntity::getSubmitTime, LocalDateTime.now())
                .set(StocktakingPlanEntity::getSubmitUserId, UserContext.getDefaultLoginUser().getUid())
                .set(StocktakingPlanEntity::getSubmitUserName, UserContext.getDefaultLoginUser().getUserName())
                .update(new StocktakingPlanEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<StocktakingPlanDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 属性赋值
        for(StocktakingPlanDTO.ListDTO data : list) {
            data.setApproveStatusName(data.getApproveStatus().getName());
            data.setModeName(data.getMode().getName());
            data.setTypeName(data.getType().getName());
            data.setStatusName(data.getStatus().getName());
            data.setSeparateRuleName(data.getSeparateRule().getName());
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(StocktakingPlanDTO.CommonDTO dto) {
        // 按照仓库盘点和仓位盘点需要验证动销时间必填
        StocktakingTypeEnum type = dto.getType();
        if (StocktakingTypeEnum.BY_SKU.equals(type)) {
           return;
        }
        ValidatorUtil.isNotNull(dto.getStartTime(), ApiError.TIME_NOT_NULL, "动销开始时间");
        ValidatorUtil.isNotNull(dto.getEndTime(), ApiError.TIME_NOT_NULL, "动销结束时间");
        if (dto.getEndTime().compareTo(dto.getStartTime()) <= 0) {
            throw new ServiceException(ApiError.START_GE_END_ERROR, "动销开始时间", "动销结束时间");
        }
        // 按仓库盘点如果仓库被禁用无法选择
        List<String> disabledWarehouseList = new ArrayList<>();
        dto.getDetailList().forEach(detail -> {
            WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(detail.getWarehouseId());
            if(ObjectUtil.isNotEmpty(updateDTO) && (updateDTO.getDisabled() || !ApproveStatusEnum.APPROVE.getStatus().equals(updateDTO.getApproveStatusCode()))){
                disabledWarehouseList.add(updateDTO.getName());
            }
        });
        if (CollectionUtil.isNotEmpty(disabledWarehouseList)){
            throw new ServiceException(ApiError.WAREHOUSE_DISABLED, JSONUtil.toJsonStr(disabledWarehouseList));
        }
    }

}
