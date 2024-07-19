package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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

                Object recipientAddress = detail.get("recipientAddress");
                if (ObjectUtil.isNotEmpty(recipientAddress)) {
                    Map<String, Object> recipientAddressMap = (Map<String, Object>) recipientAddress;
                    detail.put("buyerName", recipientAddressMap.get("name"));

                    List<Map<String, Object>> districtInfoList = (List<Map<String, Object>>) recipientAddressMap.get("districtInfo");


                }


            }
        }

        return detailList;
    }
}
