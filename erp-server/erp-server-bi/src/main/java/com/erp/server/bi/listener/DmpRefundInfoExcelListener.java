package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.DmpRefundInfoImportExcelDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.bi.enums.RefundStatusEnum;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpRefundInfoService;
import com.erp.server.bi.service.DmpRefundItemService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmpRefundInfoExcelListener extends AnalysisEventListener<DmpRefundInfoImportExcelDTO> {
    private Integer importType;


    private DmpOrderInfoService dmpOrderInfoService;

    private DmpShopInfoService dmpShopInfoService;

    private DmpRefundInfoService dmpRefundInfoService;

    private DmpRefundItemService dmpRefundItemService;

    private PlmTaskFeign plmTaskFeign;

    private List<DmpRefundInfoImportExcelDTO> list;

    private List<DmpRefundInfoEntity> refundList;

    public DmpRefundInfoExcelListener(Integer importType,List<DmpRefundInfoEntity> refundList, DmpOrderInfoService dmpOrderInfoService, DmpRefundInfoService dmpRefundInfoService
            , DmpShopInfoService dmpShopInfoService,DmpRefundItemService dmpRefundItemService, PlmTaskFeign plmTaskFeign) {
        this.importType = importType;
        this.dmpOrderInfoService = dmpOrderInfoService;
        this.dmpShopInfoService = dmpShopInfoService;
        this.dmpRefundInfoService = dmpRefundInfoService;
        this.dmpRefundItemService = dmpRefundItemService;
        this.plmTaskFeign = plmTaskFeign;
        this.refundList = refundList;
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
    public void invoke(DmpRefundInfoImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        DmpRefundInfoEntity entity = new DmpRefundInfoEntity();
        if (StringUtils.isBlank(dto.getRefundId())) {
            errorMsgList.add("退款单号不能为空");
        }
        if (StringUtils.isBlank(dto.getPlatformOrderId())) {
            errorMsgList.add("订单号不能为空");
        }
        if (StringUtils.isNotBlank(dto.getRefundId()) && dto.getRefundId().length() > 50) {
            errorMsgList.add("退款单号不能超过50个字节");
        }
        if (!StrUtils.isLetterDigit(dto.getRefundId())) {
            errorMsgList.add("退款单号只能包含字母和数字");
        }

        if (CollectionUtils.isNotEmpty(refundList)) {
            long count = refundList.stream().filter(obj -> obj.getRefundId().equals(dto.getRefundId())).count();
            if (count > 0) {
                errorMsgList.add("退款单号已存在，不能重复添加");
            }
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
            errorMsgList.add("退款数量必须大于0");
        }
        if (MathUtil.compareTo(dto.getRefundAmount(),MathUtil.ZERO) <= 0) {
            errorMsgList.add("退款金额必须大于0");
        }
        if (StringUtils.isBlank(dto.getRefundStatusName())) {
            errorMsgList.add("退款状态不能为空");
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
        if (StringUtils.isNotBlank(dto.getRefundStatusName())) {
            saleState = RefundStatusEnum.getCodeByName(dto.getRefundStatusName());
            if (saleState == null || saleState == 0) {
                errorMsgList.add("退款单状态不正确：退款单状态：新建退款，审核中，财务审核，成功，失败，作废");
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
        DmpRefundInfoEntity dmpRefundInfoEntity = dmpRefundInfoService.getByRefundId(dto.getRefundId());
        if (ObjectUtils.isEmpty(dmpRefundInfoEntity)) {
            BeanUtils.copyProperties(dto,entity);
            entity.setRefundStatus(RefundStatusEnum.getCodeByName(dto.getRefundStatusName()));
            entity.setOrderTime(dmpOrderInfoEntity.getPlatformCreateTime());
            dmpRefundInfoService.save(entity);
        }
        //同订单sku新增到同一订单下
        DmpRefundItemEntity itemEntity = new DmpRefundItemEntity();
        if (ObjectUtils.isNotEmpty(dmpRefundInfoEntity)) {
            itemEntity.setRefundId(dmpRefundInfoEntity.getId());
        } else {
            itemEntity.setRefundId(entity.getId());
        }
        itemEntity.setRefundNum(dto.getRefundNum());
        itemEntity.setQuantity(dto.getRefundNum());
        itemEntity.setSkuNo(dto.getSkuNo());
        dmpRefundItemService.save(itemEntity);
    }

    public List<DmpRefundInfoImportExcelDTO> getDateList(){
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
