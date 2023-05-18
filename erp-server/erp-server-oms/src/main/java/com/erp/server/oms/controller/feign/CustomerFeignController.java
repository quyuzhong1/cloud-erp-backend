package com.erp.server.oms.controller.feign;

import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.service.CustomerInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/customer")
public class CustomerFeignController {
    @Resource
    private CustomerInfoService customerInfoService;

    /**
     * 获取所有客户信息
     * @Author Luo_WG
     * @Date 2023/5/17 18:41
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     **/
    @PostMapping("/listCustomer")
    public List<CustomerInfoEntity> listCustomer() {
        return customerInfoService.list();
    }


    @PostMapping("/quoteCustomer")
    public Boolean quoteCustomer(@RequestBody List<String> ids) {
        return customerInfoService.quoteCustomer(ids);
    }
}
