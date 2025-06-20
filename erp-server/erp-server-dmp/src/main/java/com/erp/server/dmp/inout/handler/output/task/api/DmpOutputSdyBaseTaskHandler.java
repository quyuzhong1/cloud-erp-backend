package com.erp.server.dmp.inout.handler.output.task.api;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Scope("prototype")
@Slf4j
public abstract class DmpOutputSdyBaseTaskHandler extends DmpOutputTaskHandler {

	@Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
	
	@Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<String, String> map = this.getPushJsonDataMap(dmpRequest, dmpResponse);
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
        if(!map.isEmpty()) {
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
                if(CollUtil.isNotEmpty(sourceCodeKeys)) {
    				dmpOutputTaskRecordEntity.setSourceCode(sourceCodeKeys.stream().map(s -> {
    					String string = parseObject.getString(s);
    					if(string == null) {
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
        String status = "";
        String requestData = dmpOutputTaskRecordEntity.getRequestData();
        ApiResult handle = sdyDeliveryOrderConsumer.handle(requestData);
        if (200 == handle.getCode()) {
            status = DmpOutputTaskRecordStatusEnum.FINISH.getCode();
        } else {
            status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
        }

        dmpOutputUtils.updateStatus(id, status, String.valueOf(handle.getData()), handle.getMsg());
    }
	
	@Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("biz_no" , "msku_code");
    }
}
