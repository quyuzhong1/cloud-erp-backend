package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.bi.dto.IndicatorSaleDTO;
import com.erp.model.bi.vo.IndicatorSaleSumVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;

/**
 * 订单服务类
 */
public interface DmpOrderInfoService extends IService<DmpOrderInfoEntity> {

    /**
     * 统计销售金额
     * @param dto
     * @return
     */
    IndicatorSaleSumVO countSales(IndicatorSaleDTO dto);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 15:49
     * @param dto
     * @return PagingVO<DmpRefundInfoDTO>
     */
    PagingVO<DmpOrderInfoDTO> paging(PagingDTO<DmpReturnOrderInfoSearchDTO> dto);
}
