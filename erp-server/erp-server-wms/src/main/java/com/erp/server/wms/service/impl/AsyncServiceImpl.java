package com.erp.server.wms.service.impl;

import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.handler.PlatformSaveHandler;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.wms.service.AsyncService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异步服务类
 *
 * @author Jim
 * @date 2024/5/9 17:23
 */
@Service
public class AsyncServiceImpl implements AsyncService {

    @Async
    @Override
    public void asyncBatchQueryAndUpdateOrderStatus(List<SoB2cEntity> soB2cEntityList) {
        Map<String, List<PlatformOrderQueryDTO>> orderGroupMap = soB2cEntityList.stream()
                .filter(e -> !e.getIsCancel())
                .map(e-> new PlatformOrderQueryDTO(e.getId(), e.getPlatformCode(), e.getDictPlatform(), e.getShopId()))
                .collect(Collectors.groupingBy(PlatformOrderQueryDTO::getDictPlatform));
        if (orderGroupMap.isEmpty()){
            return;
        }
        orderGroupMap.entrySet().parallelStream().peek(e->{
            String dictPlatform = e.getKey();
            List<PlatformOrderQueryDTO> curOrderList = e.getValue();
            PlatformSaveHandler.batchQueryAndUpdateOrderStatus(dictPlatform, curOrderList);
        }).collect(Collectors.toList());
    }
}
