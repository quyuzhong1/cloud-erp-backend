package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDetailDTO;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoReturnNoticeDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoReturnNoticeDetailService;
import com.google.common.collect.Lists;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 销售退货通知单明细表 服务实现类
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@Service
public class SoReturnNoticeDetailServiceImpl extends SuperServiceImpl<SoReturnNoticeDetailMapper, SoReturnNoticeDetailEntity> implements SoReturnNoticeDetailService {

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnNoticeDTO.Add dto, String id) {
        String soReturnId = dto.getSourceId();
        //B2B退货订单明细集合
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByMainId(soReturnId);
        //B2B退货通知单
        List<SoReturnNoticeDetailEntity> noticeDetailEntities = listDetailBySourceIds(Collections.singletonList(soReturnId));
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        //查询sku
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Add::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIds);
        List<SoReturnNoticeDetailDTO.Add> detailList = dto.getDetailList();
        if(CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SO_RETURN_NOTICE_DETAIL_REQUIRED);
        }
        Map<String , Integer> returnDetailIdMap = new HashMap<>();
        for (SoReturnNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            ProductDetailEntity skuVO = productDetailEntitys.stream().filter(req -> req.getId().equals(detailDto.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            BeanMapper.copy(detailDto, detailEntity);
            detailEntity.setMainId(id);
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            if(StringUtils.isNotBlank(detailDto.getSourceDetailId())){
                //下推通知单
                SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                    throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_SKU_NOT_FOUND);
                }
                detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
                detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
                detailEntity.setReturnReasonDict(soReturnDetailEntity.getReturnReasonDict());
                detailEntity.setReturnTypeDict(soReturnDetailEntity.getReturnTypeDict());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            }else {
                ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(v -> v.getId().equals(detailDto.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                detailEntity.setSkuNo(productDetailEntity.getSkuNo());
                detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
                detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            }
            detailEntity.setIsChildSkuNo(detailDto.getIsChildSkuNo());
            detailEntity.setReturnAmount(detailDto.getReturnAmount());
            detailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            detailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            detailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            if(null == detailDto.getExchangeRate()){
                detailEntity.setExchangeRate(dto.getExchangeRate());
            }else{
                detailEntity.setExchangeRate(detailDto.getExchangeRate());
            }
            if(ignoreInventorySkuIds.contains(detailEntity.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", detailEntity.getSkuId(), detailEntity.getSkuNo());
            } else {
                if(Boolean.FALSE.equals(detailDto.getIsChildSkuNo()) && StringUtils.isNotBlank(detailDto.getSourceDetailId())){
                    //历史退货通知单的退货数量
                    Integer returnNoticeQty = noticeDetailEntities.stream()
                            .filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()))
                            .map(SoReturnNoticeDetailEntity::getReturnQty)
                            .reduce(MathUtil.ZERO, Integer::sum);
                    //退货单的退货数量
                    Integer returnQty = soReturnDetailEntities.stream()
                            .filter(req -> req.getId().equals(detailDto.getSourceDetailId()))
                            .map(SoReturnDetailEntity::getReturnQty)
                            .reduce(MathUtil.ZERO, Integer::sum);
                    //校验是否存在重复的明细并且数量大于退货数量
                    if(returnDetailIdMap.containsKey(detailDto.getSourceDetailId())){
                        Integer detailReturnQtySum = returnDetailIdMap.get(detailDto.getSourceDetailId()) + detailDto.getReturnQty();
                        if (returnQty <  detailReturnQtySum + returnNoticeQty) {
                            throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_QTY_EXCEEDS, skuVO.getSkuNo());
                        }
                        returnDetailIdMap.put(detailDto.getSourceDetailId(),detailReturnQtySum);
                    }else {
                        returnDetailIdMap.put(detailDto.getSourceDetailId(),detailDto.getReturnQty());
                    }
                    if (returnQty <  detailDto.getReturnQty() + returnNoticeQty) {
                        throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_QTY_EXCEEDS, skuVO.getSkuNo());
                    }else if(returnNoticeQty > 0 && returnQty == detailDto.getReturnQty() + returnNoticeQty){
                        // 退货通知单的数量之和等于退货订单数量，则需要对退货金额CNY，含税退货金额CNY，退货金额（本位币），含税退货金额（本位币）调整差值。
                        SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                        BigDecimal returnAmount = soReturnDetailEntity.getReturnAmount();
                        BigDecimal taxReturnAmount = soReturnDetailEntity.getTaxReturnAmount();
                        BigDecimal returnAmountLocalCurrency = soReturnDetailEntity.getReturnAmountLocalCurrency();
                        BigDecimal taxReturnAmountLocalCurrency = soReturnDetailEntity.getTaxReturnAmountLocalCurrency();
                        List<SoReturnNoticeDetailEntity> soReturnDetailEntityList = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).collect(Collectors.toList());
                        for (SoReturnNoticeDetailEntity soReturnNoticeDetail : soReturnDetailEntityList) {
                            returnAmount = returnAmount.subtract(soReturnNoticeDetail.getReturnAmount()) ;
                            taxReturnAmount = taxReturnAmount.subtract(soReturnNoticeDetail.getTaxReturnAmount());
                            returnAmountLocalCurrency = returnAmountLocalCurrency.subtract(soReturnNoticeDetail.getReturnAmountLocalCurrency());
                            taxReturnAmountLocalCurrency = taxReturnAmountLocalCurrency.subtract(soReturnNoticeDetail.getTaxReturnAmountLocalCurrency());
                        }
                        detailEntity.setReturnAmount(returnAmount);
                        detailEntity.setTaxReturnAmount(taxReturnAmount);
                        detailEntity.setReturnAmountLocalCurrency(returnAmountLocalCurrency);
                        detailEntity.setTaxReturnAmountLocalCurrency(taxReturnAmountLocalCurrency);
                    }
                }
            }
            list.add(detailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addB2c(SoReturnNoticeDTO.Add dto, String id) {

        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoB2cReturnDetailEntity> soReturnDetailEntities = FeignQuery.create(SoB2cReturnDetailEntity.class).in(SoB2cReturnDetailEntity::getId,returnDetailIds).list();
        List<String> soDetailIds = soReturnDetailEntities.stream().map(SoB2cReturnDetailEntity::getSoDetailId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = CollectionUtils.isEmpty(soDetailIds) ? Collections.emptyList()
                : FeignQuery.create(SoB2cDetailEntity.class).in(SoB2cDetailEntity::getId, soDetailIds).list();
        List<SoReturnNoticeDetailEntity> noticeDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }

        for (SoReturnNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            SoB2cReturnDetailEntity soB2cReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cReturnDetailEntity)) {
                throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_SKU_NOT_FOUND);
            }
            //退货通知单数量
            Integer returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //退货单数量
            Integer returnQty = soB2cReturnDetailEntity.getReturnQty();

            if(ignoreInventorySkuIds.contains(detailEntity.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", soB2cReturnDetailEntity.getSkuId(), soB2cReturnDetailEntity.getSkuNo());
            } else {
                if (returnQty <  detailDto.getReturnQty() + returnNoticeQty) {
                    throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_QTY_EXCEEDS);
                }
            }
            detailEntity.setMainId(id);
            detailEntity.setSkuId(soB2cReturnDetailEntity.getSkuId());
            detailEntity.setSkuNo(soB2cReturnDetailEntity.getSkuNo());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            detailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            fillB2cReturnNoticePrice(detailDto, detailEntity, soB2cReturnDetailEntity, soB2cDetailEntityList, returnNoticeQty, noticeDetailEntities);
            list.add(detailEntity);
        }
        return this.saveBatch(list);
    }

    /**
     * 填充 B2C 退货通知单明细金额。
     * <p>
     * 仅由 {@link #addB2c} 在单张单据 {@code @Transactional} 保存流程中调用；同循环内数量校验等异常亦直接抛出，
     * 不属于 {@code BatchResultDTO} 逐条容错场景。关联不到 B2C 销售订单明细时不允许跳过，否则会产生无金额明细。
     */
    private void fillB2cReturnNoticePrice(SoReturnNoticeDetailDTO.Add detailDto, SoReturnNoticeDetailEntity detailEntity,
                                          SoB2cReturnDetailEntity soB2cReturnDetailEntity, List<SoB2cDetailEntity> soB2cDetailEntityList,
                                          Integer returnNoticeQty, List<SoReturnNoticeDetailEntity> noticeDetailEntities) {
        if (Objects.nonNull(detailDto.getReturnAmount())) {
            detailEntity.setReturnAmount(detailDto.getReturnAmount());
            detailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            detailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            detailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            detailEntity.setExchangeRate(Objects.nonNull(detailDto.getExchangeRate()) ? detailDto.getExchangeRate() : detailEntity.getExchangeRate());
            return;
        }
        if (Objects.isNull(soB2cReturnDetailEntity)) {
            throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_SKU_NOT_FOUND, detailDto.getSourceDetailId());
        }
        SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream()
                .filter(req -> CharSequenceUtil.equals(req.getId(), soB2cReturnDetailEntity.getSoDetailId()))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(soB2cDetailEntity)) {
            log.warn("B2C销售订单明细不存在，sourceDetailId={}，soDetailId={}，skuNo={}",
                    detailDto.getSourceDetailId(), soB2cReturnDetailEntity.getSoDetailId(), soB2cReturnDetailEntity.getSkuNo());
            throw new ServiceException(ApiError.SO_B2C_DETAIL_NOT_FOUND);
        }
        BigDecimal exchangeRate = Objects.nonNull(detailDto.getExchangeRate()) ? detailDto.getExchangeRate() : soB2cDetailEntity.getExchangeRate();
        detailEntity.setExchangeRate(exchangeRate);
        BigDecimal price = Objects.nonNull(soB2cDetailEntity.getPrice()) ? soB2cDetailEntity.getPrice() : BigDecimal.ZERO;
        Integer returnQty = ObjectUtil.defaultIfNull(soB2cReturnDetailEntity.getReturnQty(), 0);
        if (returnQty <= 0) {
            return;
        }
        int pushedNoticeQty = ObjectUtil.defaultIfNull(returnNoticeQty, 0);
        boolean isLastBatch = pushedNoticeQty > 0 && Objects.equals(returnQty, pushedNoticeQty + detailDto.getReturnQty());
        BigDecimal lineReturnAmount = MathUtil.multiplyWithFour(price, BigDecimal.valueOf(returnQty));
        BigDecimal returnAmount;
        if (Objects.equals(returnQty, detailDto.getReturnQty())) {
            returnAmount = lineReturnAmount;
        } else if (isLastBatch) {
            returnAmount = subtractPushedReturnAmount(lineReturnAmount, detailDto.getSourceDetailId(), noticeDetailEntities,
                    SoReturnNoticeDetailEntity::getReturnAmount);
        } else {
            returnAmount = calReturnAmount(lineReturnAmount, returnQty, detailDto.getReturnQty());
        }
        detailEntity.setReturnAmount(returnAmount);
        detailEntity.setTaxReturnAmount(returnAmount);
        if (Objects.isNull(exchangeRate)) {
            throw new ServiceException(ApiError.SO_RETURN_EXCHANGE_RATE_REQUIRED, soB2cDetailEntity.getId());
        }
        BigDecimal lineReturnAmountLocalCurrency = MathUtil.multiplyWithFour(lineReturnAmount, exchangeRate);
        BigDecimal returnAmountLocalCurrency;
        BigDecimal taxReturnAmountLocalCurrency;
        if (isLastBatch) {
            returnAmountLocalCurrency = subtractPushedReturnAmount(lineReturnAmountLocalCurrency, detailDto.getSourceDetailId(), noticeDetailEntities,
                    SoReturnNoticeDetailEntity::getReturnAmountLocalCurrency);
            taxReturnAmountLocalCurrency = subtractPushedReturnAmount(lineReturnAmountLocalCurrency, detailDto.getSourceDetailId(), noticeDetailEntities,
                    SoReturnNoticeDetailEntity::getTaxReturnAmountLocalCurrency);
        } else {
            returnAmountLocalCurrency = MathUtil.multiplyWithFour(returnAmount, exchangeRate);
            taxReturnAmountLocalCurrency = MathUtil.multiplyWithFour(returnAmount, exchangeRate);
        }
        detailEntity.setReturnAmountLocalCurrency(returnAmountLocalCurrency);
        detailEntity.setTaxReturnAmountLocalCurrency(taxReturnAmountLocalCurrency);
    }

    private BigDecimal subtractPushedReturnAmount(BigDecimal totalAmount, String sourceDetailId,
                                                  List<SoReturnNoticeDetailEntity> noticeDetailEntities,
                                                  Function<SoReturnNoticeDetailEntity, BigDecimal> amountGetter) {
        if (Objects.isNull(totalAmount)) {
            log.warn("尾差吸收计算时总金额为 null，sourceDetailId={}，按 0 处理", sourceDetailId);
            return BigDecimal.ZERO;
        }
        BigDecimal remainAmount = totalAmount;
        for (SoReturnNoticeDetailEntity noticeDetail : noticeDetailEntities) {
            if (!CharSequenceUtil.equals(sourceDetailId, noticeDetail.getSourceDetailId())) {
                continue;
            }
            BigDecimal pushedAmount = amountGetter.apply(noticeDetail);
            remainAmount = remainAmount.subtract(ObjectUtil.defaultIfNull(pushedAmount, BigDecimal.ZERO));
        }
        return remainAmount;
    }

    /**
     * 按退货数量比例拆分单行金额（amount / qty * returnQty）。
     * 使用 {@link RoundingMode#DOWN} 截断，与 {@link com.erp.server.wms.service.impl.SoReturnNoticeServiceImpl#calReturnAmount} 保持一致；
     * 最后一批下推时的尾差由 {@link #fillB2cReturnNoticePrice} 按“原行金额减已下推累计”吸收。
     */
    private BigDecimal calReturnAmount(BigDecimal amount, Integer qty, Integer returnQty) {
        if (Objects.isNull(amount) || Objects.isNull(qty) || qty <= 0 || Objects.isNull(returnQty)) {
            return BigDecimal.ZERO;
        }
        return amount.divide(BigDecimal.valueOf(qty), 4, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(returnQty))
                .stripTrailingZeros();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnNoticeEntity entity,SoReturnNoticeDTO.Update dto) {
        if("B2C".equals(entity.getType())){
            return updateB2c(entity,dto);
        }else {
            return updateB2b(entity, dto);
        }
    }

    private boolean updateB2b(SoReturnNoticeEntity entity, SoReturnNoticeDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(SoReturnNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        String soReturnId = dto.getSourceId();
        //B2B退货订单明细集合
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByMainId(soReturnId);
        //B2B退货通知单
        List<SoReturnNoticeDetailEntity> noticeDetailEntities = listDetailBySourceIds(Collections.singletonList(soReturnId));
        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();
        //原明细数据
        List<SoReturnNoticeDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnNoticeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        //查询sku
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Update::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIds);

        List<SoReturnNoticeDetailDTO.Update> detailList = dto.getDetailList();
        if(CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SO_RETURN_NOTICE_DETAIL_REQUIRED);
        }
        for (SoReturnNoticeDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            if (CharSequenceUtil.isNotBlank(detailDto.getId())) {
                detailEntity.setId(detailDto.getId());
            }
            detailEntity.setReturnAmount(detailDto.getReturnAmount());
            detailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            detailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            detailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            if(null == detailDto.getExchangeRate()){
                detailEntity.setExchangeRate(dto.getExchangeRate());
            }else{
                detailEntity.setExchangeRate(detailDto.getExchangeRate());
            }
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            detailEntity.setSkuId(detailDto.getSkuId());
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(v -> v.getId().equals(detailDto.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            detailEntity.setSkuNo(productDetailEntity.getSkuNo());
            detailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            if(Boolean.FALSE.equals(detailDto.getIsChildSkuNo()) && StringUtils.isNotBlank(detailDto.getSourceDetailId())){
                SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                    throw new ServiceException(ApiError.SO_RETURN_DETAIL_SKU_NOT_FOUND);
                }
                //历史退货通知单的退货数量
                Integer returnNoticeQty = noticeDetailEntities.stream()
                        .filter(req -> !deleteIds.contains(req.getId()))
                        .filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()))
                        .map(SoReturnNoticeDetailEntity::getReturnQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                if(StringUtils.isNotBlank(detailDto.getId())){
                    returnNoticeQty = noticeDetailEntities.stream()
                            .filter(req -> !deleteIds.contains(req.getId()))
                            .filter(req -> !req.getId().equals(detailDto.getId()) && req.getSourceDetailId().equals(detailDto.getSourceDetailId()))
                            .map(SoReturnNoticeDetailEntity::getReturnQty)
                            .reduce(MathUtil.ZERO, Integer::sum);
                }
                //退货单的退货数量
                Integer returnQty = soReturnDetailEntities.stream()
                        .filter(req -> req.getId().equals(detailDto.getSourceDetailId()))
                        .map(SoReturnDetailEntity::getReturnQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                if (returnQty <  detailDto.getReturnQty() + returnNoticeQty) {
                    throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_QTY_EXCEEDS, soReturnDetailEntity.getSkuNo());
                }else if(returnNoticeQty > 0 && returnQty == detailDto.getReturnQty() + returnNoticeQty){
                    // 退货通知单的数量之和等于退货订单数量，则需要对退货金额CNY，含税退货金额CNY，退货金额（本位币），含税退货金额（本位币）调整差值。
                    BigDecimal returnAmount = soReturnDetailEntity.getReturnAmount();
                    BigDecimal taxReturnAmount = soReturnDetailEntity.getTaxReturnAmount();
                    BigDecimal returnAmountLocalCurrency = soReturnDetailEntity.getReturnAmountLocalCurrency();
                    BigDecimal taxReturnAmountLocalCurrency = soReturnDetailEntity.getTaxReturnAmountLocalCurrency();
                    List<SoReturnNoticeDetailEntity> soReturnDetailEntityList = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).collect(Collectors.toList());
                    for (SoReturnNoticeDetailEntity soReturnNoticeDetail : soReturnDetailEntityList) {
                        returnAmount = returnAmount.subtract(soReturnNoticeDetail.getReturnAmount()) ;
                        taxReturnAmount = taxReturnAmount.subtract(soReturnNoticeDetail.getTaxReturnAmount());
                        returnAmountLocalCurrency = returnAmountLocalCurrency.subtract(soReturnNoticeDetail.getReturnAmountLocalCurrency());
                        taxReturnAmountLocalCurrency = taxReturnAmountLocalCurrency.subtract(soReturnNoticeDetail.getTaxReturnAmountLocalCurrency());
                    }
                    detailEntity.setReturnAmount(returnAmount);
                    detailEntity.setTaxReturnAmount(taxReturnAmount);
                    detailEntity.setReturnAmountLocalCurrency(returnAmountLocalCurrency);
                    detailEntity.setTaxReturnAmountLocalCurrency(taxReturnAmountLocalCurrency);
                }
            }
            detailEntity.setMainId(dto.getId());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            detailEntity.setIsChildSkuNo(detailDto.getIsChildSkuNo());
            list.add(detailEntity);
            //修改操作日志
            if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                SoReturnNoticeDetailEntity old = this.getById(detailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnNoticeDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }

    private Boolean updateB2c(SoReturnNoticeEntity entity,SoReturnNoticeDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(SoReturnNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        //获取退货单详情表id
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnNoticeDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
        List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.getByIds(SoB2cReturnDetailEntity.class,returnDetailIds);

        List<SoReturnNoticeDetailEntity> list = new ArrayList<>();
        List<SoReturnNoticeDetailEntity> noticeDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
        //原明细数据
        List<SoReturnNoticeDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnNoticeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        for (SoReturnNoticeDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnNoticeDetailEntity detailEntity = new SoReturnNoticeDetailEntity();
            //退货通知单数量
            Integer returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            if (CharSequenceUtil.isNotBlank(detailDto.getId())) {
                detailEntity.setId(detailDto.getId());
                returnNoticeQty = noticeDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            if("B2C".equals(entity.getType())){
                SoB2cReturnDetailEntity soReturnDetailEntity = soB2cReturnDetailEntityList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                    throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_SKU_NOT_FOUND);
                }
                //退货单数量
                Integer returnQty = soB2cReturnDetailEntityList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoB2cReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                if (returnQty <  detailDto.getReturnQty() + returnNoticeQty) {
                    throw new ServiceException(ApiError.SO_DELIVERY_RETURN_ORDER_QTY_EXCEEDS);
                }
                detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
                detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
            }
            detailEntity.setMainId(dto.getId());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(detailEntity);
            //修改操作日志
            if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                SoReturnNoticeDetailEntity old = this.getById(detailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnNoticeDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }

    private List<String> getDeleteIds(List<SoReturnNoticeDetailDTO.Update> newList, List<SoReturnNoticeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(SoReturnNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReturnNoticeDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnNoticeDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnNoticeDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoReturnNoticeDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoReturnNoticeDetailEntity> listDetailByMainId(String id) {
        return lambdaQuery().eq(SoReturnNoticeDetailEntity::getMainId, id).list();
    }

    @Override
    public List<SoReturnNoticeDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }

}
