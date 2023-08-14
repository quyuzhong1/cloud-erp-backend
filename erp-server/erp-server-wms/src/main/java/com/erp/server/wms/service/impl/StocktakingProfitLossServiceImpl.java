package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.BillTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.StocktakingProfitLossMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import jdk.nashorn.internal.ir.annotations.Reference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘盈盘亏单 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Slf4j
@Service
public class StocktakingProfitLossServiceImpl extends SuperServiceImpl<StocktakingProfitLossMapper, StocktakingProfitLossEntity> implements StocktakingProfitLossService {

    @Resource
    private StocktakingTaskDetailService stocktakingTaskDetailService;

    @Resource
    private StocktakingTaskUserService stocktakingTaskUserService;

    @Resource
    private StocktakingProfitLossDetailService stocktakingProfitLossDetailService;

    @Resource
    private DocNoGenHelper docNoGenHelper;


    @Autowired
    private CommonService commonService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Autowired
    private OperateLogService operateLogService;


    /**
     * 盘点任务单审核通过生成盘盈盘亏单
     *
     * @param taskEntity
     * @return void
     * 如果当前存在事务就加入 如果不存在就创建一个新的
     * @author yl
     * @date 2023-08-10 11:52
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCreateBill(StocktakingTaskEntity taskEntity) {
        if (Objects.isNull(taskEntity)) {
            return;
        }
        //任务code
        String taskCode = taskEntity.getCode();
        //任务id
        String taskId = taskEntity.getId();
        List<StocktakingTaskDetailEntity> stocktakingTaskDetailList = stocktakingTaskDetailService.listBaseByMainIds(Arrays.asList(taskId));
        //以仓库分组
        Map<String, List<StocktakingTaskDetailEntity>> warehouseMap = stocktakingTaskDetailList.stream().collect(Collectors.groupingBy(StocktakingTaskDetailEntity::getWarehouseId));
        //盘盈盘亏单
        List<StocktakingProfitLossEntity> addList = new ArrayList<>(stocktakingTaskDetailList.size());
        //盘盈盘亏单 明细
        List<StocktakingProfitLossDetailEntity> addDetailList = new ArrayList<>(10);
        //单据日期
        LocalDate billDate = LocalDate.now();
        //盘盈
        BillTypeEnum profit = BillTypeEnum.PROFIT;
        //盘亏
        BillTypeEnum loss = BillTypeEnum.LOSS;

        for (Map.Entry<String, List<StocktakingTaskDetailEntity>> item : warehouseMap.entrySet()) {
            //仓库id
            String warehouseId = item.getKey();

            List<StocktakingTaskDetailEntity> taskDetailList = item.getValue();
            //盘盈的任务明细
            List<StocktakingTaskDetailEntity> profitDetailList = taskDetailList.stream().filter(d -> d.getDiffQty() > 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(profitDetailList)) {
                //处理数据
                Map<String, Object> profitMap = disposeDb(taskId, taskCode, profitDetailList, billDate, profit);
                StocktakingProfitLossEntity profitEntity = (StocktakingProfitLossEntity) profitMap.get("stocktakingProfitLoss");
                List<StocktakingProfitLossDetailEntity> profitDetailEntityList = (List<StocktakingProfitLossDetailEntity>) profitMap.get("detailEntityList");
                if (Objects.nonNull(profitEntity)) {
                    addList.add(profitEntity);
                }
                if (CollectionUtils.isNotEmpty(profitDetailEntityList)) {
                    addDetailList.addAll(profitDetailEntityList);
                }
            }
            //盘亏的任务明细
            List<StocktakingTaskDetailEntity> lossDetailList = taskDetailList.stream().filter(d -> d.getDiffQty() < 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(lossDetailList)) {
                Map<String, Object> lossMap = disposeDb(taskId, taskCode, lossDetailList, billDate, loss);
                StocktakingProfitLossEntity lossEntity = (StocktakingProfitLossEntity) lossMap.get("stocktakingProfitLoss");
                List<StocktakingProfitLossDetailEntity> lossDetailEntityList = (List<StocktakingProfitLossDetailEntity>) lossMap.get("detailEntityList");
                if (Objects.nonNull(lossEntity)) {
                    addList.add(lossEntity);
                }
                if (CollectionUtils.isNotEmpty(lossDetailEntityList)) {
                    addDetailList.addAll(lossDetailEntityList);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(addList)) {
            this.saveBatch(addList);
        }

        if (CollectionUtils.isNotEmpty(addDetailList)) {
            stocktakingProfitLossDetailService.saveBatch(addDetailList);
        }

    }


    /**
     * tab list
     *
     * @param dto
     * @return
     */
    @Override
    public List<StocktakingProfitLossDTO.TabDTO> tabList(PermissionsDTO dto) {
        List<StocktakingProfitLossDTO.TabDTO> tabList = new ArrayList<>(3);
        List<StocktakingProfitLossDTO.TabDTO> dbList = baseMapper.tabList(dto.getPermissionSql());
        //盘亏单
        String loss = BillTypeEnum.LOSS.getCode();
        //盘赢单
        String profit = BillTypeEnum.PROFIT.getCode();
        StocktakingProfitLossDTO.TabDTO all = new StocktakingProfitLossDTO.TabDTO();
        int allCount = dbList.stream().mapToInt(StocktakingProfitLossDTO.TabDTO::getCount).sum();
        all.setTabFlag(WmsConstant.ALL);
        all.setCount(allCount);
        tabList.add(all);
        //盘赢
        StocktakingProfitLossDTO.TabDTO profitTab = new StocktakingProfitLossDTO.TabDTO();
        int profitCount = dbList.stream().filter(p -> profit.equals(p.getTabFlag())).findFirst().
                map(StocktakingProfitLossDTO.TabDTO::getCount).orElse(0);
        profitTab.setTabFlag(profit);
        profitTab.setCount(profitCount);
        tabList.add(profitTab);

        //盘亏
        StocktakingProfitLossDTO.TabDTO lossTab = new StocktakingProfitLossDTO.TabDTO();
        int lossCount = dbList.stream().filter(p -> loss.equals(p.getTabFlag())).findFirst().
                map(StocktakingProfitLossDTO.TabDTO::getCount).orElse(0);
        lossTab.setTabFlag(loss);
        lossTab.setCount(lossCount);
        tabList.add(lossTab);

        return tabList;
    }

