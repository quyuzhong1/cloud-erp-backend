package com.erp.server.bi.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.server.bi.service.BiSettlementExchangeRateService;
import org.springframework.web.bind.annotation.*;

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
@LogSystemModule("数据源管理")
@RequestMapping("settlementExchangeRate")
public class BiSettlementExchangeRateController extends BaseController {

    @Resource
    private BiSettlementExchangeRateService biSettlementExchangeRateService;


    /**
     * 数据源管理-结算汇率新增
     * @author Will
     * @date: 2022/12/19 10:45
     * @param list
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UNKNOWN_UPDATE, desc = "批量新增结算汇率")
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
    @LogAction(value = LogActionEnum.UNKNOWN_UPDATE, desc = "批量更新结算汇率")
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
