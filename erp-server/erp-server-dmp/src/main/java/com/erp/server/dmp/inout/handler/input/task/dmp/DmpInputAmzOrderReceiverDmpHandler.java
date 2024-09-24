package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Address;
import com.erp.sdk.oms.amz.spapi.model.orders.BuyerInfo;
import com.erp.sdk.oms.amz.spapi.model.orders.BuyerTaxInfo;
import com.erp.sdk.oms.amz.spapi.model.orders.TaxClassification;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzOrderReceiverDmpHandler extends DmpInputAmzOrderDoChildDmpHandler {

    @Resource
    private RedisUtil redisUtil;

    @Override
    protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList) {
        if (CollUtil.isEmpty(dmpInputMongoChildList)) {
            return Collections.emptyList();
        }

        // 缓存结果key
        List<String> delKeys = new LinkedList<>();

        for (Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
            Object buyerInfoObj = dmpInputMongoChild.get("buyerInfo");
            // 订单买家信息
            BuyerInfo buyerInfo = JSON.parseObject(buyerInfoObj.toString(), BuyerInfo.class);
            if (null != buyerInfo) {
                // 税号
                String taxNo = "";
                BuyerTaxInfo buyerTaxInfo = buyerInfo.getBuyerTaxInfo();
                if (null != buyerTaxInfo) {
                    List<TaxClassification> taxClassifications = buyerInfo.getBuyerTaxInfo().getTaxClassifications();
                    if (!CollectionUtils.isEmpty(taxClassifications)) {
                        TaxClassification taxClassification = taxClassifications.stream().filter(e -> "CPF".equalsIgnoreCase(e.getName())).findFirst().orElse(null);
                        if (null != taxClassification) {
                            // 巴西税号
                            taxNo = taxClassification.getValue();
                        } else {
                            // 税号
                            taxNo = JSONUtil.toJsonStr(taxClassifications);
                        }
                    }
                }
                dmpInputMongoChild.put("receiverTaxNo", taxNo);
                // 买家名称
                String buyerName = StringUtils.isBlank(buyerInfo.getBuyerName()) ? "" : buyerInfo.getBuyerName();
                dmpInputMongoChild.put("buyerName", buyerName);
                // 买家邮箱
                String email = StringUtils.isBlank(buyerInfo.getBuyerEmail()) ? "" : buyerInfo.getBuyerEmail();
                dmpInputMongoChild.put("email", email);
            }

            // 订单地址信息
            Address shippingAddress = JSON.parseObject(JSON.toJSONString(dmpInputMongoChild), Address.class);
            if (null == shippingAddress) {
                continue;
            }
            // 当前订单
            String amazonOrderId = checkAndGetMongoValue(dmpInputMongoChild, "amazonOrderId");
            // 当前账号
            String platformShopCode = checkAndGetMongoValue(dmpInputMongoChild, "platformShopCode");

            String receiverName = "";
            // 买家名称为空使用发货单名称覆盖
            if (null == buyerInfo) {
                receiverName = shippingAddress.getName();
            }
            if (null != buyerInfo && StringUtils.isBlank(buyerInfo.getBuyerName())
            ) {
                receiverName = shippingAddress.getName();
            }
            dmpInputMongoChild.put("buyerName", receiverName);

            String secondAddress = StrUtil.concat(true, shippingAddress.getAddressLine2(), shippingAddress.getAddressLine3());
            dmpInputMongoChild.put("secondStreet", secondAddress);

            String fullAddress = StrUtil.concat(true, shippingAddress.getMunicipality());
            dmpInputMongoChild.put("fullAddress", fullAddress);

            String uniqueId = StrUtil.format("{}_{}", platformShopCode, amazonOrderId);
            // 缓存移除结果
            String addressKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getBusinessTypeName(), uniqueId);
            String buyerKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.BUYER_INFO.getBusinessTypeName(), uniqueId);
            if (redisUtil.hasKey(addressKey)) {
                delKeys.add(addressKey);
            }
            if (redisUtil.hasKey(buyerKey)) {
                delKeys.add(buyerKey);
            }
        }

        if (!delKeys.isEmpty()) {
            redisUtil.del(delKeys.toArray(new String[0]));
        }

        return dmpInputMongoChildList;
    }


}
