package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysCodeSkuDTO;
import com.erp.model.sys.entity.SysCodeEntity;
import com.erp.server.sys.mapper.SysCodeMapper;
import com.erp.server.sys.service.SysCodeService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Will
 * @version 1.0

 * @date 2022/11/21 11:35
 */
@Slf4j
@Service
public class SysCodeServiceImpl extends ServiceImpl<SysCodeMapper, SysCodeEntity>  implements SysCodeService {

    /**
     * 单号生成分布式锁等待时间（秒）。
     * 注意：单号生成涉及 Seata 分支事务，sys_code 行会被全局锁保护到全局事务提交，
     * 5s 在并发或长事务场景下经常吃不消，调到 30s 给排队留足空间。
     */
    private static final long SYS_CODE_LOCK_WAIT_SECONDS = 30L;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void initTransactionTemplate() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public String getSkuNo(SysCodeSkuDTO dto) {
        // 单条复用批量逻辑，避免双份维护
        return getSkuNoBatch(dto, 1).get(0);
    }

    @Override
    public List<String> getSkuNoBatch(SysCodeSkuDTO dto, int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        // 1) 锁必须包住事务提交点：旧实现是 @Transactional 包外、锁包内，导致 unlock 早于 commit，
        //    下一持锁者会读到旧 num 进而生成重复编码（再触发上游的 isExistSKuNo 递归 → 锁竞争雪崩）。
        //    这里改为：先拿 Redisson 锁，锁内部用 TransactionTemplate 显式开启/提交事务。
        // 2) 不再 catch (Exception) 兜底为 BILL_DATA_LOCKED，避免把真实 SQL/网络异常掩盖成"锁失败"。
        RLock lock = redisson.getLock(DistributedLockEnum.SYS_GEN_DOCNO.getCode() + ":" + dto.getType());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(SYS_CODE_LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("生成 sku 编号未获取到分布式锁: type={}, category={}", dto.getType(), dto.getCategory());
                throw new ServiceException(ApiError.BILL_DATA_LOCKED);
            }
            return transactionTemplate.execute(status -> doGenSkuNo(dto, count));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("生成 sku 编号获取锁被中断", e);
            throw new ServiceException(ApiError.BILL_DATA_LOCKED);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 在锁与事务保护下，一次性发放 count 个连续 sku 编号。
     * 顺序码保持与单条 getSkuNo 一致：[category] + 3 位填充顺序号。
     */
    private List<String> doGenSkuNo(SysCodeSkuDTO dto, int count) {
        SysCodeDTO sysCodeDto = new SysCodeDTO();
        BeanMapperUtils.copy(dto, sysCodeDto);
        getOrSaveSysCode(sysCodeDto);
        int startNum = sysCodeDto.getNum();
        List<String> codes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String code = sysCodeDto.getCategory() + String.format("%03d", startNum + i);
            if (StringUtils.isBlank(code)) {
                throw new ServiceException(ApiError.COMMON_CODE_GENERATE_FAILED);
            }
            codes.add(code);
        }
        // updateNumByCode 内部会再 +1，要让 num 推进到 startNum + count，传 startNum + count - 1
        updateNumByCode(sysCodeDto.getId(), startNum + count - 1);
        return codes;
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
                throw new ServiceException(ApiError.BILL_DATA_LOCKED);
            }
            //生成单号
            getOrSaveSysCode(dto);
            StringBuffer sysCode = new StringBuffer();
            sysCode.append(dto.getCategory())
                    .append(String.format("%02d",dto.getNum()));
            if (StringUtils.isBlank(sysCode)) {
                throw new ServiceException(ApiError.COMMON_CODE_GENERATE_FAILED);
            }
            //更新当前顺序码
            updateNumByCode(dto.getId(),dto.getNum());
            return sysCode.toString();
        } catch (Exception e) {
            log.error("生成单号获取锁异常",e);
            throw new ServiceException(ApiError.BILL_DATA_LOCKED);
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
                throw new ServiceException(ApiError.BILL_DATA_LOCKED);
            }
            //生成单号
            getOrSaveSysCode(dto);
            StringBuffer sysCode = new StringBuffer();
            sysCode.append(dto.getCategory())
                    .append(String.format("%05d",dto.getNum()));
            if (StringUtils.isBlank(sysCode)) {
                throw new ServiceException(ApiError.COMMON_CODE_GENERATE_FAILED);
            }
            //更新当前顺序码
            updateNumByCode(dto.getId(),dto.getNum());
            return sysCode.toString();
        } catch (Exception e) {
            log.error("生成单号获取锁异常",e);
            throw new ServiceException(ApiError.BILL_DATA_LOCKED);
        } finally {
            //释放锁 锁是否存在，是当前执行线程的锁
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                // 释放锁
                lock.unlock();
            }
        }
    }


