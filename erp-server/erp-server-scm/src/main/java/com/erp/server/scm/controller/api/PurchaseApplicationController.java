package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.server.scm.query.PurchaseApplicationQueryHandler;
import com.erp.server.scm.service.PurchaseApplicationDetailService;
import com.erp.server.scm.service.PurchaseApplicationService;
import com.erp.server.scm.service.SubcontractOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 采购申请管理
 *
 * @author will
 * @since 2023-03-15
 */
@Slf4j
@RestController
@LogSystemModule("采购申请单")
@RequestMapping("/purchaseApplication")
public class PurchaseApplicationController extends BaseController {

    @Resource
    private PurchaseApplicationService purchaseApplicationService;
    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;
    @Resource
    private SubcontractOrderService subcontractOrderService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<List<ScmPurchaseApplicationViewDTO>>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:paging",
            tableAlias = "pa")
    @WebAdvanceQuery(handler = PurchaseApplicationQueryHandler.class)
    public ApiResult<PagingVO<PurchaseApplicationDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseApplicationDTO.ListDTO> pagingVO = purchaseApplicationService.paging(dto);
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
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:paging",
            tableAlias = "pa")
    @WebAdvanceQuery(handler = PurchaseApplicationQueryHandler.class)
    public ApiResult<PurchaseApplicationDTO.PagingTotalDTO> pagingTotal(@RequestBody @Validated PurchaseApplicationDTO.SearchParamDTO dto) {
        PurchaseApplicationDTO.PagingTotalDTO pagingTotalDTO = purchaseApplicationService.pagingTotal(dto);
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
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:paging",
            tableAlias = "pa")
    public ApiResult<List<ListStatusCountDTO.PurchaseApplicationCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<ListStatusCountDTO.PurchaseApplicationCountDTO> list = purchaseApplicationService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增采购申请单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:cancelProcess",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto) {
        purchaseApplicationService.add(dto);
        return success();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购申请单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:update",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PurchaseApplicationDTO.UpdateDTO dto) {
        Boolean flag = purchaseApplicationService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购申请单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:add",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto) {
        Boolean flag = purchaseApplicationService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交采购申请单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:update",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseApplicationDTO.UpdateDTO dto) {
        Boolean flag = purchaseApplicationService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<PurchaseApplicationDTO.ViewDTO>
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:view",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "id")
    public ApiResult<PurchaseApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        PurchaseApplicationDTO.ViewDTO dto = purchaseApplicationService.view(id);
        return success(dto);
    }

    /**
     * 批量提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "批量提交采购申请单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:submit",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseApplicationService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "批量审核采购申请单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:approve",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchaseApplicationEntity> entityList = purchaseApplicationService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchaseApplicationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购申请单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchaseApplicationService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("采购申请单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
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
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "批量反审核采购申请单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:disApprove",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchaseApplicationEntity> entityList = purchaseApplicationService.listByIds(dto.getIds());
        List<PurchaseApplicationDetailEntity> detailList = purchaseApplicationDetailService.listByPurchaseApplicationIds(dto.getIds());
        List<SubcontractOrderEntity> subcontractOrderList = subcontractOrderService.listBySourceId(dto.getIds());
        for (String id : dto.getIds()) {
            PurchaseApplicationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购申请单不存在"));
                continue;
            }
            List<PurchaseApplicationDetailEntity> detailEntityList = detailList.stream().filter(e -> e.getPurchaseApplicationId().equals(id)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                resultDTOS.add(BatchResultDTO.fail(id,id,ApiError.ERROR_98017.msg));
                continue;
            }
            //只有未生成的单才能反审核
            long createCount = detailEntityList.stream().filter(obj -> !CreatePoTypeEnum.NOT_GENERATED.getStatus().equals(obj.getCreatePoType())).count();
            if (createCount > 0) {
                resultDTOS.add(BatchResultDTO.fail(id,id,ApiError.ERROR_98030.msg));
                continue;
            }
            List<SubcontractOrderEntity> subcontractOrderEntityList = subcontractOrderList.stream().filter(e -> e.getSourceId().equals(id)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(subcontractOrderEntityList)) {
                resultDTOS.add(BatchResultDTO.fail(id,id,ApiError.ERROR_98090.msg));
                continue;
            }
            try {
                resultDTOS.add(purchaseApplicationService.disApprove(entity));
            }catch (Exception e){
                log.error("采购申请单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 批量删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除采购申请单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:delete",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseApplicationService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }
    /**
     * 批量关闭（传明细ID）
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "批量关闭采购申请单")
    @PostMapping("/close")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:delete",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "ids")
    public ApiResult close(@RequestBody @Validated PurchaseApplicationDTO.CloseDTO dto) {
        Boolean flag = purchaseApplicationService.close(dto);
        return flag == true ? success() : failure();
    }
    /**
     * 生成采购单弹窗显示
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/viewGeneratePurchaseOrder")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:viewGeneratePurchaseOrder",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "ids")
    public ApiResult<List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO>> viewGeneratePurchaseOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO> list = purchaseApplicationService.viewGeneratePurchaseOrder(dto.getIds());
        return success(list);
    }

    /**
     * 生成采购单
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "生成采购单")
    @PostMapping("/generatePurchaseOrder")
    public ApiResult generatePurchaseOrder(@RequestBody @Validated PurchaseApplicationDTO.ListGeneratePurchaseOrderDTO dto) {
        Boolean flag = purchaseApplicationService.generatePurchaseOrder(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "scm:purchaseApplication:cancelProcess",
            serviceClass = PurchaseApplicationService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchaseApplicationService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 导入
     * @author Will
     * @date: 2023/3/15 18:22
     * @param excelImportDTO
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入采购单")
    @PostMapping("/importFile")
    public ApiResult<PurchaseApplicationDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.CommonDTO excelImportDTO, HttpServletResponse response) {
        PurchaseApplicationDetailDTO.ImportDTO dto = purchaseApplicationService.importFile(excelImportDTO.getExcelFile(), excelImportDTO.getSkuIds(), response);
        return success(dto);
    }

    /**
     * 下载模板
     * @author Will
     * @date: 22023/3/15 18:22
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载采购单模板")
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/purchaseApplicationTemplate.xlsx";
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
     * 导出
     * @author Will
     * @date: 2023/3/15 18:23
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseApplicationDTO.SearchParamDTO dto) {
        Boolean flag = purchaseApplicationService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推委外订单显示
     * @author Will
     * @date: 2023/6/12 15:00
     * @param dto
     * @return ApiResult<List<ViewGenerateSubcontractOrderDTO>>
     */
    @PostMapping(value = "/viewGenerateSubcontractOrder")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:purchaseApplication:viewGenerateSubcontractOrder",
            tableAlias = "so"
    )
    public ApiResult<List<PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO>> viewGenerateSubcontractOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO> list = purchaseApplicationService.viewGenerateSubcontractOrder(dto.getIds());
        return success(list);
    }

    /**
     * 下推委外订单保存
     * @author Will
     * @date: 2023/6/12 15:10
     * @param list
     * @return ApiResult<Void>
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推委外订单保存")
    @PostMapping(value = "/generateSubcontractOrder")
    public ApiResult<Void> generateSubcontractOrder(@RequestBody @Validated ValidList<PurchaseApplicationDTO.GenerateSubcontractOrderDTO> list) {
        purchaseApplicationService.generateSubcontractOrder(list);
        return success();
    }
    /**
     * 下推委外订单-批量获取列表采购单价
     * @param list
     * @return
     */
    @PostMapping("/batchGetSubcontractPurchasePrice")
    public ApiResult<PurchaseApplicationDTO.SubcontractPurchasePriceDTO> batchGetSubcontractPurchasePrice(@RequestBody @Validated ValidList<PurchaseApplicationDTO.GenerateSubcontractOrderDTO> list) {
        return purchaseApplicationService.batchGetSubcontractPurchasePrice(list);
    }
}
