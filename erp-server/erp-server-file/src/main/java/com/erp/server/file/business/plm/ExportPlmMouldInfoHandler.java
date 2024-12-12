package com.erp.server.file.business.plm;

import com.alibaba.excel.write.handler.WriteHandler;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.business.plm.hanlder.MouldInfoWriteHandler;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOULD_INFO;

@Component
public class ExportPlmMouldInfoHandler extends AbstractPageFileEventHandler<MouldInfoDTO.MouldInfoExportDTO, MouldInfoDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected List<MouldInfoDTO.MouldInfoExportDTO> getData(FileTask fileTask) {
        MouldInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<MouldInfoDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/mouldInfo.xlsx";
    }

    @Override
    protected PagingVO<MouldInfoDTO.MouldInfoExportDTO> getPageData(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportMouldInfo(dto);
    }

    @Override
    public List<WriteHandler> getWriteHandler() {
        return Collections.singletonList(new MouldInfoWriteHandler());
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_MOULD_INFO;
    }
}
