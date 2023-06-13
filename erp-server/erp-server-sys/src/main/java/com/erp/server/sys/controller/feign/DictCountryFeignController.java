package com.erp.server.sys.controller.feign;

import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.server.sys.service.DictCountryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 获取国家信息feign
 * @Author Luo_WG
 * @Date 2023/5/26 10:49
 **/
@RestController
@RequestMapping("feign/dictCountry")
public class DictCountryFeignController {

    @Resource
    private DictCountryService dictCountryService;

    /**
     * 根据id获取国家信息
     * @Author Luo_WG
     * @Date 2023/5/26 10:45
     * @param id
     * @return java.util.List<com.erp.model.sys.entity.DictCountryEntity>
     **/
    @PostMapping("/getCountryById")
    public DictCountryEntity getCountryById(@RequestBody String id) {
        return dictCountryService.getById(id);
    }
}
