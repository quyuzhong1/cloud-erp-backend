package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.ArrivalStatusEnum;
import com.erp.model.scm.enums.PurchaseListTypeEnum;
import com.erp.model.scm.vo.SupplierRefUserVO;
import com.erp.server.scm.service.CommonService;
import com.erp.server.scm.service.SupplierRefUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @date 2024年01月18日 9:54
 */
@Component
public class SupplierUserQueryHandler extends AbstractQueryHandler {

    @Resource
    private CommonService commonService;

    @Resource
    private SupplierRefUserService supplierRefUserService;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("supplierIds".equals(field)){
            //根据供应商进行 重置用户id
            //选择了供应商则先进行供应商查询，获取用户ids
            List<SupplierRefUserVO> supplierRefUserVOS = supplierRefUserService.getUserIdsBySupplierIds((List<String>) value, true);
            if (CollectionUtils.isNotEmpty(supplierRefUserVOS)) {
                List<String> userIds = supplierRefUserVOS.stream().map(SupplierRefUserVO::getUid).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(userIds)){
                    super.buildDefaultDTO("sui.uid", userIds);
                }else {
                    return this.getQueryEmptySql();
                }
            } else {
                //防止查询数据为空时，数据穿插
                return this.getQueryEmptySql();
            }
        }
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

