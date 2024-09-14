package com.erp.server.tms.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.UnitEnum;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.InitFirstMileAllocationDetailDTO;
import com.erp.model.tms.dto.excel.InitFirstMileAllocationDetailExcelDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.convert.InitFirstMileAllocationConverter;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 期初头程分摊
 */
public class InitFirstMileAllocationDetailExcelListener extends AnalysisEventListener<InitFirstMileAllocationDetailExcelDTO> {
    //头程发货单记录
    private final WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign = SpringUtil.getBean(WmsFirstMileDeliveryFeign.class);
    /**
     * 明细中已存在的明细
     */
    private List<InitFirstMileAllocationDetailDTO.AddDTO> detailList;
    /**
     * 错误信息
     */
    @Getter
    private List<InitFirstMileAllocationDetailExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<InitFirstMileAllocationDetailExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<InitFirstMileAllocationDetailDTO.AddDTO> successList = new ArrayList<>();

    public InitFirstMileAllocationDetailExcelListener(List<InitFirstMileAllocationDetailDTO.AddDTO> detailList) {
        this.detailList = CollectionUtils.isNotEmpty(detailList) ? detailList : new ArrayList<>();
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author zdy
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(InitFirstMileAllocationDetailExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        InitFirstMileAllocationDetailDTO.AddDTO addDTO = new InitFirstMileAllocationDetailDTO.AddDTO();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //校验数据是否已存在
        if (CollectionUtils.isNotEmpty(detailList)){
            InitFirstMileAllocationDetailDTO.AddDTO addDTO1 = detailList.stream().filter(e -> (Objects.equals(e.getBusinessCode(), excelDTO.getBusinessCode()) || Objects.equals(e.getSourceCode(), excelDTO.getSourceCode()))
                    && Objects.equals(e.getSkuNo(), excelDTO.getSkuNo())).findFirst().orElse(null);
            if (Objects.nonNull(addDTO1)){
                errorMsgList.add(StrUtil.format("发货单【{}】业务单号【{}】SKU【{}】已存在", addDTO1.getSourceCode(),addDTO1.getBusinessCode(),addDTO1.getSkuNo()));
            }
        }
        //发货单和业务单好不能同时为空
        if (StrUtil.isBlank(excelDTO.getBusinessCode()) && StrUtil.isBlank(excelDTO.getSourceCode())){
            errorMsgList.add("发货单和业务单好不能同时为空");
        }
        if (CollectionUtils.isEmpty(errorMsgList)){
            addDTO = InitFirstMileAllocationConverter.INSTANCE.excelToAddDTO(excelDTO);
            //补充明细数据
            if (StrUtil.isNotBlank(excelDTO.getSourceCode())){
                List<FirstMileDeliveryDTO.ListFirstMileDTO> firstMileDTOS = wmsFirstMileDeliveryFeign.listDetailByCodes(Collections.singletonList(excelDTO.getSourceCode()));
                if (CollectionUtils.isEmpty(firstMileDTOS)){
                    errorMsgList.add(StrUtil.format("发货单【{}】未审核或不存在",excelDTO.getSourceCode()));
                }
                FirstMileDeliveryDTO.ListFirstMileDTO firstMileDTO = firstMileDTOS.stream().filter(e -> StrUtil.isNotBlank(e.getSkuNo()) && e.getSkuNo().equals(excelDTO.getSkuNo()) && StrUtil.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equals(excelDTO.getPlatformSkuNo())).findFirst().orElse(null);
                if (Objects.isNull(firstMileDTO)){
                    errorMsgList.add(StrUtil.format("发货单【{}】明细中SKU【{}】平台SKU【{}】不存在",excelDTO.getSourceCode(), excelDTO.getSkuNo(), excelDTO.getPlatformSkuNo()));
                }else {
                    initExcel(firstMileDTO,addDTO);
                }
            }else if (StrUtil.isNotBlank(excelDTO.getBusinessCode())){
                //根据业务单号进行查询发货单
                List<FirstMileDeliveryDTO.ListFirstMileDTO> firstMileDTOS = wmsFirstMileDeliveryFeign.listDetailBySourceCodes(Collections.singletonList(excelDTO.getBusinessCode()));
                if (CollectionUtils.isEmpty(firstMileDTOS)){
                    errorMsgList.add(StrUtil.format("业务单号【{}】关联的发货单未审核或不存在",excelDTO.getBusinessCode()));
                }
                FirstMileDeliveryDTO.ListFirstMileDTO firstMileDTO = firstMileDTOS.stream().filter(e -> StrUtil.isNotBlank(e.getSkuNo()) && e.getSkuNo().equals(excelDTO.getSkuNo()) && StrUtil.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equals(excelDTO.getPlatformSkuNo())).findFirst().orElse(null);
                if (Objects.isNull(firstMileDTO)){
                    errorMsgList.add(StrUtil.format("发货单【{}】明细中SKU【{}】平台SKU【{}】不存在",excelDTO.getSourceCode(), excelDTO.getSkuNo(), excelDTO.getPlatformSkuNo()));
                }else {
                    initExcel(firstMileDTO,addDTO);
                }
            }
        }
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(addDTO);
    }
    private void initExcel(FirstMileDeliveryDTO.ListFirstMileDTO firstMileDTO, InitFirstMileAllocationDetailDTO.AddDTO addDTO){
        addDTO.setSourceId(firstMileDTO.getSourceId());
        addDTO.setSourceCode(firstMileDTO.getSourceCode());
        addDTO.setSourceDetailId(firstMileDTO.getSourceDetailId());
        addDTO.setSourceType(firstMileDTO.getSourceType());
        addDTO.setBusinessCode(firstMileDTO.getBusinessCode());
        addDTO.setBusinessType(firstMileDTO.getBusinessType());
        addDTO.setShopId(firstMileDTO.getShopId());
        if (StrUtil.isBlank(addDTO.getShopName())){
            addDTO.setShopName(firstMileDTO.getShopName());
        }
        addDTO.setWarehouseId(firstMileDTO.getWarehouseId());
        if (StrUtil.isBlank(addDTO.getWarehouseName())){
            addDTO.setWarehouseName(firstMileDTO.getWarehouseName());
        }
        addDTO.setSkuId(firstMileDTO.getSkuId());
        if (StrUtil.isBlank(addDTO.getProductName())){
            addDTO.setProductName(firstMileDTO.getProductName());
        }
        if (StrUtil.isBlank(addDTO.getCurrency())){
            addDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            addDTO.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
            addDTO.setExchangeRate(BigDecimal.ONE);
        }
        if (StrUtil.isBlank(addDTO.getWeightUnit())){
            addDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        }
    }

    public List<InitFirstMileAllocationDetailExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author zdy
     * @date: 2024/8/14 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
