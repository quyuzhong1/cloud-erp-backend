package com.erp.server.srm.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.WaitDeliveryCycleEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class WaitDeliveryQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            if("all".equals(value)){
                return " pod.execution_status in ('confirm','delivery') ";
            }
            if(WaitDeliveryCycleEnum.EXPIRED.getCode().equals(value)){
                return " date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) <= 0";
            }
            if(WaitDeliveryCycleEnum.ALMOST_OVERDUE.getCode().equals(value)){
                return " date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) > 0 and date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) <= 7";
            }
            if(WaitDeliveryCycleEnum.IN_ONE_MONTH.getCode().equals(value)){
                return " date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) > 7 and date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) <= 30";
            }
            if(WaitDeliveryCycleEnum.IN_TWO_MONTH.getCode().equals(value)){
                return " date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) > 30 and date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) <= 60";
            }
            if(WaitDeliveryCycleEnum.TWO_MONTH_LATER.getCode().equals(value)){
                return " date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) > 60 ";
            }
        }
        if ("deliveryCycle".equals(field)){
            List<String> deliveryCycle = new ArrayList<>();
            if (value instanceof String){
                deliveryCycle.add((String) value);
            }else if (value instanceof List){
                deliveryCycle.addAll((Collection<? extends String>) value);
            }
            if (CollectionUtils.isEmpty(deliveryCycle)){
                return null;
            }
            StringBuffer sb = new StringBuffer();
            sb.append("(");
            for (int i = 0; i < deliveryCycle.size(); i++) {
                if (i > 0){
                    sb.append(" or ");
                }
                if (WaitDeliveryCycleEnum.EXPIRED.getCode().equals(deliveryCycle.get(i))){
                    sb.append(" (date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) <= 0) ");
                }else if(WaitDeliveryCycleEnum.ALMOST_OVERDUE.getCode().equals(deliveryCycle.get(i))){
                    sb.append(" (date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) > 0 and date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) <= 7)");
                }else if(WaitDeliveryCycleEnum.IN_ONE_MONTH.getCode().equals(deliveryCycle.get(i))){
                    sb.append(" (date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) > 7 and date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) <= 30)");
                }else if(WaitDeliveryCycleEnum.IN_TWO_MONTH.getCode().equals(deliveryCycle.get(i))){
                    sb.append(" (date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) > 30 and date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) <= 60)");
                }else if(WaitDeliveryCycleEnum.TWO_MONTH_LATER.getCode().equals(deliveryCycle.get(i))){
                    sb.append(" (date_part('day', pod.plan_delivery_date::timestamp - now()::timestamp) > 60) ");
                }
            }
            sb.append(")");
        }
        return null;
    }
}

