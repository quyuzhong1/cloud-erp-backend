package com.erp.server.file.business.oms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
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

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_CUSTOMER;

@Component
@Slf4j
public class ExportOmsCustomerHandler extends AbstractPageFileEventHandler<CustomerDTO.PagingExportDTO, CustomerDTO.ExportDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<CustomerDTO.PagingExportDTO> getData(FileTask fileTask) {
        CustomerDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CustomerDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<CustomerDTO.PagingExportDTO> getPageData(PagingDTO<CustomerDTO.ExportDTO> dto) {
        return exportOmsFeign.exportCustomer(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_CUSTOMER;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/CustomerExport.xlsx";
    }

    @Override
    public void handle(FileTask fileTask) {
        List<CustomerDTO.PagingExportDTO> list = getData(fileTask);
        fileTask.setCount(list.size());
        StringBuilder sb = new StringBuilder();
        String excelPath = getExcelPath();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        //联系人信息
        //地址信息
        List<CustomerDTO.PagingAddressContactExportDTO> pagingAddressContactDTOS = new ArrayList<>();
        //发票信息
        List<InvoiceDTO.ViewDTO> invoiceList = new ArrayList<>();
        for (CustomerDTO.PagingExportDTO pagingExportDTO : list) {
            if (CollUtil.isNotEmpty(pagingExportDTO.getAddressContactList())) {
                pagingAddressContactDTOS.addAll(pagingExportDTO.getAddressContactList());
            }
            if (CollUtil.isNotEmpty(pagingExportDTO.getInvoiceList())) {
                invoiceList.addAll(pagingExportDTO.getInvoiceList());
            }
        }
        Pair customerInfo = new Pair(0, list);
        pairList.add(customerInfo);
        Pair addressContact = new Pair(1, pagingAddressContactDTOS);
        pairList.add(addressContact);
        Pair invoice = new Pair(2, invoiceList);
        pairList.add(invoice);
        try {
            byte[] bytes = new ExcelPrintUtils().sheetPatchExport(pairList, sb.toString(),excelPath);
            String s = FastDFSClientUtil.uploadFile(bytes, sb.toString() + ".xlsx", null);
            fileTask.setFileUrl(s);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
}
