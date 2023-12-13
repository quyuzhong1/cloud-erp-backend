package com.common.business.service;


import com.common.business.dto.RequestDTO;

/**
 * 平台数据拉取对接
 * @author Cloud
 */
public interface IReportSaveService<T> {

    /**
     * 下载数据封装数据
     * @param dto
     */
    void pullDataSave(RequestDTO dto);

    /**
     * 清洗数据
     * @param tableName
     * @param size
     */
    void cleanDataSave(String tableName, int size);

    /**
     * 更新并发送mq
     * @param mongoDatum
     */
    void updateAndSaveDb(T mongoDatum);

}
