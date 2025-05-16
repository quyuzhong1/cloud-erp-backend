package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("头程物流单")
@RequestMapping("/feign/tmsFirstMileLogistic")
public class TmsFirstMileLogisticFeignController {
    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

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
    /**
     * 根据来源id和业务类型查询头程费用分摊记录
     * @param sourceId
     * @param businessCode
     * @param reportMonth
     * @return
     */
    @GetMapping("/getRecordBySourceIdAndCode")
    List<FirstMileCostAllocationDTO.DetailDTO> getRecordBySourceIdAndCode(@RequestParam("sourceId") String sourceId, @RequestParam("businessCode") String businessCode, @RequestParam("reportMonth") LocalDate reportMonth){
        return firstMileCostAllocationService.getRecordBySourceIdAndCode(sourceId,businessCode,reportMonth);
    }
    /**
     * 根据skuId和业务类型查询头程费用分摊记录
     * @param skuId
     * @param businessCode
     * @param reportMonth
     * @return
     */
    @GetMapping("/getRecordBySkuIdAndCode")
    List<FirstMileCostAllocationDTO.DetailDTO> getRecordBySkuIdAndCode(@RequestParam("skuId")String skuId, @RequestParam("businessCode")String businessCode, @RequestParam("reportMonth")LocalDate reportMonth){
        return firstMileCostAllocationService.getRecordBySkuIdAndCode(skuId,businessCode,reportMonth);
    }
}
