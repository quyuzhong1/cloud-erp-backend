package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.ThirdMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyWdtOriginalOrderHandler extends DmpOutputTaskHandler {
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;

    @Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> DmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> DmpSoDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
                        DmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity DmpSoDetailEntity = (DmpSoDetailEntity) v;
                        String mainId = DmpSoDetailEntity.getMainId();
                        List<DmpSoDetailEntity> list = DmpSoDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(DmpSoDetailEntity);
                        DmpSoDetailEntityMap.put(mainId, list);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity DmpSoDetailEntity = (DmpSoDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        for(String changId : changeIds) {
            List<ShudiyunB2cOrderDTO> sdyDtoList = this.convert(DmpSoInfoEntityMap.get(changId), DmpSoDetailEntityMap.get(changId));
            if(CollUtil.isEmpty(sdyDtoList)) {
                map.put(changId, JSON.toJSONString(sdyDtoList));
            }
        }
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
        if(!map.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String dataId = entry.getKey();
                List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = JSON.parseArray(entry.getValue(), ShudiyunB2cOrderDTO.class);
                for (ShudiyunB2cOrderDTO shudiyunB2cOrderDTO : shudiyunB2cOrderDTOList) {
                    DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
                    String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
                    dmpOutputTaskRecordEntity.setId(id);
                    dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
                    dmpOutputTaskRecordEntity.setDataId(shudiyunB2cOrderDTO.getTransaction_unique_key().substring(dataId.length()));
                    dmpOutputTaskRecordEntity.setSourceCode(shudiyunB2cOrderDTOList.get(0).getBiz_no());
                    dmpOutputTaskRecordEntity.setRequestData(JSON.toJSONString(shudiyunB2cOrderDTO));
                    dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
                    LocalDateTime insertTime = now.plus(i, ChronoUnit.MILLIS);
                    dmpOutputTaskRecordEntity.setCreateTime(insertTime);
                    dmpOutputTaskRecordEntity.setUpdateTime(insertTime);
                    dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
                    i = i + 1;
                }
            }
        }

        return dmpOutputTaskRecordEntityList;
    }

    @Override
    protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity, DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
        String requestData = dmpOutputTaskRecordEntity.getRequestData();
        sdyDeliveryOrderConsumer.handle(requestData);
    }

    /**
     * 解析订单数据
     **/
    public List<ShudiyunB2cOrderDTO> convert(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntityList) {
        //优惠额
        BigDecimal totalDiscount = dmpSoInfoEntity.getTotalDiscount();

        //如果是亚马逊的优惠额在明细里
        if (PlatformDictEnum.AMAZON.getCode().equals(dmpSoInfoEntity.getSourcePlatform())) {
            totalDiscount = dmpSoDetailEntityList.stream().filter(req -> req.getDiscount() != null).map(req -> req.getDiscount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }

        //数帝云数据结构
        List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = new ArrayList<>();

        //订单详情
        for (int i = 0; i < dmpSoDetailEntityList.size(); i++) {
            DmpSoDetailEntity dmpSoDetailEntity = dmpSoDetailEntityList.get(i);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
            shudiyunB2cOrderDTO.setTransaction_unique_key(dmpSoInfoEntity.getId() + dmpSoDetailEntity.getId());

            shudiyunB2cOrderDTO.setBiz_no(dmpSoInfoEntity.getThirdCode());
            shudiyunB2cOrderDTO.setBiz_time(dmpSoInfoEntity.getPayTime());

            //如果是旺店通中台表的订单属于配货单，其他的都是线上原始订单
            if (PlatformDictEnum.WDT.getCode().equals(dmpSoInfoEntity.getSourcePlatform())) {
                //配货单
                shudiyunB2cOrderDTO.setTransaction_type("100.20");
            } else {
                //线上订单
                shudiyunB2cOrderDTO.setTransaction_type("100.10");
            }

            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.ONLINE_ORDER.getCode());
            shudiyunB2cOrderDTO.setBiz_status(dmpSoInfoEntity.getDeliveryStatus());

            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(dmpSoInfoEntity.getAllAmount());
            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(dmpSoInfoEntity.getTotalDiscount());

            Integer totalQty = dmpSoDetailEntityList.stream().mapToInt(DmpSoDetailEntity::getQty).sum();
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);
            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty);

            //取消金额、数量
            if (dmpSoInfoEntity.getSourceSystem().equals(PlatformDictEnum.ALI_EXPRESS.getCode())
                    || dmpSoInfoEntity.getSourceSystem().equals(PlatformDictEnum.SHOPEE.getCode())
            ) {
                shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getTotalCancelGoodsAmount());
            } else {
                if (dmpSoInfoEntity.getIsCancel()) {
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getAllAmount());

                    // 取消商品数量（合计）
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
                }
            }

            shudiyunB2cOrderDTO.setBuyer_actual_payment(dmpSoInfoEntity.getPayAmount());
            shudiyunB2cOrderDTO.setTotal_freight(dmpSoInfoEntity.getShippingAmount());
            if (PlatformDictEnum.WDT.getCode().equals(dmpSoInfoEntity.getSourceSystem())) {
                //查询旺店通对应系统店铺
                List<ThirdMappingEntity> shop = thirdMappingService.lambdaQuery()
                        .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                        .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                        .eq(ThirdMappingEntity::getThirdId, dmpSoInfoEntity.getShopId())
                        .list();
                if (CollectionUtils.isEmpty(shop)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_SHOP_MAPPING, dmpSoInfoEntity.getShopId());
                }
                ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shop.get(0).getSysId());
                CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
                if (ObjectUtil.isEmpty(customerInfo)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_CUSTOMER, shopInfo.getCustomerId());
                }
                shudiyunB2cOrderDTO.setSales_company_code(shopInfo.getSalesOrgId());
                shudiyunB2cOrderDTO.setSales_company_code(customerInfo.getFinancialOrganization());
                shudiyunB2cOrderDTO.setReceiving_company_code(customerInfo.getFinancialOrganization());
                shudiyunB2cOrderDTO.setShop_no(shopInfo.getId());
                shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());
                DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                    shudiyunB2cOrderDTO.setTransaction_currency(dictCurrencyEntity.getName());
                }
                shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());

            } else {
                String shopId = "";
                if (CharSequenceUtil.isNotBlank(dmpSoInfoEntity.getNextLevelId())) {
                    shopId = dmpSoInfoEntity.getNextLevelId();
                } else {
                    shopId = dmpSoInfoEntity.getShopId();
                }
                ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shopId);
                if (ObjectUtil.isEmpty(shopInfo)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_SHOP, shopId);
                }
                CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
                if (ObjectUtil.isEmpty(customerInfo)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_CUSTOMER, shopInfo.getCustomerId());
                }
                shudiyunB2cOrderDTO.setSales_company_code(shopInfo.getSalesOrgId());
                shudiyunB2cOrderDTO.setSales_company_code(customerInfo.getFinancialOrganization());
                shudiyunB2cOrderDTO.setReceiving_company_code(customerInfo.getFinancialOrganization());
                shudiyunB2cOrderDTO.setShop_no(shopInfo.getId());
                shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());
                DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                    shudiyunB2cOrderDTO.setTransaction_currency(dictCurrencyEntity.getName());
                }
                shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
            }

            shudiyunB2cOrderDTO.setPlatform_id(dmpSoInfoEntity.getSourceSystem());
            shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.getNameByCode(dmpSoInfoEntity.getSourcePlatform()));
            shudiyunB2cOrderDTO.setShop_no(dmpSoInfoEntity.getShopId());
            shudiyunB2cOrderDTO.setShop_name(dmpSoInfoEntity.getShopName());
            shudiyunB2cOrderDTO.setRoot_node_no(dmpSoInfoEntity.getThirdCode());
            shudiyunB2cOrderDTO.setRoot_node_create_time(dmpSoInfoEntity.getPayTime());
            shudiyunB2cOrderDTO.setRoot_node_modify_time(dmpSoInfoEntity.getPlatformUpdateTime());
            shudiyunB2cOrderDTO.setGoods_no(dmpSoDetailEntity.getPlatformSku());
            shudiyunB2cOrderDTO.setGoods_name(dmpSoDetailEntity.getSkuName());
            shudiyunB2cOrderDTO.setSpec_no(dmpSoDetailEntity.getPlatformSpuNo());
            shudiyunB2cOrderDTO.setSpec_name("");
            if (dmpSoDetailEntity.getIsGift()) {
                shudiyunB2cOrderDTO.setIs_gift(1);
            } else {
                shudiyunB2cOrderDTO.setIs_gift(0);
            }

            shudiyunB2cOrderDTO.setRemark(dmpSoDetailEntity.getItemRemark());

            // 商品状态
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getDeliveryStatus())) {
                shudiyunB2cOrderDTO.setGoods_status("10.10");
            }

            if (dmpSoInfoEntity.getIsCancel()) {
                shudiyunB2cOrderDTO.setGoods_status("10.20");
            }

            shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoDetailEntity.getQty());
            shudiyunB2cOrderDTO.setUnit(dmpSoDetailEntity.getProductUnit());

            shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount());
            if (dmpSoDetailEntity.getAfterAmount().compareTo(BigDecimal.ZERO) > 0) {
                //获得分摊的商品优惠额
                BigDecimal shareDiscount = dmpSoDetailEntity.getAfterAmount().divide(dmpSoInfoEntity.getAllAmount(), 4, RoundingMode.HALF_UP).multiply(totalDiscount);
                //计算为真实售价(原始币别)-商品分摊优惠/订单数量
                if (dmpSoDetailEntityList.size() == i-1) {
                    shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getAfterAmount().subtract(dmpSoInfoEntity.getAllAmount()).divide(MathUtil.valueOf(dmpSoDetailEntity.getQty()), 4, RoundingMode.HALF_UP));
                    shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount().subtract(dmpSoInfoEntity.getAllAmount()));
                } else {
                    shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getAfterAmount().subtract(shareDiscount).divide(MathUtil.valueOf(dmpSoDetailEntity.getQty()), 4, RoundingMode.HALF_UP));
                    shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount().subtract(shareDiscount));
                }
                totalDiscount = totalDiscount.subtract(shareDiscount);
            }

            shudiyunB2cOrderDTO.setTransaction_currency_code(dmpSoDetailEntity.getCurrencyCode());

            shudiyunB2cOrderDTO.setPost_amount(dmpSoInfoEntity.getShippingAmount());
            shudiyunB2cOrderDTO.setMsku_code(dmpSoDetailEntity.getPlatformSku());
            shudiyunB2cOrderDTO.setMsku_name(dmpSoDetailEntity.getSkuName());
            shudiyunB2cOrderDTO.setSku_code("");
            shudiyunB2cOrderDTO.setSku_name("");

            shudiyunB2cOrderDTO.setSource_system("SDC");
            shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoInfoEntity.getPlatformCode());

            shudiyunB2cOrderDTOList.add(shudiyunB2cOrderDTO);

        }
        return shudiyunB2cOrderDTOList;

    }
}
