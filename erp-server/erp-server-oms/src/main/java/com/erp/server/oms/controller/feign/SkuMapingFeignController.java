package com.erp.server.oms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.oms.entity.SkuMapingEntity;
import com.erp.server.oms.service.SkuMapingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * sku对照表
 *
 * @Author Luo_WG
 * @Date 2023/5/15 9:12
 **/
@RestController
@RequestMapping("feign/skuMaping")
public class SkuMapingFeignController extends BaseController {

    @Resource
    private SkuMapingService skuMapingService;

    /**
     * 根据skuId查询对照表
     * @Author Luo_WG
     * @Date 2023/7/13 11:33
     * @param skuIds
     * @return java.util.List<com.erp.model.oms.entity.SkuMapingEntity>
     **/
    @PostMapping("/listBySkuId")
    public List<SkuMapingEntity> listBySkuId(@RequestBody List<String> skuIds) {
        return skuMapingService.listBySkuId(skuIds);
    }
}
