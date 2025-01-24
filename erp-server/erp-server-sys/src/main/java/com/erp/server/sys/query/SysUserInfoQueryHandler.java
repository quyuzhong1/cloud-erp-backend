package com.erp.server.sys.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.rpc.oms.feign.ShopSysUserAuthFeign;
import com.erp.server.sys.service.KingdeeDepartmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class SysUserInfoQueryHandler extends AbstractQueryHandler {

    @Resource
    private ShopSysUserAuthFeign shopSysUserAuthFeign;


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("shopId".equals(field)) {
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            List<String> shopIdList;
            if (queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST)) {
                shopIdList = JSONArray.parseArray(JSON.toJSONString(value)).stream().map(Object::toString).collect(Collectors.toList());
            } else {
                shopIdList = Collections.singletonList(value.toString());
            }
            List<String> userIdList = shopSysUserAuthFeign.listUserIdByShopIdList(shopIdList);
            if (CollectionUtils.isEmpty(userIdList)) {
                userIdList.add("-1");
            }
            this.buildDefaultDTO("sui.uid", userIdList);
        }
        if ("role_id".equals(field)){
            return " sui.uid in (select sru.user_id from sys_role_user sru where sru.role_id  " + compareCodeSplicingValueSql + ")";
        }
        if ("department_id".equals(field)){
            return " sui.uid in ( select sdu.user_id from sys_department_user sdu where sdu.department_id  " + compareCodeSplicingValueSql + ")";
        }
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if ("sui.user_state".equals(field)) {
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)) {
                return "sui.user_state is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)) {
                return "sui.user_state is not null";
            }
            return " sui.user_state " + compareCodeSplicingValueSql;
        }
        return null;
    }
}
