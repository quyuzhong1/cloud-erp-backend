package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.wangdian.WdtOtherInventoryStockConsumer;
import com.erp.server.dmp.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
@Slf4j
public abstract class DmpOutputWdtBaseTaskHandler extends DmpOutputTaskHandler {

    @Resource
    private WdtOtherInventoryStockConsumer wdtOtherInventoryStockConsumer;
    @Resource
    private DictBasicService dictBasicService;

    @Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<String, String> map = this.getPushJsonDataMap(dmpRequest, dmpResponse);
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
        if (!map.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            int i = 0;
            List<String> sourceCodeKeys = this.getSourceCodeKeys();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String dataId = entry.getKey();
                String value = entry.getValue();
                DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
                String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
                dmpOutputTaskRecordEntity.setId(id);
                dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
                dmpOutputTaskRecordEntity.setDataId(dataId);
                JSONObject parseObject = JSON.parseObject(value);
                if (CollUtil.isNotEmpty(sourceCodeKeys)) {
                    dmpOutputTaskRecordEntity.setSourceCode(sourceCodeKeys.stream().map(s -> {
                        String string = parseObject.getString(s);
                        if (string == null) {
                            string = "";
                        }
                        return string;
                    }).collect(Collectors.joining("_")));
                }
                dmpOutputTaskRecordEntity.setRequestData(value);
                dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
                LocalDateTime insertTime = now.plus(i, ChronoUnit.MILLIS);
                dmpOutputTaskRecordEntity.setCreateTime(insertTime);
                dmpOutputTaskRecordEntity.setUpdateTime(insertTime);
                dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
                i = i + 1;
            }
        }

        return dmpOutputTaskRecordEntityList;
    }

    @Override
    protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity, DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
        String id = dmpOutputTaskRecordEntity.getId();
        String status = DmpOutputTaskRecordStatusEnum.FINISH.getCode();
        String requestData = dmpOutputTaskRecordEntity.getRequestData();
        String responseData = "";
        String msg = "";
        try {
            ApiResult handle = wdtOtherInventoryStockConsumer.handle(requestData);
            responseData = JSON.toJSONString(handle.getData());
            msg = handle.getMsg();
        }catch (Exception e){
            log.error("DmpOutputWdtBaseTaskHandler pushData error, id: {}, requestData: {}", id, requestData, e);
            status = DmpOutputTaskRecordStatusEnum.ERROR.getCode();
            List<DictBasicEntity> viewDTOList = dictBasicService.getByKey("wdtUpdateInventoryUser");
            String atUser = "";
            if (CollUtil.isNotEmpty(viewDTOList)){
                atUser = viewDTOList.stream().map(v -> CharSequenceUtil.format("<at user_id=\"{}\"></at>", v.getValue())).collect(Collectors.joining());
            }
            msg = atUser + "【库存差异同步处理】 " + e.getMessage();
        }
        dmpOutputUtils.updateStatus(id, status, CharSequenceUtil.isBlank(responseData) ? msg : responseData, msg);
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("biz_no", "msku_code");
    }
}
