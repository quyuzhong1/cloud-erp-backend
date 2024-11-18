package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.excel.B2BSoImportExcelDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.AddressTypeEnum;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.server.oms.service.BankAccountService;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.SoInfoService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;


public class B2BSoExcelListener extends AnalysisEventListener<B2BSoImportExcelDTO> {


    Map<String, SoInfoDTO.AddDTO> map = new HashMap<>();

    /**
     * 错误的map
     */
    Map<String, String> errorMap = new HashMap<>();


    /**
     * 仓库
     */
    private List<WarehouseDTO.UpdateDTO> warehouseList;

    /**
     * 组织列表
     */
    private List<BaseIdDTO.CodeDTO> orgList;


    /**
     * 币别
     */
    private List<DictCurrencyEntity> currencyList;

    /**
     * 字段配置
     */
    private List<DictBasicEntity> dictBasicList;

    /**
     * 用户
     */
    private List<FindUserDTO> userList;


    /**
     * 金蝶
     */
    private KingdeeFeign kingdeeFeign;

    /**
     * 部门
     */
    private List<SysDepartmentDTO> deptList;


    /**
     * 收款账号服务
     */
    private BankAccountService bankAccountService;

    /**
     * 客户
     */
    private CustomerInfoService customerInfoService;

    /**
     * 客户收货地址
     */
    private CustomerAddressService customerAddressService;

    /**
     * sku
     */
    private List<SkuVO> skuList;

    private SoInfoService soInfoService;


    /**
     * 导入错误数据
     */
    @Getter
    private List<B2BSoImportExcelDTO> errorList = new ArrayList<>(10);


