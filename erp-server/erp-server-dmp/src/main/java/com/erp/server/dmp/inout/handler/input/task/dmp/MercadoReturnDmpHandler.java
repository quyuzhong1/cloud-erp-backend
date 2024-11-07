package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.utils.CollectionUtils;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.entity.DmpLogisticInfoEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoReturnDetailService;
import com.erp.server.dmp.service.DmpSoReturnInfoService;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.mercado.order.OrderDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrdersBean;
import com.sdk.oms.mercado.dto.mercado.returnOrder.ReturnDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MercadoReturnDmpHandler extends DmpInputDbConvertDmpHandler {
    @Resource
    private DmpSoReturnInfoService dmpSoReturnInfoService;
    @Resource
    private DmpSoReturnDetailService dmpSoReturnDetailService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        //使用 DateTimeFormatter 解析字符串日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        super.afterConvertData(dmpInputDataDmpRelationMaps);
        List<Long> orderIdList = new ArrayList<>();
        Set<List<Map<String, Object>>> keySet = dmpInputDataDmpRelationMaps.keySet();
        if (CollUtil.isNotEmpty(keySet)) {
            List<String> returnIdList = new ArrayList<>();
            for (List<Map<String, Object>> key : keySet) {
                returnIdList.addAll(key.stream().map(f -> f.get("fid").toString()).collect(Collectors.toList()));

                for (Map<String, Object> map : key) {
                    if ("order".equals(map.get("resource"))) {
                        orderIdList.add(Long.valueOf(map.get("resourceId").toString()));
                    }
                }
            }

            List<DmpSoReturnInfoEntity> list = dmpSoReturnInfoService.lambdaQuery()
                    .in(DmpSoReturnInfoEntity::getThirdCode, returnIdList)
                    .in(DmpSoReturnInfoEntity::getSourceSystem, Arrays.asList(DmpBasicSystemCodeEnum.KINGDEE.getCode(), DmpBasicSystemCodeEnum.MABANG.getCode()))
                    .select(DmpSoReturnInfoEntity::getId)
                    .list();
            if (CollUtil.isNotEmpty(list)) {
                List<String> ids = list.stream().map(DmpSoReturnInfoEntity::getId).collect(Collectors.toList());
                dmpSoReturnInfoService.removeByIds(ids);
                dmpSoReturnDetailService.lambdaUpdate()
                        .in(DmpSoReturnDetailEntity::getMainId, ids)
                        .remove();
            }
        }

        if (CollectionUtil.isEmpty(orderIdList)) {
            return;
        }
        List<ParamData> orderParamList = new ArrayList<>();
        orderParamList.add(new ParamData(MercadoConstant.MONGO_BASE_FID, MercadoConstant.MONGO_BASE_FID, PannoEnum.IN, orderIdList));
        List<Map<String, Object>> orderDetailMongoList = mongoService.findMongoData(orderParamList, "mercadolibre_orderDetail_data");



        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("nextLevelId", dmpDataMap.get("nextLevelId"));
                dmpDataMap.put("shopId", dmpDataMap.get("nextLevelId"));

                Object statusObj = dmpDataMap.get("platformStatus");
                if (statusObj != null) {
                    String status = String.valueOf(statusObj);
                    if ("opened".equalsIgnoreCase(status)) {
                        dmpDataMap.put("status", "1");

                    } else if ("closed".equalsIgnoreCase(status)) {
                        dmpDataMap.put("status", "4");
                    }
                }

                //创建时间
                Object createTimeObj = dmpDataMap.get("dateCreated");
                if (createTimeObj != null) {
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(String.valueOf(createTimeObj), formatter);
                    // 转换为 LocalDateTime
                    dmpDataMap.put("platformCreateTime", offsetDateTime.toLocalDateTime());
                    dmpDataMap.put("billDate", offsetDateTime.toLocalDateTime());
                    dmpDataMap.put("returnTime", offsetDateTime.toLocalDateTime());
                }

                //修改时间
                Object updateTimeObj = dmpDataMap.get("lastUpdated");
                if (updateTimeObj != null) {
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(String.valueOf(updateTimeObj), formatter);
                    // 转换为 LocalDateTime
                    dmpDataMap.put("platformUpdateTime", offsetDateTime.toLocalDateTime());
                }


                Map<String, Object> map = orderDetailMongoList.stream().filter(req -> req.get("fid").equals(dmpDataMap.get("resourceId"))).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(map)) {
                    OrderViewDTO orderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(JSONUtil.toJsonStr(map)), OrderViewDTO.class);
                    dmpDataMap.put("allAmount", orderDTO.getPaidAmount());
                    dmpDataMap.put("currencyCode", orderDTO.getCurrencyId());
                    dmpDataMap.put("buyerName", orderDTO.getBuyer().getFirstName()+" "+orderDTO.getBuyer().getLastName());
                    dmpDataMap.put("exchangeRate", orderDTO.getOrderItems().get(0).getBaseExchangeRate());
                }
            }
        }
    }
}
