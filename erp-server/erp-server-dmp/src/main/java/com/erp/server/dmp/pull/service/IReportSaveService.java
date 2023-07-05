package com.erp.server.dmp.pull.service;


import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.gyy.GyyRefundEntity;
import org.springframework.transaction.annotation.Transactional;

public interface IReportSaveService<T> {

    /**
     * 下载数据
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
