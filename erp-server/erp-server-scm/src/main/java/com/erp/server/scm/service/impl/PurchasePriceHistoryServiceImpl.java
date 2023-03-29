package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceHistoryEntity;
import com.erp.server.scm.mapper.PurchasePriceHistoryMapper;
import com.erp.server.scm.service.PurchasePriceHistoryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-28
 */
@Service
public class PurchasePriceHistoryServiceImpl extends SuperServiceImpl<PurchasePriceHistoryMapper, PurchasePriceHistoryEntity> implements PurchasePriceHistoryService {

    @Override
    public List<PurchasePriceDetailDTO.HistoryDTO> getHistory(String priceDetailId) {
        List<PurchasePriceHistoryEntity> list = this.getByPriceDetailId(priceDetailId);
        BigDecimal hundred = new BigDecimal("100");
        List<PurchasePriceDetailDTO.HistoryDTO> resultList = BeanMapper.copyList(list, PurchasePriceDetailDTO.HistoryDTO.class);
        for (PurchasePriceDetailDTO.HistoryDTO item : resultList) {
            BigDecimal taxRate = item.getTaxRate();
            item.setTaxRate(taxRate.multiply(hundred));
        }
        return resultList;
    }


    private List<PurchasePriceHistoryEntity> getByPriceDetailId(String priceDetailId) {
        LambdaQueryWrapper<PurchasePriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceHistoryEntity::getPriceDetailId, priceDetailId);
        queryWrapper.orderByDesc(PurchasePriceHistoryEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}
