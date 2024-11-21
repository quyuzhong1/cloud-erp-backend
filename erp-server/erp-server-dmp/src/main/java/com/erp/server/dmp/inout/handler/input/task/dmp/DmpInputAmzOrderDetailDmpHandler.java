package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Money;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderItem;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzOrderDetailDmpHandler extends DmpInputAmzOrderDoChildDmpHandler {

    @Override
    protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList) {
        if (CollUtil.isEmpty(dmpInputMongoChildList)) {
            return dmpInputMongoChildList;
        }
        return dmpInputMongoChildList;
    }

}
