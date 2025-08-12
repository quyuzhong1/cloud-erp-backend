package com.erp.server.plm.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.dto.excel.SkuStdCostChangeExcelDTO;
import com.erp.server.plm.service.SkuStdCostDetailService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * SKU标准成本变更监听
 *
 * @author Jim
 * {@code @date:} 2024/08/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SkuStdCostChangeExcelListener extends AnalysisEventListener<SkuStdCostChangeExcelDTO> {

    /**
     * 错误信息
     */
    private List<SkuStdCostChangeExcelDTO> errorList = new ArrayList<>();

    /**
     * 可处理数据
     */
    private List<SkuStdCostChangeExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<SkuStdCostChangeExcelDTO> successList = new ArrayList<>();

    /**
     * 已导入的sku列表
     */
    private List<String> existImportSkuNoList = new LinkedList<>();

    private final SkuStdCostDetailService skuStdCostDetailService = SpringUtil.getBean(SkuStdCostDetailService.class);

    //sku信息
    private Map<String, String> skuMap;


    public SkuStdCostChangeExcelListener(Map<String, String> skuMap) {
        this.skuMap = skuMap;
    }

    /**
     * 每解析一行数据回调一遍
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SkuStdCostChangeExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CharSequenceUtil.isBlank(excelDTO.getSkuNo())) {
            errorMsgList.add("【SKU】不能为空");
        }
        if (CharSequenceUtil.isBlank(excelDTO.getStdCostPrice())) {
            errorMsgList.add("【标准成本(不含税)】不能都为空");
        }
        if (CharSequenceUtil.isBlank(excelDTO.getEffectiveDate())) {
            errorMsgList.add("【生效日期】不能都为空");
        }

        String skuNo = excelDTO.getSkuNo();
        if (StringUtils.isNotBlank(skuNo)) {
            String skuId = skuMap.getOrDefault(skuNo, "");
            if (org.apache.commons.lang3.StringUtils.isNotBlank(skuId)) {
                excelDTO.setSkuId(skuId);
            } else {
                errorMsgList.add("SKU不存在");
            }
        }
        //导入数据是否存在重复
        if (existImportSkuNoList.contains(excelDTO.getSkuNo())) {
            errorMsgList.add(CharSequenceUtil.format("导入SKU【{}】重复", excelDTO.getSkuNo()));
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        //添加数据用于判断是否为空
        dataList.add(excelDTO);
    }

    public List<SkuStdCostChangeExcelDTO> getExcelDateList() {
        return dataList;
    }

    /**
     * 据全部解析完后
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        //获取所有SKU编码
        List<String> skuIdList = dataList.stream().map(SkuStdCostChangeExcelDTO::getSkuId).distinct().collect(Collectors.toList());
        Map<String, SkuStdCostDetailDTO.ListDTO> lastListDTOMap = skuStdCostDetailService.mapLastBySkuIds(skuIdList);

        for (SkuStdCostChangeExcelDTO excelDTO : dataList) {
            SkuStdCostDetailDTO.ListDTO listDTO = lastListDTOMap.get(excelDTO.getSkuId());
            if (null == listDTO) {
                excelDTO.setErrorMsg("SKU不存在");
                errorList.add(excelDTO);
                continue;
            }

            // 处理重复sku导入问题
        }


//        Map<String, List<FirstMileReconciliationStandardExcelDTO>> dataMap = dataList.stream()
//                .collect(Collectors.groupingBy(FirstMileReconciliationStandardExcelDTO::getNo));
//
//        for (Map.Entry<String, List<FirstMileReconciliationStandardExcelDTO>> entry : dataMap.entrySet()) {
//            String key = entry.getKey();
//            List<FirstMileReconciliationStandardExcelDTO> value = entry.getValue();
//            for (FirstMileReconciliationStandardExcelDTO excelDTO : value) {
//                int notEmptyCount = 0;
//                if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())) {notEmptyCount++;}
//                if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())) {notEmptyCount++;}
//                if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())) {notEmptyCount++;}
//                //校验数据
//                List<LogisticsBillEntity> entityList = logisticsBillEntityList.stream().filter(v -> {
//                    //同时不为空时，匹配来源单号和业务单号
//                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
//                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
//                    }
//                    //运单号不为空时，匹配运单号
//                    if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
//                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode())) {
//                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
//                    }
//                    //同时不为空时，匹配来源单号和运单号
//                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(),excelDTO.getTransportNo())) {
//                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
//                    }
//                    //同时不为空时，匹配来源单号和业务单号
//                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
//                        if (v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
//                    }
//                    //来源单号不为空时，匹配来源单号
//                    if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode()) && v.getOutstockCode().equals(excelDTO.getSourceCode())) {return true;}
//                    //业务单号不为空时，匹配业务单号
//                    if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
//                    //运单号不为空时，匹配运单号
//                    if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
//                    return false;
//                }).collect(Collectors.toList());
//        if (CollUtil.isEmpty(entityList)) {
//            StringBuilder msg = new StringBuilder();
//            if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())) {
//                msg.append("来源单号【").append(excelDTO.getSourceCode()).append("】");
//            }
//            if (CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())) {
//                msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
//            }
//            if (CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())) {
//                msg.append("运单号【").append(excelDTO.getTransportNo()).append("】");
//            }
//            msg.append("未匹配到物流单");
//            excelDTO.setErrorMsg(msg.toString());
//            errorNoSet.add(excelDTO.getNo());
//            continue;
//        }
//                //校验两个参数及以上都存在时。是否存在关联的多条物流单
//                List<LogisticsBillEntity> logisticsBillEntityList1 = logisticsBillEntityList.stream().filter(v -> {
//                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
//                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
//                    }
//                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(), excelDTO.getBusinessCode())) {
//                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getBusinessCode().equals(excelDTO.getBusinessCode())) {return true;}
//                    }
//                    //同时不为空时，匹配来源单号和运单号
//                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getSourceCode(),excelDTO.getTransportNo())) {
//                        if (v.getOutstockCode().equals(excelDTO.getSourceCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
//                    }
//                    //同时不为空时，匹配来源单号和业务单号
//                    if (CharSequenceUtil.isAllNotBlank(excelDTO.getBusinessCode(),excelDTO.getTransportNo())) {
//                        if (v.getBusinessCode().equals(excelDTO.getBusinessCode()) && v.getTransportNo().equals(excelDTO.getTransportNo())) {return true;}
//                    }
//                    return false;
//                }).collect(Collectors.toList());
//                if (CollUtil.isEmpty(logisticsBillEntityList1) && notEmptyCount > 1) {
//                    StringBuilder msg = new StringBuilder();
//                    if(CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())){
//                        msg.append("来源单号【").append(excelDTO.getSourceCode()).append("】");
//                    }
//                    if(CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())){
//                        msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
//                    }
//                    if(CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())){
//                        msg.append("运单号【").append(excelDTO.getTransportNo()).append("】");
//                    }
//                    msg.append("匹配不到物流单");
//                    excelDTO.setErrorMsg(msg.toString());
//                    errorNoSet.add(excelDTO.getNo());
//                    continue;
//
//                }
//                if (entityList.size() > 1) {
//                    StringBuilder msg = new StringBuilder();
//                    if(CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())){
//                        msg.append("来源单号【").append(excelDTO.getSourceCode()).append("】");
//                    }
//                    if(CharSequenceUtil.isNotBlank(excelDTO.getBusinessCode())){
//                        msg.append("业务单号【").append(excelDTO.getBusinessCode()).append("】");
//                    }
//                    if(CharSequenceUtil.isNotBlank(excelDTO.getTransportNo())){
//                        msg.append("运单号【").append(excelDTO.getTransportNo()).append("】");
//                    }
//                    msg.append("存在多条物流单");
//                    excelDTO.setErrorMsg(msg.toString());
//                    errorNoSet.add(excelDTO.getNo());
//                    continue;
//                }
//                LogisticsBillEntity entity = entityList.get(0);
//                if(Objects.isNull(entity)){
//                    excelDTO.setErrorMsg("未找到物流单");
//                    errorNoSet.add(excelDTO.getNo());
//                    continue;
//                }
//                if(CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
//                    continue;
//                }
//                List<String> statusList = new ArrayList<>();
//                statusList.add(ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
//                statusList.add(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
//                reconciliationDetailEntityList.stream().filter(v->v.getSourceId().equals(entity.getId()) && !statusList.contains(v.getStatus()) && v.getMainId().equals(mainEntity.getId())).findFirst().ifPresent(v->{
//                    excelDTO.setErrorMsg("实际账单状态{已确认/已对账/差异确认}，不能更新信息");
//                    errorNoSet.add(excelDTO.getNo());
//                });
//                if(CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())){
//                    continue;
//                }
//                excelDTO.setLogisticsBillId(entity.getId());
//                LogisticsBillCostEntity costEntity = logisticsBillCostEntitieList.stream().filter(v->v.getLogisticsBillId().equals(entity.getId())).findFirst().orElse(null);
//                if(Objects.isNull(costEntity)){
//                    excelDTO.setErrorMsg("未找到物流费用");
//                    errorNoSet.add(excelDTO.getNo());
//                    continue;
//                }
//                excelDTO.setCostId(costEntity.getId());
//                if (CharSequenceUtil.isBlank(excelDTO.getBusinessCode())){
//                    excelDTO.setBusinessCode(entity.getBusinessCode());
//                }
//                if (CharSequenceUtil.isBlank(excelDTO.getSourceCode())){
//                    excelDTO.setSourceCode(entity.getOutstockCode());
//                }
//                if (CharSequenceUtil.isBlank(excelDTO.getTransportNo())){
//                    excelDTO.setTransportNo(entity.getTransportNo());
//                }
//            }
//            if(errorNoSet.contains(key)){
//                for (FirstMileReconciliationStandardExcelDTO excelDTO : value) {
//                    if(StringUtils.isBlank(excelDTO.getErrorMsg())){
//                        excelDTO.setErrorMsg("相同序号的数据存在错误，请检查");
//                    }
//                }
//                errorList.addAll(value);
//            }else {
//                successList.addAll(value);
//            }
    }
}
