package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.ApproveType;
import com.common.business.constant.IsConstant;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.ExcelImportFsDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.RedisService;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.CommonConstants;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.plm.enums.ProductTypeEnum;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.plm.vo.SkuSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.sys.openapi.UploadSkuDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.CfgSettingFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.BomOperateContent;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.constant.ProductManyDetailConstant;
import com.erp.server.plm.listener.ProductDetailExcelListener;
import com.erp.server.plm.listener.ProductDetailUpdateExcelListener;
import com.erp.server.plm.listener.ProductDetailUpdateNotApproveExcelListener;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeProductDetailService;
import com.erp.server.plm.rocketmq.sync.lingxing.SyncLingXingProductDetailService;
import com.erp.server.plm.rocketmq.sync.wangdian.SyncWangDianProductDetailService;
import com.erp.server.plm.service.*;
import com.erp.server.plm.utils.UnitConverterUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.zxing.WriterException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.python.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.ListUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.*;
import static com.alibaba.excel.EasyExcelFactory.read;
import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_SKU;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_PLM_SKU_IMAGES;

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
    private DmpTaskFeign dmpTaskFeign;
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
    private ProductDetailService productDetailService;

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
    private CfgSettingFeign cfgSettingFeign;

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

    @Autowired
    private ProductUnitService productUnitService;

    @Autowired
    private ProductVariantPropertyService productVariantPropertyService;

    @Autowired
    private ProductCustomsService productCustomsService;

    @Autowired
    private DmpMqFeign dmpMqFeign;

    @Autowired
    private SupplierFeign supplierFeign;

    @Autowired
    private BomSkuService bomSkuService;

    @Autowired
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private ProductRefLabelService productRefLabelService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    @Lazy
    private NoticeMessageService noticeMessageService;

    @Resource
    private SyncWangDianProductDetailService syncWangDianProductDetailService;

    @Resource
    private SyncLingXingProductDetailService syncLingXingProductDetailService;

    @Resource
    private InventoryFeign inventoryFeign;
    @Resource
    private LogisticsFeign logisticsFeign;


    @Resource
    private PlmAttachmentService plmAttachmentService;

    @Resource
    private ApplicationCategoryService applicationCategoryService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    //变更财务人员审核
    @Value("${changeFinancialAudit}")
    private String financial;


    private static final String SPUCLASSPATH = String.valueOf(ProductInfoEntity.class);
    private static final String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");
    public static final String SKU_COST_SALE_ORG_ID = "skuCostSaleOrgId";
    public static final String SKU_COST_WAREHOUSE = "skuCostWarehouse";

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
            //待审核，审核中
            pagingDTO.getParams().setStatusList(Arrays.asList(ProductDetailStatusEnum.APPROVAL_ING.getCode()));
        }
        if (MathUtil.TWO.toString().equals(pagingDTO.getParams().getType())) {
            //已审核
            pagingDTO.getParams().setStatusList(Arrays.asList(ProductDetailStatusEnum.APPROVAL_PASS.getCode()));
        }
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page<ProductSkuDTO> query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        //标签列表
        List<String> labelIds = pagingDTO.getParams().getLabelIds();
        List<String> labelProductIds = null;
        if (CollectionUtils.isNotEmpty(labelIds)) {
            List<ProductRefLabelVO> productRefLabelVOS = productRefLabelService.getLabelListByIds(null, new HashSet<>(labelIds), null);
            if (CollectionUtils.isNotEmpty(productRefLabelVOS)) {
                labelProductIds = productRefLabelVOS.stream().map(ProductRefLabelVO::getProductId).collect(Collectors.toList());
            } else {
                labelProductIds = new ArrayList<>();
                labelProductIds.add("-1");
            }
            pagingDTO.getParams().setLabelProductIds(labelProductIds);
        }
        List<String> saleMethodList = pagingDTO.getParams().getSaleMethodList();
        List<String> saleMethodParams = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(saleMethodList)) {
            for (String saleMethod : saleMethodList) {
                saleMethodParams.add(SaleMethodEnum.getNameByCode(Integer.valueOf(saleMethod)));
            }
            pagingDTO.getParams().setSaleMethod(StringUtils.join(saleMethodParams, ","));
        }
        IPage<ProductDetailShowDTO> pageData = productDetailMapper.paging(query, pagingDTO.getParams());
        List<ProductDetailShowDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> productIdList = list.stream().map(ProductDetailShowDTO::getId).collect(Collectors.toList());
        List<ProjectInfoEntity> projectList = projectInfoService.getByProductIdList(productIdList);
        List<String> sourceIds = list.stream().map(ProductDetailShowDTO::getId).collect(Collectors.toList());
        List<String> changeIngSourceIds = productChangeService.getBySourceId(sourceIds);
        List<ApplicationCategoryEntity> applicationCategoryList = applicationCategoryService.list();
        List<String> mainSupplierIds = list.stream().map(ProductDetailShowDTO::getMainSupplier).distinct().collect(Collectors.toList());
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(mainSupplierIds);

        List<String> skuIdList = list.stream().map(ProductDetailShowDTO::getSkuId).collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomSkuService.listBomChildBySkuIds(skuIdList);
        for (ProductDetailShowDTO item : list) {
            if(StringUtils.isNotBlank(item.getApplicationCategoryId())){
                List<String> applicationCategoryIdList = Arrays.stream(item.getApplicationCategoryId().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                List<String> applicationCategory = applicationCategoryList.stream()
                        .filter(ac -> applicationCategoryIdList.contains(ac.getId()))
                        .map(ApplicationCategoryEntity::getName)
                        .collect(Collectors.toList());
                if (ObjectUtils.isNotEmpty(applicationCategory)) {
                    String applicationCategoryName = String.join(",", applicationCategory);
                    item.setApplicationCategoryName(applicationCategoryName);
                }
            }
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(item.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                item.setIsCombination(Boolean.TRUE);
            } else {
                item.setIsCombination(Boolean.FALSE);
            }
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

            // 一级供应商名称
            if (StrUtils.isNotEmpty(item.getMainSupplier()) && supplierMap.containsKey(item.getMainSupplier())) {
                item.setMainSupplierName(supplierMap.get(item.getMainSupplier()).getName());
            }
        }

        return new PagingVO<>(pageData);
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
        ApplicationCategoryEntity applicationCategory = applicationCategoryService.getById(noSpecDetailById.getApplicationCategoryId());
        if (ObjectUtils.isNotEmpty(applicationCategory)) {
            noSpecDetailById.setApplicationCategoryName(applicationCategory.getName());
        }
        productNoSpecDetailAllDTO.setProductNoDetailDTO(noSpecDetailById);
        //产品成本信息查询列表
        List<ProductCostShowDTO> costShowDTOList = productCostService.list(productId);
        productNoSpecDetailAllDTO.setProductCostShowDTOList(costShowDTOList);
        //产品采购信息查询列表
        List<ProductPurchaseShowDTO> purchaseShowDTOList = productPurchaseService.list(productId);
        if (CollUtil.isNotEmpty(purchaseShowDTOList)) {
            List<String> supplierIds = Lists.newArrayList();
            purchaseShowDTOList.stream().forEach(r -> {
                if (StrUtils.isNotEmpty(r.getMainSupplier())) {
                    supplierIds.add(r.getMainSupplier());
                }
                if (StrUtils.isNotEmpty(r.getSecondSupplier())) {
                    supplierIds.add(r.getSecondSupplier());
                }
                noSpecDetailById.setEan(r.getEan());
            });
            if (CollUtil.isNotEmpty(supplierIds)) {
                Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);
                purchaseShowDTOList.stream().forEach(r -> {
                    if (StrUtils.isNotEmpty(r.getMainSupplier()) && supplierMap.containsKey(r.getMainSupplier())) {
                        r.setMainSupplierName(supplierMap.get(r.getMainSupplier()).getName());
                    }
                    if (StrUtils.isNotEmpty(r.getSecondSupplier()) && supplierMap.containsKey(r.getSecondSupplier())) {
                        r.setSecondSupplierName(supplierMap.get(r.getSecondSupplier()).getName());
                    }
                });
            }
        }
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
        logisticsShowDTOList.forEach(req -> {
            if (StringUtils.isNotBlank(req.getProductPropertyId())) {
                String[] split = req.getProductPropertyId().split(",");
                List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
                List<String> countryNameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                req.setProductProperty(StringUtils.join(countryNameList, ","));
                //增加是否存在电池属性
                BasicDictEntity isElectric = basicDictEntities.stream().filter(e -> e.getRemark().equals("isElectric")).findFirst().orElse(null);
                if (Objects.nonNull(isElectric)) {
                    req.setElectric(true);
                    req.setInputParams(String.format("电压:%s%s/电流:%s%s/功率:%s%s/电池容量:%s%s",
                            req.getInputVoltage().stripTrailingZeros().toPlainString(), req.getVoltageUnit(),
                            req.getInputElectric().stripTrailingZeros().toPlainString(), req.getElectricUnit(),
                            req.getInputPower().stripTrailingZeros().toPlainString(), req.getPowerUnit(),
                            req.getInputBatteryCapacity().stripTrailingZeros().toPlainString(), req.getBatteryCapacityUnit()));
                    req.setOutputParams(String.format("电压:%s%s/电流:%s%s/功率:%s%s/电池容量:%s%s",
                            req.getOutputVoltage().stripTrailingZeros().toPlainString(), req.getVoltageUnit(),
                            req.getOutputElectric().stripTrailingZeros().toPlainString(), req.getElectricUnit(),
                            req.getOutputPower().stripTrailingZeros().toPlainString(), req.getPowerUnit(),
                            req.getOutputBatteryCapacity().stripTrailingZeros().toPlainString(), req.getBatteryCapacityUnit()));
                }
            }
        });

        productNoSpecDetailAllDTO.setProductLogisticsShowDTOList(logisticsShowDTOList);
        //产品证书信息查询列表
        List<ProductCertificateShowDTO> certificateShowDTOList = productCertificateService.list(productId);
        productNoSpecDetailAllDTO.setProductCertificateShowDTOList(certificateShowDTOList);

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

        //国家列表
        List<DictCountryDTO.ListDTO>  countryList = sysUserFeign.countryList();
        //查询目的国海关编码
        List<ProductCustomsEntity> productCustomsEntityList = productCustomsService.listByProductId(productId);
        productCustomsEntityList.forEach(req -> {
            if (StringUtils.isNotBlank(req.getCountry())) {
                String[] split = req.getCountry().split(",");
                List<String> countryIdList = Arrays.asList(split);
                List<DictCountryDTO.ListDTO> dictCountryList = countryList.stream().filter(c->countryIdList.contains(c.getId())).collect(Collectors.toList());
                String countryName = dictCountryList.stream().map(DictCountryDTO.ListDTO::getNameCn).collect(Collectors.joining(","));
                req.setCountryName(countryName);
            }
            if(StringUtils.isEmpty(req.getToCurrency())){
                req.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
                req.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
            }
        });
        productNoSpecDetailAllDTO.setProductCustomsList(productCustomsEntityList);
        return productNoSpecDetailAllDTO;
    }

    @Override
    public ProductNoSpecDetailAllDTO getNoSpecDetailBySkuId(String skuId) {

        ProductDetailEntity productDetailEntity = this.getById(skuId);
        String productId = productDetailEntity.getProductId();
        ProductInfoEntity infoEntity = productInfoService.getById(productId);
        if (ObjectUtil.isNotEmpty(infoEntity) && infoEntity.getSpecType() == 2) {
          return getNoSpecDetailById(productId);
        }

        ProductNoSpecDetailAllDTO productNoSpecDetailAllDTO = new ProductNoSpecDetailAllDTO();
        //无规格产品信息明细
        ProductNoDetailDTO noSpecDetailById = productDetailMapper.getNoSpecDetailBySkuId(skuId);

        if (ObjectUtils.isNotEmpty(noSpecDetailById)) {
            //获取多级分类
            List<String> categoryIdList = basicCategoryService.getPidList(noSpecDetailById.getCategoryId());
            noSpecDetailById.setCategoryIdList(categoryIdList);

        }
        if(StringUtils.isNotBlank(noSpecDetailById.getApplicationCategoryId())){
            List<String> applicationCategoryIdList = Arrays.stream(noSpecDetailById.getApplicationCategoryId().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            noSpecDetailById.setApplicationCategoryIdList(applicationCategoryIdList);
            List<ApplicationCategoryEntity> applicationCategory = applicationCategoryService.listByIds(applicationCategoryIdList);
            if (ObjectUtils.isNotEmpty(applicationCategory)) {
                List<String> applicationCategoryNameList = applicationCategory.stream()
                        .map(ApplicationCategoryEntity::getName)
                        .collect(Collectors.toList());
                noSpecDetailById.setApplicationCategoryNameList(applicationCategoryNameList);
            }
        }
        productNoSpecDetailAllDTO.setProductNoDetailDTO(noSpecDetailById);
        //产品成本信息查询列表
        List<ProductCostShowDTO> costShowDTOList = productCostService.listBySkuId(skuId);
        productNoSpecDetailAllDTO.setProductCostShowDTOList(costShowDTOList);
        //产品采购信息查询列表
        List<ProductPurchaseShowDTO> purchaseShowDTOList = productPurchaseService.listBySkuId(skuId);
        if (CollUtil.isNotEmpty(purchaseShowDTOList)) {
            List<String> supplierIds = Lists.newArrayList();
            purchaseShowDTOList.stream().forEach(r -> {
                if (StrUtils.isNotEmpty(r.getMainSupplier())) {
                    supplierIds.add(r.getMainSupplier());
                }
                if (StrUtils.isNotEmpty(r.getSecondSupplier())) {
                    supplierIds.add(r.getSecondSupplier());
                }
            });
            if (CollUtil.isNotEmpty(supplierIds)) {
                Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);
                purchaseShowDTOList.stream().forEach(r -> {
                    if (StrUtils.isNotEmpty(r.getMainSupplier()) && supplierMap.containsKey(r.getMainSupplier())) {
                        r.setMainSupplierName(supplierMap.get(r.getMainSupplier()).getName());
                    }
                    if (StrUtils.isNotEmpty(r.getSecondSupplier()) && supplierMap.containsKey(r.getSecondSupplier())) {
                        r.setSecondSupplierName(supplierMap.get(r.getSecondSupplier()).getName());
                    }
                });
            }
        }
        ProductDetailEntity entity = this.getById(skuId);
        productNoSpecDetailAllDTO.setProductPurchaseShowDTOList(purchaseShowDTOList);
        //产品采购备注信息查询列表
        List<ProductPurchaseRemarkEntity> remarkEntityList = productPurchaseRemarkService.list(entity.getProductId());
        productNoSpecDetailAllDTO.setRemarkEntityList(remarkEntityList);
        //产品销售信息查询列表
        List<ProductSaleShowDTO> saleShowDTOList = productSaleService.listBySkuId(skuId);
        productNoSpecDetailAllDTO.setProductSaleShowDTOList(saleShowDTOList);
        //产品包装信息查询列表
        List<ProductPackShowDTO> packShowDTOList = productPackService.listBySkuId(skuId);
        productNoSpecDetailAllDTO.setProductPackShowDTOS(packShowDTOList);
        //产品物流信息查询列表
        List<ProductLogisticsShowDTO> logisticsShowDTOList = productLogisticsService.listBySkuId(skuId);
        logisticsShowDTOList.forEach(req -> {
            if (StringUtils.isNotBlank(req.getProductPropertyId())) {
                String[] split = req.getProductPropertyId().split(",");
                List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
                List<String> countryNameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                req.setProductProperty(StringUtils.join(countryNameList, ","));
                //增加是否存在电池属性
                BasicDictEntity isElectric = basicDictEntities.stream().filter(e -> e.getRemark().equals("isElectric")).findFirst().orElse(null);
                if (Objects.nonNull(isElectric)) {
                    req.setElectric(true);
                    req.setInputParams(String.format("电压:%s%s/电流:%s%s/功率:%s%s/电池容量:%s%s",
                            req.getInputVoltage().stripTrailingZeros().toPlainString(), req.getVoltageUnit(),
                            req.getInputElectric().stripTrailingZeros().toPlainString(), req.getElectricUnit(),
                            req.getInputPower().stripTrailingZeros().toPlainString(), req.getPowerUnit(),
                            req.getInputBatteryCapacity().stripTrailingZeros().toPlainString(), req.getBatteryCapacityUnit()));
                    req.setOutputParams(String.format("电压:%s%s/电流:%s%s/功率:%s%s/电池容量:%s%s",
                            req.getOutputVoltage().stripTrailingZeros().toPlainString(), req.getVoltageUnit(),
                            req.getOutputElectric().stripTrailingZeros().toPlainString(), req.getElectricUnit(),
                            req.getOutputPower().stripTrailingZeros().toPlainString(), req.getPowerUnit(),
                            req.getOutputBatteryCapacity().stripTrailingZeros().toPlainString(), req.getBatteryCapacityUnit()));
                }
            }
        });

        productNoSpecDetailAllDTO.setProductLogisticsShowDTOList(logisticsShowDTOList);
        //产品证书信息查询列表
        List<ProductCertificateShowDTO> certificateShowDTOList = productCertificateService.listBySkuId(skuId);
        productNoSpecDetailAllDTO.setProductCertificateShowDTOList(certificateShowDTOList);


        //产品辅料信息
        List<ProductAccessoriesDTO> productAccessoriesList = productAccessoriesService.getBySkuId(skuId);
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
        //国家列表
        List<DictCountryDTO.ListDTO>  countryList = sysUserFeign.countryList();
        //查询目的国海关编码
        List<ProductCustomsEntity> productCustomsEntityList = productCustomsService.listBySkuId(skuId);
        productCustomsEntityList.forEach(req -> {
            if (StringUtils.isNotBlank(req.getCountry())) {
                String[] split = req.getCountry().split(",");
                List<String> countryIdList = Arrays.asList(split);
                List<DictCountryDTO.ListDTO> dictCountryList = countryList.stream().filter(c->countryIdList.contains(c.getId())).collect(Collectors.toList());
                String countryName = dictCountryList.stream().map(DictCountryDTO.ListDTO::getNameCn).collect(Collectors.joining(","));
                req.setCountryName(countryName);
            }
            if(StringUtils.isEmpty(req.getToCurrency())){
                req.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
                req.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
            }
        });
        productNoSpecDetailAllDTO.setProductCustomsList(productCustomsEntityList);
        return productNoSpecDetailAllDTO;
    }

    @Override
    @Cacheable(cacheNames = RedisKeyConstant.CACHE_SKU_NO_INVENTORY,keyGenerator = "myKeyGenerator")
    public List<SkuVO> getNoInventorySku() {
        return this.baseMapper.getNoInventorySku();
    }

    @Override
    public void updateName(String id, String name) {
        lambdaUpdate().eq(ProductDetailEntity::getId, id)
                .set(ProductDetailEntity::getName, name)
                .update(new ProductDetailEntity());
    }

    @Override
    public List<ProductDetailEntity> listBySkuNos(List<String> skuNos) {
        if (CollectionUtils.isEmpty(skuNos)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(ProductDetailEntity::getSkuNo, skuNos).list();
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

        //国家列表
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();

        //多规格产品基础信息
        ProductManySpecBaseDTO manySpecDetailById = productDetailMapper.getManySpecDetailById(productId);
        if (!Objects.isNull(manySpecDetailById)) {
            //基础信息 禁用字段
            List<String> manySpecBaseDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_MANY_SPEC_BASE, refSkuFiledConfigList);
            manySpecDetailById.setDisableFieldList(manySpecBaseDisableFields);
            //获取多级分类
            List<String> categoryIdList = basicCategoryService.getPidList(manySpecDetailById.getCategoryId());
            manySpecDetailById.setCategoryIdList(categoryIdList);
            if(StringUtils.isNotBlank(manySpecDetailById.getApplicationCategoryId())){
                List<String> applicationCategoryIdList = Arrays.stream(manySpecDetailById.getApplicationCategoryId().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                manySpecDetailById.setApplicationCategoryIdList(applicationCategoryIdList);
                List<ApplicationCategoryEntity> applicationCategory = applicationCategoryService.listByIds(applicationCategoryIdList);
                if (ObjectUtils.isNotEmpty(applicationCategory)) {
                    List<String> applicationCategoryNameList = applicationCategory.stream()
                            .map(ApplicationCategoryEntity::getName)
                            .collect(Collectors.toList());
                    manySpecDetailById.setApplicationCategoryNameList(applicationCategoryNameList);
                }
            }
            productManyDetail.setProductManySpecBaseDTO(manySpecDetailById);
        }
        //多规格产品明细信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
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

        List<String> supplierIds = Lists.newArrayList();
        List<String> mainSupplierIds = purchaseShowDTOList.stream().map(ProductPurchaseShowDTO::getMainSupplier).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(mainSupplierIds)) {
            supplierIds.addAll(mainSupplierIds);
        }
        List<String> secondSupplierIds = purchaseShowDTOList.stream().map(ProductPurchaseShowDTO::getSecondSupplier).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(secondSupplierIds)) {
            supplierIds.addAll(secondSupplierIds);
        }
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);

        purchaseShowDTOList.forEach(req -> {
            ProductDetailEntity entity = list.stream().filter(v -> v.getId().equals(req.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            entity.setEan(req.getEan());
            FindUserDTO findUserDTO = userList.stream().filter(user -> user.getUserId().equals(req.getPurchaseUserId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                req.setCreateUserName(findUserDTO.getUserName());
            }

            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, req.getSkuId(), refSkuFiledConfigList);
            //采购信息 禁用字段
            List<String> purchaseDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_PURCHASE_SHOW_LIST, skuFiledConfigList);
            req.setDisableFieldList(purchaseDisableFields);

            if (StrUtils.isNotEmpty(req.getMainSupplier()) && supplierMap.containsKey(req.getMainSupplier())) {
                req.setMainSupplierName(supplierMap.get(req.getMainSupplier()).getName());
            }
            if (StrUtils.isNotEmpty(req.getSecondSupplier()) && supplierMap.containsKey(req.getSecondSupplier())) {
                req.setSecondSupplierName(supplierMap.get(req.getSecondSupplier()).getName());
            }

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
                List<String> countryIdList = Arrays.asList(split);
                List<DictCountryDTO.ListDTO> dictCountryList = countryList.stream().filter(c -> countryIdList.contains(c.getId())).collect(Collectors.toList());
                String countryName = dictCountryList.stream().map(DictCountryDTO.ListDTO::getNameCn).collect(Collectors.joining(","));
                req.setSaleCountryName(countryName);
            }

            List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, req.getSkuId(), refSkuFiledConfigList);
            //销售信息 禁用字段
            List<String> saleDisableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_SALE_SHOW_LIST, skuFiledConfigList);
            req.setDisableFieldList(saleDisableFields);

            if (req.getIsFinishedImg() == null) {
                req.setIsFinishedImg(MathUtil.TWO);
            }
            if (req.getIsFinishedVideo() == null) {
                req.setIsFinishedVideo(MathUtil.TWO);
            }
            if (req.getIsMarketable() == null) {
                req.setIsMarketable(MathUtil.ZERO);
            }
            if (req.getSaleState() == null) {
                req.setSaleState(MathUtil.ONE);
            }
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
            if (StringUtils.isNotBlank(req.getProductPropertyId())) {
                String[] split = req.getProductPropertyId().split(",");
                List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
                List<String> countryNameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                req.setProductProperty(StringUtils.join(countryNameList, ","));
                //增加是否存在电池属性
                BasicDictEntity isElectric = basicDictEntities.stream().filter(e -> e.getRemark().equals("isElectric")).findFirst().orElse(null);
                if (Objects.nonNull(isElectric)) {
                    req.setElectric(true);
                    req.setInputParams(String.format("电压:%s%s/电流:%s%s/功率:%s%s/电池容量:%s%s",
                            req.getInputVoltage().stripTrailingZeros().toPlainString(), req.getVoltageUnit(),
                            req.getInputElectric().stripTrailingZeros().toPlainString(), req.getElectricUnit(),
                            req.getInputPower().stripTrailingZeros().toPlainString(), req.getPowerUnit(),
                            req.getInputBatteryCapacity().stripTrailingZeros().toPlainString(), req.getBatteryCapacityUnit()));
                    req.setOutputParams(String.format("电压:%s%s/电流:%s%s/功率:%s%s/电池容量:%s%s",
                            req.getOutputVoltage().stripTrailingZeros().toPlainString(), req.getVoltageUnit(),
                            req.getOutputElectric().stripTrailingZeros().toPlainString(), req.getElectricUnit(),
                            req.getOutputPower().stripTrailingZeros().toPlainString(), req.getPowerUnit(),
                            req.getOutputBatteryCapacity().stripTrailingZeros().toPlainString(), req.getBatteryCapacityUnit()));
                }
            }
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

        //查询目的国海关编码
        List<ProductCustomsEntity> productCustomsEntityList = productCustomsService.listByProductId(productId);

        productCustomsEntityList.forEach(req -> {
            if (StringUtils.isNotBlank(req.getCountry())) {
                String[] split = req.getCountry().split(",");
                List<String> countryIdList = Arrays.asList(split);
                List<DictCountryDTO.ListDTO> dictCountryList = countryList.stream().filter(c -> countryIdList.contains(c.getId())).collect(Collectors.toList());
                String countryName = dictCountryList.stream().map(DictCountryDTO.ListDTO::getNameCn).collect(Collectors.joining(","));
                req.setCountryName(countryName);
            }
            if(StringUtils.isEmpty(req.getToCurrency())){
                req.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
                req.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
            }
        });

        productManyDetail.setProductCustomsList(productCustomsEntityList);
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
                    Map<String, Object> fieldMap = parseObject(fieldJson);
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
        //校验sku必填项
        ProductDetailDTO detailDTO = new ProductDetailDTO();
        BeanMapper.copy(productSkuBaseInfoDTO, detailDTO);
        ProductDetailEntity detailEntity = new ProductDetailEntity();
        BeanMapper.copy(productSkuBaseInfoDTO, detailEntity);
        detailEntity.setIsChange(IsConstant.NO);
        productUnitService.setupOccupy(Arrays.asList(detailEntity.getUnitId()));
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
        list.forEach(req -> {
            req.setIsChange(IsConstant.NO);
        });
        List<String> unitList = list.stream().map(ProductDetailEntity::getUnitId).collect(Collectors.toList());
        productUnitService.setupOccupy(unitList);
        return this.saveOrUpdateBatch(list);
    }


    /**
     * 比较两个对象中指定字段的值是否相同
     *
     * @param obj1 第一个对象，用于比较
     * @param obj2 第二个对象，用于比较
     * @return 返回一个包含不同字段名称的列表如果字段值相同，则列表为空
     * @throws IllegalArgumentException 如果任何一个对象为null，则抛出此异常
     */
    private List<ProductDetailDTO.SkuChangeInfoDTO> compareFields(Object obj1, Object obj2,Set<String> fieldsToCompare) {
        List<ProductDetailDTO.SkuChangeInfoDTO> differentFields = new ArrayList<>();

        if (obj1 == null || obj2 == null ) {
            throw new IllegalArgumentException("Objects cannot be null");
        }

        Class<?> class1 = obj1.getClass();
        Class<?> class2 = obj2.getClass();

        // 获取类1的所有字段
        Field[] fields1 = class1.getDeclaredFields();
        for (Field field1 : fields1) {
            String fieldName = field1.getName();
            if (fieldsToCompare.contains(fieldName)) {
                try {
                    // 查找类2是否有相同字段
                    Field field2 = class2.getDeclaredField(fieldName);

                    // 确保两个字段类型相同
                    if (field1.getType().equals(field2.getType())) {
                        field1.setAccessible(true);
                        field2.setAccessible(true);
                        Object value1 = field1.get(obj1);
                        Object value2 = field2.get(obj2);

                        // 判断字段是否为数字类型
                        boolean isNumberType = Number.class.isAssignableFrom(field1.getType());
                        // 如果值不同，则记录差异
                        if(null != value1 && null != value2 && "" != value1 && "" != value2 ){
                            if (isNumberType) {
                                // 如果是数字类型，直接比较值是否相等
                                BigDecimal bigDecimal1 = new BigDecimal(value1.toString()).setScale(2);
                                BigDecimal bigDecimal2 = new BigDecimal(value2.toString()).setScale(2);
                                if (!Objects.equals(bigDecimal1, bigDecimal2)) {
                                    ProductDetailDTO.SkuChangeInfoDTO skuChangeInfoDTO = new ProductDetailDTO.SkuChangeInfoDTO();
                                    skuChangeInfoDTO.setOldValue(bigDecimal1.toString());
                                    skuChangeInfoDTO.setNewValue(bigDecimal2.toString());
                                    skuChangeInfoDTO.setFieldName(fieldName);
                                    differentFields.add(skuChangeInfoDTO);
                                }
                            } else {
                                // 非数字类型，考虑 null 情况
                                if( !value1.equals(value2)){
                                    ProductDetailDTO.SkuChangeInfoDTO skuChangeInfoDTO = new ProductDetailDTO.SkuChangeInfoDTO();
                                    skuChangeInfoDTO.setOldValue((String)value1);
                                    skuChangeInfoDTO.setNewValue((String)value2);
                                    skuChangeInfoDTO.setFieldName(fieldName);
                                    differentFields.add(skuChangeInfoDTO);
                                }
                            }
                        }
                    }
                } catch (NoSuchFieldException e) {
                    // 该字段在类2中不存在，跳过
                    log.debug("Field {} does not exist in class {}", fieldName, class2.getName());
                } catch (IllegalAccessException e) {
                    // 无法访问字段，跳过
                    log.debug("Cannot access field {} in class {}", fieldName, class1.getName());
                }
            }
        }
        return differentFields;
    }

    @Override
    public List<ProductDetailDTO.SkuChangeInfoDTO> getProductBasicChangeField(ProductInfoDTO productInfoDTO, ProductInfoEntity oldEntity) {
        if(StringUtils.isBlank(productInfoDTO.getId())){
            return Collections.emptyList();
        }

        if(Objects.isNull(oldEntity)){
            oldEntity = productInfoService.getById(productInfoDTO.getId());
            if(Objects.isNull(oldEntity)){
                return Collections.emptyList();
            }
        }
        // 定义需要比较的字段名称集合
        Set<String> fieldsToCompare = new HashSet<>(Arrays.asList(
                "category", "chargeName"
        ));
        return compareFields(oldEntity, productInfoDTO,fieldsToCompare);
    }
    @Override
    public List<ProductDetailDTO.SkuChangeInfoDTO> getProductPackChangeField(ProductPackDTO productPackDTO, ProductPackEntity oldEntity) {
        if(StringUtils.isBlank(productPackDTO.getId())){
            return Collections.emptyList();
        }
        if(Objects.isNull(oldEntity )){
            oldEntity = productPackService.getById(productPackDTO.getId());
            if(Objects.isNull(oldEntity)){
                return Collections.emptyList();
            }
        }
        // 定义需要比较的字段名称集合
        Set<String> fieldsToCompare = new HashSet<>(Arrays.asList(
                "productLength", "productWidth", "productHeight",
                "grossWeight", "netWeight", "boxLength", "boxWidth", "boxHeight",
                "boxWeight", "boxQty"
        ));
        return compareFields(oldEntity, productPackDTO,fieldsToCompare);
    }


    /**
     * 处理产品变更通知
     * 当产品基本信息或包装信息发生变化时，发送通知
     */
    @Override
    public void handleProductChangeNotification(List<ProductDetailDTO.NoticeDTO> noticeDTOList , Boolean isTransaction) {
        // 检查是否有产品基本信息或包装信息变更
        if(CollUtil.isNotEmpty(noticeDTOList)){
            // 创建通知列表并添加变更信息
            List<ProductDetailDTO.SkuChangeFieldsDTO> noticeList = new ArrayList<>();
            for (ProductDetailDTO.NoticeDTO noticeDTO : noticeDTOList) {
                if(CollUtil.isNotEmpty(noticeDTO.getProductBasicChangeField()) || CollUtil.isNotEmpty(noticeDTO.getProductPackChangeField())){
                    // 有变动情况下才去推送消息
                    ProductDetailDTO.SkuChangeFieldsDTO skuChangeFieldsDTO = new ProductDetailDTO.SkuChangeFieldsDTO();
                    skuChangeFieldsDTO.setProductId(noticeDTO.getProductId());
                    skuChangeFieldsDTO.setName(noticeDTO.getName());
                    skuChangeFieldsDTO.setChargeId(noticeDTO.getChargeId());
                    skuChangeFieldsDTO.setChargeName(noticeDTO.getChargeName());
                    skuChangeFieldsDTO.setSkuNo(noticeDTO.getSkuNo());
                    skuChangeFieldsDTO.setProductBasicChangeField(noticeDTO.getProductBasicChangeField());
                    skuChangeFieldsDTO.setProductPackChangeField(noticeDTO.getProductPackChangeField());
                    noticeList.add(skuChangeFieldsDTO);
                }
            }
            if(noticeList.size() > 0){
                if(isTransaction){
                    // 注册事务同步监听器，在事务提交后发送变更通知
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            // 调用消息推送方法
                            noticeMessageService.productChangeNotice(NoticeEnum.PRODUCT_DETAIL_CHANGE,noticeList);
                        }
                    });
                }else{
                    // 调用消息推送方法
                    noticeMessageService.productChangeNotice(NoticeEnum.PRODUCT_DETAIL_CHANGE,noticeList);
                }
            }
        }
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
        ProductInfoDTO productSpuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO();
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();
        // 添加默认spu
        productSpuBaseInfoDTO.setSpuNo(Optional.ofNullable(productSpuBaseInfoDTO.getSpuNo()).orElse(productSkuBaseInfoDTO.getSkuNo()));
        //检查spu编号是否重复
        if (this.checkSpuNo(productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO().getSpuNo(), productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95017);
        }
        //检查sku编号是否重复
        if (this.checkSkuNo(productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getSkuNo(), productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95015);
        }
        if(CollectionUtils.isNotEmpty(productSpuBaseInfoDTO.getApplicationCategoryIdList())){
            productSpuBaseInfoDTO.setApplicationCategoryId(String.join(",", productSpuBaseInfoDTO.getApplicationCategoryIdList()));
        }
        //校验 【箱规-长宽高】必须大于等于【包装尺寸-长宽高】【为空则忽略不校验】【长，宽，高分开校验】
        ProductPackDTO productPackDTO = productNoSpecDTO.getProductPackDTO();
        checkSizeAndWeight(productPackDTO);
        productSpuBaseInfoDTO.setSpecType(1);
        //产品等级
        if (StringUtils.isNotBlank(productSpuBaseInfoDTO.getGradeId())) {
            //根据id查询字典表中的产品等级
            BasicDictEntity basicDict = basicDictService.getById(productSpuBaseInfoDTO.getGradeId());
            if (ObjectUtils.isNotEmpty(basicDict)) {
                productSpuBaseInfoDTO.setGrade(basicDict.getValue());
            }
        }

        //产品款名和产品品名关系处理
        handleProductNames(productNoSpecDTO);

        //获取产品基本信息修改的字段
        List<ProductDetailDTO.SkuChangeInfoDTO> productBasicChangeField = getProductBasicChangeField(productSpuBaseInfoDTO,null);
        //获取产品包装信息修改的字段
        List<ProductDetailDTO.SkuChangeInfoDTO> productPackChangeField = getProductPackChangeField(productPackDTO,null);

        //SKU操作日志-产品信息
        ProductInfoEntity productInfoEntity = productInfoService.getById(productSpuBaseInfoDTO.getId());
        if (StringUtils.isNotBlank(productSpuBaseInfoDTO.getId())) {
            //产品信息修改操作日志
            addProductInfoLog(productSpuBaseInfoDTO, productInfoEntity, productSpuBaseInfoDTO.getId(), productSpuBaseInfoDTO.getId());

            //单品或者Bom都需要检查库存是否大于零
            String skuId = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getId();
            productChangeService.checkInventoryGreaterThanZero(productInfoEntity,productSpuBaseInfoDTO.getPropertyId(),skuId);
        }
        //1.修改产品表 主表信息
        productSpuBaseInfoDTO.setIsNoSpecAdd(MathUtil.ONE);

        String id = productInfoService.updateSpec(productSpuBaseInfoDTO);

        //2.修改/新增 sku信息


        productSkuBaseInfoDTO.setProductId(id);

        //SKU修改操作日志
        Boolean isAdd = Boolean.TRUE;
        if (StringUtils.isNotBlank(productSkuBaseInfoDTO.getId())) {
            ProductDetailEntity oldEntity = this.getById(productSkuBaseInfoDTO.getId());
            addProductSkuBaseInfoLog(productSkuBaseInfoDTO, oldEntity, productSkuBaseInfoDTO.getId(), id);
            isAdd = Boolean.FALSE;
        }
        String skuId = this.saveOrUpdate(productSkuBaseInfoDTO);
        //给对象赋值
        productSkuBaseInfoDTO.setId(skuId);
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
            // 验证一级供应商和二级供应商是否正确
            List<String> supplierIds = Lists.newArrayList();
            String mainSupplier = productNoSpecDTO.getProductPurchaseDTO().getMainSupplier();
            String secondSupplier = productNoSpecDTO.getProductPurchaseDTO().getSecondSupplier();
            if (StrUtils.isNotEmpty(mainSupplier)) {
                supplierIds.add(mainSupplier);
            }
            if (StrUtils.isNotEmpty(secondSupplier)) {
                supplierIds.add(secondSupplier);
            }
            if (CollUtil.isNotEmpty(supplierIds)) {
                Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);
                if (StrUtils.isNotEmpty(mainSupplier) && !supplierMap.containsKey(mainSupplier)) {
                    throw new ServiceException("sku采购信息一级供应商不存在");
                }
                if (StrUtils.isNotEmpty(secondSupplier) && !supplierMap.containsKey(secondSupplier)) {
                    throw new ServiceException("sku采购信息二级供应商不存在");
                }
            }
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
            //保险属性
            if(CollUtil.isNotEmpty(productLogisticsDTO.getInsurancePropertyList())){
                productLogisticsDTO.setInsuranceProperty(productLogisticsDTO.getInsurancePropertyList().stream().collect(Collectors.joining(",")));
            }
            //SKU操作日志
            addProductLogisticsLog(productLogisticsDTO, id);
            productLogisticsService.saveOrUpdate(productLogisticsDTO);
        }

        //7.修改/新增 包装信息

        if (ObjectUtils.isNotEmpty(productPackDTO)) {
            productPackDTO.setSkuId(skuId);
            //SKU操作日志
            addProductPackLog(productPackDTO, id);
            productPackService.saveOrUpdate(productPackDTO);
        }

        //8.修改/新增 证书信息
        List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList = productNoSpecDTO.getProductCertificateList();
        if (ObjectUtils.isNotEmpty(productCertificateList)) {
            productCertificateList.forEach(obj -> obj.setSkuId(skuId));
            productCertificateService.productAddOrUpdate(productCertificateList);
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
        List<ProductCustomsEntity> productCustomsEntityList = productCustomsService.listByProductId(id);
        //11.修改/新增  目的国海关编码信息
        List<ProductCustomsDTO> productCustomsDTO = productNoSpecDTO.getProductCustomsList();
        List<ProductCustomsEntity> productCustomsDTOEntityList = BeanMapper.copyList(productCustomsDTO, ProductCustomsEntity.class);

        List<String> deleteIds = getDeleteIds(productCustomsDTOEntityList, productCustomsEntityList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            productCustomsService.removeByIds(deleteIds);
        }
        if (CollectionUtils.isNotEmpty(productCustomsDTO)) {
            List<ProductCustomsEntity> customsEntityList = new ArrayList<>();
            for (ProductCustomsDTO customsDTO : productCustomsDTO) {
                ProductCustomsEntity customsEntity = new ProductCustomsEntity();
                BeanMapper.copy(customsDTO, customsEntity);
                customsEntity.setSkuId(skuId);
                if (StringUtils.isNotEmpty(customsEntity.getToCurrency())){
                    customsEntity.setToCurrencySymbol(CurrencyEnum.getSymbolByCode(customsDTO.getToCurrency()));
                }else {
                    customsEntity.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
                    customsEntity.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
                }
                //根据sku获取是否存在记录
                ProductCustomsEntity oldEntity = productCustomsService.getBySkuIdAndCountry(skuId, customsDTO.getCountry());
                if (Objects.nonNull(oldEntity)){
                    customsEntity.setId(oldEntity.getId());
                }
                customsEntityList.add(customsEntity);
            }
            addProductCustomsLog(productCustomsDTO, id);
            //数据新增或更新
            batchAddOrUpdateCustoms(customsEntityList);
        }
        //增加默认记录
        productCustomsService.addDefaultCustoms(Collections.singletonList(skuId));

        //发送通知
        ProductDetailDTO.NoticeDTO noticeDTO = new ProductDetailDTO.NoticeDTO();
        noticeDTO.setProductId(id);
        noticeDTO.setName(productSpuBaseInfoDTO.getName());
        noticeDTO.setChargeId(productSpuBaseInfoDTO.getChargeId());
        noticeDTO.setChargeName(productSpuBaseInfoDTO.getChargeName());
        noticeDTO.setSkuNo(productSkuBaseInfoDTO.getSkuNo());
        noticeDTO.setProductBasicChangeField(productBasicChangeField);
        noticeDTO.setProductPackChangeField(productPackChangeField);
        List<ProductDetailDTO.NoticeDTO> noticeDTOList = Arrays.asList(noticeDTO);
        //发送消息
        handleProductChangeNotification(noticeDTOList,Boolean.TRUE);
        return true;
    }


    //处理产品品名和产品款名关系
    private void handleProductNames(ProductNoSpecDTO productNoSpecDTO) {
        ProductInfoDTO productSpuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO();
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();
        //sku
        String name = productSkuBaseInfoDTO.getName();
        String nameEn = productSkuBaseInfoDTO.getNameEn();
        //spu
        String spuName = productSpuBaseInfoDTO.getName();
        String spuNameEn = productSpuBaseInfoDTO.getNameEn();
        // 默认空白 ；若产品品名(中文)有值、产品款名(中文)为空，则产品款名默认=产品品名，用户可再修改
        if(StringUtils.isNotBlank(name) && StringUtils.isBlank(spuName)){
            spuName = name;
        }
        // 默认空白 ；若产品款名(中文)有值、产品品名(中文)为空，则产品品名默认=产品款名，用户可再修改
        else if(StringUtils.isNotBlank(spuName) && StringUtils.isBlank(name)){
            name = spuName;
        }

        // 默认空白 ；若产品品名(英文)有值、产品款名(英文)为空，则产品款名默认=产品品名，用户可再修改
        if(StringUtils.isNotBlank(nameEn) && StringUtils.isBlank(spuNameEn)){
            spuNameEn = nameEn;
        }
        // 默认空白 ；若产品款名(英文)有值、产品品名(英文)为空，则产品品名默认=产品款名，用户可再修改
        else if(StringUtils.isNotBlank(spuNameEn) && StringUtils.isBlank(nameEn)){
            nameEn = spuNameEn;
        }
        productSkuBaseInfoDTO.setName(name);
        productSkuBaseInfoDTO.setNameEn(nameEn);

        productSpuBaseInfoDTO.setName(spuName);
        productSpuBaseInfoDTO.setNameEn(spuNameEn);
    }

    private void checkSizeAndWeight(ProductPackDTO productPackDTO) {
        if (ObjectUtils.isNotEmpty(productPackDTO)) {
            compareDimensions(productPackDTO.getBoxLength(), productPackDTO.getProductLength(), ApiError.ERROR_LENGTH_BOX_LITTER_THAN_PRODUCT);
            compareDimensions(productPackDTO.getBoxWidth(), productPackDTO.getProductWidth(), ApiError.ERROR_WIDTH_BOX_LITTER_THAN_PRODUCT);
            compareDimensions(productPackDTO.getBoxHeight(), productPackDTO.getProductHeight(), ApiError.ERROR_HEIGHT_BOX_LITTER_THAN_PRODUCT);
            //毛重大于等于净重
            compareDimensions(productPackDTO.getGrossWeight(), productPackDTO.getNetWeight(), ApiError.ERROR_WEIGHT_GROSS_LITTER_THAN_NET);
        }
    }

    /**
     * 比较尺寸
     *
     * @author hyj
     * @date 2024/5/10 9:05
     * @param larger   大尺寸
     * @param smaller  小尺寸
     * @param apiError 报错信息
     */
    @Override
    public void compareDimensions(BigDecimal larger, BigDecimal smaller, ApiError apiError) {
        if (Objects.nonNull(larger) && larger.compareTo(BigDecimal.ZERO) > 0
                && Objects.nonNull(smaller) && smaller.compareTo(BigDecimal.ZERO) > 0
                && larger.compareTo(smaller) < 0) {
             throw new ServiceException(apiError);
        }
    }


    /**
     * 添加海关编码日志
     *
     * @param productCustomsList
     * @param productId
     * @return void
     * @Author Luo_WG
     * @Date 2023/6/28 17:58
     **/
    private void addProductCustomsLog(List<ProductCustomsDTO> productCustomsList, String productId) {
        if (CollectionUtils.isEmpty(productCustomsList)) {
            return;
        }
        List<String> ids = productCustomsList.stream().filter(obj -> StringUtils.isNotBlank(obj.getId())).map(ProductCustomsDTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<ProductCustomsEntity> productCustomsEntityList = productCustomsService.listByIds(ids);

        productCustomsList.forEach(obj -> {
            //SKU操作日志
            ProductCustomsEntity oldEntity = productCustomsEntityList.stream().filter(e -> e.getId().equals(obj.getId())).findFirst().orElse(null);
            ProductCustomsDTO oldDto = new ProductCustomsDTO();
            if (ObjectUtils.isNotEmpty(oldEntity)) {
                BeanMapperUtils.copy(oldEntity, oldDto);
                if (StringUtils.isNotBlank(obj.getCountry())) {
                    String[] split = obj.getCountry().split(",");
                    List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
                    List<String> countryNameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                    obj.setCountryName(StringUtils.join(countryNameList, ","));

                    String[] splitt = oldEntity.getCountry().split(",");
                    List<BasicDictEntity> basicDictEntitiest = basicDictService.listByIds(Arrays.asList(splitt));
                    List<String> countryNameListt = basicDictEntitiest.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                    oldDto.setCountryName(StringUtils.join(countryNameListt, ","));
                }
                ProductDetailEntity productDetailEntity = this.getById(obj.getSkuId());
                if (ObjectUtils.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                sysLogService.addSysLogByUpdate(oldDto, obj, SKUCLASSPATH, obj.getSkuId(), productId, String.format("SKU[%s]", productDetailEntity.getSkuNo()));
            }

        });
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

        List<ProductDetailDTO> productDetailList = productManySpecDTO.getProductDetailList();
        //检查sku是否重复
        for (int i = 0; i < productDetailList.size(); i++) {
            if (this.checkSkuNo(productDetailList.get(i).getSkuNo(), productDetailList.get(i).getId())) {
                throw new ServiceException(ApiError.ERROR_95015.code, ApiError.ERROR_95015.msg + " 第" + (i + 1) + "行");
            }
        }

        //校验 【箱规-长宽高】必须大于等于【包装尺寸-长宽高】【为空则忽略不校验】【长，宽，高分开校验】
        productManySpecDTO.getProductPackList().stream().forEach(productPackDTO->{
            checkSizeAndWeight(productPackDTO);
        });

        //获取产品基本信息修改的字段
        List<ProductDetailDTO.SkuChangeInfoDTO> productBasicChangeField = getProductBasicChangeField(productManySpecDTO.getProductInfoDTO(),null);
        //获取产品包装信息修改的字段
        Map<String,List<ProductDetailDTO.SkuChangeInfoDTO>> productPackChangeFieldMap = new HashMap<>();
        if(CollUtil.isNotEmpty(productManySpecDTO.getProductPackList())){
            for (ProductPackDTO productPackDTO : productManySpecDTO.getProductPackList()) {
                String skuNo = productPackDTO.getSkuNo();
                List<ProductDetailDTO.SkuChangeInfoDTO> productPackChangeField = getProductPackChangeField(productPackDTO,null);
                productPackChangeFieldMap.put(skuNo,productPackChangeField);
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
        if(CollectionUtils.isNotEmpty(productInfoDTO.getApplicationCategoryIdList())){
            productInfoDTO.setApplicationCategoryId(String.join(",", productInfoDTO.getApplicationCategoryIdList()));
        }
        //SKU操作日志-产品信息
        String id = productInfoDTO.getId();
        ProductInfoEntity productInfoEntity = productInfoService.getById(productInfoDTO.getId());
        if (ObjectUtils.isNotEmpty(productInfoDTO)) {
            if (StringUtils.isNotBlank(productInfoDTO.getId()) && ObjectUtils.isNotEmpty(productInfoEntity)) {
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
                //产品款名和产品品名关系处理
                handleProductNames(obj, productInfoDTO);
                //单品或者Bom都需要检查库存是否大于零
                productChangeService.checkInventoryGreaterThanZero(productInfoEntity,productManySpecDTO.getProductInfoDTO().getPropertyId(),obj.getId());
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
        if (!productLogisticsList.isEmpty()) {
            productLogisticsList.forEach(productLogisticsDTO -> {
                //保险属性
                if(CollUtil.isNotEmpty(productLogisticsDTO.getInsurancePropertyList())){
                    productLogisticsDTO.setInsuranceProperty(productLogisticsDTO.getInsurancePropertyList().stream().collect(Collectors.joining(",")));
                }
            });
            //操作日志
            productLogisticsList.forEach(obj -> addProductLogisticsLog(obj, productInfoDTO.getId()));
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
        List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList = productManySpecDTO.getProductCertificateList();
        if (ObjectUtils.isNotEmpty(productCertificateList)) {
            productCertificateService.productAddOrUpdate(productCertificateList);
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

        //11.修改/新增  目的国海关编码信息
        List<ProductCustomsDTO> productCustomsDTO = productManySpecDTO.getProductCustomsList();
        List<ProductCustomsEntity> productCustomsDTOEntityList = BeanMapper.copyList(productCustomsDTO, ProductCustomsEntity.class);
        List<ProductCustomsEntity> productCustomsEntityList = productCustomsService.listByProductId(productInfoDTO.getId());
        List<String> deleteIds = getDeleteIds(productCustomsDTOEntityList, productCustomsEntityList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            productCustomsService.removeByIds(deleteIds);
        }
        if (CollectionUtils.isNotEmpty(productCustomsDTO)) {
            List<ProductCustomsEntity> customsEntityList = new ArrayList<>();
            for (ProductCustomsDTO customsDTO : productCustomsDTO) {
                ProductCustomsEntity customsEntity = new ProductCustomsEntity();
                BeanMapper.copy(customsDTO, customsEntity);
                if (StringUtils.isNotEmpty(customsEntity.getToCurrency())){
                    customsEntity.setToCurrencySymbol(CurrencyEnum.getSymbolByCode(customsDTO.getToCurrency()));
                }else {
                    customsEntity.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
                    customsEntity.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
                }
                //根据sku获取是否存在记录
                ProductCustomsEntity oldEntity = productCustomsService.getBySkuIdAndCountry(customsDTO.getSkuId(), customsDTO.getCountry());
                if (Objects.nonNull(oldEntity)){
                    customsEntity.setId(oldEntity.getId());
                }
                customsEntityList.add(customsEntity);
            }
            addProductCustomsLog(productCustomsDTO, productInfoDTO.getId());
            //数据新增或更新
            batchAddOrUpdateCustoms(customsEntityList);
        }
        //增加默认记录
        productCustomsService.addDefaultCustoms(productDetailLists.stream().map(ProductDetailDTO::getId).collect(Collectors.toList()));

        List<ProductDetailDTO.NoticeDTO> noticeDTOList = new ArrayList<>();
        for (ProductDetailDTO productDetailDTO : productDetailLists) {
            //发送通知
            ProductDetailDTO.NoticeDTO noticeDTO = new ProductDetailDTO.NoticeDTO();
            noticeDTO.setProductId(id);
            noticeDTO.setName(productInfoDTO.getName());
            noticeDTO.setChargeId(productInfoDTO.getChargeId());
            noticeDTO.setChargeName(productInfoDTO.getChargeName());
            noticeDTO.setSkuNo(productDetailDTO.getSkuNo());
            noticeDTO.setProductBasicChangeField(productBasicChangeField);
            noticeDTO.setProductPackChangeField(productPackChangeFieldMap.get(productDetailDTO.getSkuNo()));
            noticeDTOList.add(noticeDTO);
        }
        //发送通知
        handleProductChangeNotification(noticeDTOList,Boolean.TRUE);
        return true;
    }

    /**
     * 报关信息新增或修改
     * @author will
     * @date 2025/6/19 17:33
     * @param customsEntityList
     * @return void
     */
    private void batchAddOrUpdateCustoms (List<ProductCustomsEntity> customsEntityList) {
        if (CollUtil.isEmpty(customsEntityList)) {
            return;
        }
        //数据新增或更新
        List<ProductCustomsEntity> addList = customsEntityList.stream().filter(obj -> isBlank(obj.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addList)) {
            productCustomsService.saveBatch(addList);
        }
        List<ProductCustomsEntity> updateList = customsEntityList.stream().filter(obj -> isNotBlank(obj.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(updateList)) {
            productCustomsService.updateBatchById(updateList);
        }
    }

    //产品款名和产品品名关系处理
    private void handleProductNames(ProductDetailDTO obj,ProductInfoDTO productInfoDTO ) {
        //sku
        String name = obj.getName();
        String nameEn = obj.getNameEn();
        //spu
        String spuName = productInfoDTO.getName();
        String spuNameEn = productInfoDTO.getNameEn();
        if(StringUtils.isNotBlank(spuName) && StringUtils.isBlank(name)){
            name = spuName;
        }
        if(StringUtils.isNotBlank(spuNameEn) && StringUtils.isBlank(nameEn)){
            nameEn = spuNameEn;
        }
        obj.setName(name);
        obj.setNameEn(nameEn);
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

        ProductInfoDTO productSpuBaseInfoDTO = variantAutoAddDTO.getProductSpuBaseInfoDTO();
        if(CollectionUtils.isNotEmpty(productSpuBaseInfoDTO.getApplicationCategoryIdList())){
            productSpuBaseInfoDTO.setApplicationCategoryId(String.join(",", productSpuBaseInfoDTO.getApplicationCategoryIdList()));
        }
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
            productVariantOptionEntity.setVariantValue(toJSONString(req.getVarianList()));
            productVariantService.setupOccupy(Arrays.asList(req.getPropertyType()));
            productVariantPropertyService.setupOccupy(req.getVarianList(), Arrays.asList(req.getPropertyType()));
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
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < models.size(); i++) {
                sb.append(models.get(i).getAuthor());
                if (i + 1 < models.size()) {
                    sb.append(",");
                }
            }
            varianList.add(sb.toString());
        }
        //过滤掉重复的变体属性
        List<ProductDetailEntity> detailEntityList = this.getSkuListByProductId(id);
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
            ProductUnitEntity productUnitEntity = productUnitService.checkUnitName("Pcs");
            if (ObjectUtils.isNotEmpty(productUnitEntity)) {
                productDetailEntity.setUnitId(productUnitEntity.getId());
                productDetailEntity.setUnitName(productUnitEntity.getName());
            }
            productDetailEntity.setProductId(id);
            productDetailEntity.setName(variantAutoAddDTO.getProductSpuBaseInfoDTO().getName());
            productDetailEntity.setNameEn(variantAutoAddDTO.getProductSpuBaseInfoDTO().getNameEn());
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
        return this.getSkuListByProductId(id);
    }


    /**
     * @param skuId:产品sku表主键id
     * @return java.lang.Boolean
     * @Description 删除多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(String skuId) {
        ProductDetailEntity detailEntity = this.getById(skuId);
        ProductDetailEntity oldEntity = Optional.ofNullable(detailEntity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "产品sku"));
        if (oldEntity.getStatus().equals(1) || oldEntity.getStatus().equals(2)) {
            throw new ServiceException(ApiError.ERROR_95241);
        }
        if (oldEntity.getOccupyStatus()) {
            throw new ServiceException(ApiError.ERROR_95242);
        }
        List<String> idList = Arrays.asList(skuId);
        //1.删除证书信息
        productCertificateService.deleteBySkuIdList(idList);
        //2.删除包装信息
        productPackService.removePack(idList);
        //3.删除物流信息
        productLogisticsService.removeLogistics(idList);
        //4.删除销售信息
        productSaleService.removeSale(idList);
        //5.删除采购信息
        productPurchaseService.removePurchase(idList);
        //6.删除成本信息
        productCostService.removeCost(idList);
        //7.删除任务关联sku 信息
        taskRefSkuConfigService.removeTaskRefSku(idList);
        //7.删除目的国海关编码
        productCustomsService.removeBySkuId(idList);
        //8.删除sku信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getId, skuId);
        ProductDetailEntity productDetailEntity = this.getById(skuId);
        if (ObjectUtils.isNotEmpty(productDetailEntity)) {
            ProductInfoEntity productInfoEntity = productInfoService.getById(productDetailEntity.getProductId());
            List<ProductDetailEntity> skuListByProductId = this.getSkuListByProductId(productDetailEntity.getProductId());
            if (ObjectUtils.isNotEmpty(productInfoEntity)) {
                if (skuListByProductId.size() == 1 && productInfoEntity.getIsFinishedProductDev() == null) {
                    ProductInfoEntity infoEntity = productInfoService.getById(productInfoEntity.getId());
                    infoEntity.setIsDeleted(Boolean.TRUE);
                    productInfoService.removeById(productInfoEntity.getId());
                }
                //添加操作日志
                SysLogEntity sysLogEntity = new SysLogEntity().setClassPath(SPUCLASSPATH).setBusinessId(productInfoEntity.getId()).setPid(productInfoEntity.getId()).setOperation("删除信息").setContent("删除了一个SKU：[" + productDetailEntity.getSkuNo() + "]");
                sysLogService.addSysLogByOther(sysLogEntity);
            }
        }
        ProductDetailEntity entity = lambdaQuery().eq(ProductDetailEntity::getId, skuId).one();
        entity.setIsDeleted(Boolean.TRUE);

        //发送金蝶
        sendPushTask(Arrays.asList(oldEntity),SyncOperateEnum.OPERATE_DELETE.getCode());
        //增加一条虚假的同步任务记录
        syncWangDianProductDetailService.addPlmPushMsg(entity);
        syncLingXingProductDetailService.addPlmPushMsg(entity);
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
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getProductId, id);
        List<ProductDetailEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        long count = list.stream().filter(req -> !req.getStatus().equals(1) && !req.getStatus().equals(2)).count();
        if (list.size() != count) {
            throw new ServiceException(ApiError.ERROR_95241);
        }
        long sign = list.stream().filter(req -> !req.getOccupyStatus()).count();
        if (list.size() != sign) {
            throw new ServiceException(ApiError.ERROR_95242);
        }

        //发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());

        List<String> skuIds = list.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        //1.删除证书信息
        productCertificateService.deleteBySkuIdList(skuIds);
        //2.删除包装信息
        productPackService.removePack(skuIds);
        //3.删除物流信息
        productLogisticsService.removeLogistics(skuIds);
        //4.删除销售信息
        productSaleService.removeSale(skuIds);
        //5.删除采购信息
        productPurchaseService.removePurchase(skuIds);
        //6.删除成本信息
        productCostService.removeCost(skuIds);
        //7.删除目的国海关编码
        productCustomsService.removeBySkuId(skuIds);

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
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
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
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
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
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductInfoEntity::getSpuNo, spuNo);
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
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductInfoEntity::getName, name);
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
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductDetailEntity::getSkuNo, sku);
        queryWrapper.last(SqlConstants.LIMIT_1);
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
    public String inportExcel(ProductNoSpecDTO productNoSpecDTO) {
        ProductInfoDTO productSpuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO();
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();
        productSpuBaseInfoDTO.setNameEn(productSkuBaseInfoDTO.getNameEn());
        //1.新增产品表 主表信息
        String id;
        if (StringUtils.isBlank(productSpuBaseInfoDTO.getId())) {
            id = productInfoService.updateSpec(productSpuBaseInfoDTO);
        }else {
            id = productSpuBaseInfoDTO.getId();
            ProductInfoEntity byId = productInfoService.getById(id);
            BeanMapper.copyNonNull(productSpuBaseInfoDTO, byId);
            //判断是否是迭代产品
            if (ProductTypeEnum.ITERATIVE_PRODUCT.getCode().equals(byId.getType())) {
                ProductDetailEntity productDetailEntity = productDetailService.getById(byId.getIterateRefSkuId());
                if (ObjectUtils.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_PRODUCT_ITERATE_REF_SKU_NOT_EXIST);
                }
                byId.setIterateRefSkuId(productDetailEntity.getId());
                byId.setIterateRefSkuNo(productDetailEntity.getSkuNo());
            } else {
                byId.setType(ProductTypeEnum.NEW_PRODUCT.getCode());
                byId.setIterateRefSkuId("");
                byId.setIterateRefSkuNo("");
            }
            productInfoService.saveOrUpdate(byId);
        }


        //2.修改/新增 sku信息

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

        ProductKeyDTO productKey = productDetailMapper.getProductKey(skuId);

        //3.修改/新增 成本信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductCostDTO())) {
            ProductCostDTO productCostDTO = productNoSpecDTO.getProductCostDTO();
            //成本信息主键id赋值
            productCostDTO.setId(ObjectUtil.isEmpty(productKey) ? "" : productKey.getCostId());
            productCostDTO.setSkuId(skuId);
            ProductCostEntity byId = productCostService.getById(productCostDTO.getId());
            if(Objects.nonNull(byId)){
                BeanMapper.copyNonNull(productNoSpecDTO.getProductCostDTO(), byId);
                BeanUtil.copyProperties(byId,productCostDTO);
            }
            //操作日志
            addProductCostLog(productCostDTO, id);
            productCostService.saveOrUpdate(productCostDTO);
        }

        //4.修改/新增 采购信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductPurchaseDTO())) {
            ProductPurchaseDTO productPurchaseDTO = productNoSpecDTO.getProductPurchaseDTO();
            productPurchaseDTO.setId(ObjectUtil.isEmpty(productKey) ? "" : productKey.getPurchaseId());
            productPurchaseDTO.setSkuId(skuId);
            ProductPurchaseEntity byId = productPurchaseService.getById(productPurchaseDTO.getId());
            if(Objects.nonNull(byId)){
                BeanMapper.copyNonNull(productNoSpecDTO.getProductPurchaseDTO(),byId);
                BeanUtil.copyProperties(byId,productPurchaseDTO);
            }

            productPurchaseService.saveOrUpdate(productPurchaseDTO);
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
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductSaleDTO())) {
            ProductSaleDTO productSaleDTO = productNoSpecDTO.getProductSaleDTO();
            productSaleDTO.setId(ObjectUtil.isEmpty(productKey) ? "" : productKey.getSaleId());
            productSaleDTO.setSkuId(skuId);
            ProductSaleEntity byId = productSaleService.getById(productSaleDTO.getId());
            if(Objects.nonNull(byId)){
                BeanMapper.copyNonNull(productNoSpecDTO.getProductSaleDTO(), byId);
                BeanUtil.copyProperties(byId,productSaleDTO);
            }
            //操作日志
            addProductSaleLog(productSaleDTO, id);
            productSaleService.saveOrUpdate(productSaleDTO);
        }
        //6.修改/新增 物流信息
        if (!ObjectUtils.isEmpty(productNoSpecDTO.getProductLogisticsDTO())) {
            ProductLogisticsDTO productLogisticsDTO = productNoSpecDTO.getProductLogisticsDTO();
            productLogisticsDTO.setId(ObjectUtil.isEmpty(productKey) ? "" : productKey.getLogisticsId());
            productLogisticsDTO.setSkuId(skuId);
            ProductLogisticsEntity byId = productLogisticsService.getById(productLogisticsDTO.getId());
            if(Objects.nonNull(byId)){
                BeanMapper.copyNonNull(productLogisticsDTO, byId);
                BeanUtil.copyProperties(byId,productLogisticsDTO);
            }
            //操作日志
            addProductLogisticsLog(productLogisticsDTO, id);
            productLogisticsService.saveOrUpdate(productLogisticsDTO);
        }

        //7.修改/新增 包装信息
        ProductPackDTO productPackDTO = productNoSpecDTO.getProductPackDTO();
        if (!ObjectUtils.isEmpty(productPackDTO)) {
            productPackDTO.setId(ObjectUtil.isEmpty(productKey) ? "" : productKey.getPackId());
            productPackDTO.setSkuId(skuId);
            ProductPackEntity byId = productPackService.getById(productPackDTO.getId());
            if(Objects.nonNull(byId)){
                BeanMapper.copyNonNull(productPackDTO, byId);
                BeanUtil.copyProperties(byId,productPackDTO);
            }

            productPackService.saveOrUpdate(productPackDTO);
        }
        return id;
    }

    /**
     * @Description 新增无规格sku信息 并推送金蝶
     * @Author jack
     * @Date 2025-02-07
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    @Override
    @Transactional
    public Boolean inportExcelAndSync(ProductNoSpecDTO productNoSpecDTO,ProductDetailEntity productBy) {
        inportExcel(productNoSpecDTO);
        //只同步审核通过的
        if(Objects.nonNull(productBy) && productBy.getStatus().equals(ProductDetailStatusEnum.APPROVAL_PASS.getCode())){
            //发送金蝶
            sendPushTask(Arrays.asList(productBy), SyncOperateEnum.OPERATE_APPROVE.getCode());

            syncWangDianProductDetailService.syncDataToWangDian(productBy);

            syncLingXingProductDetailService.syncDataToLingxing(productBy);
            //增加缓存清除
            redisUtil.hdel(RedisKeyConstant.LIST_SKU_INFO, productBy.getId());
        }
        return true;
    }

    /**
     * 获取数据导出excel
     *
     * @param productSkuExcelDTO
     * @return void
     * @Author Luo_WG
     * @Date 2022/10/10 12:09
     **/
    @Override
    public void exportProduct(ProductSkuExcelDTO productSkuExcelDTO, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("产品管理导出", EXPORT_PLM_SKU.getCode(), productSkuExcelDTO);
    }

    @Override
    public PagingVO<ProductDetailExcelExportDTO> exportProductDetail(PagingDTO<ProductSkuExcelDTO> dto ) {
        Page<ProductSkuExcelDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<ProductDetailExcelExportDTO> pageData = productDetailMapper.getExportSkuExcel(query,dto.getParams());
        List<ProductDetailExcelExportDTO> list = pageData.getRecords();
        if(CollUtil.isNotEmpty(list)) {
            Map<String, String> userIdNameMaps = new HashMap<>();
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            if(CollUtil.isNotEmpty(userList)) {
                userIdNameMaps = userList.stream()
                        .filter(u -> u != null && StringUtils.isNotBlank(u.getUserId()))
                        .collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName , (u1 , u2) -> StringUtils.isNotBlank(u1) ? u1 : u2));
            }
            Map<String, String> finalUserIdNameMaps = userIdNameMaps;

            List<String> mainSupplierIds = list.stream().map(ProductDetailExcelExportDTO::getMainSupplier).distinct().collect(Collectors.toList());
            List<String> secondSupplierIds = list.stream().map(ProductDetailExcelExportDTO::getSecondSupplier).distinct().collect(Collectors.toList());
            mainSupplierIds.addAll(secondSupplierIds);
            Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(mainSupplierIds);
            List<ApplicationCategoryEntity> applicationCategoryList = applicationCategoryService.list();
            String chargeId = "";
            String productPropertyId = "";
            String saleCountry = "";
            List<String> chargeIds = new ArrayList<>();
            List<String> productPropertyIdAndSaleCountrys = new ArrayList<>();
            // 获取保险属性字典数据并缓存
            List<BasicDictEntity> insurancePropertyList = basicDictService.listByType(BasicDictTypeEnum.INSURANCE_PROPERTY.getCode());
            Map<String, BasicDictEntity>  insurancePropertyMap = insurancePropertyList.stream()
                    .collect(Collectors.toMap(BasicDictEntity::getValue, entity -> entity));

            for(ProductDetailExcelExportDTO l : list) {
                if(StringUtils.isNotBlank(l.getApplicationCategoryId())){
                    List<String> applicationCategoryIdList = Arrays.stream(l.getApplicationCategoryId().split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                    List<ApplicationCategoryEntity> applicationCategoryEntities = applicationCategoryList.stream()
                            .filter(ac -> applicationCategoryIdList.contains(ac.getId()))
                            .collect(Collectors.toList());
                    l.setApplicationCategoryName(applicationCategoryEntities.stream()
                            .map(ApplicationCategoryEntity::getName)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(",")));
                }
                chargeId = l.getChargeId();
                if(StringUtils.isNotBlank(chargeId)) {
                    chargeIds.addAll(Arrays.stream(chargeId.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
                }
                productPropertyId = l.getProductPropertyId();
                if(StringUtils.isNotBlank(productPropertyId)) {
                    productPropertyIdAndSaleCountrys.addAll(Arrays.stream(productPropertyId.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
                }
                saleCountry = l.getSaleCountry();
                if(StringUtils.isNotBlank(saleCountry)) {
                    productPropertyIdAndSaleCountrys.addAll(Arrays.stream(saleCountry.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
                }
                //保险属性
                if(StringUtils.isNotBlank(l.getInsuranceProperty())){
                    l.setInsuranceProperty(String.join(",", productLogisticsService.getInsurancePropertyList(l.getInsuranceProperty(), insurancePropertyMap)));
                }
            }

            Map<String, String> chargeIdNameMaps = new HashMap<>();
            if(CollUtil.isNotEmpty(chargeIds)) {
                List<FindUserDTO> userListByUserIds = sysUserFeign.getUserListByUserIds(chargeIds);
                if(CollUtil.isNotEmpty(userListByUserIds)) {
                    chargeIdNameMaps = userListByUserIds.stream()
                            .filter(u -> u != null && StringUtils.isNotBlank(u.getUserId()))
                            .collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName , (u1 , u2) -> StringUtils.isNotBlank(u1) ? u1 : u2));
                }
            }
            Map<String, String> finalChargeIdNameMaps = chargeIdNameMaps;

            Map<String, String> dictValueMaps = new HashMap<>();
            if(CollUtil.isNotEmpty(productPropertyIdAndSaleCountrys)) {
                List<BasicDictEntity> dictList = basicDictService.listByIds(productPropertyIdAndSaleCountrys);
                if(CollUtil.isNotEmpty(dictList)) {
                    dictValueMaps = dictList.stream()
                            .filter(u -> u != null && StringUtils.isNotBlank(u.getValue()))
                            .collect(Collectors.toMap(BasicDictEntity::getId, BasicDictEntity::getValue , (u1 , u2) -> StringUtils.isNotBlank(u1) ? u1 : u2));
                }
            }
            Map<String, String> finalDictValueMaps = dictValueMaps;

            Map<String, BasicCategoryEntity> idBasicCategoryMaps = basicCategoryService.list().stream().collect(Collectors.toMap(BasicCategoryEntity::getId, b -> b));
            Map<String, List<BasicCategoryEntity>> idParentBasicCategoryListMaps = new HashMap<>();
            for(Map.Entry<String, BasicCategoryEntity> idBasicCategoryMap : idBasicCategoryMaps.entrySet()) {
                String key = idBasicCategoryMap.getKey();
                List<BasicCategoryEntity> resultList = new ArrayList<>();
                this.getParentBasicCategory(key, idBasicCategoryMaps, resultList);
                idParentBasicCategoryListMaps.put(key , resultList);
            }
            list.forEach(req -> {
                List<BasicCategoryEntity> categoryList = idParentBasicCategoryListMaps.get(req.getCategoryId());
                if(CollUtil.isNotEmpty(categoryList)) {
                    //一级品类
                    BasicCategoryEntity bestEntity = categoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(bestEntity)) {
                        req.setMainCategory(bestEntity.getName());
                        //二级品类
                        BasicCategoryEntity secondEntity = categoryList.stream().filter(obj -> bestEntity.getId().equals(obj.getPid())).findFirst().orElse(null);
                        if (ObjectUtils.isNotEmpty(secondEntity)) {
                            req.setSecondaryCategory(secondEntity.getName());
                        }
                    }
                }

                if (StringUtils.isNotBlank(req.getChargeId())) {
                    req.setChargeName(Arrays.stream(req.getChargeId().split(",")).filter(StringUtils::isNotBlank)
                            .map(c -> finalChargeIdNameMaps.get(c)).filter(d -> d != null).collect(Collectors.joining(",")));
                }

                if (StringUtils.isNotBlank(req.getProductPropertyId())) {
                    req.setProductProperty(Arrays.stream(req.getProductPropertyId().split(",")).filter(StringUtils::isNotBlank)
                            .map(c -> finalDictValueMaps.get(c)).filter(d -> d != null).collect(Collectors.joining(",")));
                }

                // 销售平台
                if (StrUtils.isNotEmpty(req.getSalesPlatform())) {
                    ProductSalesPlatformEnum salesPlatformEnum = ProductSalesPlatformEnum.getByCode(req.getSalesPlatform());
                    if (salesPlatformEnum != null) {
                        req.setSalesPlatform(salesPlatformEnum.getName());
                    }
                }

                // 一级供应商名称
                if (StrUtils.isNotEmpty(req.getMainSupplier()) && supplierMap.containsKey(req.getMainSupplier())) {
                    req.setMainSupplier(supplierMap.get(req.getMainSupplier()).getName());
                }

                // 二级供应商名称
                if (StrUtils.isNotEmpty(req.getSecondSupplier()) && supplierMap.containsKey(req.getSecondSupplier())) {
                    req.setSecondSupplier(supplierMap.get(req.getSecondSupplier()).getName());
                }

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
                    String userName = finalUserIdNameMaps.get(req.getPurchaseUser());
                    if(StringUtils.isNotBlank(userName)) {
                        req.setPurchaseUser(userName);
                    }
                }
                if (StringUtils.isNotBlank(req.getSaleCountry())) {
                    req.setSaleCountry(Arrays.stream(req.getSaleCountry().split(",")).filter(StringUtils::isNotBlank)
                            .map(c -> finalDictValueMaps.get(c)).filter(d -> d != null).collect(Collectors.joining(",")));
                }
                if(StringUtils.isNotBlank(req.getImageUrl())){
                    String[] imageArr = req.getImageUrl().split(",");
                    req.setImage(FastDFSClientUtil.getFileByte(imageArr[0]));
                }
            });
        }
        return new PagingVO<>(pageData);
    }

    private void getParentBasicCategory(String pid , Map<String, BasicCategoryEntity> idBasicCategoryMaps , List<BasicCategoryEntity> resultList){
        BasicCategoryEntity basicCategoryEntity = idBasicCategoryMaps.get(pid);
        if(basicCategoryEntity != null) {
            resultList.add(basicCategoryEntity);
            if (StringUtils.isNotBlank(basicCategoryEntity.getPid()) && !"0".equals(basicCategoryEntity.getPid())) {
                getParentBasicCategory(basicCategoryEntity.getPid(), idBasicCategoryMaps, resultList);
            }
        }
    }

    @Override
    public List<ProductDetailEntity> getByIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)){
            return Collections.emptyList();
        }
        return baseMapper.selectBatchIds(skuIdList);
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


    /**
     * 重算目的国申报价（定时任务）
     * @param details
     * @param isManual 是否手动计算
     */
    @Override
    public List<BatchResultDTO> resetDestDeclarePrice(List<ProductDetailEntity> details, Boolean isManual) {
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        log.info("resetDestDeclarePrice start======{}",details.size());
        if (CollectionUtils.isEmpty(details)) {
            batchResultDTOList.add(BatchResultDTO.fail("","","重算产品明细为空"));
            return batchResultDTOList;
        }
        List<String> skuIds = details.stream().filter(Objects::nonNull).map(ProductDetailEntity::getId).distinct().collect(Collectors.toList());
        List<String> skuNoList = details.stream().filter(Objects::nonNull).map(ProductDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        if (isEmpty(skuIds)) {
            batchResultDTOList.add(BatchResultDTO.fail("","","重算产品SKU明细不存在"));
            return batchResultDTOList;
        }
        //目的国申报信息
        List<ProductCustomsEntity> customsEntityList = productCustomsService.listBySkuIds(skuIds, CommonConstants.DEFAULT);
        //系统配置
        CfgSettingEntity setting = cfgSettingFeign.getByKey(CfgSettingEnum.LOGISTICS_PRODUCT_DEST_DECLARE_PRICE.getCode());
        if (Objects.isNull(setting) || Objects.isNull(setting.getDataJson()) || CollectionUtils.isEmpty(setting.getDataJson().getJSONArray("data"))) {
            batchResultDTOList.add(BatchResultDTO.fail("","","目的国申报价配置不存在"));
            return batchResultDTOList;
        }
        List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> data = JSONUtil.toList(setting.getDataJson().getJSONArray("data"), CfgSettingValueDTO.LogisticsProductDestDeclarePrice.class);
        log.info("CfgSettingValueDTO: {}", JSONUtil.toJsonStr(data));
        //sku信息
        List<DmpSkuCostEntity> skuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        BigDecimal usdRate = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), CurrencyEnum.USD.getCurrencyCode());
        if (Objects.isNull(usdRate)) {
            batchResultDTOList.add(BatchResultDTO.fail("",String.join(",", skuNoList),"USD兑换CNY汇率不存在"));
            return batchResultDTOList;
        }
        //获取SKU成本
        List<BasicDictEntity> basicDictEntities = basicDictService.listByTypeList(Arrays.asList(SKU_COST_SALE_ORG_ID, SKU_COST_WAREHOUSE));
        String skuCostSaleOrgId = basicDictEntities.stream().filter(e -> e.getType().equals(SKU_COST_SALE_ORG_ID)).map(BasicDictEntity::getValue).findFirst().orElse("");
        String skuCostWarehouseId = basicDictEntities.stream().filter(e -> e.getType().equals(SKU_COST_WAREHOUSE)).map(BasicDictEntity::getValue).findFirst().orElse("");
        InventorySkuCostDTO.QueryB2BDTO queryB2BDTO = InventorySkuCostDTO.QueryB2BDTO.builder().salesOrgId(skuCostSaleOrgId).warehouseId(skuCostWarehouseId).skuIds(skuIds).billDate(LocalDate.now()).build();
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = logisticsFeign.listSkuCostBySkuIds(queryB2BDTO);
        //税率
        List<ProductCostEntity> productCostEntityList = productCostService.listBySkuIds(skuIds);
        List<ProductCustomsEntity> addList = new ArrayList<>();
        List<ProductCustomsEntity> updateList = new ArrayList<>();
        for(String skuId : skuIds){
            ProductDetailEntity productDetailEntity = details.stream().filter(e -> Objects.equals(skuId, e.getId())).findFirst().orElse(new ProductDetailEntity());
            //目的国申报信息 默认
            ProductCustomsEntity customs = customsEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(skuId)).findFirst().orElse(new ProductCustomsEntity());
            log.info("recalDestDeclarePrice : skuId:{},destDeclarePrice:{}", skuId,customs.getToDeclarePrice());
            //是否重算目的国申报价
            BigDecimal destDeclarePrice = Objects.isNull(customs.getToDeclarePrice()) ? BigDecimal.ZERO: customs.getToDeclarePrice();
            if (destDeclarePrice.compareTo(BigDecimal.ZERO) == 0 || isManual) {
                //含税成本 默认是人民币
                BigDecimal actualTaxCost = BigDecimal.ZERO;
                //SKU成本覆盖含税成本
                InventorySkuCostDTO.SkuCostDTO skuCostDTO2 = skuCostDTOS.stream().filter(e -> e.getSkuId().equals(skuId)).findFirst().orElse(null);
                if (Objects.nonNull(skuCostDTO2)){
                    //汇率
                    BigDecimal rate = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO2.getCurrency());
                    //税率
                    BigDecimal taxRate = productCostEntityList.stream().filter(e -> e.getSkuId().equals(skuId)).map(ProductCostEntity::getTaxRate).findFirst().orElse(null);
                    if (Objects.nonNull(rate) && Objects.nonNull(taxRate)){
                        //本位币
                        BigDecimal actualNoTaxCost =  MathUtil.multiplyWithTwo(rate,skuCostDTO2.getProductCost(),4);
                        BigDecimal percentRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
                        actualTaxCost = MathUtil.multiplyWithTwo(actualNoTaxCost, MathUtil.add(BigDecimal.valueOf(1), percentRate));
                    }
                }else {
                    DmpSkuCostEntity skuCostDTO = skuCostList.stream().filter(e -> e.getSkuId().equals(skuId)).findFirst().orElse(null);
                    log.info("skuCostDTO: {}", JSONUtil.toJsonStr(skuCostDTO));
                    if (Objects.isNull(skuCostDTO)) {
                        batchResultDTOList.add(BatchResultDTO.fail(skuId,productDetailEntity.getSkuNo(), format("SKU【{}】中中台Bom关系表不存",productDetailEntity.getSkuNo())));
                        continue;
                    }
                    if (Objects.nonNull(skuCostDTO.getCostPrice())) {
                        actualTaxCost = skuCostDTO.getCostPrice();
                    }
                }

                //统一换算成美元汇率
                BigDecimal actualTaxCostUsd = MathUtil.divide(actualTaxCost, usdRate);
                log.info("actualTaxCostUsd: {}", actualTaxCostUsd);
                //根据美元计算比例
                CfgSettingValueDTO.LogisticsProductDestDeclarePrice declarePrice = data.stream().filter(e -> e.getStartPrice().compareTo(actualTaxCostUsd) < 0 && e.getEndPrice().compareTo(actualTaxCostUsd) >= 0).findFirst().orElse(null);
                log.info("declarePrice: {}", JSONUtil.toJsonStr(declarePrice));
                if (Objects.isNull(declarePrice) || Objects.isNull(declarePrice.getRate())) {
                    batchResultDTOList.add(BatchResultDTO.fail(skuId,productDetailEntity.getSkuNo(),"申报配置汇率不存在"));
                    continue;
                }
                BigDecimal resultDestDeclarePrice = actualTaxCostUsd.multiply(declarePrice.getRate()).divide(MathUtil.BigDecimal_100, 4, RoundingMode.HALF_UP);
                log.info("resultDestDeclarePrice: {}", resultDestDeclarePrice);
                customs.setToDeclarePrice(resultDestDeclarePrice);
                customs.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
                customs.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
                String oldCountry = isBlank(customs.getCountry()) || CommonConstants.DEFAULT.equals(customs.getCountry()) ? "默认" : customs.getCountry();
                String newCountry = "";
                if (Objects.isNull(customs.getId())){
                    customs.setSkuId(skuId);
                    customs.setSkuNo(productDetailEntity.getSkuNo());
                    customs.setImagesUrl(productDetailEntity.getImagesUrl());
                    customs.setCountry(CommonConstants.DEFAULT);
                    newCountry = CommonConstants.DEFAULT;
                    addList.add(customs);
                }else {
                    if (isBlank(customs.getCountry())){
                        customs.setCountry(CommonConstants.DEFAULT);
                    }
                    updateList.add(customs);
                }
                newCountry = isBlank(newCountry) || CommonConstants.DEFAULT.equals(newCountry) ? "默认" : customs.getCountry();
                String msg = "";
                if (isManual){
                    msg = format("手动重算【{}】国家从【{}】改为【{}】，目的国申报价从【{}】改为【{}】", productDetailEntity.getSkuNo(),oldCountry, newCountry,destDeclarePrice, resultDestDeclarePrice);
                }else {
                    msg = format("自动重算【{}】国家从【{}】改为【{}】，目的国申报价从【{}】改为【{}】", productDetailEntity.getSkuNo(),oldCountry, newCountry,destDeclarePrice, resultDestDeclarePrice);
                }
                sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(productDetailEntity.getProductId())
                        .setBusinessId(productDetailEntity.getId()).setOperation("编辑操作").setContent(msg));
                batchResultDTOList.add(BatchResultDTO.success(skuId,productDetailEntity.getSkuNo(),msg));
                log.info("更新目的国申报价 sku:{},目的国申报价：{}",productDetailEntity.getSkuNo(), resultDestDeclarePrice);
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)){
            log.info("updateList: {}", JSONUtil.toJsonStr(updateList));
            productCustomsService.updateBatchById(updateList);
        }
        if (CollectionUtils.isNotEmpty(addList)){
            log.info("addList: {}", JSONUtil.toJsonStr(addList));
            productCustomsService.saveBatch(addList);
        }
        log.info("resetDestDeclarePrice end======");
        return batchResultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initProductCustom(List<String> skuIds) {
        //有传值 按照传值进行sku同步 没有则同步全量
        List<ProductDetailEntity> list = null;
        if (isEmpty(skuIds)){
            list = lambdaQuery().select(ProductDetailEntity::getId).list();
        }else {
            list = lambdaQuery().select(ProductDetailEntity::getId).in(ProductDetailEntity::getId, skuIds).list();
        }
        if (isEmpty(list)){
            log.info("需要同步的sku列表为空：{}", skuIds);
            return;
        }
        log.info("同步的sku列表数量：{}", list.size());
        //当list过大时 切割处理
        if (list.size() > 100){
            List<List<ProductDetailEntity>> partition = ListUtil.partition(list, 100);
            for (List<ProductDetailEntity> list1 : partition){
                customDataProcess(list1);
            }
        }else {
            customDataProcess(list);
        }

    }

    @Override
    public List<SkuVO> listSkuPurchaseBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.listSkuPurchaseBySkuIds(skuIds);
        if (CollUtil.isNotEmpty(skuList)) {
            //供应商信息
            List<String> supplierIdList = skuList.stream().map(SkuVO::getSupplierId).collect(Collectors.toList());
            List<PurchasePriceDTO.SupplierSkuPrice> supplierSkuPriceList = scmTaskFeign.listSupplierSkuPrice(supplierIdList);
            for (SkuVO skuVO : skuList) {
                PurchasePriceDTO.SupplierSkuPrice supplierSkuPrice = supplierSkuPriceList.stream().filter(req -> req.getSupplierId().equals(skuVO.getSupplierId()) && req.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(supplierSkuPrice)) {
                    //含税价
                    skuVO.setActualTaxCost(supplierSkuPrice.getTaxPrice());
                }
            }
        }
        return skuList;

    }
    @Override
    public PagingVO<SkuVO> pagingSelect(PagingDTO<SkuVO.SelectDTO> dto) {
        Page<SkuVO.SelectDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        SkuVO.SelectDTO params = dto.getParams();
        IPage<SkuVO> pagResult = baseMapper.pagingSelect(query, params);
        return new PagingVO<>(pagResult);

    }
    /**
     * 添加已有sku到现有spu
     * @param changeSkuToSpuDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProductDetailEntity> changeSkuBySpu(ChangeSkuToSpuDTO changeSkuToSpuDTO) {
        //检查spu编号是否重复
        if (this.checkSpuNo(changeSkuToSpuDTO.getProductSpuBaseInfoDTO().getSpuNo(), changeSkuToSpuDTO.getProductSpuBaseInfoDTO().getId())) {
            throw new ServiceException(ApiError.ERROR_95017);
        }

        ProductInfoDTO productSpuBaseInfoDTO = changeSkuToSpuDTO.getProductSpuBaseInfoDTO();
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
        String newProductId = productInfoService.updateSpec(productSpuBaseInfoDTO);
        List<String> skuIds = changeSkuToSpuDTO.getSkuIds();
        if (CollectionUtils.isNotEmpty(skuIds)){
            //校验新增SKU是否已入库，有库存SKU不可变更SPU
            InventoryQtyDTO.InventoryBySkuDTO dto = InventoryQtyDTO.InventoryBySkuDTO.builder()
                    .skuIdList(skuIds)
                    .build();
            List<InventoryEntity> inventoryEntities = inventoryFeign.listInventoryBySkuIds(dto);
            List<InventoryEntity> collect = inventoryEntities.stream().filter(e -> Objects.nonNull(e) && e.getQty() > 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)){
                List<String> skuNoList = collect.stream().map(InventoryEntity::getSkuNo).distinct().collect(Collectors.toList());
                throw new ServiceException(ApiError.ERROR_99131, String.join(",",skuNoList));
            }
            //调整sku关联spu
            List<ProductDetailEntity> oldProductDetailEntityList = baseMapper.selectBatchIds(skuIds);
            List<String> oldProductIds = oldProductDetailEntityList.stream().map(ProductDetailEntity::getProductId).distinct().collect(Collectors.toList());
            //校验记录是否存在
            skuIds.forEach(skuId -> {
                ProductDetailEntity oldDetailEntity = oldProductDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(skuId)).findFirst().orElse(null);
                if (Objects.isNull(oldDetailEntity)){
                    throw new ServiceException(ApiError.ERROR_95162, skuId);
                }
            });
            //批量更新产品明细归属spu
            this.lambdaUpdate().in(ProductDetailEntity::getId, skuIds).set(ProductDetailEntity::getProductId, newProductId).update();
            //调整前后校验spu是否存在关联关系，不存在，就删除
            //根据产品id获取sku明细列表
            List<ProductDetailEntity> productDetailEntityList = this.lambdaQuery().select(ProductDetailEntity::getId,ProductDetailEntity::getProductId).in(ProductDetailEntity::getProductId, oldProductIds).list();
            List<String> hasRelationProductIds = productDetailEntityList.stream().filter(Objects::nonNull).map(ProductDetailEntity::getProductId).distinct().collect(Collectors.toList());
            //过滤存在关联记录的spu
            List<String> deleteProductIds = oldProductIds.stream().filter(e -> Objects.nonNull(e) && !hasRelationProductIds.contains(e)).distinct().collect(Collectors.toList());
            productInfoService.removeByIds(deleteProductIds);
        }
        return this.getSkuListByProductId(newProductId);
    }

    @Override
    public List<SkuVO> listApproveAndListingSku() {
        return baseMapper.listApproveAndListingSku();
    }

    @Override
    public List<String> getCategoryByQuerySql(String compareCodeSplicingValueSql) {
        return baseMapper.getCategoryByQuerySql(compareCodeSplicingValueSql);
    }

    @Override
    public List<String> getBrandByQuerySql(String compareCodeSplicingValueSql) {
        return baseMapper.getBrandByQuerySql(compareCodeSplicingValueSql);
    }

    private void customDataProcess(List<ProductDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> skuIds = list.stream().map(ProductDetailEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)){
            return;
        }
        //根据sku获取sku物流信息
        List<ProductLogisticsEntity> logisticsList = productLogisticsService.listBySkuIdList(skuIds);
        //根据sku获取默认目的国申报信息
        List<ProductCustomsEntity> customsList = productCustomsService.listBySkuIds(skuIds, CommonConstants.DEFAULT);
        List<ProductCustomsEntity> addCustomsList = new ArrayList<>();
        List<ProductCustomsEntity> updateCustomsList = new ArrayList<>();
        //查询
        list.forEach(productDetailEntity -> {
            //查询sku product_logistics 目的国申报价不为o 时同步
            ProductLogisticsEntity logistics = logisticsList.stream().filter(e -> Objects.nonNull(e) && StringUtils.isNotEmpty(e.getSkuId())
                    && e.getSkuId().equals(productDetailEntity.getId())).findFirst().orElse(new ProductLogisticsEntity());
            //查询是否存在默认 product_customs 不存在则赋值 新增，存在则更新
            ProductCustomsEntity customs = customsList.stream().filter(e -> Objects.nonNull(e) && StringUtils.isNotEmpty(e.getSkuId())
                    && e.getSkuId().equals(productDetailEntity.getId())).findFirst().orElse(new ProductCustomsEntity());
            if (StringUtils.isEmpty(customs.getId())){
                //新增默认
                addCustomsList.add(new ProductCustomsEntity()
                        .setSkuId(productDetailEntity.getId())
                        .setCountry(CommonConstants.DEFAULT)
                        .setToDeclarePrice(logistics.getDestDeclarePrice())
                        .setToCurrency(logistics.getDestCurrency())
                        .setToCurrencySymbol(logistics.getDestCurrencySymbol()));
            }else if (Objects.nonNull(logistics.getDestDeclarePrice()) && logistics.getDestDeclarePrice().compareTo(BigDecimal.ZERO) != 0){
                customs.setToCurrency(logistics.getDestCurrency());
                customs.setToDeclarePrice(logistics.getDestDeclarePrice());
                customs.setToCurrencySymbol(logistics.getDestCurrencySymbol());
                customs.setCountry(CommonConstants.DEFAULT);
                updateCustomsList.add(customs);
            }
        });
        //批量操作
        if (CollUtil.isNotEmpty(addCustomsList)){
            productCustomsService.saveBatch(addCustomsList);
        }
        if (CollUtil.isNotEmpty(updateCustomsList)){
            productCustomsService.updateBatchById(updateCustomsList);
        }
    }

    @Override
    public void initProductToWangDian(List<String> ids) {
        int count = productInfoService.count();
        int pageSize = 50;
        int pageCount = count / pageSize + 1;
        for (int i = 0; i < pageCount; i++) {
            Page<ProductInfoEntity> page = productInfoService.page(new Page<>(i, pageSize), Wrappers.<ProductInfoEntity>lambdaQuery().in(CollUtil.isNotEmpty(ids), ProductInfoEntity::getId, ids));
            List<ProductInfoEntity> records = page.getRecords();
            if (CollectionUtils.isEmpty(records)) {
                return;
            }
            List<String> infoIds = records.stream().map(ProductInfoEntity::getId).collect(Collectors.toList());
            List<ProductDetailEntity> detailEntities = list(Wrappers.<ProductDetailEntity>lambdaQuery()
                    .eq(ProductDetailEntity::getStatus, 2)
                    .in(ProductDetailEntity::getProductId, infoIds));
            for (ProductDetailEntity entity : detailEntities) {
                RateLimiter limiter = RateLimiter.create(60, 1, TimeUnit.MINUTES);
                if (limiter.tryAcquire()) {
                    try {
                        syncWangDianProductDetailService.syncDataToWangDian(entity);
                    } catch (Exception e) {
                        log.error("推送旺店通失败:{}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Override
    public void initProductToLingXing(List<String> ids) {
        int count = productInfoService.count();
        int pageSize = 50;
        int pageCount = count / pageSize + 1;
        for (int i = 0; i < pageCount; i++) {
            Page<ProductInfoEntity> page = productInfoService.page(new Page<>(i, pageSize), Wrappers.<ProductInfoEntity>lambdaQuery().in(CollUtil.isNotEmpty(ids), ProductInfoEntity::getId, ids));
            List<ProductInfoEntity> records = page.getRecords();
            if (CollectionUtils.isEmpty(records)) {
                return;
            }
            List<String> infoIds = records.stream().map(ProductInfoEntity::getId).collect(Collectors.toList());
            List<ProductDetailEntity> detailEntities = list(Wrappers.<ProductDetailEntity>lambdaQuery()
                    .eq(ProductDetailEntity::getStatus, 2)
                    .in(ProductDetailEntity::getProductId, infoIds));
            for (ProductDetailEntity entity : detailEntities) {
                RateLimiter limiter = RateLimiter.create(60, 1, TimeUnit.MINUTES);
                if (limiter.tryAcquire()) {
                    try {
                        syncLingXingProductDetailService.syncDataToLingxing(entity);
                    } catch (Exception e) {
                        log.error("推送领星失败:{}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Override
    public Boolean updateApprover(ProductDetailApproveParamDTO dto) {
        ProductDetailApproverEntity entity = new ProductDetailApproverEntity();
        BeanMapperUtils.copy(dto, entity);
        LoginUser loginUser = UserContext.getLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        entity.setCreateUserId(userId);
        entity.setCreateUserName(userName);
        return productDetailApproverService.saveOrUpdate(entity);
    }


    @Override
    @Transactional
    public Boolean applyChange(String id) {
        ProductDetailEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //验证sku是否审核
        if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95086);
        }
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        LambdaUpdateWrapper<ProductDetailEntity> updateWrapper = new LambdaUpdateWrapper<>();
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
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        Integer skuStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        List<SkuVO> skuList = baseMapper.getSkuBySkuNos(skuNoList, skuStatus);
        List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        for (SkuVO skuVO : skuList) {
            if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(null);
                skuVO.setActualTaxCost(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getCostPrice() : skuVO.getActualTaxCost());
                skuVO.setNotTaxCostPrice(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getNotTaxCostPrice() : skuVO.getNotTaxCostPrice());
            }
        }
        return skuList;
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


    public void checkRequiredField(List<String> ids, Boolean isCheck) {
        if (CollectionUtils.isEmpty(ids) || !isCheck) {
            return;
        }
        List<ProductDetailEntity> detailEntityList = this.listByIds(ids);

        //校验字段是否必填
        checkApproveField(detailEntityList);

        for (ProductDetailEntity entity : detailEntityList) {
            ProductInfoEntity productInfoEntity = productInfoService.getById(entity.getProductId());
            if (StringUtils.isBlank(productInfoEntity.getChargeId())) {
                throw new ServiceException(ApiError.ERROR_95200);
            }
            if (StringUtils.isBlank(productInfoEntity.getSaleMethod())) {
                throw new ServiceException(ApiError.ERROR_95201);
            }
            if (StringUtils.isBlank(productInfoEntity.getCategoryId())) {
                throw new ServiceException(ApiError.ERROR_95202);
            }
            if (StringUtils.isBlank(productInfoEntity.getBrandId())) {
                throw new ServiceException(ApiError.ERROR_95203);
            }
            if (StringUtils.isBlank(productInfoEntity.getGradeId())) {
                throw new ServiceException(ApiError.ERROR_95204);
            }
            if (StringUtils.isBlank(productInfoEntity.getName())) {
                throw new ServiceException(ApiError.ERROR_95206);
            }
            if (StringUtils.isBlank(productInfoEntity.getNameEn())) {
                throw new ServiceException(ApiError.ERROR_95207);
            }
            if (StringUtils.isBlank(productInfoEntity.getPropertyId())) {
                throw new ServiceException(ApiError.ERROR_95208);
            }
            if (StringUtils.isBlank(productInfoEntity.getSalesChannel())) {
                throw new ServiceException(ApiError.ERROR_95209);
            }
            if (productInfoEntity.getMoldCost() == null) {
                throw new ServiceException(ApiError.ERROR_95210);
            }
            if (productInfoEntity.getEntrustedDevelopCost() == null) {
                throw new ServiceException(ApiError.ERROR_95211);
            }

            if (StringUtils.isBlank(entity.getSkuNo())) {
                throw new ServiceException(ApiError.ERROR_95198);
            }
            if (StringUtils.isBlank(entity.getName())) {
                throw new ServiceException(ApiError.ERROR_95212);
            }
            if (StringUtils.isBlank(entity.getNameEn())) {
                throw new ServiceException(ApiError.ERROR_95213);
            }
            if (StringUtils.isBlank(entity.getChargeId())) {
                throw new ServiceException(ApiError.ERROR_95214);
            }
            if (entity.getProductState() == null) {
                throw new ServiceException(ApiError.ERROR_95215);
            }


            ProductCostEntity costEntity = productCostService.getBySkuId(entity.getId());
            if (ObjectUtils.isEmpty(costEntity)) {
                throw new ServiceException(ApiError.ERROR_95239);
            }
            if (costEntity.getProjectApprovalCost() == null) {
                throw new ServiceException(ApiError.ERROR_95216);
            }
            if (costEntity.getProjectCost() == null) {
                throw new ServiceException(ApiError.ERROR_95217);
            }
            if (costEntity.getTargetTaxCost() == null) {
                throw new ServiceException(ApiError.ERROR_95218);
            }
            if (costEntity.getRetailPrice() == null) {
                throw new ServiceException(ApiError.ERROR_95219);
            }
            if (costEntity.getMassCost() == null) {
                throw new ServiceException(ApiError.ERROR_95221);
            }
            if (costEntity.getTaxRate() == null) {
                throw new ServiceException(ApiError.ERROR_95222);
            }
            if (costEntity.getTargetNoTaxCost() == null) {
                throw new ServiceException(ApiError.ERROR_95223);
            }

            ProductSaleEntity saleEntity = productSaleService.getBySkuId(entity.getId());
            if (ObjectUtils.isEmpty(saleEntity)) {
                throw new ServiceException(ApiError.ERROR_95240);
            }
            if (saleEntity.getYearSaleQty() == null) {
                throw new ServiceException(ApiError.ERROR_95226);
            }
            if (saleEntity.getMonthSaleQty() == null) {
                throw new ServiceException(ApiError.ERROR_95227);
            }
            if (saleEntity.getTargetSalesQty() == null) {
                throw new ServiceException(ApiError.ERROR_95228);
            }
            if (saleEntity.getIsFinishedImg() == null) {
                throw new ServiceException(ApiError.ERROR_95229);
            }
            if (saleEntity.getSaleState() == null) {
                throw new ServiceException(ApiError.ERROR_95230);
            }
            if (saleEntity.getIsMarketable() == null) {
                throw new ServiceException(ApiError.ERROR_95231);
            }
            if (saleEntity.getYearSaleAmount() == null) {
                throw new ServiceException(ApiError.ERROR_95232);
            }
            if (saleEntity.getMonthSaleAmount() == null) {
                throw new ServiceException(ApiError.ERROR_95233);
            }
            if (StringUtils.isBlank(saleEntity.getSaleCountry())) {
                throw new ServiceException(ApiError.ERROR_95234);
            }
            if (saleEntity.getIsFinishedImg() == null) {
                throw new ServiceException(ApiError.ERROR_95235);
            }
            if (StringUtils.isBlank(saleEntity.getSalesPlatform())) {
                throw new ServiceException(ApiError.ERROR_95236);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendKingDeeData(String id) {
        ProductDetailEntity productDetailEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productDetailEntity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95126);
        }

        //发送金蝶
        sendPushTask(Arrays.asList(productDetailEntity),SyncOperateEnum.OPERATE_APPROVE.getCode());
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
            for (String chargeName : chargeNames) {
                String chargId = userList.stream().filter(e -> e.getUserName().equals(chargeName)).map(FindUserDTO::getUserId).findFirst().orElse("");
                if (!chargIds.contains(chargId)) {
                    flag = true;
                }
                chargIdList.add(chargId);
            }
            if (flag) {
                if (CollectionUtils.isNotEmpty(chargIdList)) {
                    obj.setChargeId(String.join(",", chargIdList));
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
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        Boolean flag = this.lambdaUpdate()
                .eq(ProductDetailEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), ProductDetailEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
        return flag;
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
        List<SkuVO> skuList = baseMapper.getSkuInfoBySkuIds(skuIds);
        if (CollUtil.isNotEmpty(skuList)) {
            List<ProductPackEntity> productPackList = productPackService.findBySkuIds(skuIds);
            Map<String, List<ProductPackEntity>> productPackMap = Maps.newHashMap();
            if (CollUtil.isNotEmpty(productPackList)) {
                productPackMap = productPackList.stream().collect(Collectors.groupingBy(ProductPackEntity::getSkuId));
            }
            //产品分类
            List<String> categoryIdList = skuList.stream().map(SkuVO::getCategoryId).distinct().collect(Collectors.toList());
            List<BasicCategoryEntity> basicCategoryList = basicCategoryService.listByIds(categoryIdList);

            List<String> skuNoList = skuList.stream().map(SkuVO::getSkuNo).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());
            List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);

            for (SkuVO skuVO : skuList) {
                if (productPackMap.containsKey(skuVO.getSkuId()) && CollUtil.isNotEmpty(productPackMap.get(skuVO.getSkuId()))) {
                    ProductPackEntity packEntity = productPackMap.get(skuVO.getSkuId()).get(0);
                    skuVO.setUnitQty(Objects.nonNull(packEntity.getBoxQty()) ? packEntity.getBoxQty().intValue() : null);
                }
                if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                    DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(null);
                    skuVO.setActualTaxCost(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getCostPrice() : skuVO.getActualTaxCost());
                    skuVO.setNotTaxCostPrice(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getNotTaxCostPrice() : skuVO.getNotTaxCostPrice());
                }
                //产品分类
                String categoryName = basicCategoryList.stream().filter(obj -> obj.getId().equals(skuVO.getCategoryId())).map(BasicCategoryEntity::getName).findFirst().orElse("");
                skuVO.setCategoryName(categoryName);
            }
        }
        return skuList;

    }

    @Override
    public List<SkuVO> searchSkuInfo(ProductDetailDTO.SearchDTO dto) {
        return baseMapper.searchSku(dto.getSearchKeyword(), dto.getStatus());
    }


    private String checkRequiredData(ProductDetailEntity productDetailEntity) {
        StringBuilder str = new StringBuilder();
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

    private String checkRequiredDataList(List<ProductDetailEntity> productDetailEntityList) {
        StringBuilder str = new StringBuilder();
        productDetailEntityList.forEach(req -> {
            if (StringUtils.isBlank(req.getSkuNo())) {
                str.append("[" + req.getName() + "] sku编号不能为空");
            }
            if (StringUtils.isBlank(req.getChargeId())) {
                str.append("[" + req.getSkuNo() + "]产品经理不能为空");
            }
            if (StringUtils.isBlank(req.getName())) {
                str.append("[" + req.getSkuNo() + "]产品名称(品名)不能为空");
            }
            if (ObjectUtils.isEmpty(req.getProductState())) {
                str.append("[" + req.getSkuNo() + "]产品开发状态不能为空");
            }
        });
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
        //国家列表
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        String productId = productDetail.getProductId();
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByProductId(productId);
        //任务与配置字段 关系
        List<TaskRefSkuConfigEntity> refSkuFiledConfigList = taskRefSkuConfigService.getDisableFieldByProductId(productId);

        List<TaskRefSkuConfigEntity> skuFiledConfigList = getSkuFiledConfigList(taskRefSkuList, skuId, refSkuFiledConfigList);
        //基础信息 禁用字段
        List<String> disableFields = getByFileldFlag(ProductManyDetailConstant.PRODUCT_MANY_SKU_DETAIL_LIST, skuFiledConfigList);
        productDetail.setDisableFieldList(disableFields);


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
            productDetail.setEan(purchaseShowDTO.getEan());
        }
        result.setProductManySkuDetail(productDetail);
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
                List<String> countryIdList = Arrays.asList(split);
                List<DictCountryDTO.ListDTO> dictCountryList = countryList.stream().filter(c -> countryIdList.contains(c.getId())).collect(Collectors.toList());
                String countryName = dictCountryList.stream().map(DictCountryDTO.ListDTO::getNameCn).collect(Collectors.joining(","));
                saleShowDTO.setSaleCountryName(countryName);
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
            logisticsShowDTOList.forEach(req -> {
                if (StringUtils.isNotBlank(req.getProductPropertyId())) {
                    String[] split = req.getProductPropertyId().split(",");
                    List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
                    List<String> countryNameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                    req.setProductProperty(StringUtils.join(countryNameList, ","));
                }
            });
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

        //查询目的国海关编码
        List<ProductCustomsEntity> productCustomsEntityList = productCustomsService.listByProductId(productId);
        List<ProductCustomsEntity> productCUstomsList = productCustomsEntityList.stream().filter(e -> e.getSkuId().equals(skuId))
                .collect(Collectors.toList());
        productCUstomsList.forEach(req -> {
            if (StringUtils.isNotBlank(req.getCountry())) {
                String[] split = req.getCountry().split(",");
                List<String> countryIdList = Arrays.asList(split);
                List<DictCountryDTO.ListDTO> dictCountryList = countryList.stream().filter(c->countryIdList.contains(c.getId())).collect(Collectors.toList());
                String countryName = dictCountryList.stream().map(DictCountryDTO.ListDTO::getNameCn).collect(Collectors.joining(","));
                req.setCountryName(countryName);
            }
        });
        result.setProductCustomsList(productCUstomsList);

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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
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
            productInfoDTO.setNameEn(skuDTO.getProductManySkuDetail().getNameEn());
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
            List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList = BeanMapperUtils.copyList(ProductCertificateDTO.ProductAddOrUpdateDTO.class, productCertificateShowList);
            productCertificateService.productAddOrUpdate(productCertificateList);
        }

        //10 修改/新增加 包装辅料信息

        List<ProductAccessoriesDTO> productAccessoriesList = skuDTO.getProductAccessoriesList();
        if (CollectionUtils.isNotEmpty(productAccessoriesList)) {
            //
            addProductAccessoriesLog(productAccessoriesList, id);
            productAccessoriesService.saveOrUpdateBatchAccessories(productAccessoriesList);
        }

        //11.修改/新增  目的国海关编码信息
        List<ProductCustomsEntity> productCustomsDTO = skuDTO.getProductCustomsList();
        List<ProductCustomsEntity> productCustomsEntityList = productCustomsService.listByProductId(id);
        List<String> deleteIds = getDeleteIds(productCustomsDTO, productCustomsEntityList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            productCustomsService.removeByIds(deleteIds);
        }
        if (CollectionUtils.isNotEmpty(productCustomsDTO)) {
            List<ProductCustomsEntity> customsEntityList = new ArrayList<>();
            for (ProductCustomsEntity customsDTO : productCustomsDTO) {
                customsEntityList.add(customsDTO);
            }
            List<ProductCustomsDTO> productCustomsDTOS = BeanMapper.copyList(productCustomsDTO, ProductCustomsDTO.class);
            addProductCustomsLog(productCustomsDTOS, id);
            productCustomsService.saveOrUpdateBatch(customsEntityList);
        }

        //发送金蝶
        sendPushTask(Arrays.asList(detailEntity),SyncOperateEnum.OPERATE_APPROVE.getCode());
    }


    private List<String> getDeleteIds(List<ProductCustomsEntity> customsEntityList, List<ProductCustomsEntity> dbList) {
        List<String> ids = customsEntityList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(ProductCustomsEntity::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(ProductCustomsEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
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
        Boolean flag = this.update(updateWrapper);
        return flag;
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
     * 获取所有明细信息包括删除，用来同步到DMP
     *
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     * @Author Luo_WG
     * @Date 2023/4/19 16:12
     **/
    public List<ProductDetailEntity> getProductDetailAll() {
        return baseMapper.getProductDetailAll();
    }

    /**
     * 更改产品状态
     *
     * @param productIds
     * @param
     * @return void
     * @author yl
     * @date 2023-06-14 11:12
     */
    @Override
    public void updateProductStateByProductIdList(List<String> productIds, Integer productState) {
        if (CollectionUtils.isNotEmpty(productIds)) {
            this.lambdaUpdate().in(ProductDetailEntity::getProductId, productIds).
                    set(ProductDetailEntity::getProductState, productState).update();
        }
    }


    /**
     * 根据产品id 集合获取数据
     *
     * @param productIdList
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     * @author yl
     * @date 2023-06-14 18:33
     */
    @Override
    public List<ProductDetailEntity> listSkuByProductIds(List<String> productIdList) {
        if (CollectionUtils.isEmpty(productIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ProductDetailEntity::getProductId, productIdList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id, Boolean isStartProcess) {
        //校验必填项
        checkRequiredField(Collections.singletonList(id), isStartProcess);
        ProductDetailEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        if (!ProductDetailStatusEnum.WAIT_COMMIT.getCode().equals(entity.getStatus()) && !ProductDetailStatusEnum.APPROVAL_NO_PASS.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95117);
        }
        //验证必填信息
        String str = checkRequiredDataList(Collections.singletonList(entity));
        if (StringUtils.isNotBlank(str)) {
            throw new ServiceException(new ApiResult<>(1, str));
        }
        List<ProjectTaskEntity> taskAllList = projectTaskService.listByProductIds(Collections.singletonList(entity.getProductId()));
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
                    List<ProjectTaskEntity> relatedTaskList = taskAllList.stream().filter(obj -> !RelatedSkuTypeEnum.NOT_RELATED.getCode().equals(obj.getRelatedSkuType()) && !TaskStateEnum.FINISH.getCode().equals(obj.getStatus()) && configTaskIds.contains(obj.getId())).collect(Collectors.toList());
                    if (relatedTaskList.size() > 0) {
                        String warning = ApiError.ERROR_801.msg;
                        String taskNames = relatedTaskList.stream().map(ProjectTaskEntity::getName).collect(Collectors.joining(","));
                        String warningMsg = String.format(warning, taskNames);
                        throw new ServiceException(ApiError.ERROR_801.code, warningMsg);
                    }
                }
            }
        }
        //提交流程
        if (isStartProcess) {
            startProcess(entity);
        }
        entity.setStatus(ProductDetailStatusEnum.APPROVAL_ING.getCode());
        Boolean result = this.updateById(entity);
        if (!result) {
          throw new ServiceException(ApiError.ERROR_1042,"产品信息");
        }
        //操作日志
        String operateContent = String.format(BomOperateContent.STATE_CHANGE, ProductDetailStatusEnum.WAIT_COMMIT.getName(), ProductDetailStatusEnum.APPROVAL_ING.getName());
        sysLogService.addSysLogBySave(operateContent,BomOperationTypeEnum.STATE_CHANGE.getType(),entity.getId(),entity.getProductId());
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.SUBMIT);
    }

    /**
     * 启动流程
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/
    public void startProcess(ProductDetailEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getSkuNo());
        startDTO.setBusinessKey(SourceTypeEnum.PRODUCT_DETAIL.getCode());
        startDTO.setBusinessName(entity.getSkuNo());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ApproveOneDTO dto,Boolean isPushWdt) {
        ProductDetailEntity entity = this.getById(dto.getId());
        if (!entity.getStatus().equals(ProductDetailStatusEnum.APPROVAL_ING.getCode())) {
            return BatchResultDTO.fail(entity.getId(),entity.getSkuNo(),ApiError.ERROR_95038.msg);
        }
        // 调用流程审核
        entity.setIsPushWdt(isPushWdt);
        approveProcess(entity, dto);
        //操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                .setBusinessId(entity.getId()).setOperation("状态变更").setContent("审核SKU[" + entity.getSkuNo() + "],操作[" + ProductDetailStatusEnum.getName(entity.getStatus()) + "]为[" + ApproveTypeEnum.getName(dto.getType()) + "]，审批意见：" + dto.getComment()));
        //增加缓存清除
        redisUtil.hdel(RedisKeyConstant.LIST_SKU_INFO, entity.getId());
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "操作成功");
    }

    /**
     * 审核流程处理
     * @param entity
     * @param dto
     */
    private void approveProcess(ProductDetailEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PRODUCT_DETAIL.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            dto.setVariablesMap(BeanUtil.beanToMap(entity));
            approveEnd(dto, entity);
        }
    }


    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(ProductDetailEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);

        ProductInfoEntity productInfoEntity = productInfoService.getById(entity.getProductId());
        if (ObjectUtil.isEmpty(productInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        Map<String, Object> productMap = BeanUtil.beanToMap(productInfoEntity);
        variablesMap.putAll(productMap);
        return variablesMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, ProductDetailEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        if(entity.getEnableTime() != null) {
            entity.setEnableTime(LocalDateTime.now());
        }
        Integer approveStatus;
        if (dto.getType().equals(ApproveTypeEnum.PASS.getStatus())) {
            //审核通过
            approveStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        } else if (dto.getType().equals(ApproveTypeEnum.CANCEL.getStatus())){
            //待提交
            approveStatus = ProductDetailStatusEnum.WAIT_COMMIT.getCode();
        } else {
            //审核不通过
            approveStatus = ProductDetailStatusEnum.APPROVAL_NO_PASS.getCode();
        }
        //更新单据状态
        updateApproveStatusForApprove(entity.getId(), approveStatus);

        //审核通过
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过 重算目的国申报单价
            resetDestDeclarePrice(Collections.singletonList(entity), Boolean.FALSE);
            //发送通知
            noticeMessageService.approveProductNotice(UserContext.getLoginUser().getUserName(), entity);
            //发送金蝶
            sendSinglePushTask(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            //取出传入流程的数据
            ProductDetailEntity bean = BeanUtil.toBean(dto.getVariablesMap(), ProductDetailEntity.class);
            if (bean.getIsPushWdt()) {
                syncWangDianProductDetailService.syncDataToWangDian(entity);
            }
            syncLingXingProductDetailService.syncDataToLingxing(entity);
        } else {
            //新增审核不通过意见
            ProductDetailCommentEntity commentEntity = new ProductDetailCommentEntity();
            commentEntity.setComment("[审核结果-审核不通过]" + dto.getComment());
            commentEntity.setProductDetailId(entity.getId());
            productDetailCommentService.save(commentEntity);
        }
        return Boolean.TRUE;
    }

    /**
     * 更新审核状态
     * @author will
     * @date 2025/5/16 12:10
     * @param id
     * @param approveStatus
     * @return Boolean
     */
    private Boolean updateApproveStatusForApprove (String id,Integer approveStatus) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //更新sku记录
        return lambdaUpdate().set(ProductDetailEntity::getStatus, approveStatus)
                .set(ProductDetailEntity::getUpdateUserId, userInfo.getUid())
                .set(ProductDetailEntity::getUpdateUserName, userInfo.getUserName())
                .eq(ProductDetailEntity::getId, id)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(ProductDetailEntity entity) {
        //已审核支持反审核
        if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getSkuNo(),ApiError.ERROR_99003.msg);
        }
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                .setBusinessId(entity.getId()).setOperation("状态变更").setContent("反审核SKU[" + entity.getSkuNo() + "],操作[" + ProductDetailStatusEnum.getName(entity.getStatus()) + "]为[" + ProductDetailStatusEnum.APPROVAL_ING.getName() + "]"));
        //修改状态为审核中
        lambdaUpdate().set(ProductDetailEntity::getStatus, ProductDetailStatusEnum.WAIT_COMMIT.getCode())
                .in(ProductDetailEntity::getIsChange, IsConstant.NO)
                .eq(ProductDetailEntity::getId, entity.getId())
                .update();
        //发送金蝶
        sendSinglePushTask(entity,SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id) {
        ProductDetailEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getStatus(), ProductDetailStatusEnum.APPROVAL_ING.getCode())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        updateApproveStatusForApprove(id, ProductDetailStatusEnum.WAIT_COMMIT.getCode());
        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                .setBusinessId(entity.getId()).setOperation("状态变更").setContent("取消流程SKU[" + entity.getSkuNo() + "],操作[" + ProductDetailStatusEnum.getName(entity.getStatus()) + "]为[" + ProductDetailStatusEnum.WAIT_COMMIT.getName() + "]"));
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PRODUCT_DETAIL.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean deleteBatch(List<String> ids) {
        List<ProductDetailEntity> entityListt = this.listByIds(ids);
        long count = entityListt.stream().filter(req -> !req.getStatus().equals(1) && !req.getStatus().equals(2)).count();
        if (entityListt.size() != count) {
            throw new ServiceException(ApiError.ERROR_95241);
        }
        long sign = entityListt.stream().filter(req -> !req.getOccupyStatus()).count();
        if (entityListt.size() != sign) {
            throw new ServiceException(ApiError.ERROR_95242);
        }
        //发送金蝶
        sendPushTask(entityListt,SyncOperateEnum.OPERATE_DELETE.getCode());

        //1.删除证书信息
        productCertificateService.deleteBySkuIdList(ids);
        //2.删除包装信息
        productPackService.removePack(ids);
        //3.删除物流信息
        productLogisticsService.removeLogistics(ids);
        //4.删除销售信息
        productSaleService.removeSale(ids);
        //5.删除采购信息
        productPurchaseService.removePurchase(ids);
        //6.删除成本信息
        productCostService.removeCost(ids);
        //7.删除任务关联sku 信息
        taskRefSkuConfigService.removeTaskRefSku(ids);
        //8.删除sku信息
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductDetailEntity::getId, ids);
        List<ProductDetailEntity> productDetailEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(productDetailEntityList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        for (ProductDetailEntity productDetailEntity : productDetailEntityList) {
            ProductInfoEntity productInfoEntity = productInfoService.getById(productDetailEntity.getProductId());
            List<ProductDetailEntity> skuListByProductId = this.getSkuListByProductId(productDetailEntity.getProductId());
            if (ObjectUtils.isNotEmpty(productInfoEntity)) {
                if (skuListByProductId.size() == 1 && productInfoEntity.getIsFinishedProductDev() == null) {
                    ProductInfoEntity infoEntity = productInfoService.getById(productInfoEntity.getId());
                    infoEntity.setIsDeleted(Boolean.TRUE);
                    productInfoService.removeById(productInfoEntity.getId());
                }
                //添加操作日志
                SysLogEntity sysLogEntity = new SysLogEntity().setClassPath(SPUCLASSPATH).setBusinessId(productInfoEntity.getId()).setPid(productInfoEntity.getId()).setOperation("删除信息").setContent("删除了一个SKU：[" + productDetailEntity.getSkuNo() + "]");
                sysLogService.addSysLogByOther(sysLogEntity);
            }
        }
        return this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBatchFiled(ProductDetailBatchUpdateDTO dto) {
        dto.setUpdateFiledCode(toUnderlineCase(dto.getUpdateFiledCode()));
        List<ProductDetailEntity> entityList = this.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        Boolean flag = Boolean.TRUE;
        //如果是产品经理需要查询name
        if (ProductBatchFieldEnum.CHARGE_ID.getCode().equals(dto.getUpdateFiledCode())
                || ProductBatchFieldEnum.SALE_METHOD.getCode().equals(dto.getUpdateFiledCode())
                || ProductBatchFieldEnum.MATERIALS.getCode().equals(dto.getUpdateFiledCode())) {
            if (ObjectUtils.isEmpty(dto.getValues())) {
                throw new ServiceException(ApiError.ERROR_9030);
            }
            List<String> productIds = entityList.stream().map(ProductDetailEntity::getProductId).distinct().collect(Collectors.toList());
            if (ProductBatchFieldEnum.CHARGE_ID.getCode().equals(dto.getUpdateFiledCode())) {
                ProductInfoEntity productInfoEntity = productInfoService.getById(productIds.get(0));
                String chargeName = commonService.getNameByIds(Arrays.asList(dto.getValues().toString().split(",")));
                //获取产品基本信息修改的字段
                ProductInfoDTO productInfoDTO = new ProductInfoDTO();
                BeanMapper.copy(productInfoEntity,productInfoDTO);
                productInfoDTO.setChargeId(String.valueOf(dto.getValues()));
                productInfoDTO.setChargeName(chargeName);
                List<ProductDetailDTO.SkuChangeInfoDTO> productBasicChangeField = getProductBasicChangeField(productInfoDTO,productInfoEntity);
                //消息推送
                List<ProductDetailDTO.NoticeDTO> noticeDTOList = new ArrayList<>();
                for (ProductDetailEntity productDetailEntity : entityList) {
                    //发送通知
                    ProductDetailDTO.NoticeDTO noticeDTO = new ProductDetailDTO.NoticeDTO();
                    noticeDTO.setProductId(productInfoDTO.getId());
                    noticeDTO.setName(productInfoDTO.getName());
                    noticeDTO.setChargeId(productInfoDTO.getChargeId());
                    noticeDTO.setChargeName(productInfoDTO.getChargeName());
                    noticeDTO.setSkuNo(productDetailEntity.getSkuNo());
                    noticeDTO.setProductBasicChangeField(productBasicChangeField);
                    noticeDTOList.add(noticeDTO);
                }

                productInfoService.lambdaUpdate()
                        .set(ProductInfoEntity::getChargeId, dto.getValues())
                        .set(ProductInfoEntity::getChargeName, chargeName).in(ProductInfoEntity::getId, productIds)
                        .update();
                this.lambdaUpdate()
                        .set(ProductDetailEntity::getChargeId, dto.getValues())
                        .set(ProductDetailEntity::getChargeName, chargeName).in(ProductDetailEntity::getId, dto.getIds())
                        .update();

                //发送消息
                handleProductChangeNotification(noticeDTOList,Boolean.TRUE);

            }
            if (ProductBatchFieldEnum.SALE_METHOD.getCode().equals(dto.getUpdateFiledCode())) {
                productInfoService.lambdaUpdate()
                        .set(ProductInfoEntity::getSaleMethod, dto.getValues())
                        .in(ProductInfoEntity::getId, productIds)
                        .update();
            }
            if (ProductBatchFieldEnum.MATERIALS.getCode().equals(dto.getUpdateFiledCode())) {
                productInfoService.lambdaUpdate()
                        .set(ProductInfoEntity::getMaterials, dto.getValues())
                        .in(ProductInfoEntity::getId, productIds)
                        .update();
            }
        } else {
            if (ProductBatchFieldEnum.DECLARE_PRICE.getCode().equals(dto.getUpdateFiledCode())) {
                dto.setValues(BigDecimal.valueOf(Double.valueOf(dto.getValues().toString())));
            }
            if (ProductBatchFieldEnum.SALE_STATE.getCode().equals(dto.getUpdateFiledCode())) {
                dto.setValues(Integer.valueOf(dto.getValues().toString()));
            }
            if (ProductBatchFieldEnum.PRODUCT_STATE.getCode().equals(dto.getUpdateFiledCode())) {
                dto.setValues(Integer.valueOf(dto.getValues().toString()));
            }
            if (ProductBatchFieldEnum.IS_MARKETABLE.getCode().equals(dto.getUpdateFiledCode())) {
                dto.setValues(Integer.valueOf(dto.getValues().toString()));
            }
            if (ProductBatchFieldEnum.GROSS_WEIGHT.getCode().equals(dto.getUpdateFiledCode())) {
                dto.setValues(MathUtil.valueOf(dto.getValues()));
            }
            if (ProductBatchFieldEnum.INSURANCE_PROPERTY.getCode().equals(dto.getUpdateFiledCode())) {
                List<String> values = (List<String>) dto.getValues();
                dto.setValues(String.join(",",values));
            }
            ProductBatchFieldEnum enumByCode = ProductBatchFieldEnum.getEnumByCode(dto.getUpdateFiledCode());
            if (enumByCode == null) {
                throw new ServiceException(ApiError.ERROR_9046, dto.getUpdateFiledCode());
            }
            flag = baseMapper.updateFiledBatch(dto.getIds(), enumByCode.getTableName(), enumByCode.getCode() , dto.getValues(), enumByCode.getKeyName());
        }
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_95243);
        }
        productLogisticsService.saveOrUpdateParentPropertyIdByChildSkuId(dto.getIds());

        //添加日志
        String fieldValue = getFieldValue(dto);
        entityList.forEach(obj -> {
            String content = format("操作了【{}】，修改字段【{}】为【{}】",obj.getSkuNo(),ProductBatchFieldEnum.getName(dto.getUpdateFiledCode()),fieldValue);
            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(obj.getProductId())
                    .setBusinessId(obj.getId()).setOperation("批量更新").setContent(content));
        });
        return flag;
    }

    /**
     * 字段值
     * @param dto
     * @return
     */
    private String getFieldValue (ProductDetailBatchUpdateDTO dto) {
        if (ProductBatchFieldEnum.PRODUCT_STATE.getCode().equals(dto.getUpdateFiledCode())) {
            return ProductDetailStateEnum.getNameByCode(Integer.valueOf(dto.getValues().toString()));
        }
        if (ProductBatchFieldEnum.SALE_STATE.getCode().equals(dto.getUpdateFiledCode())) {
            return SaleStateEnum.getNameByCode(Integer.valueOf(dto.getValues().toString()));
        }
        if (ProductBatchFieldEnum.IS_MARKETABLE.getCode().equals(dto.getUpdateFiledCode())) {
            return MathUtil.compareTo(MathUtil.ONE,Integer.valueOf(dto.getValues().toString())) == MathUtil.ZERO ? "是":"否";
        }
        if (ProductBatchFieldEnum.PRODUCT_PROPERTY_ID.getCode().equals(dto.getUpdateFiledCode())) {
            List<BasicDictEntity> basicDictList = basicDictService.listByIds(Arrays.asList(dto.getValues().toString().split(",")));
            return isEmpty(basicDictList) ? "" :basicDictList.stream().map(BasicDictEntity::getName).collect(Collectors.joining(","));
        }
        if (ProductBatchFieldEnum.CHARGE_ID.getCode().equals(dto.getUpdateFiledCode())) {
            return commonService.getNameByIds(Arrays.asList(dto.getValues().toString().split(",")));
        }
        if (ProductBatchFieldEnum.PURCHASE_USER_ID.getCode().equals(dto.getUpdateFiledCode())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getValues().toString());
            return ObjUtil.isEmpty(findUserDTO) ? "" : findUserDTO.getUserName();
        }
        return dto.getName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOccupyStatus(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Boolean.FALSE;
        }
        List<String> updateSkuIds = new ArrayList<>();
        for (String skuId : skuIds) {
            //查询redis缓存
            String redisKey = format(RedisKeyConstant.SKU_OCCUPY_CODE, skuId, Boolean.TRUE);
            Collection<String> keys = redisUtil.keys(redisKey);
            if (CollectionUtils.isNotEmpty(keys)) {
                continue;
            }
            //没查到则插入redis
            log.info("从redis缓存中查询到产品信息，skuId:{}，内容为空", skuId);
            updateSkuIds.add(skuId);
        }
        if (CollectionUtils.isEmpty(updateSkuIds)) {
            return Boolean.TRUE;
        }
        //更新数据库数据
        lambdaUpdate().set(ProductDetailEntity::getOccupyStatus, Boolean.TRUE)
                .in(ProductDetailEntity::getId, updateSkuIds)
                .eq(ProductDetailEntity::getOccupyStatus, Boolean.FALSE)
                .update();
        //更新缓存数据
        for (String skuId : updateSkuIds) {
            //添加缓存
            String redisKey = format(RedisKeyConstant.SKU_OCCUPY_CODE, skuId, Boolean.TRUE);
            redisUtil.set(redisKey, skuId, RedisService.ONE_DAY_CACHE_TIME);
        }
        return Boolean.TRUE;
    }


    @Override
    public List<SkuVO> pdaSearchSku(ProductDetailDTO.PdaSearchDTO dto) {
        Integer state = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        dto.setStatus(state);
        List<SkuVO> skuVOS = baseMapper.pdaSearchSku(dto);
        List<String> mainSupplierIds = skuVOS.stream().map(SkuVO::getMainSupplier).distinct().collect(Collectors.toList());
        List<String> secondSupplierIds = skuVOS.stream().map(SkuVO::getSecondSupplier).distinct().collect(Collectors.toList());
        mainSupplierIds.addAll(secondSupplierIds);
        List<String> supplierIds = mainSupplierIds.stream().distinct().collect(Collectors.toList());
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);
        skuVOS.forEach(req -> {
            // 一级供应商名称
            if (StrUtils.isNotEmpty(req.getMainSupplier()) && supplierMap.containsKey(req.getMainSupplier())) {
                req.setMainSupplierName(supplierMap.get(req.getMainSupplier()).getName());
            }

            // 二级供应商名称
            if (StrUtils.isNotEmpty(req.getSecondSupplier()) && supplierMap.containsKey(req.getSecondSupplier())) {
                req.setSecondSupplierName(supplierMap.get(req.getSecondSupplier()).getName());
            }
        });
        if (CollectionUtils.isEmpty(skuVOS)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        return skuVOS;
    }

    /**
     * 根据创建时间获取到对应实体
     *
     * @param createTimeList
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     * @author yl
     * @date 2023-09-01 12:21
     */
    @Override
    public List<ProductDetailEntity> listByCreateTimeList(List<LocalDateTime> createTimeList) {
        if (CollectionUtils.isEmpty(createTimeList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(ProductDetailEntity::getStatus, ProductDetailStatusEnum.APPROVAL_PASS.getCode()).
                ge(ProductDetailEntity::getCreateTime, createTimeList.get(0)).
                le(ProductDetailEntity::getCreateTime, createTimeList.get(1)).list();
    }

    @Override
    public PdaProductDetailDTO.View pdaProductView(String skuNo) {
        ProductDetailEntity productIdBySku = getProductIdBySku(skuNo);
        if (ObjectUtil.isEmpty(productIdBySku)) {
            throw new ServiceException("sku不存在");
        }
        PdaProductDetailDTO.View view = baseMapper.pdaProductView(skuNo);
        List<PdaProductDetailDTO.ParentSkuDTO> parentSkuDTOList = new ArrayList<>();
        if (StringUtils.isBlank(view.getSpuNo())) {
            view.setSpuNo("");
            view.setSpuName("");
        }

        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(Arrays.asList(view.getMainSupplier(), view.getSecondSupplier()));
        // 一级供应商名称
        if (StrUtils.isNotEmpty(view.getMainSupplier()) && supplierMap.containsKey(view.getMainSupplier())) {
            view.setMainSupplierName(supplierMap.get(view.getMainSupplier()).getName());
        }

        // 二级供应商名称
        if (StrUtils.isNotEmpty(view.getSecondSupplier()) && supplierMap.containsKey(view.getSecondSupplier())) {
            view.setSecondSupplierName(supplierMap.get(view.getSecondSupplier()).getName());
        }

        //查询子sku
        List<BomChildrenSkuDTO> sonSkuList = bomSkuService.listBomChildBySkuIds(Arrays.asList(productIdBySku.getId()));
        if (CollectionUtils.isNotEmpty(sonSkuList)) {
            PdaProductDetailDTO.ParentSkuDTO parentSkuDTO = new PdaProductDetailDTO.ParentSkuDTO();
            parentSkuDTO.setSkuNo(productIdBySku.getSkuNo());
            List<PdaProductDetailDTO.SonSkuDTO> sonSkuDTOS = BeanMapper.copyList(sonSkuList, PdaProductDetailDTO.SonSkuDTO.class);
            parentSkuDTO.setChildSkuList(sonSkuDTOS);
            parentSkuDTOList.add(parentSkuDTO);
            //如果还有其他的bom也要添加
            List<PdaProductDetailDTO.ParentSkuDTO> parentSku = getParentSku(productIdBySku.getId());
            parentSkuDTOList.addAll(parentSku);
        } else {
            List<PdaProductDetailDTO.ParentSkuDTO> parentSku = getParentSku(productIdBySku.getId());
            parentSkuDTOList.addAll(parentSku);
        }
        view.setParentSkuDTOList(parentSkuDTOList);
        return view;
    }


    /**
     * 根据sku获取父sku信息
     *
     * @param skuId
     * @return
     */
    private List<PdaProductDetailDTO.ParentSkuDTO> getParentSku(String skuId) {
        List<PdaProductDetailDTO.ParentSkuDTO> parentSkuDTOList = new ArrayList<>();
        //没有子集获取父级
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomSkuService.listBomBySkuIds(Arrays.asList(skuId));
        List<String> skuIds = bomChildrenSkuDTOS.stream().map(req -> req.getParentSkuId()).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuInfoBySkuIds = baseMapper.getSkuBaseBySkuIds(skuIds);
        if (CollectionUtils.isNotEmpty(skuInfoBySkuIds)) {
            List<String> parentSkuIds = skuInfoBySkuIds.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
            List<BomChildrenSkuDTO> sonSkuList = bomSkuService.listBomChildBySkuIds(parentSkuIds);
            for (SkuVO vo : skuInfoBySkuIds) {
                PdaProductDetailDTO.ParentSkuDTO parentSkuDTO = new PdaProductDetailDTO.ParentSkuDTO();
                parentSkuDTO.setSkuNo(vo.getSkuNo());
                List<BomChildrenSkuDTO> collect = sonSkuList.stream().filter(req -> req.getParentSkuId().equals(vo.getSkuId())).collect(Collectors.toList());
                List<PdaProductDetailDTO.SonSkuDTO> sonSkuDTOS = BeanMapper.copyList(collect, PdaProductDetailDTO.SonSkuDTO.class);
                parentSkuDTO.setChildSkuList(sonSkuDTOS);
                parentSkuDTOList.add(parentSkuDTO);
            }
        }
        return parentSkuDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProductDetailBatch(List<ProductDetailEntity> list) {
        return this.updateBatchById(list);
    }

    @Override
    public List<ProductSearchDTO.SkuListDTO> listSkuBySkuNos(ProductSearchDTO.SkuParamDTO skuParamDTO) {
        //已存在数据
        skuParamDTO.setStatusList(Arrays.asList(ProductDetailStatusEnum.APPROVAL_PASS.getCode()));
        List<String> saleMethodList = skuParamDTO.getSaleMethodList();
        List<String> saleMethodParams = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(saleMethodList)) {
            for (String saleMethod : saleMethodList) {
                saleMethodParams.add(SaleMethodEnum.getNameByCode(Integer.valueOf(saleMethod)));
            }
            skuParamDTO.setSaleMethod(StringUtils.join(saleMethodParams, ","));
        }
        List<ProductSearchDTO.SkuListDTO> list = this.baseMapper.listSkuBySkuNos(skuParamDTO);

        List<String> supplierIdList = list.stream().filter(obj -> StringUtils.isNotBlank(obj.getMainSupplier())).map(ProductSearchDTO.SkuListDTO::getMainSupplier).distinct().collect(Collectors.toList());
        //供应商名称
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(supplierIdList);

        List<ProductSearchDTO.SkuListDTO> resultList = new ArrayList<>();
        for (String skuNo : skuParamDTO.getSkuNoList()) {
            ProductSearchDTO.SkuListDTO skuListDTO = list.stream().filter(obj -> obj.getSkuNo().equals(skuNo)).findFirst().orElse(new ProductSearchDTO.SkuListDTO());
            //sku状态名称
            skuListDTO.setStatusName(ProductDetailStatusEnum.getName(skuListDTO.getStatus()));
            //供应商名称
            ProductSearchDTO.SkuListDTO finalSkuListDTO = skuListDTO;
            String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(finalSkuListDTO.getMainSupplier())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            skuListDTO.setMainSupplierName(supplierName);
            //标准零售价
            skuListDTO.setRetailPrice(Objects.isNull(skuListDTO.getRetailPrice()) ? BigDecimal.ZERO : skuListDTO.getRetailPrice());
            resultList.add(skuListDTO);
        }
        return resultList;
    }

    @Override
    public List<ProductDetailEntity> listByChargeId(String chargeId) {
        return baseMapper.listByChargeId(chargeId);
    }

    @Override
    public Boolean updateWarehouseLocationById(String id, String warehouseLocation, String warehouseLocationLarge) {
        boolean flag = lambdaUpdate()
                .eq(ProductDetailEntity::getId, id)
                .set(ProductDetailEntity::getWarehouseLocation, warehouseLocation)
                .set(ProductDetailEntity::getWarehouseLocationLarge, warehouseLocationLarge)
                .update();

        return flag;
    }

    /**
     * @param entityList
     * @description: 审核必填字段校验
     * @author Will
     * @date: 2024/2/1 10:14
     */
    private void checkApproveField(List<ProductDetailEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        //产品信息
        List<String> productIdList = entityList.stream().map(ProductDetailEntity::getProductId).collect(Collectors.toList());
        List<ProductInfoEntity> productInfoList = productInfoService.listByIds(productIdList);

        for (ProductDetailEntity detailEntity : entityList) {
            StringBuilder errMsg = new StringBuilder("");

            //SPU信息
            ProductInfoEntity productInfo = productInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getProductId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(productInfo)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            if (ObjectUtil.isEmpty(productInfo.getSaleMethod() )|| (!productInfo.getSaleMethod().contains(SaleMethodEnum.GOODS.getName()) && !productInfo.getSaleMethod().contains(SaleMethodEnum.GIFT.getName()))) {
                continue;
            }
            if (isNotBlank(errMsg)) {
                throw new ServiceException(errMsg.toString());
            }
        }
    }

    @Override
    public List<ProductDetailEntity> listBySkuNoList(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ProductDetailEntity::getSkuNo, skuNoList).list();
    }

    @Override
    public List<SkuSimpleVO> searchSkuWithCombination(String searchKeyword) {
        return baseMapper.searchSkuWithCombination(searchKeyword, ProductDetailStatusEnum.APPROVAL_PASS.getCode());
    }

    @Override
    public List<SkuVO> getSkuInfoAdvanceQuery(AdvanceQueryContainer advanceQueryContainer) {
        return baseMapper.getSkuInfoAdvanceQuery(advanceQueryContainer);
    }

    @Override
    public ExcelImportFsDTO.UrlDTO importProductFile(MultipartFile excelFile, Integer importType, HttpServletResponse response) {
        if(importType.equals(ImportTypeEnum.IMPORT_ADD.getCode())){//导入新增
            return importAdd(excelFile, importType, response);
        }else{//导入更新
            return importUpdate(excelFile,importType, response);
        }
    }

    private String getInsurancePropertyList(String insurancePropertyName, Map<String, BasicDictEntity> mapById, List<String> errorMsgList) {
        if (StringUtils.isBlank(insurancePropertyName)) {
//            errorMsgList.add("保险属性不能为空");
            return null;
        }

        if(InsurancePropertyEnum.NOT.getName().equals(insurancePropertyName)){
            BasicDictEntity entity = mapById.get(insurancePropertyName);
            if (Objects.isNull(entity)) {
                errorMsgList.add("保险属性【" + insurancePropertyName + "】在系统中未找到");
                return "";
            }
            return entity.getValue();
        }

        if(InsurancePropertyEnum.NOT.getName().equals(insurancePropertyName)){
            BasicDictEntity entity = mapById.get(insurancePropertyName);
            if (Objects.isNull(entity)) {
                errorMsgList.add("保险属性【" + insurancePropertyName + "】在系统中未找到");
                return "";
            }
            return entity.getValue();
        }

        // 处理空字符串和仅包含逗号的情况
        insurancePropertyName = insurancePropertyName.trim();
        if (insurancePropertyName.isEmpty() || insurancePropertyName.equals(",")) {
            errorMsgList.add("保险属性不能为空或仅包含逗号");
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] properties = insurancePropertyName.split("\\s*,\\s*");

        for (String property : properties) {
            property = property.trim();
            if (property.isEmpty()) {
                continue;
            }

            if (InsurancePropertyEnum.NOT.getName().equals(property)) {
                errorMsgList.add("保险属性【" + property + "】不能与其他属性同时存在");
                continue;
            }

            BasicDictEntity entity = mapById.get(property);
            if (Objects.isNull(entity)) {
                errorMsgList.add("保险属性【" + property + "】在系统中未找到");
                continue;
            }

            result.append(entity.getValue()).append(",");
        }

        if (result.length() > 0) {
            result.setLength(result.length() - 1); // 移除最后一个逗号
        }

        return result.toString();
    }

    private ExcelImportFsDTO.UrlDTO importUpdate(MultipartFile excelFile, Integer importType, HttpServletResponse response) {
        ProductDetailUpdateNotApproveExcelListener excelListenerUtil = new ProductDetailUpdateNotApproveExcelListener();
        try {
            read(excelFile.getInputStream(), ProductDetailImprotUpdateExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<ProductDetailImprotUpdateExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<ProductDetailImprotUpdateExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<ProductDetailImprotUpdateExcelDTO> successList = excelListenerUtil.getSuccessList();

        //处理验证成功数据
        List<String> productIdList = handleUpdateSuccessList(importType, successList, errorList);
        successList.removeAll(errorList);

        String excelPath = "excel/productNoSpecDetail.xlsx";
        String fileName = "productNoSpecDetail.xlsx";
        if(importType.equals(ImportTypeEnum.IMPORT_APPROVAL.getCode())){
            excelPath = "excel/productNoSpecApproveDetail.xlsx";
            fileName = "productNoSpecApproveDetail.xlsx";
        }
        String errorUrl = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            File file = ExcelUtil.exportFile(excelPath, fileName, errorList);
            if (file != null && !file.isDirectory()) {
                errorUrl = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        String successUrl = "";
        if (CollectionUtils.isNotEmpty(successList)) {
            File file = ExcelUtil.exportFile(excelPath, fileName, successList);
            if (file != null && !file.isDirectory()) {
                successUrl = FastDFSClientUtil.uploadFile(file, fileName);
                //成功添加日志文本
                for (String productId : productIdList) {
                    addProductImportLog(successUrl,productId);
                }
            }
        }
        return new ExcelImportFsDTO.UrlDTO(successUrl,errorUrl);
    }

    /**
     * 添加产品导入日志
     * @author will
     * @date 2025/3/26 09:53
     * @param successUrl
     * @param productId
     */
    private void addProductImportLog (String successUrl,String productId) {
        //新增操作日志
        SysLogEntity sysLogEntity = new SysLogEntity().setContent(format("<a href='{}' class='custom-link'>{}</a>", FastDFSClientUtil.publicUrl + successUrl,"导入成功"))
                .setBusinessId(productId)
                .setPid(productId)
                .setOperation("导入")
                .setClassPath(SysLogClassPathEnum.PRODUCTINFOENTITY.getDesc());
        //添加日志
        sysLogService.addSysLogByOther(sysLogEntity);
    }

    /**
     * 成功数据处理
     * @author will
     * @date 2025/3/26 09:51
     * @param importType
     * @param successList
     * @param errorList
     * @return java.util.List<java.lang.String>
     */
    private List<String> handleUpdateSuccessList(Integer importType, List<ProductDetailImprotUpdateExcelDTO> successList, List<ProductDetailImprotUpdateExcelDTO> errorList) {
        List<String> productIdList = new ArrayList<>();

        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<BasicDictEntity> basicDictList = basicDictService.list();
        List<ProductDetailEntity> productDetailEntityList = this.list();
        List<ProductUnitEntity> unitEntityList = productUnitService.list();
        List<BasicCategoryEntity> categoryEntityList = basicCategoryService.list();
        List<ApplicationCategoryEntity> applicationCategoryList = applicationCategoryService.list();
        Map<String, String> applicationCategoryMap = applicationCategoryList.stream()
                .collect(Collectors.toMap(ApplicationCategoryEntity::getName, ApplicationCategoryEntity::getId, (o1, o2) -> o1));

        //根据供应商名称查询供应商信息
        List<String> mainSupplierNameList = successList.stream().map(req -> req.getMainSupplier()).distinct().collect(Collectors.toList());
        List<String> secondSupplierNameList = successList.stream().map(req -> req.getSecondSupplier()).distinct().collect(Collectors.toList());
        List<String> supplierNameList = new ArrayList<>();
        supplierNameList.addAll(mainSupplierNameList);
        supplierNameList.addAll(secondSupplierNameList);
        List<SupplierEntity> supplierList = scmTaskFeign.listBySupplierByNames(supplierNameList);

        //查询产品信息
        List<ProductInfoEntity> productInfoEntities = productInfoService.lambdaQuery().select(ProductInfoEntity::getId,  ProductInfoEntity::getPropertyId).list();
        Map<String, ProductInfoEntity> productInfoMap = productInfoEntities.stream().collect(Collectors.toMap(ProductInfoEntity::getId, ProductInfoEntity -> ProductInfoEntity));

        // 调用远程服务，获取库存实体列表
        List<String> skuNoList = successList.stream().map(ProductDetailImprotUpdateExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<InventoryEntity> inventoryEntities = inventoryFeign.listInventoryBySkuNos(skuNoList);
        // 排除在途的库存
        Map<String, Integer> inventoryMap = inventoryEntities.stream()
                .filter(v -> !v.getDictInventoryStatus().equals(InventoryStatusEnum.IN_TRANSIT.getCode()))
                .collect(Collectors.groupingBy(InventoryEntity::getSkuNo, Collectors.summingInt(InventoryEntity::getQty)));

        // 获取保险属性字典数据并缓存
        List<BasicDictEntity> dictList = basicDictService.listByType(BasicDictTypeEnum.INSURANCE_PROPERTY.getCode());
        Map<String, BasicDictEntity>  insurancePropertyMap = dictList.stream()
                .collect(Collectors.toMap(BasicDictEntity::getName, entity -> entity));

        //消息推送
        List<ProductDetailDTO.NoticeDTO> noticeDTOList = new ArrayList<>();

        for (ProductDetailImprotUpdateExcelDTO dto : successList) {
            List<String> errorMsgList = new ArrayList<>();

            ProductDetailEntity productBy = productDetailEntityList.stream().filter(req -> req.getSkuNo().equals(dto.getSkuNo())).findFirst().orElse(null);

            ProductInfoDTO productInfoDTO = new ProductInfoDTO();
            //sku信息
            ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();
            if (ObjectUtils.isEmpty(productBy)) {
                errorMsgList.add("sku不存在，请选择导入新增");
                dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(dto);
                continue;
            }

            if(importType.equals(ImportTypeEnum.IMPORT_NOT_APPROVAL.getCode())){
                if (ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(productBy.getStatus())
                        || ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productBy.getStatus())) {
                    errorMsgList.add("仅{待提交，审核不通过}的状态下可导入修改");
                    dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(dto);
                    continue;
                }
            }else {
                if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productBy.getStatus())) {
                    errorMsgList.add("仅{审核通过}的状态下可导入修改");
                    dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(dto);
                    continue;
                }
            }
            productSkuBaseInfoDTO.setId(productBy.getId());
            productInfoDTO.setId(productBy.getProductId());
            //设置推荐仓位（大货区/小货区）
            if (StringUtils.isNotBlank(dto.getWarehouseLocationLarge())) {
                dto.setWarehouseLocationLarge(dto.getWarehouseLocationLarge());
            }
            if (StringUtils.isNotBlank(dto.getWarehouseLocation())) {
                dto.setWarehouseLocation(dto.getWarehouseLocation());
            }

            //校验更新未审核的字段
            String saleCountryStr = "";
            if(importType.equals(ImportTypeEnum.IMPORT_NOT_APPROVAL.getCode())){
                if (StringUtils.isNotBlank(dto.getSaleCountry())) {
                    String[] saleCountryList = dto.getSaleCountry().split(",");
                    for (String saleCountry : saleCountryList) {
                        DictCountryEntity countryEntity = sysUserFeign.getCountryById(saleCountry);
                        if (ObjectUtils.isEmpty(countryEntity)) {
                            errorMsgList.add("销售国家在系统中未找到");
                            break;
                        }
                        saleCountryStr = saleCountryStr + countryEntity.getId() + ",";
                    }
                }

                BasicDictEntity productBrand = null;
                if(StringUtils.isNotBlank(dto.getBrandName())){
                    productBrand = basicDictList.stream().filter(b -> BasicDictTypeEnum.PRODUCT_BRAND.getCode().equals(b.getType()) && b.getValue().
                            equals(dto.getBrandName())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(productBrand)) {
                        errorMsgList.add("产品品牌在系统中未找到");
                    }else {
                        productInfoDTO.setBrandId(productBrand.getId());
                        productInfoDTO.setBrandName(productBrand.getValue());
                    }
                }

                //迭代产品校验
                if (ProductTypeEnum.ITERATIVE_PRODUCT.getName().equals(dto.getTypeName())) {
                    if (isBlank(dto.getIterateRefSkuNo())) {
                        errorMsgList.add("迭代产品不能为空");
                    } else {
                        ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(req -> req.getSkuNo().equals(dto.getIterateRefSkuNo())).findFirst().orElse(null);
                        if (ObjectUtil.isEmpty(productDetailEntity)) {
                            errorMsgList.add("迭代产品在系统中未找到");
                        } else {
                            if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productDetailEntity.getStatus())) {
                                errorMsgList.add("迭代产品未审核完成不支持引用");
                            }
                            productInfoDTO.setIterateRefSkuId(productDetailEntity.getId());
                        }
                    }
                }

                //产品等级
                BasicDictEntity productGrade = null;
                if(StringUtils.isNotBlank(dto.getGrade())){
                    productGrade = basicDictList.stream().filter(b -> BasicDictTypeEnum.PRODUCT_GRADE.getCode().equals(b.getType()) && b.getValue().
                            equals(dto.getGrade())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(productGrade)) {
                        errorMsgList.add("产品等级在系统中未找到");
                    }else {
                        productInfoDTO.setGrade(productGrade.getValue());
                        productInfoDTO.setGradeId(productGrade.getId());
                    }
                }

                ProductUnitEntity productUnitEntity = new ProductUnitEntity();
                if (StringUtils.isNotBlank(dto.getUnitName())) {
                    productUnitEntity = unitEntityList.stream().filter(req -> req.getName().equals(dto.getUnitName())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(productUnitEntity)) {
                        errorMsgList.add("单位名称在系统中不存在");
                    }else{
                        productSkuBaseInfoDTO.setUnitId(productUnitEntity.getId());
                        productSkuBaseInfoDTO.setUnitName(productUnitEntity.getName());
                    }
                }
            }

            //产品经理
            List<FindUserDTO> chargeNameList = new ArrayList<>();
            if (StringUtils.isNotBlank(dto.getChargeName())) {
                chargeNameList = userList.stream().filter(e -> e.getUserName().equals(dto.getChargeName())).collect(Collectors.toList());
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(chargeNameList)) {
                    errorMsgList.add("产品经理在系统中未找到");
                }
            }
            //采购员
            List<FindUserDTO> purchaseUserList = new ArrayList<>();
            if (StringUtils.isNotBlank(dto.getPurchaseUser())) {
                purchaseUserList = userList.stream().filter(e -> e.getUserName().equals(dto.getPurchaseUser())).collect(Collectors.toList());
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(purchaseUserList)) {
                    errorMsgList.add("采购员在系统中未找到");
                }
            }
            //产品属性
            BasicDictEntity productProperty = null;
            if(StringUtils.isNotBlank(dto.getProperty())){
                productProperty = basicDictList.stream().filter(b -> BasicDictTypeEnum.PRODUCT_PROPERTY.getCode().equals(b.getType()) && b.getValue().
                        equals(dto.getProperty())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(productProperty)) {
                    errorMsgList.add("产品属性在系统中未找到");
                }else {
                    ProductInfoEntity productInfoEntity = productInfoMap.get(productBy.getProductId());
                    // 如果商品属性ID与实体中的属性ID不匹配,查sku库存
                    if(Objects.nonNull(productInfoEntity) && !productInfoEntity.getPropertyId().equals(productProperty.getId())){
                        Integer qty = null == inventoryMap.get(dto.getSkuNo()) ? 0 : inventoryMap.get(dto.getSkuNo());
                        if(qty > 0){
                            errorMsgList.add("SKU存在库存，产品属性不允许变更");
                        }
                    }
                }
            }
            //保险属性
            String insurancePropertyName = getInsurancePropertyList(dto.getInsuranceProperty(), insurancePropertyMap,errorMsgList);
            dto.setInsuranceProperty(insurancePropertyName);

            Integer purchaseState = null;
            if (StringUtils.isNotBlank(dto.getArrivalState())) {
                purchaseState = PurchaseStateEnum.getCodeByName(dto.getArrivalState());
            }

            List<BasicDictEntity> declarePropertyList = new ArrayList<>();
            if (StringUtils.isNotBlank(dto.getProductProperty())) {
                String[] productPropertyList = dto.getProductProperty().split(",");
                for (String name : productPropertyList) {
                    BasicDictEntity declareProperty = basicDictService.checkBasicDict(BasicDictTypeEnum.DECLARE_PROPERTY.getCode(), name);
                    if (ObjectUtils.isEmpty(declareProperty)) {
                        errorMsgList.add("报关产品属性在系统中未找到");
                    } else {
                        declarePropertyList.add(declareProperty);
                    }
                }
            }

            //产品开发状态
            String productState = dto.getProductStateName();
            if (StringUtils.isNotBlank(productState)) {
                Integer code = ProductDetailStateEnum.getCodeByName(productState);
                productSkuBaseInfoDTO.setProductState(code);
            }

            //产品分类
            String category = dto.getMainCategory();
            if(StringUtils.isNotBlank(category)){
                BasicCategoryEntity basicCategoryEntity = categoryEntityList.stream().filter(req -> req.getName().equals(category) && req.getPid().equals("0")).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(basicCategoryEntity)) {
                    errorMsgList.add("产品分类一级类目不存在");
                } else {
                    //父级品类
                    List<BasicCategoryEntity> categoryList = new ArrayList<>();
                    this.setParentEntity(basicCategoryEntity.getId(), categoryList, categoryEntityList);
                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(categoryList)) {
                        errorMsgList.add(ApiError.ERROR_95091.msg);
                    }
                    //一级品类
                    BasicCategoryEntity bestEntity = categoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(bestEntity) || StringUtils.isBlank(bestEntity.getCode())) {
                        errorMsgList.add(ApiError.ERROR_95091.msg);
                    }
                    //二级品类
                    String secondaryCategory = dto.getSecondaryCategory();
                    BasicCategoryEntity secondaryCategoryEntity = categoryEntityList.stream().filter(req -> req.getName().equals(secondaryCategory) && !req.getPid().equals("0")).findFirst().orElse(null);

                    if (ObjectUtils.isEmpty(secondaryCategoryEntity)) {
                        productInfoDTO.setCategory(bestEntity.getName());
                        productInfoDTO.setCategoryId(bestEntity.getId());
                    } else {
                        BasicCategoryEntity secondEntity = categoryList.stream().filter(obj -> secondaryCategoryEntity.getPid().equals(obj.getId())).findFirst().orElse(null);
                        if (ObjectUtils.isEmpty(secondEntity) || StringUtils.isBlank(secondaryCategoryEntity.getCode())) {
                            errorMsgList.add(ApiError.ERROR_95092.msg);
                        }
                        if (!bestEntity.getId().equals(secondaryCategoryEntity.getPid())) {
                            errorMsgList.add("产品分类一级类目和二级类目的关系不匹配");
                        }
                        productInfoDTO.setCategory(secondaryCategory);
                        productInfoDTO.setCategoryId(secondaryCategoryEntity.getId());
                    }
                    //存在错误信息则返回
                    if (CollectionUtils.isNotEmpty(errorMsgList)) {
                        dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                        errorList.add(dto);
                        continue;
                    }
                }
            }
            if(StringUtils.isNotBlank(dto.getApplicationCategoryName())){

                List<String> applicationCategoryNameList = Arrays.stream(dto.getApplicationCategoryName().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                List<String> applicationCategoryIdList = new ArrayList<>();
                for (String applicationCategoryName : applicationCategoryNameList) {
                    String applicationCategoryId = applicationCategoryMap.get(applicationCategoryName);
                    if (StringUtils.isBlank(applicationCategoryId)) {
                        errorMsgList.add(applicationCategoryName+" 应用分类不存在");
                        continue;
                    }
                    applicationCategoryIdList.add(applicationCategoryId);
                }
                productInfoDTO.setApplicationCategoryId(String.join(",", applicationCategoryIdList));
            }
            //存在侵权风险
            String pirateRisk = dto.getPirateRisk();
            if (StringUtils.isNotBlank(pirateRisk)) {
                if (pirateRisk.equals("是")) {
                    productInfoDTO.setPirateRisk(1);
                } else {
                    productInfoDTO.setPirateRisk(2);
                }
            }
            //是否客户定制
            String isCustomized = dto.getIsCustomized();
            if (StringUtils.isNotBlank(isCustomized)) {
                if (isCustomized.equals("是")) {
                    productInfoDTO.setIsCustomized(1);
                } else {
                    productInfoDTO.setIsCustomized(0);
                }
            }
            // 一级供应商
            String mainSupplier = dto.getMainSupplier();
            //二级供应商
            String secondSupplier = dto.getSecondSupplier();
            if (StringUtils.isNotBlank(mainSupplier)) {
                SupplierEntity mainSupplierDb = supplierList.stream().filter(s -> s.getName().equals(mainSupplier)).
                        findFirst().orElse(null);
                if (Objects.isNull(mainSupplierDb)) {
                    errorMsgList.add("一级供应商不存在");
                } else {
                    dto.setMainSupplier(mainSupplierDb.getId());
                }
            }
            if (StringUtils.isNotBlank(secondSupplier)) {
                SupplierEntity secondSupplierDb = supplierList.stream().filter(s -> s.getName().equals(secondSupplier)).
                        findFirst().orElse(null);
                if (Objects.isNull(secondSupplierDb)) {
                    errorMsgList.add("二级供应商不存在");
                } else {
                    dto.setSecondSupplier(secondSupplierDb.getId());
                }
            }

            //正常情况下箱规尺寸>=包装尺寸，毛重>=净重
            ProductKeyDTO productKey = productDetailMapper.getProductKey(productBy.getId());
            //旧包装信息
            ProductPackEntity oldPackEntity = productPackService.getById(productKey.getPackId());
            if(StringUtils.isNotBlank(dto.getBoxLength()) || StringUtils.isNotBlank(dto.getProductLength())){
                BigDecimal boxLength = MathUtil.valueOf(dto.getBoxLength());
                if(StringUtils.isBlank(dto.getBoxLength())){
                    boxLength = oldPackEntity.getBoxLength();
                }else {
                    boxLength = LengthConverterUtil.cmToMm(boxLength);
                }
                BigDecimal productLength = MathUtil.valueOf(dto.getProductLength());
                if(StringUtils.isBlank(dto.getProductLength())){
                    productLength = oldPackEntity.getProductLength();
                }else {
                    productLength = LengthConverterUtil.cmToMm(productLength);
                }
                if(boxLength.compareTo(productLength)<0){
                    errorMsgList.add(ApiError.ERROR_LENGTH_BOX_LITTER_THAN_PRODUCT.msg);
                }
            }
            if(StringUtils.isNotBlank(dto.getBoxWidth()) || StringUtils.isNotBlank(dto.getProductWidth())){
                BigDecimal boxWidth = MathUtil.valueOf(dto.getBoxWidth());
                if(StringUtils.isBlank(dto.getBoxWidth())){
                    boxWidth = oldPackEntity.getBoxWidth();
                }else {
                    boxWidth = LengthConverterUtil.cmToMm(boxWidth);
                }
                BigDecimal productWidth = MathUtil.valueOf(dto.getProductWidth());
                if(StringUtils.isBlank(dto.getProductWidth())){
                    productWidth = oldPackEntity.getProductWidth();
                }else {
                    productWidth = LengthConverterUtil.cmToMm(productWidth);
                }
                if(boxWidth.compareTo(productWidth)<0){
                    errorMsgList.add(ApiError.ERROR_WIDTH_BOX_LITTER_THAN_PRODUCT.msg);
                }
            }
            if(StringUtils.isNotBlank(dto.getBoxHeight()) || StringUtils.isNotBlank(dto.getProductHeight())){
                BigDecimal boxHeight = MathUtil.valueOf(dto.getBoxHeight());
                if(StringUtils.isBlank(dto.getBoxHeight())){
                    boxHeight = oldPackEntity.getBoxHeight();
                }else {
                    boxHeight = LengthConverterUtil.cmToMm(boxHeight);
                }
                BigDecimal productHeight = MathUtil.valueOf(dto.getProductHeight());
                if(StringUtils.isBlank(dto.getProductHeight())){
                    productHeight = oldPackEntity.getProductHeight();
                }else {
                    productHeight = LengthConverterUtil.cmToMm(productHeight);
                }
                if(boxHeight.compareTo(productHeight)<0){
                    errorMsgList.add(ApiError.ERROR_HEIGHT_BOX_LITTER_THAN_PRODUCT.msg);
                }
            }
            if(StringUtils.isNotBlank(dto.getGrossWeight()) || StringUtils.isNotBlank(dto.getNetWeight())){
                BigDecimal grossWeight = MathUtil.valueOf(dto.getGrossWeight());
                if(StringUtils.isBlank(dto.getGrossWeight())){
                    grossWeight = oldPackEntity.getGrossWeight();
                }
                BigDecimal netWeight = MathUtil.valueOf(dto.getNetWeight());
                if(StringUtils.isBlank(dto.getNetWeight())){
                    netWeight = oldPackEntity.getNetWeight();
                }
                if(grossWeight.compareTo(netWeight)<0){
                    errorMsgList.add(ApiError.ERROR_WEIGHT_GROSS_LITTER_THAN_NET.msg);
                }
            }

            //存在错误信息则返回
            if (CollUtil.isNotEmpty(errorMsgList)) {
                dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(dto);
                continue;
            }

            ProductNoSpecDTO productNoSpecDTO = new ProductNoSpecDTO();
            productInfoDTO.setApprovalStatus(0);
            productInfoDTO.setIsNoSpecAdd(MathUtil.ONE);
            if(Objects.nonNull(productProperty)){
                productInfoDTO.setProperty(productProperty.getValue());
                productInfoDTO.setPropertyId(productProperty.getId());
            }

            //sku 经理
            if (CollUtil.isNotEmpty(chargeNameList)) {
                productInfoDTO.setChargeName(chargeNameList.get(0).getUserName());
                productInfoDTO.setChargeId(chargeNameList.get(0).getUserId());
                productSkuBaseInfoDTO.setChargeName(chargeNameList.get(0).getUserName());
                productSkuBaseInfoDTO.setChargeId(chargeNameList.get(0).getUserId());
                //给sku 产品经理id
                dto.setChargeId(chargeNameList.get(0).getUserId());
            }
            if(StringUtils.isNotBlank(dto.getMaterials())){
                productInfoDTO.setMaterials(dto.getMaterials());
            }
            if(StringUtils.isNotBlank(dto.getFunctionDesc())){
                productInfoDTO.setFunctionDesc(dto.getFunctionDesc());
            }
            if(StringUtils.isNotBlank(dto.getSellSpot())){
                productInfoDTO.setSellSpot(dto.getSellSpot());
            }
            if(StringUtils.isNotBlank(dto.getSaleMethod())){
                productInfoDTO.setSaleMethod(dto.getSaleMethod());
            }
            if(StringUtils.isNotBlank(dto.getUsageDesc())){
                productInfoDTO.setUsageDesc(dto.getUsageDesc());
            }
            if(StringUtils.isNotBlank(dto.getNameEn())){
                productInfoDTO.setNameEn(dto.getNameEn());
            }
            if(StringUtils.isNotBlank(dto.getSalesChannel())){
                productInfoDTO.setSalesChannel(dto.getSalesChannel());
            }
            if(MathUtil.valueOf(dto.getMassCost()).compareTo(BigDecimal.ZERO) > 0){
                productInfoDTO.setMoldCost(MathUtil.valueOf(dto.getMoldCost()));
            }
            if(MathUtil.valueOf(dto.getEntrustedDevelopCost()).compareTo(BigDecimal.ZERO) > 0){
                productInfoDTO.setEntrustedDevelopCost(MathUtil.valueOf(dto.getEntrustedDevelopCost()));
            }
            // 添加默认spu
            productInfoDTO.setSpuNo(Optional.ofNullable(dto.getSpuNo()).orElse(dto.getSkuNo()));
            //检查spu编号是否重复
            if (Boolean.TRUE.equals(this.checkSpuNo(productInfoDTO.getSpuNo(), productInfoDTO.getId()))) {
                throw new ServiceException(ApiError.ERROR_95017);
            }
            //sku信息
            BeanMapper.copyNonNull(dto, productSkuBaseInfoDTO);
            if (StringUtils.isNotBlank(dto.getPlanListingTime())) {
                productSkuBaseInfoDTO.setPlanListingTime(LocalDate.parse(dto.getPlanListingTime(), dateTimeFormatter));
            }
            productSkuBaseInfoDTO.setProductId("");
            productSkuBaseInfoDTO.setProductState(ProductDetailStateEnum.getCodeByName(productState));

            //spu/sku基础信息
            ProductBaseInfoDTO productBaseInfoDTO = new ProductBaseInfoDTO();
            productBaseInfoDTO.setProductSpuBaseInfoDTO(productInfoDTO);
            productBaseInfoDTO.setProductSkuBaseInfoDTO(productSkuBaseInfoDTO);
            productNoSpecDTO.setProductBaseInfoDTO(productBaseInfoDTO);

            //产品成本信息表
            ProductCostDTO productCostDTO = new ProductCostDTO();
            /**
             * 预计立项成本(￥)
             */
            if(MathUtil.valueOf(dto.getProjectApprovalCost()).compareTo(BigDecimal.ZERO) > 0){
                productCostDTO.setProjectApprovalCost(MathUtil.valueOf(dto.getProjectApprovalCost()));
            }
            /**
             * 实际量产成本(￥)
             */
            if(MathUtil.valueOf(dto.getMassCost()).compareTo(BigDecimal.ZERO) > 0){
                productCostDTO.setMassCost(MathUtil.valueOf(dto.getMassCost()));
            }
            /**
             * 预计项目成本(￥)
             */
            if(MathUtil.valueOf(dto.getProjectCost()).compareTo(BigDecimal.ZERO) > 0){
                productCostDTO.setProjectCost(MathUtil.valueOf(dto.getProjectCost()));
            }
            /**
             *税率
             */
            if(MathUtil.valueOf(dto.getTaxRate()).compareTo(BigDecimal.ZERO) > 0){
                productCostDTO.setTaxRate(MathUtil.valueOf(dto.getTaxRate()));
            }
            /**
             *目标含税成本(￥)
             */
            if(MathUtil.valueOf(dto.getTargetTaxCost()).compareTo(BigDecimal.ZERO) > 0){
                productCostDTO.setTargetTaxCost(MathUtil.valueOf(dto.getTargetTaxCost()));
            }
            /**
             *目标不含税成本(￥)
             */
            if(MathUtil.valueOf(dto.getTargetNoTaxCost()).compareTo(BigDecimal.ZERO) > 0){
                productCostDTO.setTargetNoTaxCost(MathUtil.valueOf(dto.getTargetNoTaxCost()));
            }
            /**
             *标准零售价(￥)
             */
            if(MathUtil.valueOf(dto.getRetailPrice()).compareTo(BigDecimal.ZERO) > 0){
                productCostDTO.setRetailPrice(MathUtil.valueOf(dto.getRetailPrice()));
            }
            productNoSpecDTO.setProductCostDTO(productCostDTO);

            //采购信息信息
            ProductPurchaseDTO productPurchaseDTO = new ProductPurchaseDTO();
            /**
             * ean码
             */
            if(StringUtils.isNotBlank(dto.getEan())){
                productPurchaseDTO.setEan(dto.getEan());
            }
            /**
             * MOQ(最小起订量)
             */
            if(StringUtils.isNotBlank(dto.getMoq()) && MathUtil.valueOfInteger(dto.getMoq()) > 0){
                productPurchaseDTO.setMoq(MathUtil.valueOfInteger(dto.getMoq()));
            }
            /**
             * 试产数量
             */
            if(StringUtils.isNotBlank(dto.getTrialProductionQty()) && MathUtil.valueOfLong(dto.getTrialProductionQty()) > 0){
                productPurchaseDTO.setTrialProductionQty(MathUtil.valueOfLong(dto.getTrialProductionQty()));
            }
            /**
             * 首批量产数量
             */
            if(StringUtils.isNotBlank(dto.getFirstMassQty()) && MathUtil.valueOfLong(dto.getFirstMassQty()) > 0){
                productPurchaseDTO.setFirstMassQty(MathUtil.valueOfLong(dto.getFirstMassQty()));
            }
            /**
             * 计划首批下单量
             */
            if(StringUtils.isNotBlank(dto.getPlanOrderQty()) && MathUtil.valueOfLong(dto.getPlanOrderQty()) > 0){
                productPurchaseDTO.setPlanOrderQty(MathUtil.valueOfLong(dto.getPlanOrderQty()));
            }
            /**
             * 实际首批到货量
             */
            if(StringUtils.isNotBlank(dto.getActualArrivalQty()) && MathUtil.valueOfLong(dto.getActualArrivalQty()) > 0){
                productPurchaseDTO.setActualArrivalQty(MathUtil.valueOfLong(dto.getActualArrivalQty()));
            }
            /**
             * 预计首批到货时间
             */
            if (StringUtils.isNotBlank(dto.getPlanArrivalTime())) {
                productPurchaseDTO.setPlanArrivalTime(LocalDate.parse(dto.getPlanArrivalTime(), dateTimeFormatter));
            }
            /**
             * 实际首批到货时间
             */
            if (StringUtils.isNotBlank(dto.getActualArrivalTime())) {
                productPurchaseDTO.setActualArrivalTime(LocalDate.parse(dto.getActualArrivalTime(), dateTimeFormatter));
            }
            /**
             * 首批下单时间
             */
            if (StringUtils.isNotBlank(dto.getPlaceOrderTime())) {
                productPurchaseDTO.setPlaceOrderTime(LocalDate.parse(dto.getPlaceOrderTime(), dateTimeFormatter));
            }
            /**
             * 交货周期(天)
             */
            if(StringUtils.isNotBlank(dto.getDeliveryCycle()) && MathUtil.valueOf(dto.getDeliveryCycle()).compareTo(BigDecimal.ZERO) > 0){
                productPurchaseDTO.setDeliveryCycle(MathUtil.valueOf(dto.getDeliveryCycle()));
            }
            /**
             * 采购员
             */
            if (purchaseUserList.size() > 0) {
                productPurchaseDTO.setPurchaseUserId(purchaseUserList.get(0).getUserId());
            }
            /**
             * 首批到货状态
             */
            if(MathUtil.valueOf(purchaseState).compareTo(BigDecimal.ZERO) > 0){
                productPurchaseDTO.setArrivalState(purchaseState);
            }
            /**
             * 一级供应商
             */
            if(StringUtils.isNotBlank(dto.getMainSupplier())){
                productPurchaseDTO.setMainSupplier(dto.getMainSupplier());
            }
            /**
             * 二级供应商
             */
            if(StringUtils.isNotBlank(dto.getSecondSupplier())){
                productPurchaseDTO.setSecondSupplier(dto.getSecondSupplier());
            }
            productNoSpecDTO.setProductPurchaseDTO(productPurchaseDTO);

            //产品销售信息
            ProductSaleDTO productSaleDTO = new ProductSaleDTO();

            /**
             * 年目标销量
             */
            if(StringUtils.isNotBlank(dto.getYearSaleQty()) && MathUtil.valueOfLong(dto.getYearSaleQty()) > 0){
                productSaleDTO.setYearSaleQty(MathUtil.valueOfLong(dto.getYearSaleQty()));
            }
            /**
             * 年目标销售额（￥）
             */
            if(MathUtil.valueOf(dto.getYearSaleAmount()).compareTo(BigDecimal.ZERO) > 0){
                productSaleDTO.setYearSaleAmount(MathUtil.valueOf(dto.getYearSaleAmount()));
            }
            /**
             * 目标月销售量
             */
            if(MathUtil.valueOf(dto.getMonthSaleQty()).compareTo(BigDecimal.ZERO) > 0){
                productSaleDTO.setMonthSaleQty(MathUtil.valueOfLong(dto.getMonthSaleQty()));
            }
            /**
             * 目标月销售额（￥）
             */
            if(MathUtil.valueOf(dto.getMonthSaleAmount()).compareTo(BigDecimal.ZERO) > 0){
                productSaleDTO.setMonthSaleAmount(MathUtil.valueOf(dto.getMonthSaleAmount()));
            }
            /**
             * 首季度目标销量
             */
            if(MathUtil.valueOf(dto.getTargetSalesQty()).compareTo(BigDecimal.ZERO) > 0){
                productSaleDTO.setTargetSalesQty(MathUtil.valueOf(dto.getTargetSalesQty()));
            }
            /**
             * 销售国家
             */
            if (StringUtils.isNotBlank(saleCountryStr)) {
                productSaleDTO.setSaleCountry(saleCountryStr.substring(0, saleCountryStr.length() - 1));
            }
            /**
             * 图片是否完成
             */
            String isFinishedImg = dto.getIsFinishedImg();
            if (StringUtils.isNotBlank(isFinishedImg)) {
                if (isFinishedImg.equals("是")) {
                    productSaleDTO.setIsFinishedImg(1);
                } else {
                    productSaleDTO.setIsFinishedImg(2);
                }
            }
            /**
             * 视频是否完成
             */
            String isFinishedVideo = dto.getIsFinishedVideo();
            if (StringUtils.isNotBlank(isFinishedVideo)) {
                if (isFinishedVideo.equals("是")) {
                    productSaleDTO.setIsFinishedVideo(1);
                } else {
                    productSaleDTO.setIsFinishedVideo(2);
                }
            }
            /**
             * 退市时间
             */
            if (StringUtils.isNotBlank(dto.getDelistingTime())) {
                productSaleDTO.setDelistingTime(LocalDate.parse(dto.getDelistingTime(), dateTimeFormatter));
            }
            /**
             * 销售状态
             */
            if (StringUtils.isNotBlank(dto.getSaleState())) {
                Integer saleState = SaleStateEnum.getCodeByName(dto.getSaleState());
                productSaleDTO.setSaleState(saleState);
            }
            /**
             * 产品上市(含培训)资料链接
             */
            if(StringUtils.isNotBlank(dto.getDataUrl())){
                productSaleDTO.setDataUrl(dto.getDataUrl());
            }
            /**
             * 是否可销售
             */
            String isMarketable = dto.getIsMarketable();
            if (StringUtils.isNotBlank(isMarketable)) {
                if (isMarketable.equals("是")) {
                    productSaleDTO.setIsMarketable(1);
                } else {
                    productSaleDTO.setIsMarketable(0);
                }
            }
            /**
             * 销售平台
             */
            if(StringUtils.isNotBlank(dto.getSalesPlatform())){
                ProductSalesPlatformEnum productSalesPlatformEnum = ProductSalesPlatformEnum.getByName(dto.getSalesPlatform());
                if (productSalesPlatformEnum == null) {
                    errorMsgList.add("销售平台有误，请输入【全平台】或【亚马逊定制】");
                }else {
                    productSaleDTO.setSalesPlatform(productSalesPlatformEnum.getCode());
                }
            }
            productNoSpecDTO.setProductSaleDTO(productSaleDTO);

            //产品物流信息
            ProductLogisticsDTO productLogisticsDTO = new ProductLogisticsDTO();
            BeanMapper.copy(dto, productLogisticsDTO);
            /**
             * 保险属性
             */
            if(StringUtils.isNotBlank(insurancePropertyName)){
                productLogisticsDTO.setInsuranceProperty(insurancePropertyName);
            }
            /**
             * 报关产品属性
             */
            if(CollUtil.isNotEmpty(declarePropertyList)){
                List<String> productPropertyIds = declarePropertyList.stream().map(BasicDictEntity::getId).collect(Collectors.toList());
                List<String> productPropertyNames = declarePropertyList.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
                productLogisticsDTO.setProductProperty(StringUtils.join(productPropertyNames, ","));
                productLogisticsDTO.setProductPropertyId(StringUtils.join(productPropertyIds, ","));
            }
            /**
             * 报关申报价（$）
             */
            if( MathUtil.valueOf(dto.getDeclarePrice()).compareTo(BigDecimal.ZERO) > 0){
                productLogisticsDTO.setDeclarePrice(MathUtil.valueOf(dto.getDeclarePrice()));
            }
            /**
             * 报关中文名
             */
            if(StringUtils.isNotBlank(dto.getDeclareChineseName())){
                productLogisticsDTO.setDeclareChineseName(dto.getDeclareChineseName());
            }
            /**
             * 报关英文名
             */
            if(StringUtils.isNotBlank(dto.getDeclareEnglishName())){
                productLogisticsDTO.setDeclareEnglishName(dto.getDeclareEnglishName());
            }
            /**
             * 中国海关编码
             */
            if(StringUtils.isNotBlank(dto.getCustomsCode())){
                productLogisticsDTO.setCustomsCode(dto.getCustomsCode());
            }
            /**
             * 报关型号
             */
            if(StringUtils.isNotBlank(dto.getDeclareModel())){
                productLogisticsDTO.setDeclareModel(dto.getDeclareModel());
            }
            /**
             * 报关单位
             */
            if(StringUtils.isNotBlank(dto.getDeclareUnit())){
                productLogisticsDTO.setDeclareUnit(dto.getDeclareUnit());
            }
            /**
             * 申报要素
             */
            if(StringUtils.isNotBlank(dto.getDeclareElement())){
                productLogisticsDTO.setDeclareElement(dto.getDeclareElement());
            }
            /**
             * 英文材质
             */
            if(StringUtils.isNotBlank(dto.getEnglishMaterial())){
                productLogisticsDTO.setEnglishMaterial(dto.getEnglishMaterial());
            }
            /**
             * 英文用途
             */
            if(StringUtils.isNotBlank(dto.getEnglishUsage())){
                productLogisticsDTO.setEnglishUsage(dto.getEnglishUsage());
            }
            productNoSpecDTO.setProductLogisticsDTO(productLogisticsDTO);

            //产品包装信息
            ProductPackDTO productPackDTO = new ProductPackDTO();
            BeanMapper.copy(dto, productPackDTO);
            if(MathUtil.valueOf(dto.getProductLength()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setProductLength(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getProductLength())));
            }
            if(MathUtil.valueOf(dto.getProductWidth()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setProductWidth(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getProductWidth())));
            }
            if(MathUtil.valueOf(dto.getProductHeight()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setProductHeight(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getProductHeight())));
            }
            if(MathUtil.valueOf(dto.getBoxLength()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setBoxLength(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getBoxLength())));
            }
            if(MathUtil.valueOf(dto.getBoxWidth()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setBoxWidth(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getBoxWidth())));
            }
            if(MathUtil.valueOf(dto.getBoxHeight()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setBoxHeight(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getBoxHeight())));
            }
            /**
             * 毛重
             */
            if(MathUtil.valueOf(dto.getGrossWeight()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setGrossWeight(MathUtil.valueOf(dto.getGrossWeight()));
            }
            /**
             * 净重
             */
            if(MathUtil.valueOf(dto.getNetWeight()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setNetWeight(MathUtil.valueOf(dto.getNetWeight()));
            }
            /**
             * 单箱重量
             */
            if(MathUtil.valueOf(dto.getBoxWeight()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setBoxWeight(MathUtil.valueOf(dto.getBoxWeight()));
            }
            /**
             * 单箱数量
             */
            if(MathUtil.valueOf(dto.getBoxQty()).compareTo(BigDecimal.ZERO) > 0){
                productPackDTO.setBoxQty(MathUtil.valueOf(dto.getBoxQty()));
            }
            productNoSpecDTO.setProductPackDTO(productPackDTO);

            //获取产品基本信息修改的字段
            List<ProductDetailDTO.SkuChangeInfoDTO> productBasicChangeField = getProductBasicChangeField(productInfoDTO,null);
            //获取产品包装信息修改的字段
            productPackDTO.setId(productKey.getPackId());
            List<ProductDetailDTO.SkuChangeInfoDTO> productPackChangeField = getProductPackChangeField(productPackDTO,oldPackEntity);
            //发送通知
            ProductDetailDTO.NoticeDTO noticeDTO = new ProductDetailDTO.NoticeDTO();
            noticeDTO.setProductId(productInfoDTO.getId());
            noticeDTO.setName(productInfoDTO.getName());
            noticeDTO.setChargeId(productInfoDTO.getChargeId());
            noticeDTO.setChargeName(productInfoDTO.getChargeName());
            noticeDTO.setSkuNo(productSkuBaseInfoDTO.getSkuNo());
            noticeDTO.setProductBasicChangeField(productBasicChangeField);
            noticeDTO.setProductPackChangeField(productPackChangeField);
            noticeDTOList.add(noticeDTO);
            productIdList.add(productInfoDTO.getId());

            if(importType.equals(ImportTypeEnum.IMPORT_NOT_APPROVAL.getCode())){
                this.inportExcel(productNoSpecDTO);
            }else if(importType.equals(ImportTypeEnum.IMPORT_APPROVAL.getCode())){
                ProductDetailServiceImpl bean = ApplicationContextUtils.getBean(ProductDetailServiceImpl.class);
                bean.inportExcelAndSync(productNoSpecDTO,productBy);
            }
        }
        //发送消息
        handleProductChangeNotification(noticeDTOList,Boolean.FALSE);
        return productIdList;
    }

    //导入新增
    private ExcelImportFsDTO.UrlDTO importAdd(MultipartFile excelFile, Integer importType, HttpServletResponse response) {
        ProductDetailExcelListener excelListenerUtil = new ProductDetailExcelListener();
        try {
            read(excelFile.getInputStream(), ProductDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<ProductDetailExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<ProductDetailExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<ProductDetailExcelDTO> successList = excelListenerUtil.getSuccessList();

        //处理验证成功数据
        List<String> productIdList = handleImportSuccessList(successList, errorList, importType);
        successList.removeAll(errorList);

        String excelPath = "excel/productNoSpecDetail.xlsx";
        String fileName = "productNoSpecDetail.xlsx";
        String errorUrl = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            File file = ExcelUtil.exportFile(excelPath, fileName, errorList);
            if (file != null && !file.isDirectory()) {
                errorUrl = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        String successUrl = "";
        if (CollectionUtils.isNotEmpty(successList)) {
            File file = ExcelUtil.exportFile(excelPath, fileName, successList);
            if (file != null && !file.isDirectory()) {
                successUrl = FastDFSClientUtil.uploadFile(file, fileName);
                //成功添加日志文本
                for (String productId : productIdList) {
                    addProductImportLog(successUrl,productId);
                }
            }
        }
        return new ExcelImportFsDTO.UrlDTO(successUrl,errorUrl);
    }

    private List<String> handleImportSuccessList(List<ProductDetailExcelDTO> successList, List<ProductDetailExcelDTO> errorList, Integer importType) {
        List<String> prodcutIdList = new ArrayList<>();

        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<BasicDictEntity> basicDictList = basicDictService.list();
        List<ProductDetailEntity> productDetailEntityList = this.list();
        List<ProductUnitEntity> unitEntityList = productUnitService.list();
        List<BasicCategoryEntity> categoryEntityList = basicCategoryService.list();
        List<ApplicationCategoryEntity> applicationCategoryList = applicationCategoryService.list();
        Map<String, String> applicationCategoryMap = applicationCategoryList.stream()
                .collect(Collectors.toMap(ApplicationCategoryEntity::getName, ApplicationCategoryEntity::getId, (o1, o2) -> o1));

        //根据供应商名称查询供应商信息
        List<String> mainSupplierNameList = successList.stream().map(req -> req.getMainSupplier()).distinct().collect(Collectors.toList());
        List<String> secondSupplierNameList = successList.stream().map(req -> req.getSecondSupplier()).distinct().collect(Collectors.toList());
        List<String> supplierNameList = new ArrayList<>();
        supplierNameList.addAll(mainSupplierNameList);
        supplierNameList.addAll(secondSupplierNameList);
        List<SupplierEntity> supplierList = scmTaskFeign.listBySupplierByNames(supplierNameList);

        // 获取保险属性字典数据并缓存
        List<BasicDictEntity> dictList = basicDictService.listByType(BasicDictTypeEnum.INSURANCE_PROPERTY.getCode());
        Map<String, BasicDictEntity>  insurancePropertyMap = dictList.stream()
                .collect(Collectors.toMap(BasicDictEntity::getName, entity -> entity));

        for (ProductDetailExcelDTO dto : successList) {
            List<String> errorMsgList = new ArrayList<>();

            ProductDetailEntity productBy = productDetailEntityList.stream().filter(req -> req.getSkuNo().equals(dto.getSkuNo())).findFirst().orElse(null);

            ProductInfoDTO productInfoDTO = new ProductInfoDTO();


            //sku信息
            ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();
            // 判断是修改还是新增 1：新增 2：修改
            //sku重复
            if (ObjectUtil.isNotEmpty(productBy)) {
                errorMsgList.add(ApiError.ERROR_95015.msg);
            }
            //spu名称，新增单规格名称给随机雪花编码
            productInfoDTO.setName(IdUtil.getSnowflake().nextIdStr());
            productInfoDTO.setSpecType(1);
            productInfoDTO.setGrade("");

            String saleCountryStr = "";
            if (StringUtils.isNotBlank(dto.getSaleCountry())) {
                String[] saleCountryList = dto.getSaleCountry().split(",");
                for (String saleCountry : saleCountryList) {
                    DictCountryEntity countryEntity = sysUserFeign.getCountryById(saleCountry);
                    if (ObjectUtils.isEmpty(countryEntity)) {
                        errorMsgList.add("销售国家在系统中未找到");
                        break;
                    }
                    saleCountryStr = saleCountryStr + countryEntity.getId() + ",";
                }
            }

            List<FindUserDTO> chargeNameList = new ArrayList<>();
            if (StringUtils.isNotBlank(dto.getChargeName())) {
                chargeNameList = userList.stream().filter(e -> e.getUserName().equals(dto.getChargeName())).collect(Collectors.toList());
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(chargeNameList)) {
                    errorMsgList.add("产品经理在系统中未找到");
                }
            }

            List<FindUserDTO> purchaseUserList = new ArrayList<>();
            if (StringUtils.isNotBlank(dto.getPurchaseUser())) {
                purchaseUserList = userList.stream().filter(e -> e.getUserName().equals(dto.getPurchaseUser())).collect(Collectors.toList());
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(purchaseUserList)) {
                    errorMsgList.add("采购员在系统中未找到");
                }
            }

            BasicDictEntity productBrand = basicDictList.stream().filter(b -> BasicDictTypeEnum.PRODUCT_BRAND.getCode().equals(b.getType()) && b.getValue().
                    equals(dto.getBrandName())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(productBrand)) {
                errorMsgList.add("产品品牌在系统中未找到");
            }

            //迭代产品校验
            if (ProductTypeEnum.ITERATIVE_PRODUCT.getName().equals(dto.getTypeName())) {
                if (isBlank(dto.getIterateRefSkuNo())) {
                    errorMsgList.add("迭代产品不能为空");
                } else {

                    ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(req -> req.getSkuNo().equals(dto.getIterateRefSkuNo())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(productDetailEntity)) {
                        errorMsgList.add("迭代产品在系统中未找到");
                    } else {
                        if (!ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productDetailEntity.getStatus())) {
                            errorMsgList.add("迭代产品未审核完成不支持引用");
                        }
                        productInfoDTO.setIterateRefSkuId(productDetailEntity.getId());
                    }
                }
            }

            BasicDictEntity productProperty = basicDictList.stream().filter(b -> BasicDictTypeEnum.PRODUCT_PROPERTY.getCode().equals(b.getType()) && b.getValue().
                    equals(dto.getProperty())).findFirst().orElse(null);

            if (ObjectUtils.isEmpty(productProperty)) {
                errorMsgList.add("产品属性在系统中未找到");
            }
            //产品等级
            BasicDictEntity productGrade = basicDictList.stream().filter(b -> BasicDictTypeEnum.PRODUCT_GRADE.getCode().equals(b.getType()) && b.getValue().
                    equals(dto.getGrade())).findFirst().orElse(null);

            if (ObjectUtils.isEmpty(productGrade)) {
                errorMsgList.add("产品等级在系统中未找到");
            }

            ProductUnitEntity productUnitEntity = new ProductUnitEntity();
            if (StringUtils.isNotBlank(dto.getUnitName())) {
                productUnitEntity = unitEntityList.stream().filter(req -> req.getName().equals(dto.getUnitName())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(productUnitEntity)) {
                    errorMsgList.add("单位名称在系统中不存在");
                }
            }

            Integer saleState = null;
            if (StringUtils.isNotBlank(dto.getSaleState())) {
                saleState = SaleStateEnum.getCodeByName(dto.getSaleState());
            }

            Integer purchaseState = null;
            if (StringUtils.isNotBlank(dto.getArrivalState())) {
                purchaseState = PurchaseStateEnum.getCodeByName(dto.getArrivalState());
            }

            List<BasicDictEntity> declarePropertyList = new ArrayList<>();
            if (StringUtils.isNotBlank(dto.getProductProperty())) {
                String[] productPropertyList = dto.getProductProperty().split(",");
                for (String name : productPropertyList) {
                    BasicDictEntity declareProperty = basicDictService.checkBasicDict(BasicDictTypeEnum.DECLARE_PROPERTY.getCode(), name);
                    if (ObjectUtils.isEmpty(declareProperty)) {
                        errorMsgList.add("报关产品属性在系统中未找到");
                    } else {
                        declarePropertyList.add(declareProperty);
                    }
                }

            }

            //保险属性 ,导入新增：不填则默认为无
            String insurancePropertyName = getInsurancePropertyList(dto.getInsuranceProperty(), insurancePropertyMap,errorMsgList);
            if(StringUtils.isBlank(insurancePropertyName)){
                errorMsgList.add("保险属性不能为空");
            }
            dto.setInsuranceProperty(insurancePropertyName);

            //图片是否完成
            String isFinishedImg = dto.getIsFinishedImg();
            //视频是否完成
            String isFinishedVideo = dto.getIsFinishedVideo();
            //产品开发状态
            String productState = dto.getProductStateName();
            if (StringUtils.isNotBlank(productState)) {
                Integer code = ProductDetailStateEnum.getCodeByName(productState);
                productSkuBaseInfoDTO.setProductState(code);
            }

            //产品分类
            String category = dto.getMainCategory();
            BasicCategoryEntity basicCategoryEntity = categoryEntityList.stream().filter(req -> req.getName().equals(category) && req.getPid().equals("0")).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(basicCategoryEntity)) {
                errorMsgList.add("产品分类一级类目不存在");
            } else {
                //父级品类
                List<BasicCategoryEntity> categoryList = new ArrayList<>();
                this.setParentEntity(basicCategoryEntity.getId(), categoryList, categoryEntityList);
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(categoryList)) {
                    errorMsgList.add(ApiError.ERROR_95091.msg);
                }
                //一级品类
                BasicCategoryEntity bestEntity = categoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(bestEntity) || StringUtils.isBlank(bestEntity.getCode())) {
                    errorMsgList.add(ApiError.ERROR_95091.msg);
                }
                //二级品类
                String secondaryCategory = dto.getSecondaryCategory();
                BasicCategoryEntity secondaryCategoryEntity = categoryEntityList.stream().filter(req -> req.getName().equals(secondaryCategory) && !req.getPid().equals("0")).findFirst().orElse(null);

                if (ObjectUtils.isEmpty(secondaryCategoryEntity)) {
                    productInfoDTO.setCategory(bestEntity.getName());
                    productInfoDTO.setCategoryId(bestEntity.getId());
                } else {
                    BasicCategoryEntity secondEntity = categoryList.stream().filter(obj -> secondaryCategoryEntity.getPid().equals(obj.getId())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(secondEntity) || StringUtils.isBlank(secondaryCategoryEntity.getCode())) {
                        errorMsgList.add(ApiError.ERROR_95092.msg);
                    }
                    if (!bestEntity.getId().equals(secondaryCategoryEntity.getPid())) {
                        errorMsgList.add("产品分类一级类目和二级类目的关系不匹配");
                    }
                    productInfoDTO.setCategory(secondaryCategory);
                    productInfoDTO.setCategoryId(secondaryCategoryEntity.getId());
                }
                //存在错误信息则返回
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(dto);
                    continue;
                }
            }

            List<String> applicationCategoryNameList = Arrays.stream(dto.getApplicationCategoryName().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            List<String> applicationCategoryIdList = new ArrayList<>();
            for (String applicationCategoryName : applicationCategoryNameList) {
                String applicationCategoryId = applicationCategoryMap.get(applicationCategoryName);
                if (StringUtils.isBlank(applicationCategoryId)) {
                    errorMsgList.add(applicationCategoryName+" 应用分类不存在");
                    continue;
                }
                applicationCategoryIdList.add(applicationCategoryId);
            }
            productInfoDTO.setApplicationCategoryId(String.join(",", applicationCategoryIdList));

            //存在侵权风险
            String pirateRisk = dto.getPirateRisk();
            if (StringUtils.isNotBlank(pirateRisk)) {
                if (pirateRisk.equals("是")) {
                    productInfoDTO.setPirateRisk(1);
                } else {
                    productInfoDTO.setPirateRisk(2);
                }
            }
            //是否客户定制
            String isCustomized = dto.getIsCustomized();
            if (StringUtils.isNotBlank(isCustomized)) {
                if (isCustomized.equals("是")) {
                    productInfoDTO.setIsCustomized(1);
                } else {
                    productInfoDTO.setIsCustomized(0);
                }
            }

            ProductSalesPlatformEnum productSalesPlatformEnum = ProductSalesPlatformEnum.getByName(dto.getSalesPlatform());
            if (productSalesPlatformEnum == null) {
                errorMsgList.add("销售平台有误，请输入【全平台】或【亚马逊定制】");
            }

            // 一级供应商
            String mainSupplier = dto.getMainSupplier();

            //二级供应商
            String secondSupplier = dto.getSecondSupplier();


            if (StringUtils.isNotBlank(mainSupplier)) {
                SupplierEntity mainSupplierDb = supplierList.stream().filter(s -> s.getName().equals(mainSupplier)).
                        findFirst().orElse(null);
                if (Objects.isNull(mainSupplierDb)) {
                    errorMsgList.add("一级供应商不存在");
                } else {
                    dto.setMainSupplier(mainSupplierDb.getId());
                }
            }

            if (StringUtils.isNotBlank(secondSupplier)) {
                SupplierEntity secondSupplierDb = supplierList.stream().filter(s -> s.getName().equals(secondSupplier)).
                        findFirst().orElse(null);
                if (Objects.isNull(secondSupplierDb)) {
                    errorMsgList.add("二级供应商不存在");
                } else {
                    dto.setSecondSupplier(secondSupplierDb.getId());
                }
            }

            //正常情况下箱规尺寸>=包装尺寸，毛重>=净重
            if(MathUtil.valueOf(dto.getBoxLength()).compareTo(MathUtil.valueOf(dto.getProductLength()))<0){
                errorMsgList.add(ApiError.ERROR_LENGTH_BOX_LITTER_THAN_PRODUCT.msg);
            }
            if(MathUtil.valueOf(dto.getBoxWidth()).compareTo(MathUtil.valueOf(dto.getProductWidth()))<0){
                errorMsgList.add(ApiError.ERROR_WIDTH_BOX_LITTER_THAN_PRODUCT.msg);
            }
            if(MathUtil.valueOf(dto.getBoxHeight()).compareTo(MathUtil.valueOf(dto.getProductHeight()))<0){
                errorMsgList.add(ApiError.ERROR_HEIGHT_BOX_LITTER_THAN_PRODUCT.msg);
            }
            if(MathUtil.valueOf(dto.getGrossWeight()).compareTo(MathUtil.valueOf(dto.getNetWeight()))<0){
                errorMsgList.add(ApiError.ERROR_WEIGHT_GROSS_LITTER_THAN_NET.msg);
            }

            //存在错误信息则返回
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(dto);
                continue;
            }
            ProductNoSpecDTO productNoSpecDTO = new ProductNoSpecDTO();


            productInfoDTO.setBrandId(productBrand.getId());
            productInfoDTO.setBrandName(productBrand.getValue());
            productInfoDTO.setApprovalStatus(0);

            productInfoDTO.setIsNoSpecAdd(MathUtil.ONE);
            //sku 经理
            if (!com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(chargeNameList)) {
                productInfoDTO.setChargeName(chargeNameList.get(0).getUserName());
                productInfoDTO.setChargeId(chargeNameList.get(0).getUserId());
                productSkuBaseInfoDTO.setChargeName(chargeNameList.get(0).getUserName());
                productSkuBaseInfoDTO.setChargeId(chargeNameList.get(0).getUserId());
                //给sku 产品经理id
                dto.setChargeId(chargeNameList.get(0).getUserId());
            }
            productInfoDTO.setMaterials(dto.getMaterials());
            productInfoDTO.setFunctionDesc(dto.getFunctionDesc());
            productInfoDTO.setSellSpot(dto.getSellSpot());
            productInfoDTO.setSaleMethod(dto.getSaleMethod());
            productInfoDTO.setUsageDesc(dto.getUsageDesc());
            productInfoDTO.setProperty(productProperty.getValue());
            productInfoDTO.setPropertyId(productProperty.getId());
            productInfoDTO.setNameEn(dto.getNameEn());
            productInfoDTO.setGrade(productGrade.getValue());
            productInfoDTO.setGradeId(productGrade.getId());
            productInfoDTO.setSalesChannel(dto.getSalesChannel());
            productInfoDTO.setMoldCost(MathUtil.valueOf(dto.getMoldCost()));
            productInfoDTO.setEntrustedDevelopCost(MathUtil.valueOf(dto.getEntrustedDevelopCost()));
            // 添加默认spu
            productInfoDTO.setSpuNo(Optional.ofNullable(dto.getSpuNo()).orElse(dto.getSkuNo()));
            //检查spu编号是否重复
            if (Boolean.TRUE.equals(this.checkSpuNo(productInfoDTO.getSpuNo(), productInfoDTO.getId()))) {
                throw new ServiceException(ApiError.ERROR_95017);
            }
            BeanUtil.copyProperties(dto, productSkuBaseInfoDTO);
            //sku信息
            if (StringUtils.isNotBlank(dto.getPlanListingTimeStr())) {
                productSkuBaseInfoDTO.setPlanListingTime(LocalDate.parse(dto.getPlanListingTimeStr(), dateTimeFormatter));
            }
            productSkuBaseInfoDTO.setProductId("");
            if (ObjectUtil.isNotEmpty(productUnitEntity)) {
                productSkuBaseInfoDTO.setUnitId(productUnitEntity.getId());
                productSkuBaseInfoDTO.setUnitName(productUnitEntity.getName());
            }
            productSkuBaseInfoDTO.setProductState(ProductDetailStateEnum.getCodeByName(productState));

            //spu/sku基础信息
            ProductBaseInfoDTO productBaseInfoDTO = new ProductBaseInfoDTO();
            productInfoDTO.setName(productSkuBaseInfoDTO.getName());
            productInfoDTO.setNameEn(productSkuBaseInfoDTO.getNameEn());
            productBaseInfoDTO.setProductSpuBaseInfoDTO(productInfoDTO);
            productBaseInfoDTO.setProductSkuBaseInfoDTO(productSkuBaseInfoDTO);
            productNoSpecDTO.setProductBaseInfoDTO(productBaseInfoDTO);

            //产品成本信息表
            ProductCostDTO productCostDTO = new ProductCostDTO();
            /**
             * 预计立项成本(￥)
             */
            productCostDTO.setProjectApprovalCost(MathUtil.valueOf(dto.getProjectApprovalCost()));
            /**
             * 实际量产成本(￥)
             */
            productCostDTO.setMassCost(MathUtil.valueOf(dto.getMassCost()));
            /**
             * 预计项目成本(￥)
             */
            productCostDTO.setProjectCost(MathUtil.valueOf(dto.getProjectCost()));
            /**
             *税率
             */
            productCostDTO.setTaxRate(MathUtil.valueOf(dto.getTaxRate()));
            /**
             *目标含税成本(￥)
             */
            productCostDTO.setTargetTaxCost(MathUtil.valueOf(dto.getTargetTaxCost()));
            /**
             *目标不含税成本(￥)
             */
            productCostDTO.setTargetNoTaxCost(MathUtil.valueOf(dto.getTargetNoTaxCost()));
            /**
             *标准零售价(￥)
             */
            productCostDTO.setRetailPrice(MathUtil.valueOf(dto.getRetailPrice()));
            productNoSpecDTO.setProductCostDTO(productCostDTO);

            //采购信息信息
            ProductPurchaseDTO productPurchaseDTO = new ProductPurchaseDTO();
            /**
             * ean码
             */
            productPurchaseDTO.setEan(dto.getEan());
            /**
             * MOQ(最小起订量)
             */
            productPurchaseDTO.setMoq(MathUtil.valueOfInteger(dto.getMoq()));
            /**
             * 试产数量
             */
            productPurchaseDTO.setTrialProductionQty(MathUtil.valueOfLong(dto.getTrialProductionQty()));
            /**
             * 首批量产数量
             */
            productPurchaseDTO.setFirstMassQty(MathUtil.valueOfLong(dto.getFirstMassQty()));
            /**
             * 计划首批下单量
             */
            productPurchaseDTO.setPlanOrderQty(MathUtil.valueOfLong(dto.getPlanOrderQty()));
            /**
             * 实际首批到货量
             */
            productPurchaseDTO.setActualArrivalQty(MathUtil.valueOfLong(dto.getActualArrivalQty()));
            /**
             * 预计首批到货时间
             */
            if (StringUtils.isNotBlank(dto.getPlanArrivalTimeStr())) {
                productPurchaseDTO.setPlanArrivalTime(LocalDate.parse(dto.getPlanArrivalTimeStr(), dateTimeFormatter));
            }
            /**
             * 实际首批到货时间
             */
            if (StringUtils.isNotBlank(dto.getActualArrivalTimeStr())) {
                productPurchaseDTO.setActualArrivalTime(LocalDate.parse(dto.getActualArrivalTimeStr(), dateTimeFormatter));
            }
            /**
             * 首批下单时间
             */
            if (StringUtils.isNotBlank(dto.getPlaceOrderTimeStr())) {
                productPurchaseDTO.setPlaceOrderTime(LocalDate.parse(dto.getPlaceOrderTimeStr(), dateTimeFormatter));
            }
            /**
             * 交货周期(天)
             */
            productPurchaseDTO.setDeliveryCycle(MathUtil.valueOf(dto.getDeliveryCycle()));
            /**
             * 采购员
             */
            if (purchaseUserList.size() > 0) {
                productPurchaseDTO.setPurchaseUserId(purchaseUserList.get(0).getUserId());
            }
            /**
             * 首批到货状态
             */
            productPurchaseDTO.setArrivalState(purchaseState);
            /**
             * 一级供应商
             */
            productPurchaseDTO.setMainSupplier(dto.getMainSupplier());

            /**
             * 二级供应商
             */
            productPurchaseDTO.setSecondSupplier(dto.getSecondSupplier());
            productNoSpecDTO.setProductPurchaseDTO(productPurchaseDTO);

            //产品销售信息
            ProductSaleDTO productSaleDTO = new ProductSaleDTO();

            /**
             * 年目标销量
             */
            productSaleDTO.setYearSaleQty(MathUtil.valueOfLong(dto.getYearSaleQty()));

            /**
             * 年目标销售额（￥）
             */
            productSaleDTO.setYearSaleAmount(MathUtil.valueOf(dto.getYearSaleAmount()));
            /**
             * 目标月销售量
             */
            productSaleDTO.setMonthSaleQty(MathUtil.valueOfLong(dto.getMonthSaleQty()));
            /**
             * 目标月销售额（￥）
             */
            productSaleDTO.setMonthSaleAmount(MathUtil.valueOf(dto.getMonthSaleAmount()));
            /**
             * 首季度目标销量
             */
            productSaleDTO.setTargetSalesQty(MathUtil.valueOf(dto.getTargetSalesQty()));
            /**
             * 销售国家
             */
            if (StringUtils.isNotBlank(saleCountryStr)) {
                productSaleDTO.setSaleCountry(saleCountryStr.substring(0, saleCountryStr.length() - 1));
            }
            /**
             * 图片是否完成
             */
            if (StringUtils.isNotBlank(isFinishedImg)) {
                if (isFinishedImg.equals("是")) {
                    productSaleDTO.setIsFinishedImg(1);
                } else {
                    productSaleDTO.setIsFinishedImg(2);
                }
            }
            /**
             * 视频是否完成
             */
            if (StringUtils.isNotBlank(isFinishedVideo)) {
                if (isFinishedVideo.equals("是")) {
                    productSaleDTO.setIsFinishedVideo(1);
                } else {
                    productSaleDTO.setIsFinishedVideo(2);
                }
            }
            /**
             * 退市时间
             */
            if (StringUtils.isNotBlank(dto.getDelistingTimeStr())) {
                productSaleDTO.setDelistingTime(LocalDate.parse(dto.getDelistingTimeStr(), dateTimeFormatter));
            }
            /**
             * 销售状态
             */
            productSaleDTO.setSaleState(saleState);


            /**
             * 产品上市(含培训)资料链接
             */
            productSaleDTO.setDataUrl(dto.getDataUrl());

            /**
             * 是否可销售
             */
            String isMarketable = dto.getIsMarketable();
            if (StringUtils.isNotBlank(isMarketable)) {
                if (isMarketable.equals("是")) {
                    productSaleDTO.setIsMarketable(1);
                } else {
                    productSaleDTO.setIsMarketable(0);
                }
            }
            /**
             * 销售平台
             */
            productSaleDTO.setSalesPlatform(productSalesPlatformEnum.getCode());
            productNoSpecDTO.setProductSaleDTO(productSaleDTO);

            //产品物流信息
            ProductLogisticsDTO productLogisticsDTO = new ProductLogisticsDTO();
            BeanMapper.copy(dto, productLogisticsDTO);
            /**
             * 保险属性
             */
            productLogisticsDTO.setInsuranceProperty(insurancePropertyName);
            /**
             * 报关产品属性
             */
            List<String> productPropertyIds = declarePropertyList.stream().map(BasicDictEntity::getId).collect(Collectors.toList());
            List<String> productPropertyNames = declarePropertyList.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
            productLogisticsDTO.setProductProperty(StringUtils.join(productPropertyNames, ","));
            productLogisticsDTO.setProductPropertyId(StringUtils.join(productPropertyIds, ","));
            /**
             * 报关申报价（$）
             */
            productLogisticsDTO.setDeclarePrice(MathUtil.valueOf(dto.getDeclarePrice()));
            /**
             * 报关中文名
             */
            productLogisticsDTO.setDeclareChineseName(dto.getDeclareChineseName());
            /**
             * 报关英文名
             */
            productLogisticsDTO.setDeclareEnglishName(dto.getDeclareEnglishName());
            /**
             * 中国海关编码
             */
            productLogisticsDTO.setCustomsCode(dto.getCustomsCode());
            /**
             * 报关型号
             */
            productLogisticsDTO.setDeclareModel(dto.getDeclareModel());
            /**
             * 报关单位
             */
            productLogisticsDTO.setDeclareUnit(dto.getDeclareUnit());
            /**
             * 申报要素
             */
            productLogisticsDTO.setDeclareElement(dto.getDeclareElement());
            /**
             * 英文材质
             */
            productLogisticsDTO.setEnglishMaterial(dto.getEnglishMaterial());
            /**
             * 英文用途
             */
            productLogisticsDTO.setEnglishUsage(dto.getEnglishUsage());
            productNoSpecDTO.setProductLogisticsDTO(productLogisticsDTO);

            //产品包装信息
            ProductPackDTO productPackDTO = new ProductPackDTO();
            BeanMapper.copy(dto, productPackDTO);
            productPackDTO.setProductLength(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getProductLength())));
            productPackDTO.setProductWidth(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getProductWidth())));
            productPackDTO.setProductHeight(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getProductHeight())));
            productPackDTO.setBoxLength(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getBoxLength())));
            productPackDTO.setBoxWidth(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getBoxWidth())));
            productPackDTO.setBoxHeight(LengthConverterUtil.cmToMm(MathUtil.valueOf(dto.getBoxHeight())));
            /**
             * 毛重
             */
            productPackDTO.setGrossWeight(MathUtil.valueOf(dto.getGrossWeight()));
            /**
             * 净重
             */
            productPackDTO.setNetWeight(MathUtil.valueOf(dto.getNetWeight()));
            /**
             * 单箱重量
             */
            productPackDTO.setBoxWeight(MathUtil.valueOf(dto.getBoxWeight()));
            /**
             * 单箱数量
             */
            productPackDTO.setBoxQty(MathUtil.valueOf(dto.getBoxQty()));
            productNoSpecDTO.setProductPackDTO(productPackDTO);

            String productId = this.inportExcel(productNoSpecDTO);
            prodcutIdList.add(productId);
        }
        return prodcutIdList;
    }

    /**
     * list加入父级品类
     */
    private void setParentEntity(String pid, List<BasicCategoryEntity> list, List<BasicCategoryEntity> baseCategoryList) {
        BasicCategoryEntity basicCategoryEntity = baseCategoryList.stream().filter(req -> req.getId().equals(pid)).findFirst().orElse(null);
        if (ObjectUtils.isNotEmpty(basicCategoryEntity)) {
            list.add(basicCategoryEntity);
            if (!"0".equals(basicCategoryEntity.getPid())) {
                setParentEntity(basicCategoryEntity.getPid(), list, baseCategoryList);
            }
        }
    }

    @Override
    public PagingVO<ProductDetailDTO.SkuDTO> listSku(PagingDTO<ProductSkuDTO> pagingDTO) {
        if(StringUtils.isEmpty(pagingDTO.getParams().getRemoteSearchSku())){
            return new PagingVO<>();
        }
        List<String> saleMethodList = pagingDTO.getParams().getSaleMethodList();
        List<String> saleMethodParams = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(saleMethodList)) {
            for (String saleMethod : saleMethodList) {
                saleMethodParams.add(SaleMethodEnum.getNameByCode(Integer.valueOf(saleMethod)));
            }
            pagingDTO.getParams().setSaleMethod(StringUtils.join(saleMethodParams, ","));
        }
        Page<ProductDetailDTO.SkuDTO> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ProductDetailDTO.SkuDTO> pageData=  baseMapper.listSku(query, pagingDTO.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<SkuInfoSimpleVO> getSimpleSkuInfoByIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        List<SkuInfoSimpleVO> skuList = baseMapper.getSimpleSkuInfoByIds(skuIds);
        if (isEmpty(skuList)) {
            return Collections.emptyList();
        }
        List<String> skuNoList = skuList.stream().map(SkuInfoSimpleVO::getSkuNo).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());
        List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        for (SkuInfoSimpleVO skuVO : skuList) {
            if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(null);
                skuVO.setActualTaxCost(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getCostPrice() : skuVO.getActualTaxCost());
            }
        }
        return skuList;
    }


    @Override
    public List<SkuVO> accessoriesSku(String searchKeyword) {
        return baseMapper.accessoriesSku(searchKeyword, ProductDetailStatusEnum.APPROVAL_PASS.getCode());

    }

    /**
     * 获取已审核sku 未计算目的国申报价数据
     * @return
     */
    @Override
    public List<ProductDetailEntity> getProductDetailByDestDeclarePrice() {
        return baseMapper.getProductDetailByDestDeclarePrice();
    }

    @Override
    public List<SkuVO> getSkuBaseByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        return baseMapper.getSkuBaseBySkuIds(skuIds);
    }
    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param entity
     * @param operate
     */
    private void sendSinglePushTask (ProductDetailEntity entity, String operate) {
        //审核通过发送金蝶
        DmpPushTaskEntity pushTaskEntity = syncKingdeeProductDetailService.syncDataToKingdee(entity, operate);
        syncKingdeeProductDetailService.syncDataToSdy(entity, operate);
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(pushTaskEntity));
            }
        });
    }
    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    @Override
    public void sendPushTask (List<ProductDetailEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeProductDetailService.syncDataToKingdee(obj, operate);
            syncKingdeeProductDetailService.syncDataToSdy(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }
    /**
     * 根据skuid 集合获取到sku基础信息 + 采购信息（产品采购信息+产品采购含税单价）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    @Override
    public List<SkuVO> listSkuCostByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.listSkuCostByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)){
            return Collections.emptyList();
        }
        List<String> skuNoList = skuList.stream().map(SkuVO::getSkuNo).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());
        List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        for (SkuVO skuVO : skuList) {
            if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(null);
                skuVO.setActualTaxCost(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getCostPrice() : skuVO.getActualTaxCost());
                skuVO.setNotTaxCostPrice(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getNotTaxCostPrice() : skuVO.getNotTaxCostPrice());
            }
        }
        return skuList;
    }

    @Override
    public List<SkuVO> listSkuProductByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.listSkuProductByIds(skuIds);
        return skuList;
    }

    @Override
    public List<SkuVO> listSkuAllAttributeByIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuVOS = redisUtil.multiGet(RedisKeyConstant.LIST_SKU_INFO, skuIds);
        //过滤空数据
        skuVOS = skuVOS.stream().filter(Objects::nonNull).collect(Collectors.toList());
        //汇总已查询到的sku
        List<String> existSkuIds = skuVOS.stream().filter(Objects::nonNull).map(SkuVO::getSkuId).collect(Collectors.toList());
        List<String> noExistSkuIds = skuIds.stream().filter(e -> CollectionUtils.isEmpty(existSkuIds)
                || !existSkuIds.contains(e)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(noExistSkuIds)){
            return skuVOS;
        }
        List<SkuVO> skuInfos = this.getSkuInfoBySkuIds(noExistSkuIds);
        if (CollectionUtils.isEmpty(skuInfos)){
            return skuVOS;
        }
        redisUtil.putAllHashMap(RedisKeyConstant.LIST_SKU_INFO, skuInfos.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity())));
        if (CollectionUtils.isEmpty(skuVOS)){
            skuVOS = skuInfos;
        }else {
            skuVOS.addAll(skuInfos);
        }
        return skuVOS;
    }

    @Override
    public List<SkuVO> listSkuPackByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.listSkuPackByIds(skuIds);
        return skuList;
    }

    @Override
    public List<SkuVO> listSkuSaleByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.listSkuSaleByIds(skuIds);
        return skuList;
    }

    @Override
    public List<SkuVO> listSkuLogisticsByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.listSkuLogisticsByIds(skuIds);
        this.handleProperty(skuList);
        return skuList;
    }

    private void handleProperty(List<SkuVO> skuList) {
        List<BasicDictEntity> allBasicDictEntities = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());
        skuList.forEach(v->{
            SkuVO.PropertyDTO propertyDTO =new SkuVO.PropertyDTO();
            v.setPropertyDTO(propertyDTO);
            if(StringUtils.isBlank(v.getProductPropertyId())){
                return;
            }
            List<String> propertyIds = Arrays.asList(v.getProductPropertyId().split(","));
            List<String> propertyNameList = allBasicDictEntities.stream().filter(t->propertyIds.contains(t.getId())).map(BasicDictEntity::getName).collect(Collectors.toList());
            propertyDTO.setIsElectric(propertyNameList.stream().anyMatch(t->t.contains("电") && !t.contains("充电盒")));
            propertyDTO.setElectricName(propertyNameList.stream().filter(t->t.contains("电") && !t.contains("充电盒")).collect(Collectors.joining(",")));
            propertyDTO.setIsMagnetism(propertyNameList.stream().anyMatch(t->t.contains("磁")));
            propertyDTO.setMagnetismName(propertyNameList.stream().filter(t->t.contains("磁")).collect(Collectors.joining(",")));
            propertyDTO.setIsLiquid(propertyNameList.stream().anyMatch(t->t.contains("液体")));
            propertyDTO.setLiquidName(propertyNameList.stream().filter(t->t.contains("液体")).collect(Collectors.joining(",")));
            propertyDTO.setIsWood(propertyNameList.stream().anyMatch(t->t.contains("木")));
            propertyDTO.setWoodName(propertyNameList.stream().filter(t->t.contains("木")).collect(Collectors.joining(",")));
            propertyDTO.setIsPowder(propertyNameList.stream().anyMatch(t->t.contains("粉末")));
            propertyDTO.setPowderName(propertyNameList.stream().filter(t->t.contains("粉末")).collect(Collectors.joining(",")));
            propertyDTO.setIsPlaster(propertyNameList.stream().anyMatch(t->t.contains("膏体")));
            propertyDTO.setPlasterName(propertyNameList.stream().filter(t->t.contains("膏体")).collect(Collectors.joining(",")));
            propertyDTO.setIsCuttingTool(propertyNameList.stream().anyMatch(t->t.contains("刀具")));
            propertyDTO.setCuttingToolName(propertyNameList.stream().filter(t->t.contains("刀具")).collect(Collectors.joining(",")));
            propertyDTO.setIsOther(propertyNameList.stream().anyMatch(t->t.contains("CCC")));
            propertyDTO.setOtherName(propertyNameList.stream().filter(t->t.contains("CCC")).collect(Collectors.joining(",")));
        });
    }

    @Override
    public List<SkuVO> listSkuCategoryByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.listSkuCategoryByIds(skuIds);
        return skuList;
    }

    @Override
    public List<SkuVO> listSkuPurchaseByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.listSkuPurchaseByIds(skuIds);
        return skuList;
    }

    @Override
    public ProductDetailEntity getBySkuNoOrEan(String skuCode) {

        ProductDetailEntity entity = getOne(Wrappers.<ProductDetailEntity>lambdaQuery().eq(ProductDetailEntity::getSkuNo, skuCode));
        if (ObjectUtil.isEmpty(entity)) {
            ProductPurchaseEntity purchaseEntity = productPurchaseService.getOne(Wrappers.<ProductPurchaseEntity>lambdaQuery()
                    .eq(ProductPurchaseEntity::getEan, skuCode)
                    .last("LIMIT 1")
            );
            if (ObjectUtil.isNotEmpty(purchaseEntity)) {
                entity = getById(purchaseEntity.getSkuId());
            }
        }
        return entity;
    }

    @Override
    public String dimensionalWeightMeasure(DimensionalWeightDTO dto) {
        ProductDetailEntity purchaseEntity = this.getBySkuNoOrEan(dto.getBarCode());
        if(Objects.isNull(purchaseEntity)){
            throw new ServiceException("编码不存在");
        }
        ProductPackEntity productPackEntity = productPackService.getBySkuId(purchaseEntity.getId());
        if(Objects.isNull(productPackEntity)){
            throw new ServiceException("包装信息不存在");
        }
        BigDecimal length = LengthConverterUtil.cmToMm(dto.getLength());
        BigDecimal width = LengthConverterUtil.cmToMm(dto.getWidth());
        BigDecimal height = LengthConverterUtil.cmToMm(dto.getHeight());
        BigDecimal weight = dto.getWeight().multiply(new BigDecimal("1000"));
        String logContent = format("对SKU【{}】更新【包装尺寸长】从{}更新为{}，【包装尺寸宽】从{}更新为{}，【包装尺寸高】从{}更新为{}，【毛重】从{}更新为{}",purchaseEntity.getSkuNo(),productPackEntity.getProductLength(),length,productPackEntity.getProductWidth(),width,productPackEntity.getProductHeight(),height,productPackEntity.getGrossWeight(),weight);
        productPackEntity.setProductLength(length);
        productPackEntity.setProductWidth(width);
        productPackEntity.setProductHeight(height);
        productPackEntity.setGrossWeight(weight);

        BigDecimal boxLength = productPackEntity.getBoxLength().max(length);
        BigDecimal boxWidth = productPackEntity.getBoxWidth().max(width);
        BigDecimal boxHeight = productPackEntity.getBoxHeight().max(height);
        String logContent2 = format("对SKU【{}】更新【箱规尺寸长】从{}更新为{}，【箱规尺寸宽】从{}更新为{}，【箱规尺寸高】从{}更新为{}",purchaseEntity.getSkuNo(),productPackEntity.getBoxLength(),boxLength,productPackEntity.getBoxWidth(),boxWidth,productPackEntity.getBoxHeight(),boxHeight);
        productPackEntity.setBoxLength(boxLength);
        productPackEntity.setBoxWidth(boxWidth);
        productPackEntity.setBoxHeight(boxHeight);

        //获取产品包装信息修改的字段
        ProductPackDTO productPackDTO = new ProductPackDTO();
        BeanMapper.copy(productPackEntity,productPackDTO);
        List<ProductDetailDTO.SkuChangeInfoDTO> productPackChangeField = getProductPackChangeField(productPackDTO,null);

        boolean result = productPackService.updateById(productPackEntity);
        if(result){
            //发送通知
            ProductDetailEntity productDetailEntity = getById(productPackEntity.getSkuId());
            ProductInfoEntity productInfoEntity = productInfoService.getById(productDetailEntity.getProductId());
            ProductDetailDTO.NoticeDTO noticeDTO = new ProductDetailDTO.NoticeDTO();
            noticeDTO.setProductId(productInfoEntity.getId());
            noticeDTO.setName(productInfoEntity.getName());
            noticeDTO.setChargeId(productInfoEntity.getChargeId());
            noticeDTO.setChargeName(productInfoEntity.getChargeName());
            noticeDTO.setSkuNo(productDetailEntity.getSkuNo());
            noticeDTO.setProductPackChangeField(productPackChangeField);
            List<ProductDetailDTO.NoticeDTO> noticeDTOList = Arrays.asList(noticeDTO);
            //发送消息
            handleProductChangeNotification(noticeDTOList,Boolean.FALSE);

            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(purchaseEntity.getProductId())
                    .setBusinessId(purchaseEntity.getId()).setOperation("品质称重").setContent(logContent + logContent2));
        }

        return "操作成功";
    }

    @Override
    public List<SkuVO.ProductChargeInfoDTO> listProductChargeInfoByIds(List<String> skuIds) {
        if(CollectionUtils.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        List<SkuVO.ProductChargeInfoDTO> skuList = baseMapper.listProductChargeInfoByIds(skuIds);
        return skuList;
    }

    @Override
    public void printEan(PrintEanDTO printEanDTO, HttpServletResponse response) {
        // 设置响应头，告诉浏览器返回的是一个 PDF 文件
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"filename.pdf\"");
        // 创建字体
        ProductPrintFormatEnum formatEnum = ProductPrintFormatEnum.valueOf(printEanDTO.getPrintFormat());
        Document document = new Document(new Rectangle(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()),
                UnitConverterUtil.mmToPoints(printEanDTO.getHeight())));
        try (OutputStream out = response.getOutputStream()) {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("net/sf/jasperreports/fonts/dejavu/HarmonyOS_Sans_SC_Regular.ttf");
            BaseFont baseFont = BaseFont.createFont("HarmonyOS_Sans_Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, IOUtils.toByteArray(stream), null);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();
            for (PrintEanDTO.PrintSkuEanDTO dto : printEanDTO.getPrintSkuEanList()) {
                switch (formatEnum) {
                    case SEPARATELY_SKU_EAN:
                        separatelySkuEan(dto, printEanDTO, document, writer, baseFont);
                        break;
                    case EAN:
                        printEan(dto, printEanDTO, document, writer, baseFont);
                        break;
                    case SKU:
                        printSku(dto, printEanDTO, document, writer, baseFont);
                        break;
                    case MERGE_SKU_EAN:
                        mergeSkuEan(dto, printEanDTO, document, writer, baseFont);
                        break;
                    default:
                        throw new ServiceException(ApiError.ERROR_9028);
                }
            }
            document.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015, e.getMessage());
        }
    }

    @Override
    public List<ProductPackViewDTO> listProductPackBySkuIds(List<String> ids) {
        List<String> skuIds = ids.stream()
                .distinct()
                .collect(Collectors.toList());
        return baseMapper.listProductPackBySkuIds(skuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateProductPack(ProductPackViewDTO viewDTO) {
        if (isBlank(viewDTO.getSkuId())) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //校验 【箱规-长宽高】必须大于等于【包装尺寸-长宽高】【为空则忽略不校验】【长，宽，高分开校验】
        checkSizeAndWeight(viewDTO);
        ProductPackEntity entity = new ProductPackEntity();
        BeanUtils.copyProperties(viewDTO, entity);

        //获取产品包装信息修改的字段
        ProductPackDTO productPackDTO = new ProductPackDTO();
        BeanMapper.copy(viewDTO,productPackDTO);
        List<ProductDetailDTO.SkuChangeInfoDTO> productPackChangeField = getProductPackChangeField(productPackDTO,null);
        //发送通知
        ProductDetailEntity productDetailEntity = getById(entity.getSkuId());
        ProductInfoEntity productInfoEntity = productInfoService.getById(productDetailEntity.getProductId());
        ProductDetailDTO.NoticeDTO noticeDTO = new ProductDetailDTO.NoticeDTO();
        noticeDTO.setProductId(productInfoEntity.getId());
        noticeDTO.setName(productInfoEntity.getName());
        noticeDTO.setChargeId(productInfoEntity.getChargeId());
        noticeDTO.setChargeName(productInfoEntity.getChargeName());
        noticeDTO.setSkuNo(productDetailEntity.getSkuNo());
        noticeDTO.setProductPackChangeField(productPackChangeField);
        List<ProductDetailDTO.NoticeDTO> noticeDTOList = Arrays.asList(noticeDTO);
        //发送消息
        handleProductChangeNotification(noticeDTOList,Boolean.TRUE);

        productPackService.saveOrUpdate(entity);
        //产品推送金蝶
        //只同步审核通过的
        if(Objects.nonNull(productDetailEntity) && productDetailEntity.getStatus().equals(ProductDetailStatusEnum.APPROVAL_PASS.getCode())){
            //发送金蝶
            sendPushTask(Collections.singletonList(productDetailEntity), SyncOperateEnum.OPERATE_APPROVE.getCode());

            syncWangDianProductDetailService.syncDataToWangDian(productDetailEntity);

            syncLingXingProductDetailService.syncDataToLingxing(productDetailEntity);
            //增加缓存清除
            redisUtil.hdel(RedisKeyConstant.LIST_SKU_INFO, productDetailEntity.getId());
        }



        //新增操作日志
        String content = format("操作了SKU【{}】，修改字段【包装尺寸长】为【{}】、【包装尺寸宽】为【{}】、【包装尺寸高】为【{}】、【毛重】为【{}】、【净重】为【{}】、【箱规长】为【{}】、【箱规宽】为【{}】、【箱规高】为【{}】、【单箱重量】为【{}】、【单箱数量】为【{}】",productDetailEntity.getSkuNo(),entity.getProductLength(),entity.getProductWidth(),entity.getProductHeight()
                ,entity.getGrossWeight(),entity.getNetWeight(),entity.getBoxLength(),entity.getBoxWidth(),entity.getBoxHeight(),entity.getBoxWeight(),entity.getBoxQty());
        sysLogService.addSysLogByOther(new SysLogEntity().setBusinessId(entity.getSkuId()).setPid(productDetailEntity.getProductId())
                .setOperation("更新包装信息").setContent(content));
        return BatchResultDTO.success(viewDTO.getSkuId(), viewDTO.getSkuNo(), "操作成功");
    }


    private void checkSizeAndWeight(ProductPackViewDTO productPackDTO) {
        if (ObjectUtils.isNotEmpty(productPackDTO)) {
            StringBuilder errMsg = new StringBuilder();
            //包装尺寸
            if (MathUtil.compareTo(BigDecimal.ZERO, productPackDTO.getProductLength()) >= 0  || MathUtil.compareTo(BigDecimal.ZERO, productPackDTO.getProductWidth()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, productPackDTO.getProductHeight()) >= 0) {
                errMsg.append(format(ApiError.ERROR_PRODUCT_SIZE_NOT_EXIST.msg, productPackDTO.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //箱规
            if (MathUtil.compareTo(BigDecimal.ZERO, productPackDTO.getBoxLength()) >= 0 ||MathUtil.compareTo(BigDecimal.ZERO, productPackDTO.getBoxWidth()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, productPackDTO.getBoxHeight()) >= 0) {
                errMsg.append(format(ApiError.ERROR_BOX_SIZE_NOT_EXIST.msg, productPackDTO.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //毛重
            if (MathUtil.compareTo(productPackDTO.getGrossWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(format(ApiError.ERROR_GROSS_WEIGHT_NOT_EXIST.msg, productPackDTO.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //单箱重量
            if (MathUtil.compareTo(productPackDTO.getBoxWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(format(ApiError.ERROR_BOX_WEIGHT_NOT_EXIST.msg, productPackDTO.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //净重
            if (MathUtil.compareTo(productPackDTO.getNetWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(format(ApiError.ERROR_NET_WEIGHT_NOT_EXIST.msg, productPackDTO.getSkuNo())).append(ProductConstant.HTML_BR);
            }
            //单箱数量
            if (MathUtil.compareTo(productPackDTO.getBoxQty(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(format(ApiError.ERROR_BOX_QTY_NOT_EXIST.msg, productPackDTO.getSkuNo()));
            }
            if (isNotBlank(errMsg)) {
                throw new ServiceException(errMsg.toString());
            }


            compareDimensions(productPackDTO.getBoxLength(), productPackDTO.getProductLength(), ApiError.ERROR_LENGTH_BOX_LITTER_THAN_PRODUCT);
            compareDimensions(productPackDTO.getBoxWidth(), productPackDTO.getProductWidth(), ApiError.ERROR_WIDTH_BOX_LITTER_THAN_PRODUCT);
            compareDimensions(productPackDTO.getBoxHeight(), productPackDTO.getProductHeight(), ApiError.ERROR_HEIGHT_BOX_LITTER_THAN_PRODUCT);
            //毛重大于等于净重
            compareDimensions(productPackDTO.getGrossWeight(), productPackDTO.getNetWeight(), ApiError.ERROR_WEIGHT_GROSS_LITTER_THAN_NET);
        }
    }

    @Override
    public void uploadSkuImage(UploadSkuDTO dto) {
        ProductDetailEntity productDetailEntity = getBySkuNoOrEan(dto.getEan());
        if(Objects.nonNull(productDetailEntity)){
            PlmAttachmentEntity entity = new PlmAttachmentEntity();
            entity.setAttachUrl(dto.getAttachUrl());
            entity.setAttachName(dto.getAttachName());
            Class<ProductDetailEntity> aClass = ProductDetailEntity.class;
            TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
            entity.setType(tableName.value());
            entity.setBusinessId(productDetailEntity.getId());
            plmAttachmentService.save(entity);
        }
    }

    @Override
    public ExcelImportFsDTO.UrlDTO importProductUpdate(MultipartFile excelFile, HttpServletResponse response) {
        List<BasicCategoryEntity> categoryList = basicCategoryService.list();
        List<ApplicationCategoryEntity> categoryEntityList = applicationCategoryService.list();
        Map<String, String> applicationCategoryMap = categoryEntityList.stream().collect(Collectors.toMap(ApplicationCategoryEntity::getName, ApplicationCategoryEntity::getId));
        List<ProductDetailEntity> productDetailEntityList = this.list();
        ProductDetailUpdateExcelListener excelListenerUtil = new ProductDetailUpdateExcelListener(categoryList, applicationCategoryMap, productDetailEntityList);
        try {
            read(excelFile.getInputStream(), ProductDetailUpdateExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<ProductDetailUpdateExcelDTO> errorList = excelListenerUtil.getErrorList();
        List<ProductInfoDTO> successList = excelListenerUtil.getSuccessList();

        String errorUrl = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String excelPath = "excel/productUpdateError.xlsx";
            String fileName = "productUpdateError.xlsx";
            File file = ExcelUtil.exportFile(excelPath, fileName, errorList);
            if (file != null && !file.isDirectory()) {
                errorUrl = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        String successUrl = "";
        if (CollectionUtils.isNotEmpty(successList)) {
            String excelPath = "excel/productUpdateError.xlsx";
            String fileName = "productUpdateError.xlsx";
            File file = ExcelUtil.exportFile(excelPath, fileName, successList);
            if (file != null && !file.isDirectory()) {
                successUrl = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }

        for (ProductInfoDTO productInfoDTO : successList) {
            productInfoService.updateSpec(productInfoDTO);
            //添加导入日志
            addProductImportLog(successUrl,productInfoDTO.getId());
        }
        return new ExcelImportFsDTO.UrlDTO(successUrl,errorUrl);
    }

    /**
     * 合并打印
     * @param dto         sku数据
     * @param printEanDTO 参数
     * @param document    页面元素
     * @param writer      打印
     * @param baseFont    字体
     */
    private void mergeSkuEan(PrintEanDTO.PrintSkuEanDTO dto, PrintEanDTO printEanDTO, Document document, PdfWriter writer, BaseFont baseFont) throws DocumentException, IOException {

        Image image = getImage(dto.getSkuNo(), printEanDTO, writer, baseFont);
        scaleFactor(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()) - 10, image);
        // 计算条形码居中的 X 坐标
        float xPosition = getXPosition(printEanDTO, document, image);
        float barcodeYPosition = document.getPageSize().getHeight() - image.getScaledHeight() - 5;
        Image eanImage = null;
        float eanPosition = 0;
        float eanBarcodeYPosition = 0;
        if (!ObjectUtils.isEmpty(dto.getEan())) {
            eanImage = getImage(dto.getEan(), printEanDTO, writer, baseFont);
            scaleFactor(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()) - 10, eanImage);
            eanPosition = getXPosition(printEanDTO, document, eanImage);
            eanBarcodeYPosition = document.getPageSize().getHeight() - image.getScaledHeight() - eanImage.getScaledHeight() - 15;
        }
        // 绘制条形码
        PdfContentByte canvas = writer.getDirectContent();
        for (int i = 0; i < dto.getQty(); i++) {
            // 将图像添加到 PDF
            canvas.addImage(image, image.getScaledWidth(), 0, 0, image.getScaledHeight(), xPosition, barcodeYPosition);
            if (!ObjectUtils.isEmpty(dto.getEan()) && ObjectUtil.isNotEmpty(eanImage)) {
                canvas.addImage(eanImage, eanImage.getScaledWidth(), 0, 0, eanImage.getScaledHeight(), eanPosition, eanBarcodeYPosition);
            }
            document.newPage(); // 每个条形码添加到新页面
        }
    }

    /**
     * 设置缩放比例
     * @param maxWidth 最大宽度
     * @param image    图片
     */
    private void scaleFactor(float maxWidth, Image image) {
        float scaleFactor = maxWidth / image.getScaledWidth(); // 计算缩放比例
        // 确保条形码不超过最大宽度
        if (scaleFactor < 1) {
            image.scaleAbsolute(maxWidth, image.getScaledHeight() * scaleFactor);
        }
    }

    /**
     * 设置缩放比例
     * @param maxWidth 最大宽度
     * @param scaledWidth    图片宽度
     */
    private float scaleFactorFontSize(float maxWidth, float scaledWidth) {
        float scaleFactor = maxWidth / scaledWidth; // 计算缩放比例
        // 确保条形码不超过最大宽度
        if (scaleFactor < 1) {
            return 8 * scaleFactor;
        }
        return 8 ;
    }

    /**
     * 生成条形码图片
     *
     * @param content     条形码内容
     * @param printEanDTO 参数
     * @param writer      打印
     * @param baseFont    字体
     */
    private Image getImage(String content, PrintEanDTO printEanDTO, PdfWriter writer, BaseFont baseFont) {
        PdfContentByte cb = writer.getDirectContent();
        Barcode128 barcode = new Barcode128();
        barcode.setCode(content);  // 设置条形码的内容
        barcode.setCodeType(Barcode.CODE128);
        barcode.setFont(baseFont);
        barcode.setBarHeight(30f);
        barcode.setTextAlignment(printEanDTO.getTextPosition());
        if (Boolean.FALSE.equals(printEanDTO.getIsPrintText())) {
            barcode.setFont(null);
        }
        return barcode.createImageWithBarcode(cb, null, null);
    }

    /**
     * 打印sku
     *
     * @param dto         sku数据
     * @param printEanDTO 参数
     * @param document    页面元素
     * @param writer      打印
     * @param baseFont    字体
     */
    private void printSku(PrintEanDTO.PrintSkuEanDTO dto, PrintEanDTO printEanDTO, Document document, PdfWriter writer, BaseFont baseFont) throws DocumentException {

        Image image = getImage(dto.getSkuNo(), printEanDTO, writer, baseFont);
        float fontSize = scaleFactorFontSize(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()) - 10, image.getScaledWidth());
        scaleFactor(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()) - 10, image);
        // 计算条形码居中的 X 坐标
        float xPosition = getXPosition(printEanDTO, document, image);
        float barcodeYPosition = document.getPageSize().getHeight() - image.getScaledHeight() - 5;

        Font font = new Font(baseFont, fontSize);
        List<String> textContent = printEanDTO.getTextContent();
        textContent.remove(ProductContentEnum.SKU.getCode());
        StringBuilder text = new StringBuilder();
        int maxTextLength = 0;
        for (String content : textContent) {
            ProductContentEnum contentEnum = ProductContentEnum.ofCode(content);
            String addText = addText(dto, contentEnum, printEanDTO);
            if (ObjectUtils.isEmpty(addText)) {
                continue;
            }
            maxTextLength = Math.max(maxTextLength, addText.length());
            text.append(addText).append("\n");
        }
        // 获取页面宽度
        float pageWidth = document.getPageSize().getWidth();
        // 创建一个 Phrase，并设置字体
        Phrase phrase = new Phrase(fontSize, text.toString(), font);
        float bottomY = barcodeYPosition - (textContent.size() * fontSize);
        for (int i = 0; i < dto.getQty(); i++) {
            PdfContentByte canvas = writer.getDirectContent();
            // 将图像添加到 PDF
            canvas.addImage(image, image.getScaledWidth(), 0, 0, image.getScaledHeight(), xPosition, barcodeYPosition);
            ColumnText ct = new ColumnText(canvas);
            ct.setLeading(fontSize * 1.5f);
            ct.setText(phrase);
            // 设置文本绘制区域
            ct.setSimpleColumn(5, bottomY - 100, pageWidth - 5, barcodeYPosition);
            ct.setAlignment(printEanDTO.getTextPosition());
            // 绘制文本
            ct.go();
            document.newPage(); // 每个条形码添加到新页面
        }
    }

    /**
     * 获取左边起点位置
     *
     * @param printEanDTO 参数
     * @param document    页面
     * @param image       图片
     */
    private float getXPosition(PrintEanDTO printEanDTO, Document document, Image image) {
        if (Element.ALIGN_LEFT == printEanDTO.getTextPosition()) {
            return  5; // 左对齐，距离左边50个单位
        } else if (Element.ALIGN_CENTER == printEanDTO.getTextPosition()) {
            return  (document.getPageSize().getWidth() - image.getScaledWidth()) / 2;
        } else {
            return document.getPageSize().getWidth() - image.getScaledWidth() - 5; // 右对齐，距离右边50个单位
        }
    }

    /**
     * 打印ean
     *
     * @param dto         sku数据
     * @param printEanDTO 参数
     * @param document    页面元素
     * @param writer      打印
     * @param baseFont    字体
     */
    private void printEan(PrintEanDTO.PrintSkuEanDTO dto, PrintEanDTO printEanDTO, Document document, PdfWriter writer, BaseFont baseFont) throws DocumentException, IOException, WriterException {
        if (ObjectUtils.isEmpty(dto.getEan())) {
            return;
        }
        Image image = getImage(dto.getEan(), printEanDTO, writer, baseFont);
        float fontSize = scaleFactorFontSize(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()) - 10, image.getScaledWidth());
        scaleFactor(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()) - 10, image);
        // 计算条形码居中的 X 坐标
        float xPosition = getXPosition(printEanDTO, document, image);
        float barcodeYPosition = document.getPageSize().getHeight() - image.getScaledHeight() - 5;
        Font font = new Font(baseFont, fontSize);
        List<String> textContent = printEanDTO.getTextContent();
        textContent.remove(ProductContentEnum.EAN.getCode());
        StringBuilder text = new StringBuilder();
        int maxTextLength = 0;
        for (String content : textContent) {
            ProductContentEnum contentEnum = ProductContentEnum.ofCode(content);
            String addText = addText(dto, contentEnum, printEanDTO);
            if (ObjectUtils.isEmpty(addText)) {
                continue;
            }
            maxTextLength = Math.max(maxTextLength, addText.length());
            text.append(addText).append("\n");
        }
        // 获取页面宽度
        float pageWidth = document.getPageSize().getWidth();
        // 创建一个 Phrase，并设置字体
        Phrase phrase = new Phrase(fontSize, text.toString(), font);
        float bottomY = barcodeYPosition - (textContent.size() * fontSize);
        for (int i = 0; i < dto.getQty(); i++) {
            PdfContentByte canvas = writer.getDirectContent();
            // 将图像添加到 PDF
            canvas.addImage(image, image.getScaledWidth(), 0, 0, image.getScaledHeight(), xPosition, barcodeYPosition);
            ColumnText ct = new ColumnText(canvas);
            ct.setLeading(fontSize * 1.5f);
            ct.setText(phrase);
            // 设置文本绘制区域
            ct.setSimpleColumn(5, bottomY - 100, pageWidth - 5, barcodeYPosition);
            ct.setAlignment(printEanDTO.getTextPosition());
            // 绘制文本
            ct.go();
            document.newPage(); // 每个条形码添加到新页面
        }
    }

    /**
     * 分开打印
     *
     * @param dto         sku数据
     * @param printEanDTO 打印参数
     * @param document    元素
     * @param writer      写流
     * @param baseFont    字体
     */
    private void separatelySkuEan(PrintEanDTO.PrintSkuEanDTO dto, PrintEanDTO printEanDTO, Document document, PdfWriter writer, BaseFont baseFont) throws DocumentException {
        PdfContentByte canvas = writer.getDirectContent();
        Image image = getImage(dto.getSkuNo(), printEanDTO, writer, baseFont);
        scaleFactor(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()) - 10, image);
        // 计算条形码居中的 X 坐标
        float xPosition = getXPosition(printEanDTO, document, image);
        float barcodeYPosition = document.getPageSize().getHeight() - image.getScaledHeight() - 5;
        for (int i = 0; i < dto.getQty(); i++) {
            // 将图像添加到 PDF
            canvas.addImage(image, image.getScaledWidth(), 0, 0, image.getScaledHeight(), xPosition, barcodeYPosition);
            document.newPage(); // 每个条形码添加到新页面
        }
        if (!ObjectUtils.isEmpty(dto.getEan())) {
            Image eanImage = getImage(dto.getEan(), printEanDTO, writer, baseFont);
            scaleFactor(UnitConverterUtil.mmToPoints(printEanDTO.getWidth()) - 10, eanImage);
            float eanPosition = getXPosition(printEanDTO, document, eanImage);
            float eanBarcodeYPosition = document.getPageSize().getHeight() - eanImage.getScaledHeight() - 5;
            // 绘制条形码
            for (int i = 0; i < dto.getQty(); i++) {
                canvas.addImage(eanImage, eanImage.getScaledWidth(), 0, 0, eanImage.getScaledHeight(), eanPosition, eanBarcodeYPosition);
                document.newPage(); // 每个条形码添加到新页面
            }
        }
    }

    /**
     * 添加文本
     *
     * @param dto         sku数据
     * @param contentEnum 枚举
     * @param printEanDTO 参数
     */
    private String addText(PrintEanDTO.PrintSkuEanDTO dto, ProductContentEnum contentEnum, PrintEanDTO printEanDTO) {
        switch (contentEnum) {
            case SKU:
                return dto.getSkuNo();
            case EAN:
                return Optional.ofNullable(dto.getEan()).orElse("");
            case PRODUCT_NAME:
                return dto.getProductName();
            case CUSTOM:
                return printEanDTO.getCustomText();
            case MADE_IN_CHINA:
                return "MADE IN CHINA";
            case DATE:
                return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            default:
                throw new ServiceException(ApiError.ERROR_9028);
        }
    }

    @Override
    public List<SkuVO> listAllStatusSkuBySkuNos(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        List<SkuVO> skuList = baseMapper.getSkuBySkuNos(skuNoList, null);
        List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        for (SkuVO skuVO : skuList) {
            if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(null);
                skuVO.setActualTaxCost(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getCostPrice() : skuVO.getActualTaxCost());
                skuVO.setNotTaxCostPrice(Objects.nonNull(dmpSkuCost) ? dmpSkuCost.getNotTaxCostPrice() : skuVO.getNotTaxCostPrice());
            }
        }
        return skuList;
    }

}
