package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.Md5Util;
import com.common.core.utils.MessageUtils;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictPartitionEntity;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.erp.model.wms.entity.VirtualWarehouseChannelPartitionRefEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.server.wms.mapper.VirtualWarehouseChannelMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 虚拟仓渠道 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@Service
public class VirtualWarehouseChannelServiceImpl extends SuperServiceImpl<VirtualWarehouseChannelMapper, VirtualWarehouseChannelEntity> implements VirtualWarehouseChannelService {
    private static final String VM_CHANNEL_SKIP_CHECK_PLATFORM = "vmChannelSkipCheckPlatform";

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;
    @Resource
    private VirtualWarehouseChannelPartitionRefService virtualWarehouseChannelPartitionRefService;
    @Resource
    private VirtualWarehouseService virtualWarehouseService;
    @Resource
    private CustomerFeign customerFeign;
    @Resource
    private DictBasicService dictBasicService;

    /**
     * 批量新增
     *
     * @param batchUpdateDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO batchUpdate(VirtualWarehouseChannelDTO.BatchUpdateDTO batchUpdateDTO) {
        String virtualWarehouseId = batchUpdateDTO.getVirtualWarehouseId();
        VirtualWarehouseEntity warehouseEntity = virtualWarehouseService.getById(virtualWarehouseId);
        VirtualWarehouseEntity oldWarehouseEntity = Optional.ofNullable(warehouseEntity).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "虚拟仓"));
        log.info("更新虚拟仓数据，id：【{}】", oldWarehouseEntity.getId());
        //渠道配置
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> internalChannelList = CollUtil.isNotEmpty(batchUpdateDTO.getInternalChannelList())? batchUpdateDTO.getInternalChannelList() :Collections.emptyList();
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> overseasChannelList = CollUtil.isNotEmpty(batchUpdateDTO.getOverseasChannelList())? batchUpdateDTO.getOverseasChannelList() :Collections.emptyList();
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> otherChannelList = CollUtil.isNotEmpty(batchUpdateDTO.getOtherChannelList())? batchUpdateDTO.getOtherChannelList() :Collections.emptyList();
        //合并数据
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> allChannelList = Stream.concat(Stream.concat(internalChannelList.stream(), overseasChannelList.stream()), otherChannelList.stream()).collect(Collectors.toList());
        //校验数据 配置内数据重复校验
        checkData(allChannelList);
        //查找原有绑定关系
        List<VirtualWarehouseChannelEntity> existChannelList = baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseChannelEntity>()
                .eq(VirtualWarehouseChannelEntity::getVirtualWarehouseId, batchUpdateDTO.getVirtualWarehouseId()));
        List<String> ids = existChannelList.stream().map(VirtualWarehouseChannelEntity::getId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseChannelPartitionRefEntity> existRefList = virtualWarehouseChannelPartitionRefService.listByMainIds(ids);
        if (CollectionUtils.isNotEmpty(existChannelList)) {
            List<String> oldChannelIds = existChannelList.stream().map(VirtualWarehouseChannelEntity::getId).collect(Collectors.toList());
            //删除原始数据
            baseMapper.deleteBatchIds(oldChannelIds);
            //删除渠道-分区关系记录
            virtualWarehouseChannelPartitionRefService.deleteByMainIds(oldChannelIds);
        }

        //新增数据
        if (CollectionUtils.isNotEmpty(allChannelList)) {
            List<VirtualWarehouseChannelEntity> batchSaveDTOList = handleData(batchUpdateDTO, allChannelList);
            checkSameWarehouseB2bForeignPlatform(virtualWarehouseId, batchSaveDTOList);
            //检查已启用虚拟仓是否存在重合配置 多虚拟仓校验
            if (Objects.nonNull(warehouseEntity.getDisabled()) && Boolean.FALSE.equals(warehouseEntity.getDisabled())){
                checkBoundChannel(batchSaveDTOList, Boolean.TRUE);
            }
            if (CollectionUtils.isNotEmpty(batchSaveDTOList)) {
                batchSaveDTOList.forEach(entity ->{
                    List<String> partitionIds = entity.getPartitionIds();
                    boolean save = this.save(entity);
                    if (!save){
                        throw new ServiceException("保存虚拟仓渠道设置异常");
                    }
                    if (CollUtil.isNotEmpty(partitionIds)){
                        virtualWarehouseChannelPartitionRefService.saveByMainId(entity.getId(),partitionIds);
                    }
                });
            }
        }
        //添加日志
        addOperateLog(allChannelList,existChannelList,existRefList,virtualWarehouseId);

        return new BaseResultDTO.AddDTO();
    }

    /**
     * 校验数据
     * @param allChannelList
     */
    private void checkData(List<VirtualWarehouseChannelDTO.ChannelAddDTO> allChannelList) {
        if (CollUtil.isEmpty(allChannelList)){
            return;
        }
        for (VirtualWarehouseChannelDTO.ChannelAddDTO channelAddDTO : allChannelList){
            List<VirtualWarehouseChannelDTO.DetailDTO> detailDTOList = channelAddDTO.getDetailDTOList();
            if (CollUtil.isEmpty(detailDTOList)){
                continue;
            }
            //是否存在空店铺
            List<String> shopIds1 = detailDTOList.stream().map(VirtualWarehouseChannelDTO.DetailDTO::getShopIdList).flatMap(List::stream).collect(Collectors.toList());
            List<String> shopIds2 = shopIds1.stream().distinct().collect(Collectors.toList());
            if (shopIds2.size() != shopIds1.size()){
                //存在重复店铺，
                throw new ServiceException(CharSequenceUtil.format("平台【{}】渠道配置中存在重复店铺",channelAddDTO.getDictPlatform()));
            }
            //是否存在空店铺 + 有店铺数据
            boolean hasEmptyShop = Boolean.FALSE;
            boolean hasNotEmptyShop = Boolean.FALSE;
            for(VirtualWarehouseChannelDTO.DetailDTO detailDTO : detailDTOList){
                if (CollUtil.isEmpty(detailDTO.getShopIdList())){
                    detailDTO.setShopMd5(Md5Util.md5(CharSequenceUtil.EMPTY));
                    hasEmptyShop = Boolean.TRUE;
                }else {
                    detailDTO.setShopMd5(detailDTO.getShopIdList().stream().sorted().collect(Collectors.joining(",")));
                    hasNotEmptyShop = Boolean.TRUE;
                }
            }
            if (hasEmptyShop && hasNotEmptyShop){
                throw new ServiceException(CharSequenceUtil.format("平台【{}】渠道配置中店铺存在交集",channelAddDTO.getDictPlatform()));
            }
            //相同店铺配置
            Map<String, List<VirtualWarehouseChannelDTO.DetailDTO>> shopMap = detailDTOList.stream().collect(Collectors.groupingBy(VirtualWarehouseChannelDTO.DetailDTO::getShopMd5));
            for (String shopMd5 : shopMap.keySet()){
                List<VirtualWarehouseChannelDTO.DetailDTO> detailDTOS = shopMap.get(shopMd5);
                if (CollUtil.isEmpty(detailDTOS)){
                    continue;
                }
                //是否存在空分区
                List<String> partitionIds1 = detailDTOList.stream().map(VirtualWarehouseChannelDTO.DetailDTO::getShopIdList).flatMap(List::stream).collect(Collectors.toList());
                List<String> partitionIds2 = partitionIds1.stream().distinct().collect(Collectors.toList());
                if (partitionIds1.size() != partitionIds2.size()){
                    //存在重复店铺，
                    throw new ServiceException(CharSequenceUtil.format("平台【{}】渠道配置中存在重复军区",channelAddDTO.getDictPlatform()));
                }
                //是否存在空店铺 + 有店铺数据
                boolean hasEmptyPartition = Boolean.FALSE;
                boolean hasNotEmptyPartition = Boolean.FALSE;
                for(VirtualWarehouseChannelDTO.DetailDTO detailDTO : detailDTOS){
                    if (CollUtil.isEmpty(detailDTO.getPartitonIdList())){
                        hasEmptyPartition = Boolean.TRUE;
                    }else {
                        hasNotEmptyPartition = Boolean.TRUE;
                    }
                }
                if (hasEmptyPartition && hasNotEmptyPartition){
                    throw new ServiceException(CharSequenceUtil.format("平台【{}】渠道配置中军区存在交集",channelAddDTO.getDictPlatform()));
                }
            }
        }
    }

