package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.TransferApplicationDetailDTO;
import com.erp.model.wms.entity.OtherInstockDetailEntity;
import com.erp.model.wms.entity.TransferApplicationDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.TransferApplicationDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.TransferApplicationDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 调拨申请单明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-10
 */
@Service
public class TransferApplicationDetailServiceImpl extends SuperServiceImpl<TransferApplicationDetailMapper, TransferApplicationDetailEntity> implements TransferApplicationDetailService {

  @Resource
  private PlmTaskFeign plmTaskFeign;

  @Resource
  private OperateLogService operateLogService;

    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void add(List<TransferApplicationDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<TransferApplicationDetailEntity> list = BeanMapperUtils.copyList(TransferApplicationDetailEntity.class, detailList);

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        this.saveBatch(list);
        //标记SKU
        List<String> skuIds = list.stream().map(TransferApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void update(List<TransferApplicationDetailDTO.UpdateDTO> detailList, String mainId) {
        if (detailList == null) {
            detailList = new ArrayList<>();
        }
        //原明细数据
        List<TransferApplicationDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<TransferApplicationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<TransferApplicationDetailEntity> newList = BeanMapperUtils.copyList(TransferApplicationDetailEntity.class, detailList);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
        //标记SKU
        List<String> skuIds = newList.stream().map(TransferApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(TransferApplicationDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<TransferApplicationDetailEntity> listByMainId(String mainId) {
        return lambdaQuery()
                .eq(TransferApplicationDetailEntity::getMainId,mainId)
                .orderByDesc(TransferApplicationDetailEntity::getId)
                .list();
    }

    @Override
    public List<TransferApplicationDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(TransferApplicationDetailEntity::getMainId,mainIds).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<TransferApplicationDetailDTO.UpdateDTO> newList, List<TransferApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(TransferApplicationDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TransferApplicationDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<TransferApplicationDetailEntity> newList, String mainId, Boolean isUpdate) {

        //需要新增的数据
        List<TransferApplicationDetailEntity> addList = newList.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());

        //需要修改的数据
        List<String> ids = newList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId())).map(TransferApplicationDetailEntity::getId).collect(Collectors.toList());
        List<TransferApplicationDetailEntity> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            list = this.listByIds(ids);
        }

        //SKU信息
        List<String> skuIds = newList.stream().map(TransferApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        for (TransferApplicationDetailEntity detail:newList) {
            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId()) && CharSequenceUtil.isNotBlank(obj.getUnitName())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            detail.setMainId(mainId);
            //修改操作日志
            if (CharSequenceUtil.isNotBlank(detail.getId())) {
                if (CollectionUtils.isEmpty(list)) {
                    throw new ServiceException(ApiError.ERROR_99044);
                }
                TransferApplicationDetailEntity old = list.stream().filter(obj -> obj.getId().equals(detail.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_99044);
                }
                operateLogService.addModuleOperateLogByObj(old,detail, ModuleTypeEnum.TRANSFER_APPLICATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), addPairList, "编辑操作");
        }
    }

}
