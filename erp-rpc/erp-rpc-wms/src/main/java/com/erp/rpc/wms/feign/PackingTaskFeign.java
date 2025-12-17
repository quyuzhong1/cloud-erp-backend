package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.PackingTaskEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 装箱任务相关fegin
 */
@FeignClient(name = "erp-wms", contextId = "packingTaskFeign" ,configuration = {FeignErrorDecoder.class})
public interface PackingTaskFeign {

    /**
     * B2B称重
     * @param dto
     * @return
     * **/
    @PostMapping("/feign/packingTask/dimensionalWeight")
    ApiResult<String> dimensionalWeight(@RequestBody DimensionalWeightDTO dto);

    /**
     * 根据任务ID批量查询装箱信息
     * @param taskIds 任务ID集合
     */
    @PostMapping("/feign/packingTask/listCartonSpecByTaskIds")
    List<WmsCartonSpecDTO.WmsCartonSpecView> listCartonSpecByTaskIds(@RequestBody List<String> taskIds);

    /**
     * 根据来源ID查询
     */
    @GetMapping("/feign/packingTask/getBySourceId")
    PackingTaskEntity getBySourceId(@RequestParam String sourceId);

    /**
     * 根据发货单单号查询是否有装箱重量
     */
    @PostMapping("/feign/packingTask/checkCartonWeightBySourceCodes")
    List<WmsCartonSpecDTO.NoPackingView> checkCartonWeightBySourceCodes(@RequestBody List<String> sourceCodes);
    /**
     * 根据发货单单号查询是否有装箱信息
     */
    @PostMapping("/feign/packingTask/listCartonBySourceCodes")
    List<WmsCartonDTO.ListPackingCartonDTO> listCartonBySourceCodes(@RequestBody List<String> sourceCodes);

    /**
     * 根据来源ID查询装箱状态
     */
    @PostMapping("/feign/packingTask/getPackingStatusByFirstMileDelivery")
    List<PackingTaskEntity> getPackingStatusByFirstMileDelivery(@RequestBody FirstMileDeliveryEntity firstMileDeliveryEntity);
}
