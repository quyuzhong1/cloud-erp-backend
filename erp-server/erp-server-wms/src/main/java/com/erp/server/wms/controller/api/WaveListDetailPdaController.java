package com.erp.server.wms.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.server.wms.service.WaveListDetailPdaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 波次详情（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@RestController
@RequestMapping("/waveListDetailPda")
public class WaveListDetailPdaController extends BaseController {

    @Resource
    private WaveListDetailPdaService waveListDetailPdaService;

    /**
     * 挂起波次
     */
    @PostMapping("/hangUp")
    public ApiResult<?> hangUp(@RequestBody WaveListDetailPdaDTO.HangUpParamDTO hangUpDTO){
        Boolean flag = waveListDetailPdaService.hangUp(hangUpDTO);
        return flag ? success() : failure();
    }

    /**
     * 完成拣货
     */
    @PostMapping
    public ApiResult<WaveListDetailPdaDTO.FinishResultDTO> finish(@RequestBody WaveListDetailPdaDTO.FinishParamDTO finishParamDTO){
        return null;
    }
}
