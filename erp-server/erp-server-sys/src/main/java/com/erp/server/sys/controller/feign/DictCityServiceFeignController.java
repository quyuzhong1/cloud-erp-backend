package com.erp.server.sys.controller.feign;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictThirdCity;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.DictThirdCityService;
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
    private DictThirdCityService dictThirdCityService;

    /**
     * 根据id查询省/市
     */
    @PostMapping("/getById")
    public DictCityEntity getById(@RequestBody String id) {
        return dictCityService.getById(id);
    }

    /**
     * 根据ids查询省/市
     */
    @PostMapping("/listByIds")
    public List<DictCityEntity> listByIds(@RequestBody List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        return dictCityService.listByIds(ids);
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
     * 获取第三方城市
     * @param dictIds
     * @return
     */
    @GetMapping("/listThirdCityByDictIdList")
    public List<DictThirdCity> listThirdCityByDictIdList(@RequestParam(value = "dictIds") List<String> dictIds, @RequestParam(value = "platform")String platform) {
        return dictThirdCityService.listByDictIdList(dictIds,platform);
    }

    /**
     * 根据名称查询
     */
    @PostMapping("/listByNames")
    public List<DictCityEntity> listByNames(@RequestBody List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Collections.EMPTY_LIST;
        }
        return dictCityService.listByNames(names);
    }
}
