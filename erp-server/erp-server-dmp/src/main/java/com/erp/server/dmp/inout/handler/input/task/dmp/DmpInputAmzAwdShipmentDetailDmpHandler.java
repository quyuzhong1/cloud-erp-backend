package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.sdk.oms.amz.spapi.model.awd.*;
import com.erp.sdk.oms.amz.spapi.model.finances.ChargeComponent;
import com.erp.sdk.oms.amz.spapi.model.finances.ChargeComponentList;
import com.erp.sdk.oms.amz.spapi.model.finances.ShipmentItem;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Address;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.LabelPrepType;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.Decimal128;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * dmp处理明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzAwdShipmentDetailDmpHandler extends DmpInputDoChildDmpHandler {

    @Override
    protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
        List<ParamData> paramDataList = new ArrayList<>();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
        List<Map<String, Object>> mongoData = mongoService.findMongoData(paramDataList, childMongoStorageName);
        //转换成明细列表
        List<Map<String, Object>> detailMongoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(mongoData)) {
            for (Map<String, Object> mongoMap : mongoData) {
                List<Map<String, Object>> shipmentContainerQuantities = (List<Map<String, Object>>) mongoMap.get("shipmentContainerQuantities");
                String shipmentId = (String) mongoMap.get("shipmentId");
                String mongoId = (String) mongoMap.get("_id");
                if (CollUtil.isNotEmpty(shipmentContainerQuantities)) {
                    for (Map<String, Object> shipmentMap : shipmentContainerQuantities) {
                        Integer count = (Integer) shipmentMap.getOrDefault("count", 0);
                        Map<String, Object> distributionPackageMap = (Map<String, Object>)shipmentMap.getOrDefault("distributionPackage", null);
                        Map<String, Object> measurementsMap = (Map<String, Object>)distributionPackageMap.getOrDefault("measurements", null);
                        Map<String, Object> dimensionMap = new HashMap<>();
                        Map<String, Object> weightMap = new HashMap<>();
                        if (null != measurementsMap){
                            dimensionMap = (Map<String, Object>)measurementsMap.getOrDefault("dimensions", null);
                            weightMap = (Map<String, Object>)measurementsMap.getOrDefault("weight", null);
                        }
                        if (null != distributionPackageMap){
                            Map<String, Object> contentsMap = (Map<String, Object>)distributionPackageMap.getOrDefault("contents", null);
                            if (null != contentsMap){
                                List<Map<String, Object>> productsMapList = (List<Map<String, Object>>)contentsMap.getOrDefault("products", null);
                                if (CollUtil.isNotEmpty(productsMapList)){
                                    for (Map<String, Object> productMap : productsMapList){
                                        Map<String, Object> detailMap = new LinkedHashMap<>();
                                        Integer qty = (Integer)productMap.getOrDefault("quantity", 0);
                                        detailMap.put("_id", mongoId);
                                        detailMap.put("fbaShipmentId", shipmentId);
                                        detailMap.put("perBoxQty",qty );
                                        detailMap.put("boxQty", count);
                                        detailMap.put("msku", productMap.getOrDefault("sku", ""));
                                        detailMap.put("declareQty", qty * count);
                                        //箱子规格
                                        detailMap.put("packageHeight", Objects.nonNull(dimensionMap) ? ((Decimal128)dimensionMap.getOrDefault("height", BigDecimal.ZERO)).bigDecimalValue() : BigDecimal.ZERO);
                                        detailMap.put("packageWidth", Objects.nonNull(dimensionMap) ? ((Decimal128)dimensionMap.getOrDefault("width", BigDecimal.ZERO)).bigDecimalValue() : BigDecimal.ZERO);
                                        detailMap.put("packageLength", Objects.nonNull(dimensionMap) ? ((Decimal128)dimensionMap.getOrDefault("length", BigDecimal.ZERO)).bigDecimalValue() : BigDecimal.ZERO);
                                        detailMap.put("packageUnit", Objects.nonNull(dimensionMap) ? (String)dimensionMap.getOrDefault("unitOfMeasurement", "") : "");
                                        //箱重
                                        detailMap.put("packageWeight", Objects.nonNull(weightMap) ? ((Decimal128)weightMap.getOrDefault("weight", BigDecimal.ZERO)).bigDecimalValue() : BigDecimal.ZERO);
                                        detailMap.put("packageWeightUnit", Objects.nonNull(weightMap) ? (String)weightMap.getOrDefault("unitOfMeasurement", "") : "");
                                        detailMongoList.add(detailMap);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return detailMongoList;
    }

    @Override
    protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
        DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
        String parentStorageName = mainConvertId.getStorageName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
        Map<String, String> billNoIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                billNoIdMap.put(listMap.get("fba_shipment_id").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
            String billNo = dmpInputMongoChildEntity.get("fbaShipmentId").toString();
            String dmpId = billNoIdMap.get(billNo);
            dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
        }
    }
//
//
//    @Override
//    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
//        super.afterConvertData(dmpInputDataDmpRelationMaps);
//
//        // 打平原始数据
//        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
//            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
//            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
//            Map<String, Object> mongoData = mongoDataMaps.get(0);
//            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
//                Object shipmentContainerQuantities = mongoData.get("shipmentContainerQuantities");
//                if (null != shipmentContainerQuantities){
//                    List<DistributionPackageQuantity> shipmentList = JSONArray.parseArray(JSONObject.toJSONString(shipmentContainerQuantities),DistributionPackageQuantity.class);
//
//                }

//                Object createdAt = mongoData.get("createdAt");
//                dmpDataMap.put("platform_create_time",createdAt);
//                Object destinationAddress = mongoData.get("destinationAddress");
//                if (null != destinationAddress){
//
//                }
//                Object originAddress = mongoData.get("originAddress");
//                Object receivedQuantity = mongoData.get("receivedQuantity");
//                Object carrierCode = mongoData.get("carrierCode");
//                Object carrierCode = mongoData.get("carrierCode");
//                if (null != addressObj) {
//                    Address shipFromAddress = JSON.parseObject(JSONObject.toJSONString(addressObj), Address.class);
//                    dmpDataMap.put("countryId", shipFromAddress.getCountryCode());
//
//                    String fullAddress = String.join(" ",
//                            shipFromAddress.getPostalCode(),
//                            shipFromAddress.getCountryCode(),
//                            shipFromAddress.getStateOrProvinceCode(),
//                            shipFromAddress.getCity(),
//                            shipFromAddress.getAddressLine1(),
//                            shipFromAddress.getName());
//                    dmpDataMap.put("deliveryFromAddress", fullAddress);
//                } else {
//                    dmpDataMap.put("countryId", "");
//                    dmpDataMap.put("deliveryFromAddress", "");
//                }
//                Object areCasesRequired = mongoData.get("areCasesRequired");
//                if (areCasesRequired instanceof Boolean) {
//                    dmpDataMap.put("packType", (Boolean) areCasesRequired ? "原厂包装" : "混装");
//                } else {
//                    dmpDataMap.put("packType", "");
//                }
//                String labelType = "";
//                Object labelPrepTypeObj = mongoData.get("labelPrepType");
//                if (null != labelPrepTypeObj) {
//                    LabelPrepType labelPrepType = LabelPrepType.valueOf(labelPrepTypeObj.toString());
//                    labelType =  labelPrepType.getDesc();
//                }
//                dmpDataMap.put("labelType", labelType);
//            }
//        }
//    }
}
