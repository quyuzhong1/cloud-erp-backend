package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.server.wms.service.QcInfoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 质检单feign控制器
 * @CreateTime: 2023-06-19  15:48
 * @Author: zhangchunlin
 */
@RestController
@AllArgsConstructor
@RequestMapping("/feign/qcBill")
public class QcInfoFeignController extends BaseController {

    private final QcInfoService qcInfoService;

    @PostMapping("/getQcInfoByPurchaseOrder")
    public QcInfoDTO.PurchaseQcInfoDTO getQcInfoByPurchaseOrder(@RequestBody QcInfoDTO.PurchaseQcParamDTO dto) {
        return qcInfoService.getQcInfoByPurchaseOrder(dto);
    }

    @PostMapping("/getQcReceiveResult")
    public List<QcInfoDTO.QcReceiveResultDTO> getQcReceiveResult(@RequestBody List<String> purchaseDetailIds) {
        return qcInfoService.getQcReceiveResult(purchaseDetailIds);
    }
}