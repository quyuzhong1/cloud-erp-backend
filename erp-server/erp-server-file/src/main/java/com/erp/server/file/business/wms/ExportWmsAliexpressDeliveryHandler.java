package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_ALIEXPRESS_DELIVERY_EXPORT;

@Component
@Slf4j
public class ExportWmsAliexpressDeliveryHandler extends AbstractPageFileEventHandler<AliexpressDeliveryDTO.ListDTO, AliexpressDeliveryDTO.SearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/aliexpressDeliveryExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_ALIEXPRESS_DELIVERY_EXPORT;
    }

    @Override
    protected List<AliexpressDeliveryDTO.ListDTO> getData(FileTask fileTask) {
        AliexpressDeliveryDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AliexpressDeliveryDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<AliexpressDeliveryDTO.ListDTO> getPageData(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportAliexpressDelivery(dto);
    }
}
