package com.erp.server.oms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.FullyManagedImportExcelDTO;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.OrderCategoryDetailService;
import com.erp.server.oms.service.ShopInfoService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 全托管 销售订单导入
 */
public class FullyManagedImportExcelListener extends AnalysisEventListener<FullyManagedImportExcelDTO> {
    /**
     * 错误信息
     */
    @Getter
    private List<FullyManagedImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<FullyManagedImportExcelDTO> dataList = new ArrayList<>();

    /**
     * 数据校验成功的
     */
    @Getter
    private List<FullyManagedImportExcelDTO> successList = new ArrayList<>();

    private final DictBasicService dictBasicService = SpringUtil.getBean(DictBasicService.class);
    private final ShopInfoService shopInfoService = SpringUtil.getBean(ShopInfoService.class);
    private final SysUserFeign sysUserFeign = SpringUtil.getBean(SysUserFeign.class);
    private final OrderCategoryDetailService orderCategoryDetailService = SpringUtil.getBean(OrderCategoryDetailService.class);
    private final LogisticsFeign logisticsFeign = SpringUtil.getBean(LogisticsFeign.class);
    private final PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    private final WmsTaskFeign wmsTaskFeign = SpringUtil.getBean(WmsTaskFeign.class);

    public FullyManagedImportExcelListener() {
    }

