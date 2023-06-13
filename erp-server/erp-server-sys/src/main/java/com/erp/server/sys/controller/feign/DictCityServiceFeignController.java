package com.erp.server.sys.controller.feign;

import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.server.sys.service.DictCityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 省/市
 * @Author Luo_WG
 * @Date 2023/5/30 19:27
 **/
@RestController
@RequestMapping("feign/city")
public class DictCityServiceFeignController {

    @Resource
    private DictCityService dictCityService;

    /**
     * 根据id查询省/市
     */
    @PostMapping("/getById")
    public DictCityEntity getById(@RequestBody String id) {
        return dictCityService.getById(id);
    }
}
