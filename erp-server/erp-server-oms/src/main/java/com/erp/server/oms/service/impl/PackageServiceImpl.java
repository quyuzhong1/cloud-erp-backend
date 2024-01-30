package com.erp.server.oms.service.impl;

import com.common.business.enums.UnitEnum;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.scm.entity.SubcontractChangeDetailEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.PackageForecastFeign;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.PackageService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname PackageServiceImpl
 * @Description
 * @Date 2024-01-30 11:13
 * @Created by yl
 */
@Service
public class PackageServiceImpl implements PackageService {

    @Resource
    private PackageForecastFeign packageForecastFeign;

    @Resource
    private SoB2cMapper soB2cMapper;

    @Resource
    private LogisticsFeign logisticsFeign;


    /**
     * 组包合并
     *
     * @param ids 为销售订单id
     * @return
     */
    @Override
    public Boolean mergePackage(List<String> ids) {
        List<PackageForecastDTO.AddDTO> addList = assembleDbBySoIds(ids);
        packageForecastFeign.add(addList);
        return null;
    }

    /**
     * 拼装数据
     *
     * @param ids
     * @return
     */
    private List<PackageForecastDTO.AddDTO> assembleDbBySoIds(List<String> ids) {
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
            item.setLogisticsChannelName(logisticsChannel.getName());
            item.setLogisticsSupplierId(logisticsChannel.getLogisticsSupplierId());
            item.setLogisticsSupplierName(logisticsChannel.getLogisticsSupplierName());
        }

        Map<String, List<PackageDTO.ScanResultDTO>> map = list.stream().filter(s -> StringUtils.isNotBlank(s.getLogisticsSupplierId())).
                collect(Collectors.groupingBy(PackageDTO.ScanResultDTO::getLogisticsSupplierId));
        LocalDate nowDate = LocalDate.now();
        String weightUnit= UnitEnum.WeightUnitEnum.G.getCode();
        List<PackageForecastDTO.AddDTO> result = new ArrayList<>(map.size());
        for (Map.Entry<String, List<PackageDTO.ScanResultDTO>> entry : map.entrySet()) {
            String logisticsSupplierId = entry.getKey();
            List<PackageDTO.ScanResultDTO> detailList = entry.getValue();
            BigDecimal totalPackageWeight=detailList.stream().
                    map(PackageDTO.ScanResultDTO::getWeight).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            PackageForecastDTO.AddDTO addDTO = new PackageForecastDTO.AddDTO();
            addDTO.setLogisticsSupplierId(logisticsSupplierId);
            String  logisticsSupplierName = detailList.get(0).getLogisticsSupplierName();
            addDTO.setLogisticsSupplierName(logisticsSupplierName);
            addDTO.setTotalPackageQty(detailList.size());
            addDTO.setBillDate(nowDate);
            addDTO.setWeightUnit(weightUnit);
            addDTO.setTotalPackageWeight(totalPackageWeight);
            List<PackageForecastDetailDTO.AddDTO> addDetailList= BeanMapperUtils.copyList(PackageForecastDetailDTO.AddDTO.class,detailList);
            addDetailList.forEach(addDetail->addDetail.setWeightUnit(weightUnit));
            addDTO.setDetailList(addDetailList);
            result.add(addDTO);
        }


        return result;
    }


}
