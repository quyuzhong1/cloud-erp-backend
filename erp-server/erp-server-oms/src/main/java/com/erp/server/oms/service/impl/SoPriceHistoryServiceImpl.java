package com.erp.server.oms.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
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
        BigDecimal hundred = MathUtil.BigDecimal_100;
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
    public List<SoPriceHistoryEntity> listByChangeDetailIdList(List<String> changeDetailIdList) {
        if (CollectionUtils.isEmpty(changeDetailIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoPriceHistoryEntity::getChangeDetailId,changeDetailIdList).list();
    }

    /**
     * 根据销售价目表明细id查询
     * @author will
     * @date 2025/3/25 14:49
     * @param priceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoPriceHistoryEntity>
     */
    private List<SoPriceHistoryEntity> getByPriceDetailId(String priceDetailId) {
        LambdaQueryWrapper<SoPriceHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoPriceHistoryEntity::getPriceDetailId, priceDetailId);
        queryWrapper.orderByDesc(SoPriceHistoryEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}
