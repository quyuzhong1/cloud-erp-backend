package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.constant.CfgApiAuthContant;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.dto.MachineRefSoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.MachineRefSoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.MachineDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 加工单明细
 *
 * @author will
 * @since 2023-05-10
 */
@Service
public class MachineDetailServiceImpl extends SuperServiceImpl<MachineDetailMapper, MachineDetailEntity> implements MachineDetailService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private MachineSubComponentsService machineSubComponentsService;

    @Resource
    private MachineInfoService machineInfoService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MachineRefSoService machineRefSoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void add(List<MachineDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }

        List<MachineDetailEntity> list = new ArrayList<>();
        for (MachineDetailDTO.AddDTO addDTO : detailList) {
            MachineDetailEntity detailEntity = new MachineDetailEntity();
            BeanMapperUtils.copy(addDTO,detailEntity);

            List<MachineSubComponentsDTO.AddDTO> addList = addDTO.getSubComponentsList();
            List<MachineSubComponentsDTO.UpdateDTO> updateList = BeanMapperUtils.copyList(MachineSubComponentsDTO.UpdateDTO.class, addList);
            detailEntity.setSubComponentsList(updateList);
            list.add(detailEntity);
        }

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        boolean save = this.saveBatch(list);
        //新增成功
        if (save) {
            //新增子件明细
            for (MachineDetailEntity detailEntity : list) {
                List<MachineSubComponentsDTO.AddDTO> addList = BeanMapperUtils.copyList(MachineSubComponentsDTO.AddDTO.class, detailEntity.getSubComponentsList());
                machineSubComponentsService.add(addList,detailEntity.getId(),mainId);
            }
            //新增加工单销售订单关联信息
            addMachineRefSo(mainId,list);
            //标记SKU
            List<String> parentIdList = list.stream().map(MachineDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            List<String> childSkuIdList = list.stream().flatMap(obj -> Stream.of(obj.getSubComponentsList().stream().map(MachineSubComponentsDTO.AddDTO::getSkuId).toArray(String[]::new))).distinct().collect(Collectors.toList());
            parentIdList.addAll(childSkuIdList);
            plmTaskFeign.updateOccupyStatus(parentIdList);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void update(List<MachineDetailDTO.UpdateDTO> detailList, String mainId) {
        if (detailList == null) {
            detailList = new ArrayList<>();
        }
        //原明细数据
        List<MachineDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<MachineDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.TRANSFER_INFO.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
            //删除关联关系
            machineRefSoService.removeByMachineDetailIdList(deleteIds);
        }
        List<MachineDetailEntity> newList = BeanMapperUtils.copyList(MachineDetailEntity.class, detailList);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);

        //修改子件明细
        for (MachineDetailEntity detailEntity : newList) {
            machineSubComponentsService.update(detailEntity.getSubComponentsList(),detailEntity.getId(),mainId);
        }
        //标记SKU
        List<String> parentIdList = newList.stream().map(MachineDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> childSkuIdList = newList.stream().flatMap(obj -> Stream.of(obj.getSubComponentsList().stream().map(MachineSubComponentsDTO.AddDTO::getSkuId).toArray(String[]::new))).distinct().collect(Collectors.toList());
        parentIdList.addAll(childSkuIdList);
        plmTaskFeign.updateOccupyStatus(parentIdList);
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(MachineDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<MachineDetailEntity> listBySourceDetailIds(List<String> detailIds) {
        if(CollectionUtils.isEmpty(detailIds)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(MachineDetailEntity::getSourceDetailId,detailIds).list();
    }

    @Override
    public List<SoB2bProcessingDTO.ResponseDTO> listMachineBySourceIdList(List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listMachineBySourceIdList(sourceIdList);
    }

    @Override
    public List<SoB2bProcessingDTO.ResponseDTO> listMachineByRefIdList(List<String> refIdList) {
        if (CollectionUtils.isEmpty(refIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listMachineByRefIdList(refIdList);
    }

    @Override
    public List<MachineDetailEntity> listByMainId(String mainId) {
        return lambdaQuery()
                .eq(MachineDetailEntity::getMainId,mainId)
                .orderByAsc(MachineDetailEntity::getId)
                .list();
    }

    @Override
    public List<MachineDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(MachineDetailEntity::getMainId,mainIds).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<MachineDetailDTO.UpdateDTO> newList, List<MachineDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(MachineDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(MachineDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<MachineDetailEntity> newList, String mainId, Boolean isUpdate) {

        //需要新增的数据
        List<MachineDetailEntity> addList = newList.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());

        //需要修改的数据
        List<String> ids = newList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId())).map(MachineDetailEntity::getId).collect(Collectors.toList());
        List<MachineDetailEntity> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            list = this.listByIds(ids);
        }

        //SKU信息
        List<String> skuIds = newList.stream().map(MachineDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //bom信息
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(bomChildrenSkuList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }

        //主表信息
        MachineInfoEntity machineInfoEntity = machineInfoService.getById(mainId);
        if (ObjectUtils.isEmpty(machineInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        WarehouseEntity warehouseEntity = warehouseService.getById(machineInfoEntity.getWarehouseId());
        if (ObjectUtils.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //仓位必填验证
        checkWarehouseLocation(warehouseEntity,newList);

        for (int i = 0; i < newList.size(); i++) {
            MachineDetailEntity detail = newList.get(i);
            detail.setIndex(i+1);
            //验证子件数量
            checkBomChildrenSku(bomChildrenSkuList,detail);

            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId()) && CharSequenceUtil.isNotBlank(obj.getUnitName())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            detail.setMainId(mainId);
            //修改操作日志
            if (CharSequenceUtil.isNotBlank(detail.getId())) {
                if (CollectionUtils.isEmpty(list)) {
                    throw new ServiceException(ApiError.ERROR_99053);
                }
                MachineDetailEntity old = list.stream().filter(obj -> obj.getId().equals(detail.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_99053);
                }
                operateLogService.addModuleOperateLogByObj(old,detail, ModuleTypeEnum.MACHINE_INFO.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.MACHINE_INFO.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * @description: 验证子件数量
     * @author Will
     * @date: 2023/5/19 10:58
     * @param bomChildrenSkuList
     * @param detail
     */
    private void checkBomChildrenSku (List<BomChildrenSkuDTO> bomChildrenSkuList,MachineDetailEntity detail) {

        //验证SKU及子件明细数量
        List<BomChildrenSkuDTO> bomList = bomChildrenSkuList.stream().filter(obj -> obj.getParentSkuId().equals(detail.getSkuId()) && obj.getBomVersion().equals(detail.getReferenceVersion())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(bomList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        //明细子件数量验证
        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomList) {
            List<MachineSubComponentsDTO.UpdateDTO> updateList = detail.getSubComponentsList();
            Integer qty = updateList.stream().filter(obj -> obj.getSkuId().equals(bomChildrenSkuDTO.getSkuId())).map(MachineSubComponentsDTO.UpdateDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            //如果子件明细合计数量 != 明细数量 * bom子件数量
            if (MathUtil.compareTo(qty,detail.getQty() * bomChildrenSkuDTO.getQuantity()) != MathUtil.ZERO) {
                throw new ServiceException(ApiError.ERROR_99057.code, String.format(ApiError.ERROR_99057.msg,detail.getIndex(), bomChildrenSkuDTO.getSkuNo(),detail.getQty() * bomChildrenSkuDTO.getQuantity()));
            }
        }
        detail.setBomHistoryId(bomList.get(0).getBomHistoryId());
    }

    /**
     * @description: 仓位必填验证
     * @author Will
     * @date: 2023/12/19 15:19
     * @param warehouseEntity
     * @param list
     */
    private void checkWarehouseLocation (WarehouseEntity warehouseEntity,List<MachineDetailEntity> list) {
        //仓库配置
        CfgApiAuthEntity cfgApiAuthEntity = dmpTaskFeign.getByKey(new CfgApiAuthDTO.FeignDTO(CfgApiAuthContant.WAREHOUSE_LOCATION_VALIDATE));
        List<String> warehouseIdList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(cfgApiAuthEntity)) {
            CfgApiAuthDTO.WarehouseLocationValidateDTO warehouseLocationValidateDTO = JSONUtil.toBean(cfgApiAuthEntity.getValue(), CfgApiAuthDTO.WarehouseLocationValidateDTO.class);
            warehouseIdList = Arrays.stream(warehouseLocationValidateDTO.getWarehouseIds().split(",")).collect(Collectors.toList());
        }
        long count = list.stream().filter(obj -> CharSequenceUtil.isBlank(obj.getWarehouseLocation())).count();
        //判断仓位是否需要必填
        if (warehouseIdList.contains(warehouseEntity.getId()) && count > 0) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_LOCATION_NOT_NULL,warehouseEntity.getName());
        }
    }

    /**
     * @description: 新增加工单销售订单关联信息
     * @author Will
     * @date: 2023/12/6 14:52
     * @param mainId
     * @param list
     */
    private void addMachineRefSo (String mainId, List<MachineDetailEntity> list) {
        //新增加工单销售订单关联信息
        MachineInfoEntity machineInfoEntity = machineInfoService.getById(mainId);
        if (ObjectUtils.isEmpty(machineInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(machineInfoEntity.getSourceType())) {
            return;
        }
        List<String> refDetailIdList = list.stream().map(MachineDetailEntity::getRefDetailId).collect(Collectors.toList());
        List<MachineRefSoEntity> oldRefList = machineRefSoService.listBySoDetailIdList(refDetailIdList);
        if (CollectionUtils.isNotEmpty(oldRefList)) {
            //存在下推的销售订单明细id集合
            List<String> soDetailIdList = oldRefList.stream().map(MachineRefSoEntity::getSoDetailId).collect(Collectors.toList());
            //销售订单号
            String soCodes = oldRefList.stream().map(MachineRefSoEntity::getSoCode).collect(Collectors.joining(","));
            //SKU编号
            String skuNoList = list.stream().filter(obj -> soDetailIdList.contains(obj.getRefDetailId())).map(MachineDetailEntity::getSkuNo).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_SO_PUSH_MACHINE,soCodes,skuNoList);
        }
        List<MachineRefSoDTO.AddDTO> refAddList = new ArrayList<>();
        for (MachineDetailEntity detailEntity: list) {
            MachineRefSoDTO.AddDTO addDTO = new MachineRefSoDTO.AddDTO();
            if (CharSequenceUtil.isBlank(detailEntity.getRefId()) || CharSequenceUtil.isBlank(detailEntity.getRefCode()) || CharSequenceUtil.isBlank(detailEntity.getRefDetailId())) {
                continue;
            }
            addDTO.setSoId(detailEntity.getRefId());
            addDTO.setSoCode(detailEntity.getRefCode());
            addDTO.setSoDetailId(detailEntity.getRefDetailId());
            addDTO.setMachineId(mainId);
            addDTO.setMachineDetailId(detailEntity.getId());
            refAddList.add(addDTO);
        }
        if (CollUtil.isEmpty(refAddList)) {
            return;
        }
        List<MachineRefSoEntity> machineRefSoList = BeanMapperUtils.copyList(MachineRefSoEntity.class, refAddList);
        machineRefSoService.saveBatch(machineRefSoList);
    }
}
