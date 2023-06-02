package com.erp.server.sys.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysCodeSkuDTO;
import com.erp.model.sys.entity.SysCodeEntity;
import com.erp.server.sys.mapper.SysCodeMapper;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.SysCodeService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 11:35
 */
@Slf4j
@Service
public class SysCodeServiceImpl extends ServiceImpl<SysCodeMapper, SysCodeEntity>  implements SysCodeService {

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private CommonService commonService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getSkuNo(SysCodeSkuDTO dto) {
        //加锁
        RLock lock = redisson.getLock(DistributedLockEnum.SYS_GEN_DOCNO.getCode() + ":" + dto.getType());
        boolean isLock;
        try {
            // 内部会自动续期
            isLock = lock.tryLock(5, TimeUnit.SECONDS);
            log.info("是否获取到分布式锁: {}", isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
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
        } catch (InterruptedException e) {
            log.error("生成单号获取锁异常",e);
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            //释放锁  锁是否存在，是当前执行线程的锁
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                // 释放锁
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getSpuNo(SysCodeDTO dto) {
        //加锁
        RLock lock = redisson.getLock(DistributedLockEnum.SYS_GEN_DOCNO.getCode() + ":" + dto.getType());
        boolean isLock;
        try {
            isLock = lock.tryLock(5, TimeUnit.SECONDS);
            log.info("是否获取到分布式锁: {}", isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
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
        } catch (InterruptedException e) {
            log.error("生成单号获取锁异常",e);
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            // 释放锁 锁是否存在，是当前执行线程的锁
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                // 释放锁
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getSeqNo(SysCodeDTO dto) {
        //加锁
        RLock lock = redisson.getLock(DistributedLockEnum.SYS_GEN_DOCNO.getCode() + ":" + dto.getType());
        boolean isLock;
        try {
            isLock = lock.tryLock(5, TimeUnit.SECONDS);
            log.info("是否获取到分布式锁: {}", isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            //生成单号
            getOrSaveSysCode(dto);
            StringBuffer sysCode = new StringBuffer();
            sysCode.append(dto.getCategory())
                    .append(String.format("%05d",dto.getNum()));
            if (StringUtils.isBlank(sysCode)) {
                throw new ServiceException(ApiError.ERROR_9027);
            }
            //更新当前顺序码
            updateNumByCode(dto.getId(),dto.getNum());
            return sysCode.toString();
        } catch (InterruptedException e) {
            log.error("生成单号获取锁异常",e);
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            //释放锁 锁是否存在，是当前执行线程的锁
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                // 释放锁
                lock.unlock();
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @GlobalTransactional(propagation = io.seata.tm.api.transaction.Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String getBusinessNo(SysCodeDTO dto) {
        //加锁
        RLock lock = redisson.getLock(DistributedLockEnum.SYS_GEN_DOCNO.getCode() + ":" + dto.getType());
        boolean isLock;
        try {
            isLock = lock.tryLock(5, TimeUnit.SECONDS);
            log.info("是否获取到分布式锁: {}", isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
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
            log.info("seata事务id:{}", RootContext.getXID());
            updateNumByCode(dto.getId(),dto.getNum());
            return sysCode.toString();
        }  catch (InterruptedException e) {
            log.error("生成单号获取锁异常",e);
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            //释放锁  锁是否存在，是当前执行线程的锁
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                // 释放锁
                lock.unlock();
                log.info("分布式锁释放锁: {}", Thread.currentThread().getId());
            }
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
    public void updateNumByCode (String id,Integer num) {
        /**
        LambdaUpdateWrapper<SysCodeEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysCodeEntity::getId,id);
        updateWrapper.set(SysCodeEntity::getNum,num + 1);
        updateWrapper.set(SysCodeEntity::getUpdateTime,new Date());
        this.update(updateWrapper);
         */
        LoginUser loginUser = commonService.getUserInfo();
        this.baseMapper.updateNum(id, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

}
