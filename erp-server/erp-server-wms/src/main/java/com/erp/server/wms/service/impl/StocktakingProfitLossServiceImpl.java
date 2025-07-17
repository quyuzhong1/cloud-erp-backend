package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
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
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.BillTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.kingdee.SyncKingdeeStocktakingLossService;
import com.erp.server.wms.kingdee.SyncKingdeeStocktakingProfitService;
import com.erp.server.wms.mapper.StocktakingProfitLossMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import com.google.common.collect.Lists;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_STOCKTAKING_PROFIT_LOSS;

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
    private StocktakingTaskUserService stocktakingTaskUserService;

    @Resource
    private StocktakingProfitLossDetailService stocktakingProfitLossDetailService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private WarehouseService warehouseService;


    @Resource
    private SyncKingdeeStocktakingProfitService syncKingdeeStocktakingProfitService;

    @Resource
    private SyncKingdeeStocktakingLossService syncKingdeeStocktakingLossService;


    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SyncWdtOtherInStockService syncWdtOtherInStockService;

    @Resource
    private SyncWdtOtherOutStockService syncWdtOtherOutStockService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;
    @Resource
    private AbstractWdtService abstractWdtService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
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
        profitTab.setTabFlagName(BillTypeEnum.PROFIT.getName());
        profitTab.setCount(profitCount);
        tabList.add(profitTab);

        //盘亏
        StocktakingProfitLossDTO.TabDTO lossTab = new StocktakingProfitLossDTO.TabDTO();
        int lossCount = dbList.stream().filter(p -> loss.equals(p.getTabFlag())).findFirst().
                map(StocktakingProfitLossDTO.TabDTO::getCount).orElse(0);
        lossTab.setTabFlag(loss);
        lossTab.setTabFlagName(BillTypeEnum.LOSS.getName());
        lossTab.setCount(lossCount);
        tabList.add(lossTab);

        return tabList;
    }

    @Override
    public PagingVO<StocktakingProfitLossDTO.PagingViewDTO> paging(PagingDTO<StocktakingProfitLossDTO.PagingParamDTO> dto) {
        StocktakingProfitLossDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
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
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseBySourceIdList(Arrays.asList(sourceId, id));
        List<String> userIdList = taskUserList.stream().map(StocktakingTaskUserEntity::getUserId).collect(Collectors.toList());
        view.setStocktakingUserIdList(userIdList);
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        //盘点人
        List<String> stocktakingUserIdList = taskUserList.stream().filter(t -> sourceId.equals(t.getSourceId()) || id.equals(t.getSourceId())).
                map(StocktakingTaskUserEntity::getUserId).collect(Collectors.toList());
        String stocktakingUserName = userList.stream().filter(u -> stocktakingUserIdList.contains(u.getUserId())).
                map(FindUserDTO::getUserName).collect(Collectors.joining(","));
        view.setStocktakingUserName(stocktakingUserName);

        List<StocktakingProfitLossDetailDTO.ViewDTO> detailDbList = stocktakingProfitLossDetailService.listByMainIds(Collections.singletonList(id));
        view.setDetailList(detailDbList);
        return view;
    }


    /**
     * 导出
     *
     * @param params
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-11 12:10
     */
    @Override
    public Boolean exportExcel(StocktakingProfitLossDTO.ExportDTO params) {
        downloadTaskFeign.saveDownloadTask("盘盈盘亏单", EXPORT_WMS_STOCKTAKING_PROFIT_LOSS.getCode(), params);
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
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        //启动流程
        startProcess(entity);
        Boolean result = this.updateApproveStatus(entity.getId(), ApproveStatusEnum.APPROVE_ING);
        if (!result) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        List<Pair<String, String>> pairList = Lists.newArrayList(new Pair<>(entity.getId(), entity.getCode()));
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "盘点计划");
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), pairList, "提交审核");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);

    }

    /**
     * 盘盈盘亏单审核
     *
     * @param id
     * @param dto
     * @return com.common.business.dto.base.BatchResultDTO
     * @author yl
     * @date 2023-08-14 14:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(String id, ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StringUtils.isEmpty(dto.getComment())) {
            // 审核不通过必须填写审核意见
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        StocktakingProfitLossEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("未找到盘盈盘亏单");
        }

        // 审核中的数据允许审核
        if (!Objects.equals(ApproveStatusEnum.APPROVE_ING, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 盘点仓库，库区，仓位禁用时禁止审核
        List<StocktakingProfitLossDetailDTO.ViewDTO> detailList = stocktakingProfitLossDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException("未找到盘盈盘亏单详情");
        }
        List<WarehouseDTO.WarehouseDisabledAssertDTO> assertList = detailList.stream().map(WarehouseDTO.WarehouseDisabledAssertDTO::new).collect(Collectors.toList());
        warehouseService.assertDisabled(assertList);
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        List<Pair<String, String>> pairList = Lists.newArrayList(new Pair<>(entity.getId(), entity.getCode()));
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作:【{}】 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getName(),approveType.getName());
        operateLogService.batchAddModuleOperateLog(String.format(msg, approveType.getName()).concat("【%s】").concat(StringUtils.isNotEmpty(dto.getComment()) ? String.format("，意见：%s", dto.getComment()) : ""),
                ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), pairList, "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }


    /**
     * 取消流程
     *
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     * @author yl
     * @date 2023-08-14 14:46
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id) {
        StocktakingProfitLossEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("未找到盘盈盘亏单");
        }
        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        // 审核中的数据允许审核
        if (!Objects.equals(ingStatus, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        LoginUser user = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode());
        revokeDTO.setUserId(user.getUid());
        ApiResult<ProcessManagementDTO.RevokeResultDTO> apiResult = workflowFeign.revokeProcess(revokeDTO);
        if (apiResult.isSuccess()) {
            ApproveStatusEnum waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT;
            entity.setApproveStatus(waitSubmitStatus);
            Boolean result = this.updateById(entity);
            if (result) {
                List<Pair<String, String>> pairList = Collections.singletonList(entity).stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
                String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据 取消流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getName());
                operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), pairList, "取消流程操作");
            }
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }


    /**
     * 更改金蝶同步状态
     *
     * @param id
     * @param syncKingdeeId
     * @return void
     * @author yl
     * @date 2023-08-14 17:47
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(StocktakingProfitLossEntity::getId, id)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId), StocktakingProfitLossEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    /**
     * 调用审核流程
     *
     * @param entity
     * @param dto
     */
    public void approveProcess(StocktakingProfitLossEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PURCHASE_ORDER.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
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
     * variablesMap值赋值
     * @author jack
     * @date 2025/5/27 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(StocktakingProfitLossEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<StocktakingProfitLossDetailEntity> detailList = stocktakingProfitLossDetailService.lambdaQuery().eq(StocktakingProfitLossDetailEntity::getMainId,entity.getId()).list();
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    public Boolean approveEnd(ApproveOneDTO dto, StocktakingProfitLossEntity entity) {
        if (Objects.isNull(entity)) {
            return Boolean.FALSE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        Boolean result;
            if (ApproveType.PASS.equals(dto.getType())) {
                //盘盈单
                BillTypeEnum profit = BillTypeEnum.PROFIT;
                //盘盈单
                BillTypeEnum loss = BillTypeEnum.LOSS;
                //是否盘盈
                Boolean isProfit = Objects.equals(profit, entity.getBillType());
                //是否盘亏
                Boolean isLoss = Objects.equals(loss, entity.getBillType());

                InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
                if (isProfit) {
                    inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.STOCKTAKING_PROFIT.getCode());
                }
                if (isLoss) {
                    inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.STOCKTAKING_LOSS.getCode());
                }
                List<InOutStockDTO> members = baseMapper.listInventoryInOut(Collections.singletonList(entity.getId()));
                InventoryStatusEnum inventoryStatus = InventoryStatusEnum.USABLE;
                for (InOutStockDTO member : members) {
                    member.setSourceType(InventorySourceTypeEnum.STOCKTAKING_PROFIT_LOSS);
                    Integer qty = member.getQty();
                    member.setQty(Math.abs(qty));
                    member.setInventoryStatus(inventoryStatus);
                }
                if (CollectionUtils.isNotEmpty(members)) {
                    inventoryInOutStockDTO.setParamList(members);
                    //扣减库存
                    inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
                }
                // 扣除库存后才更新状态
                result = updateForApprove(entity.getId(), approveStatus);

                DmpPushTaskEntity pushTaskEntity = new DmpPushTaskEntity();
                if (isProfit) {
                    //盘盈单同步金蝶
                    pushTaskEntity = syncKingdeeStocktakingProfitService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());

                    //审核通过的盘盈单转换为其他入库单推送到旺店通
                    syncStocktakingProfitInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
                }
                if (isLoss) {
                    //盘亏单同步金蝶
                    pushTaskEntity = syncKingdeeStocktakingLossService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());

                    //审核通过的盘亏单转换为其他出库单推送到旺店通
                    syncStocktakingLossInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
                }
                //推送金蝶
                DmpPushTaskEntity finalPushTaskEntity = pushTaskEntity;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        dmpMqFeign.sendTask(Collections.singletonList(finalPushTaskEntity));
                    }
                });
            } else {
                // 更新状态
                result = updateForApprove(entity.getId(), approveStatus);
            }
        return result;
    }

    /**
     * 将盘亏单转换为其他出库单并推送到旺店通
     *
     * @param entity      盘盈盘亏单
     * @return DmpPushTaskEntity 异步任务
     * @date: 2024-05-20
     * @author: tanmujin
     */
    private void syncStocktakingLossInfoToWdt(StocktakingProfitLossEntity entity, SyncOperateEnum syncOperateEnum) {
        List<StocktakingProfitLossDetailDTO.ViewDTO> detailList = stocktakingProfitLossDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if(detailList.isEmpty()){
            throw new ServiceException(ApiError.ERROR_95107);
        }

        HashSet<String> warehouseIdSet = new HashSet<>();
        for (StocktakingProfitLossDetailDTO.ViewDTO detail : detailList) {
            warehouseIdSet.add(detail.getWarehouseId());
        }
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        Map<String, String> thirdWarehouseMap = mappingList.stream().collect(Collectors.toMap(item1 -> item1.getSysWarehouseId(), item2 -> item2.getThirdWarehouseCode()));

        detailList = detailList.stream().filter(item -> thirdWarehouseMap.containsKey(item.getWarehouseId())).collect(Collectors.toList());
        Map<String, List<StocktakingProfitLossDetailDTO.ViewDTO>> collect = detailList.stream().collect(Collectors.groupingBy(item -> item.getWarehouseId()));
        for (Map.Entry<String, List<StocktakingProfitLossDetailDTO.ViewDTO>> entry : collect.entrySet()) {
            String warehouseId = entry.getKey();
            List<CreateOtherStockoutRequest.GoodsList> outGoodsList = new ArrayList<>();
            for (StocktakingProfitLossDetailDTO.ViewDTO viewDTO : entry.getValue()) {
                CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
                outGoods.setSpecNo(viewDTO.getSkuNo());
                outGoods.setNum(BigDecimal.valueOf(Math.abs(viewDTO.getDiffQty())));
                outGoods.setPositionNo(CharSequenceUtil.isNotBlank(viewDTO.getWarehouseLocation()) ? viewDTO.getWarehouseLocation() : "");
                outGoods.setWarehouseId(warehouseId);
                outGoodsList.add(outGoods);
            }
            abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), outGoodsList, SourceTypeEnum.OTHER_OUTSTOCK);
        }
    }

    /**
     * 将盘盈单转换为其他入库单并推送到旺店通
     *
     * @param entity      盘盈盘亏单
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    private void syncStocktakingProfitInfoToWdt(StocktakingProfitLossEntity entity, SyncOperateEnum syncOperateEnum) {
        List<StocktakingProfitLossDetailDTO.ViewDTO> detailList = stocktakingProfitLossDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if(detailList.isEmpty()){
            throw new ServiceException(ApiError.ERROR_95107);
        }

        HashSet<String> warehouseIdSet = new HashSet<>();
        for (StocktakingProfitLossDetailDTO.ViewDTO detail : detailList) {
            warehouseIdSet.add(detail.getWarehouseId());
        }
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        Map<String, String> thirdWarehouseMap = mappingList.stream().collect(Collectors.toMap(item1 -> item1.getSysWarehouseId(), item2 -> item2.getThirdWarehouseCode()));

        detailList = detailList.stream().filter(item -> thirdWarehouseMap.containsKey(item.getWarehouseId())).collect(Collectors.toList());
        Map<String, List<StocktakingProfitLossDetailDTO.ViewDTO>> collect = detailList.stream().collect(Collectors.groupingBy(item -> item.getWarehouseId()));
        for (Map.Entry<String, List<StocktakingProfitLossDetailDTO.ViewDTO>> entry : collect.entrySet()) {
            String warehouseId = entry.getKey();
            List<CreateOtherStockinRequest.GoodsList> inGoodsList = new ArrayList<>();
            for (StocktakingProfitLossDetailDTO.ViewDTO viewDTO : entry.getValue()) {
                CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
                inGoods.setSpecNo(viewDTO.getSkuNo());
                inGoods.setNum(BigDecimal.valueOf(Math.abs(viewDTO.getDiffQty())));
                inGoods.setPositionNo(CharSequenceUtil.isNotBlank(viewDTO.getWarehouseLocation()) ? viewDTO.getWarehouseLocation() : "");
                inGoods.setWarehouseId(warehouseId);
                inGoodsList.add(inGoods);
            }
            abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), inGoodsList, SourceTypeEnum.OTHER_INSTOCK);
        }
    }

    private static DmpPushWdtDTO.AddDTO generateWdtInterim(StocktakingProfitLossEntity entity, String operateCode, String outerCode, String warehouseId, String thirdWarehouseCode, List<? extends CommonCreateBillGoodsReq> goodsLists, SourceTypeEnum sourceTypeEnum) {
        DmpPushWdtDTO.AddDTO addDTO = new DmpPushWdtDTO.AddDTO();
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getCode());
        addDTO.setThirdCode(outerCode);
        addDTO.setThirdType(sourceTypeEnum.getCode());
        addDTO.setWarehouseId(warehouseId);
        addDTO.setThirdWarehouseCode(thirdWarehouseCode);
        addDTO.setOperateType(operateCode);
        List<DmpPushWdtDetailDTO> detailDTOList = BeanMapper.copyList(goodsLists, DmpPushWdtDetailDTO.class);
        addDTO.setDetailDTOList(detailDTOList);
        return addDTO;
    }

    /**
     * 添加
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-08-22 18:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(StocktakingProfitLossDTO.AddDTO dto) {
        StocktakingProfitLossEntity entity = new StocktakingProfitLossEntity();
        BeanMapper.copy(dto, entity);
        handleDb(dto.getDetailList(), entity);
        String id = IdWorker.getIdStr();
        entity.setId(id);
        if (Objects.isNull(entity.getBillDate())) {
            entity.setBillDate(LocalDate.now());
        }
        BillTypeEnum billType = dto.getBillType();
        BusinessNoTypeEnum businessNoType = BusinessNoTypeEnum.STOCKTAKING_PROFIT;
        //盘亏单
        if (BillTypeEnum.LOSS.equals(billType)) {
            businessNoType = BusinessNoTypeEnum.STOCKTAKING_LOSS;
        }
        String code = docNoGenHelper.generateCode(businessNoType);
        entity.setCode(code);
        List<StocktakingProfitLossDetailDTO.AddDTO> detailList = dto.getDetailList();
        List<StocktakingProfitLossDetailEntity> addDetailList = new ArrayList<>(detailList.size());
        for (StocktakingProfitLossDetailDTO.AddDTO item : detailList) {
            StocktakingProfitLossDetailEntity detail = new StocktakingProfitLossDetailEntity();
            detail.setMainId(id);
            detail.setDiffQty(item.getDiffQty());
            detail.setFrozenQty(item.getFrozenQty());
            detail.setQty(item.getQty());
            detail.setSkuId(item.getSkuId());
            detail.setSkuNo(item.getSkuNo());
            detail.setUsableQty(item.getUsableQty());
            detail.setWarehouseId(item.getWarehouseId());
            detail.setWarehouseLocation(item.getWarehouseLocation());
            detail.setSourceDetailId(item.getSourceDetailId());
            addDetailList.add(detail);
        }
        if (this.save(entity) && stocktakingProfitLossDetailService.saveBatch(addDetailList)) {

            //标记SKU
            List<String> skuIds = addDetailList.stream().map(StocktakingProfitLossDetailEntity::getSkuId).collect(Collectors.toList());
            plmTaskFeign.updateOccupyStatus(skuIds);

            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个【%s】单号【%s】",entity.getBillType().getName() ,code), ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), entity.getId(), "新增操作");

            stocktakingTaskUserService.addTaskUser(id, SourceTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), dto.getStocktakingUserIdList());
            return id;
        }
        return "";
    }

    /**
     * 修改盘盈盘亏单
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String update(StocktakingProfitLossDTO.UpdateDTO dto) {
        StocktakingProfitLossEntity old = super.getById(dto.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "盘盈盘亏单");
        }
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        StocktakingProfitLossEntity entity = new StocktakingProfitLossEntity();
        BeanMapper.copy(dto, entity);
        if (Objects.isNull(entity.getBillDate())) {
            entity.setBillDate(LocalDate.now());
        }
        List<StocktakingProfitLossDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        handleUpdateDb(detailList, entity);
        Boolean updateResult = this.updateById(entity);
        if (updateResult) {
            stocktakingTaskUserService.addTaskUser(entity.getId(), SourceTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), dto.getStocktakingUserIdList());
            stocktakingProfitLossDetailService.updateInfo(entity.getId(), detailList);
            operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), entity.getId(), "", "");
            return dto.getId();
        }
        return "";
    }


    /**
     * 检查数据
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-10-19 10:33
     */
    private void handleDb(List<StocktakingProfitLossDetailDTO.AddDTO> detailList, StocktakingProfitLossEntity entity) {
        //库存组织id
        String inventoryOrgId = entity.getInventoryOrgId();
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(inventoryOrgId));
        if (CollectionUtils.isEmpty(orgList)) {
            throw new ServiceException(ApiError.ERROR_INVENTORY_ORG_NOT_FOUND);
        }
        BillTypeEnum billType = entity.getBillType();
        //是否盘盈单
        Boolean isProfit = BillTypeEnum.PROFIT.equals(billType);
        entity.setInventoryOrgName(orgList.get(0).getName());
        //sku id
        List<String> skuIdList = detailList.stream().map(StocktakingProfitLossDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        //仓库集合
        List<String> warehouseIdList = detailList.stream().map(StocktakingProfitLossDetailDTO.AddDTO::getWarehouseId).collect(Collectors.toList());

        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        for (WarehouseEntity item : warehouseList) {
            String orgId = item.getOrgId();
            if (!inventoryOrgId.equals(orgId)) {
                throw new ServiceException(ApiError.ERROR_ORG_WAREHOUSE_MISMATCHING);
            }

        }

        //库位集合
        List<String> warehouseLocationList = detailList.stream().map(StocktakingProfitLossDetailDTO.AddDTO::getWarehouseLocation).collect(Collectors.toList());

        //组织
        List<String> orgIdList = Collections.singletonList(inventoryOrgId);
        InventoryDTO.ParamDTO param = new InventoryDTO.ParamDTO();
        param.setOrgIdList(orgIdList);
        param.setSkuIdList(skuIdList);
        param.setWarehouseIdList(warehouseIdList);
        param.setWarehouseLocationList(warehouseLocationList);
        //库存信息
        List<InventoryEntity> inventoryInfoList = inventoryService.listInventoryByParam(param);
        //可用库存
        String usable = InventoryStatusEnum.USABLE.getCode();
        String frozen = InventoryStatusEnum.FROZEN.getCode();
        Map<String, Integer> map = new HashMap<>();
        for (StocktakingProfitLossDetailDTO.AddDTO item : detailList) {
            String skuId = item.getSkuId();
            String warehouseId = item.getWarehouseId();
            String warehouseLocation = item.getWarehouseLocation();
            StringBuffer sb = new StringBuffer();
            sb.append(skuId);
            sb.append(warehouseId);
            sb.append(CharSequenceUtil.isNotBlank(warehouseLocation) ? warehouseLocation : "");
            String mapKey = sb.toString();
            //表示有这个key
            if (map.containsKey(mapKey)) {
                throw new ServiceException("同仓库同库位同SKU 存在多条数据");
            }
            List<InventoryEntity> inventoryList = inventoryInfoList.stream().filter(i -> i.getSkuId().equals(skuId) &&
                    i.getWarehouseId().equals(warehouseId) && i.getWarehouseLocation().equals(warehouseLocation)).collect(Collectors.toList());

            Integer qty = item.getQty();
            //可用数库存
            Integer usableQty = inventoryList.stream().filter(i -> usable.equals(i.getDictInventoryStatus())).
                    map(InventoryEntity::getQty).findFirst().orElse(0);

            //冻结库存
            Integer frozenQty = inventoryList.stream().filter(i -> frozen.equals(i.getDictInventoryStatus())).
                    map(InventoryEntity::getQty).findFirst().orElse(0);
            Integer diffQty = qty - usableQty - frozenQty;
            if (diffQty.equals(0)) {
                throw new ServiceException(ApiError.ERROR_DIFF_QTY_NOT_ZERO);
            }
            //是盘盈
            if (isProfit) {
                if (diffQty < 0) {
                    throw new ServiceException(ApiError.ERROR_PROFIT_DIFF_GREATER_ZERO);
                }
            } else {
                //盘亏单
                if (diffQty > 0) {
                    throw new ServiceException(ApiError.ERROR_LOSS_DIFF_LESS_ZERO);
                }
            }

            item.setFrozenQty(frozenQty);
            item.setUsableQty(usableQty);
            item.setDiffQty(diffQty);
            map.put(mapKey, diffQty);
        }

    }

    /**
     * 处理修改的数据
     *
     * @param detailList
     * @param entity
     * @return void
     * @author yl
     * @date 2023-10-20 12:03
     */
    public void handleUpdateDb(List<StocktakingProfitLossDetailDTO.UpdateDTO> detailList, StocktakingProfitLossEntity entity) {
        //库存组织id
        String inventoryOrgId = entity.getInventoryOrgId();
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(inventoryOrgId));
        if (CollectionUtils.isEmpty(orgList)) {
            throw new ServiceException(ApiError.ERROR_INVENTORY_ORG_NOT_FOUND);
        }
        BillTypeEnum billType = entity.getBillType();
        //是否盘盈单
        Boolean isProfit = BillTypeEnum.PROFIT.equals(billType);
        entity.setInventoryOrgName(orgList.get(0).getName());
        //sku id
        List<String> skuIdList = detailList.stream().map(StocktakingProfitLossDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        //仓库集合
        List<String> warehouseIdList = detailList.stream().map(StocktakingProfitLossDetailDTO.UpdateDTO::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        Map<String, Integer> map = new HashMap<>();
        for (WarehouseEntity item : warehouseList) {
            String orgId = item.getOrgId();
            if (!inventoryOrgId.equals(orgId)) {
                throw new ServiceException(ApiError.ERROR_ORG_WAREHOUSE_MISMATCHING);
            }

        }

        //库位集合
        List<String> warehouseLocationList = detailList.stream().map(StocktakingProfitLossDetailDTO.UpdateDTO::getWarehouseLocation).collect(Collectors.toList());

        //组织
        List<String> orgIdList = Collections.singletonList(inventoryOrgId);
        InventoryDTO.ParamDTO param = new InventoryDTO.ParamDTO();
        param.setOrgIdList(orgIdList);
        param.setSkuIdList(skuIdList);
        param.setWarehouseIdList(warehouseIdList);
        param.setWarehouseLocationList(warehouseLocationList);
        //库存信息
        List<InventoryEntity> inventoryInfoList = inventoryService.listInventoryByParam(param);
        //可用库存
        String usable = InventoryStatusEnum.USABLE.getCode();
        String frozen = InventoryStatusEnum.FROZEN.getCode();
        for (StocktakingProfitLossDetailDTO.UpdateDTO item : detailList) {
            String skuId = item.getSkuId();
            String warehouseId = item.getWarehouseId();
            String warehouseLocation = item.getWarehouseLocation();
            List<InventoryEntity> inventoryList = inventoryInfoList.stream().filter(i -> i.getSkuId().equals(skuId) &&
                    i.getWarehouseId().equals(warehouseId) && i.getWarehouseLocation().equals(warehouseLocation)).collect(Collectors.toList());

            StringBuffer sb = new StringBuffer();
            sb.append(skuId);
            sb.append(warehouseId);
            sb.append(CharSequenceUtil.isNotBlank(warehouseLocation) ? warehouseLocation : "");
            String mapKey = sb.toString();
            //表示有这个key
            if (map.containsKey(mapKey)) {
                throw new ServiceException("同仓库同库位同SKU 存在多条数据");
            }
            Integer qty = item.getQty();
            //可用数库存
            Integer usableQty = inventoryList.stream().filter(i -> usable.equals(i.getDictInventoryStatus())).
                    map(InventoryEntity::getQty).findFirst().orElse(0);

            //冻结库存
            Integer frozenQty = inventoryList.stream().filter(i -> frozen.equals(i.getDictInventoryStatus())).
                    map(InventoryEntity::getQty).findFirst().orElse(0);
            Integer diffQty = qty - usableQty - frozenQty;
            if (diffQty.equals(0)) {
                throw new ServiceException(ApiError.ERROR_DIFF_QTY_NOT_ZERO);
            }
            //是盘盈
            if (isProfit) {
                if (diffQty < 0) {
                    throw new ServiceException(ApiError.ERROR_PROFIT_DIFF_GREATER_ZERO);
                }
            } else {
                //盘亏单
                if (diffQty > 0) {
                    throw new ServiceException(ApiError.ERROR_LOSS_DIFF_LESS_ZERO);
                }
            }

            item.setFrozenQty(frozenQty);
            item.setUsableQty(usableQty);
            item.setDiffQty(diffQty);
            map.put(mapKey, diffQty);
        }
    }

    /**
     * 新增 并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-23 10:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void addAndSubmit(StocktakingProfitLossDTO.AddDTO dto) {
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        this.submit(id);

    }

    /**
     * 批量保存提交
     *
     * @param list
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-08-23 11:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StocktakingProfitLossEntity> batchSave(List<StocktakingProfitLossDTO.AddDTO> list) {
        List<StocktakingProfitLossEntity> addList = new ArrayList<>(list.size());
        List<StocktakingProfitLossDetailEntity> addDetailList = new ArrayList<>(list.size());
        for (StocktakingProfitLossDTO.AddDTO dto : list) {
            StocktakingProfitLossEntity entity = new StocktakingProfitLossEntity();
            BeanMapper.copy(dto, entity);
            String id = IdWorker.getIdStr();
            entity.setId(id);
            BillTypeEnum billType = dto.getBillType();
            BusinessNoTypeEnum businessNoType = BusinessNoTypeEnum.STOCKTAKING_PROFIT;
            //盘亏单
            if (BillTypeEnum.LOSS.equals(billType)) {
                businessNoType = BusinessNoTypeEnum.STOCKTAKING_LOSS;
            }
            String code = docNoGenHelper.generateCode(businessNoType);
            entity.setCode(code);
            entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
            addList.add(entity);
            for (StocktakingProfitLossDetailDTO.AddDTO item : dto.getDetailList()) {
                StocktakingProfitLossDetailEntity detail = new StocktakingProfitLossDetailEntity();
                detail.setMainId(id);
                detail.setDiffQty(item.getDiffQty());
                detail.setFrozenQty(item.getFrozenQty());
                detail.setQty(item.getQty());
                detail.setSkuId(item.getSkuId());
                detail.setSkuNo(item.getSkuNo());
                detail.setUsableQty(item.getUsableQty());
                detail.setWarehouseId(item.getWarehouseId());
                detail.setWarehouseLocation(item.getWarehouseLocation());
                detail.setSourceDetailId(item.getSourceDetailId());
                addDetailList.add(detail);
            }
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            this.saveBatch(addList);
        }
        if (CollectionUtils.isNotEmpty(addDetailList)) {
            stocktakingProfitLossDetailService.saveBatch(addDetailList);
        }

        return addList;
    }

    @Override
    public List<StocktakingProfitLossEntity> listBySourceId(String sourceId) {
        return this.lambdaQuery().eq(StocktakingProfitLossEntity::getSourceId, sourceId).
                eq(StocktakingProfitLossEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING).
                list();

    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-10-20 14:11
     */
    @Override
    public void updateAndSubmit(StocktakingProfitLossDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        StocktakingProfitLossEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到盘盈盘亏单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        this.removeById(id);
        // 删除明细数据
        stocktakingProfitLossDetailService.removeByMainId(id);
        // 删除盘点人
        stocktakingTaskUserService.removeBySourceId(id);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的盘盈盘亏", UserContext.getDefaultLoginUser().getUserName(), entity.getCode());
        List<StocktakingProfitLossEntity> list = Collections.singletonList(entity);
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode(), pairList, "删除操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }

    @Override
    public List<StocktakingProfitLossDetailDTO.LastDTO> maxDateByParams(List<String> warehouseIds, List<String> orgIds, List<String> skuIds) {
        if (CollectionUtils.isEmpty(warehouseIds)){
            throw new ServiceException("仓库IDS 不能为空");
        }
        if (CollectionUtils.isEmpty(orgIds)){
            throw new ServiceException("组织IDS 不能为空");
        }
        if (CollectionUtils.isEmpty(skuIds)){
            throw new ServiceException("SKU IDS不能为空");
        }
        return baseMapper.maxDateByParams(warehouseIds, orgIds, skuIds);
    }

    @Override
    public String findLastOneCode(String warehouseId, String skuId, LocalDate billDate) {
        List<String> codeList = baseMapper.findLastOneCode(warehouseId, skuId, billDate);
        return codeList.stream().findFirst().orElse("");
    }

    @Override
    public boolean checkClosed(List<String> warehouseIds, List<String> warehourseLocationList, List<String> orgIds, List<String> skuIds, LocalDate billDate) {
        // 最新盘盈盘亏单有效单据日期列表
        List<StocktakingProfitLossDetailDTO.LastDTO> lastStocktakingProfitLossList = this.maxDateByParams(warehouseIds, orgIds, skuIds);
        if (CollectionUtils.isNotEmpty(lastStocktakingProfitLossList)){
            // 已有日期之前对应仓位已审核的盘盈盘亏单
            return lastStocktakingProfitLossList.stream()
                    .anyMatch(e-> warehourseLocationList.contains(e.getWarehouseLocation()) &&
                            (billDate.isBefore(e.getBillDate()) || billDate.equals(e.getBillDate()))
                    );
        }
        return false;
    }

    @Override
    public PagingVO<StocktakingProfitLossDTO.ExportViewDTO> exportStocktakingProfitLoss(PagingDTO<StocktakingProfitLossDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        //获取导出数据
        Page<StocktakingProfitLossDTO.ExportViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        List<String> sourceIdList = page.getRecords().stream().map(StocktakingProfitLossDTO.ExportViewDTO::getSourceId).collect(Collectors.toList());
        List<String> idList = page.getRecords().stream().map(StocktakingProfitLossDTO.ExportViewDTO::getId).collect(Collectors.toList());
        sourceIdList.addAll(idList);
        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseBySourceIdList(sourceIdList);
        List<String> userIdList = taskUserList.stream().map(StocktakingTaskUserEntity::getUserId).collect(Collectors.toList());
        List<String> skuIdList = page.getRecords().stream().map(StocktakingProfitLossDTO.ExportViewDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listProductDetailByIds(skuIdList);
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        //填充数据
        for (StocktakingProfitLossDTO.ExportViewDTO item : page.getRecords()) {
            BillTypeEnum type = item.getBillType();
            item.setBillTypeName(type.getName());
            String sourceId = item.getSourceId();
            String id = item.getId();
            //盘点人
            List<String> stocktakingUserIdList = taskUserList.stream().filter(t -> sourceId.equals(t.getSourceId()) || id.equals(t.getSourceId())).
                    map(StocktakingTaskUserEntity::getUserId).collect(Collectors.toList());
            String stocktakingUserName = userList.stream().filter(u -> stocktakingUserIdList.contains(u.getUserId())).
                    map(FindUserDTO::getUserName).collect(Collectors.joining(","));
            item.setStocktakingUserName(stocktakingUserName);
            String skuId = item.getSkuId();
            ProductDetailEntity sku = skuList.stream().filter(s -> s.getId().equals(skuId)).findFirst().orElse(null);
            if (Objects.nonNull(sku)) {
                item.setProductName(sku.getName());
                item.setUnit(sku.getUnitName());
            } else {
                item.setProductName("");
                item.setUnit("");
            }
        }
        return new PagingVO<>(page);
    }

    /**
     * 更改审核信息
     *
     * @param id
     * @param approveStatus
     */
    public Boolean updateForApprove(String id, ApproveStatusEnum approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return this.lambdaUpdate().eq(StocktakingProfitLossEntity::getId, id)
                .set(StocktakingProfitLossEntity::getApproveUserId, userInfo.getUid())
                .set(StocktakingProfitLossEntity::getApproveUserName, userInfo.getUserName())
                .set(StocktakingProfitLossEntity::getApproveStatus, approveStatus)
                .set(StocktakingProfitLossEntity::getApproveTime, LocalDateTime.now())
                .update(new StocktakingProfitLossEntity());
    }

    /**
     * 启动流程
     *
     * @param entity
     */
    public void startProcess(StocktakingProfitLossEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
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
        List<String> sourceIdList = new ArrayList<>(10);
        for (StocktakingProfitLossDTO.PagingViewDTO item : list) {
            sourceIdList.add(item.getId());
            String sourceId = item.getSourceId();
            if (CharSequenceUtil.isNotBlank(sourceId)) {
                sourceIdList.add(sourceId);
            }
        }

        List<String> skuIdList = list.stream().map(StocktakingProfitLossDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listProductDetailByIds(skuIdList);
        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseBySourceIdList(sourceIdList);
        List<String> userIdList=taskUserList.stream().map(StocktakingTaskUserEntity::getUserId).collect(Collectors.toList());
        List<FindUserDTO> userList = CollectionUtils.isNotEmpty(userIdList)?sysUserFeign.getUserListByUserIds(userIdList):Collections.emptyList();
        for (StocktakingProfitLossDTO.PagingViewDTO item : list) {
            String sourceId = item.getSourceId();
            String id = item.getId();
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            BillTypeEnum billType = item.getBillType();
            item.setBillTypeName(billType.getName());
            //盘点人
            List<String> stocktakingUserIdList = taskUserList.stream().filter(t -> sourceId.equals(t.getSourceId()) || id.equals(t.getSourceId())).
                    map(StocktakingTaskUserEntity::getUserId).collect(Collectors.toList());
            String stocktakingUserName = userList.stream().filter(u -> stocktakingUserIdList.contains(u.getUserId())).
                    map(FindUserDTO::getUserName).collect(Collectors.joining(","));
            item.setStocktakingUserName(stocktakingUserName);

            String skuId = item.getSkuId();
            ProductDetailEntity sku = skuList.stream().filter(s -> s.getId().equals(skuId)).findFirst().orElse(null);
            if (Objects.nonNull(sku)) {
                item.setProductName(sku.getName());
                item.setUnit(sku.getUnitName());
            } else {
                item.setProductName("");
                item.setUnit("");
            }

        }

    }


}
