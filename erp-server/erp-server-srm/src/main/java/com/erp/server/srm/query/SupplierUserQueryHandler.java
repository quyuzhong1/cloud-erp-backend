package com.erp.server.srm.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.vo.SupplierRefUserVO;
import com.erp.rpc.scm.feign.SupplierUserFeign;
import com.erp.server.srm.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @date 2024年01月18日 9:54
 */
@Component
public class SupplierUserQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("isBindWechat".equals(field)){
            boolean isBindWechat = (boolean) value;
            if (isBindWechat){
                return  "suw.union_id IS NOT NULL";
            }else {
                return  "suw.union_id IS NULL";
            }
        }
        return null;
    }
}

