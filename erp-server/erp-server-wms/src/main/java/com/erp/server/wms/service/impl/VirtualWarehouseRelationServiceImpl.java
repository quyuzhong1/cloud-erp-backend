package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.mapper.VirtualWarehouseRelationMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.VirtualWarehouseRelationService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 虚拟仓实体仓关联关系 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@Service
public class VirtualWarehouseRelationServiceImpl extends SuperServiceImpl<VirtualWarehouseRelationMapper, VirtualWarehouseRelationEntity> implements VirtualWarehouseRelationService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseRelationDTO.AddDTO addDTO) {
        VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = new VirtualWarehouseRelationEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseRelationEntity);

        // 数据处理
        handleData(virtualWarehouseRelationEntity);

        log.info("开始新增虚拟仓实体仓关联关系");
        boolean save = super.save(virtualWarehouseRelationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓实体仓关联关系保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓实体仓关联关系", virtualWarehouseRelationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseRelationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseRelationEntity.getId(), virtualWarehouseRelationEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseRelationDTO.UpdateDTO updateDTO) {
        VirtualWarehouseRelationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓实体仓关联关系"));
        VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = BeanMapperUtils.map(VirtualWarehouseRelationEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseRelationEntity);
        log.info("编辑 开始修改虚拟仓实体仓关联关系数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseRelationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓实体仓关联关系保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓实体仓关联关系日志数据，id：【{}】", virtualWarehouseRelationEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseRelationEntity.getId(), "虚拟仓实体仓关联关系");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseRelationEntity, null, virtualWarehouseRelationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseRelationEntity virtualWarehouseRelationEntity) {
        // TODO 验证数据 & 数据赋值
    }

    /**
     * 根据仓库id获取关联关系
     *
     * @param warehouseIdList
     * @return
     */
    @Override
    public List<VirtualWarehouseRelationEntity> getByWarehouseId(List<String> warehouseIdList) {
        return baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseRelationEntity>().in(VirtualWarehouseRelationEntity::getWarehouseId, warehouseIdList));
    }

    @Override
    public List<VirtualWarehouseRelationEntity> getByVirtualWarehouseId(String virtualWarehouseId) {
        return baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseRelationEntity>().eq(VirtualWarehouseRelationEntity::getVirtualWarehouseId, virtualWarehouseId));
    }

    /**
     * 批量新增
     *
     * @param batchAddDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO batchAdd(VirtualWarehouseRelationDTO.BatchAddDTO batchAddDTO) {
        List<String> warehouseIdList = batchAddDTO.getWarehouseIdList();
        List<VirtualWarehouseRelationEntity> existRelationList = baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseRelationEntity>().in(VirtualWarehouseRelationEntity::getVirtualWarehouseId, batchAddDTO.getVirtualWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseIdList)) {
            if (CollectionUtils.isNotEmpty(existRelationList)) {
                //删除原始数据
                baseMapper.deleteBatchIds(existRelationList.stream().map(VirtualWarehouseRelationEntity::getId).collect(Collectors.toList()));
            }
        } else {
            //校验实体仓库是否存在
            Optional.ofNullable(warehouseService.getById(warehouseIdList.get(0))).orElseThrow(()->new ServiceException(ApiError.ERROR_WAREHOUSE_NOTFOUND));
            //获取实体仓绑定关系
            if (CollectionUtils.isNotEmpty(existRelationList)) {
                //判断原始绑定与变更数据是否相同
                String existWarehouseId = existRelationList.get(0).getWarehouseId();
                if (!Objects.equals(warehouseIdList.get(0), existWarehouseId)) {
                    //更新原有绑定关系
                    VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = new VirtualWarehouseRelationEntity();
                    virtualWarehouseRelationEntity.setWarehouseId(warehouseIdList.get(0));
                    virtualWarehouseRelationEntity.setVirtualWarehouseId(batchAddDTO.getVirtualWarehouseId());
                    virtualWarehouseRelationEntity.setId(existRelationList.get(0).getId());
                    baseMapper.updateById(virtualWarehouseRelationEntity);
                }
            } else {
                VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = new VirtualWarehouseRelationEntity();
                virtualWarehouseRelationEntity.setWarehouseId(warehouseIdList.get(0));
                virtualWarehouseRelationEntity.setVirtualWarehouseId(batchAddDTO.getVirtualWarehouseId());
                baseMapper.insert(virtualWarehouseRelationEntity);
            }
        }
        //添加日志
        addOperateLog(batchAddDTO,existRelationList);

        return new BaseResultDTO.AddDTO();
    }

    /**
     * 添加日志
     * @author will
     * @date 2024/7/18 14:55
     * @param batchAddDTO
     * @param existRelationList
     */
    private void addOperateLog(VirtualWarehouseRelationDTO.BatchAddDTO batchAddDTO, List<VirtualWarehouseRelationEntity> existRelationList) {
        if (CollectionUtils.isEmpty(existRelationList)) {
            return;
        }
        List<String> warehouseIdList = existRelationList.stream().map(VirtualWarehouseRelationEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> allWarehouseIdList = new ArrayList<>();
        allWarehouseIdList.addAll(warehouseIdList);
        allWarehouseIdList.addAll(batchAddDTO.getWarehouseIdList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(allWarehouseIdList);
        String newWarehouseMsg = warehouseList.stream().filter(obj -> batchAddDTO.getWarehouseIdList().contains(obj.getId()))
                .map(WarehouseEntity::getName).distinct().collect(Collectors.joining(","));

        String oldWarehouseMsg = warehouseList.stream().filter(obj -> warehouseIdList.contains(obj.getId()))
                .map(WarehouseEntity::getName).distinct().collect(Collectors.joining(","));
        if (StrUtil.equals(oldWarehouseMsg,newWarehouseMsg)) {
            return;
        }
        // 操作日志
        String msg = StrUtil.format("关联仓库：从【{}】修改为【{}】",oldWarehouseMsg,newWarehouseMsg );
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), batchAddDTO.getVirtualWarehouseId(), "编辑信息");
    }

    @Override
    public PagingVO<VirtualWarehouseRelationDTO.SelectResultDTO> warehousePagingSelect(PagingDTO<VirtualWarehouseRelationDTO.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseRelationDTO.SelectResultDTO> pagResult = baseMapper.warehousePagingSelect(query, dto.getParams());
        List<VirtualWarehouseRelationDTO.SelectResultDTO> records = pagResult.getRecords();
        //排序
        pagResult.setRecords(records);
        return new PagingVO<>(pagResult);
    }
    @Override
    public PagingVO<VirtualWarehouseRelationDTO.SelectResultDTO> vmPagingSelect(PagingDTO<VirtualWarehouseRelationDTO.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseRelationDTO.SelectResultDTO> pagResult = baseMapper.vmPagingSelect(query, dto.getParams());
        List<VirtualWarehouseRelationDTO.SelectResultDTO> records = pagResult.getRecords();
        //排序
        pagResult.setRecords(records);
        return new PagingVO<>(pagResult);
    }

    @Override
    public List<VirtualWarehouseRelationEntity> listByWarehouseIdList(List<String> warehouseIdList,List<String> virtualWarehouseIdList) {
        if (CollectionUtil.isEmpty(warehouseIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<VirtualWarehouseRelationEntity> list = lambdaQuery()
                .in(VirtualWarehouseRelationEntity::getWarehouseId, warehouseIdList)
                .in(VirtualWarehouseRelationEntity::getVirtualWarehouseId,virtualWarehouseIdList)
                .list();
        return list;
    }

    @Override
    public List<VirtualWarehouseRelationDTO.IsExistVirtualResultDTO> isExistVirtualWarehouse(List<VirtualWarehouseRelationDTO.IsExistVirtualDTO> paramList) {
        //关联信息
        List<String> warehouseIdList = paramList.stream().map(VirtualWarehouseRelationDTO.IsExistVirtualDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> shopIdList = paramList.stream().map(VirtualWarehouseRelationDTO.IsExistVirtualDTO::getRelationId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseRelationDTO.SelectResultDTO> list = baseMapper.listByWarehouseIdListAndShopIdList(warehouseIdList,shopIdList);

        List<VirtualWarehouseRelationDTO.IsExistVirtualResultDTO> resultDTOList = new ArrayList<>();
        for (VirtualWarehouseRelationDTO.IsExistVirtualDTO dto :paramList) {
            VirtualWarehouseRelationDTO.IsExistVirtualResultDTO isExistVirtualResultDTO = new VirtualWarehouseRelationDTO.IsExistVirtualResultDTO();
            isExistVirtualResultDTO.setWarehouseId(dto.getWarehouseId());
            isExistVirtualResultDTO.setRelationId(dto.getRelationId());
            long count = list.stream().filter(obj ->
                    StrUtil.equals(obj.getWarehouseId(), dto.getWarehouseId())
                    && (StrUtil.isBlank(dto.getRelationId()) || (StrUtil.isNotBlank(dto.getRelationId()) && StrUtil.equals(obj.getRelationId(),dto.getRelationId())))
            ).count();
            if (count > 0) {
                isExistVirtualResultDTO.setIsExistVirtual(Boolean.TRUE);
            } else {
                isExistVirtualResultDTO.setIsExistVirtual(Boolean.FALSE);
            }
            resultDTOList.add(isExistVirtualResultDTO);
        }
        return resultDTOList;
    }

}
