package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import com.erp.model.dmp.entity.ThridUserInfoEntity;
import com.erp.server.dmp.mapper.ThridUserInfoMapper;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.ThridUserInfoService;
import com.sdk.wx.miniapp.api.WxMiniAppService;
import com.sdk.wx.miniapp.response.WxJscodeToSessionResponse;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-04-03
 */
@Slf4j
@Service
public class ThridUserInfoServiceImpl extends SuperServiceImpl<ThridUserInfoMapper, ThridUserInfoEntity> implements ThridUserInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private WxMiniAppService wxMiniAppService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ThridUserInfoDTO.AddResultDTO add(ThridUserInfoDTO.AddDTO addDTO) {
        if(StringUtils.isNotBlank(addDTO.getOpenid())){
            ThridUserInfoEntity oldThridUserInfoEntity = lambdaQuery().eq(ThridUserInfoEntity::getOpenid, addDTO.getOpenid()).one();
            if(Objects.nonNull(oldThridUserInfoEntity)){
                return new ThridUserInfoDTO.AddResultDTO(oldThridUserInfoEntity.getId(), oldThridUserInfoEntity.getId(),oldThridUserInfoEntity.getId());
            }
        }

        ThridUserInfoEntity thridUserInfoEntity = new ThridUserInfoEntity();
        BeanMapper.copy(addDTO.getUserInfo(), thridUserInfoEntity);
        thridUserInfoEntity.setUnionid(addDTO.getUnionid());
        thridUserInfoEntity.setOpenid(addDTO.getOpenid());
        thridUserInfoEntity.setType(addDTO.getType());
        boolean save = super.save(thridUserInfoEntity);
        if(!save) {
            throw new ServiceException("用户单保存失败");
        }
        return new ThridUserInfoDTO.AddResultDTO(thridUserInfoEntity.getId(), thridUserInfoEntity.getId(), thridUserInfoEntity.getId());
    }


    @Override
    public ThridUserInfoDTO.CodeToSessionResp code2Session(ThridUserInfoDTO.CodeToSessionDTO dto) {
        ThridUserInfoDTO.CodeToSessionResp codeToSessionResp = new ThridUserInfoDTO.CodeToSessionResp();
        WxJscodeToSessionResponse wxJscodeToSessionResponse = wxMiniAppService.jsCode2SessionInfo(dto.getJsCode());
        BeanMapper.copy(wxJscodeToSessionResponse, codeToSessionResp);
        if(StringUtils.isNotBlank(wxJscodeToSessionResponse.getOpenid())){
            ThridUserInfoEntity thridUserInfoEntity = lambdaQuery().eq(ThridUserInfoEntity::getOpenid, wxJscodeToSessionResponse.getOpenid()).one();
            if(Objects.nonNull(thridUserInfoEntity)){
                codeToSessionResp.setThridUserId(thridUserInfoEntity.getId());
            }
        }else {
            throw new ServiceException("登录失败");
        }
        return codeToSessionResp;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThridUserInfoDTO.UpdateDTO addOrUpdateDTO) {
        ThridUserInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "用户单"));
        ThridUserInfoEntity thridUserInfoEntity =  BeanMapperUtils.map(ThridUserInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(thridUserInfoEntity);
        log.info("编辑 开始修改用户单数据，id：【{}】", old.getId());
        boolean save = super.updateById(thridUserInfoEntity);
        if(!save) {
            throw new ServiceException("用户单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录用户单日志数据，id：【{}】", thridUserInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thridUserInfoEntity.getId(), "用户单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, thridUserInfoEntity, null, thridUserInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThridUserInfoEntity thridUserInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
