package com.erp.server.wms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.server.wms.query.WaveListPdaAdvanceQueryHandler;
import com.erp.server.wms.service.WaveListPdaService;
import com.erp.server.wms.service.WaveListService;
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
    @Resource
    private WaveListService waveListService;

    /**
     * 波次列表
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = WaveListPdaAdvanceQueryHandler.class)
    public ApiResult<PagingVO<WaveListPdaDTO.ViewDTO>> paging(@RequestBody PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO){
        PagingVO<WaveListPdaDTO.ViewDTO> pagingVO = waveListPdaService.paging(pagingDTO);
        return ApiResult.success(pagingVO);
    }

    /**
     * 单据详情
     * @param id 波次ID
     */
    @GetMapping("/detail")
    public ApiResult<WaveListPdaDTO.WaveBasicInfoDTO> detail(@RequestParam String id){
        WaveListPdaDTO.WaveBasicInfoDTO dto = waveListPdaService.waveInfo(id);
        return ApiResult.success(dto);
    }

    /**
     * 开始拣货（边拣边分）
     * @param waveId 波次ID
     * @return 波次列表
     */
    @PostMapping("/startPickingWithSideType")
    public ApiResult<WaveListDetailPdaDTO.ViewDTO> startPickingWithSideType(@RequestParam String waveId){
        WaveListDetailPdaDTO.ViewDTO dto = waveListPdaService.startPickingWithSideType(waveId);
        return ApiResult.success(dto);
    }

    /**
     * 开始拣货（先拣后分）
     * @param waveId 波次ID
     * @return 波次列表
     */
    @PostMapping("/startPickingWithSequenceType")
    public ApiResult<WaveListDetailPdaDTO.ViewDTO> startPickingWithSequenceType(@RequestParam String waveId){
        WaveListDetailPdaDTO.ViewDTO dto = waveListPdaService.startPickingWithSequenceType(waveId);
        return ApiResult.success(dto);
    }

    /**
     * 产品详情
     * @param id 波次ID
     */
    @GetMapping("/productDetail")
    public ApiResult<List<WaveListPdaDTO.ProductDetailDTO>> productDetail(@RequestParam String id){
        List<WaveListPdaDTO.ProductDetailDTO> list = waveListPdaService.productDetail(id);
        return ApiResult.success(list);
    }

    /**
     * 绑定拣货车
     */
    @PostMapping("/bindPickingCart")
    public ApiResult<?> bindPickingCart(@RequestBody WaveListPdaDTO.BindPickingCartDTO bindDTO){
        return waveListPdaService.bindPickingCart(bindDTO);
    }

}
