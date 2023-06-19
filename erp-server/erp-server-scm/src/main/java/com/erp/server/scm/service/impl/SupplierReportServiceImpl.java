package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierReportDTO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.SupplierReportMapper;
import com.erp.server.scm.service.SupplierReportService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 供应商相关报表服务实现类
 * @CreateTime: 2023-06-16  16:49
 * @Author: zhangchunlin
 */
@Service
public class SupplierReportServiceImpl implements SupplierReportService {

    @Resource
    private SupplierReportMapper supplierReportMapper;

    @Autowired
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public PagingVO<SupplierReportDTO.PagingViewDTO> supplierPaging(PagingDTO<SupplierReportDTO.PagingSearchParamDTO> paramDTO) {
        paramDTO.getParams().setPermissionSql(paramDTO.getPermissionSql());
        Page query = new Page(paramDTO.getCurrPage(), paramDTO.getPageSize());
        IPage<SupplierReportDTO.PagingViewDTO> pageData = supplierReportMapper.getPurchasePaging(query, paramDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }

        // 填充供应商报表其他字段值
        fillSupplierRptInfo(pageData.getRecords(), paramDTO.getParams());

        return new PagingVO(pageData);
    }

    /**
     * 填充供应商报表其他字段值
     * @param records
     */
    private void fillSupplierRptInfo(List<SupplierReportDTO.PagingViewDTO> records, SupplierReportDTO.PagingSearchParamDTO paramDTO) {
        List<String> supplierIds = records.stream().map(SupplierReportDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());

        // 收货批次、已收货量
        WarehouseReceiveDTO.SupplierReceiveParamDTO receiveParamDTO = new WarehouseReceiveDTO.SupplierReceiveParamDTO(supplierIds, paramDTO.getDateList());
        List<WarehouseReceiveDTO.SupplierReceiveInfoDTO> supplierReceiveInfos = wmsTaskFeign.getReceiveInfoBySupplierIds(receiveParamDTO);
        if(CollUtil.isNotEmpty(supplierReceiveInfos)) {
            Map<String,WarehouseReceiveDTO.SupplierReceiveInfoDTO> supplierRecMap = supplierReceiveInfos.stream().collect(Collectors.toMap(WarehouseReceiveDTO.SupplierReceiveInfoDTO::getSupplierId, Function.identity()));
            records.stream().forEach(record->{
                WarehouseReceiveDTO.SupplierReceiveInfoDTO supplierReceiveInfoDTO = supplierRecMap.getOrDefault(record.getId(), new WarehouseReceiveDTO.SupplierReceiveInfoDTO());
                record.setReceivedCount(Optional.ofNullable(supplierReceiveInfoDTO.getReceivedCount()).orElse(0));
                record.setReceivedQty(Optional.ofNullable(supplierReceiveInfoDTO.getReceivedQty()).orElse(0));
            });
        }

        // 入库批次、已入库量
        PoInstockDTO.SupplierInstockParamDTO instockParamDTO = new PoInstockDTO.SupplierInstockParamDTO(supplierIds, paramDTO.getDateList());
        List<PoInstockDTO.SupplierInstockInfoDTO> supplierInstockInfos =  wmsTaskFeign.getInstockInfoBySupplierIds(instockParamDTO);
        if(CollUtil.isNotEmpty(supplierInstockInfos)) {
            Map<String,PoInstockDTO.SupplierInstockInfoDTO> supplierPoMap = supplierInstockInfos.stream().collect(Collectors.toMap(PoInstockDTO.SupplierInstockInfoDTO::getSupplierId, Function.identity()));
            records.stream().forEach(record->{
                PoInstockDTO.SupplierInstockInfoDTO supplierPoInfoDTO = supplierPoMap.getOrDefault(record.getId(), new PoInstockDTO.SupplierInstockInfoDTO());
                record.setInstockCount(Optional.ofNullable(supplierPoInfoDTO.getInstockCount()).orElse(0));
                record.setInstockQty(Optional.ofNullable(supplierPoInfoDTO.getInstockQty()).orElse(0));
            });
        }

        // 质检退货批次、质检退货量
        List<String> sourceTypeList = Lists.newArrayList(SourceTypeEnum.QC_BILL.getCode());
        PurchaseReturnOrderDTO.SupplierReturnParamDTO returnParamDTO = new PurchaseReturnOrderDTO.SupplierReturnParamDTO(supplierIds, paramDTO.getDateList(), sourceTypeList);
        List<PurchaseReturnOrderDTO.SupplierReturnDTO> supplierReturnInfos = wmsTaskFeign.getReturnInfo(returnParamDTO);
        if(CollUtil.isNotEmpty(supplierReturnInfos)) {
            Map<String,PurchaseReturnOrderDTO.SupplierReturnDTO> supplierReturnMap = supplierReturnInfos.stream().collect(Collectors.toMap(PurchaseReturnOrderDTO.SupplierReturnDTO::getSupplierId, Function.identity()));
            records.stream().forEach(record->{
                PurchaseReturnOrderDTO.SupplierReturnDTO supplierReturnInfoDTO = supplierReturnMap.getOrDefault(record.getId(), new PurchaseReturnOrderDTO.SupplierReturnDTO());
                record.setQcReturnCount(Optional.ofNullable(supplierReturnInfoDTO.getQcReturnCount()).orElse(0));
                record.setQcReturnQty(Optional.ofNullable(supplierReturnInfoDTO.getQcReturnQty()).orElse(0));
            });
        }

        // 次品量
        records.stream().forEach(record->{
            if(StrUtil.isNotEmpty(record.getPurchaseOrderIds())) {
                List<String> purchaseOrderIds = Arrays.asList(record.getPurchaseOrderIds().split(","));
                QcInfoDTO.PurchaseQcParamDTO qcParamDTO = new QcInfoDTO.PurchaseQcParamDTO(paramDTO.getQcType(), purchaseOrderIds);
                QcInfoDTO.PurchaseQcInfoDTO purchaseQcInfoDTO = wmsTaskFeign.getQcInfoByPurchaseOrder(qcParamDTO);
                record.setDefectiveQty(0);
                if(Objects.nonNull(purchaseQcInfoDTO)) {
                  record.setDefectiveQty(Optional.ofNullable(purchaseQcInfoDTO.getDefectiveQty()).orElse(0));
                }
            }
        });

    }

}