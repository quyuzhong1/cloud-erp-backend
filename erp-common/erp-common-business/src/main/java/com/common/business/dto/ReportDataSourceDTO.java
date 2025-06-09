package com.common.business.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRField;

import java.util.HashMap;
import java.util.List;

@Data
@Slf4j
public class ReportDataSourceDTO<T> implements JRDataSource {

    private List<T> list;
    private int i = -1;

    public ReportDataSourceDTO(List<T> list) {
        super();
        this.list = list;
    }

    @Override
    public Object getFieldValue(JRField jrField) {
        Object value = null;
        String fieldName = jrField.getName();
        List<HashMap> listMap = BeanUtil.copyToList(list, HashMap.class);
        HashMap hashMap = listMap.get(i);
        for (Object key : hashMap.keySet()) {
            if (String.valueOf(key).equals(fieldName)) {
                Object obj = hashMap.get(key);
                if (obj != null) {
                    if (obj.getClass() == JSONArray.class) {
                        value = BeanUtil.toBean(obj, HashMap.class);
                    } else {
                        value = obj;
                    }
                }
            }
        }
        return value;
    }

    @Override
    public boolean next() {
        i++;
        if (list == null) {
            return false;
        }
        return (i < list.size());
    }
}
