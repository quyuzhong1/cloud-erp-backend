package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiDataSourceCustomDetailEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 18:15
 */
public interface BiDataSourceCustomDetailService  extends IService<BiDataSourceCustomDetailEntity> {
    /**
     * 根据自助主表id查询
     */
    List<BiDataSourceCustomDetailEntity> listByCustomIds(List<String> customIds);
    /**
     * @description: 根据自助表ids删除
     * @author Will
     * @date: 2022/12/27 11:50
     * @param customIds
     */
    void removeByCustomIds(List<String> customIds);
}
