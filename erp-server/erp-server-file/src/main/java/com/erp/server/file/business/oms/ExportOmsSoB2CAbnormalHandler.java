package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_ABNORMAL;

@Component
@Slf4j
public class ExportOmsSoB2CAbnormalHandler extends AbstractPageFileEventHandler<SoB2cAbnormalDTO.ListDTO, SoB2cAbnormalDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SoB2cAbnormalDTO.ListDTO> getData(FileTask fileTask) {
        SoB2cAbnormalDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoB2cAbnormalDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoB2cAbnormalDTO.ListDTO> getPageData(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportSoB2CAbnormal(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO_B2C_ABNORMAL;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/soB2cAbnormal.xlsx";
    }
}
