package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceHistoryEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchasePriceChangeDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchasePriceChangeDetailService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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
    private PurchasePriceHistoryService purchasePriceHistoryService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private ModuleOperateLogService moduleOperateLogService;


    /**
     * 检查区间报价是否重叠
     *
     * @param purchasePriceChangeDetailList
     * @return void
     * @author yl
     * @date 2023-03-28 12:07
     */
    @Override
    public void checkSkuInterval(String purchasePriceId, List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList, List<PurchasePriceDetailDTO.AddDTO> supplierPriceDetailList) {

        if (CollectionUtils.isNotEmpty(purchasePriceChangeDetailList)) {
            //这个是检查的
            List<PurchasePriceChangeDetailDTO.AddDTO> checkList = new ArrayList<>(10);
            checkList.addAll(purchasePriceChangeDetailList);

            //检查区间
            for (PurchasePriceChangeDetailDTO.AddDTO item : checkList) {
                Integer min = item.getMinQty();
                Integer max = item.getMaxQty();
                if (min != null) {
                    if (max == null) {
                        throw new ServiceException(ApiError.ERROR_INTERVAL_EXIST);
                    }
                }
                if (max != null) {
                    if (min == null) {
                        throw new ServiceException(ApiError.ERROR_INTERVAL_EXIST);
                    }
                }
                //当两个都不为空的时候
                if (min != null && max != null) {
                    if (min.equals(max)) {
                        throw new ServiceException(ApiError.ERROR_INTERVAL_DIFFERENT);
                    }
                }

            }

            /**
             * 查询到
             * 采购价目表的明细
             * 因为变更 也不能有区间重复的
             */

            List<PurchasePriceChangeDetailDTO.AddDTO> list = BeanMapper.copyList(supplierPriceDetailList, PurchasePriceChangeDetailDTO.AddDTO.class);
            checkList.addAll(list);
            //以sku 分组
            Map<String, List<PurchasePriceChangeDetailDTO.AddDTO>> map = checkList.stream().collect(Collectors.groupingBy(PurchasePriceChangeDetailDTO.AddDTO::getSkuId));
            for (Map.Entry<String, List<PurchasePriceChangeDetailDTO.AddDTO>> item : map.entrySet()) {

                //对应的报价
                List<PurchasePriceChangeDetailDTO.AddDTO> skuPriceList = item.getValue();
                //查询是否有无区间的
                long noInterval = skuPriceList.stream().filter(s -> (s.getMaxQty() == null || s.getMaxQty() == 0) && (s.getMinQty() == null || s.getMinQty() == 0)).count();
                //表示有无区间的
                if (noInterval > 1) {
                    throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
                } else {
                    //没有无区间 就要检查又没有不同区间的
                    List<Integer> intervalList = new ArrayList<>(10);
                    for (PurchasePriceChangeDetailDTO.AddDTO interval : skuPriceList) {
                        if (interval.getMinQty() != null && interval.getMaxQty() != null) {
                            intervalList.addAll(getInterval(interval.getMinQty(), interval.getMaxQty()));
                        }
                    }
                    //判断是否是按顺序的
                    boolean isRepetitionResult = isRepetition(intervalList);
                    //当不是的时候
                    if (!isRepetitionResult) {
                        throw new ServiceException(ApiError.ERROR_INTERVAL_OVERLAP);
                    }
                }
            }

        }

    }

    private List<Integer> getInterval(Integer min, Integer max) {
        List<Integer> resultList = new ArrayList<>(10);
        for (int i = min + 1; i <= max; i++) {
            resultList.add(i);
        }
        return resultList;
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
        List<String> currencyIdList = resultList.stream().map(PurchasePriceChangeDetailDTO.ViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        for (PurchasePriceChangeDetailDTO.ViewDTO item : resultList) {
            String priceDetailId = item.getPurchasePriceDetailId();
            PurchasePriceDetailEntity priceDetailEntity = purchasePriceDetailList.stream().filter(p -> p.getId().equals(priceDetailId)).findFirst().orElse(null);
            if (priceDetailEntity != null) {
                item.setOldCurrency(priceDetailEntity.getCurrency());
                item.setOldTaxPrice(priceDetailEntity.getTaxPrice());
                item.setOldTaxRate(priceDetailEntity.getTaxRate().multiply(hundred));
                item.setTaxRate(item.getTaxRate().multiply(hundred));
                String currency = item.getCurrency();
                String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
                item.setCurrencySymbol(currencySymbol);
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


    /**
     * 审核通过后 需要修改采购价目详情表的数据
     *
     * @param purchasePriceChangeIds
     * @return void
     * @author yl
     * @date 2023-03-28 19:06
     */
    @Override
    public void updatePurchasePriceDetail(List<String> purchasePriceChangeIds) {
        //获取到对应数据
        List<PurchasePriceChangeDetailEntity> list = this.getEntityByPriceChangeIds(purchasePriceChangeIds);
        List<String> purchasePriceDetailIdList = list.stream().map(PurchasePriceChangeDetailEntity::getPurchasePriceDetailId).collect(Collectors.toList());
        //获取采购价目详情集合
        List<PurchasePriceDetailEntity> purchasePriceDetailList = purchasePriceDetailService.listByIds(purchasePriceDetailIdList);
        List<PurchasePriceHistoryEntity> historyList = new ArrayList<>(purchasePriceDetailList.size());
        List<PurchasePriceDetailEntity> updateList = new ArrayList<>(purchasePriceDetailList.size());
        for (PurchasePriceDetailEntity item : purchasePriceDetailList) {
            //采购详情表id
            String priceDetailId = item.getId();
            //更改的价目
            PurchasePriceChangeDetailEntity changeDetail = list.stream().filter(P -> P.getPurchasePriceDetailId().equals(priceDetailId)).findFirst().orElse(null);
            if (changeDetail != null) {
                //历史的
                PurchasePriceHistoryEntity history = new PurchasePriceHistoryEntity();
                BeanMapper.copy(item, history);
                history.setPriceDetailId(priceDetailId);
                historyList.add(history);
                item.setTaxRate(changeDetail.getTaxRate());
                item.setExpireDate(changeDetail.getExpireDate());
                item.setProductName(changeDetail.getProductName());
                item.setSkuNo(changeDetail.getSkuNo());
                item.setSkuId(changeDetail.getSkuId());
                item.setTaxPrice(changeDetail.getTaxPrice());
                item.setDeliveryDay(changeDetail.getDeliveryDay());
                item.setCurrency(changeDetail.getCurrency());
                item.setEffectiveDate(changeDetail.getEffectiveDate());
                item.setMinQty(changeDetail.getMinQty());
                item.setMaxQty(changeDetail.getMaxQty());
                updateList.add(item);
            }
        }

        //修改价目详情
        purchasePriceDetailService.updateBatchById(updateList);

        //添加历史
        purchasePriceHistoryService.saveBatch(historyList);

    }


    /**
     * 修改变更价目详情信息
     *
     * @param purchasePriceChangeId
     * @param purchasePriceChangeDetailList
     * @return void
     * @author yl
     * @date 2023-03-29 9:20
     */
    @Override
    public void updatePriceChangeDetail(String purchasePriceChangeId, List<PurchasePriceChangeDetailDTO.UpdateDTO> purchasePriceChangeDetailList) {
        if (CollectionUtils.isNotEmpty(purchasePriceChangeDetailList)) {
            return;
        }
        List<PurchasePriceChangeDetailEntity> dbList = this.getEntityByPriceChangeId(purchasePriceChangeId);
        //获取到要删除的id集合
        List<String> deleteIdList = getDeleteIds(purchasePriceChangeDetailList, dbList);
        List<PurchasePriceChangeDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        this.removeByIds(deleteIdList);
        List<String> skuIds = purchasePriceChangeDetailList.stream().map(PurchasePriceChangeDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        List<PurchasePriceChangeDetailEntity> saveOrUpdateList = new ArrayList<>(purchasePriceChangeDetailList.size());
        LocalDate localDate = LocalDate.now();
        for (PurchasePriceChangeDetailDTO.UpdateDTO item : purchasePriceChangeDetailList) {
            PurchasePriceChangeDetailEntity entity = new PurchasePriceChangeDetailEntity();
            BeanMapper.copy(item, entity);
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                entity.setSkuNo(skuVO.getSkuNo());
                entity.setProductName(skuVO.getSpuName());
            }
            entity.setPurchasePriceChangeId(purchasePriceChangeId);
            //失效时间
            entity.setExpireDate(localDate.plusYears(100));
            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            entity.setTaxRate(rate);
            saveOrUpdateList.add(entity);
        }
        //这是要添加的
        List<PurchasePriceChangeDetailEntity> addList = saveOrUpdateList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //这是修改的
        List<PurchasePriceChangeDetailEntity> updateList = saveOrUpdateList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(purchasePriceChangeId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(purchasePriceChangeId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), addPairList, "编辑操作");

        //修改的
        for (PurchasePriceChangeDetailEntity update : updateList) {
            String id = update.getId();
            PurchasePriceChangeDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                moduleOperateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.PURCHASE_PRICE.getCode(), purchasePriceChangeId, "", "");
            }
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 根据供应商id获取到已变更区间数据
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     * @author yl
     * @date 2023-04-06 10:01
     */
    @Override
    public List<PurchasePriceDetailDTO.AddDTO> getBySupplierId(String supplierId) {
        List<String> statusList = new ArrayList<>(5);
        statusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        List<PurchasePriceDetailDTO.AddDTO> list = baseMapper.getBySupplierId(supplierId, statusList);
        return list;
    }


    /**
     * 获取删除的id集合
     *
     * @param purchasePriceChangeDetailList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-29 9:33
     */
    private List<String> getDeleteIds(List<PurchasePriceChangeDetailDTO.UpdateDTO> purchasePriceChangeDetailList, List<PurchasePriceChangeDetailEntity> dbList) {
        List<String> ids = purchasePriceChangeDetailList.stream().filter(p -> StringUtils.isNotBlank(p.getId())).
                map(PurchasePriceChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(PurchasePriceChangeDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<PurchasePriceChangeDetailEntity> getEntityByPriceChangeId(String priceChangeId) {
        LambdaQueryWrapper<PurchasePriceChangeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceChangeDetailEntity::getPurchasePriceChangeId, priceChangeId);
        return this.list(queryWrapper);
    }

    private List<PurchasePriceChangeDetailEntity> getEntityByPriceChangeIds(List<String> priceChangeIds) {
        if (CollectionUtils.isEmpty(priceChangeIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<PurchasePriceChangeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PurchasePriceChangeDetailEntity::getPurchasePriceChangeId, priceChangeIds);
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
    private boolean isRepetition(List<Integer> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            Set<Integer> set = new HashSet<>(list);
            if (set.size() < list.size()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
