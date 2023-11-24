package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BaseSelectDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.server.wms.service.OverseasTransferWarehouseService;
import com.erp.server.wms.service.OverseasWarehouseInboundDetailService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
     * @return ApiResult<String>
     * @author Jim
     * @date: 2023-11-16
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "海外仓入库单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated OverseasWarehouseInboundDTO.AddDTO dto) {
        return success(overseasWarehouseInboundService.add(dto));
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
    @PostMapping("/receivedList")
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
    @PostMapping("/transferWareHouseList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInbound:paging",
            tableAlias = "owi"
    )
    public ApiResult<List<BaseSelectDTO>> transferWareHouseList() {
        List<BaseSelectDTO> result = overseasTransferWarehouseService.baseSelectlist();
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
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        for (OverseasWarehouseInboundDTO.ReceivedDTO dto : dtoList) {
            BatchResultDTO submit;
            try {
                submit = overseasWarehouseInboundDetailService.manualReceived(dto);
            } catch (Exception e) {
                log.error("海外仓入库单手动签收失败:{}", e.getMessage());
                OverseasWarehouseInboundDetailEntity entity = overseasWarehouseInboundDetailService.getById(dto.getDetailId());
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(dto.getDetailId(), dto.getDetailId(), "海外仓入库单详情不存在, 提交失败");
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
}
