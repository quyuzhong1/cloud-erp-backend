package com.erp.server.scm.service.impl;

import com.alibaba.nacos.common.utils.MapUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.utils.MapUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.ReportFormsManageMapper;
import com.erp.server.scm.mapper.SubcontractOrderMapper;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.ReportFormsManageService;
import org.apache.xmlbeans.impl.regex.Match;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表管理
 * @Author Luo_WG
 * @Date 2023/6/12 17:34
 **/
@Service
public class ReportFormsManageServiceImpl extends SuperServiceImpl<ReportFormsManageMapper, PurchaseOrderDetailEntity> implements ReportFormsManageService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public PagingVO<List<PurchaseBusinessGatherTableDTO.PagingViewDTO>> purchaseBusinessGatherTablPaginge(PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseBusinessGatherTableDTO.PagingViewDTO> pageData = baseMapper.paging(query, pagingDTO.getParams());
        List<PurchaseBusinessGatherTableDTO.PagingViewDTO> records = pageData.getRecords();
        List<String> ids = records.stream().map(PurchaseBusinessGatherTableDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<String> podIdList = new ArrayList<>();
        ids.forEach(req -> {
            podIdList.addAll(Arrays.asList(req.split(",")));
        });
        Map<String,PurchaseBusinessGatherTableDTO.PagingViewDTO> map = new HashMap();

        for (PurchaseBusinessGatherTableDTO.PagingViewDTO record : records) {
            String mapKey = record.getPurchaseOrgName() + record.getSupplierName() + record.getSpuNo() + record.getSkuNo();
            if (map.get(mapKey) != null) {
                PurchaseBusinessGatherTableDTO.PagingViewDTO mapEntity = map.get(mapKey);
                mapEntity.setOrderQty(mapEntity.getOrderQty() + 1);
                mapEntity.setOrderAmount(mapEntity.getOrderAmount().add(record.getOrderAmount()));
                mapEntity.setAvgPrice(mapEntity.getOrderAmount().divide(BigDecimal.valueOf(mapEntity.getOrderQty()), 4, BigDecimal.ROUND_DOWN));
                computeNumber(mapEntity, record, podIdList);
            } else {
                map.put(mapKey, record);
                computeNumber(record, record, podIdList);
            }
        }
        List<PurchaseBusinessGatherTableDTO.PagingViewDTO> viewDTOList = new ArrayList<>();
        for (Map.Entry<String, PurchaseBusinessGatherTableDTO.PagingViewDTO> stringPagingViewDTOEntry : map.entrySet()) {
            viewDTOList.add(stringPagingViewDTOEntry.getValue());
        }
        pageData.setRecords(viewDTOList);
        return new PagingVO(pageData);
    }

    private void  computeNumber(PurchaseBusinessGatherTableDTO.PagingViewDTO mapEntity, PurchaseBusinessGatherTableDTO.PagingViewDTO record, List<String> podIdList) {
        //查询采购签收信息
        List<WarehouseReceiveDetailEntity> warehouseReceiveDetailEntities = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIdList);
        //查询采购入库信息
        List<PoInstockDetailEntity> poInstockDetailEntities = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIdList);
        //查询退货信息
        List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = wmsTaskFeign.listReturnOrderDetailByPodIds(podIdList);
        //签收
        List<WarehouseReceiveDetailEntity> warehouseReceiveDetailEntityList = warehouseReceiveDetailEntities.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(warehouseReceiveDetailEntityList)) {
            Integer receiveQty = warehouseReceiveDetailEntityList.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            mapEntity.setReceiveQty(mapEntity.getReceiveQty() + receiveQty);
            Integer exceedQty = warehouseReceiveDetailEntityList.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getExceedQty).reduce(MathUtil.ZERO, Integer::sum);
            mapEntity.setReceiveGiftQty(mapEntity.getReceiveGiftQty() + exceedQty);
            mapEntity.setReceiveAmount(mapEntity.getReceiveAmount().add(mapEntity.getTaxPrice().multiply(BigDecimal.valueOf(receiveQty))));
        }
        //入库
        List<PoInstockDetailEntity> poInstockDetailEntityList = poInstockDetailEntities.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(poInstockDetailEntityList)) {
            Integer stockInQty = poInstockDetailEntityList.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            mapEntity.setStockInQty(mapEntity.getStockInQty() + stockInQty);
            Integer exceedQty = poInstockDetailEntityList.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).map(PoInstockDetailEntity::getExceedQty).reduce(MathUtil.ZERO, Integer::sum);
            mapEntity.setStockInGiftQty(mapEntity.getStockInGiftQty() + exceedQty);
            mapEntity.setStockInAmount(mapEntity.getStockInAmount().add(mapEntity.getTaxPrice().multiply(BigDecimal.valueOf(stockInQty))));
        }
        //退货
        List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntityList = purchaseReturnOrderDetailEntities.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(purchaseReturnOrderDetailEntityList)) {
            Integer deductAmountQty = purchaseReturnOrderDetailEntityList.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getDeductAmountQty).reduce(MathUtil.ZERO, Integer::sum);
            mapEntity.setRefundQty(mapEntity.getRefundQty() + deductAmountQty);
            Integer replenishQty = purchaseReturnOrderDetailEntityList.stream().filter(req -> record.getId().contains(req.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);
            mapEntity.setReplenishQty(mapEntity.getReplenishQty() + replenishQty);
            mapEntity.setReturnAmount(mapEntity.getReturnAmount().add(purchaseReturnOrderDetailEntityList.get(0).getReturnPrice().multiply(BigDecimal.valueOf(deductAmountQty + replenishQty))));
        }
    }
}
