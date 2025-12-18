package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_B2B_THIRD_DELIVERY_REPORT;

/**
 * B2B三方发货单导出
 * @date 2025-11-28
 * @author zdy
 */
@Component
@Slf4j
public class ExportWmsB2bThirdDeliveryHandler extends AbstractPageFileEventHandler<B2bThirdDeliveryDTO.PagingViewDTO, B2bThirdDeliveryDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<B2bThirdDeliveryDTO.PagingViewDTO> getPageData(PagingDTO<B2bThirdDeliveryDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportB2bThirdDelivery(dto);
    }

    @Override
    protected List<B2bThirdDeliveryDTO.PagingViewDTO> getData(FileTask fileTask) {
        B2bThirdDeliveryDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<B2bThirdDeliveryDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/b2bThirdDeliveryExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_B2B_THIRD_DELIVERY_REPORT;
    }
}

