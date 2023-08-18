package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.utils.RedisUtil;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.R;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SeparateRuleEnum;
import com.erp.model.wms.enums.StocktakingModeEnum;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import com.erp.model.wms.enums.StocktakingTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.StocktakingTaskMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.SuperServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;
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
    private CommonService commonService;

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
        if (StringUtils.isNotBlank(warehouseId)) {
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
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseByTaskIds(idList);
        for (StocktakingTaskDTO.PagingViewDTO item : list) {
            String id = item.getId();
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            String approveStatusName = approveStatus.getName();
            item.setApproveStatusName(approveStatusName);
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
            String stocktakingUserName = taskUserList.stream().filter(t -> id.equals(t.getStocktakingTaskId())).
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
        List<String> ids = Arrays.asList(id);
        List<StocktakingTaskEntity> taskList = this.listByIds(ids);
        String code = CollectionUtils.isNotEmpty(taskList) ? taskList.get(0).getCode() : "";
        //待审核
        ApproveStatusEnum waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT;
        //审核不通过
        ApproveStatusEnum rejectStatus = ApproveStatusEnum.REJECT;
        //审核中
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        List<ApproveStatusEnum> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = taskList.stream().filter(s -> !statusList.contains(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }

        List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listBaseByMainIds(ids);
        long zeroCount = taskDetailList.stream().filter(d -> d.getQty() < 0).count();
        if(zeroCount>0){
            throw new ServiceException("盘点数量不能为负数");
        }
        //启动审核流程
        startProcess(taskList);
        //待提交的
        List<Pair<String, String>> pairList = taskList.stream().filter(t -> waitSubmitStatus.equals(t.getApproveStatus())).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = taskList.stream().filter(t -> rejectStatus.equals(t.getApproveStatus())).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        //改状态
        Boolean result = this.updateStatus(taskList, ingStatus, StocktakingStatusEnum.IN_PROGRESS);
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", BillApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.STOCKTAKING_TASK.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.STOCKTAKING_TASK.getCode(), rejectPairList, "状态变更");
        }
        return BatchResultDTO.success(code, OperationTypeEnum.SUBMIT);

    }

    /**
     * 启动流程
     *
     * @param taskList
     */
    public void startProcess(List<StocktakingTaskEntity> taskList) {
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        String userId = commonService.getUserInfo().getUid();
        taskList.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_TASK.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(userId);
            startDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(startDTO);
        });
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * 更改状态
     *
     * @param taskList
     * @param approveStatus 审核状态
     * @return billStatus 单据状态
     */
    public Boolean updateStatus(List<StocktakingTaskEntity> taskList, ApproveStatusEnum approveStatus, StocktakingStatusEnum billStatus) {
        if (CollectionUtils.isNotEmpty(taskList)) {
            for (StocktakingTaskEntity item : taskList) {
                item.setApproveStatus(approveStatus);
                if (Objects.nonNull(billStatus)) {
                    item.setStatus(billStatus);
                }
            }
            return this.updateBatchById(taskList);
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
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseByTaskIds(Arrays.asList(id));
        //盘点人
        String stocktakingUserName = taskUserList.stream().filter(t -> id.equals(t.getStocktakingTaskId())).
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
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个盘点任务单", approveType.getName()).concat("【%s】").concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.STOCKTAKING_TASK.getCode(), pairList, "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 调用审核流程
     *
     * @param entity
     * @param dto
     */
    public void approveProcess(StocktakingTaskEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
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
            stocktakingProfitLossService.autoCreateBill(entity);
        }
        return result;
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
        LoginUser userInfo = commonService.getUserInfo();
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
        String userId = commonService.getUserInfo().getUid();
        handleCancelProcess(taskEntity, userId);
        return BatchResultDTO.success(taskEntity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
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
        revokeDTO.setBusinessKey(SourceTypeEnum.SO_INFO.getCode());
        revokeDTO.setUserId(userId);
        ApiResult<ProcessManagementDTO.RevokeResultDTO> apiResult = workflowFeign.revokeProcess(revokeDTO);
        if (apiResult.isSuccess()) {
            ApproveStatusEnum waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT;
            taskEntity.setApproveStatus(waitSubmitStatus);
            Boolean result = this.updateById(taskEntity);
            if (result) {
                List<Pair<String, String>> pairList = Arrays.asList(taskEntity).stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());

                operateLogService.batchAddModuleOperateLog("盘点任务单【%s】取消流程", ModuleTypeEnum.STOCKTAKING_TASK.getCode(), pairList, "取消流程操作");
            }

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignUser(StocktakingTaskDTO.AssignUserDTO dto) {
        List<String> idList = dto.getIds();
        List<StocktakingTaskEntity> taskList = this.listByIds(idList);
        if (CollectionUtils.isEmpty(taskList)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        Boolean result = stocktakingTaskUserService.assignUser(dto.getIds(), dto.getUserIdList());
        return result;
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
        if (StringUtils.isNotBlank(warehouseId)) {
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
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
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
            String redisKey = StrUtil.format(RedisKeyConstant.INVENTORY_LOCK, entity.getCode(), item.getOrgId(), item.getWarehouseId(), item.getWarehouseLocation(), item.getSkuId(), item.getDictInventoryStatus());
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
        inventoryMap.keySet().parallelStream().forEach(key -> {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.STOCKTAKING_TASK);
            StocktakingTaskEntity insertTask = new StocktakingTaskEntity(entity, code);
            save(insertTask);
            List<InventoryEntity> inventoryEntityList = inventoryMap.get(key);
            // 根据组织+仓库+仓位+skuId 进行分组 获取不同库存状态的库存记录
            Map<String, List<InventoryEntity>> inventoryStatusMap = inventoryEntityList.stream()
                    .collect(Collectors.groupingBy(item -> StrUtil.format("{}_{}_{}_{}", item.getOrgId(), item.getWarehouseId(), item.getWarehouseLocation(), item.getSkuId())));
            List<StocktakingTaskDetailEntity> insertDetailList = inventoryStatusMap.keySet().stream().map(item -> {
                List<InventoryEntity> inventoryEntities = inventoryStatusMap.get(item);
                WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(inventoryEntities.get(0).getWarehouseId());
                String warehouseName = ObjectUtil.isNotEmpty(updateDTO) ? updateDTO.getName() : "";
                StocktakingTaskDetailEntity detailEntity = new StocktakingTaskDetailEntity(inventoryEntities, insertTask.getId(), warehouseName);
                return detailEntity;
            }).collect(Collectors.toList());
            stocktakingTaskDetailService.saveBatch(insertDetailList, 500);
        });
        return Boolean.TRUE;
    }

    private static String getGroupKey(SeparateRuleEnum separateRule, String format, Map<String, String> finalLocationAreaMap, InventoryEntity item) {
        String location = item.getWarehouseLocation();
        if (ObjectUtil.equals(separateRule, SeparateRuleEnum.WAREHOUSE_AREA)) {
            location = ObjectUtil.isNull(finalLocationAreaMap.get(item.getWarehouseLocation())) ? "" : finalLocationAreaMap.get(item.getWarehouseLocation());
        }
        String groupKey = StrUtil.format(format, item.getWarehouseId(), location);
        return groupKey;
    }
}
