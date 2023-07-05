package com.erp.server.dmp.config;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.server.dmp.bean.AbstractSparrowAnnotationBeanMap;

import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.google.common.collect.Maps;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class SaveHandler extends AbstractSparrowAnnotationBeanMap<SaveData, IReportSaveService> {
    private static final Map<PlatformApiEnum, IReportSaveService> PAY_MAP = Maps.newHashMap();

    private static final int size = 1000;
    @Override
    public Class<SaveData> getAnnotation() {
        return SaveData.class;
    }

    @Override
    public void refresh(Map<SaveData, IReportSaveService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    public static void pullDataSave(RequestDTO dto) throws Exception {
        //通过枚举获取对应service
        IReportSaveService service = PAY_MAP.get(dto.getPlatformApiEnum());

        //拉取数据 存库
        service.pullDataSave(dto);
    }

    public static void cleanDataSave(String tableName) {
        PlatformApiEnum apiEnum = PlatformApiEnum.getByMongoTable(tableName);
        if(null ==apiEnum){
            XxlJobHelper.log("清除数据失败，未找到对应的apiEnum tableName = " + tableName);
            throw new ServiceException("清除数据失败，未找到对应的apiEnum tableName = " + tableName);
        }
        //通过枚举获取对应service
        IReportSaveService service = PAY_MAP.get(apiEnum);
        //清除数据
        service.cleanDataSave(tableName, size);
    }

}
