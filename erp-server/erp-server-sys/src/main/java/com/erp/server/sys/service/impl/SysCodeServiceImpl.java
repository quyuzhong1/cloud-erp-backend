package com.erp.server.sys.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.service.RedisLock;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysCodeSkuDTO;
import com.erp.model.sys.entity.SysCodeEntity;
import com.erp.server.sys.mapper.SysCodeMapper;
import com.erp.server.sys.service.SysCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
    public String getSkuNo(SysCodeSkuDTO dto) {
        //加锁
        long time = System.currentTimeMillis() + RedisLock.LOCK_TIMEOUT;
        if (!redisLock.aotuTryLock(LOCK_SYS_CODE + dto.getType(), String.valueOf(time))) {
            throw new ServiceException(ApiError.ERROR_9026);
        }
        try {
            SysCodeDTO sysCodeDto = new SysCodeDTO();
            BeanMapperUtils.copy(dto,sysCodeDto);
            //生成单号
            getOrSaveSysCode(sysCodeDto);
            StringBuffer sysCode = new StringBuffer();
            sysCode.append(sysCodeDto.getCategory())
                    .append(String.format("%03d",sysCodeDto.getNum()))
                    .append(dto.getSalesChannel())
                    .append(dto.getColorCode())
                    .append(dto.getVersion())
                    .append(dto.getCustomized());
            if (StringUtils.isBlank(sysCode)) {
                throw new ServiceException(ApiError.ERROR_9027);
            }
            //更新当前顺序码
            updateNumByCode(sysCodeDto.getId(),sysCodeDto.getNum());
            return sysCode.toString();
        } finally {
            //解锁
            redisLock.unlock(LOCK_SYS_CODE + dto.getType(), String.valueOf(time));
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String getSpuNo(SysCodeDTO dto) {
        //加锁
        long time = System.currentTimeMillis() + RedisLock.LOCK_TIMEOUT;
        if (!redisLock.aotuTryLock(LOCK_SYS_CODE + dto.getType(), String.valueOf(time))) {
            throw new ServiceException(ApiError.ERROR_9026);
        }
        try {
            //生成单号
            getOrSaveSysCode(dto);
            StringBuffer sysCode = new StringBuffer();
            sysCode.append(dto.getCategory())
                    .append(String.format("%02d",dto.getNum()));
            if (StringUtils.isBlank(sysCode)) {
                throw new ServiceException(ApiError.ERROR_9027);
            }
            //更新当前顺序码
            updateNumByCode(dto.getId(),dto.getNum());
            return sysCode.toString();
        } finally {
            //解锁
            redisLock.unlock(LOCK_SYS_CODE + dto.getType(), String.valueOf(time));
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String getBusinessNo(SysCodeDTO dto) {
        //加锁
        long time = System.currentTimeMillis() + RedisLock.LOCK_TIMEOUT;
        if (!redisLock.aotuTryLock(LOCK_SYS_CODE + dto.getType(), String.valueOf(time))) {
            throw new ServiceException(ApiError.ERROR_9026);
        }
        try {
            //生成单号
            getOrSaveSysCode(dto);
            //判断最后修改日期是否是当天，不是则重置num
            if (dto.getUpdateTime().before(DateUtil.beginOfDay(new Date()))) {
                dto.setNum(MathUtil.ONE);
            }
            StringBuffer sysCode = new StringBuffer();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
            sysCode.append(dto.getCategory())
                    .append(LocalDateTime.now().format(formatter))
                    .append(String.format("%05d",dto.getNum()));
            if (StringUtils.isBlank(sysCode)) {
                throw new ServiceException(ApiError.ERROR_9027);
            }
            //更新当前顺序码
            updateNumByCode(dto.getId(),dto.getNum());
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
    private void getOrSaveSysCode (SysCodeDTO dto) {
        LambdaQueryWrapper<SysCodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysCodeEntity::getType,dto.getType());
        queryWrapper.eq(SysCodeEntity::getCategory,dto.getCategory());
        queryWrapper.last("LIMIT 1");
        SysCodeEntity sysCodeEntity = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(sysCodeEntity)) {
            dto.setNum(sysCodeEntity.getNum());
            dto.setId(sysCodeEntity.getId());
            dto.setUpdateTime(sysCodeEntity.getUpdateTime());
            return;
        }
        SysCodeEntity entity = new SysCodeEntity();
        BeanMapperUtils.copy(dto,entity);
        boolean flag = this.save(entity);
        dto.setNum(MathUtil.ONE);
        dto.setId(entity.getId());
        dto.setUpdateTime(entity.getUpdateTime());
        if (!flag) {
            throw new ServiceException(ApiError.Default);
        }
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
        updateWrapper.set(SysCodeEntity::getNum,num + 1);
        updateWrapper.set(SysCodeEntity::getUpdateTime,new Date());
        this.update(updateWrapper);
    }

}