    /**
     * 添加日志
     *
     * @param allChannelList
     * @param existChannelList
     * @param existRefList
     * @param virtualWarehouseId
     * @author will
     * @date 2024/7/18 14:49
     */
    private void addOperateLog(List<VirtualWarehouseChannelDTO.ChannelAddDTO> allChannelList, List<VirtualWarehouseChannelEntity> existChannelList, List<VirtualWarehouseChannelPartitionRefEntity> existRefList, String virtualWarehouseId) {
        Boolean isChange = Boolean.FALSE;
        List<String> oldChannelMsg = new ArrayList<>();
        List<String> newChannelMsg = new ArrayList<>();
        //店铺列表
        List<ShopInfoEntity> shopList = FeignQuery.list(ShopInfoEntity.class);
        //分区列表
        List<DictPartitionEntity> partitionEntityList = FeignQuery.list(DictPartitionEntity.class);

        //平台信息
        List<DictBasicEntity> platformList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        for (VirtualWarehouseChannelDTO.ChannelAddDTO channelAddDT : allChannelList) {
            List<VirtualWarehouseChannelDTO.DetailDTO> detailDTOList = channelAddDT.getDetailDTOList();
            for (VirtualWarehouseChannelDTO.DetailDTO detailDTO : detailDTOList){
                List<String> shopIds = detailDTO.getShopIdList().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
                List<String> partitionIds = detailDTO.getPartitonIdList().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
                String shopName = CollUtil.isEmpty(shopIds) ? "全部" : shopList.stream().filter(obj -> shopIds.contains(obj.getId())).map(ShopInfoEntity::getName)
                        .distinct().sorted().collect(Collectors.joining(",")) ;
                String partitionName = CollUtil.isEmpty(partitionIds) ? "全部" : partitionEntityList.stream().filter(obj -> partitionIds.contains(obj.getId())).map(DictPartitionEntity::getName)
                        .distinct().sorted().collect(Collectors.joining(",")) ;
                String msg;
                //平台名称
                String platformName = platformList.stream().filter(obj -> CharSequenceUtil.equals(obj.getValue(), channelAddDT.getDictPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
                msg = CharSequenceUtil.format("{},按店铺({}),军区({})", platformName, shopName, partitionName);
                oldChannelMsg.add(msg);
            }
        }
        //修改前数据
        Map<String, List<VirtualWarehouseChannelEntity>> map = existChannelList.stream().collect(Collectors.groupingBy(VirtualWarehouseChannelEntity::getDictPlatform));
        for (Map.Entry<String, List<VirtualWarehouseChannelEntity>> entry : map.entrySet()) {
            List<VirtualWarehouseChannelEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)){
                continue;
            }
            //按照分区进行分组
            setPartitionIds(value, existRefList);
            Map<String, List<VirtualWarehouseChannelEntity>> partitiomMd5Map = value.stream().collect(Collectors.groupingBy(VirtualWarehouseChannelEntity::getPartitionIdMd5));
            for (String partitionMd5 : partitiomMd5Map.keySet()){
                List<VirtualWarehouseChannelEntity> virtualWarehouseChannelEntityList = partitiomMd5Map.get(partitionMd5);
                List<String> shopIds = virtualWarehouseChannelEntityList.stream().map(VirtualWarehouseChannelEntity::getRelationId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
                String shopName = CollUtil.isEmpty(shopIds) ? "全部" : shopList.stream().filter(obj -> shopIds.contains(obj.getId())).map(ShopInfoEntity::getName)
                        .distinct().sorted().collect(Collectors.joining(",")) ;
                List<String> partitionIds = virtualWarehouseChannelEntityList.get(0).getPartitionIds().stream().filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
                String partitionName = CollUtil.isEmpty(partitionIds) ? "全部" : partitionEntityList.stream().filter(obj -> partitionIds.contains(obj.getId())).map(DictPartitionEntity::getName)
                        .distinct().sorted().collect(Collectors.joining(",")) ;
                String msg ;
                //平台名称
                String platformName = platformList.stream().filter(obj -> CharSequenceUtil.equals(obj.getValue(), value.get(0).getDictPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
                msg = CharSequenceUtil.format("{},按店铺({}),军区({})",platformName,shopName,partitionName);
                newChannelMsg.add(msg);
                if (!oldChannelMsg.contains(msg)) {
                    isChange =  Boolean.TRUE;
                }
            }
        }
        //size不一致或者有变更
        if (oldChannelMsg.size() != newChannelMsg.size() || isChange) {
            // 操作日志
            String msg = CharSequenceUtil.format("关联渠道：从<br>【{}】<br>修改为<br>【{}】",StrUtil.join(";<br>",CollUtil.isEmpty(newChannelMsg) ? "无配置" : newChannelMsg.stream().sorted().collect(Collectors.toList())),
                    StrUtil.join(";<br>",CollUtil.isEmpty(oldChannelMsg) ? "无配置" : oldChannelMsg.stream().sorted().collect(Collectors.toList())));
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), virtualWarehouseId, "编辑信息");
        }
    }

    /**
     * 封装数据
     *
     * @param batchUpdateDTO
     * @param dtoList
     * @return
     */
    private static List<VirtualWarehouseChannelEntity> handleData(VirtualWarehouseChannelDTO.BatchUpdateDTO batchUpdateDTO, List<VirtualWarehouseChannelDTO.ChannelAddDTO> dtoList) {
        List<VirtualWarehouseChannelEntity> batchSaveDTOList = new ArrayList<>();
        String virtualWarehouseId = batchUpdateDTO.getVirtualWarehouseId();
        //新增数据
        dtoList.forEach(dto -> dto.getDetailDTOList().forEach(detailDTO -> {
            List<String> shopIds = CollUtil.isNotEmpty(detailDTO.getShopIdList()) ? detailDTO.getShopIdList().stream().filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()) : Collections.emptyList();
            if (CollUtil.isEmpty(shopIds)){
                //按平台
                VirtualWarehouseChannelEntity channelEntity = new VirtualWarehouseChannelEntity()
                        .setVirtualWarehouseId(virtualWarehouseId)
                        .setType(VitualWarehouseChannelTypeEnum.PLATFORM.getCode())
                        .setDictPlatform(dto.getDictPlatform())
                        .setRelationId(CharSequenceUtil.EMPTY)
                        .setPartitionIds(detailDTO.getPartitonIdList());
                batchSaveDTOList.add(channelEntity);
            }else {
                //按店铺
                shopIds.forEach(shopId -> {
                    VirtualWarehouseChannelEntity channelEntity = new VirtualWarehouseChannelEntity()
                            .setVirtualWarehouseId(virtualWarehouseId)
                            .setType(VitualWarehouseChannelTypeEnum.SHOP.getCode())
                            .setDictPlatform(dto.getDictPlatform())
                            .setRelationId(shopId)
                            .setPartitionIds(detailDTO.getPartitonIdList());
                    batchSaveDTOList.add(channelEntity);
                });

            }
        }));
        return batchSaveDTOList;
    }

