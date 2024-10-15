package com.erp.server.mrp.calculation.service;

public interface DataArchivingService {

    /**
     * 归档所有数据
     */
    void dataArchiving();

    /**
     * 归档某条数据
     */
    void dataArchiving(String detailId);
}
