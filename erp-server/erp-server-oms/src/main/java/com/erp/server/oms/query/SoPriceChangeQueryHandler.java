package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.SoPriceChangeTabFlagEnum;
import com.erp.server.oms.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author will
 * @date 2024年01月08日 9:54
 */
@Component
public class SoPriceChangeQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //选项卡
        if("tab".equals(field)){
            return getTabSql(value);
        }
        return null;
    }

    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2024/1/18 19:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        //待我审核
        if (SoPriceChangeTabFlagEnum.APPROVE_ING.getCode().equals(value)) {
            super.buildDefaultDTO("sp.approve_status",Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
            //需要审核的业务ids
            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SO_PRICE_CHANGE.getCode());
            if (CollectionUtils.isNotEmpty(businessIds)) {
                super.buildDefaultDTO("sp.id", businessIds);
            }else{
                //返回空结果
                return this.getQueryEmptySql();
            }
        }
        // 已审核
        if (SoPriceChangeTabFlagEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sp.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
        }
        //不通过
        if (SoPriceChangeTabFlagEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("sp.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
        }
        return super.getSplicingSQL();
    }

}

