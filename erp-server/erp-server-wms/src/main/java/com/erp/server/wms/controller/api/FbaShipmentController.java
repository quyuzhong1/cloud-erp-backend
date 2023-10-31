package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.FbaShipmentService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.FbaShipmentDTO;

import java.util.List;

/**
 * FBA货件表
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@RestController
@LogSystemModule("FBA货件表")
@RequestMapping("/fbaShipment")
public class FbaShipmentController extends BaseController {

    @Autowired
    private FbaShipmentService fbaShipmentService;

    /**
     * 列表查询
     * @param dto
     * @return ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<FbaShipmentDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        PagingVO<FbaShipmentDTO.ListDTO> list = fbaShipmentService.paging(dto);
        return success(list);
    }

    /**
     * 拉取货件信息
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     */
    @PostMapping("/pullShipment")
//    @LogAction(value = LogActionEnum.INSERT, desc = "拉取货件")
    public ApiResult pullShipment(@RequestBody @Validated FbaShipmentDTO.pullShipmentDTO dto) {
        Boolean flag = fbaShipmentService.pullShipment(dto);
        return flag ? success() : failure();
    }

    /**
     * 查询发货记录
     * @Author Luo_WG
     * @Date 2023/10/30 17:38
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.DeliverRecordDTO>>
     **/
    @GetMapping("/listDeliverRecord")
    public ApiResult<List<FbaShipmentDTO.DeliverRecordView>> listDeliverRecord(@RequestParam(value = "id") String id) {
        List<FbaShipmentDTO.DeliverRecordView> result = fbaShipmentService.listDeliverRecord(id);
        return success(result);
    }

    /**
     * 查询货件状态记录
     * @Author Luo_WG
     * @Date 2023/10/30 17:40
     * @param code
     * @return com.common.core.controller.vo.ApiResult<java.util.List<FbaShipmentDTO.ShipmentStatusRecordDTO>>
     **/
    @GetMapping("/listShipmentStatusRecord")
    public ApiResult<List<FbaShipmentDTO.ShipmentStatusRecordView>> listShipmentStatusRecord(@RequestParam(value = "code") String code) {
        List<FbaShipmentDTO.ShipmentStatusRecordView> result = fbaShipmentService.listShipmentStatusRecord(code);
        return success(result);
    }

    /**
     * 查询收货记录
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.ReceiveRecordView>>
     **/
    @GetMapping("/listReceiveRecord")
    public ApiResult<List<FbaShipmentDTO.ReceiveRecordView>> listReceiveRecord(@RequestBody @Validated PagingDTO<FbaShipmentDTO.ReceiveRecordParam> dto) {
        List<FbaShipmentDTO.ReceiveRecordView> result = fbaShipmentService.listReceiveRecord(dto);
        return success(result);
    }

    /**
     * 详情
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.ViewDTO>>
     **/
    @GetMapping("/view")
    public ApiResult<List<FbaShipmentDTO.ViewDTO>> view(@RequestParam("id") String id) {
        List<FbaShipmentDTO.ViewDTO> result = fbaShipmentService.view(id);
        return success(result);
    }

    /**
     * 完结货件
     * @param ids
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/finishShipment")
    public ApiResult finishShipment(@RequestBody BaseIdsDTO.IdsDTO ids) {
        Boolean flag = fbaShipmentService.finishShipment(ids);
        return flag ? success() : failure();
    }

    /**
     * 下推发货单列表查询
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateDeliverView>>
     **/
    @PostMapping("/generateDeliverView")
    public ApiResult<List<FbaShipmentDTO.GenerateDeliverView>> generateDeliverView(@RequestBody BaseIdsDTO.IdsDTO ids) {
        List<FbaShipmentDTO.GenerateDeliverView> result = fbaShipmentService.generateDeliverView(ids);
        return success(result);
    }

    /**
     * 下推发货单保存
     * @Author Luo_WG
     * @Date 2023/10/31 14:35
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverSave")
    public ApiResult generateDeliverSave(@RequestBody @Validated List<FbaShipmentDTO.GenerateDeliverView> list) {
        Boolean flag = fbaShipmentService.generateDeliverSave(list);
        return flag ? success() : failure();
    }


}
