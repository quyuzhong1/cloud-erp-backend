package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceHistoryEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchasePriceHistoryMapper;
import com.erp.server.scm.service.PurchasePriceHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Resource
    private SysUserFeign sysUserFeign;


    @Override
    public List<PurchasePriceDetailDTO.ViewDTO> getHistory(String priceDetailId) {
        List<PurchasePriceHistoryEntity> list = this.getByPriceDetailId(priceDetailId);
        BigDecimal hundred = new BigDecimal("100");
        List<PurchasePriceDetailDTO.ViewDTO> resultList = BeanMapper.copyList(list, PurchasePriceDetailDTO.ViewDTO.class);
        List<String> currencyIdList = resultList.stream().map(PurchasePriceDetailDTO.ViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        for (PurchasePriceDetailDTO.ViewDTO item : resultList) {
            //币种
            String currency = item.getCurrency();
            BigDecimal taxRate = item.getTaxRate();
            item.setTaxRate(taxRate.multiply(hundred));
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            item.setCurrencySymbol(currencySymbol);
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
