package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.DictInvoiceHsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.DictInvoiceHsDTO;

/**
 * <p>
 * 发票海关编码 服务类
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
public interface DictInvoiceHsService extends SuperService<DictInvoiceHsEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictInvoiceHsDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    Boolean update(DictInvoiceHsDTO.UpdateDTO dto);
    /**
     * 分页查询
     * @author will 
     * @date 2025/4/7 18:04
     * @param dto 
     * @return PagingVO<ListDTO>
     */
    PagingVO<DictInvoiceHsDTO.ListDTO> paging(PagingDTO<DictInvoiceHsDTO.PagingParamDTO> dto);
}
