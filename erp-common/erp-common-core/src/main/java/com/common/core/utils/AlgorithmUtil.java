package com.common.core.utils;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmUtil<T> {

    /**
     * 笛卡尔乘积算法
     * @Author Luo_WG
     * @Date 2022/10/12 10:39
     * @param dimensionValue 原List
     * @param result 通过乘积转化后的数组
     * @param layer 中间参数
     * @param currentList 中间参数
     * @return void
     **/
    public void descartes(List<List<T>> dimensionValue, List<List<T>> result, int layer, List<T> currentList) {
        if (layer < dimensionValue.size() - 1) {
            if (dimensionValue.get(layer).size() == 0) {
                descartes(dimensionValue, result, layer + 1, currentList);
            } else {
                for (int i = 0; i < dimensionValue.get(layer).size(); i++) {
                    List<T> list = new ArrayList<T>(currentList);
                    list.add(dimensionValue.get(layer).get(i));
                    descartes(dimensionValue, result, layer + 1, list);
                }
            }
        } else if (layer == dimensionValue.size() - 1) {
            if (dimensionValue.get(layer).size() == 0) {
                result.add(currentList);
            } else {
                for (int i = 0; i < dimensionValue.get(layer).size(); i++) {
                    List<T> list = new ArrayList<T>(currentList);
                    list.add(dimensionValue.get(layer).get(i));
                    result.add(list);
                }
            }
        }
    }
}