    @Override
    public PagingVO<StocktakingProfitLossDTO.PagingViewDTO> paging(PagingDTO<StocktakingProfitLossDTO.PagingParamDTO> dto) {
        StocktakingProfitLossDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        String tabFlag = params.getTabFlag();
        //单据类型
        String billType = "";
        String profit = BillTypeEnum.PROFIT.getCode();
        String loss = BillTypeEnum.LOSS.getCode();
        if (profit.equals(tabFlag)) {
            billType = profit;
        } else if (loss.equals(tabFlag)) {
            billType = loss;
        }
        //盘点人
        String stocktakingUserId = params.getStocktakingUserId();
        List<String> taskIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(stocktakingUserId)) {
            List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listByUserIds(Arrays.asList(stocktakingUserId));
            taskIdList = taskUserList.stream().map(StocktakingTaskUserEntity::getStocktakingTaskId).collect(Collectors.toList());
        }
        IPage pageData = baseMapper.paging(query, params, billType, taskIdList);
        List<StocktakingProfitLossDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }


    /**
     * 详情
     *
     * @param id
     * @return
     */
    @Override
    public StocktakingProfitLossDTO.ViewDTO view(String id) {
        StocktakingProfitLossEntity profitLossEntity = this.getById(id);
        if (Objects.isNull(profitLossEntity)) {
            throw new ServiceException("未找到盘盈盘亏单");
        }
        StocktakingProfitLossDTO.ViewDTO view = new StocktakingProfitLossDTO.ViewDTO();
        BeanMapper.copy(profitLossEntity, view);
        BillTypeEnum billType = view.getBillType();
        view.setBillTypeName(billType.getName());
        ApproveStatusEnum approveStatus = view.getApproveStatus();
        view.setApproveStatusName(approveStatus.getName());
        String sourceId = view.getSourceId();
        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseByTaskIds(Arrays.asList(sourceId));
        //盘点人
        String stocktakingUserName = taskUserList.stream().filter(t -> sourceId.equals(t.getStocktakingTaskId())).
                map(StocktakingTaskUserEntity::getUserName).collect(Collectors.joining(","));
        view.setStocktakingUserName(stocktakingUserName);
        List<StocktakingProfitLossDetailDTO.ViewDTO> detailDbList = stocktakingProfitLossDetailService.listByMainIds(Arrays.asList(id));
        view.setDetailList(detailDbList);
        return view;
    }


    /**
     * 导出
     *
     * @param params
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-11 12:10
     */
    @Override
    public Boolean exportExcel(StocktakingProfitLossDTO.ExportDTO params, HttpServletResponse response) {
        String tabFlag = params.getTabFlag();
        //单据类型
        String billType = "";
        String profit = BillTypeEnum.PROFIT.getCode();
        String loss = BillTypeEnum.LOSS.getCode();
        if (profit.equals(tabFlag)) {
            billType = profit;
        } else if (loss.equals(tabFlag)) {
            billType = loss;
        }
        //盘点人
        String stocktakingUserId = params.getStocktakingUserId();
        List<String> taskIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(stocktakingUserId)) {
            List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listByUserIds(Arrays.asList(stocktakingUserId));
            taskIdList = taskUserList.stream().map(StocktakingTaskUserEntity::getStocktakingTaskId).collect(Collectors.toList());
        }
        //获取导出数据
        List<StocktakingProfitLossDTO.PagingViewDTO> list = baseMapper.listExport(params, billType, taskIdList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //填充数据
        fillDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/StocktakingProfitLoss.xlsx";
        String name = "盘盈盘亏单";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        List<String> dataFlagList = new ArrayList<>(2);
        dataFlagList.add("data1");
        dataFlagList.add("data2");
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("data1", list);
            List<StocktakingProfitLossDetailDTO.ViewDTO> detailList = new ArrayList<>(list.size() + 10);
            for (StocktakingProfitLossDTO.PagingViewDTO item : list) {
                detailList.addAll(item.getDetailList());
            }
            map.put("data2", detailList);

            new ExcelPrintUtils().compositeFillExport(map, response, sb.toString(), excelPath);
        } catch (Exception e) {
            log.error("盘盈盘亏单导出 出错 >>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
     * 盘盈盘亏单提交
     *
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     * @author yl
     * @date 2023-08-14 11:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id) {
        StocktakingProfitLossEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("未找到盘盈盘亏单");
        }
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        Boolean result = this.updateApproveStatus(entity.getId(), ApproveStatusEnum.APPROVE_ING);
        if(!result){
            throw new ServiceException("保存失败");
        }
        //启动流程
        startProcess(entity);

        // 操作日志
        List<Pair<String, String>> pairList = Lists.newArrayList(new Pair<>(entity.getId(), entity.getCode()));
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "盘点计划");
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), pairList, msg);
        return null;
    }

    /**
     * 启动流程
     * @param entity
     */
    public void startProcess(StocktakingProfitLossEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(commonService.getUserInfo().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 修改审核状态
     *
     * @param id
     * @param approveStatus
     * @return void
     * @author yl
     * @date 2023-08-14 12:05
     */
    public Boolean updateApproveStatus(String id, ApproveStatusEnum approveStatus) {
        return lambdaUpdate().eq(StocktakingProfitLossEntity::getId, id)
                .set(StocktakingProfitLossEntity::getApproveStatus, approveStatus)
                .update(new StocktakingProfitLossEntity());
    }


    /**
     * 填充列表数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-08-11 9:56
     */
    private void fillDb(List<StocktakingProfitLossDTO.PagingViewDTO> list) {
        List<String> sourceIdList = list.stream().map(StocktakingProfitLossDTO.PagingViewDTO::getSourceId).collect(Collectors.toList());
        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseByTaskIds(sourceIdList);

        List<String> idList = list.stream().map(StocktakingProfitLossDTO.PagingViewDTO::getId).collect(Collectors.toList());

        List<StocktakingProfitLossDetailDTO.ViewDTO> detailDbList = stocktakingProfitLossDetailService.listByMainIds(idList);
        for (StocktakingProfitLossDTO.PagingViewDTO item : list) {
            String sourceId = item.getSourceId();
            String id = item.getId();
            List<StocktakingProfitLossDetailDTO.ViewDTO> detailList = detailDbList.stream().filter(d -> d.getMainId().equals(id)).collect(Collectors.toList());
            item.setDetailList(detailList);
            BillTypeEnum billType = item.getBillType();
            item.setBillTypeName(billType.getName());
            //盘点人
            String stocktakingUserName = taskUserList.stream().filter(t -> sourceId.equals(t.getStocktakingTaskId())).
                    map(StocktakingTaskUserEntity::getUserName).collect(Collectors.joining(","));
            item.setStocktakingUserName(stocktakingUserName);

        }

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
    private Map<String, Object> disposeDb(String sourceId, String sourceCode, List<StocktakingTaskDetailEntity> detailList, LocalDate billDate, BillTypeEnum billType) {
        List<StocktakingProfitLossDetailEntity> detailEntityList = new ArrayList<>(detailList.size());
        StocktakingProfitLossEntity profitEntity = new StocktakingProfitLossEntity();
        profitEntity.setBillDate(billDate);
        profitEntity.setBillType(billType);
        profitEntity.setSourceId(sourceId);
        profitEntity.setSourceCode(sourceCode);
        String id = IdWorker.getIdStr();
        profitEntity.setId(id);
        BusinessNoTypeEnum businessNoType = BusinessNoTypeEnum.STOCKTAKING_PROFIT;
        //盘亏单
        if (BillTypeEnum.LOSS.equals(billType)) {
            businessNoType = BusinessNoTypeEnum.STOCKTAKING_LOSS;
        }

        String code = docNoGenHelper.generateCode(businessNoType);
        profitEntity.setCode(code);
        for (StocktakingTaskDetailEntity detail : detailList) {
            //明细
            StocktakingProfitLossDetailEntity profitDetail = new StocktakingProfitLossDetailEntity();
            profitDetail.setMainId(id);
            profitDetail.setDiffQty(detail.getDiffQty());
            profitDetail.setFrozenQty(detail.getFrozenQty());
            profitDetail.setQty(detail.getQty());
            profitDetail.setSkuId(detail.getSkuId());
            profitDetail.setSkuNo(detail.getSkuNo());
            profitDetail.setUsableQty(detail.getUsableQty());
            profitDetail.setWarehouseId(detail.getWarehouseId());
            profitDetail.setWarehouseLocation(detail.getWarehouseLocation());
            profitDetail.setSourceDetailId(detail.getId());
            detailEntityList.add(profitDetail);
        }

        Map<String, Object> map = new HashMap<>(2);
        map.put("stocktakingProfitLoss", profitEntity);
        map.put("detailEntityList", detailEntityList);

        return map;
    }
}
