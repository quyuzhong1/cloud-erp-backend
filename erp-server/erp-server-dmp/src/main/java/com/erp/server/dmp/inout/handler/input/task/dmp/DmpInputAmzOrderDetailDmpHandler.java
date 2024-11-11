package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cItemStatusEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Money;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

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
        for (Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {

            //优惠
            Object promotionDiscountObj = dmpInputMongoChild.get("promotionDiscount");
            if (promotionDiscountObj != null) {
                Map<String, Object> promotionDiscountMap = (Map<String, Object>) promotionDiscountObj;
                dmpInputMongoChild.put("discount", promotionDiscountMap.get("amount"));

        }
        return dmpInputMongoChildList;
    }

}
