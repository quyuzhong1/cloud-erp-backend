package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.IsConstant;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.ReportDataSourceDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.RedisService;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.utils.PdfUtil;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.CommonConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
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
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.CfgSettingFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.server.plm.constant.ProductManyDetailConstant;
import com.erp.server.plm.listener.ProductDetailExcelListener;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeProductDetailService;
import com.erp.server.plm.rocketmq.sync.wangdian.SyncWangDianProductDetailService;
import com.erp.server.plm.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.python.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.ListUtils;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.erp.server.plm.constant.ProductConstant.PRODUCT_PROPERTY_COST;
import static com.erp.server.plm.constant.ProductConstant.PRODUCT_PROPERTY_SERVICE;

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

/*    @Resource
    private WorkflowFeign workflowFeign;*/

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
    private NoticeMessageService noticeMessageService;

    @Resource
    private SyncWangDianProductDetailService syncWangDianProductDetailService;

    @Resource
    private InventoryFeign inventoryFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private FileTemplateFeign fileTemplateFeign;

    //变更财务人员审核
    @Value("${changeFinancialAudit}")
    private String financial;


    private static final String SPUCLASSPATH = String.valueOf(ProductInfoEntity.class);
    private static final String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

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
            //TODO 2023-03-30 暂时取消审核流程 只改状态
