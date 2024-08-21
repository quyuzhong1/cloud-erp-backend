package com.erp.server.file.business.wms;

//@Component
//@Slf4j
//public class ExportWmsInventoryAgeHandler extends AbstractDynamicHeadersFileEventHandler<InventoryDTO.PagingViewDTO, InventoryReportDTO.ExportInventoryAgeSearchParamDTO> {
//
//    @Resource
//    private ExportWmsFeign exportWmsFeign;
//
//    @Override
//    protected List<InventoryDTO.PagingViewDTO> getData(FileTask fileTask) {
//        InventoryReportDTO.ExportInventoryAgeSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<InventoryReportDTO.ExportInventoryAgeSearchParamDTO>() {
//        });
//        return listSeqData(dto);
//    }
//
//
//    @Override
//    protected PagingVO<InventoryDTO.PagingViewDTO> getPageData(PagingDTO<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> dto) {
//        return exportWmsFeign.exportInventoryAge(dto);
//    }
//
//    @Override
//    public FileTaskEventEnum getEvent() {
//        return EXPORT_WMS_INVENTORY_AGE;
//    }
//
//}
