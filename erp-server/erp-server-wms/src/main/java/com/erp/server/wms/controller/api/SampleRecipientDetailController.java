package com.erp.server.wms.controller.api;


import com.common.core.utils.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SampleRecipientDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleRecipientDetailDTO;

/**
 * 样品领用单明细
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品领用单明细")
@RequestMapping("/sampleRecipientDetail")
public class SampleRecipientDetailController extends BaseController {

    @Resource
    private SampleRecipientDetailService sampleRecipientDetailService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品领用单明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleRecipientDetailDTO.AddDTO dto) {
        return success(sampleRecipientDetailService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品领用单明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleRecipientDetail:update",
        serviceClass = SampleRecipientDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleRecipientDetailDTO.UpdateDTO dto) {
        sampleRecipientDetailService.update(dto);
        return success();
    }

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-09-01
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载样品领用单明细导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
        String standardPath = "classpath:excel/sampleRecipientDetailTemplate.xlsx";
        String standardExcelName = "sampleRecipientDetailTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     * 导入
     * @author wuhaotian
     * @date: 2025-09-01
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.IMPORT, desc = "样品领用单明细导入")
    public ApiResult<SampleRecipientDetailDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success(sampleRecipientDetailService.importFile(excelFile, response));
    }

}
