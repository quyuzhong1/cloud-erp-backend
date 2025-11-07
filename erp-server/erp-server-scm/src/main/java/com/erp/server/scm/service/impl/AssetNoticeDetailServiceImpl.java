package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.scm.mapper.AssetNoticeDetailMapper;
import com.erp.server.scm.service.AssetNoticeDetailService;
import com.erp.server.scm.service.ModuleOperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.AssetNoticeDetailEntity;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.AssetNoticeDetailDTO;
import java.util.*;
import java.util.stream.Collectors;
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
    private ModuleOperateLogService moduleOperateLogService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

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
        moduleOperateLogService.addModuleOperateLog(msg, null, assetNoticeDetailEntity.getId(), "新增操作");
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
        moduleOperateLogService.addModuleOperateLogByObj(old, assetNoticeDetailEntity, null, assetNoticeDetailEntity.getId(),"", msg);
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
        if (detailList == null) {
            detailList = new ArrayList<>();
        }

        //查询旧数据
        List<AssetNoticeDetailEntity> oldList = this.lambdaQuery()
                .eq(AssetNoticeDetailEntity::getMainId, assetNoticeId)
                .list();

        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            Set<String> deleteIdSet = new HashSet<>(deleteIds);
            List<AssetNoticeDetailEntity> removeList = oldList.stream()
                    .filter(obj -> deleteIdSet.contains(obj.getId()))
                    .collect(Collectors.toList());

            List<Pair<String, String>> pairList = removeList.stream()
                    .map(obj -> new Pair<>(obj.getAssetId(), obj.getMainId()))
                    .collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog(
                    "模具通知单删除了一个SKU【%s】",
                    ModuleTypeEnum.ASSET_NOTICE.getCode(),
                    pairList,
                    "编辑操作"
            );

            this.removeByIds(deleteIds);
        }

        List<AssetNoticeDetailEntity> newList = BeanMapperUtils.copyList(AssetNoticeDetailEntity.class, detailList);
        for (AssetNoticeDetailEntity assetNoticeDetailEntity : newList) {
            //关联开模通知单id
            if (StringUtils.isBlank(assetNoticeDetailEntity.getCreatePoType())) {
                assetNoticeDetailEntity.setMainId(assetNoticeId);
            }

            //采购关联状态
            if (StringUtils.isBlank(assetNoticeDetailEntity.getCreatePoType())) {
                assetNoticeDetailEntity.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
            }
        }
        this.saveOrUpdateBatch(newList);

        // 更新 SKU 占用状态
        List<String> skuIds = newList.stream()
                .map(AssetNoticeDetailEntity::getAssetId)
                .distinct()
                .collect(Collectors.toList());
        try {
            plmTaskFeign.updateOccupyStatus(skuIds);
        } catch (Exception e) {
            log.warn("更新 SKU 占用状态失败，skuIds={}", skuIds, e);
            throw new ServiceException(ApiError.ERROR_95322,skuIds);
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<AssetNoticeDetailDTO.UpdateDTO> newList, List<AssetNoticeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).map(obj -> obj.getId()).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(obj -> obj.getId()).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetNoticeDetailEntity assetNoticeDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
