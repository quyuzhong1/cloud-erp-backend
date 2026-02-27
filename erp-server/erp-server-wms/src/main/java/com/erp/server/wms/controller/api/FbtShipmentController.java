package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.enums.ShipmentSourceTypeEnum;
import com.erp.server.wms.query.FbaShipmentSyncQueryHandler;
import com.erp.server.wms.service.FbaShipmentPackingService;
import com.erp.server.wms.service.FbaShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FBT货件表
 *
 * @author Luo_WG
 * @since 2026-02-06
 */
@Slf4j
@RestController
@LogSystemModule("FBT货件表")
@RequestMapping("/fbtShipment")
public class FbtShipmentController extends BaseController {

    @Resource
    private FbaShipmentService fbaShipmentService;

    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;

    /**
     * 分页查询
     * @param dto
     * @return ApiResult<PagingVO<FbaShipmentDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "fs.shop_id",
            menuCode = "wms:fbtShipment:paging"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<FbaShipmentDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        dto.getParams().setSourceType(ShipmentSourceTypeEnum.FBT.getCode());
        PagingVO<FbaShipmentDTO.ListDTO> list = fbaShipmentService.paging(dto);
        return success(list);
    }

    /**
     * 单号搜索
     **/
    @PostMapping("/searchByCode")
    public ApiResult<PagingVO<FbaShipmentDTO.SearchResultDTO>> searchByCode(@RequestBody @Validated PagingDTO<FbaShipmentDTO.SearchDTO> dto) {
        dto.getParams().setSourceType(ShipmentSourceTypeEnum.FBT.getCode());
        PagingVO<FbaShipmentDTO.SearchResultDTO> pagingVO = fbaShipmentService.search(dto);
        return success(pagingVO);
    }

    /**
     * 导出
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/export")
    public ApiResult export(@RequestBody @Validated FbaShipmentDTO.PagingParamDTO dto) {
        dto.setSourceType(ShipmentSourceTypeEnum.FBT.getCode());
        fbaShipmentService.export(dto);
        return success();
    }

    /**
     * 装箱清单导出
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/packingExport")
    public ApiResult packingExport(@RequestBody @Validated FbaShipmentDTO.PagingParamDTO dto) {
        dto.setSourceType(ShipmentSourceTypeEnum.FBT.getCode());
        fbaShipmentPackingService.packingExport(dto);
        return success();
    }

    /**
     * sku映射
     * @Author Luo_WG
     * @Date 2026/02/06 10:05
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/skuMapping")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "sku映射：detailId={detailId}， 平台sku={msku} 映射 erpSku={skuNo}")
    public ApiResult skuMapping(@RequestBody @Validated FbaShipmentDTO.SkuMappingParamDTO dto) {
        Boolean flag = fbaShipmentService.skuMapping(dto);
        return flag ? success() : failure();
    }


    /**
     * 批量更新sku映射
     * @author Luo_WG
     * @date:  2026-02-06
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/skuMappingBatch")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量更新sku映射：ids={ids}")
    public ApiResult<List<BatchResultDTO>> skuMappingBatch(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = fbaShipmentService.skuMappingBatch(id);
            } catch (Exception e) {
                log.error("FBT货件单更新sku映射失败",e);
                FbaShipmentEntity entity = fbaShipmentService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "FBT货件单不存在, 更新sku映射失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 拉取货件信息
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     */
    @PostMapping("/pullShipment")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取货件")
    public ApiResult pullShipment(@RequestBody @Validated FbaShipmentDTO.PullShipmentDTO dto) {
        Boolean flag;
        try {
            UserContext.setIsUserSystem(true);
            flag = fbaShipmentService.pullShipment(dto);
        }finally {
            UserContext.clearIsUserSystem();
        }
        return flag ? success() : failure();
    }

    /**
     * 查询发货记录
     * @Author Luo_WG
     * @Date 2026/02/06 17:38
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FirstMileDeliveryDTO.DeliverRecordView>>
     **/
    @GetMapping("/listDeliverRecord")
    public ApiResult<List<FirstMileDeliveryDTO.DeliverRecordView>> listDeliverRecord(@RequestParam(value = "id") String id) {
        List<FirstMileDeliveryDTO.DeliverRecordView> result = fbaShipmentService.listDeliverRecord(id);
        return success(result);
    }

