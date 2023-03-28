package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.server.scm.mapper.PurchasePriceChangeDetailMapper;
import com.erp.server.scm.service.PurchasePriceChangeDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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


    /**
     * 检查区间报价是否重叠
     * @author yl
     * @date 2023-03-28 12:07
     * @param purchasePriceChangeDetailList
     * @return void
     */
    @Override
    public void checkSkuInterval(List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList) {
        if(CollectionUtils.isNotEmpty(purchasePriceChangeDetailList)){

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
