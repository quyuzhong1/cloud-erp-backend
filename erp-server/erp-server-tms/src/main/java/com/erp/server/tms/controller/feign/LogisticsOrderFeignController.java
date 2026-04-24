package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsOrderEntity;
import com.erp.server.tms.service.LogisticsOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 物流下单
 */
@Slf4j
@RestController
@LogSystemModule("物流下单接口")
@RequestMapping("/feign/logisticsOrder")
public class LogisticsOrderFeignController {

    @Resource
    private LogisticsOrderService logisticsOrderService;

    /**
     * 批量添加物流单
     *
     * @param entityList List<LogisticsOrderEntity>
     * @return Boolean
     */
    @PostMapping("/addBatch")
    List<AfterSaleDTO.LogisticsOrderResultDTO> addBatch(@RequestBody List<LogisticsOrderEntity> entityList) {
        return logisticsOrderService.addBatch(entityList);
    }

    /**
     * 根据物流跟踪号批量查询物流单
     *
     * @param trackNoList List<String>
     * @return List<AfterSaleDTO.LogisticsOrderResultDTO>
     */
    @PostMapping("/getLogisticsOrderListByTrackNo")
    List<LogisticsOrderDTO.ListDTO> getLogisticsOrderListByTrackNo(@RequestBody List<String> trackNoList) {
        return logisticsOrderService.getLogisticsOrderListByTrackNo(trackNoList);
    }

    /**
     * 批量取消物流单
     *
     * @param codeList List<String>
     * @return List<AfterSaleDTO.LogisticsOrderResultDTO>
     */
    @PostMapping("/batchCancel")
    List<AfterSaleDTO.LogisticsOrderResultDTO> batchCancel(@RequestBody List<String> codeList) {
        return logisticsOrderService.batchCancel(codeList);
    }

    /**
     * 批量获取物流单标签
     *
     * @param logisticsLabelDTOS List<LogisticsOrderDTO.LogisticsLabelDTO>
     * @return List<AfterSaleDTO.LogisticsOrderResultDTO>
     */
    @PostMapping("/batchGetLabel")
    List<AfterSaleDTO.LogisticsOrderResultDTO> batchGetLabel(@RequestBody List<LogisticsOrderDTO.LogisticsLabelDTO> logisticsLabelDTOS) {
        return logisticsOrderService.batchGetLabel(logisticsLabelDTOS);
    }
}
