package com.erp.server.srm.controller.feign;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.erp.server.srm.service.DeliveryOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 送货单
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@RestController
@LogSystemModule("送货单")
@RequestMapping("/feign/deliveryOrder")
public class DeliveryOrderFeginController extends BaseController {

    @Resource
    private DeliveryOrderService deliveryOrderService;

    @Resource
    private DeliveryOrderDetailService detailService;

    /**
     * 获取 tab列表
     * @return
     */
    @PostMapping("/tabList")
    public List<DeliveryOrderDTO.TabListDTO> tabList(@RequestBody DeliveryOrderDTO.ParamDTO paramDTO) {
        return deliveryOrderService.tabList(paramDTO.getSupplierIdList());
    }

    /**
     * 详情
     *
     * @param
     * @return
     */
    @GetMapping("/view")
    public DeliveryOrderDTO.ViewDTO view(@RequestParam("id") String id) {
        return deliveryOrderService.view(id);
    }

    /**
     * 分页
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     */
    @PostMapping("/paging")
    public PagingVO<DeliveryOrderDTO.ListDTO> paging(@RequestBody @Validated PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        return deliveryOrderService.paging(dto);
    }

    /**
     * 合计
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     */
    @PostMapping("/pagingTotal")
    public DeliveryOrderDTO.TotalInfo pagingTotal(@RequestBody @Validated DeliveryOrderDTO.ParamDTO dto) {
        return deliveryOrderService.pagingTotal(dto);
    }

    /**
     * 打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/print")
    public List<DeliveryOrderDTO.PrintDTO> print(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return deliveryOrderService.print(dto.getIds());
    }


    /**
     * 确认打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/confirmPrint")
    public Boolean confirmPrint(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return deliveryOrderService.confirmPrint(dto.getIds());
    }

    /**
     * 取消打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelPrint")
    public  List<BatchResultDTO> cancelPrint(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return deliveryOrderService.cancelPrint(dto.getIds());
    }

    /**
     * 获取明细来源id对应的明细
     */
    @PostMapping("/listDetailByDetailSourceIds")
    public List<DeliveryOrderDetailEntity> listDetailByDetailSourceIds(@RequestBody List<String> purchaseDetailIds){
        return detailService.listDetailByDetailSourceIds(purchaseDetailIds);
    }

    /**
     * 获取明细来源id对应的明细 和主表信息
     */
    @PostMapping("/listDetailDTOByDetailSourceIds")
    public List<DeliveryOrderDetailDTO.ListDTO> listDetailDTOByDetailSourceIds(@RequestBody List<String> purchaseDetailIds){
        return detailService.listDetailDTOByDetailSourceIds(purchaseDetailIds);
    }

    /**
     * 导出
     */
    @PostMapping("/getExportList")
    public List<DeliveryOrderExportExcelDTO> getExportList(@RequestBody DeliveryOrderDTO.ParamDTO dto){
        return deliveryOrderService.getExportList(dto);
    }

    /**
     * 下推列表
     */
    @PostMapping("/listGenerateReceive")
    public List<DeliveryOrderDTO.GenerateReceiveListDTO> listGenerateReceive(@RequestBody BaseIdsDTO.IdsDTO dto){
        return deliveryOrderService.listGenerateReceive(dto);
    }


    /**
     * 查询发货单
     */
    @PostMapping("/listByIds")
    public List<DeliveryOrderEntity> listByIds(@RequestBody List<String> ids){
        return deliveryOrderService.listByIds(ids);
    }

    /**
     * 查询发货单明细
     */
    @PostMapping("/listDetailByIds")
    public List<DeliveryOrderDetailEntity> listDetailByIds(@RequestBody List<String> detailIds){
        return detailService.listByIds(detailIds);
    }


    /**
     * 更新主记录
     */
    @PostMapping("/updateDeliveryOrder")
    public Boolean updateDeliveryOrder(@RequestBody DeliveryOrderEntity deliveryOrderEntity){
        return deliveryOrderService.updateById(deliveryOrderEntity);
    }

    /**
     * 更新明细
     */
    @PostMapping("/updateDeliveryDetail")
    public Boolean updateDeliveryDetail(@RequestBody List<DeliveryOrderDetailEntity> detailEntityGroupList){
        return detailService.updateDeliveryDetail(detailEntityGroupList);
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirmReceiveStatus")
    public Boolean confirmReceiveStatus(@RequestBody List<String> detailIds){
        return detailService.confirmReceiveStatus(detailIds);
    }


    /**
     * 反确认收货
     */
    @PostMapping("/unConfirmReceiveStatus")
    public Boolean unConfirmReceiveStatus(@RequestBody List<String> detailIds){
        return detailService.unConfirmReceiveStatus(detailIds);
    }

    /**
     * 取消收货
     */
    @PostMapping("/cancelReceive")
    public Boolean cancelReceive(@RequestBody List<String> detailIds){
        return detailService.cancelReceive(detailIds);
    }

    @PostMapping("/exportSupplierDeliveryOrder")
    public PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(@RequestBody PagingDTO<DeliveryOrderDTO.ParamDTO> dto){
        return deliveryOrderService.exportSupplierDeliveryOrder(dto);
    }
}
