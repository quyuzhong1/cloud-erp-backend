package com.erp.server.oms.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.entity.SoPriceHistoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.SoPriceHistoryMapper;
import com.erp.server.oms.service.SoPriceHistoryService;
import lombok.extern.slf4j.Slf4j;
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
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@Service
public class SoPriceHistoryServiceImpl extends SuperServiceImpl<SoPriceHistoryMapper, SoPriceHistoryEntity> implements SoPriceHistoryService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;


    @Override
    public List<SoPriceDetailDTO.HistoryDTO> getHistory(String priceDetailId) {
        List<SoPriceHistoryEntity> list = this.getByPriceDetailId(priceDetailId);
        BigDecimal hundred = new BigDecimal("100");
        List<SoPriceDetailDTO.HistoryDTO> resultList = BeanMapper.copyList(list, SoPriceDetailDTO.HistoryDTO.class);
        List<String> skuIdList = resultList.stream().map(SoPriceDetailDTO.HistoryDTO::getSkuId).collect(Collectors.toList());
        List<String> currencyIdList = resultList.stream().map(SoPriceDetailDTO.HistoryDTO::getCurrency).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        for (SoPriceDetailDTO.HistoryDTO item : resultList) {
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
    public List<SoPriceDetailDTO.SoTaxPriceViewDTO> getHistoryTaxPrice(SoPriceDetailDTO.SoTaxPriceSearchDTO dto) {

        return baseMapper.getHistoryTaxPrice(dto);
    }


    /**
     * 根据供应商id 获取到对应的合sku 价格
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     * @author yl
     * @date 2023-04-11 14:55
     */
    @Override
    public List<SoPriceDetailDTO.AddDTO> getBySupplierId(String supplierId, List<String> skuIdList) {
        LambdaQueryWrapper<SoPriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoPriceHistoryEntity::getSupplierId, supplierId);
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            queryWrapper.in(SoPriceHistoryEntity::getSkuId, skuIdList);
        }
        LocalDate now = LocalDate.now();
        queryWrapper.le(SoPriceHistoryEntity::getEffectiveDate, now);
        queryWrapper.ge(SoPriceHistoryEntity::getExpireDate, now);
        List<SoPriceHistoryEntity> list = this.list(queryWrapper);

        return BeanMapper.copyList(list, SoPriceDetailDTO.AddDTO.class);
    }

    @Override
    public List<SoPriceDetailDTO.AddDTO> listBySupplierId(List<String> supplierIds, List<String> skuIdList) {
        LambdaQueryWrapper<SoPriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SoPriceHistoryEntity::getSupplierId, supplierIds);
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            queryWrapper.in(SoPriceHistoryEntity::getSkuId, skuIdList);
        }
        LocalDate now = LocalDate.now();
        queryWrapper.le(SoPriceHistoryEntity::getEffectiveDate, now);
        queryWrapper.ge(SoPriceHistoryEntity::getExpireDate, now);
        List<SoPriceHistoryEntity> list = this.list(queryWrapper);

        return BeanMapper.copyList(list, SoPriceDetailDTO.AddDTO.class);
    }

    @Override
    public List<SoPriceHistoryEntity> getHistoryByDetailIds(List<String> SoPriceDetailIds) {
        if (CollectionUtils.isEmpty(SoPriceDetailIds)) {
            return Collections.emptyList();
        }
        List<SoPriceHistoryEntity> list = lambdaQuery().in(SoPriceHistoryEntity::getPriceDetailId, SoPriceDetailIds).orderByDesc(SoPriceHistoryEntity::getCreateTime).list();
        return list;
    }

    @Override
    public List<SoPriceHistoryEntity> listByChangeDetailIdList(List<String> changeDetailIdList) {
        if (CollectionUtils.isEmpty(changeDetailIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoPriceHistoryEntity::getChangeDetailId,changeDetailIdList).list();
    }


    private List<SoPriceHistoryEntity> getByPriceDetailId(String priceDetailId) {
        LambdaQueryWrapper<SoPriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoPriceHistoryEntity::getPriceDetailId, priceDetailId);
        queryWrapper.orderByDesc(SoPriceHistoryEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}
