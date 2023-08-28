package com.erp.server.wms.controller.pda;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
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
    @PostMapping("/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:warehouseLocationMoveInfo:update",
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
            menuCode = "wms:warehouseLocationMoveInfo:paging",
            tableAlias = ""
    )
    public ApiResult<List<WarehouseLocationMoveInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
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
            menuCode = "wms:warehouseLocationMoveInfo:paging",
            tableAlias = ""
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
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouseLocationMoveInfo:updateAndSubmit",
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
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouseLocationMoveInfo:submit",
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
                    submit = BatchResultDTO.fail(id, "仓位移动主单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getCode(), e.getMessage());
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
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouseLocationMoveInfo:approve",
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
                    approveResult = BatchResultDTO.fail(entity.getCode(), "仓位移动主单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
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
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouseLocationMoveInfo:disApprove",
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
                    disApproveResult = BatchResultDTO.fail(entity.getCode(), "仓位移动主单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
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
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouseLocationMoveInfo:delete",
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
                    deleteResult = BatchResultDTO.fail(entity.getCode(), "仓位移动主单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
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
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouseLocationMoveInfo:cancel",
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
                    cancelResult = BatchResultDTO.fail(entity.getCode(), "仓位移动主单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return success(resultDTOS);
    }

    /**
    * 详情
    * @author Luo_WG
    * @date:  2023-08-24
    * @param id
    * @return ApiResult<WarehouseLocationMoveInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouseLocationMoveInfo:view",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "id")
    public ApiResult<WarehouseLocationMoveInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(warehouseLocationMoveInfoService.view(id));
    }

}
