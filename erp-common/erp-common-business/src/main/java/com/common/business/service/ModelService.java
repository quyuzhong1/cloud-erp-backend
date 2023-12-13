package com.common.business.service;


import com.common.business.dto.RequestDTO;

public interface ModelService {
    /**
     * 下载数据
     * @param dto
     * @throws Exception
     */
    void pullDataSave(RequestDTO dto) throws Exception;

}
