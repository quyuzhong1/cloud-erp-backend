package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.validator.ValidList;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentExtendEntity;
import com.erp.model.wms.enums.ShipmentSourceTypeEnum;
import com.erp.server.wms.query.AwdShipmentQueryHandler;
import com.erp.server.wms.query.FbaShipmentSyncQueryHandler;
import com.erp.server.wms.service.FbaShipmentExtendService;
import com.erp.server.wms.service.FbaShipmentPackingService;
import com.erp.server.wms.service.FbaShipmentService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AWD货件表
 *
 * @author zdy
 * @since 2025-12-24
 */
@Slf4j
@RestController
@LogSystemModule("AWD货件表")
@RequestMapping("/awdShipment")
public class AwdShipmentController extends BaseController {

    @Resource
    private FbaShipmentService fbaShipmentService;

    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;
    @Resource
    private FbaShipmentExtendService fbaShipmentExtendService;

    /**
     * 列表查询
     * @param dto
     * @return ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "fs.shop_id",
            menuCode = "wms:fbaShipment:paging"
    )
    @WebAdvanceQuery(handler = AwdShipmentQueryHandler.class)
    public ApiResult<PagingVO<FbaShipmentDTO.AwdListDTO>> paging(@RequestBody @Validated PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        dto.getParams().setSourceType(ShipmentSourceTypeEnum.AWD.getCode());
        PagingVO<FbaShipmentDTO.AwdListDTO> list = fbaShipmentService.awdPaging(dto);
        return success(list);
    }
    /**
     * 单号搜索
     **/
    @PostMapping("/searchByCode")
    public ApiResult<PagingVO<FbaShipmentDTO.SearchResultDTO>> searchByCode(@RequestBody @Validated PagingDTO<FbaShipmentDTO.SearchDTO> dto) {
        dto.getParams().setSourceType(ShipmentSourceTypeEnum.AWD.getCode());
        PagingVO<FbaShipmentDTO.SearchResultDTO> pagingVO = fbaShipmentService.search(dto);
        return success(pagingVO);
    }

    /**
     * 导出
     * @param dto
     * @return ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>>
     */
    @PostMapping("/export")
    public ApiResult export(@RequestBody @Validated FbaShipmentDTO.PagingParamDTO dto) {
        dto.setSourceType(ShipmentSourceTypeEnum.AWD.getCode());
        fbaShipmentService.awdExport(dto);
        return success();
    }

    /**
     * 装箱清单导出
     * @param dto
     * @return ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>>
     */
    @PostMapping("/packingExport")
    public ApiResult packingExport(@RequestBody @Validated FbaShipmentDTO.PagingParamDTO dto) {
        dto.setSourceType(ShipmentSourceTypeEnum.AWD.getCode());
        fbaShipmentPackingService.packingExport(dto);
        return success();
    }

    /**
     * sku映射
     * @Author Luo_WG
     * @Date 2023/11/3 10:05
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
     * @date:  2023-10-30
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
                log.error("AWD货件单更新sku映射失败",e);
                FbaShipmentEntity entity = fbaShipmentService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "AWD货件单不存在, 更新sku映射失败");
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
     * 查询发货记录
     * @Author Luo_WG
     * @Date 2023/10/30 17:38
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.DeliverRecordDTO>>
     **/
    @GetMapping("/listDeliverRecord")
    public ApiResult<List<FirstMileDeliveryDTO.DeliverRecordView>> listDeliverRecord(@RequestParam(value = "id") String id) {
        List<FirstMileDeliveryDTO.DeliverRecordView> result = fbaShipmentService.listDeliverRecord(id);
        return success(result);
    }

    /**
     * 查询货件状态记录
     * @Author Luo_WG
     * @Date 2023/10/30 17:40
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<FbaShipmentDTO.ShipmentStatusRecordDTO>>
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
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.ViewDTO>>
     **/
    @GetMapping("/view")
    public ApiResult<FbaShipmentDTO.ViewAwdDTO> view(@RequestParam("id") String id) {
        FbaShipmentDTO.ViewAwdDTO result = fbaShipmentService.awdView(id);
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
     * @Date 2023/10/31 14:35
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推发货单保存：id = {id}")
    public ApiResult generateDeliverSave(@RequestBody @Validated List<FbaShipmentDTO.GenerateDeliverView> list) {
        Boolean flag = fbaShipmentService.generateDeliverSave(list,ShipmentSourceTypeEnum.AWD.getCode());
        return flag ? success() : failure();
    }

    /**
     * 单个下推发货单获取详情
     * @Author Luo_WG
     * @Date 2023/11/8 9:17
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.FbaDeliveryDTO.ViewDTO>
     **/
    @GetMapping("/getDeliverView")
    public ApiResult<FirstMileDeliveryDTO.ViewDTO> getDeliverView(@RequestParam("id") String id) {
        FirstMileDeliveryDTO.ViewDTO view = fbaShipmentService.getDeliverView(id);
        return success(view);
    }

    /**
     * 下推发货单保存并提交
     * @Author Luo_WG
     * @Date 2023/10/31 14:35
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverSaveAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "下推发货单保存并提交：id = {id}")
    public ApiResult generateDeliverSaveAndSubmit(@RequestBody @Validated List<FbaShipmentDTO.GenerateDeliverView> list) {
        Boolean flag = fbaShipmentService.generateDeliverSaveAndSubmit(list,ShipmentSourceTypeEnum.AWD.getCode());
        return flag ? success() : failure();
    }


    /**
     * 删除
     * @author Luo_WG
     * @date:  2023-10-30
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaShipment:delete",
            serviceClass = FbaShipmentService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "AWD货件单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = fbaShipmentService.delete(id);
            }catch (Exception e){
                log.error("FBA货件单删除失败",e);
                FbaShipmentEntity entity = fbaShipmentService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "AWD货件单不存在, 删除失败");
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
     * @Date 2023/11/17 11:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateRequisitionApplicationViewDTO>>
     **/
    @PostMapping("/generateRequisitionApplicationView")
    public ApiResult<List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO>> generateRequisitionApplicationView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> result = fbaShipmentService.generateRequisitionApplicationView(dto.getIds());
        return success(result);
    }

