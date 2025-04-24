package com.erp.server.mrp.calculation.service;

import java.time.LocalDate;

public interface DataArchivingService {

    /**
     * 归档所有数据
     */
    void dataArchiving(LocalDate calculationDate, Integer cleanDay);

    /**
     * 归档某条数据
     */
    void dataArchiving(String detailId);
}
