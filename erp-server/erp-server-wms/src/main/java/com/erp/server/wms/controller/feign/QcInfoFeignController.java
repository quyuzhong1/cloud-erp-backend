package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.server.wms.service.QcInfoService;
import com.erp.server.wms.service.QcResultService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    @Resource
    private QcResultService qcResultService;

    @PostMapping("/getQcInfoByPurchaseOrder")
    public QcInfoDTO.PurchaseQcInfoDTO getQcInfoByPurchaseOrder(@RequestBody QcInfoDTO.PurchaseQcParamDTO dto) {
        return qcInfoService.getQcInfoByPurchaseOrder(dto);
    }

    @PostMapping("/getQcReceiveResult")
    public List<QcInfoDTO.QcReceiveResultDTO> getQcReceiveResult(@RequestBody List<String> purchaseDetailIds) {
        return qcInfoService.getQcReceiveResult(purchaseDetailIds);
    }

    @PostMapping("/getFsQcNoticeTitle")
    public String getFsQcNoticeTitle(@RequestParam("title") String title){
        return qcInfoService.getFsQcNoticeTitle(title);
    }

    /**
     * 根据质检单id 查询质检结果
     * @param qcInfoIds
     * @return List<QcResultDTO.QcNoticeDTO>
     */
    @PostMapping("/listQcResultMsg")
    public List<QcResultDTO.QcNoticeDTO> listQcResultMsg(@RequestBody List<String> qcInfoIds){
        return qcResultService.listQcResultMsg(qcInfoIds);
    }
}