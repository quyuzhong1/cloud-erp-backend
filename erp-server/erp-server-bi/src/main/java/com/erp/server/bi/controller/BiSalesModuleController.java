package com.erp.server.bi.controller;

import com.erp.common.business.annotation.DataPermission;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.DataAttributeEnum;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.SalesByCountryVO;
import com.erp.model.bi.vo.SalesVO;
import com.erp.model.bi.vo.StatisticalDataVO;
import com.erp.server.bi.service.SalesOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    private SalesOrderService salesOrderService;


    /**
     * 销售额- 一级模块-月销售额趋势
     *
     * @return
     */
    @GetMapping("/byMonth")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:sales:byMonth",
            tableAlias = "dmp_order_info"
    )
    public ApiResult<StatisticalDataVO> getMonth() {
        StatisticalDataVO statistical = salesOrderService.getMonthSales();
        return success(statistical);
    }


    /**
     * 销售额- 一级模块-SKU销售额
     *
     * @return
     */
    @PostMapping("/bySku")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:bySku",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesVO>> getBySku(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesVO> resultList = salesOrderService.getBySku(dto);
        return success(resultList);
    }


    /**
     * 销售额- 一级模块-SKU销售额
     *
     * @return
     */
    @PostMapping("/bySpu")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:bySpu",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesVO>> getBySpu(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesVO> resultList = salesOrderService.getBySpu(dto);
        return success(resultList);
    }


    /**
     * 销售相关-一级模块-SKU国家销售额
     * @param dto
     * @return
     */
    @PostMapping("/byCountry")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesByCountryVO>> getByCountry(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesByCountryVO> resultList = salesOrderService.getByCountry(dto);
        return success(resultList);
    }








}
