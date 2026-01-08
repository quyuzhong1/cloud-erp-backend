package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: wtr
 * @Date: 2025/10/17 14:06
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
public class AssetNoticeQueryHandler extends AbstractQueryHandler {
    
    @Resource
    private PlmTaskFeign plmTaskFeign;
    
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            String status = value.toString();
            if("waitSubmit".equals(status)){
                super.buildDefaultDTO("an.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }else if ("approveIng".equals(status)){
                super.buildDefaultDTO("an.approve_status",ApproveStatusEnum.APPROVE_ING.getStatus());
            }else if ("waitCreate".equals(status)){
//                super.buildDefaultDTO("an.approve_status",ApproveStatusEnum.APPROVE.getStatus());
//                super.buildDefaultDTO("and1.create_po_type", Arrays.asList(CreatePoTypeEnum.NOT_GENERATED.getStatus()));
//                super.buildDefaultDTO("and1.create_po_type", Arrays.asList(CreatePoTypeEnum.PARTIAL_GENERATED.getStatus()));
                return "an.approve_status = 'approve' and and1.create_po_type in ('0','1')";
            }else if ("created".equals(status)){
                super.buildDefaultDTO("an.approve_status",ApproveStatusEnum.APPROVE.getStatus());
                super.buildDefaultDTO("and1.create_po_type", Arrays.asList(CreatePoTypeEnum.ALL_GENERATED.getStatus()));
            }else if ("reject".equals(status)){
                super.buildDefaultDTO("an.approve_status",ApproveStatusEnum.REJECT.getStatus());
            }else {
                return this.getQueryAllSql();
            }
        }
        
        // 项目名称查询：根据项目名称远程查询对应的模具编码，然后使用 in 查询
        if ("projectName".equals(field)) {
            String projectName = value != null ? value.toString() : null;
            if (StringUtils.isNotBlank(projectName)) {
                // 远程查询根据项目名称获取模具编码列表
                List<String> moldCodes = plmTaskFeign.listMoldCodesByProjectName(projectName);
                if (CollectionUtils.isNotEmpty(moldCodes)) {
                    // 使用模具编码列表进行 in 查询
                    super.buildDefaultDTO("and1.asset_code", moldCodes);
                } else {
                    // 如果没有找到匹配的模具编码，返回空结果
                    return this.getQueryEmptySql();
                }
            }
        }

        return null;
    }
}
