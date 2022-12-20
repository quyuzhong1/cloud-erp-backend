package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.bi.dto.DmpReturnOrderInfoImportExcelDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.server.bi.enums.ReturnOrderStatusEnum;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderItemService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DmpReturnOrderInfoExcelListener extends AnalysisEventListener<DmpReturnOrderInfoImportExcelDTO> {
    private Integer importType;


    private DmpOrderInfoService dmpOrderInfoService;

    private DmpShopInfoService dmpShopInfoService;

    private DmpReturnOrderInfoService dmpReturnOrderInfoService;

    private DmpReturnOrderItemService dmpReturnOrderItemService;


    private List<DmpReturnOrderInfoImportExcelDTO> list;

    public DmpReturnOrderInfoExcelListener(Integer importType, DmpOrderInfoService dmpOrderInfoService, DmpReturnOrderInfoService dmpReturnOrderInfoService
            , DmpShopInfoService dmpShopInfoService, DmpReturnOrderItemService dmpReturnOrderItemService) {
        this.importType = importType;
        this.dmpOrderInfoService = dmpOrderInfoService;
        this.dmpShopInfoService = dmpShopInfoService;
        this.dmpReturnOrderInfoService = dmpReturnOrderInfoService;
        this.dmpReturnOrderItemService = dmpReturnOrderItemService;
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
    public void invoke(DmpReturnOrderInfoImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        DmpReturnOrderInfoEntity entity = new DmpReturnOrderInfoEntity();
        DmpReturnOrderItemEntity itemEntity = new DmpReturnOrderItemEntity();
        if (StringUtils.isBlank(dto.getReturnOrderId())) {
            errorMsgList.add("退货单号不能为空");
        }
        if (StringUtils.isBlank(dto.getPlatformOrderId())) {
            errorMsgList.add("订单号不能为空");
        }
        if (StringUtils.isNotBlank(dto.getReturnOrderId()) && dto.getReturnOrderId().length() > 50) {
            errorMsgList.add("退货单号不能超过50个字节");
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
        //查询退货
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = dmpReturnOrderInfoService.getByReturnOrderId(dto.getReturnOrderId());
        if(ObjectUtils.isNotEmpty(dmpReturnOrderInfoEntity)) {
            errorMsgList.add("退货单号已存在，不能重复添加");
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
        BeanMapperUtils.copy(dto,entity);
        entity.setStatus(ReturnOrderStatusEnum.getCodeByName(dto.getStatusName()));
        boolean flag = dmpReturnOrderInfoService.save(entity);
        //新增sku明细
        if (flag) {
            itemEntity.setReturnOrderId(entity.getId());
            itemEntity.setQuantity(dto.getRefundNum());
            itemEntity.setSkuNo(dto.getSkuNo());
            dmpReturnOrderItemService.save(itemEntity);
        }
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
