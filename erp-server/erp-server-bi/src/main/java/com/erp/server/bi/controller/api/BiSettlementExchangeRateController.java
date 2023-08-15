package com.erp.server.bi.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiSettlementExchangeRateDTO;
import com.erp.server.bi.service.BiSettlementExchangeRateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 数据源管理
 * @author Will
 * @version 1.0

 * @date 2022/12/19 9:51
 */
@RestController
@RequestMapping("settlementExchangeRate")
public class BiSettlementExchangeRateController extends BaseController {

    @Resource
    private BiSettlementExchangeRateService biSettlementExchangeRateService;

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/8/15 10:27
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiSettlementExchangeRateDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiSettlementExchangeRateDTO.SearchParamDTO> dto) {
        PagingVO<BiSettlementExchangeRateDTO.ListDTO> pagingVO = biSettlementExchangeRateService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 数据源管理-结算汇率新增
     * @author Will
     * @date: 2022/12/19 10:45
     * @param list
     * @return ApiResult
     */
    @PostMapping("/batchAdd")
    public ApiResult batchAddSettlementExchangeRate(@RequestBody  List<Map<String, Object>> list) {
        Boolean flag = this.biSettlementExchangeRateService.batchAddSettlementExchangeRate(list);
        return flag == true ? success() : failure();
    }


    /**
     * 数据源管理-结算汇率编辑
     * @author Will
     * @date: 2022/12/19 10:45
     * @param list
     * @return ApiResult
     */
    @PostMapping("/batchUpdate")
    public ApiResult batchUpdate(@RequestBody  List<Map<String, Object>> list) {
        Boolean flag = this.biSettlementExchangeRateService.batchUpdateSettlementExchangeRate(list);
        return flag == true ? success() : failure();
    }

    /**
     * 数据源管理-结算汇率显示
     * @author Will
     * @date: 2022/12/20 18:48
     * @return ApiResult
     */
    @PostMapping("/list")
    public ApiResult<List<Map<String, Object>>> listSettlementExchangeRate() {
        List<Map<String, Object>> list=  biSettlementExchangeRateService.listSettlementExchangeRate();
        return  success(list);
    }

}
