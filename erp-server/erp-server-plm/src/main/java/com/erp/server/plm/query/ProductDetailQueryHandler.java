package com.erp.server.plm.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductRefLabelService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductDetailQueryHandler extends AbstractQueryHandler {

    @Resource
    private ProductRefLabelService productRefLabelService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("label".equals(field)) {
            if (null == value){
                return this.getQueryAllSql();
            }
            List<String> labelIds = JSONArray.parseArray(JSON.toJSONString(value))
                    .stream().map(Object::toString)
                    .distinct()
                    .collect(Collectors.toList());
            List<String> labelProductIds = null;
            if (!CollectionUtils.isEmpty(labelIds)) {
                List<ProductRefLabelVO> productRefLabelVOS = productRefLabelService.getLabelListByIds(null, new HashSet<>(labelIds), null);
                if (!CollectionUtils.isEmpty(productRefLabelVOS)) {
                    labelProductIds = productRefLabelVOS.stream().map(ProductRefLabelVO::getProductId).collect(Collectors.toList());
                } else {
                    labelProductIds = new ArrayList<>();
                    labelProductIds.add("-1");
                }
            }
            if (!CollectionUtils.isEmpty(labelProductIds)){
                super.buildSplicingSQLDTO("pi.id", QueryConditionEnum.IN_LIST, labelProductIds, QueryDataTypeEnum.STRING);
            }
            return super.getSplicingSQL();
        }
        return null;
    }

}

