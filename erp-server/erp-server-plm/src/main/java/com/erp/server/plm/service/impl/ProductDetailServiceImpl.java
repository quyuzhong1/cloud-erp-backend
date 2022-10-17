package com.erp.server.plm.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.AlgorithmUtil;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.model.plm.entity.ProductVariantOptionEntity;
import com.erp.server.plm.controller.ProductDetailController;
import com.erp.server.plm.enums.ProductDetailStateEnum;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.ListUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 产品明细信息服务类
 * @Author: Luo_WG
 * @Date: 2022/9/21 16:25
 **/
@Service
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetailEntity> implements ProductDetailService {

    @Resource
    private ProductDetailMapper productDetailMapper;

    @Resource
    private ProductCostService productCostService;

    @Resource
    private ProductPurchaseService productPurchaseService;

    @Resource
    private ProductSaleService productSaleService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private ProductPackService productPackService;

    @Resource
    private ProductCertificateService productCertificateService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductImagesService productImagesService;

    @Resource
    private ProductInfoMapper productInfoMapper;

    @Resource
    private ProductPurchaseRemarkService productPurchaseRemarkService;

    @Resource
    private ProductVariantOptionService productVariantOptionService;

    /**
     * @Description 产品信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param pagingDTO:查询参数
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     **/
    @Override
    public PagingVO<ProductDetailShowDTO> paging(PagingDTO<ProductSkuDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ProductDetailShowDTO> pageData = productDetailMapper.paging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    /**
     * @Description 条件查询产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param name:产品名称
     * @return ProductDetailShowDTO
     **/
    @Override
    public ProductDetailShowDTO getProductBy(String name, String skuNo){
        return productDetailMapper.listProduct(name, skuNo);
    }

    /**
     * @Description 无规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:19
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
     **/
    @Override
    public ProductNoSpecDetailAllDTO getNoSpecDetailById(String productId) {
        ProductNoSpecDetailAllDTO productNoSpecDetailAllDTO = new ProductNoSpecDetailAllDTO();
        //无规格产品信息明细
        ProductNoDetailDTO noSpecDetailById = productDetailMapper.getNoSpecDetailById(productId);
        productNoSpecDetailAllDTO.setProductNoDetailDTO(noSpecDetailById);
        //产品成本信息查询列表
        List<ProductCostShowDTO> costShowDTOList = productCostService.list(productId);
        productNoSpecDetailAllDTO.setProductCostShowDTOList(costShowDTOList);
        //产品采购信息查询列表
        List<ProductPurchaseShowDTO> purchaseShowDTOList = productPurchaseService.list(productId);
        productNoSpecDetailAllDTO.setProductPurchaseShowDTOList(purchaseShowDTOList);
        //产品采购备注信息查询列表
        List<ProductPurchaseRemarkEntity> remarkEntityList = productPurchaseRemarkService.list(productId);
        productNoSpecDetailAllDTO.setRemarkEntityList(remarkEntityList);
        //产品销售信息查询列表
        List<ProductSaleShowDTO> saleShowDTOList = productSaleService.list(productId);
        productNoSpecDetailAllDTO.setProductSaleShowDTOList(saleShowDTOList);
        //产品包装信息查询列表
        List<ProductPackShowDTO> packShowDTOList = productPackService.list(productId);
        productNoSpecDetailAllDTO.setProductPackShowDTOS(packShowDTOList);
        //产品物流信息查询列表
        List<ProductLogisticsShowDTO> logisticsShowDTOList = productLogisticsService.list(productId);
        productNoSpecDetailAllDTO.setProductLogisticsShowDTOList(logisticsShowDTOList);
        //产品证书信息查询列表
        List<ProductCertificateShowDTO> certificateShowDTOList = productCertificateService.list(productId);
        productNoSpecDetailAllDTO.setProductCertificateShowDTOList(certificateShowDTOList);
        return productNoSpecDetailAllDTO;
    }

    /**
     * @Description 多规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:19
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductManyDetailDTO>
     **/
    @Override
    public ProductManyDetailDTO getManySpecDetailById(String productId) {
        ProductManyDetailDTO productManyDetail = new ProductManyDetailDTO();
        //多规格产品基础信息
        ProductManySpecBaseDTO manySpecDetailById = productDetailMapper.getManySpecDetailById(productId);
        productManyDetail.setProductManySpecBaseDTO(manySpecDetailById);
        //多规格产品明细信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getProductId, productId);
        List<ProductDetailEntity> list = this.list(queryWrapper);
        productManyDetail.setProductManySkuDetailList(list);
        //产品成本信息查询列表
        List<ProductCostShowDTO> costShowDTOList = productCostService.list(productId);
        productManyDetail.setProductCostShowDTOList(costShowDTOList);
        //产品采购信息查询列表
        List<ProductPurchaseShowDTO> purchaseShowDTOList = productPurchaseService.list(productId);
        productManyDetail.setProductPurchaseShowDTOList(purchaseShowDTOList);
        //产品采购备注信息查询列表
        List<ProductPurchaseRemarkEntity> remarkEntityList = productPurchaseRemarkService.list(productId);
        productManyDetail.setRemarkEntityList(remarkEntityList);
        //产品销售信息查询列表
        List<ProductSaleShowDTO> saleShowDTOList = productSaleService.list(productId);
        productManyDetail.setProductSaleShowDTOList(saleShowDTOList);
        //产品包装信息查询列表
        List<ProductPackShowDTO> packShowDTOList = productPackService.list(productId);
        productManyDetail.setProductPackShowDTOS(packShowDTOList);
        //产品物流信息查询列表
        List<ProductLogisticsShowDTO> logisticsShowDTOList = productLogisticsService.list(productId);
        productManyDetail.setProductLogisticsShowDTOList(logisticsShowDTOList);
        //产品证书信息查询列表
        List<ProductCertificateShowDTO> certificateShowDTOList = productCertificateService.list(productId);
        productManyDetail.setProductCertificateShowDTOList(certificateShowDTOList);
        //产品选择的变体查询
        List<ProductVariantOptionEntity> productVariantOptionEntityList = productVariantOptionService.list(productId);
        productManyDetail.setProductVariantOptionEntityList(productVariantOptionEntityList);
        return productManyDetail;
    }

    /**
     * @Description 保存/修改产品sku信息表数据-无规格
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productSkuBaseInfoDTO 新增产品无规格sku信息请求参数
     * @return java.lang.String
     **/
    @Override
    public String saveOrUpdate(ProductSkuBaseInfoDTO productSkuBaseInfoDTO) {
        ProductDetailEntity detailEntity = new ProductDetailEntity();
        BeanMapper.copy(productSkuBaseInfoDTO, detailEntity);
        this.saveOrUpdate(detailEntity);
        return detailEntity.getId();
    }

    /**
     * @Description 保存/修改产品sku信息表数据-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productDetailList 新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductDetailDTO> productDetailList) {
        List<ProductDetailEntity> list = BeanMapper.copyList(productDetailList, ProductDetailEntity.class);
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional
    public Boolean saveOrUpdateNoSpec(ProductNoSpecDTO productNoSpecDTO) {
        //检查sku编号是否重复
        if (this.checkSpuNo(productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getSkuNo(), productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95017);
        }
        //如果是修改允许保留原来的产品名称不变
        if (this.checkName(productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO().getName(), productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95007);
        }
        ProductInfoDTO productSpuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO();
        productSpuBaseInfoDTO.setApprovalStatus(4);
        productSpuBaseInfoDTO.setSpecType(1);
        productSpuBaseInfoDTO.setGrade("");
        //1.修改产品表 主表信息
        String id = productInfoService.updateSpec(productSpuBaseInfoDTO);

        //2.修改/新增 sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();
        productSkuBaseInfoDTO.setProductId(id);
        //如果是修改sku图片 还需要修改图片表
        if (StringUtils.isNotBlank(productSkuBaseInfoDTO.getImagesUrl())) {
            ProductImagesDTO productImagesDTO = new ProductImagesDTO();
            productImagesDTO.setSkuId(productSkuBaseInfoDTO.getId());
            productImagesDTO.setProductId(productSkuBaseInfoDTO.getProductId());
            productImagesDTO.setImagesUrl(productSkuBaseInfoDTO.getImagesUrl());
            productImagesService.updateProductImage(productImagesDTO);
        }

        String skuId = this.saveOrUpdate(productSkuBaseInfoDTO);

        //3.修改/新增 成本信息
        if (ObjectUtils.isNotEmpty(productNoSpecDTO.getProductCostDTO())) {
            ProductCostDTO productCostDTO = productNoSpecDTO.getProductCostDTO();
            productCostDTO.setSkuId(skuId);
            productCostService.saveOrUpdate(productNoSpecDTO.getProductCostDTO());
        }

        //4.修改/新增 采购信息
        if (ObjectUtils.isNotEmpty(productNoSpecDTO.getProductPurchaseDTO())) {
            ProductPurchaseDTO productPurchaseDTO = productNoSpecDTO.getProductPurchaseDTO();
            productPurchaseDTO.setSkuId(skuId);
            productPurchaseService.saveOrUpdate(productNoSpecDTO.getProductPurchaseDTO());

        }
        //新增/修改采购备注信息
        if (!ListUtils.isEmpty(productNoSpecDTO.getProductPurchaseRemarkList())) {
            List<ProductPurchaseRemarkDTO> productPurchaseRemarkList = productNoSpecDTO.getProductPurchaseRemarkList();
            productPurchaseRemarkList.forEach(req -> {
                req.setProductId(id);
            });
            productPurchaseRemarkService.saveOrUpdateBatch(productPurchaseRemarkList);
        }

        //5.修改/新增 销售信息
        ProductSaleDTO productSaleDTO = productNoSpecDTO.getProductSaleDTO();
        if (ObjectUtils.isNotEmpty(productSaleDTO)) {
            productSaleDTO.setSkuId(skuId);
            productSaleService.saveOrUpdate(productSaleDTO);
        }
        //6.修改/新增 物流信息
        ProductLogisticsDTO productLogisticsDTO = productNoSpecDTO.getProductLogisticsDTO();
        if (ObjectUtils.isNotEmpty(productLogisticsDTO)) {
            productLogisticsDTO.setSkuId(skuId);
            productLogisticsService.saveOrUpdate(productLogisticsDTO);
        }

        //7.修改/新增 包装信息
        ProductPackDTO productPackDTO = productNoSpecDTO.getProductPackDTO();
        if (ObjectUtils.isNotEmpty(productPackDTO)) {
            productPackDTO.setSkuId(skuId);
            productPackService.saveOrUpdate(productPackDTO);
        }

        //8.修改/新增 证书信息
        List<ProductCertificateDTO> productCertificateList = productNoSpecDTO.getProductCertificateList();
        if (ObjectUtils.isNotEmpty(productCertificateList)) {
            productCertificateService.saveOrUpdateBatch(productCertificateList);
        }
        return true;
    }

    /**
     * @Description 新增多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:52
     * @param productManySpecDTO:新增产品多规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional
    public Boolean saveOrUpdateManySpec(ProductManySpecDTO productManySpecDTO) {
        //检查spu编号是否重复
        if (this.checkSpuNo(productManySpecDTO.getProductInfoDTO().getSpuNo(), productManySpecDTO.getProductInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95017);
        }

        //如果是修改允许保留原来的产品名称不变
        if (this.checkName(productManySpecDTO.getProductInfoDTO().getName(), productManySpecDTO.getProductInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95007);
        }
        List<ProductDetailDTO> productDetailList = productManySpecDTO.getProductDetailList();
        //检查sku是否重复
        for (int i = 0; i < productDetailList.size(); i++) {
            if (this.checkSkuNo(productDetailList.get(i).getSkuNo(), productDetailList.get(i).getId())) {
                throw new ServiceException(ApiError.ERROR_95015.code,ApiError.ERROR_95015.msg + " 第"+ (i+1) +"行");
            }
        }

        //1.修改产品表 主表信息
        ProductInfoDTO productInfoDTO = productManySpecDTO.getProductInfoDTO();
        productInfoDTO.setApprovalStatus(4);
        productInfoDTO.setSpecType(2);
        productInfoDTO.setGrade("");

        if (ObjectUtils.isNotEmpty(productInfoDTO)) {
            productInfoService.updateSpec(productInfoDTO);
        }

        //2.修改/新增 sku信息
        List<ProductDetailDTO> productDetailLists = productManySpecDTO.getProductDetailList();
        if (productDetailLists.size() > 0) {
            this.saveOrUpdateBatch(productManySpecDTO.getProductDetailList());
        }

        //3.修改/新增 成本信息
        List<ProductCostDTO> productCostList = productManySpecDTO.getProductCostList();
        if (productCostList.size() > 0) {
            productCostService.saveOrUpdateBatch(productManySpecDTO.getProductCostList());
        }

        //4.修改/新增 采购信息
        List<ProductPurchaseDTO> productPurchaseList = productManySpecDTO.getProductPurchaseList();
        if (productPurchaseList.size() > 0) {
            productPurchaseService.saveOrUpdateBatch(productManySpecDTO.getProductPurchaseList());
        }

        //新增/修改采购备注信息
        if (!ListUtils.isEmpty(productManySpecDTO.getProductPurchaseRemarkList())) {
            List<ProductPurchaseRemarkDTO> productPurchaseRemarkList = productManySpecDTO.getProductPurchaseRemarkList();
            productPurchaseRemarkList.forEach(req -> {
                req.setProductId(productInfoDTO.getId());
            });
            productPurchaseRemarkService.saveOrUpdateBatch(productPurchaseRemarkList);
        }

        //5.修改/新增 销售信息
        List<ProductSaleDTO> productSaleList = productManySpecDTO.getProductSaleList();
        if (productSaleList.size() > 0) {
            productSaleService.saveOrUpdateBatch(productSaleList);
        }

        //6.修改/新增 物流信息
        List<ProductLogisticsDTO> productLogisticsList = productManySpecDTO.getProductLogisticsList();
        if (productLogisticsList.size() > 0) {
            productLogisticsService.saveOrUpdateBatch(productLogisticsList);
        }
        //7.修改/新增 包装信息
        List<ProductPackDTO> productPackList = productManySpecDTO.getProductPackList();
        if (productPackList.size() > 0) {
            productPackService.saveOrUpdateBatch(productPackList);
        }

        //8.修改/新增 证书信息
        List<ProductCertificateDTO> productCertificateList = productManySpecDTO.getProductCertificateList();
        if (productCertificateList.size() > 0) {
            productCertificateService.saveOrUpdateBatch(productCertificateList);
        }
        return true;
    }

    /**
     * @Description 多规格自动生成
     * @Author Luo_WG
     * @Date 2022/9/26 14:54
     * @param variantAutoAddDTO:自动生成请求参数
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional
    public List<ProductDetailEntity> insertManySpecAuto(VariantAutoAddDTO variantAutoAddDTO) {
        //检查spu编号是否重复
        if (this.checkSpuNo(variantAutoAddDTO.getProductSpuBaseInfoDTO().getSpuNo(), variantAutoAddDTO.getProductSpuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95017);
        }
        //如果是修改允许保留原来的产品名称不变
        if (this.checkName(variantAutoAddDTO.getProductSpuBaseInfoDTO().getName(), variantAutoAddDTO.getProductSpuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95007);
        }
        ProductInfoDTO productSpuBaseInfoDTO = variantAutoAddDTO.getProductSpuBaseInfoDTO();
        productSpuBaseInfoDTO.setApprovalStatus(4);
        productSpuBaseInfoDTO.setSpecType(2);
        productSpuBaseInfoDTO.setGrade("");
        //1.保存产品表 基础信息获取产品id
        String id = productInfoService.updateSpec(productSpuBaseInfoDTO);
        List<VarianRefPropertyDTO> varianRefPropertyList = variantAutoAddDTO.getVarianRefPropertyList();
        List<ProductPropertyModelDTO> varianTempList = new ArrayList<>();
        productVariantOptionService.deleteByProductId(id);
        for (VarianRefPropertyDTO req : varianRefPropertyList) {
            ProductVariantOptionEntity productVariantOptionEntity = new ProductVariantOptionEntity();
            productVariantOptionEntity.setProductId(id);
            productVariantOptionEntity.setVariantType(req.getPropertyType());
            productVariantOptionEntity.setVariantValue(JSONObject.toJSONString(req.getVarianList()));
            productVariantOptionService.saveOrUpdateOptionEntity(productVariantOptionEntity);
            List<String> varianList = req.getVarianList();
            for (String varian : varianList) {
                varianTempList.add(new ProductPropertyModelDTO(req.getPropertyType(), varian));
            }
        }

        // 按指定字段（type）分组
        Map<String, List<ProductPropertyModelDTO>> modelMap = varianTempList.stream().collect(Collectors.groupingBy(ProductPropertyModelDTO::getType));
        Collection<List<ProductPropertyModelDTO>> mapValues = modelMap.values();
        List<List<ProductPropertyModelDTO>> dimensionValue = new ArrayList<>(mapValues);    // 原List

        List<List<ProductPropertyModelDTO>> result = new ArrayList<>(); // 返回集合
        new AlgorithmUtil().descartes(dimensionValue, result, 0, new ArrayList<ProductPropertyModelDTO>());

        List<String> varianList = new ArrayList<>();
        for (List<ProductPropertyModelDTO> models : result) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < models.size(); i++) {
                sb.append(models.get(i).getAuthor());
                if (i+1 < models.size()) {
                    sb.append(",");
                }
            }
            varianList.add(sb.toString());
        }

        //把变体属性放入实体类
        List<ProductDetailEntity> list = new ArrayList<>();
        for (String req : varianList) {
            ProductDetailEntity productDetailEntity = new ProductDetailEntity();
            productDetailEntity.setVariantProperty(req);
            productDetailEntity.setProductId(id);
            productDetailEntity.setName(variantAutoAddDTO.getProductSpuBaseInfoDTO().getName());
            productDetailEntity.setSkuNo("");
            list.add(productDetailEntity);
        }

        //过滤掉重复的变体属性
        List<ProductDetailEntity> detailEntityList = this.queryByProductId(id);
        for (ProductDetailEntity req : detailEntityList) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getVariantProperty().equals(req.getVariantProperty())) {
                    list.remove(i);
                }
            }
        }

        boolean bool = this.saveBatch(list);
        if(!bool){
            throw new ServiceException(1, "新增sku明细失败！");
        }
        return this.queryByProductId(id);
    }

    /**
     * @Description 删除多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     * @param skuId:产品sku表主键id
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional
    public Boolean delete(String skuId){
        //1.删除证书信息
        productCertificateService.removeCertificate(skuId);
        //2.删除包装信息
        productPackService.removePack(skuId);
        //3.删除物流信息
        productLogisticsService.removeLogistics(skuId);
        //4.删除销售信息
        productSaleService.removeSale(skuId);
        //5.删除采购信息
        productPurchaseService.removePurchase(skuId);
        //6.删除成本信息
        productCostService.removeCost(skuId);
        //7.删除sku信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getId, skuId);
        return this.remove(queryWrapper);
    }

    /**
     * @Description 根据产品主键id查询sku明细
     * @Author Luo_WG
     * @Date 2022/9/26 18:25
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     **/
    @Override
    public List<ProductDetailEntity> queryByProductId(String productId){
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getProductId, productId);
        return this.list(queryWrapper);
    }


    /**
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param skuList:sku集合
     **/
    public Boolean checkSkuNos(List<String> skuList) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductDetailEntity::getSkuNo, skuList);
        int count = this.count(queryWrapper);
        if(count > 0){
            return true;
        }
        return false;
    }

    /**
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param sku:sku
     **/
    public Boolean checkSkuNo(String sku, String id) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getSkuNo, sku);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProductDetailEntity::getId, id);
        }
        int count = this.count(queryWrapper);
        if(count > 0){
            return true;
        }
        return false;
    }

    /**
     * @Description 检查spu编号是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     * @param id:主键id
     * @param spuNo:spu编号
     * @return void
     **/
    public Boolean checkSpuNo(String spuNo, String id) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductInfoEntity::getSpuNo, spuNo);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProductInfoEntity::getId, id);
        }
        Integer count = productInfoMapper.selectCount(queryWrapper);
        if(count > 0){
            return true;
        }
        return false;
    }

    /**
     * @Description 检查产品名称是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     * @param name:产品名称
     * @param id:主键id
     * @return void
     **/
    public Boolean checkName(String name, String id) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductInfoEntity::getName, name);
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.ne(ProductInfoEntity::getId, id);
        }
        Integer count = productInfoMapper.selectCount(queryWrapper);
        if(count > 0){
            return true;
        }
        return false;
    }

    /**
     * @Description 根据sku查询sku表信息
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    public ProductDetailEntity getProductIdBySku(String sku) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getSkuNo, sku);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * @Description 导入无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional
    public Boolean inportExcel(ProductNoSpecDTO productNoSpecDTO) {

        //1.新增产品表 主表信息
        String id = productInfoService.updateSpec(productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO());

        //2.修改/新增 sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();
        productSkuBaseInfoDTO.setProductId(id);
        //如果是修改sku图片 还需要修改图片表
        if(StringUtils.isNotBlank(productSkuBaseInfoDTO.getImagesUrl())){
            ProductImagesDTO productImagesDTO = new ProductImagesDTO();
            productImagesDTO.setSkuId(productSkuBaseInfoDTO.getId());
            productImagesDTO.setProductId(productSkuBaseInfoDTO.getProductId());
            productImagesDTO.setImagesUrl(productSkuBaseInfoDTO.getImagesUrl());
            productImagesService.updateProductImage(productImagesDTO);
        }
        String skuId = this.saveOrUpdate(productSkuBaseInfoDTO);
        if(StringUtils.isBlank(productSkuBaseInfoDTO.getId())){
            ProductDetailEntity productIdBySku = this.getProductIdBySku(productSkuBaseInfoDTO.getSkuNo());
            skuId = productIdBySku.getId();
        }

        ProductKeyDTO productKey = productDetailMapper.getProductKey(skuId);

        //3.修改/新增 成本信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductCostDTO())) {
            ProductCostDTO productCostDTO = productNoSpecDTO.getProductCostDTO();
            if (ObjectUtils.isNotEmpty(productKey)) {
                productCostDTO.setId(productKey.getCostId());
            }
            productCostDTO.setSkuId(skuId);
            productCostService.saveOrUpdate(productNoSpecDTO.getProductCostDTO());
        }

        //4.修改/新增 采购信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductPurchaseDTO())) {
            ProductPurchaseDTO productPurchaseDTO = productNoSpecDTO.getProductPurchaseDTO();
            if (ObjectUtils.isNotEmpty(productKey)) {
                productPurchaseDTO.setId(productKey.getPurchaseId());
            }
            productPurchaseDTO.setSkuId(skuId);
            productPurchaseService.saveOrUpdate(productNoSpecDTO.getProductPurchaseDTO());
        }
        //新增/修改采购备注信息
        if (!ListUtils.isEmpty(productNoSpecDTO.getProductPurchaseRemarkList())) {
            List<ProductPurchaseRemarkDTO> productPurchaseRemarkList = productNoSpecDTO.getProductPurchaseRemarkList();
            productPurchaseRemarkList.forEach(req -> {
                req.setProductId(id);
            });
            productPurchaseRemarkService.saveOrUpdateBatch(productPurchaseRemarkList);
        }
        //5.修改/新增 销售信息
        ProductSaleDTO productSaleDTO = productNoSpecDTO.getProductSaleDTO();
        if (!ObjectUtils.isEmpty(productSaleDTO)) {
            if (ObjectUtils.isNotEmpty(productKey)) {
                productSaleDTO.setId(productKey.getSaleId());
            }
            productSaleDTO.setSkuId(skuId);
            productSaleService.saveOrUpdate(productSaleDTO);
        }
        //6.修改/新增 物流信息
        ProductLogisticsDTO productLogisticsDTO = productNoSpecDTO.getProductLogisticsDTO();
        if (!ObjectUtils.isEmpty(productLogisticsDTO)) {
            if (ObjectUtils.isNotEmpty(productKey)) {
                productLogisticsDTO.setId(productKey.getLogisticsId());
            }
            productLogisticsDTO.setSkuId(skuId);
            productLogisticsService.saveOrUpdate(productLogisticsDTO);
        }

        //7.修改/新增 包装信息
        ProductPackDTO productPackDTO = productNoSpecDTO.getProductPackDTO();
        if (!ObjectUtils.isEmpty(productPackDTO)) {
            if (ObjectUtils.isNotEmpty(productKey)) {
                productPackDTO.setId(productKey.getPackId());
            }
            productPackDTO.setSkuId(skuId);
            productPackService.saveOrUpdate(productPackDTO);
        }

        //8.修改/新增 证书信息
        /*ProductCertificateDTO productCertificateDTO = productNoSpecDTO.getProductCertificateDTO();
        if (!ObjectUtils.isEmpty(productCertificateDTO)) {
            productCertificateDTO.setSkuId(skuId);
            productCertificateService.saveOrUpdate(productCertificateDTO);
        }*/
        return true;
    }

    /**
     * 获取数据导出excel
     * @Author Luo_WG
     * @Date 2022/10/10 12:09
     * @param productSkuDTO productSkuDTO
     * @param response response
     * @return void
     **/
    @Override
    public void exportProduct(ProductSkuDTO productSkuDTO,HttpServletResponse response) {
        List<ExportSkuExcelDTO> exportSkuExcelDTO = productDetailMapper.getExportSkuExcel(productSkuDTO);

        exportSkuExcelDTO.forEach(req -> {
            req.setProductState(ProductDetailStateEnum.getNameByCode(Integer.valueOf(req.getProductState())));
        });

        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/productSkuDetail.xlsx";
        String name = "产品sku明细表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(exportSkuExcelDTO, response, sb.toString(), excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
