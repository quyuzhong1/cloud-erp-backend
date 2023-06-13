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
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.server.sys.service.DictCountryService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典管理
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
        return success(list);
    }
    @GetMapping("/country")
    public void addCountry(@RequestParam("country") String country) {
        dictCountryService.initRegionList(country);
    }
}
