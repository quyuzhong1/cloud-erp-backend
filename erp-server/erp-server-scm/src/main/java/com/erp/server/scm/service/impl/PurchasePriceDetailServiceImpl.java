package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.server.scm.mapper.PurchasePriceDetailMapper;
import com.erp.server.scm.service.PurchasePriceDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品采购价格明细表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class PurchasePriceDetailServiceImpl extends SuperServiceImpl<PurchasePriceDetailMapper, PurchasePriceDetailEntity> implements PurchasePriceDetailService {


    /**
     * 检查sku 区间报价
     *
     * @param purchasePriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-24 14:01
     */
    @Override
    public void checkSkuInterval(List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList) {
        if (CollectionUtils.isNotEmpty(purchasePriceDetailList)) {
            //以sku 分组
            Map<String, List<PurchasePriceDetailDTO.AddDTO>> map = purchasePriceDetailList.stream().collect(Collectors.groupingBy(PurchasePriceDetailDTO.AddDTO::getSkuId));
            for (Map.Entry<String, List<PurchasePriceDetailDTO.AddDTO>> item : map.entrySet()) {
                //skuId
                String skuId = item.getKey();
                //对应的报价
                List<PurchasePriceDetailDTO.AddDTO> skuPriceList = item.getValue();
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
                    for (PurchasePriceDetailDTO.AddDTO interval : skuPriceList) {
                        intervalList.add(interval.getMinQty());
                        intervalList.add(interval.getMaxQty());
                    }
                    //判断是否是按顺序的
                    boolean isSortedResult = isSorted(intervalList);
                    //当不是的时候
                    if(!isSortedResult){
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
