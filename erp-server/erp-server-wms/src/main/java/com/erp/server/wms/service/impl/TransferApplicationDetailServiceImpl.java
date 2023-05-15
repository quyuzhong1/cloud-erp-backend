package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.TransferApplicationDetailDTO;
import com.erp.model.wms.entity.TransferApplicationDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.TransferApplicationDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.TransferApplicationDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 调拨申请单明细表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferApplicationDetailServiceImpl extends SuperServiceImpl<TransferApplicationDetailMapper, TransferApplicationDetailEntity> implements TransferApplicationDetailService {

  @Resource
  private PlmTaskFeign plmTaskFeign;

  @Resource
  private OperateLogService operateLogService;

    
    @Override
    public void add(List<TransferApplicationDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<TransferApplicationDetailEntity> list = BeanMapperUtils.copyList(TransferApplicationDetailEntity.class, detailList);

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        this.saveBatch(list);
    }

    @Override
    public void update(List<TransferApplicationDetailDTO.UpdateDTO> details, String mainId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<TransferApplicationDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<TransferApplicationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<TransferApplicationDetailEntity> newList = BeanMapperUtils.copyList(TransferApplicationDetailEntity.class, details);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
        
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(TransferApplicationDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<TransferApplicationDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(TransferApplicationDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<TransferApplicationDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(TransferApplicationDetailEntity::getMainId,mainIds).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<TransferApplicationDetailDTO.UpdateDTO> newList, List<TransferApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TransferApplicationDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TransferApplicationDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<TransferApplicationDetailEntity> newList, String mainId, Boolean isUpdate) {

        List<TransferApplicationDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());


        //SKU信息
        List<String> skuIds = newList.stream().map(TransferApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        for (TransferApplicationDetailEntity detail:newList) {
            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            detail.setMainId(mainId);
            //修改操作日志
            if (StringUtils.isNotBlank(detail.getId())) {
                TransferApplicationDetailEntity old = this.getById(detail.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                operateLogService.addModuleOperateLogByObj(old,detail, ModuleTypeEnum.TRANSFER_APPLICATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PO_INSTOCK.getCode(), addPairList, "编辑操作");
        }


    }
}
