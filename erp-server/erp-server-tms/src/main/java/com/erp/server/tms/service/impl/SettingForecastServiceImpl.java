package com.erp.server.tms.service.impl;

import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.erp.server.tms.mapper.SettingForecastMapper;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.SettingForecastService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 预报设置 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-18
 */
@Service
public class SettingForecastServiceImpl extends SuperServiceImpl<SettingForecastMapper, SettingForecastEntity> implements SettingForecastService {

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Override
    public List<SettingForecastDTO.ListDTO> listAll() {
        List<SettingForecastEntity> list = this.list();
        List<SettingForecastDTO.ListDTO> resultList = BeanMapperUtils.copyList(SettingForecastDTO.ListDTO.class, list);
        return resultList;
    }

    @Override
    public Boolean addOrUpdate(List<SettingForecastDTO.SaveOrUpdateDTO> list) {
        List<SettingForecastEntity> saveOrUpdateList = BeanMapperUtils.copyList(SettingForecastEntity.class, list);
        // 数据处理
        handleData(saveOrUpdateList);
        return this.saveOrUpdateBatch(saveOrUpdateList);
    }

    @Override
    public Boolean delete(List<String> idList) {
        List<String> deleteIdList = idList.stream().filter(id -> !StringUtils.isNotBlank(id)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            return this.removeByIds(deleteIdList);
        }
        return Boolean.TRUE;
    }


    /**
     * 处理数据
     *
     * @param list
     */
    private void handleData(List<SettingForecastEntity> list) {
        //物流商ids
        List<String> logisticsSupplierIdList = list.stream().map(SettingForecastEntity::getLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierList = CollectionUtils.isNotEmpty(logisticsSupplierIdList) ? logisticsSupplierService.listByIds(logisticsSupplierIdList) : Collections.emptyList();
        for (SettingForecastEntity item : list) {
            String logisticsSupplierId = item.getLogisticsSupplierId();
            String logisticsSupplierName = logisticsSupplierList.stream().filter(l -> l.getId().equals(logisticsSupplierId)).
                    findFirst().map(LogisticsSupplierEntity::getSupplierName).orElse("");
            item.setLogisticsSupplierName(logisticsSupplierName);
        }

    }


}
