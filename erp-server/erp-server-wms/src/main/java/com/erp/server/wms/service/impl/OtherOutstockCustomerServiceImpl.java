package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
import com.erp.model.wms.entity.OtherOutstockCustomerEntity;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.server.wms.mapper.OtherOutstockCustomerMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.OtherOutstockCustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Resource
    private CustomerFeign customerFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(OtherOutstockCustomerDTO.AddDTO otherOutstockCustomer, String mainId) {
        OtherOutstockCustomerEntity entity = new OtherOutstockCustomerEntity();
        BeanMapperUtils.copy(otherOutstockCustomer,entity);
        entity.setMainId(mainId);
        //数据处理
        handleData(entity);

        this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OtherOutstockCustomerDTO.UpdateDTO otherOutstockCustomer,String mainId) {
        OtherOutstockCustomerEntity entity = new OtherOutstockCustomerEntity();
        BeanMapperUtils.copy(otherOutstockCustomer,entity);

        //数据处理
        handleData(entity);

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

    /**
     * @description: 数据处理
     * @author Will
     * @date: 2024/4/8 17:10
     * @param entity
     */
    private void handleData (OtherOutstockCustomerEntity entity) {
        if (CharSequenceUtil.isBlank(entity.getCustomerId())) {
            return;
        }
        CustomerInfoEntity customerInfoEntity = customerFeign.getCustomerById(entity.getCustomerId());
        if (ObjectUtil.isEmpty(customerInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        entity.setCustomerCode(customerInfoEntity.getCode());
    }
}
