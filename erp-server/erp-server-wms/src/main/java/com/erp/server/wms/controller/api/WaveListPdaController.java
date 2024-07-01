package com.erp.server.wms.controller.api;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.server.wms.service.WaveListDetailPdaService;
import com.erp.server.wms.service.WaveListPdaService;
import org.springframework.web.bind.annotation.*;
import com.erp.model.wms.dto.WaveListPdaDTO;

import javax.annotation.Resource;
import java.util.List;

/**
 * 波次列表（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@RestController
@RequestMapping("/waveListPda")
public class WaveListPdaController {

    @Resource
    private WaveListPdaService waveListPdaService;

    /**
     * 单据详情
     */
    @GetMapping("/detail")
    public ApiResult<WaveListPdaDTO.DetailDTO> detail(@RequestParam String waveId){
        return null;
    }

    /**
     * 开始拣货
     * @param waveId 波次ID
     * @return 波次列表
     */
    @PostMapping("/startPicking")
    public ApiResult<List<WaveListDetailPdaDTO.ViewDTO>> startPicking(@RequestParam String waveId){
        //修改波次状态
        //返回波次详情
        List<WaveListDetailPdaDTO.ViewDTO> list = waveListPdaService.startPicking(waveId);
        return ApiResult.success(list);
    }

    /**
     * 产品详情
     * @param id 波次ID
     */
    @GetMapping("/productDetail")
    public ApiResult<List<WaveListPdaDTO.ProductDetailDTO>> productDetail(@RequestParam String id){
        return null;
    }

    /**
     * 绑定拣货车
     */
    @PostMapping("/bindPickingCart")
    public ApiResult<?> bindPickingCart(@RequestBody WaveListPdaDTO.BindPickingCartDTO bindDTO){
        //核对扫描的拣货车信息是否匹配
        //核对成功返回拣货车信息
        //核对失败返回错误
        return null;
    }

}
