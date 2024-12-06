package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.*;
import com.erp.server.plm.mapper.MouldDetailMapper;
import com.erp.server.plm.service.MouldDetailService;
import com.erp.server.plm.service.MouldProductService;
import com.erp.server.plm.service.MouldPurchasePriceService;
import com.erp.server.plm.service.MouldRefundAgreementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
    @Override
    public void add(List<MouldInfoDTO.DetailDTO> detailList, MouldInfoEntity entity) {

        List<MouldDetailEntity> details = new ArrayList<>();
        List<MouldRefundAgreementEntity> agreementList = new ArrayList<>();
        List<MouldPurchasePriceEntity> purchasePriceList = new ArrayList<>();
        List<MouldProductEntity> productList = new ArrayList<>();
        for (MouldInfoDTO.DetailDTO dto : detailList) {
            MouldDetailEntity mouldDetail = BeanMapperUtils.map(MouldDetailEntity.class, dto);
            mouldDetail.setMainId(entity.getId());
            String code = docNoGenHelper.generateCode(entity.getMoldCategoryCode());
            mouldDetail.setMouldNo(code);
            mouldDetail.setId(IdWorker.getIdStr());
            details.add(mouldDetail);
            MouldRefundAgreementEntity agreement = BeanMapperUtils.map(MouldRefundAgreementEntity.class, dto);
            agreement.setMouldDetailId(mouldDetail.getId());
            agreementList.add(agreement);
            MouldPurchasePriceEntity price = BeanMapperUtils.map(MouldPurchasePriceEntity.class, dto);
            price.setMouldDetailId(mouldDetail.getId());
            price.setCurrency(CurrencyEnum.RMB.getCurrencyCode());
            price.setExchangeRate(BigDecimal.ONE);
            purchasePriceList.add(price);
            List<MouldProductEntity> mouldProductList = dto.getProductList().stream()
                    .map(v -> {
                        MouldProductEntity mouldProduct = new MouldProductEntity();
                        mouldProduct.setProductName(v.getProductName());
                        mouldProduct.setMouldDetailId(mouldDetail.getId());
                        mouldProduct.setImagesUrl(v.getImagesUrl());
                        return mouldProduct;
                    }).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(mouldProductList)) {
                productList.addAll(mouldProductList);
            }
        }
        ApplicationContextUtils.getBean(MouldDetailServiceImpl.class).saveBatch(details);
        mouldRefundAgreementService.saveBatch(agreementList);
        mouldPurchasePriceService.saveBatch(purchasePriceList);
        mouldProductService.saveBatch(productList);
    }
}