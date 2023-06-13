package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.scm.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 采购订单feign
 *
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@RestController
@RequestMapping("feign/scmWorkOption")
public class ScmWorkOptionFeignController {
    @Resource
    private WorkOptionService workOptionService;

    @Resource
    private SalesDemandService salesDemandService;

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseChangeService purchaseChangeService;

    @Resource
    private PurchaseApplicationService purchaseApplicationService;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("/getTableNum")
    public Integer getTableNum(@RequestBody WorkOptionDTO.TableNumDTO tableNumDTO) {
        return workOptionService.getTableNum(tableNumDTO);
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:35
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/salesDemandApprove")
    public Boolean salesDemandApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        return salesDemandService.approve(dto);
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:35
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchasePriceApprove")
    public Boolean purchasePriceApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        return purchasePriceService.approve(dto);
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchasePriceChangeApprove")
    public Boolean purchasePriceChangeApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        return purchasePriceChangeService.approve(dto);
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchaseOrderApprove")
    public Boolean purchaseOrderApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        return purchaseOrderService.approve(dto);
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchaseChangeApprove")
    public Boolean purchaseChangeApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        return purchaseChangeService.approve(dto);
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchaseApplicationApprove")
    public Boolean purchaseApplicationApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        return purchaseApplicationService.approve(dto);
    }
}
