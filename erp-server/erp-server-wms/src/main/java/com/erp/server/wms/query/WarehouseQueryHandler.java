package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.entity.CustomerInfoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓位移动搜索条件
 *
 * @author hyj
 * @date 2024/5/22
 */
@Component
public class WarehouseQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("name".equals(field)) {
            super.buildDefaultDTO("name", value);
        }
        if ("kingdeeWarehouseCode".equals(field)) {
            super.buildDefaultDTO("kingdeeWarehouseCode", value);
        }
        if ("orgId".equals(field)) {
            super.buildDefaultDTO("orgId", value);
        }
        return null;
    }


}
