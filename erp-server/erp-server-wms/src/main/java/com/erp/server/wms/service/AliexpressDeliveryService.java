package com.erp.server.wms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 速卖通发货单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-26
 */
public interface AliexpressDeliveryService extends SuperService<AliexpressDeliveryEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    void add(AliexpressDeliveryDTO.AddDTO dto);

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2024/1/26 16:33
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.AliexpressDeliveryDTO.ListDTO>
     **/
    PagingVO<AliexpressDeliveryDTO.ListDTO> paging(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto);

    /**
     * 导出excel
     * @Author Luo_WG
     * @Date 2024/1/26 16:51
     * @param dto
     * @param response
     * @return java.lang.Boolean
     **/
    Boolean exportExcel(AliexpressDeliveryDTO.SearchParamDTO dto, HttpServletResponse response);
}
