package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoPriceChangeDetailDTO;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import com.erp.model.oms.entity.SoPriceChangeEntity;
import com.erp.model.oms.entity.SoPriceDetailEntity;
import com.erp.model.oms.entity.SoPriceHistoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.SoPriceChangeDetailMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品采购变更价 明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@Service
public class SoPriceChangeDetailServiceImpl extends SuperServiceImpl<SoPriceChangeDetailMapper, SoPriceChangeDetailEntity> implements SoPriceChangeDetailService {

    @Resource
    private SoPriceDetailService soPriceDetailService;

    @Resource
    private SoPriceHistoryService soPriceHistoryService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private OperateLogService moduleOperateLogService;

    @Resource
    private SoPriceService soPriceService;



    /**
     * 根据变更表id 获取明细
     *
     * @param priceChangeId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceChangeDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-28 14:35
     */
    @Override
    public List<SoPriceChangeDetailDTO.ViewDTO> getByPriceChangeId(String priceChangeId) {
        List<SoPriceChangeDetailEntity> list = this.getEntityByPriceChangeId(priceChangeId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<SoPriceChangeDetailDTO.ViewDTO> resultList = BeanMapper.copyList(list, SoPriceChangeDetailDTO.ViewDTO.class);
        List<String> changeDetailIdList = list.stream().map(SoPriceChangeDetailEntity::getId).collect(Collectors.toList());
        //采购价目详情表id
        List<String> SoPriceDetailIds = resultList.stream().map(SoPriceChangeDetailDTO.ViewDTO::getSoPriceDetailId).collect(Collectors.toList());
        /**
         * 根据变更表id 获取到对应变更历史
         */
        List<SoPriceHistoryEntity> historyList = soPriceHistoryService.listByChangeDetailIdList(changeDetailIdList);
        //获取到对应的价目明细
        List<SoPriceDetailEntity> SoPriceDetailList = soPriceDetailService.listByIds(SoPriceDetailIds);

        BigDecimal hundred = new BigDecimal("100");
        List<String> currencyIdList = resultList.stream().map(SoPriceChangeDetailDTO.ViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        for (SoPriceChangeDetailDTO.ViewDTO item : resultList) {
            String priceDetailId = item.getSoPriceDetailId();
            SoPriceHistoryEntity historyEntity = historyList.stream().filter(h -> h.getChangeDetailId().equals(item.getId())).findFirst().orElse(null);

            SoPriceDetailEntity priceDetailEntity = SoPriceDetailList.stream().filter(p -> p.getId().equals(priceDetailId)).findFirst().orElse(null);
            if (item.getTaxRate() != null) {
                item.setTaxRate(item.getTaxRate().multiply(hundred));
            }
            String currency = item.getCurrency();
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            item.setCurrencySymbol(currencySymbol);
            Integer minQty = item.getMinQty();
            Integer maxQty = item.getMaxQty();
            if (minQty == 0 && maxQty == 0) {
                item.setMinQty(null);
                item.setMaxQty(null);
            }

            if (historyEntity != null) {
                item.setOldCurrency(historyEntity.getCurrency());
                item.setOldTaxPrice(historyEntity.getTaxPrice());
                item.setOldEffectiveDate(historyEntity.getEffectiveDate());
                if (historyEntity.getTaxRate() != null) {
                    item.setOldTaxRate(historyEntity.getTaxRate().multiply(hundred));
                }
                //升降比例
                BigDecimal offsetRate = MathUtil.divide(MathUtil.subtract(item.getTaxPrice(), item.getOldTaxPrice()), item.getOldTaxPrice()).multiply(MathUtil.BigDecimal_100);
                item.setOffsetRate(StrUtil.format("{}%",offsetRate));
            } else {
                if (priceDetailEntity != null) {
                    item.setOldCurrency(priceDetailEntity.getCurrency());
                    item.setOldTaxPrice(priceDetailEntity.getTaxPrice());
                    item.setOldEffectiveDate(priceDetailEntity.getEffectiveDate());
                    if (priceDetailEntity.getTaxRate() != null) {
                        item.setOldTaxRate(priceDetailEntity.getTaxRate().multiply(hundred));
                    }
                    //升降比例
                    BigDecimal offsetRate = MathUtil.divide(MathUtil.subtract(item.getTaxPrice(), item.getOldTaxPrice()), item.getOldTaxPrice()).multiply(MathUtil.BigDecimal_100);
                    item.setOffsetRate(StrUtil.format("{}%",offsetRate));
                }
            }
        }

        return resultList;
    }


    /**
     * 添加采购价目变更明细
     *
     * @param soPriceChangeId
     * @param soPriceChangeDetailList
     * @return void
     * @author yl
     * @date 2023-03-28 16:09
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPriceChangeDetail(String soPriceChangeId, List<SoPriceChangeDetailDTO.AddDTO> soPriceChangeDetailList) {
        if (CollectionUtils.isEmpty(soPriceChangeDetailList)) {
            return;
        }
        List<SoPriceChangeDetailEntity> addList = BeanMapper.copyList(soPriceChangeDetailList, SoPriceChangeDetailEntity.class);
        List<String> skuIds = addList.stream().map(SoPriceChangeDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        for (SoPriceChangeDetailEntity item : addList) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                item.setSkuNo(skuVO.getSkuNo());
            }

            item.setMainId(soPriceChangeId);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                item.setTaxRate(rate);
            }
        }
        //数据验证
        checkPriceChangeDetail(addList);
        this.saveBatch(addList);
    }

    /**
     * 审核通过后 需要修改采购价目详情表的数据
     *
     * @param soPriceChangeList
     * @return void
     * @author yl
     * @date 2023-03-28 19:06
     */
    @Override
    public void updateSoPriceDetail(List<SoPriceChangeEntity> soPriceChangeList) {
        List<String> soPriceChangeIds = soPriceChangeList.stream().map(SoPriceChangeEntity::getId).collect(Collectors.toList());
        //获取到对应数据
        List<SoPriceChangeDetailEntity> list = this.getEntityByPriceChangeIds(soPriceChangeIds);
        List<String> soPriceDetailIdList = list.stream().map(SoPriceChangeDetailEntity::getSoPriceDetailId).collect(Collectors.toList());
        //获取采购价目详情集合
        List<SoPriceDetailEntity> soPriceDetailList = soPriceDetailService.listByIds(soPriceDetailIdList);
        List<SoPriceHistoryEntity> historyList = new ArrayList<>(soPriceDetailList.size());
        List<SoPriceDetailEntity> updateList = new ArrayList<>(soPriceDetailList.size());
        for (SoPriceDetailEntity item : soPriceDetailList) {
            //采购详情表id
            String priceDetailId = item.getId();
            //更改的价目
            SoPriceChangeDetailEntity changeDetail = list.stream().filter(P -> P.getSoPriceDetailId().equals(priceDetailId)).findFirst().orElse(null);
            if (changeDetail != null) {
                //历史的
                SoPriceHistoryEntity history = new SoPriceHistoryEntity();
                BeanMapper.copy(item, history);
                history.setPriceDetailId(priceDetailId);
                history.setId(IdWorker.getIdStr());
                history.setChangeDetailId(changeDetail.getId());
                history.setCustomerId(changeDetail.getCustomerId());
                historyList.add(history);
                item.setTaxRate(changeDetail.getTaxRate());
                item.setSkuNo(changeDetail.getSkuNo());
                item.setSkuId(changeDetail.getSkuId());
                item.setTaxPrice(changeDetail.getTaxPrice());
                item.setDeliveryDay(changeDetail.getDeliveryDay());
                item.setCurrency(changeDetail.getCurrency());
                item.setEffectiveDate(changeDetail.getEffectiveDate());
                item.setExpireDate(changeDetail.getExpireDate());
                item.setMinQty(changeDetail.getMinQty());
                item.setMaxQty(changeDetail.getMaxQty());
                updateList.add(item);
            }
        }

        //添加历史
        soPriceHistoryService.saveBatch(historyList);

        //修改价目详情
        approveCheckData(list,updateList,soPriceChangeList);
        soPriceDetailService.updateBatchById(updateList);
    }

    /**
     * 修改变更价目详情信息
     *
     * @param SoPriceChangeId
     * @param SoPriceChangeDetailList
     * @return void
     * @author yl
     * @date 2023-03-29 9:20
     */
    @Override
    public void updatePriceChangeDetail(String SoPriceChangeId, List<SoPriceChangeDetailDTO.UpdateDTO> SoPriceChangeDetailList) {
        if (CollectionUtils.isEmpty(SoPriceChangeDetailList)) {
            return;
        }
        List<SoPriceChangeDetailEntity> dbList = this.getEntityByPriceChangeId(SoPriceChangeId);
        //获取到要删除的id集合
        List<String> deleteIdList = getDeleteIds(SoPriceChangeDetailList, dbList);
        List<SoPriceChangeDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        this.removeByIds(deleteIdList);
        List<String> skuIds = SoPriceChangeDetailList.stream().map(SoPriceChangeDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<SoPriceChangeDetailEntity> saveOrUpdateList = new ArrayList<>(SoPriceChangeDetailList.size());
        for (SoPriceChangeDetailDTO.UpdateDTO item : SoPriceChangeDetailList) {
            SoPriceChangeDetailEntity entity = new SoPriceChangeDetailEntity();
            BeanMapper.copy(item, entity);
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                entity.setSkuNo(skuVO.getSkuNo());
            }
            entity.setMainId(SoPriceChangeId);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                entity.setTaxRate(rate);
            }

            saveOrUpdateList.add(entity);
        }

        //数据验证
        checkPriceChangeDetail(saveOrUpdateList);

        //这是要添加的
        List<SoPriceChangeDetailEntity> addList = saveOrUpdateList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //这是修改的
        List<SoPriceChangeDetailEntity> updateList = saveOrUpdateList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(SoPriceChangeId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(SoPriceChangeId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), addPairList, "编辑操作");

        //修改的
        for (SoPriceChangeDetailEntity update : updateList) {
            String id = update.getId();
            SoPriceChangeDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                moduleOperateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.PURCHASE_PRICE.getCode(), SoPriceChangeId, "", "");
            }
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 根据供应商id获取到已变更区间数据
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     * @author yl
     * @date 2023-04-06 10:01
     */
    @Override
    public List<SoPriceDetailDTO.AddDTO> getBySupplierId(String supplierId) {
        List<String> statusList = new ArrayList<>(5);
        statusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        List<SoPriceDetailDTO.AddDTO> list = baseMapper.getBySupplierId(supplierId, statusList);
        return list;
    }

    @Override
    public List<SoPriceChangeDetailEntity> listBySoPriceChangeId(String SoPriceChangeId) {
        return baseMapper.listBySoPriceChangeId(SoPriceChangeId);
    }

    @Override
    public List<SoPriceChangeDetailEntity> listBySoPriceDetailIds(List<String> SoPriceDetailIds) {
        if (CollectionUtils.isEmpty(SoPriceDetailIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listBySoPriceDetailIds(SoPriceDetailIds);
    }

    @Override
    public void updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        this.lambdaUpdate()
                .in(SoPriceChangeDetailEntity::getId, ids)
                .set(SoPriceChangeDetailEntity::getRemark, remark)
                .update(new SoPriceChangeDetailEntity());
    }

    @Override
    public List<SoPriceChangeDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoPriceChangeDetailEntity::getMainId,mainIdList).
                orderByDesc(SoPriceChangeDetailEntity::getUpdateTime).list();
    }

