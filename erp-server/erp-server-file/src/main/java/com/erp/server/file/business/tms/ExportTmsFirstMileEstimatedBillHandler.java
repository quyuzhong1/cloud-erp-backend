package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FM_ESTIMATED_BILL;

/**
 * 头程暂估账单导出处理
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportTmsFirstMileEstimatedBillHandler extends AbstractPageFileEventHandler<FirstMileEstimatedBillDTO.View, FirstMileEstimatedBillDTO.PagingParam> {

    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    protected PagingVO<FirstMileEstimatedBillDTO.View> getPageData(PagingDTO<FirstMileEstimatedBillDTO.PagingParam> dto) {
        return exportTmsFeign.exportFirstMileEstimatedBill(dto);
    }

    @Override
    protected List<FirstMileEstimatedBillDTO.View> getData(FileTask fileTask) {
        FirstMileEstimatedBillDTO.PagingParam dto = readValue(fileTask.getMetaInfo(), new TypeReference<FirstMileEstimatedBillDTO.PagingParam>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/tms/firstMileEstimatedBillExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_FM_ESTIMATED_BILL;
    }
}
