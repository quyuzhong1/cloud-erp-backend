package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceHistoryEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchasePriceHistoryMapper;
import com.erp.server.scm.service.PurchasePriceHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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

    @Resource
    private PlmTaskFeign plmTaskFeign;


    @Override
    public List<PurchasePriceDetailDTO.HistoryDTO> getHistory(String priceDetailId) {
        List<PurchasePriceHistoryEntity> list = this.getByPriceDetailId(priceDetailId);
        BigDecimal hundred = new BigDecimal("100");
        List<PurchasePriceDetailDTO.HistoryDTO> resultList = BeanMapper.copyList(list, PurchasePriceDetailDTO.HistoryDTO.class);
        List<String> skuIdList = resultList.stream().map(PurchasePriceDetailDTO.HistoryDTO::getSkuId).collect(Collectors.toList());
        List<String> currencyIdList = resultList.stream().map(PurchasePriceDetailDTO.HistoryDTO::getCurrency).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        for (PurchasePriceDetailDTO.HistoryDTO item : resultList) {
            String skuId = item.getSkuId();
            //币种
            String currency = item.getCurrency();
            BigDecimal taxRate = item.getTaxRate();
            item.setTaxRate(taxRate.multiply(hundred));
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            item.setCurrencySymbol(currencySymbol);
            String skuName = skuList.stream().filter(s -> skuId.equals(s.getSkuId())).findFirst().
                    map(SkuVO::getSkuName).orElse(item.getProductName());
            item.setProductName(skuName);

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
    public List<PurchasePriceDetailDTO.AddDTO> getBySupplierId(String supplierId, List<String> skuIdList) {
        LambdaQueryWrapper<PurchasePriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceHistoryEntity::getSupplierId, supplierId);
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            queryWrapper.in(PurchasePriceHistoryEntity::getSkuId, skuIdList);
        }
        LocalDate now = LocalDate.now();
        queryWrapper.le(PurchasePriceHistoryEntity::getEffectiveDate, now);
        queryWrapper.ge(PurchasePriceHistoryEntity::getExpireDate, now);
        List<PurchasePriceHistoryEntity> list = this.list(queryWrapper);

        return BeanMapper.copyList(list, PurchasePriceDetailDTO.AddDTO.class);
    }

    @Override
    public List<PurchasePriceDetailDTO.AddDTO> listBySupplierId(List<String> supplierIds, List<String> skuIdList) {
        LambdaQueryWrapper<PurchasePriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PurchasePriceHistoryEntity::getSupplierId, supplierIds);
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            queryWrapper.in(PurchasePriceHistoryEntity::getSkuId, skuIdList);
        }
        LocalDate now = LocalDate.now();
        queryWrapper.le(PurchasePriceHistoryEntity::getEffectiveDate, now);
        queryWrapper.ge(PurchasePriceHistoryEntity::getExpireDate, now);
        List<PurchasePriceHistoryEntity> list = this.list(queryWrapper);

        return BeanMapper.copyList(list, PurchasePriceDetailDTO.AddDTO.class);
    }

    @Override
    public List<PurchasePriceHistoryEntity> getHistoryByDetailIds(List<String> purchasePriceDetailIds) {
        if (CollectionUtils.isEmpty(purchasePriceDetailIds)) {
            return Collections.emptyList();
        }
        List<PurchasePriceHistoryEntity> list = lambdaQuery().in(PurchasePriceHistoryEntity::getPriceDetailId, purchasePriceDetailIds).orderByDesc(PurchasePriceHistoryEntity::getCreateTime).list();
        return list;
    }

    @Override
    public List<PurchasePriceHistoryEntity> listByChangeDetailIdList(List<String> changeDetailIdList) {
        if (CollectionUtils.isEmpty(changeDetailIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(PurchasePriceHistoryEntity::getChangeDetailId,changeDetailIdList).list();
    }


    private List<PurchasePriceHistoryEntity> getByPriceDetailId(String priceDetailId) {
        LambdaQueryWrapper<PurchasePriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceHistoryEntity::getPriceDetailId, priceDetailId);
        queryWrapper.orderByDesc(PurchasePriceHistoryEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}
