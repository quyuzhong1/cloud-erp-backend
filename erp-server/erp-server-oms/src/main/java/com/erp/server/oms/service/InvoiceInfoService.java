package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.model.oms.entity.InvoiceDetailEntity;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

/**
 * <p>
 * 上传记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
public interface InvoiceInfoService extends SuperService<InvoiceInfoEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InvoiceInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    Boolean update(InvoiceInfoDTO.UpdateDTO dto);

    /**
     * 分页查询
     *
     * @param dto
     * @param isExport
     * @return
     */
    PagingVO<InvoiceInfoDTO.PagingViewDTO> paging(PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto, Boolean isExport);

    String downloadInvoice(String id);
    /**
     * 生成发票
     * @author will
     * @date 2025/4/9 11:43
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO batchGenerateNfeInvoice(String id,Boolean isAsync);

    List<BatchResultDTO> batchGenerateVatInvoice(List<String> ids);

    void batchSave(List<InvoiceInfoEntity> addList, List<InvoiceDetailEntity> addDetailList);

    BatchResultDTO batchUploadInvoice(String id);
    /**
     * 上传nfe发票
     * @author will
     * @date 2025/4/21 09:45
     * @param soB2cEntity
     * @return void
     */
    BatchResultDTO uploadNfeInvoice (SoB2cEntity soB2cEntity,String invoiceId);

    Boolean export(InvoiceInfoDTO.PagingParamDTO dto);

    List<InvoiceInfoEntity> listBySoIds(List<String> soIds);

    void retryInvoice();

    void queryUploadingInvoice() throws Exception;
    /**
     * 取消发票
     * @author will
     * @date 2025/4/7 18:41
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelInvoice(String id,String remark);
    /**
     * 退票
     * @author will
     * @date 2025/4/7 18:42
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO returnInvoice(String id,String remark,String returnTaxCode);
    /**
     * 无需开票
     * @author will 
     * @date 2025/4/8 09:04
     * @param soId
     * @param remark 
     * @return BatchResultDTO
     */
    BatchResultDTO notNeedInvoice( String soId, String remark,String invoiceType);
    /**
     * 开局Cce
     * @author will
     * @date 2025/4/8 09:32
     * @param dto
     * @return BatchResultDTO
     */
    BatchResultDTO updateCce(InvoiceInfoDTO.UpdateCceDTO dto);
    /**
     * 开局Cce数据回显
     * @author will
     * @date 2025/4/8 09:37
     * @param id
     * @return ViewCceDTO
     */
    InvoiceInfoDTO.ViewCceDTO viewCce(String id);
    /**
     * 导出xml
     * @author will
     * @date 2025/4/8 11:30
     * @param dto
     * @return Resource
     */
    InvoiceInfoDTO.ExportResultDTO exportXml(InvoiceInfoDTO.PagingParamDTO dto);
    /**
     * 导出pdf
     * @author will
     * @date 2025/4/8 11:30
     * @param dto
     * @return Resource
     */
    InvoiceInfoDTO.ExportResultDTO exportPdf(InvoiceInfoDTO.PagingParamDTO dto);
    /**
     * 生成发票校验
     * @author will
     * @date 2025/4/8 14:22
     * @param ids
     * @return List<CheckGenerateInvoiceDTO>
     */
    List<InvoiceTaxDTO.CheckGenerateInvoiceDTO> checkGenerateInvoice(List<String> ids);

    /**
     * 压缩zip
     * @author will
     * @date 2025/4/10 11:07
     * @param exportAttachList
     * @return StreamingResponseBody
     */
    StreamingResponseBody downloadZip (List<InvoiceInfoDTO.ExportAttachDTO> exportAttachList);

    /**
     *  删除开票失败的数据
     * @author will
     * @date 2025/4/10 19:15
     * @param soId
     * @param invoiceTypeList
     * @return void
     */
    void removeFailedBySoId (String soId,List<String> invoiceTypeList);
    /**
     * 查询开票中发票
     * @author will
     * @date 2025/4/11 16:24
     * @param id
     * @return InvoiceInfoEntity
     */
    InvoiceInfoEntity getInvoicingBySoId(String id);
    /**
     * 更新nfe发票状态
     * @author will
     * @date 2025/4/11 16:30
     * @param invoiceInfoEntity
     * @return void
     */
    void updateNfeStatusById(InvoiceInfoEntity invoiceInfoEntity);
    /**
     * 根据soId查询最新的attach
     * @author will
     * @date 2025/4/14 17:59
     * @param soId
     * @return InvoiceInfoDTO.AttachDTO
     */
    InvoiceInfoDTO.AttachDTO getNewInvoicedAttachBySoId(String soId,String invoiceType,String attachmentType);

    void initNfeInvoiceKey();
}
