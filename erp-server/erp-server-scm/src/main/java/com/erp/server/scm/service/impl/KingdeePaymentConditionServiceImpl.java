package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KingdeeReceiptConditionEntity;
import com.erp.model.scm.entity.KingdeePaymentConditionEntity;
import com.erp.server.scm.mapper.KingdeePaymentConditionMapper;
import com.erp.server.scm.service.KingdeePaymentConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.scm.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.KingdeePaymentConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-08
 */
@Slf4j
@Service
public class KingdeePaymentConditionServiceImpl extends SuperServiceImpl<KingdeePaymentConditionMapper, KingdeePaymentConditionEntity> implements KingdeePaymentConditionService {





    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeePaymentConditionDTO.UpdateDTO updateDTO) {
        KingdeePaymentConditionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        KingdeePaymentConditionEntity kingdeePaymentConditionEntity =  BeanMapperUtils.map(KingdeePaymentConditionEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeePaymentConditionEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kingdeePaymentConditionEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public void updateDisable(List<String> ids, boolean disable) {
        if (CollectionUtils.isNotEmpty(ids)) {
            this.lambdaUpdate().set(KingdeePaymentConditionEntity::getDisabled, disable).
                    in(KingdeePaymentConditionEntity::getId, ids).update();
        }
    }

    @Override
    public KingdeePaymentConditionEntity getByCode(String code) {
        if(StringUtils.isBlank(code)){
            return null;
        }
        return this.lambdaQuery().eq(KingdeePaymentConditionEntity::getCode, code).last("LIMIT 1").one();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeePaymentConditionEntity kingdeePaymentConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
