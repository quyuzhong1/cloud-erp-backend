package com.erp.server.sys.controller.feign;

import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.ImlDictCityEntity;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.ImlDictCityService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 省/市
 *
 * @Author Luo_WG
 * @Date 2023/5/30 19:27
 **/
@RestController
@RequestMapping("feign/city")
public class DictCityServiceFeignController {

    @Resource
    private DictCityService dictCityService;
    @Resource
    private ImlDictCityService imlDictCityService;

    /**
     * 根据id查询省/市
     */
    @PostMapping("/getById")
    public DictCityEntity getById(@RequestBody String id) {
        return dictCityService.getById(id);
    }


    /**
     * 获取省份城市列表
     *
     * @param countryCode
     * @return
     */
    @GetMapping("/getProvincesByCountryCode")
    public List<DictCityDTO.ListDTO> getProvincesByCountryCode(@RequestParam("countryCode") String countryCode) {
        List<DictCityDTO.ListDTO> list = dictCityService.listCity(countryCode);
        return list;
    }


    @PostMapping("/listByIdList")
    public List<DictCityEntity> listByIdList(@RequestBody List<String> idList) {

        return dictCityService.listByIdList(idList);

    }

    /**
     * 获取艾姆勒城市
     * @param dictIds
     * @return
     */
    @PostMapping("/listImlCityByDictIdList")
    public List<ImlDictCityEntity> listImlCityByDictIdList(@RequestBody List<String> dictIds) {
        return imlDictCityService.listByDictIdList(dictIds);
    }


}
