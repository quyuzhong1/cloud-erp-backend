package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.entity.SoChangeDetailEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.enums.SoChangeTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoChangeDetailMapper;
import com.erp.server.oms.service.SoChangeDetailService;
import com.erp.server.oms.service.SoDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单变更明细 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoChangeDetailServiceImpl extends SuperServiceImpl<SoChangeDetailMapper, SoChangeDetailEntity> implements SoChangeDetailService {

    @Resource
    private SoDetailService soDetailService;


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    /**
     * 添加变更详情信息
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-24 14:12
     */
    @Override
    public void addDetailList(String mainId, List<SoChangeDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SoChangeDetailEntity> addList = new ArrayList<>(detailList.size());
        //销售订单的详情id 集合
        List<String> soDetailIdList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getSoDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollectionUtils.isNotEmpty(soDetailIdList) ? soDetailService.listByIds(soDetailIdList) : Collections.emptyList();
        //币种列表
        List<String> currencyList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);
        List<String> skuIdList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        for (SoChangeDetailDTO.AddDTO item : detailList) {
            SoChangeDetailEntity soChangeDetail = new SoChangeDetailEntity();
            String skuId = item.getSkuId();
            //销售订单详情
            String soDetailId = item.getSoDetailId();
            //原来的销售订单
            SoDetailEntity soDetail = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).findFirst().orElse(null);
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (isGift) {
                price = BigDecimal.ZERO;
            }
            String currency = item.getCurrency();
            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");

            //金额
            BigDecimal amount = MathUtil.multiply(price, qty);
            soChangeDetail.setIsGift(isGift);
            soChangeDetail.setPrice(price);
            soChangeDetail.setCurrency(currency);
            soChangeDetail.setCurrencySymbol(symbol);
            soChangeDetail.setIsReissue(item.getIsReissue());
            soChangeDetail.setAmount(amount);
            soChangeDetail.setMainId(mainId);
            soChangeDetail.setSkuId(skuId);
            soChangeDetail.setRemark(item.getRemark());
            soChangeDetail.setChangeType(item.getChangeType());
            soChangeDetail.setQty(item.getQty());
            soChangeDetail.setTaxRate(item.getTaxRate());
            soChangeDetail.setOldPrice(soDetail != null ? soDetail.getPrice() : BigDecimal.ZERO);
            soChangeDetail.setOldAmount(soDetail != null ? soDetail.getAmount() : BigDecimal.ZERO);
            soChangeDetail.setOldCurrency(soDetail != null ? soDetail.getCurrency() : "");
            soChangeDetail.setOldCurrencySymbol(soDetail != null ? soDetail.getCurrencySymbol() : "");
            soChangeDetail.setOldQty(soDetail != null ? soDetail.getQty() : 0);
            soChangeDetail.setOldTaxRate(soDetail != null ? soDetail.getTaxRate() : BigDecimal.ZERO);
            soChangeDetail.setSoDetailId(soDetailId);
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            soChangeDetail.setSkuNo(skuNo);
            addList.add(soChangeDetail);
        }
        this.saveBatch(addList);


    }


    /**
     * 根据主表id 获取详情信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-25 9:00
     */
    @Override
    public List<SoChangeDetailDTO.ViewDTO> listDetailByMainId(String mainId) {
        List<SoChangeDetailEntity> dbList = this.listDetailDbByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            throw new ServiceException(ApiError.ERROR_92036);
        }
        List<SoChangeDetailDTO.ViewDTO> viewList = BeanMapper.copyList(dbList, SoChangeDetailDTO.ViewDTO.class);
        List<String> skuIdList = viewList.stream().map(SoChangeDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SoChangeDetailDTO.ViewDTO item : viewList) {
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            String unit = "";
            String productName = "";
            if (sku != null) {
                unit = sku.getUnitName();
                productName = sku.getSkuName();
            }
            item.setUnit(unit);
            item.setProductName(productName);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            //单价
            BigDecimal price = item.getPrice();
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(taxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            item.setTaxPrice(taxPrice);


            BigDecimal oldPrice = item.getOldPrice();
            BigDecimal oldTaxRate = item.getOldTaxRate();
            //含税单价=销售单价*（税率+1）
            BigDecimal oldMultiplyTax = MathUtil.add(oldTaxRate, MathUtil.BigDecimal_1);
            BigDecimal oldTaxPrice = MathUtil.multiply(oldPrice, oldMultiplyTax);
            item.setOldPrice(oldTaxPrice);
        }
        return viewList;
    }


    /**
     * 检查对应的变更类型
     *
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-25 10:14
     */
    @Override
    public void checkChange(List<SoChangeDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isNotEmpty(detailList)) {
            String deleteCode = SoChangeTypeEnum.DELETE.getCode();
            List<SoChangeDetailDTO.AddDTO> deleteDetailList = detailList.stream().filter(d -> d.getChangeType().getCode().equals(deleteCode)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteDetailList)) {
                List<String> soDetailIdList = deleteDetailList.stream().
                        map(SoChangeDetailDTO.AddDTO::getSoDetailId).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(soDetailIdList)) {
                    throw new ServiceException(ApiError.ERROR_92015);
                }
                //下推单据的数量
                Integer pushDownCount = wmsTaskFeign.getPushDownBySoDetailIds(soDetailIdList);
                if (pushDownCount > 0) {
                    throw new ServiceException(ApiError.ERROR_92015);

                }
            }
        }


    }


    /**
     * 根据主表删除明细
     *
     * @param mainIds
     * @return void
     * @author yl
     * @date 2023-05-25 11:11
     */
    @Override
    public void removeByMainIdList(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return;
        }
        LambdaQueryWrapper<SoChangeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SoChangeDetailEntity::getMainId, mainIds);
        this.remove(queryWrapper);

    }


    /**
     * 更改销售变更详情
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-25 12:03
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetailList(String mainId, List<SoChangeDetailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SoChangeDetailEntity> saveOrUpdateList = new ArrayList<>(detailList.size());
        //这是修改的
        List<SoChangeDetailDTO.UpdateDTO> updateList = detailList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<SoChangeDetailDTO.UpdateDTO> addList = detailList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<SoChangeDetailEntity> updateEntityList = BeanMapper.copyList(updateList, SoChangeDetailEntity.class);
        //这个是要添加的
        List<SoChangeDetailEntity> addEntityList = BeanMapper.copyList(addList, SoChangeDetailEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<SoChangeDetailEntity> dbList = this.listDetailDbByMainId(mainId);
        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        List<SoChangeDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> skuIdList = detailList.stream().map(SoChangeDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //币种列表
        List<String> currencyList = detailList.stream().map(SoChangeDetailDTO.UpdateDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);

        //销售订单的详情id 集合
        List<String> soDetailIdList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getSoDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollectionUtils.isNotEmpty(soDetailIdList) ? soDetailService.listByIds(soDetailIdList) : Collections.emptyList();

        for (SoChangeDetailEntity item : saveOrUpdateList) {
            String skuId = item.getSkuId();
            //销售订单详情
            String soDetailId = item.getSoDetailId();
            //原来的销售订单
            SoDetailEntity soDetail = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).findFirst().orElse(null);
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (isGift) {
                price = BigDecimal.ZERO;
            }
            String currency = item.getCurrency();
            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            //金额
            BigDecimal amount = MathUtil.multiply(price, qty);
            item.setPrice(price);
            item.setCurrencySymbol(symbol);
            item.setAmount(amount);
            item.setMainId(mainId);
            item.setOldPrice(soDetail != null ? soDetail.getPrice() : BigDecimal.ZERO);
            item.setOldAmount(soDetail != null ? soDetail.getAmount() : BigDecimal.ZERO);
            item.setOldCurrency(soDetail != null ? soDetail.getCurrency() : "");
            item.setOldCurrencySymbol(soDetail != null ? soDetail.getCurrencySymbol() : "");
            item.setOldQty(soDetail != null ? soDetail.getQty() : 0);
            item.setOldTaxRate(soDetail != null ? soDetail.getTaxRate() : BigDecimal.ZERO);
            item.setSoDetailId(soDetailId);
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            item.setSkuNo(skuNo);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 获取到删除的数据
     *
     * @param pairList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-25 12:07
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<SoChangeDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SoChangeDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<SoChangeDetailEntity> listDetailDbByMainId(String mainId) {
        return this.lambdaQuery().eq(SoChangeDetailEntity::getMainId, mainId).list();
    }
}
