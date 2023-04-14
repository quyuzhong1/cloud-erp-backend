package com.erp.server.plm.listener;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.core.enums.ApiError;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.enums.ProductDetailStateEnum;
import com.erp.model.plm.enums.PurchaseStateEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductUnitService;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDetailExcelListener extends AnalysisEventListener<ProductDetailExcelDTO> {
    private Integer importType;

    private ProductDetailService productDetailService;

    private ProductUnitService productUnitService;

    private BasicCategoryService basicCategoryService;

    private BasicDictService basicDictService;

    private SysUserFeign sysUserFeign;

    private List<ProductDetailExcelDTO> list;

    private List<ProductDetailExcelDTO> dataList = new ArrayList<>();

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public ProductDetailExcelListener(Integer importType, ProductDetailService productDetailService, ProductUnitService productUnitService,
                                      BasicCategoryService basicCategoryService, BasicDictService basicDictService, SysUserFeign sysUserFeign) {
        this.importType = importType;
        this.productDetailService = productDetailService;
        this.productUnitService = productUnitService;
        this.basicCategoryService = basicCategoryService;
        this.basicDictService = basicDictService;
        this.sysUserFeign = sysUserFeign;
        this.list = new ArrayList<>();
    }

    /**
     * @Description  每解析一行数据回调一遍
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     * @param1 productDetailExcelDTO: 导入信息
     * @param2 analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductDetailExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //添加数据用于判断是否为空
        dataList.add(dto);
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        ProductDetailShowDTO productBy = productDetailService.getProductBy("", dto.getSkuNo());
        //根据产品名称查询产品信息
        ProductDetailShowDTO productDetailShow = productDetailService.getProductBy(dto.getName(), "");
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        //sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();

        // 判断是修改还是新增 1：新增 2：修改
        if (importType == 2) {
            productSkuBaseInfoDTO.setId(productBy.getSkuId());
            if (ObjectUtils.isEmpty(productBy)) {
                errorMsgList.add("sku不存在，请选择导入新增");
            }
            productInfoDTO.setId(productBy.getId());
            if (!ObjectUtils.isEmpty(productDetailShow)) {
                if (!productDetailShow.getSkuNo().equals(dto.getSkuNo())) {
                    errorMsgList.add(ApiError.ERROR_95015.msg);
                }
            }
        } else {
            //sku重复
            if (productDetailService.checkSkuNo(dto.getSkuNo(), "")) {
                errorMsgList.add(ApiError.ERROR_95015.msg);
            }
        }

        String saleCountryStr = "";
        if(StringUtils.isNotBlank(dto.getSaleCountry())){
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
        //存在侵权风险
        String pirateRisk = dto.getPirateRisk();
        if(StringUtils.isNotBlank(pirateRisk)){
            if(pirateRisk.equals("有")){
                productInfoDTO.setPirateRisk(1);
            } else {
                productInfoDTO.setPirateRisk(2);
            }
        }
        List<FindUserDTO> resultList = sysUserFeign.getUserList();
        List<FindUserDTO> chargeNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getChargeName())) {
            chargeNameList = resultList.stream().filter(e -> e.getUserName().equals(dto.getChargeName())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(chargeNameList)) {
                errorMsgList.add("产品经理在系统中未找到");
            }
        }

        List<FindUserDTO> purchaseUserList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getPurchaseUser())) {
            purchaseUserList = resultList.stream().filter(e -> e.getUserName().equals(dto.getPurchaseUser())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(purchaseUserList)) {
                errorMsgList.add("采购员在系统中未找到");
            }
        }

        BasicDictEntity productBrand = basicDictService.checkBasicDict(BasicDictTypeEnum.PRODUCT_BRAND.getCode(), dto.getBrandName());
        if (ObjectUtils.isEmpty(productBrand)) {
            errorMsgList.add("产品品牌在系统中未找到");
        }

        BasicDictEntity productProperty = basicDictService.checkBasicDict(BasicDictTypeEnum.PRODUCT_PROPERTY.getCode(), dto.getProperty());
        if (ObjectUtils.isEmpty(productProperty)) {
            errorMsgList.add("产品属性在系统中未找到");
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

        BasicDictEntity declareProperty = new BasicDictEntity();
        if (StringUtils.isNotBlank(dto.getProductProperty())) {
            declareProperty = basicDictService.checkBasicDict(BasicDictTypeEnum.DECLARE_PROPERTY.getCode(), dto.getProductProperty());
            if (ObjectUtils.isEmpty(declareProperty)) {
                errorMsgList.add("报关产品属性在系统中未找到");
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
        String category = dto.getCategory();
        BasicCategoryEntity basicCategoryEntity = basicCategoryService.getCategoryByName(category);
        if (ObjectUtils.isEmpty(basicCategoryEntity)) {
            errorMsgList.add("产品分类不存在");
        } else {
            //父级品类
            List<BasicCategoryEntity> categoryList = basicCategoryService.listParentEntity(basicCategoryEntity.getId());
            if (CollectionUtils.isEmpty(categoryList)) {
                errorMsgList.add("产品分类不存在");
            }
            //一级品类
            BasicCategoryEntity bestEntity = categoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bestEntity) || StringUtils.isBlank(bestEntity.getCode())) {
                errorMsgList.add(ApiError.ERROR_95091.msg);
            }
            //二级品类
            BasicCategoryEntity secondEntity = categoryList.stream().filter(obj -> bestEntity.getId().equals(obj.getPid())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(secondEntity) || StringUtils.isBlank(secondEntity.getCode())) {
                errorMsgList.add(ApiError.ERROR_95092.msg);
            }
            productInfoDTO.setCategory(category);
            productInfoDTO.setCategoryId(basicCategoryEntity.getId());
        }

        String errStr = "";
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
        //spu信息
        productInfoDTO.setName(IdUtil.getSnowflake().nextIdStr());

/*        BasicCategoryEntity categoryByName = basicCategoryService.getCategoryByName(dto.getCategory());
        if (!ObjectUtils.isEmpty(categoryByName)) {
            productInfoDTO.setChargeId("产品类别Id");
            productInfoDTO.setCategory("产品类别");
        }*/
        productInfoDTO.setBrandId(productBrand.getId());
        productInfoDTO.setBrandName(productBrand.getValue());
        productInfoDTO.setApprovalStatus(0);
        productInfoDTO.setSpecType(1);
        productInfoDTO.setGrade("");
        productInfoDTO.setIsNoSpecAdd(MathUtil.ONE);
        if (!CollectionUtils.isEmpty(chargeNameList)) {
            productInfoDTO.setChargeName(chargeNameList.get(0).getUserName());
            productInfoDTO.setChargeId(chargeNameList.get(0).getUserId());
            productSkuBaseInfoDTO.setChargeName(chargeNameList.get(0).getUserName());
            productSkuBaseInfoDTO.setChargeId(chargeNameList.get(0).getUserId());
        }
        productInfoDTO.setMaterials(dto.getMaterials());
        productInfoDTO.setFunctionDesc(dto.getFunctionDesc());
        productInfoDTO.setSellSpot(dto.getSellSpot());
        productInfoDTO.setSaleMethod(dto.getSaleMethod());
        productInfoDTO.setUsageDesc(dto.getUsageDesc());
        productInfoDTO.setProperty(productProperty.getValue());
        productInfoDTO.setPropertyId(productProperty.getId());
        //sku信息
        BeanMapper.copy(dto, productSkuBaseInfoDTO);
        if (StringUtils.isNotBlank(dto.getPlanListingTimeStr())) {
            productSkuBaseInfoDTO.setPlanListingTime(LocalDateTime.parse(dto.getPlanListingTimeStr(), DateTimeFormatter.ofPattern(DateUtil.fmt_year_month)));
        }
         productSkuBaseInfoDTO.setProductState(2);
        productSkuBaseInfoDTO.setProductId("");
        productSkuBaseInfoDTO.setUnitId(productUnitEntity.getId());
        productSkuBaseInfoDTO.setUnitName(productUnitEntity.getName());
        productSkuBaseInfoDTO.setProductState(ProductDetailStateEnum.getCodeByName(productState));
        if (StringUtils.isNotBlank(dto.getFirstMassProductDateStr())) {
            productSkuBaseInfoDTO.setFirstMassProductDate(LocalDateTime.parse(dto.getFirstMassProductDateStr(), DateTimeFormatter.ofPattern(DateUtil.fmt_year_month)));
        }
        //spu/sku基础信息
        ProductBaseInfoDTO productBaseInfoDTO = new ProductBaseInfoDTO();
        productBaseInfoDTO.setProductSpuBaseInfoDTO(productInfoDTO);
        productBaseInfoDTO.setProductSkuBaseInfoDTO(productSkuBaseInfoDTO);
        productNoSpecDTO.setProductBaseInfoDTO(productBaseInfoDTO);

        //产品成本信息表
        ProductCostDTO productCostDTO = new ProductCostDTO();
        BeanMapper.copy(dto, productCostDTO);
        productNoSpecDTO.setProductCostDTO(productCostDTO);

        //采购信息信息
        ProductPurchaseDTO productPurchaseDTO = new ProductPurchaseDTO();
        productPurchaseDTO.setEan(dto.getEan());
        productPurchaseDTO.setPlanOrderQty(MathUtil.valueOfLong(dto.getPlanOrderQtyStr()));

        if (StringUtils.isNotBlank(dto.getPlaceOrderTimeStr())) {
            productPurchaseDTO.setPlaceOrderTime(LocalDate.parse(dto.getPlaceOrderTimeStr(), dateTimeFormatter));
        }
        if (StringUtils.isNotBlank(dto.getPlanArrivalTimeStr())) {
            productPurchaseDTO.setPlanArrivalTime(LocalDate.parse(dto.getPlanArrivalTimeStr(), dateTimeFormatter));
        }

        productPurchaseDTO.setMoq(MathUtil.valueOfInteger(dto.getMoqStr()));
        productPurchaseDTO.setDeliveryCycle(MathUtil.valueOf(dto.getDeliveryCycleStr()));
        if (StringUtils.isNotBlank(dto.getActualArrivalTimeStr())) {
            productPurchaseDTO.setActualArrivalTime(LocalDate.parse(dto.getActualArrivalTimeStr(), dateTimeFormatter));
        }

        productPurchaseDTO.setArrivalState(purchaseState);
        if (purchaseUserList.size() > 0) {
            productPurchaseDTO.setPurchaseUserId(purchaseUserList.get(0).getUserId());
        }
        productPurchaseDTO.setMainSupplier(dto.getMainSupplier());
        productPurchaseDTO.setSecondSupplier(dto.getSecondSupplier());
        productPurchaseDTO.setActualArrivalQty(MathUtil.valueOfLong(dto.getActualArrivalQtyStr()));
        productNoSpecDTO.setProductPurchaseDTO(productPurchaseDTO);

        //产品销售信息
        ProductSaleDTO productSaleDTO = new ProductSaleDTO();
        productSaleDTO.setYearSaleQty(MathUtil.valueOfLong(dto.getYearSaleQtyStr()));
        productSaleDTO.setYearSaleAmount(MathUtil.valueOf(dto.getYearSaleAmountStr()));
        productSaleDTO.setMonthSaleQty(MathUtil.valueOfLong(dto.getMonthSaleQtyStr()));
        productSaleDTO.setMonthSaleAmount(MathUtil.valueOf(dto.getMonthSaleAmountStr()));
        if (StringUtils.isNotBlank(dto.getDelistingTimeStr())) {
            productSaleDTO.setDelistingTime(LocalDate.parse(dto.getDelistingTimeStr(), dateTimeFormatter));
        }

        if (StringUtils.isNotBlank(dto.getListingTimeStr())) {
            productSaleDTO.setListingTime(LocalDate.parse(dto.getListingTimeStr(), dateTimeFormatter));
        }

        if (StringUtils.isNotBlank(saleCountryStr)) {
            productSaleDTO.setSaleCountry(saleCountryStr.substring(0,saleCountryStr.length()-1));
        }
        productSaleDTO.setSaleState(saleState);
        if (StringUtils.isNotBlank(isFinishedImg)) {
            if (isFinishedImg.equals("是")) {
                productSaleDTO.setIsFinishedImg(1);
            } else {
                productSaleDTO.setIsFinishedImg(2);
            }
        }
        if (StringUtils.isNotBlank(isFinishedVideo)) {
            if (isFinishedVideo.equals("是")) {
                productSaleDTO.setIsFinishedVideo(1);
            } else {
                productSaleDTO.setIsFinishedVideo(2);
            }
        }
        productNoSpecDTO.setProductSaleDTO(productSaleDTO);

        //产品物流信息
        ProductLogisticsDTO productLogisticsDTO = new ProductLogisticsDTO();
        BeanMapper.copy(dto, productLogisticsDTO);
        productLogisticsDTO.setProductProperty(declareProperty.getValue());
        productLogisticsDTO.setProductPropertyId(declareProperty.getId());
        productLogisticsDTO.setDeclarePrice(MathUtil.valueOf(dto.getDeclarePriceStr()));
        productNoSpecDTO.setProductLogisticsDTO(productLogisticsDTO);

        //产品包装信息
        ProductPackDTO productPackDTO = new ProductPackDTO();
        BeanMapper.copy(dto, productPackDTO);
        String productSize = "";
        String boxSize ="";
        String productSizeLength = dto.getProductSizeLengthStr();
        String productSizeWide = dto.getProductSizeWideStr();
        String productSizeHigh = dto.getProductSizeHighStr();
        String boxSizeLength = dto.getBoxSizeLengthStr();
        String boxSizeWide = dto.getBoxSizeWideStr();
        String boxSizeHigh = dto.getBoxSizeHighStr();

        if (StringUtils.isNotBlank(productSizeLength)) {
            productSize = productSizeLength;
        }
        if (StringUtils.isNotBlank(productSizeWide)) {
            productSize = productSize.concat("X").concat(productSizeWide);
        }
        if (StringUtils.isNotBlank(productSizeHigh)) {
            productSize = productSize.concat("X").concat(productSizeHigh);
        }
        productPackDTO.setProductSize(productSize);

        if (StringUtils.isNotBlank(boxSizeLength)) {
            boxSize = boxSizeLength;
        }
        if (StringUtils.isNotBlank(boxSizeWide)) {
            boxSize = boxSize.concat("X").concat(boxSizeWide);
        }
        if (StringUtils.isNotBlank(boxSizeHigh)) {
            boxSize = boxSize.concat("X").concat(boxSizeHigh);
        }
        productPackDTO.setGrossWeight(MathUtil.valueOf(dto.getGrossWeightStr()));
        productPackDTO.setNetWeight(MathUtil.valueOf(dto.getNetWeightStr()));
        productPackDTO.setBoxSize(boxSize);
        productPackDTO.setBoxQty(MathUtil.valueOf(dto.getBoxQtyStr()));
        productPackDTO.setBoxWeight(MathUtil.valueOf(dto.getBoxWeightStr()));
        productNoSpecDTO.setProductPackDTO(productPackDTO);

        /*//产品证书信息
        ProductCertificateDTO productCertificateDTO = new ProductCertificateDTO();
        BeanMapper.copy(dto, productCertificateDTO);
        productNoSpecDTO.setProductCertificateDTO(productCertificateDTO);*/

        productDetailService.inportExcel(productNoSpecDTO);
    }

    public List<ProductDetailExcelDTO> getDateList(){
        return list;
    }

    public List<ProductDetailExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @Description 全部解析完回调此方法
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     * @param analysisContext: 解析器上下文
     **/
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
