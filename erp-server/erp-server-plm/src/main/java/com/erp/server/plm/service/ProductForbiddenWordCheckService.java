package com.erp.server.plm.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductForbiddenWordCheckDTO;
import com.erp.model.plm.entity.ProductForbiddenWordCheckEntity;

/**
 * <p>
 * 产品违禁词检测记录 服务类
 * </p>
 */
public interface ProductForbiddenWordCheckService extends SuperService<ProductForbiddenWordCheckEntity> {

    PagingVO<ProductForbiddenWordCheckDTO.ListDTO> paging(PagingDTO<ProductForbiddenWordCheckDTO.PagingParamDTO> dto);

    ProductForbiddenWordCheckDTO.DetectDTO detect();

    BatchResultDTO delete(String id);

    String downloadReport(String id);

    void executeDetect(String id);
}
