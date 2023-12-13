package com.common.business.service;


import com.common.business.dto.RequestDTO;

/**
 * 平台数据拉取对接历史数据
 * @author Cloud
 */
public interface IReportHistoryService<T> {

    void pullDataSave(RequestDTO dto) throws Exception;

    void pullHistoryOrderInfo(RequestDTO requestDTO) throws Exception;

}
