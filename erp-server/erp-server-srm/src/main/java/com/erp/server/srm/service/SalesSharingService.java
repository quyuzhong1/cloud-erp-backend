package com.erp.server.srm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.entity.SalesSharingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.SalesSharingDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 销量共享表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-18
 */
public interface SalesSharingService extends SuperService<SalesSharingEntity> {


    PagingVO<SalesSharingDTO.ListDTO> paging(PagingDTO<SalesSharingDTO.PagingParamDTO> pagingParamDTO);

    String exportList(SalesSharingDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response);

    String getNoticeContent();

    void calSalesSharing();
}
