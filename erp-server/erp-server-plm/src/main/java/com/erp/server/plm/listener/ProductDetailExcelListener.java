package com.erp.server.plm.listener;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.core.anno.FieldValid;
import com.common.core.enums.ApiError;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.model.plm.enums.*;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductUnitService;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductDetailExcelListener extends AnalysisEventListener<ProductDetailExcelDTO> {
    private Integer importType;

    private ProductDetailService productDetailService;

    private ProductUnitService productUnitService;

    private BasicCategoryService basicCategoryService;

    private BasicDictService basicDictService;


    /**
     * plm 字典信息
     */
    private List<BasicDictEntity> basicDictList;

    /**
     * 用户信息
     */
    private List<FindUserDTO> userList;

    private List<ProductDetailExcelDTO> list;

    private List<ProductDetailExcelDTO> dataList = new ArrayList<>();

    private ScmTaskFeign scmTaskFeign;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public ProductDetailExcelListener(Integer importType, ProductDetailService productDetailService, ProductUnitService productUnitService,
                                      BasicCategoryService basicCategoryService, BasicDictService basicDictService, List<FindUserDTO> userList,
                                      List<BasicDictEntity> basicDictList,ScmTaskFeign scmTaskFeign) {
        this.importType = importType;
        this.productDetailService = productDetailService;
        this.productUnitService = productUnitService;
        this.basicCategoryService = basicCategoryService;
        this.basicDictService = basicDictService;
        this.userList = userList;
        this.basicDictList = basicDictList;
        this.list = new ArrayList<>();
        this.scmTaskFeign=scmTaskFeign;
    }

    /**
     * @Description 每解析一行数据回调一遍
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     * @param1 productDetailExcelDTO: 导入信息
     * @param2 analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductDetailExcelDTO dto, AnalysisContext analysisContext) {
        String errStr = "";

        List<String> errorMsgList = new ArrayList<>();
        //添加数据用于判断是否为空
        dataList.add(dto);
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        ProductDetailShowDTO productBy = productDetailService.getProductBy("", dto.getSkuNo());
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        //sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();

        // 判断是修改还是新增 1：新增 2：修改
        if (importType == 2) {
            if (ObjectUtils.isEmpty(productBy)) {
                errorMsgList.add("sku不存在，请选择导入新增");
            }
            if (errorMsgList.size() > 0) {
                for (int i = 0; i < errorMsgList.size(); i++) {
                    Integer indexTemp = i + 1;
                    errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
                }
                dto.setErrorMsg(errStr);
                list.add(dto);
                return;
            }
            if (ProductDetailStatusEnum.WAIT_CONFIRM.getCode().equals(productBy.getStatus())
                    || ProductDetailStatusEnum.APPROVAL_ING.getCode().equals(productBy.getStatus())
                    || ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productBy.getStatus())) {
                errorMsgList.add("仅{待提交，审核不通过}的状态下可导入修改");
            }
            productSkuBaseInfoDTO.setId(productBy.getSkuId());
            productInfoDTO.setId(productBy.getId());
//            if (!ObjectUtils.isEmpty(productDetailShow)) {
//                if (!productDetailShow.getSkuNo().equals(dto.getSkuNo())) {
//                    errorMsgList.add(ApiError.ERROR_95015.msg);
//                }
//            }

        } else {
            //sku重复
            if (productDetailService.checkSkuNo(dto.getSkuNo(), "")) {
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
                BasicDictEntity productCountry = basicDictService.checkBasicDict(BasicDictTypeEnum.COUNTRY.getCode(), saleCountry);
                if (ObjectUtils.isEmpty(productCountry)) {
                    errorMsgList.add("销售国家在系统中未找到");
                    break;
                }
                saleCountryStr = saleCountryStr + productCountry.getId() + ",";
            }
        }

        List<FindUserDTO> chargeNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getChargeName())) {
            chargeNameList = userList.stream().filter(e -> e.getUserName().equals(dto.getChargeName())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(chargeNameList)) {
                errorMsgList.add("产品经理在系统中未找到");
            }
        }

        List<FindUserDTO> purchaseUserList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getPurchaseUser())) {
            purchaseUserList = userList.stream().filter(e -> e.getUserName().equals(dto.getPurchaseUser())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(purchaseUserList)) {
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
                List<ProductDetailEntity> iterativeSkuList = productDetailService.listBySkuNos(Arrays.asList(dto.getIterateRefSkuNo()));
                if (CollectionUtils.isEmpty(iterativeSkuList)) {
                    errorMsgList.add("迭代产品在系统中未找到");
                } else {
                    ProductDetailEntity productDetailEntity = iterativeSkuList.get(0);
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
            productUnitEntity = productUnitService.checkUnitName(dto.getUnitName());
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
        BasicCategoryEntity basicCategoryEntity = basicCategoryService.getCategoryByName(category, Boolean.TRUE);
        if (ObjectUtils.isEmpty(basicCategoryEntity)) {
            errorMsgList.add("产品分类一级类目不存在");
        } else {
            //父级品类
            List<BasicCategoryEntity> categoryList = basicCategoryService.listParentEntity(basicCategoryEntity.getId());
            if (CollectionUtils.isEmpty(categoryList)) {
                errorMsgList.add(ApiError.ERROR_95091.msg);
            }
            //一级品类
            BasicCategoryEntity bestEntity = categoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bestEntity) || StringUtils.isBlank(bestEntity.getCode())) {
                errorMsgList.add(ApiError.ERROR_95091.msg);
            }
            //二级品类
            String secondaryCategory = dto.getSecondaryCategory();
            BasicCategoryEntity secondaryCategoryEntity = basicCategoryService.getCategoryByName(secondaryCategory, Boolean.FALSE);
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

        List<String> supplierNameList = new ArrayList<>(2);
        if (StringUtils.isNotBlank(mainSupplier)) {
            supplierNameList.add(mainSupplier);
        }

        if (StringUtils.isNotBlank(secondSupplier)) {
            supplierNameList.add(secondSupplier);
        }
        List<SupplierEntity>  supplierList= scmTaskFeign.listBySupplierByNames(supplierNameList);
        if(StringUtils.isNotBlank(mainSupplier)){
            SupplierEntity  mainSupplierDb= supplierList.stream().filter(s->s.getName().equals(mainSupplier)).
                    findFirst().orElse(null);
            if(Objects.isNull(mainSupplierDb)){
                errorMsgList.add("一级供应商不存在");
            }else{
                dto.setMainSupplier(mainSupplierDb.getId());
            }
        }

        if(StringUtils.isNotBlank(secondSupplier)){
            SupplierEntity  secondSupplierDb= supplierList.stream().filter(s->s.getName().equals(secondSupplier)).
                    findFirst().orElse(null);
            if(Objects.isNull(secondSupplierDb)){
                errorMsgList.add("二级供应商不存在");
            }else{
                dto.setSecondSupplier(secondSupplierDb.getId());
            }
        }

        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            dto.setErrorMsg(errStr);
            list.add(dto);
            return;
        }

        ProductNoSpecDTO productNoSpecDTO = new ProductNoSpecDTO();


/*        BasicCategoryEntity categoryByName = basicCategoryService.getCategoryByName(dto.getCategory());
        if (!ObjectUtils.isEmpty(categoryByName)) {
            productInfoDTO.setChargeId("产品类别Id");
            productInfoDTO.setCategory("产品类别");
        }*/
        productInfoDTO.setBrandId(productBrand.getId());
        productInfoDTO.setBrandName(productBrand.getValue());
        productInfoDTO.setApprovalStatus(0);

        productInfoDTO.setIsNoSpecAdd(MathUtil.ONE);
        //sku 经理
        if (!CollectionUtils.isEmpty(chargeNameList)) {
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

        //sku信息
        BeanMapper.copy(dto, productSkuBaseInfoDTO);
        if (StringUtils.isNotBlank(dto.getPlanListingTime())) {
            productSkuBaseInfoDTO.setPlanListingTime(LocalDate.parse(dto.getPlanListingTime(), dateTimeFormatter));
        }
        productSkuBaseInfoDTO.setProductId("");
        productSkuBaseInfoDTO.setUnitId(productUnitEntity.getId());
        productSkuBaseInfoDTO.setUnitName(productUnitEntity.getName());
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
        String productSize = "";
        String boxSize = "";
        String productSizeLength = dto.getProductSizeLength();
        String productSizeWide = dto.getProductSizeWide();
        String productSizeHigh = dto.getProductSizeHigh();
        String boxSizeLength = dto.getBoxSizeLength();
        String boxSizeWide = dto.getBoxSizeWide();
        String boxSizeHigh = dto.getBoxSizeHigh();
        /**
         * 产品尺寸(长)
         */
        if (StringUtils.isNotBlank(productSizeLength)) {
            productSize = productSizeLength;
        }
        /**
         * 产品尺寸(宽)
         */
        if (StringUtils.isNotBlank(productSizeWide)) {
            productSize = productSize.concat("X").concat(productSizeWide);
        }
        /**
         * 产品尺寸(高)
         */
        if (StringUtils.isNotBlank(productSizeHigh)) {
            productSize = productSize.concat("X").concat(productSizeHigh);
        }
        productPackDTO.setProductSize(productSize);
        /**
         * 箱规(长)
         */
        if (StringUtils.isNotBlank(boxSizeLength)) {
            boxSize = boxSizeLength;
        }
        /**
         * 箱规(宽)
         */
        if (StringUtils.isNotBlank(boxSizeWide)) {
            boxSize = boxSize.concat("X").concat(boxSizeWide);
        }
        /**
         * 箱规(高)
         */
        if (StringUtils.isNotBlank(boxSizeHigh)) {
            boxSize = boxSize.concat("X").concat(boxSizeHigh);
        }
        productPackDTO.setBoxSize(boxSize);
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

        /*//产品证书信息
        ProductCertificateDTO productCertificateDTO = new ProductCertificateDTO();
        BeanMapper.copy(dto, productCertificateDTO);
        productNoSpecDTO.setProductCertificateDTO(productCertificateDTO);*/

        productDetailService.inportExcel(productNoSpecDTO);
    }

    public List<ProductDetailExcelDTO> getDateList() {
        return list;
    }

    public List<ProductDetailExcelDTO> getExcelDateList() {
        return dataList;
    }

    /**
     * @param analysisContext: 解析器上下文
     * @Description 全部解析完回调此方法
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     **/
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
