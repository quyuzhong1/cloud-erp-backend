package com.erp.server.wms.controller.api;


import com.common.core.utils.ExcelUtil;
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
import org.springframework.web.multipart.MultipartFile;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SampleBackDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleBackDetailDTO;

/**
 * 样品退回详情
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品退回详情")
@RequestMapping("/sampleBackDetail")
public class SampleBackDetailController extends BaseController {

    @Resource
    private SampleBackDetailService sampleBackDetailService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品退回详情新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleBackDetailDTO.AddDTO dto) {
        return success(sampleBackDetailService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品退回详情修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleBackDetail:update",
        serviceClass = SampleBackDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleBackDetailDTO.UpdateDTO dto) {
        sampleBackDetailService.update(dto);
        return success();
    }

    /**
     * 下载模板
     * @author wuhaotian
     * @date:  2025-08-21
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载样品退回详情导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
        String standardPath = "classpath:excel/sampleBackDetailTemplate.xlsx";
        String standardExcelName = "sampleBackDetailTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     * 导入
     * @author wuhaotian
     * @date:  2025-08-21
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.IMPORT, desc = "样品退回详情导入")
    public ApiResult<SampleBackDetailDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "backUserId", required = true) String backUserId, HttpServletResponse response) {
        if (backUserId == null || backUserId.trim().isEmpty()) {
            return failure("退回人不能为空");
        }
        return success(sampleBackDetailService.importFile(excelFile, backUserId, response));
    }

}
