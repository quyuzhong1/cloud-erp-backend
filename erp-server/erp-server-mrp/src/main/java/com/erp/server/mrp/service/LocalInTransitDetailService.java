package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.LocalInTransitDetailDTO;
import com.erp.model.mrp.entity.LocalInTransitDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.mrp.vo.LocalInTransitDetailVO;

/**
 * <p>
 * 本地在途明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
public interface LocalInTransitDetailService extends SuperService<LocalInTransitDetailEntity> {

    PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<LocalInTransitDetailDTO> params);
}
