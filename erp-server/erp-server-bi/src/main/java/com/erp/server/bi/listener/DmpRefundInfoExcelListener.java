package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.bi.dto.DmpRefundInfoImportExcelDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.server.bi.enums.RefundStatusEnum;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpRefundInfoService;
import com.erp.server.bi.service.DmpRefundItemService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DmpRefundInfoExcelListener extends AnalysisEventListener<DmpRefundInfoImportExcelDTO> {
    private Integer importType;


    private DmpOrderInfoService dmpOrderInfoService;

    private DmpShopInfoService dmpShopInfoService;

    private DmpRefundInfoService dmpRefundInfoService;

    private DmpRefundItemService dmpRefundItemService;


    private List<DmpRefundInfoImportExcelDTO> list;

    public DmpRefundInfoExcelListener(Integer importType, DmpOrderInfoService dmpOrderInfoService, DmpRefundInfoService dmpRefundInfoService
            , DmpShopInfoService dmpShopInfoService,DmpRefundItemService dmpRefundItemService) {
        this.importType = importType;
        this.dmpOrderInfoService = dmpOrderInfoService;
        this.dmpShopInfoService = dmpShopInfoService;
        this.dmpRefundInfoService = dmpRefundInfoService;
        this.dmpRefundItemService = dmpRefundItemService;
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
    public void invoke(DmpRefundInfoImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        DmpRefundInfoEntity entity = new DmpRefundInfoEntity();
        DmpRefundItemEntity itemEntity = new DmpRefundItemEntity();
        if (StringUtils.isBlank(dto.getRefundId())) {
            errorMsgList.add("退款单号不能为空");
        }
        if (StringUtils.isBlank(dto.getPlatformOrderId())) {
            errorMsgList.add("订单号不能为空");
        }
        if (StringUtils.isNotBlank(dto.getRefundId()) && dto.getRefundId().length() > 50) {
            errorMsgList.add("退款单号不能超过50个字节");
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
        if (StringUtils.isBlank(dto.getSkuNo())) {
            errorMsgList.add("SKU不能为空");
        }
        if (StringUtils.isNotBlank(dto.getShopName())) {
            Integer count = dmpShopInfoService.getDmpShopInfoByParam(dto.getPlatformName(),null, dto.getShopName());
            if (count == 0) {
                errorMsgList.add("店铺在系统中未找到");
            }
        }

       if (!StrUtils.isDigit(dto.getRefundNum())) {
            errorMsgList.add("退款数量只能包含数字");
        }
        if (!StrUtils.isDigit(dto.getRefundAmount())) {
            errorMsgList.add("退款金额只能包含数字");
        }
        if (!StrUtils.isDigit(dto.getCnySettleRate())) {
            errorMsgList.add("结算汇率只能包含数字");
        }
        //查询退款
        DmpRefundInfoEntity dmpRefundInfoEntity = dmpRefundInfoService.getByRefundId(dto.getRefundId());
        if(ObjectUtils.isNotEmpty(dmpRefundInfoEntity)) {
            errorMsgList.add("退款单号已存在，不能重复添加");
        }
        //查询订单
        DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getByPlatformOrderId(dto.getPlatformOrderId());
        if(ObjectUtils.isEmpty(dmpOrderInfoEntity)) {
            errorMsgList.add("订单号系统中不存在");
        }

        Integer saleState = null;
        if (StringUtils.isNotBlank(dto.getRefundStatus())) {
            saleState = RefundStatusEnum.getCodeByName(dto.getRefundStatus());
            if (saleState == null || saleState == 0) {
                errorMsgList.add("退款单状态不正确：退款单状态：新建退款，审核中，财务审核，成功，失败，作废");
            }
        }

        if (StringUtils.isNotBlank(dto.getOrderTime())) {
            if (!DateUtil.isValid(dto.getOrderTime(),DateTimeFormatter.ISO_LOCAL_DATE)) {
                errorMsgList.add("原订单时间格式不正确");
            }
        }

        if (StringUtils.isNotBlank(dto.getRefundTime())) {
            if (!DateUtil.isValid(dto.getRefundTime(),DateTimeFormatter.ISO_LOCAL_DATE)) {
                errorMsgList.add("退货时间格式不正确");
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
        BeanMapperUtils.copy(dto,entity);
        entity.setOrderTime(EnumTimePattern.parseDate(dto.getOrderTime()));
        entity.setRefundTime(EnumTimePattern.parseDate(dto.getRefundTime()));
        entity.setRefundStatus(RefundStatusEnum.getCodeByName(dto.getRefundStatus()));
        entity.setRefundAmount(new BigDecimal(dto.getRefundAmount()));
        entity.setCurrencyRate(new BigDecimal(dto.getCurrencyRate()));
        entity.setCnySettleRate(new BigDecimal(dto.getCnySettleRate()));
        boolean flag = dmpRefundInfoService.save(entity);
        //新增sku明细
        if (flag) {
            itemEntity.setRefundId(entity.getId());
            itemEntity.setRefundNum(Integer.valueOf(dto.getRefundNum()));
            itemEntity.setSkuNo(dto.getSkuNo());
            dmpRefundItemService.save(itemEntity);
        }
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
