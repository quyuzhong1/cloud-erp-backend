package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;
import com.erp.server.bi.mapper.BiLayoutRefModuleMapper;
import com.erp.server.bi.service.BiLayoutRefModuleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 布局与模块关系表(BiLayoutRefModule)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:29:39
 */
@Service
public class BiLayoutRefModuleServiceImpl extends ServiceImpl<BiLayoutRefModuleMapper, BiLayoutRefModuleEntity> implements BiLayoutRefModuleService {


    /**
     * 保存模块
     *
     * @param layoutId
     * @param blockNo
     * @param moduleIdList
     * @return void
     * @author yl
     * @date 2022-12-13 16:37
     */
    @Override
    public void addLayoutRefModule(String layoutId, String blockNo, List<String> moduleIdList) {
        if (CollectionUtils.isNotEmpty(moduleIdList)) {
            int size = moduleIdList.size();
            List<BiLayoutRefModuleEntity> addList = new ArrayList<>(size);
            for (int i = 1; i <= size; i++) {
                BiLayoutRefModuleEntity refModule = new BiLayoutRefModuleEntity();
                refModule.setBlockNo(blockNo);
                refModule.setLayoutId(layoutId);
                refModule.setSerialNo(i);
                refModule.setModuleId(moduleIdList.get(i));
                addList.add(refModule);
            }
            this.saveBatch(addList);
        }

    }
}
