package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.server.wms.service.WaveListFeignService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private WaveListFeignService waveListFeignService;

    /**
     * 新增波次
     */
    @PostMapping("/add")
    public ApiResult<BatchResultDTO> add(@RequestBody WaveListDTO.AddDTO addDto){
        BatchResultDTO resultDTO = waveListFeignService.add(addDto);
        return resultDTO.getSuccess() ? success() : failure();
    }
}
