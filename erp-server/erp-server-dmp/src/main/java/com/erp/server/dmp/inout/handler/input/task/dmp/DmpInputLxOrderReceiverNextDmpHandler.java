package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxOrderReceiverNextDmpHandler extends DmpInputDoNextDmpHandler {


    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object addressObj = dmpInputMongoEntity.get("address_info");
        if (null == addressObj) {
            return Collections.emptyList();
        }
        Map<String, Object> addressMap = (Map<String, Object>) addressObj;
        return Collections.singletonList(addressMap);
    }
}
