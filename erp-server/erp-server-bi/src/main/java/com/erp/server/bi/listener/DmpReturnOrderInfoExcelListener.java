package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.DmpReturnOrderInfoImportExcelDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.bi.enums.ReturnOrderStatusEnum;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderItemService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmpReturnOrderInfoExcelListener extends AnalysisEventListener<DmpReturnOrderInfoImportExcelDTO> {
    private Integer importType;


    private DmpOrderInfoService dmpOrderInfoService;

    private DmpShopInfoService dmpShopInfoService;

    private DmpReturnOrderInfoService dmpReturnOrderInfoService;

    private DmpReturnOrderItemService dmpReturnOrderItemService;

    private PlmTaskFeign plmTaskFeign;

    private List<DmpReturnOrderInfoImportExcelDTO> list;

    private List<DmpReturnOrderInfoEntity> returnOrderList;

    public DmpReturnOrderInfoExcelListener(Integer importType,List<DmpReturnOrderInfoEntity> returnOrderList, DmpOrderInfoService dmpOrderInfoService, DmpReturnOrderInfoService dmpReturnOrderInfoService
            , DmpShopInfoService dmpShopInfoService, DmpReturnOrderItemService dmpReturnOrderItemService,PlmTaskFeign plmTaskFeign) {
        this.importType = importType;
        this.dmpOrderInfoService = dmpOrderInfoService;
        this.dmpShopInfoService = dmpShopInfoService;
        this.dmpReturnOrderInfoService = dmpReturnOrderInfoService;
        this.dmpReturnOrderItemService = dmpReturnOrderItemService;
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
    @Transactional
    public void invoke(DmpReturnOrderInfoImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        DmpReturnOrderInfoEntity entity = new DmpReturnOrderInfoEntity();

        if (StringUtils.isBlank(dto.getReturnOrderId())) {
            errorMsgList.add("退货单号不能为空");
        }
        if (CollectionUtils.isNotEmpty(returnOrderList)) {
            long count = returnOrderList.stream().filter(obj -> obj.getReturnOrderId().equals(dto.getReturnOrderId())).count();
            if (count > 0) {
                errorMsgList.add("退货单号已存在，不能重复添加");
            }
        }
        if (StringUtils.isBlank(dto.getPlatformOrderId())) {
            errorMsgList.add("原订单号不能为空");
        }
        if (StringUtils.isNotBlank(dto.getReturnOrderId()) && dto.getReturnOrderId().length() > 50) {
            errorMsgList.add("退货单号不能超过50个字节");
        }
        if (!StrUtils.isLetterDigitBar(dto.getReturnOrderId())) {
            errorMsgList.add("退货单号只能包含字母、数字、-");
        }
        if (StringUtils.isNotBlank(dto.getPlatformOrderId()) && dto.getPlatformOrderId().length() > 50) {
            errorMsgList.add("订单号不能超过50个字节");
        }
        if (StringUtils.isBlank(dto.getPlatformName())) {
            errorMsgList.add("平台名称不能为空");
        }
        if (StringUtils.isBlank(dto.getShopName())) {
            errorMsgList.add("店铺名称不能为空");
        }
        if(StringUtils.isBlank(dto.getSkuNo())) {
            errorMsgList.add("SKU不能为空");
        } else {
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
        if (StringUtils.isBlank(dto.getStatusName())) {
            errorMsgList.add("退货状态不能为空");
        }
        if (StringUtils.isBlank(dto.getCurrencyCode())) {
            errorMsgList.add("币种不能为空");
        }
        CurrencyEnum currencyEnum = CurrencyEnum.getByCode(dto.getCurrencyCode());
        if (ObjectUtils.isEmpty(currencyEnum)) {
            errorMsgList.add("系统中未发现该币种！");
        }
        if (StringUtils.isNotBlank(dto.getShopName())) {
            Integer count = dmpShopInfoService.getDmpShopInfoByParam(dto.getPlatformName(),null, dto.getShopName());
            if (count == 0) {
                errorMsgList.add("在平台站点中未找到该店铺");
            }
        }

        //查询订单
        DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getByPlatformOrderId(dto.getPlatformOrderId());
        if(ObjectUtils.isEmpty(dmpOrderInfoEntity)) {
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
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = dmpReturnOrderInfoService.getByReturnOrderId(dto.getReturnOrderId());
        if (ObjectUtils.isEmpty(dmpReturnOrderInfoEntity)) {
            BeanUtils.copyProperties(dto,entity);
            entity.setStatus(ReturnOrderStatusEnum.getCodeByName(dto.getStatusName()));
            entity.setOrderTime(dmpOrderInfoEntity.getPlatformCreateTime());
            dmpReturnOrderInfoService.save(entity);
        }
        //同订单sku新增到同一订单下
        DmpReturnOrderItemEntity itemEntity = new DmpReturnOrderItemEntity();
        if (ObjectUtils.isNotEmpty(dmpReturnOrderInfoEntity)) {
            itemEntity.setReturnOrderId(dmpReturnOrderInfoEntity.getId());
        } else {
            itemEntity.setReturnOrderId(entity.getId());
        }
        itemEntity.setQuantity(dto.getRefundNum());
        itemEntity.setSkuNo(dto.getSkuNo());
        itemEntity.setSellPrice(MathUtil.divide(dto.getOrderFee(),new BigDecimal(dto.getRefundNum())));
        dmpReturnOrderItemService.save(itemEntity);

        //根据明细金额更新主表订单金额
        dmpReturnOrderInfoService.updateOrderFeeById(itemEntity.getReturnOrderId());
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
