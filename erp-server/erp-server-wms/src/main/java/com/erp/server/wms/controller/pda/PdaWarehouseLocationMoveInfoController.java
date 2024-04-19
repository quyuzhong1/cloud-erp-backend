package com.erp.server.wms.controller.pda;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.server.wms.query.MarehouseMoveInfoQueryHandler;
import com.erp.server.wms.service.TransferInfoService;
import com.erp.server.wms.service.WarehouseLocationMoveDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import com.erp.model.wms.entity.WarehouseLocationMoveInfoEntity;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private WarehouseLocationMoveDetailService warehouseLocationMoveDetailService;

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
    * 新增
     * @author hyj
     * @date 2024/4/15 17:06
    * @param dto
    * @return ApiResult<String>
    */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓位移动")
    @PostMapping("/pc/add")
    public ApiResult<String> pcAdd(@RequestBody @Validated WarehouseLocationMoveInfoDTO.PcAddDTO dto) {
        return success(warehouseLocationMoveInfoService.pcAdd(dto));
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
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date 2024/4/19 11:48
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改仓位移动")
    @PostMapping("/pc/update")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:pdaWarehouseLocationMoveInfo:update",
        serviceClass = WarehouseLocationMoveInfoService.class,
        keyIdName = "id")
    public ApiResult pcUpdate(@RequestBody @Validated WarehouseLocationMoveInfoDTO.PcUpdateDTO dto) {
        dto.setPcShow(true);
        warehouseLocationMoveInfoService.pcUpdate(dto);
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
     * 获取状态统计
     * @return
     */
    @PostMapping("/pc/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:paging",
            tableAlias = "wlmi"
    )
    public ApiResult<List<WarehouseLocationMoveInfoDTO.PdaTabListDTO>> pcTabList(@RequestBody PermissionsDTO dto) {
        return success(warehouseLocationMoveInfoService.pcTabList(dto));
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
    * 列表查询-pc端
     * @author hyj
     * @date 2024/4/12 16:46
    * @param dto
    * @return ApiResult<PagingVO<WarehouseLocationMoveInfoDTO.ListDTO>>
    */
    @PostMapping("/pc/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:paging",
            tableAlias = "wlmi"
    )
    @WebAdvanceQuery(handler = MarehouseMoveInfoQueryHandler.class)
    public ApiResult<PagingVO<WarehouseLocationMoveInfoDTO.ListDTO>> pcPaging(@RequestBody @Validated PagingDTO<WarehouseLocationMoveInfoDTO.PagingParamDTO> dto) {
        return success(warehouseLocationMoveInfoService.pcPaging(dto));
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
    * 新增并提交审核
     * @author hyj
     * @date 2024/4/15 10:47
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交仓位移动")
    @PostMapping("/pc/addAndSubmit")
    public ApiResult<Void> pcAddAndSubmit(@RequestBody @Validated WarehouseLocationMoveInfoDTO.AddDTO dto) {
        dto.setPcShow(true);
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
    * 修改并提交审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交仓位移动")
    @PostMapping("/pc/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:updateAndSubmit",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> pcUpdateAndSubmit(@RequestBody @Validated WarehouseLocationMoveInfoDTO.UpdateDTO dto) {
        dto.setPcShow(true);
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
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 提交审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交仓位移动")
    @PostMapping("/pc/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:submit",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return submit(dto);
//        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
//        for (String id : dto.getIds()) {
//            BatchResultDTO submit;
//            try {
//                submit = warehouseLocationMoveInfoService.pcSubmit(id);
//            }catch (Exception e){
//                log.error("仓位移动主单 提交审核失败",e);
//                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(id);
//                if (ObjectUtil.isEmpty(entity)) {
//                    submit = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 提交失败");
//                    resultDTOS.add(submit);
//                    continue;
//                }
//                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
//            }
//            resultDTOS.add(submit);
//        }
//        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
    * 审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核仓位移动")
    @PostMapping("/pc/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:paging",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        return approve(dto);
//        List<String> ids = dto.getIds();
//        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
//        for (String id : ids) {
//            BatchResultDTO approveResult;
//            try {
//                approveResult = warehouseLocationMoveInfoService.pcApprove(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
//            }catch (Exception e){
//                log.error("仓位移动审核失败",e);
//                WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getById(id);
//                if (ObjectUtil.isEmpty(warehouseLocationMoveDetailEntity)) {
//                    approveResult = BatchResultDTO.fail(id, id, "仓位移动明细不存在, 审核失败");
//                    resultDTOS.add(approveResult);
//                    continue;
//                }
//                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(warehouseLocationMoveDetailEntity.getMainId());
//                if (ObjectUtil.isEmpty(entity)) {
//                    approveResult = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 审核失败");
//                    resultDTOS.add(approveResult);
//                    continue;
//                }
//                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
//            }
//            resultDTOS.add(approveResult);
//        }
//        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核仓位移动")
    @PostMapping("/pc/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:disApprove",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return disApprove(dto);
//        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
//        for (String id : dto.getIds()) {
//            BatchResultDTO disApproveResult;
//            try {
//                disApproveResult = warehouseLocationMoveInfoService.pcDisApprove(id);
//            }catch (Exception e){
//                log.error("仓位移动主单反审核失败",e);
//                WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getById(id);
//                if (ObjectUtil.isEmpty(warehouseLocationMoveDetailEntity)) {
//                    disApproveResult = BatchResultDTO.fail(id, id, "仓位移动明细不存在, 反审核失败");
//                    resultDTOS.add(disApproveResult);
//                    continue;
//                }
//                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(warehouseLocationMoveDetailEntity.getMainId());
//                if (ObjectUtil.isEmpty(entity)) {
//                    disApproveResult = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 反审核失败");
//                    resultDTOS.add(disApproveResult);
//                    continue;
//                }
//                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
//            }
//            resultDTOS.add(disApproveResult);
//        }
//        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
    * 删除
    * @author Luo_WG
    * @date:  2023-08-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除仓位移动")
    @PostMapping("/pc/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:delete",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return delete(dto);
//        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
//        for (String id : dto.getIds()) {
//            BatchResultDTO deleteResult;
//            try {
//                deleteResult = warehouseLocationMoveInfoService.pcDelete(id);
//            }catch (Exception e){
//                log.error("仓位移动主单删除失败",e);
//                WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getById(id);
//                if (ObjectUtil.isEmpty(warehouseLocationMoveDetailEntity)) {
//                    deleteResult = BatchResultDTO.fail(id, id, "仓位移动明细不存在, 删除失败");
//                    resultDTOS.add(deleteResult);
//                    continue;
//                }
//                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(warehouseLocationMoveDetailEntity.getMainId());
//                if (ObjectUtil.isEmpty(entity)) {
//                    deleteResult = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 删除失败");
//                    resultDTOS.add(deleteResult);
//                    continue;
//                }
//                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
//            }
//            resultDTOS.add(deleteResult);
//        }
//        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
     * @author hyj
     * @date 2024/4/19 10:37
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销仓位移动")
    @PostMapping("/pc/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:cancelProcess",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return cancelProcess(dto);
//        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
//        for (String id : dto.getIds()) {
//            BatchResultDTO cancelResult;
//            try {
//                cancelResult = warehouseLocationMoveInfoService.pcCancelProcess(id);
//            }catch (Exception e){
//                log.error("仓位移动主单撤回流程失败",e);
//                WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getById(id);
//                if (ObjectUtil.isEmpty(warehouseLocationMoveDetailEntity)) {
//                    cancelResult = BatchResultDTO.fail(id, id, "仓位移动明细不存在, 撤回流程失败");
//                    resultDTOS.add(cancelResult);
//                    continue;
//                }
//                WarehouseLocationMoveInfoEntity entity = warehouseLocationMoveInfoService.getById(warehouseLocationMoveDetailEntity.getMainId());
//                if (ObjectUtil.isEmpty(entity)) {
//                    cancelResult = BatchResultDTO.fail(id, id, "仓位移动主单不存在, 撤回流程失败");
//                    resultDTOS.add(cancelResult);
//                    continue;
//                }
//                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
//            }
//            resultDTOS.add(cancelResult);
//        }
//        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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

    /**
    * 详情
    * @author Luo_WG
    * @date:  2023-08-24
    * @param id
    * @return ApiResult<WarehouseLocationMoveInfoDTO.ViewDTO>>
    */
    @LogViewService
    @GetMapping("/pc/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:view",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "id")
    public ApiResult<WarehouseLocationMoveInfoDTO.PdaPcViewDTO> pcView(@RequestParam("id") String id) {
        return success(warehouseLocationMoveInfoService.pcView(id));
    }


    /**
     * 导出明细
     * @author hyj
     * @date 2024/4/16 11:33
     * @param dto
     */
    @LogViewService
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:export",
            serviceClass = WarehouseLocationMoveInfoService.class,
            keyIdName = "id")
    public void listExport(@RequestBody WarehouseLocationMoveInfoDTO.ExportDTO dto, HttpServletResponse response) {
        warehouseLocationMoveInfoService.listExport(dto,response);
    }
    /**
     * 导出明细
     * @author hyj
     * @date 2024/4/17 10:31
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板仓位移动")
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/pdaMoveInfoTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @LogAction(value = LogActionEnum.IMPORT, desc = "仓位移动导入")
    @PostMapping("/importFile")
    public ApiResult importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean flag = warehouseLocationMoveInfoService.importFile(excelFile,response);
        return flag == true ? success() : failure();
    }
}
