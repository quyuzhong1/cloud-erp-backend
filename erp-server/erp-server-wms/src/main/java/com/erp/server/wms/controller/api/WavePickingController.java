package com.erp.server.wms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WavePickingDTO;
import com.erp.server.wms.query.WavePickingAdvanceQueryHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 波次拣货（波次列表）
 * @date 2024-06-20
 * @author tanmujin
 */
@RestController
@RequestMapping("/wavePicking")
public class WavePickingController extends BaseController {

    /**
     * 波次拣货分页查询
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = WavePickingAdvanceQueryHandler.class)
    public ApiResult<PagingVO<WavePickingDTO.ViewDTO>> paging(@RequestBody PagingDTO<WavePickingDTO.searchParamDTO> dto){
        return null;
    }

    /**
     * 批量取消波次
     */
    @PostMapping("/cancelWaveBatch")
    public ApiResult<?> cancelWaveBatch(@RequestBody List<String> waveCodeList){
        return null;
    }

    /**
     * 批量取消已打印
     */
    @PostMapping("/cancelPrintedBatch")
    public ApiResult<?> cancelPrintedBatch(@RequestBody List<String> waveCodeList){
        return null;
    }

    /**
     * 波次拣货详情
     */
    @GetMapping("/viewDetail")
    public ApiResult<List<WavePickingDTO.DetailViewDTO>> viewDetail(@RequestParam String waveCode){
        return null;
    }

    /**
     * 移出波次
     */
    @PostMapping("/moveOut")
    public ApiResult<Boolean> moveOut(@RequestBody WavePickingDTO.moveOutDTO moveOutDTO){
        return null;
    }

    /**
     * 新增波次
     */
    @PostMapping("/add")
    public ApiResult<Boolean> add(@RequestBody WavePickingDTO.addDTO addDTO){
        return null;
    }

    /**
     * 打印
     */
    @PostMapping("/print")
    public ApiResult<?> print(){
        return null;
    }

    /**
     * 打印拣货单
     */
    @PostMapping("/printPickingSheet")
    public ApiResult<?> printPickingSheet(){
        return null;
    }

    /**
     * 打印物流单
     */
    @PostMapping("/printLogisticsSheet")
    public ApiResult<?> printLogisticsSheet(){
        return null;
    }

    /**
     * 打印配货单
     */
    @PostMapping("/printAllocateCargoSheet")
    public ApiResult<?> printAllocateSheet(){
        return null;
    }
}
