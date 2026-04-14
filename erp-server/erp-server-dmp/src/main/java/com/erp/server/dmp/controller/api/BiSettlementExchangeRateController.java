package com.erp.server.dmp.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.BiSettlementExchangeRateDTO;
import com.erp.server.dmp.service.BiSettlementExchangeRateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 汇率管理
 *
 * @author lei.nie
 * @since 2026-04-13
 */
@RestController
@LogSystemModule("数据源管理")
@RequestMapping("settlementExchangeRate")
public class BiSettlementExchangeRateController extends BaseController {

    @Resource
    private BiSettlementExchangeRateService biSettlementExchangeRateService;

    /**
     * 汇率页查询
     *
     * @param dto
     * @return ApiResult<PagingVO < ListDTO>>
     * @author Will
     * @date: 2023/8/15 10:27
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiSettlementExchangeRateDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiSettlementExchangeRateDTO.SearchParamDTO> dto) {
        PagingVO<BiSettlementExchangeRateDTO.ListDTO> pagingVO = biSettlementExchangeRateService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 数据源管理-结算汇率显示
     *
     * @return ApiResult
     * @author Will
     * @date: 2022/12/20 18:48
     */
    @PostMapping("/list")
    public ApiResult<List<Map<String, Object>>> listSettlementExchangeRate() {
        List<Map<String, Object>> list = biSettlementExchangeRateService.listSettlementExchangeRate();
        return success(list);
    }

    /**
     * 获取汇率
     *
     * @return ApiResult<BigDecimal>
     * @author Will
     * @date: 2023/8/24 17:47
     */
    @PostMapping("/findByCurrencyAndDate")
    public ApiResult<BigDecimal> findByCurrencyAndDate(@RequestBody @Validated BiSettlementExchangeRateDTO.CurrencyParamDTO dto) {
        BigDecimal exchangeRate = biSettlementExchangeRateService.findByCurrencyAndDate(dto.getDate(), dto.getCurrency());
        return success(exchangeRate);
    }

    /**
     * 将最新的汇率同步订货通
     */
    @PostMapping("/syncLastestRateToDht")
    public ApiResult<Boolean> syncLastestRateToDht() {
        biSettlementExchangeRateService.syncLastestRateToDht();
        return success();
    }
}
