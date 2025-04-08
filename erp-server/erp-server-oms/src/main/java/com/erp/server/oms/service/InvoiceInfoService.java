package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.model.oms.entity.InvoiceDetailEntity;
import com.erp.model.oms.entity.InvoiceInfoEntity;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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

    List<BatchResultDTO> batchGenerateInvoice(List<String> ids);

    void batchSave(List<InvoiceInfoEntity> addList, List<InvoiceDetailEntity> addDetailList);

    List<BatchResultDTO> batchUploadInvoice(List<String> ids);

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
    BatchResultDTO returnInvoice(String id,String remark);
    /**
     * 无需开票
     * @author will 
     * @date 2025/4/8 09:04
     * @param id
     * @param remark 
     * @return BatchResultDTO
     */
    BatchResultDTO notNeedInvoice(@NotBlank(message = "id不能为空") String id, @NotBlank(message = "备注不能为空") String remark);
    /**
     * 开局Cce
     * @author will
     * @date 2025/4/8 09:32
     * @param dto
     * @return BatchResultDTO
     */
    BatchResultDTO updateCce(InvoiceInfoDTO.@Valid UpdateCceDTO dto);
    /**
     * 开局Cce数据回显
     * @author will
     * @date 2025/4/8 09:37
     * @param id
     * @return ViewCceDTO
     */
    InvoiceInfoDTO.ViewCceDTO viewCce(String id);

    String exportXml(InvoiceInfoDTO.@Valid PagingParamDTO dto);

    String exportPdf(InvoiceInfoDTO.@Valid PagingParamDTO dto);
}
