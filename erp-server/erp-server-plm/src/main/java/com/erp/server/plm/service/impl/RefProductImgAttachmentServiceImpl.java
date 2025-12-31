package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.core.utils.ExcelUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.RefProductImgAttachmentEntity;
import com.erp.server.plm.mapper.RefProductImgAttachmentMapper;
import com.erp.server.plm.service.RefProductImgAttachmentService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.RefProductImgAttachmentDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.PlmAttachmentService;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.service.ProductImgCategoryService;
import com.erp.model.plm.entity.ProductImgCategoryEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.file.dto.FileDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.io.File;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;

/**
 * <p>
 * 图片分类附件关联表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
 */
@Slf4j
@Service
public class RefProductImgAttachmentServiceImpl extends SuperServiceImpl<RefProductImgAttachmentMapper, RefProductImgAttachmentEntity> implements RefProductImgAttachmentService {

    @Resource
    private FileFeign fileFeign;
    
    @Resource
    private PlmAttachmentService plmAttachmentService;
    
    @Resource
    private ProductDetailService productDetailService;
    
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    
    @Resource
    private ProductImgCategoryService productImgCategoryService;
    
    // 所有分类ID
    private static final String ALL_CATEGORY_ID = "1000000000000000001";
    // 产品主图分类ID
    private static final String PRODUCT_MAIN_IMAGE_CATEGORY_ID = "1000000000000000002";
    // 产品缩略图分类ID
    private static final String PRODUCT_THUMBNAIL_CATEGORY_ID = "1000000000000000003";
    // 缩略图压缩目标大小（KB），可根据需要调整
    private static final Long THUMBNAIL_TARGET_SIZE_KB = 200L;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RefProductImgAttachmentDTO.AddDTO addDTO) {
        RefProductImgAttachmentEntity refProductImgAttachmentEntity = new RefProductImgAttachmentEntity();
        BeanMapperUtils.copy(addDTO, refProductImgAttachmentEntity);

        // 数据处理
        handleData(refProductImgAttachmentEntity);

        log.info("开始新增图片分类附件关联单");
        boolean save = super.save(refProductImgAttachmentEntity);
        if(!save) {
            throw new ServiceException("图片分类附件关联单保存失败");
        }

        // 如果分类是产品主图，自动生成产品缩略图
        if (PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(refProductImgAttachmentEntity.getCategoryId())) {
            generateThumbnail(refProductImgAttachmentEntity);
        }


        return new BaseResultDTO.AddDTO(refProductImgAttachmentEntity.getId(), refProductImgAttachmentEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RefProductImgAttachmentDTO.UpdateDTO addOrUpdateDTO) {
        RefProductImgAttachmentEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "图片分类附件关联单"));
        RefProductImgAttachmentEntity refProductImgAttachmentEntity =  BeanMapperUtils.map(RefProductImgAttachmentEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(refProductImgAttachmentEntity);
        log.info("编辑 开始修改图片分类附件关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(refProductImgAttachmentEntity);
        if(!save) {
            throw new ServiceException("图片分类附件关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录图片分类附件关联单日志数据，id：【{}】", refProductImgAttachmentEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), refProductImgAttachmentEntity.getId(), "图片分类附件关联单");
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<RefProductImgAttachmentDTO.ListDTO> paging(PagingDTO<RefProductImgAttachmentDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<RefProductImgAttachmentDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<RefProductImgAttachmentDTO.TabListDTO> tabList(PermissionsDTO param) {
        RefProductImgAttachmentDTO.PagingParamDTO searchParam = new RefProductImgAttachmentDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<RefProductImgAttachmentDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(RefProductImgAttachmentDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new RefProductImgAttachmentDTO.TabListDTO(status, 0));
        }
        });
        list.add(new RefProductImgAttachmentDTO.TabListDTO("all", list.stream().mapToInt(RefProductImgAttachmentDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(RefProductImgAttachmentDTO.ExportDTO param, HttpServletResponse response) {
        List<RefProductImgAttachmentDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/refProductImgAttachment.xlsx";
        String name = "图片分类附件关联单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
//            throw new ServiceException(ApiError.ERROR_1015);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(RefProductImgAttachmentEntity refProductImgAttachmentEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public RefProductImgAttachmentDTO.ViewDTO view(String id) {
    RefProductImgAttachmentEntity refProductImgAttachmentEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到图片分类附件关联单数据"));
    RefProductImgAttachmentDTO.ViewDTO data = BeanMapperUtils.map(RefProductImgAttachmentDTO.ViewDTO.class, refProductImgAttachmentEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(RefProductImgAttachmentDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<RefProductImgAttachmentDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(RefProductImgAttachmentDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }

    /**
     * 生成产品缩略图
     * 当新增产品主图时，自动生成压缩后的缩略图
     * @param mainImageEntity 产品主图关联实体
     */
    private void generateThumbnail(RefProductImgAttachmentEntity mainImageEntity) {
        try {
            // 1. 获取原附件信息
            PlmAttachmentEntity originalAttachment = plmAttachmentService.getById(mainImageEntity.getAttachmentId());
            if (originalAttachment == null || StrUtil.isBlank(originalAttachment.getAttachUrl())) {
                log.warn("无法生成缩略图：原附件不存在或附件URL为空，attachmentId: {}", mainImageEntity.getAttachmentId());
                return;
            }

            // 2. 调用FileFeign压缩并上传图片
            String thumbnailUrl = fileFeign.compressAndUploadImage(originalAttachment.getAttachUrl(), THUMBNAIL_TARGET_SIZE_KB);
            if (StrUtil.isBlank(thumbnailUrl)) {
                log.warn("压缩图片失败，无法生成缩略图，原URL: {}", originalAttachment.getAttachUrl());
                return;
            }

            // 3. 创建缩略图附件记录
            PlmAttachmentEntity thumbnailAttachment = new PlmAttachmentEntity();
            thumbnailAttachment.setAttachUrl(thumbnailUrl);
            // 缩略图文件名添加_thumb后缀
            String originalName = originalAttachment.getAttachName();
            String thumbnailName = originalName != null && originalName.contains(".") 
                    ? originalName.replaceFirst("(\\.\\w+)$", "_thumb$1")
                    : (originalName != null ? originalName + "_thumb.jpg" : "thumbnail.jpg");
            thumbnailAttachment.setAttachName(thumbnailName.toLowerCase());
            
            // 计算缩略图大小（MB）
            // 注意：这里无法直接获取压缩后文件的实际大小，可以后续通过下载文件获取
            thumbnailAttachment.setAttachSize(BigDecimal.valueOf(THUMBNAIL_TARGET_SIZE_KB / 1024.0));
            thumbnailAttachment.setType(originalAttachment.getType());
            thumbnailAttachment.setBusinessId(originalAttachment.getBusinessId());
            plmAttachmentService.save(thumbnailAttachment);

            // 4. 创建缩略图分类附件关联记录
            RefProductImgAttachmentEntity thumbnailEntity = new RefProductImgAttachmentEntity();
            thumbnailEntity.setCategoryId(PRODUCT_THUMBNAIL_CATEGORY_ID);
            thumbnailEntity.setProductDetailId(mainImageEntity.getProductDetailId());
            thumbnailEntity.setSkuNo(mainImageEntity.getSkuNo());
            thumbnailEntity.setAttachmentId(thumbnailAttachment.getId());
            super.save(thumbnailEntity);

            // 5. 更新product_detail表的images_url字段，将新生成的缩略图URL放在第一位
            updateProductDetailImagesUrl(mainImageEntity.getProductDetailId(), thumbnailUrl);

            log.info("成功生成产品缩略图：主图ID={}, 缩略图ID={}, 缩略图URL={}", 
                    mainImageEntity.getId(), thumbnailEntity.getId(), thumbnailUrl);
        } catch (Exception e) {
            log.error("生成产品缩略图失败：主图ID={}, 错误信息={}", mainImageEntity.getId(), e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 更新product_detail表的images_url字段，将新生成的缩略图URL放在第一位
     * @param productDetailId 产品明细ID
     * @param thumbnailUrl 新生成的缩略图URL
     */
    private void updateProductDetailImagesUrl(String productDetailId, String thumbnailUrl) {
        if (StrUtil.isBlank(productDetailId) || StrUtil.isBlank(thumbnailUrl)) {
            log.warn("更新product_detail的images_url失败：参数为空，productDetailId={}, thumbnailUrl={}", 
                    productDetailId, thumbnailUrl);
            return;
        }

        try {
            // 1. 获取product_detail记录
            ProductDetailEntity productDetail = productDetailService.getById(productDetailId);
            if (productDetail == null) {
                log.warn("更新product_detail的images_url失败：产品明细不存在，productDetailId={}", productDetailId);
                return;
            }

            // 2. 获取当前的images_url（用逗号分割）
            String currentImagesUrl = productDetail.getImagesUrl();
            List<String> imageUrlList = new ArrayList<>();
            
            // 如果当前images_url不为空，先添加到列表
            if (StrUtil.isNotBlank(currentImagesUrl)) {
                // 按逗号分割，过滤空字符串
                imageUrlList = Arrays.stream(currentImagesUrl.split(","))
                        .map(String::trim)
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
            }

            // 3. 将新的缩略图URL放在第一位（如果已存在则先移除，再添加到第一位）
            imageUrlList.remove(thumbnailUrl); // 如果已存在，先移除
            imageUrlList.add(0, thumbnailUrl); // 添加到第一位

            // 4. 重新组合成逗号分割的字符串
            String newImagesUrl = String.join(",", imageUrlList);

            // 5. 更新images_url字段
            productDetail.setImagesUrl(newImagesUrl);
            productDetailService.updateById(productDetail);

            log.info("成功更新product_detail的images_url：productDetailId={}, 新images_url={}", 
                    productDetailId, newImagesUrl);
        } catch (Exception e) {
            log.error("更新product_detail的images_url失败：productDetailId={}, thumbnailUrl={}, 错误信息={}", 
                    productDetailId, thumbnailUrl, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 批量上传图片
     * @param dto 批量上传参数
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 300000)
    @Transactional(rollbackFor = Exception.class)
    public void batchUpload(RefProductImgAttachmentDTO.BatchUploadDTO dto) {
        log.info("开始批量上传图片，zipUrl={}, categoryId={}", dto.getZipUrl(), dto.getCategoryId());
        
        // 1. 检查ZIP文件大小（限制300M）
        try {
            byte[] zipBytes = fileFeign.downloadFile(dto.getZipUrl());
            if (zipBytes == null || zipBytes.length == 0) {
                throw new ServiceException("ZIP文件为空或不存在");
            }
            double zipSizeMB = zipBytes.length / (1024.0 * 1024.0);
            zipSizeMB = Math.round(zipSizeMB * 100.0) / 100.0;
            if (zipSizeMB > 300) {
                throw new ServiceException(ApiError.FILE_SIZE_EXCEEDS_LIMIT, 300);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("检查ZIP文件大小失败：{}", dto.getZipUrl(), e);
            throw new ServiceException("检查ZIP文件大小失败：" + e.getMessage());
        }
        
        // 2. 调用文件服务解压缩ZIP文件并上传所有文件，获取文件信息列表（不传输文件本体）
        List<FileDTO.ExtractedFileInfo> extractedFiles;
        try {
            extractedFiles = fileFeign.unzipAndUploadFiles(dto.getZipUrl());
        } catch (Exception e) {
            log.error("解压缩ZIP文件失败：{}", dto.getZipUrl(), e);
            throw new ServiceException("解压缩ZIP文件失败：" + e.getMessage());
        }
        
        if (CollUtil.isEmpty(extractedFiles)) {
            throw new ServiceException("ZIP文件中没有找到文件");
        }
        
        // 3. 从文件名提取SKU编号（第一次出现"_"前缀）
        Map<String, List<FileDTO.ExtractedFileInfo>> skuFileMap = new HashMap<>();
        List<RefProductImgAttachmentDTO.BatchUploadErrorDTO> errorList = new ArrayList<>();
        List<String> filesToDelete = new ArrayList<>(); // 记录需要删除的文件URL
        
        for (FileDTO.ExtractedFileInfo fileInfo : extractedFiles) {
            String fileName = fileInfo.getFileName();
            if (StrUtil.isBlank(fileName)) {
                errorList.add(new RefProductImgAttachmentDTO.BatchUploadErrorDTO(
                        fileName, "", "文件名为空"));
                filesToDelete.add(fileInfo.getFileUrl());
                continue;
            }
            
            // 提取文件名（不含扩展名）
            String fileNameWithoutExt = fileName;
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileNameWithoutExt = fileName.substring(0, lastDotIndex);
            }
            
            // 提取SKU编号（第一次出现"_"前缀）
            String skuNo;
            int underscoreIndex = fileNameWithoutExt.indexOf('_');
            if (underscoreIndex > 0) {
                skuNo = fileNameWithoutExt.substring(0, underscoreIndex);
            } else {
                skuNo = fileNameWithoutExt;
            }
            
            if (StrUtil.isBlank(skuNo)) {
                errorList.add(new RefProductImgAttachmentDTO.BatchUploadErrorDTO(
                        fileName, skuNo, "无法从文件名提取SKU编号"));
                filesToDelete.add(fileInfo.getFileUrl());
                continue;
            }
            
            skuFileMap.computeIfAbsent(skuNo, k -> new ArrayList<>()).add(fileInfo);
        }
        
        // 4. 查询已审核的SKU
        List<String> skuNoList = new ArrayList<>(skuFileMap.keySet());
        List<ProductDetailEntity> productDetailList = productDetailService.lambdaQuery()
                .in(ProductDetailEntity::getSkuNo, skuNoList)
                .eq(ProductDetailEntity::getStatus, ProductDetailStatusEnum.APPROVAL_PASS.getCode())
                .list();
        
        // 构建SKU到ProductDetail的映射
        Map<String, ProductDetailEntity> productDetailMap = productDetailList.stream()
                .collect(Collectors.toMap(ProductDetailEntity::getSkuNo, pd -> pd, (existing, replacement) -> existing));
        
        // 5. 处理每个SKU的图片
        int successCount = 0;
        int failCount = 0;
        
        for (Map.Entry<String, List<FileDTO.ExtractedFileInfo>> entry : skuFileMap.entrySet()) {
            String skuNo = entry.getKey();
            List<FileDTO.ExtractedFileInfo> fileInfos = entry.getValue();
            
            ProductDetailEntity productDetail = productDetailMap.get(skuNo);
            if (productDetail == null) {
                // SKU不存在或未审核通过，删除文件并记录错误
                for (FileDTO.ExtractedFileInfo fileInfo : fileInfos) {
                    errorList.add(new RefProductImgAttachmentDTO.BatchUploadErrorDTO(
                            fileInfo.getFileName(), skuNo, "SKU不存在或未审核通过"));
                    filesToDelete.add(fileInfo.getFileUrl());
                    failCount++;
                }
                continue;
            }
            
            // 处理该SKU的所有图片
            for (FileDTO.ExtractedFileInfo fileInfo : fileInfos) {
                try {
                    // 保存附件记录
                    PlmAttachmentEntity attachmentEntity = new PlmAttachmentEntity();
                    attachmentEntity.setAttachUrl(fileInfo.getFileUrl());
                    attachmentEntity.setAttachName(fileInfo.getFileName());
                    attachmentEntity.setAttachSize(BigDecimal.valueOf(fileInfo.getFileSize() / 1024.0 / 1024.0)); // MB
                    attachmentEntity.setType("product_image");
                    attachmentEntity.setBusinessId(productDetail.getId());
                    plmAttachmentService.save(attachmentEntity);
                    
                    // 创建图片分类附件关联记录
                    RefProductImgAttachmentEntity refEntity = new RefProductImgAttachmentEntity();
                    refEntity.setCategoryId(dto.getCategoryId());
                    refEntity.setProductDetailId(productDetail.getId());
                    refEntity.setSkuNo(skuNo);
                    refEntity.setAttachmentId(attachmentEntity.getId());
                    super.save(refEntity);
                    
                    // 如果分类是产品主图，自动生成产品缩略图
                    if (PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(dto.getCategoryId())) {
                        generateThumbnail(refEntity);
                    }
                    
                    successCount++;
                } catch (Exception e) {
                    log.error("处理图片失败：fileName={}, skuNo={}, fileUrl={}", 
                            fileInfo.getFileName(), skuNo, fileInfo.getFileUrl(), e);
                    errorList.add(new RefProductImgAttachmentDTO.BatchUploadErrorDTO(
                            fileInfo.getFileName(), skuNo, "处理失败：" + e.getMessage()));
                    filesToDelete.add(fileInfo.getFileUrl());
                    failCount++;
                }
            }
        }
        
        // 6. 删除失败的文件
        if (CollUtil.isNotEmpty(filesToDelete)) {
            try {
                fileFeign.deleteBatchFile(filesToDelete);
                log.info("删除失败文件，共{}个", filesToDelete.size());
            } catch (Exception e) {
                log.error("删除失败文件时出错", e);
                // 不抛出异常，避免影响主流程
            }
        }
        
        // 7. 生成错误信息Excel并上传到FastDFS
        String errorUrl = "";
        if (CollUtil.isNotEmpty(errorList)) {
            try {
                String fileName = "批量上传图片错误信息.xlsx";
                File file = ExcelUtil.exportFile(fileName, "error", errorList, RefProductImgAttachmentDTO.BatchUploadErrorDTO.class);
                if (file != null && !file.isDirectory()) {
                    errorUrl = FastDFSClientUtil.uploadFile(file, fileName);
                }
            } catch (Exception e) {
                log.error("生成错误信息Excel失败", e);
            }
        }
        
        // 8. 更新任务结果（如果有taskId）
        if (StrUtil.isNotBlank(dto.getTaskId())) {
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setCount(successCount);
            importResultDTO.setRemark("处理完成，成功" + successCount + "条，失败" + failCount + "条");
            importResultDTO.setErrorUrl(errorUrl);
            importResultDTO.setFinishTime(LocalDateTime.now());
            importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            downloadTaskFeign.updateTask(importResultDTO);
        }
        
        log.info("批量上传图片完成，成功{}条，失败{}条", successCount, failCount);
    }

    /**
     * 单个删除
     * 删除关联信息、附件表和文件
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        log.info("开始删除图片分类附件关联记录，id={}", id);
        
        // 1. 查询关联记录
        RefProductImgAttachmentEntity refEntity = super.getById(id);
        if (refEntity == null) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "图片分类附件关联记录");
        }
        
        // 2. 查询附件记录
        String attachmentId = refEntity.getAttachmentId();
        String fileUrl = null;
        if (StrUtil.isNotBlank(attachmentId)) {
            PlmAttachmentEntity attachmentEntity = plmAttachmentService.getById(attachmentId);
            if (attachmentEntity != null) {
                fileUrl = attachmentEntity.getAttachUrl();
                
                // 3. 删除关联记录
                boolean deleted = super.removeById(id);
                if (!deleted) {
                    throw new ServiceException("删除关联记录失败");
                }
                
                // 4. 删除附件记录
                boolean attachmentDeleted = plmAttachmentService.removeById(attachmentId);
                if (!attachmentDeleted) {
                    log.warn("删除附件记录失败，attachmentId={}", attachmentId);
                }
                
                // 5. 删除FastDFS中的文件
                if (StrUtil.isNotBlank(fileUrl)) {
                    try {
                        int deleteResult = fileFeign.deleteFile(fileUrl);
                        if (deleteResult == 0) {
                            log.info("删除文件成功，fileUrl={}", fileUrl);
                        } else {
                            log.warn("删除文件失败，fileUrl={}, result={}", fileUrl, deleteResult);
                        }
                    } catch (Exception e) {
                        log.error("删除文件时出错，fileUrl={}", fileUrl, e);
                        // 不抛出异常，避免影响主流程
                    }
                }
            } else {
                // 附件记录不存在，只删除关联记录
                boolean deleted = super.removeById(id);
                if (!deleted) {
                    throw new ServiceException("删除关联记录失败");
                }
            }
        } else {
            // 没有附件ID，只删除关联记录
            boolean deleted = super.removeById(id);
            if (!deleted) {
                throw new ServiceException("删除关联记录失败");
            }
        }
        
        // 使用SKU编号作为code，如果没有则使用ID
        String code = StrUtil.isNotBlank(refEntity.getSkuNo()) ? refEntity.getSkuNo() : id;
        log.info("删除图片分类附件关联记录成功，id={}", id);
        return BatchResultDTO.success(id, code, "删除成功");
    }

    /**
     * 移动分类
     * 将多个图片移动到新的分类
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveCategory(RefProductImgAttachmentDTO.MoveCategoryDTO dto) {
        log.info("开始移动图片分类，ids={}, categoryId={}", dto.getIds(), dto.getCategoryId());
        
        // 1. 校验目标分类是否存在
        ProductImgCategoryEntity targetCategory = productImgCategoryService.getById(dto.getCategoryId());
        if (targetCategory == null) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "目标分类");
        }
        
        // 2. 校验目标分类不是"所有分类"和"产品主图"分类
        if (ALL_CATEGORY_ID.equals(dto.getCategoryId())) {
            throw new ServiceException("不能移动到\"所有分类\"");
        }
        if (PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(dto.getCategoryId())) {
            throw new ServiceException("不能移动到\"产品主图\"分类");
        }
        if (PRODUCT_THUMBNAIL_CATEGORY_ID.equals(dto.getCategoryId())) {
            throw new ServiceException("不能移动到\"产品缩略图\"分类");
        }
        
        // 3. 批量查询关联记录
        List<RefProductImgAttachmentEntity> refEntityList = super.listByIds(dto.getIds());
        if (CollUtil.isEmpty(refEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "图片分类附件关联记录");
        }
        
        // 4. 批量更新分类ID
        boolean updated = super.lambdaUpdate()
                .in(RefProductImgAttachmentEntity::getId, dto.getIds())
                .set(RefProductImgAttachmentEntity::getCategoryId, dto.getCategoryId())
                .update();
        
        if (!updated) {
            throw new ServiceException("移动分类失败");
        }
        
        log.info("移动图片分类成功，共移动{}条记录到分类{}", refEntityList.size(), dto.getCategoryId());
        return Boolean.TRUE;
    }

    /**
     * 批量下载图片
     * 创建异步下载任务
     */
    @Override
    public Boolean batchDownload(RefProductImgAttachmentDTO.BatchDownloadDTO dto) {
        log.info("开始创建批量下载图片任务，ids={}", dto.getIds());
        
        // 1. 校验至少选择一张图片
        if (CollUtil.isEmpty(dto.getIds())) {
            throw new ServiceException("请至少选择一张图片");
        }
        
        // 2. 校验最多50张图片
        if (dto.getIds().size() > 50) {
            throw new ServiceException("最多支持50张图片下载");
        }
        
        // 3. 创建异步下载任务（传递ids，由handler处理）
        downloadTaskFeign.saveDownloadTask("批量下载图片", "EXPORT_PLM_PRODUCT_IMAGES", dto);
        
        log.info("批量下载图片任务创建成功，共{}张图片", dto.getIds().size());
        return Boolean.TRUE;
    }

    /**
     * 构建产品图片文件夹结构并创建ZIP（用于批量下载）
     * 按分类拆分文件夹，构建文件夹结构，然后创建ZIP文件
     */
    @Override
    public String buildProductImagesFolderStructure(RefProductImgAttachmentDTO.BatchDownloadDTO dto) {
        log.info("开始构建产品图片文件夹结构并创建ZIP，ids={}", dto.getIds());
        
        // 1. 批量查询关联记录
        List<RefProductImgAttachmentEntity> refEntityList = super.listByIds(dto.getIds());
        if (CollUtil.isEmpty(refEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "图片分类附件关联记录");
        }
        
        // 2. 收集所有附件ID
        List<String> attachmentIds = refEntityList.stream()
                .map(RefProductImgAttachmentEntity::getAttachmentId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        // 3. 批量查询附件记录
        Map<String, PlmAttachmentEntity> attachmentMap = new HashMap<>();
        if (CollUtil.isNotEmpty(attachmentIds)) {
            List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByIds(attachmentIds);
            attachmentMap = attachmentList.stream()
                    .collect(Collectors.toMap(PlmAttachmentEntity::getId, a -> a, (existing, replacement) -> existing));
        }
        
        // 4. 收集所有分类ID
        List<String> categoryIds = refEntityList.stream()
                .map(RefProductImgAttachmentEntity::getCategoryId)
                .filter(StrUtil::isNotBlank)
                .filter(cid -> !ALL_CATEGORY_ID.equals(cid)) // 排除"所有分类"
                .distinct()
                .collect(Collectors.toList());
        
        // 5. 批量查询分类记录（包括所有父分类）
        Map<String, ProductImgCategoryEntity> categoryMap = new HashMap<>();
        if (CollUtil.isNotEmpty(categoryIds)) {
            // 先查询直接分类
            List<ProductImgCategoryEntity> categoryList = productImgCategoryService.listByIds(categoryIds);
            categoryMap = categoryList.stream()
                    .collect(Collectors.toMap(ProductImgCategoryEntity::getId, c -> c, (existing, replacement) -> existing));
            
            // 查询所有父分类
            Set<String> allCategoryIds = new HashSet<>(categoryIds);
            for (ProductImgCategoryEntity category : categoryList) {
                collectParentCategoryIds(category, allCategoryIds, categoryMap);
            }
            
            // 如果还有未查询的父分类，批量查询
            Map<String, ProductImgCategoryEntity> finalCategoryMap = categoryMap;
            List<String> missingParentIds = allCategoryIds.stream()
                    .filter(id -> !finalCategoryMap.containsKey(id) && !ALL_CATEGORY_ID.equals(id))
                    .collect(Collectors.toList());
            
            if (CollUtil.isNotEmpty(missingParentIds)) {
                List<ProductImgCategoryEntity> parentCategoryList = productImgCategoryService.listByIds(missingParentIds);
                for (ProductImgCategoryEntity parent : parentCategoryList) {
                    categoryMap.put(parent.getId(), parent);
                }
            }
        }
        
        // 6. 构建分类路径映射（从第一层开始）
        Map<String, String> categoryPathMap = new HashMap<>();
        for (String categoryId : categoryIds) {
            ProductImgCategoryEntity category = categoryMap.get(categoryId);
            if (category != null) {
                String path = buildCategoryPath(category, categoryMap);
                categoryPathMap.put(categoryId, path);
            }
        }
        
        // 7. 按分类分组，构建文件夹结构
        // folderStructure: key是文件夹路径（如"分类1/子分类1"），value是文件URL列表
        Map<String, List<String>> folderStructure = new LinkedHashMap<>();
        Map<String, String> fileUrlToNameMap = new HashMap<>();
        
        for (RefProductImgAttachmentEntity refEntity : refEntityList) {
            String categoryId = refEntity.getCategoryId();
            PlmAttachmentEntity attachment = attachmentMap.get(refEntity.getAttachmentId());
            
            if (attachment == null || StrUtil.isBlank(attachment.getAttachUrl())) {
                continue;
            }
            
            String fileUrl = attachment.getAttachUrl();
            String fileName = attachment.getAttachName();
            if (StrUtil.isBlank(fileName)) {
                // 从URL中提取文件名
                int lastSlash = fileUrl.lastIndexOf('/');
                if (lastSlash >= 0 && lastSlash < fileUrl.length() - 1) {
                    fileName = fileUrl.substring(lastSlash + 1);
                } else {
                    fileName = "file_" + System.currentTimeMillis();
                }
            }
            
            // 确定文件夹路径
            String folderPath = "";
            if (StrUtil.isNotBlank(categoryId) && !ALL_CATEGORY_ID.equals(categoryId)) {
                folderPath = categoryPathMap.getOrDefault(categoryId, "");
            }
            
            // 添加到文件夹结构
            folderStructure.computeIfAbsent(folderPath, k -> new ArrayList<>()).add(fileUrl);
            fileUrlToNameMap.put(fileUrl, fileName);
        }
        
        // 8. 校验是否有可下载的图片
        if (folderStructure.isEmpty() || folderStructure.values().stream().allMatch(List::isEmpty)) {
            throw new ServiceException("未找到可下载的图片");
        }
        
        // 9. 构建CreateZipDTO并调用文件中心创建ZIP
        FileDTO.CreateZipDTO createZipDTO = FileDTO.CreateZipDTO.builder()
                .folderStructure(folderStructure)
                .fileUrlToNameMap(fileUrlToNameMap)
                .build();
        
        String zipUrl = fileFeign.createZipFromFolderStructure(createZipDTO);
        
        if (StrUtil.isBlank(zipUrl)) {
            throw new ServiceException("创建ZIP文件失败");
        }
        
        log.info("构建产品图片文件夹结构并创建ZIP完成，共{}个文件夹，{}个文件，zipUrl={}", 
                folderStructure.size(), 
                folderStructure.values().stream().mapToInt(List::size).sum(),
                zipUrl);
        return zipUrl;
    }
    
    /**
     * 收集父分类ID
     * @param category 分类实体
     * @param allCategoryIds 所有分类ID集合（用于收集）
     * @param categoryMap 已查询的分类映射
     */
    private void collectParentCategoryIds(ProductImgCategoryEntity category, Set<String> allCategoryIds, Map<String, ProductImgCategoryEntity> categoryMap) {
        if (category == null) {
            return;
        }
        
        String parentId = category.getParentId();
        if (StrUtil.isNotBlank(parentId) && !ALL_CATEGORY_ID.equals(parentId) && !allCategoryIds.contains(parentId)) {
            allCategoryIds.add(parentId);
            // 如果父分类已在categoryMap中，递归收集其父分类
            ProductImgCategoryEntity parent = categoryMap.get(parentId);
            if (parent != null) {
                collectParentCategoryIds(parent, allCategoryIds, categoryMap);
            }
        }
    }
    
    /**
     * 构建分类路径（从第一层开始）
     * @param category 分类实体
     * @param categoryMap 所有分类的映射（包括父分类）
     * @return 分类路径，如 "分类1/子分类1"
     */
    private String buildCategoryPath(ProductImgCategoryEntity category, Map<String, ProductImgCategoryEntity> categoryMap) {
        if (category == null) {
            return "";
        }
        
        // 如果是"所有分类"，返回空字符串
        if (ALL_CATEGORY_ID.equals(category.getId())) {
            return "";
        }
        
        // 从第一层开始构建路径
        List<String> pathList = new ArrayList<>();
        ProductImgCategoryEntity current = category;
        
        // 向上查找父分类，直到第一层（level=1）或"所有分类"
        while (current != null) {
            // 如果是"所有分类"，停止
            if (ALL_CATEGORY_ID.equals(current.getId())) {
                break;
            }
            
            // 添加到路径列表（从当前分类开始）
            pathList.add(0, current.getName());
            
            // 如果是第一层，停止
            if (current.getLevel() != null && current.getLevel() == 1) {
                break;
            }
            
            // 查找父分类
            String parentId = current.getParentId();
            if (StrUtil.isBlank(parentId) || ALL_CATEGORY_ID.equals(parentId)) {
                break;
            }
            
            current = categoryMap.get(parentId);
            if (current == null) {
                break;
            }
        }
        
        // 如果路径列表为空，返回分类名称
        if (pathList.isEmpty()) {
            return category.getName();
        }
        
        // 拼接路径
        return String.join("/", pathList);
    }
}
    