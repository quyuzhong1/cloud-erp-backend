package com.erp.server.dmp.config;

import com.erp.server.dmp.bean.AbstractSparrowAnnotationBeanMap;
import com.erp.server.dmp.entity.dto.RequestDTO;
import com.erp.server.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class SaveHandler extends AbstractSparrowAnnotationBeanMap<SaveData, IReportSaveService> {
    private static final Map<PlatformApiEnum, IReportSaveService> PAY_MAP = Maps.newHashMap();
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

}
