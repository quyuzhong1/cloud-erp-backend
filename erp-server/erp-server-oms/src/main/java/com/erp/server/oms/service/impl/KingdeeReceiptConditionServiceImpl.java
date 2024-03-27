package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KingdeeReceiptConditionEntity;
import com.erp.server.oms.mapper.KingdeeReceiptConditionMapper;
import com.erp.server.oms.service.KingdeeReceiptConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KingdeeReceiptConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;



    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeReceiptConditionDTO.UpdateDTO updateDTO) {
        KingdeeReceiptConditionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶收款条件"));
        KingdeeReceiptConditionEntity kingdeeReceiptConditionEntity =  BeanMapperUtils.map(KingdeeReceiptConditionEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeeReceiptConditionEntity);
        log.info("编辑 开始修改金蝶收款条件数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kingdeeReceiptConditionEntity);
        if(!save) {
            throw new ServiceException("金蝶收款条件保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录金蝶收款条件日志数据，单号：【{}】", kingdeeReceiptConditionEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), kingdeeReceiptConditionEntity.getCode(), "金蝶收款条件");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
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


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeeReceiptConditionEntity kingdeeReceiptConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
