package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.StrUtils;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.enums.*;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductUnitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailExcelListener extends AnalysisEventListener<ProductDetailExcelDTO> {
    private Integer importType;

    private ProductDetailService productDetailService;

    private ProductUnitService productUnitService;

    private BasicCategoryService basicCategoryService;

    private BasicDictService basicDictService;

    private SysUserFeign sysUserFeign;

    private List<ProductDetailExcelDTO> list;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");

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
        
        if (StringUtils.isBlank(dto.getName())) {
            errorMsgList.add("产品名称不能为空");
        }
        if (StringUtils.isNotBlank(dto.getName()) && dto.getName().length() > 250) {
            errorMsgList.add("产品名称不能超过250个字节");
        }
        if (StringUtils.isBlank(dto.getChargeName())) {
            errorMsgList.add("产品负责人不能为空");
        }
        if (StringUtils.isBlank(dto.getBrandName())) {
            errorMsgList.add("产品品牌不能为空");
        }
        if (StringUtils.isBlank(dto.getSkuNo())) {
            errorMsgList.add("sku不能为空");
        }
        if (!StrUtils.isLetterDigit(dto.getSkuNo())) {
            errorMsgList.add("sku只能包含数字和字母");
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
                    errorMsgList.add(ApiError.ERROR_95007.msg);
                }
            }
        } else {
            //sku重复
            if (productDetailService.checkSkuNo(dto.getSkuNo(), "")) {
                errorMsgList.add(ApiError.ERROR_95015.msg);
            }
            if (productDetailService.checkName(dto.getName(), "")) {
                errorMsgList.add(ApiError.ERROR_95007.msg);
            }
        }

        //销售方式不正确
        if(StringUtils.isNotBlank(dto.getSaleMethod())) {
            String[] saleMethodList = dto.getSaleMethod().split(",");
            for (String saleMethod : saleMethodList) {
                if (SaleMethodEnum.getCodeByName(saleMethod) == null) {
                    errorMsgList.add("销售方式不正确");
                }
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
      /*
        List<BasicDictEntity> basicDictEntities = basicDictService.listByIds(Arrays.asList(split));
        List<String> nameList = basicDictEntities.stream().map(BasicDictEntity::getValue).collect(Collectors.toList());
        req.setSaleCountry(StringUtils.join(nameList, ","));*/


        //存在侵权风险
        String pirateRisk = dto.getPirateRisk();
        if(StringUtils.isNotBlank(pirateRisk)){
            if(!pirateRisk.equals("有") && !pirateRisk.equals("无")){
                errorMsgList.add("存在侵权风险：有 或者 无");
            }
            if(pirateRisk.equals("有")){
                productInfoDTO.setPirateRisk(1);
            } else {
                productInfoDTO.setPirateRisk(2);
            }
        }
        List<FindUserDTO> chargeNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getChargeName())) {
            BaseSearchDTO baseSearchDTO = new BaseSearchDTO();
            baseSearchDTO.setSearchKeyword(dto.getChargeName());
            ApiResult<List<FindUserDTO>> listApiResult = sysUserFeign.userList(baseSearchDTO);
            chargeNameList = listApiResult.getData();
            if (CollectionUtils.isEmpty(chargeNameList)) {
                errorMsgList.add("产品经理在系统中未找到");
            }
        }

        List<FindUserDTO> purchaseUserList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getPurchaseUser())) {
            BaseSearchDTO baseSearchDTO = new BaseSearchDTO();
            baseSearchDTO.setSearchKeyword(dto.getPurchaseUser());
            ApiResult<List<FindUserDTO>> listApiResult = sysUserFeign.userList(baseSearchDTO);
            purchaseUserList =  listApiResult.getData();
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
            if (saleState == null || saleState == 0) {
                errorMsgList.add("销售状态不正确：销售状态：未销售，销售中，清仓中，已下架");
            }
        }

        Integer purchaseState = null;
        if (StringUtils.isNotBlank(dto.getArrivalState())) {
            purchaseState = PurchaseStateEnum.getCodeByName(dto.getArrivalState());
            if (purchaseState == null || purchaseState == 0) {
                errorMsgList.add("首批到货状态不正确：首批到货状态：1.未到货 2.已到货 3.部分到货");
            }
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
        if(StringUtils.isNotBlank(isFinishedImg)){
            if(!isFinishedImg.equals("是") && !isFinishedImg.equals("否")){
                errorMsgList.add("图片是否完成：是 或者 否");

            }
        }

        //视频是否完成
        String isFinishedVideo = dto.getIsFinishedVideo();
        if(StringUtils.isNotBlank(isFinishedVideo)) {
            if(!isFinishedVideo.equals("是") && !isFinishedVideo.equals("否")){
                errorMsgList.add("视频是否完成：是 或者 否");
            }
        }
        //产品开发状态
        String productState = dto.getProductState();
        if (StringUtils.isNotBlank(productState)) {
            Integer code = ProductDetailStateEnum.getCodeByName(productState);
            if (ObjectUtils.isEmpty(code)) {
                errorMsgList.add("产品开发状态有误");
            }
        }

        //产品分类
        String category = dto.getCategory();
        if (StringUtils.isBlank(category)) {
            errorMsgList.add("产品分类不能为空");
        } else {
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
        productInfoDTO.setName(dto.getName());

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
        productSkuBaseInfoDTO.setPlanListingTime(dto.getPlanListingTime());
        productSkuBaseInfoDTO.setProductState(2);
        productSkuBaseInfoDTO.setProductId("");
        productSkuBaseInfoDTO.setUnitId(productUnitEntity.getId());
        productSkuBaseInfoDTO.setUnitName(productUnitEntity.getName());
        productSkuBaseInfoDTO.setProductState(ProductDetailStateEnum.getCodeByName(productState));
        productSkuBaseInfoDTO.setFirstMassProductDate(dto.getFirstMassProductDate());

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
        productPurchaseDTO.setPlanOrderQty(dto.getPlanOrderQty());
        productPurchaseDTO.setPlaceOrderTime(dto.getPlaceOrderTime());
        productPurchaseDTO.setPlanArrivalTime(dto.getPlanArrivalTime());
        productPurchaseDTO.setMoq(dto.getMoq());
        productPurchaseDTO.setDeliveryCycle(dto.getDeliveryCycle());
        productPurchaseDTO.setActualArrivalTime(dto.getActualArrivalTime());
        productPurchaseDTO.setArrivalState(purchaseState);
        if (purchaseUserList.size() > 0) {
            productPurchaseDTO.setPurchaseUserId(purchaseUserList.get(0).getUserId());
        }
        productPurchaseDTO.setMainSupplier(dto.getMainSupplier());
        productPurchaseDTO.setSecondSupplier(dto.getSecondSupplier());
        productPurchaseDTO.setActualArrivalQty(dto.getActualArrivalQty());
        productNoSpecDTO.setProductPurchaseDTO(productPurchaseDTO);

        //产品销售信息
        ProductSaleDTO productSaleDTO = new ProductSaleDTO();
        productSaleDTO.setYearSaleQty(dto.getYearSaleQty());
        productSaleDTO.setYearSaleAmount(dto.getYearSaleAmount());
        productSaleDTO.setMonthSaleQty(dto.getMonthSaleQty());
        productSaleDTO.setMonthSaleAmount(dto.getMonthSaleAmount());
        if (StringUtils.isNotBlank(saleCountryStr)) {
            productSaleDTO.setSaleCountry(saleCountryStr.substring(0,saleCountryStr.length()-1));
        }
/*        productSaleDTO.setListingTime(dto.getListingTime());
        productSaleDTO.setDelistingTime(dto.getDelistingTime());   */
        productSaleDTO.setListingTime(null);
        productSaleDTO.setDelistingTime(null);
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
        productNoSpecDTO.setProductLogisticsDTO(productLogisticsDTO);

        //产品包装信息
        ProductPackDTO productPackDTO = new ProductPackDTO();
        BeanMapper.copy(dto, productPackDTO);
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
