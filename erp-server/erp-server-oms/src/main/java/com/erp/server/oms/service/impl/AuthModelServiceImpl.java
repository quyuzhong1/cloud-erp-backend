package com.erp.server.oms.service.impl;


import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.server.oms.service.AuthModelService;
import org.springframework.stereotype.Service;

/**
 * 授权模板模式
 * @Author Luo_WG
 * @Date 2023/10/23 18:49
 **/
@Service
public class AuthModelServiceImpl implements AuthModelService {

    @Override
    public void shopAuthorize(ShopAuthorizeDTO dto) throws Exception {
        //模板模式 处理数据 存库
        AuthSaveHandler.pullDataSave(dto);
    }
}
