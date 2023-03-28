package com.erp.server.plm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SkuApproveConfigureEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.AlgorithmUtil;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.workflow.dto.ApproveProcessDTO;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.model.workflow.dto.TaskShowDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.common.business.constant.IsConstant;
import com.erp.server.plm.constant.ProductManyDetailConstant;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeProductDetailService;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 产品明细信息服务类
 * @Author: Luo_WG
 * @Date: 2022/9/21 16:25
 **/
@Slf4j
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

    @Resource
    private ProjectInfoService projectInfoService;

    @Resource
    private ProductVariantService productVariantService;

    @Resource
    private SysLogService sysLogService;

    @Resource
    private ProjectTaskService projectTaskService;

    @Resource
    private SyncKingdeeProductDetailService syncKingdeeProductDetailService;

    @Resource
    private ProductChangeService productChangeService;

    @Resource
    private ProductPlanService productPlanService;

    @Resource
    private ProductAccessoriesService productAccessoriesService;

    @Resource
    private ProductAttestationService productAttestationService;

    @Autowired
    private ProductArchiveService archiveService;

    //变更财务人员审核
    @Value("${changeFinancialAudit}")
    private String financial;


    private static final String SPUCLASSPATH = String.valueOf(ProductInfoEntity.class);
    private static final String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);

    /**
     * @param pagingDTO:查询参数
     * @return java.util.List<com.erp.model.plm.dto.ProductDetailShowDTO>
     * @Description 产品信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     **/
    @Override
    public PagingVO<ProductDetailShowDTO> paging(PagingDTO<ProductSkuDTO> pagingDTO) {
        //待审核查询分配给自己的数据
        if (MathUtil.ONE.toString().equals(pagingDTO.getParams().getType())) {
            LoginUser loginUser = CommonInterceptor.threadLocal.get();
            List<TaskShowDTO> workflowList = workflowFeign.queryMyToDo(loginUser.getUid());
            //无待办则直接返回
            if (CollectionUtils.isEmpty(workflowList)) {
                IPage<ProductDetailShowDTO> list = new Page<>();
                return new PagingVO(list);
            }
            List<String> processIds = workflowList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
            pagingDTO.getParams().setProcessIds(processIds);
            //待审核，审核中
            pagingDTO.getParams().setStatusList(Arrays.asList(ProductDetailStatusEnum.WAIT_CONFIRM.getCode(), ProductDetailStatusEnum.APPROVAL_ING.getCode()));
        }
        if (MathUtil.TWO.toString().equals(pagingDTO.getParams().getType())) {
            //已审核
            pagingDTO.getParams().setStatusList(Arrays.asList(ProductDetailStatusEnum.APPROVAL_PASS.getCode()));
        }
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());


        IPage<ProductDetailShowDTO> pageData = productDetailMapper.paging(query, pagingDTO.getParams());
        List<ProductDetailShowDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        List<String> productIdList = list.stream().map(ProductDetailShowDTO::getId).collect(Collectors.toList());
        List<ProjectInfoEntity> projectList = projectInfoService.getByProductIdList(productIdList);
        List<String> sourceIds = list.stream().map(ProductDetailShowDTO::getId).collect(Collectors.toList());
        List<String> changeIngSourceIds = productChangeService.getBySourceId(sourceIds);

        for (ProductDetailShowDTO item : list) {
            item.setStatusName(ProductDetailStatusEnum.getName(item.getStatus()));
            Boolean isChangeIng = changeIngSourceIds.contains(item.getId());
            item.setIsChangeIng(isChangeIng);
            //首批到货状态
            Integer arrivalState = item.getArrivalState();
            item.setArrivalStateName(PurchaseStateEnum.getNameByCode(arrivalState));
            //侵权风险
            Integer pirateRisk = item.getPirateRisk();
            item.setPirateRiskName(PirateRiskEnum.getName(pirateRisk));
            //产品状态
            Integer productState = item.getProductState();
            item.setProductStateName(ProductDetailStateEnum.getNameByCode(productState));
            //是否可销售
            Integer isMarketable = item.getIsMarketable();
            Integer saleState = item.getSaleState();
            String saleStateName = SaleStateEnum.getNameByCode(saleState);
            item.setSaleStateName(saleStateName);
            Integer yes = 0;
            if (yes.equals(isMarketable)) {
                item.setIsMarketableName("是");
            } else {
                item.setIsMarketableName("否");
            }
            //项目经理
            String projectChargeName = projectList.stream().filter(p -> p.getProductId().
                    equals(item.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getChargeName())).orElse("");
            item.setProjectChargeName(projectChargeName);


        }

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

        List<ProductDetailEntity> detailEntityList = this.queryByProductId(productId);

        //产品辅料信息
        List<ProductAccessoriesDTO> productAccessoriesList = productAccessoriesService.getByProductId(productId);
        List<String> accessoriesSkuIds = productAccessoriesList.stream().filter(a -> StringUtils.isNotBlank(a.getAccessoriesSkuId())).
                map(ProductAccessoriesDTO::getAccessoriesSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> detailList = this.getByIdList(accessoriesSkuIds);
        for (ProductAccessoriesDTO accessories : productAccessoriesList) {
            ProductDetailEntity detailEntity = detailList.stream().filter(d -> d.getId().equals(accessories.getAccessoriesSkuId())).
                    findFirst().orElse(null);
            if (detailEntity != null) {
                accessories.setAccessoriesSkuImagesUrl(detailEntity.getImagesUrl());
                accessories.setAccessoriesSkuName(detailEntity.getName());
                accessories.setAccessoriesSkuNo(detailEntity.getSkuNo());
            }
        }
        productNoSpecDetailAllDTO.setProductAccessoriesList(productAccessoriesList);

        //产品认证信息
        List<ProductAttestationDTO> productAttestationList = productAttestationService.getByProductId(productId);
        for (ProductAttestationDTO attestation : productAttestationList) {
            String skuNo = detailEntityList.stream().filter(d -> d.getId().equals(attestation.getSkuId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            attestation.setSkuNo(skuNo);

            String skuImagesUrl = detailEntityList.stream().filter(d -> d.getId().equals(attestation.getSkuId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getImagesUrl())).orElse("");
            attestation.setSkuImagesUrl(skuImagesUrl);
        }
        productNoSpecDetailAllDTO.setProductAttestationList(productAttestationList);
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
        if (!Objects.isNull(manySpecDetailById)) {
            //基础信息 禁用字段
            List<String> manySpecBaseDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_MANY_SPEC_BASE, refSkuFiledConfigList);
            manySpecDetailById.setDisableFieldList(manySpecBaseDisableFields);
            //获取多级分类
            List<String> categoryIdList = basicCategoryService.getPidList(manySpecDetailById.getCategoryId());
            manySpecDetailById.setCategoryIdList(categoryIdList);
            productManyDetail.setProductManySpecBaseDTO(manySpecDetailById);
        }
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

        //产品辅料信息
        List<ProductAccessoriesDTO> productAccessoriesList = productAccessoriesService.getByProductId(productId);
        List<String> accessoriesSkuIds = productAccessoriesList.stream().filter(a -> StringUtils.isNotBlank(a.getAccessoriesSkuId())).
                map(ProductAccessoriesDTO::getAccessoriesSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> detailList = this.getByIdList(accessoriesSkuIds);

        for (ProductAccessoriesDTO accessories : productAccessoriesList) {
            ProductDetailEntity detailEntity = detailList.stream().filter(d -> d.getId().equals(accessories.getAccessoriesSkuId())).
                    findFirst().orElse(null);
            if (detailEntity != null) {
                accessories.setAccessoriesSkuImagesUrl(detailEntity.getImagesUrl());
                accessories.setAccessoriesSkuName(detailEntity.getName());
                accessories.setAccessoriesSkuNo(detailEntity.getSkuNo());
            }
        }


        productManyDetail.setProductAccessoriesList(productAccessoriesList);

        //产品认证信息
        List<ProductAttestationDTO> productAttestationList = productAttestationService.getByProductId(productId);
        for (ProductAttestationDTO attestation : productAttestationList) {
            String skuNo = list.stream().filter(d -> d.getId().equals(attestation.getSkuId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            attestation.setSkuNo(skuNo);

            String skuImagesUrl = list.stream().filter(d -> d.getId().equals(attestation.getSkuId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getImagesUrl())).orElse("");
            attestation.setSkuImagesUrl(skuImagesUrl);
        }
        productManyDetail.setProductAttestationList(productAttestationList);
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
    @Override
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
        detailEntity.setIsChange(IsConstant.NO);
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
            req.setIsChange(IsConstant.NO);
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
        //SKU操作日志-产品信息
        ProductInfoEntity productInfoEntity = productInfoService.getById(productSpuBaseInfoDTO.getId());
        if (StringUtils.isNotBlank(productSpuBaseInfoDTO.getId())) {
            //产品信息修改操作日志
            addProductInfoLog(productSpuBaseInfoDTO, productInfoEntity, productSpuBaseInfoDTO.getId(), productSpuBaseInfoDTO.getId());
        }
        //1.修改产品表 主表信息
        productSpuBaseInfoDTO.setIsNoSpecAdd(MathUtil.ONE);
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
        //SKU修改操作日志
        Boolean isAdd = false;
        if (StringUtils.isNotBlank(productSkuBaseInfoDTO.getId())) {
            ProductDetailEntity oldEntity = this.getById(productSkuBaseInfoDTO.getId());
            addProductSkuBaseInfoLog(productSkuBaseInfoDTO, oldEntity, productSkuBaseInfoDTO.getId(), id);
        } else {
            isAdd = true;
        }
        String skuId = this.saveOrUpdate(productSkuBaseInfoDTO);
        //SKU新增操作日志
        if (isAdd) {
            sysLogService.addSysLogBySave("生成了一个SKU：[" + productSkuBaseInfoDTO.getSkuNo() + "]", SKUCLASSPATH, skuId, id);
        }

        //3.修改/新增 成本信息
        if (ObjectUtils.isNotEmpty(productNoSpecDTO.getProductCostDTO())) {
            ProductCostDTO productCostDTO = productNoSpecDTO.getProductCostDTO();
            productCostDTO.setSkuId(skuId);
            //SKU操作日志
            addProductCostLog(productCostDTO, id);
            productCostService.saveOrUpdate(productNoSpecDTO.getProductCostDTO());
        }

        //4.修改/新增 采购信息
        if (ObjectUtils.isNotEmpty(productNoSpecDTO.getProductPurchaseDTO())) {
            ProductPurchaseDTO productPurchaseDTO = productNoSpecDTO.getProductPurchaseDTO();
            productPurchaseDTO.setSkuId(skuId);
            //SKU操作日志
            addProductPurchaseLog(productPurchaseDTO, id);
            productPurchaseService.saveOrUpdate(productNoSpecDTO.getProductPurchaseDTO());

        }
        //新增/修改采购备注信息
        if (!ListUtils.isEmpty(productNoSpecDTO.getProductPurchaseRemarkList())) {
            List<ProductPurchaseRemarkDTO> productPurchaseRemarkList = productNoSpecDTO.getProductPurchaseRemarkList();
            List<SysLogEntity> list = new LinkedList<>();
            productPurchaseRemarkList.forEach(req -> {
                req.setProductId(id);
                list.add(new SysLogEntity().setContent("更新采购备注信息：" + req.getRemark()).setClassPath(SPUCLASSPATH).setBusinessId(id).setPid(id));
            });
            //SKU操作日志
            sysLogService.addSysLogByBatchSave(list);
            productPurchaseRemarkService.saveOrUpdateBatch(productPurchaseRemarkList);
        }

        //5.修改/新增 销售信息
        ProductSaleDTO productSaleDTO = productNoSpecDTO.getProductSaleDTO();
        if (ObjectUtils.isNotEmpty(productSaleDTO)) {
            productSaleDTO.setSkuId(skuId);
            //SKU操作日志
            addProductSaleLog(productSaleDTO, id);
            productSaleService.saveOrUpdate(productSaleDTO);
        }
        //6.修改/新增 物流信息
        ProductLogisticsDTO productLogisticsDTO = productNoSpecDTO.getProductLogisticsDTO();
        if (ObjectUtils.isNotEmpty(productLogisticsDTO)) {
            productLogisticsDTO.setSkuId(skuId);
            //SKU操作日志
            addProductLogisticsLog(productLogisticsDTO, id);
            productLogisticsService.saveOrUpdate(productLogisticsDTO);
        }

        //7.修改/新增 包装信息
        ProductPackDTO productPackDTO = productNoSpecDTO.getProductPackDTO();
        if (ObjectUtils.isNotEmpty(productPackDTO)) {
            productPackDTO.setSkuId(skuId);
            //SKU操作日志
            addProductPackLog(productPackDTO, id);
            productPackService.saveOrUpdate(productPackDTO);
        }

        //8.修改/新增 证书信息
        List<ProductCertificateDTO> productCertificateList = productNoSpecDTO.getProductCertificateList();
        if (ObjectUtils.isNotEmpty(productCertificateList)) {
            //SKU操作日志
            addProductCertificateLog(productCertificateList, id);
            productCertificateService.saveOrUpdateBatch(productCertificateList);
        }
        //更新规划中的首批入库时间和上市时间
        productPlanService.updateRealDateByProductId(id);

        //9.修改/新增  包装辅料信息
        List<ProductAccessoriesDTO> productAccessoriesList = productNoSpecDTO.getProductAccessoriesList();
        if (CollectionUtils.isNotEmpty(productAccessoriesList)) {

            for (ProductAccessoriesDTO accessories : productAccessoriesList) {
                accessories.setParentSkuId(skuId);
                accessories.setProductId(id);
            }

            //添加包装辅料的日志
            addProductAccessoriesLog(productAccessoriesList, id);

            productAccessoriesService.saveOrUpdateBatchAccessories(productAccessoriesList);
        }

        //10.修改/新增  认证信息
        List<ProductAttestationDTO> productAttestationList = productNoSpecDTO.getProductAttestationList();
        if (CollectionUtils.isNotEmpty(productAttestationList)) {
            for (ProductAttestationDTO attestation : productAttestationList) {
                attestation.setSkuId(skuId);
            }

            addProductAttestationLog(productAttestationList, id);
            productAttestationService.saveOrUpdateBatchAttestation(productAttestationList);
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
        //SKU操作日志-产品信息
        ProductInfoEntity productInfoEntity = productInfoService.getById(productInfoDTO.getId());
        if (ObjectUtils.isNotEmpty(productInfoDTO)) {
            if (StringUtils.isNotBlank(productInfoDTO.getId())) {
                //产品操作日志
                addProductInfoLog(productInfoDTO, productInfoEntity, productInfoDTO.getId(), productInfoDTO.getId());
            }
            productInfoService.updateSpec(productInfoDTO);
        }

        //2.修改/新增 sku信息
        List<ProductDetailDTO> productDetailLists = productManySpecDTO.getProductDetailList();
        if (productDetailLists.size() > 0) {
            //操作日志
            productDetailLists.forEach(obj -> {
                ProductDetailEntity oldEntity = this.getById(obj.getId());
                //sku操作日志
                addProductDetailLog(obj, oldEntity, obj.getId(), productInfoDTO.getId());
            });
            this.saveOrUpdateBatch(productManySpecDTO.getProductDetailList());
        }

        //3.修改/新增 成本信息
        List<ProductCostDTO> productCostList = productManySpecDTO.getProductCostList();
        if (productCostList.size() > 0) {
            //操作日志
            productCostList.stream().forEach(obj -> addProductCostLog(obj, productInfoDTO.getId()));
            productCostService.saveOrUpdateBatch(productManySpecDTO.getProductCostList());
        }

        //4.修改/新增 采购信息
        List<ProductPurchaseDTO> productPurchaseList = productManySpecDTO.getProductPurchaseList();
        if (productPurchaseList.size() > 0) {
            //操作日志
            productPurchaseList.stream().forEach(obj -> addProductPurchaseLog(obj, productInfoDTO.getId()));
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
            //操作日志
            productSaleList.stream().forEach(obj -> addProductSaleLog(obj, productInfoDTO.getId()));
            productSaleService.saveOrUpdateBatch(productSaleList);
        }

        //6.修改/新增 物流信息
        List<ProductLogisticsDTO> productLogisticsList = productManySpecDTO.getProductLogisticsList();
        if (productLogisticsList.size() > 0) {
            //操作日志
            productLogisticsList.stream().forEach(obj -> addProductLogisticsLog(obj, productInfoDTO.getId()));
            productLogisticsService.saveOrUpdateBatch(productLogisticsList);
        }
        //7.修改/新增 包装信息
        List<ProductPackDTO> productPackList = productManySpecDTO.getProductPackList();
        if (productPackList.size() > 0) {
            //操作日志
            productPackList.stream().forEach(obj -> addProductPackLog(obj, productInfoDTO.getId()));
            productPackService.saveOrUpdateBatch(productPackList);
        }

        //8.修改/新增 证书信息
        List<ProductCertificateDTO> productCertificateList = productManySpecDTO.getProductCertificateList();
        if (productCertificateList.size() > 0) {
            //操作日志
            addProductCertificateLog(productCertificateList, productInfoDTO.getId());
            productCertificateService.saveOrUpdateBatch(productCertificateList);
        }
        //更新规划中的首批入库时间和上市时间
        productPlanService.updateRealDateByProductId(productInfoDTO.getId());

        //9.修改/新增  包装辅料信息
        List<ProductAccessoriesDTO> productAccessoriesList = productManySpecDTO.getProductAccessoriesList();
        if (CollectionUtils.isNotEmpty(productAccessoriesList)) {
            productAccessoriesList.stream().forEach(p -> p.setProductId(productInfoDTO.getId()));
            //添加包装辅料的日志
            addProductAccessoriesLog(productAccessoriesList, productInfoDTO.getId());
            productAccessoriesService.saveOrUpdateBatchAccessories(productAccessoriesList);
        }

        //10.修改/新增  认证信息
        List<ProductAttestationDTO> productAttestationList = productManySpecDTO.getProductAttestationList();
        if (CollectionUtils.isNotEmpty(productAttestationList)) {
            addProductAttestationLog(productAttestationList, productInfoDTO.getId());
            productAttestationService.saveOrUpdateBatchAttestation(productAttestationList);
        }


        return true;
    }


    /**
     * 添加产品认证的日志
     *
     * @param productAttestationList
     * @param productId
     * @return void
     * @author yl
     * @date 2023-02-27 10:02
     */
    private void addProductAttestationLog(List<ProductAttestationDTO> productAttestationList, String productId) {

        if (CollectionUtils.isEmpty(productAttestationList)) {
            return;
        }
        List<AttestationDTO> attestationList = new ArrayList<>(10);

        List<String> skuIds = productAttestationList.stream().map(ProductAttestationDTO::getSkuId).collect(Collectors.toList());

        List<ProductAttestationEntity> attestationDataList = productAttestationService.getBySkuIds(skuIds);

        for (ProductAttestationDTO item : productAttestationList) {
            //产品认证
            List<String> productDictList = item.getProductList();
            for (String productDict : productDictList) {
                AttestationDTO product = new AttestationDTO();
                product.setSkuId(item.getSkuId());
                product.setType(ProductManyDetailConstant.PRODUCT_ATTESTATION);
                product.setDictId(productDict);
                attestationList.add(product);
            }

            //其它认证
            List<String> otherDictList = item.getOtherList();
            for (String otherDict : otherDictList) {
                AttestationDTO other = new AttestationDTO();
                other.setDictId(otherDict);
                other.setSkuId(item.getSkuId());
                other.setType(ProductManyDetailConstant.OTHER_ATTESTATION);
                attestationList.add(other);
            }

            //运输认证
            List<String> transportDictList = item.getTransportList();
            for (String transportDict : transportDictList) {
                AttestationDTO transport = new AttestationDTO();
                transport.setSkuId(item.getSkuId());
                transport.setType(ProductManyDetailConstant.TRANSPORT_ATTESTATION);
                transport.setDictId(transportDict);
                attestationList.add(transport);
            }
        }
        if (CollectionUtils.isEmpty(attestationList)) {
            return;
        }

        attestationList.forEach(obj -> {
            //SKU操作日志
            ProductAttestationEntity oldEntity = attestationDataList.stream().filter(e -> e.getDictId().equals(obj.getDictId())).findFirst().orElse(null);
            AttestationDTO oldDto = new AttestationDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
            }
            ProductDetailEntity productDetailEntity = this.getById(obj.getSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            sysLogService.addSysLogByUpdate(oldDto, obj, SKUCLASSPATH, obj.getSkuId(), productId, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
        });
    }

    /**
     * 添加包装辅料的信息 操作日志
     *
     * @param productAccessoriesList
     * @param productId
     * @return void
     * @author yl
     * @date 2023-02-27 9:58
     */

    private void addProductAccessoriesLog(List<ProductAccessoriesDTO> productAccessoriesList, String productId) {
        if (CollectionUtils.isEmpty(productAccessoriesList)) {
            return;
        }
        List<String> ids = productAccessoriesList.stream().filter(obj -> StringUtils.isNotBlank(obj.getId())).map(ProductAccessoriesDTO::getId).collect(Collectors.toList());
        List<ProductAccessoriesEntity> accessoriesEntityList = productAccessoriesService.getListByIds(ids);
        productAccessoriesList.forEach(obj -> {
            //SKU操作日志
            ProductAccessoriesEntity oldEntity = accessoriesEntityList.stream().filter(e -> e.getId().equals(obj.getId())).findFirst().orElse(null);
            ProductAccessoriesDTO oldDto = new ProductAccessoriesDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
            }
            ProductDetailEntity productDetailEntity = this.getById(obj.getParentSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            sysLogService.addSysLogByUpdate(oldDto, obj, SKUCLASSPATH, obj.getParentSkuId(), productId, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
        });
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
        //产品等级
        if (StringUtils.isNotBlank(productSpuBaseInfoDTO.getGradeId())) {
            //根据id查询字典表中的产品等级
            BasicDictEntity basicDict = basicDictService.getById(productSpuBaseInfoDTO.getGradeId());
            if (ObjectUtils.isNotEmpty(basicDict)) {
                productSpuBaseInfoDTO.setGrade(basicDict.getValue());
            }
        }
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
            //查询变体信息
            List<ProductVariantDTO> productVariantDTOS = productVariantService.listVariantAndProperty();
            if (CollectionUtils.isEmpty(productVariantDTOS)) {
                throw new ServiceException(ApiError.ERROR_95079);
            }
            //变体颜色信息
            ProductVariantDTO productVariantDTO = productVariantDTOS.stream().filter(obj -> obj.getPropertyType().equals("颜色")).findAny().orElse(null);
            if (ObjectUtils.isEmpty(productVariantDTO)) {
                throw new ServiceException(ApiError.ERROR_95080);
            }
            //变体颜色属性值
            List<ProductVariantPropertyDTO> productVariantPropertyList = productVariantDTO.getProductVariantPropertyList();
            if (CollectionUtils.isEmpty(productVariantPropertyList)) {
                throw new ServiceException(ApiError.ERROR_95081);
            }
            ProductVariantPropertyDTO propertyDto = productVariantPropertyList.stream().filter(obj -> split.contains(obj.getPropertyValue())).findAny().orElse(null);
            if (ObjectUtils.isEmpty(propertyDto) || StringUtils.isBlank(propertyDto.getPropertyCode())) {
                throw new ServiceException(ApiError.ERROR_95074);
            }
            //生成sku编码
            String skuNo = sysCodeService.getSkuNo(id, propertyDto.getPropertyCode());
            productDetailEntity.setSkuNo(skuNo);
            productDetailEntity.setChargeId(productSpuBaseInfoDTO.getChargeId());
            productDetailEntity.setChargeName(productSpuBaseInfoDTO.getChargeName());
            list.add(productDetailEntity);
        }

        boolean bool = this.saveBatch(list);
        if (!bool) {
            throw new ServiceException(1, "新增sku明细失败！");
        }

        //需要默认添加 产品认证 和 产品包装信息


        //SKU新增操作日志
        List<SysLogEntity> logs = new LinkedList<>();
        //SKU新增任务关联数据
        List<ProjectTaskRefSkuEntity> projectTaskRefSkuList = new ArrayList<>();


        list.forEach(obj -> {
            //配置表单生成SKU需要建立关联关系
            if (IsConstant.YES.equals(variantAutoAddDTO.getFlag())) {
                ProjectTaskRefSkuEntity entity = new ProjectTaskRefSkuEntity();
                entity.setProductId(id);
                entity.setSkuId(obj.getId());
                entity.setTaskId(variantAutoAddDTO.getTaskId());
                entity.setIsFinishTask(IsConstant.YES);
                projectTaskRefSkuList.add(entity);
            }
            logs.add(new SysLogEntity().setClassPath(SKUCLASSPATH).setBusinessId(obj.getId()).setPid(id).setOperation("新增信息").setContent("生成了一个SKU：[" + obj.getSkuNo() + "]"));
        });
        if (StringUtils.isBlank(productSpuBaseInfoDTO.getId())) {
            logs.add(new SysLogEntity().setClassPath(SPUCLASSPATH).setBusinessId(id).setPid(id).setOperation("新增信息").setContent("生成了一个产品：[" + productSpuBaseInfoDTO.getName() + "]"));
        }

        if (CollectionUtils.isNotEmpty(projectTaskRefSkuList)) {
            projectTaskRefSkuService.saveBatch(projectTaskRefSkuList);
        }
        //新增日志
        sysLogService.addSysLogByBatchSave(logs);
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

        ProductDetailEntity productDetailEntity = this.getById(skuId);

        if (ObjectUtils.isNotEmpty(productDetailEntity)) {
            ProductInfoEntity productInfoEntity = productInfoService.getById(productDetailEntity.getProductId());
            if (ObjectUtils.isNotEmpty(productInfoEntity)) {
                //添加操作日志
                SysLogEntity sysLogEntity = new SysLogEntity().setClassPath(SPUCLASSPATH).setBusinessId(productInfoEntity.getId()).setPid(productInfoEntity.getId()).setOperation("删除信息").setContent("删除了一个SKU：[" + productDetailEntity.getSkuNo() + "]");
                sysLogService.addSysLogByOther(sysLogEntity);
            }
        }
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public ProductDetailEntity getProductIdBySku(String sku) {
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductDetailEntity::getSkuNo, sku);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     * @Description 根据sku查询sku表信息(数据清洗)
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     **/
    @Override
    public CleanSkuDto getProductIdBySkuClean(String sku) {
        return baseMapper.getProductIdBySkuClean(sku);
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
     * @param productSkuExcelDTO
     * @param response           response
     * @return void
     * @Author Luo_WG
     * @Date 2022/10/10 12:09
     **/
    @Override
    public void exportProduct(ProductSkuExcelDTO productSkuExcelDTO, HttpServletResponse response) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<ProductDetailExcelDTO> list = productDetailMapper.getExportSkuExcel(productSkuExcelDTO);
        list.forEach(req -> {
            //销售状态编码转换成中文
            req.setProductStateName(ProductDetailStateEnum.getNameByCode(Integer.valueOf(req.getProductStateName())));
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
        String excelPath = "excel/productNoSpecDetailExport.xlsx";
        String name = "产品sku明细表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
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
    public List<BaseIdDTO> getNotFinish(List<String> skuIdList) {
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            return baseMapper.getNotFinish(skuIdList);
        }
        return new ArrayList<>();
    }


    @Override
    public Boolean approvalPass(ProductDetailOperateDTO dto) {
        ProductDetailEntity entity = this.getById(dto.getId());
        //验证是否设置审核人
       /* ProductDetailApproverEntity approverEntity = productDetailApproverService.getProductDetailApprover();
        if (ObjectUtils.isEmpty(approverEntity)) {
            throw new ServiceException(ApiError.ERROR_95082);
        }*/
        //只有待审核和审核中数据可以审核
        if (!ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus()) && !ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95038);
        }
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
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
        //查询审核任务下所有待办
        Integer code = ProductDetailStatusEnum.APPROVAL_ING.getCode();
        //更新产品信息状态
        Boolean flag = this.updateProductDetailState(dto.getId(), code, userId, userName);
        if (flag) {
            //新增操作日志
            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                    .setBusinessId(dto.getId()).setOperation("状态变更").setContent("审核SKU[" + entity.getSkuNo() + "],操作[" + ProductDetailStatusEnum.getName(entity.getStatus()) + "]为[" + ProductDetailStatusEnum.APPROVAL_ING.getName() + "]"));
        }
        workflowFeign.taskPass(approveProcess);
        return true;
    }

    @Override
    @Transactional
    public Boolean approvalReject(ProductDetailOperateDTO dto) {
        ProductDetailEntity entity = this.getById(dto.getId());
        //验证是否设置审核人
        ProductDetailApproverEntity approverEntity = productDetailApproverService.getProductDetailApprover();
        if (ObjectUtils.isEmpty(approverEntity)) {
            throw new ServiceException(ApiError.ERROR_95082);
        }
        //只有待审核和审核中数据可以审核
        if (!ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus()) && !ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95046);
        }
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
        //这是用户待审核的流程id
        List<String> processInstanceIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
        //传过来的流程id 和 当前用户的流程id 如果当前用户的流程id 不包含 就是不能审核
        if (!processInstanceIds.contains(entity.getProcessId())) {
            throw new ServiceException(ApiError.ERROR_95049);
        }
        String taskId = myToDoList.stream().filter(obj -> entity.getProcessId().equals(obj.getProcessInstanceId())).map(TaskShowDTO::getTaskId).findFirst().orElse("");
        //审核不通过
        ApproveProcessDTO approveProcess = new ApproveProcessDTO();
        approveProcess.setTaskId(taskId);
        approveProcess.setProcessInstanceId(entity.getProcessId());
        approveProcess.setUserId(userId);
        approveProcess.setComment(dto.getComment());
        workflowFeign.taskNoPass(approveProcess);

        Integer code = ProductDetailStatusEnum.APPROVAL_NO_PASS.getCode();
        //更新产品信息状态
        Boolean flag = this.updateProductDetailState(dto.getId(), code, userId, userName);
        if (flag) {
            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                    .setBusinessId(dto.getId()).setOperation("状态变更").setContent("审核SKU[" + entity.getSkuNo() + "]操作[" + ProductDetailStatusEnum.getName(entity.getStatus()) + "]为[" + ProductDetailStatusEnum.APPROVAL_NO_PASS.getName() + "]，原因：" + dto.getComment()));
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
        BeanMapperUtils.copy(dto, entity);
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        entity.setCreateUserId(userId);
        entity.setCreateUserName(userName);
        return productDetailApproverService.saveOrUpdate(entity);
    }

    @Override
    @Transactional
    public Boolean productDetailProcessPass(String processId) {
        LoginUser loginUser = commonService.getUserInfo();
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getProcessId, processId);
        queryWrapper.last("LIMIT 1");
        ProductDetailEntity entity = this.getOne(queryWrapper);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.Default);
        }
        String statusName = ProductDetailStatusEnum.getName(entity.getStatus());
        entity.setStatus(ProductDetailStatusEnum.APPROVAL_PASS.getCode());
        entity.setUpdateUserId(loginUser.getUid());
        entity.setUpdateUserName(loginUser.getUserName());
        entity.setSyncKingdeeStatus(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode());
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setBusinessId(entity.getId()).setPid(entity.getProductId())
                .setOperation("状态变更").setContent("审核SKU[" + entity.getSkuNo() + "],操作[" + statusName + "]为[" + ProductDetailStatusEnum.APPROVAL_PASS.getName() + "]"));
        //审核通过后发送到金蝶系统
        syncKingdeeProductDetailService.syncDataToKingdee(entity);

        return this.updateById(entity);
    }


    @Override
    @Transactional
    public Boolean applyChange(String id) {
        ProductDetailEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        Integer code = ProductDetailStatusEnum.APPROVAL_PASS.getCode();

        //验证sku是否审核
        if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95086);
        }
        LoginUser loginUser = commonService.getUserInfo();
        LambdaUpdateWrapper<ProductDetailEntity> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(ProductDetailEntity::getIsChange, IsConstant.YES);
        updateWrapper.set(ProductDetailEntity::getUpdateUserId, loginUser.getUid());
        updateWrapper.set(ProductDetailEntity::getUpdateUserName, loginUser.getUserName());
        updateWrapper.eq(ProductDetailEntity::getId, id);
        boolean flag = this.update(updateWrapper);
        if (flag) {
            //新增操作日志
            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setBusinessId(entity.getId()).setPid(entity.getProductId())
                    .setOperation("申请变更").setContent("申请变更SKU[" + entity.getSkuNo() + "]信息"));
        }
        return flag;
    }


    @Override
    @Transactional
    public Boolean deApprove(String id) {
        ProductDetailEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //验证sku是否审核通过
        if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95087);
        }
        String statusName = ProductDetailStatusEnum.getName(entity.getStatus());
        //重新启动流程
        this.productDetailStartProcess(entity);
        //反审核后更新是否申请变更
        entity.setIsChange(IsConstant.NO);
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                .setBusinessId(entity.getId()).setOperation("状态变更").setContent("反审核SKU[" + entity.getSkuNo() + "],操作[" + statusName + "]为[" + ProductDetailStatusEnum.WAIT_CONFIRM.getName() + "]"));
        //反审核后用新的流程审核人员审核
        return this.updateById(entity);
    }

    @Override
    public Boolean restartProcessPass(ProductDetailOperateDTO dto) {
        ProductDetailEntity entity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        if (!ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95088);
        }
        this.productDetailStartProcess(entity);
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setBusinessId(entity.getId()).setPid(entity.getProductId())
                .setOperation("重启审核流程").setContent("SKU[" + entity.getSkuNo() + "]重启审核流程"));
        //反审核后用新的流程审核人员审核
        return this.updateById(entity);
    }

    @Override
    public ProductDetailDTO getSkuByParam(Map<String, String> params) {
        if (params == null) {
            return null;
        }
        String id = params.get("id");
        String skuNo = params.get("skuNo");
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.eq(ProductDetailEntity::getId, id);
        }
        if (StringUtils.isNotBlank(skuNo)) {
            queryWrapper.eq(ProductDetailEntity::getSkuNo, skuNo);
        }
        queryWrapper.last("limit 1");
        ProductDetailEntity entity = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(entity)) {
            ProductDetailDTO dto = new ProductDetailDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }
        return null;
    }

    @Override
    public List<SkuVO> getSkuBySkuNos(List<String> skuNoList) {
        return baseMapper.getSkuBySkuNos(skuNoList);
    }

    /**
     * 根据sku id 获取
     *
     * @param skuIdList
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-01-31 10:13
     */
    @Override
    public List<SkuVO> getSkuBySkuIds(List<String> skuIdList) {
        return baseMapper.getSkuBySkuIds(skuIdList);
    }


    /**
     * 搜索sku
     *
     * @param searchKeyword
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-01-11 15:06
     */
    @Override
    public List<SkuVO> searchSku(String searchKeyword) {
        return baseMapper.searchSku(searchKeyword, ProductDetailStatusEnum.APPROVAL_PASS.getCode());
    }


    @Override
    public void updateProductStateByProductId(String productId, Integer state) {
        LambdaUpdateWrapper<ProductDetailEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductDetailEntity::getProductId, productId);
        updateWrapper.set(ProductDetailEntity::getProductState, state);
        this.update(updateWrapper);
    }

    @Override
    public Boolean commit(String id) {
        ProductDetailEntity productDetailEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        if (!ProductDetailStatusEnum.WAIT_COMMIT.getCode().equals(productDetailEntity.getStatus()) && !ProductDetailStatusEnum.APPROVAL_NO_PASS.getCode().equals(productDetailEntity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95117);
        }
        //验证必填信息
        String str = checkRequiredData(productDetailEntity);
        if (StringUtils.isNotBlank(str)) {
            throw new ServiceException(new ApiResult(1, str));
        }

        List<ProjectTaskEntity> taskAllList = projectTaskService.listByProductId(productDetailEntity.getProductId());
        //存在关联任务并且含配置表单的任务需要验证是否完成
        if (CollectionUtils.isNotEmpty(taskAllList)) {
            List<String> taskIdList = taskAllList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            //配置表单信息
            List<TaskRefSkuConfigEntity> configList = taskRefSkuConfigService.getByTaskIds(taskIdList);

            if (CollectionUtils.isNotEmpty(configList)) {
                List<TaskRefSkuConfigEntity> hasConfigList = configList.stream().filter(obj -> StringUtils.isNotBlank(obj.getFieldJson())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(hasConfigList)) {

                    List<String> configTaskIds = hasConfigList.stream().map(TaskRefSkuConfigEntity::getTaskId).collect(Collectors.toList());

                    //验证关联任务是否已全部完成
                    long relatedCount = taskAllList.stream().filter(obj -> !RelatedSkuTypeEnum.NOT_RELATED.getCode().equals(obj.getRelatedSkuType()) && !TaskStateEnum.FINISH.getCode().equals(obj.getStatus()) && configTaskIds.contains(obj.getId()) ).count();
                    if (relatedCount > 0) {
                        throw new ServiceException(ApiError.ERROR_95083);
                    }
                }
            }
        }
        //启动流程
        productDetailStartProcess(productDetailEntity);

        return this.updateById(productDetailEntity);
    }

    @Override
    public Boolean unCommit(String id) {
        ProductDetailEntity productDetailEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //待审核、审核中的sku才能取消流程
        if (!ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(productDetailEntity.getStatus()) && !ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(productDetailEntity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95118);
        }
        //中止之前的流程
        ApproveProcessDTO processDTO = new ApproveProcessDTO();
        processDTO.setProcessInstanceId(productDetailEntity.getProcessId());
        workflowFeign.terminate(processDTO);

        //清除流程id
        productDetailEntity.setProcessId("");
        //更新审核状态
        productDetailEntity.setStatus(ProductDetailStatusEnum.WAIT_COMMIT.getCode());
        return this.updateById(productDetailEntity);
    }

    @Override
    public Boolean sendKingDeeData(String id) {
        ProductDetailEntity productDetailEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productDetailEntity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95126);
        }
        syncKingdeeProductDetailService.syncDataToKingdee(productDetailEntity);
        return Boolean.TRUE;
    }

    @Override
    public void handleChargeId() {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        if (CollectionUtils.isEmpty(userList)) {
            return;
        }
        List<ProductDetailEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<ProductDetailEntity> resultList = new ArrayList<>();
        List<ProductDetailEntity> productDetailEntityList = list.stream().filter(obj -> StringUtils.isBlank(obj.getChargeId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(productDetailEntityList)) {
            return;
        }
        productDetailEntityList.forEach(obj -> {
            List<String> chargeNames = Arrays.stream(obj.getChargeName().split(",")).collect(Collectors.toList());
            List<String> chargIds = Arrays.stream(obj.getChargeId().split(",")).collect(Collectors.toList());
            Boolean flag = false;
            List<String> chargIdList = new ArrayList<>();
            for (String chargeName: chargeNames) {
                String chargId = userList.stream().filter(e -> e.getUserName().equals(chargeName)).map(FindUserDTO::getUserId).findFirst().orElse("");
                if (!chargIds.contains(chargId)) {
                    flag = true;
                }
                chargIdList.add(chargId);
            }
            if (flag) {
                if (CollectionUtils.isNotEmpty(chargIdList)) {
                    obj.setChargeId(String.join(",",chargIdList));
                }
                resultList.add(obj);
            }
        });
        if (CollectionUtils.isNotEmpty(resultList)) {
            this.updateBatchById(resultList);
        }

    }

    @Override
    public List<ProductDetailEntity> listByAuditPass() {
        List<ProductDetailEntity> list = lambdaQuery().eq(ProductDetailEntity::getStatus, ProductDetailStatusEnum.APPROVAL_PASS.getCode()).list();
        return list;
    }

    @Override
    public List<SkuVO> searchParentSku(String searchKeyword, String bomId) {
        return baseMapper.searchParentSku(searchKeyword, ProductDetailStatusEnum.APPROVAL_PASS.getCode(), bomId);
    }

    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus) {
        return this.lambdaUpdate()
                .eq(ProductDetailEntity::getId, id)
                .set(ProductDetailEntity::getSyncKingdeeStatus, syncKingdeeStatus)
                .set(ProductDetailEntity::getSyncKingdeeTime, LocalDateTime.now())
                .update();
    }


    /**
     * 根据skuid 集合获取到sku 信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-03-21 12:06
     */
    @Override
    public List<SkuVO> getSkuInfoBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        return baseMapper.getSkuInfoBySkuIds(skuIds);

    }


    private String checkRequiredData(ProductDetailEntity productDetailEntity) {
        StringBuffer str = new StringBuffer();
        if (StringUtils.isBlank(productDetailEntity.getSkuNo())) {
            str.append("sku编号不能为空");
        }
        if (StringUtils.isBlank(productDetailEntity.getChargeId())) {
            str.append("产品经理不能为空");
        }
        if (StringUtils.isBlank(productDetailEntity.getName())) {
            str.append("产品名称(品名)不能为空");
        }
        if (ObjectUtils.isEmpty(productDetailEntity.getProductState())) {
            str.append("产品开发状态不能为空");
        }
        return str.toString();
    }

    /**
     * 获取 审核通过 的sku 信息
     *
     * @param searchKeyword
     * @return
     */
    @Override
    public List<ChangeInfoDTO> getSku(String searchKeyword) {
        Integer state = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        return baseMapper.searchStateSku(state, searchKeyword);
    }


    /**
     * 在根据sku id 获取到产品信息
     * 在bom 和变更那边用到
     *
     * @param skuId
     * @return com.erp.model.plm.dto.ProductSmallestUnitDTO
     * @author yl
     * @date 2023-01-29 14:14
     */
    @Override
    public ProductSmallestUnitDTO getSkuBySkuId(String skuId) {
        ProductSmallestUnitDTO result = new ProductSmallestUnitDTO();
        ProductDetailEntity productDetail = this.getById(skuId);
        if (Objects.isNull(productDetail)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        String productId = productDetail.getProductId();
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByProductId(productId);
        //任务与配置字段 关系
        List<TaskRefSkuConfigEntity> refSkuFiledConfigList = taskRefSkuConfigService.getDisableFieldByProductId(productId);

        List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, skuId, refSkuFiledConfigList);
        //基础信息 禁用字段
        List<String> disableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_MANY_SKU_DETAIL_LIST, skuFiledConfigList);
        productDetail.setDisableFieldList(disableFields);

        result.setProductManySkuDetail(productDetail);

        //多规格产品基础信息
        ProductManySpecBaseDTO manySpecDetail = productDetailMapper.getManySpecDetailById(productId);
        if (manySpecDetail != null) {
            manySpecDetail.setDisableFieldList(disableFields);
            //获取多级分类
            List<String> categoryIdList = basicCategoryService.getPidList(manySpecDetail.getCategoryId());
            manySpecDetail.setCategoryIdList(categoryIdList);
        }

        result.setProductManySpecBaseDTO(manySpecDetail);

        //产品成本信息查询列表
        List<ProductCostShowDTO> costShowDTOList = productCostService.list(productId);
        ProductCostShowDTO costShowDTO = costShowDTOList.stream().
                filter(c -> c.getSkuId().equals(skuId)).findFirst().orElse(null);
        if (costShowDTO != null) {
            costShowDTO.setDisableFieldList(disableFields);
            result.setProductCostShowDTO(costShowDTO);
        }
        //产品采购信息查询列表
        List<ProductPurchaseShowDTO> purchaseShowDTOList = productPurchaseService.list(productId);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        ProductPurchaseShowDTO purchaseShowDTO = purchaseShowDTOList.stream().
                filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
        if (purchaseShowDTO != null) {
            purchaseShowDTO.setDisableFieldList(disableFields);
            FindUserDTO findUserDTO = userList.stream().filter(user -> user.getUserId().equals(purchaseShowDTO.getPurchaseUserId())).findFirst().orElse(null);
            if (findUserDTO != null) {
                purchaseShowDTO.setCreateUserName(findUserDTO.getUserName());
            }
            result.setProductPurchaseShowDTO(purchaseShowDTO);
        }
        //产品采购备注信息查询列表
        List<ProductPurchaseRemarkEntity> remarkEntityList = productPurchaseRemarkService.list(productId);
        result.setRemarkEntityList(remarkEntityList);

        //产品销售信息查询列表
        List<ProductSaleShowDTO> saleShowDTOList = productSaleService.list(productId);
        ProductSaleShowDTO saleShowDTO = saleShowDTOList.stream().
                filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
        if (saleShowDTO != null) {
            saleShowDTO.setDisableFieldList(disableFields);
            String saleCountry = saleShowDTO.getSaleCountry();
            if (StringUtils.isNotBlank(saleCountry)) {
                String[] split = saleCountry.split(",");
                List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
                List<String> nameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                saleShowDTO.setSaleCountryName(StringUtils.join(nameList, ","));
            }
            result.setProductSaleShowDTO(saleShowDTO);
        }
        //产品包装信息查询列表
        List<ProductPackShowDTO> packShowDTOList = productPackService.list(productId);
        ProductPackShowDTO packShowDTO = packShowDTOList.stream().
                filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
        if (packShowDTO != null) {
            packShowDTO.setDisableFieldList(disableFields);
            result.setProductPackShowDTO(packShowDTO);
        }

        //产品物流信息查询列表
        List<ProductLogisticsShowDTO> logisticsShowDTOList = productLogisticsService.list(productId);
        ProductLogisticsShowDTO logisticsShowDTO = logisticsShowDTOList.stream().
                filter(l -> l.getSkuId().equals(skuId)).findFirst().orElse(null);
        if (logisticsShowDTO != null) {
            logisticsShowDTO.setDisableFieldList(disableFields);
            result.setProductLogisticsShowDTO(logisticsShowDTO);
        }

        //产品证书信息查询列表
        List<ProductCertificateShowDTO> certificateShowDTOList = productCertificateService.list(productId);
        List<ProductCertificateShowDTO> certificateShowList = certificateShowDTOList.stream().
                filter(c -> c.getSkuId().equals(skuId)).collect(Collectors.toList());
        for (ProductCertificateShowDTO item : certificateShowList) {
            item.setDisableFieldList(disableFields);
        }
        result.setProductCertificateShowDTOList(certificateShowList);

        //产品认证信息
        List<ProductAttestationDTO> productAttestationList = productAttestationService.getByProductId(productId);
        ProductAttestationDTO productAttestationDTO = productAttestationList.stream().filter(attestation ->
                skuId.equals(attestation.getSkuId())).findFirst().orElse(null);
        if (productAttestationDTO != null) {
            productAttestationDTO.setSkuNo(productDetail.getSkuNo());
            productAttestationDTO.setSkuImagesUrl(productDetail.getImagesUrl());
        }
        result.setProductAttestationDTO(productAttestationDTO);

        //产品包装辅料
        List<ProductAccessoriesDTO> accessoriesList = productAccessoriesService.getByProductId(productId);
        for (ProductAccessoriesDTO item : accessoriesList) {
            item.setProductId(productId);
        }
        List<String> accessoriesSkuIds = accessoriesList.stream().filter(a -> StringUtils.isNotBlank(a.getAccessoriesSkuId())).
                map(ProductAccessoriesDTO::getAccessoriesSkuId).collect(Collectors.toList());

        List<ProductDetailEntity> detailList = this.getByIdList(accessoriesSkuIds);
        for (ProductAccessoriesDTO accessories : accessoriesList) {
            ProductDetailEntity detailEntity = detailList.stream().filter(d -> d.getId().equals(accessories.getAccessoriesSkuId())).
                    findFirst().orElse(null);
            if (detailEntity != null) {
                accessories.setAccessoriesSkuImagesUrl(detailEntity.getImagesUrl());
                accessories.setAccessoriesSkuName(detailEntity.getName());
                accessories.setAccessoriesSkuNo(detailEntity.getSkuNo());
            }
        }
        result.setProductAccessoriesList(accessoriesList);

        return result;
    }


    /**
     * 变更管理 审核通过后
     * 变更sku
     *
     * @param skuDTO
     * @return void
     * @author yl
     * @date 2023-01-30 17:12
     */
    @Override
    @Transactional
    public void changeSku(ProductSmallestUnitDTO skuDTO) {
        String id = skuDTO.getProductManySpecBaseDTO().getId();
        ProductManySpecBaseDTO baseDTO = skuDTO.getProductManySpecBaseDTO();
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        BeanMapper.copy(baseDTO, productInfoDTO);
        //SKU操作日志-产品信息
        ProductInfoEntity productInfoEntity = productInfoService.getById(id);
        if (ObjectUtils.isNotEmpty(baseDTO)) {
            //产品等级
            if (StringUtils.isNotBlank(productInfoDTO.getGradeId())) {
                //根据id查询字典表中的产品等级
                BasicDictEntity basicDict = basicDictService.getById(productInfoDTO.getGradeId());
                if (ObjectUtils.isNotEmpty(basicDict)) {
                    productInfoDTO.setGrade(basicDict.getValue());
                }
            }
            if (StringUtils.isNotBlank(baseDTO.getId())) {
                //产品操作日志
                addProductInfoLog(productInfoDTO, productInfoEntity, baseDTO.getId(), baseDTO.getId());
            }
            productInfoService.updateSpecByChangeSku(productInfoDTO);
        }

        ProductDetailEntity detailEntity = skuDTO.getProductManySkuDetail();
        ProductDetailEntity oldEntity = this.getById(detailEntity.getId());
        //sku操作日志
        ProductDetailDTO detail = new ProductDetailDTO();
        BeanMapper.copy(detailEntity, detail);
        addProductDetailLog(detail, oldEntity, detail.getId(), detailEntity.getProductId());
        //2.修改/新增 sku信息
        if (detailEntity != null) {
            detailEntity.setSyncKingdeeStatus(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode());
            this.updateById(detailEntity);
        }
        //3.修改/新增 成本信息
        ProductCostShowDTO costShowDTO = skuDTO.getProductCostShowDTO();
        if (costShowDTO != null) {
            ProductCostDTO productCostDTO = new ProductCostDTO();
            BeanMapper.copy(costShowDTO, productCostDTO);
            //SKU操作日志
            addProductCostLog(productCostDTO, id);
            productCostService.saveOrUpdate(productCostDTO);
        }

        //4.修改/新增 采购信息
        ProductPurchaseShowDTO purchaseShowDTO = skuDTO.getProductPurchaseShowDTO();
        if (purchaseShowDTO != null) {
            ProductPurchaseDTO productPurchaseDTO = new ProductPurchaseDTO();
            BeanMapper.copy(purchaseShowDTO, productPurchaseDTO);
            //SKU操作日志
            addProductPurchaseLog(productPurchaseDTO, id);
            productPurchaseService.saveOrUpdate(productPurchaseDTO);
        }
        //新增/修改采购备注信息
        List<ProductPurchaseRemarkEntity> remarkEntityList = skuDTO.getRemarkEntityList();
        if (CollectionUtils.isNotEmpty(remarkEntityList)) {
            List<ProductPurchaseRemarkDTO> productPurchaseRemarkList = BeanMapper.copyList(remarkEntityList, ProductPurchaseRemarkDTO.class);
            productPurchaseRemarkList.forEach(req -> {
                req.setProductId(id);
            });

            List<SysLogEntity> list = new LinkedList<>();
            remarkEntityList.forEach(req -> {
                list.add(new SysLogEntity().setContent("更新采购备注信息：" + req.getRemark()).setClassPath(SPUCLASSPATH).setBusinessId(id).setPid(id));
            });
            //SKU操作日志
            sysLogService.addSysLogByBatchSave(list);
            productPurchaseRemarkService.saveOrUpdateBatch(productPurchaseRemarkList);
        }


        //5.修改/新增 销售信息
        ProductSaleShowDTO productSaleShowDTO = skuDTO.getProductSaleShowDTO();
        if (productSaleShowDTO != null) {
            ProductSaleDTO productSaleDTO = new ProductSaleDTO();
            BeanMapper.copy(productSaleShowDTO, productSaleDTO);
            //SKU操作日志
            addProductSaleLog(productSaleDTO, id);
            productSaleService.saveOrUpdate(productSaleDTO);
        }

        //6.修改/新增 物流信息
        ProductLogisticsShowDTO productLogisticsShowDTO = skuDTO.getProductLogisticsShowDTO();
        if (productLogisticsShowDTO != null) {
            ProductLogisticsDTO productLogisticsDTO = new ProductLogisticsDTO();
            BeanMapper.copy(productLogisticsShowDTO, productLogisticsDTO);
            //SKU操作日志
            addProductLogisticsLog(productLogisticsDTO, id);
            productLogisticsService.saveOrUpdate(productLogisticsDTO);
        }

        //7.修改/新增 包装信息
        ProductPackShowDTO productPackShowDTO = skuDTO.getProductPackShowDTO();
        if (productPackShowDTO != null) {
            ProductPackDTO productPackDTO = new ProductPackDTO();
            BeanMapper.copy(productPackShowDTO, productPackDTO);
            //SKU操作日志
            addProductPackLog(productPackDTO, id);
            productPackService.saveOrUpdate(productPackDTO);
        }
        //8.修改/新增 证书信息
        List<ProductCertificateShowDTO> productCertificateShowList = skuDTO.getProductCertificateShowDTOList();
        if (CollectionUtils.isNotEmpty(productCertificateShowList)) {
            List<ProductCertificateDTO> productCertificateList = BeanMapper.copyList(productCertificateShowList, ProductCertificateDTO.class);
            //SKU操作日志
            addProductCertificateLog(productCertificateList, id);
            productCertificateService.saveOrUpdateBatch(productCertificateList);
        }

        //9.修改/新增 产品认证信息
        ProductAttestationDTO productAttestationDTO = skuDTO.getProductAttestationDTO();
        if (!Objects.isNull(productAttestationDTO)) {
            List<ProductAttestationDTO> list = new ArrayList<>(1);
            list.add(productAttestationDTO);
            //添加日志
            addProductAttestationLog(list, id);
            productAttestationService.saveOrUpdateBatchAttestation(list);
        }
        //10 修改/新增加 包装辅料信息

        List<ProductAccessoriesDTO> productAccessoriesList = skuDTO.getProductAccessoriesList();
        if (CollectionUtils.isNotEmpty(productAccessoriesList)) {
            //
            addProductAccessoriesLog(productAccessoriesList, id);
            productAccessoriesService.saveOrUpdateBatchAccessories(productAccessoriesList);
        }
        //编辑通过后发送金蝶
        syncKingdeeProductDetailService.syncDataToKingdee(detailEntity);
    }


    /**
     * 根据skuId 获取产品经理
     *
     * @param skuIdList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-02-01 17:25
     */
    @Override
    public List<String> getManagerBySkuIds(List<String> skuIdList) {
        List<String> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(ProductDetailEntity::getChargeId);
            queryWrapper.in(ProductDetailEntity::getId, skuIdList);
            List<String> chargeIds = this.listObjs(queryWrapper, Object::toString);
            //有逗号
            for (String chargeId : chargeIds) {
                if (chargeId.contains(",")) {
                    String chargeIdList[] = chargeId.split(",");
                    for (String item : chargeIdList) {
                        resultList.add(item);
                    }
                } else {
                    resultList.add(chargeId);
                }

            }
        }

        return resultList.stream().filter(s -> StringUtils.isNotBlank(s)).distinct().collect(Collectors.toList());
    }


    /**
     * 根据部门获取对应的人员
     *
     * @param secondDeptName
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-02-01 17:50
     */
    @Override
    public List<String> getApproveLead(String secondDeptName) {
        List<String> deptNames = Arrays.stream(secondDeptName.split(",")).distinct().collect(Collectors.toList());
        List<SysUserDeptDTO> list = sysUserFeign.getByDeptNames(deptNames);
        List<String> leadIds = list.stream().map(SysUserDeptDTO::getUid).distinct().collect(Collectors.toList());
        return leadIds;
    }


    /**
     * @param id
     * @param state
     * @param userId
     * @param userName
     * @return Boolean
     * @description: 更新产品信息状态
     * @author Will
     * @date: 2022/11/28 15:40
     */
    private Boolean updateProductDetailState(String id, Integer state, String userId, String userName) {
        LambdaUpdateWrapper<ProductDetailEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ProductDetailEntity::getStatus, state);
        updateWrapper.set(ProductDetailEntity::getUpdateUserId, userId);
        updateWrapper.set(ProductDetailEntity::getUpdateUserName, userName);
        updateWrapper.eq(ProductDetailEntity::getId, id);
        return this.update(updateWrapper);
    }


    /**
     * @param productDetailEntity
     * @description: 启动审核流程
     * @author Will
     * @date: 2022/12/1 18:33
     */
    private void productDetailStartProcess(ProductDetailEntity productDetailEntity) {
        //sku启动审核流程
        //ProductDetailApproverEntity approverEntity = productDetailApproverService.getProductDetailApprover();
        //验证是否设置审核人
        /*if (ObjectUtils.isEmpty(approverEntity)) {
            throw new ServiceException(ApiError.ERROR_95082);
        }*/
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
            //List<String> firstApproveIdList = Arrays.stream(approverEntity.getFirstApproveId().split(",")).collect(Collectors.toList());
            //审核人2
            //List<String> secondApproveIdList = Arrays.stream(approverEntity.getSecondApproveId().split(",")).collect(Collectors.toList());
            //审核人3
            //List<String> thirdApproveIdList = Arrays.stream(approverEntity.getThirdApproveId().split(",")).collect(Collectors.toList());
            //审核人1(产品经理)
            List<String> firstApproveIdList = new ArrayList<>();
            if (StringUtils.isBlank(productDetailEntity.getChargeId())) {
                throw new ServiceException(ApiError.ERROR_9030);
            }
            List<String> firstApproveIds = Arrays.stream(productDetailEntity.getChargeId().split(",")).distinct().collect(Collectors.toList());
            firstApproveIdList.addAll(firstApproveIds);
            //审核人2(产品经理上级)
            List<UserSuperiorDTO> superiorList = sysUserFeign.listSuperiorByUserIds(firstApproveIds);
            if (CollectionUtils.isEmpty(superiorList)) {
                throw new ServiceException(ApiError.ERROR_9039);
            }
            List<String> secondApproveIdList = superiorList.stream().filter(obj -> ChargeSuperiorEnum.DIRECT_SUPERIOR.getName().equals(obj.getSuperiorType())).map(UserSuperiorDTO::getUserId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(secondApproveIdList)) {
                throw new ServiceException(ApiError.ERROR_9034);
            }
            //审核人3(产品研发中心负责人、供应链中心负责人)
            String thirdDeptName = SkuApproveConfigureEnum.FOURTH_APPROVE.getDesc();
            List<String> thirdApproveIdList = setApproveLead(thirdDeptName);
            //审核人4(Cindy)
            //财务人员
            if (StringUtils.isBlank(financial)) {
                throw new ServiceException(ApiError.ERROR_9035);
            }
            Map<String, Object> parameterMap = new HashMap<>();

            parameterMap.put("firstApproveIdList", firstApproveIdList);
            parameterMap.put("secondApproveIdList", secondApproveIdList);
            parameterMap.put("thirdApproveIdList", thirdApproveIdList);
            parameterMap.put("fourthApproveIdList", Arrays.asList(financial));
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
    }

    private List<String> setApproveLead(String deptName) {
        List<String> deptNames = Arrays.stream(deptName.split(",")).distinct().collect(Collectors.toList());
        List<SysUserDeptDTO> list = sysUserFeign.getByDeptNames(deptNames);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiResult.error(1, deptName.concat("，未找到直属上级")));
        }
        List<String> leadIds = list.stream().map(SysUserDeptDTO::getUid).distinct().collect(Collectors.toList());
        return leadIds;
    }


    /**
     * 产品信息修改日志
     */
    private void addProductInfoLog(ProductInfoDTO productInfoDTO, ProductInfoEntity oldEntity, String businessId, String pid) {
        ProductInfoDTO oldDto = new ProductInfoDTO();
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            BeanMapperUtils.copy(oldEntity, oldDto);
        }
        sysLogService.addSysLogByUpdate(oldDto, productInfoDTO, SPUCLASSPATH, businessId, pid, String.format("SPU[%s]", oldEntity.getName()));
    }

    /**
     * sku单属性修改日志
     */
    private void addProductSkuBaseInfoLog(ProductSkuBaseInfoDTO productSkuBaseInfoDTO, ProductDetailEntity oldEntity, String businessId, String pid) {
        ProductSkuBaseInfoDTO oldDto = new ProductSkuBaseInfoDTO();
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            BeanMapperUtils.copy(oldEntity, oldDto);
        }
        sysLogService.addSysLogByUpdate(oldDto, productSkuBaseInfoDTO, SKUCLASSPATH, businessId, pid, String.format("SKU[%s]", oldEntity.getSkuNo()));
    }

    /**
     * sku多属性修改日志
     */
    private void addProductDetailLog(ProductDetailDTO productDetailDTO, ProductDetailEntity oldEntity, String businessId, String pid) {
        ProductDetailDTO oldDto = new ProductDetailDTO();
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            BeanMapperUtils.copy(oldEntity, oldDto);
        }
        sysLogService.addSysLogByUpdate(oldDto, productDetailDTO, SKUCLASSPATH, businessId, pid, String.format("SKU[%s]", oldEntity.getSkuNo()));
    }

    /**
     * 成本信息新增或修改日志
     */
    private void addProductCostLog(ProductCostDTO productCostDTO, String pid) {
        if (StringUtils.isNotBlank(productCostDTO.getId())) {
            ProductCostEntity oldEntity = productCostService.getById(productCostDTO.getId());
            ProductCostDTO oldDto = new ProductCostDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
            }
            ProductDetailEntity productDetailEntity = this.getById(productCostDTO.getSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            sysLogService.addSysLogByUpdate(oldDto, productCostDTO, SKUCLASSPATH, productCostDTO.getSkuId(), pid, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
        }
    }

    /**
     * 采购信息新增或修改日志
     */
    private void addProductPurchaseLog(ProductPurchaseDTO productPurchaseDTO, String pid) {
        if (StringUtils.isNotBlank(productPurchaseDTO.getId())) {
            ProductPurchaseEntity oldEntity = productPurchaseService.getById(productPurchaseDTO.getId());
            ProductPurchaseDTO oldDto = new ProductPurchaseDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
            }
            ProductDetailEntity productDetailEntity = this.getById(productPurchaseDTO.getSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            sysLogService.addSysLogByUpdate(oldDto, productPurchaseDTO, SKUCLASSPATH, productPurchaseDTO.getSkuId(), pid, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
        }
    }

    /**
     * 销售信息新增或修改日志
     */
    private void addProductSaleLog(ProductSaleDTO productSaleDTO, String pid) {
        if (StringUtils.isNotBlank(productSaleDTO.getId())) {
            ProductSaleEntity oldEntity = productSaleService.getById(productSaleDTO.getId());
            ProductSaleDTO oldDto = new ProductSaleDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
            }
            ProductDetailEntity productDetailEntity = this.getById(productSaleDTO.getSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            sysLogService.addSysLogByUpdate(oldDto, productSaleDTO, SKUCLASSPATH, productSaleDTO.getSkuId(), pid, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
        }
    }

    /**
     * 物流信息新增或修改日志
     */
    private void addProductLogisticsLog(ProductLogisticsDTO productLogisticsDTO, String pid) {
        if (StringUtils.isNotBlank(productLogisticsDTO.getId())) {
            ProductLogisticsEntity oldEntity = productLogisticsService.getById(productLogisticsDTO.getId());
            ProductLogisticsDTO oldDto = new ProductLogisticsDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
            }
            ProductDetailEntity productDetailEntity = this.getById(productLogisticsDTO.getSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            sysLogService.addSysLogByUpdate(oldDto, productLogisticsDTO, SKUCLASSPATH, productLogisticsDTO.getSkuId(), pid, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
        }
    }

    /**
     * 包装信息新增或修改日志
     */
    private void addProductPackLog(ProductPackDTO productPackDTO, String pid) {
        if (StringUtils.isNotBlank(productPackDTO.getId())) {
            ProductPackEntity oldEntity = productPackService.getById(productPackDTO.getId());
            ProductPackDTO oldDto = new ProductPackDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
            }
            ProductDetailEntity productDetailEntity = this.getById(productPackDTO.getSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            sysLogService.addSysLogByUpdate(oldDto, productPackDTO, SKUCLASSPATH, productPackDTO.getSkuId(), pid, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
        }
    }

    /**
     * 证书信息新增或修改日志
     */
    private void addProductCertificateLog(List<ProductCertificateDTO> productCertificateList, String pid) {
        List<String> ids = productCertificateList.stream().filter(obj -> StringUtils.isNotBlank(obj.getId())).map(ProductCertificateDTO::getId).collect(Collectors.toList());
        List<ProductCertificateEntity> certificateEntityList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            certificateEntityList = productCertificateService.listByIds(ids);
        }
        List<ProductCertificateEntity> finalCertificateEntityList = certificateEntityList;
        productCertificateList.forEach(obj -> {
            //SKU操作日志
            ProductCertificateEntity oldEntity = finalCertificateEntityList.stream().filter(e -> e.getId().equals(obj.getId())).findFirst().orElse(null);
            ProductCertificateDTO oldDto = new ProductCertificateDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
            }
            ProductDetailEntity productDetailEntity = this.getById(obj.getSkuId());
            if (ObjectUtils.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            sysLogService.addSysLogByUpdate(oldDto, obj, SKUCLASSPATH, obj.getSkuId(), pid, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
        });
    }


}
