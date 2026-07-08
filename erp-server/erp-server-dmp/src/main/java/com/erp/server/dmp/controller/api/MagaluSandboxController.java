package com.erp.server.dmp.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.sdk.oms.magalu.dto.MagaluShopInfoDTO;
import com.sdk.oms.magalu.service.MagaluService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Magalu 沙箱测试辅助接口（造单等），仅用于测试环境联调。
 */
@RestController
@RequestMapping("/magalu/sandbox")
public class MagaluSandboxController extends BaseController {

    @Resource
    private MagaluService magaluService;

    @PostMapping("/onboarding")
    public ApiResult<JSONObject> onboarding(@RequestBody Map<String, String> request) {
        MagaluShopInfoDTO shopInfoDTO = requireShopInfo(request.get("shopId"));
        return success(magaluService.putSandboxOnboarding(shopInfoDTO));
    }

    @PostMapping("/createOrder")
    public ApiResult<JSONObject> createOrder(@RequestBody Map<String, String> request) {
        MagaluShopInfoDTO shopInfoDTO = requireShopInfo(request.get("shopId"));
        String sku = StringUtils.defaultIfBlank(request.get("sku"), "ERPTESTSKU20260702113421");
        int quantity = 1;
        if (StringUtils.isNotBlank(request.get("quantity"))) {
            quantity = Integer.parseInt(request.get("quantity"));
        }
        String paymentMethod = StringUtils.defaultIfBlank(request.get("paymentMethod"), "pix");
        JSONObject onboarding = magaluService.putSandboxOnboarding(shopInfoDTO);
        JSONObject order = magaluService.createSandboxSampleOrder(shopInfoDTO, sku, quantity, paymentMethod);
        Map<String, Object> result = new LinkedHashMap<>(4);
        result.put("onboarding", onboarding);
        result.put("order", order);
        return success(new JSONObject(result));
    }

    private MagaluShopInfoDTO requireShopInfo(String shopId) {
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException("shopId不能为空");
        }
        MagaluShopInfoDTO shopInfoDTO = magaluService.getShopInfoByShopId(shopId);
        if (shopInfoDTO == null || StringUtils.isBlank(shopInfoDTO.getAccessToken())) {
            throw new ServiceException("Magalu店铺授权信息不存在");
        }
        return shopInfoDTO;
    }
}
