package com.erp.server.oms.sdk.sob2c;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cSourcePlatformEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.oms.service.ISoB2cHandleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public abstract class AbstractSoB2cHandle implements ISoB2cHandleService {


    public static final String ORDER_ID_LIST = "orderIdList";

    public static final String NORMAL = "normal";

    /**
     * 转换订单拉取任务
     */
    public DmpInoutDTO.CreateInputDTO createInputDTO(List<SoB2cEntity> shopGroupedList) {
        SoB2cEntity soB2cEntity = shopGroupedList.get(0);
        DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
        dto.setNextLevelId(soB2cEntity.getShopId());
        dto.setSystemCode(soB2cEntity.getDictPlatform());
        dto.setBillType(BusinessTypeEnum.ORDER.getCode());
        //  DmpInputTaskTaskTypeEnum	NORMAL("normal", "正常任务"),
        dto.setTaskType(NORMAL);

        // 构建 orderIdList 并封装为 JSON
        Map<String, List<String>> map = Collections.singletonMap(
                ORDER_ID_LIST,
                shopGroupedList.stream()
                        .map(SoB2cEntity::getPlatformCode)
                        .collect(Collectors.toList())
        );
        dto.setDetailExtendJson(JSON.toJSONString(map));
        return dto;
    }
}
