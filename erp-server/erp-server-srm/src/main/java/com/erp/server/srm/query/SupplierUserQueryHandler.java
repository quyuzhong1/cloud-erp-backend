package com.erp.server.srm.query;

import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.vo.SupplierRefUserVO;
import com.erp.rpc.wms.feign.SupplierUserFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @date 2024年01月18日 9:54
 */
@Component
public class SupplierUserQueryHandler extends AbstractQueryHandler {

    @Resource
    private SupplierUserFeign supplierUserFeign;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("supplierIds".equals(field)){
            List<String> ids = new ArrayList<>();
            //根据供应商进行 重置用户id
            if(value instanceof String){
                ids.add((String) value);
            }else if (value instanceof List){
                ids.addAll((List<String>)value);
            }
            if (CollectionUtils.isEmpty(ids)){
                return this.getQueryEmptySql();
            }
            //选择了供应商则先进行供应商查询，获取用户ids
            List<SupplierRefUserVO> supplierRefUserVOS = supplierUserFeign.getSupplierRefByUids(ids);
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