    /**
     * @description: 审核通过验证
     * @author Will
     * @date: 2024/1/15 17:36
     * @param detailList
     * @param updateList
     */
    private void approveCheckData (List<SoPriceChangeDetailEntity> detailList,List<SoPriceDetailEntity> updateList
            ,List<SoPriceChangeEntity> SoPriceChangeList) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }

        Map<String, List<SoPriceChangeDetailEntity>> map = detailList.stream().collect(Collectors.groupingBy(SoPriceChangeDetailEntity::getCustomerId));

        for (Map.Entry<String, List<SoPriceChangeDetailEntity>> entry :map.entrySet()) {

            List<SoPriceChangeDetailEntity> value = entry.getValue();
            List<String> SoPriceDetailIdList = value.stream().map(SoPriceChangeDetailEntity::getSoPriceDetailId).collect(Collectors.toList());

            List<SoPriceDetailEntity> list = updateList.stream().filter(obj -> SoPriceDetailIdList.contains(obj.getId())).collect(Collectors.toList());
            //报价信息验证
            soPriceDetailService.checkSoPriceDetail(entry.getKey(),SoPriceChangeList.get(0).getSoOrgId(),list);
        }
    }

    /**
     * 获取删除的id集合
     *
     * @param SoPriceChangeDetailList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-29 9:33
     */
    private List<String> getDeleteIds(List<SoPriceChangeDetailDTO.UpdateDTO> SoPriceChangeDetailList, List<SoPriceChangeDetailEntity> dbList) {
        List<String> ids = SoPriceChangeDetailList.stream().filter(p -> StringUtils.isNotBlank(p.getId())).
                map(SoPriceChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SoPriceChangeDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<SoPriceChangeDetailEntity> getEntityByPriceChangeId(String priceChangeId) {
        LambdaQueryWrapper<SoPriceChangeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoPriceChangeDetailEntity::getMainId, priceChangeId);
        return this.list(queryWrapper);
    }

    private List<SoPriceChangeDetailEntity> getEntityByPriceChangeIds(List<String> priceChangeIds) {
        if (CollectionUtils.isEmpty(priceChangeIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SoPriceChangeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SoPriceChangeDetailEntity::getMainId, priceChangeIds);
        return this.list(queryWrapper);
    }


    /**
     * @description: 数据验证
     * @author Will
     * @date: 2024/1/15 17:41
     * @param list
     */
    private void checkPriceChangeDetail (List<SoPriceChangeDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (SoPriceChangeDetailEntity entity : list) {
            //检验失效时间需要大于等于生效时间
            if (entity.getExpireDate().isBefore(entity.getEffectiveDate())) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_DATE,entity.getSkuNo());
            }
        }
    }
}
