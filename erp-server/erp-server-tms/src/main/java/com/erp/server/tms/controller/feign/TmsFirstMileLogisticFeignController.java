package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.server.tms.service.TmsDeclareBillService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("头程物流单")
@RequestMapping("/feign/tmsFirstMileLogistic")
public class TmsFirstMileLogisticFeignController {
    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;

    @Resource
    private TmsDeclareBillService tmsDeclareBillService;

    /**
     * 根据来源id查询物流单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param outstockIds
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/listByOutstockIds")
    public List<LogisticsBillEntity> listByOutstockIds(@RequestBody List<String> outstockIds) {
        return tmsFirstMileLogisticService.listByOutstockIds(outstockIds);
    }

    /**
     * 自动生成头程物流单
     **/
    @PostMapping("/autoGenerateFirstMileLogistic")
    BatchResultDTO autoGenerateFirstMileLogistic(@RequestBody AutoGenerateBillDTO autoGenerateBillDTO){
        return tmsFirstMileLogisticService.autoGenerateFirstMileLogistic(autoGenerateBillDTO);
    }

}
