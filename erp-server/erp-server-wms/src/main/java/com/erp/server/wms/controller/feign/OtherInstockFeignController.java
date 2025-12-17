package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.TransferApplicationEntity;
import com.erp.server.wms.service.OtherInstockService;
import com.erp.server.wms.service.TransferApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * feign控制器
 *
 */
@RestController
@RequestMapping("/feign/otherInstock")
public class OtherInstockFeignController extends BaseController{

    @Resource
    private OtherInstockService otherInstockService;

    @PostMapping("/listByCodes")
    List<OtherInstockEntity> listByCodes(@RequestBody @Validated List<String> list){
        return otherInstockService.listByCodes(list);
    }

    @PostMapping("/updateApproveStatus")
    public void updateApproveStatus(@RequestBody @Validated OtherInstockDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        otherInstockService.updateApproveStatus(updateApprovalStatusDTO);
    }
    @GetMapping("/listOtherInstockByExhibitionId")
    public List<ExhibitionOrderDTO.DownstreamListDTO> listOtherInstockByExhibitionId(@RequestParam(value = "exhibitionId") String exhibitionId) {
        return otherInstockService.listOtherInstockByExhibitionId(exhibitionId);
    }
}