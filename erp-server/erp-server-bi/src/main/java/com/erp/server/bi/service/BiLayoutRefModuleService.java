package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;

import java.util.List;

/**
 * 布局与模块关系表(BiLayoutRefModule)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:29:39
 */
public interface BiLayoutRefModuleService  extends IService<BiLayoutRefModuleEntity> {



    /**
     * 保存模块
     * @author yl
     * @date 2022-12-13 16:37
     * @param layoutId
     * @param blockNo
     * @param moduleIdList
     * @return void
     */
    void addLayoutRefModule(String layoutId, String blockNo, List<String> moduleIdList);
}
