package com.erp.server.sys.controller.api;


import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictNodeDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.server.sys.service.DictCountryService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 国家管理
 *
 * @author Lambda
 * @since 2023-03-21
 */
@RestController
@RequestMapping("dict/country")
public class DictCountryController extends BaseController {


    @Resource
    private DictCountryService dictCountryService;


    /**
     * 获取国家列表
     *
     * @param
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictCountryDTO.ListDTO>> list() {
        List<DictCountryDTO.ListDTO> list = dictCountryService.listCountry();
        List<DictCountryDTO.ListDTO> resultList = list.stream()
                .filter(e -> !e.getDisabled())
                .collect(Collectors.toList());
        return success(resultList);
    }

    @GetMapping("/country")
    public void addCountry(@RequestParam("country") String country) {
        dictCountryService.initRegionList(country);
    }

    /**
     * 根据类型获取到区域国家列表列表
     *
     * @param type
     */
    @GetMapping("/areaCountryList")
    public ApiResult<List<DictCountryDTO.CascadeDTO>> areaCountryListByType(@RequestParam("type") String type) {
        List<DictCountryDTO.CascadeDTO> list = dictCountryService.areaCountryListByType(type);
        return success(list);
    }


    /**
     * 查询国家区域数据
     * @author Will
     * @date: 2023/11/9 9:36
     * @param dto
     * @return ApiResult<List<ListRegionDTO>>
     */
    @PostMapping("/listAreaCountry")
    public ApiResult<List<DictCountryDTO.ListRegionDTO>> listAreaCountry(@RequestBody @Validated DictCountryDTO.ListParamDTO dto) {
        List<DictCountryDTO.ListRegionDTO> list = dictCountryService.listAreaCountry(dto);
        return success(list);
    }

    /**
     * 根据参数查询国家数据
     * @author Will
     * @date: 2023/11/9 15:03
     * @param dto
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/listCountryByParam")
    public ApiResult<List<DictCountryDTO.ListDTO>> listCountryByParam(@RequestBody @Validated DictCountryDTO.ListParamDTO dto) {
        List<DictCountryDTO.ListDTO> list = dictCountryService.listCountryByParam(dto);
        return success(list);
    }

}
