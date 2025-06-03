package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.server.wms.service.TransferApplicationService;
import com.erp.server.wms.service.TransferInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * feign控制器
 *
 */
@RestController
@RequestMapping("/feign/transferApplication")
public class TransferApplicationFeignController extends BaseController{

    @Resource
    private TransferApplicationService transferApplicationService;

    @PostMapping("/listByCodes")
    List<TransferApplicationEntity> listByCodes(@RequestBody @Validated List<String> list){
        return transferApplicationService.listByCodes(list);
    }

    @PostMapping("/updateApproveStatus")
    public void updateApproveStatus(@RequestBody @Validated TransferApplicationDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        transferApplicationService.updateApproveStatus(updateApprovalStatusDTO);
    }
}