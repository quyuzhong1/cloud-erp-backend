package com.erp.rpc.plm.feign;

import com.erp.model.plm.vo.SkuVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * plm 远程获取产品信息
 * @author Lambda
 * @Classname PlmProductFeign
 * @Description TODO
 * @Date 2023-04-17 15:45
 * @Created by yl
 */
@FeignClient(name="erp-plm")
public interface PlmProductFeign {


    @PostMapping("feign/product/getSkuInfoByIds")
    List<SkuVO> getSkuInfoByIds(@RequestBody List<String> skuIds);
}
