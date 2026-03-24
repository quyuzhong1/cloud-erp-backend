package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.erp.model.wms.entity.QcApplicationDetailEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.QcApplicationDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcApplicationDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_WMS_QC_APPLICATION_DETAIL;


/**
 * <p>
 * 质检申请单明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcApplicationDetailServiceImpl extends SuperServiceImpl<QcApplicationDetailMapper, QcApplicationDetailEntity> implements QcApplicationDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<QcApplicationDetailDTO.AddDTO> detailList, String mainId,String sourceId) {
        List<QcApplicationDetailEntity> qcApplicationDetailList = BeanUtil.copyToList(detailList, QcApplicationDetailEntity.class);

        // 数据处理
        handleData(qcApplicationDetailList,mainId,sourceId);

        log.info("开始新增质检申请单明细单");
        boolean save = super.saveBatch(qcApplicationDetailList);
        if(!save) {
            throw new ServiceException("质检申请单明细单保存失败");
        }
        return Boolean.TRUE;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<QcApplicationDetailDTO.UpdateDTO> detailList, String mainId,String sourceId) {
        if (CollUtil.isNotEmpty(detailList)) {
            throw new ServiceException(ApiError.QC_APPLICATION_DETAIL_NOT_EXIST);
        }
        List<QcApplicationDetailEntity> qcApplicationDetailList = BeanUtil.copyToList(detailList, QcApplicationDetailEntity.class);

        //原明细数据
        List<QcApplicationDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<QcApplicationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.QC_APPLICATION.getCode(),pairList,"编辑操作");
            List<QcApplicationDetailEntity> list = lambdaQuery().in(QcApplicationDetailEntity::getMainId, deleteIds).list();
            list.forEach(req -> req.setIsDeleted(Boolean.TRUE));
            this.removeByIds(deleteIds);
        }

        // 数据处理
        handleData(qcApplicationDetailList,mainId,sourceId);
        log.info("编辑 开始修改质检申请单明细单数据，id：【{}】", mainId);
        boolean save = super.updateBatchById(qcApplicationDetailList);
        if(!save) {
            throw new ServiceException("质检申请单明细单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("质检申请明细导入", IMPORT_WMS_QC_APPLICATION_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<QcApplicationDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(QcApplicationDetailEntity::getMainId, mainId).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<QcApplicationDetailDTO.UpdateDTO> newList, List<QcApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(QcApplicationDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(QcApplicationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<QcApplicationDetailEntity> qcApplicationDetailList,String mainId,String sourceId) {
        if (CollUtil.isEmpty(qcApplicationDetailList)) {
            return;
        }

        //添加操作日志
        List<QcApplicationDetailEntity> addList = qcApplicationDetailList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.QC_APPLICATION.getCode(), addPairList, "编辑操作");
        }


        //查询来源采购订单数据
        PurchaseOrderSupplierEntity poSupplierEntity = CharSequenceUtil.isBlank(sourceId) ? new PurchaseOrderSupplierEntity() : FeignQuery.getById(PurchaseOrderSupplierEntity.class, sourceId);

        for (QcApplicationDetailEntity data : qcApplicationDetailList) {
               //校验供应商信息
                if (CharSequenceUtil.isNotBlank(data.getSupplierId()) && !CharSequenceUtil.equals(data.getSupplierId(),poSupplierEntity.getSupplierId())) {
                    throw new ServiceException(ApiError.QC_APPLICATION_SUPPLIER_NOT_DIFF);
                }
                //校验申请质检数量 TODO


            //操作日志
            if (StringUtils.isNotBlank(data.getId())) {
                QcApplicationDetailEntity old = this.getById(data.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.QC_APPLICATION_NOT_EXIST);
                }
                operateLogService.addModuleOperateLogByObj(old,data, ModuleTypeEnum.QC_APPLICATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }


    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<QcApplicationDetailDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(QcApplicationDetailDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}
