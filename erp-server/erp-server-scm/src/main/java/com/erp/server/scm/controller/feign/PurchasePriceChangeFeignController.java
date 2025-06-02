package com.erp.server.scm.controller.feign;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.server.scm.service.PurchasePriceChangeService;
import com.erp.server.scm.service.PurchasePriceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购价目表
 * @Author Luo_WG
 * @Date 2023/6/30 19:44
 **/
@RestController
@RequestMapping("feign/purchasePriceChange")
public class PurchasePriceChangeFeignController {

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

     @PostMapping("/listByCodes")
    public List<PurchasePriceChangeEntity> listByCodes(@RequestBody List<String> codes) {
        return purchasePriceChangeService.listByCodes(codes);
    }

     @PostMapping("/updateApproveStatus")
    public void updateApproveStatus(@RequestBody PurchasePriceChangeDTO.UpdateApprovalStatusDTO  updateApprovalStatusDTO) {
         purchasePriceChangeService.updateApproveStatus(updateApprovalStatusDTO);
     }
}
