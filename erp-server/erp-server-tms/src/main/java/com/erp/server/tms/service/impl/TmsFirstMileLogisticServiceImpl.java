package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 头程物流单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsFirstMileLogisticServiceImpl extends SuperServiceImpl<LogisticsBillMapper, LogisticsBillEntity> implements TmsFirstMileLogisticService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileLogisticDTO.AddDTO addDTO) {
        LogisticsBillEntity tmsFirstMileLogisticEntity = new LogisticsBillEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileLogisticEntity);
        // 数据处理
        handleData(tmsFirstMileLogisticEntity);

        log.info("开始新增头程物流单");
        boolean save = super.save(tmsFirstMileLogisticEntity);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "头程物流单" , tmsFirstMileLogisticEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileLogisticEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsFirstMileLogisticEntity.getId(), tmsFirstMileLogisticEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileLogisticDTO.UpdateDTO updateDTO) {
        LogisticsBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程物流单"));
        LogisticsBillEntity tmsFirstMileLogisticEntity =  BeanMapperUtils.map(LogisticsBillEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileLogisticEntity);
        log.info("编辑 开始修改头程物流单数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileLogisticEntity);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程物流单日志数据，id：【{}】", tmsFirstMileLogisticEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsFirstMileLogisticEntity.getId(), "头程物流单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileLogisticEntity, null, tmsFirstMileLogisticEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtil.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillEntity::getSourceId, sourceIds).list();
    }
    @Override
    public List<TmsFirstMileLogisticDTO.TabListDTO> tabList() {
        return null;
    }

    @Override
    public PagingVO<TmsFirstMileLogisticDTO.PagingVO> paging(PagingDTO<TmsFirstMileLogisticDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public TmsFirstMileLogisticDTO.StatisticsVO statistics() {
        return null;
    }

    @Override
    public TmsFirstMileLogisticDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public List<BatchResultDTO> updateLogisticsStatus(TmsFirstMileLogisticDTO.UpdateLogisticsStatusDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> updateInvoicesStatus(TmsFirstMileLogisticDTO.UpdateInvoicesStatusDTO dto) {
        return null;
    }

    @Override
    public void exportInvoices(List<String> ids, HttpServletResponse response) {

    }

    @Override
    public List<BatchResultDTO> updateChannel(TmsFirstMileLogisticDTO.UpdateChannelDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> generateReconciliation(TmsFirstMileLogisticDTO.GenerateReconciliationDTO dto) {
        return null;
    }

    @Override
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public void export(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {

    }

    @Override
    public void exportFeeDetail(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {

    }

    @Override
    public List<BatchResultDTO> delete(List<String> ids) {
        return null;
    }

    @Override
    public TmsFirstMileLogisticDTO.HistoryTrackDTO getHistoryTrack(String id) {
        return null;
    }

    @Override
    public TmsFirstMileLogisticDTO.DeliveryDTO getCanGenerateDeliveryOrder(TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto) {
        return null;
    }

    @Override
    public Boolean updateRemark(TmsFirstMileLogisticDTO.UpdateRemarkDTO dto) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsBillEntity tmsFirstMileLogisticEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
