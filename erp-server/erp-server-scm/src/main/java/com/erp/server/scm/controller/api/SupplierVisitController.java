package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.server.scm.query.SupplierCredentialQueryHandler;
import com.erp.server.scm.query.SupplierVisitQueryHandler;
import com.erp.server.scm.service.SupplierCredentialService;
import com.erp.server.scm.service.SupplierVisitService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 现场考察
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@LogSystemModule("供应商列表")
@RequestMapping("/supplier/visit")
public class SupplierVisitController extends BaseController {


    @Resource
    private SupplierVisitService supplierVisitService;

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplierVisit:paging",
            tableAlias = "sv"
    )
    public ApiResult<List<SupplierVisitDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(supplierVisitService.tabList(dto));
    }

    /**
     * 根据供应商id拜访分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierVisitDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<BaseIdDTO> dto) {
        PagingVO<SupplierVisitDTO.PagingViewDTO> pagingVO = supplierVisitService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 现场考察分页列表
     *
     * @return
     */
    @PostMapping("/pagingList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplierVisit:paging",
            tableAlias = "sv"
    )
    @WebAdvanceQuery(handler = SupplierVisitQueryHandler.class)
    public ApiResult<PagingVO<SupplierVisitDTO.ListDTO>> pagingList(@RequestBody @Validated PagingDTO<SupplierVisitDTO.PagingParamDTO> dto) {
        PagingVO<SupplierVisitDTO.ListDTO> pagingVO = supplierVisitService.pagingList(dto);
        return success(pagingVO);
    }

    /**
     * 新增现场考察
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增现场考察")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SupplierVisitDTO.AddDTO dto) {
       Boolean  result= supplierVisitService.add(dto);
        return result==true?success():failure();
    }


    /**
     * 编辑现场考察
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "编辑现场考察")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplierVisit:update",
            serviceClass = SupplierVisitService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SupplierVisitDTO.UpdateDTO dto) {
        Boolean  result= supplierVisitService.update(dto);
        return result==true?success():failure();
    }

    /**
     * 现场考察详情
     * @author jack
     * @date:  2025-06-21
     * @return ApiResult<SupplierCredentialDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplierVisit:view",
            serviceClass = SupplierVisitService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SupplierVisitDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(supplierVisitService.view(id));
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplierVisit:export",
            tableAlias = "sv"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery(handler = SupplierVisitQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated SupplierVisitDTO.PagingParamDTO dto, HttpServletResponse response) {
        supplierVisitService.exportList(dto, response);
        return success();
    }

    /**
     * 导入
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入现场考察")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = supplierVisitService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板现场考察")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        supplierVisitService.downloadTemplate(response);
        return success();
    }

}
