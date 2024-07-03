package com.erp.server.wms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.server.wms.query.WaveListAdvanceQueryHandler;
import com.erp.server.wms.service.WaveListService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 波次列表
 * @date 2024-06-20
 * @author tanmujin
 */
@RestController
@RequestMapping("/waveList")
public class WaveListController extends BaseController {

    @Resource
    private WaveListService waveListService;

    /**
     * 分页查询
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = WaveListAdvanceQueryHandler.class)
    public ApiResult<PagingVO<WaveListDTO.ViewDTO>> paging(@RequestBody PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO){
        PagingVO<WaveListDTO.ViewDTO> pagingVO = waveListService.paging(pagingDTO);
        return ApiResult.success(pagingVO);
    }

    /**
     * 取消波次（批量）
     */
    @PostMapping("/cancelWaveBatch")
    public ApiResult<List<BatchResultDTO>> cancelWaveBatch(@RequestBody BaseIdsDTO.IdsDTO idsDTO){
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (String id : idsDTO.getIds()) {
            BatchResultDTO dto = waveListService.cancelWave(id);
            resultList.add(dto);
        }
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }

    /**
     * 取消已打印（批量）
     */
    @PostMapping("/cancelPrintedBatch")
    public ApiResult<List<BatchResultDTO>> cancelPrintedBatch(@RequestBody BaseIdsDTO.IdsDTO idsDTO){
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (String id : idsDTO.getIds()) {
            BatchResultDTO dto = waveListService.cancelPrinted(id);
            resultList.add(dto);
        }
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }

    /**
     * 打印拣货单
     */
    @PostMapping("/printPickingBill")
    public ApiResult<?> printPickingBill(){
        return null;
    }

    /**
     * 打印物流单
     */
    @PostMapping("/printLogisticsBill")
    public ApiResult<?> printLogisticsBill(){
        return null;
    }

    /**
     * 打印配货单
     */
    @PostMapping("/printAllocateCargoBill")
    public ApiResult<?> printAllocateCargoBill(){
        return null;
    }

    /**
     * tabList
     */
    @GetMapping("/tabList")
    public ApiResult<List<WaveListDTO.TabDTO>> tabList() {
        List<WaveListDTO.TabDTO> list = waveListService.tabList();
        return ApiResult.success(list);
    }
}
