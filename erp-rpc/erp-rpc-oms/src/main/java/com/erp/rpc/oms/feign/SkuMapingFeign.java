package com.erp.rpc.oms.feign;

import com.erp.model.oms.entity.SkuMapingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "skuMaping")
public interface SkuMapingFeign {

    /**
     * 根据skuId查询对照表
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.oms.entity.SkuMapingEntity>
     * @Author Luo_WG
     * @Date 2023/7/13 11:33
     **/
    @PostMapping("feign/skuMaping/listBySkuId")
    List<SkuMapingEntity> listBySkuId(@RequestBody List<String> skuIds);
}
