package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpOrderInfoDTO;
import com.erp.model.bi.dto.DmpOrderInfoSearchDTO;
import com.erp.model.bi.dto.DmpOrderStateDTO;
import com.erp.server.bi.service.DmpOrderInfoService;
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

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:15
 */
@RestController
@RequestMapping("bi/dmpOrderInfo")
public class DmpOrderInfoController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    /**
     * @description: 销售数据-分页查询
     * @author Will
     * @date: 2022/12/15 10:48
     * @param dto
     * @return ApiResult<PagingVO<DmpOrderInfoDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<DmpOrderInfoDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpOrderInfoSearchDTO> dto) {
        PagingVO<DmpOrderInfoDTO> pagingVO = dmpOrderInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 销售数据-修改状态
     * @author Will
     * @date: 2022/12/15 10:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated DmpOrderStateDTO dto) {
        Boolean flag = dmpOrderInfoService.updateState(dto);
        return flag == true ? this.success() : this.failure();
    }


    /**
     *  销售数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    public void exportExcel(@RequestBody DmpOrderInfoSearchDTO dto, HttpServletResponse response) {
        dmpOrderInfoService.exportExcel(dto, response);
    }

    /**
     * @description: 下载模板
     * @author Will
     * @date: 2022/12/15 18:42
     * @param request
     * @param response

     */
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/dmpOrderInfo.xlsx";
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
        }

    }

}
