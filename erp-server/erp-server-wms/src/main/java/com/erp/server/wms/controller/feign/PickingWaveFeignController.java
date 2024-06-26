package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.erp.model.wms.dto.PickingWaveDTO;

/**
 * 拣货波次Feign
 * @date 2024-06-26
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/pickingWave")
public class PickingWaveFeignController {

    @PostMapping("/add")
    public ApiResult<BatchResultDTO> add(@RequestBody PickingWaveDTO.AddDTO addDto){
        return null;
    }
}
