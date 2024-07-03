package com.erp.server.wms.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.server.wms.service.WaveListDetailService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 波次详情
 * @date 2024-06-28
 * @author tanmujin
 */
@RestController
@RequestMapping("/waveListDetail")
public class WaveListDetailController extends BaseController {

    @Resource
    private WaveListDetailService waveDetailService;

    /**
     * 根据波次ID查询详情
     * @param id 波次ID
     */
    @GetMapping("/view")
    public ApiResult<WaveListDetailDTO.ViewDTO> view(@RequestParam String id){
        WaveListDetailDTO.ViewDTO viewDTO = waveDetailService.view(id);
        return ApiResult.success(viewDTO);
    }

    /**
     * 移出波次
     */
    @PostMapping("/moveOut")
    public ApiResult<?> moveOut(@RequestBody WaveListDetailDTO.MoveOutDTO moveOutDTO){
        return waveDetailService.moveOut(moveOutDTO);
    }
}
