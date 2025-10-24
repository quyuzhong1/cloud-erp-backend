package com.erp.server.fms.controller.feign;

import com.erp.server.fms.service.AssetAcceptDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: wtr
 * @Date: 2025/10/23 10:31
 * @Param:
 * @Return:
 * @Description:
 **/
@RestController
@RequestMapping("feign/assetAcept")
public class AssetAceptFeignController {

    @Resource
    private AssetAcceptDetailService assetAcceptDetailService;

    /**
     *
     * @param 资产采购单明细id
     * @return
     */
    @PostMapping("/getAcceptQty")
    public Integer getAcceptQtyByDetailId(@RequestBody String detailId) {
        return assetAcceptDetailService.getAcceptQtyByDetailId(detailId);
    }
}
