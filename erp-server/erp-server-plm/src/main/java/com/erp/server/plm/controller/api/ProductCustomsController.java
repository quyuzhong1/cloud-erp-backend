package com.erp.server.plm.controller.api;


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
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.server.plm.service.ProductCustomsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 目的国清关信息
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@LogSystemModule("供应商列表")
@RequestMapping("/supplier/visit")
public class ProductCustomsController extends BaseController {


    @Resource
    private ProductCustomsService productCustomsService;


    /**
     * 分页查询
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productCustoms:paging",
            tableAlias = "pc"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<ProductCustomsDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ProductCustomsDTO.PagingParamDTO> dto) {
        PagingVO<ProductCustomsDTO.ListDTO> pagingVO = productCustomsService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增目的国清关信息")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated ProductCustomsDTO.AddListDTO dto) {
       Boolean  result= productCustomsService.add(dto);
       return result==true?success():failure();
    }


    /**
     * 编辑
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "编辑目的国清关信息")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productCustoms:update",
            serviceClass = ProductCustomsService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated ProductCustomsDTO.UpdateDTO dto) {
        Boolean  result= productCustomsService.update(dto);
        return result==true?success():failure();
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-06-21
     * @return ApiResult<SupplierCredentialDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productCustoms:view",
            serviceClass = ProductCustomsService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ProductCustomsDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(productCustomsService.view(id));
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
            menuCode = "plm:productCustoms:export",
            tableAlias = "pc"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery
    public ApiResult<Object> exportList(@RequestBody @Validated ProductCustomsDTO.PagingParamDTO dto, HttpServletResponse response) {
        productCustomsService.exportList(dto, response);
        return success();
    }

    /**
     * 导入
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入目的国清关信息")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = productCustomsService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        productCustomsService.downloadTemplate(response);
        return success();
    }

}
