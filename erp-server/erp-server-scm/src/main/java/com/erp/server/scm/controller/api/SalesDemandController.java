package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.server.scm.service.SalesDemandService;
import org.apache.ibatis.annotations.Param;
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
import java.util.List;

/**
 * 备货申请管理
 *
 * @author will
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/salesDemand")
public class SalesDemandController extends BaseController {

    @Resource
   private SalesDemandService salesDemandService;

   /**
    * 分页查询
    * @author Will
    * @date: 2023/3/15 16:47
    * @param dto
    * @return ApiResult<PagingVO<SalesDemandDTO.listDTO>>
    */
   @PostMapping("/paging")
   @DataPermission(operationType = DataAttributeEnum.LIST,
           tableField = "apply_user_id",
           menuCode = "scm:salesDemand:paging",
           tableAlias = "sd")
   public ApiResult<PagingVO<SalesDemandDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<SalesDemandDTO.SearchParamDTO> dto) {
        PagingVO<SalesDemandDTO.ListDTO> pagingVO = salesDemandService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 查询数量
     * @author Will
     * @date: 2023/3/15 17:34
     * @return ApiResult
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:paging",
            tableAlias = "sd")
    public ApiResult<List<ListStatusCountDTO.SalesDemandCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<ListStatusCountDTO.SalesDemandCountDTO> list = salesDemandService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:add",
            serviceClass = SalesDemandService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated SalesDemandDTO.AddDTO dto) {
        salesDemandService.add(dto);
        return success();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:update",
            serviceClass = SalesDemandService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SalesDemandDTO.UpdateDTO dto) {
        Boolean flag = salesDemandService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:addAndSubmit",
            serviceClass = SalesDemandService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SalesDemandDTO.AddDTO dto) {
        Boolean flag = salesDemandService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:updateAndSubmit",
            serviceClass = SalesDemandService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SalesDemandDTO.UpdateDTO dto) {
        Boolean flag = salesDemandService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<ScmSalesDemandDTO>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:view",
            serviceClass = SalesDemandService.class,
            keyIdName = "id")
    public ApiResult<SalesDemandDTO.ViewDTO> view(@Param("id") String id) {
        SalesDemandDTO.ViewDTO dto = salesDemandService.view(id);
        return success(dto);
    }

    /**
     * 批量删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:delete",
            serviceClass = SalesDemandService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = salesDemandService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }


    /**
     * 批量作废
     * @author Will
     * @date: 2023/3/15 17:50
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:invalid",
            serviceClass = SalesDemandService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = salesDemandService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:submit",
            serviceClass = SalesDemandService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = salesDemandService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:approve",
            serviceClass = SalesDemandService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        salesDemandService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/3/15 17:50
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:disApprove",
            serviceClass = SalesDemandService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = salesDemandService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }


    /**
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:cancelProcess",
            serviceClass = SalesDemandService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = salesDemandService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导入
     * @author Will
     * @date: 2023/3/15 18:22
     * @param excelImportDTO
     * @return ApiResult
     */
    @PostMapping("/importFile")
    public ApiResult<SalesDemandDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.CommonDTO excelImportDTO, HttpServletResponse response) {
        SalesDemandDetailDTO.ImportDTO list = salesDemandService.importFile(excelImportDTO.getExcelFile(), excelImportDTO.getSkuIds(),response);
        return success(list);
    }

    /**
     * 下载模板
     * @author Will
     * @date: 22023/3/15 18:22
     * @param request
     * @param response
     */
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/salesDemandTemplate.xlsx";
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
     * 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            menuCode = "scm:salesDemand:paging",
            tableAlias = "sd")
    public ApiResult exportExcel(@RequestBody SalesDemandDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = salesDemandService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }



}
