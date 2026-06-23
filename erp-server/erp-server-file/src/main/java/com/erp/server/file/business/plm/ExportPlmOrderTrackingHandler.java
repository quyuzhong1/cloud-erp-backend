package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ORDER_TRACKING;

@Component
public class ExportPlmOrderTrackingHandler extends AbstractPageFileEventHandler<MouldInfoDTO.OrderTrackingExportDTO, MouldInfoDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected List<MouldInfoDTO.OrderTrackingExportDTO> getData(FileTask fileTask) {
        MouldInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<MouldInfoDTO.PagingParamDTO>() {
        });
        List<String> mouldIdList = new ArrayList<>();
        List<MouldInfoDTO.OrderTrackingExportDTO> dtos = listSeqData(dto);
        for (MouldInfoDTO.OrderTrackingExportDTO exportDTO : dtos) {
            if (!ObjectUtils.isEmpty(exportDTO.getDetailId()) && mouldIdList.contains(exportDTO.getDetailId())) {
                exportDTO.setProjectNo("");
                exportDTO.setName("");
                exportDTO.setMouldNo("");
                exportDTO.setThirdMouldNo("");
                exportDTO.setStatusName("");
                exportDTO.setSupplierName("");
                exportDTO.setQty(null);
                exportDTO.setTaxPrice(null);
                exportDTO.setTaxRate(null);
                exportDTO.setPayMethodName("");
                exportDTO.setPaymentConditionName("");
                exportDTO.setIsNeedRefundName("");
                exportDTO.setRefundStandardName("");
                exportDTO.setRefundOrderQty(null);
                exportDTO.setRefundAmount(null);
                exportDTO.setRefundStatusName("");
                exportDTO.setPurchaseQty(null);
                exportDTO.setReceiveQty(null);
                exportDTO.setStockInQty(null);
                exportDTO.setDiffQty(null);
                exportDTO.setUpdateTime(null);
            }
            mouldIdList.add(exportDTO.getDetailId());
        }
        return dtos;
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/orderTracking.xlsx";
    }

    @Override
    protected PagingVO<MouldInfoDTO.OrderTrackingExportDTO> getPageData(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportOrderTracking(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_ORDER_TRACKING;
    }
}
