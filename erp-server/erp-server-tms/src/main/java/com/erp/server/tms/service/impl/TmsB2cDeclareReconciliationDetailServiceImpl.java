package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationEntity;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.server.tms.mapper.TmsB2cDeclareReconciliationDetailMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsB2cDeclareReconciliationDetailDTO.AddDTO addDTO) {
        TmsB2cDeclareReconciliationDetailEntity tmsB2cDeclareReconciliationDetailEntity = new TmsB2cDeclareReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, tmsB2cDeclareReconciliationDetailEntity);

        // 数据处理
        handleData(tmsB2cDeclareReconciliationDetailEntity);

        log.info("开始新增b2c报关对账单明细");
        boolean save = super.save(tmsB2cDeclareReconciliationDetailEntity);
        if(!save) {
            throw new ServiceException("b2c报关对账单明细保存失败");
        }
        // TODO 新增明细（如果有明细的话）


        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "b2c报关对账单明细" , tmsB2cDeclareReconciliationDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), tmsB2cDeclareReconciliationDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(tmsB2cDeclareReconciliationDetailEntity.getId(), tmsB2cDeclareReconciliationDetailEntity.getId());
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
    public BaseResultDTO.AddDTO updateStatus(TmsB2cDeclareReconciliationDetailDTO.UpdateStatusDTO dto) {
        TmsB2cDeclareReconciliationDetailEntity entity = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到b2c报关对账单明细数据"));
        if (!StrUtil.equals(entity.getStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
            throw new ServiceException("只有待对账数据支持更新对账");
        }
        lambdaUpdate().eq(TmsB2cDeclareReconciliationDetailEntity::getId,dto.getId())
                .set(TmsB2cDeclareReconciliationDetailEntity::getStatus,dto.getStatus())
                .update();

        return new BaseResultDTO.AddDTO(entity.getId(),entity.getSoCode());
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsB2cDeclareReconciliationDetailEntity tmsB2cDeclareReconciliationDetailEntity) {

    }

    /**
     * @description: 分页查询数据处理
     * @author Will
     * @date: 2024/3/26 10:02
     * @param list
     */
    private void fillList (List<TmsB2cDeclareReconciliationDetailDTO.ListDTO> list) {


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
