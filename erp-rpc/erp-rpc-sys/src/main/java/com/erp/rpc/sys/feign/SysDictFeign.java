package com.erp.rpc.sys.feign;

import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.DictCityEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Classname: SysDictFeign
 * @Description: 字典feign接口
 * @CreateTime: 2023-06-21  15:10
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-sys", contextId = "dictBasic")
public interface SysDictFeign {

    /**
     * 获取字典数据 根据属性
     * @param type
     * @return
     */
    @GetMapping("/feign/dictBasic/getByType")
    List<DictBasicDTO.ViewDTO> getByType(@RequestParam(value = "type") String type);


    /**
     * 根据国家id 集合 获取到国家列表
     * @param ids
     * @return
     */
    @PostMapping("feign/dictCountry/listCountryByIds")
    List<DictCountryEntity> listCountryByIds(@RequestBody List<String> ids);


    /**
     * 根据地区id 集合 获取到地区列表
     * @param ids
     * @return
     */
    @PostMapping("feign/globalArea/listGlobalAreaByIds")
    List<DictGlobalAreaEntity> listGlobalAreaByIds(@RequestBody List<String> ids);

    @PostMapping("/feign/dictBasic/getByType")
    List<DictCityEntity> listByIdList(List<String> placeIdList);
}
