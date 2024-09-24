package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.server.wms.service.WmsCartonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 装箱内容Feign
 * @date 2024-08-31
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/wmsCarton")
public class WmsCartonFeignController {

    @Resource
    private WmsCartonService wmsCartonService;

    @GetMapping("/listByPackingTaskId")
    List<WmsCartonDTO.DetailDTO> listByPackingTaskId(@RequestParam String packingTaskId){
        return wmsCartonService.listByPackingTaskId(packingTaskId);
    }
}