    /**
     * 通过虚拟仓id获取关联渠道
     *
     * @param virtualWarehouseId
     */
    @Override
    public List<VirtualWarehouseChannelEntity> getByVirtualWarehouseId(String virtualWarehouseId) {
        return baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseChannelEntity>()
                .eq(VirtualWarehouseChannelEntity::getVirtualWarehouseId, virtualWarehouseId).orderByAsc(VirtualWarehouseChannelEntity::getId));
    }

    /**
     * 获取所有绑定的平台（聚合）
     */
    @Override
    public List<VirtualWarehouseDTO.BindChannelDto> getBindedDictPlatform() {
        return baseMapper.getBindedDictPlatform();
    }

    /**
     * 获取所有绑定的平台（不聚合）
     */
    @Override
    public List<VirtualWarehouseDTO.BindChannelDto> getBindedDictPlatformNoGroup() {
        return baseMapper.getBindedDictPlatformNoGroup();
    }

    /**
     * 获取当前渠道绑定的店铺
     *
     * @param dictPlatform
     * @return
     */
    @Override
    public List<String> getBindedShopByDictPlatform(String dictPlatform) {
        return baseMapper.getBindedShopByDictPlatform(dictPlatform);
    }

    @Override
    public List<VirtualWarehouseRelationEntity> getVirtualWarehouse(VirtualWarehouseChannelDTO.PlatformDTO platformDTO) {
        VirtualWarehouseChannelEntity channelEntity = this.getByPlatform(platformDTO);
        if (ObjectUtil.isEmpty(channelEntity)) {
            return Collections.emptyList();
        }
        List<VirtualWarehouseRelationEntity> warehouseEntityList = virtualWarehouseRelationService.listByWarehouseIdList(platformDTO.getWarehouseIdList(), Collections.singletonList(channelEntity.getVirtualWarehouseId()));
        if (ObjectUtil.isEmpty(warehouseEntityList)) {
            return Collections.emptyList();
        }
        return warehouseEntityList;
    }

    @Override
    public List<VirtualWarehouseRelationDTO.ListPlatformDTO> listVirtualWarehouseByPlatform(VirtualWarehouseChannelDTO.ListPlatformDTO listPlatformDTO) {
        List<VirtualWarehouseChannelEntity> channelEntityList = baseMapper.listVirtualWarehouseByPlatform(listPlatformDTO);
        if (CollectionUtils.isEmpty(channelEntityList)) {
            return Collections.emptyList();
        }
        List<String> virtualWarehouseIdList = channelEntityList.stream().map(VirtualWarehouseChannelEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseRelationEntity> warehouseEntityList = virtualWarehouseRelationService.listByWarehouseIdList(listPlatformDTO.getWarehouseIdList(), virtualWarehouseIdList);
        if (ObjectUtil.isEmpty(warehouseEntityList)) {
            return Collections.emptyList();
        }
        List<VirtualWarehouseRelationDTO.ListPlatformDTO> resultList = new ArrayList<>();
        for (VirtualWarehouseRelationEntity entity : warehouseEntityList) {
            VirtualWarehouseRelationDTO.ListPlatformDTO platformDTO = new VirtualWarehouseRelationDTO.ListPlatformDTO();
            platformDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            platformDTO.setWarehouseId(entity.getWarehouseId());
            String dictPlatform = channelEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId()))
                    .map(VirtualWarehouseChannelEntity::getDictPlatform).findFirst().orElse("");
            platformDTO.setDictPlatform(dictPlatform);
            List<String> relationIdList = channelEntityList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getRelationId()) && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId()))
                    .map(VirtualWarehouseChannelEntity::getRelationId).distinct().collect(Collectors.toList());
            platformDTO.setRelationIdList(relationIdList);
            List<String> partitionIdList = channelEntityList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getPartitionId()) && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId()))
                    .map(VirtualWarehouseChannelEntity::getPartitionId).distinct().collect(Collectors.toList());
            platformDTO.setPartitionIds(partitionIdList);
            resultList.add(platformDTO);
        }
        return resultList;
    }

    @Override
    public List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> listCfgRuleVirtualWarehouse(List<String> platformList) {
        List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(platformList)) {
            return resultList;
        }
        //根据平台查询
        VirtualWarehouseChannelDTO.ListPlatformDTO listPlatformDTO = new VirtualWarehouseChannelDTO.ListPlatformDTO();
        listPlatformDTO.setDictPlatformList(platformList);
        List<VirtualWarehouseChannelEntity> virtualWarehouseChannelList = baseMapper.listVirtualWarehouseByPlatform(listPlatformDTO);
        if (CollectionUtils.isEmpty(virtualWarehouseChannelList)) {
            return resultList;
        }
        List<String> virtualWarehouseIdList = virtualWarehouseChannelList.stream().map(VirtualWarehouseChannelEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseRelationEntity> virtualWarehouseRelationList = virtualWarehouseRelationService.listByVirtualWarehouseIdList(virtualWarehouseIdList);
        if (CollectionUtils.isEmpty(virtualWarehouseRelationList)) {
            return resultList;
        }
        Map<String, List<VirtualWarehouseChannelEntity>> map = virtualWarehouseChannelList.stream().collect(Collectors.groupingBy(obj -> obj.getVirtualWarehouseId().concat(obj.getDictPlatform()).concat(obj.getRelationId())));
        for (Map.Entry<String, List<VirtualWarehouseChannelEntity>> entry : map.entrySet()) {
            List<VirtualWarehouseChannelEntity> value = entry.getValue();
            //实体仓库
            List<String> warehouseIdList = virtualWarehouseRelationList.stream().filter(obj -> CharSequenceUtil.equals(obj.getVirtualWarehouseId(), value.get(0).getVirtualWarehouseId())).map(VirtualWarehouseRelationEntity::getWarehouseId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(warehouseIdList)) {
                continue;
            }
            for (String warehouseId : warehouseIdList) {
                VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO resultDTO = new VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO();
                resultDTO.setWarehouseId(warehouseId);
                resultDTO.setVirtualWarehouseId(value.get(0).getVirtualWarehouseId());
                resultDTO.setDictPlatform(value.get(0).getDictPlatform());
                resultDTO.setType(value.get(0).getType());
                //店铺id
                if (CharSequenceUtil.equals(VitualWarehouseChannelTypeEnum.SHOP.getCode(),value.get(0).getType())) {
                    List<String> relationIdList = value.stream().map(VirtualWarehouseChannelEntity::getRelationId).distinct().collect(Collectors.toList());
                    resultDTO.setRelationIdList(relationIdList);
                }
                List<String> partitionIdList = value.stream().map(VirtualWarehouseChannelEntity::getPartitionId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
                resultDTO.setPartitionIdList(partitionIdList);
                resultList.add(resultDTO);
            }
        }
        return resultList;
    }

    @Override
    public VirtualWarehouseChannelDTO.ViewDTO view(String id) {
        VirtualWarehouseEntity warehouseEntity = virtualWarehouseService.getById(id);
        if (Objects.isNull(warehouseEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "虚拟仓");
        }
        VirtualWarehouseChannelDTO.ViewDTO viewDTO = new VirtualWarehouseChannelDTO.ViewDTO();
        viewDTO.setVirtualWarehouseId(id);
        viewDTO.setVirtualWarehouseName(warehouseEntity.getName());
        viewDTO.setVirtualWarehouseCode(warehouseEntity.getCode());
        //获取关联渠道
        List<VirtualWarehouseChannelEntity> vmChannelEntityList = this.getByVirtualWarehouseId(id);
        List<String> channelIds = vmChannelEntityList.stream().map(VirtualWarehouseChannelEntity::getId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseChannelPartitionRefEntity> refEntityList = virtualWarehouseChannelPartitionRefService.listByMainIds(channelIds);
        //平台信息
        List<DictBasicEntity> platformList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        Map<String, List<VirtualWarehouseChannelEntity>> dictPlatformMap = vmChannelEntityList.stream().collect(Collectors.groupingBy(VirtualWarehouseChannelEntity::getDictPlatform));
        //国内 渠道列表
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> internalChannelList = new ArrayList<>();
        //海外 渠道列表
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> overseasChannelList = new ArrayList<>();
        //其他 渠道列表
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> otherChannelList = new ArrayList<>();
        //店铺信息
        List<ShopInfoEntity> shopInfoEntityList = FeignQuery.list(ShopInfoEntity.class);
        List<DictPartitionEntity> dictPartitionEntityList = FeignQuery.list(DictPartitionEntity.class);
        for (String dictPlatform : dictPlatformMap.keySet()){
            DictBasicEntity dictDTO = platformList.stream().filter(e -> Objects.equals(e.getValue(), dictPlatform)).findFirst().orElse(null);
            if (Objects.isNull(dictDTO)){
                continue;
            }
            List<VirtualWarehouseChannelEntity> virtualWarehouseChannelEntities = dictPlatformMap.get(dictPlatform);
            //构建明细
            List<VirtualWarehouseChannelDTO.DetailDTO> detailDTOList = buildChannelDetail(virtualWarehouseChannelEntities,refEntityList,shopInfoEntityList,dictPartitionEntityList);
            if (DictBasicTypeEnum.SALES_PLATFORM_INTERNAL.getType().equals(dictDTO.getSubType())){
                internalChannelList.add(VirtualWarehouseChannelDTO.ChannelAddDTO.builder().dictPlatform(dictPlatform).detailDTOList(detailDTOList).build());
            }else if (DictBasicTypeEnum.SALES_PLATFORM_OVERSEAS.getType().equals(dictDTO.getSubType())){
                overseasChannelList.add(VirtualWarehouseChannelDTO.ChannelAddDTO.builder().dictPlatform(dictPlatform).detailDTOList(detailDTOList).build());
            }else if (DictBasicTypeEnum.SALES_PLATFORM_OTHER.getType().equals(dictDTO.getSubType())){
                otherChannelList.add(VirtualWarehouseChannelDTO.ChannelAddDTO.builder().dictPlatform(dictPlatform).detailDTOList(detailDTOList).build());
            }

        }
        viewDTO.setInternalChannelList(internalChannelList);
        viewDTO.setOverseasChannelList(overseasChannelList);
        viewDTO.setOtherChannelList(otherChannelList);
        return viewDTO;
    }

    private List<VirtualWarehouseChannelDTO.DetailDTO> buildChannelDetail(List<VirtualWarehouseChannelEntity> virtualWarehouseChannelEntities, List<VirtualWarehouseChannelPartitionRefEntity> refEntityList, List<ShopInfoEntity> shopInfoEntityList, List<DictPartitionEntity> dictPartitionEntityList) {
        if (CollUtil.isEmpty(virtualWarehouseChannelEntities)){
            return Collections.emptyList();
        }
        setPartitionIds(virtualWarehouseChannelEntities, refEntityList);
        Map<String, List<VirtualWarehouseChannelEntity>> partitiomMd5Map = virtualWarehouseChannelEntities.stream().collect(Collectors.groupingBy(VirtualWarehouseChannelEntity::getPartitionIdMd5));
        List<VirtualWarehouseChannelDTO.DetailDTO> detailDTOList = new ArrayList<>();
        for (String partitionMd5 : partitiomMd5Map.keySet()){
            List<VirtualWarehouseChannelEntity> channelEntityList = partitiomMd5Map.get(partitionMd5);
            //店铺
            List<String> shopIds = channelEntityList.stream().map(VirtualWarehouseChannelEntity::getRelationId).distinct().sorted().collect(Collectors.toList());
            List<String> shopNameList = shopInfoEntityList.stream().filter(e -> shopIds.contains(e.getId())).map(ShopInfoEntity::getName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            //分区
            List<String> partitionIds = CollUtil.isEmpty(channelEntityList) ? Collections.emptyList() : channelEntityList.get(0).getPartitionIds();
            List<String> partitionNameList = dictPartitionEntityList.stream().filter(e -> partitionIds.contains(e.getId())).map(DictPartitionEntity::getName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());

            detailDTOList.add(VirtualWarehouseChannelDTO.DetailDTO.builder().shopIdList(shopIds).shopNameList(CollUtil.isEmpty(shopNameList) ? Collections.singletonList("全部") : shopNameList)
                    .partitonIdList(partitionIds).partitonNameList(CollUtil.isEmpty(partitionNameList) ? Collections.singletonList("全部") : partitionNameList).build());
        }
        return detailDTOList;
    }

    private void setPartitionIds(List<VirtualWarehouseChannelEntity> virtualWarehouseChannelEntities, List<VirtualWarehouseChannelPartitionRefEntity> refEntityList) {
        virtualWarehouseChannelEntities.forEach(e -> {
            List<String> partitionIds = refEntityList.stream().filter(f -> Objects.equals(e.getId(), f.getMainId())).map(VirtualWarehouseChannelPartitionRefEntity::getPartitionId).distinct().sorted().collect(Collectors.toList());
            e.setPartitionIds(partitionIds);
            e.setPartitionIdMd5(Md5Util.md5(CollUtil.isEmpty(partitionIds)? CharSequenceUtil.EMPTY : String.join(",", partitionIds)));
        });
    }

    /**
     * 根据关联id和平台查询
     *
     * @param platformDTO
     * @return VirtualWarehouseChannelEntity
     * @author will
     * @date 2024/6/12 12:34
     */
    private VirtualWarehouseChannelEntity getByPlatform(VirtualWarehouseChannelDTO.PlatformDTO platformDTO) {
        return baseMapper.getByPlatform(platformDTO);
    }

    /**
     * 校验已绑定的渠道不能重复绑定
     *
     * @param curChannelEntitieList
     * @param hasPartitionIds
     */
    @Override
    public void checkBoundChannel(List<VirtualWarehouseChannelEntity> curChannelEntitieList, Boolean hasPartitionIds) {
        if (CollUtil.isEmpty(curChannelEntitieList)) {
            return;
        }
        //比较数据
        List<VirtualWarehouseDTO.BindChannelDto> curChannelDTO = buildBaseChannelDTO(curChannelEntitieList,hasPartitionIds);
        curChannelDTO = filterAllScopeSkipCheckPlatform(curChannelDTO);
        if (CollUtil.isEmpty(curChannelDTO)) {
            return;
        }
        //获取当前已经绑定的所有渠道
        List<VirtualWarehouseDTO.BindChannelDto> allBindedList = baseMapper.getBindedDictPlatformNoGroup();
        if (CollUtil.isEmpty(allBindedList)) {
            return;
        }
        StringBuilder msg = new StringBuilder();
        boolean contained = areListsMutuallyContained(curChannelDTO, allBindedList, msg);
        if (contained) {
            throw new ServiceException(msg.toString());
        }

    }

    /**
     * 同一实体仓下不同虚拟仓不可重复配置B2B海外线下平台。
     */
    @Override
    public void checkSameWarehouseB2bForeignPlatform(String virtualWarehouseId, List<VirtualWarehouseChannelEntity> curChannelEntitieList) {
        if (CollUtil.isEmpty(curChannelEntitieList)) {
            return;
        }
        String b2bForeignPlatform = PlatformDictEnum.B2B_FOREIGN.getCode();
        boolean hasB2bForeign = curChannelEntitieList.stream()
                .anyMatch(e -> CharSequenceUtil.equals(b2bForeignPlatform, e.getDictPlatform()));
        if (!hasB2bForeign) {
            return;
        }
        List<VirtualWarehouseRelationEntity> relations = virtualWarehouseRelationService.getByVirtualWarehouseId(virtualWarehouseId);
        if (CollUtil.isEmpty(relations)) {
            return;
        }
        List<String> warehouseIds = relations.stream()
                .map(VirtualWarehouseRelationEntity::getWarehouseId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(warehouseIds)) {
            return;
        }
        List<VirtualWarehouseRelationEntity> sameWarehouseRelations = virtualWarehouseRelationService.getByWarehouseId(warehouseIds);
        List<String> otherVirtualWarehouseIds = sameWarehouseRelations.stream()
                .map(VirtualWarehouseRelationEntity::getVirtualWarehouseId)
                .filter(id -> !CharSequenceUtil.equals(id, virtualWarehouseId))
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(otherVirtualWarehouseIds)) {
            return;
        }
        List<VirtualWarehouseChannelEntity> conflictChannelList = baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseChannelEntity>()
                .in(VirtualWarehouseChannelEntity::getVirtualWarehouseId, otherVirtualWarehouseIds)
                .eq(VirtualWarehouseChannelEntity::getDictPlatform, b2bForeignPlatform));
        if (CollUtil.isEmpty(conflictChannelList)) {
            return;
        }
        List<String> conflictVirtualWarehouseIds = conflictChannelList.stream()
                .map(VirtualWarehouseChannelEntity::getVirtualWarehouseId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<VirtualWarehouseEntity> conflictVirtualWarehouses = virtualWarehouseService.listByIds(conflictVirtualWarehouseIds);
        Map<String, String> conflictVmNameMap = conflictVirtualWarehouses.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(VirtualWarehouseEntity::getId, VirtualWarehouseEntity::getName, (left, right) -> left));
        List<WarehouseEntity> warehouseEntityList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIds);
        Map<String, String> warehouseNameMap = warehouseEntityList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (left, right) -> left));
        List<com.erp.model.wms.entity.DictBasicEntity> platformList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        String platformName = platformList.stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getValue(), b2bForeignPlatform))
                .map(com.erp.model.wms.entity.DictBasicEntity::getName)
                .findFirst()
                .orElse(b2bForeignPlatform);
        Set<String> msgSet = new LinkedHashSet<>();
        for (String conflictVirtualWarehouseId : conflictVirtualWarehouseIds) {
            List<String> sharedWarehouseIds = sameWarehouseRelations.stream()
                    .filter(r -> CharSequenceUtil.equals(r.getVirtualWarehouseId(), conflictVirtualWarehouseId))
                    .map(VirtualWarehouseRelationEntity::getWarehouseId)
                    .filter(warehouseIds::contains)
                    .distinct()
                    .collect(Collectors.toList());
            String warehouseName = sharedWarehouseIds.stream()
                    .map(warehouseNameMap::get)
                    .filter(CharSequenceUtil::isNotBlank)
                    .findFirst()
                    .orElse(CharSequenceUtil.EMPTY);
            String conflictVmName = conflictVmNameMap.getOrDefault(conflictVirtualWarehouseId, CharSequenceUtil.EMPTY);
            String format = MessageUtils.getMessage(ApiError.VM_SAME_WAREHOUSE_B2B_FOREIGN_ERROR,
                    warehouseName, conflictVmName, platformName);
            msgSet.add(format);
        }
        throw new ServiceException(String.join("；", msgSet));
    }

    /**
     * 字典配置的平台在店铺和军区均为全部时，跳过重复绑定校验。
     */
    private List<VirtualWarehouseDTO.BindChannelDto> filterAllScopeSkipCheckPlatform(List<VirtualWarehouseDTO.BindChannelDto> curChannelDTO) {
        List<String> skipPlatformList = dictBasicService.getByKey(VM_CHANNEL_SKIP_CHECK_PLATFORM).stream()
                .map(com.erp.model.wms.entity.DictBasicEntity::getValue)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(skipPlatformList)) {
            return curChannelDTO;
        }
        return curChannelDTO.stream()
                .filter(e -> !(skipPlatformList.contains(e.getDictPlatform())
                        && CharSequenceUtil.isBlank(e.getRelationId())
                        && CharSequenceUtil.isBlank(e.getPartitionId())))
                .collect(Collectors.toList());
    }

    /**
     * 构建比较实体参数
     * @param curChannelEntitieList
     * @param hasPartitionIds
     * @return
     */
    private List<VirtualWarehouseDTO.BindChannelDto> buildBaseChannelDTO(List<VirtualWarehouseChannelEntity> curChannelEntitieList, Boolean hasPartitionIds) {
        if (CollUtil.isEmpty(curChannelEntitieList)){
            return Collections.emptyList();
        }
        List<String> ids = curChannelEntitieList.stream().map(VirtualWarehouseChannelEntity::getId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<VirtualWarehouseChannelPartitionRefEntity> refEntityList;
        if (!hasPartitionIds){
            refEntityList = virtualWarehouseChannelPartitionRefService.listByMainIds(ids);
        } else {
            refEntityList = null;
        }
        List<VirtualWarehouseDTO.BindChannelDto> bindChannelDtoList = new ArrayList<>();
        curChannelEntitieList.forEach(e -> {
            List<String> partitionIds = hasPartitionIds ? e.getPartitionIds() : CollUtil.isEmpty(refEntityList) ? Collections.emptyList() :
                    refEntityList.stream().filter(f -> Objects.equals(e.getId(), f.getMainId())).map(VirtualWarehouseChannelPartitionRefEntity::getPartitionId).distinct().collect(Collectors.toList());
            if (CollUtil.isEmpty(partitionIds)){
                bindChannelDtoList.add(VirtualWarehouseDTO.BindChannelDto.builder().virtualWarehouseId(e.getVirtualWarehouseId()).dictPlatform(e.getDictPlatform()).relationId(e.getRelationId()).partitionId(null).build());
            }else {
                partitionIds.forEach(partitionId -> bindChannelDtoList.add(VirtualWarehouseDTO.BindChannelDto.builder().virtualWarehouseId(e.getVirtualWarehouseId()).dictPlatform(e.getDictPlatform()).relationId(e.getRelationId()).partitionId(partitionId).build()));
            }
        });
        return bindChannelDtoList;
    }
    /**
     * 判断两个集合是否双向包含
     * @param list1
     * @param list2
     * @param msg
     * @return
     */
    private boolean areListsMutuallyContained(List<VirtualWarehouseDTO.BindChannelDto> list1, List<VirtualWarehouseDTO.BindChannelDto> list2, StringBuilder msg){
        List<DictBasicEntity> dictBasicByKey = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        //字典平台名称
        Map<String, String> dictMap = dictBasicByKey.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName));
        //分区
        List<DictPartitionEntity> partitionEntityList = FeignQuery.list(DictPartitionEntity.class);
        Map<String, String> partitionMap = partitionEntityList.stream().collect(Collectors.toMap(DictPartitionEntity::getId, DictPartitionEntity::getName));
        //店铺
        List<ShopInfoEntity> shopInfoEntityList = FeignQuery.list(ShopInfoEntity.class);
        Map<String, String> shopMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
        return list1.stream().anyMatch(e1 -> list2.stream().anyMatch(e2 -> isEntityContained(e1, e2,msg,dictMap.get(e1.getDictPlatform()), e2.getVirtualWarehouseName(),shopMap.get(e1.getRelationId()), partitionMap.get(e1.getPartitionId()))));
    }

    /**
     * 比较包含
     * @param e1
     * @param e2
     * @param msg
     * @param dictPlatformName
     * @param virtualWarehouseName
     * @param partitionName
     * @return
     */
    private boolean isEntityContained(VirtualWarehouseDTO.BindChannelDto e1, VirtualWarehouseDTO.BindChannelDto e2, StringBuilder msg, String dictPlatformName, String virtualWarehouseName,String shopName, String partitionName) {
        boolean isSame = (equalsOrWildcard(e1.getDictPlatform(), e2.getDictPlatform()) &&
                !equalsOrWildcard(e1.getVirtualWarehouseId(), e2.getVirtualWarehouseId()) &&
                matchesOrWildcard(e1.getRelationId(), e2.getRelationId()) &&
                matchesOrWildcard(e1.getPartitionId(), e2.getPartitionId()));
        if (isSame){
            String format = MessageUtils.getMessage(ApiError.VM_CHANNEL_RELATION_ERROR,  dictPlatformName,CharSequenceUtil.isBlank(shopName) ? "全部" : shopName, CharSequenceUtil.isBlank(partitionName) ? "全部" : partitionName, virtualWarehouseName);
            if (!msg.toString().contains(format)){
                msg.append(format);
            }
        }
        return isSame;
    }

    private boolean matchesOrWildcard(String value1, String value2) {
        return CharSequenceUtil.isBlank(value1) || CharSequenceUtil.isBlank(value2) || Objects.equals(value1,value2);
    }

    private boolean equalsOrWildcard(String value1, String value2) {
        return Objects.equals(value1, value2);
    }
}
