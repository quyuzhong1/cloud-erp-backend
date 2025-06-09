package com.erp.server.tms.query;

import cn.hutool.core.collection.CollUtil;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.server.tms.service.DictBasicService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 中转报关表
 */
@Component
public class LogisticsBillQueryHandler extends AbstractQueryHandler {
    @Resource
    private DictBasicService dictBasicService;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("tab")){
            String statusGroupType = DictBasicEnum.LOGISTIC_TRACK_STATUS_GROUP.getType();
            String statusType = DictBasicEnum.LOGISTIC_TRACK_STATUS.getType();
            List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(statusGroupType);
            List<DictBasicDTO.ViewDTO> trackStatusList = dictBasicService.getByKey(statusType);
            DictBasicDTO.ViewDTO viewDTO = dictList.stream().filter(e -> Objects.nonNull(value) && StringUtils.isNotBlank(e.getCode())
                            && Objects.equals(value.toString(), e.getCode())).findFirst().orElse(null);
            if (Objects.nonNull(viewDTO)){
                String code = viewDTO.getCode();
                List<String> statusList = trackStatusList.stream().filter(s -> StringUtils.isNotBlank(code) && StringUtils.isNotBlank(s.getRemark())
                                && s.getRemark().equals(code)).map(DictBasicDTO.ViewDTO::getCode).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(statusList)){
                    this.buildDefaultDTO("lbd.track_status", statusList);
                }
            }
        }
        return null;
    }
}

