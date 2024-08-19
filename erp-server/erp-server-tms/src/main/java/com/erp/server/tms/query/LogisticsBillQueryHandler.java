package com.erp.server.tms.query;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.utils.CollectionUtils;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.TransferDeclareTabFlagEnum;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.server.tms.service.DictBasicService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 中转报关表
 */
@Component
public class LogisticsBillQueryHandler extends AbstractQueryHandler {
    @Autowired
    private DictBasicService dictBasicService;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("tab")){
            String statusGroupType = DictBasicEnum.LOGISTIC_TRACK_STATUS_GROUP.getType();
            String statusType = DictBasicEnum.LOGISTIC_TRACK_STATUS.getType();
            List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(statusGroupType);
            List<DictBasicDTO.ViewDTO> trackStatusList = dictBasicService.getByKey(statusType);
            DictBasicDTO.ViewDTO viewDTO = dictList.stream().filter(e -> Objects.nonNull(value) && StringUtils.isNotBlank(e.getRemark()) && Objects.equals(value.toString(), e.getRemark()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(viewDTO)){
                String remark = viewDTO.getRemark();
                List<String> statusList = trackStatusList.stream().filter(s -> StringUtils.isNotBlank(remark) && s.getRemark().equals(remark)).
                        map(DictBasicDTO.ViewDTO::getCode).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(statusList)){
                    this.buildDefaultDTO("lbd.track_status", statusList);
                }
            }
        }
        return null;
    }
}