/*             LoginUser loginUser = UserContext.getLoginUser();
            List<TaskShowDTO> workflowList = workflowFeign.queryMyToDo(loginUser.getUid());
            //无待办则直接返回
            if (CollectionUtils.isEmpty(workflowList)) {
                IPage<ProductDetailShowDTO> list = new Page<>();
                return new PagingVO(list);
            }
            List<String> processIds = workflowList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
            pagingDTO.getParams().setProcessIds(processIds);*/
            //待审核，审核中
            pagingDTO.getParams().setStatusList(Arrays.asList(ProductDetailStatusEnum.WAIT_CONFIRM.getCode(), ProductDetailStatusEnum.APPROVAL_ING.getCode()));
        }
        if (MathUtil.TWO.toString().equals(pagingDTO.getParams().getType())) {
            //已审核
            pagingDTO.getParams().setStatusList(Arrays.asList(ProductDetailStatusEnum.APPROVAL_PASS.getCode()));
        }
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
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
            return new PagingVO(pageData);
        }
        List<String> productIdList = list.stream().map(ProductDetailShowDTO::getId).collect(Collectors.toList());
        List<ProjectInfoEntity> projectList = projectInfoService.getByProductIdList(productIdList);
        List<String> sourceIds = list.stream().map(ProductDetailShowDTO::getId).collect(Collectors.toList());
        List<String> changeIngSourceIds = productChangeService.getBySourceId(sourceIds);

        List<String> mainSupplierIds = list.stream().map(ProductDetailShowDTO::getMainSupplier).distinct().collect(Collectors.toList());
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(mainSupplierIds);

        List<String> skuIdList = list.stream().map(ProductDetailShowDTO::getSkuId).collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomSkuService.listBomChildBySkuIds(skuIdList);
        for (ProductDetailShowDTO item : list) {
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
        if (ObjectUtil.isNotEmpty(infoEntity)) {
            if (infoEntity.getSpecType() == 2) {
                return getNoSpecDetailById(productId);
            }
        }

        ProductNoSpecDetailAllDTO productNoSpecDetailAllDTO = new ProductNoSpecDetailAllDTO();
        //无规格产品信息明细
        ProductNoDetailDTO noSpecDetailById = productDetailMapper.getNoSpecDetailBySkuId(skuId);

        if (ObjectUtils.isNotEmpty(noSpecDetailById)) {
            //获取多级分类
            List<String> categoryIdList = basicCategoryService.getPidList(noSpecDetailById.getCategoryId());
            noSpecDetailById.setCategoryIdList(categoryIdList);

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
        //校验sku必填项
        ProductDetailDTO detailDTO = new ProductDetailDTO();
        BeanMapper.copy(productSkuBaseInfoDTO, detailDTO);
        ProductDetailEntity detailEntity = new ProductDetailEntity();
        BeanMapper.copy(productSkuBaseInfoDTO, detailEntity);
        detailEntity.setIsChange(IsConstant.NO);
        productUnitService.setupOccupy(Arrays.asList(detailEntity.getUnitId()));
        this.saveOrUpdate(detailEntity);
        //同步到SCM
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_SKU_TAG.getName(), Arrays.asList(detailEntity), IdUtil.simpleUUID());
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_SKU_TAG.getName(), Arrays.asList(detailEntity), IdUtil.simpleUUID());

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
        //同步到SCM
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_SKU_TAG.getName(), list, IdUtil.simpleUUID());
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_SKU_TAG.getName(), list, IdUtil.simpleUUID());

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


        productSkuBaseInfoDTO.setProductId(id);

        //SKU修改操作日志
        Boolean isAdd = false;
        if (StringUtils.isNotBlank(productSkuBaseInfoDTO.getId())) {
            ProductDetailEntity oldEntity = this.getById(productSkuBaseInfoDTO.getId());
            addProductSkuBaseInfoLog(productSkuBaseInfoDTO, oldEntity, productSkuBaseInfoDTO.getId(), id);
        } else {
            isAdd = true;
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
            productCustomsService.saveOrUpdateBatch(customsEntityList);
        }
        //增加默认记录
        productCustomsService.addDefaultCustoms(Collections.singletonList(skuId));
        return true;
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
                && Objects.nonNull(smaller) && smaller.compareTo(BigDecimal.ZERO) > 0) {
            if (larger.compareTo(smaller) < 0) {
                throw new ServiceException(apiError);
            }
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
            productCustomsService.saveOrUpdateBatch(customsEntityList);
        }
        //增加默认记录
        productCustomsService.addDefaultCustoms(productDetailLists.stream().map(ProductDetailDTO::getId).collect(Collectors.toList()));
        return true;
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(String skuId) {
        ProductDetailEntity detailEntity = this.getById(skuId);
        Optional.ofNullable(detailEntity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "产品sku"));
        if (detailEntity.getStatus().equals(1) || detailEntity.getStatus().equals(2)) {
            throw new ServiceException(ApiError.ERROR_95241);
        }
        if (detailEntity.getOccupyStatus()) {
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
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
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
        sendPushTask(Arrays.asList(detailEntity),SyncOperateEnum.OPERATE_DELETE.getCode());
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
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper();
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
        ProductInfoDTO productSpuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSpuBaseInfoDTO();
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO();
        productSpuBaseInfoDTO.setNameEn(productSkuBaseInfoDTO.getNameEn());
        //1.新增产品表 主表信息
        String id = productInfoService.updateSpec(productSpuBaseInfoDTO);

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
//        downloadTaskFeign.saveDownloadTask("产品sku明细表", EXPORT_PLM_PRODUCT_DETAIL.getCode(), productSkuExcelDTO);
        List<ProductDetailExcelExportDTO> list = productDetailMapper.getExportSkuExcel(productSkuExcelDTO);
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

            String chargeId = "";
            String productPropertyId = "";
            String saleCountry = "";
            List<String> chargeIds = new ArrayList<>();
            List<String> productPropertyIdAndSaleCountrys = new ArrayList<>();
            for(ProductDetailExcelExportDTO l : list) {
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
                    req.setProductProperty(Arrays.stream(req.getSaleCountry().split(",")).filter(StringUtils::isNotBlank)
                            .map(c -> finalDictValueMaps.get(c)).filter(d -> d != null).collect(Collectors.joining(",")));
                }
                if(StringUtils.isNotBlank(req.getImageUrl())){
                    String[] imageArr = req.getImageUrl().split(",");
                    req.setImage(FastDFSClientUtil.getFileByte(imageArr[0]));
                }
            });
        }
        ExcelUtil.export("产品sku明细表", "产品sku明细表", list, ProductDetailExcelExportDTO.class, response,productSkuExcelDTO.getExportFields());
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = RedisKeyConstant.CACHE_SKU_NO_INVENTORY, allEntries = true)
    public Boolean approvalPass(ProductDetailOperateDTO dto, Boolean isCheck) {
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
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();

        //TODO 2023-03-30 暂时取消审核流程 只改状态
/*        List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
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
        approveProcess.setComment(dto.getComment());*/
        //审核通过 重算目的国申报单价
        recalDestDeclarePrice(Collections.singletonList(entity));
        //查询审核任务下所有待办
        Integer code = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        //更新产品信息状态
        Boolean flag = this.updateProductDetailState(dto.getId(), code, userId, userName);
        if (flag) {
            //新增操作日志
            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                    .setBusinessId(dto.getId()).setOperation("状态变更").setContent("审核SKU[" + entity.getSkuNo() + "],操作[" + ProductDetailStatusEnum.getName(entity.getStatus()) + "]为[" + ProductDetailStatusEnum.APPROVAL_PASS.getName() + "]，审批意见：" + dto.getComment()));
        }
        //workflowFeign.taskPass(approveProcess);
        //发送通知
        noticeMessageService.approveProductNotice(userName, entity);

        //发送金蝶
        sendPushTask(Arrays.asList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
        if (isCheck) {
            syncWangDianProductDetailService.syncDataToWangDian(entity);
        }
        //增加缓存清除
        redisUtil.hdel(RedisKeyConstant.LIST_SKU_INFO, entity.getId());
        return true;
    }

    /**
     * 重算目的国申报价
     * @param details
     */
    @Override
    public void recalDestDeclarePrice(List<ProductDetailEntity> details) {
        log.info("recalDestDeclarePrice start======{}",details.size());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<String> skuIds = details.stream().filter(Objects::nonNull).map(ProductDetailEntity::getId).distinct().collect(Collectors.toList());
        List<String> skuNoList = details.stream().filter(Objects::nonNull).map(ProductDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(skuIds)) {
            return;
        }
        //目的国申报信息
        List<ProductCustomsEntity> customsEntityList = productCustomsService.listBySkuIds(skuIds, CommonConstants.DEFAULT);
        //系统配置
        CfgSettingEntity setting = cfgSettingFeign.getByKey(CfgSettingEnum.LOGISTICS_PRODUCT_DEST_DECLARE_PRICE.getCode());
        if (Objects.isNull(setting) || Objects.isNull(setting.getDataJson()) || CollectionUtils.isEmpty(setting.getDataJson().getJSONArray("data"))) {
            return;
        }
        List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> data = JSONUtil.toList(setting.getDataJson().getJSONArray("data"), CfgSettingValueDTO.LogisticsProductDestDeclarePrice.class);
        log.info("CfgSettingValueDTO: {}", JSONUtil.toJsonStr(data));
        //sku信息
        List<DmpSkuCostEntity> skuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        BigDecimal usdRate = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), CurrencyEnum.USD.getCurrencyCode());
        if (Objects.isNull(usdRate)) {
            return;
        }
        List<ProductCustomsEntity> addList = new ArrayList<>();
        List<ProductCustomsEntity> updateList = new ArrayList<>();
        for(String skuId : skuIds){
            //目的国申报信息 默认
            ProductCustomsEntity customs = customsEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(skuId)).findFirst().orElse(new ProductCustomsEntity());

            log.info("recalDestDeclarePrice : skuId:{},destDeclarePrice:{}", skuId,customs.getToDeclarePrice());
            //是否重算目的国申报价
            BigDecimal destDeclarePrice = Objects.isNull(customs.getToDeclarePrice()) ? BigDecimal.ZERO: customs.getToDeclarePrice();
            if (destDeclarePrice.compareTo(BigDecimal.ZERO) == 0) {
                DmpSkuCostEntity skuCostDTO = skuCostList.stream().filter(e -> e.getSkuId().equals(skuId)).findFirst().orElse(null);
                log.info("skuCostDTO: {}", JSONUtil.toJsonStr(skuCostDTO));
                if (Objects.isNull(skuCostDTO)) {
                    continue;
                }
                //含税成本 默认是人民币
                BigDecimal actualTaxCost = BigDecimal.ZERO;
                if (Objects.nonNull(skuCostDTO.getCostPrice())) {
                    actualTaxCost = skuCostDTO.getCostPrice();
                }
                //统一换算成美元汇率
                BigDecimal actualTaxCostUsd = MathUtil.divide(actualTaxCost, usdRate);
                log.info("actualTaxCostUsd: {}", actualTaxCostUsd);
                //根据美元计算比例
                CfgSettingValueDTO.LogisticsProductDestDeclarePrice declarePrice = data.stream().filter(e -> e.getStartPrice().compareTo(actualTaxCostUsd) < 0 && e.getEndPrice().compareTo(actualTaxCostUsd) >= 0).findFirst().orElse(null);
                log.info("declarePrice: {}", JSONUtil.toJsonStr(declarePrice));
                if (Objects.isNull(declarePrice) || Objects.isNull(declarePrice.getRate())) {
                    continue;
                }
                BigDecimal resultDestDeclarePrice = actualTaxCostUsd.multiply(declarePrice.getRate()).divide(MathUtil.BigDecimal_100, 4, RoundingMode.HALF_UP);
                log.info("resultDestDeclarePrice: {}", resultDestDeclarePrice);
                customs.setToDeclarePrice(resultDestDeclarePrice);
                customs.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
                customs.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
                if (Objects.isNull(customs.getId())){
                    customs.setCountry(CommonConstants.DEFAULT);
                    addList.add(customs);
                }else {
                    updateList.add(customs);
                }

                log.info("更新目的国申报价 sku:{},目的国申报价：{}",skuCostDTO.getSkuNo(), resultDestDeclarePrice);
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
        log.info("recalDestDeclarePrice end======");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initProductCustom(List<String> skuIds) {
        //有传值 按照传值进行sku同步 没有则同步全量
        List<ProductDetailEntity> list = null;
        if (CollectionUtil.isEmpty(skuIds)){
            list = lambdaQuery().select(ProductDetailEntity::getId).list();
        }else {
            list = lambdaQuery().select(ProductDetailEntity::getId).in(ProductDetailEntity::getId, skuIds).list();
        }
        if (CollectionUtil.isEmpty(list)){
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
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
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
        return this.queryByProductId(newProductId);
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
        if (CollectionUtil.isNotEmpty(addCustomsList)){
            productCustomsService.saveBatch(addCustomsList);
        }
        if (CollectionUtil.isNotEmpty(updateCustomsList)){
            productCustomsService.updateBatchById(updateCustomsList);
        }
    }

    @Override
    public void initProductToWangDian(List<String> ids) {
        int count = productInfoService.count();
        int pageSize = 50;
        int pageCount = count / pageSize + 1;
        for (int i = 0; i < pageCount; i++) {
            Page<ProductInfoEntity> page = productInfoService.page(new Page<>(i, pageSize), Wrappers.<ProductInfoEntity>lambdaQuery().in(CollectionUtil.isNotEmpty(ids), ProductInfoEntity::getId, ids));
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

    private int getGoodsType(String saleMethod, String property){
        SaleMethodEnum saleMethodEnum = SaleMethodEnum.getEnumByType(saleMethod);
        if (org.springframework.util.ObjectUtils.isEmpty(saleMethodEnum)){
            return 0;
        }
        switch (saleMethodEnum){
            case GOODS:return (PRODUCT_PROPERTY_COST.equals(property) || PRODUCT_PROPERTY_SERVICE.equals(property)) ? 5 : 1;
            case PACKAGING_MATERIALS: return 3;
            case SEMI_FINISHED:return 2;
            default: return 0;
        }
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
        LoginUser loginUser = UserContext.getLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        //TODO 2023-03-30 暂时取消审核流程 只改状态
        /*List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
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
        workflowFeign.taskNoPass(approveProcess);*/

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
        LoginUser loginUser = UserContext.getLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        entity.setCreateUserId(userId);
        entity.setCreateUserName(userName);
        return productDetailApproverService.saveOrUpdate(entity);
    }

    @Override
    @Transactional
    public Boolean productDetailProcessPass(String processId) {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
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
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setBusinessId(entity.getId()).setPid(entity.getProductId())
                .setOperation("状态变更").setContent("审核SKU[" + entity.getSkuNo() + "],操作[" + statusName + "]为[" + ProductDetailStatusEnum.APPROVAL_PASS.getName() + "]"));

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
        LoginUser loginUser = UserContext.getDefaultLoginUser();
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
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
        //TODO 2023-03-30 暂时取消审核流程 只改状态
//        this.productDetailStartProcess(entity);
        entity.setStatus(ProductDetailStatusEnum.WAIT_COMMIT.getCode());
        //反审核后更新是否申请变更
        entity.setIsChange(IsConstant.NO);
        //新增操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                .setBusinessId(entity.getId()).setOperation("状态变更").setContent("反审核SKU[" + entity.getSkuNo() + "],操作[" + statusName + "]为[" + ProductDetailStatusEnum.APPROVAL_ING.getName() + "]"));

        //发送金蝶
        sendPushTask(Arrays.asList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        return this.updateById(entity);
    }

    @Override
    public Boolean restartProcessPass(ProductDetailOperateDTO dto) {
        ProductDetailEntity entity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        if (!ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_95088);
        }
        //TODO 2023-03-30 暂时取消审核流程 只改状态
//        this.productDetailStartProcess(entity);
        entity.setStatus(ProductDetailStatusEnum.APPROVAL_ING.getCode());
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
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        Integer skuStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        List<SkuVO> skuList = baseMapper.getSkuBySkuNos(skuNoList, skuStatus);
        List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        for (SkuVO skuVO : skuList) {
            if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(new DmpSkuCostEntity());
                skuVO.setActualTaxCost(dmpSkuCost.getCostPrice());
                skuVO.setNotTaxCostPrice(dmpSkuCost.getNotTaxCostPrice());
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
            /*if (StringUtils.isBlank(productInfoEntity.getSpuNo())) {
                throw new ServiceException(ApiError.ERROR_95199);
            }*/
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
    public Boolean commit(String id) {
/*        //校验必填
        ProductCostEntity costEntity = productCostService.getBySkuId(id);
        if (costEntity.getActualTaxCost() == null) {
            throw new ServiceException(ApiError.ERROR_95237);
        }
        if (costEntity.getActualNoTaxCost() == null) {
            throw new ServiceException(ApiError.ERROR_95238);
        }*/
        //校验必填项
        checkRequiredField(Arrays.asList(id), Boolean.TRUE);
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
        //启动流程
//        productDetailStartProcess(productDetailEntity);
        productDetailEntity.setStatus(ProductDetailStatusEnum.APPROVAL_ING.getCode());
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
/*        ApproveProcessDTO processDTO = new ApproveProcessDTO();
        processDTO.setProcessInstanceId(productDetailEntity.getProcessId());
        workflowFeign.terminate(processDTO);*/

        //清除流程id
        productDetailEntity.setProcessId("");
        //更新审核状态
        productDetailEntity.setStatus(ProductDetailStatusEnum.WAIT_COMMIT.getCode());
        return this.updateById(productDetailEntity);
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

            List<String> skuNoList = skuList.stream().filter(e -> StringUtils.isNotEmpty(e.getSkuNo())).map(SkuVO::getSkuNo).distinct().collect(Collectors.toList());
            List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);

            for (SkuVO skuVO : skuList) {
                if (productPackMap.containsKey(skuVO.getSkuId()) && CollUtil.isNotEmpty(productPackMap.get(skuVO.getSkuId()))) {
                    ProductPackEntity packEntity = productPackMap.get(skuVO.getSkuId()).get(0);
                    skuVO.setUnitQty(Objects.nonNull(packEntity.getBoxQty()) ? packEntity.getBoxQty().intValue() : null);
                }
                if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                    DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(new DmpSkuCostEntity());
                    skuVO.setActualTaxCost(dmpSkuCost.getCostPrice());
                    skuVO.setNotTaxCostPrice(dmpSkuCost.getNotTaxCostPrice());
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

    private String checkRequiredDataList(List<ProductDetailEntity> productDetailEntityList) {
        StringBuffer str = new StringBuffer();
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
        Integer waitConfirmCode = ProductDetailStatusEnum.APPROVAL_ING.getCode();
        BusinessProcessEntity processEntity = businessProcessService.getProcessByBusinessKey(businessKey);
        if (!Objects.isNull(processEntity)) {
            StartProcessDTO startProcess = new StartProcessDTO();
            LoginUser loginUser = UserContext.getDefaultLoginUser();
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
            //TODO 2023-03-30 暂时取消审核流程 只改状态
            /*//启动流程
            ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
            String processId = processResult.getProcessId();
            if (StringUtils.isNotBlank(processId)) {
                productDetailEntity.setProcessId(processId);
                productDetailEntity.setBusinessProcessId(processEntity.getId());
                productDetailEntity.setStatus(waitConfirmCode);
            }*/
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
    public Boolean submit(List<String> ids, Boolean isCheck) {
        //校验必填项
        checkRequiredField(ids, isCheck);
        List<ProductDetailEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }


        //待提交、审核不通过才可以提交
        long count = entityList.stream().filter(entity ->
                entity.getStatus().equals(ProductDetailStatusEnum.WAIT_COMMIT.getCode())
                        || entity.getStatus().equals(ProductDetailStatusEnum.APPROVAL_NO_PASS.getCode())
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_95117);
        }
        //验证必填信息
        String str = checkRequiredDataList(entityList);
        if (StringUtils.isNotBlank(str)) {
            throw new ServiceException(new ApiResult(1, str));
        }
        List<String> productIds = entityList.stream().map(ProductDetailEntity::getProductId).collect(Collectors.toList());
        List<ProjectTaskEntity> taskAllList = projectTaskService.listByProductIds(productIds);
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
        boolean flag = lambdaUpdate().set(ProductDetailEntity::getStatus, ProductDetailStatusEnum.APPROVAL_ING.getCode())
                .in(ProductDetailEntity::getId, ids)
                .update();
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ProductDetailEntity entity, String type, String comment, Boolean isNeedProcess) {
        if (!(entity.getStatus().equals(ProductDetailStatusEnum.WAIT_CONFIRM.getCode())
                || entity.getStatus().equals(ProductDetailStatusEnum.APPROVAL_ING.getCode()))) {
            return BatchResultDTO.fail(entity.getId(),entity.getSkuNo(),ApiError.ERROR_95038.msg);
        }
        List<ProductDetailEntity> entityList = Arrays.asList(entity);
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        Integer approveStatus;
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            approveStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
            //发送通知
            noticeMessageService.approveProductNotice(UserContext.getLoginUser().getUserName(), entity);
            //发送金蝶
            sendSinglePushTask(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            syncWangDianProductDetailService.syncDataToWangDian(entityList);
        } else {
            approveStatus = ProductDetailStatusEnum.APPROVAL_NO_PASS.getCode();
            //新增审核不通过意见
            ProductDetailCommentEntity commentEntity = new ProductDetailCommentEntity();
            commentEntity.setComment("[审核结果-审核不通过]" + comment);
            commentEntity.setProductDetailId(entity.getId());
            productDetailCommentService.save(commentEntity);
        }
        //操作日志
        sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(entity.getProductId())
                .setBusinessId(entity.getId()).setOperation("状态变更").setContent("审核SKU[" + entity.getSkuNo() + "],操作[" + ProductDetailStatusEnum.getName(entity.getStatus()) + "]为[" + ApproveTypeEnum.getName(type) + "]，审批意见：" + comment));
        //更新sku记录
        lambdaUpdate().set(ProductDetailEntity::getStatus, approveStatus)
                .set(ProductDetailEntity::getUpdateUserId, userInfo.getUid())
                .set(ProductDetailEntity::getUpdateUserName, userInfo.getUserName())
                .eq(ProductDetailEntity::getId, entity.getId())
                .update();
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "操作成功");
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
        boolean flag = lambdaUpdate().set(ProductDetailEntity::getStatus, ProductDetailStatusEnum.WAIT_COMMIT.getCode())
                .in(ProductDetailEntity::getIsChange, IsConstant.NO)
                .eq(ProductDetailEntity::getId, entity.getId())
                .update();
        //发送金蝶
        sendSinglePushTask(entity,SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "操作成功");
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<ProductDetailEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = entityList.stream().filter(entity ->
                entity.getStatus().equals(ProductDetailStatusEnum.APPROVAL_ING.getCode())
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 撤销现有流程

        entityList.forEach(obj -> {
            //新增操作日志
            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(obj.getProductId())
                    .setBusinessId(obj.getId()).setOperation("状态变更").setContent("取消流程SKU[" + obj.getSkuNo() + "],操作[" + ProductDetailStatusEnum.getName(obj.getStatus()) + "]为[" + ProductDetailStatusEnum.WAIT_CONFIRM.getName() + "]"));
        });
        boolean flag = lambdaUpdate().set(ProductDetailEntity::getStatus, ProductDetailStatusEnum.WAIT_COMMIT.getCode())
                .set(ProductDetailEntity::getProcessId, "")
                .in(ProductDetailEntity::getId, ids)
                .update();
        List<ProductDetailEntity> syncDateList = this.listByIds(ids);
        //同步到SCM
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_SKU_TAG.getName(), syncDateList, IdUtil.simpleUUID());
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_SKU_TAG.getName(), syncDateList, IdUtil.simpleUUID());
        return flag;
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
        LambdaQueryWrapper<ProductDetailEntity> queryWrapper = new LambdaQueryWrapper();
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
                    //同步到SCM
//                    mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_INFO_TAG.getName(), infoEntity, IdUtil.simpleUUID());
                    //同步到WMS
//                    mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_INFO_TAG.getName(), infoEntity, IdUtil.simpleUUID());

                    productInfoService.removeById(productInfoEntity.getId());
                }
                //添加操作日志
                SysLogEntity sysLogEntity = new SysLogEntity().setClassPath(SPUCLASSPATH).setBusinessId(productInfoEntity.getId()).setPid(productInfoEntity.getId()).setOperation("删除信息").setContent("删除了一个SKU：[" + productDetailEntity.getSkuNo() + "]");
                sysLogService.addSysLogByOther(sysLogEntity);
            }
        }
        List<ProductDetailEntity> detailEntityList = this.listByIds(ids);
        //同步到SCM
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_SKU_TAG.getName(), detailEntityList, IdUtil.simpleUUID());
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_SKU_TAG.getName(), detailEntityList, IdUtil.simpleUUID());

        return this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBatchFiled(ProductDetailBatchUpdateDTO dto) {
        dto.setUpdateFiledCode(StrUtil.toUnderlineCase(dto.getUpdateFiledCode()));
        List<ProductDetailEntity> entityList = this.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

/*        List<ProductDetailEntity> detailApprovalList = entityList.stream().filter(req -> ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(req.getStatus())).collect(Collectors.toList());
        if (ProductBatchFieldEnum.WAREHOUSE_LOCATION.getCode().equals(dto.getUpdateFiledCode()) && entityList.size() != detailApprovalList.size()) {
            throw new ServiceException(ApiError.ERROR_APPROVE_UPDATE_LOCATION);
        }

        //审核通过后支持批量更新【销售状态】【是否可销售】【产品开发状态】【推荐仓位】
        long count = entityList.stream().filter(entity ->
                entity.getStatus().equals(ProductDetailStatusEnum.APPROVAL_PASS.getCode())
                        && (ProductBatchFieldEnum.SALE_STATE.getCode().equals(dto.getUpdateFiledCode())
                        || ProductBatchFieldEnum.IS_MARKETABLE.getCode().equals(dto.getUpdateFiledCode())
                        || ProductBatchFieldEnum.PRODUCT_STATE.getCode().equals(dto.getUpdateFiledCode())
                        || ProductBatchFieldEnum.WAREHOUSE_LOCATION.getCode().equals(dto.getUpdateFiledCode()))
        ).count();

        if (count != detailApprovalList.size()) {
            throw new ServiceException(ApiError.ERROR_95176);
        }*/
        Boolean flag = Boolean.TRUE;
        //如果是产品经理需要查询name
        if (ProductBatchFieldEnum.CHARGE_ID.getCode().equals(dto.getUpdateFiledCode()) || ProductBatchFieldEnum.SALE_METHOD.getCode().equals(dto.getUpdateFiledCode())) {
            if (ObjectUtils.isEmpty(dto.getValues())) {
                throw new ServiceException(ApiError.ERROR_9030);
            }
            List<ProductDetailEntity> detailEntityList = this.listByIds(dto.getIds());
            List<String> productIds = detailEntityList.stream().map(ProductDetailEntity::getProductId).distinct().collect(Collectors.toList());
            if (ProductBatchFieldEnum.CHARGE_ID.getCode().equals(dto.getUpdateFiledCode())) {
                String chargeName = commonService.getNameByIds(Arrays.asList(dto.getValues().toString().split(",")));
                productInfoService.lambdaUpdate()
                        .set(ProductInfoEntity::getChargeId, dto.getValues())
                        .set(ProductInfoEntity::getChargeName, chargeName).in(ProductInfoEntity::getId, productIds)
                        .update();
                this.lambdaUpdate()
                        .set(ProductDetailEntity::getChargeId, dto.getValues())
                        .set(ProductDetailEntity::getChargeName, chargeName).in(ProductDetailEntity::getId, dto.getIds())
                        .update();
            }
            if (ProductBatchFieldEnum.SALE_METHOD.getCode().equals(dto.getUpdateFiledCode())) {
                productInfoService.lambdaUpdate()
                        .set(ProductInfoEntity::getSaleMethod, dto.getValues())
                        .in(ProductInfoEntity::getId, productIds)
                        .update();
            }

            List<ProductInfoEntity> productInfoEntities = productInfoService.listByIds(productIds);
            //同步到SCM
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_INFO_TAG.getName(), productInfoEntities, IdUtil.simpleUUID());
            //同步到WMS
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_INFO_TAG.getName(), productInfoEntities, IdUtil.simpleUUID());

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
            ProductBatchFieldEnum enumByCode = ProductBatchFieldEnum.getEnumByCode(dto.getUpdateFiledCode());
            if (enumByCode == null) {
                throw new ServiceException(ApiError.ERROR_9046, dto.getUpdateFiledCode());
            }
            flag = baseMapper.updateFiledBatch(dto.getIds(), enumByCode.getTableName(), enumByCode.getCode(), dto.getValues(), enumByCode.getKeyName());
        }
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_95243);
        }
        productLogisticsService.saveOrUpdateParentPropertyIdByChildSkuId(dto.getIds());
        //同步到SCM
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_SKU_TAG.getName(), list, IdUtil.simpleUUID());
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_SKU_TAG.getName(), list, IdUtil.simpleUUID());

        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOccupyStatus(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Boolean.TRUE;
        }
        List<String> updateSkuIds = new ArrayList<>();
        for (String skuId : skuIds) {
            //查询redis缓存
            String redisKey = StrUtil.format(RedisKeyConstant.SKU_OCCUPY_CODE, skuId, Boolean.TRUE);
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
            String redisKey = StrUtil.format(RedisKeyConstant.SKU_OCCUPY_CODE, skuId, Boolean.TRUE);
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
            ProductSearchDTO.SkuListDTO skuListDTO = list.stream().filter(obj -> obj.getSkuNo().equals(skuNo)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuListDTO)) {
                skuListDTO = new ProductSearchDTO.SkuListDTO();
            }
            //sku状态名称
            skuListDTO.setStatusName(ProductDetailStatusEnum.getName(skuListDTO.getStatus()));
            //供应商名称
            ProductSearchDTO.SkuListDTO finalSkuListDTO = skuListDTO;
            String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(finalSkuListDTO.getMainSupplier())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            skuListDTO.setMainSupplierName(supplierName);
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

        List<ProductDetailEntity> list = lambdaQuery().in(ProductDetailEntity::getId, id).list();
        //同步到SCM
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_SKU_TAG.getName(), list, IdUtil.simpleUUID());
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_SKU_TAG.getName(), list, IdUtil.simpleUUID());

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

        //包装信息
        List<String> skuIdList = entityList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        List<ProductPackEntity> productPackList = productPackService.listBySkuIdList(skuIdList);

        for (ProductDetailEntity detailEntity : entityList) {
            StringBuffer errMsg = new StringBuffer("");

            //SPU信息
            ProductInfoEntity productInfo = productInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getProductId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(productInfo)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            if (ObjectUtil.isEmpty(productInfo.getSaleMethod() )|| (!productInfo.getSaleMethod().contains(SaleMethodEnum.GOODS.getName()) && !productInfo.getSaleMethod().contains(SaleMethodEnum.GIFT.getName()))) {
                continue;
            }
            /**
             * 当产品销售方式为商品时，包装尺寸、箱规 、毛重、单箱重量、净重、单箱数量不能为空
             */
            ProductPackEntity productPackEntity = productPackList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), detailEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(productPackEntity)) {
                errMsg.append(StrUtil.format(ApiError.ERROR_PRODUCT_PACK_NOT_EXIST.msg,detailEntity.getSkuNo())).append("</br>");
                continue;
            }
            //包装尺寸
            if (MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getProductLength()) >= 0  || MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getProductWidth()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getProductHeight()) >= 0) {
                errMsg.append(StrUtil.format(ApiError.ERROR_PRODUCT_SIZE_NOT_EXIST.msg, detailEntity.getSkuNo())).append("</br>");
            }
            //箱规
            if (MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getBoxLength()) >= 0 ||MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getBoxWidth()) >= 0 || MathUtil.compareTo(BigDecimal.ZERO, productPackEntity.getBoxHeight()) >= 0) {
                errMsg.append(StrUtil.format(ApiError.ERROR_BOX_SIZE_NOT_EXIST.msg, detailEntity.getSkuNo())).append("</br>");
            }
            //毛重
            if (MathUtil.compareTo(productPackEntity.getGrossWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(StrUtil.format(ApiError.ERROR_GROSS_WEIGHT_NOT_EXIST.msg, detailEntity.getSkuNo())).append("</br>");
            }
            //单箱重量
            if (MathUtil.compareTo(productPackEntity.getBoxWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(StrUtil.format(ApiError.ERROR_BOX_WEIGHT_NOT_EXIST.msg, detailEntity.getSkuNo())).append("</br>");
            }
            //净重
            if (MathUtil.compareTo(productPackEntity.getNetWeight(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(StrUtil.format(ApiError.ERROR_NET_WEIGHT_NOT_EXIST.msg, detailEntity.getSkuNo())).append("</br>");
            }
            //单箱数量
            if (MathUtil.compareTo(productPackEntity.getBoxQty(), MathUtil.ZERO) == MathUtil.ZERO) {
                errMsg.append(StrUtil.format(ApiError.ERROR_BOX_QTY_NOT_EXIST.msg, detailEntity.getSkuNo()));
            }
            if (StrUtil.isNotBlank(errMsg)) {
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
    public Boolean importProductFile(MultipartFile excelFile, Integer importType, HttpServletResponse response) {
        ProductDetailExcelListener excelListenerUtil = new ProductDetailExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), ProductDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
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
        handleImportSuccessList(successList, errorList, importType);

        if (errorList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/productNoSpecDetail.xlsx";
            String name = "productNoSpecDetail";
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

    private void handleImportSuccessList(List<ProductDetailExcelDTO> successList, List<ProductDetailExcelDTO> errorList, Integer importType) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<BasicDictEntity> basicDictList = basicDictService.list();
        List<ProductDetailEntity> productDetailEntityList = this.list();
        List<ProductUnitEntity> unitEntityList = productUnitService.list();
        List<BasicCategoryEntity> categoryEntityList = basicCategoryService.list();


        //根据供应商名称查询供应商信息
        List<String> mainSupplierNameList = successList.stream().map(req -> req.getMainSupplier()).distinct().collect(Collectors.toList());
        List<String> secondSupplierNameList = successList.stream().map(req -> req.getSecondSupplier()).distinct().collect(Collectors.toList());
        List<String> supplierNameList = new ArrayList<>();
        supplierNameList.addAll(mainSupplierNameList);
        supplierNameList.addAll(secondSupplierNameList);
        List<SupplierEntity> supplierList = scmTaskFeign.listBySupplierByNames(supplierNameList);

        for (ProductDetailExcelDTO dto : successList) {
            List<String> errorMsgList = new ArrayList<>();

            ProductDetailEntity productBy = productDetailEntityList.stream().filter(req -> req.getSkuNo().equals(dto.getSkuNo())).findFirst().orElse(null);

            ProductInfoDTO productInfoDTO = new ProductInfoDTO();


            //sku信息
            ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();
            // 判断是修改还是新增 1：新增 2：修改
            if (importType == 2) {
                if (ObjectUtils.isEmpty(productBy)) {
                    errorMsgList.add("sku不存在，请选择导入新增");
                }
                //存在错误信息则返回
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(dto);
                    continue;
                }
                if (ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(productBy.getStatus())
                        || ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(productBy.getStatus())
                        || ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productBy.getStatus())) {
                    errorMsgList.add("仅{待提交，审核不通过}的状态下可导入修改");
                }
                productSkuBaseInfoDTO.setId(productBy.getId());
                productInfoDTO.setId(productBy.getProductId());
                //设置推荐仓位（大货区/小货区）
                if (StringUtils.isEmpty(dto.getWarehouseLocationLarge())) {
                    dto.setWarehouseLocationLarge(productBy.getWarehouseLocationLarge());
                }
                if (StringUtils.isEmpty(dto.getWarehouseLocation())) {
                    dto.setWarehouseLocation(productBy.getWarehouseLocation());
                }
            } else {
                //sku重复
                if (ObjectUtil.isNotEmpty(productBy)) {
                    errorMsgList.add(ApiError.ERROR_95015.msg);
                }
                //spu名称，新增单规格名称给随机雪花编码
                productInfoDTO.setName(IdUtil.getSnowflake().nextIdStr());
                productInfoDTO.setSpecType(1);
                productInfoDTO.setGrade("");
            }

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
                if (StrUtil.isBlank(dto.getIterateRefSkuNo())) {
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
                    errorMsgList.add("二级类目不存在");
                } else {
                    BasicCategoryEntity secondEntity = categoryList.stream().filter(obj -> secondaryCategoryEntity.getPid().equals(obj.getId())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(secondEntity) || StringUtils.isBlank(secondaryCategoryEntity.getCode())) {
                        errorMsgList.add(ApiError.ERROR_95092.msg);
                    }
                    if (!bestEntity.getId().equals(secondaryCategoryEntity.getPid())) {
                        errorMsgList.add("产品分类一级类目和二级类目的关系不匹配");
                    }
                }
                //存在错误信息则返回
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(dto);
                    continue;
                }
                productInfoDTO.setCategory(secondaryCategory);
                productInfoDTO.setCategoryId(secondaryCategoryEntity.getId());
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
            //sku信息
            BeanMapper.copy(dto, productSkuBaseInfoDTO);
            if (StringUtils.isNotBlank(dto.getPlanListingTime())) {
                productSkuBaseInfoDTO.setPlanListingTime(LocalDate.parse(dto.getPlanListingTime(), dateTimeFormatter));
            }
            productSkuBaseInfoDTO.setProductId("");
            if (ObjectUtil.isNotEmpty(productUnitEntity)) {
                productSkuBaseInfoDTO.setUnitId(productUnitEntity.getId());
                productSkuBaseInfoDTO.setUnitName(productUnitEntity.getName());
            }
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
            if (StringUtils.isNotBlank(dto.getDelistingTime())) {
                productSaleDTO.setDelistingTime(LocalDate.parse(dto.getDelistingTime(), dateTimeFormatter));
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

            this.inportExcel(productNoSpecDTO);
        }
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
        if (CollUtil.isEmpty(skuList)) {
            return Collections.emptyList();
        }
        //供应商信息
//        List<String> supplierIdList = skuList.stream().map(SkuInfoSimpleVO::getSupplierId).collect(Collectors.toList());
//        List<PurchasePriceDTO.SupplierSkuPrice> supplierSkuPriceList = scmTaskFeign.listSupplierSkuPrice(supplierIdList);
//
//        Map<String, List<PurchasePriceDTO.SupplierSkuPrice>> suppelierMap = supplierSkuPriceList
//                .stream()
//                .collect(Collectors.groupingBy(PurchasePriceDTO.SupplierSkuPrice::getSupplierId));
//
//        for (SkuInfoSimpleVO skuVO : skuList) {
//            List<PurchasePriceDTO.SupplierSkuPrice> supplierList = suppelierMap.get(skuVO.getSupplierId());
//            if (CollectionUtils.isNotEmpty(supplierList)){
//                PurchasePriceDTO.SupplierSkuPrice supplierSkuPrice = supplierList.get(0);
//                //含税价
//                skuVO.setActualTaxCost(supplierSkuPrice.getTaxPrice());
//            }
//        }

        List<String> skuNoList = skuList.stream().map(SkuInfoSimpleVO::getSkuNo).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());
        List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        for (SkuInfoSimpleVO skuVO : skuList) {
            if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(new DmpSkuCostEntity());
                skuVO.setActualTaxCost(dmpSkuCost.getCostPrice());
            }
        }
        return skuList;
    }

    @Override
    public void initProductSizeAndBoxSize() {
        // 查询出所有需要进行初始化的产品尺寸或箱规
        List<ProductPackEntity> productPacks = productPackService.list();
        List<List<ProductPackEntity>> partition = Lists.partition(productPacks, 500);
        partition.parallelStream()
                .forEach(packs ->{
                    try {
                        packs.forEach(this::convertSize);
                    } catch (Exception e) {
                        log.error("数据异常{}", e.getMessage(), e);
                    }
                    productPackService.updateBatchById(packs);
                });
    }

    private void convertSize(ProductPackEntity pack) {
        List<BigDecimal> productSizeList = Arrays.stream(Optional.ofNullable(pack.getProductSize()).orElse("").split("X"))
                .filter(StrUtil::isNotBlank)
                .map(BigDecimal::new)
                .collect(Collectors.toList());
        //产品尺寸-长
        pack.setProductLength(LengthConverterUtil.cmToMm(productSizeList.stream().findFirst().orElse(BigDecimal.ZERO)));
        //产品尺寸-宽
        pack.setProductWidth(LengthConverterUtil.cmToMm(productSizeList.stream().skip(1).findFirst().orElse(BigDecimal.ZERO)));
        //产品尺寸-高
        pack.setProductHeight(LengthConverterUtil.cmToMm(productSizeList.stream().skip(2).findFirst().orElse(BigDecimal.ZERO)));
        List<BigDecimal> boxSizeList = Arrays.stream(Optional.ofNullable(pack.getBoxSize()).orElse("").split("X"))
                .filter(StrUtil::isNotBlank)
                .map(BigDecimal::new)
                .collect(Collectors.toList());
        //箱规-长
        pack.setBoxLength(LengthConverterUtil.cmToMm(boxSizeList.stream().findFirst().orElse(BigDecimal.ZERO)));
        //箱规-宽
        pack.setBoxWidth(LengthConverterUtil.cmToMm(boxSizeList.stream().skip(1).findFirst().orElse(BigDecimal.ZERO)));
        //箱规-高
        pack.setBoxHeight(LengthConverterUtil.cmToMm(boxSizeList.stream().skip(2).findFirst().orElse(BigDecimal.ZERO)));
    }

    @Override
    public List<SkuVO> accessoriesSku(String searchKeyword) {
        return baseMapper.accessoriesSku(searchKeyword, ProductDetailStatusEnum.APPROVAL_PASS.getCode());

    }

    @Override
    public List<ProductDetailExcelDTO> getProductDetailExportData(String metaInfo) {
        return null;
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
    private void sendPushTask (List<ProductDetailEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeProductDetailService.syncDataToKingdee(obj, operate);
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
        List<String> skuNoList = skuList.stream().filter(e -> StringUtils.isNotEmpty(e.getSkuNo())).map(SkuVO::getSkuNo).distinct().collect(Collectors.toList());
        List<DmpSkuCostEntity> dmpSkuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList);
        for (SkuVO skuVO : skuList) {
            if (CollectionUtils.isNotEmpty(dmpSkuCostList)) {
                DmpSkuCostEntity dmpSkuCost = dmpSkuCostList.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(new DmpSkuCostEntity());
                skuVO.setActualTaxCost(dmpSkuCost.getCostPrice());
                skuVO.setNotTaxCostPrice(dmpSkuCost.getNotTaxCostPrice());
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
        String logContent = StrUtil.format("更新【包装尺寸长】从{}更新为{}，【包装尺寸宽】从{}更新为{}，【包装尺寸高】从{}更新为{}，【毛重】从{}更新为{}",productPackEntity.getProductLength(),length,productPackEntity.getProductWidth(),width,productPackEntity.getProductHeight(),height,productPackEntity.getGrossWeight(),weight);
        productPackEntity.setProductLength(length);
        productPackEntity.setProductWidth(width);
        productPackEntity.setProductHeight(height);
        productPackEntity.setGrossWeight(weight);
        boolean result = productPackService.updateById(productPackEntity);
        if(result){
            sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(purchaseEntity.getProductId())
                    .setBusinessId(purchaseEntity.getId()).setOperation("品质称重").setContent(logContent));
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
        List<String> base64List = new ArrayList<>();
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(printEanDTO.getPrintType());
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.PRODUCT_DETAIL.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        try (InputStream inputStream = FastDFSClientUtil.getInputStream(fileTemplateEntity.getUrl());
             OutputStream out = response.getOutputStream()) {
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(inputStream);
            JasperHelperUtil.prepareReport(jasperReport, FileTypeEnum.PDF.getCode());
            for (PrintEanDTO.PrintSkuEanDTO dto : printEanDTO.getPrintSkuEanList()) {
                if (ObjectUtils.isNotEmpty(dto.getEan()) && printEanDTO.getTypeList().contains("EAN")) {
                    Map<String, Object> eanMap = new HashMap<>();
                    eanMap.put("code", dto.getEan());
                    JasperPrint eanJasperPrint = JasperFillManager.fillReport(jasperReport, eanMap, new ReportDataSourceDTO<>(Collections.singletonList(dto.getEan())));
                    byte[] eanBytes = JasperExportManager.exportReportToPdf(eanJasperPrint);
                    String eanBase = Base64.getEncoder().encodeToString(eanBytes);
                    for (int i = 0; i < dto.getQty(); i++) {
                        base64List.add("data:application/pdf;base64," + eanBase);
                    }
                }
                String skuBase = null;
                if (printEanDTO.getTypeList().contains("SKU")) {
                    Map<String, Object> skuMap = new HashMap<>();
                    skuMap.put("code", dto.getSkuNo());
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, skuMap, new ReportDataSourceDTO<>(Collections.singletonList(dto.getSkuNo())));
                    byte[] skuBytes = JasperExportManager.exportReportToPdf(jasperPrint);
                    skuBase = Base64.getEncoder().encodeToString(skuBytes);
                    for (int i = 0; i < dto.getQty(); i++) {
                        base64List.add("data:application/pdf;base64," + skuBase);
                    }
                }
            }
            String newMergePdfBase64 = PdfUtil.getNewMergePdfBase64(base64List);
            // 设置响应头，告诉浏览器返回的是一个 PDF 文件
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"filename.pdf\""); // 设置 PDF 的显示方式和文件名
            BASE64Decoder decoder = new BASE64Decoder();
            // 将 Base64 编码的字符串解码为字节数组
            byte[] pdfBytes = decoder.decodeBuffer(newMergePdfBase64);
            // 将字节数组写入到响应输出流中
            out.write(pdfBytes);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }
}
