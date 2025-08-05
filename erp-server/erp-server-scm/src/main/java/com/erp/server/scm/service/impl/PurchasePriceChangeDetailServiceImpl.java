package com.erp.server.scm.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceService;
import com.erp.server.scm.mapper.PurchasePriceChangeDetailMapper;
import com.erp.server.scm.service.*;
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

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private SyncKingdeePurchasePriceService syncKingdeePurchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

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
        List<String> changeDetailIdList = list.stream().map(PurchasePriceChangeDetailEntity::getId).collect(Collectors.toList());
        //采购价目详情表id
        List<String> purchasePriceDetailIds = resultList.stream().map(PurchasePriceChangeDetailDTO.ViewDTO::getPurchasePriceDetailId).collect(Collectors.toList());
        /**
         * 根据变更表id 获取到对应变更历史
         */
        List<PurchasePriceHistoryEntity> historyList = purchasePriceHistoryService.listByChangeDetailIdList(changeDetailIdList);
        //获取到对应的价目明细
        List<PurchasePriceDetailEntity> purchasePriceDetailList = purchasePriceDetailService.listByIds(purchasePriceDetailIds);

        BigDecimal hundred = new BigDecimal("100");
        List<String> currencyIdList = resultList.stream().map(PurchasePriceChangeDetailDTO.ViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        for (PurchasePriceChangeDetailDTO.ViewDTO item : resultList) {
            String priceDetailId = item.getPurchasePriceDetailId();
            PurchasePriceHistoryEntity historyEntity = historyList.stream().filter(h -> h.getChangeDetailId().equals(item.getId())).findFirst().orElse(null);

            PurchasePriceDetailEntity priceDetailEntity = purchasePriceDetailList.stream().filter(p -> p.getId().equals(priceDetailId)).findFirst().orElse(null);
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
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        for (PurchasePriceChangeDetailEntity item : addList) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                item.setSkuNo(skuVO.getSkuNo());
                item.setProductName(skuVO.getSpuName());
            }

            item.setPurchasePriceChangeId(purchasePriceChangeId);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                item.setTaxRate(rate);
            }
        }
        //数据验证
        checkPriceChangeDetail(purchasePriceChangeId,addList);
        this.saveBatch(addList);
    }

    /**
     * 审核通过后 需要修改采购价目详情表的数据
     *
     * @param purchasePriceChangeList
     * @return void
     * @author yl
     * @date 2023-03-28 19:06
     */
    @Override
    public void updatePurchasePriceDetail(List<PurchasePriceChangeEntity> purchasePriceChangeList) {
        List<String> purchasePriceChangeIds = purchasePriceChangeList.stream().map(PurchasePriceChangeEntity::getId).collect(Collectors.toList());
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
                /*String supplierId = purchasePriceChangeList.stream().filter(p -> p.getPurchasePriceId().equals(item.getPurchasePriceId())).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getSupplierId())).orElse("");*/
                //历史的
                PurchasePriceHistoryEntity history = new PurchasePriceHistoryEntity();
                BeanMapper.copy(item, history);
                history.setPriceDetailId(priceDetailId);
                history.setId(IdWorker.getIdStr());
                history.setChangeDetailId(changeDetail.getId());
                history.setSupplierId(changeDetail.getSupplierId());
                historyList.add(history);
                item.setTaxRate(changeDetail.getTaxRate());
                item.setProductName(changeDetail.getProductName());
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
        purchasePriceHistoryService.saveBatch(historyList);

        //修改价目详情
        approveCheckData(list,updateList,purchasePriceChangeList);
        purchasePriceDetailService.updateBatchById(updateList);
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
        if (CollectionUtils.isEmpty(purchasePriceChangeDetailList)) {
            return;
        }
        List<PurchasePriceChangeDetailEntity> dbList = this.getEntityByPriceChangeId(purchasePriceChangeId);
        //获取到要删除的id集合
        List<String> deleteIdList = getDeleteIds(purchasePriceChangeDetailList, dbList);
        List<PurchasePriceChangeDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        this.removeByIds(deleteIdList);
        List<String> skuIds = purchasePriceChangeDetailList.stream().map(PurchasePriceChangeDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
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
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                entity.setTaxRate(rate);
            }

            saveOrUpdateList.add(entity);
        }

        //数据验证
        checkPriceChangeDetail(purchasePriceChangeId,saveOrUpdateList);

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

    @Override
    public List<PurchasePriceChangeDetailEntity> listByPurchasePriceChangeId(String purchasePriceChangeId) {
        return baseMapper.listByPurchasePriceChangeId(purchasePriceChangeId);
    }

    @Override
    public List<PurchasePriceChangeDetailEntity> listByPurchasePriceDetailIds(List<String> purchasePriceDetailIds) {
        if (CollectionUtils.isEmpty(purchasePriceDetailIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listByPurchasePriceDetailIds(purchasePriceDetailIds);
    }

    @Override
    public void updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        this.lambdaUpdate()
                .in(PurchasePriceChangeDetailEntity::getId, ids)
                .set(PurchasePriceChangeDetailEntity::getRemark, remark)
                .update(new PurchasePriceChangeDetailEntity());
    }

    @Override
    public List<PurchasePriceChangeDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(PurchasePriceChangeDetailEntity::getPurchasePriceChangeId,mainIdList).
                orderByDesc(PurchasePriceChangeDetailEntity::getUpdateTime).list();
    }

    @Override
    public PurchasePriceChangeDetailEntity getLatest(String purchasePriceDetailId, String id) {
        return baseMapper.getLatest(purchasePriceDetailId,id);
    }

    /**
     * @description: 审核通过验证
     * @author Will
     * @date: 2024/1/15 17:36
     * @param detailList
     * @param updateList
     */
    private void approveCheckData (List<PurchasePriceChangeDetailEntity> detailList,List<PurchasePriceDetailEntity> updateList
            ,List<PurchasePriceChangeEntity> purchasePriceChangeList) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }

        Map<String, List<PurchasePriceChangeDetailEntity>> map = detailList.stream().collect(Collectors.groupingBy(PurchasePriceChangeDetailEntity::getSupplierId));

        for (Map.Entry<String, List<PurchasePriceChangeDetailEntity>> entry :map.entrySet()) {

            List<PurchasePriceChangeDetailEntity> value = entry.getValue();
            List<String> purchasePriceDetailIdList = value.stream().map(PurchasePriceChangeDetailEntity::getPurchasePriceDetailId).collect(Collectors.toList());

            List<PurchasePriceDetailEntity> list = updateList.stream().filter(obj -> purchasePriceDetailIdList.contains(obj.getId())).collect(Collectors.toList());
            //报价信息验证
            purchasePriceDetailService.checkPurchasePriceDetail(entry.getKey(),purchasePriceChangeList.get(0).getPurchaseOrgId(),list);
        }
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
     * @description: 数据验证
     * @author Will
     * @date: 2024/1/15 17:41
     * @param list
     */
    private void checkPriceChangeDetail (String purchasePriceChangeId,List<PurchasePriceChangeDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        PurchasePriceChangeEntity purchasePriceChangeEntity = purchasePriceChangeService.getById(purchasePriceChangeId);
        if (ObjUtil.isEmpty(purchasePriceChangeEntity)) {
            throw new ServiceException(ApiError.ERROR_98028);
        }
        //区间验证
        checkPurchasePriceChangeDetail(purchasePriceChangeEntity.getPurchaseOrgId(),list);
    }

    /**
     * 数据校验
     * @author will
     * @date 2025/5/7 09:31
     * @param purchaseOrgId
     * @param list
     * @return void
     */
    public void checkPurchasePriceChangeDetail (String purchaseOrgId,List<PurchasePriceChangeDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //查询供应商信息
        List<String> supplierIdList = list.stream().map(PurchasePriceChangeDetailEntity::getSupplierId).distinct().collect(Collectors.toList());
        List<String> skuIdList = list.stream().map(PurchasePriceChangeDetailEntity::getSkuId).collect(Collectors.toList());
        //已存在的采购调价明细数据
        List<PurchasePriceChangeDetailEntity> purchaseDetailList = listCheckPurchasePriceChangeDetail(supplierIdList, purchaseOrgId, skuIdList);
        if (CollectionUtils.isNotEmpty(purchaseDetailList)) {
            List<String> oldIdList = list.stream().map(PurchasePriceChangeDetailEntity::getId).collect(Collectors.toList());
            purchaseDetailList = purchaseDetailList.stream().filter(obj -> !oldIdList.contains(obj.getId())).collect(Collectors.toList());
        }
        //采购价目表明细数据
        List<PurchasePriceDetailDTO.ViewDTO> purchasePriceDetailList = purchasePriceDetailService.listCheckPurchasePriceDetail(supplierIdList, purchaseOrgId, skuIdList);
        if (CollectionUtils.isNotEmpty(purchasePriceDetailList)) {
            List<String> priceDetailIdList = list.stream().map(PurchasePriceChangeDetailEntity::getPurchasePriceDetailId).collect(Collectors.toList());
            purchasePriceDetailList = purchasePriceDetailList.stream().filter(obj -> !priceDetailIdList.contains(obj.getId())).collect(Collectors.toList());
        }
        /**
         * 校验
         * 1、数据与新增同类数据校验
         * 2、数据与调价表未审核数据进行校验
         * 3、数据与价目表数据进行校验
         */
        for (int i = 0;i < list.size();i++) {
            PurchasePriceChangeDetailEntity entity = list.get(i);
            //检验失效时间需要大于生效时间
            if (entity.getExpireDate().isBefore(entity.getEffectiveDate())) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_DATE,entity.getSkuNo());
            }
            //校验区间到需要大于区间从
            if (entity.getMaxQty().compareTo(entity.getMinQty()) <= MathUtil.ZERO) {
                throw new ServiceException(ApiError.ERROR_SO_PRICE_INTERVAL_SIZE,entity.getSkuNo());
            }

            //1、数据与新增同类数据校验
            for (int j = 0;j < list.size();j++) {
                PurchasePriceChangeDetailEntity detailEntity = list.get(j);
                if (i == j || !StrUtil.equals(entity.getSkuId(),detailEntity.getSkuId())) {
                    continue;
                }
                //验证是否重叠
                checkOverlap(entity,detailEntity);
            }

            //2、数据与调价表未审核数据进行校验
            List<PurchasePriceChangeDetailEntity> oldList = purchaseDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(),entity.getSupplierId()) &&  StrUtil.equals(obj.getSkuId(), entity.getSkuId())).map(obj -> BeanMapperUtils.map(PurchasePriceChangeDetailEntity.class,obj) ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(oldList)) {
                //验证是否重叠
                oldList.forEach(obj -> checkOverlap(entity,obj));
            }

            //3、数据与价目表数据进行校验
            List<PurchasePriceDetailEntity> oldPriceDetailList = purchasePriceDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSupplierId(),entity.getSupplierId()) &&  StrUtil.equals(obj.getSkuId(), entity.getSkuId())).map(obj -> BeanMapperUtils.map(PurchasePriceDetailEntity.class,obj) ).collect(Collectors.toList());
            if (ObjUtil.isNotEmpty(oldPriceDetailList)) {
                oldPriceDetailList.forEach(obj -> checkOverlap(entity,obj));
            }
        }
    }

    /**
     * 验证是否重叠
     * @author will
     * @date 2025/5/7 09:59
     * @param entity
     * @param detailEntity
     * @return void
     */
    private void checkOverlap (PurchasePriceChangeDetailEntity entity,PurchasePriceChangeDetailEntity detailEntity) {
        //区间重叠时
        if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
            //时间不能重叠
            boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
            if (overlap) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_DATE_OVERLAP,entity.getSkuNo());
            }
        }
        //时间重叠时
        boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
        if (overlap) {
            //区间不能重叠
            if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                    && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
                throw new ServiceException(ApiError.ERROR_INTERVAL_SUPPLIER_OVERLAP);
            }
        }
    }

    /**
     * 验证是否重叠
     * @author will
     * @date 2025/5/7 09:59
     * @param entity
     * @param detailEntity
     * @return void
     */
    private void checkOverlap (PurchasePriceChangeDetailEntity entity,PurchasePriceDetailEntity detailEntity) {
        //区间重叠时
        if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
            //时间不能重叠
            boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
            if (overlap) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_DATE_OVERLAP,entity.getSkuNo());
            }
        }
        //时间重叠时
        boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
        if (overlap) {
            //区间不能重叠
            if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                    && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
                throw new ServiceException(ApiError.ERROR_INTERVAL_SUPPLIER_OVERLAP);
            }
        }
    }

    /**
     * 查询未审核通过数据
     * @author will
     * @date 2025/5/7 09:58
     * @param supplierIdList
     * @param purchaseOrgId
     * @param skuIdList
     * @return List<PurchasePriceChangeDetailEntity>
     */
    public List<PurchasePriceChangeDetailEntity> listCheckPurchasePriceChangeDetail(List<String> supplierIdList, String purchaseOrgId, List<String> skuIdList) {
        List<String> statusList = new ArrayList<>(4);
        statusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        statusList.add(ApproveStatusEnum.REJECT.getStatus());
        List<PurchasePriceChangeDetailEntity> list = baseMapper.listCheckPurchasePriceChangeDetail(supplierIdList, statusList, purchaseOrgId, skuIdList);
        return list;
    }
}
