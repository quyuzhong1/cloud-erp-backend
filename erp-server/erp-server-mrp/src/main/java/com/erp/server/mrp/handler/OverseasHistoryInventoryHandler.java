package com.erp.server.mrp.handler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OverseasHistoryInventoryHandler extends AbstractQueryHandler {
    @Resource
    private OverseasProviderFeign overseasProviderFeign;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("warehouseId".equals(field)) {
            // 查询关联仓库ID
            List<String> codeList = overseasProviderFeign.listProviderWarehouseBySql(compareCodeSplicingValueSql);
            if (CollectionUtils.isEmpty(codeList)) {
                return getQueryEmptySql();
            }
            super.buildSplicingSQLDTO("oi.warehouse_code", QueryConditionEnum.IN_LIST, codeList, QueryDataTypeEnum.STRING);
        }
        return null;
    }
}
