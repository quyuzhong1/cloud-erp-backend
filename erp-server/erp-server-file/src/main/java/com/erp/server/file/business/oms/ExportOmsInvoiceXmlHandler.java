package com.erp.server.file.business.oms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.FileTaskEventEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.model.oms.enums.AttachmentTypeEnum;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.FileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_XML;

/**
 * 导出发票XML Handler
 * 调用OMS服务构建ZIP文件
 */
@Component
@Slf4j
public class ExportOmsInvoiceXmlHandler implements FileEventHandler {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_INVOICE_XML;
    }

    @Override
    public void handle(FileTask fileTask) {
        log.info("开始处理导出发票XML任务，taskId={}", fileTask.getId());
        
        try {
            // 1. 读取任务参数
            InvoiceInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), 
                    new TypeReference<InvoiceInfoDTO.PagingParamDTO>() {});
            
            // 2. 调用OMS服务构建ZIP文件
            String zipUrl = exportOmsFeign.buildInvoiceAttachZip(dto, AttachmentTypeEnum.INVOICE_INFO_XML.getCode());
            
            if (StrUtil.isBlank(zipUrl)) {
                throw new ServiceException("创建ZIP文件失败");
            }
            
            // 3. 查询附件数量用于设置count
            int count = CollUtil.size(exportOmsFeign.listExportUrl(dto, AttachmentTypeEnum.INVOICE_INFO_XML.getCode()));
            
            // 4. 更新任务结果
            fileTask.setFileUrl(zipUrl);
            fileTask.setCount(count);
            
            log.info("导出发票XML任务处理完成，taskId={}, zipUrl={}, count={}", 
                    fileTask.getId(), zipUrl, fileTask.getCount());
        } catch (Exception e) {
            log.error("导出发票XML任务处理失败，taskId={}", fileTask.getId(), e);
            throw new ServiceException("导出发票XML失败: " + e.getMessage());
        }
    }
    
    private <P> P readValue(String params, TypeReference<P> type) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(params, type);
        } catch (Exception e) {
            throw new ServiceException("解析任务参数失败: " + e.getMessage());
        }
    }
}
