package com.erp.server.file.business.srm;

//@Component
//public class ExportSrmPoReconciliationHandler extends AbstractDetailPageFileEventHandler<PoReconciliationDTO.ListDTO,PoReconciliationDTO.ExportDTO, PoReconciliationDTO.PagingParamDTO> {
//    @Resource
//    private ExportSysFeign exportSysFeign;
//    @Override
//    protected List<PoReconciliationDTO.ListDTO> getData(FileTask fileTask) {
//        PoReconciliationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PoReconciliationDTO.PagingParamDTO>() {
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
//    protected PagingVO<PoReconciliationDTO.ListDTO> getPageData(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto) {
//        return exportSysFeign.exportCity(dto);
//    }
//
//    @Override
//    public FileTaskEventEnum getEvent() {
//        return EXPORT_SRM_PO_RECONCILIATION;
//    }
//}
