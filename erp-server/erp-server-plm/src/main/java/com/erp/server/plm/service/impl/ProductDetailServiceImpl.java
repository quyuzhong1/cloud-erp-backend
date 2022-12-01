package com.erp.server.plm.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.AlgorithmUtil;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.workflow.dto.ApproveProcessDTO;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.model.workflow.dto.TaskShowDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.ProductManyDetailConstant;
import com.erp.server.plm.enums.*;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CommonService commonService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private TaskRefSkuConfigService taskRefSkuConfigService;

    @Resource
    private SysCodeService sysCodeService;

    @Resource
    private ProjectTaskRefSkuService projectTaskRefSkuService;

    @Resource
    private BusinessProcessService businessProcessService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private ProductDetailCommentService productDetailCommentService;

    @Resource
    private ProductDetailApproverService productDetailApproverService;

    /**
     * @param pagingDTO:查询参数
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     * @Description 产品信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     **/
    @Override
    public PagingVO<ProductDetailShowDTO> paging(PagingDTO<ProductSkuDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ProductDetailShowDTO> pageData = productDetailMapper.paging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    /**
     * @param name:产品名称
     * @return ProductDetailShowDTO
     * @Description 条件查询产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     **/
    @Override
    public ProductDetailShowDTO getProductBy(String name, String skuNo) {
        return productDetailMapper.listProduct(name, skuNo);
    }

    /**
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
     * @Description 无规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:19
     **/
    @Override
    public ProductNoSpecDetailAllDTO getNoSpecDetailById(String productId) {
        ProductNoSpecDetailAllDTO productNoSpecDetailAllDTO = new ProductNoSpecDetailAllDTO();
        //无规格产品信息明细
        ProductNoDetailDTO noSpecDetailById = productDetailMapper.getNoSpecDetailById(productId);
        if (ObjectUtils.isNotEmpty(noSpecDetailById)) {
            //获取多级分类
            List<String> categoryIdList = basicCategoryService.getPidList(noSpecDetailById.getCategoryId());
            noSpecDetailById.setCategoryIdList(categoryIdList);
        }
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
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductManyDetailDTO>
     * @Description 多规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:19
     **/
    @Override
    public ProductManyDetailDTO getManySpecDetailById(String productId) {
        //任务与sku 关系
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByProductId(productId);
        //任务与配置字段 关系
        List<TaskRefSkuConfigEntity> refSkuFiledConfigList = taskRefSkuConfigService.getDisableFieldByProductId(productId);

        ProductManyDetailDTO productManyDetail = new ProductManyDetailDTO();
        //多规格产品基础信息
        ProductManySpecBaseDTO manySpecDetailById = productDetailMapper.getManySpecDetailById(productId);
        //基础信息 禁用字段
        List<String> manySpecBaseDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_MANY_SPEC_BASE, refSkuFiledConfigList);
        manySpecDetailById.setDisableFieldList(manySpecBaseDisableFields);
        //获取多级分类
        List<String> categoryIdList = basicCategoryService.getPidList(manySpecDetailById.getCategoryId());
        manySpecDetailById.setCategoryIdList(categoryIdList);
        productManyDetail.setProductManySpecBaseDTO(manySpecDetailById);

        //多规格产品明细信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getProductId, productId);
        queryWrapper.orderByDesc(ProductDetailEntity::getId);
        List<ProductDetailEntity> list = this.list(queryWrapper);
        for (ProductDetailEntity item : list) {
            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, item.getId(), refSkuFiledConfigList);
            //基础信息 禁用字段
            List<String> manySkuDetailDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_MANY_SKU_DETAIL_LIST, skuFiledConfigList);
            item.setDisableFieldList(manySkuDetailDisableFields);
        }


        productManyDetail.setProductManySkuDetailList(list);
        //产品成本信息查询列表
        List<ProductCostShowDTO> costShowDTOList = productCostService.list(productId);
        for (ProductCostShowDTO costShow : costShowDTOList) {
            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, costShow.getSkuId(), refSkuFiledConfigList);
            //成本信息 禁用字段
            List<String> costDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_COST_SHOW_LIST, skuFiledConfigList);
            costShow.setDisableFieldList(costDisableFields);
        }

        productManyDetail.setProductCostShowDTOList(costShowDTOList);
        //产品采购信息查询列表
        List<ProductPurchaseShowDTO> purchaseShowDTOList = productPurchaseService.list(productId);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        purchaseShowDTOList.forEach(req -> {
            FindUserDTO findUserDTO = userList.stream().filter(user -> user.getUserId().equals(req.getPurchaseUserId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                req.setCreateUserName(findUserDTO.getUserName());
            }

            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, req.getSkuId(), refSkuFiledConfigList);
            //采购信息 禁用字段
            List<String> purchaseDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_PURCHASE_SHOW_LIST, skuFiledConfigList);
            req.setDisableFieldList(purchaseDisableFields);

        });

        productManyDetail.setProductPurchaseShowDTOList(purchaseShowDTOList);
        //产品采购备注信息查询列表
        List<ProductPurchaseRemarkEntity> remarkEntityList = productPurchaseRemarkService.list(productId);
        productManyDetail.setRemarkEntityList(remarkEntityList);

        //产品销售信息查询列表
        List<ProductSaleShowDTO> saleShowDTOList = productSaleService.list(productId);
        saleShowDTOList.forEach(req -> {
            if (StringUtils.isNotBlank(req.getSaleCountry())) {
                String[] split = req.getSaleCountry().split(",");
                List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
                List<String> nameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                req.setSaleCountryName(StringUtils.join(nameList, ","));
            }

            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, req.getSkuId(), refSkuFiledConfigList);
            //销售信息 禁用字段
            List<String> saleDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_SALE_SHOW_LIST, skuFiledConfigList);
            req.setDisableFieldList(saleDisableFields);
        });

        productManyDetail.setProductSaleShowDTOList(saleShowDTOList);
        //产品包装信息查询列表
        List<ProductPackShowDTO> packShowDTOList = productPackService.list(productId);
        packShowDTOList.stream().forEach(req -> {
            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, req.getSkuId(), refSkuFiledConfigList);
            //包装 禁用字段
            List<String> packDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_PACK_SHOW, skuFiledConfigList);
            req.setDisableFieldList(packDisableFields);
        });
        productManyDetail.setProductPackShowDTOS(packShowDTOList);
        //产品物流信息查询列表
        List<ProductLogisticsShowDTO> logisticsShowDTOList = productLogisticsService.list(productId);
        logisticsShowDTOList.stream().forEach(req -> {
            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, req.getSkuId(), refSkuFiledConfigList);
            //物流 禁用字段
            List<String> logisticsDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_LOGISTICS_SHOW_LIST, skuFiledConfigList);
            req.setDisableFieldList(logisticsDisableFields);
        });

        productManyDetail.setProductLogisticsShowDTOList(logisticsShowDTOList);
        //产品证书信息查询列表
        List<ProductCertificateShowDTO> certificateShowDTOList = productCertificateService.list(productId);
        certificateShowDTOList.stream().forEach(req -> {
            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, req.getSkuId(), refSkuFiledConfigList);
            //证书 禁用字段
            List<String> certificateDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_CERTIFICATE_SHOW_LIST, skuFiledConfigList);
            req.setDisableFieldList(certificateDisableFields);
        });

        productManyDetail.setProductCertificateShowDTOList(certificateShowDTOList);
        //产品选择的变体查询
        List<ProductVariantOptionEntity> productVariantOptionEntityList = productVariantOptionService.list(productId);
        productManyDetail.setProductVariantOptionEntityList(productVariantOptionEntityList);
        return productManyDetail;
    }


    /**
     * 根据skuid  以及查询对应的任务字段关系
     *
     * @param taskRefSkuList
     * @param skuId
     * @param refSkuFiledConfigList
     * @return java.util.List<com.erp.model.plm.entity.TaskRefSkuConfigEntity>
     * @author yl
     * @date 2022-11-28 14:42
     */
    private List<TaskRefSkuConfigEntity> getSkuFiledConfigList(List<ProjectTaskRefSkuEntity> taskRefSkuList, String skuId, List<TaskRefSkuConfigEntity> refSkuFiledConfigList) {
        List<String> taskIdList = taskRefSkuList.stream().filter(t -> t.getSkuId().equals(skuId)).map(ProjectTaskRefSkuEntity::getTaskId).collect(Collectors.toList());
        List<TaskRefSkuConfigEntity> resultList = refSkuFiledConfigList.stream().filter(f -> taskIdList.contains(f.getTaskId())).collect(Collectors.toList());
        return resultList;
    }


    /**
     * 并集获取到禁用的字段
     *
     * @param flag
     * @param refSkuFiledConfigList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-11-28 14:03
     */
    public List<String> getByFileldFlag(String flag, List<TaskRefSkuConfigEntity> refSkuFiledConfigList) {
        List<String> resultList = new ArrayList<>(10);
        try {
            for (TaskRefSkuConfigEntity skuField : refSkuFiledConfigList) {
                String fieldJson = skuField.getFieldJson();
                if (StringUtils.isNotBlank(fieldJson)) {
                    Map<String, Object> fieldMap = JSONObject.parseObject(fieldJson);
                    if (fieldMap.containsKey(flag)) {
                        List<Map<String, Object>> fieldInfoList = (List<Map<String, Object>>) fieldMap.get(flag);
                        for (Map<String, Object> fieldInfoMap : fieldInfoList) {
                            if (fieldInfoMap.containsKey("prop")) {
                                resultList.add(fieldInfoMap.get("prop").toString());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("getByFileFlag ", e);
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * @param productSkuBaseInfoDTO 新增产品无规格sku信息请求参数
     * @return java.lang.String
     * @Description 保存/修改产品sku信息表数据-无规格
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     **/
    @Override
    public String saveOrUpdate(ProductSkuBaseInfoDTO productSkuBaseInfoDTO) {
        ProductDetailEntity detailEntity = new ProductDetailEntity();
        BeanMapper.copy(productSkuBaseInfoDTO, detailEntity);
        LoginUser loginUser = commonService.getUserInfo();
        if (StringUtils.isBlank(detailEntity.getId())) {
            detailEntity.setCreateUserId(loginUser.getUid());
            detailEntity.setCreateUserName(loginUser.getUserName());
        } else {
            detailEntity.setUpdateUserId(loginUser.getUid());
            detailEntity.setUpdateUserName(loginUser.getUserName());
        }
        this.saveOrUpdate(detailEntity);
        return detailEntity.getId();
    }

    /**
     * @param productDetailList 新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     * @Description 保存/修改产品sku信息表数据-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductDetailDTO> productDetailList) {
        List<ProductDetailEntity> list = BeanMapper.copyList(productDetailList, ProductDetailEntity.class);
        LoginUser loginUser = commonService.getUserInfo();
        list.forEach(req -> {
            if (ObjectUtils.isNotEmpty(loginUser)) {
                if (StringUtils.isBlank(req.getId())) {
                    req.setCreateUserId(loginUser.getUid());
                    req.setCreateUserName(loginUser.getUserName());
                } else {
                    req.setUpdateUserId(loginUser.getUid());
                    req.setUpdateUserName(loginUser.getUserName());
                }
            }
        });
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
     **/
    @Override
    @Transactional
    public Boolean saveOrUpdateNoSpec(ProductNoSpecDTO productNoSpecDTO) {
        //检查spu编号是否重复
        if (this.checkSpuNo(productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO().getSpuNo(), productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95017);
        }
        //检查sku编号是否重复
        if (this.checkSkuNo(productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getSkuNo(), productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95015);
        }

        //如果是修改允许保留原来的产品名称不变
        if (this.checkName(productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO().getName(), productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95007);
        }
        ProductInfoDTO productSpuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO();
        productSpuBaseInfoDTO.setSpecType(1);

        //产品等级
        if (StringUtils.isNotBlank(productSpuBaseInfoDTO.getGradeId())) {
            //根据id查询字典表中的产品等级
            BasicDictEntity basicDict = basicDictService.getById(productSpuBaseInfoDTO.getGradeId());
            if (ObjectUtils.isNotEmpty(basicDict)) {
                productSpuBaseInfoDTO.setGrade(basicDict.getValue());
            }
        }

        //1.修改产品表 主表信息
        String id = productInfoService.updateSpec(productSpuBaseInfoDTO);

        //2.修改/新增 sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();

        productSkuBaseInfoDTO.setProductId(id);
        //如果是修改sku图片 还需要修改图片表
/*        if (StringUtils.isNotBlank(productSkuBaseInfoDTO.getImagesUrl())) {
            ProductImagesDTO productImagesDTO = new ProductImagesDTO();
            productImagesDTO.setSkuId(productSkuBaseInfoDTO.getId());
            productImagesDTO.setProductId(productSkuBaseInfoDTO.getProductId());
            productImagesDTO.setImagesUrl(productSkuBaseInfoDTO.getImagesUrl());
            productImagesService.updateProductImage(productImagesDTO);
        }*/

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
     * @param productManySpecDTO:新增产品多规格sku信息请求参数
     * @return java.lang.Boolean
     * @Description 新增多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:52
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
                throw new ServiceException(ApiError.ERROR_95015.code, ApiError.ERROR_95015.msg + " 第" + (i + 1) + "行");
            }
        }

        //1.修改产品表 主表信息
        ProductInfoDTO productInfoDTO = productManySpecDTO.getProductInfoDTO();
        productInfoDTO.setSpecType(2);
        //产品等级
        if (StringUtils.isNotBlank(productInfoDTO.getGradeId())) {
            //根据id查询字典表中的产品等级
            BasicDictEntity basicDict = basicDictService.getById(productInfoDTO.getGradeId());
            if (ObjectUtils.isNotEmpty(basicDict)) {
                productInfoDTO.setGrade(basicDict.getValue());
            }
        }
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
     * @param variantAutoAddDTO:自动生成请求参数
     * @return java.lang.Boolean
     * @Description 多规格自动生成
     * @Author Luo_WG
     * @Date 2022/9/26 14:54
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
                if (i + 1 < models.size()) {
                    sb.append(",");
                }
            }
            varianList.add(sb.toString());
        }
        //过滤掉重复的变体属性
        List<ProductDetailEntity> detailEntityList = this.queryByProductId(id);
        //把变体属性放入实体类
        List<ProductDetailEntity> list = new ArrayList<>();
        for (String req : varianList) {
            //判断是否已经存在该变体属性
            long count = detailEntityList.stream().filter(obj -> req.equals(obj.getVariantProperty())).count();
            if (count > 0) {
                continue;
            }
            ProductDetailEntity productDetailEntity = new ProductDetailEntity();
            productDetailEntity.setVariantProperty(req);
            productDetailEntity.setProductId(id);
            productDetailEntity.setName(variantAutoAddDTO.getProductSpuBaseInfoDTO().getName());
            //获取颜色
            List<String> split = Arrays.asList(req.split(","));
            String variantColor = Arrays.stream(VariantColorEnum.values()).filter(obj -> split.contains(obj.getName())).map(VariantColorEnum::getName).findAny().orElse(null);
            if (StringUtils.isBlank(variantColor)) {
                throw new ServiceException(ApiError.ERROR_95074);
            }
            //生成sku编码
            String skuNo = sysCodeService.getSkuNo(id, variantColor);
            productDetailEntity.setSkuNo(skuNo);
            productDetailEntity.setChargeId(productSpuBaseInfoDTO.getChargeId());
            productDetailEntity.setChargeName(productSpuBaseInfoDTO.getChargeName());
            //sku启动审核流程
            ProductDetailApproverEntity approverEntity = productDetailApproverService.getProductDetailApprover();
            //验证是否设置审核人
            if (ObjectUtils.isEmpty(approverEntity)) {
                throw new ServiceException(ApiError.ERROR_95076);
            }
            String businessKey = BusinessProcessEnum.PRODUCT_DETAIL.getBusinessKey();
            //初始状态为待审核
            Integer waitConfirmCode = ProductDetailStatusEnum.WAIT_CONFIRM.getCode();
            BusinessProcessEntity processEntity = businessProcessService.getProcessByBusinessKey(businessKey);
            if (!Objects.isNull(processEntity)) {
                StartProcessDTO startProcess = new StartProcessDTO();
                LoginUser loginUser = commonService.getUserInfo();
                startProcess.setBusinessKey(processEntity.getBusinessKey());
                startProcess.setProcessDefinitionKey(processEntity.getProcessDefinitionKey());
                startProcess.setUserId(loginUser.getUid());
                //审核人1
                List<String> firstApproveIdList = Arrays.stream(approverEntity.getFirstApproveId().split(",")).collect(Collectors.toList());
                //审核人2
                List<String> secondApproveIdList = Arrays.stream(approverEntity.getSecondApproveId().split(",")).collect(Collectors.toList());
                //审核人3
                List<String> thirdApproveIdList = Arrays.stream(approverEntity.getThirdApproveId().split(",")).collect(Collectors.toList());
                Map<String, Object> parameterMap = new HashMap<>();
                parameterMap.put("firstApproveIdList",firstApproveIdList);
                parameterMap.put("secondApproveIdList",secondApproveIdList);
                parameterMap.put("thirdApproveIdList",thirdApproveIdList);
                startProcess.setParameterMap(parameterMap);
                //启动流程
                ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
                String processId = processResult.getProcessId();
                if (StringUtils.isNotBlank(processId)) {
                    productDetailEntity.setProcessId(processId);
                    productDetailEntity.setBusinessProcessId(processEntity.getId());
                    productDetailEntity.setStatus(waitConfirmCode);
                }
            }

            list.add(productDetailEntity);
        }

        boolean bool = this.saveBatch(list);
        if (!bool) {
            throw new ServiceException(1, "新增sku明细失败！");
        }
        return this.queryByProductId(id);
    }

    /**
     * @param skuId:产品sku表主键id
     * @return java.lang.Boolean
     * @Description 删除多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     **/
    @Override
    @Transactional
    public Boolean delete(String skuId) {
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

        //7.删除任务关联sku 信息
        taskRefSkuConfigService.removeTaskRefSku(skuId);

        //8.删除sku信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getId, skuId);
        return this.remove(queryWrapper);
    }

    /**
     * @param id:产品sku表主键id
     * @return java.lang.Boolean
     * @Description 根据产品id删除产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     **/
    @Override
    @Transactional
    public Boolean deleteByProductId(String id) {
        //删除sku信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getProductId, id);
        List<ProductDetailEntity> list = this.list(queryWrapper);
        list.forEach(req -> {
            //1.删除证书信息
            productCertificateService.removeCertificate(req.getSkuNo());
            //2.删除包装信息
            productPackService.removePack(req.getSkuNo());
            //3.删除物流信息
            productLogisticsService.removeLogistics(req.getSkuNo());
            //4.删除销售信息
            productSaleService.removeSale(req.getSkuNo());
            //5.删除采购信息
            productPurchaseService.removePurchase(req.getSkuNo());
            //6.删除成本信息
            productCostService.removeCost(req.getSkuNo());
        });
        return this.remove(queryWrapper);
    }

    /**
     * @param skuId:产品sku表主键id
     * @return java.lang.Boolean
     * @Description 删除多规格sku信息-批量
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     **/
    @Override
    @Transactional
    public Boolean deleteBatch(List<String> skuId) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductDetailEntity::getId, skuId);
        return this.remove(queryWrapper);
    }

    /**
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     * @Description 根据产品主键id查询sku明细
     * @Author Luo_WG
     * @Date 2022/9/26 18:25
     **/
    @Override
    public List<ProductDetailEntity> queryByProductId(String productId) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getProductId, productId);
        return this.list(queryWrapper);
    }


    /**
     * @param skuList:sku集合
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     **/
    public Boolean checkSkuNos(List<String> skuList) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductDetailEntity::getSkuNo, skuList);
        int count = this.count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    /**
     * @param sku:sku
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     **/
    public Boolean checkSkuNo(String sku, String id) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getSkuNo, sku);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProductDetailEntity::getId, id);
        }
        int count = this.count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    /**
     * @param id:主键id
     * @param spuNo:spu编号
     * @return void
     * @Description 检查spu编号是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     **/
    public Boolean checkSpuNo(String spuNo, String id) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ProductInfoEntity::getSpuNo, spuNo);
        queryWrapper.eq(ProductInfoEntity::getDeleteState, IsConstant.NO);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProductInfoEntity::getId, id);
        }
        Integer count = productInfoMapper.selectCount(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    /**
     * @param name:产品名称
     * @param id:主键id
     * @return void
     * @Description 检查产品名称是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     **/
    public Boolean checkName(String name, String id) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductInfoEntity::getName, name);
        queryWrapper.eq(ProductInfoEntity::getDeleteState, IsConstant.NO);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProductInfoEntity::getId, id);
        }
        Integer count = productInfoMapper.selectCount(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    /**
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     * @Description 根据sku查询sku表信息
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     **/
    public ProductDetailEntity getProductIdBySku(String sku) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getSkuNo, sku);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 根据产品id 获取对应的sku
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     * @author yl
     * @date 2022-11-21 17:17
     */
    @Override
    public List<ProductDetailEntity> getSkuListByProductId(String productId) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    /**
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     * @Description 导入无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
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
        if (StringUtils.isNotBlank(productSkuBaseInfoDTO.getImagesUrl())) {
            ProductImagesDTO productImagesDTO = new ProductImagesDTO();
            productImagesDTO.setSkuId(productSkuBaseInfoDTO.getId());
            productImagesDTO.setProductId(productSkuBaseInfoDTO.getProductId());
            productImagesDTO.setImagesUrl(productSkuBaseInfoDTO.getImagesUrl());
            productImagesService.updateProductImage(productImagesDTO);
        }
        String skuId = this.saveOrUpdate(productSkuBaseInfoDTO);
        if (StringUtils.isBlank(productSkuBaseInfoDTO.getId())) {
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
     *
     * @param productSkuExcelDTO exportSkuExcelDTO
     * @param response           response
     * @return void
     * @Author Luo_WG
     * @Date 2022/10/10 12:09
     **/
    @Override
    public void exportProduct(ProductSkuExcelDTO productSkuExcelDTO, HttpServletResponse response) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<ExportSkuExcelDTO> exportSkuExcelDTO = productDetailMapper.getExportSkuExcel(productSkuExcelDTO);
        exportSkuExcelDTO.forEach(req -> {
            //销售状态编码转换成中文
            req.setProductState(ProductDetailStateEnum.getNameByCode(Integer.valueOf(req.getProductState())));
            if (StringUtils.isNotBlank(req.getSaleState())) {
                req.setSaleState(SaleStateEnum.getNameByCode(Integer.valueOf(req.getSaleState())));
            }
            //采购状态编码转换成中文
            if (StringUtils.isNotBlank(req.getArrivalState())) {
                req.setArrivalState(PurchaseStateEnum.getNameByCode(Integer.valueOf(req.getArrivalState())));
            }
            if (StringUtils.isNotBlank(req.getPurchaseUser())) {
                FindUserDTO findUserDTO = userList.stream().filter(q -> q.getUserId().equals(req.getPurchaseUser())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(findUserDTO)) {
                    req.setPurchaseUser(findUserDTO.getUserName());
                }
            }

            if (StringUtils.isNotBlank(req.getPurchaseUser())) {
                String[] split = req.getSaleCountry().split(",");
                List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
                List<String> nameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                req.setSaleCountry(StringUtils.join(nameList, ","));
            }
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

    @Override
    public List<ProductDetailEntity> getByIdList(List<String> skuIdList) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            queryWrapper.in(ProductDetailEntity::getId, skuIdList);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }


    /**
     * 获取那些sku 没有完成
     *
     * @param skuIdList
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.String>>
     * @author yl
     * @date 2022-11-28 18:29
     */
    @Override
    public List<String> getNotFinish(List<String> skuIdList) {
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(ProductDetailEntity::getSkuNo);
            queryWrapper.eq(ProductDetailEntity::getIsFinishTask, IsConstant.NO);
            queryWrapper.in(ProductDetailEntity::getId, skuIdList);
            return listObjs(queryWrapper,Object::toString);
        }
        return new ArrayList<>();

    }


    @Override
    public Boolean approvalPass(ProductDetailOperateDTO dto) {
        ProductDetailEntity entity = this.getById(dto.getId());
        //验证是否设置审核人
        ProductDetailApproverEntity approverEntity = productDetailApproverService.getProductDetailApprover();
        if (ObjectUtils.isEmpty(approverEntity)) {
            throw new ServiceException(ApiError.ERROR_95076);
        }
        //只有待审核和审核中数据可以审核
        if (!ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus()) && !ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95038);
        }
        //验证sku关联任务是否已完成
        if (!IsConstant.YES.equals(entity.getIsFinishTask())) {
            throw new ServiceException(ApiError.ERROR_95077);
        }

        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
        //这是用户待审核的流程id
        List<String> processInstanceIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
        //传过来的流程id 和 当前用户的流程id 如果当前用户的流程id 不包含 就是不能审核
        if (!processInstanceIds.contains(entity.getProcessId())) {
            throw new ServiceException(ApiError.ERROR_95049);
        }
        //同流程下一个人只有一个待办
        TaskShowDTO taskShowDTO = myToDoList.stream().filter(obj -> obj.getProcessInstanceId().equals(entity.getProcessId())).findFirst().orElse(null);
        //调用审核通过审核流
        ApproveProcessDTO approveProcess = new ApproveProcessDTO();
        approveProcess.setTaskId(taskShowDTO.getTaskId());
        approveProcess.setProcessInstanceId(entity.getProcessId());
        approveProcess.setUserId(userId);
        approveProcess.setComment(dto.getComment());
        CompletableFuture completableFuture = CompletableFuture.supplyAsync(() -> {
            return workflowFeign.taskPass(approveProcess);
        });

        //查询审核任务下所有待办
        Integer code = ProductDetailStatusEnum.APPROVAL_ING.getCode();
        //更新产品信息状态
        Boolean flag = this.updateProductDetailState(dto.getId(), code, userId, userName);
        if (flag) {
            //新增操作日志

        }
        return true;
    }

    @Override
    public Boolean approvalReject(ProductDetailOperateDTO dto) {
        ProductDetailEntity entity = this.getById(dto.getId());
        //验证是否设置审核人
        ProductDetailApproverEntity approverEntity = productDetailApproverService.getProductDetailApprover();
        if (ObjectUtils.isEmpty(approverEntity)) {
            throw new ServiceException(ApiError.ERROR_95076);
        }
        //只有待审核和审核中数据可以审核
        if (!ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus()) && !ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95046);
        }
        //验证sku关联任务是否已完成
        if (!IsConstant.YES.equals(entity.getIsFinishTask())) {
            throw new ServiceException(ApiError.ERROR_95077);
        }

        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
        //这是用户待审核的流程id
        List<String> processInstanceIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
        //传过来的流程id 和 当前用户的流程id 如果当前用户的流程id 不包含 就是不能审核
        if (!processInstanceIds.contains(entity.getProcessId())) {
            throw new ServiceException(ApiError.ERROR_95049);
        }
        Integer code = ProductDetailStatusEnum.APPROVAL_NO_PASS.getCode();
        //更新产品信息状态
        Boolean flag = this.updateProductDetailState(dto.getId(), code, userId, userName);
        if (flag) {
            //新增操作日志

        }
        //新增审核不通过意见
        ProductDetailCommentEntity commentEntity = new ProductDetailCommentEntity();
        commentEntity.setComment("[审核结果-审核不通过]" + dto.getComment());
        commentEntity.setProductDetailId(dto.getId());
        commentEntity.setCreateUserName(loginUser.getUserName());
        commentEntity.setCreateUserId(loginUser.getUid());
        return productDetailCommentService.save(commentEntity);
    }


    @Override
    public Boolean updateApprover(ProductDetailApproveParamDTO dto) {
        ProductDetailApproverEntity entity = new ProductDetailApproverEntity();
        BeanMapperUtils.copy(dto,entity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        entity.setCreateUserId(userId);
        entity.setCreateUserName(userName);
        return  productDetailApproverService.saveOrUpdate(entity);
    }

    @Override
    @Transactional
    public Boolean productDetailProcessPass(String processId) {
        LoginUser loginUser = commonService.getUserInfo();
        LambdaQueryWrapper< ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getProcessId, processId);
        queryWrapper.last("LIMIT 1");
        ProductDetailEntity entity = this.getOne(queryWrapper);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.Default);
        }
        entity.setStatus(ProductDetailStatusEnum.APPROVAL_PASS.getCode());
        entity.setUpdateUserId(loginUser.getUid());
        entity.setUpdateUserName(loginUser.getUserName());
        return  this.updateById(entity);
    }

    @Override
    public Boolean applyChange(String id) {
        ProductDetailEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95078);
        }
        Integer code = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        //验证sku关联任务是否完成
        if (IsConstant.YES.equals(entity.getIsFinishTask())) {
            throw new ServiceException(ApiError.ERROR_95079);
        }
        //验证sku是否审核通过
        if (!code.equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95080);
        }
        LoginUser loginUser = commonService.getUserInfo();
        LambdaUpdateWrapper<ProductDetailEntity> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(ProductDetailEntity::getIsChange,IsConstant.YES);
        updateWrapper.set(ProductDetailEntity::getUpdateUserId,loginUser.getUid());
        updateWrapper.set(ProductDetailEntity::getUpdateUserName,loginUser.getUserName());
        updateWrapper.eq(ProductDetailEntity::getId,id);
        return this.update(updateWrapper);
    }


    @Override
    @Transactional
    public Boolean deApprove(String id) {
        ProductDetailEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95078);
        }
        //验证sku是否审核通过
        if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95081);
        }
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        //调用回退方法
        ApproveProcessDTO approveProcess = new ApproveProcessDTO();
        approveProcess.setProcessInstanceId(entity.getProcessId());
        approveProcess.setUserId(userId);
        CompletableFuture completableFuture = CompletableFuture.supplyAsync(() -> {
            return workflowFeign.rejectOriginProcess(approveProcess);
        });
        Integer code = ProductDetailStatusEnum.WAIT_CONFIRM.getCode();
        //更新产品信息状态
        return this.updateProductDetailState(id, code, userId, userName);
    }


    /**
     * @description: 更新产品信息状态
     * @author Will
     * @date: 2022/11/28 15:40
     * @param id
     * @param state
     * @param userId
     * @param userName
     * @return Boolean
     */
    private Boolean updateProductDetailState(String id,Integer state,String userId,String userName) {
        LambdaUpdateWrapper<ProductDetailEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ProductDetailEntity::getStatus,state);
        updateWrapper.set(ProductDetailEntity::getUpdateUserId,userId);
        updateWrapper.set(ProductDetailEntity::getUpdateUserName,userName);
        updateWrapper.eq(ProductDetailEntity::getId,id);
        return this.update(updateWrapper);
    }
}
