package com.erp.server.file.business.srm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.SalesSharingDTO;
import com.erp.rpc.srm.feign.ExportSrmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_SALES_SHARING_REPORT;

@Component
@Slf4j
public class ExportSrmSalesSharingHandler extends AbstractPageFileEventHandler<SalesSharingDTO.ListDTO, SalesSharingDTO.PagingParamDTO> {
    @Resource
    private ExportSrmFeign exportSrmFeign;

    @Override
    public String getExcelPath() {
        return "excel/srm/salesSharingExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SRM_SALES_SHARING_REPORT;
    }

    @Override
    protected List<SalesSharingDTO.ListDTO> getData(FileTask fileTask) {
        SalesSharingDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SalesSharingDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SalesSharingDTO.ListDTO> getPageData(PagingDTO<SalesSharingDTO.PagingParamDTO> dto) {
        return exportSrmFeign.exportSalesSharing(dto);
    }
}
