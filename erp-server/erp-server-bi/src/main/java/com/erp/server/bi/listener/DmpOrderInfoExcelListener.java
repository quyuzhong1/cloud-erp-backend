package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.model.dmp.dto.DmpOrderInfoImportExcelDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.enums.SalesPlatformEnum;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.OrderStateEnum;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpOrderItemService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmpOrderInfoExcelListener extends AnalysisEventListener<DmpOrderInfoImportExcelDTO> {
    private Integer importType;


    private DmpOrderInfoService dmpOrderInfoService;

    private DmpOrderItemService dmpOrderItemService;


    private DmpShopInfoService dmpShopInfoService;

    private SysUserFeign sysUserFeign;

    private PlmTaskFeign plmTaskFeign;

    private List<DmpOrderInfoImportExcelDTO> list;

    private List<DmpOrderInfoEntity> orderList ;

    private List<SysDepartmentDTO> deptList;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public DmpOrderInfoExcelListener(Integer importType, List<DmpOrderInfoEntity> orderList, List<SysDepartmentDTO> deptList, PlmTaskFeign plmTaskFeign, DmpOrderItemService dmpOrderItemService, DmpOrderInfoService dmpOrderInfoService, DmpShopInfoService dmpShopInfoService, SysUserFeign sysUserFeign) {
        this.importType = importType;
        this.dmpOrderInfoService = dmpOrderInfoService;
        this.dmpOrderItemService = dmpOrderItemService;
        this.dmpShopInfoService = dmpShopInfoService;
        this.sysUserFeign = sysUserFeign;
        this.plmTaskFeign = plmTaskFeign;
        this.orderList = orderList;
        this.deptList = deptList;
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
    public void invoke(DmpOrderInfoImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        DmpOrderInfoEntity entity = new DmpOrderInfoEntity();
        if (StringUtils.isBlank(dto.getPlatformOrderId())) {
            errorMsgList.add("订单号不能为空");
        }
        if (StringUtils.isNotBlank(dto.getPlatformOrderId()) && dto.getPlatformOrderId().length() > 50) {
            errorMsgList.add("订单号不能超过50个字节");
        }
        if (!StrUtils.isLetterDigitBar(dto.getPlatformOrderId())) {
            errorMsgList.add("订单号只能包含字母、数字、-");
        }

        if (StringUtils.isBlank(dto.getSourcePlatform())) {
            errorMsgList.add("平台名称不能为空");
        } else {
            SalesPlatformEnum platformEnum = SalesPlatformEnum.getByName(dto.getSourcePlatform());
            if (ObjectUtils.isEmpty(platformEnum)) {
                errorMsgList.add("系统中不存在此平台名称");
            }
        }
        if (StringUtils.isBlank(dto.getSite())) {
            errorMsgList.add("站点不能为空");
        }
        if (StringUtils.isBlank(dto.getShopName())) {
            errorMsgList.add("店铺名称不能为空");
        }
        if (StringUtils.isNotBlank(dto.getShopName())) {
            Integer count = dmpShopInfoService.getDmpShopInfoByParam(dto.getSourcePlatform(), dto.getSite(), dto.getShopName());
            if (count == 0) {
                errorMsgList.add("在平台站点中未找到该店铺");
            }
        }
        if (StringUtils.isBlank(dto.getBuyerName())) {
            errorMsgList.add("下单人不能为空");
        }
        if (StringUtils.isBlank(dto.getOrderStateName())) {
            errorMsgList.add("订单状态不能为空");
        }

        if (CollectionUtils.isNotEmpty(orderList)) {
            long count = orderList.stream().filter(obj -> obj.getPlatformOrderId().equals(dto.getPlatformOrderId())).count();
            if (count > 0) {
                errorMsgList.add("订单号已存在，不能重复添加");
            }
        }

        if (StringUtils.isNotBlank(dto.getManPhone())) {
            if (!ValidatorUtil.isMobile(dto.getManPhone())) {
                errorMsgList.add("下单电话1不正确");
            }
        }
        if (StringUtils.isNotBlank(dto.getSecondPhone())) {
            if (!ValidatorUtil.isMobile(dto.getSecondPhone())) {
                errorMsgList.add("下单电话2不正确");
            }
        }
        if(StringUtils.isBlank(dto.getCountryNameCn())) {
            errorMsgList.add("国家名称不能为空");
        }
        if(ObjectUtils.isNull(dto.getPlatformCreateTime())) {
            errorMsgList.add("订单下单时间不能为空");
        }

        if(StringUtils.isBlank(dto.getChargeName())) {
            errorMsgList.add("销售员不能为空");
        }

        if(StringUtils.isBlank(dto.getDeptName())) {
            errorMsgList.add("销售员不能为空");
        }

        if (StringUtils.isNotBlank(dto.getChargeName())) {
            if (CollectionUtils.isEmpty(deptList)) {
                errorMsgList.add("销售事业部在系统中未找到");
            } else {
                long count = deptList.stream().filter(obj -> dto.getDeptName().equals(obj.getName())).count();
                if (count < 1) {
                    errorMsgList.add("销售事业部在系统中未找到");
                }
            }
        }

        List<FindUserDTO> chargeNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getChargeName())) {
            BaseSearchDTO baseSearchDTO = new BaseSearchDTO();
            baseSearchDTO.setSearchKeyword(dto.getChargeName());
            ApiResult<List<FindUserDTO>> listApiResult = sysUserFeign.userList(baseSearchDTO);
            chargeNameList = listApiResult.getData();
            if (CollectionUtils.isEmpty(chargeNameList)) {
                errorMsgList.add("销售员在系统中未找到");
            }
        }

        Integer saleState = null;
        if (StringUtils.isNotBlank(dto.getOrderStateName())) {
            saleState = OrderStateEnum.getCodeByName(dto.getOrderStateName());
            if (saleState == null || saleState == 0) {
                errorMsgList.add("订单状态不正确：订单状态：配货中，已发货，已完成，已作废，退货，退款");
            }
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
        if(StringUtils.isBlank(dto.getItemName())) {
            errorMsgList.add("品名不能为空");
        }
        if(ObjectUtils.isEmpty(dto.getSellPrice())) {
            errorMsgList.add("单价不能为空");
        }
        if(ObjectUtils.isEmpty(dto.getQuantity())) {
            errorMsgList.add("数量不能为空");
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
        DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getByPlatformOrderId(dto.getPlatformOrderId());
        if (ObjectUtils.isEmpty(dmpOrderInfoEntity)) {
            BeanUtils.copyProperties(dto,entity);
            entity.setOrderStatus(OrderStateEnum.getCodeByName(dto.getOrderStateName()));
            entity.setChargeId(chargeNameList.get(0).getUserId());
            dmpOrderInfoService.save(entity);
        }
        //同订单sku新增到同一订单下
        DmpOrderItemEntity dmpOrderItemEntity = new DmpOrderItemEntity();
        if (ObjectUtils.isNotEmpty(dmpOrderInfoEntity)) {
            dmpOrderItemEntity.setOrderId(dmpOrderInfoEntity.getId());
        } else {
            dmpOrderItemEntity.setOrderId(entity.getId());
        }
        dmpOrderItemEntity.setSkuNo(dto.getSkuNo());
        dmpOrderItemEntity.setItemName(dto.getItemName());
        dmpOrderItemEntity.setSellPrice(dto.getSellPrice());
        dmpOrderItemEntity.setQuantity(dto.getQuantity());
        dmpOrderItemService.save(dmpOrderItemEntity);

    }

    public List<DmpOrderInfoImportExcelDTO> getDateList(){
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
