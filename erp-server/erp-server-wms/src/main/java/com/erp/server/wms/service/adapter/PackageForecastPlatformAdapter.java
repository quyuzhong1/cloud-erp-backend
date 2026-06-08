package com.erp.server.wms.service.adapter;

import cn.hutool.core.date.DateTime;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastEntity;

import java.util.Collections;
import java.util.List;

/**
 * 组包预报平台适配器。
 */
public interface PackageForecastPlatformAdapter {

    String platform();

    boolean isForecast(List<String> ids);

    List<BatchResultDTO> upload(PackageForecastDTO.UploadDTO dto);

    String print(String id);

    List<BatchResultDTO> cancel(List<String> ids);

    void syncTrackingStatus(PackageForecastEntity entity);

    default List<PackageForecastEntity> listSyncTrackingStatus(DateTime dateTime) {
        return Collections.emptyList();
    }
}
