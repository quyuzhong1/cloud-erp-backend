package com.erp.server.file.business.plm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.FileTaskEventEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.RefProductImgAttachmentDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.FileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_IMAGES;

/**
 * 批量下载图片Handler
 * 调用PLM服务构建文件夹结构并创建ZIP
 */
@Component
@Slf4j
public class ExportPlmProductImagesHandler implements FileEventHandler {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT_IMAGES;
    }

    @Override
    public void handle(FileTask fileTask) {
        log.info("开始处理批量下载图片任务，taskId={}", fileTask.getId());
        
        try {
            // 1. 读取任务参数
            RefProductImgAttachmentDTO.BatchDownloadDTO dto = readValue(fileTask.getMetaInfo(), 
                    new TypeReference<RefProductImgAttachmentDTO.BatchDownloadDTO>() {});
            
            if (CollUtil.isEmpty(dto.getIds())) {
                throw new ServiceException("图片ID列表不能为空");
            }
            
            // 2. 调用PLM服务构建文件夹结构并创建ZIP
            String zipUrl = exportPlmFeign.buildProductImagesFolderStructure(dto);
            
            if (StrUtil.isBlank(zipUrl)) {
                throw new ServiceException("创建ZIP文件失败");
            }
            
            // 3. 更新任务结果
            fileTask.setFileUrl(zipUrl);
            // 文件数量设置为请求的图片数量
            fileTask.setCount(dto.getIds().size());
            
            log.info("批量下载图片任务处理完成，taskId={}, zipUrl={}, count={}", 
                    fileTask.getId(), zipUrl, fileTask.getCount());
        } catch (Exception e) {
            log.error("批量下载图片任务处理失败，taskId={}", fileTask.getId(), e);
            throw new ServiceException("批量下载图片失败: " + e.getMessage());
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

