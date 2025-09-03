package com.erp.server.oms.controller.api;


import com.common.core.utils.ExcelUtil;
import com.erp.model.wms.dto.SampleBorrowDetailDTO;
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
import com.erp.server.oms.service.ExhibitionOrderDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 展会订单详情
 *
 * @author jack
 * @since 2025-08-29
 */
@Slf4j
@RestController
@LogSystemModule("展会订单详情")
@RequestMapping("/exhibitionOrderDetail")
public class ExhibitionOrderDetailController extends BaseController {

    @Resource
    private ExhibitionOrderDetailService exhibitionOrderDetailService;

    /**
     * 下载模板
     * @author jack
     * @date:  2025-04-21
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载展会订单明细导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
        String standardPath = "classpath:excel/exhibitionOrderDetailTemplate.xlsx";
        String standardExcelName = "exhibitionOrderDetailTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }


    /**
     * 导入
     * @author jack
     * @date:  2025-04-21
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.EXPORT, desc = "展会订单明细导入")
    public ApiResult<ExhibitionOrderDetailDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "id",required = false) String id,@RequestParam(value = "recipientUserId") String recipientUserId,  @RequestParam(value = "isTax") Boolean isTax, HttpServletResponse response) {
        return success(exhibitionOrderDetailService.importFile(excelFile,id,recipientUserId,isTax, response));
    }

}
