package com.erp.server.oms.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class SkuMappingQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            String val = value.toString();
            if("all".equals(val)){
                return "";
            }
            if("not".equals(val)){
                super.buildSplicingSQLDTO("li.match_result", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
            }
            if("already".equals(val)){
                super.buildSplicingSQLDTO("li.match_result", QueryConditionEnum.EQ,true, QueryDataTypeEnum.BOOLEAN);
            }
        }
        return null;
    }
}

