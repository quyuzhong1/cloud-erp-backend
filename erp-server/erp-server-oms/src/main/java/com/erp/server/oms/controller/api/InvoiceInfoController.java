package com.erp.server.oms.controller.api;


import cn.hutool.core.collection.CollUtil;
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
import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.erp.server.oms.service.InvoiceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
     * 生成Vat发票
     * 传参销售订单ids
     */
    @PostMapping("/generateVatInvoice")
    public ApiResult<List<BatchResultDTO>> generateVatInvoice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = invoiceInfoService.batchGenerateVatInvoice(dto.getIds());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 生成Nfe发票
     * @author will 
     * @date 2025/4/9 11:46
     * @param dto 
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/generateNfeInvoice")
    public ApiResult<List<BatchResultDTO>> generateNfeInvoice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = invoiceInfoService.batchGenerateNfeInvoice(id);
            }catch (Exception e){
                log.error("生成Nfe发票失败",e);
                List<InvoiceInfoEntity> entityList = invoiceInfoService.listBySoIds(Collections.singletonList(id));
                if (CollUtil.isEmpty(entityList)) {
                    resultDTO = BatchResultDTO.fail(id, id, "生成Nfe发票不存在, 生成Nfe发票失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(id, id, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 生成发票校验
     * @author will
     * @date 2025/4/8 14:22
     * @param dto
     * @return ApiResult<List<ViewDTO>>
     */
    @PostMapping("/checkGenerateInvoice")
    public ApiResult<List<InvoiceTaxDTO.CheckGenerateInvoiceDTO>> checkGenerateInvoice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<InvoiceTaxDTO.CheckGenerateInvoiceDTO> list = invoiceInfoService.checkGenerateInvoice(dto.getIds());
        return  success(list);
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
    @WebAdvanceQuery
    public ApiResult<ResponseEntity<StreamingResponseBody>> exportXml(@RequestBody @Valid InvoiceInfoDTO.PagingParamDTO dto) {
        StreamingResponseBody responseBody = invoiceInfoService.exportXml(dto);
        String fileName = "attachments_" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + ".zip";
        ResponseEntity<StreamingResponseBody> body = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
        return success(body);
    }

    /**
     * 导出发票pdf(返回url下载)
     * @author will
     * @date 2025/4/8 09:39
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/exportPdf")
    @WebAdvanceQuery
    public ApiResult<Resource> exportPdf(@RequestBody @Valid InvoiceInfoDTO.PagingParamDTO dto) {
        return success(invoiceInfoService.exportPdf(dto));
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
    public ApiResult<BatchResultDTO> returnInvoice(@RequestBody @Validated InvoiceInfoDTO.ReturnRemarkDTO dto) {
        return success(invoiceInfoService.returnInvoice(dto.getId(),dto.getRemark(),dto.getReturnTaxCode()));
    }

    /**
     * 无需开票
     * @author will
     * @date 2025/4/7 18:40
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/notNeedInvoice")
    public ApiResult<BatchResultDTO> notNeedInvoice(@RequestBody @Validated InvoiceInfoDTO.SoRemarkDTO dto) {
        return success(invoiceInfoService.notNeedInvoice(dto.getSoId(),dto.getRemark()));
    }
}
