package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OtherOutstockDetailDTO;
import com.erp.model.wms.entity.OtherOutstockDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.OtherOutstockDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.OtherOutstockDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 其他出库明细表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class OtherOutstockDetailServiceImpl extends SuperServiceImpl<OtherOutstockDetailMapper, OtherOutstockDetailEntity> implements OtherOutstockDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OperateLogService operateLogService;


    @Override
    public void add(List<OtherOutstockDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<OtherOutstockDetailEntity> list = BeanMapperUtils.copyList(OtherOutstockDetailEntity.class, detailList);

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        this.saveBatch(list);
    }

    @Override
    public void update(List<OtherOutstockDetailDTO.UpdateDTO> detailList, String mainId) {
        if (detailList == null) {
            detailList = new ArrayList<>();
        }
        //原明细数据
        List<OtherOutstockDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<OtherOutstockDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.OTHER_OUTSTOCK.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<OtherOutstockDetailEntity> newList = BeanMapperUtils.copyList(OtherOutstockDetailEntity.class, detailList);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(OtherOutstockDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<OtherOutstockDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(OtherOutstockDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<OtherOutstockDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(OtherOutstockDetailEntity::getMainId,mainIds).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<OtherOutstockDetailDTO.UpdateDTO> newList, List<OtherOutstockDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(OtherOutstockDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(OtherOutstockDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<OtherOutstockDetailEntity> newList, String mainId, Boolean isUpdate) {

        //需要新增的数据
        List<OtherOutstockDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //需要修改的数据
        List<String> ids = newList.stream().filter(obj -> StringUtils.isNotBlank(obj.getId())).map(OtherOutstockDetailEntity::getId).collect(Collectors.toList());
        List<OtherOutstockDetailEntity> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            list = this.listByIds(ids);
        }

        //SKU信息
        List<String> skuIds = newList.stream().map(OtherOutstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        for (OtherOutstockDetailEntity detail:newList) {
            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId()) && StringUtils.isNotBlank(obj.getUnitName())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            detail.setMainId(mainId);
            //修改操作日志
            if (StringUtils.isNotBlank(detail.getId())) {
                if (CollectionUtils.isEmpty(list)) {
                    throw new ServiceException(ApiError.ERROR_99044);
                }
                OtherOutstockDetailEntity old = list.stream().filter(obj -> obj.getId().equals(detail.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_99044);
                }
                operateLogService.addModuleOperateLogByObj(old,detail, ModuleTypeEnum.OTHER_OUTSTOCK.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), addPairList, "编辑操作");
        }
    }
}
