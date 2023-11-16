package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsBillDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 物流单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsBillServiceImpl extends SuperServiceImpl<LogisticsBillMapper, LogisticsBillEntity> implements LogisticsBillService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private LogisticsBillDetailService logisticsBillDetailService;

    @Autowired
    private DictBasicService dictBasicService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsBillDTO.AddDTO addDTO) {
        LogisticsBillEntity logisticsBillEntity = new LogisticsBillEntity();
        BeanMapperUtils.copy(addDTO, logisticsBillEntity);

        // 数据处理
        handleData(logisticsBillEntity);

        log.info("开始新增物流单");
        boolean save = super.save(logisticsBillEntity);
        if (!save) {
            throw new ServiceException("物流单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流单", logisticsBillEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsBillEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        logisticsBillDetailService.add(addDTO, logisticsBillEntity.getId());
        return new BaseResultDTO.AddDTO(logisticsBillEntity.getId(), logisticsBillEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsBillDTO.UpdateDTO updateDTO) {
        LogisticsBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流单"));
        LogisticsBillEntity logisticsBillEntity = BeanMapperUtils.map(LogisticsBillEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsBillEntity);
        log.info("编辑 开始修改物流单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsBillEntity);
        if (!save) {
            throw new ServiceException("物流单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）
        logisticsBillDetailService.update(updateDTO, logisticsBillEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录物流单日志数据，id：【{}】", logisticsBillEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsBillEntity.getId(), "物流单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsBillEntity, null, logisticsBillEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(LogisticsBillEntity::getSourceId, sourceIds).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsBillEntity logisticsBillEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public Boolean logisticsBillBatchSave(List<LogisticsBillDTO.AddDTO> addDTOList) {
        List<String> sourceIds = addDTOList.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> billEntityList = this.listBySourceIds(sourceIds);
        for (LogisticsBillDTO.AddDTO addDTO : addDTOList) {
            LogisticsBillEntity saveEntity = new LogisticsBillEntity();
            BeanMapper.copy(addDTO, saveEntity);
            LogisticsBillEntity logisticsBillEntity = billEntityList.stream().filter(req -> req.getSourceId().equals(addDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(logisticsBillEntity)) {
                saveEntity.setId(logisticsBillEntity.getId());
            }
            this.saveOrUpdate(saveEntity);
            logisticsBillDetailService.removeByMainIds(Arrays.asList(saveEntity.getId()));
            List<LogisticsBillDetailDTO.AddDTO> detailList = addDTO.getDetailList();
            List<LogisticsBillDetailEntity> detailEntityList = new ArrayList<>();
            for (LogisticsBillDetailDTO.AddDTO dto : detailList) {
                LogisticsBillDetailEntity saveDetailEntity = new LogisticsBillDetailEntity();
                saveDetailEntity.setMainId(saveEntity.getId());
                saveDetailEntity.setTrackNo(dto.getTrackNo());
                saveDetailEntity.setTrackStatus(dto.getTrackStatus() == null ? "" : dto.getTrackStatus());
                detailEntityList.add(saveDetailEntity);
            }
            logisticsBillDetailService.saveOrUpdateBatch(detailEntityList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listLogisticsBillVoBySourceIds(sourceIdList);
    }

    @Override
    public List<LogisticsBillEntity> listByOutstockCodeList(List<String> outstockCodeList) {
        if (CollectionUtils.isEmpty(outstockCodeList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(LogisticsBillEntity::getOutstockCode, outstockCodeList).list();
    }


    @Override
    public List<LogisticsBillDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<LogisticsBillDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        String type = DictBasicEnum.LOGISTIC_TRACK_STATUS.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);
        List<LogisticsBillDTO.TabListDTO> resultList = new ArrayList<>(dictList.size());
        for (DictBasicDTO.ViewDTO item : dictList) {
            LogisticsBillDTO.TabListDTO tab = new LogisticsBillDTO.TabListDTO();
            String tabFlag = item.getCode();
            tab.setTabFlag(tabFlag);
            tab.setTabName(item.getName());
            Integer count = list.stream().filter(r -> r.getTabFlag().equals(tabFlag)).
                    map(LogisticsBillDTO.TabListDTO::getCount).findFirst().orElse(0);
            tab.setCount(count);
            resultList.add(tab);
        }
        LogisticsBillDTO.TabListDTO allTab = new LogisticsBillDTO.TabListDTO();
        allTab.setTabFlag(LogisticTrackStatusEnum.ALL.getCode());
        allTab.setTabName(LogisticTrackStatusEnum.ALL.getName());
        Integer allCount = resultList.stream().mapToInt(LogisticsBillDTO.TabListDTO::getCount).sum();
        allTab.setCount(allCount);
        resultList.add(allTab);
        return resultList;
    }

    @Override
    public PagingVO<LogisticsBillDTO.PagingVO> paging(PagingDTO<LogisticsBillDTO.PagingParamDTO> dto) {
        LogisticsBillDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);

        return null;
    }
}
