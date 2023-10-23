package com.erp.server.oms.service;


import com.erp.model.oms.dto.ShopAuthorizeDTO;

public interface AuthModelService {
    /**
     * 下载数据
     * @param dto
     * @throws Exception
     */
    void shopAuthorize(ShopAuthorizeDTO dto) throws Exception;

}
