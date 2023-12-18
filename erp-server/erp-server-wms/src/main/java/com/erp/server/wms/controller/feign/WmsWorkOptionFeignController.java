package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.core.utils.BeanMapper;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购订单feign
 *
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@RestController
@RequestMapping("feign/wmsWorkOption")
public class WmsWorkOptionFeignController {
    @Resource
    private WorkOptionService workOptionService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @Resource
    private PoInstockService poInstockService;

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    @Resource
    private TransferApplicationService transferApplicationService;

    @Resource
    private StocktakingTaskService stocktakingTaskService;

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Resource
    private OverseasDeliveryPlanService overseasDeliveryPlanService;

    /**
     * 根据入参查询单据数量
     *
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("/getTableNum")
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        return workOptionService.getTableNum(myWorkOptionDTOList);
    }

    /**
     * 采购收货审核
     *
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @PostMapping("/warehouseReceiveApprove")
    public Boolean warehouseReceiveApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = warehouseReceiveService.approve(baseApproveParamDTO);
        return flag;
    }

    /**
     * 采购入库审核
     *
     * @param baseApproveParamDTO
     * @return ApiResult
     * @author Will
     * @date: 2023/4/11 20:11
     */
    @PostMapping("/poInstockApprove")
    public void poInstockApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        poInstockService.approve(baseApproveParamDTO);
    }

    /**
     * 采购退货审核
     *
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @PostMapping("/purchaseReturnOrderApprove")
    public Boolean purchaseReturnOrderApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = purchaseReturnOrderService.approve(baseApproveParamDTO);
        return flag;
    }

    /**
     * 调拨申请单审核
     *
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/8/3 16:38
     **/
    @PostMapping("/transferApplicationApprove")
    public Boolean transferApplicationApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        transferApplicationService.approve(baseApproveParamDTO);
        return Boolean.TRUE;
    }

    /**
     * 盘底任务审核
     *
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/8/3 16:38
     **/
    @PostMapping("/stocktakingTaskApprove")
    public Boolean stocktakingTaskApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        ApproveOneDTO oneDto = new ApproveOneDTO();
        BeanMapper.copy(baseApproveParamDTO,oneDto);
        String id= CollectionUtils.isNotEmpty(baseApproveParamDTO.getIds())?baseApproveParamDTO.getIds().get(0):"";
        oneDto.setId(id);
        stocktakingTaskService.approve(id, oneDto);
        return Boolean.TRUE;
    }

    /**
     * 头程发货单
     * @Author Luo_WG
     * @Date 2023/11/15 18:01
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("/fbaDeliveryApprove")
    public Boolean fbaDeliveryApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        ApproveOneDTO oneDto = new ApproveOneDTO();
        BeanMapper.copy(baseApproveParamDTO,oneDto);
        String id= CollectionUtils.isNotEmpty(baseApproveParamDTO.getIds())?baseApproveParamDTO.getIds().get(0):"";
        oneDto.setId(id);
        firstMileDeliveryService.approve(oneDto);
        return Boolean.TRUE;
    }

    /**
     * 海外发货计划
     * @Author Luo_WG
     * @Date 2023/11/17 16:16
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("/overseasDeliveryPlanApprove")
    public Boolean overseasDeliveryPlanApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        ApproveOneDTO oneDto = new ApproveOneDTO();
        BeanMapper.copy(baseApproveParamDTO,oneDto);
        String id= CollectionUtils.isNotEmpty(baseApproveParamDTO.getIds())?baseApproveParamDTO.getIds().get(0):"";
        oneDto.setId(id);
        overseasDeliveryPlanService.approve(oneDto);
        return Boolean.TRUE;
    }
}
