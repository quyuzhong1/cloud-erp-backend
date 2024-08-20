package com.erp.server.tms.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.dto.excel.InventorySkuCostDetailExcelDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 期初头程分摊
 */
public class InventorySkuCostDetailExcelListener extends AnalysisEventListener<InventorySkuCostDetailExcelDTO> {
    private final PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    /**
     * 错误信息
     */
    @Getter
    private List<InventorySkuCostDetailExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<InventorySkuCostDetailExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<InventorySkuCostDetailDTO.AddDTO> successList = new ArrayList<>();

    public InventorySkuCostDetailExcelListener() {

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
    public void invoke(InventorySkuCostDetailExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        InventorySkuCostDetailDTO.AddDTO addDTO = new InventorySkuCostDetailDTO.AddDTO();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //数据copy
        BeanMapperUtils.copy(excelDTO, addDTO);
        if (StrUtil.isNotBlank(addDTO.getSkuNo())){
            List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.listBySkuNos(Collections.singletonList(addDTO.getSkuNo()));
            if (CollectionUtils.isEmpty(productDetailEntityList)){
                errorMsgList.add(StrUtil.format("SKU【{}】不存在",addDTO.getSkuNo()));
            }else {
                addDTO.setSkuId(productDetailEntityList.get(0).getId());
            }
        }
        if (StrUtil.isBlank(addDTO.getUnit())){
            addDTO.setUnit("pcs");
        }
        if (StrUtil.isBlank(addDTO.getProductName())){
            if (StrUtil.isNotBlank(addDTO.getSkuId())){
                List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(Collections.singletonList(addDTO.getSkuId()));
                if (CollectionUtils.isEmpty(skuVOList)){
                    errorMsgList.add(StrUtil.format("SKU【{}】产品名称不存在",addDTO.getSkuNo()));
                }else {
                    addDTO.setProductName(skuVOList.get(0).getSkuName());
                }
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        successList.add(addDTO);
    }

    public List<InventorySkuCostDetailExcelDTO> getExcelDateList(){
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
