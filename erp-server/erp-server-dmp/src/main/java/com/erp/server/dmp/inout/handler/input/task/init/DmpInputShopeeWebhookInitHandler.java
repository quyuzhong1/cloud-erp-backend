package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 虾皮订单状态 webhook 初始化数据。
 */
@Service
@Scope("prototype")
public class DmpInputShopeeWebhookInitHandler extends DmpInputInitHandler {

    private static final String NEXT_LEVEL_ID = "nextLevelId";

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String data = ThirdWarehouseContext.getData();
        if (StringUtils.isBlank(data)) {
            return Collections.emptyList();
        }
        JSONObject payload = JSONUtil.parseObj(data);
        JSONObject webhookData = payload.getJSONObject("data");
        if (Objects.isNull(webhookData)) {
            return Collections.emptyList();
        }
        JSONObject initData = JSONUtil.createObj();
        initData.set("thirdCode", webhookData.getStr("ordersn"));
        initData.set("platformCode", webhookData.getStr("ordersn"));
        initData.set("platformOriginalStatus", webhookData.getStr("status"));
        initData.set("updateTime", webhookData.getLong("update_time"));
        initData.set("deliveryTime", webhookData.getLong("update_time"));
        initData.set("shopId", payload.getStr("shop_id"));
        initData.set("msgId", payload.getStr("msg_id"));
        if (StringUtils.isAnyBlank(initData.getStr("thirdCode"), initData.getStr("platformOriginalStatus")) || Objects.isNull(initData.getLong("deliveryTime"))) {
            return Collections.emptyList();
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(initData.toString()));
    }
}
