package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ZipTaskResultDTO;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailImprotTypeEnum;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ImageProcessService;
import com.erp.server.plm.service.PlmAttachmentService;
import com.erp.server.plm.service.ProductDetailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ImageProcessServiceImpl implements ImageProcessService {

    @Resource
    private PlmAttachmentService plmAttachmentService;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private ProductDetailService productDetailService;


    @Resource
    private CommonService commonService;

    private static final String SKUTABLE ="product_detail";

    /**
     * 处理图片上传和压缩
     * @author jack
     * @date 2025-07-26
     * @param file                上传的文件，类型为MultipartFile
     * @param fileName            文件名，用于标识和匹配旧附件
     * @param productDetailEntity 产品明细实体，包含产品相关信息
     * @param size                图片压缩的目标尺寸（宽度或高度）
     * @param result              任务结果DTO，用于记录处理成功、失败及总数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processImage(MultipartFile file, String fileName, ProductDetailEntity productDetailEntity, Long size, ZipTaskResultDTO result,String importType) {
        boolean success = false;
        int retry = 0;

        // 最多尝试3次处理图片上传与压缩逻辑
        while (!success && retry < 3) {
            try {
                // 压缩图片并获取新的MultipartFile对象
//                MultipartFile newMultipartFile = commonService.compressImage(file, size);

                // 上传压缩后的文件，并获取访问URL
                String url = fileFeign.uploadFile(file);

                // 计算原始文件大小（单位MB，保留四位小数）
                double fileSize = (double) Math.round((file.getSize() / (1024.0 * 1024.0)) * 10000) / 10000;

                // 构造并保存新的附件记录
                PlmAttachmentEntity attachment = new PlmAttachmentEntity();
                attachment.setBusinessId(productDetailEntity.getId());
                attachment.setAttachUrl(url);
                attachment.setAttachName(file.getOriginalFilename());
                attachment.setType(SKUTABLE);
                attachment.setAttachSize(BigDecimal.valueOf(fileSize));
                plmAttachmentService.save(attachment);

                // 更新任务结果：成功数加一
                result.incrementSuccess(productDetailEntity.getId(),url);
                success = true;
            } catch (Exception e) {
                retry++;
                // 达到最大重试次数时，标记该文件处理失败
                if (retry >= 3) {
                    result.incrementFailed(productDetailEntity.getSkuNo(),file.getOriginalFilename());
                }
            }
        }
    }



    /**
     * 图片处理
     * @author jack
     * @date 2025-07-26
     */
    @Override
    public List<MultipartFile> processZip(String zipUrl) throws IOException {
        // 下载ZIP文件字节数据
        byte[] zipBytes = fileFeign.downloadFile(zipUrl);
        if (Objects.isNull(zipBytes) || zipBytes.length == 0) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND,zipUrl);
        }
        List<MultipartFile> result = new ArrayList<>();

        // 解压ZIP文件，提取其中的图片文件
        try (ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(new ByteArrayInputStream(zipBytes)), Charset.forName("GBK"))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String fileName = new File(entry.getName()).getName();
                String ext = getExtension(fileName);
                if (!isImage(ext)) continue;

                // 读取图片文件内容到字节数组
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zipStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] imageBytes = baos.toByteArray();
                MultipartFile multipartFile = byteToMultipartFile(imageBytes, fileName);
                result.add(multipartFile);
            }
        }
        return result;
    }


    private MultipartFile byteToMultipartFile(byte[] fileBytes, String fileName) {
        MultipartFile multipartFile = new MockMultipartFile("multipartFile", fileName,
                getContentType(fileName), fileBytes);
        return multipartFile;
    }

    private String getContentType(String fileName) {
        String ext = getExtension(fileName).toLowerCase();
        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }

    private boolean isImage(String ext) {
        return Arrays.asList("jpg","jpeg","png","gif","bmp").contains(ext.toLowerCase());
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }
}
