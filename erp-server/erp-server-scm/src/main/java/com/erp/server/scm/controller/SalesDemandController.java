package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandPagingParamDTO;
import com.erp.model.scm.dto.SalesDemandPagingViewDTO;
import com.erp.server.scm.service.SalesDemandService;
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
import java.util.List;

/**
 * 销售需求
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
    * @return ApiResult<PagingVO<List<ScmSalesDemandDTO>>>
    */
   @PostMapping("/paging")
    public ApiResult<PagingVO<List<SalesDemandPagingViewDTO>>> queryByPage(@RequestBody @Validated PagingDTO<SalesDemandPagingParamDTO> dto) {
        PagingVO<List<SalesDemandPagingViewDTO>> pagingVO = salesDemandService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取备货单号
     * @author Will
     * @date: 2023/3/16 10:52
     * @return ApiResult
     */
    @GetMapping("/getCode")
    public ApiResult getCode() {
        String code = salesDemandService.getCode();
        return success(code);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param salesDemandDTO
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SalesDemandDTO salesDemandDTO) {
        Boolean flag = salesDemandService.add(salesDemandDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param salesDemandDTO
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SalesDemandDTO salesDemandDTO) {
        Boolean flag = salesDemandService.update(salesDemandDTO);
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
    public ApiResult<SalesDemandDTO> view(@Param("id") String id) {
        SalesDemandDTO salesDemandDTO = salesDemandService.view(id);
        return success(salesDemandDTO);
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param id
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestParam("id") String id) {
        Boolean flag = salesDemandService.delete(id);
        return flag == true ? success() : failure();
    }


    /**
     * 批量作废
     * @author Will
     * @date: 2023/3/15 17:50
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestParam("ids") List<String> ids) {
        Boolean flag = salesDemandService.invalid(ids);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param id
     * @return ApiResult
     */
    @PostMapping("/commit")
    public ApiResult commit(@RequestParam("id") String id) {
        Boolean flag = salesDemandService.commit(id);
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
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        salesDemandService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/3/15 17:50
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/unApprove")
    public ApiResult unAudit(@RequestParam("ids") List<String> ids) {
        Boolean flag = salesDemandService.unApprove(ids);
        return flag == true ? success() : failure();
    }


    /**
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestParam("id") String id) {
        Boolean result = salesDemandService.cancelProcess(id);
        return result == true ? success() : failure();
    }

    /**
     * 导入
     * @author Will
     * @date: 2023/3/15 18:22
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @PostMapping("/importFile")
    public ApiResult importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean flag = salesDemandService.importFile(excelFile,response);
        return flag == true ? success() : failure();
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
        String path = "classpath:excel/salesDemand.xlsx";
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
     * @param salesDemandPagingParamDTO
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody SalesDemandPagingParamDTO salesDemandPagingParamDTO, HttpServletResponse response) {
        Boolean flag = salesDemandService.exportExcel(salesDemandPagingParamDTO, response);
        return flag == true ? success() : failure();
    }



}
