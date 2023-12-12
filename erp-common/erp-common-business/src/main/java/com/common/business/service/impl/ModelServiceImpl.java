package com.common.business.service.impl;


import com.common.business.dto.RequestDTO;
import com.common.business.handler.SaveHandler;
import com.common.business.service.ModelService;
import org.springframework.stereotype.Service;

/**
 * 平台数据拉取对接模板模式
 * @author Cloud
 */
@Service
public class ModelServiceImpl implements ModelService {

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        //模板模式 处理数据 存库
        SaveHandler.pullDataSave(dto);
    }
}
