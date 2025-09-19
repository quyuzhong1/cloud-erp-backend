package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONObjectCodec;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class TikTokNextDmpHandler extends DmpInputDoNextDmpHandler{


    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
        List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
        if(CollUtil.isNotEmpty(detailList)) {
            for(Map<String, Object> detail : detailList) {
                detail.put("receiverTaxNo", detail.get("cpf"));
                detail.put("email", detail.get("buyerEmail"));
                Object recipientAddress = detail.get("recipientAddress");
                if (ObjectUtil.isNotEmpty(recipientAddress)) {
                    Map<String, Object> recipientAddressMap = (Map<String, Object>) recipientAddress;
                    String regionCode = "";
                    Object regionCodeObj = recipientAddressMap.get("regionCode");
                    if (null != regionCodeObj) {
                        regionCode = regionCodeObj.toString();
                    }
                    if ("JP".equalsIgnoreCase(regionCode)) {
                        String newName = "";
                        if (recipientAddressMap.get("lastName") != null && recipientAddressMap.get("firstName") != null) {
                            newName = CharSequenceUtil.format("{} {}", recipientAddressMap.get("lastName"), recipientAddressMap.get("firstName"));
                        }
                        detail.put("buyerName", newName);
                        detail.put("receiverName", newName);
                    } else {
                        detail.put("buyerName", recipientAddressMap.get("name"));
                        detail.put("receiverName", recipientAddressMap.get("name"));
                    }
                    detail.put("mainStreet", recipientAddressMap.get("addressLine1"));
                    detail.put("secondStreet", recipientAddressMap.get("addressLine2") + " " + recipientAddressMap.get("addressLine3") + " " + recipientAddressMap.get("addressLine4"));
                    detail.put("mainPhone", recipientAddressMap.get("phoneNumber"));
                    detail.put("receiverTelNumber", recipientAddressMap.get("phoneNumber"));
                    detail.put("country", recipientAddressMap.get("regionCode"));
                    detail.put("postCode", recipientAddressMap.get("postalCode"));
                    detail.put("fullAddress", recipientAddressMap.get("fullAddress") + " " + recipientAddressMap.get("addressDetail"));

                    List<Map<String, Object>> districtInfoList = (List<Map<String, Object>>) recipientAddressMap.get("districtInfo");

                    String district = "";
                    for (Map<String, Object> map : districtInfoList) {
                        Object addressLevelName = map.get("addressLevelName");
                        if ("JP".equalsIgnoreCase(regionCode)){
                            if (String.valueOf(addressLevelName).equalsIgnoreCase("Prefecture")) {
                                detail.put("province", map.get("addressName"));
                            }
                            if (String.valueOf(addressLevelName).equalsIgnoreCase("City, Town, Village")) {
                                detail.put("city", map.get("addressName"));
                            }
                        } else {
                            if (String.valueOf(addressLevelName).equalsIgnoreCase("state")) {
                                detail.put("province", map.get("addressName"));
                            }
                            if (String.valueOf(addressLevelName).equalsIgnoreCase("city")) {
                                detail.put("city", map.get("addressName"));
                            }
                        }
                        if (String.valueOf(addressLevelName).equalsIgnoreCase("Sub-district")) {
                            district = district + " " + map.get("addressName");
                        }
                        if (String.valueOf(addressLevelName).equalsIgnoreCase("Urban Community")) {
                            district = district + " " + map.get("addressName");
                        }
                    }
                    detail.put("district", district);
                }
            }
        }
        return detailList;
    }
}
