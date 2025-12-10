package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.erp.server.tms.service.InventorySkuCostService;
import com.erp.server.tms.service.SmallBagCostAllocationService;
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
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @Resource
    private InventorySkuCostService inventorySkuCostService;

    @Resource
    private SmallBagCostAllocationService smallBagCostAllocationService;

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
     *获取有预警的物流单
     **/
    @PostMapping("/hasWarnPaging")
    List<TmsFirstMileLogisticDTO.PagingVO> hasWarnPaging(@RequestBody TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO){
        return tmsFirstMileLogisticService.hasWarnPaging(pagingParamDTO);
    }

    /**
     * 根据来源id和业务类型查询头程费用分摊记录
     * @param detailDTO
     * @return
     */
    @PostMapping("/getRecordBySourceIdAndCode")
    List<FirstMileCostAllocationDTO.DetailDTO> getRecordBySourceIdAndCode(@RequestBody FirstMileCostAllocationDTO.DetailDTO detailDTO){
        return firstMileCostAllocationService.getRecordBySourceIdAndCode(detailDTO.getSourceId(),detailDTO.getBusinessCode(),detailDTO.getReportMonth());
    }
    /**
     * 根据skuId和业务类型查询头程费用分摊记录
     * @param detailDTO
     * @return
     */
    @PostMapping("/getRecordBySkuIdAndCode")
    List<FirstMileCostAllocationDTO.DetailDTO> getRecordBySkuIdAndCode(@RequestBody FirstMileCostAllocationDTO.DetailDTO detailDTO){
        return firstMileCostAllocationService.getRecordBySkuIdAndCode(detailDTO.getSkuId(),detailDTO.getBusinessCode(),detailDTO.getReportMonth());
    }


    /**
     * 查询sku成本
     * @author will
     * @date 2025/12/10 14:40
     * @param paramDTO
     * @return List<InvSkuCostDTO>
     */
    @PostMapping("/listInventorySkuCost")
    List<InventorySkuCostDTO.InvSkuCostDTO> listInventorySkuCost(@RequestBody InventorySkuCostDTO.SkuCostParamDTO paramDTO){
        return inventorySkuCostService.listInventorySkuCost(paramDTO);
    }
    /**
     * 查询小包费用分摊
     * @author will
     * @date 2025/12/10 14:40
     * @param paramDTO
     * @return List<SmallBagCostDTO>
     */
    @PostMapping("/listSmallBagCost")
    List<SmallBagCostAllocationDTO.SmallBagCostDTO> listSmallBagCost(@RequestBody SmallBagCostAllocationDTO.SmallBagCostParamDTO paramDTO){
        return smallBagCostAllocationService.listSmallBagCost(paramDTO);
    }
}
