package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.BomChangeDetailsEntity;

/**
 * 变更管理变更实体的信息表(ProductChangeDetails)表服务接口
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
public interface BomChangeDetailsService extends IService<BomChangeDetailsEntity> {


    void saveChangeDetails(String id, String detailsJson);

    String getDetailsJson(String changeId);
}
