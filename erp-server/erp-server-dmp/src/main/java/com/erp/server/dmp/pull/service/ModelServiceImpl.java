package com.erp.server.dmp.pull.service;

import com.erp.model.dmp.dto.RequestDTO;
import com.erp.server.dmp.config.SaveHandler;
import com.erp.server.dmp.pull.service.ModelService;
import org.springframework.stereotype.Service;

@Service
public class ModelServiceImpl implements ModelService {

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        //模板模式 处理数据 存库
        SaveHandler.pullDataSave(dto);
    }
}
