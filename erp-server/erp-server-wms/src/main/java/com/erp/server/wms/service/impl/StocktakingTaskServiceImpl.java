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
import com.common.business.constant.ApproveType;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.RedisUtil;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.message.constant.DistributeKeyConstant;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.common.business.constant.RedisCacheConstants;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.StocktakingTaskDetailExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.common.message.constant.DistributeKeyConstant;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.listener.StocktakingTaskDetailExcelImportHelper;
import com.erp.server.wms.listener.StocktakingTaskDetailExcelTemplateWriter;
import com.erp.server.wms.mapper.StocktakingTaskMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.util.StocktakingInventoryLockHelper;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    /**
     * 临界区内 setIfAbsent 分批大小（不拆分 MultiLock，仅分批写入）。
     * <p>
     * 盘点库存 Redis lock / 索引生命周期（与 UAT/历史 {@code lockInventoryForStocktaking} 一致）：
     * 加锁 {@code SET NX} 不设 TTL，释锁靠业务显式 {@code DEL}；索引随释锁/回滚清理，异常残留走 plan 级 SCAN 兜底。
     * 同 plan 的下推（加锁+落库+索引）与释锁共用 planCode {@code @DistributeLocker}，在方法执行期互斥。
     * <p>
     * 事务边界：plan 锁随 {@link #createStocktakingTasksUnderPlanLock} 方法返回释放，外层 {@code createTaskList} DB 事务可能尚未 commit；
     * 有意不使用 {@code unlockAfterTx}，避免大计划下推长临界区阻塞同 plan 任务释锁。
     * 残余窗口靠 {@link #loadLockKeysReferencedByOtherIncompleteTasks} 与业务侧「已下推则拒绝」兜底，正常单次下推与任务完成链路不应重叠。
     */
    private static final int STOCKTAKING_INVENTORY_LOCK_WRITE_BATCH_SIZE = 200;

    /** 超过该唯一维度数时分批维度分布式锁，单批不超过此值，避免 Redisson MultiLock 过大超时 */
    private static final int STOCKTAKING_DIM_LOCK_LARGE_PLAN_THRESHOLD = 300;

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
    private StocktakingProfitLossDetailService stocktakingProfitLossDetailService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private RedisUtil redisUtil;
    @Resource
    @Lazy
    private StocktakingTaskService self;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private StocktakingPlanService stocktakingPlanService;
    @Resource
    private StocktakingTaskRollbackService stocktakingTaskRollbackService;

    /**
     * tab list
     *
     * @param dto
     * @return
     */
    @Override
    public List<StocktakingTaskDTO.TabDTO> tabList(PermissionsDTO dto) {
        List<StocktakingTaskDTO.TabDTO> tabList = new ArrayList<>(4);
        List<StocktakingTaskDTO.TabDTO> dbList = baseMapper.tabList(getPermissionSql(dto.getPermissionSql()));
        StocktakingTaskDTO.TabDTO all = new StocktakingTaskDTO.TabDTO();
        int allCount = dbList.stream().mapToInt(StocktakingTaskDTO.TabDTO::getCount).sum();
        all.setTabFlag(WmsConstant.ALL);
        all.setCount(allCount);
        all.setTabFlagName("全部");
        tabList.add(all);
        for (ApproveStatusWithoutRejectEnum approveStatus : ApproveStatusWithoutRejectEnum.values()) {
            String tabFlag = approveStatus.getStatus();
            StocktakingTaskDTO.TabDTO tabDTO = new StocktakingTaskDTO.TabDTO();
            tabDTO.setTabFlag(tabFlag);
            int count = dbList.stream().filter(a -> tabFlag.equals(a.getTabFlag())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
            tabDTO.setCount(count);
            tabDTO.setTabFlagName(approveStatus.getName());
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
        params.setPermissionSql(getPermissionSql(dto.getPermissionSql()));
        Page query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<StocktakingTaskDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    private String getPermissionSql(String permissionSql) {
        if (StringUtils.isBlank(permissionSql)) {
            return null;
        }
        return " and exists (select 1 from stocktaking_task_detail std where st.id=std.main_id and std.is_deleted=FALSE " + permissionSql + ")";
    }


    /**
     * 填充数据
     *
     * @param list
     */
    private void fillDb(List<StocktakingTaskDTO.PagingViewDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // 一次性获取所有需要的ID
        List<String> idList = list.stream()
                .map(StocktakingTaskDTO.PagingViewDTO::getId)
                .collect(Collectors.toList());

        // 并行查询所有需要的数据
        CompletableFuture<List<StocktakingTaskDetailEntity>> detailFuture = CompletableFuture.supplyAsync(
                () -> stocktakingTaskDetailService.listBaseByMainIds(idList));

        CompletableFuture<List<StocktakingTaskUserEntity>> userFuture = CompletableFuture.supplyAsync(
                () -> stocktakingTaskUserService.listBaseBySourceIdList(idList));

        CompletableFuture<ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>>> apiResultFuture =
                CompletableFuture.supplyAsync(() -> {
                    ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
                    list.forEach(obj -> dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(
                            SourceTypeEnum.STOCKTAKING_TASK.getCode(), obj.getId())));

                    if (CollectionUtils.isNotEmpty(dtoList)) {
                        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> result = workflowFeign.curApprover(dtoList);
                        if (200 != result.getCode()) {
                            throw new ServiceException(ApiError.WF_CUR_APPROVER_QUERY_FAILED, result.getMsg());
                        }
                        return result;
                    }
                    return null;
                });

        // 等待所有查询完成
        CompletableFuture.allOf(detailFuture, userFuture, apiResultFuture).join();

        List<StocktakingTaskDetailEntity> taskDetailList = detailFuture.join();
        List<StocktakingTaskUserEntity> taskUserList = userFuture.join();
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = apiResultFuture.join();

        // 预处理数据 - 按主ID分组
        Map<String, List<StocktakingTaskDetailEntity>> detailByMainId = taskDetailList.stream()
                .collect(Collectors.groupingBy(StocktakingTaskDetailEntity::getMainId));

        Map<String, List<StocktakingTaskUserEntity>> userBySourceId = taskUserList.stream()
                .collect(Collectors.groupingBy(StocktakingTaskUserEntity::getSourceId));

        Map<String, List<ProcessManagementDTO.CurApproveInfoDTO>> approveInfoByBusinessId =
                listApiResult != null && CollectionUtils.isNotEmpty(listApiResult.getData())
                        ? listApiResult.getData().stream()
                        .collect(Collectors.groupingBy(ProcessManagementDTO.CurApproveInfoDTO::getBusinessId))
                        : Collections.emptyMap();

        // 预加载所有详情数据
        Map<String, List<StocktakingTaskDetailDTO.ViewDTO>> viewDtoByMainId = new HashMap<>();
        Map<String, List<String>> stocktakingTaskDetailIdListByMainId = new HashMap<>();
        for (String mainId : idList) {
            List<StocktakingTaskDetailDTO.ViewDTO> viewDTOS = stocktakingTaskDetailService.listByMainId(mainId);
            viewDtoByMainId.put(mainId, viewDTOS);

            List<String> detailIds = viewDTOS.stream()
                    .map(StocktakingTaskDetailDTO.ViewDTO::getId)
                    .collect(Collectors.toList());
            stocktakingTaskDetailIdListByMainId.put(mainId, detailIds);
        }

        // 预加载所有盈亏详情数据
        Map<String, List<StocktakingProfitLossDetailEntity>> profitLossBySourceDetailId = new HashMap<>();
        Set<String> allDetailIds = stocktakingTaskDetailIdListByMainId.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        if (!allDetailIds.isEmpty()) {
            List<StocktakingProfitLossDetailEntity> profitLossDetails = stocktakingProfitLossDetailService.listBySourceIds(new ArrayList<>(allDetailIds));
            profitLossBySourceDetailId = profitLossDetails.stream()
                    .collect(Collectors.groupingBy(StocktakingProfitLossDetailEntity::getSourceDetailId));
        }

        for (StocktakingTaskDTO.PagingViewDTO item : list) {
            String id = item.getId();

            // 处理盘点盈亏状态
            List<StocktakingTaskDetailDTO.ViewDTO> viewDTOS = viewDtoByMainId.get(id);
            if (viewDTOS != null) {
                List<StocktakingTaskDetailDTO.ViewDTO> filterList = viewDTOS.stream()
                        .filter(obj -> obj.getDiffQty() != 0)
                        .collect(Collectors.toList());

                if (filterList.isEmpty()) {
                    item.setIsPushStocktakingProfitLoss(Boolean.FALSE);
                    item.setPushStocktakingProfitLossStatus(PushStocktakingProfitLossStatusEnum.NOT_NEED_GENERATE.getCode());
                } else {
                    List<String> detailIds = stocktakingTaskDetailIdListByMainId.get(id);
                    Map<String, List<StocktakingProfitLossDetailEntity>> finalProfitLossBySourceDetailId = profitLossBySourceDetailId;
                    long count = detailIds.stream()
                            .filter(detailId -> !finalProfitLossBySourceDetailId.getOrDefault(detailId, Collections.emptyList()).isEmpty())
                            .count();

                    if (count == 0) {
                        item.setIsPushStocktakingProfitLoss(Boolean.TRUE);
                        item.setPushStocktakingProfitLossStatus(PushStocktakingProfitLossStatusEnum.NOT_GENERATE.getCode());
                    } else if (count == filterList.size()) {
                        item.setIsPushStocktakingProfitLoss(Boolean.FALSE);
                        item.setPushStocktakingProfitLossStatus(PushStocktakingProfitLossStatusEnum.GENERATED.getCode());
                    } else {
                        item.setIsPushStocktakingProfitLoss(Boolean.TRUE);
                        item.setPushStocktakingProfitLossStatus(PushStocktakingProfitLossStatusEnum.NOT_ALL_GENERATE.getCode());
                    }
                }
            }

            // 设置各种名称
            item.setApproveStatusName(item.getApproveStatus().getName());
            item.setSeparateRuleName(Objects.nonNull(item.getSeparateRule()) ? item.getSeparateRule().getName() : "");
            item.setStocktakingModeName(Objects.nonNull(item.getStocktakingMode()) ? item.getStocktakingMode().getName() : "");
            item.setStocktakingTypeName(Objects.nonNull(item.getStocktakingType()) ? item.getStocktakingType().getName() : "");
            item.setStocktakingStatusName(Objects.nonNull(item.getStocktakingStatus()) ? item.getStocktakingStatus().getName() : "");

            // 设置盘点人
            List<StocktakingTaskUserEntity> users = userBySourceId.get(id);
            if (users != null) {
                item.setStocktakingUserName(users.stream()
                        .map(StocktakingTaskUserEntity::getUserName)
                        .collect(Collectors.joining(",")));
            }

            // 设置仓库名称
            List<StocktakingTaskDetailEntity> details = detailByMainId.get(id);
            if (details != null) {
                item.setWarehouseName(details.stream()
                        .map(StocktakingTaskDetailEntity::getWarehouseName)
                        .distinct()
                        .collect(Collectors.joining(",")));

                // SKU统计数
                item.setSkuCount(Math.toIntExact(details.stream()
                        .map(StocktakingTaskDetailEntity::getSkuId)
                        .distinct()
                        .count()));
            }

            // 设置当前审批人
            if (listApiResult != null) {
                List<ProcessManagementDTO.CurApproveInfoDTO> approveInfos = approveInfoByBusinessId.get(id);
                if (approveInfos != null) {
                    String curApprove = approveInfos.stream()
                            .filter(e -> StringUtils.isNotBlank(e.getCurApproveName()))
                            .map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName)
                            .collect(Collectors.joining(","));
                    if (StringUtils.isNotBlank(curApprove)) {
                        item.setApproveUserName(curApprove);
                    }
                }
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
            throw new ServiceException(ApiError.BILL_WAIT_SUBMIT_TO_APPROVE_ING);
        }

        List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listBaseByMainIds(Collections.singletonList(id));
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
            throw new ServiceException(ApiError.BILL_NOT_EXIST);
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
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseBySourceIdList(Collections.singletonList(id));
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @DistributeLocker(businessType = DistributeKeyConstant.BILL_BUSINESS_LOCK_KEY, keyName = "id", unlockAfterTx = true)
    public BatchResultDTO approve(String id, ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StringUtils.isEmpty(dto.getComment())) {
            // 审核不通过必须填写审核意见
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        StocktakingTaskEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("未找到盘点任务单");
        }
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        // 审核中的数据允许审核
        if (!Objects.equals(ingStatus, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY);
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
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.WF_APPROVE_FAILED);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (Objects.isNull(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    /**
     * variablesMap值赋值
     * @author jack
     * @date 2025/5/27 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(StocktakingTaskEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<StocktakingTaskDetailEntity> detailList = stocktakingTaskDetailService.lambdaQuery().eq(StocktakingTaskDetailEntity::getMainId,entity.getId()).list();
        if (CollUtil.isNotEmpty(detailList)) {
            variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        }
        return variablesMap;
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
        if (ApproveType.REJECT.equals(dto.getType())) {
            approveStatus = ApproveStatusEnum.REJECT;
            billStatus = StocktakingStatusEnum.IN_PROGRESS;
        }
        Boolean result = updateForApprove(entity.getId(), approveStatus, billStatus);

        // 查询盘点计划下其他单据是否全部审核完成
        List<StocktakingTaskEntity> stocktakingTaskEntities = listBySourceId(entity.getSourceId());
        // 全部审核完成 修改盘点计划单据状态
        if (stocktakingTaskEntities.stream().allMatch(task -> Objects.equals(task.getStatus(), StocktakingStatusEnum.COMPLETED))) {
            stocktakingPlanService.updateForStocktakingStatus(entity.getSourceId(), StocktakingStatusEnum.COMPLETED);
        }
        if (ApproveStatusEnum.APPROVE.equals(approveStatus)) {
            releaseInventoryLockByTaskId(entity.getId());
        }
        return result;
    }

    @Override
    public void releaseInventoryLockByTaskId(String taskId) {
        if (CharSequenceUtil.isBlank(taskId)) {
            return;
        }
        StocktakingTaskEntity task = getById(taskId);
        if (Objects.isNull(task) || CharSequenceUtil.isBlank(task.getSourceCode())) {
            log.warn("释锁跳过：任务不存在或无计划单号，taskId={}", taskId);
            return;
        }
        String planCode = task.getSourceCode();
        runAfterCommit(() -> releaseInventoryLockByTaskWithRetry(taskId, planCode, "releaseByTaskId:" + taskId));
    }

    /**
     * 按计划单号释放该计划下全部 Redis 盘点库存锁。
     * <p>
     * 优先从 plan-keys 索引精确删除；无索引时回退 {@code lock:wms:inventory:{planCode}_*} SCAN（历史数据）。
     */
    @Override
    public void releaseInventoryLockByPlanCode(String planCode) {
        if (CharSequenceUtil.isBlank(planCode)) {
            return;
        }
        self.releaseInventoryLockByPlanCodeUnderPlanLock(planCode);
    }

    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.WMS_STOCKTAKING_INVENTORY_DIM_KEY,
            keyName = "planCode",
            waiteTime = 180,
            maxRetries = 5
    )
    public void releaseInventoryLockByTaskUnderPlanLock(String planCode, String taskId) {
        StocktakingTaskEntity task = getById(taskId);
        releaseInventoryLockByTask(task);
    }

    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.WMS_STOCKTAKING_INVENTORY_DIM_KEY,
            keyName = "planCode",
            waiteTime = 180,
            maxRetries = 5
    )
    public void releaseInventoryLockByPlanCodeUnderPlanLock(String planCode) {
        doReleaseInventoryLockByPlanCode(planCode);
    }

    /**
     * 释放计划下全部 Redis 盘点库存锁（无分布式锁；调用方已在 plan 临界区内时使用）。
     */
    private void doReleaseInventoryLockByPlanCode(String planCode) {
        if (CharSequenceUtil.isBlank(planCode)) {
            return;
        }
        int releasedFromIndex = releaseInventoryLockByPlanCodeFromIndex(planCode);
        int releasedFromLegacy = 0;
        if (!isPlanInventoryLockFullyReleased(planCode)) {
            releasedFromLegacy = releaseInventoryLockByPlanCodeLegacy(planCode);
        }
        if (releasedFromIndex + releasedFromLegacy > 0) {
            log.warn("释放盘点计划库存锁：planCode={}, indexCount={}, legacyCount={}",
                    planCode, releasedFromIndex, releasedFromLegacy);
        }
    }

    /**
     * 从 plan-keys 索引精确释锁。
     *
     * @param planCode 计划单号
     * @return 成功删除的 lock 数量
     */
    private int releaseInventoryLockByPlanCodeFromIndex(String planCode) {
        String indexKey = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_PLAN_KEYS, planCode);
        Set<Object> lockKeys = redisUtil.sGet(indexKey);
        if (CollUtil.isEmpty(lockKeys)) {
            return 0;
        }
        int releasedCount = 0;
        for (Object lockKeyObj : lockKeys) {
            String lockKey = String.valueOf(lockKeyObj);
            if (releaseInventoryLockIfOwnedByPlan(lockKey, planCode)) {
                releasedCount++;
                redisUtil.setRemove(indexKey, lockKeyObj);
            } else if (!isInventoryLockOwnedByPlan(lockKey, planCode)) {
                redisUtil.setRemove(indexKey, lockKeyObj);
            } else {
                log.warn("计划释锁跳过非本计划 key，保留 index 成员：planCode={}, lockKey={}", planCode, lockKey);
            }
        }
        cleanupInventoryLockIndexIfEmpty(indexKey);
        return releasedCount;
    }

    /**
     * 历史计划或 index 未覆盖 lock 时的释锁兜底：按 planCode 前缀 SCAN，仅删除本 plan 占用的 key。
     *
     * @param planCode 计划单号
     * @return 成功删除的 lock 数量
     */
    private int releaseInventoryLockByPlanCodeLegacy(String planCode) {
        String keyPattern = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_CODE, planCode);
        Collection<String> keys = redisUtil.scanKeys(keyPattern);
        if (CollUtil.isEmpty(keys)) {
            return 0;
        }
        int releasedCount = 0;
        for (String key : keys) {
            if (releaseInventoryLockIfOwnedByPlan(key, planCode)) {
                releasedCount++;
            }
        }
        if (releasedCount > 0) {
            log.warn("释放盘点计划库存锁(legacy)：planCode={}, count={}", planCode, releasedCount);
        }
        return releasedCount;
    }

    /** 任务释锁重试封装，见 {@link #releaseInventoryLockByTask(StocktakingTaskEntity)} 设计说明 */
    private void releaseInventoryLockByTaskWithRetry(String taskId, String planCode, String context) {
        try {
            self.releaseInventoryLockByTaskUnderPlanLock(planCode, taskId);
        } catch (Exception e) {
            log.error("盘点任务释锁失败，重试一次：taskId={}, planCode={}, context={}", taskId, planCode, context, e);
            try {
                self.releaseInventoryLockByTaskUnderPlanLock(planCode, taskId);
            } catch (Exception retryEx) {
                log.error("盘点任务释锁重试仍失败，需人工处理 Redis 锁：taskId={}, planCode={}, context={}",
                        taskId, planCode, context, retryEx);
            }
        }
    }

    /** 计划下全部任务已删时使用；失败重试一次，仍失败仅记录错误日志 */
    private void releaseInventoryLockByPlanCodeWithRetry(String planCode, String context) {
        try {
            self.releaseInventoryLockByPlanCodeUnderPlanLock(planCode);
        } catch (Exception e) {
            log.error("盘点计划释锁失败，重试一次：context={}, planCode={}", context, planCode, e);
            try {
                self.releaseInventoryLockByPlanCodeUnderPlanLock(planCode);
            } catch (Exception retryEx) {
                log.error("盘点计划释锁重试仍失败，需人工处理 Redis 锁：context={}, planCode={}", context, planCode, retryEx);
            }
        }
    }

    /**
     * 按任务释放 Redis 盘点库存锁。
     * <p>
     * 按 task-keys 索引成员精确释锁；无索引时按任务明细推导 lockKey 兜底。
     * 同一 lockKey 若仍被本计划下其它未完成任务引用，则保留该锁。
     * 计划下全部任务完成后，再触发 plan 级兜底释锁。
     * 调用方须已持有 planCode 分布式锁（见 {@link #releaseInventoryLockByTaskUnderPlanLock}）。
     */
    private void releaseInventoryLockByTask(StocktakingTaskEntity task) {
        if (Objects.isNull(task) || CharSequenceUtil.isBlank(task.getSourceCode())) {
            return;
        }
        String planCode = task.getSourceCode();
        String taskId = task.getId();
        String indexKey = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_TASK_KEYS, taskId);
        Set<Object> indexedLockKeys = redisUtil.sGet(indexKey);
        if (CollUtil.isEmpty(indexedLockKeys)) {
            Set<String> derivedLockKeys = resolveTaskInventoryLockKeysFromDetails(task, planCode);
            if (CollUtil.isEmpty(derivedLockKeys)) {
                log.warn("释锁跳过：任务无 task-keys 且无法从明细推导 lockKey：taskCode={}", task.getCode());
            } else {
                log.warn("释锁兜底：任务无 task-keys，按明细推导 lockKey：taskCode={}, keyCount={}",
                        task.getCode(), derivedLockKeys.size());
                indexedLockKeys = new HashSet<>(derivedLockKeys);
            }
        }
        if (CollUtil.isNotEmpty(indexedLockKeys)) {
            Set<String> otherReferencedLockKeys = loadLockKeysReferencedByOtherIncompleteTasks(task.getSourceId(), taskId);
            int releasedCount = 0;
            int skippedSharedCount = 0;
            for (Object lockKeyObj : indexedLockKeys) {
                String lockKey = String.valueOf(lockKeyObj);
                if (otherReferencedLockKeys.contains(lockKey)) {
                    skippedSharedCount++;
                    continue;
                }
                if (releaseInventoryLockIfOwnedByPlan(lockKey, planCode)) {
                    releasedCount++;
                }
                removeLockKeyFromPlanIndex(planCode, lockKey);
            }
            log.warn("释放盘点任务库存锁：taskCode={}, releasedCount={}, skippedSharedCount={}",
                    task.getCode(), releasedCount, skippedSharedCount);
        }
        redisUtil.del(indexKey);
        tryReleasePlanInventoryLockWhenAllTasksCompleted(task);
    }

    /**
     * 批量加载同计划下其它未完成任务引用的 lockKey（无索引任务共用一次库存/仓库反查，避免 N+1）。
     *
     * @param sourceId      计划 id
     * @param excludeTaskId 当前释锁任务 id（排除自身 task-keys）
     * @return 仍被其它未完成任务引用的 lockKey 集合；无引用时返回空 Set
     */
    private Set<String> loadLockKeysReferencedByOtherIncompleteTasks(String sourceId, String excludeTaskId) {
        if (CharSequenceUtil.isBlank(sourceId)) {
            return Collections.emptySet();
        }
        List<StocktakingTaskEntity> planTasks = listBySourceId(sourceId);
        if (CollUtil.isEmpty(planTasks)) {
            return Collections.emptySet();
        }
        Set<String> referencedLockKeys = new HashSet<>();
        List<StocktakingTaskEntity> deriveTasks = new ArrayList<>();
        for (StocktakingTaskEntity planTask : planTasks) {
            if (Objects.equals(planTask.getId(), excludeTaskId)) {
                continue;
            }
            if (Objects.equals(planTask.getStatus(), StocktakingStatusEnum.COMPLETED)) {
                continue;
            }
            String otherIndexKey = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_TASK_KEYS, planTask.getId());
            Set<Object> otherLockKeys = redisUtil.sGet(otherIndexKey);
            if (CollUtil.isNotEmpty(otherLockKeys)) {
                for (Object otherLockKeyObj : otherLockKeys) {
                    referencedLockKeys.add(String.valueOf(otherLockKeyObj));
                }
                continue;
            }
            deriveTasks.add(planTask);
        }
        if (CollUtil.isEmpty(deriveTasks)) {
            return referencedLockKeys;
        }
        List<String> deriveTaskIds = deriveTasks.stream().map(StocktakingTaskEntity::getId).collect(Collectors.toList());
        List<StocktakingTaskDetailEntity> allDetailList = stocktakingTaskDetailService.listBaseByMainIds(deriveTaskIds);
        InventoryLockDeriveContext deriveContext = buildInventoryLockDeriveContext(allDetailList);
        Map<String, List<StocktakingTaskDetailEntity>> detailsByTaskId = allDetailList.stream()
                .collect(Collectors.groupingBy(StocktakingTaskDetailEntity::getMainId));
        for (StocktakingTaskEntity planTask : deriveTasks) {
            referencedLockKeys.addAll(resolveTaskInventoryLockKeysFromDetails(planTask, planTask.getSourceCode(),
                    deriveContext, detailsByTaskId.get(planTask.getId())));
        }
        return referencedLockKeys;
    }

    /**
     * 无 task-keys 索引时，按任务明细 + 库存反查 org 推导本任务可能占用的 lockKey 集合。
     *
     * @param task     盘点任务
     * @param planCode 计划单号
     * @return 推导出的 lockKey 集合；无法推导时返回空 Set
     */
    private Set<String> resolveTaskInventoryLockKeysFromDetails(StocktakingTaskEntity task, String planCode) {
        List<StocktakingTaskDetailEntity> detailList = stocktakingTaskDetailService.listBaseByMainIds(
                Collections.singletonList(task.getId()));
        InventoryLockDeriveContext deriveContext = buildInventoryLockDeriveContext(detailList);
        return resolveTaskInventoryLockKeysFromDetails(task, planCode, deriveContext, detailList);
    }

    /**
     * 在已构建的反查上下文中按任务明细推导 lockKey（批量释锁兜底复用，避免重复查库存/仓库）。
     *
     * @param task          盘点任务
     * @param planCode      计划单号
     * @param deriveContext 共用 org/仓库反查结果
     * @param detailList    该任务明细；为空时返回空 Set
     * @return 推导出的 lockKey 集合
     */
    private Set<String> resolveTaskInventoryLockKeysFromDetails(StocktakingTaskEntity task, String planCode,
                                                                InventoryLockDeriveContext deriveContext,
                                                                List<StocktakingTaskDetailEntity> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return Collections.emptySet();
        }
        Set<String> lockKeys = new LinkedHashSet<>();
        for (StocktakingTaskDetailEntity detail : detailList) {
            if (detail.getUsableQty() != null) {
                appendDetailInventoryLockKeys(lockKeys, planCode, detail, InventoryStatusEnum.USABLE.getCode(), deriveContext);
            }
            if (detail.getFrozenQty() != null) {
                appendDetailInventoryLockKeys(lockKeys, planCode, detail, InventoryStatusEnum.FROZEN.getCode(), deriveContext);
            }
        }
        return lockKeys;
    }

    /**
     * 按明细维度反查 org 后追加完整 lockKey；org 未知时回退 plan+wh+库位+sku+status 通配 SCAN（与 UAT 通配释锁一致）。
     */
    private void appendDetailInventoryLockKeys(Set<String> lockKeys, String planCode, StocktakingTaskDetailEntity detail,
                                               String inventoryStatus, InventoryLockDeriveContext deriveContext) {
        List<String> orgIds = resolveInventoryOrgIdsForDetail(detail, inventoryStatus, deriveContext);
        if (CollUtil.isEmpty(orgIds)) {
            appendDetailInventoryLockKeysFromPatternScan(lockKeys, planCode, detail, inventoryStatus);
            return;
        }
        for (String orgId : orgIds) {
            lockKeys.add(buildInventoryLockRedisKey(planCode, orgId, detail.getWarehouseId(),
                    detail.getWarehouseLocation(), detail.getSkuId(), inventoryStatus));
            if (StocktakingInventoryLockHelper.isRawWarehouseLocationDifferent(detail.getWarehouseLocation(), inventoryStatus)) {
                lockKeys.add(CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK, planCode, orgId,
                        detail.getWarehouseId(), detail.getWarehouseLocation(), detail.getSkuId(), inventoryStatus));
            }
        }
    }

    /**
     * org 无法反查时，按 planCode+wh+库位+sku+status 通配 SCAN 收集本 plan 占用的 lockKey。
     */
    private void appendDetailInventoryLockKeysFromPatternScan(Set<String> lockKeys, String planCode,
                                                              StocktakingTaskDetailEntity detail, String inventoryStatus) {
        String normalizedLocation = normalizeWarehouseLocationForInventoryLock(detail.getWarehouseLocation(), inventoryStatus);
        collectPlanOwnedLockKeysFromPattern(lockKeys, planCode, CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK,
                planCode, "*", detail.getWarehouseId(), normalizedLocation, detail.getSkuId(), inventoryStatus));
        if (StocktakingInventoryLockHelper.isRawWarehouseLocationDifferent(detail.getWarehouseLocation(), inventoryStatus)) {
            collectPlanOwnedLockKeysFromPattern(lockKeys, planCode, CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK,
                    planCode, "*", detail.getWarehouseId(), detail.getWarehouseLocation(), detail.getSkuId(), inventoryStatus));
        }
    }

    /**
     * SCAN 匹配 pattern 下 value 为本 plan 的 lockKey 并加入集合。
     */
    private void collectPlanOwnedLockKeysFromPattern(Set<String> lockKeys, String planCode, String keyPattern) {
        Collection<String> keys = redisUtil.scanKeys(keyPattern);
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        for (String lockKey : keys) {
            if (isInventoryLockOwnedByPlan(lockKey, planCode)) {
                lockKeys.add(lockKey);
            }
        }
    }

    /**
     * 无 task-keys 释锁兜底：一次性构建 wh+库位+sku+status → orgId 与仓库 org 反查表。
     */
    private InventoryLockDeriveContext buildInventoryLockDeriveContext(List<StocktakingTaskDetailEntity> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return new InventoryLockDeriveContext(Collections.emptyMap(), Collections.emptyMap());
        }
        return new InventoryLockDeriveContext(buildInventoryOrgIdsLookupByDetails(detailList),
                resolveWarehouseOrgMap(detailList));
    }

    /**
     * 批量加载 wh+库位+sku+status → orgId 列表，供无 task-keys 释锁兜底使用。
     */
    private Map<String, List<String>> buildInventoryOrgIdsLookupByDetails(List<StocktakingTaskDetailEntity> detailList) {
        List<String> warehouseIds = detailList.stream()
                .map(StocktakingTaskDetailEntity::getWarehouseId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<String> skuIds = detailList.stream()
                .map(StocktakingTaskDetailEntity::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(warehouseIds) || CollUtil.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        List<InventoryEntity> inventoryRows = inventoryService.lambdaQuery()
                .in(InventoryEntity::getWarehouseId, warehouseIds)
                .in(InventoryEntity::getSkuId, skuIds)
                .in(InventoryEntity::getDictInventoryStatus,
                        Arrays.asList(InventoryStatusEnum.USABLE.getCode(), InventoryStatusEnum.FROZEN.getCode()))
                .list();
        if (CollUtil.isEmpty(inventoryRows)) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> lookup = new HashMap<>();
        for (InventoryEntity inventory : inventoryRows) {
            if (CharSequenceUtil.isBlank(inventory.getOrgId())) {
                continue;
            }
            String dimKey = StocktakingInventoryLockHelper.buildDimensionKeyWithoutOrg(inventory.getWarehouseId(),
                    inventory.getWarehouseLocation(), inventory.getSkuId(), inventory.getDictInventoryStatus());
            lookup.computeIfAbsent(dimKey, key -> new ArrayList<>());
            List<String> orgIds = lookup.get(dimKey);
            if (!orgIds.contains(inventory.getOrgId())) {
                orgIds.add(inventory.getOrgId());
            }
        }
        return lookup;
    }

    /**
     * 解析单条明细在指定库存状态下可能对应的 orgId 列表。
     */
    private List<String> resolveInventoryOrgIdsForDetail(StocktakingTaskDetailEntity detail, String inventoryStatus,
                                                         InventoryLockDeriveContext deriveContext) {
        String dimKey = StocktakingInventoryLockHelper.buildDimensionKeyWithoutOrg(detail.getWarehouseId(),
                detail.getWarehouseLocation(), detail.getSkuId(), inventoryStatus);
        List<String> orgIds = deriveContext.getOrgLookup().get(dimKey);
        if (CollUtil.isNotEmpty(orgIds)) {
            return orgIds;
        }
        String fallbackOrgId = deriveContext.getWarehouseOrgFallbackMap().get(detail.getWarehouseId());
        if (CharSequenceUtil.isBlank(fallbackOrgId)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(fallbackOrgId);
    }

    /**
     * 批量解析仓库 orgId，供无 task-keys 释锁兜底使用。
     */
    private Map<String, String> resolveWarehouseOrgMap(List<StocktakingTaskDetailEntity> detailList) {
        List<String> warehouseIds = detailList.stream()
                .map(StocktakingTaskDetailEntity::getWarehouseId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(warehouseIds)) {
            return Collections.emptyMap();
        }
        return warehouseService.listByIds(warehouseIds).stream()
                .filter(warehouse -> CharSequenceUtil.isNotBlank(warehouse.getOrgId()))
                .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getOrgId, (a, b) -> a));
    }

    /**
     * 从 plan-keys 索引移除单条 lockKey 成员。
     *
     * @param planCode 计划单号
     * @param lockKey  完整 Redis lock key
     */
    private void removeLockKeyFromPlanIndex(String planCode, String lockKey) {
        if (CharSequenceUtil.isBlank(planCode) || CharSequenceUtil.isBlank(lockKey)) {
            return;
        }
        String indexKey = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_PLAN_KEYS, planCode);
        redisUtil.setRemove(indexKey, lockKey);
        cleanupInventoryLockIndexIfEmpty(indexKey);
    }

    /**
     * 计划下全部在途任务已完成时，释放 plan 级 Redis 库存锁。
     * 须由已持有 planCode 分布式锁的调用方触发（避免与下推加锁竞态）。
     * 失败由外层 {@link #releaseInventoryLockByTaskWithRetry} 整段重试；本处 {@code log.warn} 保留 {@code allTasksCompleted} 排查上下文。
     */
    private void tryReleasePlanInventoryLockWhenAllTasksCompleted(StocktakingTaskEntity task) {
        if (Objects.isNull(task) || CharSequenceUtil.isBlank(task.getSourceId())
                || CharSequenceUtil.isBlank(task.getSourceCode())) {
            return;
        }
        List<StocktakingTaskEntity> planTasks = listBySourceId(task.getSourceId());
        if (CollUtil.isEmpty(planTasks)) {
            log.warn("plan 级兜底释锁：context=allTasksCompleted, reason=noPlanTasks, planCode={}, sourceId={}",
                    task.getSourceCode(), task.getSourceId());
            doReleaseInventoryLockByPlanCode(task.getSourceCode());
            return;
        }
        boolean allCompleted = planTasks.stream()
                .allMatch(planTask -> Objects.equals(planTask.getStatus(), StocktakingStatusEnum.COMPLETED));
        if (allCompleted) {
            log.warn("plan 级兜底释锁：context=allTasksCompleted, reason=allTasksCompleted, planCode={}, sourceId={}",
                    task.getSourceCode(), task.getSourceId());
            doReleaseInventoryLockByPlanCode(task.getSourceCode());
        }
    }

    /**
     * 下推生成任务后，将该任务对应库存的完整 lockKey 写入 Redis Set 索引。
     * <p>
     * 与 lock key 一致不设 TTL，由释锁/回滚时显式 {@code DEL}（见 {@link #STOCKTAKING_INVENTORY_LOCK_WRITE_BATCH_SIZE} 常量注释）。
     *
     * @param taskId        任务 id
     * @param planCode      计划单号
     * @param inventoryList 该任务覆盖的库存行（与加锁维度一致）
     */
    private void registerTaskInventoryLockKeys(String taskId, String planCode, List<InventoryEntity> inventoryList) {
        if (CharSequenceUtil.isBlank(taskId) || CollUtil.isEmpty(inventoryList)) {
            return;
        }
        String[] lockKeys = inventoryList.stream()
                .map(item -> buildInventoryLockRedisKey(planCode, item.getOrgId(), item.getWarehouseId(),
                        item.getWarehouseLocation(), item.getSkuId(), item.getDictInventoryStatus()))
                .distinct()
                .toArray(String[]::new);
        if (lockKeys.length == 0) {
            return;
        }
        String indexKey = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_TASK_KEYS, taskId);
        redisUtil.sSet(indexKey, lockKeys);
    }

    /**
     * 删除任务锁索引（计划整单删除/回滚时清理，避免 Redis 残留 Set）。
     *
     * @param taskIds 任务 id 列表
     */
    private void deleteTaskInventoryLockIndex(List<String> taskIds) {
        if (CollUtil.isEmpty(taskIds)) {
            return;
        }
        taskIds.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .map(taskId -> CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_TASK_KEYS, taskId))
                .forEach(redisUtil::del);
    }

    private boolean releaseInventoryLockIfOwnedByPlan(String lockKey, String planCode) {
        if (!isInventoryLockOwnedByPlan(lockKey, planCode)) {
            return false;
        }
        redisUtil.del(lockKey);
        return true;
    }

    /**
     * 判断 lock key 当前是否仍由指定计划占用。
     */
    private boolean isInventoryLockOwnedByPlan(String lockKey, String planCode) {
        Object existing = redisUtil.get(lockKey);
        return existing != null && Objects.equals(planCode, String.valueOf(existing));
    }

    /**
     * 判断计划级盘点库存锁是否已全部释放（含 plan-keys 索引与 planCode 前缀 SCAN 兜底）。
     */
    private boolean isPlanInventoryLockFullyReleased(String planCode) {
        String indexKey = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_PLAN_KEYS, planCode);
        if (!cleanupReleasedInventoryLockIndex(indexKey, planCode)) {
            return false;
        }
        String keyPattern = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_CODE, planCode);
        Collection<String> keys = redisUtil.scanKeys(keyPattern);
        if (CollUtil.isEmpty(keys)) {
            return true;
        }
        for (String key : keys) {
            if (isInventoryLockOwnedByPlan(key, planCode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 清理 index 中已无 Redis lock 或 value 非本计划的 stale 成员；若仍有本 plan 占用则返回 false。
     *
     * @param indexKey  plan-keys 或 task-keys 索引 key
     * @param planCode  计划单号
     * @return 是否不存在本 plan 仍占用的 lock
     */
    private boolean cleanupReleasedInventoryLockIndex(String indexKey, String planCode) {
        Set<Object> indexedKeys = redisUtil.sGet(indexKey);
        if (CollUtil.isEmpty(indexedKeys)) {
            return true;
        }
        boolean anyOwned = false;
        for (Object lockKeyObj : indexedKeys) {
            String lockKey = String.valueOf(lockKeyObj);
            if (isInventoryLockOwnedByPlan(lockKey, planCode)) {
                anyOwned = true;
            } else {
                redisUtil.setRemove(indexKey, lockKeyObj);
            }
        }
        cleanupInventoryLockIndexIfEmpty(indexKey);
        return !anyOwned;
    }

    /**
     * index Set 为空时删除 index key，避免残留空 Set。
     */
    private void cleanupInventoryLockIndexIfEmpty(String indexKey) {
        Set<Object> remaining = redisUtil.sGet(indexKey);
        if (CollUtil.isEmpty(remaining)) {
            redisUtil.del(indexKey);
        }
    }

    /**
     * 事务提交后执行释锁；无活跃事务时立即执行（如非事务入口调用的兼容路径）。
     * <p>
     * Redis 不参与 DB 事务：调用方须先完成 DB 写操作，再注册本回调；仅在外层事务成功提交后执行释锁。
     */
    private void runAfterCommit(Runnable runnable) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("盘点库存锁 afterCommit 立即执行失败", e);
            }
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    log.error("盘点库存锁 afterCommit 执行失败", e);
                }
            }
        });
    }

    /**
     * 按单库存维度 SCAN 判断是否已有其它计划占用锁；同 planCode 的锁不计入冲突。
     */
    @Override
    public boolean isInventoryLockedForStocktaking(String planCode, String orgId, String warehouseId, String warehouseLocation, String skuId, String dictInventoryStatus) {
        String normalizedLocation = StocktakingInventoryLockHelper.normalizeWarehouseLocation(warehouseLocation, dictInventoryStatus);
        String normalizedPattern = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK, "*", orgId, warehouseId,
                normalizedLocation, skuId, dictInventoryStatus);
        if (hasOtherPlanInventoryLockConflict(planCode, normalizedPattern)) {
            return true;
        }
        if (StocktakingInventoryLockHelper.isRawWarehouseLocationDifferent(warehouseLocation, dictInventoryStatus)) {
            String rawPattern = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK, "*", orgId, warehouseId,
                    StrUtils.null2EmptyWithTrim(warehouseLocation), skuId, dictInventoryStatus);
            return hasOtherPlanInventoryLockConflict(planCode, rawPattern);
        }
        return false;
    }

    /**
     * SCAN 指定 pattern 下是否存在其它 plan 占用的 lock。
     */
    private boolean hasOtherPlanInventoryLockConflict(String planCode, String keyPattern) {
        Collection<String> keys = redisUtil.scanKeys(keyPattern);
        if (CollUtil.isEmpty(keys)) {
            return false;
        }
        for (String key : keys) {
            Object existing = redisUtil.get(key);
            if (existing == null) {
                continue;
            }
            if (CharSequenceUtil.isBlank(planCode)) {
                return true;
            }
            if (!Objects.equals(planCode, String.valueOf(existing))) {
                return true;
            }
        }
        return false;
    }

    private void rollbackStocktakingTaskCreation(String planId, String planCode) {
        List<StocktakingTaskEntity> taskEntityList = listBySourceId(planId);
        List<String> taskIds = CollUtil.isEmpty(taskEntityList)
                ? Collections.emptyList()
                : taskEntityList.stream().map(StocktakingTaskEntity::getId).collect(Collectors.toList());
        boolean rollbackSuccess = false;
        try {
            stocktakingTaskRollbackService.rollbackTasksByPlanId(planId, planCode);
            rollbackSuccess = true;
        } catch (Exception rollbackException) {
            log.error("盘点任务数据库回滚失败，Redis 锁仍占用需人工处理：planId={}, planCode={}", planId, planCode, rollbackException);
        }
        if (!rollbackSuccess) {
            return;
        }
        try {
            deleteTaskInventoryLockIndex(taskIds);
        } catch (Exception indexException) {
            log.error("盘点任务回滚清理锁索引失败，planId={}, planCode={}", planId, planCode, indexException);
        }
        releaseInventoryLockByPlanCodeWithRetry(planCode, "taskCreationRollback:" + planId);
    }

    /**
     * 加锁成功后将 lockKey 写入计划级索引，供计划释锁精确删除。
     * <p>
     * 与 lock key 一致不设 TTL，由释锁/回滚时显式清理（见 {@link #STOCKTAKING_INVENTORY_LOCK_WRITE_BATCH_SIZE} 常量注释）。
     *
     * @param planCode 计划单号
     * @param lockKey  完整 Redis lock key
     */
    private void registerPlanInventoryLockKey(String planCode, String lockKey) {
        if (CharSequenceUtil.isBlank(planCode) || CharSequenceUtil.isBlank(lockKey)) {
            return;
        }
        String indexKey = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK_PLAN_KEYS, planCode);
        redisUtil.sSet(indexKey, lockKey);
    }

    /**
     * 在已持有 planCode 分布式锁的临界区内加 Redis 盘点库存锁（维度 MultiLock 仍按批获取）。
     */
    private void lockInventoryForStocktakingInPlanLockScope(String planCode, List<InventoryEntity> inventoryList) {
        normalizeInventoryListLocationsForLock(inventoryList);
        validateInventoryListForStocktaking(planCode, inventoryList);
        int uniqueDimensions = countUniqueStocktakingDimensions(inventoryList);
        if (uniqueDimensions > STOCKTAKING_DIM_LOCK_LARGE_PLAN_THRESHOLD) {
            log.warn("盘点计划【{}】库存维度 {} 超过阈值 {}，plan 锁内分批维度分布式锁",
                    planCode, uniqueDimensions, STOCKTAKING_DIM_LOCK_LARGE_PLAN_THRESHOLD);
            List<InventoryEntity> dedupedList = dedupeInventoryByDimension(inventoryList);
            List<List<InventoryEntity>> batches = Lists.partition(dedupedList, STOCKTAKING_DIM_LOCK_LARGE_PLAN_THRESHOLD);
            int totalBatches = batches.size();
            for (int i = 0; i < totalBatches; i++) {
                List<InventoryEntity> batch = batches.get(i);
                log.warn("盘点计划【{}】分批加锁：batch={}/{}, dimensionCount={}", planCode, i + 1, totalBatches, batch.size());
                self.acquireStocktakingInventoryLocksAfterValidated(planCode, batch);
            }
        } else {
            self.acquireStocktakingInventoryLocksAfterValidated(planCode, inventoryList);
        }
    }

    /**
     * 在 planCode 分布式锁内完成盘点库存加锁、任务落库与 task-keys 索引注册（下推统一入口）。
     * <p>
     * 事务边界：本方法返回时 plan 锁即释放，外层 {@code createTaskList} 的 DB 事务可能尚未 commit（见类常量注释）。
     * 须经 Spring 代理调用以触发 {@code @DistributeLocker}。
     */
    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.WMS_STOCKTAKING_INVENTORY_DIM_KEY,
            keyName = "planCode",
            waiteTime = 180,
            maxRetries = 5
    )
    public void createStocktakingTasksUnderPlanLock(String planCode, StocktakingPlanEntity entity, List<InventoryEntity> inventoryList) {
        lockInventoryForStocktakingInPlanLockScope(planCode, inventoryList);
        persistStocktakingTasksFromInventory(entity, inventoryList, planCode);
    }

    /**
     * 按分单规则落库盘点任务/明细并注册 task-keys 索引（须在 planCode 分布式锁与 DB 事务内调用）。
     */
    private void persistStocktakingTasksFromInventory(StocktakingPlanEntity entity, List<InventoryEntity> inventoryList, String planCode) {
        SeparateRuleEnum separateRule = entity.getSeparateRule();
        Map<String, String> locationAreaMap = new HashMap<>();
        if (ObjectUtil.equals(separateRule, SeparateRuleEnum.WAREHOUSE_AREA)) {
            locationAreaMap = warehouseLocationService.locationAreaMap();
        }
        String format = SeparateRuleEnum.getFormatStr(separateRule);
        Map<String, String> finalLocationAreaMap = locationAreaMap;
        Map<String, List<InventoryEntity>> inventoryMap = inventoryList
                .stream()
                .collect(Collectors.groupingBy(item -> getGroupKey(separateRule, format, finalLocationAreaMap, item)));
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        String uid = userInfo.getUid();
        String username = userInfo.getUserName();
        inventoryMap.keySet().forEach(key -> {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.STOCKTAKING_TASK);
            StocktakingTaskEntity insertTask = new StocktakingTaskEntity(entity, code, uid, username);
            insertTask.setBillDate(entity.getStocktakingDate());
            this.save(insertTask);
            List<InventoryEntity> inventoryEntityList = inventoryMap.get(key);
            Map<String, List<InventoryEntity>> inventoryStatusMap = inventoryEntityList.stream()
                    .collect(Collectors.groupingBy(item -> CharSequenceUtil.format("{}_{}_{}_{}", item.getOrgId(), item.getWarehouseId(), item.getWarehouseLocation(), item.getSkuId())));
            List<StocktakingTaskDetailEntity> insertDetailList = inventoryStatusMap.keySet().stream().map(item -> {
                List<InventoryEntity> inventoryEntities = inventoryStatusMap.get(item);
                String warehouseName = resolveWarehouseNameById(inventoryEntities.get(0).getWarehouseId());
                return new StocktakingTaskDetailEntity(inventoryEntities, insertTask.getId(), warehouseName, uid, username);
            }).collect(Collectors.toList());
            stocktakingTaskDetailService.saveBatch(insertDetailList, 500);
            registerTaskInventoryLockKeys(insertTask.getId(), planCode, inventoryEntityList);
            String msg = CharSequenceUtil.format("由盘点计划【{}】自动生成盘点任务单号为【{}】单据", planCode, code);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_TASK.getCode(), insertTask.getId(), "新增单据", uid, username);
        });
    }

    /**
     * 按库存维度去重，同一 org+仓+库位+SKU+状态 仅保留首条，供大计划分批加锁使用。
     *
     * @param inventoryList 待加锁库存行
     * @return 去重后的库存行列表（保持首次出现顺序）
     */
    private List<InventoryEntity> dedupeInventoryByDimension(List<InventoryEntity> inventoryList) {
        if (CollUtil.isEmpty(inventoryList)) {
            return Collections.emptyList();
        }
        Map<String, InventoryEntity> deduped = new LinkedHashMap<>();
        for (InventoryEntity item : inventoryList) {
            String dimensionKey = buildStocktakingDimensionKey(item.getOrgId(), item.getWarehouseId(),
                    item.getWarehouseLocation(), item.getSkuId(), item.getDictInventoryStatus());
            deduped.putIfAbsent(dimensionKey, item);
        }
        return new ArrayList<>(deduped.values());
    }

    /**
     * 统计待加锁库存行的唯一维度数（去重后）。
     */
    private int countUniqueStocktakingDimensions(List<InventoryEntity> inventoryList) {
        if (CollUtil.isEmpty(inventoryList)) {
            return 0;
        }
        Set<String> dimensions = new HashSet<>();
        for (InventoryEntity item : inventoryList) {
            dimensions.add(buildStocktakingDimensionKey(item.getOrgId(), item.getWarehouseId(),
                    item.getWarehouseLocation(), item.getSkuId(), item.getDictInventoryStatus()));
        }
        return dimensions.size();
    }

    /**
     * 校验库存行后，在维度分布式锁内执行 Redis 盘点库存锁预检与写入。
     * <p>
     * 非下推主路径，下推请使用 {@link #createStocktakingTasksUnderPlanLock}。
     */
    @Override
    public void acquireStocktakingInventoryLocks(String planCode, List<InventoryEntity> inventoryList) {
        validateInventoryListForStocktaking(planCode, inventoryList);
        self.acquireStocktakingInventoryLocksAfterValidated(planCode, inventoryList);
    }

    /**
     * 在 {@code @DistributeLocker} 保护的临界区内执行盘点 Redis 库存锁预检与写入（不再重复校验库存行）。
     * 须为 public 且经 Spring 代理调用。
     *
     * @param planCode      计划单号
     * @param inventoryList 已通过校验的待加锁库存行
     */
    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.WMS_STOCKTAKING_INVENTORY_DIM_KEY,
            keyName = "inventoryList.orgId,inventoryList.warehouseId,inventoryList.warehouseLocation,inventoryList.skuId,inventoryList.dictInventoryStatus",
            waiteTime = 120,
            maxRetries = 5
    )
    public void acquireStocktakingInventoryLocksAfterValidated(String planCode, List<InventoryEntity> inventoryList) {
        lockInventoryForStocktakingUnderDimLock(planCode, inventoryList);
    }

    /**
     * 大计划分批加锁：planCode 分布式锁覆盖全部分批过程，各批内再经 {@link #acquireStocktakingInventoryLocksAfterValidated} 获取维度 MultiLock。
     * <p>
     * 非下推主路径；下推请使用 {@link #createStocktakingTasksUnderPlanLock}（含落库与 task-keys）。
     * 本方法保留供 legacy/单独加锁场景，勿接入 {@code createTaskList}。
     */
    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.WMS_STOCKTAKING_INVENTORY_DIM_KEY,
            keyName = "planCode",
            waiteTime = 180,
            maxRetries = 5
    )
    public void acquireStocktakingInventoryLocksByPlan(String planCode, List<InventoryEntity> inventoryList) {
        validateInventoryListForStocktaking(planCode, inventoryList);
        List<InventoryEntity> dedupedList = dedupeInventoryByDimension(inventoryList);
        List<List<InventoryEntity>> batches = Lists.partition(dedupedList, STOCKTAKING_DIM_LOCK_LARGE_PLAN_THRESHOLD);
        int totalBatches = batches.size();
        for (int i = 0; i < totalBatches; i++) {
            List<InventoryEntity> batch = batches.get(i);
            log.warn("盘点计划【{}】分批加锁：batch={}/{}, dimensionCount={}", planCode, i + 1, totalBatches, batch.size());
            self.acquireStocktakingInventoryLocksAfterValidated(planCode, batch);
        }
    }

    /**
     * 下推前校验库存行必填字段（org/仓/SKU/状态），防止生成无效维度锁 key。
     *
     * @param planCode      计划单号
     * @param inventoryList 待加锁库存行
     */
    private void validateInventoryListForStocktaking(String planCode, List<InventoryEntity> inventoryList) {
        if (CollUtil.isEmpty(inventoryList)) {
            return;
        }
        for (InventoryEntity item : inventoryList) {
            if (CharSequenceUtil.isBlank(item.getOrgId())) {
                log.warn("盘点计划【{}】存在无效库存记录：orgId 为空, warehouseId={}, skuId={}", planCode, item.getWarehouseId(), item.getSkuId());
                throw new ServiceException(ApiError.WH_STOCKTAKING_INVENTORY_INVALID, planCode, resolveWarehouseNameById(item.getWarehouseId()), item.getSkuNo());
            }
            if (CharSequenceUtil.isBlank(item.getWarehouseId()) || CharSequenceUtil.isBlank(item.getSkuId())) {
                log.warn("盘点计划【{}】存在无效库存记录：warehouseId={}, skuId={}", planCode, item.getWarehouseId(), item.getSkuId());
                String warehouseName = resolveWarehouseNameById(item.getWarehouseId());
                throw new ServiceException(ApiError.WH_STOCKTAKING_INVENTORY_INVALID, planCode, warehouseName, item.getSkuNo());
            }
            if (CharSequenceUtil.isBlank(item.getDictInventoryStatus())) {
                log.warn("盘点计划【{}】存在无效库存记录：dictInventoryStatus 为空, warehouseId={}, skuId={}", planCode, item.getWarehouseId(), item.getSkuId());
                throw new ServiceException(ApiError.WH_STOCKTAKING_INVENTORY_INVALID, planCode, resolveWarehouseNameById(item.getWarehouseId()), item.getSkuNo());
            }
        }
    }

    private void lockInventoryForStocktakingUnderDimLock(String planCode, List<InventoryEntity> inventoryList) {
        Set<String> checkedDimensions = new HashSet<>();
        for (List<InventoryEntity> batch : Lists.partition(inventoryList, STOCKTAKING_INVENTORY_LOCK_WRITE_BATCH_SIZE)) {
            for (InventoryEntity item : batch) {
                String normalizedLocation = normalizeWarehouseLocationForInventoryLock(item.getWarehouseLocation(), item.getDictInventoryStatus());
                String dimensionKey = buildStocktakingDimensionKey(item.getOrgId(), item.getWarehouseId(),
                        normalizedLocation, item.getSkuId(), item.getDictInventoryStatus());
                if (checkedDimensions.add(dimensionKey)
                        && isInventoryLockedForStocktaking(planCode, item.getOrgId(), item.getWarehouseId(),
                        item.getWarehouseLocation(), item.getSkuId(), item.getDictInventoryStatus())) {
                    throwStocktakingTaskExist(item);
                }
                String redisKey = buildInventoryLockRedisKey(planCode, item.getOrgId(), item.getWarehouseId(),
                        item.getWarehouseLocation(), item.getSkuId(), item.getDictInventoryStatus());
                acquireStocktakingInventoryLock(item, planCode, redisKey);
                registerPlanInventoryLockKey(planCode, redisKey);
            }
        }
    }

    /**
     * 加锁前规范化库存行库位，使 {@code @DistributeLocker} 与 Redis lock key 维度一致。
     */
    private void normalizeInventoryListLocationsForLock(List<InventoryEntity> inventoryList) {
        if (CollUtil.isEmpty(inventoryList)) {
            return;
        }
        for (InventoryEntity item : inventoryList) {
            item.setWarehouseLocation(StocktakingInventoryLockHelper.normalizeWarehouseLocation(
                    item.getWarehouseLocation(), item.getDictInventoryStatus()));
        }
    }

    /**
     * 写入盘点 Redis 库存锁；同 plan 重入时视为已持有。
     * <p>
     * 与 UAT/历史 {@code lockInventoryForStocktaking} 一致：{@code SET NX} 不设 TTL，
     * 锁由业务释锁显式删除，避免盘点周期内过早过期（见 {@link #STOCKTAKING_INVENTORY_LOCK_WRITE_BATCH_SIZE} 常量注释）。
     */
    private void acquireStocktakingInventoryLock(InventoryEntity item, String planCode, String redisKey) {
        if (Boolean.TRUE.equals(redisUtil.setIfAbsent(redisKey, planCode))) {
            return;
        }
        Object existing = redisUtil.get(redisKey);
        if (!Objects.equals(planCode, String.valueOf(existing))) {
            throwStocktakingTaskExist(item);
        }
    }

    private static String normalizeWarehouseLocationForInventoryLock(String warehouseLocation, String inventoryStatus) {
        return StocktakingInventoryLockHelper.normalizeWarehouseLocation(warehouseLocation, inventoryStatus);
    }

    /**
     * 构建完整 Redis 盘点库存 lock key（库位已规范化）。
     */
    private static String buildInventoryLockRedisKey(String planCode, String orgId, String warehouseId,
                                                     String warehouseLocation, String skuId, String inventoryStatus) {
        return CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK, planCode, orgId, warehouseId,
                normalizeWarehouseLocationForInventoryLock(warehouseLocation, inventoryStatus), skuId, inventoryStatus);
    }

    /**
     * 构建库存维度 key（与 {@code @DistributeLocker} keyName 字段组合结果一致，分隔符为 {@code |}）。
     */
    private static String buildStocktakingDimensionKey(String orgId, String warehouseId, String warehouseLocation,
                                                       String skuId, String dictInventoryStatus) {
        return StocktakingInventoryLockHelper.buildDimensionKey(orgId, warehouseId, warehouseLocation, skuId, dictInventoryStatus);
    }

    private void throwStocktakingTaskExist(InventoryEntity item) {
        String warehouseName = resolveWarehouseNameById(item.getWarehouseId());
        log.error("仓库【{}】库位【{}】 SKU【{}】【{}】库存 已存在盘点任务，不能重复创建",
                warehouseName, item.getWarehouseLocation(), item.getSkuNo(), item.getDictInventoryStatus());
        throw new ServiceException(ApiError.WH_STOCKTAKING_TASK_EXIST, warehouseName, item.getWarehouseLocation(), item.getSkuNo(), item.getDictInventoryStatus());
    }

    private String resolveWarehouseNameById(String warehouseId) {
        if (CharSequenceUtil.isBlank(warehouseId)) {
            return "";
        }
        return resolveWarehouseName(warehouseService.detailWithCache(warehouseId));
    }

    private String resolveWarehouseName(WarehouseDTO.UpdateDTO updateDTO) {
        if (ObjectUtil.isEmpty(updateDTO)) {
            return "";
        }
        if (CharSequenceUtil.isNotBlank(updateDTO.getName())) {
            return updateDTO.getName();
        }
        return CharSequenceUtil.blankToDefault(updateDTO.getKingdeeWarehouseCode(), "");
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
        // 任务信息
        String taskCode = taskEntity.getCode();
        String taskId = taskEntity.getId();
        LocalDate billDate = LocalDate.now();
        BillTypeEnum profit = BillTypeEnum.PROFIT;
        BillTypeEnum loss = BillTypeEnum.LOSS;

        // 获取盘点任务明细
        List<StocktakingTaskDetailEntity> stocktakingTaskDetailList = stocktakingTaskDetailService.listBaseByMainIds(Collections.singletonList(taskId));
        if (CollectionUtils.isEmpty(stocktakingTaskDetailList)) {
            return Collections.emptyList();
        }

        long count = stocktakingTaskDetailList.stream().filter(item -> item.getDiffQty() == 0).count();
        if (count == stocktakingTaskDetailList.size()) {
            throw new ServiceException(ApiError.WH_STOCKTAKING_NOT_NEED_PUSH,taskCode);
        }

        // 遍历 taskDetailIdList，检查是否有对应的盈亏明细
        Iterator<StocktakingTaskDetailEntity> iterator = stocktakingTaskDetailList.iterator();
        List<StocktakingProfitLossDetailEntity> stocktakingProfitLossDetailList = new ArrayList<>();
        StringBuilder stocktakingProfitLossCode = new StringBuilder();
        while (iterator.hasNext()) {
            StocktakingTaskDetailEntity detail = iterator.next();
            stocktakingProfitLossDetailList.addAll(stocktakingProfitLossDetailService.listBySourceId(detail.getId()));

            if (!CollectionUtils.isEmpty(stocktakingProfitLossDetailList) || detail.getDiffQty() == 0) {
                iterator.remove();
            }
        }

        if (!CollectionUtils.isEmpty(stocktakingProfitLossDetailList)) {
            List<String> mainIdList = stocktakingProfitLossDetailList.stream().map(item -> item.getMainId()).collect(Collectors.toList());
            List<StocktakingProfitLossEntity> stocktakingProfitLossList = stocktakingProfitLossService.listByIds(mainIdList);
            for (StocktakingProfitLossEntity stocktakingProfitLoss : stocktakingProfitLossList) {
                stocktakingProfitLossCode.append("【" +stocktakingProfitLoss.getCode() + "】 ");
            }
        }

        if (stocktakingTaskDetailList.isEmpty()) {
            throw new ServiceException(ApiError.WH_STOCKTAKING_PUSH_OVER,taskCode,stocktakingProfitLossCode);
        }

        // 获取仓库信息（用于库存组织）
        List<String> warehouseIdList = stocktakingTaskDetailList.stream()
                .map(StocktakingTaskDetailEntity::getWarehouseId)
                .collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = CollectionUtils.isNotEmpty(warehouseIdList)
                ? warehouseService.listByIds(warehouseIdList)
                : Collections.emptyList();

        // 按仓库分组
        Map<String, List<StocktakingTaskDetailEntity>> warehouseMap = stocktakingTaskDetailList.stream()
                .collect(Collectors.groupingBy(StocktakingTaskDetailEntity::getWarehouseId));

        // 最终返回的盘盈盘亏单列表
        List<StocktakingProfitLossDTO.AddDTO> addList = new ArrayList<>();

        for (Map.Entry<String, List<StocktakingTaskDetailEntity>> entry : warehouseMap.entrySet()) {
            String warehouseId = entry.getKey();
            List<StocktakingTaskDetailEntity> taskDetailList = entry.getValue();

            // 获取库存组织 ID
            String orgId = warehouseList.stream()
                    .filter(w -> w.getId().equals(warehouseId))
                    .map(WarehouseEntity::getOrgId)
                    .findFirst()
                    .orElse("");

            //按 SKU 排序（确保相同 SKU 的行在一起）
            List<StocktakingTaskDetailEntity> sortedDetails = taskDetailList.stream()
                    .sorted(Comparator.comparing(StocktakingTaskDetailEntity::getSkuNo))
                    .collect(Collectors.toList());

            //拆分盘盈明细（200 行一单，但相同 SKU 不拆分）
            List<List<StocktakingTaskDetailEntity>> profitBatches = splitBySkuAndBatch(
                    sortedDetails.stream().filter(d -> d.getDiffQty() > 0).collect(Collectors.toList()),
                    200
            );

            //拆分盘亏明细（200 行一单，但相同 SKU 不拆分）
            List<List<StocktakingTaskDetailEntity>> lossBatches = splitBySkuAndBatch(
                    sortedDetails.stream().filter(d -> d.getDiffQty() < 0).collect(Collectors.toList()),
                    200
            );

            //生成盘盈单
            for (List<StocktakingTaskDetailEntity> batch : profitBatches) {
                StocktakingProfitLossDTO.AddDTO profitAddDTO = disposeDb(taskId, taskCode, batch, billDate, profit);
                profitAddDTO.setInventoryOrgId(orgId);
                addList.add(profitAddDTO);
            }

            //生成盘亏单
            for (List<StocktakingTaskDetailEntity> batch : lossBatches) {
                StocktakingProfitLossDTO.AddDTO lossAddDTO = disposeDb(taskId, taskCode, batch, billDate, loss);
                lossAddDTO.setInventoryOrgId(orgId);
                addList.add(lossAddDTO);
            }
        }

        return addList;
    }

    /**
     * 按 SKU 排序后，200 行拆单，但相同 SKU 不拆分
     */
    private List<List<StocktakingTaskDetailEntity>> splitBySkuAndBatch(
            List<StocktakingTaskDetailEntity> details, int batchSize) {

        List<List<StocktakingTaskDetailEntity>> batches = new ArrayList<>();
        if (CollectionUtils.isEmpty(details)) {
            return batches;
        }

        List<StocktakingTaskDetailEntity> currentBatch = new ArrayList<>();

        for (int i = 0; i < details.size(); i++) {
            StocktakingTaskDetailEntity detail = details.get(i);

            // 如果当前批次为空，直接添加（新批次开始）
            if (currentBatch.isEmpty()) {
                currentBatch.add(detail);
                continue;
            }

            // 检查是否可以添加到当前批次：
            // 如果当前批次未满（< batchSize）
            // 如果当前明细的SKU与当前批次的最后一个SKU相同（允许无限扩容）
            StocktakingTaskDetailEntity lastInBatch = currentBatch.get(currentBatch.size() - 1);
            boolean canAddToCurrentBatch =
                    (currentBatch.size() < batchSize) ||
                            detail.getSkuNo().equals(lastInBatch.getSkuNo());

            if (canAddToCurrentBatch) {
                currentBatch.add(detail);
            } else {
                // 否则，结束当前批次，开始新批次
                batches.add(new ArrayList<>(currentBatch));
                currentBatch.clear();
                currentBatch.add(detail);
            }
        }

        // 添加最后一批
        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        return batches;
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

    @Override
    public Boolean pushStocktakingProfitLoss(BaseIdsDTO.IdsDTO dto) {
        List<StocktakingTaskEntity> stocktakingTaskList = this.listByIds(dto.getIds());

        for (StocktakingTaskEntity stocktakingTaskEntity : stocktakingTaskList) {
            // 组装盘盈盘亏单所需要的数据
            List<StocktakingProfitLossDTO.AddDTO> list = this.packageProfitLoss(stocktakingTaskEntity);
            stocktakingProfitLossService.batchSave(list);
            String msg = StrUtil.format("用户【{}】下推盘盈盘亏单 ", UserContext.getDefaultLoginUser().getUserName());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_TASK.getCode(), stocktakingTaskEntity.getId(), "生成盘盈盘亏单");

        }
        return Boolean.TRUE;
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
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-08 18:08
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        StocktakingTaskEntity taskEntity = this.getById(id);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException("未找到盘点任务单");
        }
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        // 审核中的数据允许审核
        if (!Objects.equals(ingStatus, taskEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        String userId = UserContext.getDefaultLoginUser().getUid();
        handleCancelProcess(dto,taskEntity, userId);
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void handleCancelProcess(ApproveDTO.CancelProcessDTO dto,StocktakingTaskEntity taskEntity, String userId) {
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
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
                List<Pair<String, String>> pairList = Collections.singletonList(taskEntity).stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());

                operateLogService.batchAddModuleOperateLog("盘点任务单【%s】取消流程", ModuleTypeEnum.STOCKTAKING_TASK.getCode(), pairList, "取消流程操作");
            }

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO assignUser(String id, List<String> userIdList) {
        StocktakingTaskEntity task = this.getById(id);
        if (Objects.isNull(task)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST);
        }
        Boolean result = stocktakingTaskUserService.assignUser(task, userIdList);

        return BatchResultDTO.success(task.getId(), task.getCode(), "分配成功");

    }

    @Override
    public Boolean exportExcel(StocktakingTaskDTO.ExportDTO params, HttpServletResponse response) {
        params.setPermissionSql(getPermissionSql(params.getPermissionSql()));
        //获取导出数据
        List<StocktakingTaskDTO.PagingViewDTO> list = baseMapper.listExport(params);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.FILE_EXPORT_DATA_EMPTY);
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
        List<StocktakingTaskDetailExcelDTO> errorList;
        try {
            errorList = StocktakingTaskDetailExcelImportHelper.importListSheets(
                    excelFile, this, stocktakingTaskDetailService, stocktakingProfitLossService,
                    warehouseService, operateLogService);
        } catch (Exception e) {
            log.error("盘点任务明细导入错误！>>>>>{}", e);
            return Boolean.FALSE;
        }
        if (errorList.size() > 0) {
            String fileName = "盘点任务明细错误信息";
            ExcelUtil.export(fileName, "error", errorList, StocktakingTaskDetailExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String excelName = "template.xlsx";
        try {
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            StocktakingTaskDetailExcelTemplateWriter.writeEmptyTemplate(output);
            output.flush();
        } catch (Exception e) {
            log.error("盘点任务单 downloadTemplate  出错了 e==={}", e);
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
    }

    @Override
    public List<StocktakingTaskEntity> listBySourceId(String sourceId) {
        return lambdaQuery()
                .eq(StocktakingTaskEntity::getSourceId, sourceId)
                .list();
    }

    @Override
    public List<StocktakingTaskEntity> listBySourceIds(List<String> sourceIds) {
        if (CollUtil.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(StocktakingTaskEntity::getSourceId, sourceIds)
                .list();
    }

    /**
     * 删除计划下属盘点任务及明细；反审核、计划删除等场景调用。
     * <p>
     * 执行顺序：先 {@code removeByMainId/removeByIds}，再注册 {@code afterCommit → releaseInventoryLockByPlanCode}；
     * 无下属任务时仍按计划单号补偿释锁，避免 DB 已空但 Redis 锁残留。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeBySourceId(String sourceId) {
        StocktakingPlanEntity planEntity = stocktakingPlanService.getById(sourceId);
        String planCode = ObjectUtil.isNotEmpty(planEntity) ? planEntity.getCode() : null;
        List<StocktakingTaskEntity> taskEntityList = listBySourceId(sourceId);
        if (CollUtil.isEmpty(taskEntityList)) {
            if (CharSequenceUtil.isNotBlank(planCode)) {
                runAfterCommit(() -> releaseInventoryLockByPlanCodeWithRetry(planCode, "removeBySourceId:" + sourceId));
            }
            return Boolean.TRUE;
        }
        List<String> mainIds = taskEntityList.stream().map(StocktakingTaskEntity::getId).collect(Collectors.toList());
        stocktakingTaskDetailService.removeByMainId(mainIds);
        this.removeByIds(mainIds);
        deleteTaskInventoryLockIndex(mainIds);
        if (CharSequenceUtil.isNotBlank(planCode)) {
            runAfterCommit(() -> releaseInventoryLockByPlanCodeWithRetry(planCode, "removeBySourceId:" + sourceId));
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createTaskList(StocktakingPlanEntity entity, List<StocktakingPlanDetailEntity> detailEntityList,Boolean isNowExecute) {
        // 1. 查询所有需要盘点的库存记录
        List<InventoryEntity> inventoryList = inventoryService.listByStocktakingType(entity, detailEntityList);
        if (CollUtil.isEmpty(inventoryList)) {
            log.error("盘点计划【{}】没有需要盘点的库存记录", entity.getId());
            return Boolean.TRUE;
        }

        if (!isNowExecute) {
            if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
                throw new ServiceException(ApiError.BILL_APPROVED_ONLY_CAN_PUSH);
            }
        }

        if (entity.getStocktakingDate().isBefore(LocalDate.now())) {
            throw new ServiceException(ApiError.WH_STOCKTAKING_BILL_DATE_NEED_GREATER_THAN_TODAY);
        }

        String planCode = entity.getCode();
        try {
            self.createStocktakingTasksUnderPlanLock(planCode, entity, inventoryList);
        } catch (Exception e) {
            rollbackStocktakingTaskCreation(entity.getId(), planCode);
            throw e;
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createTaskListByJob(StocktakingPlanEntity entity, List<StocktakingPlanDetailEntity> detailEntityList) {
        // 1. 查询所有需要盘点的库存记录
        List<InventoryEntity> inventoryList = inventoryService.listByStocktakingType(entity, detailEntityList);
        if (CollUtil.isEmpty(inventoryList)) {
            log.error("盘点计划【{}】没有需要盘点的库存记录", entity.getId());
            return Boolean.TRUE;
        }

        String planCode = entity.getCode();
        try {
            self.createStocktakingTasksUnderPlanLock(planCode, entity, inventoryList);
        } catch (Exception e) {
            rollbackStocktakingTaskCreation(entity.getId(), planCode);
            throw e;
        }
        return Boolean.TRUE;
    }

    /**
     * 无 task-keys 释锁兜底时的共用反查上下文（org 维度 lookup + 仓库 org 兜底）。
     */
    private static final class InventoryLockDeriveContext {
        private final Map<String, List<String>> orgLookup;
        private final Map<String, String> warehouseOrgFallbackMap;

        private InventoryLockDeriveContext(Map<String, List<String>> orgLookup,
                                           Map<String, String> warehouseOrgFallbackMap) {
            this.orgLookup = orgLookup;
            this.warehouseOrgFallbackMap = warehouseOrgFallbackMap;
        }

        private Map<String, List<String>> getOrgLookup() {
            return orgLookup;
        }

        private Map<String, String> getWarehouseOrgFallbackMap() {
            return warehouseOrgFallbackMap;
        }
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
