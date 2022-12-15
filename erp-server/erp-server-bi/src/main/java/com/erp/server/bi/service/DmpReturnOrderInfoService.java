package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.bi.dto.TargetSaleDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 汇总退货金额 根据sku和订单号
     * @param orderIds
     * @param sku
     * @return
     */
    BigDecimal sumRefundAmount(List<String> orderIds, TargetSaleDTO sku);

    /**
     * @description: 导出
     * @author Will
     * @date: 2022/12/15 10:48
     * @param dto
     * @param response
     */
    void exportExcel(DmpReturnOrderInfoSearchDTO dto, HttpServletResponse response);
}
