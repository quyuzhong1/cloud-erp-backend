package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.excel.SoDetailImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname SoDetailExcelListener
 * @Description TODO
 * @Date 2023-05-17 19:47
 * @Created by yl
 */
public class SoDetailExcelListener extends AnalysisEventListener<SoDetailImportExcelDTO> {


    /**
     * sku 信息
     */
    List<SkuVO> skuList;

    /**
     * 成功的数据
     */
    private List<SoDetailDTO.ExcelDTO> successList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private List<SoDetailImportExcelDTO> errorList = new ArrayList<>();


    /**
     * 带过来
     *
     * @param skuList
     */
    public SoDetailExcelListener(List<SkuVO> skuList) {
        this.skuList = skuList;
    }


    /**
     * 没解析一行执行一次
     *
     * @param soDetailImportExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-04-21 19:40
     */
    @Override
    public void invoke(SoDetailImportExcelDTO soDetailImportExcelDTO, AnalysisContext analysisContext) {
        List<String> msgList = FieldValidUtil.fieldValid(soDetailImportExcelDTO);
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        String skuNo = soDetailImportExcelDTO.getSkuNo();
        SkuVO sku = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(sku)) {
            errorMsgList.add("sku 不存在");
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            soDetailImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(soDetailImportExcelDTO);
            return;
        }
        SoDetailDTO.ExcelDTO addDTO = new SoDetailDTO.ExcelDTO();
        //是否赠品
        String isReissue = soDetailImportExcelDTO.getIsReissue();
        addDTO.setIsReissue(isReissue.equals("是"));
        //是否赠品
        String isGift = soDetailImportExcelDTO.getIsGift();
        addDTO.setIsClose(isGift.equals("是"));

        //是否关闭
        String isClose = soDetailImportExcelDTO.getIsClose();
        addDTO.setIsClose(StringUtils.isBlank(isClose) ? false : isClose.equals("是"));
        addDTO.setSkuNo(skuNo);
        addDTO.setSkuId(sku.getSkuId());
        addDTO.setProductName(sku.getSkuName());
        addDTO.setUnit(sku.getUnitName());
        //销售数量
        String qty = soDetailImportExcelDTO.getQty();
        addDTO.setQty(Integer.valueOf(qty));

        //税率
        String taxRateStr = soDetailImportExcelDTO.getTaxRate();
        BigDecimal taxRate = BigDecimal.ZERO;
        if (StringUtils.isNotBlank(taxRateStr)) {
            taxRate = new BigDecimal(taxRateStr);
        }
        addDTO.setTaxRate(taxRate);
        addDTO.setRemark(soDetailImportExcelDTO.getRemark());
        successList.add(addDTO);


    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


    public List<SoDetailImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<SoDetailDTO.ExcelDTO> getSuccessList() {
        return successList;
    }
}
