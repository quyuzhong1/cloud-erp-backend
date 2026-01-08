package com.erp.server.sys.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysEventTrackingEntity;
import com.erp.server.sys.mapper.SysEventTrackingMapper;
import com.erp.server.sys.service.SysDepartmentUserService;
import com.erp.server.sys.service.SysEventTrackingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.SysEventTrackingDTO;
import com.common.core.utils.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 前端埋点事件记录 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-02-19
 */
@Slf4j
@Service
public class SysEventTrackingServiceImpl extends SuperServiceImpl<SysEventTrackingMapper, SysEventTrackingEntity> implements SysEventTrackingService {

    @Resource
    private SysDepartmentUserService sysDepartmentUserService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SysEventTrackingDTO.AddDTO addDTO) {
        SysEventTrackingEntity sysEventTrackingEntity = new SysEventTrackingEntity();
        BeanMapperUtils.copy(addDTO, sysEventTrackingEntity);

        // 数据处理
        handleData(sysEventTrackingEntity, addDTO);

//        log.info("开始新增前端埋点事件记录");
        boolean save = super.save(sysEventTrackingEntity);
        if(!save) {
            throw new ServiceException("前端埋点事件记录保存失败");
        }

        return new BaseResultDTO.AddDTO(sysEventTrackingEntity.getId(), sysEventTrackingEntity.getId());
    }



    /**
    * 新增修改处理数据
    */
    private void handleData(SysEventTrackingEntity entity, SysEventTrackingDTO.AddDTO addDTO) {
        //  验证数据 & 数据赋值
        LoginUser loginUser = UserContext.getLoginUser();
        if (null != loginUser){
            entity.setUserId(loginUser.getUid());
            entity.setUserName(loginUser.getUserName());
        }
        if (null != addDTO.getEventData()){
            entity.setEventData(addDTO.getEventData().toJSONString());
        }

        // 部门信息
        if (StringUtils.isNotBlank(entity.getUserId())) {
            //获取部门信息
            SysDepartmentUserNumberDTO depart = sysDepartmentUserService.getDeptByUserId(entity.getUserId());
            if (null != depart){
                entity.setDeptId(depart.getDepartmentId());
                entity.setDeptName(depart.getDepartmentName());
            }
        }

        // 当前请求
        HttpServletRequest request = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
            entity.setIpAddress(IpUtils.getIpAddr(request));
        }
    }
}
