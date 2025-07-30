package com.erp.server.plm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.*;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProductDetailImageExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_PLM_SKU_IMAGES;

/**
 * @Description: 产品明细信息 图片处理类
 * @Author: jack
 * @Date: 2025-07-25
 **/
@Slf4j
@Service
public class ProductDetailImagesServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetailEntity> implements ProductDetailImagesService {


    @Resource
    private SysLogService sysLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @Resource
    private ImageProcessService imageProcessService;

    @Resource
    private PlmAttachmentService plmAttachmentService;

    @Resource
    private FileFeign fileFeign;

    @Resource
    @Qualifier("zipImageExecutorPool")
    private ExecutorService zipImageExecutorPool;

    private static final String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);

    /**
     * 更新产品主图
     * @author jack
     * @date 2025-07-26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadProductImage(ProductDetailDTO.ProductImagesDTO dto) {
        ProductDetailEntity productDetailEntity = getById(dto.getSkuId());
        if (ObjectUtils.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        Integer status = productDetailEntity.getStatus();
        if (status.equals(ProductDetailStatusEnum.APPROVAL_ING.getCode())) {
            throw new ServiceException(ApiError.ERROR_95291);
        }

        String oldImagesUrl = productDetailEntity.getImagesUrl();

        List<String> imagesUrls = dto.getImagesUrls();
        String imagesUrlStr = "";
        if(CollUtil.isNotEmpty(imagesUrls)){//删除图片
            imagesUrlStr = String.join(",", imagesUrls);
        }
        boolean save = lambdaUpdate()
                .eq(ProductDetailEntity::getId, dto.getSkuId())
                .set(ProductDetailEntity::getImagesUrl, imagesUrlStr)
                .update();

        if (save) {
            sysLogService.addSysLogBySave("sku图片由[" + oldImagesUrl + "]变更为[" + imagesUrlStr + "]", SKUCLASSPATH, productDetailEntity.getId(), productDetailEntity.getProductId());

            if(StringUtils.isNotBlank(oldImagesUrl)){
                List<String> list = Arrays.asList(oldImagesUrl.split(","));
                //则需要删除
                plmAttachmentService.lambdaUpdate()
                        .in(PlmAttachmentEntity::getAttachUrl, list)
                        .eq(PlmAttachmentEntity::getBusinessId, productDetailEntity.getId())
                        .set(PlmAttachmentEntity::getIsDeleted,true)
                        .update();
                fileFeign.deleteBatchFile(list);
            }
        }
        return save;
    }

    /**
     * 异步导入SKU图片（主页）
     * @author jack
     * @date 2025-07-25
     */
    @Override
    public Boolean importZip(ProductDetailDTO.ProductImagesZipDTO dto) {
        downloadTaskFeign.saveImportTask("SKU图片导入", IMPORT_PLM_SKU_IMAGES.getCode(), dto);
        return Boolean.TRUE;
    }


    /**
     * @author jack
     * @date 2025-07-26
     *
     * 导入商品详情图片（通过解析压缩包实现）
     * 该方法接收一个包含压缩包文件URL的导入DTO对象，解析压缩包中的图片文件，
     * 根据文件名提取SKU编号，并将图片与对应的商品详情进行关联处理。
     * 同时会排除状态为“待审核”的商品详情数据。
     * 最终更新任务执行结果到下载任务服务中。
     * @param dto 包含文件URL和任务ID的导入参数对象，不能为空
     */
    @Override
    public void importProductDetailImages(ProductDetailDTO.ProductImagesZipDTO dto) {
        List<MultipartFile> multipartFiles = null;
        try {
            // 解析压缩包获取图片文件列表
            multipartFiles = imageProcessService.processZip(dto.getFileUrl());
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }

        ZipTaskResultDTO result = new ZipTaskResultDTO();
        List<ProductDetailImageExcelDTO> errorList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(multipartFiles)) {
            // 根据文件名获取SKU集合(去除后缀，去除下划线)
            List<String> skuNoList = multipartFiles.stream()
                    .map(MultipartFile::getOriginalFilename)
                    .map(name -> {
                        return getFileNameNotExt(name);
                    }).map(name -> {
                        if (name.contains("_")) {
                            return name.substring(0, name.indexOf("_"));  // 提取下划线前的部分作为SKU编号
                        }
                        return name;  // 不含下划线则直接使用文件名作为SKU编号
                    })
                    .distinct()
                    .collect(Collectors.toList());
            if(CollUtil.isEmpty(skuNoList)){
                throw new ServiceException("SKU图片格式有异常,主图SKU，非主图使用SKU_1");
            }

            // 排除待审核的SKU，只处理非“待审核”状态的商品详情
            List<ProductDetailEntity> productDetailList = lambdaQuery()
                    .in(ProductDetailEntity::getSkuNo, skuNoList)
                    .ne(ProductDetailEntity::getStatus, ProductDetailStatusEnum.APPROVAL_ING.getCode())
                    .list();

                // 构建SKU到商品详情实体的映射，用于快速查找
                Map<String, ProductDetailEntity> productDetailMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo, productDetailEntity -> productDetailEntity, (existing, replacement) -> existing));
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                // 获取压缩图片大小的配置
                Long size = getImgUploadSize();

                // 遍历所有图片文件并异步处理
                for (MultipartFile file : multipartFiles) {
                    // 每次循束，总处理数加一
                    result.incrementTotal();

                    String originalFilename = file.getOriginalFilename();
                    String fileName = getFileNameNotExt(originalFilename);
                    String skuNo = fileName.contains("_") ? fileName.substring(0, fileName.indexOf("_")) : fileName;

                    if (!productDetailMap.containsKey(skuNo)) {
                        // 如果SKU不存在于产品明细中，跳过处理
                        result.incrementFailed(skuNo,originalFilename);
                        continue;
                    }

                    ProductDetailEntity productDetailEntity = productDetailMap.get(skuNo);

                    // 异步处理单张图片
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        imageProcessService.processImage(file, fileName, productDetailEntity, size, result,dto.getImportType());
                    }, zipImageExecutorPool);
                    futures.add(future);
                }
                // 等待所有异步任务完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            Map<String, List<String>> successFiles = result.getSuccessFiles();
            //判空
            if (MapUtil.isNotEmpty(successFiles)) {
                // 构建SKU到商品详情实体的映射，用于快速查找
                productDetailMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, productDetailEntity -> productDetailEntity, (existing, replacement) -> existing));

                for (Map.Entry<String, List<String>> entry : successFiles.entrySet()) {
                    String skuId = entry.getKey();
                    List<String> value = entry.getValue();

                    ProductDetailEntity productDetailEntity = productDetailMap.getOrDefault(skuId,null);
                    if (Objects.isNull(productDetailEntity)) {
                        continue;
                    }
                    String imagesUrl = productDetailEntity.getImagesUrl();
                    if (StringUtils.isNotBlank(imagesUrl)) {
                        List<String> list = Arrays.asList(imagesUrl.split(","));
                        if(ProductDetailImprotTypeEnum.ADD.getCode().equals(dto.getImportType())){
                            //保留产品原有图片，并在新增新上传的图片
                            value.addAll(list);
                        }else {
                            //新上传的图片替换原有图片
                            //则需要删除
                            plmAttachmentService.lambdaUpdate()
                                    .in(PlmAttachmentEntity::getAttachUrl, list)
                                    .eq(PlmAttachmentEntity::getBusinessId, productDetailEntity.getId())
                                    .set(PlmAttachmentEntity::getIsDeleted,true)
                                    .update();
                            fileFeign.deleteBatchFile(list);
                        }
                    }

                    // 先按是否有下划线排序，再按字典序排序
                    List<PlmAttachmentEntity> newPlmAttachmentList = plmAttachmentService.lambdaQuery()
                            .eq(PlmAttachmentEntity::getBusinessId, productDetailEntity.getId())
                            .eq(PlmAttachmentEntity::getType, "product_detail")
                            .in(PlmAttachmentEntity::getAttachUrl, value)
                            .list();

                    // 添加自定义排序逻辑
                    newPlmAttachmentList.sort((a, b) -> {
                        String nameA = a.getAttachName();
                        String nameB = b.getAttachName();

                        boolean hasUnderscoreA = nameA.contains("_");
                        boolean hasUnderscoreB = nameB.contains("_");

                        // 如果一个有下划线，一个没有下划线
                        if (hasUnderscoreA != hasUnderscoreB) {
                            return hasUnderscoreA ? 1 : -1; // 无下划线的排在前面
                        }

                        // 如果都没有下划线，按字典序排序
                        if (!hasUnderscoreA && !hasUnderscoreB) {
                            return nameA.compareTo(nameB);
                        }

                        // 如果都有下划线，按 下划线后的数字 排序
                        try {
                            String numStrA = nameA.substring(nameA.indexOf("_") + 1);
                            String numStrB = nameB.substring(nameB.indexOf("_") + 1);
                            Integer numA = Integer.valueOf(numStrA);
                            Integer numB = Integer.valueOf(numStrB);
                            return numA.compareTo(numB);
                        } catch (NumberFormatException e) {
                            // 如果解析数字失败，按字典序排序
                            return nameA.compareTo(nameB);
                        }
                    });

                    List<String> iamgesUrls = newPlmAttachmentList.stream().map(PlmAttachmentEntity::getAttachUrl).collect(Collectors.toList());
                    String iamgesUrl = String.join(",", iamgesUrls);

                    lambdaUpdate().set(ProductDetailEntity::getImagesUrl, iamgesUrl)
                            .eq(ProductDetailEntity::getId, skuId)
                            .update();
                }
            }

            Map<String, List<String>> failFiles = result.getFailFiles();
            if (MapUtil.isNotEmpty(failFiles)){
                for (Map.Entry<String, List<String>> entry : failFiles.entrySet()) {
                    String skuNo = entry.getKey();
                    List<String> value = entry.getValue();
                    String errorMsg = String.join(",", value);
                    ProductDetailImageExcelDTO excelDTO = new ProductDetailImageExcelDTO();
                    excelDTO.setSkuNo(skuNo);
                    excelDTO.setErrorMsg("以下图片导入失败："+errorMsg);
                    errorList.add(excelDTO);
                }
            }
        }

        //导出错误数据
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "图片批量导入错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, ProductDetailImageExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }

        // 构造任务处理结果并更新任务状态
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(result.getTotal());
        importResultDTO.setRemark("处理完成，失败" + result.getFailed() + "条");
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        importResultDTO.setErrorUrl(url);
        downloadTaskFeign.updateTask(importResultDTO);
    }


    private String getFileNameNotExt(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    /**
     * 查询配置
     */
    private Long getImgUploadSize() {
        Long size = 0L;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SettingEnum.IMG_UPLOAD_SIZE_KEY.getKey())
                .eq(CfgSettingEntity::getType, SettingEnum.IMG_UPLOAD_SIZE_KEY.getType())
                .list();
        if (CollectionUtil.isNotEmpty(list) && ObjectUtil.isNotNull(list.get(0).getValue())) {
            size = Long.valueOf(list.get(0).getValue());
        }
        return size;
    }
}
