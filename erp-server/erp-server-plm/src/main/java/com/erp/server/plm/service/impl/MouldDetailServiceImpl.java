package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.config.DocNoGenHelper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.MouldDetailDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.MouldRefundStatusEnum;
import com.erp.server.plm.mapper.MouldDetailMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 模具明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */

@Slf4j
@Service
public class MouldDetailServiceImpl extends SuperServiceImpl<MouldDetailMapper, MouldDetailEntity> implements MouldDetailService {

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private MouldProductService mouldProductService;
    @Resource
    private MouldPurchasePriceService mouldPurchasePriceService;
    @Resource
    private MouldRefundAgreementService mouldRefundAgreementService;
    @Resource
    private MouldRefProductService mouldRefProductService;

    @Override
    public void add(List<MouldDetailDTO.UpdateDTO> detailList, MouldInfoEntity entity, boolean isDraft) {
        verifyData(detailList, isDraft);
        List<MouldDetailEntity> mouldDetailList = list(Wrappers.<MouldDetailEntity>lambdaQuery().eq(MouldDetailEntity::getMainId, entity.getId()));
        List<String> detailIds = mouldDetailList.stream().map(MouldDetailEntity::getId).collect(Collectors.toList());
        //过滤已返的数据编辑合同返还约定
        List<String> refundIds = mouldRefundAgreementService.listByMouldDetailIdListAndStatus(detailIds, MouldRefundStatusEnum.RETURNED.getCode());
        if (!CollectionUtils.isEmpty(detailIds)) {
            mouldProductService.remove(Wrappers.<MouldProductEntity>lambdaQuery().in(MouldProductEntity::getMouldDetailId, detailIds));
            mouldPurchasePriceService.remove(Wrappers.<MouldPurchasePriceEntity>lambdaQuery().in(MouldPurchasePriceEntity::getMouldDetailId, detailIds));
            mouldRefundAgreementService.remove(Wrappers.<MouldRefundAgreementEntity>lambdaQuery()
                    .ne(MouldRefundAgreementEntity::getRefundStatus, MouldRefundStatusEnum.RETURNED.getCode())
                    .notIn(!CollectionUtils.isEmpty(refundIds), MouldRefundAgreementEntity::getMouldDetailId, refundIds)
                    .in(MouldRefundAgreementEntity::getMouldDetailId, detailIds));
            mouldRefProductService.remove(Wrappers.<MouldRefProductEntity>lambdaQuery().in(MouldRefProductEntity::getMouldDetailId, detailIds));
            List<String> removeIds = detailIds.stream().
                    filter(id -> detailList.stream().noneMatch(v -> Objects.equals(v.getId(), id)))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(removeIds)) {
                removeByIds(removeIds);
            }
        }
        List<MouldDetailEntity> details = new ArrayList<>();
        List<MouldRefundAgreementEntity> agreementList = new ArrayList<>();
        List<MouldPurchasePriceEntity> purchasePriceList = new ArrayList<>();
        List<MouldProductEntity> productList = new ArrayList<>();
        List<MouldRefProductEntity> mouldRefProductList = new ArrayList<>();
        for (MouldDetailDTO.UpdateDTO dto : detailList) {
            MouldDetailEntity mouldDetail = BeanMapperUtils.map(MouldDetailEntity.class, dto);
            if (ObjectUtils.isEmpty(dto.getId())) {
                mouldDetail.setMainId(entity.getId());
                String code = docNoGenHelper.generateMouldDetailCode(entity.getMouldCategoryCode());
                mouldDetail.setMouldNo(code);
                dto.setMouldNo(code);
                mouldDetail.setId(IdWorker.getIdStr());
            }
            details.add(mouldDetail);
            if (!refundIds.contains(dto.getId())) {
                MouldRefundAgreementEntity agreement = BeanMapperUtils.map(MouldRefundAgreementEntity.class, dto);
                agreement.setMouldDetailId(mouldDetail.getId());
                agreement.setId(null);
                agreementList.add(agreement);
            }
            MouldPurchasePriceEntity price = BeanMapperUtils.map(MouldPurchasePriceEntity.class, dto);
            price.setMouldDetailId(mouldDetail.getId());
            price.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            price.setExchangeRate(BigDecimal.ONE);
            price.setId(null);
            purchasePriceList.add(price);
            List<MouldProductEntity> mouldProductList = Optional.ofNullable(dto.getProductList())
                    .orElse(new ArrayList<>()).stream()
                    .map(v -> {
                        MouldProductEntity mouldProduct = new MouldProductEntity();
                        mouldProduct.setProductName(v.getProductName());
                        mouldProduct.setMouldDetailId(mouldDetail.getId());
                        mouldProduct.setTypeId(v.getTypeId());
                        mouldProduct.setLength(v.getLength());
                        mouldProduct.setHeight(v.getHeight());
                        mouldProduct.setWidth(v.getWidth());
                        mouldProduct.setMouldHoles(v.getMouldHoles());
                        mouldProduct.setMaterial(v.getMaterial());
                        if (!CollectionUtils.isEmpty(v.getImagesUrl())) {
                            mouldProduct.setImagesUrl(String.join(",", v.getImagesUrl()));
                        }
                        mouldProduct.setId(null);
                        return mouldProduct;
                    }).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(mouldProductList)) {
                productList.addAll(mouldProductList);
            }
            List<MouldRefProductEntity> productEntityList = Optional.ofNullable(dto.getRefProductList())
                    .orElse(new ArrayList<>()).stream()
                    .map(v -> {
                        MouldRefProductEntity product = new MouldRefProductEntity();
                        product.setMouldDetailId(mouldDetail.getId());
                        product.setSkuId(v.getSkuId());
                        product.setSupplierId(v.getSupplierId());
                        product.setSkuNo(v.getSkuNo());
                        product.setId(null);
                        return product;
                    }).collect(Collectors.toList());
            mouldRefProductList.addAll(productEntityList);
        }
        ApplicationContextUtils.getBean(MouldDetailServiceImpl.class).saveOrUpdateBatch(details);
        mouldRefundAgreementService.saveBatch(agreementList);
        mouldPurchasePriceService.saveBatch(purchasePriceList);
        mouldProductService.saveBatch(productList);
        mouldRefProductService.saveBatch(mouldRefProductList);
    }

    private void verifyData(List<MouldDetailDTO.UpdateDTO> detailList, boolean isDraft) {
        for (MouldDetailDTO.UpdateDTO dto : detailList) {
            if (Boolean.TRUE.equals(dto.getIsNeedRefund()) && Boolean.FALSE.equals(isDraft)) {
                StringBuilder sb = new StringBuilder();
                if (CollectionUtils.isEmpty(dto.getRefProductList())) {
                    sb.append("关联下单sku不能为空，");
                }
                if (ObjectUtils.isEmpty(dto.getRefundAmount())) {
                    sb.append("返还金额不能为空，");
                }
                if (ObjectUtils.isEmpty(dto.getRefundOrderQty())) {
                    sb.append("返还单量不能为空，");
                }
                if (ObjectUtils.isEmpty(dto.getRefundStandard())) {
                    sb.append("返还标准不能为空，");
                }
                if (!ObjectUtils.isEmpty(sb.toString())) {
                    throw new ServiceException(sb.insert(0, "费用返还为是时").toString());
                }
            }
        }
    }

    @Override
    public List<MouldDetailDTO.ViewDTO> listByMouldId(String id) {

        List<MouldDetailEntity> list = list(Wrappers.<MouldDetailEntity>lambdaQuery().eq(MouldDetailEntity::getMainId, id));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<String> detailIds = list.stream().map(MouldDetailEntity::getId).collect(Collectors.toList());
        List<MouldProductEntity> mouldProductList = mouldProductService.listByMouldDetailIdList(detailIds);
        List<MouldPurchasePriceEntity> mouldPurchasePriceList = mouldPurchasePriceService.listByMouldDetailIdList(detailIds);
        List<MouldRefundAgreementEntity> agreementList = mouldRefundAgreementService.listByMouldDetailIdList(detailIds);
        List<MouldRefProductEntity> mouldRefProductList = mouldRefProductService.listByMouldDetailIdList(detailIds);
        List<MouldDetailDTO.ViewDTO> result = new ArrayList<>();
        for (MouldDetailEntity entity : list) {
            List<MouldProductEntity> productList = mouldProductList.stream()
                    .filter(v -> v.getMouldDetailId().equals(entity.getId()))
                    .collect(Collectors.toList());
            List<MouldRefProductEntity> refList = mouldRefProductList.stream()
                    .filter(v -> v.getMouldDetailId().equals(entity.getId()))
                    .collect(Collectors.toList());
            MouldPurchasePriceEntity purchasePrice = mouldPurchasePriceList.stream()
                    .filter(v -> v.getMouldDetailId().equals(entity.getId()))
                    .findFirst()
                    .orElse(new MouldPurchasePriceEntity());
            MouldRefundAgreementEntity refundAgreement = agreementList.stream()
                    .filter(v -> v.getMouldDetailId().equals(entity.getId()))
                    .findFirst()
                    .orElse(new MouldRefundAgreementEntity());
            result.add(MouldDetailDTO.ViewDTO.buildView(entity, purchasePrice, refundAgreement, refList, productList));
        }
        return result;
    }
}