package com.erp.server.bi.service;/**
 * @author Lambda
 * @Classname listYearMonthValue
 * @Description
 * @Date 2023-09-18 12:29
 * @Created by yl
 */

import com.erp.model.bi.dto.BiTargetYearDTO;

import java.util.List;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-18 12:29
 */
public interface ListYearMonthValueStrategy {

    List<BiTargetYearDTO.YearMonthValueDTO> ListYearMonthValue(Integer year,String metrics,List<String> flagIdList);
}
