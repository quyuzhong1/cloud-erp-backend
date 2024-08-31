package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.server.wms.service.OverseasTransferWarehouseService;
import com.erp.server.wms.service.OverseasWarehouseInboundDetailService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 海外仓入库单
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("海外仓入库单")
@RequestMapping("/overseasWarehouseInbound")
public class OverseasWarehouseInboundController extends BaseController {

    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Resource
    private OverseasWarehouseInboundDetailService overseasWarehouseInboundDetailService;
    @Resource
    private OverseasTransferWarehouseService overseasTransferWarehouseService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult
     * @author Jim
     * @date: 2023-11-27
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "海外仓入库单新增")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:update",
            serviceClass = OverseasWarehouseInboundService.class,
            keyIdName = "owi")
    public ApiResult<?> add(@RequestBody @Validated OverseasWarehouseInboundDTO.AddDTO dto) {
        overseasWarehouseInboundService.add(dto);
        return success();
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Jim
     * @date: 2023-11-16
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "海外仓入库单修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:update",
            serviceClass = OverseasWarehouseInboundService.class,
            keyIdName = "owi")
    public ApiResult<?> update(@RequestBody @Validated OverseasWarehouseInboundDTO.UpdateDTO dto) {
        overseasWarehouseInboundService.update(dto);
        return success();
    }

    /**
     * 查询详情
     *
     * @param id
     * @return ApiResult<OverseasWarehouseInboundDTO>
     * @author Jim
     * @date: 2023/11/27
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:view",
            serviceClass = OverseasTransferWarehouseService.class,
            keyIdName = "id")
    public ApiResult<OverseasWarehouseInboundDTO.ViewDTO> viewList(@Param("id") String id) {
        OverseasWarehouseInboundDTO.ViewDTO dto = overseasWarehouseInboundService.view(id);
        return success(dto);
    }


    /**
     * 查询详情列表
     *
     * @return ApiResult<List < OverseasWarehouseInboundDTO.ViewDTO>>
     * @author Jim
     * @date: 2023/11/27
     */
    @PostMapping("/viewList")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:view",
            serviceClass = OverseasTransferWarehouseService.class,
            keyIdName = "id")
    public ApiResult<List<OverseasWarehouseInboundDetailDTO.ViewListDTO>> view(@RequestBody @Validated OverseasWarehouseInboundDTO.ViewListReqDTO dto) {
        List<OverseasWarehouseInboundDetailDTO.ViewListDTO> resultList = overseasWarehouseInboundService.viewList(dto);
        return success(resultList);
    }

    /**
     * 列表状态数量统计
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>>
     * @Author Jim
     * @Date 2023/11/27
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:overseasWarehouseInbound:paging",
            tableAlias = "owi"
    )
    public ApiResult<List<OverseasWarehouseInboundDTO.CountDTO>> listCount(@RequestBody @Validated PermissionsDTO dto) {
        List<OverseasWarehouseInboundDTO.CountDTO> resultDTO = overseasWarehouseInboundService.listCount(dto);
        return success(resultDTO);
    }


    /**
     * 列表查询
     *
     * @return ApiResult
     * @author Jim
     * @date: 2023-11-21
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:paging",
            tableAlias = "owi"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<OverseasWarehouseInboundDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OverseasWarehouseInboundDTO.PagingParamDTO> dto) {
        PagingVO<OverseasWarehouseInboundDTO.ListDTO> result = overseasWarehouseInboundService.paging(dto);
        return success(result);
    }

    /**
     * 查询签收记录
     *
     * @author Jim
     * @date: 2023-11-24
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:paging",
            keyIdName = "owi")
    @GetMapping("/listReceiveRecord")
    public ApiResult<List<OverseasWarehouseInboundDTO.ReceiveRecordView>> listReceiveRecord(@RequestParam(value = "detailId") String detailId) {
        List<OverseasWarehouseInboundDTO.ReceiveRecordView> result = overseasWarehouseInboundDetailService.listReceiveRecord(detailId);
        return success(result);
    }

    /**
     * 中转仓列表查询
     *
     * @return ApiResult
     * @author Jim
     * @date: 2023-11-24
     */
    @GetMapping("/transferWareHouseList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:paging",
            tableAlias = "owi"
    )
    public ApiResult<List<BaseSelectDTO>> transferWareHouseList(
            @RequestParam(value = "sourceId", required = false) String sourceId
    ) {
        List<BaseSelectDTO> result = overseasTransferWarehouseService.baseSelectlist(sourceId);
        return success(result);
    }

    /**
     * 中转仓对应物流名称
     *
     * @return ApiResult
     * @author Jim
     * @date: 2023-11-24
     */
    @GetMapping("/transferWareHouse/logisticsProductList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:paging",
            tableAlias = "owi"
    )
    public ApiResult<List<BaseSelectDTO>> transferWareHouseLogisticsProductList(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "code", required = false) String code
    ) {
        List<BaseSelectDTO> result = overseasTransferWarehouseService.LogisticsProductList(id, code);
        return success(result);
    }

    /**
     * 手动签收
     *
     * @return ApiResult
     * @author Jim
     * @date: 2023-11-24
     */
    @PostMapping("/manualReceived")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "手动签收:{detailId}")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:update",
            serviceClass = OverseasWarehouseInboundService.class,
            keyIdName = "owi")
    public ApiResult<?> manualReceived(@RequestBody @Validated List<OverseasWarehouseInboundDTO.ReceivedDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = overseasWarehouseInboundDetailService.allManualReceived(dtoList);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 手动完结
     *
     * @return ApiResult
     * @author Jim
     * @date: 2023-11-24
     */
    @PostMapping("/manualFinish")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "手动完结:{id}")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:update",
            serviceClass = OverseasWarehouseInboundService.class,
            keyIdName = "owi")
    public ApiResult<?> manualFinish(@RequestBody @Validated List<OverseasWarehouseInboundDTO.FinishDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        for (OverseasWarehouseInboundDTO.FinishDTO dto : dtoList) {
            BatchResultDTO submit;
            try {
                submit = overseasWarehouseInboundService.manualFinish(dto);
            } catch (Exception e) {
                log.error("海外仓入库单手动完成失败:{}", e.getMessage());
                OverseasWarehouseInboundEntity entity = overseasWarehouseInboundService.getById(dto.getId());
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(dto.getId(), dto.getId(), "海外仓入库单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), "", e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量取消入库
     *
     * @param dto ids
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * @date: 2023-11-27
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "海外仓入库单取消")
    @PostMapping("/cancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:cancel",
            serviceClass = OverseasWarehouseInboundService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = overseasWarehouseInboundService.cancel(id);
            } catch (Exception e) {
                log.error("海外仓入库单 取消失败", e);
                OverseasWarehouseInboundEntity entity = overseasWarehouseInboundService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "海外仓入库单不存在, 取消失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量删除
     *
     * @param dto ids
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * @date: 2023-11-27
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "海外仓入库单删除")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:delete",
            serviceClass = OverseasWarehouseInboundService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = overseasWarehouseInboundService.delete(id);
            } catch (Exception e) {
                log.error("海外仓入库单删除失败", e);
                OverseasWarehouseInboundEntity entity = overseasWarehouseInboundService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "海外仓入库单不存在, 删除失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 导出数据
     *
     * @param dto ids
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * @date: 2023-11-27
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出海外入库单")
    @PostMapping("/export")
    public ApiResult<?> exportWarehouse(@RequestBody @Valid OverseasWarehouseInboundDTO.ExportDTO dto) {
        Boolean result = overseasWarehouseInboundService.exportExcel(dto);
        return result ? success() : failure();
    }

    /**
     * 获取海外仓入库单可选择的物流产品
     */
    @GetMapping("/getLogisticByTransferWarehouseId")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> getLogisticByTransferWarehouseId(@RequestParam(value = "transferWarehouseId") String transferWarehouseId) {
        List<BaseDropDownDTO.CommonDTO> baseSelectDTOS = overseasWarehouseInboundService.getLogisticByTransferWarehouseId(transferWarehouseId);
        return success(baseSelectDTOS);
    }
}
