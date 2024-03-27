package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.erp.server.tms.query.TmsB2cDeclareReconciliationDetailQueryHandler;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;

/**
 * b2c报关对账单明细
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@RestController
@LogSystemModule("b2c报关对账单明细")
@RequestMapping("/tmsB2cDeclareReconciliationDetail")
public class TmsB2cDeclareReconciliationDetailController extends BaseController {

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;

    /**
     * 分页查询
     * @author Will
     * @date: 2024/3/27 14:18
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:paging",
            tableAlias = "tbdr"
    )
    @WebAdvanceQuery(handler = TmsB2cDeclareReconciliationDetailQueryHandler.class)
    public ApiResult<PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsB2cDeclareReconciliationDetailDTO.PagingParamDTO> dto) {
        return success(tmsB2cDeclareReconciliationDetailService.paging(dto));
    }

    /**
    * 更新对账状态
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.INSERT, desc = "更新对账状态")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated TmsB2cDeclareReconciliationDetailDTO.UpdateStatusDTO dto) {

        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = tmsB2cDeclareReconciliationDetailService.updateStatus(id,dto.getStatus());
            }catch (Exception e){
                log.error("b2c报关对账单明细 更新对账状态失败",e);
                TmsB2cDeclareReconciliationDetailEntity entity = tmsB2cDeclareReconciliationDetailService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "b2c报关对账单明细不存在, 更新对账状态失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getSoCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下载模板
     * @author Will
     * @date: 22024/3/27 18:22
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载报关对账单模板")
    @GetMapping("/exportExcelTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/declareReconciliationDetailTemplate.xlsx";
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
     * 导入对账单
     * @author Will
     * @date: 2024/3/27 11:30
     * @param excelImportDTO
     * @param response
     * @return ApiResult<ImportDTO>
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入对账单明细")
    @PostMapping("/importFile")
    public ApiResult<TmsB2cDeclareReconciliationDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated TmsB2cDeclareReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        TmsB2cDeclareReconciliationDetailDTO.ImportDTO importDTO = tmsB2cDeclareReconciliationDetailService.importFile(excelImportDTO, response);
        return success(importDTO);
    }
}
