package com.erp.server.wms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.server.wms.mapper.VirtualWarehouseChannelMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.VirtualWarehouseChannelService;
import com.erp.server.wms.service.VirtualWarehouseRelationService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;

    @Resource
    private CustomerFeign customerFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseChannelDTO.AddDTO addDTO) {
        VirtualWarehouseChannelEntity virtualWarehouseChannelEntity = new VirtualWarehouseChannelEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseChannelEntity);

        // 数据处理
        handleData(virtualWarehouseChannelEntity);

        log.info("开始新增虚拟仓渠道");
        boolean save = super.save(virtualWarehouseChannelEntity);
        if (!save) {
            throw new ServiceException("虚拟仓渠道保存失败");
        }
        return new BaseResultDTO.AddDTO(virtualWarehouseChannelEntity.getId(), virtualWarehouseChannelEntity.getId());
    }

    /**
     * 批量新增
     *
     * @param batchAddDTO
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO batchAdd(VirtualWarehouseChannelDTO.BatchAddDTO batchAddDTO) {
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> dtoList = batchAddDTO.getChannelList();
        //查找原有绑定关系
        List<VirtualWarehouseChannelEntity> existChannelList = baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseChannelEntity>()
                .eq(VirtualWarehouseChannelEntity::getVirtualWarehouseId, batchAddDTO.getVirtualWarehouseId()));
        if (CollectionUtils.isNotEmpty(existChannelList)) {
            //删除原始数据
            baseMapper.deleteBatchIds(existChannelList.stream().map(VirtualWarehouseChannelEntity::getId).collect(Collectors.toList()));
        }
        //新增数据
        if (CollectionUtils.isNotEmpty(dtoList)) {
            List<VirtualWarehouseChannelEntity> batchSaveDTOList = handleData(batchAddDTO, dtoList);
            if (CollectionUtils.isNotEmpty(batchSaveDTOList)) {
                this.saveBatch(batchSaveDTOList);
            }
        }
        //添加日志
        addOperateLog(batchAddDTO,existChannelList);

        return new BaseResultDTO.AddDTO();
    }
    /**
     * 添加日志
     * @author will
     * @date 2024/7/18 14:49
     * @param batchAddDTO
     * @param existChannelList
     */
    private void addOperateLog(VirtualWarehouseChannelDTO.BatchAddDTO batchAddDTO,List<VirtualWarehouseChannelEntity> existChannelList) {

        if(CollectionUtils.isEmpty(existChannelList)) {
            return;
        }
        Boolean isChange = Boolean.FALSE;
        List<String> oldChannelMsg = new ArrayList<>();
        List<String> newChannelMsg = new ArrayList<>();
        //修改后数据
        List<ShopInfoEntity> shopList = FeignQuery.list(ShopInfoEntity.class);

        //平台信息
        List<DictBasicDTO.ViewDTO> platformList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        for (VirtualWarehouseChannelDTO.ChannelAddDTO channelAddDT : batchAddDTO.getChannelList()) {
            String shopName = CollectionUtils.isEmpty(channelAddDT.getRelationList()) ? "" : shopList.stream().filter(obj -> channelAddDT.getRelationList().contains(obj.getId())).map(ShopInfoEntity::getName)
                    .distinct().collect(Collectors.joining(",")) ;
            String msg;
            //平台名称
            String platformName = platformList.stream().filter(obj -> StrUtil.equals(obj.getValue(), channelAddDT.getDictPlatform())).map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");

            if (StrUtil.isBlank(shopName)) {
                msg = StrUtil.format("{},{}", platformName, "按"+ VitualWarehouseChannelTypeEnum.getName(channelAddDT.getType()));
            } else {
                msg = StrUtil.format("{},{}({})", platformName, "按"+ VitualWarehouseChannelTypeEnum.getName(channelAddDT.getType()), shopName);
            }
            oldChannelMsg.add(msg);
        }
        //修改前数据
        Map<String, List<VirtualWarehouseChannelEntity>> map = existChannelList.stream().collect(Collectors.groupingBy(VirtualWarehouseChannelEntity::getDictPlatform));
        for (Map.Entry<String, List<VirtualWarehouseChannelEntity>> entry : map.entrySet()) {
            List<VirtualWarehouseChannelEntity> value = entry.getValue();
            List<String> relationIdList = value.stream().map(VirtualWarehouseChannelEntity::getRelationId).collect(Collectors.toList());
            String shopName = CollectionUtils.isEmpty(relationIdList) ? "" : shopList.stream().filter(obj -> relationIdList.contains(obj.getId())).map(ShopInfoEntity::getName)
                    .distinct().collect(Collectors.joining(",")) ;
            String msg ;
            //平台名称
            String platformName = platformList.stream().filter(obj -> StrUtil.equals(obj.getValue(), value.get(0).getDictPlatform())).map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");

            if (StrUtil.isBlank(shopName)) {
                msg = StrUtil.format("{},{}", platformName, "按"+ VitualWarehouseChannelTypeEnum.getName(value.get(0).getType()));
            } else {
                msg = StrUtil.format("{},{}({})", platformName, "按"+ VitualWarehouseChannelTypeEnum.getName(value.get(0).getType()), shopName);
            }
            newChannelMsg.add(msg);
            if (!oldChannelMsg.contains(msg)) {
                isChange =  Boolean.TRUE;
            }
        }
        //size不一致或者有变更
        if (oldChannelMsg.size() != newChannelMsg.size() || isChange) {
            // 操作日志
            String msg = StrUtil.format("关联渠道：从【{}】修改为【{}】",StrUtil.join(";",newChannelMsg),StrUtil.join(";",oldChannelMsg));
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), batchAddDTO.getVirtualWarehouseId(), "编辑信息");
        }
    }

    /**
     * 封装数据
     *
     * @param batchAddDTO
     * @param dtoList
     * @return
     */
    private static List<VirtualWarehouseChannelEntity> handleData(VirtualWarehouseChannelDTO.BatchAddDTO batchAddDTO, List<VirtualWarehouseChannelDTO.ChannelAddDTO> dtoList) {
        List<VirtualWarehouseChannelEntity> batchSaveDTOList = new ArrayList<>();
        //新增数据
        dtoList.forEach(dto -> {
            //封装数据店铺数据
            if (Objects.equals(dto.getType(), VitualWarehouseChannelTypeEnum.SHOP.getCode())) {
                dto.getRelationList().forEach(relationId -> {
                    VirtualWarehouseChannelEntity virtualWarehouseChannelEntity = new VirtualWarehouseChannelEntity();
                    BeanUtils.copyProperties(dto, virtualWarehouseChannelEntity);
                    virtualWarehouseChannelEntity.setRelationId(relationId);
                    virtualWarehouseChannelEntity.setVirtualWarehouseId(batchAddDTO.getVirtualWarehouseId());
                    batchSaveDTOList.add(virtualWarehouseChannelEntity);
                });
            } else {
                //封装平台数据
                VirtualWarehouseChannelEntity virtualWarehouseChannelEntity = new VirtualWarehouseChannelEntity();
                BeanUtils.copyProperties(dto, virtualWarehouseChannelEntity);
                virtualWarehouseChannelEntity.setVirtualWarehouseId(batchAddDTO.getVirtualWarehouseId());
                batchSaveDTOList.add(virtualWarehouseChannelEntity);
            }
        });
        return batchSaveDTOList;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseChannelDTO.UpdateDTO updateDTO) {
        VirtualWarehouseChannelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓渠道"));
        VirtualWarehouseChannelEntity virtualWarehouseChannelEntity = BeanMapperUtils.map(VirtualWarehouseChannelEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseChannelEntity);
        log.info("编辑 开始修改虚拟仓渠道数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseChannelEntity);
        if (!save) {
            throw new ServiceException("虚拟仓渠道保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓渠道日志数据，id：【{}】", virtualWarehouseChannelEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseChannelEntity.getId(), "虚拟仓渠道");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseChannelEntity, null, virtualWarehouseChannelEntity.getId(), msg);
        return Boolean.TRUE;
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
            return Collections.EMPTY_LIST;
        }
        List<VirtualWarehouseRelationEntity> warehouseEntityList = virtualWarehouseRelationService.listByWarehouseIdList(platformDTO.getWarehouseIdList(), Arrays.asList(channelEntity.getVirtualWarehouseId()));
        if (ObjectUtil.isEmpty(warehouseEntityList)) {
            return Collections.EMPTY_LIST;
        }
        return warehouseEntityList;
    }

    @Override
    public List<VirtualWarehouseRelationDTO.ListPlatformDTO> listVirtualWarehouseByPlatform(VirtualWarehouseChannelDTO.ListPlatformDTO listPlatformDTO) {
        List<VirtualWarehouseChannelEntity> channelEntityList = baseMapper.listVirtualWarehouseByPlatform(listPlatformDTO);
        if (CollectionUtils.isEmpty(channelEntityList)) {
            return Collections.EMPTY_LIST;
        }
        List<String> virtualWarehouseIdList = channelEntityList.stream().map(VirtualWarehouseChannelEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseRelationEntity> warehouseEntityList = virtualWarehouseRelationService.listByWarehouseIdList(listPlatformDTO.getWarehouseIdList(), virtualWarehouseIdList);
        if (ObjectUtil.isEmpty(warehouseEntityList)) {
            return Collections.EMPTY_LIST;
        }
        List<VirtualWarehouseRelationDTO.ListPlatformDTO> resultList = new ArrayList<>();
        for (VirtualWarehouseRelationEntity entity : warehouseEntityList) {
            VirtualWarehouseRelationDTO.ListPlatformDTO platformDTO = new VirtualWarehouseRelationDTO.ListPlatformDTO();
            platformDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            platformDTO.setWarehouseId(entity.getWarehouseId());
            String dictPlatform = channelEntityList.stream().filter(obj -> StrUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId()))
                    .map(VirtualWarehouseChannelEntity::getDictPlatform).findFirst().orElse("");
            platformDTO.setDictPlatform(dictPlatform);
            List<String> relationIdList = channelEntityList.stream().filter(obj -> StrUtil.isNotBlank(obj.getRelationId()) && StrUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId()))
                    .map(VirtualWarehouseChannelEntity::getRelationId).collect(Collectors.toList());
            platformDTO.setRelationIdList(relationIdList);
            resultList.add(platformDTO);
        }
        return resultList;
    }

    @Override
    public List<VirtualWarehouseDTO.BindChannelDto> getByParams(VirtualWarehouseChannelDTO.ChannelAddDTO newChannel) {
        return baseMapper.getByParams(newChannel);
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
        Map<String, List<VirtualWarehouseChannelEntity>> map = virtualWarehouseChannelList.stream().collect(Collectors.groupingBy(obj -> obj.getVirtualWarehouseId().concat(obj.getRelationId())));
        for (Map.Entry<String, List<VirtualWarehouseChannelEntity>> entry : map.entrySet()) {
            List<VirtualWarehouseChannelEntity> value = entry.getValue();
            //实体仓库
            List<String> warehouseIdList = virtualWarehouseRelationList.stream().filter(obj -> StrUtil.equals(obj.getVirtualWarehouseId(), value.get(0).getVirtualWarehouseId())).map(VirtualWarehouseRelationEntity::getWarehouseId).collect(Collectors.toList());
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
                if (StrUtil.equals(VitualWarehouseChannelTypeEnum.SHOP.getCode(),value.get(0).getType())) {
                    List<String> relationIdList = value.stream().map(VirtualWarehouseChannelEntity::getRelationId).distinct().collect(Collectors.toList());
                    resultDTO.setRelationIdList(relationIdList);
                }
                resultList.add(resultDTO);
            }
        }
        return resultList;
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
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseChannelEntity virtualWarehouseChannelEntity) {

    }


}
