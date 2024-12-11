package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncSoB2cService;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.OmsPushMsgService;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SyncSoB2cServiceImpl implements SyncSoB2cService {
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private OmsPushMsgService omsPushMsgService;
    @Resource
    private DictBasicService dictBasicService;

    @Override
    public Map<String, Object> syncDataToSdyFieldHandler(SoB2cEntity soB2cEntity,
                                                         List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                         SoB2cDetailEntity soB2cDetailEntity,
                                                         String operate,
                                                         List<SkuVO> skuVOList,
                                                         List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                         List<ProductDetailEntity> parentSkuList,
                                                         List<ListingInfoEntity> listingInfoEntities,
                                                         List<CurrencyDTO.ViewDTO> currencyList,
                                                         List<DictCurrencyEntity> dictCurrencyEntities,
                                                         List<ShopInfoEntity> shopInfoList,
                                                         List<CustomerInfoEntity> customerInfoList,
                                                         List<BaseIdDTO.CodeDTO> companyEntities,
                                                         List<DictBasicEntity> dictBasicEntityList,
                                                         List<DictBasicEntity> dictList) {

        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //优惠额
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal shareTotalDiscount = BigDecimal.ZERO;
        if (soB2cEntity.getTotalDiscount() != null) {
            totalDiscount = soB2cEntity.getTotalDiscount();
        }

        //总售价
        BigDecimal totalAmount = soB2cEntity.getAmount();

        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

        shudiyunB2cOrderDTO.setBiz_uni_key(soB2cEntity.getId() + soB2cDetailEntity.getId());
        shudiyunB2cOrderDTO.setBiz_no(soB2cEntity.getCode());
        if (soB2cEntity.getPayTime() != null) {
            shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(soB2cEntity.getPayTime()));
        }
        // 平台订单：默认配货单  手工单：默认线下订单
        if (SourceTypeEnum.SELF_ADD.getCode().equals(soB2cEntity.getSourceType())) {
            shudiyunB2cOrderDTO.setTransaction_type("线下订单");

            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.getName(soB2cEntity.getTransactionSubType()));
        } else {
            shudiyunB2cOrderDTO.setTransaction_type("配货单");
            shudiyunB2cOrderDTO.setTransaction_sub_type("配货单");
        }
        if (CharSequenceUtil.isBlank(soB2cEntity.getBillStatus())) {
            soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        }
        shudiyunB2cOrderDTO.setBiz_status(SoB2cBillStatusEnum.getName(soB2cEntity.getBillStatus()));

        shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operate, soB2cEntity.getVersion(), soB2cDetailEntity.getVersion()));
        shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(soB2cEntity.getAmount());
        //总优惠金额
        shudiyunB2cOrderDTO.setDiscount_deduction_amount(soB2cEntity.getTotalDiscount());

        Integer totalQty = soB2cDetailEntityList.stream().mapToInt(SoB2cDetailEntity::getQty).sum();
        shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);
        shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty);

        //取消金额、数量
        if (soB2cEntity.getDictPlatform().equals(PlatformDictEnum.ALI_EXPRESS.getCode())
                || soB2cEntity.getDictPlatform().equals(PlatformDictEnum.SHOPEE.getCode())
        ) {
            shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(soB2cEntity.getTotalCancelGoodsAmount());
        } else {
            if (Boolean.TRUE.equals(soB2cEntity.getIsCancel()) && soB2cEntity.getIsCancel() != null) {
                BigDecimal totalCancelGoodsAmount = soB2cDetailEntityList.stream().map(req -> req.getAmount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(totalCancelGoodsAmount);

                // 取消商品数量（合计）
                shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
            }
        }

        shudiyunB2cOrderDTO.setBuyer_actual_payment(soB2cEntity.getPayAmount());
        shudiyunB2cOrderDTO.setTotal_freight(soB2cEntity.getShippingFee());
        String customerId = "";
        ShopInfoEntity shopInfo = shopInfoList.stream().filter(req -> req.getId().equals(soB2cEntity.getShopId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(shopInfo)) {
            String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(shopInfo.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
            shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);
            shudiyunB2cOrderDTO.setShop_no(shopInfo.getId());
            shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());
            DictCurrencyEntity dictCurrencyEntity = dictCurrencyEntities.stream().filter(req -> req.getId().equals(shopInfo.getTradeCurrency())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                shudiyunB2cOrderDTO.setTransaction_currency(dictCurrencyEntity.getName());
            }
            shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
            shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
            shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());
            customerId = shopInfo.getCustomerId();
        }

        String finalCustomerId = customerId;
        CustomerInfoEntity customerInfo = customerInfoList.stream().filter(req -> req.getId().equals(finalCustomerId)).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(customerInfo)) {
            BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
            }
            String subPlatformType = customerInfo.getPlatformType();
            if (StringUtils.isNotBlank(subPlatformType)) {
                DictBasicEntity dictBasicEntity = dictList.stream().filter(req -> req.getName().equals(subPlatformType)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(dictBasicEntity)) {
                    shudiyunB2cOrderDTO.setSubplatform_no(dictBasicEntity.getValue());
                    shudiyunB2cOrderDTO.setSubplatform_name(dictBasicEntity.getName());
                }
            }
        }

        shudiyunB2cOrderDTO.setPlatform_id(soB2cEntity.getDictPlatform());
        String platformName = dictBasicEntityList.stream().filter(req -> req.getValue().equals(customerInfo.getPlatformType())).map(DictBasicEntity::getName).findFirst().orElse("");
        shudiyunB2cOrderDTO.setPlatform_name(platformName);
        shudiyunB2cOrderDTO.setRoot_node_no(soB2cEntity.getPlatformCode());
        if (soB2cEntity.getPayTime() != null) {
            shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(soB2cEntity.getPayTime()));
        }
        shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(soB2cEntity.getUpdateTime()));
        shudiyunB2cOrderDTO.setGoods_no(soB2cDetailEntity.getSkuNo());
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(soB2cDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
        shudiyunB2cOrderDTO.setGoods_name(skuVO.getSkuName());

        if (CharSequenceUtil.isNotBlank(skuVO.getSpuNo())) {
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSpuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSpuName());
        } else {
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSkuName());
        }

        BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(soB2cDetailEntity.getSkuId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
            shudiyunB2cOrderDTO.setIs_comb(1);
            shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getSkuNo());
            shudiyunB2cOrderDTO.setSuite_name(bomChildrenSkuDTO.getSkuName());
        } else {
            bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(soB2cDetailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO)) {
                shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getParentSkuNo());
                BomChildrenSkuDTO finalBomChildrenSkuDTO = bomChildrenSkuDTO;
                String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
                shudiyunB2cOrderDTO.setSuite_name(skuName);
            } else {
                shudiyunB2cOrderDTO.setSuite_no(skuVO.getSkuNo());
                shudiyunB2cOrderDTO.setSuite_name(skuVO.getSkuName());
            }
        }

        shudiyunB2cOrderDTO.setSuite_no(skuVO.getSkuNo());
        shudiyunB2cOrderDTO.setSuite_name(skuVO.getSkuName());
        shudiyunB2cOrderDTO.setRemark(soB2cEntity.getRemark());
        shudiyunB2cOrderDTO.setGoods_status("未发货");
        // 商品状态
        if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
            shudiyunB2cOrderDTO.setGoods_status("已发货");
        }

        if (Boolean.TRUE.equals(soB2cEntity.getIsCancel()) && soB2cEntity.getIsCancel() != null) {
            shudiyunB2cOrderDTO.setGoods_status("已取消");
            shudiyunB2cOrderDTO.setBiz_status("已取消");
        }

        shudiyunB2cOrderDTO.setGoods_transaction_quantity(soB2cDetailEntity.getQty());
        shudiyunB2cOrderDTO.setUnit(skuVO.getUnitName());

        shudiyunB2cOrderDTO.setGoods_transaction_amount(soB2cDetailEntity.getAmount());
        if (soB2cDetailEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal shareDiscount = BigDecimal.ZERO;
            //获得售价占比分摊的商品优惠额
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                shareDiscount = soB2cDetailEntity.getAmount().divide(totalAmount, 4, RoundingMode.DOWN).multiply(totalDiscount);
            }
            //计算为真实售价(原始币别)-商品分摊优惠/订单数量
            shudiyunB2cOrderDTO.setPrice(soB2cDetailEntity.getAmount().subtract((totalDiscount.subtract(shareTotalDiscount))).divide(MathUtil.valueOf(soB2cDetailEntity.getQty()), 4, RoundingMode.DOWN));
            shudiyunB2cOrderDTO.setGoods_transaction_amount(soB2cDetailEntity.getAmount().subtract((totalDiscount.subtract(shareTotalDiscount))));
        }

        if (skuVO.getRetailPrice() != null) {
            shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(skuVO.getRetailPrice());
        } else {
            shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(BigDecimal.ZERO);
        }

        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(soB2cDetailEntity.getCurrency())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(viewDTO)) {
            shudiyunB2cOrderDTO.setTransaction_currency(viewDTO.getName());
            shudiyunB2cOrderDTO.setTransaction_currency_code(viewDTO.getId());
        }

        shudiyunB2cOrderDTO.setPost_amount(soB2cEntity.getShippingFee());
        shudiyunB2cOrderDTO.setMsku_code(soB2cDetailEntity.getPlatformSkuNo());
        String skuName = listingInfoEntities.stream().filter(req -> req.getPlatform().equals(soB2cEntity.getDictPlatform())
                && req.getPlatformSkuNo().equals(soB2cDetailEntity.getPlatformSkuNo()))
                .sorted(Comparator.comparing(ListingInfoEntity::getUpdateTime).reversed()) // 倒序排序
                .map(ListingInfoEntity::getPlatformSkuName).findFirst().orElse("");
        if (CharSequenceUtil.isBlank(skuName)) {
            shudiyunB2cOrderDTO.setMsku_name(skuVO.getSkuName());
        } else {
            shudiyunB2cOrderDTO.setMsku_name(skuName);
        }

        shudiyunB2cOrderDTO.setSku_code(skuVO.getSkuNo());
        shudiyunB2cOrderDTO.setSku_name(skuVO.getSkuName());

        shudiyunB2cOrderDTO.setSource_system("SDC");
        shudiyunB2cOrderDTO.setRoot_node_no_initial(soB2cEntity.getPlatformCode());

        if (SourceTypeEnum.SELF_ADD.getCode().equals(soB2cEntity.getSourceType())) {
            shudiyunB2cOrderDTO.setMsku_code(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setMsku_name(skuVO.getSkuName());
            shudiyunB2cOrderDTO.setRoot_node_no_initial(soB2cEntity.getCode());
            shudiyunB2cOrderDTO.setRoot_node_no(soB2cEntity.getCode());
        }
        return JSONObject.parseObject(JSONObject.toJSONString(shudiyunB2cOrderDTO), Map.class);
    }

    @Override
    public void syncDataToSdy(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soB2cDetailEntityList, String operate) {
        this.syncDataToSdyFieldHandlerBatch(soB2cEntity, soB2cDetailEntityList, operate);

    }

    @Override
    public void syncDataToSdy(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> detailEntityList, String operate, List<SkuVO> skuVOList, List<BomChildrenSkuDTO> bomChildrenSkuDTOS, List<ProductDetailEntity> parentSkuList, List<ListingInfoEntity> listingInfoEntities, List<CurrencyDTO.ViewDTO> currencyList, List<DictCurrencyEntity> dictCurrencyEntities, List<ShopInfoEntity> shopInfoList, List<CustomerInfoEntity> customerInfoList, List<BaseIdDTO.CodeDTO> companyEntities, List<DictBasicEntity> dictBasicEntityList, List<DictBasicEntity> dictList) {
        for (SoB2cDetailEntity soB2cDetailEntity : detailEntityList) {
            //同步配货单
            OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
            omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_DELIVERY_ORDER.getCode());
            omsPushMsgEntity.setSourceId(soB2cDetailEntity.getId());
            omsPushMsgEntity.setSourceCode(soB2cEntity.getCode() + "_" + soB2cDetailEntity.getSkuNo());
            omsPushMsgEntity.setSyncOperate(operate);
            omsPushMsgEntity.setPushData(JSON.toJSONString(this.syncDataToSdyFieldHandler(soB2cEntity, detailEntityList, soB2cDetailEntity, operate, skuVOList, bomChildrenSkuDTOS, parentSkuList, listingInfoEntities, currencyList, dictCurrencyEntities, shopInfoList, customerInfoList, companyEntities, dictBasicEntityList, dictList)));
            omsPushMsgService.save(omsPushMsgEntity);
        }
    }

    private void syncDataToSdyFieldHandlerBatch(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soB2cDetailEntityList, String operate) {
        List<String> skuNos = soB2cDetailEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = soB2cDetailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomBySkuIds(skuIds);

        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(soB2cEntity.getCurrency()));
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }

        ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, soB2cEntity.getShopId());

        //组织信息
        CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
        List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(customerInfo)) {
            companyEntities = sysUserFeign.getAccountingCompanyList(Arrays.asList(customerInfo.getFinancialOrganization(), shopInfo.getSalesOrgId()));
        }
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
            DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            //优惠额
            BigDecimal totalDiscount = BigDecimal.ZERO;
            BigDecimal shareTotalDiscount = BigDecimal.ZERO;
            if (soB2cEntity.getTotalDiscount() != null) {
                totalDiscount = soB2cEntity.getTotalDiscount();
            }

            //总售价
            BigDecimal totalAmount = soB2cEntity.getAmount();

            List<String> skuNoList = soB2cDetailEntityList.stream().map(req -> req.getPlatformSkuNo()).distinct().collect(Collectors.toList());
            //组织信息
            List<ListingInfoEntity> listingInfoEntities = FeignQuery.create(ListingInfoEntity.class).in(ListingInfoEntity::getPlatformSkuNo, skuNoList).list();

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

            shudiyunB2cOrderDTO.setBiz_uni_key(soB2cEntity.getId() + soB2cDetailEntity.getId());
            shudiyunB2cOrderDTO.setBiz_no(soB2cEntity.getCode());
            if (soB2cEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(soB2cEntity.getPayTime()));
            }
            // 平台订单：默认配货单  手工单：默认线下订单
            if (SourceTypeEnum.SELF_ADD.getCode().equals(soB2cEntity.getSourceType())) {
                shudiyunB2cOrderDTO.setTransaction_type("线下订单");

                shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.getName(soB2cEntity.getTransactionSubType()));
            } else {
                shudiyunB2cOrderDTO.setTransaction_type("配货单");
                shudiyunB2cOrderDTO.setTransaction_sub_type("配货单");
            }
            if (CharSequenceUtil.isBlank(soB2cEntity.getBillStatus())) {
                soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            }
            shudiyunB2cOrderDTO.setBiz_status(SoB2cBillStatusEnum.getName(soB2cEntity.getBillStatus()));

            shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operate, soB2cEntity.getVersion(), soB2cDetailEntity.getVersion()));
            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(soB2cEntity.getAmount());
            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(soB2cEntity.getTotalDiscount());

            Integer totalQty = soB2cDetailEntityList.stream().mapToInt(SoB2cDetailEntity::getQty).sum();
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);
            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty);

            //取消金额、数量
            if (soB2cEntity.getDictPlatform().equals(PlatformDictEnum.ALI_EXPRESS.getCode())
                    || soB2cEntity.getDictPlatform().equals(PlatformDictEnum.SHOPEE.getCode())
            ) {
                shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(soB2cEntity.getTotalCancelGoodsAmount());
            } else {
                if (Boolean.TRUE.equals(soB2cEntity.getIsCancel()) && soB2cEntity.getIsCancel() != null) {
                    BigDecimal totalCancelGoodsAmount = soB2cDetailEntityList.stream().map(req -> req.getAmount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(totalCancelGoodsAmount);

                    // 取消商品数量（合计）
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
                }
            }

            shudiyunB2cOrderDTO.setBuyer_actual_payment(soB2cEntity.getPayAmount());
            shudiyunB2cOrderDTO.setTotal_freight(soB2cEntity.getShippingFee());

            if (ObjectUtil.isNotEmpty(shopInfo)) {
                String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(shopInfo.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
                shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);
                shudiyunB2cOrderDTO.setShop_no(shopInfo.getId());
                shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());
                DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                    shudiyunB2cOrderDTO.setTransaction_currency(dictCurrencyEntity.getName());
                }
                shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
                shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());
            }

            if (ObjectUtil.isNotEmpty(customerInfo)) {
                BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                    shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                    shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                    shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                }
                String subPlatformType = customerInfo.getPlatformType();
                if (StringUtils.isNotBlank(subPlatformType)) {
                    List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").eq(DictBasicEntity::getName, subPlatformType).list();
                    if (CollUtil.isNotEmpty(dictList)) {
                        shudiyunB2cOrderDTO.setSubplatform_no(dictList.get(0).getValue());
                        shudiyunB2cOrderDTO.setSubplatform_name(dictList.get(0).getName());
                    }
                }
            }


            shudiyunB2cOrderDTO.setPlatform_id(soB2cEntity.getDictPlatform());

            String platformName = dictBasicEntityList.stream().filter(req -> req.getValue().equals(customerInfo.getPlatformType())).map(DictBasicEntity::getName).findFirst().orElse("");
            shudiyunB2cOrderDTO.setPlatform_name(platformName);

            shudiyunB2cOrderDTO.setRoot_node_no(soB2cEntity.getPlatformCode());
            if (soB2cEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(soB2cEntity.getPayTime()));
            }
            shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(soB2cEntity.getUpdateTime()));
            shudiyunB2cOrderDTO.setGoods_no(soB2cDetailEntity.getSkuNo());
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(soB2cDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            shudiyunB2cOrderDTO.setGoods_name(skuVO.getSkuName());

            if (CharSequenceUtil.isNotBlank(skuVO.getSpuNo())) {
                shudiyunB2cOrderDTO.setSpec_no(skuVO.getSpuNo());
                shudiyunB2cOrderDTO.setSpec_name(skuVO.getSpuName());
            } else {
                shudiyunB2cOrderDTO.setSpec_no(skuVO.getSkuNo());
                shudiyunB2cOrderDTO.setSpec_name(skuVO.getSkuName());
            }

            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(soB2cDetailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
                shudiyunB2cOrderDTO.setIs_comb(1);
                shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getSkuNo());
                shudiyunB2cOrderDTO.setSuite_name(bomChildrenSkuDTO.getSkuName());
            } else {
                bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(soB2cDetailEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO)) {
                    shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getParentSkuNo());
                    BomChildrenSkuDTO finalBomChildrenSkuDTO = bomChildrenSkuDTO;
                    String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
                    shudiyunB2cOrderDTO.setSuite_name(skuName);
                } else {
                    shudiyunB2cOrderDTO.setSuite_no(skuVO.getSkuNo());
                    shudiyunB2cOrderDTO.setSuite_name(skuVO.getSkuName());
                }
            }

            shudiyunB2cOrderDTO.setSuite_no(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setSuite_name(skuVO.getSkuName());
            shudiyunB2cOrderDTO.setRemark(soB2cEntity.getRemark());
            shudiyunB2cOrderDTO.setGoods_status("未发货");
            // 商品状态
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                shudiyunB2cOrderDTO.setGoods_status("已发货");
            }

            if (Boolean.TRUE.equals(soB2cEntity.getIsCancel()) && soB2cEntity.getIsCancel() != null) {
                shudiyunB2cOrderDTO.setGoods_status("已取消");
                shudiyunB2cOrderDTO.setBiz_status("已取消");
            }

            shudiyunB2cOrderDTO.setGoods_transaction_quantity(soB2cDetailEntity.getQty());
            shudiyunB2cOrderDTO.setUnit(skuVO.getUnitName());

            shudiyunB2cOrderDTO.setGoods_transaction_amount(soB2cDetailEntity.getAmount());
            if (soB2cDetailEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal shareDiscount = BigDecimal.ZERO;
                //获得售价占比分摊的商品优惠额
                if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    shareDiscount = soB2cDetailEntity.getAmount().divide(totalAmount, 4, RoundingMode.DOWN).multiply(totalDiscount);
                }
                //计算为真实售价(原始币别)-商品分摊优惠/订单数量
                shudiyunB2cOrderDTO.setPrice(soB2cDetailEntity.getAmount().subtract((totalDiscount.subtract(shareTotalDiscount))).divide(MathUtil.valueOf(soB2cDetailEntity.getQty()), 4, RoundingMode.DOWN));
                shudiyunB2cOrderDTO.setGoods_transaction_amount(soB2cDetailEntity.getAmount().subtract((totalDiscount.subtract(shareTotalDiscount))));
            }

            if (skuVO.getRetailPrice() != null) {
                shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(skuVO.getRetailPrice());
            } else {
                shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(BigDecimal.ZERO);
            }

            CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(soB2cDetailEntity.getCurrency())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(viewDTO)) {
                shudiyunB2cOrderDTO.setTransaction_currency(viewDTO.getName());
                shudiyunB2cOrderDTO.setTransaction_currency_code(viewDTO.getId());
            }

            shudiyunB2cOrderDTO.setPost_amount(soB2cEntity.getShippingFee());
            shudiyunB2cOrderDTO.setMsku_code(soB2cDetailEntity.getPlatformSkuNo());
            String skuName = listingInfoEntities.stream().filter(req -> req.getPlatform().equals(soB2cEntity.getDictPlatform())
                    && req.getPlatformSkuNo().equals(soB2cDetailEntity.getPlatformSkuNo()))
                    .sorted(Comparator.comparing(ListingInfoEntity::getUpdateTime).reversed()) // 倒序排序
                    .map(ListingInfoEntity::getPlatformSkuName).findFirst().orElse("");
            if (CharSequenceUtil.isBlank(skuName)) {
                shudiyunB2cOrderDTO.setMsku_name(skuVO.getSkuName());
            } else {
                shudiyunB2cOrderDTO.setMsku_name(skuName);
            }

            shudiyunB2cOrderDTO.setSku_code(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setSku_name(skuVO.getSkuName());

            shudiyunB2cOrderDTO.setSource_system("SDC");
            shudiyunB2cOrderDTO.setRoot_node_no_initial(soB2cEntity.getPlatformCode());

            if (SourceTypeEnum.SELF_ADD.getCode().equals(soB2cEntity.getSourceType())) {
                shudiyunB2cOrderDTO.setMsku_code(skuVO.getSkuNo());
                shudiyunB2cOrderDTO.setMsku_name(skuVO.getSkuName());
                shudiyunB2cOrderDTO.setRoot_node_no_initial(soB2cEntity.getCode());
                shudiyunB2cOrderDTO.setRoot_node_no(soB2cEntity.getCode());
            }

            //同步配货单
            OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
            omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_DELIVERY_ORDER.getCode());
            omsPushMsgEntity.setSourceId(soB2cDetailEntity.getId());
            omsPushMsgEntity.setSourceCode(soB2cEntity.getCode() + "_" + soB2cDetailEntity.getSkuNo());
            omsPushMsgEntity.setSyncOperate(operate);
            omsPushMsgEntity.setPushData(JSON.toJSONString(shudiyunB2cOrderDTO));
            omsPushMsgService.save(omsPushMsgEntity);
        }

    }
}
