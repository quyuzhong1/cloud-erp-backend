package com.erp.server.srm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.srm.entity.CfgOperateLogFieldEntity;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.server.srm.mapper.CfgOperateLogFieldMapper;
import com.erp.server.srm.service.CfgOperateLogFieldService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Service
public class CfgOperateLogFieldServiceImpl extends SuperServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {


    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String  classPath = String.valueOf(DeliveryOrderDetailEntity.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgOperateLogFieldEntity().setField("deliveryQty").setFieldName("送货数量").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("giftQty").setFieldName("赠品数量").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(classPath).setType(0) .setEnumClass("")
        );

        CfgOperateLogFieldServiceImpl bean = ApplicationContextUtils.getBean(CfgOperateLogFieldServiceImpl.class);
        return bean.saveBatch(logFields);

    }
}
