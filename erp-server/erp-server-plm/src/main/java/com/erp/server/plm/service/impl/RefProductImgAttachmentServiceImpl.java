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
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
import com.erp.rpc.sys.feign.SysUserFeign;
import com.common.business.dto.FindUserDTO;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;
import com.erp.server.plm.service.PlmAttachmentService;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.service.ProductImgCategoryService;
import com.erp.model.plm.entity.ProductImgCategoryEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.server.plm.service.OperateLogService;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.file.dto.FileDTO;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_IMAGES;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_PLM_PRODUCT_IMG_ATTACHMENT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.concurrent.ExecutorService;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.enums.SettingEnum;

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
    
    @Resource
    private SysUserFeign sysUserFeign;
    
    @Resource
    private OperateLogService operateLogService;
    
    @Resource
    @Qualifier("zipImageExecutorPool")
    private ExecutorService zipImageExecutorPool;
    
    // 所有分类ID
    private static final String ALL_CATEGORY_ID = "1000000000000000001";
    // 产品主图分类ID
    private static final String PRODUCT_MAIN_IMAGE_CATEGORY_ID = "1000000000000000002";
    // SKU类路径（用于操作日志）
    private static final String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);
    
    /**
     * 获取图片上传大小配置（KB）
     * @return 配置的大小（KB），如果未配置返回0
     */
    private Long getImgUploadSizeConfig() {
        Long size = 0L;
        try {
            List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                    .eq(CfgSettingEntity::getKey, SettingEnum.IMG_UPLOAD_SIZE_KEY.getKey())
                    .list();
            if (CollUtil.isNotEmpty(list) && ObjectUtil.isNotNull(list.get(0).getValue())) {
                size = Long.valueOf(list.get(0).getValue());
            }
        } catch (Exception e) {
            log.warn("获取图片上传大小配置失败，将使用默认值0，错误信息：{}", e.getMessage());
        }
        return size;
    }
    
    /**
     * 获取"未分类"的分类ID
     * 如果不存在，返回"所有分类"ID作为默认值
     * @return 未分类的分类ID
     */
    private String getUncategorizedCategoryId() {
        try {
            // 查询"所有分类"下名为"未分类"的子分类
            List<ProductImgCategoryEntity> categories = productImgCategoryService.lambdaQuery()
                    .eq(ProductImgCategoryEntity::getParentId, ALL_CATEGORY_ID)
                    .eq(ProductImgCategoryEntity::getName, "未分类")
                    .list();
            if (CollUtil.isNotEmpty(categories)) {
                return categories.get(0).getId();
            }
            // 如果找不到，使用"所有分类"ID作为默认值
            log.warn("未找到'未分类'分类，使用'所有分类'作为默认值");
            return ALL_CATEGORY_ID;
        } catch (Exception e) {
            log.warn("获取'未分类'分类ID失败，使用'所有分类'作为默认值，错误信息：{}", e.getMessage());
            return ALL_CATEGORY_ID;
        }
    }

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
            throw new ServiceException(ApiError.PRODUCT_IMG_ATTACHMENT_SAVE_FAILED);
        }

        // 如果分类是产品主图，确保只有一个主图：将之前的主图移出主图分类
        boolean isMainImage = PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(refProductImgAttachmentEntity.getCategoryId());
        if (isMainImage) {
            // 将同一个SKU的其他主图移出主图分类，归类到未分类
            String uncategorizedCategoryId = getUncategorizedCategoryId();
            List<RefProductImgAttachmentEntity> oldMainImages = super.lambdaQuery()
                    .eq(RefProductImgAttachmentEntity::getProductDetailId, refProductImgAttachmentEntity.getProductDetailId())
                    .eq(RefProductImgAttachmentEntity::getCategoryId, PRODUCT_MAIN_IMAGE_CATEGORY_ID)
                    .ne(RefProductImgAttachmentEntity::getId, refProductImgAttachmentEntity.getId())
                    .list();
            
            for (RefProductImgAttachmentEntity oldMainImage : oldMainImages) {
                oldMainImage.setCategoryId(uncategorizedCategoryId);
                super.updateById(oldMainImage);
                log.info("将旧主图移出主图分类，归类到未分类，refId={}", oldMainImage.getId());
            }
        }
        
        // 缩略图不管怎么样都要生成，不管是什么分类
        if (isMainImage) {
            // 如果是主图，生成缩略图并更新product_detail的images_url（放在第一位）
            generateThumbnail(refProductImgAttachmentEntity);
        } else {
            // 如果不是主图，生成缩略图并更新images_url（拼接到最后面）
            String productDetailId = refProductImgAttachmentEntity.getProductDetailId();
            String skuNo = refProductImgAttachmentEntity.getSkuNo();
            if (StrUtil.isNotBlank(productDetailId) && StrUtil.isNotBlank(skuNo)) {
                // 生成缩略图
                String thumbnailUrl = generateThumbnailWithoutUpdate(
                        refProductImgAttachmentEntity.getAttachmentId(), 
                        productDetailId, 
                        skuNo
                );
                // 获取原图URL
                PlmAttachmentEntity attachment = plmAttachmentService.getById(refProductImgAttachmentEntity.getAttachmentId());
                if (attachment != null && StrUtil.isNotBlank(attachment.getAttachUrl())) {
                    String originalUrl = attachment.getAttachUrl();
                    // 将缩略图URL（或原图URL）拼接到images_url的末尾
                    String finalUrl = StrUtil.isNotBlank(thumbnailUrl) ? thumbnailUrl : originalUrl;
                    appendThumbnailUrlToImagesUrl(productDetailId, finalUrl);
                }
            }
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
            throw new ServiceException(ApiError.PRODUCT_IMG_ATTACHMENT_SAVE_FAILED);
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
        // 填充 sku_no
        if (StrUtil.isNotBlank(refProductImgAttachmentEntity.getProductDetailId())) {
            ProductDetailEntity productDetail = productDetailService.getById(refProductImgAttachmentEntity.getProductDetailId());
            if (ObjectUtil.isNotEmpty(productDetail) && StrUtil.isNotBlank(productDetail.getSkuNo())) {
                refProductImgAttachmentEntity.setSkuNo(productDetail.getSkuNo());
            }
        }
    }

    @Override
    public RefProductImgAttachmentDTO.ViewDTO view(String id) {
    RefProductImgAttachmentEntity refProductImgAttachmentEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException(ApiError.PRODUCT_IMG_ATTACHMENT_NOT_FOUND));
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
        // 收集需要查询 sku_no 的记录（sku_no 为空且 productDetailId 不为空）
        List<String> productDetailIds = list.stream()
                .filter(data -> StrUtil.isBlank(data.getSkuNo()) && StrUtil.isNotBlank(data.getProductDetailId()))
                .map(RefProductImgAttachmentDTO.ListDTO::getProductDetailId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询 ProductDetail
        Map<String, String> productDetailIdToSkuNoMap = new HashMap<>();
        if (CollUtil.isNotEmpty(productDetailIds)) {
            List<ProductDetailEntity> productDetailList = productDetailService.listByIds(productDetailIds);
            if (CollUtil.isNotEmpty(productDetailList)) {
                productDetailIdToSkuNoMap = productDetailList.stream()
                        .filter(pd -> StrUtil.isNotBlank(pd.getSkuNo()))
                        .collect(Collectors.toMap(
                                ProductDetailEntity::getId,
                                ProductDetailEntity::getSkuNo,
                                (v1, v2) -> v1
                        ));
            }
        }
        
        // 填充 sku_no
        final Map<String, String> finalMap = productDetailIdToSkuNoMap;
        for(RefProductImgAttachmentDTO.ListDTO data : list) {
            if (StrUtil.isBlank(data.getSkuNo()) && StrUtil.isNotBlank(data.getProductDetailId())) {
                String skuNo = finalMap.get(data.getProductDetailId());
                if (StrUtil.isNotBlank(skuNo)) {
                    data.setSkuNo(skuNo);
                }
            }
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

            // 1.1 获取配置的图片上传大小（KB）
            Long targetSizeKB = getImgUploadSizeConfig();
            if (targetSizeKB == null || targetSizeKB <= 0) {
                log.info("图片上传大小配置为0或未配置，跳过生成缩略图，直接使用原图，attachmentId: {}", mainImageEntity.getAttachmentId());
                // 如果不需要压缩，thumbnailAttachmentId应该等于attachmentId
                mainImageEntity.setThumbnailAttachmentId(mainImageEntity.getAttachmentId());
                super.updateById(mainImageEntity);
                return;
            }

            String thumbnailAttachmentId = null;

            // 1.2 检查原图大小，如果符合要求则不需要生成缩略图
            try {
                List<FileDTO.FileSizeInfo> fileSizeInfoList = fileFeign.getBatchFileSize(
                        Collections.singletonList(originalAttachment.getAttachUrl()));
                if (CollUtil.isNotEmpty(fileSizeInfoList)) {
                    FileDTO.FileSizeInfo fileSizeInfo = fileSizeInfoList.get(0);
                    if (fileSizeInfo.getFileSize() != null && fileSizeInfo.getFileSize() > 0) {
                        long fileSizeKB = fileSizeInfo.getFileSize() / 1024;
                        if (fileSizeKB <= targetSizeKB) {
                            log.info("原图大小{}KB符合要求（配置要求≤{}KB），无需生成缩略图，使用原图，attachmentId: {}", 
                                    fileSizeKB, targetSizeKB, mainImageEntity.getAttachmentId());
                            // 如果不需要压缩，thumbnailAttachmentId应该等于attachmentId
                            mainImageEntity.setThumbnailAttachmentId(mainImageEntity.getAttachmentId());
                            super.updateById(mainImageEntity);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取原图大小失败，继续生成缩略图，attachmentId: {}, 错误信息: {}", 
                        mainImageEntity.getAttachmentId(), e.getMessage());
            }

            // 2. 调用FileFeign压缩并上传图片
            String thumbnailUrl = fileFeign.compressAndUploadImage(originalAttachment.getAttachUrl(), targetSizeKB);
            if (StrUtil.isBlank(thumbnailUrl)) {
                log.warn("压缩图片失败，无法生成缩略图，使用原图，原URL: {}", originalAttachment.getAttachUrl());
                // 如果压缩失败，thumbnailAttachmentId应该等于attachmentId
                mainImageEntity.setThumbnailAttachmentId(mainImageEntity.getAttachmentId());
                super.updateById(mainImageEntity);
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
            
            thumbnailAttachment.setAttachSize(BigDecimal.valueOf(targetSizeKB != null && targetSizeKB > 0 ? targetSizeKB / 1024.0 : 0.2));
            thumbnailAttachment.setType(originalAttachment.getType());
            thumbnailAttachment.setBusinessId(originalAttachment.getBusinessId());
            plmAttachmentService.save(thumbnailAttachment);
            thumbnailAttachmentId = thumbnailAttachment.getId();

            // 4. 更新原记录的thumbnailAttachmentId字段（不再创建新的分类记录）
            mainImageEntity.setThumbnailAttachmentId(thumbnailAttachmentId);
            super.updateById(mainImageEntity);

            // 5. 更新product_detail表的images_url字段，将新生成的缩略图URL放在第一位
            updateProductDetailImagesUrl(mainImageEntity.getProductDetailId(), thumbnailUrl);

            log.info("成功生成产品缩略图：主图ID={},  缩略图URL={}",
                    mainImageEntity.getId(), thumbnailUrl);
        } catch (Exception e) {
            log.error("生成产品缩略图失败：主图ID={}, 错误信息={}", mainImageEntity.getId(), e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 生成产品缩略图（不更新images_url）
     * @param mainImageAttachmentId 产品主图附件ID
     * @param productDetailId 产品明细ID
     * @param skuNo SKU编号
     * @return 生成的缩略图URL，如果生成失败或不需要生成则返回null（表示使用原图）
     */
    private String generateThumbnailWithoutUpdate(String mainImageAttachmentId, String productDetailId, String skuNo) {
        try {
            // 1. 查找对应的ref_product_img_attachment记录
            RefProductImgAttachmentEntity refEntity = super.lambdaQuery()
                    .eq(RefProductImgAttachmentEntity::getProductDetailId, productDetailId)
                    .eq(RefProductImgAttachmentEntity::getAttachmentId, mainImageAttachmentId)
                    .one();
            
            if (refEntity == null) {
                log.warn("无法生成缩略图：找不到对应的关联记录，attachmentId: {}, productDetailId: {}", 
                        mainImageAttachmentId, productDetailId);
                return null;
            }

            // 2. 获取原附件信息
            PlmAttachmentEntity originalAttachment = plmAttachmentService.getById(mainImageAttachmentId);
            if (originalAttachment == null || StrUtil.isBlank(originalAttachment.getAttachUrl())) {
                log.warn("无法生成缩略图：原附件不存在或附件URL为空，attachmentId: {}", mainImageAttachmentId);
                return null;
            }

            String originalUrl = originalAttachment.getAttachUrl();

            // 3. 获取配置的图片上传大小（KB）
            Long targetSizeKB = getImgUploadSizeConfig();
            if (targetSizeKB == null || targetSizeKB <= 0) {
                log.info("图片上传大小配置为0或未配置，跳过生成缩略图，使用原图，attachmentId: {}", mainImageAttachmentId);
                // 如果不需要压缩，thumbnailAttachmentId应该等于attachmentId
                refEntity.setThumbnailAttachmentId(mainImageAttachmentId);
                super.updateById(refEntity);
                return null; // 返回null表示使用原图，不生成缩略图
            }

            // 4. 检查原图大小，如果符合要求则不需要生成缩略图
            try {
                List<FileDTO.FileSizeInfo> fileSizeInfoList = fileFeign.getBatchFileSize(
                        Collections.singletonList(originalUrl));
                if (CollUtil.isNotEmpty(fileSizeInfoList)) {
                    FileDTO.FileSizeInfo fileSizeInfo = fileSizeInfoList.get(0);
                    if (fileSizeInfo.getFileSize() != null && fileSizeInfo.getFileSize() > 0) {
                        long fileSizeKB = fileSizeInfo.getFileSize() / 1024;
                        if (fileSizeKB <= targetSizeKB) {
                            log.info("原图大小{}KB符合要求（配置要求≤{}KB），无需生成缩略图，使用原图，attachmentId: {}", 
                                    fileSizeKB, targetSizeKB, mainImageAttachmentId);
                            // 如果不需要压缩，thumbnailAttachmentId应该等于attachmentId
                            refEntity.setThumbnailAttachmentId(mainImageAttachmentId);
                            super.updateById(refEntity);
                            return null; // 返回null表示使用原图，不生成缩略图
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取原图大小失败，继续生成缩略图，attachmentId: {}, 错误信息: {}", 
                        mainImageAttachmentId, e.getMessage());
            }

            // 5. 调用FileFeign压缩并上传图片
            String thumbnailUrl = fileFeign.compressAndUploadImage(originalUrl, targetSizeKB);
            if (StrUtil.isBlank(thumbnailUrl)) {
                log.warn("压缩图片失败，无法生成缩略图，使用原图，原URL: {}", originalUrl);
                // 如果压缩失败，thumbnailAttachmentId应该等于attachmentId
                refEntity.setThumbnailAttachmentId(mainImageAttachmentId);
                super.updateById(refEntity);
                return null; // 返回null表示使用原图，不生成缩略图
            }

            // 6. 创建缩略图附件记录
            PlmAttachmentEntity thumbnailAttachment = new PlmAttachmentEntity();
            thumbnailAttachment.setAttachUrl(thumbnailUrl);
            // 缩略图文件名添加_thumb后缀
            String originalName = originalAttachment.getAttachName();
            String thumbnailName = originalName != null && originalName.contains(".") 
                    ? originalName.replaceFirst("(\\.\\w+)$", "_thumb$1")
                    : (originalName != null ? originalName + "_thumb.jpg" : "thumbnail.jpg");
            thumbnailAttachment.setAttachName(thumbnailName.toLowerCase());
            
            // 计算缩略图大小（MB）
            thumbnailAttachment.setAttachSize(BigDecimal.valueOf(targetSizeKB / 1024.0));
            thumbnailAttachment.setType(originalAttachment.getType());
            thumbnailAttachment.setBusinessId(originalAttachment.getBusinessId());
            plmAttachmentService.save(thumbnailAttachment);

            // 7. 更新原记录的thumbnailAttachmentId字段（不再创建新的分类记录）
            refEntity.setThumbnailAttachmentId(thumbnailAttachment.getId());
            super.updateById(refEntity);

            log.info("成功生成产品缩略图：主图附件ID={}, 缩略图附件ID={}, 缩略图URL={}", 
                    mainImageAttachmentId, thumbnailAttachment.getId(), thumbnailUrl);
            return thumbnailUrl; // 返回缩略图URL
        } catch (Exception e) {
            log.error("生成产品缩略图失败：主图附件ID={}, 错误信息={}", mainImageAttachmentId, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
            return null; // 返回null表示使用原图
        }
    }
    
    /**
     * 在product_detail的images_url中用缩略图URL替换原图URL
     * @param productDetailId 产品明细ID
     * @param originalUrl 原图URL
     * @param thumbnailUrl 缩略图URL（如果为null，则不替换，保留原图）
     */
    private void replaceOriginalUrlWithThumbnailInImagesUrl(String productDetailId, String originalUrl, String thumbnailUrl) {
        if (StrUtil.isBlank(productDetailId) || StrUtil.isBlank(originalUrl)) {
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
                imageUrlList = Arrays.stream(currentImagesUrl.split(","))
                        .map(String::trim)
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
            }

            // 3. 替换原图URL为缩略图URL
            if (StrUtil.isNotBlank(thumbnailUrl)) {
                // 如果原图URL存在，替换为缩略图URL
                int originalIndex = imageUrlList.indexOf(originalUrl);
                if (originalIndex >= 0) {
                    imageUrlList.set(originalIndex, thumbnailUrl);
                    log.info("在images_url中用缩略图URL替换原图URL，productDetailId={}, 原图={}, 缩略图={}", 
                            productDetailId, originalUrl, thumbnailUrl);
                } else {
                    // 如果原图URL不存在，将缩略图URL放在第一位
                    imageUrlList.remove(thumbnailUrl); // 先移除可能存在的缩略图URL
                    imageUrlList.add(0, thumbnailUrl);
                    log.info("在images_url中添加缩略图URL，productDetailId={}, 缩略图={}", 
                            productDetailId, thumbnailUrl);
                }
            }
            // 如果thumbnailUrl为null，不做替换，保留原图URL

            // 4. 重新组合成逗号分割的字符串
            String newImagesUrl = String.join(",", imageUrlList);

            // 5. 更新images_url字段
            productDetail.setImagesUrl(newImagesUrl);
            productDetailService.updateById(productDetail);

            log.info("成功更新product_detail的images_url：productDetailId={}, 新images_url={}", 
                    productDetailId, newImagesUrl);
        } catch (Exception e) {
            log.error("更新product_detail的images_url失败：productDetailId={}, originalUrl={}, thumbnailUrl={}, 错误信息={}", 
                    productDetailId, originalUrl, thumbnailUrl, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 更新product_detail的images_url（使用缩略图URL，放在第一位，然后添加原图URL）
     * @param productDetailId 产品明细ID
     * @param thumbnailUrl 缩略图URL
     * @param originalUrl 原图URL
     */
    private void updateProductDetailImagesUrlWithThumbnail(String productDetailId, String thumbnailUrl, String originalUrl) {
        if (StrUtil.isBlank(productDetailId)) {
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
                imageUrlList = Arrays.stream(currentImagesUrl.split(","))
                        .map(String::trim)
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
            }

            // 3. 将缩略图URL放在第一位（如果已存在则先移除）
            imageUrlList.remove(thumbnailUrl);
            imageUrlList.add(0, thumbnailUrl);
            
            // 4. 添加原图URL（如果不存在）
            if (StrUtil.isNotBlank(originalUrl) && !imageUrlList.contains(originalUrl)) {
                imageUrlList.add(originalUrl);
            }

            // 5. 重新组合成逗号分割的字符串
            String newImagesUrl = String.join(",", imageUrlList);

            // 6. 更新images_url字段
            productDetail.setImagesUrl(newImagesUrl);
            productDetailService.updateById(productDetail);

            log.info("成功更新product_detail的images_url（含缩略图）：productDetailId={}, 新images_url={}", 
                    productDetailId, newImagesUrl);
        } catch (Exception e) {
            log.error("更新product_detail的images_url失败：productDetailId={}, thumbnailUrl={}, originalUrl={}, 错误信息={}", 
                    productDetailId, thumbnailUrl, originalUrl, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 更新product_detail的images_url（使用原图URL）
     * @param productDetailId 产品明细ID
     * @param originalUrl 原图URL
     */
    private void updateProductDetailImagesUrlWithOriginal(String productDetailId, String originalUrl) {
        if (StrUtil.isBlank(productDetailId) || StrUtil.isBlank(originalUrl)) {
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
                imageUrlList = Arrays.stream(currentImagesUrl.split(","))
                        .map(String::trim)
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
            }

            // 3. 将原图URL放在第一位（如果已存在则先移除）
            imageUrlList.remove(originalUrl);
            imageUrlList.add(0, originalUrl);

            // 4. 重新组合成逗号分割的字符串
            String newImagesUrl = String.join(",", imageUrlList);

            // 5. 更新images_url字段
            productDetail.setImagesUrl(newImagesUrl);
            productDetailService.updateById(productDetail);

            log.info("成功更新product_detail的images_url（原图）：productDetailId={}, 新images_url={}", 
                    productDetailId, newImagesUrl);
        } catch (Exception e) {
            log.error("更新product_detail的images_url失败：productDetailId={}, originalUrl={}, 错误信息={}", 
                    productDetailId, originalUrl, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 将缩略图URL拼接到product_detail的images_url的末尾
     * @param productDetailId 产品明细ID
     * @param thumbnailUrl 缩略图URL（如果为null，则不添加）
     */
    private void appendThumbnailUrlToImagesUrl(String productDetailId, String thumbnailUrl) {
        if (StrUtil.isBlank(productDetailId) || StrUtil.isBlank(thumbnailUrl)) {
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
                imageUrlList = Arrays.stream(currentImagesUrl.split(","))
                        .map(String::trim)
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
            }

            // 3. 如果缩略图URL已存在，先移除
            imageUrlList.remove(thumbnailUrl);
            
            // 4. 将缩略图URL添加到列表末尾
            imageUrlList.add(thumbnailUrl);

            // 5. 重新组合成逗号分割的字符串
            String newImagesUrl = String.join(",", imageUrlList);

            // 6. 更新images_url字段
            productDetail.setImagesUrl(newImagesUrl);
            productDetailService.updateById(productDetail);

            log.info("成功将缩略图URL拼接到images_url末尾：productDetailId={}, 缩略图URL={}, 新images_url={}", 
                    productDetailId, thumbnailUrl, newImagesUrl);
        } catch (Exception e) {
            log.error("更新product_detail的images_url失败：productDetailId={}, thumbnailUrl={}, 错误信息={}", 
                    productDetailId, thumbnailUrl, e.getMessage(), e);
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
     * 异步导入批量上传图片
     */
    @Override
    public Boolean importBatchUpload(RefProductImgAttachmentDTO.BatchUploadDTO dto) {
        // 将 zipUrl 赋值到 fileUrl（前端传的是 zipUrl，统一使用 fileUrl）
        if (StrUtil.isNotBlank(dto.getZipUrl()) && StrUtil.isBlank(dto.getFileUrl())) {
            dto.setFileUrl(dto.getZipUrl());
        }
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        // 直接传递 BatchUploadDTO 作为参数，saveImportTask 的 params 是 Object 类型，可以接收任何对象
        // taskId 会在 FileTaskContext 中设置，这里不需要设置
        downloadTaskFeign.saveImportTask("批量上传图片", IMPORT_PLM_PRODUCT_IMG_ATTACHMENT.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 批量上传图片
     * @param dto 批量上传参数
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 300000)
    @Transactional(rollbackFor = Exception.class)
    public void batchUpload(RefProductImgAttachmentDTO.BatchUploadDTO dto) {
        // 设置操作人
        if (StringUtils.isNotBlank(dto.getUserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getUserId());
            if (Objects.nonNull(findUserDTO)) {
                LoginUser user = new LoginUser();
                user.setUid(findUserDTO.getUserId());
                user.setUserName(findUserDTO.getUserName());
                user.setRealName(findUserDTO.getRealName());
                user.setUserAccount(findUserDTO.getMobile());
                user.setMobile(findUserDTO.getMobile());
                UserContext.setLoginUser(user);
            }
        }
        
        log.info("开始批量上传图片，fileUrl={}, categoryId={}", dto.getFileUrl(), dto.getCategoryId());
        
        // 1. 检查ZIP文件大小（限制300M）
        try {
            byte[] zipBytes = fileFeign.downloadFile(dto.getFileUrl());
            if (zipBytes == null || zipBytes.length == 0) {
                throw new ServiceException(ApiError.FILE_ZIP_NOT_FOUND, dto.getFileUrl());
            }
            double zipSizeMB = zipBytes.length / (1024.0 * 1024.0);
            zipSizeMB = Math.round(zipSizeMB * 100.0) / 100.0;
            if (zipSizeMB > 300) {
                throw new ServiceException(ApiError.FILE_SIZE_EXCEEDS_LIMIT, 300);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("检查ZIP文件大小失败：{}", dto.getFileUrl(), e);
            throw new ServiceException(ApiError.FILE_CHECK_SIZE_FAILED, e.getMessage());
        }
        
        // 2. 调用文件服务解压缩ZIP文件并上传所有文件，获取文件信息列表（不传输文件本体）
        List<FileDTO.ExtractedFileInfo> extractedFiles;
        try {
            extractedFiles = fileFeign.unzipAndUploadFiles(dto.getFileUrl());
        } catch (Exception e) {
            log.error("解压缩ZIP文件失败：{}", dto.getFileUrl(), e);
            throw new ServiceException(ApiError.FILE_ZIP_EXTRACT_FAILED, e.getMessage());
        }
        
        if (CollUtil.isEmpty(extractedFiles)) {
            throw new ServiceException(ApiError.FILE_ZIP_EMPTY);
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
            // 如果是主图分类，确保只有一个主图：将之前的主图移出主图分类
            String uncategorizedCategoryId = getUncategorizedCategoryId();
            boolean isMainImageCategory = PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(dto.getCategoryId());
            if (isMainImageCategory) {
                List<RefProductImgAttachmentEntity> oldMainImages = super.lambdaQuery()
                        .eq(RefProductImgAttachmentEntity::getProductDetailId, productDetail.getId())
                        .eq(RefProductImgAttachmentEntity::getCategoryId, PRODUCT_MAIN_IMAGE_CATEGORY_ID)
                        .list();
                
                for (RefProductImgAttachmentEntity oldMainImage : oldMainImages) {
                    oldMainImage.setCategoryId(uncategorizedCategoryId);
                    super.updateById(oldMainImage);
                    log.info("批量上传：将旧主图移出主图分类，归类到未分类，refId={}", oldMainImage.getId());
                }
            }
            
            boolean isFirstFile = true;
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
                    
                    // 确定分类：如果是主图分类，只有第一个文件是主图，其他为未分类
                    String categoryId = dto.getCategoryId();
                    if (isMainImageCategory && !isFirstFile) {
                        categoryId = uncategorizedCategoryId;
                    }
                    
                    // 创建图片分类附件关联记录
                    RefProductImgAttachmentEntity refEntity = new RefProductImgAttachmentEntity();
                    refEntity.setCategoryId(categoryId);
                    refEntity.setProductDetailId(productDetail.getId());
                    refEntity.setSkuNo(skuNo);
                    refEntity.setAttachmentId(attachmentEntity.getId());
                    super.save(refEntity);
                    
                    // 如果分类是产品主图，自动生成产品缩略图
                    if (PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(categoryId)) {
                        generateThumbnail(refEntity);
                    }
                    
                    isFirstFile = false;
                    
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
     * 删除关联信息、附件表和文件，并同步更新product_detail的images_url
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
        
        String productDetailId = refEntity.getProductDetailId();
        boolean isMainImage = PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(refEntity.getCategoryId());
        
        // 2. 查询附件记录（原图和缩略图）
        String attachmentId = refEntity.getAttachmentId();
        String thumbnailAttachmentId = refEntity.getThumbnailAttachmentId();
        String originalUrl = null;
        String thumbnailUrl = null;
        String fileUrl = null;
        
        if (StrUtil.isNotBlank(attachmentId)) {
            PlmAttachmentEntity attachmentEntity = plmAttachmentService.getById(attachmentId);
            if (attachmentEntity != null) {
                originalUrl = attachmentEntity.getAttachUrl();
                fileUrl = originalUrl;
            }
        }
        
        if (StrUtil.isNotBlank(thumbnailAttachmentId)) {
            PlmAttachmentEntity thumbnailAttachmentEntity = plmAttachmentService.getById(thumbnailAttachmentId);
            if (thumbnailAttachmentEntity != null) {
                thumbnailUrl = thumbnailAttachmentEntity.getAttachUrl();
            }
        }
        
        // 3. 删除关联记录
        boolean deleted = super.removeById(id);
        if (!deleted) {
            throw new ServiceException(ApiError.BILL_DELETE_FAILED);
        }
        
        // 4. 删除附件记录
        if (StrUtil.isNotBlank(attachmentId)) {
            boolean attachmentDeleted = plmAttachmentService.removeById(attachmentId);
            if (!attachmentDeleted) {
                log.warn("删除附件记录失败，attachmentId={}", attachmentId);
            }
        }
        
        if (StrUtil.isNotBlank(thumbnailAttachmentId) && !thumbnailAttachmentId.equals(attachmentId)) {
            boolean thumbnailDeleted = plmAttachmentService.removeById(thumbnailAttachmentId);
            if (!thumbnailDeleted) {
                log.warn("删除缩略图附件记录失败，thumbnailAttachmentId={}", thumbnailAttachmentId);
            }
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
        
        // 6. 同步更新product_detail的images_url
        if (StrUtil.isNotBlank(productDetailId)) {
            updateProductDetailImagesUrlAfterDelete(productDetailId, originalUrl, thumbnailUrl, isMainImage);
        }
        
        // 使用SKU编号作为code，如果没有则使用ID
        String code = StrUtil.isNotBlank(refEntity.getSkuNo()) ? refEntity.getSkuNo() : id;
        log.info("删除图片分类附件关联记录成功，id={}", id);
        return BatchResultDTO.success(id, code, "删除成功");
    }
    
    /**
     * 删除图片后更新product_detail的images_url
     * @param productDetailId 产品明细ID
     * @param originalUrl 原图URL
     * @param thumbnailUrl 缩略图URL
     * @param isMainImage 是否为主图
     */
    private void updateProductDetailImagesUrlAfterDelete(String productDetailId, String originalUrl, String thumbnailUrl, boolean isMainImage) {
        try {
            // 1. 获取product_detail记录
            ProductDetailEntity productDetail = productDetailService.getById(productDetailId);
            if (productDetail == null) {
                log.warn("更新product_detail的images_url失败：产品明细不存在，productDetailId={}", productDetailId);
                return;
            }
            
            String oldImagesUrl = productDetail.getImagesUrl();
            if (StrUtil.isBlank(oldImagesUrl)) {
                log.info("product_detail的images_url为空，无需更新，productDetailId={}", productDetailId);
                return;
            }
            
            // 2. 解析当前的images_url列表
            List<String> imageUrlList = Arrays.stream(oldImagesUrl.split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
            
            if (CollUtil.isEmpty(imageUrlList)) {
                log.info("product_detail的images_url列表为空，无需更新，productDetailId={}", productDetailId);
                return;
            }
            
            // 3. 判断删除的是否是主图（第一张）
            boolean deletedIsMainImage = false;
            if (isMainImage && CollUtil.isNotEmpty(imageUrlList)) {
                // 检查第一张是否是当前删除的缩略图URL或原图URL
                String firstUrl = imageUrlList.get(0);
                if (firstUrl.equals(thumbnailUrl) || firstUrl.equals(originalUrl)) {
                    deletedIsMainImage = true;
                }
            }
            
            // 4. 从列表中删除对应的URL（可能是缩略图URL或原图URL）
            boolean removed = imageUrlList.remove(thumbnailUrl);
            if (!removed) {
                removed = imageUrlList.remove(originalUrl);
            }
            
            if (!removed) {
                log.warn("未在product_detail的images_url中找到要删除的URL，productDetailId={}, originalUrl={}, thumbnailUrl={}", 
                        productDetailId, originalUrl, thumbnailUrl);
            }
            
            // 5. 如果删除的是主图，需要把第二张移到第一位，并更新对应的ref_product_img_attachment分类
            if (deletedIsMainImage && CollUtil.isNotEmpty(imageUrlList)) {
                // 第二张图片的URL（现在会成为第一张）
                String newMainImageUrl = imageUrlList.get(0);
                
                // 查找对应的ref_product_img_attachment记录
                // 先通过缩略图URL查找，如果找不到，再通过原图URL查找
                RefProductImgAttachmentEntity newMainImageRef = null;
                
                // 查询所有未删除的ref记录
                List<RefProductImgAttachmentEntity> refList = super.lambdaQuery()
                        .eq(RefProductImgAttachmentEntity::getProductDetailId, productDetailId)
                        .eq(RefProductImgAttachmentEntity::getIsDeleted, false)
                        .list();
                
                // 查询所有相关的attachment记录
                Set<String> refAttachmentIds = refList.stream()
                        .flatMap(ref -> {
                            Set<String> ids = new HashSet<>();
                            if (StrUtil.isNotBlank(ref.getAttachmentId())) {
                                ids.add(ref.getAttachmentId());
                            }
                            if (StrUtil.isNotBlank(ref.getThumbnailAttachmentId())) {
                                ids.add(ref.getThumbnailAttachmentId());
                            }
                            return ids.stream();
                        })
                        .collect(Collectors.toSet());
                
                if (CollUtil.isNotEmpty(refAttachmentIds)) {
                    List<PlmAttachmentEntity> attachments = plmAttachmentService.listByIds(new ArrayList<>(refAttachmentIds));
                    Map<String, PlmAttachmentEntity> attachmentMap = attachments.stream()
                            .collect(Collectors.toMap(PlmAttachmentEntity::getId, att -> att));
                    
                    // 查找newMainImageUrl对应的ref记录
                    for (RefProductImgAttachmentEntity ref : refList) {
                        PlmAttachmentEntity refOriginalAttachment = attachmentMap.get(ref.getAttachmentId());
                        String refThumbnailAttachmentId = StrUtil.isNotBlank(ref.getThumbnailAttachmentId()) 
                                ? ref.getThumbnailAttachmentId() 
                                : ref.getAttachmentId();
                        PlmAttachmentEntity refThumbnailAttachment = attachmentMap.get(refThumbnailAttachmentId);
                        
                        if (refThumbnailAttachment != null && newMainImageUrl.equals(refThumbnailAttachment.getAttachUrl())) {
                            newMainImageRef = ref;
                            break;
                        } else if (refOriginalAttachment != null && newMainImageUrl.equals(refOriginalAttachment.getAttachUrl())) {
                            newMainImageRef = ref;
                            break;
                        }
                    }
                }
                
                // 更新新主图的分类
                if (newMainImageRef != null) {
                    if (!PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(newMainImageRef.getCategoryId())) {
                        newMainImageRef.setCategoryId(PRODUCT_MAIN_IMAGE_CATEGORY_ID);
                        super.updateById(newMainImageRef);
                        log.info("更新新主图的分类，refId={}, url={}", newMainImageRef.getId(), newMainImageUrl);
                    }
                } else {
                    log.warn("未找到新主图对应的ref_product_img_attachment记录，productDetailId={}, newMainImageUrl={}", 
                            productDetailId, newMainImageUrl);
                }
            }
            
            // 6. 更新product_detail的images_url
            String newImagesUrl = "";
            if (CollUtil.isNotEmpty(imageUrlList)) {
                newImagesUrl = String.join(",", imageUrlList);
            }
            
            boolean updateResult = productDetailService.lambdaUpdate()
                    .eq(ProductDetailEntity::getId, productDetailId)
                    .set(ProductDetailEntity::getImagesUrl, newImagesUrl)
                    .update();
            
            if (updateResult) {
                // 记录操作日志
                operateLogService.addSysLogBySave(
                        "sku图片由[" + oldImagesUrl + "]变更为[" + newImagesUrl + "]",
                        SKUCLASSPATH,
                        productDetailId,
                        productDetail.getProductId()
                );
                log.info("成功更新product_detail的images_url，productDetailId={}, 旧images_url={}, 新images_url={}", 
                        productDetailId, oldImagesUrl, newImagesUrl);
            } else {
                log.warn("更新product_detail的images_url失败，productDetailId={}", productDetailId);
            }
        } catch (Exception e) {
            log.error("更新product_detail的images_url时出错，productDetailId={}, originalUrl={}, thumbnailUrl={}, 错误信息={}", 
                    productDetailId, originalUrl, thumbnailUrl, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
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
            throw new ServiceException(ApiError.PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_ALL);
        }
        if (PRODUCT_MAIN_IMAGE_CATEGORY_ID.equals(dto.getCategoryId())) {
            throw new ServiceException(ApiError.PRODUCT_IMG_CATEGORY_MOVE_FORBIDDEN_MAIN);
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
            throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
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
            throw new ServiceException(ApiError.PRODUCT_IMG_DOWNLOAD_MIN_REQUIRED);
        }
        
        // 2. 校验最多50张图片
        if (dto.getIds().size() > 50) {
            throw new ServiceException(ApiError.PRODUCT_IMG_DOWNLOAD_MAX_LIMIT);
        }
        
        // 3. 创建异步下载任务（传递ids，由handler处理）
        downloadTaskFeign.saveDownloadTask("批量下载图片", EXPORT_PLM_PRODUCT_IMAGES.getCode(), dto);
        
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
            // 递归查询所有分类及其父分类
            Set<String> allCategoryIds = new HashSet<>(categoryIds);
            Set<String> processedIds = new HashSet<>();
            
            // 不断查询父分类，直到没有新的父分类为止
            // 设置最大循环次数，防止数据异常导致死循环（一般分类层级不会超过10层）
            int maxLoopCount = 20;
            int loopCount = 0;
            
            while (loopCount < maxLoopCount) {
                loopCount++;
                
                // 找出还未处理的分类ID
                List<String> toQueryIds = allCategoryIds.stream()
                        .filter(id -> !processedIds.contains(id) && !ALL_CATEGORY_ID.equals(id))
                        .collect(Collectors.toList());
                
                if (CollUtil.isEmpty(toQueryIds)) {
                    break; // 所有分类都已处理
                }
                
                // 批量查询这批分类
                List<ProductImgCategoryEntity> categoryList = productImgCategoryService.listByIds(toQueryIds);
                if (CollUtil.isEmpty(categoryList)) {
                    break; // 没有查询到新的分类，退出循环
                }
                
                // 将查询到的分类添加到 categoryMap
                boolean hasNewParent = false;
                for (ProductImgCategoryEntity category : categoryList) {
                    categoryMap.put(category.getId(), category);
                    processedIds.add(category.getId());
                    
                    // 收集父分类ID
                    String parentId = category.getParentId();
                    if (StrUtil.isNotBlank(parentId) && !ALL_CATEGORY_ID.equals(parentId) && !processedIds.contains(parentId)) {
                        allCategoryIds.add(parentId);
                        hasNewParent = true;
                    }
                }
                
                // 如果本轮没有发现新的父分类，提前退出
                if (!hasNewParent) {
                    break;
                }
            }
            
            // 如果达到最大循环次数，记录警告日志
            if (loopCount >= maxLoopCount) {
                log.warn("批量下载图片-查询分类层级时达到最大循环次数限制，可能存在循环引用，categoryIds={}", categoryIds);
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
            throw new ServiceException(ApiError.PRODUCT_IMG_DOWNLOAD_NOT_FOUND);
        }
        
        // 9. 构建CreateZipDTO并调用文件中心创建ZIP
        FileDTO.CreateZipDTO createZipDTO = FileDTO.CreateZipDTO.builder()
                .folderStructure(folderStructure)
                .fileUrlToNameMap(fileUrlToNameMap)
                .build();
        
        String zipUrl = fileFeign.createZipFromFolderStructure(createZipDTO);
        
        if (StrUtil.isBlank(zipUrl)) {
            throw new ServiceException(ApiError.FILE_ZIP_CREATE_FAILED, "");
        }
        
        log.info("构建产品图片文件夹结构并创建ZIP完成，共{}个文件夹，{}个文件，zipUrl={}", 
                folderStructure.size(), 
                folderStructure.values().stream().mapToInt(List::size).sum(),
                zipUrl);
        return zipUrl;
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

    /**
     * 上传产品主图（对比新增、删除、保留，自动生成缩略图，更新images_url）
     * @param dto 上传产品主图参数（包含skuId和imagesUrls）
     * @author wuhaotian
     * @date: 2025-12-29
     * @return Boolean
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadProductMainImage(RefProductImgAttachmentDTO.UploadProductMainImageDTO dto) {
        log.info("开始上传产品主图，skuId={}, imagesUrls={}", dto.getSkuId(), dto.getImagesUrls());
        
        // 1. 验证SKU是否存在，状态是否允许修改
        ProductDetailEntity productDetailEntity = productDetailService.getById(dto.getSkuId());
        if (ObjectUtil.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
        }
        Integer status = productDetailEntity.getStatus();
        if (status.equals(ProductDetailStatusEnum.APPROVAL_ING.getCode())) {
            throw new ServiceException(ApiError.PRODUCT_UPLOAD_FORBIDDEN_IN_APPROVING);
        }
        
        String oldImagesUrl = productDetailEntity.getImagesUrl();
        List<RefProductImgAttachmentDTO.UploadProductMainImageDTO.ImageInfo> newImagesUrls = dto.getImagesUrls() != null ? dto.getImagesUrls() : new ArrayList<>();
        
        // 构建图片URL到名称的映射（用于后续设置图片名称）
        Map<String, String> urlToNameMap = new HashMap<>();
        for (RefProductImgAttachmentDTO.UploadProductMainImageDTO.ImageInfo imageInfo : newImagesUrls) {
            if (StrUtil.isNotBlank(imageInfo.getImageUrl()) && StrUtil.isNotBlank(imageInfo.getImageName())) {
                urlToNameMap.put(imageInfo.getImageUrl(), imageInfo.getImageName());
            }
        }
        
        // 2. 查询现有的所有关联记录（不限制分类，因为需要处理主图和未分类）
        // 注意：需要查询所有记录（包括已删除的），以便恢复已删除但仍在images_url中的记录
        List<RefProductImgAttachmentEntity> existingRefList = super.lambdaQuery()
                .eq(RefProductImgAttachmentEntity::getProductDetailId, dto.getSkuId())
                .eq(RefProductImgAttachmentEntity::getIsDeleted, false)
                .list();
        
        // 查询所有记录（包括已删除的），用于恢复已删除但仍在images_url中的记录
        List<RefProductImgAttachmentEntity> allRefListIncludingDeleted = super.lambdaQuery()
                .eq(RefProductImgAttachmentEntity::getProductDetailId, dto.getSkuId())
                .list();
        
        // 获取现有的附件ID列表（包括原图和缩略图）- 从未删除的记录中获取
        List<String> existingAttachmentIds = existingRefList.stream()
                .map(RefProductImgAttachmentEntity::getAttachmentId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        
        // 收集所有缩略图attachmentId - 从未删除的记录中获取
        List<String> thumbnailAttachmentIds = existingRefList.stream()
                .map(RefProductImgAttachmentEntity::getThumbnailAttachmentId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        
        // 同时收集已删除记录的attachmentId（包括原图和缩略图），以便查询对应的attachment
        List<String> deletedAttachmentIds = allRefListIncludingDeleted.stream()
                .filter(ref -> Boolean.TRUE.equals(ref.getIsDeleted()))
                .map(RefProductImgAttachmentEntity::getAttachmentId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        
        List<String> deletedThumbnailAttachmentIds = allRefListIncludingDeleted.stream()
                .filter(ref -> Boolean.TRUE.equals(ref.getIsDeleted()))
                .map(RefProductImgAttachmentEntity::getThumbnailAttachmentId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        
        // 合并所有attachmentId（去重），包括已删除记录的
        Set<String> allAttachmentIds = new HashSet<>(existingAttachmentIds);
        allAttachmentIds.addAll(thumbnailAttachmentIds);
        allAttachmentIds.addAll(deletedAttachmentIds);
        allAttachmentIds.addAll(deletedThumbnailAttachmentIds);
        
        // 查询现有的附件记录，构建URL到原图附件的映射（原图URL -> 原图attachment）
        // 同时构建缩略图URL到原图附件的映射（缩略图URL -> 原图attachment）
        Map<String, PlmAttachmentEntity> existingAttachmentMap = new HashMap<>(); // 原图URL -> 原图attachment
        Map<String, PlmAttachmentEntity> thumbnailUrlToOriginalMap = new HashMap<>(); // 缩略图URL -> 原图attachment
        Map<String, RefProductImgAttachmentEntity> urlToRefMap = new HashMap<>(); // 原图URL -> ref记录（只包含未删除的）
        Map<String, RefProductImgAttachmentEntity> thumbnailUrlToDeletedRefMap = new HashMap<>(); // 缩略图URL -> 已删除的ref记录（用于恢复）
        Map<String, RefProductImgAttachmentEntity> originalUrlToDeletedRefMap = new HashMap<>(); // 原图URL -> 已删除的ref记录（用于恢复）
        Map<String, PlmAttachmentEntity> attachmentMap = new HashMap<>(); // attachmentId -> attachment（用于后续恢复已删除记录）
        if (CollUtil.isNotEmpty(allAttachmentIds)) {
            List<PlmAttachmentEntity> allAttachments = plmAttachmentService.listByIds(new ArrayList<>(allAttachmentIds));
            attachmentMap = allAttachments.stream()
                    .collect(Collectors.toMap(PlmAttachmentEntity::getId, att -> att));
            
            // 处理未删除的记录
            for (RefProductImgAttachmentEntity refEntity : existingRefList) {
                PlmAttachmentEntity originalAttachment = attachmentMap.get(refEntity.getAttachmentId());
                if (originalAttachment != null && StrUtil.isNotBlank(originalAttachment.getAttachUrl())) {
                    // 原图URL -> 原图attachment
                    existingAttachmentMap.put(originalAttachment.getAttachUrl(), originalAttachment);
                    urlToRefMap.put(originalAttachment.getAttachUrl(), refEntity);
                    
                    // 如果有缩略图，构建缩略图URL -> 原图attachment的映射
                    String thumbnailAttachmentId = StrUtil.isNotBlank(refEntity.getThumbnailAttachmentId()) 
                            ? refEntity.getThumbnailAttachmentId() 
                            : refEntity.getAttachmentId();
                    PlmAttachmentEntity thumbnailAttachment = attachmentMap.get(thumbnailAttachmentId);
                    if (thumbnailAttachment != null && StrUtil.isNotBlank(thumbnailAttachment.getAttachUrl())) {
                        // 缩略图URL -> 原图attachment（用于通过缩略图URL找到原图）
                        thumbnailUrlToOriginalMap.put(thumbnailAttachment.getAttachUrl(), originalAttachment);
                    }
                }
            }
            
            // 处理已删除的记录，构建映射以便后续恢复
            for (RefProductImgAttachmentEntity refEntity : allRefListIncludingDeleted) {
                if (Boolean.TRUE.equals(refEntity.getIsDeleted())) {
                    PlmAttachmentEntity originalAttachment = attachmentMap.get(refEntity.getAttachmentId());
                    if (originalAttachment != null && StrUtil.isNotBlank(originalAttachment.getAttachUrl())) {
                        // 原图URL -> 已删除的ref记录
                        originalUrlToDeletedRefMap.put(originalAttachment.getAttachUrl(), refEntity);
                        
                        // 如果有缩略图，构建缩略图URL -> 已删除的ref记录的映射（关键修复）
                        String thumbnailAttachmentId = StrUtil.isNotBlank(refEntity.getThumbnailAttachmentId()) 
                                ? refEntity.getThumbnailAttachmentId() 
                                : refEntity.getAttachmentId();
                        PlmAttachmentEntity thumbnailAttachment = attachmentMap.get(thumbnailAttachmentId);
                        if (thumbnailAttachment != null && StrUtil.isNotBlank(thumbnailAttachment.getAttachUrl())) {
                            // 缩略图URL -> 已删除的ref记录（用于通过缩略图URL找到已删除的记录并恢复）
                            thumbnailUrlToDeletedRefMap.put(thumbnailAttachment.getAttachUrl(), refEntity);
                            // 同时构建缩略图URL -> 原图attachment的映射
                            thumbnailUrlToOriginalMap.put(thumbnailAttachment.getAttachUrl(), originalAttachment);
                        }
                    }
                }
            }
        }
        
        // 2.1 兼容旧数据：从product_detail.images_url中获取URL，如果不在新系统中，需要创建记录
        List<String> oldUrlsFromImagesUrl = new ArrayList<>();
        if (StrUtil.isNotBlank(oldImagesUrl)) {
            oldUrlsFromImagesUrl = Arrays.stream(oldImagesUrl.split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        }
        
        // 2.2 处理旧数据：从product_detail.images_url中获取URL，恢复已删除的记录或创建新记录
        List<String> urlsToGetSize = new ArrayList<>();
        List<String> oldUrlsToCreate = new ArrayList<>(); // 记录需要创建新记录的URL，用于后续更新文件大小
        for (String oldUrl : oldUrlsFromImagesUrl) {
            // 先检查是否是缩略图URL，如果是，转换为原图URL
            String originalUrl = oldUrl;
            if (thumbnailUrlToOriginalMap.containsKey(oldUrl)) {
                originalUrl = thumbnailUrlToOriginalMap.get(oldUrl).getAttachUrl();
            }
            
            // 如果原图URL已经在现有映射中（未删除），跳过
            if (existingAttachmentMap.containsKey(originalUrl)) {
                continue;
            }
            
            // 检查是否是已删除的记录（通过缩略图URL或原图URL）
            RefProductImgAttachmentEntity deletedRef = thumbnailUrlToDeletedRefMap.get(oldUrl);
            if (deletedRef == null) {
                deletedRef = originalUrlToDeletedRefMap.get(originalUrl);
            }
            
            if (deletedRef != null) {
                // 从已查询的attachmentMap中获取对应的原图attachment
                PlmAttachmentEntity originalAttachment = attachmentMap.get(deletedRef.getAttachmentId());
                
                if (originalAttachment != null) {
                    // 恢复已删除的记录
                    deletedRef.setIsDeleted(false);
                    super.updateById(deletedRef);
                    
                    // 添加到映射中（使用原图URL作为key）
                    existingAttachmentMap.put(originalUrl, originalAttachment);
                    urlToRefMap.put(originalUrl, deletedRef);
                    log.info("恢复已删除的ref_product_img_attachment记录，id={}, 缩略图url={}, 原图url={}", 
                            deletedRef.getId(), oldUrl, originalUrl);
                } else {
                    log.warn("无法找到已删除记录对应的attachment，refId={}, attachmentId={}", 
                            deletedRef.getId(), deletedRef.getAttachmentId());
                }
                continue;
            }
            
            // 这个URL在旧系统中，但不在新系统中，需要创建记录
            log.info("发现旧数据URL，需要创建记录：{}", oldUrl);
            oldUrlsToCreate.add(oldUrl); // 记录需要创建新记录的URL
            urlsToGetSize.add(oldUrl);
            
            // 先检查这个URL是否是缩略图URL，如果是，应该使用对应的原图attachment
            PlmAttachmentEntity attachmentEntity = null;
            if (thumbnailUrlToOriginalMap.containsKey(oldUrl)) {
                // 如果是缩略图URL，使用对应的原图attachment
                attachmentEntity = thumbnailUrlToOriginalMap.get(oldUrl);
                log.info("识别到旧数据URL是缩略图URL，使用对应的原图attachment，url={}, attachmentId={}", oldUrl, attachmentEntity.getId());
            } else {
                // 查找或创建attachment记录
                attachmentEntity = plmAttachmentService.lambdaQuery()
                        .eq(PlmAttachmentEntity::getAttachUrl, oldUrl)
                        .eq(PlmAttachmentEntity::getBusinessId, dto.getSkuId())
                        .last("LIMIT 1")
                        .one();
            }
            
            if (attachmentEntity == null) {
                // 创建新的attachment记录（文件大小稍后批量获取后设置）
                attachmentEntity = new PlmAttachmentEntity();
                attachmentEntity.setAttachUrl(oldUrl);
                
                // 优先使用传入的名称，如果传入的名称为空，从URL提取文件名
                String fileName = urlToNameMap.get(oldUrl);
                if (StrUtil.isBlank(fileName)) {
                    fileName = oldUrl;
                    int lastSlash = oldUrl.lastIndexOf('/');
                    if (lastSlash >= 0 && lastSlash < oldUrl.length() - 1) {
                        fileName = oldUrl.substring(lastSlash + 1);
                    }
                }
                attachmentEntity.setAttachName(fileName);
                attachmentEntity.setAttachSize(BigDecimal.ZERO); // 稍后批量获取后更新
                attachmentEntity.setType("product_detail");
                attachmentEntity.setBusinessId(dto.getSkuId());
                plmAttachmentService.save(attachmentEntity);
                log.info("为旧数据创建attachment记录，id={}, url={}, 名称={}", attachmentEntity.getId(), oldUrl, fileName);
            } else {
                // 如果attachment已存在，检查是否需要更新名称
                String newName = urlToNameMap.get(oldUrl);
                if (StrUtil.isNotBlank(newName) && !newName.equals(attachmentEntity.getAttachName())) {
                    // 如果传入了新名称，更新名称
                    plmAttachmentService.lambdaUpdate()
                            .eq(PlmAttachmentEntity::getId, attachmentEntity.getId())
                            .set(PlmAttachmentEntity::getAttachName, newName)
                            .update();
                    log.info("更新attachment名称，id={}, url={}, 旧名称={}, 新名称={}", 
                            attachmentEntity.getId(), oldUrl, attachmentEntity.getAttachName(), newName);
                    attachmentEntity.setAttachName(newName);
                } else {
                    // 保留原有名称
                    log.info("找到已存在的attachment记录，保留原有名称，id={}, url={}, attachName={}", 
                            attachmentEntity.getId(), oldUrl, attachmentEntity.getAttachName());
                }
            }
            
            // 检查是否已有ref_product_img_attachment记录（不限制分类，包括已删除的）
            RefProductImgAttachmentEntity existingRef = urlToRefMap.get(originalUrl);
            if (existingRef == null) {
                existingRef = super.lambdaQuery()
                        .eq(RefProductImgAttachmentEntity::getProductDetailId, dto.getSkuId())
                        .eq(RefProductImgAttachmentEntity::getAttachmentId, attachmentEntity.getId())
                        .last("LIMIT 1")
                        .one();
            }
            
            if (existingRef == null) {
                // 确定分类：旧数据中第一个URL为主图，其他为未分类
                String uncategorizedCategoryId = getUncategorizedCategoryId();
                String firstOldUrl = CollUtil.isNotEmpty(oldUrlsFromImagesUrl) ? oldUrlsFromImagesUrl.get(0) : null;
                // 注意：这里应该使用originalUrl而不是oldUrl来判断是否是第一个
                String firstOriginalUrl = null;
                if (CollUtil.isNotEmpty(oldUrlsFromImagesUrl)) {
                    String firstUrl = oldUrlsFromImagesUrl.get(0);
                    if (thumbnailUrlToOriginalMap.containsKey(firstUrl)) {
                        firstOriginalUrl = thumbnailUrlToOriginalMap.get(firstUrl).getAttachUrl();
                    } else {
                        firstOriginalUrl = firstUrl;
                    }
                }
                String categoryId = originalUrl.equals(firstOriginalUrl) ? PRODUCT_MAIN_IMAGE_CATEGORY_ID : uncategorizedCategoryId;
                
                // 创建ref_product_img_attachment记录（但不调用add方法，避免重复生成缩略图）
                RefProductImgAttachmentEntity refEntity = new RefProductImgAttachmentEntity();
                refEntity.setCategoryId(categoryId);
                refEntity.setProductDetailId(dto.getSkuId());
                refEntity.setSkuNo(productDetailEntity.getSkuNo());
                refEntity.setAttachmentId(attachmentEntity.getId());
                super.save(refEntity);
                
                // 添加到映射中（使用原图URL作为key）
                existingAttachmentMap.put(originalUrl, attachmentEntity);
                urlToRefMap.put(originalUrl, refEntity);
                log.info("为旧数据创建ref_product_img_attachment记录，id={}, url={}, categoryId={}", refEntity.getId(), originalUrl, categoryId);
            } else {
                // 如果已存在，添加到映射中（使用原图URL作为key）
                existingAttachmentMap.put(originalUrl, attachmentEntity);
                urlToRefMap.put(originalUrl, existingRef);
            }
        }
        
        // 3. 将传入的URL转换为原图URL（如果传入的是缩略图URL，找到对应的原图URL）
        // 构建传入URL到原图URL的映射，同时保持原始顺序
        Map<String, String> inputUrlToOriginalUrlMap = new HashMap<>();
        List<String> normalizedNewImagesUrls = new ArrayList<>(); // 保持顺序，用于确定主图
        for (RefProductImgAttachmentDTO.UploadProductMainImageDTO.ImageInfo imageInfo : newImagesUrls) {
            String inputUrl = imageInfo.getImageUrl();
            // 先检查是否是原图URL
            if (existingAttachmentMap.containsKey(inputUrl)) {
                inputUrlToOriginalUrlMap.put(inputUrl, inputUrl);
                normalizedNewImagesUrls.add(inputUrl);
            } else if (thumbnailUrlToOriginalMap.containsKey(inputUrl)) {
                // 如果是缩略图URL，找到对应的原图URL
                PlmAttachmentEntity originalAttachment = thumbnailUrlToOriginalMap.get(inputUrl);
                String originalUrl = originalAttachment.getAttachUrl();
                inputUrlToOriginalUrlMap.put(inputUrl, originalUrl);
                normalizedNewImagesUrls.add(originalUrl);
                log.info("识别到缩略图URL，转换为原图URL：{} -> {}", inputUrl, originalUrl);
            } else {
                // 既不是原图URL也不是缩略图URL，按原图URL处理（可能是新上传的）
                inputUrlToOriginalUrlMap.put(inputUrl, inputUrl);
                normalizedNewImagesUrls.add(inputUrl);
            }
        }
        
        // 3.1 对比新旧图片URL列表，找出要保留、要删除、要新增的（使用原图URL）
        Set<String> existingUrls = new HashSet<>(existingAttachmentMap.keySet());
        Set<String> newUrls = new HashSet<>(normalizedNewImagesUrls);
        
        // 要保留的URL（新旧都有的，使用原图URL）
        Set<String> toKeepUrls = new HashSet<>(existingUrls);
        toKeepUrls.retainAll(newUrls);
        
        // 要删除的URL（旧有但新没有的，使用原图URL）
        Set<String> toDeleteUrls = new HashSet<>(existingUrls);
        toDeleteUrls.removeAll(newUrls);
        
        // 要新增的URL（新有但旧没有的，使用原图URL）
        Set<String> toAddUrls = new HashSet<>(newUrls);
        toAddUrls.removeAll(existingUrls);
        
        // 重要：主图应该是传入列表的第一个（imagesUrls.get(0)），而不是normalizedNewImagesUrls的第一个
        // 因为normalizedNewImagesUrls可能因为URL转换而改变顺序
        String firstImageUrl = null;
        if (CollUtil.isNotEmpty(newImagesUrls)) {
            String firstInputUrl = newImagesUrls.get(0).getImageUrl();
            // 将第一个传入URL转换为原图URL
            firstImageUrl = inputUrlToOriginalUrlMap.getOrDefault(firstInputUrl, firstInputUrl);
            log.info("确定主图URL：传入的第一个URL={}, 转换后的原图URL={}", firstInputUrl, firstImageUrl);
        }
        
        // 将新增的URL也加入到需要获取文件大小的列表中
        urlsToGetSize.addAll(toAddUrls);
        
        log.info("图片对比结果：保留{}个，删除{}个，新增{}个", toKeepUrls.size(), toDeleteUrls.size(), toAddUrls.size());
        
        // 3.1 批量获取文件大小
        Map<String, Long> fileSizeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(urlsToGetSize)) {
            try {
                List<FileDTO.FileSizeInfo> fileSizeInfoList = fileFeign.getBatchFileSize(new ArrayList<>(urlsToGetSize));
                if (CollUtil.isNotEmpty(fileSizeInfoList)) {
                    for (FileDTO.FileSizeInfo fileSizeInfo : fileSizeInfoList) {
                        if (fileSizeInfo.getFileSize() != null) {
                            fileSizeMap.put(fileSizeInfo.getFileUrl(), fileSizeInfo.getFileSize());
                        }
                    }
                }
                log.info("批量获取文件大小完成，共{}个文件", fileSizeMap.size());
            } catch (Exception e) {
                log.warn("批量获取文件大小失败，将使用默认值0，错误信息：{}", e.getMessage());
            }
        }
        
        // 4. 删除需要删除的图片
        if (CollUtil.isNotEmpty(toDeleteUrls)) {
            List<String> refIdsToDelete = new ArrayList<>();
            List<String> fileUrlsToDelete = new ArrayList<>();
            for (String url : toDeleteUrls) {
                RefProductImgAttachmentEntity refEntity = urlToRefMap.get(url);
                if (refEntity != null) {
                    refIdsToDelete.add(refEntity.getId());
                }
                fileUrlsToDelete.add(url);
            }
            
            // 删除关联记录
            if (CollUtil.isNotEmpty(refIdsToDelete)) {
                super.removeByIds(refIdsToDelete);
            }
            
            // 删除附件记录
            if (CollUtil.isNotEmpty(fileUrlsToDelete)) {
                plmAttachmentService.lambdaUpdate()
                        .in(PlmAttachmentEntity::getAttachUrl, fileUrlsToDelete)
                        .eq(PlmAttachmentEntity::getBusinessId, dto.getSkuId())
                        .set(PlmAttachmentEntity::getIsDeleted, true)
                        .update();
                
                // 删除文件
                fileFeign.deleteBatchFile(fileUrlsToDelete);
            }
        }
        
        // 5. 更新旧数据的文件大小（批量获取后更新）
        // 注意：只更新文件大小，不修改attachName
        if (CollUtil.isNotEmpty(oldUrlsToCreate)) {
            for (String oldUrl : oldUrlsToCreate) {
                // 先检查是否是缩略图URL，如果是，转换为原图URL
                String originalUrl = oldUrl;
                if (thumbnailUrlToOriginalMap.containsKey(oldUrl)) {
                    originalUrl = thumbnailUrlToOriginalMap.get(oldUrl).getAttachUrl();
                }
                
                PlmAttachmentEntity attachmentEntity = existingAttachmentMap.get(originalUrl);
                if (attachmentEntity != null && attachmentEntity.getAttachSize().compareTo(BigDecimal.ZERO) == 0) {
                    // 尝试从fileSizeMap中获取文件大小（可能使用原图URL或缩略图URL作为key）
                    Long fileSizeBytes = fileSizeMap.get(oldUrl);
                    if (fileSizeBytes == null) {
                        fileSizeBytes = fileSizeMap.get(originalUrl);
                    }
                    if (fileSizeBytes != null && fileSizeBytes > 0) {
                        BigDecimal fileSizeMB = BigDecimal.valueOf(fileSizeBytes)
                                .divide(BigDecimal.valueOf(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP);
                        // 只更新文件大小，不修改attachName
                        plmAttachmentService.lambdaUpdate()
                                .eq(PlmAttachmentEntity::getId, attachmentEntity.getId())
                                .set(PlmAttachmentEntity::getAttachSize, fileSizeMB)
                                .update();
                        log.info("更新旧数据文件大小，id={}, url={}, size={}MB, attachName保持不变={}", 
                                attachmentEntity.getId(), oldUrl, fileSizeMB, attachmentEntity.getAttachName());
                    }
                }
            }
        }
        
        // 6. 确保只有一个主图：第一个图片为主图，其他图片归类到"未分类"
        String uncategorizedCategoryId = getUncategorizedCategoryId();
        
        // 6.1 主图URL已在步骤3中确定（使用传入列表的第一个）
        
        // 6.2 将之前的主图（如果不在新列表中）移出主图分类，归类到"未分类"
        List<RefProductImgAttachmentEntity> oldMainImages = super.lambdaQuery()
                .eq(RefProductImgAttachmentEntity::getProductDetailId, dto.getSkuId())
                .eq(RefProductImgAttachmentEntity::getCategoryId, PRODUCT_MAIN_IMAGE_CATEGORY_ID)
                .list();
        
        for (RefProductImgAttachmentEntity oldMainImage : oldMainImages) {
            PlmAttachmentEntity oldAttachment = plmAttachmentService.getById(oldMainImage.getAttachmentId());
            if (oldAttachment != null && !normalizedNewImagesUrls.contains(oldAttachment.getAttachUrl())) {
                // 这个主图不在新列表中，移出主图分类，归类到"未分类"
                oldMainImage.setCategoryId(uncategorizedCategoryId);
                super.updateById(oldMainImage);
                log.info("将图片移出主图分类，归类到未分类，refId={}, url={}", oldMainImage.getId(), oldAttachment.getAttachUrl());
            }
        }
        
        // 6.3 新增需要新增的图片（直接创建记录，生成缩略图，但不更新product_detail）
        // 构建原图URL到缩略图URL的映射
        Map<String, String> originalToThumbnailMap = new HashMap<>();
        if (CollUtil.isNotEmpty(toAddUrls)) {
            for (String imageUrl : toAddUrls) {
                // 先查找或创建attachment（通过原图URL查找）
                PlmAttachmentEntity attachmentEntity = plmAttachmentService.lambdaQuery()
                        .eq(PlmAttachmentEntity::getAttachUrl, imageUrl)
                        .eq(PlmAttachmentEntity::getBusinessId, dto.getSkuId())
                        .last("LIMIT 1")
                        .one();
                
                if (attachmentEntity == null) {
                    // 如果不存在，创建新的attachment
                    attachmentEntity = new PlmAttachmentEntity();
                    attachmentEntity.setAttachUrl(imageUrl);
                    
                    // 优先使用传入的名称，如果传入的名称为空，从URL提取文件名
                    String fileName = urlToNameMap.get(imageUrl);
                    if (StrUtil.isBlank(fileName)) {
                        fileName = imageUrl;
                        int lastSlash = imageUrl.lastIndexOf('/');
                        if (lastSlash >= 0 && lastSlash < imageUrl.length() - 1) {
                            fileName = imageUrl.substring(lastSlash + 1);
                        }
                    }
                    attachmentEntity.setAttachName(fileName);
                    
                    // 使用批量获取的文件大小
                    Long fileSizeBytes = fileSizeMap.get(imageUrl);
                    if (fileSizeBytes != null && fileSizeBytes > 0) {
                        BigDecimal fileSizeMB = BigDecimal.valueOf(fileSizeBytes)
                                .divide(BigDecimal.valueOf(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP);
                        attachmentEntity.setAttachSize(fileSizeMB);
                    } else {
                        attachmentEntity.setAttachSize(BigDecimal.ZERO);
                    }
                    
                    attachmentEntity.setType("product_detail");
                    attachmentEntity.setBusinessId(dto.getSkuId());
                    plmAttachmentService.save(attachmentEntity);
                    log.info("为新图片创建attachment记录，id={}, url={}, fileName={}", attachmentEntity.getId(), imageUrl, fileName);
                } else {
                    // 如果attachment已存在，检查是否需要更新名称
                    String newName = urlToNameMap.get(imageUrl);
                    if (StrUtil.isNotBlank(newName) && !newName.equals(attachmentEntity.getAttachName())) {
                        // 如果传入了新名称，更新名称
                        plmAttachmentService.lambdaUpdate()
                                .eq(PlmAttachmentEntity::getId, attachmentEntity.getId())
                                .set(PlmAttachmentEntity::getAttachName, newName)
                                .update();
                        log.info("更新attachment名称，id={}, url={}, 旧名称={}, 新名称={}", 
                                attachmentEntity.getId(), imageUrl, attachmentEntity.getAttachName(), newName);
                        attachmentEntity.setAttachName(newName);
                    } else {
                        // 保留原有名称
                        log.info("找到已存在的attachment记录，保留原有名称，id={}, url={}, attachName={}", 
                                attachmentEntity.getId(), imageUrl, attachmentEntity.getAttachName());
                    }
                }
                
                // 确定分类：第一个图片为主图，其他为未分类（使用原图URL判断）
                String categoryId = imageUrl.equals(firstImageUrl) ? PRODUCT_MAIN_IMAGE_CATEGORY_ID : uncategorizedCategoryId;
                
                // 检查是否已有ref_product_img_attachment记录
                RefProductImgAttachmentEntity existingRef = super.lambdaQuery()
                        .eq(RefProductImgAttachmentEntity::getProductDetailId, dto.getSkuId())
                        .eq(RefProductImgAttachmentEntity::getAttachmentId, attachmentEntity.getId())
                        .last("LIMIT 1")
                        .one();
                
                if (existingRef == null) {
                    // 直接创建ref_product_img_attachment记录（不调用add方法）
                    RefProductImgAttachmentEntity refEntity = new RefProductImgAttachmentEntity();
                    refEntity.setCategoryId(categoryId);
                    refEntity.setProductDetailId(dto.getSkuId());
                    refEntity.setSkuNo(productDetailEntity.getSkuNo());
                    refEntity.setAttachmentId(attachmentEntity.getId());
                    super.save(refEntity);
                    
                    // 生成缩略图（不更新product_detail）
                    String thumbnailUrl = generateThumbnailWithoutUpdate(attachmentEntity.getId(), dto.getSkuId(), productDetailEntity.getSkuNo());
                    // 保存原图URL到缩略图URL的映射
                    if (StrUtil.isNotBlank(thumbnailUrl)) {
                        originalToThumbnailMap.put(imageUrl, thumbnailUrl);
                    }
                    
                    // 添加到映射中
                    existingAttachmentMap.put(imageUrl, attachmentEntity);
                    urlToRefMap.put(imageUrl, refEntity);
                    log.info("为新图片创建ref_product_img_attachment记录，id={}, url={}, categoryId={}", refEntity.getId(), imageUrl, categoryId);
                } else {
                    // 如果已存在，更新分类
                    if (!categoryId.equals(existingRef.getCategoryId())) {
                        existingRef.setCategoryId(categoryId);
                        super.updateById(existingRef);
                        log.info("更新图片分类，refId={}, url={}, 新分类={}", existingRef.getId(), imageUrl, categoryId);
                    }
                    
                    // 添加到映射中
                    existingAttachmentMap.put(imageUrl, attachmentEntity);
                    urlToRefMap.put(imageUrl, existingRef);
                }
            }
        }
        
        // 6.4 更新保留的图片分类：确保只有第一个是主图，其他都是未分类
        if (CollUtil.isNotEmpty(toKeepUrls) && StrUtil.isNotBlank(firstImageUrl)) {
            for (String keepUrl : toKeepUrls) {
                RefProductImgAttachmentEntity refEntity = urlToRefMap.get(keepUrl);
                if (refEntity != null) {
                    String shouldBeCategoryId = keepUrl.equals(firstImageUrl) ? PRODUCT_MAIN_IMAGE_CATEGORY_ID : uncategorizedCategoryId;
                    if (!shouldBeCategoryId.equals(refEntity.getCategoryId())) {
                        refEntity.setCategoryId(shouldBeCategoryId);
                        super.updateById(refEntity);
                        log.info("更新图片分类，refId={}, url={}, 新分类={}", refEntity.getId(), keepUrl, shouldBeCategoryId);
                    }
                }
            }
        }
        
        // 7. 构建原图URL到缩略图URL的完整映射（包括保留的URL）
        Map<String, String> allOriginalToThumbnailMap = new HashMap<>(originalToThumbnailMap);
        
        // 查询所有保留的图片记录，从thumbnailAttachmentId获取缩略图
        if (CollUtil.isNotEmpty(toKeepUrls)) {
            // 查询所有保留URL对应的ref记录
            List<String> keepAttachmentIds = toKeepUrls.stream()
                    .map(url -> {
                        PlmAttachmentEntity att = existingAttachmentMap.get(url);
                        return att != null ? att.getId() : null;
                    })
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
            
            if (CollUtil.isNotEmpty(keepAttachmentIds)) {
                List<RefProductImgAttachmentEntity> keepRefs = super.lambdaQuery()
                        .eq(RefProductImgAttachmentEntity::getProductDetailId, dto.getSkuId())
                        .in(RefProductImgAttachmentEntity::getAttachmentId, keepAttachmentIds)
                        .list();
                
                // 收集所有缩略图attachmentId（包括等于原图的）
                Set<String> thumbnailAttachmentIdsSec = new HashSet<>();
                for (RefProductImgAttachmentEntity ref : keepRefs) {
                    if (StrUtil.isNotBlank(ref.getThumbnailAttachmentId())) {
                        thumbnailAttachmentIdsSec.add(ref.getThumbnailAttachmentId());
                    } else {
                        // 如果thumbnailAttachmentId为空，使用原图ID
                        thumbnailAttachmentIdsSec.add(ref.getAttachmentId());
                    }
                }
                
                if (CollUtil.isNotEmpty(thumbnailAttachmentIdsSec)) {
                    List<PlmAttachmentEntity> thumbnailAttachments = plmAttachmentService.listByIds(new ArrayList<>(thumbnailAttachmentIdsSec));
                    Map<String, PlmAttachmentEntity> thumbnailAttachmentMap = thumbnailAttachments.stream()
                            .collect(Collectors.toMap(PlmAttachmentEntity::getId, ta -> ta));
                    
                    // 构建attachmentId到缩略图URL的映射
                    Map<String, String> attachmentIdToThumbnailUrlMap = new HashMap<>();
                    for (RefProductImgAttachmentEntity ref : keepRefs) {
                        String thumbAttachmentId = StrUtil.isNotBlank(ref.getThumbnailAttachmentId()) 
                                ? ref.getThumbnailAttachmentId() 
                                : ref.getAttachmentId();
                        PlmAttachmentEntity thumbAttachment = thumbnailAttachmentMap.get(thumbAttachmentId);
                        if (thumbAttachment != null && StrUtil.isNotBlank(thumbAttachment.getAttachUrl())) {
                            attachmentIdToThumbnailUrlMap.put(ref.getAttachmentId(), thumbAttachment.getAttachUrl());
                        }
                    }
                    
                    // 对于每个保留的原图URL，通过attachmentId找到对应的缩略图
                    for (String originalUrl : toKeepUrls) {
                        PlmAttachmentEntity originalAttachment = existingAttachmentMap.get(originalUrl);
                        if (originalAttachment != null) {
                            String thumbnailUrl = attachmentIdToThumbnailUrlMap.get(originalAttachment.getId());
                            if (StrUtil.isNotBlank(thumbnailUrl)) {
                                allOriginalToThumbnailMap.put(originalUrl, thumbnailUrl);
                            }
                        }
                    }
                }
            }
        }
        
        // 8. 更新product_detail的images_url（用缩略图URL替换原图URL）
        // 重要：保持传入的顺序，而不是使用Set的顺序
        // 按照normalizedNewImagesUrls的顺序构建最终URL列表
        List<String> finalImagesUrls = new ArrayList<>();
        for (String normalizedUrl : normalizedNewImagesUrls) {
            // 检查这个URL是否在保留或新增列表中
            if (toKeepUrls.contains(normalizedUrl) || toAddUrls.contains(normalizedUrl)) {
                // 查找对应的缩略图URL
                String thumbnailUrl = allOriginalToThumbnailMap.get(normalizedUrl);
                // 如果找到了对应的缩略图URL，使用缩略图URL，否则使用原图URL
                finalImagesUrls.add(StrUtil.isNotBlank(thumbnailUrl) ? thumbnailUrl : normalizedUrl);
            }
        }
        
        String imagesUrlStr = "";
        if (CollUtil.isNotEmpty(finalImagesUrls)) {
            imagesUrlStr = String.join(",", finalImagesUrls);
        }
        
        boolean updateResult = productDetailService.lambdaUpdate()
                .eq(ProductDetailEntity::getId, dto.getSkuId())
                .set(ProductDetailEntity::getImagesUrl, imagesUrlStr)
                .update();
        
        // 8. 记录操作日志
        if (updateResult) {
            operateLogService.addSysLogBySave(
                    "sku图片由[" + oldImagesUrl + "]变更为[" + imagesUrlStr + "]",
                    SKUCLASSPATH,
                    productDetailEntity.getId(),
                    productDetailEntity.getProductId()
            );
        }
        
        log.info("上传产品主图完成，skuId={}", dto.getSkuId());
        return updateResult;
    }

    /**
     * 上传产品主图文件（保存原图和缩略图，返回缩略图URL）
     * @param multipartFileList 图片文件数组
     * @param skuId SKU ID
     * @author wuhaotian
     * @date: 2025-12-29
     * @return List<String> 返回缩略图URL列表
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public List<String> uploadProductMainImageFile(MultipartFile[] multipartFileList, String skuId) {
        log.info("开始上传产品主图文件，skuId={}, 文件数量={}", skuId, multipartFileList != null ? multipartFileList.length : 0);
        
        if (multipartFileList == null || multipartFileList.length == 0) {
            throw new ServiceException(ApiError.FILE_NOT_FOUND);
        }
        
        // 1. 验证SKU是否存在，状态是否允许修改
        ProductDetailEntity productDetailEntity = productDetailService.getById(skuId);
        if (ObjectUtil.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
        }
        Integer status = productDetailEntity.getStatus();
        if (status.equals(ProductDetailStatusEnum.APPROVAL_ING.getCode())) {
            throw new ServiceException(ApiError.PRODUCT_UPLOAD_FORBIDDEN_IN_APPROVING);
        }
        
        // 2. 确保只有一个主图：将之前的主图移出主图分类
        String uncategorizedCategoryId = getUncategorizedCategoryId();
        List<RefProductImgAttachmentEntity> oldMainImages = super.lambdaQuery()
                .eq(RefProductImgAttachmentEntity::getProductDetailId, skuId)
                .eq(RefProductImgAttachmentEntity::getCategoryId, PRODUCT_MAIN_IMAGE_CATEGORY_ID)
                .list();
        
        for (RefProductImgAttachmentEntity oldMainImage : oldMainImages) {
            oldMainImage.setCategoryId(uncategorizedCategoryId);
            super.updateById(oldMainImage);
            log.info("将旧主图移出主图分类，归类到未分类，refId={}", oldMainImage.getId());
        }
        
        List<String> thumbnailUrls = new ArrayList<>();
        boolean isFirstFile = true;
        
        // 3. 遍历每个文件，上传并生成缩略图
        for (MultipartFile multipartFile : multipartFileList) {
            try {
                // 3.1 上传原图到FastDFS
                String originalFileUrl = fileFeign.uploadFile(multipartFile);
                if (StrUtil.isBlank(originalFileUrl)) {
                    log.warn("上传原图失败，文件名：{}", multipartFile.getOriginalFilename());
                    continue;
                }
                
                // 3.2 获取文件大小（MB）
                BigDecimal fileSizeMB = BigDecimal.valueOf(multipartFile.getSize())
                        .divide(BigDecimal.valueOf(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP);
                
                // 3.3 保存原图到attachment表
                PlmAttachmentEntity originalAttachment = new PlmAttachmentEntity();
                originalAttachment.setAttachUrl(originalFileUrl);
                String originalFileName = multipartFile.getOriginalFilename();
                if (StrUtil.isBlank(originalFileName)) {
                    originalFileName = "image_" + System.currentTimeMillis() + ".jpg";
                }
                originalAttachment.setAttachName(originalFileName.toLowerCase());
                originalAttachment.setAttachSize(fileSizeMB);
                originalAttachment.setType("product_detail");
                originalAttachment.setBusinessId(skuId);
                plmAttachmentService.save(originalAttachment);
                
                // 3.4 创建ref_product_img_attachment记录（第一个为主图，其他为未分类）
                String categoryId = isFirstFile ? PRODUCT_MAIN_IMAGE_CATEGORY_ID : uncategorizedCategoryId;
                RefProductImgAttachmentEntity mainImageRef = new RefProductImgAttachmentEntity();
                mainImageRef.setCategoryId(categoryId);
                mainImageRef.setProductDetailId(skuId);
                mainImageRef.setSkuNo(productDetailEntity.getSkuNo());
                mainImageRef.setAttachmentId(originalAttachment.getId());
                super.save(mainImageRef);
                
                // 3.5 生成缩略图（不更新product_detail）
                String thumbnailUrl = generateThumbnailWithoutUpdate(
                        originalAttachment.getId(), 
                        skuId, 
                        productDetailEntity.getSkuNo()
                );
                
                // 如果生成了缩略图，使用缩略图URL，否则使用原图URL
                String finalUrl = StrUtil.isNotBlank(thumbnailUrl) ? thumbnailUrl : originalFileUrl;
                thumbnailUrls.add(finalUrl);
                
                // 如果是第一个文件（主图），在images_url中用缩略图URL替换原图URL
                if (isFirstFile) {
                    if (StrUtil.isNotBlank(thumbnailUrl)) {
                        replaceOriginalUrlWithThumbnailInImagesUrl(skuId, originalFileUrl, thumbnailUrl);
                        log.info("成功上传产品主图文件并生成缩略图，原图URL={}, 缩略图URL={}", originalFileUrl, thumbnailUrl);
                    } else {
                        log.info("无需生成缩略图或生成失败，使用原图作为主图，原图URL={}", originalFileUrl);
                    }
                }
                
                isFirstFile = false; // 后续文件不是主图
                
            } catch (Exception e) {
                log.error("上传产品主图文件失败，文件名={}, 错误信息={}", 
                        multipartFile.getOriginalFilename(), e.getMessage(), e);
                // 继续处理下一个文件，不中断整个流程
            }
        }
        
        log.info("上传产品主图文件完成，skuId={}, 成功生成{}个缩略图", skuId, thumbnailUrls.size());
        return thumbnailUrls;
    }

    /**
     * 处理产品保存后的图片URL（创建attachment记录、创建ref记录、异步生成缩略图）
     * @param skuId SKU ID
     * @param imagesUrl 图片URL字符串（逗号分隔）
     * @author wuhaotian
     * @date: 2025-12-29
     */
    @Override
    public void handleProductImagesAfterSave(String skuId, String imagesUrl) {
        handleProductImagesAfterSave(skuId, imagesUrl, null);
    }
    
    /**
     * 处理产品保存后的图片URL（创建attachment记录、创建ref记录、异步生成缩略图）
     * @param skuId SKU ID
     * @param imagesUrl 图片URL字符串（逗号分隔）
     * @param urlToNameMap URL到图片名称的映射（可选，为null时从URL提取文件名）
     * @author wuhaotian
     * @date: 2025-12-29
     */
    public void handleProductImagesAfterSave(String skuId, String imagesUrl, Map<String, String> urlToNameMap) {
        if (StrUtil.isBlank(imagesUrl) || StrUtil.isBlank(skuId)) {
            log.debug("图片URL或SKU ID为空，跳过处理，skuId={}, imagesUrl={}", skuId, imagesUrl);
            return;
        }

        log.info("开始处理产品保存后的图片，skuId={}, imagesUrl={}, 是否有名称映射={}", 
                skuId, imagesUrl, urlToNameMap != null && !urlToNameMap.isEmpty());

        // 1. 查询产品明细信息
        ProductDetailEntity productDetailEntity = productDetailService.getById(skuId);
        if (ObjectUtil.isEmpty(productDetailEntity)) {
            log.warn("产品明细不存在，跳过图片处理，skuId={}", skuId);
            return;
        }

        // 2. 解析图片URL列表
        List<String> imageUrls = Arrays.stream(imagesUrl.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(imageUrls)) {
            log.debug("解析后的图片URL列表为空，跳过处理，skuId={}", skuId);
            return;
        }

        // 3. 查询现有的所有关联记录（不限制分类）
        List<RefProductImgAttachmentEntity> existingRefList = super.lambdaQuery()
                .eq(RefProductImgAttachmentEntity::getProductDetailId, skuId)
                .list();

        // 获取现有的附件ID列表
        List<String> existingAttachmentIds = existingRefList.stream()
                .map(RefProductImgAttachmentEntity::getAttachmentId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());

        // 查询现有的附件记录，构建URL到附件的映射
        Map<String, PlmAttachmentEntity> existingAttachmentMap = new HashMap<>();
        Map<String, RefProductImgAttachmentEntity> urlToRefMap = new HashMap<>();
        Set<String> existingUrls = new HashSet<>();
        if (CollUtil.isNotEmpty(existingAttachmentIds)) {
            List<PlmAttachmentEntity> existingAttachments = plmAttachmentService.listByIds(existingAttachmentIds);
            for (PlmAttachmentEntity attachment : existingAttachments) {
                if (StrUtil.isNotBlank(attachment.getAttachUrl())) {
                    existingAttachmentMap.put(attachment.getAttachUrl(), attachment);
                    existingUrls.add(attachment.getAttachUrl());
                    // 找到对应的关联记录
                    for (RefProductImgAttachmentEntity refEntity : existingRefList) {
                        if (refEntity.getAttachmentId().equals(attachment.getId())) {
                            urlToRefMap.put(attachment.getAttachUrl(), refEntity);
                            break;
                        }
                    }
                }
            }
        }
        
        // 3.1 确定主图：imagesUrl中第一个URL为主图，其他为未分类
        String uncategorizedCategoryId = getUncategorizedCategoryId();
        String firstImageUrl = CollUtil.isNotEmpty(imageUrls) ? imageUrls.get(0) : null;
        
        // 3.2 更新现有记录的分类：确保只有第一个是主图，其他都是未分类
        if (CollUtil.isNotEmpty(existingRefList) && StrUtil.isNotBlank(firstImageUrl)) {
            for (RefProductImgAttachmentEntity refEntity : existingRefList) {
                PlmAttachmentEntity attachment = existingAttachmentMap.values().stream()
                        .filter(att -> att.getId().equals(refEntity.getAttachmentId()))
                        .findFirst()
                        .orElse(null);
                if (attachment != null && StrUtil.isNotBlank(attachment.getAttachUrl())) {
                    String shouldBeCategoryId = attachment.getAttachUrl().equals(firstImageUrl) 
                            ? PRODUCT_MAIN_IMAGE_CATEGORY_ID 
                            : uncategorizedCategoryId;
                    if (!shouldBeCategoryId.equals(refEntity.getCategoryId())) {
                        refEntity.setCategoryId(shouldBeCategoryId);
                        super.updateById(refEntity);
                        log.info("更新现有图片分类，refId={}, url={}, 新分类={}", refEntity.getId(), attachment.getAttachUrl(), shouldBeCategoryId);
                    }
                }
            }
        }

        // 4. 处理每个图片URL，收集需要生成缩略图的URL
        // 使用线程安全的Map收集原图URL到缩略图URL的映射
        Map<String, String> originalToThumbnailMap = new ConcurrentHashMap<>();
        // 收集所有需要异步生成缩略图的任务
        List<CompletableFuture<Void>> thumbnailTasks = new ArrayList<>();
        
        for (String imageUrl : imageUrls) {
            try {
                // 4.1 如果该URL已存在，跳过
                if (existingUrls.contains(imageUrl)) {
                    log.debug("图片URL已存在，跳过处理，url={}", imageUrl);
                    continue;
                }

                // 4.2 查找或创建attachment记录
                PlmAttachmentEntity attachmentEntity = plmAttachmentService.lambdaQuery()
                        .eq(PlmAttachmentEntity::getAttachUrl, imageUrl)
                        .eq(PlmAttachmentEntity::getBusinessId, skuId)
                        .last("LIMIT 1")
                        .one();

                if (attachmentEntity == null) {
                    // 创建新的attachment记录
                    attachmentEntity = new PlmAttachmentEntity();
                    attachmentEntity.setAttachUrl(imageUrl);
                    
                    // 优先使用传入的名称，如果传入的名称为空，从URL提取文件名
                    String fileName = (urlToNameMap != null && urlToNameMap.containsKey(imageUrl)) 
                            ? urlToNameMap.get(imageUrl) 
                            : null;
                    if (StrUtil.isBlank(fileName)) {
                        fileName = imageUrl;
                        int lastSlash = imageUrl.lastIndexOf('/');
                        if (lastSlash >= 0 && lastSlash < imageUrl.length() - 1) {
                            fileName = imageUrl.substring(lastSlash + 1);
                        }
                    }
                    attachmentEntity.setAttachName(fileName);
                    attachmentEntity.setAttachSize(BigDecimal.ZERO); // 稍后可以通过文件服务获取
                    attachmentEntity.setType("product_detail");
                    attachmentEntity.setBusinessId(skuId);
                    plmAttachmentService.save(attachmentEntity);
                    log.info("为图片创建attachment记录，id={}, url={}, fileName={}", attachmentEntity.getId(), imageUrl, fileName);
                } else {
                    // 如果attachment已存在，检查是否需要更新名称
                    String newName = (urlToNameMap != null && urlToNameMap.containsKey(imageUrl)) 
                            ? urlToNameMap.get(imageUrl) 
                            : null;
                    if (StrUtil.isNotBlank(newName) && !newName.equals(attachmentEntity.getAttachName())) {
                        // 如果传入了新名称，更新名称
                        plmAttachmentService.lambdaUpdate()
                                .eq(PlmAttachmentEntity::getId, attachmentEntity.getId())
                                .set(PlmAttachmentEntity::getAttachName, newName)
                                .update();
                        log.info("更新attachment名称，id={}, url={}, 旧名称={}, 新名称={}", 
                                attachmentEntity.getId(), imageUrl, attachmentEntity.getAttachName(), newName);
                        attachmentEntity.setAttachName(newName);
                    } else {
                        log.debug("保留原有attachment名称，id={}, url={}, attachName={}", 
                                attachmentEntity.getId(), imageUrl, attachmentEntity.getAttachName());
                    }
                }

                // 4.3 检查是否已有ref_product_img_attachment记录
                RefProductImgAttachmentEntity existingRef = urlToRefMap.get(imageUrl);
                
                // 确定分类：第一个图片为主图，其他为未分类
                String categoryId = imageUrl.equals(firstImageUrl) ? PRODUCT_MAIN_IMAGE_CATEGORY_ID : uncategorizedCategoryId;

                if (existingRef == null) {
                    // 创建ref_product_img_attachment记录
                    RefProductImgAttachmentEntity refEntity = new RefProductImgAttachmentEntity();
                    refEntity.setCategoryId(categoryId);
                    refEntity.setProductDetailId(skuId);
                    refEntity.setSkuNo(productDetailEntity.getSkuNo());
                    refEntity.setAttachmentId(attachmentEntity.getId());
                    super.save(refEntity);
                    log.info("创建产品图片关联记录，id={}, skuId={}, url={}, categoryId={}", refEntity.getId(), skuId, imageUrl, categoryId);

                    // 4.4 异步生成缩略图（不立即更新images_url）
                    final String attachmentId = attachmentEntity.getId();
                    final String skuNo = productDetailEntity.getSkuNo();
                    final String finalOriginalUrl = imageUrl; // 保存原图URL用于后续替换
                    
                    CompletableFuture<Void> thumbnailTask = CompletableFuture.runAsync(() -> {
                        try {
                            log.info("开始异步生成缩略图，attachmentId={}, skuId={}, originalUrl={}", attachmentId, skuId, finalOriginalUrl);
                            String thumbnailUrl = generateThumbnailWithoutUpdate(attachmentId, skuId, skuNo);
                            // 将结果放入Map，不立即更新images_url
                            if (StrUtil.isNotBlank(thumbnailUrl)) {
                                originalToThumbnailMap.put(finalOriginalUrl, thumbnailUrl);
                                log.info("成功异步生成缩略图，attachmentId={}, skuId={}, 原图URL={}, 缩略图URL={}", 
                                        attachmentId, skuId, finalOriginalUrl, thumbnailUrl);
                            } else {
                                log.info("无需生成缩略图或生成失败，使用原图，attachmentId={}, skuId={}, 原图URL={}", 
                                        attachmentId, skuId, finalOriginalUrl);
                            }
                        } catch (Exception e) {
                            log.error("异步生成缩略图失败，attachmentId={}, skuId={}, 错误信息={}", 
                                    attachmentId, skuId, e.getMessage(), e);
                        }
                    }, zipImageExecutorPool);
                    
                    thumbnailTasks.add(thumbnailTask);
                }

            } catch (Exception e) {
                log.error("处理图片URL失败，url={}, skuId={}, 错误信息={}", imageUrl, skuId, e.getMessage(), e);
                // 继续处理下一个URL，不中断整个流程
            }
        }

        // 5. 等待所有异步缩略图生成任务完成，然后统一更新images_url
        if (CollUtil.isNotEmpty(thumbnailTasks)) {
            try {
                // 等待所有任务完成（最多等待5分钟）
                CompletableFuture.allOf(thumbnailTasks.toArray(new CompletableFuture[0]))
                        .get(5, java.util.concurrent.TimeUnit.MINUTES);
                
                log.info("所有缩略图生成任务完成，skuId={}, 共生成{}个缩略图", skuId, originalToThumbnailMap.size());
                
                // 统一更新images_url：用缩略图URL替换原图URL
                // 先从所有ref记录中获取已存在的缩略图URL
                Map<String, String> allThumbnailMap = new HashMap<>(originalToThumbnailMap);
                
                // 查询所有图片记录，构建完整的缩略图映射
                List<RefProductImgAttachmentEntity> allRefs = super.lambdaQuery()
                        .eq(RefProductImgAttachmentEntity::getProductDetailId, skuId)
                        .list();
                
                if (CollUtil.isNotEmpty(allRefs)) {
                    // 收集所有缩略图attachmentId
                    Set<String> thumbnailAttachmentIds = new HashSet<>();
                    Map<String, String> attachmentIdToOriginalUrlMap = new HashMap<>();
                    
                    for (RefProductImgAttachmentEntity ref : allRefs) {
                        String thumbAttachmentId = StrUtil.isNotBlank(ref.getThumbnailAttachmentId()) 
                                ? ref.getThumbnailAttachmentId() 
                                : ref.getAttachmentId();
                        thumbnailAttachmentIds.add(thumbAttachmentId);
                        
                        // 找到对应的原图URL
                        PlmAttachmentEntity originalAttachment = existingAttachmentMap.values().stream()
                                .filter(att -> att.getId().equals(ref.getAttachmentId()))
                                .findFirst()
                                .orElse(null);
                        if (originalAttachment != null && StrUtil.isNotBlank(originalAttachment.getAttachUrl())) {
                            attachmentIdToOriginalUrlMap.put(ref.getAttachmentId(), originalAttachment.getAttachUrl());
                        }
                    }
                    
                    // 批量查询缩略图attachment
                    if (CollUtil.isNotEmpty(thumbnailAttachmentIds)) {
                        List<PlmAttachmentEntity> thumbnailAttachments = plmAttachmentService.listByIds(new ArrayList<>(thumbnailAttachmentIds));
                        Map<String, PlmAttachmentEntity> thumbnailAttachmentMap = thumbnailAttachments.stream()
                                .collect(Collectors.toMap(PlmAttachmentEntity::getId, ta -> ta));
                        
                        // 构建原图URL到缩略图URL的映射
                        for (RefProductImgAttachmentEntity ref : allRefs) {
                            String originalUrl = attachmentIdToOriginalUrlMap.get(ref.getAttachmentId());
                            if (StrUtil.isNotBlank(originalUrl)) {
                                String thumbAttachmentId = StrUtil.isNotBlank(ref.getThumbnailAttachmentId()) 
                                        ? ref.getThumbnailAttachmentId() 
                                        : ref.getAttachmentId();
                                PlmAttachmentEntity thumbAttachment = thumbnailAttachmentMap.get(thumbAttachmentId);
                                if (thumbAttachment != null && StrUtil.isNotBlank(thumbAttachment.getAttachUrl())) {
                                    allThumbnailMap.put(originalUrl, thumbAttachment.getAttachUrl());
                                }
                            }
                        }
                    }
                }
                
                if (!allThumbnailMap.isEmpty()) {
                    // 获取当前的images_url
                    ProductDetailEntity currentProductDetail = productDetailService.getById(skuId);
                    if (currentProductDetail != null && StrUtil.isNotBlank(currentProductDetail.getImagesUrl())) {
                        List<String> currentImageUrls = Arrays.stream(currentProductDetail.getImagesUrl().split(","))
                                .map(String::trim)
                                .filter(StrUtil::isNotBlank)
                                .collect(Collectors.toList());
                        
                        // 替换原图URL为缩略图URL
                        for (int i = 0; i < currentImageUrls.size(); i++) {
                            String originalUrl = currentImageUrls.get(i);
                            String thumbnailUrl = allThumbnailMap.get(originalUrl);
                            if (StrUtil.isNotBlank(thumbnailUrl)) {
                                currentImageUrls.set(i, thumbnailUrl);
                                log.debug("在images_url中替换原图URL为缩略图URL，原图={}, 缩略图={}", originalUrl, thumbnailUrl);
                            }
                        }
                        
                        // 更新images_url
                        String newImagesUrl = String.join(",", currentImageUrls);
                        currentProductDetail.setImagesUrl(newImagesUrl);
                        productDetailService.updateById(currentProductDetail);
                        log.info("统一更新product_detail的images_url完成，skuId={}, 新images_url={}", skuId, newImagesUrl);
                    }
                }
            } catch (java.util.concurrent.TimeoutException e) {
                log.warn("等待缩略图生成任务超时，skuId={}，将使用已完成的缩略图更新images_url", skuId);
                // 即使超时，也使用已完成的缩略图进行更新（复用上面的逻辑）
                // 查询所有ref记录，获取缩略图URL
                Map<String, String> allThumbnailMap = new HashMap<>(originalToThumbnailMap);
                List<RefProductImgAttachmentEntity> allRefs = super.lambdaQuery()
                        .eq(RefProductImgAttachmentEntity::getProductDetailId, skuId)
                        .list();
                
                if (CollUtil.isNotEmpty(allRefs)) {
                    Set<String> thumbnailAttachmentIds = new HashSet<>();
                    Map<String, String> attachmentIdToOriginalUrlMap = new HashMap<>();
                    
                    for (RefProductImgAttachmentEntity ref : allRefs) {
                        String thumbAttachmentId = StrUtil.isNotBlank(ref.getThumbnailAttachmentId()) 
                                ? ref.getThumbnailAttachmentId() 
                                : ref.getAttachmentId();
                        thumbnailAttachmentIds.add(thumbAttachmentId);
                        
                        PlmAttachmentEntity originalAttachment = existingAttachmentMap.values().stream()
                                .filter(att -> att.getId().equals(ref.getAttachmentId()))
                                .findFirst()
                                .orElse(null);
                        if (originalAttachment != null && StrUtil.isNotBlank(originalAttachment.getAttachUrl())) {
                            attachmentIdToOriginalUrlMap.put(ref.getAttachmentId(), originalAttachment.getAttachUrl());
                        }
                    }
                    
                    if (CollUtil.isNotEmpty(thumbnailAttachmentIds)) {
                        List<PlmAttachmentEntity> thumbnailAttachments = plmAttachmentService.listByIds(new ArrayList<>(thumbnailAttachmentIds));
                        Map<String, PlmAttachmentEntity> thumbnailAttachmentMap = thumbnailAttachments.stream()
                                .collect(Collectors.toMap(PlmAttachmentEntity::getId, ta -> ta));
                        
                        for (RefProductImgAttachmentEntity ref : allRefs) {
                            String originalUrl = attachmentIdToOriginalUrlMap.get(ref.getAttachmentId());
                            if (StrUtil.isNotBlank(originalUrl)) {
                                String thumbAttachmentId = StrUtil.isNotBlank(ref.getThumbnailAttachmentId()) 
                                        ? ref.getThumbnailAttachmentId() 
                                        : ref.getAttachmentId();
                                PlmAttachmentEntity thumbAttachment = thumbnailAttachmentMap.get(thumbAttachmentId);
                                if (thumbAttachment != null && StrUtil.isNotBlank(thumbAttachment.getAttachUrl())) {
                                    allThumbnailMap.put(originalUrl, thumbAttachment.getAttachUrl());
                                }
                            }
                        }
                    }
                }
                
                if (!allThumbnailMap.isEmpty()) {
                    ProductDetailEntity currentProductDetail = productDetailService.getById(skuId);
                    if (currentProductDetail != null && StrUtil.isNotBlank(currentProductDetail.getImagesUrl())) {
                        List<String> currentImageUrls = Arrays.stream(currentProductDetail.getImagesUrl().split(","))
                                .map(String::trim)
                                .filter(StrUtil::isNotBlank)
                                .collect(Collectors.toList());
                        
                        for (int i = 0; i < currentImageUrls.size(); i++) {
                            String originalUrl = currentImageUrls.get(i);
                            String thumbnailUrl = allThumbnailMap.get(originalUrl);
                            if (StrUtil.isNotBlank(thumbnailUrl)) {
                                currentImageUrls.set(i, thumbnailUrl);
                            }
                        }
                        
                        String newImagesUrl = String.join(",", currentImageUrls);
                        currentProductDetail.setImagesUrl(newImagesUrl);
                        productDetailService.updateById(currentProductDetail);
                        log.info("超时后统一更新product_detail的images_url完成，skuId={}, 新images_url={}", skuId, newImagesUrl);
                    }
                }
            } catch (Exception e) {
                log.error("等待缩略图生成任务或更新images_url失败，skuId={}, 错误信息={}", skuId, e.getMessage(), e);
            }
        }

        log.info("完成处理产品保存后的图片，skuId={}, 共处理{}个图片URL", skuId, imageUrls.size());
    }
}
    