package com.erp.server.file.business.plm;

import com.common.business.enums.FileTaskEventEnum;
import com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler;
import com.erp.server.file.dto.DynamicExcelDTO;
import com.erp.server.file.entity.FileTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductDetailExportHandler extends AbstractDynamicHeadersFileEventHandler {


    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_PLM_PRODUCT_DETAIL;
    }


    @Override
    protected DynamicExcelDTO getData(FileTask fileTask) {
        return null;
    }
}
