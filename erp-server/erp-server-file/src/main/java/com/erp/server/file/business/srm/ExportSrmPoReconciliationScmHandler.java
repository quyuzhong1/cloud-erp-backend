//package com.erp.server.file.business.srm;
//
//import com.common.business.dto.base.PagingDTO;
//import com.common.business.enums.FileTaskEventEnum;
//import com.common.business.vo.PagingVO;
//import com.erp.model.srm.dto.PoReconciliationDTO;
//import com.erp.model.sys.dto.DictCityDTO;
//import com.erp.rpc.sys.feign.ExportSysFeign;
//import com.erp.server.file.core.AbstractDetailPageFileEventHandler;
//import com.erp.server.file.entity.FileTask;
//import com.fasterxml.jackson.core.type.TypeReference;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.List;
//
//import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_PO_RECONCILIATION_SCM;
//
//@Component
//public class ExportSrmPoReconciliationScmHandler extends AbstractDetailPageFileEventHandler<DictCityDTO.PagingViewDTO, PoReconciliationDTO.ExportDTO, DictCityDTO.ProvincePagingParamDTO> {
//    @Resource
//    private ExportSysFeign exportSysFeign;
//    @Override
//    protected List<DictCityDTO.PagingViewDTO> getData(FileTask fileTask) {
//        DictCityDTO.ProvincePagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DictCityDTO.ProvincePagingParamDTO>() {
//        });
//        return listSeqData(dto);
//    }
//
//    @Override
//    protected String getExcelPath() {
//        return "excel/srm/exportPoReconciliation.xlsx";
//    }
//
//    @Override
//    protected PagingVO<DictCityDTO.PagingViewDTO> getPageData(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
//        return exportSysFeign.exportCity(dto);
//    }
//
//    @Override
//    public FileTaskEventEnum getEvent() {
//        return EXPORT_SRM_PO_RECONCILIATION_SCM;
//    }
//
//
//    @Override
//    protected PoReconciliationDTO.ExportDTO getMainData(FileTask fileTask) {
//        return null;
//    }
//}
