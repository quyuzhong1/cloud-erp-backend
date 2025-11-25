package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.CfgMoldReturnAlertRuleDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_CFG_MOLD_RETURN;

/**
 * 模具返还策略导出
 *
 * @author jack
 * @date 2025-10-16
 */
@Component
@Slf4j
public class ExportPlmCfgMoldReturnHandler extends AbstractPageFileEventHandler<CfgMoldReturnAlertRuleDTO.ListDTO, CfgMoldReturnAlertRuleDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<CfgMoldReturnAlertRuleDTO.ListDTO> getPageData(PagingDTO<CfgMoldReturnAlertRuleDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportCfgMoldReturn(dto);
    }

    @Override
    protected List<CfgMoldReturnAlertRuleDTO.ListDTO> getData(FileTask fileTask) {
        CfgMoldReturnAlertRuleDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgMoldReturnAlertRuleDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/cfgMoldReturn.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_CFG_MOLD_RETURN;
    }
}
