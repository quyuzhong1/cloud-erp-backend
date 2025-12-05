package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.TransferInDetailDTO;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.model.wms.entity.TransferOutDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.TransferInDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.TransferInDetailService;
import com.erp.server.wms.service.TransferOutDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 分布式调入单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferInDetailServiceImpl extends SuperServiceImpl<TransferInDetailMapper, TransferInDetailEntity> implements TransferInDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TransferOutDetailService transferOutDetailService;

    /**
     * 添加明细
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-26 15:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String mainId, List<TransferInDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<TransferInDetailEntity> addDetailList = BeanMapper.copyList(detailList, TransferInDetailEntity.class);
        addDetailList.stream().forEach(a -> a.setMainId(mainId));
        this.saveBatch(addDetailList);
    }


    /**
     * 删除明细
     *
     * @param mainIds
     * @return void
     * @author yl
     * @date 2023-05-29 8:53
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMainIdList(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return;
        }
        LambdaQueryWrapper<TransferInDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TransferInDetailEntity::getMainId, mainIds);
        this.remove(queryWrapper);
    }


    /**
     * 根据主表id 获取到详情
     *
     * @param mainId 主表id
     * @return java.util.List<com.erp.model.wms.dto.TransferInDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-29 11:45
     */
    @Override
    public List<TransferInDetailDTO.ViewDTO> listByMainId(String mainId) {
        List<TransferInDetailEntity> dbList = this.listDbByMainId(mainId);
        List<TransferInDetailDTO.ViewDTO> list = BeanMapper.copyList(dbList, TransferInDetailDTO.ViewDTO.class);
        List<String> skuIdList = list.stream().map(TransferInDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (TransferInDetailDTO.ViewDTO item : list) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                item.setProductName(skuVO.getSkuName());
                item.setVariantProperty(skuVO.getVariantProperty());
                item.setUnit(skuVO.getUnitName());
                item.setUnitName(skuVO.getUnitName());
            } else {
                item.setProductName("");
                item.setVariantProperty("");
                item.setUnit("");
                item.setUnitName("");
            }
        }
        return list;
    }

    @Override
    public List<TransferInDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TransferInDetailEntity::getMainId,mainIdList).list();
    }


    /**
     * 更改详情
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-29 14:11
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetailList(String mainId, List<TransferInDetailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<TransferInDetailEntity> saveOrUpdateList = new ArrayList<>(detailList.size());
        //这是修改的
        List<TransferInDetailDTO.UpdateDTO> updateList = detailList.stream().filter(c -> CharSequenceUtil.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<TransferInDetailDTO.UpdateDTO> addList = detailList.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<TransferInDetailEntity> updateEntityList = BeanMapper.copyList(updateList, TransferInDetailEntity.class);
        //这个是要添加的
        List<TransferInDetailEntity> addEntityList = BeanMapper.copyList(addList, TransferInDetailEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<TransferInDetailEntity> dbList = this.listDbByMainId(mainId);
        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        List<TransferInDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> skuIdList = detailList.stream().map(TransferInDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (TransferInDetailEntity item : saveOrUpdateList) {
            item.setMainId(mainId);
            String skuId = item.getSkuId();
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            item.setSkuNo(skuNo);
        }
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个分布式调入产品【%s】", ModuleTypeEnum.TRANSFER_IN.getCode(), removePairList, "编辑操作");
        //这是添加
        List<Pair<String, String>> addPairList = saveOrUpdateList.stream().filter(s -> CharSequenceUtil.isBlank(s.getId())).map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个分布式调入产品【%s】", ModuleTypeEnum.TRANSFER_IN.getCode(), addPairList, "编辑操作");
        //修改的
        updateEntityList = saveOrUpdateList.stream().filter(s -> CharSequenceUtil.isNotBlank(s.getId())).collect(Collectors.toList());
        for (TransferInDetailEntity update : updateEntityList) {
            String id = update.getId();
            TransferInDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.TRANSFER_IN.getCode(), mainId, "", "");
            }
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }

    @Override
    public List<TransferInDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
        return this.baseMapper.listSourceDetailIds(sourceDetailIds);
    }


    /**
     * 检查数量
     *
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-06-01 16:26
     */
    @Override
    public void checkQty(List<TransferInDetailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<String> sourceDetailIds = detailList.stream().map(TransferInDetailDTO.UpdateDTO::getSourceDetailId).collect(Collectors.toList());
        List<TransferOutDetailEntity> transferOutDetailList = transferOutDetailService.listByIds(sourceDetailIds);
        List<TransferInDetailDTO.QtyDTO> transferInDetailList = baseMapper.listByDetailIds(sourceDetailIds);
        for (TransferInDetailDTO.UpdateDTO item : detailList) {
            String id = item.getId();
            String sourceDetailId = item.getSourceDetailId();
            //计划调入数量
            Integer planQty = item.getPlanQty();
            //途损数量
            Integer transitDamageQty = item.getTransitDamageQty();
            //调入数量
            Integer qty = item.getQty();
            if (transitDamageQty + qty > planQty) {
                throw new ServiceException(ApiError.ERROR_99069);
            }
            //调出的数量
            int outQty = transferOutDetailList.stream().filter(o -> o.getId().equals(sourceDetailId)).
                    map(TransferOutDetailEntity::getQty).findFirst().orElse(0);
            int alreadyInQty = transferInDetailList.stream().filter(i -> i.getSourceDetailId().equals(sourceDetailId) && !i.getId().equals(id)).
                    mapToInt(TransferInDetailDTO.QtyDTO::getPlanQty).sum();
            if (alreadyInQty + planQty > outQty) {
                throw new ServiceException(ApiError.ERROR_99065);
            }

        }

    }

    @Override
    public void updateKingdeeDetailId(JSONArray list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            String detailId = (String) jsonObject.get("detailId");
            String kingdeeDetailId = (String) jsonObject.get("kingdeeDetailId");
            this.lambdaUpdate()
                    .set(TransferInDetailEntity::getKingdeeDetailId, kingdeeDetailId)
                    .eq(TransferInDetailEntity::getId, detailId)
                    .update();
        }
    }

    /**
     * 获取到删除的数据
     *
     * @param pairList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-25 12:07
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<TransferInDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(TransferInDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }

    private List<TransferInDetailEntity> listDbByMainId(String mainId) {
        return this.lambdaQuery().eq(TransferInDetailEntity::getMainId, mainId).list();
    }
}
