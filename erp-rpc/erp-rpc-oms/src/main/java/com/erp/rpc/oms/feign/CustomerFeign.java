package com.erp.rpc.oms.feign;

import com.erp.model.oms.entity.CustomerInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "customer")
public interface CustomerFeign {
    /**
     * 获取客户信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     * @return com.erp.model.oms.entity.SoInfoEntity
     **/
    @PostMapping("feign/customer/listCustomer")
    List<CustomerInfoEntity> listCustomer();


    /**
     * 引用客户
     * @author yl
     * @date 2023-05-17 18:35
     * @param ids  ids
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     */
    @PostMapping("feign/customer/quoteCustomer")
    List<CustomerInfoEntity> quoteCustomer(@RequestBody List<String> ids);
}
