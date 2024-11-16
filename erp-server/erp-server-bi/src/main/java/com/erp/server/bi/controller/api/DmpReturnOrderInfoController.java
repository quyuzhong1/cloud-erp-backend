package com.erp.server.bi.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.bi.service.BiOrderInfoService;
import com.erp.server.bi.service.BiReturnOrderInfoService;
import com.erp.server.bi.service.BiReturnOrderItemService;
import com.erp.server.bi.service.BiShopInfoService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 数据源管理
 * @author Will
 * @version 1.0

 * @date 2022/12/13 15:15
 */
@RestController
@LogSystemModule("数据源管理")
@RequestMapping("dmpReturnOrderInfo")
public class DmpReturnOrderInfoController extends BaseController {

    @Resource
    private BiOrderInfoService biOrderInfoService;

    @Resource
    private BiReturnOrderInfoService biReturnOrderInfoService;

    @Resource
    private BiReturnOrderItemService biReturnOrderItemService;

    @Resource
    private BiShopInfoService biShopInfoService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 退货数据-分页查询
     *
     * @return 查询结果
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dmpReturnOrderInfo:paging", tableAlias = "droi")
    public ApiResult<PagingVO<DmpReturnOrderInfoDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        PagingVO<DmpReturnOrderInfoDTO> pagingVO = biReturnOrderInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     *  退货数据-导出
     * @author Will
     * @date: 2022/12/15 11:45
     * @param dto
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "退货数据导出")
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dmpReturnOrderInfo:paging", tableAlias = "droi")
    public ApiResult<Void> exportExcel(@RequestBody DmpReturnOrderInfoSearchDTO dto, HttpServletResponse response) {
        biReturnOrderInfoService.exportExcel(dto, response);
        return success();
    }

    /**
     * 退货数据-导入
     * @author Will
     * @date: 2022/12/16 11:10
     * @param excelFile
     * @param importType
     * @param response
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "退货数据导入")
    @PostMapping("/importReturnOrderFile")
    public ApiResult<Object> importReturnOrderFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, HttpServletResponse response) {
        boolean flag = biReturnOrderInfoService.importOrderFile(excelFile, importType, response);
        return flag ? this.success() : this.failure();
    }


    /**
     * 退货数据-下载模板
     * @author Will
     * @date: 2022/12/15 18:42
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "退货数据下载模板")
    @GetMapping("/exportTemplate")
    public ApiResult<Void> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/dmpReturnOrderInfoTemplate.xlsx";
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
