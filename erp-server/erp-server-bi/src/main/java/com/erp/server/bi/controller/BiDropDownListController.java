package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.vo.SalesPlatformEnumVO;
import com.erp.model.bi.vo.SalesSiteEnumVO;
import com.erp.model.bi.vo.SelectShowVO;
import com.erp.model.dmp.enums.SalesPlatformEnum;
import com.erp.model.dmp.enums.SalesSiteEnum;
import com.erp.server.bi.enums.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BI 下拉列表
 *
 * @Author Cloud
 * @Date 2022/12/19 11:26
 **/

@RestController
@RequestMapping("bi/drop/down")
public class BiDropDownListController extends BaseController {

    /**
     * 平台下拉列表
     *
     * @return 单条数据
     */
    @GetMapping("/platform/list")
    public ApiResult<List<SalesPlatformEnumVO>> listPlatformDropDown() {
        List<SalesPlatformEnumVO> result = Arrays.stream(SalesPlatformEnum.values())
                .map(x -> new SalesPlatformEnumVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 站点下拉列表
     * @return
     */
    @GetMapping("/site/list")
    public ApiResult<List<SalesSiteEnumVO>> listSiteDropDown() {
        List<SalesSiteEnumVO> result = Arrays.stream(SalesSiteEnum.values())
                .map(x -> new SalesSiteEnumVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 启用禁用下拉框
     * @return
     */
    @GetMapping("/state/list")
    public ApiResult<List<SelectShowVO>> listStateDropDown() {
        List<SelectShowVO> result = Arrays.stream(BiStateEnum.values())
                .map(x -> new SelectShowVO().setCode(x.getCode()).setName(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 店铺标识下拉框
     * @return
     */
    @GetMapping("/storeSign/list")
    public ApiResult<List<SelectShowVO>> listStoreSignDropDown() {
        List<SelectShowVO> result = Arrays.stream(StoreSignEnum.values())
                .map(x -> new SelectShowVO().setName(x.getCode()).setDesc(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 订单状态下拉框
     * @return
     */
    @GetMapping("/orderStatus/list")
    public ApiResult<List<SelectShowVO>> listOrderStatusDropDown() {
        List<SelectShowVO> result = Arrays.stream(OrderStateEnum.values())
                .map(x -> new SelectShowVO().setCode(x.getCode()).setName(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


    @GetMapping("/time/type/list")
    public ApiResult<List<SelectShowVO>> listTimeTypeDropDown() {
        List<SelectShowVO> result = Arrays.stream(TimeTypeEnum.values())
                .map(x -> new SelectShowVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    @GetMapping("/settle/method/list")
    public ApiResult<List<SelectShowVO>> listSettleMethodDropDown() {
        List<SelectShowVO> result = Arrays.stream(SettleMethodEnum.values())
                .map(x -> new SelectShowVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

}