    private final String xsyCode = KingdeeBusinessOperatorTypeEnum.XSY.getCode();

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
    public void invoke(B2BSoImportExcelDTO excelDTO, AnalysisContext analysisContext) {

        Integer rowNumber = analysisContext.readSheetHolder().getApproximateTotalRowNumber();
        if (rowNumber > 5000) {
            throw new ServiceException("导入最高要支持5000条");
        }

        //序号
        String no = excelDTO.getNo();
        SoInfoDTO.AddDTO addDTO = map.get(no);
        /**
         *  当获取为空的时候
         *  分两种情况 是第一次
         */
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        Boolean isSoNull = Objects.isNull(addDTO);

        if (Boolean.TRUE.equals(isSoNull)) {
            List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
            if (CollectionUtils.isNotEmpty(msgList)) {
                errorMsgList.addAll(msgList);
            }
            if (returnErrorList(excelDTO, errorMsgList, no)) return;
            addDTO = new SoInfoDTO.AddDTO();
            //要货日期
            String requireDateStr = excelDTO.getRequireDate();
            LocalDate requireDate = LocalDateUtil.parseStrToLocalDate(requireDateStr);
            if (Objects.isNull(requireDate)) {
                errorMsgList.add("要货日期不能为空");
            }
            addDTO.setRequireDate(requireDate);
            //单据日期
            String billDateStr = excelDTO.getBillDate();
            LocalDate billDate = LocalDateUtil.parseStrToLocalDate(billDateStr);
            if (Objects.isNull(billDate)) {
                errorMsgList.add("单据日期不能为空");
            }
            addDTO.setBillDate(billDate);
            if (Objects.nonNull(requireDate) && Objects.nonNull(billDate) && requireDate.compareTo(billDate) < 0) {
                errorMsgList.add("要货日期必须大于单据日期");
            }
            //单据类型
            String orderTypeStr = excelDTO.getOrderTypeStr();
            String orderType = BillTypeEnum.getCodeByName(orderTypeStr);
            if (StringUtils.isBlank(orderType)) {
                errorMsgList.add("单据类型不存在");
            }
            addDTO.setOrderType(orderType);

            //销售组织
            String salesOrgName = excelDTO.getSalesOrgName();
            BaseIdDTO.CodeDTO salesOrg = orgList.stream().filter(o -> o.getName().equals(salesOrgName)).findFirst().
                    orElse(null);
            if (Objects.isNull(salesOrg)) {
                errorMsgList.add("销售组织不存在");
            }
            String salesOrgId = "";
            if (Objects.nonNull(salesOrg)) {
                salesOrgId = salesOrg.getId();

            }
            addDTO.setSalesOrgId(salesOrgId);
            //销售部门
            String salesDeptName = excelDTO.getSalesDeptName();
            String salesDeptId = deptList.stream().filter(d -> d.getName().equals(salesDeptName)).findFirst().
                    map(SysDepartmentDTO::getId).orElse("");
            addDTO.setSalesDeptId(salesDeptId);
            if (StringUtils.isBlank(salesDeptId)) {
                errorMsgList.add("销售部门不存在");
            }

            //销售员
            String sellerName = excelDTO.getSellerName();
            String sellerId = userList.stream().filter(u -> u.getUserName().equals(sellerName)).findFirst().
                    map(FindUserDTO::getUserId).orElse("");
            addDTO.setSellerId(sellerId);
            if (StringUtils.isBlank(sellerId)) {
                errorMsgList.add("销售员不存在");
            }
            //存在错误数据则直接返回
            if (returnErrorList(excelDTO, errorMsgList, no)) return;

            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO businessOperatorDTO = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            businessOperatorDTO.setOrgId(salesOrgId);
            businessOperatorDTO.setBusinessOperatorType(xsyCode);
            businessOperatorDTO.setUserId(sellerId);
            KingdeeOperatorRefPostDTO.OperatorDTO businessOperator = kingdeeFeign.getBusinessOperator(businessOperatorDTO);
            if (Objects.isNull(businessOperator)) {
                errorMsgList.add("金蝶未存在该销售员");
            }
            //是否收取运费
            String isCollectShippingFeeStr = excelDTO.getIsCollectShippingFee();
            Boolean isCollectShippingFee = Boolean.FALSE;
            if (StringUtils.isNotBlank(isCollectShippingFeeStr)) {
                isCollectShippingFee = isCollectShippingFeeStr.equals("是");
            }
            addDTO.setIsCollectShippingFee(isCollectShippingFee);

            String warehouseName = excelDTO.getWarehouseName();

            String warehouseId = warehouseList.stream().filter(w -> w.getName().equals(warehouseName)).findFirst().
                    map(WarehouseDTO.UpdateDTO::getId).orElse("");
            if (StringUtils.isBlank(warehouseId)) {
                errorMsgList.add("仓库不存在");
            }
            addDTO.setWarehouseId(warehouseId);

            //收款账号
            String receiveAccountStr = excelDTO.getReceiveAccount();
            String receiveAccount = "";
            BankAccountEntity bankAccount = bankAccountService.findByOrgIdAndAccountName(salesOrgId, receiveAccountStr);
            if (Objects.isNull(bankAccount)) {
                errorMsgList.add("收款账号不存在");
            } else {
                receiveAccount = bankAccount.getBankAccountNo();
            }
            addDTO.setReceiveAccount(receiveAccount);
            //收款方式
            String receiveMethodStr = excelDTO.getReceiveMethod();
            String receiveMethod = dictBasicList.stream().filter(d -> d.getName().equals(receiveMethodStr)).findFirst().
                    map(DictBasicEntity::getValue).orElse("");
            if (StringUtils.isBlank(receiveMethod)) {
                errorMsgList.add("收款方式不存在");
            }
            addDTO.setReceiveMethod(receiveMethod);
            //收款日期
            String receiveDateStr = excelDTO.getReceiveDate();
            if (StringUtils.isNotBlank(receiveDateStr)) {
                addDTO.setReceiveDate(LocalDateUtil.parseStrToLocalDate(receiveDateStr));
            }

            //贸易条款
            String tradeTermStr = excelDTO.getTradeTerm();
            String tradeTerm = "";
            if (StringUtils.isNotBlank(tradeTermStr)) {
                tradeTerm = dictBasicList.stream().filter(d -> d.getName().equals(tradeTermStr)).findFirst().
                        map(DictBasicEntity::getValue).orElse("");
                if (StringUtils.isBlank(tradeTerm)) {
                    errorMsgList.add("贸易条款不存在");
                }
            }
            addDTO.setTradeTerm(tradeTerm);
            //客户
            String customerName = excelDTO.getCustomerName();
            CustomerInfoEntity customerInfo = customerInfoService.getByName(customerName);
            String customerId = "";
            if (Objects.isNull(customerInfo)) {
                errorMsgList.add("客户不存在");
            } else {
                customerId = customerInfo.getId();
            }
            addDTO.setCustomerId(customerId);

            //收货人
            String receiverName = excelDTO.getReceiverName();
            addDTO.setReceiverName(receiverName);

            //联系电话
            String telNumber = excelDTO.getTelNumber();
            addDTO.setTelNumber(telNumber);

            //收货地址
            String receiveAddress = excelDTO.getReceiveAddress();
            List<CustomerAddressDTO.ViewDTO> customerAddressList = customerAddressService.listByMainId(customerId);
            String customerAddressId = customerAddressList.stream().filter(c -> c.getAddress().equals(receiveAddress)).
                    findFirst().map(CustomerAddressDTO.ViewDTO::getId).orElse("");
            if (StringUtils.isBlank(customerAddressId)) {
                errorMsgList.add("联系地址不存在");
            }
            addDTO.setReceiveAddressId(customerAddressId);

            //交货方式
            String deliveryModeStr = excelDTO.getDeliveryMode();
            String deliveryMode = dictBasicList.stream().filter(d -> d.getName().equals(deliveryModeStr)).findFirst().
                    map(DictBasicEntity::getValue).orElse("");
            if (StringUtils.isBlank(deliveryMode)) {
                errorMsgList.add("交货方式不存在");
            }
            addDTO.setDeliveryMode(deliveryMode);

            //地址类型
            String addressTypeStr = excelDTO.getAddressType();
            String addressType = AddressTypeEnum.getCodeByName(addressTypeStr);
            if (StringUtils.isBlank(addressType)) {
                errorMsgList.add("地址类型不存在");
            }
            addDTO.setAddressType(addressType);

            //币别
            String currencyStr = excelDTO.getCurrency();

            String currency = currencyList.stream().filter(c -> c.getName().equals(currencyStr)).
                    findFirst().map(DictCurrencyEntity::getId).orElse("");

            if (StringUtils.isBlank(currency)) {
                errorMsgList.add("币种不存在");
            }
            addDTO.setCurrency(currency);
            //是否含税
            String isTaxStr = excelDTO.getIsTax();
            Boolean isTax = "是".equals(isTaxStr);
            addDTO.setIsTax(isTax);

            String receiveConditionStr = excelDTO.getReceiveCondition();
            String receiveCondition = dictBasicList.stream().filter(d -> d.getName().equals(receiveConditionStr)).findFirst().
                    map(DictBasicEntity::getValue).orElse("");
            if (StringUtils.isBlank(receiveCondition)) {
                errorMsgList.add("收款条件不存在");
            }
            addDTO.setReceiveCondition(receiveCondition);
            String remark = excelDTO.getRemark();
            addDTO.setRemark(remark);


            //银行手续费
            String bankServiceFeeStr = excelDTO.getBankServiceFee();
            BigDecimal bankServiceFee = MathUtil.getBigDecimalByStr(bankServiceFeeStr);

            //运费
            String shippingFeeStr = excelDTO.getShippingFee();
            BigDecimal shippingFee = MathUtil.getBigDecimalByStr(shippingFeeStr);


            //收款金额
            String receiveAmountStr = excelDTO.getReceiveAmount();
            BigDecimal receiveAmount = MathUtil.getBigDecimalByStr(receiveAmountStr);

            //报关费
            String customsFeeStr = excelDTO.getCustomsFee();
            BigDecimal customsFee = MathUtil.getBigDecimalByStr(customsFeeStr);

            //折扣总额
            String discountAmountStr = excelDTO.getDiscountAmount();
            BigDecimal discountAmount = MathUtil.getBigDecimalByStr(discountAmountStr);

            addDTO.setBankServiceFee(bankServiceFee);
            addDTO.setShippingFee(shippingFee);
            addDTO.setReceiveAmount(receiveAmount);
            addDTO.setCustomsFee(customsFee);
            addDTO.setDiscountAmount(discountAmount);

        }
        List<SoDetailDTO.AddDTO> detailList = Objects.nonNull(addDTO.getDetailList()) ? addDTO.getDetailList() : Lists.newArrayList();
        SoDetailDTO.AddDTO addDetail = new SoDetailDTO.AddDTO();
        //是否赠品
        String isGiftStr = excelDTO.getIsGift();
        if (StringUtils.isBlank(isGiftStr)) {
            errorMsgList.add("是否赠品不能为空");
        }
        addDetail.setIsGift("是".equals(isGiftStr));

        //是否补发
        String isReissueStr = excelDTO.getIsReissue();
        if (StringUtils.isBlank(isReissueStr)) {
            errorMsgList.add("是否补发不能为空");
        }
        addDetail.setIsReissue("是".equals(isReissueStr));

        //是否关闭
        String isCloseStr = excelDTO.getIsClose();
        if (StringUtils.isBlank(isCloseStr)) {
            errorMsgList.add("是否关闭不能为空");
        }
        addDetail.setIsClose("是".equals(isCloseStr));
        addDetail.setRemark(excelDTO.getDetailRemark());

        //sku no
        String skuNo = excelDTO.getSkuNo();
        if (StringUtils.isBlank(skuNo)) {
            errorMsgList.add("sku不能为空");
        }
        String skuId = "";
        SkuVO skuVO = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(skuVO)) {
            errorMsgList.add("sku不存在");
        } else {
            skuId = skuVO.getSkuId();
        }
        addDetail.setSkuId(skuId);
        //数量
        String qtyStr = excelDTO.getQty();
        Integer qty = StringUtils.isNotBlank(qtyStr) ? Integer.valueOf(qtyStr) : 0;
        addDetail.setQty(qty);
        //销售单价
        String priceStr = excelDTO.getPrice();
        BigDecimal price = MathUtil.getBigDecimalByStr(priceStr);
        addDetail.setPrice(price);

        //税率
        String taxRateStr = excelDTO.getTaxRate();
        BigDecimal taxRate = MathUtil.getBigDecimalByStr(taxRateStr);
        addDetail.setTaxRate(taxRate);
        detailList.add(addDetail);

        addDTO.setDetailList(detailList);
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            errorMap.put(no, no);
            //删除存在的销售订单信息
            map.remove(no);
            return;
        }

        map.put(no, addDTO);

    }

    private boolean returnErrorList(B2BSoImportExcelDTO excelDTO, List<String> errorMsgList, String no) {
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorMap.put(no, no);
            errorList.add(excelDTO);
            return true;
        }
        return false;
    }

    /**
     * 所有执行完成后执行
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!map.isEmpty()) {
            for (Map.Entry<String, SoInfoDTO.AddDTO> item : map.entrySet()) {
                String no = item.getKey();
                if (!errorMap.containsKey(no)) {
                    SoInfoDTO.AddDTO addDTO = item.getValue();
                    soInfoService.add(addDTO);
                }
            }
        }
        map.clear();
        errorMap.clear();
    }


}
