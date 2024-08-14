package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.ReportFormsManageMapper;
import com.erp.server.wms.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PURCHASE_BUSINESS;

/**
 * 报表管理
 * @Author Luo_WG
 * @Date 2023/6/12 17:34
 **/
@Service
public class ReportFormsManageServiceImpl extends SuperServiceImpl<ReportFormsManageMapper, PurchaseOrderDetailEntity> implements ReportFormsManageService {

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private PoReturnDetailService poReturnDetailService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public PagingVO<List<PurchaseBusinessGatherTableDTO.PagingViewDTO>> purchaseBusinessGatherTablePaging(PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseBusinessGatherTableDTO.PagingViewDTO> pageData = baseMapper.paging(query, pagingDTO.getParams());
        List<PurchaseBusinessGatherTableDTO.PagingViewDTO> pagingViewDTOList = pageData.getRecords();
        for (PurchaseBusinessGatherTableDTO.PagingViewDTO viewDTO : pagingViewDTOList) {
            if (viewDTO.getAvgPrice() != null) {
                viewDTO.setReturnAmount(viewDTO.getAvgPrice().multiply(BigDecimal.valueOf(viewDTO.getRefundQty() + viewDTO.getReplenishQty())));
            }
        }
        return new PagingVO(pageData);

    }

