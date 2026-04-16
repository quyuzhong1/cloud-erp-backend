package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsBillEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "tmsFirstMileLogistic" ,configuration = {FeignErrorDecoder.class})
public interface TmsFirstMileLogisticFeign {

    /**
     * 根据来源id查询物流单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param outstockIds
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/feign/tmsFirstMileLogistic/listByOutstockIds")
    List<LogisticsBillEntity> listByOutstockIds(@RequestBody List<String> outstockIds);

    /**
     * 自动生成头程物流单
     **/
    @PostMapping("/feign/tmsFirstMileLogistic/autoGenerateFirstMileLogistic")
    BatchResultDTO autoGenerateFirstMileLogistic(@RequestBody AutoGenerateBillDTO autoGenerateBillDTO);


    /**
     * 获取有预警的物流单
     **/
    @PostMapping("/feign/tmsFirstMileLogistic/hasWarnPaging")
    List<TmsFirstMileLogisticDTO.PagingVO> hasWarnPaging(@RequestBody TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO);

    /**
     * 根据来源id和业务类型查询头程费用分摊记录
     * @param detailDTO
     * @return
     */
    @PostMapping("/feign/tmsFirstMileLogistic/getRecordBySourceIdAndCode")
    List<FirstMileCostAllocationDTO.DetailDTO> getRecordBySourceIdAndCode(@RequestBody FirstMileCostAllocationDTO.DetailDTO detailDTO);

    /**
     * 根据skuId和业务类型查询头程费用分摊记录
     * @param detailDTO
     * @return
     */
    @PostMapping("/feign/tmsFirstMileLogistic/getRecordBySkuIdAndCode")
    List<FirstMileCostAllocationDTO.DetailDTO> getRecordBySkuIdAndCode(@RequestBody FirstMileCostAllocationDTO.DetailDTO detailDTO);
    /**
     * 查询sku成本信息
     * @author will
     * @date 2025/12/10 14:07
     * @param paramDTO
     * @return void
     */
    @PostMapping("/feign/tmsFirstMileLogistic/listInventorySkuCost")
    List<InventorySkuCostDTO.InvSkuCostDTO> listInventorySkuCost(@RequestBody InventorySkuCostDTO.SkuCostParamDTO paramDTO);
    /**
     * 查询小包分摊费用
     * @author will
     * @date 2025/12/10 14:25
     * @param bagCostParamDTO
     * @return List<SmallBagCostDTO>
     */
    @PostMapping("/feign/tmsFirstMileLogistic/listSmallBagCost")
    List<SmallBagCostAllocationDTO.SmallBagCostDTO> listSmallBagCost(@RequestBody SmallBagCostAllocationDTO.SmallBagCostParamDTO bagCostParamDTO);

    /**
     * 添加日志
     * @author will
     * @date 2026/1/26 16:03
     * @param addLogDTO
     * @return void
     */
    @PostMapping("/feign/tmsFirstMileLogistic/addFirstMileLogisticLog")
    void addFirstMileLogisticLog(@RequestBody TmsFirstMileLogisticDTO.AddLogDTO addLogDTO);
}
