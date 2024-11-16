package com.erp.server.srm.controller.api;


import com.common.business.annotation.DataIdempotent;
import com.common.business.annotation.Idempotent;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.query.IQueryHandler;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.server.srm.query.DeliveryOrderQueryHandler;
import com.erp.server.srm.service.CommonService;
import com.erp.server.srm.service.DeliveryOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
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
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 送货单
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@RestController
@LogSystemModule("送货单")
@RequestMapping("/deliveryOrder")
public class DeliveryOrderController extends BaseController {

    @Resource
    private DeliveryOrderService deliveryOrderService;

    @Resource
    private CommonService commonService;

    /**
     * 获取 tab列表
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<DeliveryOrderDTO.TabListDTO>> tabList() {
        List<DeliveryOrderDTO.TabListDTO> tabList = deliveryOrderService.tabList(Collections.singletonList(commonService.getSupplierEntity().getId()));
        return success(tabList);
    }

    /**
     * 详情
     *
     * @param
     * @return
     */
    @LogViewService
    @GetMapping("/view")
    public ApiResult<DeliveryOrderDTO.ViewDTO> view(@Param("id") String id) {
        DeliveryOrderDTO.ViewDTO view = deliveryOrderService.view(id);
        return success(view);
    }

    /**
     * 根据code获取详情
     * @param
     * @return
     */
    @LogViewService
    @GetMapping("/viewByCode")
    public ApiResult<DeliveryOrderDTO.ViewDTO> viewByCode(@Param("code") String code) {
        DeliveryOrderDTO.ViewDTO view = deliveryOrderService.viewByCode(code);
        return success(view);
    }

    /**
     * 分页
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = DeliveryOrderQueryHandler.class)
    public ApiResult<PagingVO<DeliveryOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        dto.getParams().setSupplierIdList(Collections.singletonList(commonService.getSupplierEntity().getId()));
        return success(deliveryOrderService.paging(dto));
    }

    /**
     * 合计
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     */
    @PostMapping("/pagingTotal")
    @WebAdvanceQuery(handler = DeliveryOrderQueryHandler.class)
    public ApiResult<DeliveryOrderDTO.TotalInfo> pagingTotal(@RequestBody @Validated DeliveryOrderDTO.ParamDTO dto) {
        dto.setSupplierIdList(Collections.singletonList(commonService.getSupplierEntity().getId()));
        return success(deliveryOrderService.pagingTotal(dto));
    }

    /**
    * 新增
    * @author lrp
    * @date:  2024-01-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "送货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DeliveryOrderDTO.AddDTO dto) {
        return success(deliveryOrderService.add(dto));
    }

    /**
    * 编辑
    * @author lrp
    * @date:  2024-01-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "送货单编辑")
    @DataIdempotent(keyIdName = "dto.id")
    public ApiResult<?> update(@RequestBody @Validated DeliveryOrderDTO.UpdateDTO dto) {
        deliveryOrderService.update(dto);
        return success();
    }

    /**
     * 打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/print")
    public ApiResult<List<DeliveryOrderDTO.PrintDTO>> print(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(deliveryOrderService.print(dto.getIds()));
    }

    /**
     * 确认打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/confirmPrint")
    public ApiResult<?> confirmPrint(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(deliveryOrderService.confirmPrint(dto.getIds()));
    }

    /**
     * 取消打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelPrint")
    public ApiResult<List<BatchResultDTO>> cancelPrint(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> batchResultDTOList = deliveryOrderService.cancelPrint(dto.getIds());
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess)?success(batchResultDTOList):failure(batchResultDTOList);
    }

    /**
     * 删除送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除送货单")
    public ApiResult<?> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(deliveryOrderService.delete(dto.getIds()));
    }

    /**
     * 导入模板-下载
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "发货单下载模板")
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "excel/srmDeliveryTemplate.xlsx";
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

    /**
     * 导入
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "发货单导入")
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = deliveryOrderService.importExcel(excelFile, response);
        return result?success():failure();
    }
}