    /*@Override
    public PagingVO<List<PurchaseBusinessGatherTableDTO.PagingViewDTO>> purchaseBusinessGatherTablePaging(PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseBusinessGatherTableDTO.PagingViewDTO> pageData = baseMapper.paging(query, pagingDTO.getParams());
        List<PurchaseBusinessGatherTableDTO.PagingViewDTO> records = pageData.getRecords();
        List<String> ids = records.stream().map(PurchaseBusinessGatherTableDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<String> podIdList = new ArrayList<>();
        ids.forEach(req -> {
            podIdList.addAll(Arrays.asList(req.split(",")));
        });
        if (CollectionUtils.isEmpty(podIdList)) {
            return new PagingVO(pageData);
        }
        //查询采购签收信息
        List<WarehouseReceiveDetailEntity> warehouseReceiveDetailEntities = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIdList);
        //查询采购入库信息
        List<PoInstockDetailEntity> poInstockDetailEntities = poInstockDetailService.listDetailByPodIds(podIdList);
        //查询退货信息
        List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(podIdList);
        //查询采购明细
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = purchaseOrderDetailService.listByIds(podIdList);
        Map<String, PurchaseBusinessGatherTableDTO.PagingViewDTO> map = new HashMap();
        for (PurchaseBusinessGatherTableDTO.PagingViewDTO record : records) {
            String mapKey = record.getPurchaseOrgName() + record.getSupplierName() + record.getSpuNo() + record.getSkuNo();
            if (map.get(mapKey) != null) {
                PurchaseBusinessGatherTableDTO.PagingViewDTO mapEntity = map.get(mapKey);
                mapEntity.setOrderQty(mapEntity.getOrderQty() + 1);
                mapEntity.setOrderAmount(mapEntity.getOrderAmount().add(record.getOrderAmount()));
                mapEntity.setAvgPrice(mapEntity.getOrderAmount().divide(BigDecimal.valueOf(mapEntity.getOrderQty()), 4, BigDecimal.ROUND_DOWN));
                computeReceive(mapEntity, record, warehouseReceiveDetailEntities, pagingDTO.getParams(), purchaseOrderDetailEntityList);
                computeInstock(mapEntity, record, poInstockDetailEntities, pagingDTO.getParams(), purchaseOrderDetailEntityList);
                computeReturn(mapEntity, record, purchaseReturnOrderDetailEntities, pagingDTO.getParams());
            } else {
                map.put(mapKey, record);
                computeReceive(record, record, warehouseReceiveDetailEntities, pagingDTO.getParams(), purchaseOrderDetailEntityList);
                computeInstock(record, record, poInstockDetailEntities, pagingDTO.getParams(), purchaseOrderDetailEntityList);
                computeReturn(record, record, purchaseReturnOrderDetailEntities, pagingDTO.getParams());
            }
        }
        List<PurchaseBusinessGatherTableDTO.PagingViewDTO> viewDTOList = new ArrayList<>();
        for (Map.Entry<String, PurchaseBusinessGatherTableDTO.PagingViewDTO> stringPagingViewDTOEntry : map.entrySet()) {
            viewDTOList.add(stringPagingViewDTOEntry.getValue());
        }
        pageData.setRecords(viewDTOList);
        return new PagingVO(pageData);
    }*/
/*
    @Override
    public Boolean exportExcelPurchaseBusiness(PurchaseBusinessGatherTableDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<PurchaseBusinessGatherTableDTO.PagingViewDTO> viewDTOS = baseMapper.exportExcel(dto);
        List<String> ids = viewDTOS.stream().map(PurchaseBusinessGatherTableDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<String> podIdList = new ArrayList<>();
        ids.forEach(req -> {
            podIdList.addAll(Arrays.asList(req.split(",")));
        });
        //查询采购签收信息
        List<WarehouseReceiveDetailEntity> warehouseReceiveDetailEntities = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(podIdList);
        //查询采购入库信息
        List<PoInstockDetailEntity> poInstockDetailEntities = poInstockDetailService.listDetailByPodIds(podIdList);
        //查询退货信息
        List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(podIdList);
        //查询采购明细
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = purchaseOrderDetailService.listByIds(podIdList);
        Map<String, PurchaseBusinessGatherTableDTO.PagingViewDTO> map = new HashMap();
        for (PurchaseBusinessGatherTableDTO.PagingViewDTO record : viewDTOS) {
            String mapKey = record.getPurchaseOrgName() + record.getSupplierName() + record.getSpuNo() + record.getSkuNo();
            if (map.get(mapKey) != null) {
                PurchaseBusinessGatherTableDTO.PagingViewDTO mapEntity = map.get(mapKey);
                mapEntity.setOrderQty(mapEntity.getOrderQty() + 1);
                mapEntity.setOrderAmount(mapEntity.getOrderAmount().add(record.getOrderAmount()));
                mapEntity.setAvgPrice(mapEntity.getOrderAmount().divide(BigDecimal.valueOf(mapEntity.getOrderQty()), 4, BigDecimal.ROUND_DOWN));
                computeReceive(mapEntity, record, warehouseReceiveDetailEntities, dto, purchaseOrderDetailEntityList);
                computeInstock(mapEntity, record, poInstockDetailEntities, dto, purchaseOrderDetailEntityList);
                computeReturn(mapEntity, record, purchaseReturnOrderDetailEntities, dto);
            } else {
                map.put(mapKey, record);
                computeReceive(record, record, warehouseReceiveDetailEntities, dto, purchaseOrderDetailEntityList);
                computeInstock(record, record, poInstockDetailEntities, dto, purchaseOrderDetailEntityList);
                computeReturn(record, record, purchaseReturnOrderDetailEntities, dto);
            }
        }
        List<PurchaseBusinessGatherTableDTO.PagingViewDTO> viewDTOList = new ArrayList<>();
        for (Map.Entry<String, PurchaseBusinessGatherTableDTO.PagingViewDTO> stringPagingViewDTOEntry : map.entrySet()) {
            viewDTOList.add(stringPagingViewDTOEntry.getValue());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/purchaseBusinessGatherExport.xlsx";
        String name = "采购业务汇总表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(viewDTOList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }
    */
    @Override
    public Boolean exportExcelPurchaseBusiness(PurchaseBusinessGatherTableDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("采购业务汇总表", EXPORT_WMS_PURCHASE_BUSINESS.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<PurchaseBusinessGatherTableDTO.PagingViewDTO> exportPurchaseBusiness(PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> dto) {
        IPage<PurchaseBusinessGatherTableDTO.PagingViewDTO> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        for (PurchaseBusinessGatherTableDTO.PagingViewDTO viewDTO : page.getRecords()) {
            if (viewDTO.getAvgPrice() != null) {
                viewDTO.setReturnAmount(viewDTO.getAvgPrice().multiply(BigDecimal.valueOf(viewDTO.getRefundQty() + viewDTO.getReplenishQty())));
            }
        }
        return new PagingVO<>(page);
    }

    /**
     * 计算签收单统计数据
     * @Author Luo_WG
     * @Date 2023/6/15 18:19
     * @param mapEntity 第一段数据
     * @param record 查询的采购单汇总信息
     * @param warehouseReceiveDetailEntities 签收单详情信息
     * @param dto 查询参数
     * @param purchaseOrderDetailEntityList 采购单详情信息
     * @return void
     **/
    private void computeReceive(PurchaseBusinessGatherTableDTO.PagingViewDTO mapEntity,
                                PurchaseBusinessGatherTableDTO.PagingViewDTO record,
                                List<WarehouseReceiveDetailEntity> warehouseReceiveDetailEntities,
                                PurchaseBusinessGatherTableDTO.PagingParamDTO dto,
                                List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList) {
        //签收PurchaseOrderDetailEntity
        List<WarehouseReceiveDetailEntity> warehouseReceiveDetailEntityList = warehouseReceiveDetailEntities.stream().filter(req ->
                record.getId().contains(req.getPurchaseOrderDetailId())
                        && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())
                        && ((req.getBillDate().isAfter(dto.getBillDateList().get(0)) && dto.getBillDateList().get(1).isAfter(req.getBillDate()))
                        || (req.getBillDate().isEqual(dto.getBillDateList().get(0)) || dto.getBillDateList().get(1).isEqual(req.getBillDate()))
                        )
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(warehouseReceiveDetailEntityList)) {
            for (WarehouseReceiveDetailEntity warehouseReceiveDetailEntity : warehouseReceiveDetailEntityList) {
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntityList.stream().filter(req -> req.getId().equals(warehouseReceiveDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(purchaseOrderDetailEntity)) {
                    purchaseOrderDetailEntity.setTaxPrice(BigDecimal.ZERO);
                }
                if (record.getId().contains(warehouseReceiveDetailEntity.getPurchaseOrderDetailId())) {
                    mapEntity.setReceiveQty(mapEntity.getReceiveQty() + warehouseReceiveDetailEntity.getReceiveQty());
                    mapEntity.setReceiveGiftQty(mapEntity.getReceiveGiftQty() + warehouseReceiveDetailEntity.getExceedQty());
                    mapEntity.setReceiveAmount(mapEntity.getReceiveAmount().add(purchaseOrderDetailEntity.getTaxPrice().multiply(BigDecimal.valueOf(mapEntity.getReceiveQty()))));

                }
            }
        }
    }

    /**
     * 计算入库单统计数据
     * @Author Luo_WG
     * @Date 2023/6/15 18:19
     * @param mapEntity 第一段数据
     * @param record 查询的采购单汇总信息
     * @param poInstockDetailEntities 入库单详情信息
     * @param dto 查询参数
     * @param purchaseOrderDetailEntityList 采购单详情信息
     * @return void
     **/
    private void computeInstock(PurchaseBusinessGatherTableDTO.PagingViewDTO mapEntity,
                                PurchaseBusinessGatherTableDTO.PagingViewDTO record,
                                List<PoInstockDetailEntity> poInstockDetailEntities,
                                PurchaseBusinessGatherTableDTO.PagingParamDTO dto,
                                List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList) {
        //入库
        List<PoInstockDetailEntity> poInstockDetailEntityList = poInstockDetailEntities.stream().filter(req ->
                record.getId().contains(req.getPurchaseOrderDetailId())
                        && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())
                        && ((req.getBillDate().isAfter(dto.getBillDateList().get(0)) && dto.getBillDateList().get(1).isAfter(req.getBillDate()))
                        || (req.getBillDate().isEqual(dto.getBillDateList().get(0)) || dto.getBillDateList().get(1).isEqual(req.getBillDate()))
                )
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(poInstockDetailEntityList)) {
            for (PoInstockDetailEntity poInstockDetailEntity : poInstockDetailEntityList) {
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntityList.stream().filter(req -> req.getId().equals(poInstockDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(purchaseOrderDetailEntity)) {
                    purchaseOrderDetailEntity.setTaxPrice(BigDecimal.ZERO);
                }
                if (record.getId().contains(poInstockDetailEntity.getPurchaseOrderDetailId())) {
                    mapEntity.setStockInQty(mapEntity.getReceiveQty() + poInstockDetailEntity.getStockInQty());
                    mapEntity.setStockInGiftQty(mapEntity.getReceiveGiftQty() + poInstockDetailEntity.getExceedQty());
                    mapEntity.setStockInAmount(mapEntity.getStockInAmount().add(purchaseOrderDetailEntity.getTaxPrice().multiply(BigDecimal.valueOf(mapEntity.getStockInQty()))));

                }
            }
        }
    }

    /**
     * 计算退货单统计数据
     * @Author Luo_WG
     * @Date 2023/6/15 18:19
     * @param mapEntity 第一段数据
     * @param record 查询的采购单汇总信息
     * @param purchaseReturnOrderDetailEntities 退货单详情信息
     * @param dto 查询参数
     * @return void
     **/
    private void computeReturn(PurchaseBusinessGatherTableDTO.PagingViewDTO mapEntity,
                               PurchaseBusinessGatherTableDTO.PagingViewDTO record,
                               List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities,
                               PurchaseBusinessGatherTableDTO.PagingParamDTO dto) {
        //退货
        List<PoReturnDetailEntity> poReturnDetailEntityList = purchaseReturnOrderDetailEntities.stream().filter(req ->
                record.getId().contains(req.getPurchaseOrderDetailId())
                        && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())
                        && ((req.getBillDate().isAfter(dto.getBillDateList().get(0)) && dto.getBillDateList().get(1).isAfter(req.getBillDate()))
                        || (req.getBillDate().isEqual(dto.getBillDateList().get(0)) || dto.getBillDateList().get(1).isEqual(req.getBillDate()))
                )
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(poReturnDetailEntityList)) {
            Integer deductAmountQty = poReturnDetailEntityList.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).map(PoReturnDetailEntity::getDeductAmountQty).reduce(MathUtil.ZERO, Integer::sum);
            mapEntity.setRefundQty(mapEntity.getRefundQty() + deductAmountQty);
            Integer replenishQty = poReturnDetailEntityList.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);
            mapEntity.setReplenishQty(mapEntity.getReplenishQty() + replenishQty);
            mapEntity.setReturnAmount(mapEntity.getReturnAmount().add(record.getAvgPrice().multiply(BigDecimal.valueOf(deductAmountQty + replenishQty))));
        }
    }
}
