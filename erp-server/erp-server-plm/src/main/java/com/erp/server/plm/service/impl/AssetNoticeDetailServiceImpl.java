package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.AssetNoticeDetailEntity;
import com.erp.server.plm.mapper.AssetNoticeDetailMapper;
import com.erp.server.plm.service.AssetNoticeDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.AssetNoticeDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AssetNoticeDetailServiceImpl extends SuperServiceImpl<AssetNoticeDetailMapper, AssetNoticeDetailEntity> implements AssetNoticeDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetNoticeDetailDTO.AddDTO addDTO) {
        AssetNoticeDetailEntity assetNoticeDetailEntity = new AssetNoticeDetailEntity();
        BeanMapperUtils.copy(addDTO, assetNoticeDetailEntity);

        // 数据处理
        handleData(assetNoticeDetailEntity);

        log.info("开始新增");
        boolean save = super.save(assetNoticeDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , assetNoticeDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, null, assetNoticeDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetNoticeDetailEntity.getId(), assetNoticeDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetNoticeDetailDTO.UpdateDTO addOrUpdateDTO) {
        AssetNoticeDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        AssetNoticeDetailEntity assetNoticeDetailEntity =  BeanMapperUtils.map(AssetNoticeDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetNoticeDetailEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(assetNoticeDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", assetNoticeDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetNoticeDetailEntity.getId(), "");
        operateLogService.addSysLogByUpdate(old, assetNoticeDetailEntity, null, assetNoticeDetailEntity.getId(),"", msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<AssetNoticeDetailDTO.AddDTO> detailList, String assetNoticeId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<AssetNoticeDetailEntity> assetNoticeDetailEntities = new ArrayList<>();
        for (AssetNoticeDetailDTO.AddDTO addDTO : detailList) {
            AssetNoticeDetailEntity assetNoticeDetailEntity = new AssetNoticeDetailEntity();
            BeanMapperUtils.copy(addDTO, assetNoticeDetailEntity);
            assetNoticeDetailEntity.setMainId(assetNoticeId);
            assetNoticeDetailEntity.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
            assetNoticeDetailEntities.add(assetNoticeDetailEntity);
        }
        super.saveBatch(assetNoticeDetailEntities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<AssetNoticeDetailDTO.UpdateDTO> detailList, String assetNoticeId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<AssetNoticeDetailEntity> assetNoticeDetailEntities = new ArrayList<>();
        for (AssetNoticeDetailDTO.UpdateDTO updateDTO : detailList) {
            AssetNoticeDetailEntity assetNoticeDetailEntity = new AssetNoticeDetailEntity();
            BeanMapperUtils.copy(updateDTO, assetNoticeDetailEntity);
            assetNoticeDetailEntity.setMainId(assetNoticeId);
            assetNoticeDetailEntities.add(assetNoticeDetailEntity);
        }
        super.updateBatchById(assetNoticeDetailEntities);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetNoticeDetailEntity assetNoticeDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
