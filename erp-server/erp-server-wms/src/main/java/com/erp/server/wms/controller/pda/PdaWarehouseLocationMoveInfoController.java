package com.erp.server.wms.controller.pda;


import com.erp.server.wms.service.TransferInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.enums.LogActionEnum;
import com.erp.server.wms.service.WarehouseLocationMoveInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.WarehouseLocationMoveInfoDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import com.erp.model.wms.entity.WarehouseLocationMoveInfoEntity;

/**
 * 仓位移动主表
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
@Slf4j
@RestController
@LogSystemModule("PDA仓位移动")
@RequestMapping("/pdaWarehouseLocationMoveInfo")
public class PdaWarehouseLocationMoveInfoController extends BaseController {

    @Autowired
    private WarehouseLocationMoveInfoService warehouseLocationMoveInfoService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<String>
    */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓位移动")
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated WarehouseLocationMoveInfoDTO.AddDTO dto) {
        return success(warehouseLocationMoveInfoService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult
    */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改仓位移动")
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:pdaWarehouseLocationMoveInfo:update",
        serviceClass = WarehouseLocationMoveInfoService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseLocationMoveInfoDTO.UpdateDTO dto) {
        warehouseLocationMoveInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:paging",
            tableAlias = "wlmi"
    )
    public ApiResult<List<WarehouseLocationMoveInfoDTO.PdaTabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(warehouseLocationMoveInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @return ApiResult<PagingVO<WarehouseLocationMoveInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:paging",
            tableAlias = "wlmi"
    )
    public ApiResult<PagingVO<WarehouseLocationMoveInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<WarehouseLocationMoveInfoDTO.PagingParamDTO> dto) {
        return success(warehouseLocationMoveInfoService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交仓位移动")
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated WarehouseLocationMoveInfoDTO.AddDTO dto) {
        warehouseLocationMoveInfoService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交仓位移动")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:updateAndSubmit",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated WarehouseLocationMoveInfoDTO.UpdateDTO dto) {
        warehouseLocationMoveInfoService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交仓位移动")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:submit",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = warehouseLocationMoveInfoService.submit(id);
            }catch (Exception e){
                log.error("仓位移动主单 提交审核失败",e);
                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return success(resultDTOS);
    }

    /**
    * 审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核仓位移动")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:approve",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = warehouseLocationMoveInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("仓位移动主单审核失败",e);
                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return success(resultDTOS);
    }

    /**
    * 反审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核仓位移动")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:disApprove",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = warehouseLocationMoveInfoService.disApprove(id);
            }catch (Exception e){
                log.error("仓位移动主单反审核失败",e);
                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return success(resultDTOS);
    }


    /**
    * 删除
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除仓位移动")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:delete",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = warehouseLocationMoveInfoService.delete(id);
            }catch (Exception e){
                log.error("仓位移动主单删除失败",e);
                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return success(resultDTOS);
    }

    /**
    * 撤销
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销仓位移动")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:cancelProcess",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = warehouseLocationMoveInfoService.cancelProcess(id);
            }catch (Exception e){
                log.error("仓位移动主单撤回流程失败",e);
                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return success(resultDTOS);
    }

    /**
     * 作废
     * @author Luo_WG
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废仓位移动")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:invalid",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = warehouseLocationMoveInfoService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
    * 详情
    * @author Luo_WG
    * @date:  2023-08-24
    * @param id
    * @return ApiResult<WarehouseLocationMoveInfoDTO.ViewDTO>>
    */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:view",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "id")
    public ApiResult<WarehouseLocationMoveInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(warehouseLocationMoveInfoService.view(id));
    }

}
