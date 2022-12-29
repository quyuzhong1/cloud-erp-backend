package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.dto.BiSalesMonitoringDTO;
import com.erp.model.bi.entity.BiSalesMonitoringEntity;
import com.erp.server.bi.mapper.BiSalesMonitoringMapper;
import com.erp.server.bi.service.BiSalesMonitoringService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 16:24
 */
@Service
public class BiSalesMonitoringServiceImpl extends ServiceImpl<BiSalesMonitoringMapper, BiSalesMonitoringEntity>
        implements BiSalesMonitoringService {

    @Override
    public void batchAdd(List<BiSalesMonitoringDTO> list) {
        if (CollectionUtils.isNotEmpty(list) || list.size() == 0) {
            throw new ServiceException(ApiError.ERROR_97016);
        }
    }

    @Override
    public void batchUpdate(List<BiSalesMonitoringDTO> list) {
        if (CollectionUtils.isNotEmpty(list) || list.size() == 0) {

        }
    }

    @Override
    public List<BiSalesMonitoringDTO> listBiSalesMonitoring() {
        return null;
    }
}