//    @Deprecated
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
//    public String getBusinessNo(SysCodeDTO dto) {
//        //加锁
//        RLock lock = redisson.getLock(DistributedLockEnum.SYS_GEN_DOCNO.getCode() + ":" + dto.getType());
//        boolean isLock;
//        try {
//            isLock = lock.tryLock(5, TimeUnit.SECONDS);
//            log.info("是否获取到分布式锁: {}", isLock);
//            if (!isLock) {
//                throw new ServiceException(ApiError.ERROR_DATA_LOCKED);
//            }
//            //生成单号
//            getOrSaveSysCode(dto);
//            //判断最后修改日期是否是当天，不是则重置num
//            if (dto.getUpdateTime().before(DateUtil.beginOfDay(new Date()))) {
//                dto.setNum(MathUtil.ONE);
//            }
//            StringBuffer sysCode = new StringBuffer();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
//            sysCode.append(dto.getCategory())
//                    .append(LocalDateTime.now().format(formatter))
//                    .append(String.format("%05d",dto.getNum()));
//            if (StringUtils.isBlank(sysCode)) {
//                throw new ServiceException(ApiError.ERROR_9027);
//            }
//            //更新当前顺序码
//            log.info("seata事务id:{}", RootContext.getXID());
//            updateNumByCode(dto.getId(),dto.getNum());
//            return sysCode.toString();
//        }  catch (InterruptedException e) {
//            log.error("生成单号获取锁异常",e);
//            throw new ServiceException(ApiError.ERROR_DATA_LOCKED);
//        } finally {
//            //释放锁  锁是否存在，是当前执行线程的锁
//            if(lock.isLocked() && lock.isHeldByCurrentThread()){
//                // 释放锁
//                lock.unlock();
//                log.info("分布式锁释放锁: {}", Thread.currentThread().getId());
//            }
//        }
//    }


    /**
     * @description: 验证编码类型是否已经存在
     * @author Will
     * @date: 2022/11/21 17:58
     * @param dto
     */
    private void getOrSaveSysCode (SysCodeDTO dto) {
        LambdaQueryWrapper<SysCodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 注意：sys_code.type 列在 PostgreSQL 中实际是 character varying。
        // LambdaQueryWrapper 直接传 Integer 会被 JDBC 按 INTEGER 绑定，
        // 触发 PSQL "operator does not exist: character varying = integer"。
        // 这里显式按 String 传，使参数走 VARCHAR 绑定。
        queryWrapper.eq(SysCodeEntity::getType, dto.getType() == null ? null : dto.getType());
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
        LocalDateTime now = LocalDateTime.now();
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        entity.setUpdateTime(now);
        entity.setUpdateUserId(userId);
        entity.setUpdateUserName(userName);
        entity.setCreateTime(now);
        entity.setCreateUserId(userId);
        entity.setCreateUserName(userName);
        boolean flag = this.save(entity);
        dto.setNum(MathUtil.ONE);
        dto.setId(entity.getId());
        dto.setUpdateTime(entity.getUpdateTime());
        if (!flag) {
            throw new ServiceException(ApiError.HTTP_UNKNOWN);
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
        LoginUser loginUser = UserContext.getNonLoginUser();
      lambdaUpdate().eq(SysCodeEntity::getId,id)
              .set(SysCodeEntity::getNum,num + 1)
              .set(SysCodeEntity::getUpdateTime, LocalDateTime.now())
              .set(SysCodeEntity::getUpdateUserId, loginUser.getUid())
              .set(SysCodeEntity::getUpdateUserName, loginUser.getUserName())
              .update();
    }

}
