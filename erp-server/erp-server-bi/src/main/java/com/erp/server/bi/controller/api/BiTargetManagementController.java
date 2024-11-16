package com.erp.server.bi.controller.api;

import static com.alibaba.excel.EasyExcel.read;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.common.business.annotation.DataPermission;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.exception.ServiceException;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiTargetManagementImportExcelDTO;
import com.erp.model.bi.dto.BiTargetManagementShowDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.listener.BiTargetManagementExcelListener;
import com.erp.server.bi.service.BiTargetManagementService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * 目标管理
 * @author Will
 * @version 1.0

 * @date 2022/12/21 17:32
 */
@RestController
@LogSystemModule("目标管理")
@RequestMapping("targetManagement")
public class BiTargetManagementController extends BaseController {

    @Resource
    private BiTargetManagementService biTargetManagementService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 目标管理-分页查询
     * @author Will
     * @date: 2022/12/21 18:01
     * @param dto
     * @return ApiResult<PagingVO<BiTargetManagementShowDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
                    tableField = "create_user_id",
                    menuCode = "bi:targetManagement:paging",
                    tableAlias = "btm")
    public ApiResult<PagingVO<BiTargetManagementShowDTO>> queryByPage(@RequestBody @Validated PagingDTO<AdvanceSearchDTO> dto) {
        PagingVO<BiTargetManagementShowDTO> pagingVO = biTargetManagementService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 目标管理-删除
     * @author Will
     * @date: 2022/12/26 14:59
     * @param id
     * @return ApiResult
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除目标管理")
    public ApiResult<Void> delete(@RequestParam("id") String id) {
        biTargetManagementService.removeById(id);
        return success();
    }


    /**
     * 目标管理-导入
     * @author Will
     * @date: 2022/12/21 18:05
     * @param excelFile
     * @param response
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入目标管理")
    @PostMapping("/importOrderFile")
    public ApiResult<Void> importOrderFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        BiTargetManagementExcelListener excelListenerUtil = new BiTargetManagementExcelListener(biTargetManagementService, plmTaskFeign);
        try {
            read(excelFile.getInputStream(), BiTargetManagementImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<BiTargetManagementImportExcelDTO> list = excelListenerUtil.getDateList();
            if (list.size() > 0) {
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/biTargetManagement.xlsx";
                String name = "biTargetManagement";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                new ExcelPrintUtils().patchExport(list, response, sb.toString(),excelPath);
                return failure();
            }
        } catch (IOException e) {
            throw new ServiceException(ApiError.Default);
        }
        return success();
    }


    /**
     * 目标管理-下载模板
     * @author Will
     * @date: 2022/12/21 18:42
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "目标管理下载模板")
    @GetMapping("/exportTemplate")
    public ApiResult<Void> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/biTargetManagementTemplate.xlsx";
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
            throw new ServiceException(ApiError.Default);
        }
        return success();
    }

}
