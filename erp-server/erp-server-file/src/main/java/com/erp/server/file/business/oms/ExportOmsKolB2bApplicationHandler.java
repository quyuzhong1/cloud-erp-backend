package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolB2bApplicationDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_B2B_APPLICATION_REPORT;

/**
 * 样品借用导出
 * @date 2025-08-21
 * @author jack
 */
@Component
@Slf4j
public class ExportOmsKolB2bApplicationHandler extends AbstractPageFileEventHandler<KolB2bApplicationDTO.ListDTO, KolB2bApplicationDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<KolB2bApplicationDTO.ListDTO> getPageData(PagingDTO<KolB2bApplicationDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportKolB2bApplication(dto);
    }

    @Override
    protected List<KolB2bApplicationDTO.ListDTO> getData(FileTask fileTask) {
        KolB2bApplicationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<KolB2bApplicationDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/kolB2bApplicationExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_KOL_B2B_APPLICATION_REPORT;
    }
}
