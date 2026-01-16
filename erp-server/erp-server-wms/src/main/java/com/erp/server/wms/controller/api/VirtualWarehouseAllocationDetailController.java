package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import com.erp.server.wms.service.VirtualWarehouseAllocationService;
import com.erp.server.wms.service.VirtualWarehousePushHandleRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 虚拟仓分货单明细
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@RestController
@LogSystemModule("分货单明细")
@RequestMapping("/virtualWarehouseAllocationDetail")
public class VirtualWarehouseAllocationDetailController extends BaseController {

    @Resource
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Autowired
    private VirtualWarehousePushHandleRelationService virtualWarehousePushHandleRelationService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-06-05
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "分货单明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehouseAllocationDetailDTO.AddDTO dto) {
        return success(virtualWarehouseAllocationDetailService.batchAdd(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date: 2024-06-05
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "分货单明细修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocationDetail:update",
            serviceClass = VirtualWarehouseAllocationDetailService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualWarehouseAllocationDetailDTO.UpdateDTO dto) {
        virtualWarehouseAllocationDetailService.batchUpdate(dto);
        return success();
    }

    /**
     * 手动完结
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "手动完结分货单信息")
    @PostMapping("/manualFinish")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocationDetail:manualFinish",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<BatchResultDTO> manualFinish(@RequestBody @Validated VirtualWarehouseAllocationDTO.ManualFinishDto dto) {
        String id = dto.getDetailId();
        //已处理状态且同步失败状态
        String handleStatus = VirtualWarehouseAllocationStatusEnum.HANDLE.getCode();
        BatchResultDTO submit;
        String flagCode = id;
        try {
            VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity = virtualWarehouseAllocationDetailService.getById(id);
            if (Objects.isNull(vmAllocationDetailEntity)) {
                submit = BatchResultDTO.fail(id, id, "分货单明细不存在");
            } else {
                VirtualWarehouseAllocationEntity vmAllocationEntity = virtualWarehouseAllocationService.getById(vmAllocationDetailEntity.getMainId());

                if (Objects.isNull(vmAllocationEntity)) {
                    submit = BatchResultDTO.fail(id, id, "分货单不存在");
                } else {
                    //只有已处理状态且同步失败状态可以手动完结
                    if (!Objects.equals(handleStatus, vmAllocationEntity.getStatus())) {
                        submit = BatchResultDTO.fail(id, vmAllocationEntity.getCode(), ApiError.VM_MANUAL_STATUS_ERROR.getMsg());
                    } else {
                        flagCode = vmAllocationEntity.getCode();
                        submit = virtualWarehouseAllocationDetailService.manualFinish(vmAllocationDetailEntity, vmAllocationEntity, dto);
                    }
                }
            }
        } catch (Exception e) {
            log.error("手动完结分货单失败>>>>{}", e);
            submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
        }
        return submit.getSuccess() ? success(submit) : failure(submit);
    }

    /**
     * 同步
     *
     * @param detailId
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "同步分货单信息")
    @GetMapping("/sync")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocationDetail:sync",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> sync(@RequestParam(value = "detailId") String detailId) {
        String id = detailId;
        //已处理状态且同步失败状态
        String handleStatus = VirtualWarehouseAllocationStatusEnum.HANDLE.getCode();
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        BatchResultDTO submit;
        String flagCode = id;
        try {
            VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity = virtualWarehouseAllocationDetailService.getById(id);
            if (Objects.isNull(vmAllocationDetailEntity)) {
                submit = BatchResultDTO.fail(id, id, "分货单明细不存在");
            } else {
                VirtualWarehouseAllocationEntity vmAllocationEntity = virtualWarehouseAllocationService.getById(vmAllocationDetailEntity.getMainId());

                if (Objects.isNull(vmAllocationEntity)) {
                    submit = BatchResultDTO.fail(id, id, "分货单不存在");
                } else {
                    //只有已处理状态且同步失败状态可以同步
                    if (!Objects.equals(handleStatus, vmAllocationEntity.getStatus())) {
                        submit = BatchResultDTO.fail(id, vmAllocationEntity.getCode(), ApiError.VM_SYNC_ERROR_STATUS_ONLY.getMsg());
                    } else {
                        submit = virtualWarehouseAllocationDetailService.sync(vmAllocationDetailEntity, vmAllocationEntity);
                    }
                }
            }
        } catch (Exception e) {
            log.error("手动同步分货单失败>>>>{}", e);
            submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
        }
        resultDTOS.add(submit);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }

    /**
     * 展示分货单同步信息
     *
     * @param detailId
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "展示分货单同步信息")
    @GetMapping("/viewSyncInfo")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocationDetail:viewSyncInfo",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<DmpPushTaskDTO.SyncInfoDTO>> viewSyncInfo(@RequestParam(value = "detailId") String detailId) {
       return success(virtualWarehouseAllocationDetailService.viewSyncInfo(detailId));
    }

    /**
     * 展示信息
     *
     * @param detailId
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "展示分货单同步信息")
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocationDetail:view",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<VirtualWarehouseAllocationDTO.ThirdCodeDto>> view(@RequestParam(value = "detailId") String detailId) {
       return success(virtualWarehouseAllocationDetailService.view(detailId));
    }
    /**
     * 展示完结信息
     *
     * @param detailId
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "展示分货单同步信息")
    @GetMapping("/viewManualFinish")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocationDetail:viewManualFinish",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<VirtualWarehouseAllocationDTO.ManualFinishViewDTO> viewManualFinish(@RequestParam(value = "detailId") String detailId) {
       return success(virtualWarehouseAllocationDetailService.viewManualFinish(detailId));
    }

    /**
     * 更新备注
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新备注 id={id}, remark={remark}")
    public ApiResult<Boolean> updateRemark(@RequestBody @Validated VirtualWarehouseAllocationDTO.UpdateRemarkDTO updateRemarkDTO){
        return success(virtualWarehouseAllocationDetailService.updateRemark(updateRemarkDTO));
    }

}