    /**
     * 查询货件状态记录
     * @Author Luo_WG
     * @Date 2026/02/06 17:40
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<FbaShipmentDTO.ShipmentStatusRecordView>>
     **/
    @GetMapping("/listShipmentStatusRecord")
    public ApiResult<List<FbaShipmentDTO.ShipmentStatusRecordView>> listShipmentStatusRecord(@RequestParam(value = "id") String id) {
        List<FbaShipmentDTO.ShipmentStatusRecordView> result = fbaShipmentService.listShipmentStatusRecord(id);
        return success(result);
    }

    /**
     * 查询收货记录
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.ReceiveRecordView>>
     **/
    @GetMapping("/listReceiveRecord")
    public ApiResult<List<FbaShipmentDTO.ReceiveRecordView>> listReceiveRecord(@RequestParam(value = "id") String id) {
        List<FbaShipmentDTO.ReceiveRecordView> result = fbaShipmentService.listReceiveRecord(id);
        return success(result);
    }

    /**
     * 详情
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.FbaShipmentDTO.ViewDTO>
     **/
    @GetMapping("/view")
    public ApiResult<FbaShipmentDTO.ViewDTO> view(@RequestParam("id") String id) {
        FbaShipmentDTO.ViewDTO result = fbaShipmentService.view(id);
        return success(result);
    }

    /**
     * 完结货件
     * @param ids
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/finishShipment")
    @LogAction(value = LogActionEnum.UPDATE, desc = "完结货件：ids = {ids}")
    public ApiResult finishShipment(@RequestBody BaseIdsDTO.IdsDTO ids) {
        Boolean flag = fbaShipmentService.finishShipment(ids.getIds());
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
     * @Date 2026/02/06 14:35
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推发货单保存：id = {id}")
    public ApiResult generateDeliverSave(@RequestBody @Validated List<FbaShipmentDTO.GenerateDeliverView> list) {
        Boolean flag = fbaShipmentService.generateDeliverSave(list, ShipmentSourceTypeEnum.FBT.getCode());
        return flag ? success() : failure();
    }

    /**
     * 单个下推发货单获取详情
     * @Author Luo_WG
     * @Date 2026/02/06 9:17
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.FirstMileDeliveryDTO.ViewDTO>
     **/
    @GetMapping("/getDeliverView")
    public ApiResult<FirstMileDeliveryDTO.ViewDTO> getDeliverView(@RequestParam("id") String id) {
        FirstMileDeliveryDTO.ViewDTO view = fbaShipmentService.getDeliverView(id);
        return success(view);
    }

