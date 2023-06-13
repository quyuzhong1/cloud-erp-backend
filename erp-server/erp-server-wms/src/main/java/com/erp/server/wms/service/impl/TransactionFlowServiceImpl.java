package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
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
        // 此处修复BUG，部分库存状态不需要控制库位
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.of(param.getDictInventoryStatus());
        if (Objects.equals(Boolean.FALSE, inventoryStatus.getControlLocation())) {
            transactionFlowEntity.setWarehouseLocation("");
        } else {
            transactionFlowEntity.setWarehouseLocation(param.getWarehouseLocation());
        }
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
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.TransFlowPagingViewDTO> pageData = this.baseMapper.pagingForInv(query, pagingParamDTO.getParams());
        fillInventoryTransactionFlowPageData(pageData.getRecords());
        return new PagingVO(pageData);
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
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<InventoryDTO.InOutStockSummaryPagingViewDTO> pageData = this.baseMapper.pagingList(query, pagingParamDTO.getParams());
        fillTransactionSummary(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportSummaryExcel(InventoryDTO.ExcelInOutStockSummarySearchParamDTO param, HttpServletResponse response) {
        OutputStream outputStream = null;
        List<InventoryDTO.InOutStockSummaryPagingViewDTO> dataList = this.baseMapper.exportSummaryList(param);
        fillTransactionSummary(dataList);
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
        //设置水平居中
        titleStyle.setAlignment(HorizontalAlignment.LEFT);
        //设置垂直对齐的样式为居中对齐;
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);
        titleStyle.setBorderBottom(BorderStyle.THIN); //下边框
        titleStyle.setBorderLeft(BorderStyle.THIN);//左边框
        titleStyle.setBorderTop(BorderStyle.THIN);//上边框
        titleStyle.setBorderRight(BorderStyle.THIN);//右边框

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
        XSSFRow rowTitle0 = sheet.createRow(rowNo);// 第一行标题
        Cell cell = rowTitle0.createCell(0);
        cell.setCellValue("");
        cell = rowTitle0.createCell(1);
        cell.setCellValue("");
        cell = rowTitle0.createCell(2);
        cell.setCellValue("");
        cell = rowTitle0.createCell(3);
        cell.setCellValue("");
        cell = rowTitle0.createCell(4);
        cell.setCellStyle(titleNoBorderStyle);
        cell.setCellValue("入库");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 5));
        cell = rowTitle0.createCell(6);
        cell.setCellStyle(titleNoBorderStyle);
        cell.setCellValue("出库");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 7));

        ++rowNo;
        XSSFRow rowTitle1 = sheet.createRow(rowNo);// 第二行标题
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
        cell.setCellValue("入库汇总");
        cell = rowTitle1.createCell(5);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("入库类型/数量");
        cell = rowTitle1.createCell(6);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("出库汇总");
        cell = rowTitle1.createCell(7);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("出库类型/数量");

        // 内容行
        for(int i = 0;i < dataList.size();i++) {
            InventoryDTO.InOutStockSummaryPagingViewDTO data = dataList.get(i);
            ++rowNo;

            XSSFRow rowContent = sheet.createRow(rowNo);
            rowContent.setHeight((short) (40 * 20));
            rowContent.setHeightInPoints((short) 50);
            cell = rowContent.createCell(0);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtil.format("{}\n{}", data.getSkuNo(), data.getProductName()));
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
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getTotalInstockQty()));
            cell = rowContent.createCell(5);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtil.format("采购入库：{}盘盈入库：{}\n其他入库：{}退货入库：{}\n调拨入库：{}加工入库：{}",
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getPurchaseInstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getInventoryProfitInstockQty()), 10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getOtherInstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getSaleReturnQty()), 10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getTransferInstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getMachineInstockQty()), 10, " " )));
            cell = rowContent.createCell(6);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtils.null2EmptyWithTrim(data.getTotalOutstockQty()));
            cell = rowContent.createCell(7);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(StrUtil.format("采购退货：{}调拨出库：{}\n销售出库：{}盘亏出库：{}\n其他出库：{}加工出库：{}",
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getPurchaseReturnQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getTransferOutstockQty()), 10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getSaleOutstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getInventoryLossOutstockQty()), 10, " "),
                    StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getOtherOutstockQty()), 10, " "), StrUtils.rightPadding(StrUtils.null2EmptyWithTrim(data.getMachineOutstockQty()),10, " ") ));
        }

        String fileName = StrUtil.format("出入库列表数据{}.xlsx", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
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

    private void fillTransactionSummary(List<InventoryDTO.InOutStockSummaryPagingViewDTO> dataList) {
        if(CollUtil.isEmpty(dataList)) {
            return;
        }
        List<String> skuIds = dataList.stream().map(InventoryDTO.InOutStockSummaryPagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        Map<String,WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        for(InventoryDTO.InOutStockSummaryPagingViewDTO data : dataList) {
            if(skuMap.containsKey(data.getSkuId())) {
                SkuVO skuVO = skuMap.get(data.getSkuId());
                data.setProductName(skuVO.getSkuName());
                data.setProductImgUrl(skuVO.getSkuImagesUrl());
            }
            // 仓库名称赋值
            WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(data.getWarehouseId(), (v) -> warehouseService.detailWithCache(v));
            if (Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId())) {
                data.setWarehouseName(warehouseDetail.getName());
            }
            // 查询期初库存（后续出现性能问题，单独出接口改前端调用）
            InitStockDTO.ConditionDTO condition = new InitStockDTO.ConditionDTO();
            condition.setWarehouseId(data.getWarehouseId());
            condition.setSkuId(data.getSkuId());
            Integer iniQty = initStockService.getInitQty(condition);
            data.setInitQty(iniQty);
        }
    }

}