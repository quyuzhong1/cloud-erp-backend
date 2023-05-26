package com.erp.rpc.sys.feign;

import com.erp.model.sys.entity.DictCountryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "erp-sys", contextId = "dictCountry")
public interface DictCountryFeign {

    /**
     * 根据id获取国家信息
     * @Author Luo_WG
     * @Date 2023/5/26 10:45
     * @param id
     * @return java.util.List<com.erp.model.sys.entity.DictCountryEntity>
     **/
    @PostMapping("feign/dictCountry/getCountryById")
    DictCountryEntity getCountryById(String id);
}
