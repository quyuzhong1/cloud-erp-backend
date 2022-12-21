package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.server.bi.service.BiSettlementExchangeRateService;
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
 * @description: TODO
 * @date 2022/12/19 9:51
 */
@RestController
@RequestMapping("bi/settlementExchangeRate")
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
    @PostMapping("/batchAdd")
    public ApiResult batchAddSettlementExchangeRate(@RequestBody  List<Map<String, String>> list) {
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
    public ApiResult batchUpdate(@RequestBody  List<Map<String, String>> list) {
        Boolean flag = this.biSettlementExchangeRateService.batchUpdateSettlementExchangeRate(list);
        return flag == true ? success() : failure();
    }

    /**
     * 数据源管理-结算汇率显示
     * @author Will
     * @date: 2022/12/20 18:48
     * @return ApiResult
     */
    @RequestMapping("/list")
    public ApiResult<List<Map<String, String>>> listSettlementExchangeRate() {
        List<Map<String, String>> list=  biSettlementExchangeRateService.listSettlementExchangeRate();
        return  success(list);
    }

}
