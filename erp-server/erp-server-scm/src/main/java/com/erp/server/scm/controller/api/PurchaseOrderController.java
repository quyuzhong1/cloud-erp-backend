package com.erp.server.scm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.*;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.server.scm.query.OrderConfirmQueryHandler;
import com.erp.server.scm.query.PurchaseOrderQueryHandler;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.erp.server.scm.service.SupplierUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购订单管理
 * @author will
 * @since 2023-03-16
 */
@Slf4j
@RestController
@LogSystemModule("采购订单")
@RequestMapping("/purchaseOrder")
public class PurchaseOrderController extends BaseController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;
    @Resource
    private SupplierUserService supplierUserService;
    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;
    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            warehouseTableField = "po.delivery_warehouse_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    @WebAdvanceQuery(handler = PurchaseOrderQueryHandler.class)
    public ApiResult<PagingVO<PurchaseOrderDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseOrderDTO.ListDTO> pagingVO = purchaseOrderService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 采购单号分页查询
     * @author will
     * @date 2024/11/11 11:46
     * @param dto
     * @return ApiResult<PagingVO<SourceCodeDTO>>
     */
    @PostMapping("/purchaseCodePaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            warehouseTableField = "po.delivery_warehouse_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    public ApiResult<PagingVO<PurchaseOrderDTO.SourceCodeDTO>> purchaseCodePaging(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SourceCodeParamDTO> dto) {
        PagingVO<PurchaseOrderDTO.SourceCodeDTO> pagingVO = purchaseOrderService.purchaseCodePaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表查询合计
     *
     * @param dto
     * @return
     */
    @PostMapping("/pagingTotal")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            warehouseTableField = "po.delivery_warehouse_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    @WebAdvanceQuery(handler = PurchaseOrderQueryHandler.class)
    public ApiResult<PurchaseOrderDTO.PagingTotalDTO> pagingTotal(@RequestBody @Validated PurchaseOrderDTO.SearchParamDTO dto) {
        PurchaseOrderDTO.PagingTotalDTO pagingTotalDTO = purchaseOrderService.pagingTotal(dto);
        return success(pagingTotalDTO);
    }
    
    /**
     * 查询数量
     * @author Will
     * @date: 2023/3/15 17:34
     * @return ApiResult
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            warehouseTableField = "po.delivery_warehouse_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    public ApiResult<List<ListStatusCountDTO.PurchaseOrderCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<ListStatusCountDTO.PurchaseOrderCountDTO> list = purchaseOrderService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto)
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增采购订单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:add",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<?> add(@RequestBody @Validated PurchaseOrderDTO.AddDTO dto) {
        PurchaseOrderEntity entity = purchaseOrderService.add(dto);
        return success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode()));
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购订单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:update",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PurchaseOrderDTO.UpdateDTO dto) {
        Boolean flag = purchaseOrderService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购订单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:add",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<?> addAndSubmit(@RequestBody @Validated PurchaseOrderDTO.AddDTO dto) {
        BatchResultDTO resultDTO = purchaseOrderService.addAndSubmit(dto);
        return resultDTO.getSuccess() ? success(resultDTO) : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交采购订单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:update",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<?> updateAndSubmit(@RequestBody @Validated PurchaseOrderDTO.UpdateDTO dto) {
        Boolean flag = purchaseOrderService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 更新备注
     * @author Will
     * @date: 2023/7/19 14:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "更新备注采购订单:明细备注={remark}")
    @PostMapping("/updateRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:update",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> updateRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchaseOrderDetailEntity> detailList = purchaseOrderDetailService.listByIds(dto.getIds());
        Map<String, PurchaseOrderDetailEntity> detailMap = detailList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        List<String> mainIds = detailList.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).distinct().collect(Collectors.toList());
        Map<String, PurchaseOrderEntity> entityMap = purchaseOrderService.mapByIds(mainIds);
        for (String id : dto.getIds()) {
            PurchaseOrderDetailEntity detailEntity = detailMap.get(id);
            if(Objects.isNull(detailEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购订单明细不存在"));
                continue;
            }
            PurchaseOrderEntity entity = entityMap.get(detailEntity.getPurchaseOrderId());
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购订单不存在"));
                continue;
            }
            try {
                BaseIdsDTO.RemarkDTO remarkDTO = new BaseIdsDTO.RemarkDTO();
                remarkDTO.setRemark(dto.getRemark());
                remarkDTO.setIds(Collections.singletonList(id));
                Boolean flag = purchaseOrderService.updateRemark(remarkDTO);
                if (flag){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "修更新备注采购订单成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "修更新备注采购订单失败"));
                }
            }catch (Exception e){
                log.error("采购订单提交失败",e);
                resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<PurchaseOrderDTO.viewDTO>
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:view",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<PurchaseOrderDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseOrderDTO.ViewDTO dto = purchaseOrderService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除采购订单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:delete",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchaseOrderEntity> entityMap = purchaseOrderService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchaseOrderEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchaseOrderService.delete(entity));
            }catch (Exception e){
                log.error("采购订单删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购订单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:submit",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchaseOrderEntity> entityMap = purchaseOrderService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchaseOrderEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchaseOrderService.submit(entity, Boolean.TRUE));
            }catch (Exception e){
                log.error("采购订单提交失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量作废
     * @author Will
     * @date: 2023/3/15 17:50
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "批量作废采购订单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:invalid",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchaseOrderEntity> entityMap = purchaseOrderService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchaseOrderEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchaseOrderService.invalid(entity, dto.getRemark()));
            }catch (Exception e){
                log.error("采购订单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "批量审核采购订单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:approve",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = purchaseOrderService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("采购订单审核失败",e);
                PurchaseOrderEntity entity = purchaseOrderService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "采购订单不存在, 审核失败");
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
     * 批量反审核
     * @author Will
     * @date: 2023/3/15 17:50
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "批量反审核采购订单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:disApprove",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = purchaseOrderService.disApprove(id);
            }catch (Exception e){
                log.error("委外订单反审核失败",e);
                PurchaseOrderEntity entity = purchaseOrderService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "委外订单不存在, 反审核失败");
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
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购订单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:cancelProcess",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchaseOrderEntity> entityMap = purchaseOrderService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchaseOrderEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchaseOrderService.cancelProcess(entity));
            }catch (Exception e){
                log.error("采购订单撤销失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 采购变更数据显示
     * @author Will
     * @date: 2023/3/31 14:28
     * @param dto
     * @return ApiResult<AddDTO>
     */
    @PostMapping("/viewPurchaseChange")
    public ApiResult<PurchaseChangeDTO.ViewDTO> viewPurchaseChange(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        PurchaseChangeDTO.ViewDTO viewDTO = purchaseOrderService.viewPurchaseChange(dto.getIds());
        return success(viewDTO);
    }


    /**
     * 下推收货单弹框数据显示
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/viewGenerateReceive")
    public ApiResult<List<PurchaseOrderDTO.ViewGenerateReceiveDTO>> viewGenerateReceive(@RequestBody @Validated PurchaseOrderDTO.PushIdDTO dto) {
        List<PurchaseOrderDTO.ViewGenerateReceiveDTO> list = purchaseOrderService.viewGenerateReceive(dto.getPurchaseDetailIdList());
        return success(list);
    }

    /**
     * 下推采购入库单弹窗显示
     * @author Will
     * @date: 2023/4/13 11:20
     * @param dto
     * @return ApiResult<List<ViewGenerateReceiveDTO>>
     */
    @PostMapping("/viewGenerateStockIn")
    public ApiResult<List<PurchaseOrderDTO.ViewGenerateStockInDTO>> viewGenerateStockIn(@RequestBody @Validated PurchaseOrderDTO.PushIdDTO dto) {
        List<PurchaseOrderDTO.ViewGenerateStockInDTO> list = purchaseOrderService.viewGenerateStockIn(dto.getPurchaseDetailIdList());
        return success(list);
    }

    /**
     * 结束交货
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "结束采购订单交货:ids={ids},备注={remark}")
    @PostMapping("/finishDelivery")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:finishDelivery",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> finishDelivery(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = purchaseOrderDetailService.finishDelivery(dto.getIds(), dto.getRemark(),Boolean.TRUE);
        return result == true ? success() : failure();
    }

    /**
     * 查询采购合同PDF数据
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购合同PDF")
    @GetMapping("/listPurchaseContractPdf")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:exportPurchaseContractPdf",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<PurchaseOrderDTO.ExportPdfDTO> listPurchaseContractPdf(@RequestParam("id") String id) {
        PurchaseOrderDTO.ExportPdfDTO exportPdfDTO = purchaseOrderService.listPurchaseContractPdf(id);
        return success(exportPdfDTO);
    }

    /**
     * 导出采购合同PDF
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购合同PDF")
    @PostMapping("/exportPurchaseContractPdf")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:exportPurchaseContractPdf",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public void exportPurchaseContractPdf(@RequestBody @Valid BaseIdDTO dto, HttpServletResponse response) {
        purchaseOrderService.exportPurchaseContractPdf(dto.getId(),response);
    }

    /**
     * 导入
     * @author Will
     * @date: 2023/3/15 18:22
     * @param excelImportDTO
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入采购订单")
    @PostMapping("/importFile")
    public ApiResult<PurchaseOrderDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.purchaseOrderExcelImportDTO excelImportDTO, HttpServletResponse response) {
        PurchaseOrderDetailDTO.ImportDTO importDTO = purchaseOrderService.importFile(excelImportDTO, response);
        return success(importDTO);
    }

    /**
     * 下载模板
     * @author Will
     * @date: 22023/3/15 18:22
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载采购订单模板")
    @GetMapping("/exportExcelTemplate")
    public ApiResult<?> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/purchaseOrderTemplate.xlsx";
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
     *  导出
     * @author Will
     * @date: 2023/3/15 18:23
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购订单")
    @PostMapping(value = "/exportExcel")
    public ApiResult<?> exportExcel(@RequestBody PurchaseOrderDTO.SearchParamDTO dto) {
        Boolean flag = purchaseOrderService.exportExcel(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 查询单个采购订单
     * @author Will
     * @date: 2023/4/17 9:14
     * @param id
     * @return ApiResult<GetOneDTO>
     */
    @GetMapping(value = "/getPurchaseOrder")
    public ApiResult<PurchaseOrderDTO.GetOneDTO> getPurchaseOrder(@RequestParam("id") String id) {
        PurchaseOrderDTO.GetOneDTO getOneDTO = purchaseOrderService.getPurchaseOrder(id);
        return success(getOneDTO);
    }


    /**
     * 根据采购订单id 获取质检产品信息
     * @author yl
     * @date 2023-04-17 18:23
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO>
     */
    @GetMapping(value = "/getQcProductInfo")
    public ApiResult<PurchaseOrderDTO.GetQcProductDTO> getQcProductInfo(@RequestParam("id") String id) {
        PurchaseOrderDTO.GetQcProductDTO result = purchaseOrderService.getQcProductInfo(id);
        return success(result);
    }

    /**
     * 根据采购订单明细id 获取质检产品信息
     * @author jack
     * @date 2025-03-27
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO>
     */
    @GetMapping(value = "/getQcProductInfoByDetailId")
    public ApiResult<PurchaseOrderDTO.GetQcProductDTO> getQcProductInfoByDetailId(@RequestParam("id") String id) {
        PurchaseOrderDTO.GetQcProductDTO result = purchaseOrderService.getQcProductInfoByDetailId(id);
        return success(result);
    }

    /**
     * 添加产品数据显示
     * @author Will
     * @date: 2023/4/14 10:21
     * @param dto
     * @return ApiResult<ViewProductDTO>
     */
    @PostMapping(value = "/viewProduct")
    public ApiResult<List<PurchaseOrderDetailDTO.ViewProductDTO>> viewProduct(@RequestBody @Validated PurchaseOrderDetailDTO.ProductSearchParamDTO dto) {
        List<PurchaseOrderDetailDTO.ViewProductDTO> list = purchaseOrderDetailService.viewProduct(dto);
        return success(list);
    }


    /**
     * 采购订单 下推退货单数据显示
     * @author yl
     * @date 2023-04-25 9:37
     * @param dto
     * @return
     */
    @PostMapping("/viewGeneratePurchaseReturnOrder")
    public ApiResult<List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>> viewGeneratePurchaseReturnOrder(@RequestBody @Validated PurchaseOrderDTO.PushIdDTO dto) {
        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = purchaseOrderService.viewGeneratePurchaseReturnOrder(dto.getPurchaseDetailIdList());
        return success(list);
    }


    /**
     * 查询委外采购订单
     * @author Will
     * @date: 2023/6/15 15:09
     * @param dto
     * @return ApiResult<List<ViewSubcontractPoDTO>>
     */
    @PostMapping("/viewSubcontractPo")
    public ApiResult<List<PurchaseOrderDTO.ViewSubcontractPoDTO>> viewSubcontractPo(@RequestBody @Validated BaseIdDTO dto) {
        List<PurchaseOrderDTO.ViewSubcontractPoDTO> list = purchaseOrderService.viewSubcontractPo(dto.getId());
        return success(list);
    }


    /**
     * 网采合同导出
     * @Author Luo_WG
     * @Date 2023/7/13 16:29
     * @param id
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "网采合同导出")
    @GetMapping("/exportPurchaseContract")
    public ApiResult<?> exportPurchaseContract(@RequestParam("id") String id, HttpServletResponse response) {
        Boolean flag = purchaseOrderService.exportPurchaseContract(id, response);
        return flag == true ? success() : failure();
    }

    /**
     * 供应商确认
     * @author Will
     * @date: 2024/1/16 16:56
     * @param dto
     * @return ApiResult<List<ViewSubcontractPoDTO>>
     */
    @PostMapping("/supplierConfirm")
    public ApiResult<List<BatchResultDTO>> supplierConfirm(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = purchaseOrderService.supplierConfirm(id);
            }catch (Exception e){
                log.error("采购订单 提交审核失败",e);
                PurchaseOrderEntity entity = purchaseOrderService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "采购订单不存在, 提交失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 下载结束交货模板
     * @author Will
     * @date: 2024/3/0 10:22
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载结束交货模板")
    @GetMapping("/exportEndRecveiveTemplate")
    public ApiResult<?> exportEndRecveiveTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/purchaseEndReceiveTemplate.xlsx";
        String excelName = "purchaseEndReceiveTemplate.xlsx";
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
     * 导入结束交货采购订单
     * @author Will
     * @date: 2024/3/5 11:07
     * @param multipartFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入结束交货采购订单")
    @PostMapping("/importEndReceiveFile")
    public ApiResult<?> importEndReceiveFile(@RequestParam("excelFile") MultipartFile multipartFile, HttpServletResponse response) {
        purchaseOrderService.importEndReceiveFile(multipartFile, response);
        return success();
    }

    /**
     *  导出SRM采购订单
     * @author zdy
     * @date: 2023/3/15 18:23
     * @param dto
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出SRM采购订单")
    @PostMapping(value = "/exportSrmExcel")
    @WebAdvanceQuery(handler = OrderConfirmQueryHandler.class)
    public ApiResult<?> exportSrmExcel(@RequestBody @Validated PurchaseOrderDTO.SrmSearchParamDTO dto, HttpServletResponse response) {
        SupplierUserInfoVO info = purchaseOrderService.getSrmSupplierUserInfo();
        dto.setSupplierId(info.getSupplierId());
        Boolean flag = purchaseOrderService.exportSrmExcel(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 查询Srm采购合同信息
     * @author zdy
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "查询Srm采购合同信息")
    @GetMapping("/listSrmPurchaseContractPdf")
    public ApiResult<PurchaseOrderDTO.ExportPdfDTO> listSrmPurchaseContractPdf(@RequestParam("id") String id) {
        SupplierUserInfoVO info = purchaseOrderService.getSrmSupplierUserInfo();
        //采购订单是否是该供应商合同
        PurchaseOrderSupplierEntity orderSupplier = purchaseOrderSupplierService.getByPurchaseOrderId(id);
        if (ObjectUtils.isEmpty(orderSupplier)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        if (StringUtils.isEmpty(orderSupplier.getSupplierId()) || !orderSupplier.getSupplierId().equals(info.getSupplierId())){
            throw new ServiceException(ApiError.ERROR_98120, info.getSupplierName());
        }
        PurchaseOrderDTO.ExportPdfDTO exportPdfDTO = purchaseOrderService.listPurchaseContractPdf(id);
        return success(exportPdfDTO);
    }

    /**
     * 导出SRM采购合同PDF
     * @author zdy
     * @date: 2023/3/15 17:59
     * @param dto
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出SRM采购合同PDF")
    @PostMapping("/exportSrmPurchaseContractPdf")
    public void exportSrmPurchaseContractPdf(@RequestBody @Valid BaseIdDTO dto, HttpServletResponse response) {
        SupplierUserInfoVO info = purchaseOrderService.getSrmSupplierUserInfo();
        //采购订单是否是该供应商合同
        PurchaseOrderSupplierEntity orderSupplier = purchaseOrderSupplierService.getByPurchaseOrderId(dto.getId());
        if (ObjectUtils.isEmpty(orderSupplier)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        if (StringUtils.isEmpty(orderSupplier.getSupplierId()) || !orderSupplier.getSupplierId().equals(info.getSupplierId())){
            throw new ServiceException(ApiError.ERROR_98120, info.getSupplierName());
        }
        purchaseOrderService.exportPurchaseContractPdf(dto.getId(),response);
    }

    /**
     * 合同状态更新
     * @author jack
     * @date: 2025/5/12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateContractStampStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:updateContractStampStatus",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<?> updateContractStampStatus(@RequestBody @Validated PurchaseOrderDTO.ContractStampStatusParamsDTO dto) {
        purchaseOrderService.updateContractStampStatus(dto);
        return success();
    }
    /**
     * (供应商 + 采购订单 + sku )采购数量计算
     * @author jack
     * @date: 2025-06-24
     * @return ApiResult<String>
     */
    @GetMapping("/calSupplierPurchaseQty")
    public ApiResult<String> calSupplierPurchaseQty() {
        purchaseOrderService.calSupplierPurchaseQty();
        return success();
    }

}
