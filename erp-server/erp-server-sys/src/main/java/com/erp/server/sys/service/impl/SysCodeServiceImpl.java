package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.web.service.RedisLock;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysCodeEntity;
import com.erp.server.sys.interceptor.SysInterceptor;
import com.erp.server.sys.mapper.SysCodeMapper;
import com.erp.server.sys.service.SysCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 11:35
 */
@Service
public class SysCodeServiceImpl extends ServiceImpl<SysCodeMapper, SysCodeEntity>  implements SysCodeService {

    @Resource
    private RedisLock redisLock;

    /**
     * 系统编号的缓存锁
     */
    public static final String LOCK_SYS_CODE = "lock:sys_code:";


    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String getSysCode(SysCodeDTO dto) {
        //加锁
        long time = System.currentTimeMillis() + RedisLock.LOCK_TIMEOUT;
        if (!redisLock.aotuTryLock(LOCK_SYS_CODE + dto.getType(), String.valueOf(time))) {
            throw new ServiceException(ApiError.ERROR_9026);
        }
        try {
            //生成单号
            SysCodeDTO codeDto = getOrSaveSysCode(dto);
            StringBuffer sysCode = new StringBuffer();
            sysCode.append(codeDto.getCategory())
                    .append(String.format("%03d",codeDto.getNum()))
                    .append(codeDto.getColorCode())
                    .append(codeDto.getSaleChannel())
                    .append(codeDto.getVersion())
                    .append(codeDto.getCustomized());
            if (StringUtils.isBlank(sysCode)) {
                throw new ServiceException(ApiError.ERROR_9027);
            }
            //更新当前顺序码
            updateNumByCode(codeDto.getId(),codeDto.getNum());
            return sysCode.toString();
        } finally {
            //解锁
            redisLock.unlock(LOCK_SYS_CODE + dto.getType(), String.valueOf(time));
        }
    }

    /**
     * @description: 验证编码类型是否已经存在
     * @author Will
     * @date: 2022/11/21 17:58
     * @param dto
     
     */
    private SysCodeDTO getOrSaveSysCode (SysCodeDTO dto) {
        LambdaQueryWrapper<SysCodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysCodeEntity::getType,dto.getType());
        queryWrapper.eq(SysCodeEntity::getCategory,dto.getCategory());
        queryWrapper.last("LIMIT 1");
        SysCodeEntity sysCodeEntity = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(sysCodeEntity)) {
            dto.setNum(sysCodeEntity.getNum());
            dto.setId(sysCodeEntity.getId());
            return dto;
        }
        SysCodeEntity entity = new SysCodeEntity();
        LoginUser loginUser = SysInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            entity.setCreateUserId(loginUser.getUid());
            entity.setCreateUserName(loginUser.getUserName());
        }
        BeanMapperUtils.copy(dto,entity);
        boolean flag = this.save(entity);
        dto.setNum(1);
        dto.setId(entity.getId());
        if (!flag) {
            throw new ServiceException(ApiError.Default);
        }
        return dto;
    }

    /**
     * @description: 更新顺序码
     * @author Will
     * @date: 2022/11/21 18:34
     * @param id
     * @param num
     */
    private void updateNumByCode (String id,Integer num) {
        LambdaUpdateWrapper<SysCodeEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysCodeEntity::getId,id);
        updateWrapper.set(SysCodeEntity::getNum,num);
        this.update(updateWrapper);
    }

}
