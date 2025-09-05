package com.erp.server.oms.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ExhibitionOrderQueryHandler extends AbstractQueryHandler {


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        /**
         * SPU编号查询
         */
        if("spuNo".equals(field)){
            String queryField = "pi.spu_no";
            List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
                    || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) ||  queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,queryConditionEnum,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<SkuVO> skuVOS = plmTaskFeign.getSkuInfoAdvanceQuery(advanceQueryContainer);
                List<String> skuIds = skuVOS.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(skuIds)){
                    return this.getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("eod.sku_id",QueryConditionEnum.IN_LIST,skuIds, QueryDataTypeEnum.STRING);
            }
            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,QueryConditionEnum.IN_LIST,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<SkuVO> skuVOS = plmTaskFeign.getSkuInfoAdvanceQuery(advanceQueryContainer);
                List<String> skuIds = skuVOS.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(skuIds)){
                    return this.getQueryAllSql();
                }
                super.buildSplicingSQLDTO("eod.sku_id",QueryConditionEnum.NOT_IN_LIST,skuIds,QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }

        super.buildDefaultDTO("eo.approve_status", value);
        return super.getSplicingSQL();
    }
}
