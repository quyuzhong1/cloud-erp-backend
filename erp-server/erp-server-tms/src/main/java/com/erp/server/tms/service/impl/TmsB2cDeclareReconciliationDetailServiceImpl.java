package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationEntity;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.erp.model.tms.enums.InstockForecastStatusEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.TmsB2cDeclareReconciliationStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.mapper.TmsB2cDeclareReconciliationDetailMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * b2c报关对账单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsB2cDeclareReconciliationDetailServiceImpl extends SuperServiceImpl<TmsB2cDeclareReconciliationDetailMapper, TmsB2cDeclareReconciliationDetailEntity> implements TmsB2cDeclareReconciliationDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Autowired
    private TmsB2cDeclareReconciliationService tmsB2cDeclareReconciliationService;

    @Autowired
    private TransferDeclareService transferDeclareService;

    @Autowired
    private TransferDeclareDetailService transferDeclareDetailService;

    @Autowired
    private SoB2cFeign soB2cFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(List<TmsB2cDeclareReconciliationDetailDTO.AddDTO> detailList) {
        List<TmsB2cDeclareReconciliationDetailEntity> reconciliationDetailList = BeanMapperUtils.copyList(TmsB2cDeclareReconciliationDetailEntity.class, detailList);

        //新增数据验证
        checkAddData(reconciliationDetailList);
        //新增数据处理
        List<TmsB2cDeclareReconciliationDetailEntity> resultList = handleAddData(reconciliationDetailList);
        //无新增数据则直接返回
        if (CollectionUtils.isEmpty(resultList)) {
            return new BaseResultDTO.AddDTO();
        }
        log.info("开始新增报关对账单明细");
        boolean save = super.saveBatch(reconciliationDetailList);
        if(!save) {
            throw new ServiceException("报关对账单明细保存失败");
        }
        // 操作日志
        return new BaseResultDTO.AddDTO(reconciliationDetailList.get(0).getId(), reconciliationDetailList.get(0).getId());
    }


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<TmsB2cDeclareReconciliationDetailDTO.UpdateDTO> detailList, String mainId) {

        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<TmsB2cDeclareReconciliationDetailEntity> list =  BeanMapperUtils.copyList(TmsB2cDeclareReconciliationDetailEntity.class, detailList);

        handleUpdateData (list,mainId);

        //原明细数据被删除的需要清除mainId
        List<TmsB2cDeclareReconciliationDetailEntity> oldList = this.listMainIdList(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<TmsB2cDeclareReconciliationDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(mainId, obj.getSoCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个销售订单【%s】", ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(),pairList,"编辑操作");
            //更新主表id
            if (CollectionUtils.isNotEmpty(deleteList)) {
                deleteList.stream().forEach(obj -> obj.setMainId(""));
                list.addAll(deleteList);
            }
        }

        log.info("编辑 开始修改报关对账单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public void exportDetailList(TmsB2cDeclareReconciliationDetailDTO.ExportDTO param, HttpServletResponse response) {
        List<TmsB2cDeclareReconciliationDetailDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/tmsB2cDeclareReconciliationDetail.xlsx";
        String name = "b2c报关对账单明细导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsB2cDeclareReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsB2cDeclareReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<TmsB2cDeclareReconciliationDetailEntity> newList, List<TmsB2cDeclareReconciliationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * @description: 根据主表id集合查询
     * @author Will
     * @date: 2024/3/26 12:01
     * @param mainIdList
     * @return List<TmsB2cDeclareReconciliationDetailEntity>
     */
    @Override
    public List<TmsB2cDeclareReconciliationDetailEntity> listMainIdList (List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(TmsB2cDeclareReconciliationDetailEntity::getMainId,mainIdList).list();
    }

    @Override
    public BatchResultDTO updateStatus(String id, String status) {
        TmsB2cDeclareReconciliationDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到b2c报关对账单明细数据"));
        if (!StrUtil.equals(entity.getStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
            throw new ServiceException("只有待对账数据支持更新对账");
        }
        lambdaUpdate().eq(TmsB2cDeclareReconciliationDetailEntity::getId,id)
                .set(TmsB2cDeclareReconciliationDetailEntity::getStatus,status)
                .update();
        // 记录主单操作日志
        operateLogService.addModuleOperateLog(StrUtil.format("销售订单【{}】更新状态为【{}】",entity.getSoCode(), TmsB2cDeclareReconciliationStatusEnum.getName(status)), ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), entity.getId(), "更新状态操作");
        return BatchResultDTO.success(entity.getId(), entity.getSoCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public Boolean cleanDetailMainId(String id) {
        return  lambdaUpdate().eq(TmsB2cDeclareReconciliationDetailEntity::getMainId,id)
                .set(TmsB2cDeclareReconciliationDetailEntity::getMainId,"")
                .update();
    }

    /**
     * @description: 保存校验
     * @author Will
     * @date: 2024/3/26 16:57
     * @param reconciliationDetailList
     */
    private void checkAddData (List<TmsB2cDeclareReconciliationDetailEntity> reconciliationDetailList) {
        if (CollectionUtils.isEmpty(reconciliationDetailList)) {
            return;
        }
        List<String> sourceDetailIdList = reconciliationDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<TmsB2cDeclareReconciliationDetailEntity> oldDetailList = this.listDetailBySourceDetailIdList(sourceDetailIdList);

        if (CollectionUtils.isNotEmpty(oldDetailList)) {
            String codes = oldDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getSourceCode).collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_HAS_GENERATE,codes);
        }
    }

    /**
     * @description: 新增数据处理
     * @author Will
     * @date: 2024/3/26 17:05
     * @param reconciliationDetailList
     * @return List<TmsB2cDeclareReconciliationDetailEntity>
     */
    private  List<TmsB2cDeclareReconciliationDetailEntity> handleAddData (List<TmsB2cDeclareReconciliationDetailEntity> reconciliationDetailList) {
        //可新增数据
        List<TmsB2cDeclareReconciliationDetailEntity> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(reconciliationDetailList)) {
            return resultList;
        }
        //中专报关明细数据
        List<String> sourceDetailIdList = reconciliationDetailList.stream().map(TmsB2cDeclareReconciliationDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<TransferDeclareDetailEntity> transferDeclareDetailList = transferDeclareDetailService.listByIds(sourceDetailIdList);

        //中专报关数据
        List<String> declareIdList = transferDeclareDetailList.stream().map(TransferDeclareDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<TransferDeclareEntity> transferDeclareList = transferDeclareService.listByIds(declareIdList);

        //B2c销售订单
        List<String> soIdList = transferDeclareDetailList.stream().map(TransferDeclareDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cEntity> soB2cList = soB2cFeign.listByIds(soIdList);

        //b2c销售订单明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cFeign.listDetailByMainIds(soIdList);

        //b2c收货订单
        List<SoB2cReceiverEntity> soB2cReceiverList = soB2cFeign.listSoB2cReceiverByMainIdList(soIdList);

        //b2c销售订单物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(soIdList);

        for (TmsB2cDeclareReconciliationDetailEntity detailEntity : reconciliationDetailList) {
            //中转报关明细
            TransferDeclareDetailEntity transferDeclareDetail = transferDeclareDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(transferDeclareDetail)) {
                throw new ServiceException("未找到中专报关明细");
            }
            //中专报关主表信息
            TransferDeclareEntity transferDeclareEntity = transferDeclareList.stream().filter(obj -> StrUtil.equals(obj.getId(), transferDeclareDetail.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(transferDeclareEntity)) {
                throw new ServiceException("未找到中专报关信息");
            }
            detailEntity.setSourceId(transferDeclareEntity.getId());
            detailEntity.setSourceCode(transferDeclareEntity.getCode());
            detailEntity.setDate(transferDeclareEntity.getInstockForecastDate());
            //销售订单
            SoB2cEntity soB2cEntity = soB2cList.stream().filter(obj -> StrUtil.equals(obj.getId(), transferDeclareDetail.getSoId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cEntity)) {
                throw new ServiceException("未找到B2c销售订单");
            }
            detailEntity.setSoId(transferDeclareDetail.getSoId());
            detailEntity.setSoCode(transferDeclareDetail.getSoCode());
            detailEntity.setShopId(soB2cEntity.getShopId());

            //销售订单明细
            List<SoB2cDetailEntity> detailList = soB2cDetailList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), soB2cEntity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException("未找到B2c销售订单明细");
            }
            long skuCount = detailList.stream().map(SoB2cDetailEntity::getSkuId).distinct().count();
            detailEntity.setQty(Math.toIntExact(skuCount));

            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cReceiverEntity)) {
                throw new ServiceException("未找到B2c销售订单买家信息");
            }
            detailEntity.setCountry(soB2cReceiverEntity.getCountry());
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cLogisticsEntity)) {
                throw new ServiceException("未找到B2c销售订单物流信息");
            }
            detailEntity.setEstimateWeight(soB2cLogisticsEntity.getWeight());
            detailEntity.setEstimateWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());
        }
        return resultList;
    }

    /**
     * @description: 根据来源明细id集合查询
     * @author Will
     * @date: 2024/3/26 16:56
     * @param sourceDetailIdList
     * @return List<TmsB2cDeclareReconciliationDetailEntity>
     */
    private List<TmsB2cDeclareReconciliationDetailEntity> listDetailBySourceDetailIdList (List<String> sourceDetailIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TmsB2cDeclareReconciliationDetailEntity::getSourceDetailId,sourceDetailIdList)
                .list();
    }

    /**
     * @description: 分页查询数据处理
     * @author Will
     * @date: 2024/3/26 10:02
     * @param list
     */
    private void fillList (List<TmsB2cDeclareReconciliationDetailDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (TmsB2cDeclareReconciliationDetailDTO.ListDTO listDTO :list) {
            listDTO.setInstockForecastStatusName(InstockForecastStatusEnum.UPLOAD_SUCCESS.getName());
        }
    }

    private void handleUpdateData (List<TmsB2cDeclareReconciliationDetailEntity> list,String mainId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //已存在对应明细
        List<String> detailIdList = list.stream().map(TmsB2cDeclareReconciliationDetailEntity::getId).collect(Collectors.toList());
        List<TmsB2cDeclareReconciliationDetailEntity> declareReconciliationDetailList = this.listByIds(detailIdList);
        //对账单
        TmsB2cDeclareReconciliationEntity declareReconciliationEntity = tmsB2cDeclareReconciliationService.getById(mainId);
        if (ObjectUtils.isEmpty(declareReconciliationEntity)) {
            throw new ServiceException(ApiError.ERROR_DECLARE_RECONCILIATION_NOT_EXIST);
        }
        //新增不需要添加新增SKU的日志
        List<TmsB2cDeclareReconciliationDetailEntity> addList = list.stream().filter(obj -> StrUtil.isBlank(obj.getMainId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSoCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条销售订单【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(), addPairList, "编辑操作");
        }
        for (TmsB2cDeclareReconciliationDetailEntity entity : list) {
            //添加日志
            TmsB2cDeclareReconciliationDetailEntity old = declareReconciliationDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_DECLARE_RECONCILIATION_DETAIL_NOT_EXIST);
            }
            //供应商、结算组织验证
            if (!StrUtil.equals(declareReconciliationEntity.getLogisticsSupplierId(),old.getLogisticsSupplierId())) {
                throw new ServiceException(ApiError.ERROR_DECLARE_RECONCILIATION_ADD_DETAIL,declareReconciliationEntity.getCode(),declareReconciliationEntity.getLogisticsSupplierName());
            }
            entity.setMainId(mainId);
            //操作日志
            operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(),mainId,"",String.format("【%s】",old.getSoCode()));
        }
    }
}
