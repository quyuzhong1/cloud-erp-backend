package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolSocialMediaEntity;
import com.erp.server.oms.mapper.KolSocialMediaMapper;
import com.erp.server.oms.service.KolSocialMediaService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolSocialMediaDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import cn.hutool.crypto.digest.DigestUtil;
import com.erp.model.oms.enums.KolSocialMediaTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
/**
 * <p>
 * 达人社媒数据表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolSocialMediaServiceImpl extends SuperServiceImpl<KolSocialMediaMapper, KolSocialMediaEntity> implements KolSocialMediaService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolSocialMediaDTO.AddDTO addDTO) {
        KolSocialMediaEntity kolSocialMediaEntity = new KolSocialMediaEntity();
        BeanMapperUtils.copy(addDTO, kolSocialMediaEntity);

        // 数据处理
        handleData(kolSocialMediaEntity);

        log.info("开始新增达人社媒数据单");
        boolean save = super.save(kolSocialMediaEntity);
        if(!save) {
            throw new ServiceException("达人社媒数据单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "达人社媒数据单" , kolSocialMediaEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_SOCIAL_MEDIA.getCode(), kolSocialMediaEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(kolSocialMediaEntity.getId(), kolSocialMediaEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolSocialMediaDTO.UpdateDTO addOrUpdateDTO) {
        KolSocialMediaEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "达人社媒数据单"));
        KolSocialMediaEntity kolSocialMediaEntity =  BeanMapperUtils.map(KolSocialMediaEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolSocialMediaEntity);
        log.info("编辑 开始修改达人社媒数据单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolSocialMediaEntity);
        if(!save) {
            throw new ServiceException("达人社媒数据单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录达人社媒数据单日志数据，id：【{}】", kolSocialMediaEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolSocialMediaEntity.getId(), "达人社媒数据单");
        operateLogService.addModuleOperateLogByObj(old, kolSocialMediaEntity, ModuleTypeEnum.KOL_SOCIAL_MEDIA.getCode(), kolSocialMediaEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolSocialMediaEntity kolSocialMediaEntity) {
        // type 枚举校验（如果为空，可以设置默认值，或者保持为空）
        if (StrUtil.isNotBlank(kolSocialMediaEntity.getType())) {
            KolSocialMediaTypeEnum typeEnum = KolSocialMediaTypeEnum.getByCode(kolSocialMediaEntity.getType());
            if (typeEnum == null) {
                throw new ServiceException("类型【" + kolSocialMediaEntity.getType() + "】不存在，请使用 manual（手动）或 auto（自动）");
            }
        }else {
            kolSocialMediaEntity.setType(KolSocialMediaTypeEnum.MANUAL.getCode());
        }

        // 第三平台如果为空，默认填 "erp"
        if (StrUtil.isBlank(kolSocialMediaEntity.getThirdPlatform())) {
            kolSocialMediaEntity.setThirdPlatform("erp");
        }

        // urlHash 用 hutool hash 工具（如果 url 不为空）
        if (StrUtil.isNotBlank(kolSocialMediaEntity.getUrl())) {
            String urlHash = DigestUtil.md5Hex(kolSocialMediaEntity.getUrl());
            kolSocialMediaEntity.setUrlHash(urlHash);
        }

        // 入库时间填当前时间戳（毫秒）
        if (kolSocialMediaEntity.getInsertTimestamp() == null) {
            kolSocialMediaEntity.setInsertTimestamp(System.currentTimeMillis());
        }

        // uniqueKey 填 urlHash
        if (StrUtil.isNotBlank(kolSocialMediaEntity.getUrlHash())) {
            kolSocialMediaEntity.setUniqueKey(kolSocialMediaEntity.getUrlHash());
        }
    }
}
