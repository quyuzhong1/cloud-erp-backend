package com.erp.server.plm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
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
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.server.plm.service.ProductCertificateService;
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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 产品认证
 * * @author Will
 * @version 1.0
 * @date 2024/2/19 10:38
 */
@RestController
@LogSystemModule("产品认证")
@RequestMapping("productCertificate")
public class ProductCertificateController extends BaseController {

    @Resource
    private ProductCertificateService productCertificateService;

    /**
     * 分页查询
     * @author Will
     * @date: 2024/2/19 10:52
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productCertificate:paging",
            tableAlias = "pc")
    public ApiResult<PagingVO<ProductCertificateDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<ProductCertificateDTO.SearchParamDTO> dto) {
        PagingVO<ProductCertificateDTO.ListDTO> pagingVO = productCertificateService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 上传证书
     * @author Will
     * @date: 2024/2/19 10:52
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "上传证书")
    @PostMapping("/add")
    public ApiResult add(@ModelAttribute @Validated ProductCertificateDTO.AddDTO dto) {
        productCertificateService.add(dto);
        return  success();
    }

    /**
     * 修改
     * @author Will
     * @date: 2024/2/19 10:52
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改产品证书信息")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productCertificate:update",
            serviceClass = ProductCertificateService.class,
            keyIdName = "id")
    public ApiResult update(@ModelAttribute @Validated ProductCertificateDTO.UpdateDTO dto) {
        Boolean flag = productCertificateService.update(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 查询详情
     * @author Will
     * @date: 2024/2/19 10:52
     * @param id
     * @return ApiResult<ProductCertificateDTO.viewDTO>
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productCertificate:view",
            serviceClass = ProductCertificateService.class,
            keyIdName = "id")
    public ApiResult<ProductCertificateDTO.ViewDTO> view(@Param("id") String id) {
        ProductCertificateDTO.ViewDTO dto = productCertificateService.view(id);
        return success(dto);
    }

    /**
     * 删除
     * @author Will
     * @date: 2024/2/19 10:52
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除产品证书信息")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productCertificate:delete",
            serviceClass = ProductCertificateService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = productCertificateService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 下载模板
     * @author Will
     * @date: 2024/2/19 17:46
     * @param request
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载产品认证信息模板")
    @GetMapping("/exportExcelTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/productCertificateTemplate.xlsx";
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
        return success();
    }

    /**
     * 导入
     * @author Will
     * @date: 2024/2/20 9:59
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "产品证书信息批量导入")
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = productCertificateService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出产品证书信息")
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "plm:productCertificate:paging",
            tableAlias = "pc"
    )
    public ApiResult exportExcel(@RequestBody ProductCertificateDTO.ExportParamDTO dto) {
        Boolean flag = productCertificateService.exportExcel(dto);
        return flag == true ? success() : failure();
    }
}
