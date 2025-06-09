package com.erp.server.plm.listener;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.ProductCertificateExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProductCertificateExcelListener extends AnalysisEventListener<ProductCertificateExcelDTO> {

    /**
     * 错误信息
     */
    private List<ProductCertificateExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<ProductCertificateExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<ProductCertificateExcelDTO> successList = new ArrayList<>();

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(ProductCertificateExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空

        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        // 获取超链接地址
        successList.add(excelDTO);

    }

    public List<ProductCertificateExcelDTO> getErrorList(){
        return errorList;
    }

    public List<ProductCertificateExcelDTO> getSuccessList(){
        return successList;
    }

    public List<ProductCertificateExcelDTO> getExcelDateList(){
        return dataList;
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        log.info("读取到了一条额外信息:{}", JSONUtil.toJsonStr(extra));
        String extraText = extra.getText();
        Integer rowIndex = extra.getRowIndex();
        Integer columnIndex = extra.getColumnIndex();
        switch (extra.getType()) {
            case COMMENT:
                log.info("额外信息是批注,在rowIndex:{},columnIndex;{},内容是:{}", rowIndex, columnIndex, extraText);
                break;
            case HYPERLINK:
                log.info(
                        "额外信息是超链接,覆盖了一个区间,在firstRowIndex:{},firstColumnIndex;{},lastRowIndex:{},lastColumnIndex:{},内容是:{}",
                        extra.getFirstRowIndex(), extra.getFirstColumnIndex(), extra.getLastRowIndex(),extra.getLastColumnIndex(), extraText);
                break;
            case MERGE:
                log.info(
                        "额外信息是合并单元格,覆盖了一个区间,在firstRowIndex:{},firstColumnIndex;{},lastRowIndex:{},lastColumnIndex:{}",
                        extra.getFirstRowIndex(), extra.getFirstColumnIndex(), extra.getLastRowIndex(),extra.getLastColumnIndex());
                break;
            default:
        }
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        return;
    }


}
