package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.KingdeeReceiptConditionDTO;
import com.erp.model.oms.entity.KingdeeReceiptConditionEntity;
import com.erp.server.oms.mapper.KingdeeReceiptConditionMapper;
import com.erp.server.oms.service.KingdeeReceiptConditionService;
import com.erp.server.oms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
/**
 * <p>
 * 金蝶收款条件 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-08
 */
@Slf4j
@Service
public class KingdeeReceiptConditionServiceImpl extends SuperServiceImpl<KingdeeReceiptConditionMapper, KingdeeReceiptConditionEntity> implements KingdeeReceiptConditionService {
    @Resource
    private OperateLogService operateLogService;


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeReceiptConditionDTO.UpdateDTO updateDTO) {
        KingdeeReceiptConditionEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶收款条件");
        }
        KingdeeReceiptConditionEntity kingdeeReceiptConditionEntity =  BeanMapperUtils.map(KingdeeReceiptConditionEntity.class, updateDTO);
        log.info("编辑 开始修改金蝶收款条件数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kingdeeReceiptConditionEntity);
        if(!save) {
            throw new ServiceException("金蝶收款条件保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录金蝶收款条件日志数据，单号：【{}】", kingdeeReceiptConditionEntity.getCode());
            String msg =  CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kingdeeReceiptConditionEntity.getCode(), "金蝶收款条件");
        operateLogService.addModuleOperateLogByObj(old, kingdeeReceiptConditionEntity, null, kingdeeReceiptConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void updateDisable(List<String> ids, boolean disable) {
        if (CollectionUtils.isNotEmpty(ids)) {
            this.lambdaUpdate().set(KingdeeReceiptConditionEntity::getDisabled, disable).
                    in(KingdeeReceiptConditionEntity::getId, ids).update();
        }
    }
}
