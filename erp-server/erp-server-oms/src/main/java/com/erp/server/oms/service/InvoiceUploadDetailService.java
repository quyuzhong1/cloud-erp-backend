package com.erp.server.oms.service;
import com.erp.model.oms.entity.InvoiceUploadDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.InvoiceUploadDetailDTO;

import java.util.List;

/**
 * <p>
 * 上传记录订单明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
public interface InvoiceUploadDetailService extends SuperService<InvoiceUploadDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InvoiceUploadDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    Boolean update(InvoiceUploadDetailDTO.UpdateDTO dto);

    /**
     * 批量新增
     * @param id
     * @param detailList
     */
    void batchAdd(String id, List<InvoiceUploadDetailDTO.AddDTO> detailList);

    /**
     * 批量更新
     * @param id
     * @param detailList
     */
    void batchUpdate(String id, List<InvoiceUploadDetailDTO.UpdateDTO> detailList);
}
