package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.ApproveOneDTO;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.server.scm.service.CfgSupplierSalesService;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import com.erp.server.scm.service.SubcontractOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author jack
 * @version 1.0
 * @date 2025-06-23
 */
@RestController
@RequestMapping("feign/cfgSupplierSales")
public class CfgSupplierSalesFeignController {

    @Resource
    private CfgSupplierSalesService cfgSupplierSalesService;

    /**
     * @description: 获取所有启用的销量设置以及明细
     * @author Jack
     * @date: 2025-06-23
     */
    @PostMapping("/listAll")
    public List<CfgSupplierSalesDTO.ListAllDTO> listAll() {
        return cfgSupplierSalesService.listAll();
    }

}
