package com.cloud.erp.chrome.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname UploadDataListener
 * @Description TODO
 * @Date 2022-08-23 16:19
 * @Created by yl
 */

public class UploadDataListener<T> extends AnalysisEventListener<T> {

    /**
     * 解析的数据
     */
    List<T> list = new ArrayList<>();

    /**
     * 正文起始行
     */
    private Integer headRowNumber;

    /**
     * 合并单元格
     */
    private List<CellExtra> extraMergeInfoList = new ArrayList<>();


    public UploadDataListener(Integer headRowNumber) {
        this.headRowNumber = headRowNumber;
    }


    /**
     * 每一条数据都会执行
     * @author yl
     * @date 2022-08-23 16:22
     * @param data
     * @param context
     * @return void
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        list.add(data);

    }

    //所有数据解析完 会调用
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        switch (extra.getType()) {
            case COMMENT: {

                break;
            }
            case HYPERLINK: {
                if ("Sheet1!A1".equals(extra.getText())) {

                } else if ("Sheet2!A1".equals(extra.getText())) {


                } else {

                }
                break;
            }
            case MERGE: {
                if (extra.getRowIndex() >= headRowNumber) {
                    extraMergeInfoList.add(extra);
                }
                break;
            }
            default: {
            }
        }
    }


    public List<CellExtra> getExtraMergeInfoList() {
        return extraMergeInfoList;
    }

    public List<T> getData() {

        return list;
    }
}
