package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
import com.erp.model.wms.entity.OtherOutstockCustomerEntity;
import com.erp.server.wms.mapper.OtherOutstockCustomerMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.OtherOutstockCustomerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 其他出库客户表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class OtherOutstockCustomerServiceImpl extends SuperServiceImpl<OtherOutstockCustomerMapper, OtherOutstockCustomerEntity> implements OtherOutstockCustomerService {

    @Resource
    private OperateLogService operateLogService;

    @Override
    public void add(OtherOutstockCustomerDTO.AddDTO otherOutstockCustomer, String mainId) {
        OtherOutstockCustomerEntity entity = new OtherOutstockCustomerEntity();
        BeanMapperUtils.copy(otherOutstockCustomer,entity);
        entity.setMainId(mainId);
        this.save(entity);
    }

    @Override
    public void update(OtherOutstockCustomerDTO.UpdateDTO otherOutstockCustomer,String mainId) {
        OtherOutstockCustomerEntity entity = new OtherOutstockCustomerEntity();
        BeanMapperUtils.copy(otherOutstockCustomer,entity);

        //添加操作日志
        OtherOutstockCustomerEntity old = this.getById(otherOutstockCustomer.getId());
        operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.OTHER_OUTSTOCK.getCode(),mainId,"",null);
        this.updateById(entity);
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(OtherOutstockCustomerEntity::getMainId,mainIds).remove();
    }

    @Override
    public OtherOutstockCustomerEntity getByMainId(String mainId) {
       return lambdaQuery().eq(OtherOutstockCustomerEntity::getMainId,mainId).one();
    }

}
