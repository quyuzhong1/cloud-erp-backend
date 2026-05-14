package com.erp.server.scm.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.scm.enums.AssetPurchaseOrderTabListEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/10/23 9:53
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class AssetPurchaseOrderQueryHandler extends AbstractQueryHandler {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            String status = value.toString();
            if("waitSubmit".equals(status)){
                super.buildDefaultDTO("apo.approve_status", AssetPurchaseOrderTabListEnum.WAIT_SUBMIT.getStatus());
            }else if ("approveIng".equals(status)){
                super.buildDefaultDTO("apo.approve_status",AssetPurchaseOrderTabListEnum.APPROVE_ING.getStatus());
            }else if ("waitReceive".equals(status)){
                super.buildDefaultDTO("apo.approve_status",ApproveStatusEnum.APPROVE.getStatus());
                super.buildDefaultDTO("apod.end_receive",AssetPurchaseOrderTabListEnum.WAIT_RECEIVE.getStatus());
            }else if ("allReceive".equals(status)){
                super.buildDefaultDTO("apod.end_receive",AssetPurchaseOrderTabListEnum.ALL_RECEIVE.getStatus());
            }else if ("close".equals(status)){
                super.buildDefaultDTO("apod.end_receive",AssetPurchaseOrderTabListEnum.CLOSE.getStatus());
            }else if ("reject".equals(status)){
                super.buildDefaultDTO("apo.approve_status",AssetPurchaseOrderTabListEnum.REJECT.getStatus());
            }else {
                return this.getQueryAllSql();
            }
        }

        // 项目名称查询：把当前高级查询条件透传到 PLM 端走 @WebAdvanceQuery 切面解析，
        // 远端按 mi.project_name 完整支持 EQ / CONTAINS / STARTS_WITH 等所有比较符
        // 和大小写不敏感匹配；这里只用拿到的模具 ID 在主表上做 IN 关联。
        if ("projectName".equals(field)) {
            QueryConditionEnum compareCode = AdvanceQueryContext.getCompareCode();
            if (compareCode == null) {
                return null;
            }
            AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(
                    "mi.project_name", compareCode, value, QueryDataTypeEnum.STRING);
            List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
            advanceQueryDTOList.add(advanceQueryDTO);
            AdvanceQueryContainer container = AdvanceQueryContainer.builder()
                    .advanceQueryDTOList(advanceQueryDTOList).build();
            List<MoldInfoEntity> moldInfoList = plmTaskFeign.listMoldInfoAdvanceQuery(container);
            List<String> moldIds = CollectionUtils.isEmpty(moldInfoList) ? new ArrayList<>()
                    : moldInfoList.stream().map(MoldInfoEntity::getId)
                            .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (compareCode == QueryConditionEnum.NE
                    || compareCode == QueryConditionEnum.NOT_IN_LIST
                    || compareCode == QueryConditionEnum.NOT_CONTAINS) {
                if (moldIds.isEmpty()) {
                    return this.getQueryAllSql();
                }
                super.buildSplicingSQLDTO("apod.asset_id",
                        QueryConditionEnum.NOT_IN_LIST, moldIds, QueryDataTypeEnum.STRING);
            } else {
                if (moldIds.isEmpty()) {
                    return this.getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("apod.asset_id",
                        QueryConditionEnum.IN_LIST, moldIds, QueryDataTypeEnum.STRING);
            }
        }

        return null;
    }
}
