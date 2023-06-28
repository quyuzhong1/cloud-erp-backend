package com.erp.server.wms.controller.feign;

import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.server.wms.service.TransferInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/17 16:01
 */
@RestController
@RequestMapping("feign/transferInfo")
public class TransferInfoFeignController {

    @Resource
    private TransferInfoService transferInfoService;

    /**
     * @description: 根据编码查询有效直接直接调拨单
     * @author Will
     * @date: 2023/6/28 17:34
     * @param code 
     * @return TransferInfoDTO.ViewDTO
     */
    @PostMapping("/ViewTransferInfoByCode")
    public TransferInfoDTO.ViewDTO ViewTransferInfoByCode(@RequestBody String code) {
        return transferInfoService.ViewTransferInfoByCode(code);
    }
}
