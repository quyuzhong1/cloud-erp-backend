package com.erp.server.sys.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.sys.entity.ImlDictCityEntity;
import com.erp.server.sys.mapper.ImlDictCityMapper;
import com.erp.server.sys.service.ImlDictCityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 城市字典表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2023-11-22
 */
@Slf4j
@Service
public class ImlDictCityServiceImpl extends SuperServiceImpl<ImlDictCityMapper, ImlDictCityEntity> implements ImlDictCityService {

    /**
    * 新增修改处理数据
    */
    private void handleData(ImlDictCityEntity imlDictCityEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
