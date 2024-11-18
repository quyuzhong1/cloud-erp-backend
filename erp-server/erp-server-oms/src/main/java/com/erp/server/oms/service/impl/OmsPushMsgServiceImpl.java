package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.OmsPushMsgDTO;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.server.oms.mapper.OmsPushMsgMapper;
import com.erp.server.oms.service.OmsPushMsgService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 本地推送消息表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-22
 */
@Slf4j
@Service
public class OmsPushMsgServiceImpl extends SuperServiceImpl<OmsPushMsgMapper, OmsPushMsgEntity> implements OmsPushMsgService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OmsPushMsgDTO.AddDTO addDTO) {
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        BeanMapperUtils.copy(addDTO, omsPushMsgEntity);

        log.info("开始新增本地推送消息单");
        boolean save = super.save(omsPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), ApiError.ERROR_92160.msg , omsPushMsgEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, omsPushMsgEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(omsPushMsgEntity.getId(), omsPushMsgEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OmsPushMsgDTO.UpdateDTO updateDTO) {
        OmsPushMsgEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, ApiError.ERROR_92160.msg);
        }
        OmsPushMsgEntity omsPushMsgEntity =  BeanMapperUtils.map(OmsPushMsgEntity.class, updateDTO);
        log.info("编辑 开始修改本地推送消息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(omsPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }
        // 记录主单操作日志
            log.info("编辑 开始记录本地推送消息单日志数据，id：【{}】", omsPushMsgEntity.getId());
            String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), omsPushMsgEntity.getId(), ApiError.ERROR_92160.msg);
        operateLogService.addModuleOperateLogByObj(old, omsPushMsgEntity, null, omsPushMsgEntity.getId(), msg);
        return Boolean.TRUE;
    }

}
