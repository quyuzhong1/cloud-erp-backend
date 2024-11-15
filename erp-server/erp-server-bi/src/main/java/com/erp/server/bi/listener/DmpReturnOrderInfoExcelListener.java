package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.DmpReturnOrderInfoImportExcelDTO;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.bi.enums.ReturnOrderStatusEnum;
import com.erp.server.bi.service.BiOrderInfoService;
import com.erp.server.bi.service.BiReturnOrderInfoService;
import com.erp.server.bi.service.BiReturnOrderItemService;
import com.erp.server.bi.service.BiShopInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmpReturnOrderInfoExcelListener extends AnalysisEventListener<DmpReturnOrderInfoImportExcelDTO> {

    private BiOrderInfoService biOrderInfoService;

    private BiShopInfoService biShopInfoService;

    private BiReturnOrderInfoService biReturnOrderInfoService;

    private BiReturnOrderItemService biReturnOrderItemService;

    private PlmTaskFeign plmTaskFeign;

    private List<DmpReturnOrderInfoImportExcelDTO> list;

    private List<BiReturnOrderInfoEntity> returnOrderList;

    public DmpReturnOrderInfoExcelListener(List<BiReturnOrderInfoEntity> returnOrderList, BiOrderInfoService biOrderInfoService, BiReturnOrderInfoService biReturnOrderInfoService
            , BiShopInfoService biShopInfoService, BiReturnOrderItemService biReturnOrderItemService, PlmTaskFeign plmTaskFeign) {
        this.biOrderInfoService = biOrderInfoService;
        this.biShopInfoService = biShopInfoService;
        this.biReturnOrderInfoService = biReturnOrderInfoService;
        this.biReturnOrderItemService = biReturnOrderItemService;
        this.plmTaskFeign = plmTaskFeign;
        this.returnOrderList = returnOrderList;
        this.list = new ArrayList<>();
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2022/12/16 10:26
    * @param dto
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(DmpReturnOrderInfoImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        BiReturnOrderInfoEntity entity = new BiReturnOrderInfoEntity();

        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        if (CollectionUtils.isNotEmpty(returnOrderList)) {
            long count = returnOrderList.stream().filter(obj -> obj.getReturnCode().equals(dto.getReturnCode())).count();
            if (count > 0) {
                errorMsgList.add("退货单号已存在，不能重复添加");
            }
        }
        if (!StrUtils.isLetterDigitBar(dto.getReturnCode())) {
            errorMsgList.add("退货单号只能包含字母、数字、-");
        }

        if(StringUtils.isNotBlank(dto.getSkuNo())) {
            if (!StrUtils.isLetterDigit(dto.getSkuNo())) {
                errorMsgList.add("SKU只能包含字母和数字");
            }
            //根据sku编号查询sku
            Map<String,String> skuParams = new HashMap<>();
            skuParams.put("skuNo",dto.getSkuNo());
            ProductDetailDTO productDetailDTO = plmTaskFeign.getSkuByParam(skuParams);
            if (ObjectUtils.isEmpty(productDetailDTO)) {
                errorMsgList.add("系统中不存在此sku编号");
            }
        }
        if (MathUtil.compareTo(dto.getRefundNum(),MathUtil.ZERO) <= 0) {
            errorMsgList.add("退货数量必须大于0");
        }
        if (MathUtil.compareTo(dto.getOrderFee(),MathUtil.ZERO) <= 0) {
            errorMsgList.add("退货金额必须大于0");
        }
        CurrencyEnum currencyEnum = CurrencyEnum.getByCode(dto.getCurrencyCode());
        if (ObjectUtils.isEmpty(currencyEnum)) {
            errorMsgList.add("系统中未发现该币种！");
        }
        if (StringUtils.isNotBlank(dto.getShopName())) {
            Integer count = biShopInfoService.getDmpShopInfoByParam(dto.getPlatformName(),null, dto.getShopName());
            if (count == 0) {
                errorMsgList.add("在平台站点中未找到该店铺");
            }
        }

        //查询订单
        BiOrderInfoEntity biOrderInfoEntity = biOrderInfoService.getByPlatformOrderId(dto.getPlatformOrderId());
        if(ObjectUtils.isEmpty(biOrderInfoEntity)) {
            errorMsgList.add("订单号系统中不存在");
        }

        Integer saleState = null;
        if (StringUtils.isNotBlank(dto.getStatusName())) {
            saleState = ReturnOrderStatusEnum.getCodeByName(dto.getStatusName());
            if (saleState == null || saleState == 0) {
                errorMsgList.add("退货单状态不正确：退货单状态：待处理，已退款，已重发，已完成，已作废");
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
        //查询退货
        BiReturnOrderInfoEntity biReturnOrderInfoEntity = biReturnOrderInfoService.getByReturnOrderId(dto.getReturnCode());
        if (ObjectUtils.isEmpty(biReturnOrderInfoEntity)) {
            BeanUtils.copyProperties(dto,entity);
            entity.setStatus(ReturnOrderStatusEnum.getCodeByName(dto.getStatusName()));
            entity.setOrderTime(biOrderInfoEntity.getPlatformCreateTime());
            biReturnOrderInfoService.save(entity);
        }
        //同订单sku新增到同一订单下
        BiReturnOrderItemEntity itemEntity = new BiReturnOrderItemEntity();
        if (ObjectUtils.isNotEmpty(biReturnOrderInfoEntity)) {
            itemEntity.setReturnOrderId(biReturnOrderInfoEntity.getId());
        } else {
            itemEntity.setReturnOrderId(entity.getId());
        }
        itemEntity.setQuantity(dto.getRefundNum());
        itemEntity.setSkuNo(dto.getSkuNo());
        itemEntity.setSellPrice(MathUtil.divide(dto.getOrderFee(),new BigDecimal(dto.getRefundNum())));
        biReturnOrderItemService.save(itemEntity);

        //根据明细金额更新主表订单金额
        biReturnOrderInfoService.updateOrderFeeById(itemEntity.getReturnOrderId());
    }

    public List<DmpReturnOrderInfoImportExcelDTO> getDateList(){
        return list;
    }

    /**
     * @description: 全部解析完回调此方法
     * @author Will
     * @date: 2022/12/16 10:26
     * @param analysisContext

     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
