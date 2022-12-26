package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.MathUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;
import com.erp.server.bi.mapper.BiSettlementExchangeRateMapper;
import com.erp.server.bi.service.BiSettlementExchangeRateService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 10:00
 */
@Service
public class BiSettlementExchangeRateServiceImpl extends ServiceImpl<BiSettlementExchangeRateMapper, BiSettlementExchangeRateEntity>
        implements BiSettlementExchangeRateService {


    @Override
    public Boolean batchAddSettlementExchangeRate(List<Map<String, Object>> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.Default);
        }
        Boolean overlap;
        for (int i = 0; i < list.size();i++) {
            Map<String, Object> map1 = list.get(i);
            List<String> settlementDateList1 = (List<String>) map1.get("settlementDateList");
            String settlementDateBegin1 = settlementDateList1.get(0);
            String settlementDateEnd1 = settlementDateList1.get(1);
            for (int j = i + 1; j < list.size();j++) {
                Map<String, Object> map2 = list.get(j);
                List<String> settlementDateList2 = (List<String>) map2.get("settlementDateList");
                String settlementDateBegin2 = settlementDateList2.get(0);
                String settlementDateEnd2 = settlementDateList2.get(1);
                 overlap = isOverlap(settlementDateBegin1, settlementDateEnd1, settlementDateBegin2, settlementDateEnd2);
                 if (overlap) {
                     throw new ServiceException(ApiError.ERROR_97010);
                 }
            }
        }
        List<BiSettlementExchangeRateEntity> entityList = new ArrayList<>();
        for (Map<String, Object> map:list) {
            Iterator<Map.Entry<String, Object>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
            List<String> settlementDateList = (List<String>) map.get("settlementDateList");
            String settlementDateBegin = settlementDateList.get(0);
            String settlementDateEnd = settlementDateList.get(1);
            LocalDate beginDate = LocalDate.parse(settlementDateBegin);
            LocalDate endDate = LocalDate.parse(settlementDateEnd);;
            if (ObjectUtils.isNotEmpty(iterator)) {
                while (iterator.hasNext()) {
                    BiSettlementExchangeRateEntity entity = new BiSettlementExchangeRateEntity();
                    Map.Entry entry = (java.util.Map.Entry) iterator.next();
                    String key = entry.getKey().toString();
                    String value = ObjectUtils.isEmpty(entry.getValue()) ? "" : entry.getValue().toString();
                    if ("settlementDateList".equals(key)) {
                        continue;
                    }
                    String id= IdWorker.getIdStr();
                    entity.setId(id);
                    entity.setExchangeRate(MathUtil.valueOf(value));
                    entity.setSourceCurrencyCode(key);
                    entity.setTargetCurrencyCode("CNY");
                    entity.setSettlementDateBegin(beginDate);
                    entity.setSettlementDateEnd(endDate);
                    entityList.add(entity);
                }
            }
        }
        return  this.saveBatch(entityList);
    }

    @Override
    public List<Map<String, Object>> listSettlementExchangeRate() {
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<BiSettlementExchangeRateEntity> list = this.list();
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, List<BiSettlementExchangeRateEntity>> newMap = list.stream().collect(Collectors.groupingBy(obj -> obj.getSettlementDateBegin().toString().concat(",").concat(obj.getSettlementDateEnd().toString())));
           for (Map.Entry<String, List<BiSettlementExchangeRateEntity>> entry:newMap.entrySet()) {
               String key = entry.getKey();
               String[] date = key.split(",");
               List<BiSettlementExchangeRateEntity> value = entry.getValue();
               LinkedHashMap<String,Object> map = new LinkedHashMap<>();
               List<String> settlementDateList = new ArrayList<>();
               settlementDateList.add(date[0]);
               settlementDateList.add(date[1]);
               map.put("settlementDateList",settlementDateList);
               for (BiSettlementExchangeRateEntity entity : value) {
                   String rate = MathUtil.compareTo(entity.getExchangeRate(), BigDecimal.ZERO) == 0 ? BigDecimal.ZERO.toString() : entity.getExchangeRate().toString();
                   map.put(entity.getSourceCurrencyCode(), rate);
               }
               mapList.add(map);
           }

        }
        return mapList;
    }

    @Override
    public Boolean batchUpdateSettlementExchangeRate(List<Map<String, Object>> list) {
        List<BiSettlementExchangeRateEntity> list1 = this.list();
        if (CollectionUtils.isNotEmpty(list1)) {
            List<String> ids = list1.stream().map(BiSettlementExchangeRateEntity::getId).collect(Collectors.toList());
             this.removeByIds(ids);
        }
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        //重新新增数据
        return this.batchAddSettlementExchangeRate(list);
    }


    /**
     * @return true重叠。false不重叠
     */
    public static boolean isOverlap(String settlementDateBegin1, String settlementDateEnd1, String settlementDateBegin2, String settlementDateEnd2) {
        LocalDate date1 = LocalDate.parse(settlementDateBegin1);
        LocalDate date2 = LocalDate.parse(settlementDateEnd1);
        LocalDate date3 = LocalDate.parse(settlementDateBegin2);
        LocalDate date4 = LocalDate.parse(settlementDateEnd2);
        if (date1.isAfter(date2) || date3.isAfter(date4)) {
            throw new ServiceException(ApiError.ERROR_97011);
        }
        if ((date1.compareTo(date3) >= 0 && date4.compareTo(date1) >= 0) || (date3.compareTo(date1) >= 0 && date2.compareTo(date3) >= 0)) {
            return true;
        }
        return false;
    }
}
