package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.mercadolocal.dto.mercadolocal.shipment.BillViewDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class MercadoLocalReceiverDmpHandler extends DmpInputDoNextDmpHandler {
    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        String id = dmpInputTaskEntity.getId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, id).list();
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        DmpInputTaskEntity dmpInputTaskEntity = list.stream().filter(req -> "1899659842348408323".equals(req.getCfgInputId())).findFirst().orElse(null);

        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getId()));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "mercadolibre_local_shipment_data");

        DmpInputTaskEntity dmpInputTaskEntity1 = list.stream().filter(req -> "1899659842348408324".equals(req.getCfgInputId())).findFirst().orElse(null);

        List<ParamData> paramDataList1 = new ArrayList<>();
        paramDataList1.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity1.getId()));
        List<Map<String, Object>> dmpInputMongoChildList1 = mongoService.findMongoData(paramDataList1, "mercadolibre_local_bill_data");

        List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
        if (CollUtil.isNotEmpty(detailList)) {
            for (Map<String, Object> detail : detailList) {
                //买家
                Object buyerObj = detail.get("buyer");
                if (ObjectUtil.isNotEmpty(buyerObj)) {
                    Map<String, Object> buyerMap = (Map<String, Object>) buyerObj;
                    detail.put("buyerId", buyerMap.get("id"));
                    detail.put("buyerName", buyerMap.get("nickname"));
                }
                //收件人税号
                Object fid = detail.get("fid");
                if (fid!= null) {
                    Map<String, Object> billMap = dmpInputMongoChildList1.stream().filter(req -> req.get("fid").toString().equals(fid.toString())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(billMap)) {
                        BillViewDTO billViewDTO = JSON.parseObject(JSON.toJSONString(billMap), BillViewDTO.class);
                        BillViewDTO.BuyerDTO buyerDTO = Objects.nonNull(billViewDTO) ? billViewDTO.getBuyer() : null;
                        BillViewDTO.BuyerDTO.BillingInfoDTO billingInfoDTO = Objects.nonNull(buyerDTO) ? buyerDTO.getBillingInfo() : null;
                        detail.put("receiverTaxNo", Objects.nonNull(billingInfoDTO) ? billingInfoDTO.getIdentification().getNumber() : "");
                        detail.put("ieNo", Objects.nonNull(billingInfoDTO) ? billingInfoDTO.getTaxes().getInscriptions().getStateRegistration() : "");
                    }
                }

                Map<String, Object> shipmentIdMap = (Map<String, Object>) detail.get("shipping");
                Object shipmentId = shipmentIdMap.get("id");
                if (shipmentId != null) {
                    Map<String, Object> shipmentMap = dmpInputMongoChildList.stream().filter(req -> req.get("fid").equals(shipmentId)).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(shipmentMap)) {
                        //地址
                        Map<String, Object> destinationMap = (Map<String, Object>) shipmentMap.get("destination");

                        detail.put("receiverName", destinationMap.get("receiverName"));
                        detail.put("receiverTelNumber", destinationMap.get("receiverPhone"));

                        Object shippingAddressObj = destinationMap.get("shippingAddress");
                        if (ObjectUtil.isNotEmpty(shippingAddressObj)) {
                            Map<String, Object> shippingAddressMap = (Map<String, Object>) shippingAddressObj;
                            Map<String, Object> countryMap = (Map<String, Object>) shippingAddressMap.get("country");
                            Map<String, Object> stateMap = (Map<String, Object>) shippingAddressMap.get("state");
                            Map<String, Object> cityMap = (Map<String, Object>) shippingAddressMap.get("city");
                            detail.put("country", countryMap.get("fid"));

                            detail.put("postCode", shippingAddressMap.get("zipCode"));
                            detail.put("province", stateMap.get("name"));
                            detail.put("city", cityMap.get("name"));
                            detail.put("district", shippingAddressMap.get("addressLine"));

                            Map<String, Object> neighborhoodMap = (Map<String, Object>) shippingAddressMap.get("neighborhood");
                            Map<String, Object> municipalityMap = (Map<String, Object>) shippingAddressMap.get("municipality");

                            detail.put("fullAddress", (ObjectUtil.isNotEmpty(neighborhoodMap.get("name")) ? neighborhoodMap.get("name") : "") + " "
                                    + (ObjectUtil.isNotEmpty(municipalityMap.get("name")) ? municipalityMap.get("name") : "") + " "
                                    + shippingAddressMap.get("comment"));

                            detail.put("mainStreet", (ObjectUtil.isNotEmpty(neighborhoodMap.get("name")) ? neighborhoodMap.get("name") : "") + " "
                                    + (ObjectUtil.isNotEmpty(municipalityMap.get("name")) ? municipalityMap.get("name") : "") + " "
                                    + shippingAddressMap.get("comment"));
                            detail.put("mainPhone", destinationMap.get("receiverPhone"));
                        }
                    }
                }
            }
        }

        return detailList;
    }
}
