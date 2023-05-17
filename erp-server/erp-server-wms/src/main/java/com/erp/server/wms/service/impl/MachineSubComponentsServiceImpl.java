package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.MachineSubComponentsMapper;
import com.erp.server.wms.service.MachineSubComponentsService;
import com.erp.server.wms.service.OperateLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 加工单子件明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-10
 */
@Service
public class MachineSubComponentsServiceImpl extends SuperServiceImpl<MachineSubComponentsMapper, MachineSubComponentsEntity> implements MachineSubComponentsService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OperateLogService operateLogService;


    @Override
    public void add(List<MachineSubComponentsDTO.AddDTO> addList, String detailId) {
        if (CollectionUtils.isEmpty(addList)) {
            return;
        }
        List<MachineSubComponentsEntity> list = BeanMapperUtils.copyList(MachineSubComponentsEntity.class, addList);

        //处理明细数据
        doOpHandleDetails(list,detailId,Boolean.FALSE);
        //批量新增
        this.saveBatch(list);
    }

    @Override
    public void update(List<MachineSubComponentsDTO.UpdateDTO> updateList, String detailId) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }
        //原明细数据
        List<MachineSubComponentsEntity> oldList = this.listByDetailId(detailId);
        List<String> deleteIds = getDeleteIds(updateList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<MachineSubComponentsEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getDetailId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个子件SKU【%s】", ModuleTypeEnum.TRANSFER_INFO.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<MachineSubComponentsEntity> newList = BeanMapperUtils.copyList(MachineSubComponentsEntity.class, updateList);

        //处理明细id及操作日志
        doOpHandleDetails(newList,detailId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public List<MachineSubComponentsEntity> listByDetailId(String detailId) {
        List<MachineSubComponentsEntity> list = lambdaQuery().eq(MachineSubComponentsEntity::getDetailId, detailId).list();
        return list;
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<MachineSubComponentsDTO.UpdateDTO> newList, List<MachineSubComponentsEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(MachineSubComponentsDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(MachineSubComponentsEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<MachineSubComponentsEntity> newList, String detailId, Boolean isUpdate) {

        //SKU信息
        List<String> skuIds = newList.stream().map(MachineSubComponentsEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        for (MachineSubComponentsEntity detail:newList) {
            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId()) && StringUtils.isNotBlank(obj.getUnitName())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            detail.setDetailId(detailId);
            //修改操作日志
            if (StringUtils.isNotBlank(detail.getId())) {
                MachineSubComponentsEntity old = this.getById(detail.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                operateLogService.addModuleOperateLogByObj(old,detail, ModuleTypeEnum.MACHINE_INFO.getCode(),detailId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }

}
