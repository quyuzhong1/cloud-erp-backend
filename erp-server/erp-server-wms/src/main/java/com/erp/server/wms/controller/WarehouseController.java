package com.erp.server.wms.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 仓库表 前端控制器
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/warehouse")
public class WarehouseController extends BaseController {



    /**
     * 保存或者修改仓库
     *
     * @param
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate() {
        return success();
    }

}
