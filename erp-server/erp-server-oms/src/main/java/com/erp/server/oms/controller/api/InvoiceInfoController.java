package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.server.oms.service.InvoiceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 上传记录
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@RestController
@LogSystemModule("上传记录")
@RequestMapping("/invoiceInfo")
public class InvoiceInfoController extends BaseController {

    @Resource
    private InvoiceInfoService invoiceInfoService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "上传记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated InvoiceInfoDTO.AddDTO dto) {
        return success(invoiceInfoService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "上传记录修改")
    public ApiResult<?> update(@RequestBody @Validated InvoiceInfoDTO.UpdateDTO dto) {
        invoiceInfoService.update(dto);
        return success();
    }

    /**
     * 上传记录分页查询
     *
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<InvoiceInfoDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto) {
        PagingVO<InvoiceInfoDTO.PagingViewDTO> pagingVO = invoiceInfoService.paging(dto, false);
        return success(pagingVO);
    }


    /**
     * 下载发票
     * 返回下载地址
     */
    @PostMapping("/downloadInvoice")
    public ApiResult<String> downloadInvoice(@RequestBody @Validated BaseIdDTO dto) {
        return success(invoiceInfoService.downloadInvoice(dto.getId()));
    }

    /**
     * 生成发票
     * 传参销售订单ids
     */
    @PostMapping("/generateInvoice")
    public ApiResult<List<BatchResultDTO>> generateInvoice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = invoiceInfoService.batchGenerateInvoice(dto.getIds());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 上传发票
     */
    @PostMapping("/uploadInvoice")
    public ApiResult<List<BatchResultDTO>> uploadInvoice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = invoiceInfoService.batchUploadInvoice(dto.getIds());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @param dto
     * @return
     */
    @PostMapping("/export")
    public ApiResult<Object> export(@RequestBody @Valid InvoiceInfoDTO.PagingParamDTO dto) {
        Boolean result = invoiceInfoService.export(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 导出发票xml(返回url下载)
     * @author will
     * @date 2025/4/8 09:39
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/exportXml")
    public ApiResult<String> exportXml(@RequestBody @Valid InvoiceInfoDTO.PagingParamDTO dto) {
        String xmlUrl = invoiceInfoService.exportXml(dto);
        return success(xmlUrl);
    }

    /**
     * 导出发票pdf(返回url下载)
     * @author will
     * @date 2025/4/8 09:39
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/exportPdf")
    public ApiResult<String> exportPdf(@RequestBody @Valid InvoiceInfoDTO.PagingParamDTO dto) {
        String pdfUrl = invoiceInfoService.exportPdf(dto);
        return success(pdfUrl);
    }

    /**
     * 开具Cce数据回显
     * @author will
     * @date 2025/4/8 09:37
     * @param id
     * @return ApiResult<ViewCceDTO>
     */
    @GetMapping("/viewCce")
    @LogViewService
    public ApiResult<InvoiceInfoDTO.ViewCceDTO> viewCce(@RequestParam(value = "id") String id) {
        return success(invoiceInfoService.viewCce(id));
    }

    /**
     * 开局Cce
     * @author will
     * @date 2025/4/8 09:31
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping("/updateCce")
    public ApiResult<BatchResultDTO> updateCce(@RequestBody @Valid InvoiceInfoDTO.UpdateCceDTO dto) {
        return success(invoiceInfoService.updateCce(dto));
    }

    /**
     * 取消发票
     * @author will
     * @date 2025/4/7 18:40
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/cancelInvoice")
    public ApiResult<BatchResultDTO> cancelInvoice(@RequestBody @Validated InvoiceInfoDTO.RemarkDTO dto) {
        return success(invoiceInfoService.cancelInvoice(dto.getId(),dto.getRemark()));
    }

    /**
     * 退票
     * @author will
     * @date 2025/4/7 18:40
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/returnInvoice")
    public ApiResult<BatchResultDTO> returnInvoice(@RequestBody @Validated InvoiceInfoDTO.RemarkDTO dto) {
        return success(invoiceInfoService.returnInvoice(dto.getId(),dto.getRemark()));
    }

    /**
     * 无需开票
     * @author will
     * @date 2025/4/7 18:40
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/notNeedInvoice")
    public ApiResult<BatchResultDTO> notNeedInvoice(@RequestBody @Validated InvoiceInfoDTO.RemarkDTO dto) {
        return success(invoiceInfoService.notNeedInvoice(dto.getId(),dto.getRemark()));
    }
}
