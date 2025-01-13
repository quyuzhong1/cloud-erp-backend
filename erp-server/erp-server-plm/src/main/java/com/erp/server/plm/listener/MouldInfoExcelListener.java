package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.MouldDetailDTO;
import com.erp.model.plm.dto.MouldInfoImportDTO;
import com.erp.model.plm.dto.MouldProductDTO;
import com.erp.model.plm.enums.RefundStandardEnum;
import lombok.Getter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class MouldInfoExcelListener extends AnalysisEventListener<MouldInfoImportDTO.MouldInfoExcelDTO> {

    private final Map<String, String> typeNameMap;
    private final Map<String, String> dictBasicNameMap;
    private final Map<String, String> paymentConditionNameMap;
    private final Map<String, String> supplierMap;

    /**
     * 导入正确数据
     */
    private List<MouldDetailDTO.ViewDTO> successList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private final List<MouldInfoImportDTO.MouldInfoExcelDTO> errorList = new ArrayList<>();

    public MouldInfoExcelListener(Map<String, String> typeNameMap, Map<String, String> dictBasicNameMap, Map<String, String> paymentConditionNameMap, Map<String, String> supplierMap) {
        this.typeNameMap = typeNameMap;
        this.dictBasicNameMap = dictBasicNameMap;
        this.paymentConditionNameMap = paymentConditionNameMap;
        this.supplierMap = supplierMap;
    }

    @Override
    public void invoke(MouldInfoImportDTO.MouldInfoExcelDTO data, AnalysisContext context) {
        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (!CollectionUtils.isEmpty(msgList)) {
            msgList.add(String.join(",", msgList));
        }
        if (!typeNameMap.containsKey(data.getTypeName())) {
            msgList.add("模具类型不存在");
        }
        if (!dictBasicNameMap.containsKey(data.getPayMethodName())) {
            msgList.add("结算方式不存在");
        }
        if (!paymentConditionNameMap.containsKey(data.getPaymentConditionName())) {
            msgList.add("付款条件不存在");
        }
        if (!supplierMap.containsKey(data.getSupplierName())) {
            msgList.add("供应商不存在");
        }
        MouldDetailDTO.ViewDTO viewDTO1 = successList.stream()
                .filter(v -> v.getMouldNo().equals(data.getNum()))
                .findFirst()
                .orElse(null);
        MouldDetailDTO.ViewDTO dto = new MouldDetailDTO.ViewDTO();
        dto.setMouldNo(data.getNum());
        dto.setPayMethodId(dictBasicNameMap.get(data.getPayMethodName()));
        dto.setPaymentCondition(paymentConditionNameMap.get(data.getPaymentConditionName()));
        dto.setThirdMouldNo(data.getThirdMouldNo());
        dto.setSupplierId(supplierMap.get(data.getSupplierName()));
        if (!ObjectUtils.isEmpty(data.getLifeCycle())) {
            dto.setLifeCycle(Integer.parseInt(data.getLifeCycle()));
        }
        if (!ObjectUtils.isEmpty(data.getDevelopCycle())) {
        dto.setDevelopCycle(Integer.parseInt(data.getDevelopCycle()));
        }
        if (!ObjectUtils.isEmpty(data.getEnableDate())) {
            dto.setEnableDate(LocalDateUtil.parseStrToLocalDate(data.getEnableDate()));
        }
        if (!ObjectUtils.isEmpty(data.getQty())) {
            dto.setQty(Integer.parseInt(data.getQty()));
        }
        if (!ObjectUtils.isEmpty(data.getTaxPrice())) {
            dto.setTaxPrice(new BigDecimal(data.getTaxPrice()));
        }
        if (!ObjectUtils.isEmpty(data.getTaxRate())) {
            dto.setTaxRate(new BigDecimal(data.getTaxRate()));
        }
        dto.setIsNeedRefund("是".equals(data.getIsNeedRefundName()));
        if (!ObjectUtils.isEmpty(data.getRefundStandardName())) {
            dto.setRefundStandard(RefundStandardEnum.getCodeByName(data.getRefundStandardName()));
        }
        if (!ObjectUtils.isEmpty(data.getRefundOrderQty())) {
            dto.setRefundOrderQty(Integer.parseInt(data.getRefundOrderQty()));
        }
        if (!ObjectUtils.isEmpty(data.getRefundAmount())) {
            dto.setRefundAmount(new BigDecimal(data.getRefundAmount()));
        }
        MouldProductDTO.ViewDTO viewDTO = new MouldProductDTO.ViewDTO();
        viewDTO.setTypeId(typeNameMap.get(data.getTypeName()));
        viewDTO.setMouldHoles(data.getMouldHoles());
        if (!ObjectUtils.isEmpty(data.getLength())) {
            viewDTO.setLength(new BigDecimal(data.getLength()));
        }
        if (!ObjectUtils.isEmpty(data.getWidth())) {
            viewDTO.setWidth(new BigDecimal(data.getWidth()));
        }
        if (!ObjectUtils.isEmpty(data.getHeight())) {
            viewDTO.setHeight(new BigDecimal(data.getHeight()));
        }
        viewDTO.setMaterial(data.getMaterial());
        viewDTO.setProductName(data.getProductName());
        dto.setProductList(Collections.singletonList(viewDTO));
        if (!ObjectUtils.isEmpty(viewDTO1) && !checkFieldEquals(viewDTO1, dto)) {
            msgList.add("若为同一模具下的不同产品，序号+其他字段均一致");
        }
        //存在错误数据则直接返回
        if (!CollectionUtils.isEmpty(msgList)) {
            data.setErrorMsg(String.join(",", msgList));
            errorList.add(data);
            return;
        }
        successList.add(dto);
    }

    /**
     * 校验字段参数是否一致
     */
    private boolean checkFieldEquals(MouldDetailDTO.ViewDTO viewDTO1, MouldDetailDTO.ViewDTO dto) {

        return Objects.equals(viewDTO1.getPayMethodId(), dto.getPayMethodId()) &&
                Objects.equals(viewDTO1.getPaymentCondition(), dto.getPaymentCondition()) &&
                Objects.equals(viewDTO1.getThirdMouldNo(), dto.getThirdMouldNo()) &&
                Objects.equals(viewDTO1.getLifeCycle(), dto.getLifeCycle()) &&
                Objects.equals(viewDTO1.getDevelopCycle(), dto.getDevelopCycle()) &&
                Objects.equals(viewDTO1.getEnableDate(), dto.getEnableDate()) &&
                Objects.equals(viewDTO1.getQty(), dto.getQty()) &&
                Objects.equals(viewDTO1.getTaxPrice(), dto.getTaxPrice()) &&
                Objects.equals(viewDTO1.getTaxRate(), dto.getTaxRate()) &&
                Objects.equals(viewDTO1.getIsNeedRefund(), dto.getIsNeedRefund()) &&
                Objects.equals(viewDTO1.getRefundStandard(), dto.getRefundStandard()) &&
                Objects.equals(viewDTO1.getRefundOrderQty(), dto.getRefundOrderQty()) &&
                Objects.equals(viewDTO1.getRefundAmount(), dto.getRefundAmount());
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        successList = successList.stream()
                .collect(Collectors.groupingBy(MouldDetailDTO.ViewDTO::getMouldNo)) // 按照 mouldNo 分组
                .entrySet().stream()
                .map(entry -> {
                    // 将分组后的产品列表合并
                    List<MouldProductDTO.ViewDTO> dtos = entry.getValue().stream()
                            .map(MouldDetailDTO.ViewDTO::getProductList)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    // 获取分组的第一个 DTO，如果不存在则创建一个新的
                    MouldDetailDTO.ViewDTO dto = entry.getValue().stream()
                            .findFirst()
                            .orElse(new MouldDetailDTO.ViewDTO());
                    // 设置合并后的产品列表
                    dto.setProductList(dtos);
                    return dto;
                })
                .collect(Collectors.toList()); // 将结果收集为列表
    }

}
