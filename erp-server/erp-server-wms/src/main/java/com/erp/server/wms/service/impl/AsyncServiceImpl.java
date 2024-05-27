package com.erp.server.wms.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.handler.PlatformSaveHandler;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异步服务类
 *
 * @author Jim
 * @date 2024/5/9 17:23
 */
@Slf4j
@Service
public class AsyncServiceImpl implements AsyncService {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Async("wmsErpExecutor")
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


    @Async
    @Override
    @DataIdempotent(keyIdName = "soId")
    public void asyncShipOrder(String soId, String soCode, String dictPlatform, String sourceDTOJson, String businessDesc) {
        PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
        platformShipOrderDTO.setSoB2cId(soId);
        platformShipOrderDTO.setDictPlatform(dictPlatform);
        try {
            PlatformSaveHandler.shipOrder(platformShipOrderDTO);
        } catch (Exception e) {
            log.error("【{}】销售单【{}】 标记发货失败 >>>错误信息{}", businessDesc, soCode, ExceptionUtil.stacktraceToString(e));
            // 独立异常
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                    soId,
                    SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode(),
                    sourceDTOJson,
                    e.getMessage(),
                    ExceptionUtil.stacktraceToString(e),
                    ""
            );
            soB2cFeign.addSoB2cError(addError);
            log.warn("【{}】销售单【{}】标记发货失败记录结束", businessDesc, soCode);
        }
    }
}
