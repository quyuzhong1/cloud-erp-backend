package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.business.vo.SeriesVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.vo.ProductPlanGroupVO;
import com.erp.model.plm.vo.ProductPlanStatisticsVO;
import com.erp.model.plm.vo.ProductPlanVO;
import com.erp.server.plm.service.ProductPlanService;
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
 *  产品规划
 * @author Will
 * @date: 2023/2/21 9:45
 */
@RestController
@RequestMapping("plm/product/plan")
public class ProductPlanController extends BaseController {

    @Resource
    private ProductPlanService productPlanService;


    /**
     * 产品规划-分页查询
     * @author Will
     * @date: 2023/2/21 9:52
     * @param dto
     * @return ApiResult<PagingVO<List<ProductPlanVO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<ProductPlanVO>>> queryByPage(@RequestBody @Validated PagingDTO<ProductPlanSearchDTO> dto) {
        PagingVO<List<ProductPlanVO>> pagingVO = productPlanService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 产品规划-查询详情
     * @author Will
     * @date: 2023/2/21 10:01
     * @param id
     * @return ApiResult<ProductPlanDetailsDTO>
     */
    @GetMapping("/productPlanDetails")
    public ApiResult<ProductPlanDetailsDTO> productPlanDetails(@Param("id") String id) {
        ProductPlanDetailsDTO productPlanDTO = productPlanService.productPlanDetails(id);
        return success(productPlanDTO);
    }

    /**
     * 产品规划-图片上传
     * @author Will
     * @date: 2023/2/21 12:03
     * @param multipartFile
     * @param id
     * @return ApiResult
     */
    @PostMapping("/uploadImageUrl")
    public ApiResult uploadImageUrl(@RequestParam("multipartFile") MultipartFile multipartFile,@RequestParam("id") String id) {
        Boolean flag = productPlanService.uploadImageUrl(multipartFile,id);
        return flag == true ? success() : failure();
    }

    /**
     * 产品规划-规划开发
     * @author Will
     * @date: 2023/2/21 10:54
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/planDevelopProduct")
    public ApiResult planDevelopProduct(@RequestBody @Validated ProductPlanDevelopDTO dto) {
        Boolean flag = productPlanService.planDevelopProduct(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 产品规划-删除
     * @author Will
     * @date: 2023/2/21 10:03
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = productPlanService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

    /**
     * 产品规划-添加备注
     * @author Will
     * @date: 2023/2/21 10:07
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/addRemark")
    public ApiResult addRemark(@RequestBody @Validated ProductPlanRemarkDTO dto) {
        Boolean flag = productPlanService.addRemark(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 产品规划-指标数据
     * @author Will
     * @date: 2023/2/21 11:13
     * @param dto
     * @return ApiResult<List<ProductPlanStatisticsVO>>
     */
    @PostMapping("/listProductPlanStatistics")
    public ApiResult<List<ProductPlanStatisticsVO>> listProductPlanStatistics(@RequestBody ProductPlanGroupSerachDTO dto) {
        List<ProductPlanStatisticsVO> list = productPlanService.listProductPlanStatistics(dto);
        return  success(list);
    }

    /**
     * 产品规划-表格数据-产品经理
     * @author Will
     * @date: 2023/2/21 11:23
     * @param dto
     * @return ApiResult<List<ProductPlanGroupVO>>
     */
    @PostMapping("/listTableChargeName")
    public ApiResult<List<ProductPlanGroupVO>> listTableChargeName(@RequestBody ProductPlanGroupSerachDTO dto) {
        List<ProductPlanGroupVO> list = productPlanService.listTableChargeName(dto);
        return  success(list);
    }

    /**
     * 产品规划-表格数据-产品等级
     * @author Will
     * @date: 2023/2/21 11:24
     * @param dto
     * @return ApiResult<List<ProductPlanGroupVO>>
     */
    @PostMapping("/listTableGrade")
    public ApiResult<List<ProductPlanGroupVO>> listTableGrade(@RequestBody ProductPlanGroupSerachDTO dto) {
        List<ProductPlanGroupVO> list = productPlanService.listTableGrade(dto);
        return  success(list);
    }

    /**
     * 产品规划-表格数据-产品分类
     * @author Will
     * @date: 2023/2/21 11:25
     * @param dto
     * @return ApiResult<List<ProductPlanGroupVO>>
     */
    @PostMapping("/listTableCategory")
    public ApiResult<List<ProductPlanGroupVO>> listTableCategory(@RequestBody ProductPlanGroupSerachDTO dto) {
        List<ProductPlanGroupVO> list = productPlanService.listTableCategory(dto);
        return  success(list);
    }

    /**
     * 产品规划-立项趋势
     * @author Will
     * @date: 2023/2/21 11:42
     * @param dto
     * @return ApiResult<List<ProductPlanGroupVO>>
     */
    @PostMapping("/listApprovalTrend")
    public ApiResult<List<SeriesVO>> listApprovalTrend(@RequestBody ProductPlanGroupSerachDTO dto) {
        List<SeriesVO> list = productPlanService.listApprovalTrend(dto);
        return  success(list);
    }

    /**
     * @description: 查询所有未关联规划
     * @author Will
     * @date: 2023/2/24 11:48
     * @return ApiResult<List<SelectShowDTO>>
     */
    @GetMapping("/listNotRelatedProductPlan")
    public ApiResult<List<SelectShowDTO>> listNotRelatedProductPlan() {
        List<SelectShowDTO> list = productPlanService.listNotRelatedProductPlan();
        return  success(list);
    }


    /**
     *  产品规划-导入规划
     * @author Will
     * @date: 2023/2/21 10:11
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @PostMapping("/importFile")
    public ApiResult importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean flag = productPlanService.importFile(excelFile,response);
        return flag == true ? success() : failure();
    }

    /**
     * 产品规划-下载模板
     * @author Will
     * @date: 2023/2/21 10:26
     * @param request
     * @param response

     */
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/productPlanTemplate.xlsx";
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
    * 产品规划-导出规划
    * @author Will
    * @date: 2023/2/21 10:29
    * @param productPlanSearchDTO
    * @param response
    * @return ApiResult
    */
    @PostMapping(value = "/exportProductPlan")
    public ApiResult exportProductPlan(@RequestBody ProductPlanSearchDTO productPlanSearchDTO, HttpServletResponse response) {
        Boolean flag = productPlanService.exportProductPlan(productPlanSearchDTO, response);
        return flag == true ? success() : failure();
    }




}

