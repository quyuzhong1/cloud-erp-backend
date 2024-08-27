package com.erp.server.file.business.wms;

import cn.hutool.core.lang.Pair;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_INVENTORY;

@Component
@Slf4j
public class ExportWmsVirtualInventoryHandler extends AbstractPageFileEventHandler<VirtualInventoryDTO.ListDTO, VirtualInventoryDTO.SearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/virtualInventory.xlsx";
    }

    @Override
    public void handle(FileTask fileTask) {
        List<VirtualInventoryDTO.ListDTO> list = getData(fileTask);
        fileTask.setCount(list.size());
        //明细数据
        List<VirtualInventoryDTO.ListDetailDTO> detailList = list.stream().flatMap(obj -> Stream.of(obj.getDetailList().toArray(new VirtualInventoryDTO.ListDetailDTO[0]))).collect(Collectors.toList());
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(MathUtil.ZERO, list));
        pairList.add(new Pair<>(MathUtil.ONE, detailList));
        StringBuilder sb = new StringBuilder();
        String excelPath = getExcelPath();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
        try {
            byte[] bytes = new ExcelPrintUtils().sheetPatchExport(pairList, sb.toString(),excelPath);
            String s = FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
            fileTask.setFileUrl(s);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_INVENTORY;
    }

    @Override
    protected List<VirtualInventoryDTO.ListDTO> getData(FileTask fileTask) {
        VirtualInventoryDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualInventoryDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<VirtualInventoryDTO.ListDTO>  getPageData(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto) {
        return exportWmsFeign.getVirtualInventory(dto);
    }
}
