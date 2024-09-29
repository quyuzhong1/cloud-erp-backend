package com.erp.server.plm.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.erp.server.plm.listener.ProductCertificateExcelListener;
import com.erp.server.plm.mapper.ProductCertificateMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_CERTIFICATE;

/**
 * 认证实现类
 */
@Service
@Slf4j
public class ProductCertificateServiceImpl extends ServiceImpl<ProductCertificateMapper, ProductCertificateEntity>
    implements ProductCertificateService {

    @Resource
    private ProductCertificateMapper productCertificateMapper;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private SysLogService sysLogService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private PlmAttachmentService plmAttachmentService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public PagingVO<ProductCertificateDTO.ListDTO> paging(PagingDTO<ProductCertificateDTO.SearchParamDTO> pagingDTO) {
        ProductCertificateDTO.SearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ProductCertificateDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        List<ProductCertificateDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        handlePaging(records);
        return new PagingVO(pageData);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ProductCertificateDTO.AddDTO dto) {
        List<ProductCertificateEntity> resultList = handleAdd(dto);

        //数据验证
        checkProductCertificate(resultList);

        //新增数据
        this.saveOrUpdateBatch(resultList);
        //上传附件
        uploadFile (resultList);
        //操作日志
        List<SysLogEntity> sysLogEntityList = new LinkedList<>();
        resultList.forEach(obj -> {
            sysLogEntityList.add(
                    new SysLogEntity().setContent(String.format("新增了一个【产品证书】"))
                            .setBusinessId(obj.getSkuId())
                            .setPid(obj.getId())
                            .setOperation("新增操作")
            );
        });
        sysLogService.addSysLogByBatchSave(sysLogEntityList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ProductCertificateDTO.UpdateDTO dto) {
        ProductCertificateEntity old = this.getById(dto.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品认证"));

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
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "产品认证");
        sysLogService.addSysLogByUpdate(old, entity, String.valueOf(ProductCertificateEntity.class),old.getSkuId(),entity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void productAddOrUpdate(List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList) {
        //结果集
        List<ProductCertificateEntity> resultList = new ArrayList<>();

        //产品信息
        List<String> skuIdList = productCertificateList.stream().map(ProductCertificateDTO.ProductAddOrUpdateDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listByIds(skuIdList);

        for (ProductCertificateDTO.ProductAddOrUpdateDTO productAddOrUpdateDTO : productCertificateList) {
            //新增数据附件不能为空
            if (StrUtil.isBlank(productAddOrUpdateDTO.getId()) && StrUtil.isBlank(productAddOrUpdateDTO.getAttachmentId())) {
                throw new ServiceException(ApiError.TIME_NOT_NULL,"新增附件");
            }
            ProductCertificateEntity entity = BeanMapperUtils.map(ProductCertificateEntity.class, productAddOrUpdateDTO);
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), productAddOrUpdateDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            entity.setProductId(productDetailEntity.getProductId());
            resultList.add(entity);
        }
        //新增数据
        this.saveOrUpdateBatch(resultList);
        //绑定附件id
        updateAttachmentId(resultList);
        //删除附件
        productCertificateList.forEach(obj -> deleteFile(obj.getRemoveFileIdList(),obj.getId()));

        //操作日志
        List<SysLogEntity> sysLogEntityList = new LinkedList<>();
        resultList.forEach(obj -> {
            sysLogEntityList.add(
                    new SysLogEntity().setContent(String.format("新增了一个【产品证书】"))
                            .setBusinessId(obj.getSkuId())
                            .setClassPath(String.valueOf(ProductCertificateEntity.class))
                            .setPid(obj.getId())
                            .setOperation("编辑操作")
            );
        });
        sysLogService.addSysLogByBatchSave(sysLogEntityList);
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
        if (CollectionUtils.isEmpty(removeFileIdList) || StrUtil.isBlank(businessId)) {
            return;
        }
        ProductCertificateEntity productCertificateEntity = this.getById(businessId);
        if (ObjectUtil.isEmpty(productCertificateEntity)) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"产品证书");
        }

        List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByBusinessIds(Arrays.asList(businessId));
        long count = attachmentList.stream().filter(obj -> !removeFileIdList.contains(obj.getId())).count();
        if (count <= 0) {
            throw new ServiceException(ApiError.ERROR_FILE_NOT_DELETE_ALL);
        }
        List<PlmAttachmentEntity> removeFileList = attachmentList.stream().filter(obj -> removeFileIdList.contains(obj.getId())).collect(Collectors.toList());
        //删除附件表数据
        plmAttachmentService.removeByIds(removeFileIdList);
        for (PlmAttachmentEntity entity : removeFileList) {
            //fastdfs删除附件
            FastDFSClientUtil.deleteFile(entity.getAttachUrl());
        }
        //操作日志
        List<SysLogEntity> sysLogEntityList = new LinkedList<>();
        removeFileList.forEach(obj -> {
            sysLogEntityList.add(
                    new SysLogEntity().setContent(StrUtil.format("删除了一个产品证书【{}】",obj.getAttachName()))
                            .setBusinessId(productCertificateEntity.getSkuId())
                            .setClassPath(String.valueOf(ProductCertificateEntity.class))
                            .setPid(businessId)
                            .setOperation("删除附件")
            );
        });
        sysLogService.addSysLogByBatchSave(sysLogEntityList);
    }

    @Override
    public ProductCertificateDTO.ViewDTO view(String id) {
        ProductCertificateEntity old = this.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品认证信息"));
        ProductCertificateDTO.ViewDTO viewDTO = new ProductCertificateDTO.ViewDTO();
        BeanMapperUtils.copy(old,viewDTO);
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
        String typeName = certificateTypeList.stream().filter(obj -> StrUtil.equals(viewDTO.getType(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        viewDTO.setTypeName(typeName);
        //证书项目名称
        String dictProjectName = certificateProjectList.stream().filter(obj -> StrUtil.equals(viewDTO.getDictProject(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        viewDTO.setDictProjectName(dictProjectName);

        //产品信息
        ProductDetailEntity productDetailEntity = productDetailService.getById(viewDTO.getSkuId());
        if (ObjectUtil.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
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
    public Boolean delete(List<String> ids) {
        log.info("编辑 开始更新产品认证,ids={}", ids);
        //删除认证信息
       this.removeByIds(ids);

        //删除附件
        List<PlmAttachmentEntity> attachmentList = plmAttachmentService.listByBusinessIds(ids);
        if (CollectionUtils.isEmpty(attachmentList)) {
            return Boolean.TRUE;
        }
        //删除附件表数据
        List<String> attachmentIdList = attachmentList.stream().map(PlmAttachmentEntity::getId).collect(Collectors.toList());
        plmAttachmentService.removeByIds(attachmentIdList);
        for (PlmAttachmentEntity entity : attachmentList) {
            //fastdfs删除附件
            FastDFSClientUtil.deleteFile(entity.getAttachUrl());
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        ProductCertificateExcelListener excelListenerUtil = new ProductCertificateExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), ProductCertificateExcelDTO.class, excelListenerUtil)
                    .sheet(0)
                    .doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<ProductCertificateExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<ProductCertificateExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<ProductCertificateExcelDTO> successList = excelListenerUtil.getSuccessList();
        //处理验证成功数据
        handleImportSuccessList(successList,errorList);

        if (errorList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/productCertificateError.xlsx";
            String name = "productCertificate";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
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
     */
    private void handleImportSuccessList (List<ProductCertificateExcelDTO> successList,List<ProductCertificateExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<String> skuNoList = successList.stream().map(ProductCertificateExcelDTO::getSkuNo).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listBySkuNoList(skuNoList);
        
        //认证项目
        List<String> dictProductList = successList.stream().map(obj -> ProductCertificateProjectEnum.getCode(obj.getDictProjectName())).distinct().collect(Collectors.toList());
        List<String> skuIdList = skuList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<ProductCertificateEntity> productCertificateList = listBySkuListAndDictProductList(skuIdList, dictProductList);

        //产品认证
        List<BasicDictEntity> productAttestationList = basicDictService.listByType(BasicDictTypeEnum.PRODUCT_ATTESTATION.getCode());
        //运输认证
        List<BasicDictEntity> transportAttestationList = basicDictService.listByType(BasicDictTypeEnum.TRANSPORT_ATTESTATION.getCode());
        //其他认证
        List<BasicDictEntity> otherAttestationList = basicDictService.listByType(BasicDictTypeEnum.OTHER_ATTESTATION.getCode());

        //新增的数据
        List<ProductCertificateExcelDTO> resultList = new ArrayList<>();

        for (ProductCertificateExcelDTO excelDTO : successList) {

            List<String> errorMsgList = new ArrayList<>();
            //产品认证
            if (StrUtil.equals(ProductCertificateTypeEnum.PRODUCT_ATTESTATION.getName(),excelDTO.getTypeName())) {
                long count = productAttestationList.stream().filter(obj -> StrUtil.equals(excelDTO.getDictProjectName(), obj.getName())).count();
                if (count == 0) {
                    errorMsgList.add("产品认证下未找到证书项目");
                }
            }
            //运输认证
            if (StrUtil.equals(ProductCertificateTypeEnum.TRANSPORT_ATTESTATION.getName(),excelDTO.getTypeName())) {
                long count = transportAttestationList.stream().filter(obj -> StrUtil.equals(excelDTO.getDictProjectName(), obj.getName())).count();
                if (count == 0) {
                    errorMsgList.add("运输认证下未找到证书项目");
                }
                if (StrUtil.isBlank(excelDTO.getCertificateValidTimeStr())) {
                    errorMsgList.add("运输认证有效期不能为空");
                }
            }
            //其他认证
            if (StrUtil.equals(ProductCertificateTypeEnum.OTHER_ATTESTATION.getName(),excelDTO.getTypeName())) {
                long count = otherAttestationList.stream().filter(obj -> StrUtil.equals(excelDTO.getDictProjectName(), obj.getName())).count();
                if (count == 0) {
                    errorMsgList.add("其他认证下未找到证书项目");
                }
            }
            //产品信息
            ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> StrUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                errorMsgList.add("系统中未找到SKU");
            }
            //校验传进来的参数是否重复
            if (!StrUtil.equals(ProductCertificateTypeEnum.OTHER_ATTESTATION.getName(),excelDTO.getTypeName())) {
                long count = resultList.stream().filter(obj -> StrUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())
                                && StrUtil.equals(obj.getDictProjectName(), excelDTO.getDictProjectName()))
                        .count();
                if (count > 0) {
                    errorMsgList.add(StrUtil.format(ApiError.ERROR_PRODUCT_CERTIFICATE_EXIST.msg,excelDTO.getSkuNo(), excelDTO.getDictProjectName()));
                }
            }
            //配置信息
            Map<SettingEnum, String> cfgSettingList = dmpTaskFeign.getCfgSettingList(SettingEnum.URL_CHANGE);

            String pathUrl = excelDTO.getPathUrl();
            MultipartFile multipartFile = null;
            try {
                multipartFile = getMulFileByPath(pathUrl,cfgSettingList);
            } catch (Exception e) {
                errorMsgList.add("文件路径下未找到文件");
            }
            //存在错误信息则
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
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
                checkProductCertificateParam(Arrays.asList(entity),skuList,productCertificateList);
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //存在错误信息则
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            //新增数据
            this.saveOrUpdate(entity);
            resultList.add(excelDTO);

            //上传附件
            uploadFile (Arrays.asList(entity));
        }
    }

    /**
     * 获取MultipartFile
     */
    private  MultipartFile getMulFileByPath(String filePath,Map<SettingEnum, String> cfgSettingMap) {
        //查询配置进行转换
        String urlPath = toFilePath(filePath, cfgSettingMap);
        if (StrUtil.equals(filePath,urlPath)) {
            return FileUtil.toMultipartFile(filePath);
        }
        //配置信息
        Map<SettingEnum, String> nasUserMap = dmpTaskFeign.getCfgSettingList(SettingEnum.NAS_USERNAME_PWD);
        if (ObjectUtil.isEmpty(nasUserMap)) {
            throw new ServiceException("未找到共享文件配置信息");
        }
        List<String> nasUserList = Arrays.stream(nasUserMap.get(SettingEnum.PLM_NAS_USERNAME_PWD).split(",")).collect(Collectors.toList());
        if (nasUserList.size() != 2) {
            throw new ServiceException("未找到共享文件配置信息");
        }
        MultipartFile multipartFile = null;
        try {
             multipartFile = SambaUtil.toMultipartFile(urlPath, nasUserList.get(0), nasUserList.get(1));
        } catch (Exception e) {
           throw new ServiceException("获取共享文件失败");
        }
        return multipartFile;
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
        if (ObjectUtil.isEmpty(cfgSettingMap)) {
            return  filePath;
        }
        if (StrUtil.isBlank(cfgSettingMap.get(SettingEnum.PLM_PRODUCT_CERTIFICATE_IMPORT_URL))) {
            return  filePath;
        }
        List<String> urlList = Arrays.stream(cfgSettingMap.get(SettingEnum.PLM_PRODUCT_CERTIFICATE_IMPORT_URL).split(",")).collect(Collectors.toList());
        if (urlList.size() != 2) {
            return  filePath;
        }
        //判断路径是否以正则开头
        if (!filePath.matches(urlList.get(0).concat(".*"))) {
            return  filePath;
        }
        String removePath = SambaUtil.removePrefix(filePath,urlList.get(0));
        return  urlList.get(1).concat(removePath);
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
            String typeName = certificateTypeList.stream().filter(obj -> StrUtil.equals(showDTO.getType(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            showDTO.setTypeName(typeName);
            //证书项目名称
            String dictProjectName = certificateProjectList.stream().filter(obj -> StrUtil.equals(showDTO.getDictProject(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
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
            String typeName = certificateTypeList.stream().filter(obj -> StrUtil.equals(listDTO.getType(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setTypeName(typeName);
            //证书项目名称
            String dictProjectName = certificateProjectList.stream().filter(obj -> StrUtil.equals(listDTO.getDictProject(), obj.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setDictProjectName(dictProjectName);

            //附件全路径
            listDTO.setFullAttachUrl(StrUtil.format("{}{}",FastDFSClientUtil.publicUrl,listDTO.getAttachUrl()));
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
            //其他认证无需校验
            if (ProductCertificateProjectEnum.OTHER_CERTIFICATE.getCode().equals(value.get(0).getDictProject())) {
                continue;
            }
            //验证保存时数据是否重复
            if (value.size() > MathUtil.ONE) {
                String skuNo = productDetailEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), value.get(0).getSkuId())).map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
                throw new ServiceException(ApiError.ERROR_PRODUCT_CERTIFICATE_EXIST,skuNo, ProductCertificateProjectEnum.getName(value.get(0).getDictProject()));
            }
            //验证是否和已存在数据重复
            long count = productCertificateList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), value.get(0).getSkuId()) && StrUtil.equals(obj.getDictProject(), value.get(0).getDictProject())).count();
            if (count > 0) {
                String skuNo = productDetailEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), value.get(0).getSkuId())).map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
                throw new ServiceException(ApiError.ERROR_PRODUCT_CERTIFICATE_EXIST,skuNo, ProductCertificateProjectEnum.getName(value.get(0).getDictProject()));
            }
        }

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
        return list;
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
                ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> StrUtil.equals(obj.getSkuNo(), skuNo)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(StrUtil.format("SKU【{}】系统不存在",skuNo));
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

        HashMap<String,File> map = new HashMap<>();
        List<PlmAttachmentEntity> attachmentList = new ArrayList<>();
        for (ProductCertificateEntity entity : resultList) {
            //附件
            MultipartFile multipartFile = entity.getMultipartFile();
            if (ObjectUtil.isEmpty(multipartFile)) {
                continue;
            }
            double size = multipartFile.getSize();
            double fileSize = size / (1024 * 1024);
            fileSize = (double) Math.round(fileSize * 10000) / 10000;
            if (fileSize > 300) {
                throw new ServiceException(ApiError.ERROR_95160, 300);
            }
            String fileName = multipartFile.getOriginalFilename().toLowerCase();
            if (fileName.length() > 200) {
                throw new ServiceException(ApiError.ERROR_1018);
            }
            if (StrUtil.isBlank(fileName)) {
                try {
                    fileName = URLDecoder.decode(multipartFile.getName(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            File file = FileUtil.multiToFile(multipartFile);
            if (ObjectUtil.isEmpty(map.get(entity.getDictProject()))) {
                map.put(entity.getDictProject(),file);
            } else {
                file = map.get(entity.getDictProject());
            }
            String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
            if (StringUtils.isBlank(fileUrl)) {
                throw new ServiceException(ApiError.ERROR_95018);
            }
            PlmAttachmentEntity attachmentEntity = new PlmAttachmentEntity();
            attachmentEntity.setBusinessId(entity.getId());
            attachmentEntity.setAttachUrl(fileUrl);
            attachmentEntity.setAttachName(StrUtil.isBlank(fileName) ? multipartFile.getName().toLowerCase() : fileName);
            attachmentEntity.setType(ProductCertificateEntity.TABLE_NAME);
            attachmentEntity.setAttachSize(new BigDecimal(fileSize));
            attachmentList.add(attachmentEntity);
        }
        plmAttachmentService.saveBatch(attachmentList);

        //操作日志
        List<SysLogEntity> sysLogEntityList = new LinkedList<>();
        attachmentList.forEach(obj -> {
            //skuId
            ProductCertificateEntity entity = resultList.stream().filter(e -> StrUtil.equals(e.getId(), obj.getBusinessId())).findFirst().orElse(new ProductCertificateEntity());
            sysLogEntityList.add(
                    new SysLogEntity().setContent(StrUtil.format("新增了一个产品证书附件【{}】",obj.getAttachName()))
                            .setBusinessId(entity.getSkuId())
                            .setClassPath(String.valueOf(ProductCertificateEntity.class))
                            .setPid(obj.getBusinessId())
                            .setOperation("新增附件")
            );
        });
        sysLogService.addSysLogByBatchSave(sysLogEntityList);

    }

}




