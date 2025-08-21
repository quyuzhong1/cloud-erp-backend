package com.erp.server.wms.controller.api;


import com.common.core.utils.ExcelUtil;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SampleScrapDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 样品报废单明细表
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("样品报废单明细表")
@RequestMapping("/sampleScrapDetail")
public class SampleScrapDetailController extends BaseController {

    @Resource
    private SampleScrapDetailService sampleScrapDetailService;

    /**
     * 下载模板
     * @author jack
     * @date:  2025-04-21
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载样品报废单导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
        String standardPath = "classpath:excel/sampleScrapDetailTemplate.xlsx";
        String standardExcelName = "sampleScrapDetailTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     * 导入
     * @author jack
     * @date:  2025-04-21
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品报废单明细导入")
    public ApiResult<SampleScrapDetailDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success(sampleScrapDetailService.importFile(excelFile, response));
    }



}
