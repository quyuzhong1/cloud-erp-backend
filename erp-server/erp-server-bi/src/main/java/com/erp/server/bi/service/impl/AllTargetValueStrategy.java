package com.erp.server.bi.service.impl;

/**
 * @author Lambda
 * @Classname AllTargetValue
 * @Description
 * @Date 2023-09-18 14:02
 * @Created by yl
 */

import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.server.bi.mapper.BiTargetYearMapper;
import com.erp.server.bi.service.ListYearMonthValueStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 所有的目标值
 *
 * @Description
 * @Author yl
 * @Date 2023-09-18 14:02
 */
@Component
public class AllTargetValueStrategy implements ListYearMonthValueStrategy {

    @Resource
    private BiTargetYearMapper biTargetYearMapper;


    /**
     * 获取到所有的目标值
     *
     * @return
     */
    @Override
    public List<BiTargetYearDTO.YearMonthValueDTO> ListYearMonthValue(Integer year, String metrics,List<String> flagIdList) {
        String metricsName = MetricsEnum.getNameByCode(metrics);
        return biTargetYearMapper.AllTargetValue(year, metrics,metricsName);
    }
}