    /**
     * FBA货件下推要货申请保存
     * @Author Luo_WG
     * @Date 2023/11/17 11:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateRequisitionApplicationSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "AWD货件下推要货申请保存")
    public ApiResult generateRequisitionApplicationSave(@RequestBody @Validated ValidList<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> dto) {
        Boolean flag = fbaShipmentService.generateRequisitionApplicationSave(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * FBA货件下推要货申请保存并提交
     * @Author Luo_WG
     * @Date 2023/11/17 11:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateRequisitionApplicationSaveAndSubmit")
    @LogAction(value = LogActionEnum.INSERT, desc = "AWD货件下推要货申请保存并提交")
    public ApiResult generateRequisitionApplicationSaveAndSubmit(@RequestBody @Validated ValidList<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> dto) {
        Boolean flag = fbaShipmentService.generateRequisitionApplicationSaveAndSubmit(dto.getList());
        return flag ? success() : failure();
    }



    /**
     * 重新生成调拨单
     * @author Jim
     * @date:  2024-01-15
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/regenerateTransferOut")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaShipment:delete",
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
                log.error("AWD货件单重新生成调拨单失败",e);
                FbaShipmentEntity entity = fbaShipmentService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "AWD货件单不存在, 重新生成调拨单失败");
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
        dto.getParams().setSourceType(ShipmentSourceTypeEnum.AWD.getCode());
        PagingVO<FbaShipmentDTO.SearchResultDTO> pagingVO = fbaShipmentService.searchByCodeWithRequisition(dto);
        return success(pagingVO);
    }

    /**
     *  货件快粘贴
     **/
    @PostMapping("/requisitionFbaQuickPaste")
    public ApiResult<List<FbaShipmentDTO.SearchResultDTO>> requisitionFbaQuickPaste(@RequestBody @Validated FbaShipmentDTO.QuickPasteDTO dto) {
        return success(fbaShipmentService.requisitionFbaQuickPaste(dto));
    }

    /**
     * FBA货件同步信息分页
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
     * @date: 2025/05/09
     */
    @PostMapping("/viewList")
    public ApiResult<List<FbaShipmentDTO.AwdListDTO>> view(@RequestBody @Validated FbaShipmentDTO.ViewListReqDTO dto) {
        dto.setSourceType(ShipmentSourceTypeEnum.AWD.getCode());
        List<FbaShipmentDTO.AwdListDTO> resultList = fbaShipmentService.viewAwdList(dto);
        return success(resultList);
    }
    /**
     * 手动签收
     *
     * @return ApiResult
     * @author zdy
     * @date: 2025-12-29
     */
    @PostMapping("/manualReceived")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "手动签收:{detailId}")
    public ApiResult<List<BatchResultDTO>> manualReceived(@RequestBody @Validated List<FbaShipmentDTO.ReceivedDTO> dtoList) {
        List<String> ids = dtoList.stream().map(FbaShipmentDTO.ReceivedDTO::getId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<FbaShipmentEntity> entityList = fbaShipmentService.listByIds(ids);
        List<BatchResultDTO> resultDTOS = new ArrayList<>(entityList.size());
        for (String id : ids) {
            FbaShipmentEntity entity = entityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "货件单不存在, 手动签收失败"));
                continue;
            }
            List<FbaShipmentDTO.ReceivedDTO> dtoList1 = dtoList.stream().filter(e -> e.getId().equals(id)).collect(Collectors.toList());
            BatchResultDTO resultDTO = null;
            try {
                resultDTO = fbaShipmentService.manualAwdReceived(dtoList1,entity);
                resultDTOS.add(resultDTO);
            }catch (Exception e){
                log.error("AWD货件单手动签收失败",e);
                resultDTOS.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }

        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 打印标签
     *
     * @return ApiResult
     * @author zdy
     * @date: 2025-12-24
     */
    @PostMapping("/printLabel")
    public ApiResult<WmsAttachmentDTO.UpdateDTO> printLabel(@RequestBody @Validated FbaShipmentDTO.PrintLabelDTO dto) {
        WmsAttachmentDTO.UpdateDTO updateDTO = fbaShipmentService.printLabel(dto);
        return success(updateDTO);
    }
}
