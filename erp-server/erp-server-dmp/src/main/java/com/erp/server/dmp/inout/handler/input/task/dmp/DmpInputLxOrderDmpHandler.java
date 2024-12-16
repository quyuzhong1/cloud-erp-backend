package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.erp.server.dmp.service.ThirdShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxOrderDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private ThirdShopService thirdShopService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputLxOrderDmpHandler 处理完成: taskId={}", dmpInputTaskEntity.getId());
        // 领星平台来源
        List<DictBasicDTO.ViewDTO> dictbaseList = dictBasicService.getByKey("lingxingPlatformCode");


        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                // update_time
                String updateTimeStr = dmpDataMap.getOrDefault("update_time", "").toString();
                LocalDateTime platformUpdateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(updateTimeStr)), ZoneId.systemDefault());
                dmpDataMap.put("platformUpdateTime", platformUpdateTime);

                // 创建时间
                String platformCreateTimeStr = dmpDataMap.getOrDefault("global_purchase_time", "").toString();
                LocalDateTime platformCreateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(platformCreateTimeStr)), ZoneId.systemDefault());
                dmpDataMap.put("platformCreateTime", platformCreateTime);

                // 平台原始信息
                Object platformInfoListObj = dmpDataMap.get("platform_info");
                if (null != platformInfoListObj) {
                    JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(platformInfoListObj));
                    if (CollectionUtils.isNotEmpty(jsonArray)) {
                        Object platformInfoObjIndex1 = jsonArray.get(0);
                        Map<String, Object> platformInfoMap = (Map<String, Object>) platformInfoObjIndex1;
                        String platformOriginalStatus = platformInfoMap.getOrDefault("status", "").toString();
                        dmpDataMap.put("platformOriginalStatus", platformOriginalStatus);

                        // 解析来源平台
                        String platformCodeStr = platformInfoMap.getOrDefault("platform_code", "").toString();
                        if (StringUtils.isNotBlank(platformCodeStr)) {
                            DictBasicDTO.ViewDTO viewDTO = dictbaseList.stream().filter(e -> e.getValue().equalsIgnoreCase(platformCodeStr)).findFirst().orElse(null);
                            if (null == viewDTO){
                                ServiceException.runError("dmp字典未找到领星平台：" + platformCodeStr);
                            }
                            String name = viewDTO.getName();
                            dmpDataMap.put("sourcePlatform", name);
                        }
                    }
                }
            }
        }
    }

}
