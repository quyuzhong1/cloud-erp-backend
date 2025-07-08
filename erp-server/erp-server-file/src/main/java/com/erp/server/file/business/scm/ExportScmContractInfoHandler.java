package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ContractInfoDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_CONTRACT_INFO_REPORT;

@Component
@Slf4j
public class ExportScmContractInfoHandler extends AbstractPageFileEventHandler<ContractInfoDTO.ListDTO, ContractInfoDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    public String getExcelPath() {
        return "excel/scm/contractInfoExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_CONTRACT_INFO_REPORT;
    }

    @Override
    protected List<ContractInfoDTO.ListDTO> getData(FileTask fileTask) {
        ContractInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ContractInfoDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ContractInfoDTO.ListDTO> getPageData(PagingDTO<ContractInfoDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportContractInfo(dto);
    }
}
