package com.erp.server.plm.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.AttachmentDTO;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.dto.excel.ProductCertificateExcelDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.model.plm.entity.ProductCertificateEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private CommonService commonService;

    private static final  String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);

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
        //新增数据
        this.saveOrUpdateBatch(resultList);
        //上传附件
        uploadFile (resultList);
    }

    /**
     * @description: 新增数据处理
     * @author Will
     * @date: 2024/2/19 15:46
     * @param dto
     * @return List<ProductCertificateEntity>
     */
    private List<ProductCertificateEntity> handleAdd(ProductCertificateDTO.AddDTO dto) {
        List<String> skuIdList = dto.getSkuIdList();
        List<ProductCertificateDTO.FileDTO> fileList = dto.getFileList();

        //产品信息
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listByIds(skuIdList);

        //结果集
        List<ProductCertificateEntity> resultList = new ArrayList<>();
        for (String skuId : skuIdList) {
            for (ProductCertificateDTO.FileDTO fileDTO : fileList) {
                ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), skuId)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95154);
                }
                ProductCertificateEntity entity = new ProductCertificateEntity();
                entity.setSkuId(skuId);
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
     * @description: 上传文件
     * @author Will
     * @date: 2024/2/19 15:46
     * @param resultList
     */
    private void uploadFile (List<ProductCertificateEntity> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        List<PlmAttachmentEntity> attachmentList = new ArrayList<>();
        for (ProductCertificateEntity entity : resultList) {
            //附件
            MultipartFile multipartFile = entity.getMultipartFile();
            if (ObjectUtil.isEmpty(multipartFile)) {
                continue;
            }
            double size = multipartFile.getSize();
            double fileSize = size / (1024 * 1024);
            fileSize = (double) Math.round(fileSize * 100) / 100;
            if (fileSize > 300) {
                throw new ServiceException(ApiError.ERROR_95160, 300);
            }
            String fileName = multipartFile.getOriginalFilename().toLowerCase();
            if (fileName.length() > 200) {
                throw new ServiceException(ApiError.ERROR_1018);
            }
            File file = FileUtil.multiToFile(multipartFile);
            String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
            if (StringUtils.isBlank(fileUrl)) {
                throw new ServiceException(ApiError.ERROR_95018);
            }
            PlmAttachmentEntity attachmentEntity = new PlmAttachmentEntity();
            attachmentEntity.setBusinessId(entity.getId());
            attachmentEntity.setAttachUrl(fileUrl);
            attachmentEntity.setAttachName(fileName);
            attachmentEntity.setType(ProductCertificateEntity.TABLE_NAME);
            attachmentList.add(attachmentEntity);
        }
        plmAttachmentService.saveBatch(attachmentList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ProductCertificateDTO.UpdateDTO dto) {
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

        //添加日志
        ProductCertificateEntity old = this.getById(dto.getId());
        // 记录产品认证操作日志
        log.info("编辑 开始记录产品认证日志数据，id：【{}】", entity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), entity.getId(), "产品认证");
        sysLogService.addSysLogByUpdate(old, entity, ModuleTypeEnum.PRODUCT_CERTIFICATE.getCode(),old.getSkuId(),entity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void productAddOrUpdate(List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList) {
        //结果集
        List<ProductCertificateEntity> resultList = new ArrayList<>();

        for (ProductCertificateDTO.ProductAddOrUpdateDTO productAddOrUpdateDTO : productCertificateList) {
            //新增数据附件不能为空
            if (StrUtil.isBlank(productAddOrUpdateDTO.getId()) && ObjectUtil.isEmpty(productAddOrUpdateDTO.getMultipartFile())) {
                throw new ServiceException(ApiError.TIME_NOT_NULL,"新增附件");
            }
            ProductCertificateEntity entity = new ProductCertificateEntity();
            entity.setId(productAddOrUpdateDTO.getId());
            entity.setSkuId(productAddOrUpdateDTO.getSkuId());
            entity.setType(productAddOrUpdateDTO.getType());
            entity.setDictProject(productAddOrUpdateDTO.getType());
            entity.setMultipartFile(productAddOrUpdateDTO.getMultipartFile());
            entity.setRemark(productAddOrUpdateDTO.getRemark());
            resultList.add(entity);
        }
        //新增数据
        this.saveOrUpdateBatch(resultList);
        //上传附件
        uploadFile(resultList);
        //删除附件
        productCertificateList.forEach(obj -> deleteFile(obj.getRemoveFileIdList(),obj.getId()));
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
                    .extraRead(CellExtraTypeEnum.HYPERLINK)
                    .extraRead(CellExtraTypeEnum.COMMENT)
                    .sheet(0)
                    .headRowNumber(1)
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


    }


    @Override
    public Boolean exportExcel(ProductCertificateDTO.SearchParamDTO params, HttpServletResponse response) {
        List<ProductCertificateDTO.ListDTO> records = this.baseMapper.exportExcel(params);
        if (CollectionUtils.isEmpty(records)) {
            throw new ServiceException(ApiError.ERROR_IMPORT_DATA_NOT_NULL,"产品认证");
        }
        //数据赋值处理
        handlePaging(records);
        String name = "产品认证列表";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/productCertificate.xlsx";
        try {
            new ExcelPrintUtils().patchExport(records, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("产品认证列表导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
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
        return productCertificateMapper.list(productId);
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
        return productCertificateMapper.listBySkuId(skuId);
    }

    @Override
    public List<ProductCertificateEntity> listBySkuIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<ProductCertificateEntity> list = this.lambdaQuery().in(ProductCertificateEntity::getSkuId, skuIdList).list();
        return list;
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

}




