package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_TRANSFER_APPLICATION;

@Component
@Slf4j
public class ExportWmsTransferApplicationHandler extends AbstractPageFileEventHandler<TransferApplicationDTO.ListDTO, TransferApplicationDTO.SearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/transferApplication.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_TRANSFER_APPLICATION;
    }

    @Override
    protected List<TransferApplicationDTO.ListDTO> getData(FileTask fileTask) {
        TransferApplicationDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TransferApplicationDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<TransferApplicationDTO.ListDTO> getPageData(PagingDTO<TransferApplicationDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportTransferApplication(dto);
    }
}
