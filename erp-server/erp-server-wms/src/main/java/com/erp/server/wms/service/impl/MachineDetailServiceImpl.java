package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.MachineDetailMapper;
import com.erp.server.wms.service.MachineDetailService;
import com.erp.server.wms.service.OperateLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<MachineDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<MachineDetailEntity> list = BeanMapperUtils.copyList(MachineDetailEntity.class, detailList);

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        boolean save = this.saveBatch(list);
        //新增成功
        if (save) {
            //新增子件明细

        }
    }

    @Override
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
        }
        List<MachineDetailEntity> newList = BeanMapperUtils.copyList(MachineDetailEntity.class, detailList);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(MachineDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<MachineDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(MachineDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<MachineDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(MachineDetailEntity::getMainId,mainIds).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<MachineDetailDTO.UpdateDTO> newList, List<MachineDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(MachineDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(MachineDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<MachineDetailEntity> newList, String mainId, Boolean isUpdate) {

        List<MachineDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());


        //SKU信息
        List<String> skuIds = newList.stream().map(MachineDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        for (MachineDetailEntity detail:newList) {
            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId()) && StringUtils.isNotBlank(obj.getUnitName())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            detail.setMainId(mainId);
            //修改操作日志
            if (StringUtils.isNotBlank(detail.getId())) {
                MachineDetailEntity old = this.getById(detail.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
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

}
