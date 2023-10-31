package com.erp.server.sys.controller.feign;

import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.server.sys.service.DictCountryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 获取国家信息feign
 * @Author Luo_WG
 * @Date 2023/5/26 10:49
 **/
@RestController
@RequestMapping("/feign/dictCountry")
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


    /**
     * 获取国家列表
     * @param
     * @return
     */
    @GetMapping("/list")
    public List<DictCountryDTO.ListDTO> list() {
        List<DictCountryDTO.ListDTO> list = dictCountryService.listCountry();
        return list;
    }

}
