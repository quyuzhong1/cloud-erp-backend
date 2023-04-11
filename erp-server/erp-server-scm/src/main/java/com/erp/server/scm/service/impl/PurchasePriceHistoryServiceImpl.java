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
import java.time.LocalDate;
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
    public List<PurchasePriceDetailDTO.HistoryDTO> getHistory(String priceDetailId) {
        List<PurchasePriceHistoryEntity> list = this.getByPriceDetailId(priceDetailId);
        BigDecimal hundred = new BigDecimal("100");
        List<PurchasePriceDetailDTO.HistoryDTO> resultList = BeanMapper.copyList(list, PurchasePriceDetailDTO.HistoryDTO.class);
        List<String> currencyIdList = resultList.stream().map(PurchasePriceDetailDTO.HistoryDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        for (PurchasePriceDetailDTO.HistoryDTO item : resultList) {
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

    @Override
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getHistoryTaxPrice(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto) {

        return baseMapper.getHistoryTaxPrice(dto);
    }


    /**
     * 根据供应商id 获取到对应的合sku 价格
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     * @author yl
     * @date 2023-04-11 14:55
     */
    @Override
    public List<PurchasePriceDetailDTO.AddDTO> getBySupplierId(String supplierId) {
        LambdaQueryWrapper<PurchasePriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceHistoryEntity::getSupplierId, supplierId);
        LocalDate now = LocalDate.now();
        queryWrapper.ge(PurchasePriceHistoryEntity::getEffectiveDate, now);
        queryWrapper.le(PurchasePriceHistoryEntity::getExpireDate, now);
        List<PurchasePriceHistoryEntity> list = this.list(queryWrapper);

        return BeanMapper.copyList(list,PurchasePriceDetailDTO.AddDTO.class);
    }


    private List<PurchasePriceHistoryEntity> getByPriceDetailId(String priceDetailId) {
        LambdaQueryWrapper<PurchasePriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceHistoryEntity::getPriceDetailId, priceDetailId);
        queryWrapper.orderByDesc(PurchasePriceHistoryEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}
