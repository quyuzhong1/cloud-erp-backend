package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercadolocal.dto.mercadolocal.shipment.BillViewDTO;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class MercadoLocalBillDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        String id = dmpInputTaskEntity.getId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, id).list();
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        DmpInputTaskEntity dmpInputTaskEntity = list.stream().filter(req -> "1899659842348408324".equals(req.getCfgInputId())).findFirst().orElse(null);

        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getId()));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "mercadolibreLocal_bill_data");

        List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
        if (CollUtil.isNotEmpty(detailList)) {
            for (Map<String, Object> detail : detailList) {
                Object fid = detail.get("fid");
                if (fid != null) {
                    Map<String, Object> billMap = dmpInputMongoChildList.stream().filter(req -> req.get("fid").toString().equals(fid.toString())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(billMap)) {
                        //地址
                        detail.put("nextLevelId", nextLevelId);
                        detail.put("sourcePlatform", JSON.parseObject(dmpCfgInputConvertEntity.getFixedValueJson()).get("sourcePlatform"));
                        detail.put("sourceSystem", JSON.parseObject(dmpCfgInputConvertEntity.getFixedValueJson()).get("sourceSystem"));
                        detail.put("thirdCode", fid);
                        detail.put("platformCode", fid);
                        detail.put("thirdDetailId", fid);
                        detail.put("platformDetailId", fid);
                        detail.put("shopId", nextLevelId);
                        BillViewDTO billViewDTO = JSON.parseObject(JSON.toJSONString(billMap), BillViewDTO.class);
                        BillViewDTO.BuyerDTO buyerDTO = billViewDTO.getBuyer();
                        BillViewDTO.BuyerDTO.BillingInfoDTO billingInfoDTO = buyerDTO.getBillingInfo();
                        BillViewDTO.BuyerDTO.BillingInfoDTO.AddressDTO addressDTO = billingInfoDTO.getAddress();
                        detail.put("postalCode", addressDTO.getZipCode());
                        detail.put("city", addressDTO.getCityName());
                        detail.put("country", addressDTO.getCountryId());
                        detail.put("buyerName", billingInfoDTO.getName()+billingInfoDTO.getLastName());
                        detail.put("address1", addressDTO.getStreetName());
                        detail.put("state", addressDTO.getState().getName());
                        detail.put("taxNo", billingInfoDTO.getIdentification().getNumber());
                        detail.put("registrationNo", billingInfoDTO.getTaxes().getInscriptions().getStateRegistration());
                    }
                }
            }
        }

        return detailList;
    }
}
