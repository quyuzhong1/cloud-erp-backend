package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: wtr
 * @Date: 2025/10/30 9:22
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class AssetPurchaseChangeQueryHandler extends AbstractQueryHandler {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            String status = value.toString();
            if("waitSubmit".equals(status)){
                super.buildDefaultDTO("apc.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }else if ("approveIng".equals(status)){
                super.buildDefaultDTO("apc.approve_status",ApproveStatusEnum.APPROVE_ING.getStatus());
            }else if ("approve".equals(status)){
                super.buildDefaultDTO("apc.approve_status",ApproveStatusEnum.APPROVE.getStatus());
            }else if ("reject".equals(status)){
                super.buildDefaultDTO("apc.approve_status",ApproveStatusEnum.REJECT.getStatus());
            }else {
                return this.getQueryAllSql();
            }
        }

        // 模具名称查询：根据模具档案名称远程查询模具编码，再按 asset_code in 查询
        if (isAssetNameField(field)) {
            return handleAssetNameQuery(value, "apcd.asset_code");
        }

        return null;
    }

    private String handleAssetNameQuery(Object value, String assetCodeField) {
        String moldName = value != null ? value.toString() : null;
        if (StringUtils.isBlank(moldName)) {
            return null;
        }
        List<String> moldCodes = plmTaskFeign.listMoldCodesByName(moldName);
        if (CollectionUtils.isNotEmpty(moldCodes)) {
            super.buildDefaultDTO(assetCodeField, moldCodes);
            return null;
        }
        return this.getQueryEmptySql();
    }

    private boolean isAssetNameField(String field) {
        return "assetName".equals(field) || "moldName".equals(field) || "apcd.asset_name".equals(field);
    }
}
