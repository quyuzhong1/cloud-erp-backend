package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.handler.PlatformSaveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.entity.SubcontractChangeDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.PackageForecastFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.PackageService;
import com.erp.server.oms.service.SoB2cRefService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname PackageServiceImpl
 * @Description
 * @Date 2024-01-30 11:13
 * @Created by yl
 */
@Service
@Slf4j
public class PackageServiceImpl implements PackageService {

    @Resource
    private PackageForecastFeign packageForecastFeign;

    @Resource
    private SoB2cMapper soB2cMapper;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private SoB2cRefService soB2cRefService;

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;


    /**
     * 组包合并
     *
     * @param dto
     * @return
     */
    @Override
    public List<BatchResultDTO> mergePackage(PackageDTO.MergePackageDTO dto) {
        List<PackageForecastDTO.AddDTO> addList = assembleDbBySoIds(dto);
        return packageForecastFeign.add(addList);
    }

    /**
     * 拼装数据
     *
     * @param dto
     * @return
     */
    private List<PackageForecastDTO.AddDTO> assembleDbBySoIds(PackageDTO.MergePackageDTO dto) {
        List<String> ids = dto.getIds();
        Boolean isAutoOut = dto.getIsAutoOut();

        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<PackageDTO.ScanResultDTO> list = soB2cMapper.listMergePackageBySoIds(ids);
        List<String> logisticsChannelIdList = list.stream().filter(a -> StringUtils.isNotBlank(a.getLogisticsChannelId())).
                map(PackageDTO.ScanResultDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());

        List<LogisticsChannelDTO.BaseDTO> channelList = CollectionUtils.isNotEmpty(logisticsChannelIdList) ? logisticsFeign.listChannelInfoById(logisticsChannelIdList) : Collections.emptyList();
        for (PackageDTO.ScanResultDTO item : list) {
            String logisticsChannelId = item.getLogisticsChannelId();
            LogisticsChannelDTO.BaseDTO logisticsChannel = channelList.stream().
                    filter(l -> l.getId().equals(logisticsChannelId)).findFirst().orElse(null);
            if(logisticsChannel!=null){
                item.setLogisticsChannelName(logisticsChannel.getName());
                item.setLogisticsSupplierId(logisticsChannel.getLogisticsSupplierId());
                item.setLogisticsSupplierName(logisticsChannel.getLogisticsSupplierName());
            }
        }

        Map<String, List<PackageDTO.ScanResultDTO>> map = list.stream().filter(s -> StringUtils.isNotBlank(s.getLogisticsSupplierId())).
                collect(Collectors.groupingBy(req -> req.getLogisticsSupplierId()+"-"+req.getLogisticsChannelId()+"-"+req.getTransferLogisticsSupplierId()));

        LocalDate nowDate = LocalDate.now();
        String weightUnit= UnitEnum.WeightUnitEnum.G.getCode();
        List<PackageForecastDTO.AddDTO> result = new ArrayList<>(map.size());
        for (Map.Entry<String, List<PackageDTO.ScanResultDTO>> entry : map.entrySet()) {
            List<PackageDTO.ScanResultDTO> detailList = entry.getValue();
            BigDecimal totalPackageWeight=detailList.stream().
                    map(PackageDTO.ScanResultDTO::getWeight).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            PackageForecastDTO.AddDTO addDTO = new PackageForecastDTO.AddDTO();
            addDTO.setLogisticsSupplierId(detailList.get(0).getLogisticsSupplierId());
            String logisticsSupplierName = detailList.get(0).getLogisticsSupplierName();
            addDTO.setLogisticsSupplierName(logisticsSupplierName);
            addDTO.setTotalPackageQty(detailList.size());
            addDTO.setBillDate(nowDate);
            addDTO.setWeightUnit(weightUnit);
            addDTO.setTotalPackageWeight(totalPackageWeight);
            List<PackageForecastDetailDTO.AddDTO> addDetailList= BeanMapperUtils.copyList(PackageForecastDetailDTO.AddDTO.class,detailList);
            addDetailList.forEach(addDetail->addDetail.setWeightUnit(weightUnit));
            addDTO.setDetailList(addDetailList);
            result.add(addDTO);

            //自动发货
            if (isAutoOut) {
                List<String> soIdList = list.stream().map(req -> req.getSoId()).collect(Collectors.toList());
                soB2cDeliveryFeign.mergePackageDelivery(soIdList);
            }
        }

        return result;
    }

}
