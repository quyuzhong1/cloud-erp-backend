package com.erp.server.plm.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.annotation.DistributeLocker;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.file.SambaUtil;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.dto.AttachmentDTO;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.dto.excel.ProductCertificateExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.enums.ProductCertificateProjectEnum;
import com.erp.model.plm.enums.ProductCertificateTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.plm.listener.ProductCertificateExcelListener;
import com.erp.server.plm.mapper.ProductCertificateMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_CERTIFICATE;

/**
 * 认证实现类
 */
@Service
@Slf4j
public class ProductCertificateServiceImpl extends ServiceImpl<ProductCertificateMapper, ProductCertificateEntity>
    implements ProductCertificateService {

    private static final String PRODUCT_CERTIFICATE_LOCK_BIZ = "plm:pc";
    private static final Set<String> OVERWRITE_CERTIFICATE_TYPE_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ProductCertificateTypeEnum.PRODUCT_ATTESTATION.getCode(),
            ProductCertificateTypeEnum.TRANSPORT_ATTESTATION.getCode()
    )));

    @Resource
    private ProductCertificateMapper productCertificateMapper;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private PlmAttachmentService plmAttachmentService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FileFeign fileFeign;
    @Override
    public PagingVO<ProductCertificateDTO.ListDTO> paging(PagingDTO<ProductCertificateDTO.SearchParamDTO> pagingDTO) {
        ProductCertificateDTO.SearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page<ProductCertificateDTO.SearchParamDTO> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ProductCertificateDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        List<ProductCertificateDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO<>(pageData);
        }
        //数据赋值处理
        handlePaging(records);
        return new PagingVO<>(pageData);
    }


    @Override
    public void add(ProductCertificateDTO.AddDTO dto) {
        List<ProductCertificateEntity> resultList = handleAdd(dto);
        List<String> lockKeys = buildCertificateLockKeys(resultList);
        ApplicationContextUtils.getBean(ProductCertificateServiceImpl.class).addWithLock(lockKeys, resultList);
    }

    @DistributeLocker(businessType = PRODUCT_CERTIFICATE_LOCK_BIZ, keyName = "lockKeys", waiteTime = 60)
    @Transactional(rollbackFor = Exception.class)
    public void addWithLock(List<String> lockKeys, List<ProductCertificateEntity> resultList) {
        // 新增场景必须保证每条证书都有有效文件，避免主表落库但无附件
        checkAddFile(resultList);

        // 产品认证/运输认证重复上传时，复用已存在证书主键并通过新附件覆盖展示结果
        bindOverwriteCertificateId(resultList);

        //数据验证
        checkProductCertificate(resultList);

        //新增数据
        this.saveOrUpdateBatch(resultList);
        //上传附件
        uploadFile (resultList);
        //操作日志
        List<OperateLogEntity> operateLogEntityList = new LinkedList<>();
        resultList.forEach(obj -> {
            operateLogEntityList.add(
                    new OperateLogEntity().setContent("新增了一个【产品证书】")
                            .setBusinessId(obj.getSkuId())
                            .setClassPath(String.valueOf(ProductCertificateEntity.class))
                            .setPid(obj.getId())
                            .setOperation("新增操作")
            );
        });
        operateLogService.addSysLogByBatchSave(operateLogEntityList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ProductCertificateDTO.UpdateDTO dto) {
        ProductCertificateEntity old = this.getById(dto.getId());
        ProductCertificateEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "产品认证"));

        ProductCertificateEntity entity = new ProductCertificateEntity();
        entity.setId(dto.getId());
        entity.setRemark(dto.getRemark());
        entity.setMultipartFile(dto.getMultipartFile());
        entity.setCertificateValidTime(ObjectUtil.isEmpty(dto.getCertificateValidTimeStr()) ? null : LocalDate.parse(dto.getCertificateValidTimeStr(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        log.info("编辑 开始更新产品认证", entity.getId());
        //更新主表数据
        this.updateById(entity);
        //上传附件
        uploadFile(Arrays.asList(entity));
        //删除附件
        deleteFile(dto.getRemoveFileIdList(),dto.getId());

        // 记录产品认证操作日志
        log.info("编辑 开始记录产品认证日志数据，id：【{}】", entity.getId());
        String msg = format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "产品认证");
        operateLogService.addSysLogByUpdate(old, entity, String.valueOf(ProductCertificateEntity.class),oldEntity.getSkuId(),entity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void productAddOrUpdate(List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList) {
        //结果集
        List<ProductCertificateEntity> resultList = new ArrayList<>();

        //产品信息
        List<String> skuIdList = productCertificateList.stream().map(ProductCertificateDTO.ProductAddOrUpdateDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listByIds(skuIdList);

        for (ProductCertificateDTO.ProductAddOrUpdateDTO productAddOrUpdateDTO : productCertificateList) {
            //新增数据附件不能为空
            if (isBlank(productAddOrUpdateDTO.getId()) && isBlank(productAddOrUpdateDTO.getAttachmentId())) {
                throw new ServiceException(ApiError.COMMON_PARAM_TIME_REQUIRED,"新增附件");
            }
            ProductCertificateEntity entity = BeanMapperUtils.map(ProductCertificateEntity.class, productAddOrUpdateDTO);
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), productAddOrUpdateDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
            }
            entity.setProductId(productDetailEntity.getProductId());
            resultList.add(entity);
        }
        List<String> lockKeys = buildCertificateLockKeys(resultList);
        ApplicationContextUtils.getBean(ProductCertificateServiceImpl.class)
                .productAddOrUpdateWithLock(lockKeys, resultList, productCertificateList, productDetailEntityList);
    }

    @DistributeLocker(businessType = PRODUCT_CERTIFICATE_LOCK_BIZ, keyName = "lockKeys", waiteTime = 60)
    @Transactional(rollbackFor = Exception.class)
    public void productAddOrUpdateWithLock(List<String> lockKeys,
                                           List<ProductCertificateEntity> resultList,
                                           List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList,
                                           List<ProductDetailEntity> productDetailEntityList) {
        // 与证书管理上传保持一致：新增/编辑时都校验证书项目唯一性（编辑排除自身）
        checkProductCertificateForProductAddOrUpdate(resultList, productDetailEntityList);
        //新增数据
        this.saveOrUpdateBatch(resultList);
        //绑定附件id
        updateAttachmentId(resultList);
        //删除附件
        productCertificateList.forEach(obj -> deleteFile(obj.getRemoveFileIdList(),obj.getId()));

        //操作日志
        List<OperateLogEntity> operateLogEntityList = new LinkedList<>();
        resultList.forEach(obj -> {
            operateLogEntityList.add(
                    new OperateLogEntity().setContent("新增了一个【产品证书】")
                            .setBusinessId(obj.getSkuId())
                            .setClassPath(String.valueOf(ProductCertificateEntity.class))
                            .setPid(obj.getId())
                            .setOperation("编辑操作")
            );
        });
        operateLogService.addSysLogByBatchSave(operateLogEntityList);
    }

    /**
     * @description: 绑定附件id
     * @author Will
     * @date: 2024/2/21 18:06
     * @param resultList
     */
    private void updateAttachmentId (List<ProductCertificateEntity> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        List<PlmAttachmentEntity> list = new ArrayList<>();
        for (ProductCertificateEntity entity : resultList) {
            PlmAttachmentEntity attachmentEntity = new PlmAttachmentEntity();
            attachmentEntity.setId(entity.getAttachmentId());
            attachmentEntity.setBusinessId(entity.getId());
            list.add(attachmentEntity);
        }
        plmAttachmentService.saveOrUpdateBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySkuIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return;
        }
        List<ProductCertificateEntity> productCertificateList = listBySkuIdList(skuIdList);
        if (CollectionUtils.isEmpty(productCertificateList)) {
            return;
        }
        List<String> ids = productCertificateList.stream().map(ProductCertificateEntity::getId).collect(Collectors.toList());
        //删除
        delete(ids);
    }


    /**
     * @description: 删除附件
     * @author Will
     * @date: 2024/2/19 16:12
     * @param removeFileIdList
     * @param businessId
     */
    private void deleteFile (List<String> removeFileIdList,String businessId) {
        if (CollectionUtils.isEmpty(removeFileIdList) || isBlank(businessId)) {
            return;
        }
        ProductCertificateEntity productCertificateEntity = this.getById(businessId);
        if (ObjectUtil.isEmpty(productCertificateEntity)) {
            throw new ServiceException(ApiError.COMMON_PARAM_TIME_REQUIRED,"产品证书");
        }

        List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByBusinessIds(Arrays.asList(businessId));
        long count = attachmentList.stream().filter(obj -> !removeFileIdList.contains(obj.getId())).count();
        if (count <= 0) {
            throw new ServiceException(ApiError.FILE_NOT_DELETE_ALL);
        }
        List<PlmAttachmentEntity> removeFileList = attachmentList.stream().filter(obj -> removeFileIdList.contains(obj.getId())).collect(Collectors.toList());
        //删除附件表数据
        plmAttachmentService.removeByIds(removeFileIdList);
        for (PlmAttachmentEntity entity : removeFileList) {
            //fastdfs删除附件
            fileFeign.deleteFile(entity.getAttachUrl());
        }
        //操作日志
        List<OperateLogEntity> operateLogEntityList = new LinkedList<>();
        removeFileList.forEach(obj -> {
            operateLogEntityList.add(
                    new OperateLogEntity().setContent(format("删除了一个产品证书【{}】",obj.getAttachName()))
                            .setBusinessId(productCertificateEntity.getSkuId())
                            .setClassPath(String.valueOf(ProductCertificateEntity.class))
                            .setPid(businessId)
                            .setOperation("删除附件")
            );
        });
        operateLogService.addSysLogByBatchSave(operateLogEntityList);
    }

    @Override
    public ProductCertificateDTO.ViewDTO view(String id) {
        ProductCertificateEntity old = this.getById(id);
        ProductCertificateEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "产品认证信息"));
        ProductCertificateDTO.ViewDTO viewDTO = new ProductCertificateDTO.ViewDTO();
        BeanMapperUtils.copy(oldEntity,viewDTO);
        //数据处理
        handleView(viewDTO);
        return viewDTO;
    }

    /**
     * @description: 处理查看详情数据
     * @author Will
     * @date: 2024/2/19 16:24
     * @param viewDTO
     */
    private void handleView (ProductCertificateDTO.ViewDTO viewDTO) {
        //证书类型
        List<BasicDictEntity> certificateTypeList = basicDictService.listByType(BasicDictTypeEnum.CERTIFICATE_TYPE.getCode());
        //证书项目
        List<BasicDictEntity> certificateProjectList = basicDictService.listByType(BasicDictTypeEnum.CERTIFICATE_PROJECT.getCode());
        //证书类型名称
        String typeName = certificateTypeList.stream().filter(obj -> CharSequenceUtil.equals(viewDTO.getType(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        viewDTO.setTypeName(typeName);
        //证书项目名称
        String dictProjectName = certificateProjectList.stream().filter(obj -> CharSequenceUtil.equals(viewDTO.getDictProject(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        viewDTO.setDictProjectName(dictProjectName);

        //产品信息
        ProductDetailEntity productDetailEntity = productDetailService.getById(viewDTO.getSkuId());
        if (ObjectUtil.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.PRODUCT_INFO_NOT_FOUND);
        }
        viewDTO.setSkuNo(productDetailEntity.getSkuNo());

        //查询历史附件
        List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByBusinessIds(Arrays.asList(viewDTO.getId()));
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<AttachmentDTO.ListDTO> list = BeanMapperUtils.copyList(AttachmentDTO.ListDTO.class, attachmentList);
            viewDTO.setHistoryFileList(list);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(List<String> ids) {
        log.info("编辑 开始更新产品认证,ids={}", ids);
        List<ProductCertificateEntity> entityList = this.listByIds(ids);
        //删除认证信息
       this.removeByIds(ids);

        //删除附件
        List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByBusinessIds(ids);
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            //删除附件表数据
            List<String> attachmentIdList = attachmentList.stream().map(PlmAttachmentEntity::getId).collect(Collectors.toList());
            plmAttachmentService.removeByIds(attachmentIdList);
            for (PlmAttachmentEntity entity : attachmentList) {
                //fastdfs删除附件
                fileFeign.deleteFile(entity.getAttachUrl());
            }
        } else {
            log.warn("产品证书删除时未找到附件，按无附件脏数据兼容处理, certificateIds={}", ids);
        }
        Map<String, ProductDetailEntity> stringProductDetailEntityMap = productDetailService.getByIdList(entityList.stream().map(ProductCertificateEntity::getSkuId).collect(Collectors.toList())).stream().collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity(),(v1,v2)->v1));
        return entityList.stream()
                .map(
                entity->BatchResultDTO.success(entity.getId(),stringProductDetailEntityMap.get(entity.getSkuId())!=null?stringProductDetailEntityMap.get(entity.getSkuId()).getSkuNo():entity.getSkuId()))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        ProductCertificateExcelListener excelListenerUtil = new ProductCertificateExcelListener();
        try {
            EasyExcelFactory.read(excelFile.getInputStream(), ProductCertificateExcelDTO.class, excelListenerUtil)
                    .sheet(0)
                    .doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        List<ProductCertificateExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        List<ProductCertificateExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<ProductCertificateExcelDTO> successList = excelListenerUtil.getSuccessList();
        List<String> skuNoList = successList.stream().map(ProductCertificateExcelDTO::getSkuNo).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listBySkuNoList(skuNoList);
        //处理验证成功数据
        handleImportSuccessList(successList, errorList, skuList);

        if (errorList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            String excelPath = "excel/productCertificateError.xlsx";
            String name = "productCertificate";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.FILE_EXPORT_ERROR_DATA_FAILED);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
     * @description: 处理导入成功数据
     * @author Will
     * @date: 2024/2/20 10:29
     * @param successList
     * @param errorList
     * @param skuList
     */
    private void handleImportSuccessList (List<ProductCertificateExcelDTO> successList,
                                          List<ProductCertificateExcelDTO> errorList,
                                          List<ProductDetailEntity> skuList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        Map<String, ProductDetailEntity> skuNoMap = skuList.stream()
                .filter(obj -> !isBlank(obj.getSkuNo()))
                .collect(Collectors.toMap(ProductDetailEntity::getSkuNo, Function.identity(), (left, right) -> left));
        Map<String, String> skuIdSkuNoMap = skuList.stream()
                .filter(obj -> !isBlank(obj.getId()))
                .collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getSkuNo, (left, right) -> left));

        //产品认证
        List<BasicDictEntity> productAttestationList = basicDictService.listByType(BasicDictTypeEnum.PRODUCT_ATTESTATION.getCode());
        //运输认证
        List<BasicDictEntity> transportAttestationList = basicDictService.listByType(BasicDictTypeEnum.TRANSPORT_ATTESTATION.getCode());
        //其他认证
        List<BasicDictEntity> otherAttestationList = basicDictService.listByType(BasicDictTypeEnum.OTHER_ATTESTATION.getCode());
        //配置信息
        Map<SettingEnum, String> cfgSettingList = dmpTaskFeign.getCfgSettingList(SettingEnum.URL_CHANGE);
        // 批量拉取一次当前已存在的有效证书键，避免循环里重复查整批附件
        Set<String> validCertificateKeySet = loadValidCertificateKeySet(skuList, successList);

        //新增的数据
        List<ProductCertificateExcelDTO> resultList = new ArrayList<>();

        for (ProductCertificateExcelDTO excelDTO : successList) {

            List<String> errorMsgList = new ArrayList<>();
            //产品认证
            if (CharSequenceUtil.equals(ProductCertificateTypeEnum.PRODUCT_ATTESTATION.getName(),excelDTO.getTypeName())) {
                long count = productAttestationList.stream().filter(obj -> CharSequenceUtil.equals(excelDTO.getDictProjectName(), obj.getName())).count();
                if (count == 0) {
                    errorMsgList.add("产品认证下未找到证书项目");
                }
            }
            //运输认证
            if (CharSequenceUtil.equals(ProductCertificateTypeEnum.TRANSPORT_ATTESTATION.getName(),excelDTO.getTypeName())) {
                long count = transportAttestationList.stream().filter(obj -> CharSequenceUtil.equals(excelDTO.getDictProjectName(), obj.getName())).count();
                if (count == 0) {
                    errorMsgList.add("运输认证下未找到证书项目");
                }
                if (isBlank(excelDTO.getCertificateValidTimeStr())) {
                    errorMsgList.add("运输认证有效期不能为空");
                }
            }
            //其他认证
            if (CharSequenceUtil.equals(ProductCertificateTypeEnum.OTHER_ATTESTATION.getName(),excelDTO.getTypeName())) {
                long count = otherAttestationList.stream().filter(obj -> CharSequenceUtil.equals(excelDTO.getDictProjectName(), obj.getName())).count();
                if (count == 0) {
                    errorMsgList.add("其他认证下未找到证书项目");
                }
            }
            //产品信息
            ProductDetailEntity productDetailEntity = skuNoMap.get(excelDTO.getSkuNo());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                errorMsgList.add("系统中未找到SKU");
            }
            //校验传进来的参数是否重复
            if (!CharSequenceUtil.equals(ProductCertificateTypeEnum.OTHER_ATTESTATION.getName(),excelDTO.getTypeName())) {
                long count = resultList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())
                                && CharSequenceUtil.equals(obj.getDictProjectName(), excelDTO.getDictProjectName()))
                        .count();
                if (count > 0) {
                    errorMsgList.add(format(ApiError.PRODUCT_CERTIFICATE_EXISTS.getMsg(),excelDTO.getSkuNo(), excelDTO.getDictProjectName()));
                }
            }
            String pathUrl = excelDTO.getPathUrl();
            MultipartFile multipartFile = null;
            try {
                multipartFile = getMulFileByPath(pathUrl,cfgSettingList);
            } catch (Exception e) {
                errorMsgList.add(isBlank(e.getMessage()) ? "文件路径下未找到文件" : e.getMessage());
            }
            if ((ObjectUtil.isEmpty(multipartFile) || multipartFile.isEmpty())
                    && errorMsgList.stream().noneMatch(msg -> CharSequenceUtil.contains(msg, "未找到文件")
                    || CharSequenceUtil.contains(msg, "获取共享文件失败"))) {
                errorMsgList.add("文件路径下未找到文件");
            }
            if (!ObjectUtil.isEmpty(multipartFile) && isBlank(multipartFile.getOriginalFilename())) {
                errorMsgList.add("导入文件名称未找到");
            }
            //存在错误信息则
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                continue;
            }
            ProductCertificateEntity entity = new ProductCertificateEntity();
            entity.setSkuId(productDetailEntity.getId());
            entity.setProductId(productDetailEntity.getProductId());
            entity.setType(ProductCertificateTypeEnum.getCode(excelDTO.getTypeName()));
            entity.setDictProject(ProductCertificateProjectEnum.getCode(excelDTO.getDictProjectName()));
            entity.setCertificateValidTime(ObjectUtil.isEmpty(excelDTO.getCertificateValidTimeStr()) ? null : LocalDate.parse(excelDTO.getCertificateValidTimeStr(), DateTimeFormatter.ofPattern("yyyy/M/d")));
            entity.setMultipartFile(multipartFile);
            entity.setRemark(excelDTO.getRemark());
            //数据验证
            try {
                checkImportCertificateExists(entity, skuIdSkuNoMap, validCertificateKeySet);
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //存在错误信息则
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            try {
                ApplicationContextUtils.getBean(ProductCertificateServiceImpl.class)
                        .saveImportCertificateWithLock(buildCertificateLockKey(entity.getSkuId(), entity.getDictProject()),
                                entity,
                                skuIdSkuNoMap);
                if (!ProductCertificateProjectEnum.OTHER_CERTIFICATE.getCode().equals(entity.getDictProject())) {
                    validCertificateKeySet.add(buildCertificateLockKey(entity.getSkuId(), entity.getDictProject()));
                }
                resultList.add(excelDTO);
            } catch (Exception e) {
                if (!isBlank(entity.getId())) {
                    this.removeById(entity.getId());
                }
                errorMsgList.add(e.getMessage());
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
            }
        }
    }

    @DistributeLocker(businessType = PRODUCT_CERTIFICATE_LOCK_BIZ, keyName = "lockKey", waiteTime = 60)
    @Transactional(rollbackFor = Exception.class)
    public void saveImportCertificateWithLock(String lockKey,
                                              ProductCertificateEntity entity,
                                              Map<String, String> skuIdSkuNoMap) {
        checkImportCertificateExists(entity, skuIdSkuNoMap, null);
        //新增数据
        this.saveOrUpdate(entity);
        //上传附件
        uploadFile(Collections.singletonList(entity));
        //操作日志
        operateLogService.addSysLogByBatchSave(Collections.singletonList(
                new OperateLogEntity().setContent("新增了一个【产品证书】")
                        .setBusinessId(entity.getSkuId())
                        .setClassPath(String.valueOf(ProductCertificateEntity.class))
                        .setPid(entity.getId())
                        .setOperation("新增操作")
        ));
    }

    private List<String> buildCertificateLockKeys(List<ProductCertificateEntity> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        return resultList.stream()
                .map(entity -> buildCertificateLockKey(entity.getSkuId(), entity.getDictProject()))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private String buildCertificateLockKey(String skuId, String dictProject) {
        if (isBlank(skuId) || isBlank(dictProject)) {
            return null;
        }
        return skuId.concat(":").concat(dictProject);
    }

    private Set<String> loadValidCertificateKeySet(List<ProductDetailEntity> skuList,
                                                   List<ProductCertificateExcelDTO> successList) {
        Set<String> validCertificateKeySet = new HashSet<>();
        if (CollectionUtils.isEmpty(skuList) || CollectionUtils.isEmpty(successList)) {
            return validCertificateKeySet;
        }
        List<String> skuIdList = skuList.stream()
                .map(ProductDetailEntity::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<String> dictProjectList = successList.stream()
                .map(ProductCertificateExcelDTO::getDictProjectName)
                .map(ProductCertificateProjectEnum::getCode)
                .filter(StringUtils::isNotBlank)
                .filter(dictProject -> !ProductCertificateProjectEnum.OTHER_CERTIFICATE.getCode().equals(dictProject))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIdList) || CollectionUtils.isEmpty(dictProjectList)) {
            return validCertificateKeySet;
        }
        List<ProductCertificateEntity> productCertificateList = productCertificateMapper
                .listValidCertificateKeys(skuIdList, dictProjectList);
        if (CollectionUtils.isEmpty(productCertificateList)) {
            return validCertificateKeySet;
        }
        productCertificateList.stream()
                .map(obj -> buildCertificateLockKey(obj.getSkuId(), obj.getDictProject()))
                .filter(StringUtils::isNotBlank)
                .forEach(validCertificateKeySet::add);
        return validCertificateKeySet;
    }

    private void checkImportCertificateExists(ProductCertificateEntity entity,
                                              Map<String, String> skuIdSkuNoMap,
                                              Set<String> validCertificateKeySet) {
        if (ObjectUtils.isEmpty(entity)
                || ProductCertificateProjectEnum.OTHER_CERTIFICATE.getCode().equals(entity.getDictProject())) {
            return;
        }
        String certificateKey = buildCertificateLockKey(entity.getSkuId(), entity.getDictProject());
        boolean exists = CollectionUtils.isNotEmpty(validCertificateKeySet)
                ? validCertificateKeySet.contains(certificateKey)
                : productCertificateMapper.existsValidCertificate(entity.getSkuId(), entity.getDictProject(), entity.getId());
        if (!exists) {
            return;
        }
        String skuNo = ObjectUtils.isEmpty(skuIdSkuNoMap) ? "" : skuIdSkuNoMap.getOrDefault(entity.getSkuId(), "");
        throw new ServiceException(ApiError.PRODUCT_CERTIFICATE_EXISTS,
                skuNo,
                ProductCertificateProjectEnum.getName(entity.getDictProject()));
    }

    /**
     * 获取MultipartFile
     */
    private  MultipartFile getMulFileByPath(String filePath,Map<SettingEnum, String> cfgSettingMap) {
        String normalizedPath = normalizeImportPath(filePath);
        //查询配置进行转换
        String urlPath = toFilePath(normalizedPath, cfgSettingMap);
        if (!isSambaPath(urlPath)) {
            String finalUrlPath = urlPath;
            if (isWindowsDrivePath(finalUrlPath)) {
                finalUrlPath = "file:///".concat(finalUrlPath.replace("\\", "/"));
            }
            return FileUtil.toMultipartFile(finalUrlPath);
        }
        return getSambaMultipartFile(urlPath);
    }

    /**
     * 获取共享目录文件
     */
    private MultipartFile getSambaMultipartFile(String filePath) {
        //配置信息
        Map<SettingEnum, String> nasUserMap = dmpTaskFeign.getCfgSettingList(SettingEnum.NAS_USERNAME_PWD);
        if (ObjectUtil.isEmpty(nasUserMap) || isBlank(nasUserMap.get(SettingEnum.PLM_NAS_USERNAME_PWD))) {
            throw new ServiceException("未找到共享文件配置信息: PLM_NAS_USERNAME_PWD");
        }
        List<String> nasUserList = Arrays.stream(nasUserMap.get(SettingEnum.PLM_NAS_USERNAME_PWD).split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        if (nasUserList.size() != 2 || isBlank(nasUserList.get(0)) || isBlank(nasUserList.get(1))) {
            throw new ServiceException("共享文件配置格式错误: PLM_NAS_USERNAME_PWD");
        }
        try {
            return SambaUtil.toMultipartFile(normalizeSambaPath(filePath), nasUserList.get(0), nasUserList.get(1));
        } catch (Exception e) {
            String errorMsg = buildSambaErrorMsg(e);
            log.error("读取共享文件失败,path={},msg={}", normalizeSambaPath(filePath), errorMsg, e);
            throw new ServiceException(errorMsg);
        }
    }

    /**
     * 构建共享文件读取错误信息
     */
    private String buildSambaErrorMsg(Exception e) {
        String rawMsg = getRootCauseMessage(e);
        if (isBlank(rawMsg)) {
            return "获取共享文件失败";
        }
        String msg = rawMsg.toLowerCase();
        if (msg.contains("logon failure")
                || msg.contains("status_logon_failure")
                || msg.contains("authentication")) {
            return "获取共享文件失败: 共享账号或密码错误";
        }
        if (msg.contains("access is denied")
                || msg.contains("status_access_denied")
                || msg.contains("permission denied")) {
            return "获取共享文件失败: 共享目录无读取权限";
        }
        if (msg.contains("unknown host")
                || msg.contains("unknownhostexception")) {
            return "获取共享文件失败: 共享服务器地址无法解析";
        }
        if (msg.contains("network name cannot be found")
                || msg.contains("bad network name")
                || msg.contains("status_bad_network_name")) {
            return "获取共享文件失败: 共享名称错误";
        }
        if (msg.contains("connection refused")
                || msg.contains("connect timed out")
                || msg.contains("no route to host")
                || msg.contains("network is unreachable")
                || msg.contains("failed to connect")) {
            return "获取共享文件失败: 共享服务器网络不可达";
        }
        if (msg.contains("object name not found")
                || msg.contains("status_object_name_not_found")
                || msg.contains("no such file")
                || msg.contains("file not found")
                || msg.contains("cannot find the path specified")
                || msg.contains("path not found")) {
            return "共享路径下未找到文件";
        }
        return "获取共享文件失败: " + rawMsg;
    }

    /**
     * 获取最底层异常信息
     */
    private String getRootCauseMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null && cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause == null ? "" : cause.getMessage();
    }

    /**
     * 是否走共享目录读取
     */
    private boolean isSambaPath(String filePath) {
        if (isBlank(filePath)) {
            return false;
        }
        String path = normalizeImportPath(filePath).replace("\\", "/");
        if (StringUtils.startsWithIgnoreCase(path, "smb://") || path.startsWith("//")) {
            return true;
        }
        // 带协议(http/https/file)按URL读取
        if (path.matches("^[a-zA-Z][a-zA-Z0-9+\\-.]*://.*")) {
            return false;
        }
        // 盘符路径按URL/file读取
        if (path.matches("^[a-zA-Z]:/.*")) {
            return false;
        }
        if (!path.contains("/")) {
            return false;
        }
        String hostPart = StringUtils.substringBefore(path, "/");
        return hostPart.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$") || hostPart.contains(".");
    }

    /**
     * 是否是Windows盘符路径
     */
    private boolean isWindowsDrivePath(String filePath) {
        if (isBlank(filePath)) {
            return false;
        }
        String path = normalizeImportPath(filePath).replace("\\", "/");
        return path.matches("^[a-zA-Z]:/.*");
    }

    /**
     * 共享路径标准化
     */
    private String normalizeSambaPath(String filePath) {
        String path = normalizeImportPath(filePath).replace("\\", "/");
        path = StringUtils.removeStartIgnoreCase(path, "smb://");
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    /**
     * 导入路径标准化
     */
    private String normalizeImportPath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.trim().replace('￥', '\\').replace('¥', '\\');
    }

    /**
     * @description: 格式化url
     * @author Will
     * @date: 2024/4/3 15:41
     * @param filePath
     * @param cfgSettingMap
     * @return String
     */
    private String toFilePath (String filePath,Map<SettingEnum, String> cfgSettingMap) {
        String normalizedPath = normalizeImportPath(filePath);
        if (ObjectUtil.isEmpty(cfgSettingMap)) {
            return  normalizedPath;
        }
        String importUrlSetting = cfgSettingMap.get(SettingEnum.PLM_PRODUCT_CERTIFICATE_IMPORT_URL);
        if (isBlank(importUrlSetting)) {
            return  normalizedPath;
        }
        String[] urlArr = importUrlSetting.split(",", 2);
        if (urlArr.length != 2 || isBlank(urlArr[0]) || isBlank(urlArr[1])) {
            return  normalizedPath;
        }
        String sourcePrefix = normalizeImportPath(urlArr[0]).replace("\\", "/");
        String targetPrefix = normalizeImportPath(urlArr[1]).replace("\\", "/");
        String currentPath = normalizedPath.replace("\\", "/");
        // 去掉末尾斜杠，避免因为配置斜杠差异匹配不上
        sourcePrefix = StringUtils.removeEnd(sourcePrefix, "/");
        if (!StringUtils.startsWithIgnoreCase(currentPath, sourcePrefix)) {
            return  normalizedPath;
        }
        if (currentPath.length() > sourcePrefix.length() && currentPath.charAt(sourcePrefix.length()) != '/') {
            return normalizedPath;
        }
        String removePath = currentPath.substring(sourcePrefix.length());
        if (isBlank(removePath)) {
            return targetPrefix;
        }
        if (targetPrefix.endsWith("/") && removePath.startsWith("/")) {
            return targetPrefix.concat(removePath.substring(1));
        }
        if (!targetPrefix.endsWith("/") && !removePath.startsWith("/")) {
            return targetPrefix.concat("/").concat(removePath);
        }
        return targetPrefix.concat(removePath);
    }

    @Override
    public Boolean exportExcel(ProductCertificateDTO.ExportParamDTO params) {
        downloadTaskFeign.saveDownloadTask("产品认证列表",EXPORT_PLM_PRODUCT_CERTIFICATE.getCode(), params);
        return Boolean.TRUE;
    }



    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCertificateShowDTO>
     **/
    @Override
    public List<ProductCertificateShowDTO> list(String productId) {
        List<ProductCertificateShowDTO> list = productCertificateMapper.list(productId);
        //数据处理
        handleProductCertificateShow(list);
        return list;
    }

    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param skuId
     * @return java.util.List<com.erp.model.plm.dto.ProductCertificateShowDTO>
     **/
    @Override
    public List<ProductCertificateShowDTO> listBySkuId(String skuId) {
        List<ProductCertificateShowDTO> list = productCertificateMapper.listBySkuId(skuId);
        //数据处理
        handleProductCertificateShow(list);
        return list;
    }

    /**
     * @description:查询数据处理
     * @author Will
     * @date: 2024/2/27 16:58
     * @param list
     */
    private void handleProductCertificateShow (List<ProductCertificateShowDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //证书类型
        List<BasicDictEntity> certificateTypeList = basicDictService.listByType(BasicDictTypeEnum.CERTIFICATE_TYPE.getCode());

        //证书项目
        List<BasicDictEntity> certificateProjectList = basicDictService.listByType(BasicDictTypeEnum.CERTIFICATE_PROJECT.getCode());
        for (ProductCertificateShowDTO showDTO : list) {
            //证书类型名称
            String typeName = certificateTypeList.stream().filter(obj -> CharSequenceUtil.equals(showDTO.getType(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            showDTO.setTypeName(typeName);
            //证书项目名称
            String dictProjectName = certificateProjectList.stream().filter(obj -> CharSequenceUtil.equals(showDTO.getDictProject(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            showDTO.setDictProjectName(dictProjectName);
        }
    }

    @Override
    public List<ProductCertificateEntity> listBySkuIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<ProductCertificateEntity> list = this.lambdaQuery().in(ProductCertificateEntity::getSkuId, skuIdList).list();
        return list;
    }

    @Override
    public PagingVO<ProductCertificateDTO.ListDTO> exportProductCertificate(PagingDTO<ProductCertificateDTO.ExportParamDTO> dto) {
        Page<ProductCertificateDTO.ListDTO> page = this.baseMapper.exportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        //数据赋值处理
        handlePaging(page.getRecords());
        return new PagingVO<>(page);
    }

    /**
     * @description: 数据赋值处理
     * @author Will
     * @date: 2024/2/19 11:45
     * @param records
     */
    private void handlePaging(List<ProductCertificateDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //证书类型
        List<BasicDictEntity> certificateTypeList = basicDictService.listByType(BasicDictTypeEnum.CERTIFICATE_TYPE.getCode());

        //证书项目
        List<BasicDictEntity> certificateProjectList = basicDictService.listByType(BasicDictTypeEnum.CERTIFICATE_PROJECT.getCode());
        for (ProductCertificateDTO.ListDTO listDTO : records) {
            //证书类型名称
            String typeName = certificateTypeList.stream().filter(obj -> CharSequenceUtil.equals(listDTO.getType(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setTypeName(typeName);
            //证书项目名称
            String dictProjectName = certificateProjectList.stream().filter(obj -> CharSequenceUtil.equals(listDTO.getDictProject(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setDictProjectName(dictProjectName);

            //附件全路径
            listDTO.setFullAttachUrl(format("{}{}",FastDFSClientUtil.publicUrl,listDTO.getAttachUrl()));
        }
    }


    /**
     * @description: 验证重复
     * @author Will
     * @date: 2024/2/27 18:35
     * @param resultList
     */
    private void checkProductCertificate (List<ProductCertificateEntity> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        //产品信息
        List<String> skuIdList = resultList.stream().map(ProductCertificateEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listByIds(skuIdList);

        List<String> dictProductList = resultList.stream().map(ProductCertificateEntity::getDictProject).distinct().collect(Collectors.toList());
        List<ProductCertificateEntity> productCertificateList = listBySkuListAndDictProductList(skuIdList, dictProductList);
        Set<String> currentIdSet = resultList.stream().map(ProductCertificateEntity::getId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(productCertificateList) && CollectionUtils.isNotEmpty(currentIdSet)) {
            productCertificateList = productCertificateList.stream().filter(obj -> !currentIdSet.contains(obj.getId())).collect(Collectors.toList());
        }

        checkProductCertificateParam(resultList,productDetailEntityList,productCertificateList);
    }

    /**
     * @description: 参数传递验证
     * @author Will
     * @date: 2024/3/20 12:20
     * @param resultList
     * @param productDetailEntityList
     * @param productCertificateList
     */
    private void checkProductCertificateParam (List<ProductCertificateEntity> resultList,List<ProductDetailEntity> productDetailEntityList,List<ProductCertificateEntity> productCertificateList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        Map<String, List<ProductCertificateEntity>> map = resultList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getDictProject())));
        for (Map.Entry<String, List<ProductCertificateEntity>> entry : map.entrySet()) {
            List<ProductCertificateEntity> value = entry.getValue();
            // 其他认证允许同证书项目继续新增，不拦截重复
            if (isAppendCertificateType(value.get(0))) {
                continue;
            }
            //验证保存时数据是否重复
            if (value.size() > MathUtil.ONE) {
                String skuNo = productDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), value.get(0).getSkuId())).map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
                throw new ServiceException(ApiError.PRODUCT_CERTIFICATE_EXISTS,skuNo, ProductCertificateProjectEnum.getName(value.get(0).getDictProject()));
            }
            //验证是否和已存在数据重复
            long count = productCertificateList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), value.get(0).getSkuId()) && CharSequenceUtil.equals(obj.getDictProject(), value.get(0).getDictProject())).count();
            if (count > 0) {
                String skuNo = productDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), value.get(0).getSkuId())).map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
                throw new ServiceException(ApiError.PRODUCT_CERTIFICATE_EXISTS,skuNo, ProductCertificateProjectEnum.getName(value.get(0).getDictProject()));
            }
        }

    }

    private void bindOverwriteCertificateId(List<ProductCertificateEntity> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        List<ProductCertificateEntity> overwriteList = resultList.stream()
                .filter(this::isOverwriteCertificateType)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(overwriteList)) {
            return;
        }
        List<String> skuIdList = overwriteList.stream()
                .map(ProductCertificateEntity::getSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<String> dictProjectList = overwriteList.stream()
                .map(ProductCertificateEntity::getDictProject)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<ProductCertificateEntity> existedCertificateList = listBySkuListAndDictProductList(skuIdList, dictProjectList);
        if (CollectionUtils.isEmpty(existedCertificateList)) {
            return;
        }
        Map<String, ProductCertificateEntity> existedCertificateMap = existedCertificateList.stream()
                .collect(Collectors.toMap(this::buildCertificateUniqueKey, Function.identity(), (oldValue, newValue) -> newValue));
        overwriteList.forEach(entity -> {
            ProductCertificateEntity existedCertificate = existedCertificateMap.get(buildCertificateUniqueKey(entity));
            if (ObjectUtils.isEmpty(existedCertificate)) {
                return;
            }
            entity.setId(existedCertificate.getId());
        });
    }

    private String buildCertificateUniqueKey(ProductCertificateEntity entity) {
        if (ObjectUtils.isEmpty(entity) || isBlank(entity.getSkuId()) || isBlank(entity.getDictProject())) {
            return "";
        }
        return entity.getSkuId().concat(":").concat(entity.getDictProject());
    }

    private boolean isOverwriteCertificateType(ProductCertificateEntity entity) {
        return !ObjectUtils.isEmpty(entity) && OVERWRITE_CERTIFICATE_TYPE_SET.contains(entity.getType());
    }

    private boolean isAppendCertificateType(ProductCertificateEntity entity) {
        return !ObjectUtils.isEmpty(entity)
                && CharSequenceUtil.equals(entity.getType(), ProductCertificateTypeEnum.OTHER_ATTESTATION.getCode());
    }

    /**
     * 产品管理页面保存证书时的重复校验：
     * 1. 校验请求内重复
     * 2. 校验与库内重复（更新场景排除自身id）
     */
    private void checkProductCertificateForProductAddOrUpdate(List<ProductCertificateEntity> resultList, List<ProductDetailEntity> productDetailEntityList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        List<String> skuIdList = resultList.stream().map(ProductCertificateEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> dictProductList = resultList.stream().map(ProductCertificateEntity::getDictProject).distinct().collect(Collectors.toList());
        List<ProductCertificateEntity> productCertificateList = listBySkuListAndDictProductList(skuIdList, dictProductList);
        Set<String> currentIdSet = resultList.stream().map(ProductCertificateEntity::getId).filter(id -> !isBlank(id)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(productCertificateList) && CollectionUtils.isNotEmpty(currentIdSet)) {
            productCertificateList = productCertificateList.stream().filter(obj -> !currentIdSet.contains(obj.getId())).collect(Collectors.toList());
        }
        checkProductCertificateParam(resultList, productDetailEntityList, productCertificateList);
    }

    /**
     * @description: 查询认证
     * @author Will
     * @date: 2024/2/27 18:27
     * @param skuIdList
     * @param dictProductList
     * @return List<ProductCertificateEntity>
     */
    private List<ProductCertificateEntity> listBySkuListAndDictProductList (List<String> skuIdList,List<String> dictProductList) {
        List<ProductCertificateEntity> list = lambdaQuery().in(CollectionUtils.isNotEmpty(skuIdList),ProductCertificateEntity::getSkuId, skuIdList)
                .in(CollectionUtils.isNotEmpty(dictProductList),ProductCertificateEntity::getDictProject, dictProductList)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> businessIds = list.stream().map(ProductCertificateEntity::getId).collect(Collectors.toList());
        List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByBusinessIds(businessIds);
        if (CollectionUtils.isEmpty(attachmentList)) {
            return Collections.emptyList();
        }
        Set<String> validBusinessIdSet = attachmentList.stream().map(PlmAttachmentEntity::getBusinessId).collect(Collectors.toSet());
        return list.stream().filter(obj -> validBusinessIdSet.contains(obj.getId())).collect(Collectors.toList());
    }

    /**
     * @description: 新增数据处理
     * @author Will
     * @date: 2024/2/19 15:46
     * @param dto
     * @return List<ProductCertificateEntity>
     */
    private List<ProductCertificateEntity> handleAdd(ProductCertificateDTO.AddDTO dto) {
        List<String> skuNoList = dto.getSkuNoList();
        List<ProductCertificateDTO.FileDTO> fileList = dto.getFileList();

        //产品信息
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listBySkuNoList(skuNoList);

        //结果集
        List<ProductCertificateEntity> resultList = new ArrayList<>();
        for (String skuNo : skuNoList) {
            for (ProductCertificateDTO.FileDTO fileDTO : fileList) {
                ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), skuNo)).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(productDetailEntity)) {
                    throw new ServiceException(format("SKU【{}】系统不存在",skuNo));
                }
                ProductCertificateEntity entity = new ProductCertificateEntity();
                entity.setSkuId(productDetailEntity.getId());
                entity.setProductId(productDetailEntity.getProductId());
                entity.setType(dto.getType());
                entity.setDictProject(fileDTO.getDictProject());
                entity.setMultipartFile(fileDTO.getMultipartFile());
                entity.setCertificateValidTime(ObjectUtil.isEmpty(dto.getCertificateValidTimeStr()) ? null : LocalDate.parse(dto.getCertificateValidTimeStr(),DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                entity.setRemark(dto.getRemark());
                resultList.add(entity);
            }
        }
        return resultList;
    }

    /**
     * 新增上传时，证书文件必填且不可为空文件
     */
    private void checkAddFile(List<ProductCertificateEntity> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        for (ProductCertificateEntity entity : resultList) {
            MultipartFile multipartFile = entity.getMultipartFile();
            if (ObjectUtil.isEmpty(multipartFile) || multipartFile.isEmpty()) {
                throw new ServiceException("证书文件不能为空");
            }
        }
    }
    public static String chineseToUnicode(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            int chr1 = (char) str.charAt(i);
            // 汉字范围 \u4e00 - \u9fa5 (中文)
            if (chr1 >= 19968 && chr1 <= 171941) {
                result += "\\u" + Integer.toHexString(chr1);
            } else {
                result += str.charAt(i);
            }
        }
        return result;
    }


    /**
     * @description: 上传文件
     * @author Will
     * @date: 2024/2/19 15:46
     * @param resultList
     */
    private void uploadFile (List<ProductCertificateEntity> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }

        HashMap<String,MultipartFile> map = new HashMap<>();
        List<PlmAttachmentEntity> attachmentList = new ArrayList<>();
        for (ProductCertificateEntity entity : resultList) {
            //附件
            MultipartFile multipartFile = entity.getMultipartFile();
            if (ObjectUtils.isEmpty(multipartFile)) {
                continue;
            }
            double size = multipartFile.getSize();
            double fileSize = size / (1024 * 1024);
            fileSize = (double) Math.round(fileSize * 10000) / 10000;
            if (fileSize > 300) {
                throw new ServiceException(ApiError.FILE_SIZE_EXCEEDS_LIMIT, 300);
            }
            //原名称
            String fileName = multipartFile.getOriginalFilename();
            if (ObjectUtils.isEmpty(fileName)) {
                throw new ServiceException("导入文件名称未找到");
            }
            fileName = fileName.toLowerCase();

            if (fileName.length() > 200) {
                throw new ServiceException(ApiError.COMMON_PARAM_NAME_TOO_LONG);
            }
            if (isBlank(fileName)) {
                try {
                    fileName = URLDecoder.decode(multipartFile.getName(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new ServiceException("导入文件名称转换失败");
                }
            }

            if (ObjectUtil.isEmpty(map.get(entity.getDictProject()))) {
                map.put(entity.getDictProject(),multipartFile);
            } else {
                multipartFile = map.get(entity.getDictProject());
            }
            String fileUrl = fileFeign.uploadFileAndName(multipartFile, fileName);
            if (StringUtils.isBlank(fileUrl)) {
                throw new ServiceException(ApiError.FILE_UPLOAD_FAILED);
            }
            PlmAttachmentEntity attachmentEntity = new PlmAttachmentEntity();
            attachmentEntity.setBusinessId(entity.getId());
            attachmentEntity.setAttachUrl(fileUrl);
            attachmentEntity.setAttachName(isBlank(fileName) ? multipartFile.getName().toLowerCase() : fileName);
            attachmentEntity.setType(ProductCertificateEntity.TABLE_NAME);
            attachmentEntity.setAttachSize(BigDecimal.valueOf(fileSize));
            attachmentList.add(attachmentEntity);
        }
        plmAttachmentService.saveBatch(attachmentList);

        //操作日志
        List<OperateLogEntity> operateLogEntityList = new LinkedList<>();
        attachmentList.forEach(obj -> {
            //skuId
            ProductCertificateEntity entity = resultList.stream().filter(e -> CharSequenceUtil.equals(e.getId(), obj.getBusinessId())).findFirst().orElse(new ProductCertificateEntity());
            operateLogEntityList.add(
                    new OperateLogEntity().setContent(format("新增了一个产品证书附件【{}】",obj.getAttachName()))
                            .setBusinessId(entity.getSkuId())
                            .setClassPath(String.valueOf(ProductCertificateEntity.class))
                            .setPid(obj.getBusinessId())
                            .setOperation("新增附件")
            );
        });
        operateLogService.addSysLogByBatchSave(operateLogEntityList);

    }

}




