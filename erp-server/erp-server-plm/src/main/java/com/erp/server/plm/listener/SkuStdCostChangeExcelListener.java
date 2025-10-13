package com.erp.server.plm.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.dto.excel.SkuStdCostChangeExcelDTO;
import com.erp.model.plm.entity.SkuStdCostDetailEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.SkuStdCostDetailService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private final String taskId;

    private Integer count = 0;
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

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    private final SkuStdCostDetailService skuStdCostDetailService = SpringUtil.getBean(SkuStdCostDetailService.class);

    //sku信息
    private Map<String, String> skuMap;


    public SkuStdCostChangeExcelListener(String taskId, Map<String, String> skuMap) {
        this.taskId = taskId;
        this.skuMap = skuMap;
    }

    /**
     * 每解析一行数据回调一遍
     */
    @Override
    public void invoke(SkuStdCostChangeExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
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
            errorMsgList.add("【标准成本(不含税)】不能为空");
        }
        if (CharSequenceUtil.isBlank(excelDTO.getEffectiveDateStr())) {
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
        existImportSkuNoList.add(excelDTO.getSkuNo());
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
                excelDTO.setErrorMsg(CharSequenceUtil.format("SKU={}:不存在【已审核】,不支持变更", excelDTO.getSkuNo()));
                errorList.add(excelDTO);
                continue;
            }
            // 校验
            SkuStdCostDetailDTO.ChangeCommonDTO changeCommonDTO = new SkuStdCostDetailDTO.ChangeCommonDTO(
                    new BigDecimal(excelDTO.getStdCostPrice()),
                    excelDTO.getCurrency(),
                    LocalDateUtil.parseStrToLocalDate(excelDTO.getEffectiveDateStr()));
            try {
                skuStdCostDetailService.changeAdd(changeCommonDTO, listDTO, listDTO);
                successList.add(excelDTO);
            } catch (Exception e) {
                excelDTO.setErrorMsg(e.getMessage());
                errorList.add(excelDTO);
            }
        }

    }
}
