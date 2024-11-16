package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.RedisUtil;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.StocktakingTaskDetailExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.listener.StocktakingTaskExcelListener;
import com.erp.server.wms.mapper.StocktakingTaskMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘点任务表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
@Slf4j
public class StocktakingTaskServiceImpl extends SuperServiceImpl<StocktakingTaskMapper, StocktakingTaskEntity> implements StocktakingTaskService {


    @Resource
    private StocktakingTaskDetailService stocktakingTaskDetailService;
    @Resource
    private StocktakingTaskUserService stocktakingTaskUserService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private StocktakingPlanService stocktakingPlanService;

    /**
     * tab list
     *
     * @param dto
     * @return
     */
    @Override
    public List<StocktakingTaskDTO.TabDTO> tabList(PermissionsDTO dto) {
        List<StocktakingTaskDTO.TabDTO> tabList = new ArrayList<>(4);
        List<StocktakingTaskDTO.TabDTO> dbList = baseMapper.tabList(dto.getPermissionSql());
        StocktakingTaskDTO.TabDTO all = new StocktakingTaskDTO.TabDTO();
        int allCount = dbList.stream().mapToInt(StocktakingTaskDTO.TabDTO::getCount).sum();
        all.setTabFlag(WmsConstant.ALL);
        all.setCount(allCount);
        tabList.add(all);
        for (ApproveStatusEnum approveStatus : ApproveStatusEnum.values()) {
            String tabFlag = approveStatus.getStatus();
            StocktakingTaskDTO.TabDTO tabDTO = new StocktakingTaskDTO.TabDTO();
            tabDTO.setTabFlag(tabFlag);
            int count = dbList.stream().filter(a -> tabFlag.equals(a.getTabFlag())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
            tabDTO.setCount(count);
            tabList.add(tabDTO);
        }
        return tabList;
    }

    /**
     * 分页获取
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<StocktakingTaskDTO.PagingViewDTO> paging(PagingDTO<StocktakingTaskDTO.PagingParamDTO> dto) {
        StocktakingTaskDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        String tabFlag = params.getTabFlag();
        //对应tab的状态
        List<String> tabList = new ArrayList<>(1);
        if (!WmsConstant.ALL.equals(tabFlag)) {
            tabList.add(tabFlag);
        }
        List<String> mainIdList = new ArrayList<>();
        //仓库id
        String warehouseId = params.getWarehouseId();
        if (CharSequenceUtil.isNotBlank(warehouseId)) {
            List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listByWarehouseIds(Arrays.asList(warehouseId));
            List<String> mainIds = taskDetailList.stream().map(StocktakingTaskDetailEntity::getMainId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(mainIds)) {
                return new PagingVO<>(new Page<>());
            }
            mainIdList.addAll(mainIds);
        }
        IPage pageData = baseMapper.paging(query, params, tabList, mainIdList);
        List<StocktakingTaskDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }


    /**
     * 填充数据
     *
     * @param list
     */
    private void fillDb(List<StocktakingTaskDTO.PagingViewDTO> list) {
        List<String> idList = list.stream().map(StocktakingTaskDTO.PagingViewDTO::getId).collect(Collectors.toList());
        //获取到详情
        List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listBaseByMainIds(idList);
        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseBySourceIdList(idList);
        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(idList);
        for (StocktakingTaskDTO.PagingViewDTO item : list) {
            String id = item.getId();
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            String approveStatusName = approveStatus.getName();
            item.setApproveStatusName(approveStatusName);
            List<String> curApproveName = processTaskManagementEntities.stream().filter(req -> req.getBusinessId().equals(item.getId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
            String waitApproveUserName = StringUtils.join(curApproveName, ",");
            item.setWaitApproveUserName(waitApproveUserName);
            //分担规则
            SeparateRuleEnum separateRule = item.getSeparateRule();
            item.setSeparateRuleName(Objects.nonNull(separateRule) ? separateRule.getName() : "");
            //盘点方式
            StocktakingModeEnum stocktakingMode = item.getStocktakingMode();
            item.setStocktakingModeName(Objects.nonNull(stocktakingMode) ? stocktakingMode.getName() : "");
            //盘点类型
            StocktakingTypeEnum itemStocktakingType = item.getStocktakingType();
            item.setStocktakingTypeName(Objects.nonNull(itemStocktakingType) ? itemStocktakingType.getName() : "");
            //盘点状态
            StocktakingStatusEnum stocktakingStatus = item.getStocktakingStatus();
            item.setStocktakingStatusName(Objects.nonNull(stocktakingStatus) ? stocktakingStatus.getName() : "");
            //盘点人
            String stocktakingUserName = taskUserList.stream().filter(t -> id.equals(t.getSourceId())).
                    map(StocktakingTaskUserEntity::getUserName).collect(Collectors.joining(","));
            item.setStocktakingUserName(stocktakingUserName);


            //仓库
            String warehouseName = taskDetailList.stream().filter(d -> id.equals(d.getMainId())).
                    map(StocktakingTaskDetailEntity::getWarehouseName).distinct().collect(Collectors.joining(","));
            item.setWarehouseName(warehouseName);

            //sku 统计数
            Integer skuCount = Math.toIntExact(taskDetailList.stream().filter(d -> id.equals(d.getMainId())).
                    map(StocktakingTaskDetailEntity::getSkuId).distinct().count());

            item.setSkuCount(skuCount);

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id) {
        StocktakingTaskEntity task = this.getById(id);
        if (Objects.isNull(task)) {
            throw new ServiceException("盘点任务不存在");
        }
        String code = task.getCode();
        ApproveStatusEnum approveStatus = task.getApproveStatus();
        //待审核
        ApproveStatusEnum waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT;
        //审核不通过
        ApproveStatusEnum rejectStatus = ApproveStatusEnum.REJECT;
        //审核中
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        List<ApproveStatusEnum> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        if (!statusList.contains(approveStatus)) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }

        List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listBaseByMainIds(Arrays.asList(id));
        long zeroCount = taskDetailList.stream().filter(d -> d.getQty() < 0).count();
        if (zeroCount > 0) {
            throw new ServiceException("盘点数量不能为负数");
        }
        //启动审核流程
        startProcess(task);
        //待提交的
        List<Pair<String, String>> pairList = Lists.newArrayList(task).stream().filter(t -> waitSubmitStatus.equals(t.getApproveStatus())).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = Lists.newArrayList(task).stream().filter(t -> rejectStatus.equals(t.getApproveStatus())).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        //改状态
        Boolean result = this.updateStatus(task, ingStatus, StocktakingStatusEnum.IN_PROGRESS);
        // 查询盘点计划下其他单据是否全部审核完成
        List<StocktakingTaskEntity> stocktakingTaskEntities = listBySourceId(task.getSourceId());
        // 全部审核完成 修改盘点计划单据状态
        StocktakingStatusEnum stocktakingStatus = isAllMatchStocktakingStatus(stocktakingTaskEntities);
        if (ObjectUtil.isNotEmpty(stocktakingStatus)) {
            stocktakingPlanService.updateForStocktakingStatus(task.getSourceId(), stocktakingStatus);
        }
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", BillApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.STOCKTAKING_TASK.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.STOCKTAKING_TASK.getCode(), rejectPairList, "状态变更");


        }
        return BatchResultDTO.success(task.getId(), code, OperationTypeEnum.SUBMIT);

    }

    /**
     * 启动流程
     *
     * @param task
     */
    public void startProcess(StocktakingTaskEntity task) {
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        String userId = UserContext.getDefaultLoginUser().getUid();
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(task.getId());
        startDTO.setBusinessCode(task.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_TASK.getCode());
        startDTO.setBusinessName(task.getCode());
        startDTO.setUserId(userId);
        startDTO.setVariablesMap(BeanUtil.beanToMap(task));
        resultList.add(startDTO);
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * 更改状态
     *
     * @param task
     * @param approveStatus 审核状态
     * @return billStatus 单据状态
     */
    public Boolean updateStatus(StocktakingTaskEntity task, ApproveStatusEnum approveStatus, StocktakingStatusEnum billStatus) {
        if (Objects.nonNull(task)) {
            task.setApproveStatus(approveStatus);
            if (Objects.nonNull(billStatus)) {
                task.setStatus(billStatus);
            }

            return this.updateById(task);
        }
        return Boolean.TRUE;
    }

    @Override
    public StocktakingTaskDTO.ViewDTO view(String id) {
        StocktakingTaskDTO.ViewDTO view = baseMapper.getViewById(id);
        if (Objects.isNull(view)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        //盘点方式
        StocktakingModeEnum stocktakingMode = view.getStocktakingMode();
        view.setStocktakingModeName(stocktakingMode.getName());
        //盘点类型
        StocktakingTypeEnum stocktakingType = view.getStocktakingType();
        view.setStocktakingTypeName(stocktakingType.getName());
        //审核状态
        ApproveStatusEnum approveStatus = view.getApproveStatus();
        view.setApproveStatusName(approveStatus.getName());

        //分单规则
        SeparateRuleEnum separateRule = view.getSeparateRule();
        view.setSeparateRuleName(separateRule.getName());
        //盘点状态
        StocktakingStatusEnum stocktakingStatus = view.getStocktakingStatus();
        view.setStocktakingStatusName(stocktakingStatus.getName());

        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseBySourceIdList(Arrays.asList(id));
        //盘点人
        String stocktakingUserName = taskUserList.stream().filter(t -> id.equals(t.getSourceId())).
                map(StocktakingTaskUserEntity::getUserName).collect(Collectors.joining(","));
        view.setStocktakingUserName(stocktakingUserName);
        //获取到对应的详情
        List<StocktakingTaskDetailDTO.ViewDTO> detailList = stocktakingTaskDetailService.listByMainId(id);
        view.setDetailList(detailList);
        return view;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(String id, ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StringUtils.isEmpty(dto.getComment())) {
            // 审核不通过必须填写审核意见
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        StocktakingTaskEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("未找到盘点任务单");
        }
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        // 审核中的数据允许审核
        if (!Objects.equals(ingStatus, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        // 调用流程审核
        approveProcess(entity, dto);

        //添加日志
        List<Pair<String, String>> pairList = Lists.newArrayList(new Pair<>(entity.getId(), entity.getCode()));
        String comment = dto.getComment();
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个盘点任务单", approveType.getName()).concat("【%s】").concat(CharSequenceUtil.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.STOCKTAKING_TASK.getCode(), pairList, "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
//        // 查询盘点计划下其他单据是否全部审核完成
//        List<StocktakingTaskEntity> stocktakingTaskEntities = listBySourceId(entity.getSourceId());
//        // 全部审核完成 修改盘点计划单据状态
//        if (stocktakingTaskEntities.stream().allMatch(task -> Objects.equals(task.getStatus(), StocktakingStatusEnum.COMPLETED))) {
//            stocktakingPlanService.updateForStocktakingStatus(entity.getSourceId(), StocktakingStatusEnum.COMPLETED);
//        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    private static StocktakingStatusEnum isAllMatchStocktakingStatus(List<StocktakingTaskEntity> stocktakingTaskEntities) {
        if (CollectionUtils.isEmpty(stocktakingTaskEntities)) {
            return null;
        }
        if (stocktakingTaskEntities.stream().anyMatch(task -> Objects.equals(task.getStatus(), StocktakingStatusEnum.IN_PROGRESS) || Objects.equals(task.getStatus(), StocktakingStatusEnum.RECOUNT))) {
            return StocktakingStatusEnum.IN_PROGRESS;
        }
        if (stocktakingTaskEntities.stream().allMatch(task -> Objects.equals(task.getStatus(), StocktakingStatusEnum.NOT_STARTED) || Objects.equals(task.getStatus(), StocktakingStatusEnum.RECOUNT))) {
            return StocktakingStatusEnum.NOT_STARTED;
        }
        return null;
    }

    /**
     * 调用审核流程
     *
     * @param entity
     * @param dto
     */
    public void approveProcess(StocktakingTaskEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_TASK.getCode());
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
        if (Objects.isNull(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }


    }


    /**
     * 审核后的操作
     *
     * @param dto
     * @param entity
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, StocktakingTaskEntity entity) {
        if (Objects.isNull(entity)) {
            return Boolean.FALSE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        //完成
        StocktakingStatusEnum billStatus = StocktakingStatusEnum.COMPLETED;
        Boolean isPass = Boolean.TRUE;
        if (ApproveType.REJECT.equals(dto.getType())) {
            //变待提交 状态改为复盘中
            approveStatus = ApproveStatusEnum.REJECT;
            billStatus = StocktakingStatusEnum.RECOUNT;
            isPass = Boolean.FALSE;
        }
        Boolean result = updateForApprove(entity.getId(), approveStatus, billStatus);
        if (result && isPass) {
            // 组装盘盈盘亏单所需要的数据
            List<StocktakingProfitLossDTO.AddDTO> list = this.packageProfitLoss(entity);
            // 批量提审
            List<StocktakingProfitLossEntity> profitLossList = stocktakingProfitLossService.batchSave(list);

        }
        // 查询盘点计划下其他单据是否全部审核完成
        List<StocktakingTaskEntity> stocktakingTaskEntities = listBySourceId(entity.getSourceId());
        // 全部审核完成 修改盘点计划单据状态
        if (stocktakingTaskEntities.stream().allMatch(task -> Objects.equals(task.getStatus(), StocktakingStatusEnum.COMPLETED))) {
            stocktakingPlanService.updateForStocktakingStatus(entity.getSourceId(), StocktakingStatusEnum.COMPLETED);
        }
        return result;
    }

    /**
     * 组装盘盈盘亏单保存数据
     *
     * @param
     * @return java.util.List<com.erp.model.wms.dto.StocktakingProfitLossDTO.AddDTO>
     * @author yl
     * @date 2023-08-23 11:28
     */
    public List<StocktakingProfitLossDTO.AddDTO> packageProfitLoss(StocktakingTaskEntity taskEntity) {
        //任务code
        String taskCode = taskEntity.getCode();
        //任务id
        String taskId = taskEntity.getId();
        List<StocktakingTaskDetailEntity> stocktakingTaskDetailList = stocktakingTaskDetailService.listBaseByMainIds(Arrays.asList(taskId));
        List<String> warehouseIdList = stocktakingTaskDetailList.stream().map(StocktakingTaskDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = CollectionUtils.isNotEmpty(warehouseIdList) ? warehouseService.listByIds(warehouseIdList) : Collections.emptyList();
        //以仓库分组
        Map<String, List<StocktakingTaskDetailEntity>> warehouseMap = stocktakingTaskDetailList.stream().collect(Collectors.groupingBy(StocktakingTaskDetailEntity::getWarehouseId));
        //盘盈盘亏单 添加实体
        List<StocktakingProfitLossDTO.AddDTO> addList = new ArrayList<>(stocktakingTaskDetailList.size());
        //单据日期
        LocalDate billDate = LocalDate.now();
        //盘盈
        BillTypeEnum profit = BillTypeEnum.PROFIT;
        //盘亏
        BillTypeEnum loss = BillTypeEnum.LOSS;

        for (Map.Entry<String, List<StocktakingTaskDetailEntity>> item : warehouseMap.entrySet()) {
            List<StocktakingTaskDetailEntity> taskDetailList = item.getValue();
            //盘盈的任务明细
            List<StocktakingTaskDetailEntity> profitDetailList = taskDetailList.stream().filter(d -> d.getDiffQty() > 0).collect(Collectors.toList());

            //库存组织
            String orgId = warehouseList.stream().filter(w -> w.getId().equals(item.getKey())).
                    map(WarehouseEntity::getOrgId).findFirst().orElse("");
            if (CollectionUtils.isNotEmpty(profitDetailList)) {
                //处理盘盈数据
                StocktakingProfitLossDTO.AddDTO profitAddDTO = disposeDb(taskId, taskCode, profitDetailList, billDate, profit);
                profitAddDTO.setInventoryOrgId(orgId);
                addList.add(profitAddDTO);
            }
            //盘亏的任务明细
            List<StocktakingTaskDetailEntity> lossDetailList = taskDetailList.stream().filter(d -> d.getDiffQty() < 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(lossDetailList)) {
                //处理盘亏数据
                StocktakingProfitLossDTO.AddDTO lossAddDTO = disposeDb(taskId, taskCode, lossDetailList, billDate, loss);
                lossAddDTO.setInventoryOrgId(orgId);
                addList.add(lossAddDTO);
            }
        }
        return addList;
    }


    /**
     * 处理数据
     *
     * @param sourceId
     * @param sourceCode
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-08-10 15:05
     */
    private StocktakingProfitLossDTO.AddDTO disposeDb(String sourceId, String sourceCode, List<StocktakingTaskDetailEntity> detailList, LocalDate billDate, BillTypeEnum billType) {
        List<StocktakingProfitLossDetailDTO.AddDTO> addDetailList = new ArrayList<>(detailList.size());
        StocktakingProfitLossDTO.AddDTO addDTO = new StocktakingProfitLossDTO.AddDTO();
        addDTO.setBillDate(billDate);
        addDTO.setBillType(billType);
        addDTO.setSourceId(sourceId);
        addDTO.setSourceCode(sourceCode);
        for (StocktakingTaskDetailEntity detail : detailList) {
            //明细
            StocktakingProfitLossDetailDTO.AddDTO addDetail = new StocktakingProfitLossDetailDTO.AddDTO();
            addDetail.setDiffQty(detail.getDiffQty());
            addDetail.setFrozenQty(detail.getFrozenQty());
            addDetail.setQty(detail.getQty());
            addDetail.setSkuId(detail.getSkuId());
            addDetail.setSkuNo(detail.getSkuNo());
            addDetail.setUsableQty(detail.getUsableQty());
            addDetail.setWarehouseId(detail.getWarehouseId());
            addDetail.setWarehouseLocation(detail.getWarehouseLocation());
            addDetail.setSourceDetailId(detail.getId());
            addDetailList.add(addDetail);
        }
        addDTO.setDetailList(addDetailList);
        return addDTO;
    }

    /**
     * 根据code 获取任务信息
     *
     * @param taskCode
     * @return
     */
    @Override
    public StocktakingTaskEntity getByCode(String taskCode) {
        if (CharSequenceUtil.isBlank(taskCode)) {
            return null;
        }
        return this.lambdaQuery().eq(StocktakingTaskEntity::getCode, taskCode).
                last("LIMIT 1").one();
    }

    /**
     * 获取到盘点任务 盘点数量为0 的
     *
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.StocktakingTaskDTO.CheckResultDTO>
     * @author yl
     * @date 2023-08-22 10:36
     */
    @Override
    public List<StocktakingTaskDTO.CheckResultDTO> checkQty(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        List<StocktakingTaskDTO.CheckResultDTO> resultList = baseMapper.listQtyZero(ids);
        String userName = UserContext.getDefaultLoginUser().getUserName();
        String moduleType = ModuleTypeEnum.STOCKTAKING_TASK.getCode();
        List<OperateLogDTO.AddModuleOperateLogDTO> addList = new ArrayList<>(10);
        //以任务id分组
        Map<String, List<StocktakingTaskDTO.CheckResultDTO>> map = resultList.stream().collect(Collectors.groupingBy(StocktakingTaskDTO.CheckResultDTO::getId));
        for (Map.Entry<String, List<StocktakingTaskDTO.CheckResultDTO>> item : map.entrySet()) {
            String taskId = item.getKey();
            OperateLogDTO.AddModuleOperateLogDTO addModuleOperateLog = new OperateLogDTO.AddModuleOperateLogDTO();
            addModuleOperateLog.setBusinessId(taskId);
            addModuleOperateLog.setOperation("确认操作");
            StringBuffer sb = new StringBuffer();
            sb.append(userName).append("确认了,").append("盘点任务单:");
            List<StocktakingTaskDTO.CheckResultDTO> list = item.getValue();
            String code = list.get(0).getCode();
            sb.append(code + " ");
            for (StocktakingTaskDTO.CheckResultDTO detail : list) {
                sb.append("SKU为: ");
                sb.append(detail.getSkuNo() + " ");
                sb.append("盘点库存为 0");
            }
            addModuleOperateLog.setContent(sb.toString());
            addModuleOperateLog.setModuleType(moduleType);
            addList.add(addModuleOperateLog);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            operateLogService.batchAddModuleOperateLog(addList);
        }
        return resultList;
    }

    /**
     * 更改审核信息
     *
     * @param id
     * @param approveStatus
     * @return
     */
    public Boolean updateForApprove(String id, ApproveStatusEnum approveStatus, StocktakingStatusEnum billStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return this.lambdaUpdate().eq(StocktakingTaskEntity::getId, id)
                .set(StocktakingTaskEntity::getApproveUserId, userInfo.getUid())
                .set(StocktakingTaskEntity::getApproveUserName, userInfo.getUserName())
                .set(StocktakingTaskEntity::getApproveStatus, approveStatus)
                .set(StocktakingTaskEntity::getStatus, billStatus)
                .set(StocktakingTaskEntity::getApproveTime, LocalDateTime.now())
                .update(new StocktakingTaskEntity());

    }


    /**
     * 撤销流程
     *
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-08 18:08
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id) {
        StocktakingTaskEntity taskEntity = this.getById(id);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException("未找到盘点任务单");
        }
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        // 审核中的数据允许审核
        if (!Objects.equals(ingStatus, taskEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        String userId = UserContext.getDefaultLoginUser().getUid();
        handleCancelProcess(taskEntity, userId);
        List<StocktakingTaskEntity> stocktakingTaskEntities = listBySourceId(taskEntity.getSourceId());
        // 全部审核完成 修改盘点计划单据状态
        StocktakingStatusEnum stocktakingStatus = isAllMatchStocktakingStatus(stocktakingTaskEntities);
        if (ObjectUtil.isNotEmpty(stocktakingStatus)) {
            stocktakingPlanService.updateForStocktakingStatus(taskEntity.getSourceId(), stocktakingStatus);
        }
        return BatchResultDTO.success(taskEntity.getId(), taskEntity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    /**
     * 处理取消流程
     *
     * @param taskEntity
     * @param userId
     * @return void
     * @author yl
     * @date 2023-08-08 18:14
     */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleCancelProcess(StocktakingTaskEntity taskEntity, String userId) {
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(taskEntity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_TASK.getCode());
        revokeDTO.setUserId(userId);
        ApiResult<ProcessManagementDTO.RevokeResultDTO> apiResult = workflowFeign.revokeProcess(revokeDTO);
        if (apiResult.isSuccess()) {
            ApproveStatusEnum waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT;
            taskEntity.setApproveStatus(waitSubmitStatus);
            taskEntity.setStatus(StocktakingStatusEnum.NOT_STARTED);
            Boolean result = this.updateById(taskEntity);
            if (result) {
                List<Pair<String, String>> pairList = Arrays.asList(taskEntity).stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());

                operateLogService.batchAddModuleOperateLog("盘点任务单【%s】取消流程", ModuleTypeEnum.STOCKTAKING_TASK.getCode(), pairList, "取消流程操作");
            }

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO assignUser(String id, List<String> userIdList) {
        StocktakingTaskEntity task = this.getById(id);
        if (Objects.isNull(task)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        Boolean result = stocktakingTaskUserService.assignUser(task, userIdList);

        return BatchResultDTO.success(task.getId(), task.getCode(), "分配成功");

    }

    @Override
    public Boolean exportExcel(StocktakingTaskDTO.ExportDTO params, HttpServletResponse response) {
        String tabFlag = params.getTabFlag();
        //对应tab的状态
        List<String> tabList = new ArrayList<>(1);
        if (!WmsConstant.ALL.equals(tabFlag)) {
            tabList.add(tabFlag);
        }
        List<String> mainIdList = new ArrayList<>();
        //仓库id
        String warehouseId = params.getWarehouseId();
        if (CharSequenceUtil.isNotBlank(warehouseId)) {
            List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listByWarehouseIds(Arrays.asList(warehouseId));
            List<String> mainIds = taskDetailList.stream().map(StocktakingTaskDetailEntity::getMainId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(mainIds)) {
                throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
            }
            mainIdList.addAll(mainIds);
        }
        //获取导出数据
        List<StocktakingTaskDTO.PagingViewDTO> list = baseMapper.listExport(params, tabList, mainIdList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //填充数据
        fillDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/StocktakingTask.xlsx";
        String name = "盘点任务列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            log.error("盘点任务列表导出 出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<WarehouseEntity> warehouseList = warehouseService.list();
        StocktakingTaskExcelListener excelListener = new StocktakingTaskExcelListener(this, stocktakingTaskDetailService, warehouseList, operateLogService);
        try {
            EasyExcel.read(excelFile.getInputStream(), StocktakingTaskDetailExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("盘点任务明细导入错误！>>>>>{}", e);
            return Boolean.FALSE;
        }
        List<StocktakingTaskDetailExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "盘点任务明细错误信息";
            ExcelUtil.export(fileName, "error", errorList, StocktakingTaskDetailExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/StocktakingTaskTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("盘点任务单 downloadTemplate  出错了 e==={}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public List<StocktakingTaskEntity> listBySourceId(String sourceId) {
        return lambdaQuery()
                .eq(StocktakingTaskEntity::getSourceId, sourceId)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeBySourceId(String sourceId) {
        List<StocktakingTaskEntity> taskEntityList = listBySourceId(sourceId);
        if (CollUtil.isEmpty(taskEntityList)) {
            return Boolean.TRUE;
        }
        List<String> mainIds = taskEntityList.stream().map(StocktakingTaskEntity::getId).collect(Collectors.toList());
        // 删除明细表数据
        stocktakingTaskDetailService.removeByMainId(mainIds);
        // 删除主表数据
        this.removeByIds(mainIds);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createTaskList(StocktakingPlanEntity entity, List<StocktakingPlanDetailEntity> detailEntityList) {
        // 1. 查询所有需要盘点的库存记录
        List<InventoryEntity> inventoryList = inventoryService.listByStocktakingType(entity, detailEntityList);
        if (CollUtil.isEmpty(inventoryList)) {
            log.error("盘点计划【{}】没有需要盘点的库存记录", entity.getId());
            return Boolean.TRUE;
        }
        String planCode = entity.getCode();
        // 2. 对需要盘点的 组织+仓库+仓位+skuId+库存状态 进行增加锁定库存操作
        inventoryList.stream().forEach(item -> {
            // 判断如果已存在盘点任务，抛出异常
            String existKey = CharSequenceUtil.format(RedisKeyConstant.INVENTORY_LOCK, "*", item.getOrgId(), item.getWarehouseId(), item.getWarehouseLocation(), item.getSkuId(), item.getDictInventoryStatus());
            Collection<String> keys = redisUtil.keys(existKey);
            if (CollUtil.isNotEmpty(keys)) {
                WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(item.getWarehouseId());
                String warehouseName = ObjectUtil.isNotEmpty(updateDTO) ? updateDTO.getName() : item.getWarehouseId();
                log.error("仓库【{}】库位【{}】 SKU【{}】【{}】库存 已存在盘点任务，不能重复创建", warehouseName, item.getWarehouseLocation(), item.getSkuNo(), item.getDictInventoryStatus());
                throw new ServiceException(ApiError.STOCKTAKING_TASK_EXIST, warehouseName, item.getWarehouseLocation(), item.getSkuNo(), item.getDictInventoryStatus());
            }
            String redisKey = CharSequenceUtil.format(RedisKeyConstant.INVENTORY_LOCK, entity.getCode(), item.getOrgId(), item.getWarehouseId(), item.getWarehouseLocation(), item.getSkuId(), item.getDictInventoryStatus());
            redisUtil.set(redisKey, planCode);
        });
        // 3. 对库存记录进行分组，按照分单规则进行分组
        SeparateRuleEnum separateRule = entity.getSeparateRule();
        Map<String, String> locationAreaMap = new HashMap<>();
        if (ObjectUtil.equals(separateRule, SeparateRuleEnum.WAREHOUSE_AREA)) {
            // 查询仓位对应的库区
            locationAreaMap = warehouseLocationService.locationAreaMap();
        }
        String format = SeparateRuleEnum.getFormatStr(separateRule);
        ;
        Map<String, String> finalLocationAreaMap = locationAreaMap;
        Map<String, List<InventoryEntity>> inventoryMap = inventoryList
                .stream()
                .collect(Collectors.groupingBy(item -> {
                    // 按照仓库区域分单时，仓位需要转换为库区
                    String groupKey = getGroupKey(separateRule, format, finalLocationAreaMap, item);
                    return groupKey;
                }));
        // 4. 根据分组结果构建数据并保存盘点任务
        // 避免多线程时，只有主线程才能获取到用户信息
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        String uid = userInfo.getUid();
        String username = userInfo.getUserName();
        inventoryMap.keySet().parallelStream().forEach(key -> {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.STOCKTAKING_TASK);
            StocktakingTaskEntity insertTask = new StocktakingTaskEntity(entity, code, uid, username);
            this.save(insertTask);
            List<InventoryEntity> inventoryEntityList = inventoryMap.get(key);
            // 根据组织+仓库+仓位+skuId 进行分组 获取不同库存状态的库存记录
            Map<String, List<InventoryEntity>> inventoryStatusMap = inventoryEntityList.stream()
                    .collect(Collectors.groupingBy(item -> CharSequenceUtil.format("{}_{}_{}_{}", item.getOrgId(), item.getWarehouseId(), item.getWarehouseLocation(), item.getSkuId())));
            List<StocktakingTaskDetailEntity> insertDetailList = inventoryStatusMap.keySet().stream().map(item -> {
                List<InventoryEntity> inventoryEntities = inventoryStatusMap.get(item);
                WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(inventoryEntities.get(0).getWarehouseId());
                String warehouseName = ObjectUtil.isNotEmpty(updateDTO) ? updateDTO.getName() : "";
                StocktakingTaskDetailEntity detailEntity = new StocktakingTaskDetailEntity(inventoryEntities, insertTask.getId(), warehouseName, uid, username);
                return detailEntity;
            }).collect(Collectors.toList());
            stocktakingTaskDetailService.saveBatch(insertDetailList, 500);
            String msg = CharSequenceUtil.format("由盘点计划【{}】自动生成盘点任务单号为【{}】单据", planCode, code);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_TASK.getCode(), insertTask.getId(), "新增单据", uid, username);
        });
        return Boolean.TRUE;
    }

    private static String getGroupKey(SeparateRuleEnum separateRule, String format, Map<String, String> finalLocationAreaMap, InventoryEntity item) {
        String location = item.getWarehouseLocation();
        if (ObjectUtil.equals(separateRule, SeparateRuleEnum.WAREHOUSE_AREA)) {
            location = ObjectUtil.isNull(finalLocationAreaMap.get(item.getWarehouseLocation())) ? "" : finalLocationAreaMap.get(item.getWarehouseLocation());
        }
        String groupKey = CharSequenceUtil.format(format, item.getWarehouseId(), location);
        return groupKey;
    }
}
