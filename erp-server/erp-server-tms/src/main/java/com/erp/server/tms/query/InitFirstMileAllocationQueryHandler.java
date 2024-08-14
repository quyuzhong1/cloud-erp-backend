package com.erp.server.tms.query;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.server.tms.service.DictBasicService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 中转报关表
 */
@Component
public class InitFirstMileAllocationQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return null;
    }
}

