package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolSocialMediaDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_SOCIAL_MEDIA;

/**
 * 达人社媒数据表导出
 * @date 2025-12-04
 * @author wuhaotian
 */
@Component
@Slf4j
public class ExportOmsKolSocialMediaHandler extends AbstractPageFileEventHandler<KolSocialMediaDTO.ListDTO, KolSocialMediaDTO.ParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<KolSocialMediaDTO.ListDTO> getPageData(PagingDTO<KolSocialMediaDTO.ParamDTO> dto) {
        return exportOmsFeign.exportKolSocialMedia(dto);
    }

    @Override
    protected List<KolSocialMediaDTO.ListDTO> getData(FileTask fileTask) {
        KolSocialMediaDTO.ParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<KolSocialMediaDTO.ParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/kolSocialMediaExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_KOL_SOCIAL_MEDIA;
    }
}

