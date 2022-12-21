package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.dto.BiSettlementExchangeRateDTO;
import com.erp.model.dmp.entity.BiSettlementExchangeRateEntity;
import com.erp.server.bi.mapper.BiSettlementExchangeRateMapper;
import com.erp.server.bi.service.BiSettlementExchangeRateService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public Boolean batchAddSettlementExchangeRate(List<Map<String, String>> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.Default);
        }
        List<String> count = list.stream().map((Map m) -> (String) m.get("settlementDate")).distinct().collect(Collectors.toList());
        if (count.size() > 1) {
            throw  new ServiceException(ApiError.ERROR_97010);
        }

        List<BiSettlementExchangeRateEntity> entityList = new ArrayList<>();
        for (Map<String, String> map:list) {
            Iterator<Map.Entry<String, String>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
            BiSettlementExchangeRateEntity entity = new BiSettlementExchangeRateEntity();
            String settlementDate = map.get("settlementDate");
            LocalDateTime localDateTime = LocalDateTime.now();
            try {
                DateFormat format= new SimpleDateFormat("yyyy-MM");
                Date parse = format.parse(settlementDate);
                localDateTime = LocalDateUtil.date2LocalDateTime(parse);
            } catch (ParseException e) {
                throw new ServiceException(ApiError.Default);
            }
            if (ObjectUtils.isNotEmpty(iterator)) {
                while (iterator.hasNext()) {
                    Map.Entry entry = (java.util.Map.Entry) iterator.next();
                    String key = entry.getKey().toString();
                    String value = ObjectUtils.isEmpty(entry.getValue()) ? "" : entry.getValue().toString();
                    if ("settlementDate".equals(key)) {
                        continue;
                    }

                    entity.setRate(MathUtil.valueOf(value));
                    entity.setSourceCurrencyCode(key);
                    entity.setTargetCurrencyCode("CNY");
                    entity.setSettlementDate(localDateTime);
                    entityList.add(entity);
                }
            }
        }
        return  this.saveBatch(entityList);
    }

    @Override
    public List<Map<String, String>> listSettlementExchangeRate() {
        List<Map<String, String>> mapList = new ArrayList<>();
        List<BiSettlementExchangeRateEntity> list = this.list();
        if (CollectionUtils.isNotEmpty(list)) {
            Map<LocalDate, List<BiSettlementExchangeRateEntity>> newMap = list.stream().collect(Collectors.groupingBy(obj -> obj.getSettlementDate().toLocalDate()));
           for (Map.Entry<LocalDate, List<BiSettlementExchangeRateEntity>> entry:newMap.entrySet()) {
               LocalDate key = entry.getKey();
               List<BiSettlementExchangeRateEntity> value = entry.getValue();
               Map<String,String> map = new HashMap<>();
               map.put("settlementDate",key.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
               for (BiSettlementExchangeRateEntity entity : value) {
                   map.put(entity.getSourceCurrencyCode(),entity.getRate().toString());
               }
               mapList.add(map);
           }

        }
        return mapList;
    }

    @Override
    public Boolean batchUpdateSettlementExchangeRate(List<Map<String, String>> list) {

        List<BiSettlementExchangeRateEntity> list1 = this.list();
        if (CollectionUtils.isNotEmpty(list1)) {
            List<String> ids = list1.stream().map(BiSettlementExchangeRateEntity::getId).collect(Collectors.toList());
            boolean remove = this.removeByIds(ids);
            if (remove) {
                //重新新增数据
                this.batchAddSettlementExchangeRate(list);
            }
        }
        return true;
    }


    /**
     * 新增验证是否重复
     */
    private void checkExchangeRate(BiSettlementExchangeRateDTO dto) {
        BiSettlementExchangeRateEntity entity = getOneByParams(dto);
        if (ObjectUtils.isNotEmpty(entity)) {
            throw new ServiceException(500,String.format("源币种[%s],目标币种[%s],结算日期[%s]汇率已存在",dto.getTargetCurrencyCode(),dto.getTargetCurrencyCode(),dto.getSettlementDate()));
        }
    }

    /**
     * 根据目标币种、源币种、日期查询
     */
    private BiSettlementExchangeRateEntity getOneByParams(BiSettlementExchangeRateDTO dto) {
        LambdaQueryWrapper<BiSettlementExchangeRateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSettlementExchangeRateEntity::getSourceCurrencyCode,dto.getSourceCurrencyCode());
        queryWrapper.eq(BiSettlementExchangeRateEntity::getTargetCurrencyCode,dto.getSourceCurrencyCode());
        queryWrapper.eq(BiSettlementExchangeRateEntity::getSettlementDate,dto.getSettlementDate());
        queryWrapper.last("limit 1");
        return  this.getOne(queryWrapper);
    }
}
