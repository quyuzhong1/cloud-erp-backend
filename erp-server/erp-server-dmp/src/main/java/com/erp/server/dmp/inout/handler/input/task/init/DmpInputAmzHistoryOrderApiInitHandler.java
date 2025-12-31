package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.DmpSoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzHistoryOrderApiInitHandler extends DmpInputAmzHistoryOrderAbstractApiInitHandler {

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private DmpSoInfoService dmpSoInfoService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取店铺信息
        String shopId = nextLevelId;
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(shopId);
        List<ShopInfoEntity> relatedshopInfoList = shopInfoFeign.getRelatedShopById(shopInfo);
        AmazonShopInfoDTO shopInfoDTO = initShopInfoDTO(shopInfo,  relatedshopInfoList);
        return getDmpInputTaskInitDTOS(shopInfoDTO);
    }



    /**
     * 查询订单
     */
    private List<DmpInputTaskInitDTO> getDmpInputTaskInitDTOS(AmazonShopInfoDTO shopInfoDTO) {
        // 开始时间
        LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
        // 结束时间
        LocalDateTime endTime = dmpInputTaskEntity.getEndTime();
        List<PlatformAmazonOrderDTO> mongoOrderList = findOrdersByCriteria(startTime.toString(), endTime.toString(), shopInfoDTO.getPlatformShopCode(), Order.OrderStatusEnum.SHIPPED.getValue());
        if (CollectionUtils.isEmpty(mongoOrderList)){
            return Collections.emptyList();
        }
        List<String> orderIdList = mongoOrderList.stream().map(e -> e.getOrder().getAmazonOrderId()).distinct().collect(Collectors.toList());

        List<DmpSoInfoEntity> dmpList = dmpSoInfoService.lambdaQuery()
                .eq(DmpSoInfoEntity::getSourceSystem, PlatformDictEnum.AMAZON.getCode())
                .eq(DmpSoInfoEntity::getSourcePlatform, PlatformDictEnum.AMAZON.getCode())
                .in(DmpSoInfoEntity::getThirdCode, orderIdList)
                .list();
        List<String> existOrderId = dmpList.stream().map(DmpSoInfoEntity::getThirdCode).collect(Collectors.toList());


        List<JSONObject> curJsonList = mongoOrderList.stream()
                .filter(e-> !existOrderId.contains(e.getOrder().getAmazonOrderId()))
                .map(e -> fillDataAndToJsonObject(e.getOrder(), shopInfoDTO, e.getUniqueId())).collect(Collectors.toList());

        // 返回下载源数据
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSONArray.toJSONString(curJsonList)));
    }

    /**
     * 设置亚马逊账号和转换JSON
     */
    private JSONObject fillDataAndToJsonObject(Order entity, AmazonShopInfoDTO shopInfoDTO, String uniqueId) {
        JSONObject json = (JSONObject) JSON.toJSON(entity);
        json.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        // 根据站点判断店铺ID
        AmazonShopInfoDTO.ShopNameDTO shopNameDTO = shopInfoDTO.getMarketplaceShopIdMap().get(entity.getMarketplaceId());
        json.put("shopId", shopNameDTO.getShopId());
        json.put("shopName", shopNameDTO.getShopName());
        json.put("uniqueId", uniqueId);
        return json;
    }
}
