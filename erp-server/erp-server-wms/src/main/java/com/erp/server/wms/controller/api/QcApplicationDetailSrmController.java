package com.erp.server.wms.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.erp.server.wms.service.QcApplicationDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 质检申请单SRM明细表
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@RestController
@LogSystemModule("质检申请单SRM明细表")
@RequestMapping("/qcApplicationDetailSrm")
public class QcApplicationDetailSrmController extends BaseController {

    @Resource
    private QcApplicationDetailService qcApplicationDetailService;


    /**
     * 下载模板
     * @author will
     * @date:  2026-03-20
     * @param request
     * @return ApiResult<Object>>
     */
    @GetMapping("/exportTemplate")
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载质检申请单模板")
    public ApiResult<Object>exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "excel/qcApplicationDetailTemplate.xlsx";
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
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
        return success();
    }

    /**
     *  质检申请明细导入
     * @author will
     * @date: 2026-03-20
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "质检申请导入")
    @PostMapping(value = "/importExcel")
    public ApiResult<QcApplicationDetailDTO.ImportDTO> importExcel(@RequestBody @Valid QcApplicationDetailDTO.ImportParamDTO dto) {
        return success(qcApplicationDetailService.importExcel(dto));
    }


}
