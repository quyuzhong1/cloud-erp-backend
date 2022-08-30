package com.cloud.erp.chrome.controller;


import com.cloud.erp.chrome.dto.GyyShipmentsDTO;
import com.cloud.erp.chrome.service.GyyShipmentsInfoService;
import com.erp.common.dto.base.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

/**
 * <p>
 * 管易云 erp 发货信息表 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-08-26
 */
@RestController
@RequestMapping("gyy/api")
@CrossOrigin(origins = "*")
public class GyyShipmentsInfoController extends BaseController {



    @Autowired
    private GyyShipmentsInfoService gyyShipmentsInfoService;



    @PostMapping("/importFile")
    public ApiResult importCsv(@RequestBody GyyShipmentsDTO dto) {
        gyyShipmentsInfoService.saveShipmentsCsvByUrl(dto);
        return success();
    }

}

