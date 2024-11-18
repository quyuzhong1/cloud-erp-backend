package com.erp.server.wms.query;

import cn.hutool.core.util.ObjUtil;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.utils.QueryUtils;
import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.erp.model.wms.enums.BillTypeEnum;
import com.erp.server.wms.service.StocktakingTaskUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class StocktakingProfitLossQueryHandler extends AbstractQueryHandler {

    @Resource
    private StocktakingTaskUserService stocktakingTaskUserService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if("stocktakingUserId".equals(field)){
            if (ObjUtil.isNotEmpty(value)) {
                List<String> stringList = new ArrayList<>();
                if (value instanceof String) {
                    stringList = Collections.singletonList(value.toString());
                } else if (value instanceof List) {
                    Collection<String> collection = (Collection<String>) value;
                     stringList = new ArrayList<>(collection);
                }
                List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listByUserIds(stringList);
                List<String> sourceIdList = taskUserList.stream().map(StocktakingTaskUserEntity::getSourceId).collect(Collectors.toList());

                //比较符
                QueryConditionEnum condEnum = QueryConditionEnum.IN_LIST;
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(null,condEnum,sourceIdList,QueryDataTypeEnum.STRING);

                String compareValueSQL = QueryUtils.splicingCompareValueSQL(condEnum,advanceQueryDTO);

                return "(spl.source_id "+ compareValueSQL +
                        " or spl.id "+ compareValueSQL +")";
            }
        }
        return null;
    }


    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        // 盘盈单
        if (BillTypeEnum.PROFIT.getCode().equals(value)) {
            super.buildDefaultDTO("spl.bill_type", BillTypeEnum.PROFIT.getCode());
        }
        // 盘亏单
        if (BillTypeEnum.LOSS.getCode().equals(value)) {
            super.buildDefaultDTO("spl.bill_type", BillTypeEnum.LOSS.getCode());
        }
        return super.getSplicingSQL();
    }
}
