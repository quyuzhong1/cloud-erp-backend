package com.erp.server.wms.controller.api;


import com.common.core.utils.ExcelUtil;
import com.erp.model.wms.dto.SampleBorrowDetailDTO;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SampleBorrowDetailService;
import com.common.core.controller.vo.ApiResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 借用变更单明细表
 *
 * @author jack
 * @since 2025-08-26
 */
@Slf4j
@RestController
@LogSystemModule("借用变更单明细表")
@RequestMapping("/sampleBorrowDetail")
public class SampleBorrowDetailController extends BaseController {

    @Resource
    private SampleBorrowDetailService sampleBorrowDetailService;

    /**
     * 下载模板
     * @author jack
     * @date:  2025-04-21
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载样品借用单导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
        String standardPath = "classpath:excel/sampleBorrowDetailTemplate.xlsx";
        String standardExcelName = "sampleBorrowDetailTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     * 导入
     * @author jack
     * @date:  2025-04-21
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品借用单明细导入")
    public ApiResult<SampleBorrowDetailDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success(sampleBorrowDetailService.importFile(excelFile, response));
    }




}
