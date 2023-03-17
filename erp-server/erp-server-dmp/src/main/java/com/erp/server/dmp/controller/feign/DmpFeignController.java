package com.erp.server.dmp.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 远程调用控制层
 * @author Will
 * @date: 2023/1/11 17:46
 */
@RestController
@RequestMapping("dmp/feign")
public class DmpFeignController extends BaseController {

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @PostMapping("/getShopById")
    public DmpShopInfoDTO getShopById(@RequestBody String shopId) {
        return dmpShopInfoService.getShopById(shopId);
    }

}
