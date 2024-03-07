package com.erp.server.wms.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.QueryUtils;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class SoReturnNoticeQueryHandler extends AbstractQueryHandler {


    @Resource
    private SoReturnFeign soReturnFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //退货类型
        if("srnd.return_type_dict".equals(field)){
            List<String> valueList;
            if (value instanceof Collection<?>) {
                Collection<Object> list = (Collection<Object>) value;
                valueList = list.stream().map(Object::toString).collect(Collectors.toList());
            } else {
                valueList = Collections.singletonList(value.toString());
            }
            List<SoReturnDetailEntity> soReturnDetailEntityList = soReturnFeign.listDetailByReturnType(valueList);
            List<String> detailIdList = soReturnDetailEntityList.stream().map(SoReturnDetailEntity::getId).collect(Collectors.toList());
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
                    || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) ||  queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
                if(CollectionUtils.isEmpty(valueList)){
                    return this.getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("srnd.source_detail_id",QueryConditionEnum.IN_LIST,detailIdList,QueryDataTypeEnum.STRING);
            }
            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                if(CollectionUtils.isEmpty(valueList)){
                    return this.getQueryAllSql();
                }
                super.buildSplicingSQLDTO("srnd.source_detail_id",QueryConditionEnum.NOT_IN_LIST,detailIdList,QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}

