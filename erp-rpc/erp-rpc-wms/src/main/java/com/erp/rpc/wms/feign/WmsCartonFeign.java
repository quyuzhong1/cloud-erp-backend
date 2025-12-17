package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.WmsCartonDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 装箱信息Feign
 * @date 2024-08-31
 * @author tanmujin
 */
@FeignClient(name = "erp-wms", path = "/feign/wmsCarton", contextId = "wmsCartonFeign",configuration = {FeignErrorDecoder.class})
public interface WmsCartonFeign {

    @GetMapping("/listByPackingTaskId")
    List<WmsCartonDTO.DetailDTO> listByPackingTaskId(@RequestParam String packingTaskId,@RequestParam("fbaShipmentCodes") List<String> fbaShipmentCodes);

    /**
     * 根据箱子查询装箱内容
     * @param boxIds
     * @return
     */
    @GetMapping("/listSkuByBoxIds")
    List<WmsCartonDTO.CartonSkuDTO> listSkuByBoxIds(@RequestParam("boxIds") List<String> boxIds);
}
