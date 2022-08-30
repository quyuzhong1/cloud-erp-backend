package com.cloud.erp.chrome.handler;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname ConvertHandler
 * @Description TODO
 * @Date 2022/5/30 12:15
 * @Created by yl
 */
@Component
public class ConvertHandler {
    public <T> List<List<T>>   splitList(List<T> dataList, int limit) {
        List<List<T>> list = new ArrayList<>();
        int dataSize = dataList.size();
        int cmFlag = dataSize % limit;
        int divisorSize = dataSize / limit;
        int forSize = cmFlag > 0 ? divisorSize + 1 : divisorSize;
        for (int i = 0; i < forSize; i++) {
            int flagNum = i * limit;
            if (i == 0) {
                list.add(dataList.subList(i, limit));
            } else {
                if (i == forSize - 1) {
                    list.add(dataList.subList(flagNum, dataSize));
                } else {
                    list.add(dataList.subList(flagNum, flagNum + limit));
                }

            }
        }
        return list;
    }
}
