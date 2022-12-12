package com.erp.server.dmp.pull.service;


import com.erp.model.dmp.dto.RequestDTO;

public interface ModelService {
    /*
    * 拉取数据 存库
    * */
    void pullDataSave(RequestDTO dto) throws Exception;

}
