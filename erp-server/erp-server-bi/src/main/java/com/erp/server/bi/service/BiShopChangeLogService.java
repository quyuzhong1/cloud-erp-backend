package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.dmp.dto.DmpShopChangeLogDTO;
import com.erp.model.dmp.entity.BiShopChangeLogEntity;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/15 14:30
 */
public interface BiShopChangeLogService extends IService<BiShopChangeLogEntity> {
    PagingVO<DmpShopChangeLogDTO> paging(PagingDTO<AdvanceSearchDTO> dto);
}
