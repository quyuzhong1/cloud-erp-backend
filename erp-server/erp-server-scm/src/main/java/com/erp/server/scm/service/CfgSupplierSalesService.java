package com.erp.server.scm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.entity.CfgSupplierSalesEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;

/**
 * <p>
 * 销量设置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
public interface CfgSupplierSalesService extends SuperService<CfgSupplierSalesEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-06-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgSupplierSalesDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-06-13
    * @param dto
    * @return
    */
    Boolean update(CfgSupplierSalesDTO.UpdateDTO dto);


    PagingVO<CfgSupplierSalesDTO.ListDTO> paging(PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> pagingParamDTO);
}
