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

    /**
     * 导入正确数据
     */
    private List<MouldDetailDTO.ViewDTO> successList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private final List<MouldInfoImportDTO.MouldInfoExcelDTO> errorList = new ArrayList<>();

    public MouldInfoExcelListener(Map<String, String> typeNameMap, Map<String, String> dictBasicNameMap, Map<String, String> paymentConditionNameMap) {
        this.typeNameMap = typeNameMap;
        this.dictBasicNameMap = dictBasicNameMap;
        this.paymentConditionNameMap = paymentConditionNameMap;
    }

    @Override
    public void invoke(MouldInfoImportDTO.MouldInfoExcelDTO data, AnalysisContext context) {
        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (!CollectionUtils.isEmpty(msgList)) {
            data.setErrorMsg(String.join(",", msgList));
            errorList.add(data);
            return;
        }
        if (!typeNameMap.containsKey(data.getTypeName())) {
            data.setErrorMsg("模具类型不存在");
            errorList.add(data);
            return;
        }
        if (!dictBasicNameMap.containsKey(data.getPayMethodName())) {
            data.setErrorMsg("付款方式不存在");
            errorList.add(data);
            return;
        }
        if (!paymentConditionNameMap.containsKey(data.getPaymentConditionName())) {
            data.setErrorMsg("付款条件不存在");
            errorList.add(data);
            return;
        }
        MouldDetailDTO.ViewDTO viewDTO1 = successList.stream()
                .filter(v -> v.getMouldNo().equals(data.getNum()))
                .findFirst()
                .orElse(null);
        MouldDetailDTO.ViewDTO dto = new MouldDetailDTO.ViewDTO();
        dto.setMouldNo(data.getNum());
        dto.setTypeId(typeNameMap.get(data.getTypeName()));
        dto.setPayMethodId(dictBasicNameMap.get(data.getPayMethodName()));
        dto.setPaymentCondition(paymentConditionNameMap.get(data.getPaymentConditionName()));
        dto.setThirdMouldNo(data.getThirdMouldNo());
        dto.setMouldHoles(data.getMouldHoles());
        dto.setLength(new BigDecimal(data.getLength()).multiply(new BigDecimal(10)));
        dto.setWidth(new BigDecimal(data.getWidth()).multiply(new BigDecimal(10)));
        dto.setHeight(new BigDecimal(data.getHeight()).multiply(new BigDecimal(10)));
        dto.setMaterial(data.getMaterial());
        dto.setLifeCycle(Integer.parseInt(data.getLifeCycle()));
        dto.setDevelopCycle(Integer.parseInt(data.getDevelopCycle()));
        dto.setEnableDate(LocalDateUtil.parseStrToLocalDate(data.getEnableDate()));
        dto.setQty(Integer.parseInt(data.getQty()));
        dto.setTaxPrice(new BigDecimal(data.getTaxPrice()));
        dto.setTaxRate(new BigDecimal(data.getTaxRate()));
        dto.setIsNeedRefund("是".equals(data.getIsNeedRefundName()));
        dto.setRefundStandard(RefundStandardEnum.getCodeByName(data.getRefundStandardName()));
        dto.setRefundOrderQty(Integer.parseInt(data.getRefundOrderQty()));
        dto.setRefundAmount(new BigDecimal(data.getRefundAmount()));
        MouldProductDTO.ViewDTO viewDTO = new MouldProductDTO.ViewDTO();
        viewDTO.setProductName(data.getProductName());
        dto.setProductList(Collections.singletonList(viewDTO));
        if (!ObjectUtils.isEmpty(viewDTO1) && !checkFieldEquals(viewDTO1, dto)) {
            data.setErrorMsg("若为同一模具下的不同产品，序号+其他字段均一致");
            errorList.add(data);
            return;
        }
        //存在错误数据则直接返回
        if (!CollectionUtils.isEmpty(errorList)) {
            return;
        }
        successList.add(dto);
    }

    /**
     * 校验字段参数是否一致
     */
    private boolean checkFieldEquals(MouldDetailDTO.ViewDTO viewDTO1, MouldDetailDTO.ViewDTO dto) {

        return viewDTO1.getTypeId().equals(dto.getTypeId()) && viewDTO1.getPayMethodId().equals(dto.getPayMethodId()) &&
                viewDTO1.getPaymentCondition().equals(dto.getPaymentCondition()) && viewDTO1.getThirdMouldNo().equals(dto.getThirdMouldNo()) &&
                viewDTO1.getMouldHoles().equals(dto.getMouldHoles()) && viewDTO1.getLength().equals(dto.getLength()) &&
                viewDTO1.getWidth().equals(dto.getWidth()) && viewDTO1.getHeight().equals(dto.getHeight()) &&
                viewDTO1.getMaterial().equals(dto.getMaterial()) && viewDTO1.getLifeCycle().equals(dto.getLifeCycle()) &&
                viewDTO1.getDevelopCycle().equals(dto.getDevelopCycle()) && viewDTO1.getEnableDate().equals(dto.getEnableDate()) &&
                viewDTO1.getQty().equals(dto.getQty()) && viewDTO1.getTaxPrice().equals(dto.getTaxPrice()) &&
                viewDTO1.getTaxRate().equals(dto.getTaxRate()) && viewDTO1.getIsNeedRefund().equals(dto.getIsNeedRefund()) &&
                viewDTO1.getRefundStandard().equals(dto.getRefundStandard()) && viewDTO1.getRefundOrderQty().equals(dto.getRefundOrderQty()) &&
                viewDTO1.getRefundAmount().equals(dto.getRefundAmount());
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
