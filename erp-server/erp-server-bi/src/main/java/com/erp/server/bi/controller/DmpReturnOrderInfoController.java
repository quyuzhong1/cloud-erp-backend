package com.erp.server.bi.controller;

import com.erp.common.business.annotation.DataPermission;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.erp.common.business.enums.DataAttributeEnum;
import com.common.core.exception.ServiceException;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderItemService;
import com.erp.server.bi.service.DmpShopInfoService;
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

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:15
 */
@RestController
@RequestMapping("bi/dmpReturnOrderInfo")
public class DmpReturnOrderInfoController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;

    @Resource
    private DmpReturnOrderItemService dmpReturnOrderItemService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

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
        PagingVO<DmpReturnOrderInfoDTO> pagingVO = dmpReturnOrderInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     *  退货数据-导出
     * @author Will
     * @date: 2022/12/15 11:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dmpReturnOrderInfo:paging", tableAlias = "droi")
    public ApiResult exportExcel(@RequestBody DmpReturnOrderInfoSearchDTO dto, HttpServletResponse response) {
        dmpReturnOrderInfoService.exportExcel(dto, response);
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
    @PostMapping("/importReturnOrderFile")
    public ApiResult importReturnOrderFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, HttpServletResponse response) {
        Boolean flag = dmpReturnOrderInfoService.importOrderFile(excelFile, importType, response);
        return flag == true ? this.success() : this.failure();
    }


    /**
     * 退货数据-下载模板
     * @author Will
     * @date: 2022/12/15 18:42
     * @param request
     * @param response
     */
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.Default);
        }
        return success();
    }

}
