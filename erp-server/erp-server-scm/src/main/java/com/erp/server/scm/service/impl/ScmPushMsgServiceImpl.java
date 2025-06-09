package com.erp.server.scm.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.ScmPushMsgDTO;
import com.erp.model.scm.entity.ScmPushMsgEntity;
import com.erp.server.scm.mapper.ScmPushMsgMapper;
import com.erp.server.scm.service.ScmPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 本地推送消息表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-29
 */
@Slf4j
@Service
public class ScmPushMsgServiceImpl extends SuperServiceImpl<ScmPushMsgMapper, ScmPushMsgEntity> implements ScmPushMsgService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ScmPushMsgDTO.AddDTO addDTO) {
        ScmPushMsgEntity scmPushMsgEntity = new ScmPushMsgEntity();
        BeanMapperUtils.copy(addDTO, scmPushMsgEntity);

        log.info("开始新增本地推送消息单");
        boolean save = super.save(scmPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }

        return new BaseResultDTO.AddDTO(scmPushMsgEntity.getId(), scmPushMsgEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ScmPushMsgDTO.UpdateDTO updateDTO) {
        ScmPushMsgEntity old = super.getById(updateDTO.getId());
        ScmPushMsgEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "本地推送消息单"));
        ScmPushMsgEntity scmPushMsgEntity =  BeanMapperUtils.map(ScmPushMsgEntity.class, updateDTO);

        log.info("编辑 开始修改本地推送消息单数据，id：【{}】", oldEntity.getId());
        boolean save = super.updateById(scmPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }
        return Boolean.TRUE;
    }


}
