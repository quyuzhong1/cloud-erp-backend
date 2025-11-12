package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.goodcang.dto.request.GoodCangInventoryRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputGoodCangInstockTransFlowInitHandler extends DmpInputGoodCangTransFlowInitHandler {

    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 顶级mongo数据
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isBlank(parentStorageName)) {
            return Collections.emptyList();
        }
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        List<Map<String, Object>> parentData = mongoService.findMongoData(paramDataList, parentStorageName);
        if (CollectionUtils.isEmpty(parentData)) {
            // 主数据不存在明细无需处理
            return Collections.emptyList();
        }
        // 父级任务
        DmpInputTaskEntity parentTaskEntity = dmpInputTaskService.getById(dmpInputTaskEntity.getParentTaskId());
        // 开始时间=任务开始时间
        LocalDateTime createDateFrom = parentTaskEntity.getStartTime();
        // 结束时间=任务指定结束时间
//        LocalDateTime createDateEnd = parentTaskEntity.getEndTime();
        LocalDateTime createDateEnd = LocalDateTime.now();
        // 退货单号列表
        List<String> referenceNoList = new LinkedList<>();
        for (Map<String, Object> parentDatum : parentData) {
//            String createTime = parentDatum.getOrDefault("create_at", "").toString();
//            if (StringUtils.isNotBlank(createTime)) {
//                LocalDateTime addTime = LocalDateTime.parse(createTime, DATE_FORMATTER);
//                if (createDateFrom == null || addTime.isBefore(createDateFrom)) {
//                    createDateFrom = addTime.minusMinutes(1);
//                }
//            }
            String code = parentDatum.getOrDefault("receiving_code", "").toString();
            if (StringUtils.isNotBlank(code)) {
                referenceNoList.add(code);
            }
        }
        if (CollUtil.isEmpty(referenceNoList)) {
            // 来源数据异常找不到退货单号
            ServiceException.runError("来源数据异常找不到退货单号:" + dmpInputTaskEntity.getId());
        }
        if (null == createDateFrom) {
            // 来源数据异常找不到创建日期
            ServiceException.runError("来源数据异常找不到创建日期:" + dmpInputTaskEntity.getId());
        }

        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();

        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.GOODCANG.getCode()));
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            throw new ServiceException("谷仓授权信息不存在");
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(parentData.get(0).getOrDefault("nextLevelId", "").toString()))
                .findFirst()
                .orElse(null);
        if(null == overseasProviderEntity) {
            throw new ServiceException("谷仓对应授权ID信息不存在");
        }
        String authId = overseasProviderEntity.getId();
        ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());

        // createDateFrom和createDateEnd按一个月期间分组
        List<Pair<LocalDateTime, LocalDateTime>> pairsDateTimeList = splitDateRangeByMonth(createDateFrom, createDateEnd);

        List<JSONObject> allResultList = new ArrayList<>();
        for (Pair<LocalDateTime, LocalDateTime> dateTimePair : pairsDateTimeList) {
            int batchSize = 200;
            GoodCangInventoryRequestDTO requestDTO = new GoodCangInventoryRequestDTO();
            requestDTO.setCreate_date_from(dateTimePair.getKey().format(DATE_FORMATTER));
            requestDTO.setCreate_date_end(dateTimePair.getValue().format(DATE_FORMATTER));
            requestDTO.setApplication_code(7);
            requestDTO.setPageSize(batchSize);
            List<JSONObject> allResult = requestFLowByNoList(referenceNoList, batchSize, requestDTO, apiType, authId);
            allResultList.addAll(allResult);
        }

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResultList));
        return Collections.singletonList(dmpInputTaskInitDTO);
    }
}
