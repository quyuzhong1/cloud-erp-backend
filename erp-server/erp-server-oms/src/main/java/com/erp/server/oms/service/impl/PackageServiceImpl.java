package com.erp.server.oms.service.impl;

import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.PackageForecastFeign;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.PackageService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
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
     * @param ids
     * @return
     */
    private List<PackageForecastDTO.AddDTO> assembleDbBySoIds(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        List<PackageDTO.ScanResultDTO> list=soB2cMapper.listMergePackageBySoIds(ids);
        List<String> logisticsChannelIdList = list.stream().filter(a -> StringUtils.isNotBlank(a.getLogisticsChannelId())).
                map(PackageDTO.ScanResultDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());

        List<LogisticsChannelDTO.BaseDTO> channelList =CollectionUtils.isNotEmpty(logisticsChannelIdList)? logisticsFeign.listChannelInfoById(logisticsChannelIdList):Collections.emptyList();


        return null;
    }
}
