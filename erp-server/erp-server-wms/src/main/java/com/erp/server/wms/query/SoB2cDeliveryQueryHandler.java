package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SoB2cDeliveryQueryHandler extends AbstractQueryHandler {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("sbd.logistic_type".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            } else {
                super.buildDefaultDTO("sbd.logistic_type", searchType);
            }
        }

        if("sbd.tab".equals(field)){
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }

            // 待处理
            if (SoB2cDeliveryStatusEnum.WAIT_HANDLE.getStatus().equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.WAIT_HANDLE.getStatus());
            }
            //拣货中
            if (SoB2cDeliveryStatusEnum.PICKING.getStatus().equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.PICKING.getStatus());
            }
            //虚假发货
            if (SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getStatus().equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getStatus());
            }
            //已发货
            if (SoB2cDeliveryStatusEnum.SHIPPED.getStatus().equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.SHIPPED.getStatus());
            }
            //取消发货
            if (SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus().equals(searchType)) {
                super.buildDefaultDTO("sbd.status", SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());
            }
        }
        if("isIntercept".equals(field)){
            List<SoB2cEntity> soB2cEntities = soB2cFeign.listWithIsIntercept();
            if(CollectionUtils.isEmpty(soB2cEntities)){
                return getQueryEmptySql();
            }
            List<String> soIds = soB2cEntities.stream().map(BaseEntity::getId).collect(Collectors.toList());
            Boolean bool = (Boolean) value;
            if(bool){
                super.buildDefaultDTO("sbd.source_id",soIds);
            }else {
                super.buildSplicingSQLDTO("sbd.source_id", QueryConditionEnum.NOT_IN_LIST,soIds, QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}