    /**
     * 下推发货单保存并提交
     * @Author Luo_WG
     * @Date 2026/02/06 14:35
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverSaveAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "下推发货单保存并提交：id = {id}")
    public ApiResult generateDeliverSaveAndSubmit(@RequestBody @Validated List<FbaShipmentDTO.GenerateDeliverView> list) {
        Boolean flag = fbaShipmentService.generateDeliverSaveAndSubmit(list, ShipmentSourceTypeEnum.FBT.getCode());
        return flag ? success() : failure();
    }


    /**
     * 删除
     * @author Luo_WG
     * @date:  2026-02-06
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbtShipment:delete",
            serviceClass = FbaShipmentService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "FBT货件单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = fbaShipmentService.delete(id);
            }catch (Exception e){
                log.error("FBT货件单删除失败",e);
                FbaShipmentEntity entity = fbaShipmentService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "FBT货件单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下推要货申请列表查询
     * @Author Luo_WG
     * @Date 2026/02/06 11:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateRequisitionApplicationViewDTO>>
     **/
    @PostMapping("/generateRequisitionApplicationView")
    public ApiResult<List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO>> generateRequisitionApplicationView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> result = fbaShipmentService.generateRequisitionApplicationView(dto.getIds());
        return success(result);
    }

    /**
     * FBT货件下推要货申请保存
     * @Author Luo_WG
     * @Date 2026/02/06 11:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateRequisitionApplicationSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "FBT货件下推要货申请保存")
    public ApiResult generateRequisitionApplicationSave(@RequestBody @Validated ValidList<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> dto) {
        Boolean flag = fbaShipmentService.generateRequisitionApplicationSave(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * FBT货件下推要货申请保存并提交
     * @Author Luo_WG
     * @Date 2026/02/06 11:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateRequisitionApplicationSaveAndSubmit")
    @LogAction(value = LogActionEnum.INSERT, desc = "FBT货件下推要货申请保存并提交")
    public ApiResult generateRequisitionApplicationSaveAndSubmit(@RequestBody @Validated ValidList<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> dto) {
        Boolean flag = fbaShipmentService.generateRequisitionApplicationSaveAndSubmit(dto.getList());
        return flag ? success() : failure();
    }



    /**
     * 重新生成调拨单
     * @author Jim
     * @date:  2026-02-06
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/regenerateTransferOut")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbtShipment:delete",
            serviceClass = FbaShipmentService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "重新生成调拨单:ids={ids}")
    public ApiResult<List<BatchResultDTO>> regenerateTransferOut(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = fbaShipmentService.regenerateTransferOut(id);
            }catch (Exception e){
                log.error("FBT货件单重新生成调拨单失败",e);
                FbaShipmentEntity entity = fbaShipmentService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "FBT货件单不存在, 重新生成调拨单失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查询装箱清单
     */
    @PostMapping("/getPacking")
    public ApiResult<List<FbaShipmentPackingDTO.ViewDTO>> getPacking(@RequestBody @Validated BaseIdDTO dto) {
        return success(fbaShipmentPackingService.listPacking(Collections.singletonList(dto.getId())));
    }

    /**
     * 单号搜索(按要货申请)
     **/
    @PostMapping("/searchByCodeWithRequisition")
    public ApiResult<PagingVO<FbaShipmentDTO.SearchResultDTO>> searchByCodeWithRequisition(@RequestBody @Validated PagingDTO<FbaShipmentDTO.SearchDTO> dto) {
        dto.getParams().setSourceType(ShipmentSourceTypeEnum.FBT.getCode());
        PagingVO<FbaShipmentDTO.SearchResultDTO> pagingVO = fbaShipmentService.searchByCodeWithRequisition(dto);
        return success(pagingVO);
    }

    /**
     *  货件快粘贴
     **/
    @PostMapping("/requisitionFbtQuickPaste")
    public ApiResult<List<FbaShipmentDTO.SearchResultDTO>> requisitionFbtQuickPaste(@RequestBody @Validated FbaShipmentDTO.QuickPasteDTO dto) {
        return success(fbaShipmentService.requisitionFbaQuickPaste(dto));
    }

    /**
     * FBT货件同步信息分页
     */
    @PostMapping("/syncPaging")
    @WebAdvanceQuery(handler = FbaShipmentSyncQueryHandler.class)
    public ApiResult<PagingVO<FbaShipmentDTO.SyncViewDTO>> syncWarehouseProductView(@RequestBody PagingDTO<AdvanceQueryContainer> advanceQueryDTO){
        return success(fbaShipmentService.syncPaging(advanceQueryDTO));
    }

    /**
     * 查询详情列表
     *
     * @return ApiResult<List < FbaShipmentDTO.ViewDTO>>
     * @author zdy
     * @date: 2026/02/06
     */
    @PostMapping("/viewList")
    public ApiResult<List<FbaShipmentDTO.ListDTO>> viewList(@RequestBody @Validated FbaShipmentDTO.ViewListReqDTO dto) {
        dto.setSourceType(ShipmentSourceTypeEnum.FBT.getCode());
        List<FbaShipmentDTO.ListDTO> resultList = fbaShipmentService.viewList(dto);
        return success(resultList);
    }
    /**
     * 调整签收
     *
     * @return ApiResult
     * @author Jim
     * @date: 2026-02-06
     */
    @PostMapping("/changeReceived")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "调整签收:{detailId}")
    public ApiResult changeReceived(@RequestBody @Validated List<FbaShipmentDTO.ReceivedDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = fbaShipmentService.changeReceived(dtoList);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 打印标签
     *
     * @return ApiResult
     * @author zdy
     * @date: 2026-02-06
     */
    @PostMapping("/printLabel")
    public ApiResult<WmsAttachmentDTO.UpdateDTO> printLabel(@RequestBody @Validated FbaShipmentDTO.PrintLabelDTO dto) {
        WmsAttachmentDTO.UpdateDTO updateDTO = fbaShipmentService.printLabel(dto);
        return success(updateDTO);
    }
}
