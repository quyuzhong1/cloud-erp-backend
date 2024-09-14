package com.erp.server.wms.controller.api;


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
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.server.wms.query.VirtualWarehouseAllocationQueryHandler;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import com.erp.server.wms.service.VirtualWarehouseAllocationService;
import com.erp.server.wms.service.VirtualWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 虚拟仓分货单
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@RestController
@LogSystemModule("分货单")
@RequestMapping("/virtualWarehouseAllocation")
public class VirtualWarehouseAllocationController extends BaseController {

    @Resource
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-06-05
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "分货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehouseAllocationDTO.AddDTO dto) {
        return success(virtualWarehouseAllocationService.add(dto));
    }

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-06-05
     */
    @PostMapping("/saveAndSubmit")
    @LogAction(value = LogActionEnum.INSERT, desc = "分货单保存并提交")
    public ApiResult<BatchResultDTO> addAndSubmit(@RequestBody @Validated VirtualWarehouseAllocationDTO.UpdateDTO dto) {
        return success(virtualWarehouseAllocationService.saveAndSubmit(dto));
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "分货单修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:update",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualWarehouseAllocationDTO.UpdateDTO dto) {
        virtualWarehouseAllocationService.update(dto);
        return success();
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date: 2024-06-05
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:paging",
            tableAlias = "vma"
    )
    @WebAdvanceQuery(handler = VirtualWarehouseAllocationQueryHandler.class)
    public ApiResult<PagingVO<VirtualWarehouseAllocationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualWarehouseAllocationDTO.PagingParamDTO> dto) {
        return success(virtualWarehouseAllocationService.paging(dto));
    }

    /**
     * 详情
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:view",
            serviceClass = VirtualWarehouseService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<VirtualWarehouseAllocationDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        return success(virtualWarehouseAllocationService.view(id));
    }

    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交分货单信息")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:submit",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        //待提交
        String waitSubmitStatus = VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode();
        for (String id : ids) {
            BatchResultDTO submit;
            String flagCode = id;
            try {
                VirtualWarehouseAllocationEntity allocationEntity = virtualWarehouseAllocationService.getById(id);
                if (Objects.isNull(allocationEntity)) {
                    submit = BatchResultDTO.fail(id, id, "分货单不存在");
                } else {
                    //只有待提交状态可以修改
                    if (!Objects.equals(waitSubmitStatus, allocationEntity.getStatus())) {
                        submit = BatchResultDTO.fail(id, allocationEntity.getCode(), ApiError.IS_SUBMIT_IN_SUBMIT.msg);
                    } else {
                        flagCode = allocationEntity.getCode();
                        submit = virtualWarehouseAllocationService.submit(allocationEntity);
                    }
                }
            } catch (Exception e) {
                log.error("提交分货单失败>>>>{}", e);
                submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
            }
            resultDTOS.add(submit);
        }

        return resultDTOS.stream().anyMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    @LogAction(value = LogActionEnum.SUBMIT, desc = "作废分货单信息")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:invalid",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        //待提交
        String waitSubmitStatus = VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode();
        for (String id : ids) {
            BatchResultDTO submit;
            String flagCode = id;
            try {
                VirtualWarehouseAllocationEntity allocationEntity = virtualWarehouseAllocationService.getById(id);
                if (Objects.isNull(allocationEntity)) {
                    submit = BatchResultDTO.fail(id, id, "分货单不存在");
                } else {
                    //只有待提交状态可以修改
                    if (!Objects.equals(waitSubmitStatus, allocationEntity.getStatus())) {
                        submit = BatchResultDTO.fail(id, allocationEntity.getCode(), ApiError.ERROR_98009.msg);
                    } else {
                        flagCode = allocationEntity.getCode();
                        submit = virtualWarehouseAllocationService.invalid(allocationEntity, VirtualWarehouseAllocationStatusEnum.INVALID.getCode(), dto.getRemark());
                    }
                }
            } catch (Exception e) {
                log.error("作废分货单失败>>>>{}", e);
                submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().anyMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出明细
     *
     * @param dto
     * @author hyj
     * @date 2024/4/16 11:33
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出分货单")
    @PostMapping("/export")
    public ApiResult<Boolean> export(@RequestBody VirtualWarehouseAllocationDTO.ExportDTO dto) {
        virtualWarehouseAllocationService.export(dto);
        return success(true);
    }

    /**
     * 下载导入模板
     *
     * @param request
     * @param response
     * @author hyj
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载分货单导入模板")
    @GetMapping("/exportTemplate")
    public void exportTemplate(@RequestParam(value = "type") String type, HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/VwAllocation"+type+"Template.xlsx";
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

    /**
     * 导入
     * @param type
     * @param excelFile
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "分货单导入")
    @PostMapping("/importFile")
    public ApiResult<VirtualWarehouseAllocationDTO.DetailViewDto> importFile(@RequestParam(value = "type") String type, @RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success(virtualWarehouseAllocationService.importFile(type,excelFile, response));
    }

    /**
     * 获取数量统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:paging",
            tableAlias = "vma"
    )
    public ApiResult<List<VirtualWarehouseAllocationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(virtualWarehouseAllocationService.tabList(dto));
    }

    /**
     * 展示作废信息
     *
     * @param id
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "展示作废信息")
    @GetMapping("/viewInvalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:viewInvalid",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<VirtualWarehouseAllocationDTO.ManualFinishViewDTO> viewInvalid(@RequestParam(value = "id") String id) {
        return success(virtualWarehouseAllocationService.viewInvalid(id));
    }

    /**
     * 更新备注
     */
    @PostMapping("/updateRemark")
    public ApiResult<Boolean> updateRemark(@RequestBody @Validated VirtualWarehouseAllocationDTO.UpdateRemarkDTO updateRemarkDTO){
        return success(virtualWarehouseAllocationService.updateRemark(updateRemarkDTO));
    }

    /**
     * 查询虚拟仓库存数据
     * @author will
     * @date 2024/8/2 10:34
     * @param list
     * @return ApiResult<VirtualInventoryQtyDTO>
     */
    @PostMapping("/listVirtualInventory")
    public ApiResult<List<VirtualWarehouseAllocationDTO.VirtualInventoryQtyDTO>> getVirtualInventory(@RequestBody @Validated List<VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO> list){
        return success(virtualWarehouseAllocationService.listVirtualInventory(list));
    }

}