    /**
     * 每解析一行执行一次
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-04-21 19:40
     */
    @Override
    public void invoke(FullyManagedImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        Integer rowNumber = analysisContext.readSheetHolder().getApproximateTotalRowNumber();
        if (rowNumber > 5000) {
            errorMsgList.add("导入最高要支持5000条");
        }
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        try {
            excelDTO.setIndex(Integer.valueOf(excelDTO.getIndexStr()));
        }catch (Exception e){
            errorMsgList.add("序号格式错误");
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getPayTimeStr())) {
            try {
                excelDTO.setPayTime(LocalDateUtil.stringToLocalDateTime(excelDTO.getPayTimeStr()));
            }catch (Exception e){
                errorMsgList.add("支付时间格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getRequiredDeliveryTimeStr())) {
            try {
                excelDTO.setRequiredDeliveryTime(LocalDateUtil.stringToLocalDateTime(excelDTO.getRequiredDeliveryTimeStr()));
            }catch (Exception e){
                errorMsgList.add("预计发货时间格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getRequiredReceiveTimeStr())) {
            try {
                excelDTO.setRequiredReceiveTime(LocalDateUtil.stringToLocalDateTime(excelDTO.getRequiredReceiveTimeStr()));
            }catch (Exception e){
                errorMsgList.add("预计收货时间格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getAmountStr())) {
            try {
                excelDTO.setAmount(new BigDecimal(excelDTO.getAmountStr()));
            }catch (Exception e){
                errorMsgList.add("订单金额格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getPriceStr())) {
            try {
                excelDTO.setPrice(new BigDecimal(excelDTO.getPriceStr()));
            }catch (Exception e){
                errorMsgList.add("真实售价格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getTaxRateStr())) {
            try {
                excelDTO.setTaxRate(new BigDecimal(excelDTO.getTaxRateStr()));
            }catch (Exception e){
                errorMsgList.add("税率式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getActualShippingCostStr())) {
            try {
                excelDTO.setActualShippingCost(new BigDecimal(excelDTO.getActualShippingCostStr()));
            }catch (Exception e){
                errorMsgList.add("实际运费格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getQtyStr())) {
            try {
                excelDTO.setQty(new Integer(excelDTO.getQtyStr()));
            }catch (Exception e){
                errorMsgList.add("下单数量格式错误");
            }
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getPackageSize())) {
            //根据*拆分，并转换成长宽高
            String[] sizeArr = excelDTO.getPackageSize().split("\\*");
            if (sizeArr.length == 3) {
                excelDTO.setLength(new BigDecimal(sizeArr[0]));
                excelDTO.setWidth(new BigDecimal(sizeArr[1]));
                excelDTO.setHeight(new BigDecimal(sizeArr[2]));
            }else {
                errorMsgList.add("包装尺寸格式错误");
            }
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        //数据校验是成功的
        dataList.add(excelDTO);
    }

    public List<FullyManagedImportExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * 所有执行完成后执行
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        List<String> shopNameList = dataList.stream().map(FullyManagedImportExcelDTO::getShopName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> channelNameList = dataList.stream().map(FullyManagedImportExcelDTO::getChannelName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> skuNoList = dataList.stream().map(FullyManagedImportExcelDTO::getSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> warehouseNameList = dataList.stream().map(FullyManagedImportExcelDTO::getDeliveryWarehouseName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());

        //全托管平台类型
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.FULLY_MANAGED.getType());
        //店铺列表
        List<ShopInfoDTO.ListDTO> shopList = shopInfoService.listShopByName(shopNameList);
        //币种列表
        List<DictCurrencyEntity> currencyEntityList = sysUserFeign.currencyList();
        //订单来源类型
        List<DictBasicDTO.ViewDTO> orderSourceTypeList = dictBasicService.getByKey(DictBasicTypeEnum.ORDER_SOURCE_TYPE.getType());
        //类目列表
        List<OrderCategoryDetailDTO.ListDTO> categoryList = orderCategoryDetailService.listOrderCategory();
        //渠道列表
        List<LogisticsChannelEntity> logisticsChannelEntityList = logisticsFeign.listChannelByNameList(channelNameList);
        //sku列表
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        //仓库列表
        List<WarehouseDTO.ListDTO> warehouseList = wmsTaskFeign.listWarehouseByNameList(warehouseNameList);

        for (FullyManagedImportExcelDTO excelDTO : dataList) {
            List<String> errorMsgList = new ArrayList<>();
            //相同序号的平台订单号是否一致
            List<FullyManagedImportExcelDTO> sameIndexList = dataList.stream().filter(v -> v.getIndex().equals(excelDTO.getIndex())).collect(Collectors.toList());
            List<String> platformCount = sameIndexList.stream().map(FullyManagedImportExcelDTO::getPlatformCode).distinct().collect(Collectors.toList());
            if(platformCount.size() > 1){
                errorMsgList.add("序号【"+excelDTO.getIndex()+"】存在多个平台订单号");
            }
            List<String> dictPlatformCount = sameIndexList.stream().map(FullyManagedImportExcelDTO::getDictPlatformName).distinct().collect(Collectors.toList());
            if(dictPlatformCount.size() > 1){
                errorMsgList.add("序号【"+excelDTO.getIndex()+"】存在多个平台类型");
            }
            //平台类型
            String platform = dictList.stream().filter(v -> v.getName().equals(excelDTO.getDictPlatformName())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse(null);
            if(Objects.isNull(platform)){
                errorMsgList.add("全托管平台类型不存在【"+excelDTO.getDictPlatformName()+"】");
            }else {
                excelDTO.setDictPlatform(platform);
            }
            //店铺
            ShopInfoDTO.ListDTO shop = shopList.stream().filter(v -> v.getName().equals(excelDTO.getShopName())).findFirst().orElse(null);
            if(Objects.isNull(shop)){
                errorMsgList.add(ApiError.SHOP_NOT_EXIST_NO_PERMISSION.getMsg());
            }else {
                excelDTO.setShopId(shop.getId());
            }
            //币种
            DictCurrencyEntity currency = currencyEntityList.stream().filter(v -> v.getId().equals(excelDTO.getCurrencyCode())).findFirst().orElse(null);
            if(Objects.isNull(currency)){
                errorMsgList.add("币种不存在【"+excelDTO.getCurrencyCode()+"】");
            }else {
                excelDTO.setCurrency(currency.getId());
            }
            //订单来源类型
            String orderSourceType = orderSourceTypeList.stream().filter(v -> v.getName().equals(excelDTO.getOrderSourceTypeName())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse(null);
            if(Objects.isNull(orderSourceType)){
                errorMsgList.add("订单来源类型不存在【"+excelDTO.getOrderSourceTypeName()+"】");
            }else {
                excelDTO.setOrderSourceType(orderSourceType);
            }
            //类目
            if (CharSequenceUtil.isNotBlank(excelDTO.getCategoryNameList())){
                List<String> categoryIds = categoryList.stream().filter(v -> excelDTO.getCategoryNameList().equals(v.getName())).map(OrderCategoryDetailDTO.ListDTO::getId).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(categoryIds)){
                    errorMsgList.add("类目不存在【"+excelDTO.getCategoryNameList()+"】");
                }else {
                    excelDTO.setCategoryIdList(categoryIds);
                }
            }
            //渠道
            if (CharSequenceUtil.isNotBlank(excelDTO.getChannelName())){
                String channelId = logisticsChannelEntityList.stream().filter(v -> v.getName().equals(excelDTO.getChannelName())).map(LogisticsChannelEntity::getId).findFirst().orElse(null);
                if(Objects.isNull(channelId)){
                    errorMsgList.add("渠道不存在【"+excelDTO.getChannelName()+"】");
                }else {
                    excelDTO.setChannelId(channelId);
                }
            }
            //sku
            String skuId = skuList.stream().filter(v -> v.getSkuNo().equals(excelDTO.getSkuNo())).map(SkuVO::getSkuId).findFirst().orElse(null);
            if(Objects.isNull(skuId)){
                errorMsgList.add("SKU不存在【"+excelDTO.getSkuNo()+"】");
            }else {
                excelDTO.setSkuId(skuId);
            }
            //仓库
            if (CharSequenceUtil.isNotBlank(excelDTO.getDeliveryWarehouseName())){
                WarehouseDTO.ListDTO warehouse = warehouseList.stream().filter(v -> v.getName().equals(excelDTO.getDeliveryWarehouseName())).findFirst().orElse(null);
                if(Objects.isNull(warehouse)){
                    errorMsgList.add(ApiError.WH_NOT_EXIST_OR_NO_PERMISSION.getMsg());
                }else {
                    excelDTO.setDeliveryWarehouseId(warehouse.getId());
                }
            }
            //保存里面的验证
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                //错误数据
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
            }else {
                //成功数据
                successList.add(excelDTO);
            }
        }
    }

}
