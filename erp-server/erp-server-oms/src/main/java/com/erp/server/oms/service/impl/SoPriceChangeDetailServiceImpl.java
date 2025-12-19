package com.erp.server.oms.service.impl;


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
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品销售变更价 明细表 服务实现类
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
    private SoPriceChangeService soPriceChangeService;

    /**
     * 根据变更表id 获取明细
     * @param priceChangeId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceChangeDetailDTO.UpdateDTO>
     * @author will
     * @date 2025-03-28 14:35
     */
    @Override
    public List<SoPriceChangeDetailDTO.ViewDTO> getByPriceChangeId(String priceChangeId) {
        List<SoPriceChangeDetailEntity> list = this.getEntityByPriceChangeId(priceChangeId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<SoPriceChangeDetailDTO.ViewDTO> resultList = BeanMapper.copyList(list, SoPriceChangeDetailDTO.ViewDTO.class);
        List<String> changeDetailIdList = list.stream().map(SoPriceChangeDetailEntity::getId).collect(Collectors.toList());
        //销售价目详情表id
        List<String> soPriceDetailIds = resultList.stream().map(SoPriceChangeDetailDTO.ViewDTO::getSoPriceDetailId).collect(Collectors.toList());
        /**
         * 根据变更表id 获取到对应变更历史
         */
        List<SoPriceHistoryEntity> historyList = soPriceHistoryService.listByChangeDetailIdList(changeDetailIdList);
        //获取到对应的价目明细
        List<SoPriceDetailEntity> soPriceDetailList = soPriceDetailService.listByIds(soPriceDetailIds);

        BigDecimal hundred = MathUtil.BigDecimal_100;
        List<String> currencyIdList = resultList.stream().map(SoPriceChangeDetailDTO.ViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        for (SoPriceChangeDetailDTO.ViewDTO item : resultList) {
            String priceDetailId = item.getSoPriceDetailId();
            SoPriceHistoryEntity historyEntity = historyList.stream().filter(h -> h.getChangeDetailId().equals(item.getId())).findFirst().orElse(null);

            SoPriceDetailEntity priceDetailEntity = soPriceDetailList.stream().filter(p -> p.getId().equals(priceDetailId)).findFirst().orElse(null);
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
     * 添加销售价目变更明细
     * @param soPriceChangeId
     * @param soPriceChangeDetailList
     * @return void
     * @author will
     * @date 2025-03-28 16:09
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPriceChangeDetail(String soPriceChangeId, List<SoPriceChangeDetailDTO.AddDTO> soPriceChangeDetailList) {
        if (CollectionUtils.isEmpty(soPriceChangeDetailList)) {
            return;
        }
        //数据处理
        List<SoPriceChangeDetailEntity> addList = BeanMapper.copyList(soPriceChangeDetailList, SoPriceChangeDetailEntity.class);
        handlePriceChangeDetail(soPriceChangeId,addList);
        //数据验证
        checkPriceChangeDetail(soPriceChangeId,addList);
        this.saveBatch(addList);
    }
    /**
     * 处理销售价目变更明细
     * @param soPriceChangeId
     * @param addList
     * @return void
     */
    private void handlePriceChangeDetail (String soPriceChangeId,List<SoPriceChangeDetailEntity> addList) {
        //销售调价数据
        List<String> soPriceDetailIdList = addList.stream().map(SoPriceChangeDetailEntity::getSoPriceDetailId).distinct().collect(Collectors.toList());
        List<SoPriceDetailDTO.ViewDTO> viewList = soPriceDetailService.listBySoPriceDetailIds(soPriceDetailIdList);

        List<String> skuIds = addList.stream().map(SoPriceChangeDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        for (SoPriceChangeDetailEntity item : addList) {
            String skuId = item.getSkuId();
            skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().ifPresent(skuVO -> item.setSkuNo(skuVO.getSkuNo()));

            item.setMainId(soPriceChangeId);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                item.setTaxRate(rate);
            }
            //更新销售报价的编码
            viewList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), item.getSoPriceDetailId())).findFirst().ifPresent(obj -> {
                item.setSoPriceCode(obj.getPriceCode());
            });
        }
    }

    /**
     * 审核通过后 需要修改销售价目详情表的数据
     * @param soPriceChangeList
     * @return void
     * @author will
     * @date 2025-03-28 19:06
     */
    @Override
    public void updateSoPriceDetail(List<SoPriceChangeEntity> soPriceChangeList) {
        List<String> soPriceChangeIds = soPriceChangeList.stream().map(SoPriceChangeEntity::getId).collect(Collectors.toList());
        //获取到对应数据
        List<SoPriceChangeDetailEntity> list = this.getEntityByPriceChangeIds(soPriceChangeIds);
        List<String> soPriceDetailIdList = list.stream().map(SoPriceChangeDetailEntity::getSoPriceDetailId).collect(Collectors.toList());
        //获取销售价目详情集合
        List<SoPriceDetailEntity> soPriceDetailList = soPriceDetailService.listByIds(soPriceDetailIdList);
        List<SoPriceHistoryEntity> historyList = new ArrayList<>(soPriceDetailList.size());
        List<SoPriceDetailEntity> updateList = new ArrayList<>(soPriceDetailList.size());
        for (SoPriceDetailEntity item : soPriceDetailList) {
            //销售详情表id
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
     * @param soPriceChangeId
     * @param soPriceChangeDetailList
     * @return void
     * @author will
     * @date 2025-03-29 9:20
     */
    @Override
    public void updatePriceChangeDetail(String soPriceChangeId, List<SoPriceChangeDetailDTO.UpdateDTO> soPriceChangeDetailList) {
        if (CollectionUtils.isEmpty(soPriceChangeDetailList)) {
            return;
        }
        List<SoPriceChangeDetailEntity> dbList = this.getEntityByPriceChangeId(soPriceChangeId);
        //获取到要删除的id集合
        List<String> deleteIdList = getDeleteIds(soPriceChangeDetailList, dbList);
        List<SoPriceChangeDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        this.removeByIds(deleteIdList);
        //数据处理
        List<SoPriceChangeDetailEntity> saveOrUpdateList = BeanMapper.copyList(soPriceChangeDetailList, SoPriceChangeDetailEntity.class);
        handlePriceChangeDetail(soPriceChangeId,saveOrUpdateList);

        //数据验证
        checkPriceChangeDetail(soPriceChangeId,saveOrUpdateList);

        //这是要添加的
        List<SoPriceChangeDetailEntity> addList = saveOrUpdateList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //这是修改的
        List<SoPriceChangeDetailEntity> updateList = saveOrUpdateList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(soPriceChangeId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(soPriceChangeId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_PRICE_CHANGE.getCode(), addPairList, "编辑操作");

        //修改的
        for (SoPriceChangeDetailEntity update : updateList) {
            String id = update.getId();
            dbList.stream().filter(d -> d.getId().equals(id)).findFirst().ifPresent(old -> moduleOperateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.SO_PRICE.getCode(), soPriceChangeId, "", ""));
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
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

    /**
     * 审核通过验证
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
            List<String> soPriceDetailIdList = value.stream().map(SoPriceChangeDetailEntity::getSoPriceDetailId).collect(Collectors.toList());

            List<SoPriceDetailEntity> list = updateList.stream().filter(obj -> soPriceDetailIdList.contains(obj.getId())).collect(Collectors.toList());
            //报价信息验证
            soPriceDetailService.checkSoPriceDetail(entry.getKey(),SoPriceChangeList.get(0).getSoOrgId(),list);
        }
    }

    /**
     * 获取删除的id集合
     * @param SoPriceChangeDetailList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author will
     * @date 2025-03-29 9:33
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
        queryWrapper.orderByDesc(SoPriceChangeDetailEntity::getId);
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
     * 数据验证
     * @author Will
     * @date: 2024/1/15 17:41
     * @param list
     */
    private void checkPriceChangeDetail (String soPriceChangeId,List<SoPriceChangeDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (SoPriceChangeDetailEntity entity : list) {
            //检验失效时间需要大于等于生效时间
            if (entity.getExpireDate().isBefore(entity.getEffectiveDate())) {
                throw new ServiceException(ApiError.SO_PRICE_EXPIRE_BEFORE_EFFECTIVE,entity.getSkuNo());
            }
            //校验区间到需要大于区间从
            if (entity.getMaxQty().compareTo(entity.getMinQty()) < MathUtil.ZERO) {
                throw new ServiceException(ApiError.SO_PRICE_INTERVAL_INVALID,entity.getSkuNo());
            }
        }
        SoPriceChangeEntity soPriceChangeEntity = soPriceChangeService.getById(soPriceChangeId);
        if (ObjUtil.isEmpty(soPriceChangeEntity)) {
            throw new ServiceException(ApiError.PURCHASE_PRICE_CHANGE_NOT_FOUND);
        }
        //区间验证
        checkSoPriceChangeDetail(soPriceChangeEntity.getSoOrgId(),list);

    }

    /**
     * 数据校验
     * @author will
     * @date 2025/5/7 09:31
     * @param soOrgId
     * @param list
     * @return void
     */
    public void checkSoPriceChangeDetail (String soOrgId,List<SoPriceChangeDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //查询客户信息
        List<String> customerIdList = list.stream().map(SoPriceChangeDetailEntity::getCustomerId).distinct().collect(Collectors.toList());
        List<String> skuIdList = list.stream().map(SoPriceChangeDetailEntity::getSkuId).collect(Collectors.toList());
        //已存在的销售调价明细数据
        List<SoPriceChangeDetailEntity> soPriceChangeDetailList = listCheckSoPriceChangeDetail(customerIdList, soOrgId, skuIdList);
        if (CollectionUtils.isNotEmpty(soPriceChangeDetailList)) {
            List<String> oldIdList = list.stream().map(SoPriceChangeDetailEntity::getId).collect(Collectors.toList());
            soPriceChangeDetailList = soPriceChangeDetailList.stream().filter(obj -> !oldIdList.contains(obj.getId())).collect(Collectors.toList());
        }
        //销售价目表明细数据
        List<SoPriceDetailDTO.ViewDTO> soPriceDetailList = soPriceDetailService.listCheckSoPriceDetail(customerIdList, soOrgId, skuIdList);
        if (CollectionUtils.isNotEmpty(soPriceDetailList)) {
            List<String> priceDetailIdList = list.stream().map(SoPriceChangeDetailEntity::getSoPriceDetailId).collect(Collectors.toList());
            soPriceDetailList = soPriceDetailList.stream().filter(obj -> !priceDetailIdList.contains(obj.getId())).collect(Collectors.toList());
        }
        /**
         * 校验
         * 1、数据与新增同类数据校验
         * 2、数据与调价表未审核数据进行校验
         * 3、数据与价目表数据进行校验
         */
        for (int i = 0;i < list.size();i++) {
            SoPriceChangeDetailEntity entity = list.get(i);
            //检验失效时间需要大于生效时间
            if (entity.getExpireDate().isBefore(entity.getEffectiveDate())) {
                throw new ServiceException(ApiError.SO_PRICE_EXPIRE_BEFORE_EFFECTIVE,entity.getSkuNo());
            }
            //校验区间到需要大于区间从
            if (entity.getMaxQty().compareTo(entity.getMinQty()) <= MathUtil.ZERO) {
                throw new ServiceException(ApiError.SO_PRICE_INTERVAL_INVALID,entity.getSkuNo());
            }

            //1、数据与新增同类数据校验
            for (int j = 0;j < list.size();j++) {
                SoPriceChangeDetailEntity detailEntity = list.get(j);
                if (i == j || !StrUtil.equals(entity.getSkuId(),detailEntity.getSkuId())) {
                    continue;
                }
                //验证是否重叠
                checkOverlap(entity,detailEntity);
            }

            //2、数据与调价表未审核数据进行校验
            List<SoPriceChangeDetailEntity> oldList = soPriceChangeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCustomerId(),entity.getCustomerId()) &&  StrUtil.equals(obj.getSkuId(), entity.getSkuId())).map(obj -> BeanMapperUtils.map(SoPriceChangeDetailEntity.class,obj) ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(oldList)) {
                //验证是否重叠
                oldList.forEach(obj -> checkOverlap(entity,obj));
            }

            //3、数据与价目表数据进行校验
            List<SoPriceDetailEntity> oldPriceDetailList = soPriceDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCustomerId(),entity.getCustomerId()) &&  StrUtil.equals(obj.getSkuId(), entity.getSkuId())).map(obj -> BeanMapperUtils.map(SoPriceDetailEntity.class,obj) ).collect(Collectors.toList());
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
    private void checkOverlap (SoPriceChangeDetailEntity entity,SoPriceChangeDetailEntity detailEntity) {
        //区间重叠时
        if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
            //时间不能重叠
            boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
            if (overlap) {
                throw new ServiceException(ApiError.PURCHASE_PRICE_DATE_OVERLAP,entity.getSkuNo());
            }
        }
        //时间重叠时
        boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
        if (overlap) {
            //区间不能重叠
            if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                    && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
                throw new ServiceException(ApiError.SUPPLIER_INTERVAL_OVERLAP);
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
    private void checkOverlap (SoPriceChangeDetailEntity entity,SoPriceDetailEntity detailEntity) {
        //区间重叠时
        if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
            //时间不能重叠
            boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
            if (overlap) {
                throw new ServiceException(ApiError.PURCHASE_PRICE_DATE_OVERLAP,entity.getSkuNo());
            }
        }
        //时间重叠时
        boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
        if (overlap) {
            //区间不能重叠
            if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                    && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
                throw new ServiceException(ApiError.SUPPLIER_INTERVAL_OVERLAP);
            }
        }
    }

    /**
     * 查询未审核通过数据
     * @author will
     * @date 2025/5/7 09:58
     * @param customerIdList
     * @param soOrgId
     * @param skuIdList
     * @return List<SoPriceChangeDetailEntity>
     */
    public List<SoPriceChangeDetailEntity> listCheckSoPriceChangeDetail(List<String> customerIdList, String soOrgId, List<String> skuIdList) {
        List<String> statusList = new ArrayList<>(4);
        statusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        statusList.add(ApproveStatusEnum.REJECT.getStatus());
        List<SoPriceChangeDetailEntity> list = baseMapper.listCheckSoPriceChangeDetail(customerIdList, statusList, soOrgId, skuIdList);
        return list;
    }

}
