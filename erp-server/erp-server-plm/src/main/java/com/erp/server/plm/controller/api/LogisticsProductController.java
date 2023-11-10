package com.erp.server.plm.controller.api;/**
 * @author Lambda
 * @Classname LogisticsController
 * @Description TODO
 * @Date 2023-11-06 12:24
 * @Created by yl
 */

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.server.plm.service.LogisticsProductService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 物流产品
 *
 * @Author yl
 * @Date 2023-11-06 12:24
 */
@RestController
@LogSystemModule("物流产品")
@RequestMapping("logistics/product")
public class LogisticsProductController extends BaseController {

    @Resource
    private LogisticsProductService logisticsProductService;


    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<LogisticsProductDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<LogisticsProductDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsProductDTO.PagingVO> pagingVO = logisticsProductService.paging(dto);
        return success(pagingVO);

    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<LogisticsProductDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        LogisticsProductDTO.ViewDTO viewDTO = logisticsProductService.view(id);
        return success(viewDTO);

    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售出库单")
    public ApiResult update(@RequestBody @Valid LogisticsProductDTO.UpdateDTO dto) {
        Boolean updateResult = logisticsProductService.update(dto);
        return updateResult?success():failure();

    }


    /**
     * 导出产品信息
     * @param dto
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出物流产品信息")
    @PostMapping("/export")
    public ApiResult exportExcel(@RequestBody @Valid LogisticsProductDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = logisticsProductService.exportExcel(dto, response);
        return result ? success() : failure();
    }

    /**
     * 导入产品信息
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入物流产品信息")
    @PostMapping("/importExcel")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = logisticsProductService.importExcel(excelFile, response);
        return result ? success() : failure();
    }


    /**
     * 下载模板
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板物流产品")
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/logisticsProductTemplate.xlsx";
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


}
