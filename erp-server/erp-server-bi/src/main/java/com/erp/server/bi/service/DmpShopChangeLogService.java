package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.dmp.dto.DmpShopChangeLogDTO;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 14:30
 */
public interface DmpShopChangeLogService extends IService<DmpShopChangeLogEntity> {
    PagingVO<DmpShopChangeLogDTO> paging(PagingDTO<AdvanceSearchDTO> dto);
}
