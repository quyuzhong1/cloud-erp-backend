package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportParseFbmReturnDmpHandler extends DmpInputAmzReportReturnCommonDmpHandler {

    public static final String DATE_TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzReportParseFbmReturnDmpHandler afterConvertData 处理");
        // 根据账号和订单ID查询源订单信息
        List<SoB2cEntity> orderList = queryOrderList(dmpInputDataDmpRelationMaps);
        // 查询授权店铺信息
        List<ShopInfoEntity> shopList = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null));


        for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            // 解析对应店铺(默认-请求的店铺)
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                String shopId = dmpDataMap.getOrDefault(NEXT_LEVEL_ID, "").toString();
                String shopName = "";
                ShopInfoEntity shopInfo = checkShopInfo(dmpDataMap, shopId, orderList, shopList);
                dmpDataMap.put(SHOP_ID, shopId);
                if (null != shopInfo){
                    shopName = shopInfo.getName();
                    dmpDataMap.put(COUNTRY, shopInfo.getDictCountryCode());
                }
                dmpDataMap.put(SHOP_NAME, shopName);

                String returnDateStr = dmpDataMap.getOrDefault(RETURN_REQUEST_DATE, "").toString();
                if (StringUtils.isNotBlank(returnDateStr)){
                    LocalDateTime returnLocalDateTime = parseToTime(returnDateStr);
                    // 平台退货时间
                    dmpDataMap.put(RETURN_TIME, returnLocalDateTime);
                    // 平台创建时间
                    dmpDataMap.put(PLATFORM_CREATE_TIME, returnLocalDateTime);
                }

                String returnDeliveryDateStr = dmpDataMap.getOrDefault(RETURN_DELIVERY_DATE, "").toString();
                if (StringUtils.isNotBlank(returnDeliveryDateStr)){
                    LocalDateTime returnDeliveryTime = parseToTime(returnDeliveryDateStr);
                    // 平台更新时间
                    dmpDataMap.put(PLATFORM_UPDATE_TIME, returnDeliveryTime);
                }


                // 单据状态：1待处理 2已退款 3已重发 4已完成 5已作废
                dmpDataMap.put(STATUS, 4);
            }
        }
    }


    public LocalDateTime parseToTime(String dateStr){
        if (StringUtils.isBlank(dateStr)){
            return null;
        }
        // 18-Nov-2024
        return LocalDateTime.parse(dateStr + " 00:00:00", DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.ENGLISH));
    }
}
