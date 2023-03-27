package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.scm.mapper.PurchasePriceDetailMapper;
import com.erp.server.scm.service.PurchasePriceDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
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


    @Resource
    private PlmTaskFeign plmTaskFeign;

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
     * 添加明细
     *
     * @param purchasePriceId
     * @param purchasePriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-24 15:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPriceDetail(String purchasePriceId, List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList) {
        if (CollectionUtils.isEmpty(purchasePriceDetailList)) {
            return;
        }
        List<PurchasePriceDetailEntity> addList = BeanMapper.copyList(purchasePriceDetailList, PurchasePriceDetailEntity.class);
        List<String> skuIds = addList.stream().map(PurchasePriceDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        LocalDate localDate = LocalDate.now();
        for (PurchasePriceDetailEntity item : addList) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                item.setSkuNo(skuVO.getSkuNo());
                item.setProductName(skuVO.getSpuName());
            }
            item.setPurchasePriceId(purchasePriceId);
            //失效时间
            item.setExpireDate(localDate.plusYears(100));
            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            item.setTaxRate(rate);
        }
        this.saveBatch(addList);
    }


    /**
     * 根据价目表id 获取产品明细信息
     *
     * @param purchasePriceId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-27 9:48
     */
    @Override
    public List<PurchasePriceDetailDTO.UpdateDTO> getByPurchasePriceId(String purchasePriceId) {
        List<PurchasePriceDetailEntity> list = this.getListByPurchasePriceId(purchasePriceId);
        return BeanMapper.copyList(list, PurchasePriceDetailDTO.UpdateDTO.class);
    }


    /**
     * 修改产品明细
     *
     * @param purchasePriceId
     * @param purchasePriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-27 11:18
     */
    @Override
    public void updatePriceDetail(String purchasePriceId, List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList) {
        if (CollectionUtils.isEmpty(purchasePriceDetailList)) {
            return;
        }
        List<PurchasePriceDetailEntity> dbList = this.getListByPurchasePriceId(purchasePriceId);
        //获取到删除id集合
        List<String> deleteIdList = getDeleteIds(purchasePriceDetailList, dbList);
        this.removeByIds(deleteIdList);
        List<String> skuIds = purchasePriceDetailList.stream().map(PurchasePriceDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        List<PurchasePriceDetailEntity> saveOrUpdateList = new ArrayList<>(purchasePriceDetailList.size());
        LocalDate localDate = LocalDate.now();
        for (PurchasePriceDetailDTO.UpdateDTO item : purchasePriceDetailList) {
            PurchasePriceDetailEntity entity = new PurchasePriceDetailEntity();
            BeanMapper.copy(item, entity);
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                entity.setSkuNo(skuVO.getSkuNo());
                entity.setProductName(skuVO.getSpuName());
            }
            entity.setPurchasePriceId(purchasePriceId);
            //失效时间
            entity.setExpireDate(localDate.plusYears(100));
            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            entity.setTaxRate(rate);
            saveOrUpdateList.add(entity);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 获取到删除的集合
     *
     * @param purchasePriceDetailList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-27 11:27
     */
    private List<String> getDeleteIds(List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList, List<PurchasePriceDetailEntity> dbList) {
        List<String> ids = purchasePriceDetailList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchasePriceDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(PurchasePriceDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<PurchasePriceDetailEntity> getListByPurchasePriceId(String purchasePriceId) {
        LambdaQueryWrapper<PurchasePriceDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceDetailEntity::getPurchasePriceId, purchasePriceId);
        return this.list(queryWrapper);

    }

    @Override
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getTaxPrice(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto) {
        return baseMapper.getTaxPrice(dto);
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
