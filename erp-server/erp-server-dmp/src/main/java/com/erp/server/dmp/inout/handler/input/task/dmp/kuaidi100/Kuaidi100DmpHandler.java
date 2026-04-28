package com.erp.server.dmp.inout.handler.input.task.dmp.kuaidi100;

import cn.hutool.core.util.ObjectUtil;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import io.seata.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 功能描述：快递100原始数据转换为 DMP 标准实体处理器
 *
 * @author jack
 * @date 2026-03-31
 */
@Service
@Scope("prototype")
public class Kuaidi100DmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private Kuaidi100Service kuaidi100Service;

    @Override
    protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {
        Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();

        for (Map<String, Object> dmpInputMongoBaseEntity : dmpInputMongoEntityList) {
            ArrayList<TreeMap<String, Object>> valueList = new ArrayList<>();

            // 快递100返回的 nu 为单号，state 为总状态，data 为轨迹明细
            String trackNo = String.valueOf(dmpInputMongoBaseEntity.get("nu"));
            String overallStatus = kuaidi100Service.convertTrackStatus(String.valueOf(dmpInputMongoBaseEntity.get("state")));

            Object dataObj = dmpInputMongoBaseEntity.get("data");
            if (ObjectUtil.isNotEmpty(dataObj) && dataObj instanceof List) {
                List<Map<String, Object>> trackDetails = (List<Map<String, Object>>) dataObj;

                for (Map<String, Object> detail : trackDetails) {
                    TreeMap<String, Object> dmpInputDmpBaseEntity = new TreeMap<>();
                    this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);

                    dmpInputDmpBaseEntity.put("trackNo", trackNo);
                    dmpInputDmpBaseEntity.put("trackTime", detail.get("time"));
                    dmpInputDmpBaseEntity.put("content", detail.get("context"));
                    // 对于明细轨迹，快递100建议根据 state 判断总状态，明细状态可辅助参考
                    dmpInputDmpBaseEntity.put("status", overallStatus);
                    dmpInputDmpBaseEntity.put("orderStatus", overallStatus);
                    dmpInputDmpBaseEntity.put("address", detail.get("areaName"));

                    valueList.add(dmpInputDmpBaseEntity);
                }
            } else {
                // 如果没有明细轨迹（如刚下单尚未有轨迹），创建一个基础记录
                TreeMap<String, Object> dmpInputDmpBaseEntity = new TreeMap<>();
                this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);
                dmpInputDmpBaseEntity.put("trackNo", trackNo);
                dmpInputDmpBaseEntity.put("orderStatus", LogisticTrackStatusEnum.NOT_FIND.getCode());
                dmpInputDmpBaseEntity.put("status", LogisticTrackStatusEnum.NOT_FIND.getCode());
                valueList.add(dmpInputDmpBaseEntity);
            }

            ArrayList<Map<String, Object>> keyList = new ArrayList<>();
            keyList.add(dmpInputMongoBaseEntity);
            dmpInputDataDmpRelationMaps.put(keyList, valueList);
        }

        this.afterConvertData(dmpInputDataDmpRelationMaps);
        return dmpInputDataDmpRelationMaps;
    }


}
