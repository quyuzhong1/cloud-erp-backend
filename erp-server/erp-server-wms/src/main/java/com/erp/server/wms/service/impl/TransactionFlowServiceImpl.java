package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO.InOutStockSummaryPagingViewDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO.ListDailyInventoryDTO;
import com.erp.model.wms.dto.inventory.TransactionFlowDTO;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.entity.TransferOutEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.TransactionFlowMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * @Classname: TransactionFlowServiceImpl

 * @CreateTime: 2023-04-25  19:43
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class TransactionFlowServiceImpl extends SuperServiceImpl<TransactionFlowMapper, TransactionFlowEntity> implements TransactionFlowService {

    @Resource
    private TransactionFlowMapper transactionFlowMapper;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private TransferOutService transferOutService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private InventoryHisService inventoryHisService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public List<TransactionFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId) {
        List<TransactionFlowEntity> txnFlows =  lambdaQuery()
                .eq(TransactionFlowEntity::getSourceType, sourceType)
                .eq(TransactionFlowEntity::getSourceId, sourceId)
                .eq(TransactionFlowEntity::getOperationMode, InventoryOperationModeEnum.APPROVE.getCode())
                .eq(TransactionFlowEntity::getIsUnapproved, Boolean.FALSE)
                .orderByAsc(TransactionFlowEntity::getTradeTime)
                .orderByAsc(TransactionFlowEntity::getId)
                .list();

        return txnFlows;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUnapprovedById(String id, Integer version) {
        LoginUser loginUser =  UserContext.getDefaultLoginUser();
        return transactionFlowMapper.updateUnapprovedById(id, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TransactionFlowDTO param, InventoryBusinessTypeEnum businessType, String transactionRuleId, Integer afterInventoryQty, InventoryModeEnum inventoryModeEnum) {
        // 记录交易流水
        TransactionFlowEntity transactionFlowEntity = new TransactionFlowEntity();
        transactionFlowEntity.setBillDate(param.getBillDate());
        transactionFlowEntity.setInventoryId(param.getInventoryId());
        transactionFlowEntity.setInventoryDetailId(param.getInventoryDetailId());
        transactionFlowEntity.setOrgId(param.getOrgId());
        // 获取仓库名称
        WarehouseDTO.UpdateDTO warehouse = warehouseService.detailWithCache(param.getWarehouseId());
        ValidatorUtil.isTrue(Objects.nonNull(warehouse) && StrUtils.isNotEmpty(warehouse.getId()),()->new ServiceException(ApiError.ERROR_99002));

        transactionFlowEntity.setWarehouseId(param.getWarehouseId());
        transactionFlowEntity.setWarehouseName(warehouse.getName());
        // 此处修复BUG，部分库存状态不需要控制库位
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.getByCode(param.getDictInventoryStatus());
        if (Objects.equals(Boolean.FALSE, inventoryStatus.getControlLocation())) {
            transactionFlowEntity.setWarehouseLocation("");
        } else {
            transactionFlowEntity.setWarehouseLocation(param.getWarehouseLocation());
        }
        transactionFlowEntity.setDictInventoryStatus(param.getDictInventoryStatus());
        // 批次日期取库存明细表上关联的日期
        // 在途库存入库批次日期置为空
        LocalDate instockBatchDate = Objects.equals(InventoryStatusEnum.IN_TRANSIT, inventoryStatus) ? null : param.getInstockBatchDate();
        transactionFlowEntity.setInstockBatchDate(instockBatchDate);
        transactionFlowEntity.setSkuId(param.getSkuId());
        transactionFlowEntity.setSkuNo(param.getSkuNo());
        transactionFlowEntity.setSourceType(param.getSourceType());
        transactionFlowEntity.setSourceId(param.getSourceId());
        transactionFlowEntity.setSourceCode(param.getSourceCode());
        transactionFlowEntity.setSourceDetailId(param.getSourceDetailId());
        transactionFlowEntity.setDictBizType(businessType.getCode());
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        transactionFlowEntity.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "");
        transactionFlowEntity.setTradeTime(LocalDateTime.now());
        transactionFlowEntity.setTransactionRuleId(StrUtils.null2EmptyWithTrim(transactionRuleId));
        Integer qty = param.getQty();
        if(Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
            qty = qty * -1;
        }
        transactionFlowEntity.setQty(qty);
        transactionFlowEntity.setCurInventoryQty(afterInventoryQty);
        transactionFlowEntity.setOperationMode(StrUtils.null2EmptyWithTrim(param.getOperationMode()));
        transactionFlowEntity.setVersion(1);
        transactionFlowEntity.setTransactionNo(param.getTransactionNo());
        // 如果是反审核操作，字段是否反审核设置为true，否则后面对同一单据查询会把这条记录查询出来
        // TODO 后补单待定
        if(Objects.equals(transactionFlowEntity.getOperationMode(),InventoryOperationModeEnum.UN_APPROVE.getCode())) {
            transactionFlowEntity.setIsUnapproved(Boolean.TRUE);
        }
        boolean save = super.save(transactionFlowEntity);
        ValidatorUtil.isTrue(save, ()->new ServiceException("库存数据保存失败"));
    }

    @Override
    public void add(TransactionFlowEntity tradeParam, Integer afterInventoryQty) {
        // 记录交易流水
        LoginUser loginUser = UserContext.getDefaultLoginUser();

        // 复制所有参数
        TransactionFlowEntity transactionFlow = new TransactionFlowEntity();
        BeanMapper.copy(tradeParam,transactionFlow);
        // 更改指定的参数
        transactionFlow.setCurInventoryQty(afterInventoryQty);
        transactionFlow.setTradeTime(LocalDateTime.now());
        transactionFlow.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "0");
        transactionFlow.setVersion(1);

        // 个别参数设置空值
        transactionFlow.setId(null);
//        transactionFlow.setCreateUserId(null);
//        transactionFlow.setCreateUserName(null);
//        transactionFlow.setCreateTime(null);
//        transactionFlow.setUpdateUserId(null);
//        transactionFlow.setUpdateUserName(null);
//        transactionFlow.setUpdateTime(null);

        boolean save = super.save(transactionFlow);
        ValidatorUtil.isTrue(save, ()->new ServiceException("库存数据保存失败"));
    }

    @Override
    public PagingVO<InventoryDTO.TransFlowPagingViewDTO> pagingForInv(PagingDTO<InventoryDTO.TransFlowSearchParamDTO> pagingParamDTO) {
        // 显示所有的库存交易流水
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.TransFlowPagingViewDTO> pageData = this.baseMapper.pagingForInv(query, pagingParamDTO.getParams());
        fillInventoryTransactionFlowPageData(pageData.getRecords());
        return new PagingVO(pageData);
    }
    @Override
    public void exportTransFlow(InventoryDTO.ExportInvFlowSearchParamDTO param) {
        downloadTaskFeign.saveExportTask("库存流水明细导出", EXPORT_WMS_INVENTORY_TRANS_FLOW.getCode(), param);
    }

    @Override
    public PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> paging(PagingDTO<InventoryDTO.InOutStockTransFlowSearchParamDTO> pagingParamDTO) {
        // 出入库流水，只展示跟出入库交易相关的业务，且无需做状态映射
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.InOutStockTransFlowPagingViewDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        fillTransactionFlowPageData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportExcel(InventoryDTO.ExportInOutStockTransFlowSearchParamDTO param) {
        downloadTaskFeign.saveExportTask("出入库流水数据", EXPORT_WMS_INVENTORY_IN_OUT_STOCK.getCode(), param);
    }

    @Override
    public PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO> pagingSummary(PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO> pagingParamDTO) {
        // 出入库列表，展示跟出入库交易相关的业务，有些单据动作需做状态映射
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.InOutStockSummaryPagingViewDTO> pageData = this.baseMapper.pagingList(query, pagingParamDTO.getParams());
        fillTransactionSummary(pageData.getRecords(),pagingParamDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public void exportSummaryExcel(InventoryDTO.ExcelInOutStockSummarySearchParamDTO param, HttpServletResponse response) {
//        // 查询数据
//        List<InventoryDTO.InOutStockSummaryPagingViewDTO> dataList = this.baseMapper.exportSummaryList(param);
//
//        // 填充数据
//        InventoryDTO.InOutStockSummarySearchParamDTO paramD = BeanMapperUtils.map(InventoryDTO.InOutStockSummarySearchParamDTO.class, param);
//        fillTransactionSummary(dataList,paramD);
        PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO> pagingParamDTO = new PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO>();
        pagingParamDTO.setParams(BeanUtil.copyProperties(param, InventoryDTO.InOutStockSummarySearchParamDTO.class));
        pagingParamDTO.setPageSize(-1);
        pagingParamDTO.setPermissionSql(param.getPermissionSql());
        // 导出
        PagingVO<InOutStockSummaryPagingViewDTO> pagingSummary = this.pagingSummary(pagingParamDTO);
        List<InOutStockSummaryPagingViewDTO> dataList = (List<InOutStockSummaryPagingViewDTO>)pagingSummary.getList();
        exportTransactionSummaryExcel(dataList, response);
    }

    @Override
    public PagingVO<InventoryReportDTO.TransportPagingDTO> transportPagingList(PagingDTO<InventoryReportDTO.TransportSearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        // 在途库存大于0的才查询出来
        IPage<InventoryReportDTO.TransportPagingDTO> pageData = this.baseMapper.transportPagingList(query, pagingParamDTO.getParams());
        // 填充
        fillTransportPageData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportTransportExcel(InventoryReportDTO.ExportTransportSearchParamDTO pagingParamDTO) {
        downloadTaskFeign.saveExportTask("在途库存导出", EXPORT_WMS_INVENTORY_TRANSPORT.getCode(), pagingParamDTO);
    }

    @Override
    public PagingVO<InventoryReportDTO.ListTransportPagingDTO> transportList(PagingDTO<InventoryReportDTO.ListTransportSearchParam> pagingParamDTO) {
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        // 在途库存大于0的才查询出来
        IPage<InventoryReportDTO.ListTransportPagingDTO> pageData = this.baseMapper.transportList(query, pagingParamDTO.getParams());
        fillTransportListData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void overrideInventoryFlow(LocalDate startDate, String inventoryId, String orgName) {
        log.info("###TransactionFlowServiceImpl:::overrideInventoryFlow startDate ={} orgName ={} 库存流水重算开始 inventory_id={}, start_time={}",  startDate, orgName, inventoryId, LocalDateTime.now() );
        List<TransactionFlowEntity> flowList = lambdaQuery()
                .ge(TransactionFlowEntity::getBillDate, startDate)
                .eq(TransactionFlowEntity::getInventoryId, inventoryId)
                .last("for update")
                .list();
        if(CollUtil.isEmpty(flowList)) {
            log.warn("###TransactionFlowServiceImpl>>>overrideInventoryFlow:::未找到需要重算的库存流水，组织:{}, 库存id:{}, 开始时间:{}", orgName, inventoryId, startDate);
            // 流水不存在则删除历史库存
            inventoryHisService.removeByInventoryIds(Collections.singletonList(inventoryId), startDate);
            return;
        }
        flowList= flowList.stream().sorted(Comparator.comparing(TransactionFlowEntity::getBillDate)
                        .thenComparing(TransactionFlowEntity::getTradeTime)
                        .thenComparing(TransactionFlowEntity::getId))
                .collect(Collectors.toList());
        InventoryHisEntity hisEntity = inventoryHisService.findLastInventory(inventoryId, startDate.minusDays(1));
        if(ObjectUtil.isEmpty(hisEntity)){
            hisEntity = new InventoryHisEntity(inventoryId, startDate.minusDays(1),0);
        }
        // 重算库存流水
        overrideFlowByInventoryId(flowList, hisEntity);
        // 重算历史库存
        inventoryHisService.overrideInventoryHis(flowList, hisEntity);
        log.info("###TransactionFlowServiceImpl:::overrideInventoryFlow 库存流水重算完成 inventory_id={}, end_time={}",  inventoryId, LocalDateTime.now());
    }

    @Override
    public PagingVO<InventoryReportDTO.ListDailyInventoryDTO> dailyInventoryPaging(PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> pagingParamDTO) {
        InventoryReportDTO.DailyInventoryParamDTO params = pagingParamDTO.getParams();
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        if (ObjectUtils.isEmpty(params.getDate())) {
            params.setDate(LocalDate.now());
        }
        IPage<InventoryReportDTO.ListDailyInventoryDTO> pageData = baseMapper.dailyInventoryPaging(query, pagingParamDTO.getParams());
        handleDailyInventory(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportDailyInventory(InventoryReportDTO.DailyInventoryParamDTO params) {
        downloadTaskFeign.saveExportTask("每日库存导出", EXPORT_WMS_INVENTORY_DAILY.getCode(), params);
    }

    @Override
    public PagingVO<ListDailyInventoryDTO> exportInventoryDaily(PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> dto) {
        if (ObjectUtils.isEmpty(dto.getParams().getDate())) {
            dto.getParams().setDate(LocalDate.now());
        }
        PagingVO<InventoryReportDTO.ListDailyInventoryDTO> pageData = this.dailyInventoryPaging(dto);
        List<ListDailyInventoryDTO> dataList = (List<ListDailyInventoryDTO>) pageData.getList();
        // 填充
//        handleDailyInventory(dataList);
        return new PagingVO<>(dataList, pageData.getTotalCount(),dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> exportInventoryInOutStock(PagingDTO<InventoryDTO.ExportInOutStockTransFlowSearchParamDTO> dto) {
        // 出入库流水，只展示跟出入库交易相关的业务，且无需做状态映射
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<InventoryDTO.InOutStockTransFlowPagingViewDTO> page = this.baseMapper.exportList(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        fillTransactionFlowPageData(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<InOutStockSummaryPagingViewDTO> exportInOutStockSummary(PagingDTO<InventoryDTO.ExcelInOutStockSummarySearchParamDTO> dto) {

//        // 查询数据
//        List<InventoryDTO.InOutStockSummaryPagingViewDTO> dataList = this.baseMapper.exportSummaryList(param);
//
//        // 填充数据
//        InventoryDTO.InOutStockSummarySearchParamDTO paramD = BeanMapperUtils.map(InventoryDTO.InOutStockSummarySearchParamDTO.class, param);
//        fillTransactionSummary(dataList,paramD);
//        PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO> pagingParamDTO = new PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO>();
//        pagingParamDTO.setParams(BeanUtil.copyProperties(param, InventoryDTO.InOutStockSummarySearchParamDTO.class));
//        pagingParamDTO.setPageSize(-1);
//
//        // 导出
//        PagingVO<InOutStockSummaryPagingViewDTO> pagingSummary = this.pagingSummary(pagingParamDTO);
//        List<InOutStockSummaryPagingViewDTO> dataList = (List<InOutStockSummaryPagingViewDTO>)pagingSummary.getList();
//        exportTransactionSummaryExcel(dataList, response);
        return null;
    }

    @Override
    public PagingVO<InventoryDTO.TransFlowPagingViewDTO> exportInventoryTransFlow(PagingDTO<InventoryDTO.ExportInvFlowSearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<InventoryDTO.TransFlowPagingViewDTO> page = this.baseMapper.exportTransFlow(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        // 填充名称
        fillInventoryTransactionFlowPageData(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<InventoryReportDTO.TransportPagingDTO> exportInventoryTransport(PagingDTO<InventoryReportDTO.ExportTransportSearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        // 如果是否选导出处理
        if (CollUtil.isNotEmpty(dto.getParams().getItems())) {
            List<InventoryReportDTO.ExportTransportItem> checkData = dto.getParams().getItems();
            List<String> warehouseIds = checkData.stream().map(InventoryReportDTO.ExportTransportItem::getWarehouseId).distinct().collect(Collectors.toList());
            List<String> skuIds = checkData.stream().map(InventoryReportDTO.ExportTransportItem::getSkuId).distinct().collect(Collectors.toList());
            dto.getParams().setWarehouseIdList(warehouseIds);
            dto.getParams().setSkuIdList(skuIds);
        }
        // 在途库存大于0的才查询出来
        Page<InventoryReportDTO.TransportPagingDTO> page = this.baseMapper.exportTransport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollUtil.isEmpty(page.getRecords())) {
            fillTransportPageData(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<String> listByOrgId(LocalDate startDate, String orgId, String inventoryId, Boolean fromTable) {
        return baseMapper.listByOrgId(startDate, orgId, inventoryId, fromTable);
    }

    /**
     * @description: 每日库存数据处理
     * @author Will
     * @date: 2023/12/6 19:32
     * @param dataList
     */
    private void handleDailyInventory (List<InventoryReportDTO.ListDailyInventoryDTO> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        List<String> warehouseIdList = dataList.stream().map(InventoryReportDTO.ListDailyInventoryDTO::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIdList);

        List<String> orgIdList = dataList.stream().map(InventoryReportDTO.ListDailyInventoryDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);

        for (InventoryReportDTO.ListDailyInventoryDTO inventoryDTO : dataList) {
            //销售状态
            inventoryDTO.setSaleStateName(SaleStateEnum.getNameByCode(inventoryDTO.getSaleState()));
            //仓库信息
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(obj -> obj.getId().equals(inventoryDTO.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(updateDTO)) {
                inventoryDTO.setWarehouseCode(updateDTO.getKingdeeWarehouseCode());
                inventoryDTO.setWarehouseName(updateDTO.getName());
                inventoryDTO.setDisabled(updateDTO.getDisabled());
            }
            //组织信息
            BaseIdDTO.CodeDTO codeDTO = orgList.stream().filter(obj -> obj.getId().equals(inventoryDTO.getOrgId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(codeDTO)) {
                inventoryDTO.setOrgName(codeDTO.getName());
            }
        }

    }

    /**
     * 重算单条及时库存流水
     * @param flowList
     */
    private void overrideFlowByInventoryId(List<TransactionFlowEntity> flowList, InventoryHisEntity hisEntity) {
        AtomicReference<Integer> afterQty = ObjectUtil.isNotEmpty(hisEntity) ? new AtomicReference<>(hisEntity.getQty()) : new AtomicReference<>(0);
        List<TransactionFlowEntity> updateList = new ArrayList<>();
        flowList.stream().forEachOrdered(flow -> {
            afterQty.set(flow.getQty() + afterQty.get());
            updateList.add(new TransactionFlowEntity(flow.getId(), afterQty.get()));
        });
        updateBatchById(updateList);

    }

    /**
     * 填充即时库存查看交易流水其他字段值
     * @param dataList
     */
    private void fillInventoryTransactionFlowPageData(List<InventoryDTO.TransFlowPagingViewDTO> dataList) {
        if(CollUtil.isEmpty(dataList)) {
            return;
        }
        List<String> warehouseIds = dataList.stream().map(req -> req.getWarehouseId()).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);

        List<String> skuIds = dataList.stream().map(InventoryDTO.TransFlowPagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));
        dataList.stream().forEach(data->{
            if(skuMap.containsKey(data.getSkuId()) && CollUtil.isNotEmpty(skuMap.get(data.getSkuId()))) {
                SkuVO skuVO = skuMap.get(data.getSkuId()).get(0);
                data.setProductName(skuVO.getSkuName());
                data.setSpuNo(skuVO.getSpuNo());
            }
            InventorySourceTypeEnum inventorySourceType = InventorySourceTypeEnum.getByCode(data.getSourceType());
            data.setSourceTypeName(Optional.ofNullable(inventorySourceType).map(InventorySourceTypeEnum::getName).orElse(""));
            InventoryOperationModeEnum inventoryOperationMode = InventoryOperationModeEnum.getByCode(data.getOperationMode());
            data.setOperationModeName(Optional.ofNullable(inventoryOperationMode).map(InventoryOperationModeEnum::getName).orElse(""));
            InventoryStatusEnum inventoryStatus = InventoryStatusEnum.getByCode(data.getInventoryStatus());
            data.setInventoryStatusName(Optional.ofNullable(inventoryStatus).map(InventoryStatusEnum::getName).orElse(""));
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(data.getWarehouseId()) && req.getCode().equals(data.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            data.setWarehouseLocationName(warehouseLocationEntity.getName());
        });
    }

    private void fillTransactionFlowPageData(List<InventoryDTO.InOutStockTransFlowPagingViewDTO> dataList) {
        if(CollUtil.isEmpty(dataList)) {
            return;
        }
        List<String> skuIds = dataList.stream().map(InventoryDTO.InOutStockTransFlowPagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuSaleByIds(skuIds);
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));
        Map<String, SysAccountingCompanyEntity> accountingCompanyMap = Maps.newHashMap();

        List<String> sourceIdList = dataList.stream().map(InventoryDTO.InOutStockTransFlowPagingViewDTO::getSourceId).distinct().collect(Collectors.toList());
        DmpSyncTaskDTO.ListDTO listDTO = new DmpSyncTaskDTO.ListDTO(sourceIdList, PlatformEnum.KINGDEE.getDesc(), PlatformEnum.ERP.getDesc());
        List<DmpPushTaskEntity> pushTaskList = dmpMqFeign.listByParam(listDTO);
        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = dataList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIdAndCode(paramList);
        dataList.stream().forEach(data->{
            if(skuMap.containsKey(data.getSkuId()) && CollUtil.isNotEmpty(skuMap.get(data.getSkuId()))) {
                SkuVO skuVO = skuMap.get(data.getSkuId()).get(0);
                data.setProductName(skuVO.getSkuName());
                data.setSpuNo(skuVO.getSpuNo());
                data.setSaleState(skuVO.getSaleState());
            }
            SysAccountingCompanyEntity sysAccountingCompanyEntity = accountingCompanyMap.computeIfAbsent(data.getOrgId(),(v)->sysUserFeign.getCompanyById(v));
            if(Objects.nonNull(sysAccountingCompanyEntity)) {
                data.setOrgName(sysAccountingCompanyEntity.getCompanyName());
            }
            if (CollectionUtils.isNotEmpty(pushTaskList)) {
                String status = pushTaskList.stream().filter(obj -> obj.getSourceId().equals(data.getSourceId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getStatus())).orElse("");
                String statusName = SyncStatusEnum.getNameByCode(status);
                data.setSyncKingdeeStatus(status);
                data.setSyncKingdeeStatusName(statusName);
            }
            InventorySourceTypeEnum inventorySourceType = InventorySourceTypeEnum.getByCode(data.getSourceType());
            data.setSourceTypeName(Optional.ofNullable(inventorySourceType).map(InventorySourceTypeEnum::getName).orElse(""));
            InventoryOperationModeEnum inventoryOperationMode = InventoryOperationModeEnum.getByCode(data.getOperationMode());
            data.setOperationModeName(Optional.ofNullable(inventoryOperationMode).map(InventoryOperationModeEnum::getName).orElse(""));
            InventoryStatusEnum inventoryStatus = InventoryStatusEnum.getByCode(data.getInventoryStatus());
            data.setInventoryStatusName(Optional.ofNullable(inventoryStatus).map(InventoryStatusEnum::getName).orElse(""));
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> e.getWarehouseId().equals(data.getWarehouseId()) && e.getCode().equals(data.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            data.setWarehouseLocationName(warehouseLocationEntity.getName());
        });
    }

    private void fillTransactionSummary(List<InventoryDTO.InOutStockSummaryPagingViewDTO> dataList,InventoryDTO.InOutStockSummarySearchParamDTO paramDTO) {
        if(CollUtil.isEmpty(dataList)) {
            return;
        }
        List<String> skuIds = dataList.stream().map(InventoryDTO.InOutStockSummaryPagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        // 此处修复，返回的记录按sku id不是唯一的了
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));
        Map<String,WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        
        Map<String, InventoryReportDTO.ListDailyInventoryDTO> warehouseIdSkuStartMaps = new HashMap<>();
        Map<String, InventoryReportDTO.ListDailyInventoryDTO> warehouseIdSkuEndMaps = new HashMap<>();
        
        List<String> warehouseIdList = dataList.stream().filter(i -> CharSequenceUtil.isNotBlank(i.getWarehouseId()))
        		.map(InventoryDTO.InOutStockSummaryPagingViewDTO::getWarehouseId).distinct().collect(Collectors.toList());
		if(CollUtil.isNotEmpty(warehouseIdList) && CollUtil.isNotEmpty(skuIds)) {
			// 查询期初库存
	        InventoryReportDTO.DailyInventoryParamDTO startParams = new InventoryReportDTO.DailyInventoryParamDTO();
	        startParams.setDateType(paramDTO.getDateType());
	        if(skuIds.size() <= 100) {//经过UAT测试，当大于100个sku条件查询时，比没条件慢
	        	startParams.setWarehouseIdList(warehouseIdList);
	        	startParams.setSkuIdList(skuIds);
	        }
	        startParams.setDate(paramDTO.getDateList().get(0).minusDays(1L));
	        List<InventoryReportDTO.ListDailyInventoryDTO> startList = baseMapper.listDailyInventoryQty(startParams);
	        if(CollUtil.isNotEmpty(startList)) {
	        	warehouseIdSkuStartMaps = startList.stream().collect(Collectors.toMap(s -> s.getWarehouseId() + "_" + s.getSkuId(), s -> s));
	        }
	        
	        // 查询结余库存
	        startParams.setDate(paramDTO.getDateList().get(1));
	        List<InventoryReportDTO.ListDailyInventoryDTO> endList = baseMapper.listDailyInventoryQty(startParams);
	        if(CollUtil.isNotEmpty(endList)) {
	        	warehouseIdSkuEndMaps = endList.stream().collect(Collectors.toMap(s -> s.getWarehouseId() + "_" + s.getSkuId(), s -> s));
	        }
		}
        for(InventoryDTO.InOutStockSummaryPagingViewDTO data : dataList) {
            if(skuMap.containsKey(data.getSkuId())) {
                SkuVO skuVO = skuMap.get(data.getSkuId()).get(0);
                data.setProductName(skuVO.getSkuName());
                data.setProductImgUrl(skuVO.getSkuImagesUrl());
            }
            // 仓库名称赋值
            WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(data.getWarehouseId(), (v) -> warehouseService.detailWithCache(v));
            if (Objects.nonNull(warehouseDetail) && CharSequenceUtil.isNotEmpty(warehouseDetail.getId())) {
                data.setWarehouseName(warehouseDetail.getName());
            }
            
            Integer initQty = MathUtil.ZERO;
            Integer balanceQty = MathUtil.ZERO;
            if(CharSequenceUtil.isNotBlank(data.getWarehouseId()) && CharSequenceUtil.isNotBlank(data.getSkuId())) {
            	String qtyKey = data.getWarehouseId() + "_" + data.getSkuId();
                ListDailyInventoryDTO listDailyInventoryDTO = warehouseIdSkuStartMaps.get(qtyKey);
    			if (listDailyInventoryDTO != null) {
                    initQty = listDailyInventoryDTO.getBalanceQty();
                }
    			listDailyInventoryDTO = warehouseIdSkuEndMaps.get(qtyKey);
                if (listDailyInventoryDTO != null) {
                    balanceQty = listDailyInventoryDTO.getBalanceQty();
                }
            }
            data.setInitQty(initQty);
            data.setBalanceQty(balanceQty);
        }
    }

    private void fillTransportPageData(List<InventoryReportDTO.TransportPagingDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 此处优化，取最新的产品名称和产品图片，防止数据没同步过来，销售状态和SPU则不取最新的，防止查询和显示不一样
        List<String> skuIds = list.stream().map(InventoryReportDTO.TransportPagingDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, SysAccountingCompanyEntity> accountingCompanyMap = Maps.newHashMap();
        list.stream().forEach(data -> {
            // 仓库名称赋值
            WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(data.getWarehouseId(), (v) -> warehouseService.detailWithCache(v));
            if (Objects.nonNull(warehouseDetail) && CharSequenceUtil.isNotEmpty(warehouseDetail.getId())) {
                data.setWarehouseName(warehouseDetail.getName());
            }
            // 仓库组织
            SysAccountingCompanyEntity sysAccountingCompanyEntity = accountingCompanyMap.computeIfAbsent(data.getOrgId(), (v) -> sysUserFeign.getCompanyById(v));
            if (Objects.nonNull(sysAccountingCompanyEntity)) {
                data.setOrgName(sysAccountingCompanyEntity.getCompanyName());
            }
            if (skuMap.containsKey(data.getSkuId()) && CollUtil.isNotEmpty(skuMap.get(data.getSkuId()))) {
                SkuVO skuVO = skuMap.get(data.getSkuId()).get(0);
                // 产品名称
                data.setProductName(skuVO.getSkuName());
                // 产品图片
                data.setProductImgUrl(skuVO.getSkuImagesUrl());
            }
            // 销售状态名称
            data.setSaleStateName(SaleStateEnum.getNameByCode(data.getSaleState()));
        });
    }

    private void fillTransportListData(List<InventoryReportDTO.ListTransportPagingDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 采购订单单号
        List<String> purchaseCodeList = list.stream().filter(r->Objects.equals(r.getSourceType(), InventoryTransportTypeEnum.PURCHASE.getCode()) && StrUtils.isNotEmpty(r.getSourceCode()))
                .map(InventoryReportDTO.ListTransportPagingDTO::getSourceCode).distinct().collect(Collectors.toList());
        Map<String, PurchaseOrderEntity> purchaseOrderMap = Maps.newHashMap();
        if(CollUtil.isNotEmpty(purchaseCodeList)) {
            List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.getPurchaseOrderByCodes(purchaseCodeList);
            purchaseOrderMap = purchaseOrderList.stream().collect(Collectors.toMap(PurchaseOrderEntity::getCode,Function.identity()));
        }

        // 调拨出库单单号
        List<String> transferCodeList = list.stream().filter(r->Objects.equals(r.getSourceType(), InventoryTransportTypeEnum.TRANSFER.getCode()) && StrUtils.isNotEmpty(r.getSourceCode()))
                .map(InventoryReportDTO.ListTransportPagingDTO::getSourceCode).distinct().collect(Collectors.toList());
        Map<String,TransferOutEntity> transferOutMap = Maps.newHashMap();
        if(CollUtil.isNotEmpty(transferCodeList)) {
            List<TransferOutEntity> transferOutList = transferOutService.findByCodes(transferCodeList);
            transferOutMap = transferOutList.stream().collect(Collectors.toMap(TransferOutEntity::getCode,Function.identity()));
        }

        // 此处优化，取最新的产品名称和产品图片，防止数据没同步过来，销售状态和SPU则不取最新的，防止查询和显示不一样
        List<String> skuIds = list.stream().map(InventoryReportDTO.ListTransportPagingDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));
        for(InventoryReportDTO.ListTransportPagingDTO data : list) {
            if (skuMap.containsKey(data.getSkuId()) && CollUtil.isNotEmpty(skuMap.get(data.getSkuId()))) {
                // 产品名称
                SkuVO skuVO = skuMap.get(data.getSkuId()).get(0);
                data.setProductName(skuVO.getSkuName());
            }
            // 单据名称
            data.setSourceTypeName(InventoryTransportTypeEnum.getLabelByCode(data.getSourceType()));
            // 分步式调出单
            if(Objects.equals(data.getSourceType(), InventoryTransportTypeEnum.TRANSFER.getCode())) {
                TransferOutEntity transferOutEntity = transferOutMap.getOrDefault(data.getSourceCode(),new TransferOutEntity());
                data.setBillDate(transferOutEntity.getBillDate());
                data.setCreateUserId(transferOutEntity.getCreateUserId());
                data.setCreateUserName(transferOutEntity.getCreateUserName());
                data.setCreateTime(transferOutEntity.getCreateTime());
            } else if (Objects.equals(data.getSourceType(), InventoryTransportTypeEnum.PURCHASE.getCode())) {
                PurchaseOrderEntity purchaseOrderEntity = purchaseOrderMap.getOrDefault(data.getSourceCode(),new PurchaseOrderEntity());
                data.setBillDate(purchaseOrderEntity.getPurchaseDate());
                data.setCreateUserId(purchaseOrderEntity.getCreateUserId());
                data.setCreateUserName(purchaseOrderEntity.getCreateUserName());
                data.setCreateTime(purchaseOrderEntity.getCreateTime());
            }
        }
    }

    private void exportTransactionSummaryExcel(List<InventoryDTO.InOutStockSummaryPagingViewDTO> dataList, HttpServletResponse response) {
        OutputStream outputStream = null;
        // 声明一个工作簿
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFCellStyle contentCellStyle = wb.createCellStyle();
        // 水平居左
        contentCellStyle.setAlignment(HorizontalAlignment.LEFT);
        contentCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); //垂直居中
        contentCellStyle.setWrapText(true);//自动换行
        contentCellStyle.setBorderBottom(BorderStyle.THIN); //下边框
        contentCellStyle.setBorderLeft(BorderStyle.THIN);//左边框
        contentCellStyle.setBorderTop(BorderStyle.THIN);//上边框
        contentCellStyle.setBorderRight(BorderStyle.THIN);//右边框

        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 13);
        CellStyle titleStyle = wb.createCellStyle();
        // 设置水平居中
        titleStyle.setAlignment(HorizontalAlignment.LEFT);
        // 设置垂直对齐的样式为居中对齐;
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);
        // 下边框
        titleStyle.setBorderBottom(BorderStyle.THIN);
        // 左边框
        titleStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        titleStyle.setBorderTop(BorderStyle.THIN);
        // 右边框
        titleStyle.setBorderRight(BorderStyle.THIN);

        CellStyle titleNoBorderStyle = wb.createCellStyle();
        //设置水平居中
        titleNoBorderStyle.setAlignment(HorizontalAlignment.LEFT);
        //设置垂直对齐的样式为居中对齐;
        titleNoBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleNoBorderStyle.setFont(titleFont);

        // 创建sheet页
        XSSFSheet sheet = wb.createSheet("出入库列表");
        sheet.setDefaultColumnWidth(1 * 256);
        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(5, 40 * 256);
        sheet.setColumnWidth(7, 40 * 256);

        int rowNo = 0;
        // 第一行标题
        XSSFRow rowTitle0 = sheet.createRow(rowNo);
        Cell cell = rowTitle0.createCell(0);
        cell.setCellValue("");
        cell = rowTitle0.createCell(1);
        cell.setCellValue("");
        cell = rowTitle0.createCell(2);
        cell.setCellValue("");
        cell = rowTitle0.createCell(3);
        cell.setCellValue("");
        cell = rowTitle0.createCell(4);
        cell.setCellValue("");
        cell = rowTitle0.createCell(5);
        cell.setCellStyle(titleNoBorderStyle);
        cell.setCellValue("入库");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 6));
        cell = rowTitle0.createCell(6);
        cell.setCellStyle(titleNoBorderStyle);
        cell.setCellValue("出库");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 7, 8));

        ++rowNo;
        // 第二行标题
        XSSFRow rowTitle1 = sheet.createRow(rowNo);
        cell = rowTitle1.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("产品信息");
        cell = rowTitle1.createCell(1);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("SPU型号");
        cell = rowTitle1.createCell(2);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("仓库名称");
        cell = rowTitle1.createCell(3);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("期初库存");
        cell = rowTitle1.createCell(4);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("结余库存");
        cell = rowTitle1.createCell(5);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("入库汇总");
        cell = rowTitle1.createCell(6);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("入库类型/数量");
        cell = rowTitle1.createCell(7);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("出库汇总");
        cell = rowTitle1.createCell(8);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("出库类型/数量");

        // 内容行
        for(int i = 0;i < dataList.size();i++) {
            InventoryDTO.InOutStockSummaryPagingViewDTO data = dataList.get(i);
            ++rowNo;

            XSSFRow rowContent = sheet.createRow(rowNo);
            rowContent.setHeight((short) (45 * 20));
            rowContent.setHeightInPoints((short) 60);
            cell = rowContent.createCell(0);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(CharSequenceUtil.format("{}\n{}", data.getSkuNo(), data.getProductName()));
            cell = rowContent.createCell(1);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getSpuNo()));
            cell = rowContent.createCell(2);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getWarehouseName()));
            cell = rowContent.createCell(3);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getInitQty()));
            cell = rowContent.createCell(4);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getBalanceQty()));
            cell = rowContent.createCell(5);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getTotalInstockQty()));
            cell = rowContent.createCell(6);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(CharSequenceUtil.format("采购入库：{}盘盈入库：{}\n其他入库：{}退货入库：{}\n调拨入库：{}加工入库：{}\n退料入库：{}",
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getPurchaseInstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getInventoryProfitInstockQty()), 10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getOtherInstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getSaleReturnQty()), 10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getTransferInstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getMachineInstockQty()), 10, " " ),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getReturnMaterielQty()), 10, " ")));
            cell = rowContent.createCell(7);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getTotalOutstockQty()));
            cell = rowContent.createCell(8);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(CharSequenceUtil.format("采购退货：{}调拨出库：{}\n销售出库：{}盘亏出库：{}\n其他出库：{}加工出库：{}\n领料出库：{}",
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getPurchaseReturnQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getTransferOutstockQty()), 10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getSaleOutstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getInventoryLossOutstockQty()), 10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getOtherOutstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getMachineOutstockQty()),10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getReceiveMaterielQty()),10, " ")));
        }

        String fileName = CharSequenceUtil.format("出入库列表数据{}.xlsx", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            outputStream = response.getOutputStream();
            wb.write(outputStream);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

}