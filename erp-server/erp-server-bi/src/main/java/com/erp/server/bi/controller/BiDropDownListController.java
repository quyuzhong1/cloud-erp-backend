package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.vo.SalesPlatformEnumVO;
import com.erp.model.bi.vo.SalesSiteEnumVO;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.SalesPlatformEnum;
import com.erp.model.dmp.enums.SalesSiteEnum;
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
        List<SalesPlatformEnumVO> result = Arrays.stream(SalesPlatformEnum.values()).map(x -> new SalesPlatformEnumVO(x.getCode(),x.getName(),x.getDesc())).collect(Collectors.toList());
        return success(result);
    }

    /**
     * 站点下拉列表
     * @return
     */
    @GetMapping("/site/list")
    public ApiResult<List<SalesSiteEnumVO>> listSiteDropDown() {
        List<SalesSiteEnumVO> result = Arrays.stream(SalesSiteEnum.values()).map(x -> new SalesSiteEnumVO(x.getCode(),x.getName(),x.getDesc())).collect(Collectors.toList());
        return success(result);
    }
}
