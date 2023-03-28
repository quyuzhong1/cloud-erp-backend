package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchasePriceChangeDetailMapper;
import com.erp.server.scm.service.PurchasePriceChangeDetailService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品采购变更价 明细表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Service
public class PurchasePriceChangeDetailServiceImpl extends SuperServiceImpl<PurchasePriceChangeDetailMapper, PurchasePriceChangeDetailEntity> implements PurchasePriceChangeDetailService {


    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 检查区间报价是否重叠
     *
     * @param purchasePriceChangeDetailList
     * @return void
     * @author yl
     * @date 2023-03-28 12:07
     */
    @Override
    public void checkSkuInterval(List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList) {
        if (CollectionUtils.isNotEmpty(purchasePriceChangeDetailList)) {

            //以sku 分组
            Map<String, List<PurchasePriceChangeDetailDTO.AddDTO>> map = purchasePriceChangeDetailList.stream().collect(Collectors.groupingBy(PurchasePriceChangeDetailDTO.AddDTO::getSkuId));
            for (Map.Entry<String, List<PurchasePriceChangeDetailDTO.AddDTO>> item : map.entrySet()) {
                //skuId
                String skuId = item.getKey();
                //对应的报价
                List<PurchasePriceChangeDetailDTO.AddDTO> skuPriceList = item.getValue();
                //查询是否有无区间的
                long noInterval = skuPriceList.stream().filter(s -> (s.getMaxQty() == null || s.getMaxQty() == 0) && (s.getMinQty() == null || s.getMinQty() == 0)).count();
                //表示有无区间的
                if (noInterval > 0) {
                    if (skuPriceList.size() > 0) {
                        throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
                    }
                } else {
                    //没有无区间 就要检查又没有不同区间的
                    List<Integer> intervalList = new ArrayList<>(10);
                    for (PurchasePriceChangeDetailDTO.AddDTO interval : skuPriceList) {
                        intervalList.add(interval.getMinQty());
                        intervalList.add(interval.getMaxQty());
                    }
                    //判断是否是按顺序的
                    boolean isSortedResult = isSorted(intervalList);
                    //当不是的时候
                    if (!isSortedResult) {
                        throw new ServiceException(ApiError.ERROR_INTERVAL_OVERLAP);
                    }
                    long distCount = intervalList.stream().distinct().count();
                    if (distCount != intervalList.size()) {
                        throw new ServiceException(ApiError.ERROR_INTERVAL_OVERLAP);
                    }
                }
            }

        }

    }


    /**
     * 根据变更表id 获取明细
     *
     * @param priceChangeId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-28 14:35
     */
    @Override
    public List<PurchasePriceChangeDetailDTO.ViewDTO> getByPriceChangeId(String priceChangeId) {
        List<PurchasePriceChangeDetailEntity> list = this.getEntityByPriceChangeId(priceChangeId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<PurchasePriceChangeDetailDTO.ViewDTO> resultList = BeanMapper.copyList(list, PurchasePriceChangeDetailDTO.ViewDTO.class);
        //采购价目详情表id
        List<String> purchasePriceDetailIds = resultList.stream().map(PurchasePriceChangeDetailDTO.ViewDTO::getPurchasePriceDetailId).collect(Collectors.toList());
        //获取到对应的价目明细
        List<PurchasePriceDetailEntity> purchasePriceDetailList = purchasePriceDetailService.listByIds(purchasePriceDetailIds);
        BigDecimal hundred = new BigDecimal("100");
        for (PurchasePriceChangeDetailDTO.ViewDTO item : resultList) {
            String priceDetailId = item.getPurchasePriceDetailId();
            PurchasePriceDetailEntity priceDetailEntity = purchasePriceDetailList.stream().filter(p -> p.getId().equals(priceDetailId)).findFirst().orElse(null);
            if (priceDetailEntity != null) {
                item.setOldCurrency(priceDetailEntity.getCurrency());
                item.setOldTaxPrice(priceDetailEntity.getTaxPrice());
                item.setOldTaxRate(priceDetailEntity.getTaxRate().multiply(hundred));
                item.setTaxRate(item.getTaxRate().multiply(hundred));
            }
        }

        return resultList;
    }


    /**
     * 添加采购价目变更明细
     *
     * @param purchasePriceChangeId
     * @param purchasePriceChangeDetailList
     * @return void
     * @author yl
     * @date 2023-03-28 16:09
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPriceChangeDetail(String purchasePriceChangeId, List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList) {
        if (CollectionUtils.isEmpty(purchasePriceChangeDetailList)) {
            return;
        }
        List<PurchasePriceChangeDetailEntity> addList = BeanMapper.copyList(purchasePriceChangeDetailList, PurchasePriceChangeDetailEntity.class);
        List<String> skuIds = addList.stream().map(PurchasePriceChangeDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        LocalDate localDate = LocalDate.now();
        for (PurchasePriceChangeDetailEntity item : addList) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                item.setSkuNo(skuVO.getSkuNo());
                item.setProductName(skuVO.getSpuName());
            }
            item.setPurchasePriceChangeId(purchasePriceChangeId);
            //失效时间
            item.setExpireDate(localDate.plusYears(100));
            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            item.setTaxRate(rate);
        }
        this.saveBatch(addList);
    }


    private List<PurchasePriceChangeDetailEntity> getEntityByPriceChangeId(String priceChangeId) {
        LambdaQueryWrapper<PurchasePriceChangeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceChangeDetailEntity::getPurchasePriceChangeId, priceChangeId);
        return this.list(queryWrapper);

    }


    /**
     * 判断是否按顺序排序
     *
     * @param list
     * @return boolean
     * @author yl
     * @date 2023-03-24 14:28
     */
    private boolean isSorted(List<Integer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) > list.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

}
