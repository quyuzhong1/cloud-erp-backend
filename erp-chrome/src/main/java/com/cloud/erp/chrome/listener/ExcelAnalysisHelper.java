package com.cloud.erp.chrome.listener;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.metadata.CellExtra;

import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @Classname ExcelAnalysisHelper
 * @Description TODO
 * @Date 2022-08-23 16:12
 * @Created by yl
 */
@Slf4j
public class ExcelAnalysisHelper<T> {
    public List<T> getList(MultipartFile file, Class<T> clazz) {
        return getList(file, clazz, 0, 1);
    }

    public List<T> getList(MultipartFile file, Class<T> clazz, Integer sheetNo, Integer headRowNumber) {
        UploadDataListener<T> listener = new UploadDataListener<>(headRowNumber);
        try {
            EasyExcel.read(file.getInputStream(), clazz, listener).extraRead(CellExtraTypeEnum.MERGE).sheet(sheetNo).headRowNumber(headRowNumber).doRead();
        } catch (IOException e) {

        }
        List<CellExtra> extraMergeInfoList = listener.getExtraMergeInfoList();
        if (CollectionUtils.isEmpty(extraMergeInfoList)) {
            return listener.getData();
        }
        List<T> data = explainMergeData(listener.getData(), extraMergeInfoList, headRowNumber);
        return data;
    }


    /**
     * 处理合并单元格
     * @author yl
     * @date 2022-08-23 16:27
     * @param data
     * @param extraMergeInfoList
     * @param headRowNumber
     * @return java.util.List<T>
     */
    private List<T> explainMergeData(List<T> data, List<CellExtra> extraMergeInfoList, Integer headRowNumber) {
        //循环所有合并单元格信息
        extraMergeInfoList.forEach(cellExtra -> {
            int firstRowIndex = cellExtra.getFirstRowIndex() - headRowNumber;
            int lastRowIndex = cellExtra.getLastRowIndex() - headRowNumber;
            int firstColumnIndex = cellExtra.getFirstColumnIndex();
            int lastColumnIndex = cellExtra.getLastColumnIndex();
            //获取初始值
            Object initValue = getInitValueFromList(firstRowIndex, firstColumnIndex, data);
            //设置值
            for (int i = firstRowIndex; i <= lastRowIndex; i++) {
                for (int j = firstColumnIndex; j <= lastColumnIndex; j++) {
                    setInitValueToList(initValue, i, j, data);
                }
            }
        });
        return data;

    }
     /**
     * 方法说明
     * @author yl
     * @date 2022-08-23 16:42
     * @param filedValue 值
      * @param rowIndex 行
      * @param  columnIndex 列
      * @param data 解析的数据
     * @return
     */
    private void setInitValueToList(Object filedValue, int rowIndex, int columnIndex, List<T> data) {
        T object = data.get(rowIndex);
        for (Field field : object.getClass().getDeclaredFields()) {
            //提升反射性能，关闭安全检查
            field.setAccessible(true);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null) {
                if (annotation.index() == columnIndex) {
                    try {
                        field.set(object, filedValue);
                        break;
                    } catch (IllegalAccessException e) {
                        throw new ServiceException(ApiError.ERROR_1012);
                    }
                }
            }
        }

    }

    /**
     * 获取合并单元格的初始值
     * rowIndex对应list的索引
     * columnIndex对应实体内的字段
     *
     * @param firstRowIndex    起始行
     * @param firstColumnIndex 起始列
     * @param data             列数据
     * @return 初始值
     */
    private Object getInitValueFromList(int firstRowIndex, int firstColumnIndex, List<T> data) {
        Object filedValue = null;
        T object = data.get(firstRowIndex);
        for (Field field : object.getClass().getDeclaredFields()) {
            //提升反射性能，关闭安全检查
            field.setAccessible(true);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null) {
                if (annotation.index() == firstColumnIndex) {
                    try {
                        filedValue = field.get(object);
                        break;
                    } catch (IllegalAccessException e) {
                        throw new ServiceException(ApiError.ERROR_1012);
                    }
                }
            }
        }
        return filedValue;

    }


}
