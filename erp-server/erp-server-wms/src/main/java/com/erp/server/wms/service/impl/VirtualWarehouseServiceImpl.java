package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.wms.mapper.VirtualWarehouseMapper;
import com.erp.server.wms.service.VirtualWarehouseChannelService;
import com.erp.server.wms.service.VirtualWarehouseRelationService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.sdk.wangdian.sdk.impl.Api;
import org.apache.bcel.generic.IF_ACMPEQ;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;

/**
 * <p>
 * 虚拟仓 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@Service
public class VirtualWarehouseServiceImpl extends SuperServiceImpl<VirtualWarehouseMapper, VirtualWarehouseEntity> implements VirtualWarehouseService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;
    @Resource
    private VirtualWarehouseChannelService virtualWarehouseChannelService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResultDTO.AddDTO add(VirtualWarehouseDTO.AddDTO addDTO) {
        VirtualWarehouseEntity virtualWarehouseEntity = new VirtualWarehouseEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseEntity);

        // 数据处理
        handleData(virtualWarehouseEntity, addDTO.getWarehouseIdList(), addDTO.getThirdMappingList());

        log.info("开始新增虚拟仓");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XNC);
        virtualWarehouseEntity.setCode(code);
        boolean save = super.save(virtualWarehouseEntity);
        if (!save) {
            throw new ServiceException("虚拟仓保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓", virtualWarehouseEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), virtualWarehouseEntity.getId(), "新增操作");
        //绑定信息
        bindInfo(addDTO.getWarehouseIdList(), addDTO.getChannelList(), addDTO.getThirdMappingList(), virtualWarehouseEntity);

        return new BaseResultDTO.AddDTO(virtualWarehouseEntity.getId(), code);
    }

    /**
     * 绑定信息
     *
     * @param warehouseIdList
     * @param channelList
     * @param thirdMappingList
     * @param virtualWarehouseEntity
     */
    private void bindInfo(List<String> warehouseIdList, List<VirtualWarehouseChannelDTO.ChannelAddDTO> channelList,
                          List<ThirdMappingDTO.AddDTO> thirdMappingList, VirtualWarehouseEntity virtualWarehouseEntity) {
        //新增关联渠道
        bindChannel(channelList, virtualWarehouseEntity.getId());
        //新增关联仓库
        bindRelation(warehouseIdList, virtualWarehouseEntity.getId());
        //新增关联外部仓
        bindThirdMapping(thirdMappingList, virtualWarehouseEntity);
    }

    /**
     * 新增关联外部仓
     *
     * @param thirdMappingList
     */
    private void bindThirdMapping(List<ThirdMappingDTO.AddDTO> thirdMappingList, VirtualWarehouseEntity virtualWarehouseEntity) {
        ThirdMappingDTO.AddDTO addDTO = new ThirdMappingDTO.AddDTO();
        addDTO.setSysId(virtualWarehouseEntity.getId());
        addDTO.setSysName(virtualWarehouseEntity.getName());
        addDTO.setType(ThirdSysTypeEnum.VIRTUAL_WAREHOUSE.getCode());
        List<ThirdMappingDTO.ThirdAddDTO> thirdList = new ArrayList<>();
        List<ThirdMappingDTO.AddDTO> collect = thirdMappingList.stream().filter(item -> StringUtils.isNotBlank(item.getThirdId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            collect.forEach(thirdMapping -> {
                ThirdMappingDTO.ThirdAddDTO thirdAddDTO = new ThirdMappingDTO.ThirdAddDTO();
                thirdAddDTO.setSysType(PlatformDictEnum.WDT.getCode());
                thirdAddDTO.setThirdId(thirdMapping.getThirdId());
                thirdAddDTO.setSysName(virtualWarehouseEntity.getName());
                thirdList.add(thirdAddDTO);
            });
        }
        addDTO.setThirdList(thirdList);
        dmpThirdMappingFeign.add(addDTO);
    }


    /**
     * 新增关联仓库
     *
     * @param warehouseIdList
     * @param virtualWarehouseEntityId
     */
    private void bindRelation(List<String> warehouseIdList, String virtualWarehouseEntityId) {
        VirtualWarehouseRelationDTO.BatchAddDTO batchAddDTO = new VirtualWarehouseRelationDTO.BatchAddDTO();
        batchAddDTO.setWarehouseIdList(warehouseIdList);
        batchAddDTO.setVirtualWarehouseId(virtualWarehouseEntityId);
        virtualWarehouseRelationService.batchAdd(batchAddDTO);
    }

    /**
     * 新增关联渠道
     *
     * @param channelList
     * @param virtualWarehouseEntityId
     */
    private void bindChannel(List<VirtualWarehouseChannelDTO.ChannelAddDTO> channelList, String virtualWarehouseEntityId) {
        //校验一个渠道只能绑定一种类型
        Map<String, List<VirtualWarehouseChannelDTO.ChannelAddDTO>> collect = channelList.stream().collect(Collectors.groupingBy(VirtualWarehouseChannelDTO.ChannelAddDTO::getDictPlatform));
        collect.forEach((k, v) -> {
            if (v.size() > 1) {
                throw new ServiceException(ApiError.ERROR_ONLYONE);
            }
        });
        VirtualWarehouseChannelDTO.BatchAddDTO batchAddDTO = new VirtualWarehouseChannelDTO.BatchAddDTO();
        batchAddDTO.setVirtualWarehouseId(virtualWarehouseEntityId);
        batchAddDTO.setChannelList(channelList);
        virtualWarehouseChannelService.batchAdd(batchAddDTO);
    }


    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseDTO.UpdateDTO updateDTO) {
        VirtualWarehouseEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓"));
        VirtualWarehouseEntity virtualWarehouseEntity = BeanMapperUtils.map(VirtualWarehouseEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseEntity, updateDTO.getWarehouseIdList(), updateDTO.getThirdMappingList());
        log.info("编辑 开始修改虚拟仓数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehouseEntity);
        if (!save) {
            throw new ServiceException("虚拟仓保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓日志数据，单号：【{}】", virtualWarehouseEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseEntity.getCode(), "虚拟仓");
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseEntity, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), virtualWarehouseEntity.getId(), msg);
        //绑定信息
        bindInfo(updateDTO.getWarehouseIdList(), updateDTO.getChannelList(), updateDTO.getThirdMappingList(), virtualWarehouseEntity);
        return Boolean.TRUE;
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<VirtualWarehouseDTO.ListDTO> paging(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        return new PagingVO(pageData);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseEntity virtualWarehouseEntity, List<String> warehouseIdList, List<ThirdMappingDTO.AddDTO> thirdMappingList) {
        //校验名称全局唯一
        VirtualWarehouseEntity existVm = baseMapper.selectOne(new LambdaQueryWrapper<VirtualWarehouseEntity>().eq(VirtualWarehouseEntity::getName, virtualWarehouseEntity.getName()));
        if (Objects.nonNull(existVm) && !Objects.equals(existVm.getId(), virtualWarehouseEntity.getId())) {
            throw new ServiceException(ApiError.ERROR_VMNAME_EXIST);
        }
        //校验实体仓是否被别的虚拟仓绑定--当前只绑定一个实体仓库
//        if (CollectionUtils.isNotEmpty(warehouseIdList)) {
//            List<VirtualWarehouseRelationEntity> warehouseRelationList = virtualWarehouseRelationService.getByWarehouseId(warehouseIdList);
//            if (StringUtils.isNotBlank(virtualWarehouseEntity.getId())) {
//                List<VirtualWarehouseRelationEntity> collect = warehouseRelationList.stream().filter(item -> !Objects.equals(item.getVirtualWarehouseId(), virtualWarehouseEntity.getId())).collect(Collectors.toList());
//                if (CollectionUtils.isNotEmpty(collect)) {
//                    VirtualWarehouseEntity vmEntity = baseMapper.selectById(warehouseRelationList.get(0).getVirtualWarehouseId());
//                    throw new ServiceException(ApiError.ERROR_WAREHOUSE_BINDED, vmEntity.getName());
//                }
//            } else {
//                if (CollectionUtils.isNotEmpty(warehouseRelationList)) {
//                    VirtualWarehouseEntity vmEntity = baseMapper.selectById(warehouseRelationList.get(0).getVirtualWarehouseId());
//                    throw new ServiceException(ApiError.ERROR_WAREHOUSE_BINDED, vmEntity.getName());
//                }
//            }
//        }
        if (CollectionUtils.isNotEmpty(thirdMappingList)) {
            //校验关联外部仓
            checkDmpThirdMapping(virtualWarehouseEntity.getId(), virtualWarehouseEntity.getName(), thirdMappingList);
        }

        virtualWarehouseEntity.setDisabled(false);
    }

    private void checkDmpThirdMapping(String virtualWarehouseId, String virtualWarehouseName, List<ThirdMappingDTO.AddDTO> thirdMappingList) {
        ThirdMappingDTO.AddDTO addDTO = thirdMappingList.get(0);
        ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
        viewParamDTO.setType(ThirdSysTypeEnum.VIRTUAL_WAREHOUSE.getCode());
        viewParamDTO.setSysType(PlatformDictEnum.WDT.getCode());
        viewParamDTO.setThirdId(addDTO.getThirdId());
        List<ThirdMappingEntity> thirdList = dmpThirdMappingFeign.getByThirdId(viewParamDTO);
        if (CollectionUtils.isNotEmpty(thirdList)) {
            long count = thirdList.stream().filter(item -> !Objects.equals(item.getSysId(), virtualWarehouseId)).count();
            if (count > 0) {
                throw new ServiceException(ApiError.EXIST_THIRD_WAREHOUSE_MAPPING, thirdList.stream().map(ThirdMappingEntity::getSysName).collect(Collectors.joining()));
            }
        }
    }

    /**
     * 修改状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateState(VirtualWarehouseDTO.UpdateStateDTO updateStateDTO) {
        VirtualWarehouseEntity vmEntity = Optional.ofNullable(this.getById(updateStateDTO.getId())).orElseThrow(() -> new ServiceException(ApiError.ERROR_VM_NOTFOUND));
        //获取原始状态
        Boolean dbDisabled = vmEntity.getDisabled();
        if (dbDisabled.equals(updateStateDTO.getDisabled())) {
            throw new ServiceException(ApiError.ERROR_SAME_DISABLED);
        }
        VirtualWarehouseEntity virtualWarehouseEntity = new VirtualWarehouseEntity();
        virtualWarehouseEntity.setId(updateStateDTO.getId());
        virtualWarehouseEntity.setDisabled(updateStateDTO.getDisabled());
        baseMapper.updateById(virtualWarehouseEntity);
        return Boolean.TRUE;
    }

    @Override
    public VirtualWarehouseDTO.ViewDTO view(String id) {
        VirtualWarehouseEntity vmEntity = Optional.ofNullable(this.getById(id)).orElseThrow(() -> new ServiceException(ApiError.ERROR_VM_NOTFOUND));
        VirtualWarehouseDTO.ViewDTO viewDTO = new VirtualWarehouseDTO.ViewDTO();
        BeanUtils.copyProperties(vmEntity, viewDTO);
        //获取关联渠道
        List<VirtualWarehouseChannelEntity> vmChannelEntityList = virtualWarehouseChannelService.getByVirtualWarehouseId(id);
        Map<String, List<VirtualWarehouseChannelEntity>> collect = vmChannelEntityList.stream().collect(Collectors.groupingBy(VirtualWarehouseChannelEntity::getDictPlatform));
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> channelList = new ArrayList<>();
        collect.forEach((key, list) -> {
            VirtualWarehouseChannelDTO.ChannelAddDTO channelAddDTO = new VirtualWarehouseChannelDTO.ChannelAddDTO();
            if (CollectionUtils.isEmpty(list)) {
                channelAddDTO.setDictPlatform(key);
            } else {
                channelAddDTO.setDictPlatform(key);
                channelAddDTO.setType(list.get(0).getType());
                channelAddDTO.setRelationList(list.stream().map(VirtualWarehouseChannelEntity::getRelationId).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
            }
            channelList.add(channelAddDTO);
        });
        viewDTO.setChannelList(channelList);
        //获取关联仓库
        List<VirtualWarehouseRelationEntity> warehouseRelationList = virtualWarehouseRelationService.getByVirtualWarehouseId(id);
        viewDTO.setWarehouseIdList(warehouseRelationList.stream().map(VirtualWarehouseRelationEntity::getWarehouseId).collect(Collectors.toList()));
        //获取关联外部仓
        ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
        viewParamDTO.setSysId(id);
        viewParamDTO.setType(ThirdSysTypeEnum.VIRTUAL_WAREHOUSE.getCode());
        ThirdMappingDTO.MappingViewDTO view = dmpThirdMappingFeign.view(viewParamDTO);
        if (Objects.nonNull(view)) {
            viewDTO.setThirdMappingList(view.getThirdList());
        }
        return viewDTO;
    }
}
