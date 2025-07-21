package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierReportDTO;
import com.erp.model.scm.enums.QcInsideTypeEnum;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.SupplierReportMapper;
import com.erp.server.scm.service.SupplierReportService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_REPORT;

/**
 * 供应商相关报表服务实现类
 * @CreateTime: 2023-06-16  16:49
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class SupplierReportServiceImpl implements SupplierReportService {

    @Resource
    private SupplierReportMapper supplierReportMapper;

    @Autowired
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public PagingVO<SupplierReportDTO.PagingViewDTO> supplierPaging(PagingDTO<SupplierReportDTO.PagingSearchParamDTO> paramDTO) {
        paramDTO.getParams().setPermissionSql(paramDTO.getPermissionSql());
        Page query = new Page(paramDTO.getCurrPage(), paramDTO.getPageSize());
        if (null != paramDTO.getParams() && QcInsideTypeEnum.INSIDE_QC.getCode().equalsIgnoreCase(paramDTO.getParams().getQcType())){
            paramDTO.getParams().setIsInside(true);
        }
        if (null != paramDTO.getParams() && QcInsideTypeEnum.OUTSIDE_QC.getCode().equalsIgnoreCase(paramDTO.getParams().getQcType())){
            paramDTO.getParams().setIsInside(false);
        }
        IPage<SupplierReportDTO.PagingViewDTO> pageData = supplierReportMapper.getPurchasePaging(query, paramDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }

        // 填充供应商报表其他字段值
        fillSupplierRptInfo(pageData.getRecords(), paramDTO.getParams());

        return new PagingVO(pageData);
    }

    @Override
    public void exportList(SupplierReportDTO.ExportSearchParamDTO paramDTO) {
        downloadTaskFeign.saveDownloadTask("供应商报表导出", EXPORT_SCM_SUPPLIER_REPORT.getCode(), paramDTO);
    }

    @Override
    public PagingVO<SupplierReportDTO.PagingViewDTO> exportSupplierReport(PagingDTO<SupplierReportDTO.ExportSearchParamDTO> dto) {

        dto.getParams().setPermissionSql(dto.getParams().getPermissionSql());
        Page<SupplierReportDTO.PagingViewDTO> page = supplierReportMapper.exportList(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        // 填充供应商报表其他字段值
        fillSupplierRptInfo(page.getRecords(), dto.getParams());
        return new PagingVO<>(page);
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
        List<String> sourceTypeList = Lists.newArrayList(SourceTypeEnum.QC_INFO.getCode());
        PurchaseReturnOrderDTO.SupplierReturnParamDTO returnParamDTO = new PurchaseReturnOrderDTO.SupplierReturnParamDTO(supplierIds, paramDTO.getDateList(), sourceTypeList);
        log.info("质检退货查询参数：{}", JSONObject.toJSONString(returnParamDTO));
        List<PurchaseReturnOrderDTO.SupplierReturnDTO> supplierReturnInfos = wmsTaskFeign.getReturnInfo(returnParamDTO);
        log.info("质检退货查询结果：{}", JSONObject.toJSONString(supplierReturnInfos));
        if(CollUtil.isNotEmpty(supplierReturnInfos)) {
            Map<String,PurchaseReturnOrderDTO.SupplierReturnDTO> supplierReturnMap = supplierReturnInfos.stream().collect(Collectors.toMap(PurchaseReturnOrderDTO.SupplierReturnDTO::getSupplierId, Function.identity()));
            records.stream().forEach(record->{
                PurchaseReturnOrderDTO.SupplierReturnDTO supplierReturnInfoDTO = supplierReturnMap.getOrDefault(record.getId(), new PurchaseReturnOrderDTO.SupplierReturnDTO());
                record.setQcReturnCount(Optional.ofNullable(supplierReturnInfoDTO.getQcReturnCount()).orElse(0));
                record.setQcReturnQty(Optional.ofNullable(supplierReturnInfoDTO.getQcReturnQty()).orElse(0));
            });
        }

        // 次品量
        Integer orderPartitionSize = 1000;
        records.stream().forEach(record->{
            if(StrUtil.isNotEmpty(record.getPurchaseOrderIds())) {
                List<String> purchaseOrderIds = Arrays.asList(record.getPurchaseOrderIds().split(","));
                // 此处优化，防止数据量过大，拆分成多次查询（1000个一组），如果还是很慢，则优化成前端异步加载
                List<List<String>> partitionList = ListUtil.partition(purchaseOrderIds, orderPartitionSize);
                Integer sumDefectiveQty = 0;
                for(List<String> purchaseOrderIdList : partitionList) {
                    QcInfoDTO.PurchaseQcParamDTO qcParamDTO = new QcInfoDTO.PurchaseQcParamDTO(paramDTO.getQcType(), purchaseOrderIdList);
                    QcInfoDTO.PurchaseQcInfoDTO purchaseQcInfoDTO = wmsTaskFeign.getQcInfoByPurchaseOrder(qcParamDTO);

                    if(Objects.nonNull(purchaseQcInfoDTO)) {
                        sumDefectiveQty = sumDefectiveQty + Optional.ofNullable(purchaseQcInfoDTO.getDefectiveQty()).orElse(0);
                    }
                }
                record.setDefectiveQty(sumDefectiveQty);
            }
        });

        // 合格率(批次)
        records.stream().forEach(record->{
            if(Objects.isNull(record.getQcReturnCount())) {
                record.setQcReturnCount(0);
            }
            if(Objects.isNull(record.getQcReturnQty())) {
                record.setQcReturnQty(0);
            }
            // 合格率（批次）
            // 1-（退货批次/收货批次）*100%
            // 如果收货批次为0，则为
            if(Objects.isNull(record.getReceivedCount()) || (Objects.nonNull(record.getReceivedCount()) && record.getReceivedCount().intValue() == 0 ) ) {
                record.setPassRateCount(BigDecimal.ZERO);
            } else {
                BigDecimal passRateCount = BigDecimal.ONE.subtract(new BigDecimal(record.getQcReturnCount().intValue()).divide(new BigDecimal(record.getReceivedCount().intValue()),4, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100"));
                record.setPassRateCount(passRateCount);
            }
            // 合格率（量）
            // （1-退货量/收货量）*100%
            if(Objects.isNull(record.getReceivedQty())  ||  (Objects.nonNull(record.getReceivedQty()) && record.getReceivedQty().intValue() == 0 ) ) {
                record.setPassRateQty(BigDecimal.ZERO);
            } else {
                BigDecimal passRateQty = BigDecimal.ONE.subtract(new BigDecimal(record.getQcReturnQty()).divide(new BigDecimal(record.getReceivedQty().intValue()),4, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100"));
                record.setPassRateQty(passRateQty);
            }
        });

    }

}