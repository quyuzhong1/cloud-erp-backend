package com.erp.server.dmp.controller.api;


import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO;
import com.erp.server.dmp.query.AdsErpInventoryDiffFlowQueryHandler;
import com.erp.server.dmp.service.AdsErpInventoryDiffFlowService;

import lombok.extern.slf4j.Slf4j;

/**
 * 第三方仓流水差异表
 *
 * @author shukai
 * @since 2025-11-14
 */
@Slf4j
@RestController
@LogSystemModule("第三方仓流水差异表")
@RequestMapping("/adsErpInventoryDiffFlow")
public class AdsErpInventoryDiffFlowController extends BaseController {

    @Resource
    private AdsErpInventoryDiffFlowService adsErpInventoryDiffFlowService;

    /**
    * 列表查询
    * @author shukai
    * @date: 2025-11-14
    * @param dto
    * @return ApiResult<PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = AdsErpInventoryDiffFlowQueryHandler.class)
    public ApiResult<PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffFlowService.paging(dto));
    }
    
    /**
     *  统计
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/total")
    @WebAdvanceQuery(handler = AdsErpInventoryDiffFlowQueryHandler.class)
    public ApiResult<AdsErpInventoryDiffFlowDTO.TotalDTO> total(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffFlowService.total(dto));
    }
    
    /**
     * 重新生成
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方仓流水差异表重新生成")
    @PostMapping(value = "/reCreate")
    public ApiResult<Boolean> reCreate(@RequestBody @Validated AdsErpInventoryDiffFlowDTO.ReCreateDTO dto) {
        return success(adsErpInventoryDiffFlowService.reCreate(dto));
    }
    
    /**
     * 修改备注
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方仓流水差异表修改备注")
    @PostMapping(value = "/updateRemark")
    public ApiResult<Boolean> updateRemark(@RequestBody @Validated AdsErpInventoryDiffFlowDTO.UpdateRemarkDTO dto) {
    	return success(adsErpInventoryDiffFlowService.updateRemark(dto));
    }

    /**
     *  导出Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "第三方仓流水差异表导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated AdsErpInventoryDiffFlowDTO.ExpotParamDTO dto) {
        return success(adsErpInventoryDiffFlowService.exportExcel(dto));
    }
    
    /**
     * 下载期初模板
     */
    @GetMapping("/exportTemplate")
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载期初模板")
    public ApiResult<Object> exportTrackTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "excel/platformInitStock.xlsx";
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
     *  导入期初
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "第三方仓流水差异表导入")
    @PostMapping(value = "/importExcel")
    public ApiResult<Boolean> importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response)  throws Exception{
        return success(adsErpInventoryDiffFlowService.importExcel(excelFile , response));
    }


}
