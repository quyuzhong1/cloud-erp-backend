package com.erp.server.oms.controller.api;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.oms.service.InvoiceInfoService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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

    @Resource
    private SoB2cService soB2cService;

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
                resultDTO = invoiceInfoService.batchGenerateNfeInvoice(id,Boolean.TRUE);
            }catch (Exception e){
                log.error("生成Nfe发票失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "b2c订单不存在, 生成Nfe发票失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
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
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = invoiceInfoService.batchUploadInvoice(id);
            }catch (Exception e){
                log.error("上传发票失败",e);
                InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getById(id);
                if (ObjectUtil.isEmpty(invoiceInfoEntity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "开票清单不存在, 上传发票失败");
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
    public ResponseEntity<StreamingResponseBody> exportXml(@RequestBody @Valid InvoiceInfoDTO.PagingParamDTO dto) {
        InvoiceInfoDTO.ExportResultDTO resultDTO = invoiceInfoService.exportXml(dto);
        // 编码文件名（兼容所有Java版本）
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(resultDTO.getFileName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("编码失败");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resultDTO.getResponseBody());
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
    public ResponseEntity<StreamingResponseBody> exportPdf(@RequestBody @Valid InvoiceInfoDTO.PagingParamDTO dto) {
        InvoiceInfoDTO.ExportResultDTO resultDTO = invoiceInfoService.exportPdf(dto);
        // 编码文件名（兼容所有Java版本）
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(resultDTO.getFileName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("编码失败");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resultDTO.getResponseBody());
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
    public ApiResult<List<BatchResultDTO>> notNeedInvoice(@RequestBody @Validated InvoiceInfoDTO.NoNeedInvoiceDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getSoIdList().size());
        for (String id : dto.getSoIdList()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = invoiceInfoService.notNeedInvoice(id,dto.getRemark(),dto.getInvoiceType());
            }catch (Exception e){
                log.error("无需开票失败",e);
                SoB2cEntity soB2cEntity = soB2cService.getById(id);
                if (ObjUtil.isEmpty(soB2cEntity)) {
                    resultDTO = BatchResultDTO.fail(id, soB2cEntity.getCode(), "销售订单不存在, 无需开票失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(id, soB2cEntity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 初始化处理nfe发票密钥数据
     * @return
     */
    @PostMapping("/initNfeInvoiceKey")
    public ApiResult  initNfeInvoiceKey() {
        invoiceInfoService.initNfeInvoiceKey();
        return success();
    }
}
