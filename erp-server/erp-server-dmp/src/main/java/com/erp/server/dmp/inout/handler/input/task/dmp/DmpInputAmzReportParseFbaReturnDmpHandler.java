package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportParseFbaReturnDmpHandler extends DmpInputAmzReportReturnCommonDmpHandler {


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzReportParseFbaReturnDmpHandler afterConvertData 处理");
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

                // 单据状态：1待处理 2已退款 3已重发 4已完成 5已作废
                dmpDataMap.put(STATUS, 4);
            }
        }
    }

}
