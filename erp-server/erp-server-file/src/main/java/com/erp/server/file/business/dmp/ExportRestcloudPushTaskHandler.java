package com.erp.server.file.business.dmp;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExportRestcloudPushTaskHandler extends AbstractPageFileEventHandler<DmpOutputTaskRecordDTO.PagingDTO, DmpOutputTaskRecordDTO.ExpotParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected List<DmpOutputTaskRecordDTO.PagingDTO> getData(FileTask fileTask) {
        DmpOutputTaskRecordDTO.ExpotParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpOutputTaskRecordDTO.ExpotParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DmpOutputTaskRecordDTO.PagingDTO> getPageData(PagingDTO<DmpOutputTaskRecordDTO.ExpotParamDTO> dto) {
        return exportDmpFeign.exportRestcloudPushTask(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_RESTCLOUD_PUSH_TASK;
    }

    @Override
    public String getExcelPath() {
        return "excel/dmp/newDmpPushTask.xlsx";
    }
}
