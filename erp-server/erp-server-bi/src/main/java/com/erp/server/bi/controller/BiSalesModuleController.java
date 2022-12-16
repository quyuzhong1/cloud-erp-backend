package com.erp.server.bi.controller;

import com.erp.common.business.annotation.DataPermission;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.DataAttributeEnum;
import com.erp.model.bi.vo.StatisticalDataVO;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 销售额模块Api 接口
 *
 * @Classname BiSalesBusinessDivisionController
 * @Description TODO
 * @Date 2022-12-15 11:34
 * @Created by yl
 */
@RestController
@RequestMapping("bi/sales")
public class BiSalesModuleController extends BaseController {


    @Resource
    private DmpOrderInfoService orderInfoService;


    @GetMapping("/byMonth")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:sales:byMonth",
            tableAlias = "dmp_order_info"
    )
    public ApiResult<StatisticalDataVO> getMonth() {
        StatisticalDataVO statistical = orderInfoService.getMonthSales();
        return success(statistical);
    }


}
