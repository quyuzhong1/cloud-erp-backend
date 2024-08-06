package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.ALIEXPRESS_DELIVERY_EXPORT;

@Component
@Slf4j
public class AliexpressDeliveryExportHandler extends AbstractPageFileEventHandler<AliexpressDeliveryDTO.ListDTO, AliexpressDeliveryDTO.SearchParamDTO> {


    @Override
    public String getExcelPath() {
        return "excel/aliexpressDeliveryExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return ALIEXPRESS_DELIVERY_EXPORT;
    }

    @Override
    protected List<AliexpressDeliveryDTO.ListDTO> getData(FileTask fileTask) {
        return null;
    }


    @Override
    protected PagingVO<AliexpressDeliveryDTO.ListDTO> getPageData(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        return null;
    }
}
