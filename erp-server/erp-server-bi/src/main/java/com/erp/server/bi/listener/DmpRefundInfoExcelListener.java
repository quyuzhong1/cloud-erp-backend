package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.DmpRefundInfoImportExcelDTO;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiRefundInfoEntity;
import com.erp.model.dmp.entity.BiRefundItemEntity;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.bi.enums.RefundStatusEnum;
import com.erp.server.bi.service.BiOrderInfoService;
import com.erp.server.bi.service.BiRefundInfoService;
import com.erp.server.bi.service.BiRefundItemService;
import com.erp.server.bi.service.BiShopInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmpRefundInfoExcelListener extends AnalysisEventListener<DmpRefundInfoImportExcelDTO> {

    private BiOrderInfoService biOrderInfoService;

    private BiShopInfoService biShopInfoService;

    private BiRefundInfoService biRefundInfoService;

    private BiRefundItemService biRefundItemService;

    private PlmTaskFeign plmTaskFeign;

    private List<DmpRefundInfoImportExcelDTO> list;

    private List<BiRefundInfoEntity> refundList;

    public DmpRefundInfoExcelListener(List<BiRefundInfoEntity> refundList, BiOrderInfoService biOrderInfoService, BiRefundInfoService biRefundInfoService
            , BiShopInfoService biShopInfoService, BiRefundItemService biRefundItemService, PlmTaskFeign plmTaskFeign) {
        this.biOrderInfoService = biOrderInfoService;
        this.biShopInfoService = biShopInfoService;
        this.biRefundInfoService = biRefundInfoService;
        this.biRefundItemService = biRefundItemService;
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
    @Transactional(rollbackFor = Exception.class)
    public void invoke(DmpRefundInfoImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        BiRefundInfoEntity entity = new BiRefundInfoEntity();
        if (!StrUtils.isLetterDigitBar(dto.getRefundCode())) {
            errorMsgList.add("退款单号只能包含字母、数字、-");
        }

        if (CollectionUtils.isNotEmpty(refundList)) {
            long count = refundList.stream().filter(obj -> obj.getRefundCode().equals(dto.getRefundCode())).count();
            if (count > 0) {
                errorMsgList.add("退款单号已存在，不能重复添加");
            }
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
            errorMsgList.add("退款数量必须大于0");
        }
        if (MathUtil.compareTo(dto.getRefundAmount(),MathUtil.ZERO) <= 0) {
            errorMsgList.add("退款金额必须大于0");
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
        if (StringUtils.isNotBlank(dto.getRefundStatusName())) {
            saleState = RefundStatusEnum.getCodeByName(dto.getRefundStatusName());
            if (saleState == null || saleState == 0) {
                errorMsgList.add("退款单状态不正确：退款单状态：新建退款，审核中，财务审核，成功，失败，作废");
            }
        }

        StringBuilder errStr = new StringBuilder();
        if (!errorMsgList.isEmpty()) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr.append(indexTemp).append("、").append(errorMsgList.get(i)).append("；");
            }
            dto.setErrorMsg(errStr.toString());
            list.add(dto);
            return;
        }
        BiRefundInfoEntity biRefundInfoEntity = biRefundInfoService.getByRefundId(dto.getRefundCode());
        if (ObjectUtils.isEmpty(biRefundInfoEntity)) {
            BeanUtils.copyProperties(dto,entity);
            entity.setRefundStatus(RefundStatusEnum.getCodeByName(dto.getRefundStatusName()));
            entity.setOrderTime(biOrderInfoEntity.getPlatformCreateTime());
            biRefundInfoService.save(entity);
        }
        //同订单sku新增到同一订单下
        BiRefundItemEntity itemEntity = new BiRefundItemEntity();
        if (ObjectUtils.isNotEmpty(biRefundInfoEntity)) {
            itemEntity.setRefundId(biRefundInfoEntity.getId());
        } else {
            itemEntity.setRefundId(entity.getId());
        }
        itemEntity.setRefundNum(dto.getRefundNum());
        itemEntity.setQuantity(dto.getRefundNum());
        itemEntity.setSkuNo(dto.getSkuNo());
        biRefundItemService.save(itemEntity);
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
        // document why this method is empty
    }
}
