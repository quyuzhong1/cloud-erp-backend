package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;

/**
 * 退货订单服务类
 */
public interface DmpReturnOrderInfoService extends IService<DmpReturnOrderInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 16:17
     * @param dto
     * @return PagingVO<DmpReturnOrderInfoDTO>
     */
    PagingVO<DmpReturnOrderInfoDTO> paging(PagingDTO<DmpReturnOrderInfoSearchDTO> dto);
}
