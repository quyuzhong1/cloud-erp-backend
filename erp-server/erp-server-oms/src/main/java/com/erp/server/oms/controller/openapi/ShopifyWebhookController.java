package com.erp.server.oms.controller.openapi;

import com.erp.model.oms.dto.ShopifyWebhookDTO;
import com.erp.server.oms.service.ShopInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/shopifyWebhook")
public class ShopifyWebhookController {

    @Resource
    private ShopInfoService shopInfoService;


    /**
     * 请求查看存储的客户数据
     * @Author Luo_WG
     * @Date 2024/2/23 14:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/customersDataRequest")
    public void customersDataRequest(@RequestBody ShopifyWebhookDTO.CustomersDataRequestDTO dto, HttpServletResponse response, HttpServletRequest request) {
        shopInfoService.customersDataRequest(dto, response, request);
    }

    /**
     * 要求删除客户数据
     * @Author Luo_WG
     * @Date 2024/2/23 14:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/customersRedact")
    public void customersRedact(@RequestBody ShopifyWebhookDTO.CustomersRedactDTO dto, HttpServletResponse response, HttpServletRequest request) {
        shopInfoService.customersRedact(dto, response, request);
    }


    /**
     * 要求删除店铺数据
     * @Author Luo_WG
     * @Date 2024/2/23 14:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/shopRedact")
    public void shopRedact(@RequestBody ShopifyWebhookDTO.ShopRedactDTO dto, HttpServletResponse response, HttpServletRequest request) {
        shopInfoService.shopRedact(dto, response, request);
    }



    /**
     * 要求删除店铺数据测试
     * @Author Luo_WG
     * @Date 2024/2/23 14:07
     * @param data
     * @return com.common.core.controller.vo.ApiResult
     **/

    @PostMapping("/shopRedactTest")
    public ResponseEntity<String> shopRedactTest(@RequestBody String data, HttpServletResponse response, HttpServletRequest request) {

        return shopInfoService.shopRedactTest(data, response, request);

    }


}
