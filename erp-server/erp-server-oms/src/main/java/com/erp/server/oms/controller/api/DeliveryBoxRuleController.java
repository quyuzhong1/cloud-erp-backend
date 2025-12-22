package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogViewService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.server.oms.query.DeliveryBoxRuleQueryHandler;
import com.erp.server.oms.service.DeliveryBoxRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 
 *
 * @author wtr
 * @since 2025-11-24
 */
@Slf4j
@RestController
@LogSystemModule("发货箱规")
@RequestMapping("/deliveryBoxRule")
public class DeliveryBoxRuleController extends BaseController {

    @Resource
    private DeliveryBoxRuleService deliveryBoxRuleService;

    /**
    * 新增发货箱规
    * @author wtr
    * @date:  2025-11-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增发货箱规")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DeliveryBoxRuleDTO.AddDTO dto) {
        return success(deliveryBoxRuleService.add(dto));
    }

    /**
    * 修改发货箱规
    * @author wtr
    * @date:  2025-11-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改发货箱规")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:deliveryBoxRule:update",
        serviceClass = DeliveryBoxRuleService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DeliveryBoxRuleDTO.UpdateDTO dto) {
        return success(deliveryBoxRuleService.update(dto));
    }

    /**
     * 列表查询
     * @author wtr
     * @date: 2025-10-16
     * @param dto
     * @return ApiResult<PagingVO<AssetNoticeDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:deliveryBoxRule:paging",
            tableAlias = "dbr"
    )
    @WebAdvanceQuery(handler = DeliveryBoxRuleQueryHandler.class)
    public ApiResult<PagingVO<DeliveryBoxRuleDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DeliveryBoxRuleDTO.PagingParamDTO> dto) {
        return success(deliveryBoxRuleService.paging(dto));
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:deliveryBoxRule:view",
            serviceClass = DeliveryBoxRuleService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DeliveryBoxRuleDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(deliveryBoxRuleService.view(id));
    }

    /**
     * 导入发货箱规
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入发货箱规")
    @PostMapping("/importFile")
    public ApiResult importFile(@RequestBody BaseDTO.ImportDTO dto,HttpServletResponse response) {
        Boolean result = deliveryBoxRuleService.importFile(dto, response);
        return result ? success() : failure();
    }

    /**
     * 下载发货箱规模板
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载发货箱规模板")
    @GetMapping("/exportTemplate")
    public ApiResult<Object> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/deliveryBoxRuleTemplate.xlsx";
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
            throw new ServiceException(ApiError.ERROR_95131);
        }
        return success();
    }

    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:deliveryBoxRule:export",
            tableAlias = "dbr"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public ApiResult<Object> exportList(@RequestBody @Validated DeliveryBoxRuleDTO.ExportDTO dto, HttpServletResponse response) {
        deliveryBoxRuleService.exportList(dto, response);
        return success();
    }

    /**
     * 根据sku获取箱规
     * @param skuList
     * @return
     */
    @PostMapping("/listBoxRuleBySku")
    @LogViewService
    public ApiResult<List<DeliveryBoxRuleDTO.ListBoxRuleBySkuDTO>> listBoxRuleBySku(@RequestBody List<DeliveryBoxRuleDTO.SkuDTO> skuList) {
        return success(deliveryBoxRuleService.listBoxRuleBySku(skuList));
    }
}
