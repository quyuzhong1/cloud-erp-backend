package com.common.business.service;


import com.common.business.dto.RequestDTO;

import java.util.List;

/**
 * 平台数据拉取对接
 * @author Cloud
 */
public interface IPlatformDataSaveService<T, R> {

    /**
     * 下载数据
     * @param dto
     * @return
     */
    List<T> downloadData(RequestDTO dto);

    /**
     * 下载数据
     * @param sourceDataList
     * @return
     */
    List<R> convertData(List<T> sourceDataList);

}
