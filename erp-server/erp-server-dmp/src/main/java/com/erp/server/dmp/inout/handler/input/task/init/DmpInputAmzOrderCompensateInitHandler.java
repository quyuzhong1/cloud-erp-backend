package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzOrderCompensateInitHandler extends DmpInputAmzCommonInitHandler {

    @Resource
    private DmpSoInfoService dmpSoInfoService;
    @Resource
    private DmpCfgInputDetailService dmpCfgInputDetailService;

    public static final String ORDER_ID_LIST = "orderIdList";

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取缺失的信息
        List<DmpSoInfoEntity> soList = dmpSoInfoService.findSoMissingDetail(
                dmpInputTaskEntity.getStartTime(),
                dmpInputTaskEntity.getEndTime(),
                PlatformDictEnum.AMAZON.getCode(),
                nextLevelId);
        if (CollectionUtils.isEmpty(soList)){
            log.warn("【亚马逊订单补偿】 shopId={},无信息补充", nextLevelId);
            return Collections.emptyList();
        }
        List<String> orderCodeList = soList.stream().map(DmpSoInfoEntity::getPlatformCode).distinct().collect(Collectors.toList());
        // 按数量30分组
        List<List<String>> partition = Lists.partition(orderCodeList, 20);

        //查询任务是否存在
        List<DmpInoutDTO.ListDTO> list =  dmpCfgInputDetailService.listBySystemCodeAndBillType(
                Collections.singletonList(PlatformDictEnum.AMAZON.getCode()),
                Collections.singletonList(BusinessTypeEnum.ORDER.getCode()),
                Collections.singletonList(nextLevelId));
        if (CollectionUtils.isEmpty(list)){
            ServiceException.runError("任务不存在");
        }
        DmpInoutDTO.ListDTO listDTO = list.get(0);

        List<Map<String, List<String>>> resultList = new LinkedList<>();
        for (List<String> curList : partition) {
            // 构建 orderIdList 并封装为 JSON
            Map<String, List<String>> map = Collections.singletonMap(ORDER_ID_LIST, curList);
            resultList.add(map);
            String detailJson = JSON.toJSONString(map);
            // 创建中台任务
            DmpInputHotfixCreateRequest dmpInputCreateRequest = new DmpInputHotfixCreateRequest();
            dmpInputCreateRequest.setCfgInputDetailIdList(Collections.singletonList(listDTO.getDetailId()));
            dmpInputCreateRequest.setCfgInputId(listDTO.getCfgInputId());
            dmpInputCreateRequest.setDetailExtendJson(detailJson);
            // 拉取时间
            dmpInputCreateRequest.setStartTime(dmpInputTaskEntity.getStartTime());
            dmpInputCreateRequest.setEndTime(dmpInputTaskEntity.getEndTime());
            dmpInputCreateRequest.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
            // 创建任务
            dmpInputCreateFactory.createHotfixInputTask(dmpInputCreateRequest);
        }

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(resultList)));
    }
}
