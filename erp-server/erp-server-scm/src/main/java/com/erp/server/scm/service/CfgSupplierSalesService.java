package com.erp.server.scm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.entity.CfgSupplierSalesEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

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
    BaseResultDTO.AddDTO add(CfgSupplierSalesDTO.CommonDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-06-13
    * @param dto
    * @return
    */
    Boolean update(CfgSupplierSalesDTO.CommonDTO dto);


    PagingVO<CfgSupplierSalesDTO.ListDTO> paging(PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> pagingParamDTO);

    BatchResultDTO delete(String id);

    BatchResultDTO enable(String id, Boolean disabled);

    void exportList(CfgSupplierSalesDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response);

    CfgSupplierSalesDTO.ViewDTO view(String id);

    List<CfgSupplierSalesDTO.ListAllDTO> listAll();

    List<String> getDisplayField();
}
