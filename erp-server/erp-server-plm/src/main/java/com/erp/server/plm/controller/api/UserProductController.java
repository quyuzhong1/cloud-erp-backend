package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.plm.service.UserAddProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("user/product")
public class UserProductController extends BaseController {

    @Autowired
    private UserAddProductService userAddProductService;

    /**
     * 产品列表-收藏产品
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-09 14:38
     */
    @PostMapping("/userAddProduct")
    //   @RequestPermissions("plm:user:product:userAddProduct")
    public ApiResult userAddProduct(@RequestParam(value = "productId") String productId) {
        boolean flag = userAddProductService.userAddProduct(productId);
        return flag == true ? success() : failure();
    }

    /**
     * 产品列表-取消收藏产品
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-09 14:38
     */
    @PostMapping("/userCancelProduct")
    //  @RequestPermissions("plm:user:product:userCancelProduct")
    public ApiResult userCancelProduct(@RequestParam(value = "productId") String productId) {
        boolean flag = userAddProductService.userCancelProduct(productId);
        return flag == true ? success() : failure();
    }

    /**
     * 产品列表-取消收藏产品
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-09 14:38
     */
    @PostMapping("/list")
    //  @RequestPermissions("plm:user:product:list")
    public ApiResult list() {
        List<Map<String, Object>> list = userAddProductService.listByUserId();
        return success(list);
    }
}
