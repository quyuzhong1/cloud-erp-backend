package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BaseDropDownDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.DictAmazonAreaCountryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.DictAmazonAreaCountryDTO;

import java.util.Arrays;
import java.util.List;

/**
 * 店铺管理
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@RestController
@RequestMapping("/dictAmazonAreaCountry")
public class DictAmazonAreaCountryController extends BaseController {

    @Autowired
    private DictAmazonAreaCountryService dictAmazonAreaCountryService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-30
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated DictAmazonAreaCountryDTO.AddDTO dto) {
        return success(dictAmazonAreaCountryService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-30
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated DictAmazonAreaCountryDTO.UpdateDTO dto) {
        dictAmazonAreaCountryService.update(dto);
        return success();
    }

    /**
     * 获取区域列表
     * @return
     */
    @GetMapping("/areaList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> areaList() {
        List<BaseDropDownDTO.CommonDTO> resultList = dictAmazonAreaCountryService.areaList();
        return success(resultList);
    }


    /**
     * 根据区域获取国家列表
     * @return
     */
    @GetMapping("/countryList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> countryList(@RequestParam("area") String area) {
        List<BaseDropDownDTO.CommonDTO> resultList = dictAmazonAreaCountryService.listCountryByArea(Arrays.asList(area));
        return success(resultList);
    }


}
