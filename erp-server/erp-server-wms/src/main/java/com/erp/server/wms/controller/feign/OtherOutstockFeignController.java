package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.server.wms.service.OtherInstockService;
import com.erp.server.wms.service.OtherOutstockService;
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
@RequestMapping("/feign/otherOutstock")
public class OtherOutstockFeignController extends BaseController{

    @Resource
    private OtherOutstockService otherOutstockService;

    @PostMapping("/listByCodes")
    List<OtherOutstockEntity> listByCodes(@RequestBody @Validated List<String> list){
        return otherOutstockService.listByCodes(list);
    }

    @PostMapping("/updateApproveStatus")
    public void updateApproveStatus(@RequestBody @Validated OtherOutstockDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        otherOutstockService.updateApproveStatus(updateApprovalStatusDTO);
    }
}