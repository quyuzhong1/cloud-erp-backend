package com.common.business.handler;

import com.common.business.annotation.SaveData;
import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.google.common.collect.Maps;
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
            throw new ServiceException("清除数据失败，未找到对应的apiEnum tableName = " + tableName);
        }
        //通过枚举获取对应service
        IReportSaveService service = PAY_MAP.get(apiEnum);
        if(null != service){
            //清洗数据
            service.cleanDataSave(tableName, size);
        }

    }

}
