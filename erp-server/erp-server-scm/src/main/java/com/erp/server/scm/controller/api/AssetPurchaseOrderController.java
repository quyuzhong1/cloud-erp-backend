package com.erp.server.scm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.server.scm.query.AssetPurchaseOrderQueryHandler;
import com.erp.server.scm.service.AssetPurchaseOrderDetailService;
import com.erp.server.scm.service.AssetPurchaseOrderService;
import com.erp.server.scm.service.PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.annotation.Resource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.scm.entity.AssetPurchaseOrderEntity;

/**
 * 
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@RestController
@LogSystemModule("资产采购订单")
@RequestMapping("/assetPurchaseOrder")
public class AssetPurchaseOrderController extends BaseController {

    @Resource
    private AssetPurchaseOrderService assetPurchaseOrderService;

    @Resource
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetPurchaseOrderDTO.AddDTO dto) {
        return success(assetPurchaseOrderService.add(dto));
    }

    /**
    * 修改
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:assetPurchaseOrder:update",
        serviceClass = AssetPurchaseOrderService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetPurchaseOrderDTO.UpdateDTO dto) {
        assetPurchaseOrderService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:paging",
            tableAlias = ""
    )
    public ApiResult<List<AssetPurchaseOrderDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(assetPurchaseOrderService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return ApiResult<PagingVO<AssetPurchaseOrderDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:paging",
            tableAlias = "apo"
    )
    @WebAdvanceQuery(handler = AssetPurchaseOrderQueryHandler.class)
    public ApiResult<PagingVO<AssetPurchaseOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AssetPurchaseOrderDTO.PagingParamDTO> dto) {
        return success(assetPurchaseOrderService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AssetPurchaseOrderDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = assetPurchaseOrderService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:updateAndSubmit",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AssetPurchaseOrderDTO.UpdateDTO dto) {
        assetPurchaseOrderService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:submit",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseOrderEntity> list = assetPurchaseOrderService.lambdaQuery().in(AssetPurchaseOrderEntity::getId, ids).list();
		Map<String, AssetPurchaseOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = assetPurchaseOrderService.submit(id);
            }catch (Exception e){
                log.error(" 提交审核失败",e);
                AssetPurchaseOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "不存在, 提交失败");
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
    * 审核
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:approve",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AssetPurchaseOrderEntity> list = assetPurchaseOrderService.lambdaQuery().in(AssetPurchaseOrderEntity::getId, ids).list();
		Map<String, AssetPurchaseOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseOrderEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetPurchaseOrderService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("审核失败",e);
                AssetPurchaseOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "不存在, 审核失败");
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
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:disApprove",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseOrderEntity> list = assetPurchaseOrderService.lambdaQuery().in(AssetPurchaseOrderEntity::getId, ids).list();
		Map<String, AssetPurchaseOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = assetPurchaseOrderService.disApprove(id);
            }catch (Exception e){
                log.error("反审核失败",e);
                AssetPurchaseOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "不存在, 反审核失败");
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
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:delete",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseOrderEntity> list = assetPurchaseOrderService.lambdaQuery().in(AssetPurchaseOrderEntity::getId, ids).list();
		Map<String, AssetPurchaseOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = assetPurchaseOrderService.delete(id);
            }catch (Exception e){
                log.error("删除失败",e);
                AssetPurchaseOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "不存在, 删除失败");
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
    * 作废
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:invalid",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AssetPurchaseOrderEntity> list = assetPurchaseOrderService.lambdaQuery().in(AssetPurchaseOrderEntity::getId, ids).list();
		Map<String, AssetPurchaseOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = assetPurchaseOrderService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("作废失败",e);
                AssetPurchaseOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:cancelProcess",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<AssetPurchaseOrderEntity> list = assetPurchaseOrderService.lambdaQuery().in(AssetPurchaseOrderEntity::getId, ids).list();
        Map<String, AssetPurchaseOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = assetPurchaseOrderService.cancelProcess(id);
            }catch (Exception e){
                log.error("撤回流程失败",e);
                AssetPurchaseOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "不存在, 撤回流程失败");
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
    * 详情
    * @author wtr
    * @date:  2025-10-16
    * @param id
    * @return ApiResult<AssetPurchaseOrderDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:view",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AssetPurchaseOrderDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(assetPurchaseOrderService.view(id));
    }

    /**
     * 结束交货
     * @author wtr
     * @date:  2025-10-16
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "结束采购订单验收:ids={ids},备注={remark}")
    @PostMapping("/endReceive")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:assetPurchaseOrder:endReceive",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> endReceive(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = assetPurchaseOrderDetailService.endReceive(dto.getIds(), dto.getRemark());
        return result == true ? success() : failure();
    }

    /**
     * 合同状态更新
     * @author wtr
     * @date:  2025-10-16
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateContractStampStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:assetPurchaseOrder:updateContractStampStatus",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "合同状态更新")
    public ApiResult<?> updateContractStampStatus(@RequestBody @Validated AssetPurchaseOrderDTO.ContractStampStatusParamsDTO dto) {
        assetPurchaseOrderService.updateContractStampStatus(dto);
        return success();
    }

    /**
     * 导入
     * @author wtr
     * @date:  2025-10-16
     * @param excelImportDTO
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入资产采购单")
    @PostMapping("/importFile")
    public ApiResult<AssetPurchaseOrderDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.CommonDTO excelImportDTO, HttpServletResponse response) {
        AssetPurchaseOrderDTO.ImportDTO importDTO = assetPurchaseOrderService.importFile(excelImportDTO.getExcelFile(), response);
        return success(importDTO);
    }

    /**
     * 下载模板
     * @author wtr
     * @date:  2025-10-16
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载资产采购单模板")
    @GetMapping("/exportTemplate")
    public ApiResult<Object> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/assetPurchaseOrderTemplate.xlsx";
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
        return success();
    }

    /**
    * 导出Excel数据
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:assetPurchaseOrder:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public ApiResult<Object> exportList(@RequestBody @Validated AssetPurchaseOrderDTO.ExportDTO dto, HttpServletResponse response) {
        assetPurchaseOrderService.exportList(dto, response);
        return success();
    }

    /**
     * 下拉选择列表（支持关键字查询）
     * @author wtr
     * @date: 2025-10-16
     * @param paramDTO 查询参数
     * @return ApiResult<List<AssetPurchaseOrderDTO.SelectDTO>>
     */
    @PostMapping("/selectList")
    public ApiResult<List<AssetPurchaseOrderDTO.SelectDTO>> selectList(@RequestBody AssetPurchaseOrderDTO.SelectParamDTO paramDTO) {
        return success(assetPurchaseOrderService.selectList(paramDTO));
    }
    /**
     * 查询资产采购订单明细（用于资产验收单添加明细）
     *
     * @param assetPurchaseOrderId 资产采购订单ID
     * @return 明细列表
     */
    @PostMapping("/queryDetailsForAccept")
    public ApiResult<List<AssetPurchaseOrderDTO.DetailForAcceptDTO>> queryDetailsForAccept(@RequestBody String assetPurchaseOrderId) {
        return success(assetPurchaseOrderService.queryDetailsForAccept(assetPurchaseOrderId));
    }

    /**
     * 网采合同导出
     * @param id
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "网采合同导出")
    @GetMapping("/exportPurchaseContract")
    public ApiResult<?> exportPurchaseContract(@RequestParam("id") String id, HttpServletResponse response) {
        Boolean flag = assetPurchaseOrderService.exportPurchaseContract(id, response);
        return flag == true ? success() : failure();
    }

    /**
     * 查询采购合同PDF数据
     * @author
     * @date:
     * @param id
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购合同PDF")
    @GetMapping("/listAssetPurchaseContractPdf")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:assetPurchaseOrder:exportAssetPurchaseContractPdf",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<AssetPurchaseOrderDTO.ExportPdfDTO> listPurchaseContractPdf(@RequestParam("id") String id) {
        AssetPurchaseOrderDTO.ExportPdfDTO exportPdfDTO = assetPurchaseOrderService.listPurchaseContractPdf(id);
        return success(exportPdfDTO);
    }

    /**
     * 导出采购合同PDF
     * @author
     * @date:
     * @param
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购合同PDF")
    @PostMapping("/exportAssetPurchaseContractPdf")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:assetPurchaseOrder:exportAssetPurchaseContractPdf",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public void exportAssetPurchaseContractPdf(@RequestBody @Valid BaseIdDTO dto, HttpServletResponse response) {
        assetPurchaseOrderService.exportAssetPurchaseContractPdf(dto.getId(),response);
    }

    /**
     * 获取关联单据
     * @param detailId
     * @return
     */
    @GetMapping("/getAcceptByDetailId")
    public ApiResult<?> getAcceptByDetailId(@RequestParam("detailId") String detailId) {
        return assetPurchaseOrderService.getAcceptByDetailId(detailId);
    }

    /**
     * 下推资产验收单弹窗显示
     * @author
     * @date:
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/viewGenerateAssetAccept")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:assetNotice:viewGenerateAssetAccept",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<List<AssetPurchaseOrderDTO.ViewGenerateAssetAcceptDTO>> viewGenerateAssetAccept(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<AssetPurchaseOrderDTO.ViewGenerateAssetAcceptDTO> list = assetPurchaseOrderService.viewGenerateAssetAccept(dto);
        return success(list);
    }

    /**
     * 下推资产验收单
     * @author
     * @date:
     * @param dtoList
     * @return ApiResult
     */
    @PostMapping("/generateAssetAccept")
    @LogAction(value = LogActionEnum.INSERT, desc = "生成资产采购单")
    public ApiResult<Object> generateAssetAccept(@RequestBody @Validated List<AssetPurchaseOrderDTO.GenerateAssetAcceptDTO> dtoList) {
        Boolean flag = assetPurchaseOrderService.generateAssetAccept(dtoList);
        return flag == true ? success() : failure();
    }

    /**
     * 下推采购变更单弹窗显示
     * @author
     * @date:
     * @param dto 明细id
     * @return ApiResult
     */
    @PostMapping("/viewGeneratePurchaseChangeOrder")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:assetNotice:viewGenerateAssetAccept",
            serviceClass = AssetPurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<AssetPurchaseOrderDTO.ViewGeneratePurchaseChangeDTO> viewGeneratePurchaseChangeOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
       AssetPurchaseOrderDTO.ViewGeneratePurchaseChangeDTO viewGeneratePurchaseChangeOrderDTO = assetPurchaseOrderService.viewGeneratePurchaseChangeOrder(dto);
        return success(viewGeneratePurchaseChangeOrderDTO);
    }

}
