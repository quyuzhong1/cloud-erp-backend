package com.erp.server.srm.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.vo.SupplierConfigVO;
import com.erp.server.srm.service.CfgSettingService;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对账单
 * @author Will
 * @date: 2024/1/25 9:57
 */
@RestController
@RequestMapping("/feign/poReconciliation")
public class SrmPoReconciliationFeignController extends BaseController {
    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    /**
     * 新增对账明细
     * @author Will
     * @date: 2024/1/25 9:57
     * @param addList
     */
    @PostMapping("/add")
    public void add(@RequestBody @Validated List<PoReconciliationDetailDTO.AddDTO> addList) {
         poReconciliationDetailScmService.add(addList);
    }

}
