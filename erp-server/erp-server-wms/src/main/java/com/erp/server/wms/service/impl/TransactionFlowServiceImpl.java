package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.ExportTransactionFlowDTO;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.TransactionFlowDTO;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.TransactionFlowMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.InitStockService;
import com.erp.server.wms.service.TransactionFlowService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname: TransactionFlowServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:43
 * @Author: zhangchunlin
 */
@Service
public class TransactionFlowServiceImpl extends SuperServiceImpl<TransactionFlowMapper, TransactionFlowEntity> implements TransactionFlowService {

    @Autowired
    private TransactionFlowMapper transactionFlowMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private InitStockService initStockService;

    @Override
    public List<TransactionFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId) {
        List<TransactionFlowEntity> txnFlows =  lambdaQuery().eq(TransactionFlowEntity::getSourceType, sourceType)
                .eq(TransactionFlowEntity::getSourceId, sourceId).eq(TransactionFlowEntity::getOperationMode, InventoryOperationModeEnum.APPROVE.getCode())
                .eq(TransactionFlowEntity::getIsUnapproved, Boolean.FALSE).list();
        if(CollUtil.isNotEmpty(txnFlows)) {
            txnFlows = txnFlows.stream().sorted(Comparator.comparing(TransactionFlowEntity::getCreateTime)).collect(Collectors.toList());
        }
        return txnFlows;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUnapprovedById(String id, Integer version) {
        LoginUser loginUser =  commonService.getUserInfo();
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
        transactionFlowEntity.setWarehouseLocation(param.getWarehouseLocation());
        // TODO 暂时还没有库位表
        transactionFlowEntity.setDictInventoryStatus(param.getDictInventoryStatus());
        // 批次日期取库存明细表上关联的日期
        transactionFlowEntity.setInstockBatchDate(param.getInstockBatchDate());
        transactionFlowEntity.setSkuId(param.getSkuId());
        transactionFlowEntity.setSkuNo(param.getSkuNo());
        transactionFlowEntity.setSourceType(param.getSourceType());
        transactionFlowEntity.setSourceId(param.getSourceId());
        transactionFlowEntity.setSourceCode(param.getSourceCode());
        transactionFlowEntity.setSourceDetailId(param.getSourceDetailId());
        transactionFlowEntity.setDictBizType(businessType.getCode());
        LoginUser loginUser = commonService.getUserInfo();
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
    public PagingVO<InventoryDTO.TransFlowPagingViewDTO> pagingForInv(PagingDTO<InventoryDTO.TransFlowSearchParamDTO> pagingParamDTO) {
        // 显示所有的库存交易流水
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.TransFlowPagingViewDTO> pageData = this.baseMapper.pagingForInv(query, pagingParamDTO.getParams());
        fillInventoryTransactionFlowPageData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<InventoryDTO.InOutStockTransFlowPagingViewDTO> paging(PagingDTO<InventoryDTO.InOutStockTransFlowSearchParamDTO> pagingParamDTO) {
        // 出入库流水，只展示跟出入库交易相关的业务，且无需做状态映射
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.InOutStockTransFlowPagingViewDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        fillTransactionFlowPageData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportExcel(InventoryDTO.ExportInOutStockTransFlowSearchParamDTO param, HttpServletResponse response) {
        // 出入库流水，只展示跟出入库交易相关的业务，且无需做状态映射
        List<InventoryDTO.InOutStockTransFlowPagingViewDTO> dataList = this.baseMapper.exportList(param);
        fillTransactionFlowPageData(dataList);
        List<ExportTransactionFlowDTO> resultList = BeanMapperUtils.copyList(ExportTransactionFlowDTO.class, dataList);
        String fileName = StrUtil.format("出入库流水数据{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        try {
            ExcelUtil.exportAdapt(fileName, "出入库流水数据", resultList, ExportTransactionFlowDTO.class, response, null);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public PagingVO<InventoryDTO.InOutStockSummaryPagingViewDTO> pagingSummary(PagingDTO<InventoryDTO.InOutStockSummarySearchParamDTO> pagingParamDTO) {
        // 出入库列表，展示跟出入库交易相关的业务，有些单据动作需做状态映射
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.InOutStockSummaryPagingViewDTO> pageData = this.baseMapper.pagingList(query, pagingParamDTO.getParams());
        fillTransactionSummary(pageData.getRecords(), pagingParamDTO.getParams().getDateList());
        return new PagingVO(pageData);
    }

    /**
     * 填充即时库存查看交易流水其他字段值
     * @param dataList
     */
    private void fillInventoryTransactionFlowPageData(List<InventoryDTO.TransFlowPagingViewDTO> dataList) {
        if(CollUtil.isEmpty(dataList)) {
            return;
        }
        List<String> skuIds = dataList.stream().map(InventoryDTO.TransFlowPagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        dataList.stream().forEach(data->{
            if(skuMap.containsKey(data.getSkuId())) {
                SkuVO skuVO = skuMap.get(data.getSkuId());
                data.setProductName(skuVO.getSkuName());
                data.setSpuNo(skuVO.getSpuNo());
            }
            InventorySourceTypeEnum inventorySourceType = InventorySourceTypeEnum.of(data.getSourceType());
            data.setSourceTypeName(Optional.ofNullable(inventorySourceType).map(InventorySourceTypeEnum::getName).orElse(""));
            InventoryOperationModeEnum inventoryOperationMode = InventoryOperationModeEnum.of(data.getOperationMode());
            data.setOperationModeName(Optional.ofNullable(inventoryOperationMode).map(InventoryOperationModeEnum::getName).orElse(""));
            InventoryStatusEnum inventoryStatus = InventoryStatusEnum.of(data.getInventoryStatus());
            data.setInventoryStatusName(Optional.ofNullable(inventoryStatus).map(InventoryStatusEnum::getName).orElse(""));
        });
    }

    private void fillTransactionFlowPageData(List<InventoryDTO.InOutStockTransFlowPagingViewDTO> dataList) {
        if(CollUtil.isEmpty(dataList)) {
            return;
        }
        List<String> skuIds = dataList.stream().map(InventoryDTO.InOutStockTransFlowPagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        Map<String, SysAccountingCompanyEntity> accountingCompanyMap = Maps.newHashMap();
        dataList.stream().forEach(data->{
            if(skuMap.containsKey(data.getSkuId())) {
                SkuVO skuVO = skuMap.get(data.getSkuId());
                data.setProductName(skuVO.getSkuName());
                data.setSpuNo(skuVO.getSpuNo());
            }
            SysAccountingCompanyEntity sysAccountingCompanyEntity = accountingCompanyMap.computeIfAbsent(data.getOrgId(),(v)->sysUserFeign.getCompanyById(v));
            if(Objects.nonNull(sysAccountingCompanyEntity)) {
                data.setOrgName(sysAccountingCompanyEntity.getCompanyName());
            }
            InventorySourceTypeEnum inventorySourceType = InventorySourceTypeEnum.of(data.getSourceType());
            data.setSourceTypeName(Optional.ofNullable(inventorySourceType).map(InventorySourceTypeEnum::getName).orElse(""));
            InventoryOperationModeEnum inventoryOperationMode = InventoryOperationModeEnum.of(data.getOperationMode());
            data.setOperationModeName(Optional.ofNullable(inventoryOperationMode).map(InventoryOperationModeEnum::getName).orElse(""));
            InventoryStatusEnum inventoryStatus = InventoryStatusEnum.of(data.getInventoryStatus());
            data.setInventoryStatusName(Optional.ofNullable(inventoryStatus).map(InventoryStatusEnum::getName).orElse(""));
        });
    }

    private void fillTransactionSummary(List<InventoryDTO.InOutStockSummaryPagingViewDTO> dataList, List<LocalDate> dateList) {
        if(CollUtil.isEmpty(dataList)) {
            return;
        }
        List<String> skuIds = dataList.stream().map(InventoryDTO.InOutStockSummaryPagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        // 如果dateList为空，则传值今天
        if(CollUtil.isEmpty(dateList)) {
            dateList = Lists.newArrayList(LocalDate.now(), LocalDate.now());
        }
        for(InventoryDTO.InOutStockSummaryPagingViewDTO data : dataList) {
            if(skuMap.containsKey(data.getSkuId())) {
                SkuVO skuVO = skuMap.get(data.getSkuId());
                data.setProductName(skuVO.getSkuName());
                data.setProductImgUrl(skuVO.getSkuImagesUrl());
            }
            // 查询期初库存（后续出现性能问题，单独出接口改前端调用）
            InitStockDTO.ConditionDTO condition = new InitStockDTO.ConditionDTO();
            condition.setWarehouseId(data.getWarehouseId());
            condition.setSkuId(data.getSkuId());
            condition.setDateList(dateList);
            initStockService.getInitQty(condition);
        }
    }

}