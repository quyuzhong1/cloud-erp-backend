package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.server.wms.service.WaveListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 波次列表Feign控制器
 * @date 2024-06-26
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/waveList")
public class WaveListFeignController extends BaseController {

    @Resource
    private WaveListService waveListService;

    /**
     * 新增波次
     */
    @PostMapping("/add")
    public BaseResultDTO.AddDTO add(@RequestBody WaveListDTO.AddDTO addDto){
        return waveListService.add(addDto);
    }



    /**
     * 波次状态自动更新
     * @author jack
     * @date 2024/11/28
     */
    @GetMapping("/waveListStatusAutoChange")
    public void waveListStatusAutoChange(@RequestParam("deliveryId") String deliveryId){
        if(StringUtils.isNotBlank(deliveryId)){
            waveListService.waveListStatusAutoChange(deliveryId);
        }
    }
}
