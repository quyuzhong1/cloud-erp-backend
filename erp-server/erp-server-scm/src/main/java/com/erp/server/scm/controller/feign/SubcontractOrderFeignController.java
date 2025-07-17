package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.ApproveOneDTO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
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
 * @author Will
 * @version 1.0
 * @date 2023/6/26 10:19
 */
@RestController
@RequestMapping("feign/subcontractOrder")
public class SubcontractOrderFeignController {

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    /**
     * @param sourceDetailIds
     * @return List<SubcontractOrderDetailEntity>
     * @description: 根据ids查询委外订单明细
     * @author Will
     * @date: 2023/6/26 10:21
     */
    @PostMapping("/listSubcontractDetailByIds")
    public List<SubcontractOrderDetailEntity> listSubcontractDetailByIds(@RequestBody List<String> sourceDetailIds) {
        if (CollectionUtils.isEmpty(sourceDetailIds)) {
            return Collections.EMPTY_LIST;
        }
        return subcontractOrderDetailService.listByIds(sourceDetailIds);
    }

    /**
     * @description: 根据ids查询子级委外订单明细
     * @author Will
     * @date: 2024/1/29 10:15
     * @param parentIdList
     * @return List<SubcontractOrderDetailEntity> 
     */
    @PostMapping("/listChildSubcontractDetailByIds")
    public List<SubcontractOrderDetailEntity> listChildSubcontractDetailByIds(@RequestBody List<String> parentIdList) {
        if (CollectionUtils.isEmpty(parentIdList)) {
            return Collections.EMPTY_LIST;
        }
        return subcontractOrderDetailService.listChildSubcontractDetailByIds(parentIdList);
    }
    
    /**
     * 根据主表ids查询委外订单明细
     * @author Will
     * @date: 2024/1/10 10:58
     * @param mainIdList 
     * @return List<SubcontractOrderDetailEntity> 
     */
    @PostMapping("/listSubcontractDetailByMainIds")
    public List<SubcontractOrderDetailEntity> listSubcontractDetailByMainIds(@RequestBody List<String> mainIdList) {
        return subcontractOrderDetailService.listByMainIds(mainIdList);
    }

    /**
     * 根据ids查询委外订单
     * @author Will
     * @date: 2024/1/9 15:40
     * @param sourceIdList
     * @return List<SubcontractOrderEntity>
     */
    @PostMapping("/listSubcontractOrderByIds")
    public List<SubcontractOrderEntity> listSubcontractOrderByIds(@RequestBody List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.EMPTY_LIST;
        }
        return subcontractOrderService.listByIds(sourceIdList);
    }

    /**
     * 审核
     *
     * @param dto
     * @return void
     * @Author Luo_WG
     * @Date 2023/7/12 12:55
     **/
    @PostMapping("/subcontractOrderApprove")
    public Boolean subcontractOrderApprove(@RequestBody ApproveOneDTO dto) {
        subcontractOrderService.approve(dto);
        return Boolean.TRUE;
    }

    /**
     * 根据bom sku
     * @author yl
     * @date 2023-10-12 9:39
     * @param bomSkuId bom skuId
     * @return java.util.List<com.erp.model.scm.dto.SubcontractOrderDTO.ListDTO>
     */
    @PostMapping("/listByBomSku")
    public List<SubcontractOrderDTO.ListDTO> listByBomSku(@RequestBody String bomSkuId) {
        return subcontractOrderService.listByBomSku(bomSkuId);
    }

    @PostMapping("/listBySourceId")
    public List<SubcontractOrderEntity> listByCodes(@RequestBody List<String> list) {
        return subcontractOrderService.listByCodes(list);
    }

    @PostMapping("/updateApproveStatus")
    public void updateApproveStatus(@RequestBody SubcontractOrderDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        subcontractOrderService.updateApproveStatus(updateApprovalStatusDTO);
    }
}
