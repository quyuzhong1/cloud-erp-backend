package com.erp.server.scm.controller.feign;

import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/6/26 10:19
 */
@RestController
@RequestMapping("feign/subcontractOrder")
public class SubcontractOrderFeignController {

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    /**
     * @description: 根据ids查询委外订单明细
     * @author Will
     * @date: 2023/6/26 10:21
     * @param sourceDetailIds
     * @return List<SubcontractOrderDetailEntity>
     */
    @PostMapping("/listSubcontractDetailByIds")
    public List<SubcontractOrderDetailEntity> listSubcontractDetailByIds(@RequestBody List<String> sourceDetailIds) {
        return subcontractOrderDetailService.listByIds(sourceDetailIds);
    }
}
