package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.bi.dto.TargetSaleDTO;
import com.erp.model.bi.vo.TargetSaleCountVO;
import com.erp.model.bi.vo.TargetSaleSumVO;
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
    TargetSaleSumVO sumSales(TargetSaleDTO dto);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 15:49
     * @param dto
     * @return PagingVO<DmpRefundInfoDTO>
     */
    PagingVO<DmpOrderInfoDTO> paging(PagingDTO<DmpOrderInfoSearchDTO> dto);

    /**
     * 统计销量
     * @param dto
     * @return
     */
    TargetSaleCountVO countSalesVolume(TargetSaleDTO dto);

    /**
     * 统计订单销量
     * @param dto
     * @return
     */
    TargetSaleCountVO countOrderQuantity(TargetSaleDTO dto);

    /**
     * 统计订单销量
     * @param dto
     * @return
     */
    TargetSaleSumVO countRefundRate(TargetSaleDTO dto);

    /**
     * 统计退款金额
     * @param dto
     * @return
     */
    TargetSaleSumVO countRefundAmount(TargetSaleDTO dto);

    /**
     * 统计退款订单数量
     * @param dto
     * @return
     */
    TargetSaleCountVO countRefundOrderNum(TargetSaleDTO dto);

    /**
     * 客单价统计
     * @param dto
     * @return
     */
    TargetSaleSumVO statisticsCustomerPrice(TargetSaleDTO dto);

    /**
     * 国内销售占比统计
     * @param dto
     * @return
     */
    TargetSaleSumVO statisticsDomesticSalesRatio(TargetSaleDTO dto);
}
