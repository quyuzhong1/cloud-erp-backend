package com.erp.server.plm.service;

import com.common.business.service.SuperService;
import com.erp.model.plm.dto.MouldDocInfoDTO;
import com.erp.model.plm.entity.MouldDocInfoEntity;

import java.util.List;

/**
 * <p>
 * 模具文档信息 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface MouldDocInfoService extends SuperService<MouldDocInfoEntity> {


    /**
     * 保存文档信息
     *
     * @param docList 文档
     * @param id      模具id
     */
    void add(List<MouldDocInfoDTO.UpdateDTO> docList, String id);

    /**
     * 根据模具id查询文档
     * @param id id
     */
    List<MouldDocInfoDTO.ViewDTO> listByMouldId(String id);
}
