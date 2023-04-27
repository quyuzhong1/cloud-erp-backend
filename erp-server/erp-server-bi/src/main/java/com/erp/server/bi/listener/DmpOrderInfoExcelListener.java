package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.enums.SalesPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.dto.DmpOrderInfoImportExcelDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.OrderStateEnum;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmpOrderInfoExcelListener extends AnalysisEventListener<DmpOrderInfoImportExcelDTO> {
    private Integer importType;

    private DmpShopInfoService dmpShopInfoService;

    private SysUserFeign sysUserFeign;

    private PlmTaskFeign plmTaskFeign;

    private List<DmpOrderInfoImportExcelDTO> errorList = new ArrayList<>();


    private List<DmpOrderInfoImportExcelDTO> successList = new ArrayList<>();

    private List<DmpOrderInfoImportExcelDTO> allList = new ArrayList<>();

    private List<SysDepartmentDTO> deptList;

    public DmpOrderInfoExcelListener(Integer importType, List<SysDepartmentDTO> deptList, PlmTaskFeign plmTaskFeign, DmpShopInfoService dmpShopInfoService, SysUserFeign sysUserFeign) {
        this.importType = importType;
        this.dmpShopInfoService = dmpShopInfoService;
        this.sysUserFeign = sysUserFeign;
        this.plmTaskFeign = plmTaskFeign;
        this.deptList = deptList;
    }

    /**
     * @description: 每解析一行数据回调一遍
     * @author Will
     * @date: 2022/12/16 10:26
     * @param dto
     * @param analysisContext

     */
    @Override
    public void invoke(DmpOrderInfoImportExcelDTO dto, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(dto);
        List<String> errorMsgList = new ArrayList<>();

        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if  (StringUtils.isNotBlank(dto.getPlatformOrderId())) {
            if (StringUtils.isNotBlank(dto.getPlatformOrderId()) && dto.getPlatformOrderId().length() > 50) {
                errorMsgList.add("订单号不能超过50个字节");
            }
            if (StringUtils.isNotBlank(dto.getPlatformOrderId()) && !StrUtils.isLetterDigitBar(dto.getPlatformOrderId())) {
                errorMsgList.add("订单号只能包含字母、数字、-");
            }
        }

        if (StringUtils.isNotBlank(dto.getSourcePlatform())) {
            SalesPlatformEnum platformEnum = SalesPlatformEnum.getByName(dto.getSourcePlatform());
            if (ObjectUtils.isEmpty(platformEnum)) {
                errorMsgList.add("系统中不存在此平台名称");
            }
        }
        if (StringUtils.isNotBlank(dto.getShopName())) {
            Integer count = dmpShopInfoService.getDmpShopInfoByParam(dto.getSourcePlatform(), dto.getSite(), dto.getShopName());
            if (count == 0) {
                errorMsgList.add("在平台站点中未找到该店铺");
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
        if(ObjectUtils.isEmpty(dto.getSellPriceOrigin())) {
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
            errorList.add(dto);
            return;
        }
        successList.add(dto);
    }

    public List<DmpOrderInfoImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<DmpOrderInfoImportExcelDTO> getSuccessList(){
        return successList;
    }

    public List<DmpOrderInfoImportExcelDTO> getAllList(){
        return allList;
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
