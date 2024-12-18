package com.erp.server.plm.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.BomCombinationImportExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.listener.BomCombinationExcelListener;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alibaba.excel.EasyExcelFactory.read;

/**
 * @author Will
 * @version 1.0
 * @description: 组合产品业务层
 * @date 2023/8/16 10:01
 */
@Service
@Slf4j
public class BomCombinationServiceImpl implements BomCombinationService {

    @Resource
    private BomInfoMapper bomInfoMapper;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private ProductUnitService productUnitService;

    @Override
    public PagingVO<BomCombinationDTO.ListDTO> paging(PagingDTO<BomCombinationDTO.SearchParamDTO> dto) {
        BomCombinationDTO.SearchParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<BomCombinationDTO.SearchParamDTO> query = new Page<BomCombinationDTO.SearchParamDTO>(dto.getCurrPage(), dto.getPageSize());
        IPage<BomCombinationDTO.ListDTO> pageData = bomInfoMapper.combinationPaging(query, params);
        //处理分页数据
        handlePaging(pageData.getRecords());
        return new PagingVO<BomCombinationDTO.ListDTO>(pageData);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(BomCombinationDTO.AddDTO dto) {
        //新增产品信息
        String skuId = addProductDetail(dto);
        //新增bom信息
        Boolean addBom = addBomCombination(dto, skuId);
        return addBom;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(BomCombinationDTO.UpdateDTO dto) {
        //新增产品信息
        String skuId = updateProductDetail(dto);
        //新增bom信息
        Boolean addBom = updateBomCombination(dto, skuId);
        return addBom;
    }

    @Override
    public BomCombinationDTO.ViewDTO view(BaseIdDTO dto) {
        BomCombinationDTO.ViewDTO resultDTO = new BomCombinationDTO.ViewDTO();

        List<BomSkuDTO> bomList = bomSkuService.getByBomId(dto.getId());
        if (CollectionUtils.isEmpty(bomList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        BomSkuDTO bomSkuDTO = bomList.get(0);

        List<String> childSkuIds = bomSkuDTO.getChildren().stream().flatMap(obj -> Stream.of(obj.getParentSkuId(),obj.getSkuId())).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listByIds(childSkuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //父级SKU名称
        String parentSkuName = skuList.stream().filter(obj -> obj.getId().equals(bomSkuDTO.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        resultDTO.setId(dto.getId());
        resultDTO.setSkuNo(bomSkuDTO.getSkuNo());
        resultDTO.setName(parentSkuName);
        List<BomCombinationDetailDTO.ViewDTO> detailList = new ArrayList<>();
        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomSkuDTO.getChildren()) {
            BomCombinationDetailDTO.ViewDTO viewDTO = new BomCombinationDetailDTO.ViewDTO();
            viewDTO.setId(bomChildrenSkuDTO.getId());
            viewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
            viewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
            viewDTO.setQty(bomChildrenSkuDTO.getQuantity());
            //子SKU名称
            String childSkuName = skuList.stream().filter(obj -> obj.getId().equals(bomChildrenSkuDTO.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            viewDTO.setProductName(childSkuName);
            detailList.add(viewDTO);
        }
        resultDTO.setDetailList(detailList);
        return resultDTO;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {

        BomCombinationExcelListener excelListenerUtil = new BomCombinationExcelListener();

        try {
            read(excelFile.getInputStream(), BomCombinationImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<BomCombinationImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<BomCombinationImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<BomCombinationImportExcelDTO> errorList = excelListenerUtil.getErrorList();

        handleImportSuccessData (successList,errorList);


        if (CollectionUtils.isNotEmpty(errorList)) {
            StringBuilder sb = new StringBuilder();
            String excelPath = "excel/bomCombination.xlsx";
            String name = "bomCombination";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * @description: 导入数据新增处理
     * @author Will
     * @date: 2023/8/17 15:52
     * @param successList
     * @param errorList
     */
    private void handleImportSuccessData (List<BomCombinationImportExcelDTO> successList,List<BomCombinationImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        Map<String, List<BomCombinationImportExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(BomCombinationImportExcelDTO::getParentSkuNo));
        for (Map.Entry<String, List<BomCombinationImportExcelDTO>> entry :  map.entrySet()) {
            List<BomCombinationImportExcelDTO> value = entry.getValue();
            long count = value.stream().map(BomCombinationImportExcelDTO::getName).distinct().count();
            if (count > 1) {
                log.error("组合产品SKU【{}】对应名称不一致",entry.getKey());
                throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_PARENT_SKU_NAME_DIFFERENT,entry.getKey());
            }
            //bom信息
            List<BomSkuEntity> bomSkuList = bomSkuService.listByParentSkuNos(Arrays.asList(entry.getKey()));
            try {
                if (CollectionUtils.isNotEmpty(bomSkuList)) {
                    throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_EXPORT,entry.getKey());
                }
                //新增bom
                BomCombinationDTO.AddDTO addDTO = new BomCombinationDTO.AddDTO();
                addDTO.setSkuNo(entry.getKey());
                addDTO.setName(value.get(0).getName());
                List<BomCombinationDetailDTO.UpdateDTO> detailList = getDetailList(value, bomSkuList);
                List<BomCombinationDetailDTO.AddDTO> addList = BeanMapperUtils.copyList(BomCombinationDetailDTO.AddDTO.class, detailList);
                addDTO.setDetailList(addList);
                this.add(addDTO);
            } catch (Exception e) {
                value.forEach(obj -> obj.setErrorMsg(e.getMessage()));
                errorList.addAll(value);
            }
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/bomCombinationTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("组合产品 downloadTemplate  出错了 e==={}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public String checkBomChildSku(BomCombinationDTO.CheckBomParentSkuDTO dto) {
        //skuId集合
        List<String> skuIdList = dto.getChildSkuList().stream().map(BomCombinationDTO.CheckBomChildSkuDTO::getSkuId).collect(Collectors.toList());

        List<BomDTO.BomSku> bomSkuList = bomSkuService.listAllBomByChildSkuIdList(skuIdList);
        if (CollectionUtils.isEmpty(bomSkuList)) {
            return null;
        }
        List<String> errorSkuNoList = new ArrayList<>();
        Map<String, List<BomDTO.BomSku>> map = bomSkuList.stream().collect(Collectors.groupingBy(BomDTO.BomSku::getParentSkuNo));
        for (Map.Entry<String, List<BomDTO.BomSku>> entry : map.entrySet()) {
            List<BomDTO.BomSku> value = entry.getValue();
            //父级SKU相同无需校验
            if (CharSequenceUtil.equals(entry.getKey(),dto.getSkuNo())) {
                continue;
            }
            //子级数量不一致无需校验
            if (skuIdList.size() != value.size()) {
                continue;
            }
            //是否匹配
            Boolean isMatch = Boolean.TRUE;
            for (BomDTO.BomSku bomSku : value) {
                long count = dto.getChildSkuList().stream()
                        .filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), bomSku.getSkuId())
                                && MathUtil.compareTo(obj.getQty(), bomSku.getQty()) == MathUtil.ZERO)
                        .count();
                if (count == MathUtil.ZERO) {
                    isMatch = Boolean.FALSE;
                }
            }
            if (isMatch) {
                errorSkuNoList.add(entry.getKey());
            }
        }
        if (CollectionUtils.isEmpty(errorSkuNoList)) {
            return null;
        }
        String parentSkuNos = errorSkuNoList.stream().collect(Collectors.joining(","));
        return parentSkuNos;
    }

    /**
     * @description: 获取组合产品明细
     * @author Will
     * @date: 2023/8/17 15:47
     * @param value
     * @param bomSkuList
     * @return List<UpdateDTO>
     */
    private List<BomCombinationDetailDTO.UpdateDTO> getDetailList (List<BomCombinationImportExcelDTO> value,List<BomSkuEntity> bomSkuList) {
        List<BomCombinationDetailDTO.UpdateDTO> updateList = new ArrayList<>();

        List<String> childSkuNos = value.stream().map(BomCombinationImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        //sku信息
        List<ProductDetailEntity> skuList = productDetailService.listBySkuNos(childSkuNos);
        for (BomCombinationImportExcelDTO importExcelDTO :value ) {
            BomCombinationDetailDTO.UpdateDTO updateDTO = new BomCombinationDetailDTO.UpdateDTO();
            //产品信息SKU
            ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(importExcelDTO.getSkuNo())).findFirst().orElse(null);
            if (productDetailEntity == null) {
                throw new ServiceException(ApiError.ERROR_NOT_FOUND_SKU,importExcelDTO.getSkuNo());
            }
            //bom信息
            BomSkuEntity bomSkuEntity = bomSkuList.stream().filter(obj -> obj.getSkuId().equals(productDetailEntity.getId())).findFirst().orElse(null);
            if (bomSkuEntity != null) {
                updateDTO.setId(bomSkuEntity.getId());
            }
            updateDTO.setSkuId(productDetailEntity.getId());
            updateDTO.setQty(Integer.valueOf(importExcelDTO.getQty()));
            updateList.add(updateDTO);
        }

        //校验导入数据是否重复
        BomCombinationDTO.CheckBomParentSkuDTO dto = new BomCombinationDTO.CheckBomParentSkuDTO();
        dto.setSkuNo(value.get(0).getSkuNo());
        List<BomCombinationDTO.CheckBomChildSkuDTO> childSkuList = updateList.stream().map(obj -> new BomCombinationDTO.CheckBomChildSkuDTO(obj.getSkuId(), obj.getQty())).collect(Collectors.toList());
        dto.setChildSkuList(childSkuList);
        String parentSkuNos = checkBomChildSku(dto);
        if (CharSequenceUtil.isNotBlank(parentSkuNos)) {
            throw new ServiceException(CharSequenceUtil.format("子产品明细与已存在捆绑商品【{}】的子件一致，如需继续创建，请手动单个创建",parentSkuNos));
        }
        return updateList;
    }

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/16 18:20
     * @param dto
     * @return String
     */
    private String addProductDetail (BomCombinationDTO.AddDTO dto) {

        //组合产品不能输入中文
        checkSkuNo(dto.getSkuNo());

        Map<String, List<BomCombinationDetailDTO.AddDTO>> map = dto.getDetailList().stream().collect(Collectors.groupingBy(obj -> obj.getSkuId()));
        long count = map.entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).count();
        if (count > MathUtil.ZERO) {
            throw new ServiceException("组合品子件不能重复");
        }

        Map<String, String> params = new HashMap<>();
        params.put("skuNo",dto.getSkuNo());
        ProductDetailDTO productDetailDTO = productDetailService.getSkuByParam(params);
        //已存在则直接返回sku主键id
        if (ObjectUtils.isNotEmpty(productDetailDTO)) {
            log.info("已存在SKU【{}】",dto.getSkuNo());
           throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_SKU,dto.getSkuNo());
        }

        List<BomCombinationDetailDTO.AddDTO> detailList = dto.getDetailList();
        //新增产品信息
        String id = commonProductDetail(dto, detailList.get(0).getSkuId());
        //提交并审核
        skuSubmitApprove(id);
        return id;
    }


    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/17 10:17
     * @param dto
     * @return String
     */
    private String updateProductDetail (BomCombinationDTO.UpdateDTO dto) {
        //原组合产品信息
        List<BomSkuDTO> oldList = bomSkuService.getByBomId(dto.getId());
        if (CollectionUtils.isEmpty(oldList)) {
            throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_NOT_EXIST);
        }
        //组合产品不能输入中文
        checkSkuNo(dto.getSkuNo());

        Map<String, List<BomCombinationDetailDTO.UpdateDTO>> map = dto.getDetailList().stream().collect(Collectors.groupingBy(obj -> obj.getSkuId()));
        long count = map.entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).count();
        if (count > MathUtil.ZERO) {
            throw new ServiceException("组合品子件不能重复");
        }

        Map<String, String> params = new HashMap<>();
        params.put("skuNo",dto.getSkuNo());
        ProductDetailDTO productDetailDTO = productDetailService.getSkuByParam(params);
        //已存在则直接返回sku主键id
        if (ObjectUtils.isNotEmpty(productDetailDTO)) {
            log.info("已存在SKU【{}】",dto.getSkuNo());

            //修改时变更组合产品编码
            if (!oldList.get(0).getSkuNo().equals(dto.getSkuNo())) {
                throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_SKU,dto.getSkuNo());
            }
            //如果已变更sku名称则需要更新sku名称
            if (!dto.getName().equals(productDetailDTO.getName())) {
                updateSkuName(dto.getName(),productDetailDTO);
            }
            return productDetailDTO.getId();
        }
        List<BomCombinationDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //新增产品信息
        String id = commonProductDetail(dto, detailList.get(0).getSkuId());
        //提交并审核
        skuSubmitApprove(id);
        return id;
    }

    /**
     * 更新sku名称
     */
    private void updateSkuName (String name,ProductDetailDTO productDetailDTO) {
        //审核通过后需要反审核
        Boolean isApprove = Boolean.FALSE;
        if (ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productDetailDTO.getStatus()) ) {
            productDetailService.deApprove(productDetailDTO.getId());
            isApprove = Boolean.TRUE;
        }
        //更新名称
        productDetailService.updateName(productDetailDTO.getId(),name);
        //审核
        if (isApprove) {
            skuSubmitApprove(productDetailDTO.getId());
        }
    }

    /**
     * @description: 新增产品信息
     * @author Will
     * @date: 2023/8/17 10:16
     * @param dto
     * @param childSkuId
     * @return String
     */
    private String commonProductDetail (BomCombinationDTO.CommonDTO dto,String childSkuId) {

        ProductDetailEntity child = productDetailService.getById(childSkuId);
        if (ObjectUtils.isEmpty(child)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        ProductInfoEntity productInfoEntity = productInfoService.getById(child.getProductId());

        //产品信息新增
        ProductNoSpecDTO productNoSpecDTO = new ProductNoSpecDTO();
        ProductBaseInfoDTO productBaseInfoDTO = new ProductBaseInfoDTO();

        //产品信息
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        productInfoDTO.setName(dto.getName());
        productInfoDTO.setBrandId("");
        productInfoDTO.setBrandName("");
        productInfoDTO.setChargeId("");
        productInfoDTO.setChargeName("");
        productInfoDTO.setChargeId(child.getChargeId());
        productInfoDTO.setChargeName(child.getChargeName());
        productInfoDTO.setCategory(productInfoEntity.getCategory());
        productInfoDTO.setCategoryId(productInfoEntity.getCategoryId());
        productInfoDTO.setSaleMethod(productInfoEntity.getSaleMethod());

        //产品属性默认填自研发
        BasicDictEntity basicDictEntity = basicDictService.listByTypeAndValue(BasicDictTypeEnum.PRODUCT_PROPERTY.getCode(), ProductConstant.PRODUCT_PROPERTY_DEFAULT);
        if (ObjectUtils.isNotEmpty(basicDictEntity)) {
            productInfoDTO.setProperty(ProductConstant.PRODUCT_PROPERTY_DEFAULT);
            productInfoDTO.setPropertyId(basicDictEntity.getId());
        }
        productBaseInfoDTO.setProductSpuBaseInfoDTO(productInfoDTO);
        //sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();
        productSkuBaseInfoDTO.setSkuNo(dto.getSkuNo());
        productSkuBaseInfoDTO.setName(dto.getName());
        productSkuBaseInfoDTO.setChargeId(child.getChargeId());
        productSkuBaseInfoDTO.setChargeName(child.getChargeName());
        productSkuBaseInfoDTO.setProductState(ProductDetailStateEnum.DEVELOP_FINISH.getCode());


        //单位默认Pcs
        ProductUnitEntity productUnitEntity = productUnitService.getByName(ProductConstant.PRODUCT_UNIT_DEFAULT);
        if (ObjectUtils.isNotEmpty(productUnitEntity)) {
            productSkuBaseInfoDTO.setUnitName(ProductConstant.PRODUCT_UNIT_DEFAULT);
            productSkuBaseInfoDTO.setUnitId(productUnitEntity.getId());
        }

        productBaseInfoDTO.setProductSkuBaseInfoDTO(productSkuBaseInfoDTO);
        productNoSpecDTO.setProductBaseInfoDTO(productBaseInfoDTO);
        //成本信息
        ProductCostDTO productCostDTO = new ProductCostDTO();
        productNoSpecDTO.setProductCostDTO(productCostDTO);
        //采购信息信息
        ProductPurchaseDTO productPurchaseDTO = new ProductPurchaseDTO();
        productNoSpecDTO.setProductPurchaseDTO(productPurchaseDTO);
        //销售信息
        ProductSaleDTO productSaleDTO = new ProductSaleDTO();
        productSaleDTO.setSaleState(SaleStateEnum.SALES.getCode());
        productNoSpecDTO.setProductSaleDTO(productSaleDTO);
        //物流信息
        ProductLogisticsDTO productLogisticsDTO = new ProductLogisticsDTO();
        productNoSpecDTO.setProductLogisticsDTO(productLogisticsDTO);
        //包装信息
        ProductPackDTO productPackDTO = new ProductPackDTO();
        productNoSpecDTO.setProductPackDTO(productPackDTO);
        //包装辅料信息
        List<ProductAccessoriesDTO> productAccessoriesList = new ArrayList<>();
        ProductAccessoriesDTO productAccessoriesDTO = new ProductAccessoriesDTO();
        productAccessoriesDTO.setParentSkuNo(dto.getSkuNo());
        productAccessoriesList.add(productAccessoriesDTO);
        productNoSpecDTO.setProductAccessoriesList(productAccessoriesList);
        //证书信息
        List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList = new ArrayList<>();
        productNoSpecDTO.setProductCertificateList(productCertificateList);
        //海关信息
        List<ProductCustomsDTO> productCustomsList = new ArrayList<>();
        productNoSpecDTO.setProductCustomsList(productCustomsList);

        productDetailService.saveOrUpdateNoSpec(productNoSpecDTO);

        return productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getId();
    }

    /**
     * @description: 产品信息提交审核
     * @author Will
     * @date: 2023/8/17 11:29
     * @param id
     */
    private void skuSubmitApprove (String id) {
        //提交
        Boolean submit = productDetailService.submit(Arrays.asList(id),Boolean.FALSE);
        if (!submit) {
            throw new ServiceException(ApiError.ERROR_1042);
        }
        //审核
        ProductDetailOperateDTO dto = new ProductDetailOperateDTO();
        dto.setId(id);
        Boolean approve = productDetailService.approvalPass(dto,Boolean.FALSE);
        if (!approve) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
    }

    /**
     * @description: 新增bom信息
     * @author Will
     * @date: 2023/8/16 18:21
     * @param dto
     * @param skuId
     * @return Boolean
     */
    private Boolean addBomCombination (BomCombinationDTO.AddDTO dto,String skuId) {
        AddBomDTO addBomDTO = new AddBomDTO();
        //BOM主表信息
        addBomDTO.setVersion(StringPool.ONE);
        addBomDTO.setType(BomTypeEnum.COMBINATION.getType());
        addBomDTO.setSubmitType(BomTypeEnum.CREATE.getType());
        addBomDTO.setSourceType(SourceTypeEnum.PRODUCT_COMBINATION.getCode());
        //BOM父级SKU信息
        List<BomSkuDTO> skuList = new ArrayList<>();
        BomSkuDTO bomSkuDTO = new BomSkuDTO();
        bomSkuDTO.setSkuId(skuId);
        bomSkuDTO.setSkuNo(dto.getSkuNo());
        //BOM子级SKU信息
        List<BomChildrenSkuDTO> children = new ArrayList<>();
        List<String> skuIds = dto.getDetailList().stream().map(BomCombinationDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> childList = productDetailService.listByIds(skuIds);
        if (CollectionUtils.isEmpty(childList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        for (BomCombinationDetailDTO.AddDTO addDetail : dto.getDetailList()) {
            BomChildrenSkuDTO childrenSkuDTO = new BomChildrenSkuDTO();
            ProductDetailEntity child = childList.stream().filter(obj -> obj.getId().equals(addDetail.getSkuId())).findFirst().orElse(null);
            if (child == null) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            childrenSkuDTO.setSkuId(addDetail.getSkuId());
            childrenSkuDTO.setSkuNo(child.getSkuNo());
            childrenSkuDTO.setProductId(child.getProductId());
            childrenSkuDTO.setQuantity(addDetail.getQty());
            childrenSkuDTO.setSkuName(child.getName());
            children.add(childrenSkuDTO);
        }
        bomSkuDTO.setChildren(children);
        skuList.add(bomSkuDTO);
        addBomDTO.setSkuList(skuList);
        String bomId = bomInfoService.insert(addBomDTO);
        //审核BOM
        bomSubmitApprove(bomId);
        return Boolean.TRUE;
    }

    /**
     * @description: 修改bom信息
     * @author Will
     * @date: 2023/8/17 11:15
     * @param dto
     * @param skuId
     * @return Boolean
     */
    private Boolean updateBomCombination (BomCombinationDTO.UpdateDTO dto,String skuId) {

        BomInfoEntity bomInfoEntity = bomInfoService.getById(dto.getId());
        if (ObjectUtils.isEmpty(bomInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        if (!BomStateEnum.AUDIT_PASS.getState().equals(bomInfoEntity.getState()) && !BomStateEnum.WAIT_AUDIT.getState().equals(bomInfoEntity.getState())) {
            throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_STATE);
        }
        //解除归档
        if (BomStateEnum.AUDIT_PASS.getState().equals(bomInfoEntity.getState())) {
            bomInfoService.removeArchive(dto.getId());
        }
        UpdateBomDTO updateBomDTO = new UpdateBomDTO();
        updateBomDTO.setId(dto.getId());
        //父级SKU
        List<BomSkuDTO> skuList = new ArrayList<>();
        BomSkuDTO bomSkuDTO = new BomSkuDTO();
        bomSkuDTO.setSkuId(skuId);
        bomSkuDTO.setSkuNo(dto.getSkuNo());

        List<String> skuIds = dto.getDetailList().stream().map(BomCombinationDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> childList = productDetailService.listByIds(skuIds);
        if (CollectionUtils.isEmpty(childList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //子级SKU
        List<BomChildrenSkuDTO> children = new ArrayList<>();

        List<String> skuIdList = new ArrayList<>();

        for (BomCombinationDetailDTO.UpdateDTO updateDTO : dto.getDetailList()) {
            BomChildrenSkuDTO childrenSkuDTO = new BomChildrenSkuDTO();
            ProductDetailEntity child = childList.stream().filter(obj -> obj.getId().equals(updateDTO.getSkuId())).findFirst().orElse(null);
            if (child == null) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(child.getStatus())) {
                throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_SKU_APPROVE_PASS);
            }
            childrenSkuDTO.setParentSkuId(skuId);
            childrenSkuDTO.setSkuId(updateDTO.getSkuId());
            childrenSkuDTO.setSkuNo(child.getSkuNo());
            childrenSkuDTO.setSkuName(child.getName());
            childrenSkuDTO.setProductId(child.getProductId());
            childrenSkuDTO.setQuantity(updateDTO.getQty());
            children.add(childrenSkuDTO);
            if (skuIdList.contains(updateDTO.getSkuId())) {
                throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_CHILD_SKU_REPEAT,child.getSkuNo());
            }
            skuIdList.add(updateDTO.getSkuId());
        }
        bomSkuDTO.setChildren(children);
        skuList.add(bomSkuDTO);
        updateBomDTO.setSkuList(skuList);
        bomInfoService.edit(updateBomDTO);

        //自动提交并审核
        bomSubmitApprove(dto.getId());
        return Boolean.TRUE;
    }

    /**
     * @description: bom信息提交审核
     * @author Will
     * @date: 2023/8/17 11:40
     * @param bomId
     * @return Boolean
     */
    private Boolean bomSubmitApprove (String bomId) {
        //提交
        Boolean submit = bomInfoService.submitAudit(bomId);
        if (!submit) {
            throw new ServiceException(ApiError.ERROR_1042);
        }
        AuditParamDTO dto = new AuditParamDTO();
        dto.setId(bomId);
        bomInfoService.approvalPass(dto);
        return Boolean.TRUE;
    }


    /**
     * @description: 处理分页数据
     * @author Will
     * @date: 2023/8/16 14:25
     * @param records
     */
    private void handlePaging (List<BomCombinationDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        List<String> supplierIdList = records.stream().filter(obj -> CollectionUtils.isNotEmpty(obj.getChildList())).flatMap(obj -> Stream.of(obj.getChildList().stream().filter(e -> StringUtils.isNotBlank(e.getMainSupplierId()))
                .map(BomCombinationDTO.ChildDTO::getMainSupplierId).toArray(String[]::new))).distinct().collect(Collectors.toList());
        List<PurchasePriceDTO.SupplierSkuPrice> supplierSkuPriceList = scmTaskFeign.listSupplierSkuPrice(supplierIdList);

        for (BomCombinationDTO.ListDTO dto : records) {
            if (CollectionUtils.isEmpty(dto.getChildList())) {
                continue;
            }
            for (BomCombinationDTO.ChildDTO childDTO : dto.getChildList()) {
                PurchasePriceDTO.SupplierSkuPrice supplierSkuPrice = supplierSkuPriceList.stream().filter(req -> req.getSupplierId().equals(childDTO.getMainSupplierId()) && req.getSkuId().equals(childDTO.getChildSkuId())).findFirst().orElse(null);
                if (supplierSkuPrice == null) {
                    continue;
                }
                //含税价
                childDTO.setActualTaxCost(supplierSkuPrice.getTaxPrice());
            }
            //子级sku编号
            String childSkoNos = dto.getChildList().stream().map(BomCombinationDTO.ChildDTO::getChildSkuNo).collect(Collectors.joining(","));
            dto.setChildSkuNos(childSkoNos);
            BigDecimal childSkuCost = dto.getChildList().stream().map(obj -> MathUtil.multiply(MathUtil.compareTo(obj.getActualTaxCost(), BigDecimal.ZERO) == MathUtil.ZERO ? obj.getTargetTaxCost() : obj.getActualTaxCost(),obj.getQty())).reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setChildSkuCost(childSkuCost);
        }
    }

    /**
     * 校验不能为中文
     */
    private void checkSkuNo (String skuNo) {
        String desc = FieldFormatPatternTypeEnum.ENUM_NOT_CHINESE.getDesc();
        Boolean matches = skuNo.matches(desc);
        //枚举格式未匹配正确
        if (!matches) {
            throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_SKU_NOT_CHINESE,skuNo);
        }
    };
}
