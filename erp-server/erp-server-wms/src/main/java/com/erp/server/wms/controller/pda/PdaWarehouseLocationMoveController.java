package com.erp.server.wms.controller.pda;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.entity.WarehouseLocationMoveEntity;
import com.erp.server.wms.query.MarehouseMoveInfoQueryHandler;
import com.erp.server.wms.service.TransferInfoService;
import com.erp.server.wms.service.WarehouseLocationMoveDetailService;
import com.erp.server.wms.service.WarehouseLocationMoveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
public class PdaWarehouseLocationMoveController extends BaseController {

    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;

    @Resource
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
    public ApiResult<String> add(@RequestBody @Validated WarehouseLocationMoveDTO.AddDTO dto) {
        return success(warehouseLocationMoveService.add(dto));
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
    public ApiResult<String> pcAdd(@RequestBody @Validated WarehouseLocationMoveDTO.PcAddDTO dto) {
        return success(warehouseLocationMoveService.pcAdd(dto));
    }

    /**
     * 新增并审核
     * @param dto
     * @return ApiResult<String>
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓位移动（自动审核）")
    @PostMapping("/addAndApprove")
    public ApiResult<String> addAndApprove(@RequestBody @Validated WarehouseLocationMoveDTO.AddDTO dto) {
        return success(warehouseLocationMoveService.addAndApprove(dto));
    }

    /**
     * 更新并审核
     * @param dto
     * @return ApiResult<String>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新仓位移动（自动审核）")
    @PostMapping("/updateAndApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:update",
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "id")
    public ApiResult<String> updateAndApprove(@RequestBody @Validated WarehouseLocationMoveDTO.UpdateDTO dto) {
        return success(warehouseLocationMoveService.updateAndApprove(dto));
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
        serviceClass = WarehouseLocationMoveService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseLocationMoveDTO.UpdateDTO dto) {
        warehouseLocationMoveService.update(dto);
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
        serviceClass = WarehouseLocationMoveService.class,
        keyIdName = "id")
    public ApiResult pcUpdate(@RequestBody @Validated WarehouseLocationMoveDTO.PcUpdateDTO dto) {
        dto.setPcShow(true);
        warehouseLocationMoveService.pcUpdate(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "wlmd.warehouse_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:paging",
            tableAlias = "wlmi"
    )
    public ApiResult<List<WarehouseLocationMoveDTO.PdaTabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(warehouseLocationMoveService.tabList(dto));
    }

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/pc/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "wlmd.warehouse_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:paging",
            tableAlias = "wlmi"
    )
    public ApiResult<List<WarehouseLocationMoveDTO.PdaTabListDTO>> pcTabList(@RequestBody PermissionsDTO dto) {
        return success(warehouseLocationMoveService.pcTabList(dto));
    }

    /**
    * 列表查询
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @return ApiResult<PagingVO<WarehouseLocationMoveDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "wlmd.warehouse_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:paging",
            tableAlias = "wlmi"
    )
    public ApiResult<PagingVO<WarehouseLocationMoveDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<WarehouseLocationMoveDTO.PagingParamDTO> dto) {
        return success(warehouseLocationMoveService.paging(dto));
    }
    /**
    * 列表查询-pc端
     * @author hyj
     * @date 2024/4/12 16:46
    * @param dto
    * @return ApiResult<PagingVO<WarehouseLocationMoveDTO.ListDTO>>
    */
    @PostMapping("/pc/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "wlmd.warehouse_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:paging",
            tableAlias = "wlmi"
    )
    @WebAdvanceQuery(handler = MarehouseMoveInfoQueryHandler.class)
    public ApiResult<PagingVO<WarehouseLocationMoveDTO.ListDTO>> pcPaging(@RequestBody @Validated PagingDTO<WarehouseLocationMoveDTO.PagingParamDTO> dto) {
        return success(warehouseLocationMoveService.pcPaging(dto));
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
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated WarehouseLocationMoveDTO.AddDTO dto) {
        warehouseLocationMoveService.addAndSubmit(dto);
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
    public ApiResult<Void> pcAddAndSubmit(@RequestBody @Validated WarehouseLocationMoveDTO.AddDTO dto) {
        dto.setPcShow(true);
        warehouseLocationMoveService.addAndSubmit(dto);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated WarehouseLocationMoveDTO.UpdateDTO dto) {
        warehouseLocationMoveService.updateAndSubmit(dto);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "id")
    public ApiResult<Void> pcUpdateAndSubmit(@RequestBody @Validated WarehouseLocationMoveDTO.UpdateDTO dto) {
        dto.setPcShow(true);
        warehouseLocationMoveService.updateAndSubmit(dto);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = warehouseLocationMoveService.submit(id);
            }catch (Exception e){
                log.error("仓位移动主单 提交审核失败",e);
                WarehouseLocationMoveEntity entity = warehouseLocationMoveService.getById(id);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return submit(dto);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = warehouseLocationMoveService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("仓位移动主单审核失败",e);
                WarehouseLocationMoveEntity entity = warehouseLocationMoveService.getById(id);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = warehouseLocationMoveService.disApprove(id);
            }catch (Exception e){
                log.error("仓位移动主单反审核失败",e);
                WarehouseLocationMoveEntity entity = warehouseLocationMoveService.getById(id);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = warehouseLocationMoveService.delete(id);
            }catch (Exception e){
                log.error("仓位移动主单删除失败",e);
                WarehouseLocationMoveEntity entity = warehouseLocationMoveService.getById(id);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = warehouseLocationMoveService.pcApprove(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("仓位移动主单审核失败",e);
                WarehouseLocationMoveEntity entity = warehouseLocationMoveService.getById(id);
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
    @PostMapping("/pc/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:disApprove",
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return disApprove(dto);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return delete(dto);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = warehouseLocationMoveService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("仓位移动主单撤回流程失败",e);
                WarehouseLocationMoveEntity entity = warehouseLocationMoveService.getById(id);
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
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> pcCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return cancelProcess(dto);
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
        Boolean flag = warehouseLocationMoveService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
    * 详情
    * @author Luo_WG
    * @date:  2023-08-24
    * @param id
    * @return ApiResult<WarehouseLocationMoveDTO.ViewDTO>>
    */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:view",
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "id")
    public ApiResult<WarehouseLocationMoveDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(warehouseLocationMoveService.view(id));
    }

    /**
    * 详情
    * @author Luo_WG
    * @date:  2023-08-24
    * @param id
    * @return ApiResult<WarehouseLocationMoveDTO.ViewDTO>>
    */
    @LogViewService
    @GetMapping("/pc/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaWarehouseLocationMoveInfo:pc:view",
            serviceClass = WarehouseLocationMoveService.class,
            keyIdName = "id")
    public ApiResult<WarehouseLocationMoveDTO.PcViewDTO> pcView(@RequestParam("id") String id) {
        return success(warehouseLocationMoveService.pcView(id));
    }


    /**
     * 导出明细
     * @author hyj
     * @date 2024/4/16 11:33
     * @param dto
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出仓位移动")
    @PostMapping("/export")
    public ApiResult<Boolean> listExport(@RequestBody WarehouseLocationMoveDTO.ExportDTO dto) {
        warehouseLocationMoveService.listExport(dto);
        return success(true);
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @LogAction(value = LogActionEnum.IMPORT, desc = "仓位移动导入")
    @PostMapping("/importFile")
    public ApiResult<WarehouseLocationMoveDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success(warehouseLocationMoveService.importFile(excelFile,response));
    }
}
