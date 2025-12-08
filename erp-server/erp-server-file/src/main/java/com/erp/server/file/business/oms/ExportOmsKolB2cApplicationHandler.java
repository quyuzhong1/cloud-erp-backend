package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolB2cApplicationDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_B2C_APPLICATION;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_PARTNER_INFO;

/**
 * B2C寄样申请导出
 * @date 2025-12-03
 * @author jack
 */
@Component
@Slf4j
public class ExportOmsKolB2cApplicationHandler extends AbstractPageFileEventHandler<KolB2cApplicationDTO.ListDTO, KolB2cApplicationDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<KolB2cApplicationDTO.ListDTO> getPageData(PagingDTO<KolB2cApplicationDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportOmsKolB2cApplication(dto);
    }

    @Override
    protected List<KolB2cApplicationDTO.ListDTO> getData(FileTask fileTask) {
        KolB2cApplicationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<KolB2cApplicationDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/kolB2cApplicationExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_KOL_B2C_APPLICATION;
    }
}
