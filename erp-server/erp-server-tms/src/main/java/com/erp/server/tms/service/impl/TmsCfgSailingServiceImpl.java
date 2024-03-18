package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TmsCfgSailingDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.TmsCfgSailingEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.server.tms.mapper.TmsCfgSailingMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 截单开船配置 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@Service
public class TmsCfgSailingServiceImpl extends SuperServiceImpl<TmsCfgSailingMapper, TmsCfgSailingEntity> implements TmsCfgSailingService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private LogisticsChannelService logisticsChannelService;

    @Autowired
    private DictBasicService dictBasicService;

    @Autowired
    private LogisticsSupplierService logisticsSupplierService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsCfgSailingDTO.AddDTO addDTO) {
        TmsCfgSailingEntity tmsCfgSailingEntity = new TmsCfgSailingEntity();
        BeanMapperUtils.copy(addDTO, tmsCfgSailingEntity);

        // 数据处理
        List<TmsCfgSailingEntity> resultList =   handleData(tmsCfgSailingEntity,addDTO.getLogisticsChannelIdList(),Boolean.TRUE);

        log.info("开始新增截单开船配置");
        boolean save = super.saveBatch(resultList);
        if(!save) {
            throw new ServiceException("截单开船配置保存失败");
        }

        return new BaseResultDTO.AddDTO(tmsCfgSailingEntity.getId(), tmsCfgSailingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsCfgSailingDTO.UpdateDTO updateDTO) {
        TmsCfgSailingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "截单开船配置"));
        TmsCfgSailingEntity tmsCfgSailingEntity =  BeanMapperUtils.map(TmsCfgSailingEntity.class, updateDTO);

        log.info("编辑 开始修改截单开船配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsCfgSailingEntity);
        if(!save) {
            throw new ServiceException("截单开船配置保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsCfgSailingDTO.ListDTO> paging(PagingDTO<TmsCfgSailingDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsCfgSailingDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public TmsCfgSailingDTO.ViewDTO view(String id) {
        TmsCfgSailingEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到截单发船数据"));
        TmsCfgSailingDTO.ViewDTO data = BeanMapperUtils.map(TmsCfgSailingDTO.ViewDTO.class, entity);
        return data;
    }

    @Override
    public BatchResultDTO delete(String id) {
        TmsCfgSailingEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到截单发船数据"));
        // 删除主单数据
        log.info("删除 开始删除截单发船数据，id：【{}】", id);
        this.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }

    @Override
    public  List<TmsCfgSailingEntity> getByLogisticsChannelIdList (List<String> logisticsChannelIdList) {
        List<TmsCfgSailingEntity> list = lambdaQuery()
                .in(TmsCfgSailingEntity::getLogisticsChannelId, logisticsChannelIdList)
                .list();
        return list;
    }


    /**
    * 新增修改处理数据
    */
    private List<TmsCfgSailingEntity> handleData(TmsCfgSailingEntity tmsCfgSailingEntity,List<String> logisticsChannelIdList,Boolean isAdd) {
        List<TmsCfgSailingEntity> resultList = new ArrayList<>();
        List<LogisticsChannelEntity> logisticsChannelList = logisticsChannelService.listByIds(logisticsChannelIdList);

        //查询原信息
        List<TmsCfgSailingEntity> oldList = getByLogisticsChannelIdList(logisticsChannelIdList);

        for (String  logisticsChannelId : logisticsChannelIdList) {
            TmsCfgSailingEntity entity = new TmsCfgSailingEntity();
            BeanMapperUtils.copy(tmsCfgSailingEntity,entity);
            LogisticsChannelEntity channelEntity = logisticsChannelList.stream().filter(obj -> StrUtil.equals(logisticsChannelId, obj.getId())).findFirst().orElse(new LogisticsChannelEntity());
            entity.setLogisticsSupplierId(channelEntity.getMainId());
            entity.setLogisticsChannelId(logisticsChannelId);
            //新增校验是否重复
            TmsCfgSailingEntity old = oldList.stream().filter(obj -> StrUtil.equals(obj.getLogisticsChannelId(), logisticsChannelId)).findFirst().orElse(null);
            if (isAdd && ObjectUtil.isNotEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_CFG_SAILING_EXIST,channelEntity.getName());
            }
            resultList.add(entity);
        }
        return resultList;
    }

    /**
     * 分页查询处理数据
     */
    private void fillList(List<TmsCfgSailingDTO.ListDTO> list) {
       if (CollectionUtil.isEmpty(list)) {
           return;
       }
       //物流商信息
       List<String> logisticsSupplierIdList = list.stream().map(TmsCfgSailingDTO.ListDTO::getLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierList = logisticsSupplierService.listByIds(logisticsSupplierIdList);
        //渠道信息
       List<String> logisticsChannelIdList = list.stream().map(TmsCfgSailingDTO.ListDTO::getLogisticsChannelId).collect(Collectors.toList());
       List<LogisticsChannelEntity> logisticsChannelList = logisticsChannelService.listByIds(logisticsChannelIdList);

        List<DictBasicEntity> dictList  = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.WEEK.getType(), DictBasicEnum.MONTH.getType()));

        for (TmsCfgSailingDTO.ListDTO listDTO : list) {

           //物流商名称
           String supplierName = logisticsSupplierList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getLogisticsSupplierId()))
                   .findFirst().flatMap(obj -> Optional.ofNullable(obj.getSupplierName())).orElse("");
           listDTO.setLogisticsSupplierName(supplierName);
           //物流渠道
           String logisticsChannelName = logisticsChannelList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getLogisticsChannelId()))
                   .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
           listDTO.setLogisticsChannelName(logisticsChannelName);

           //开船日期
            String startDateName = dictList.stream().filter(obj -> StrUtil.equals(obj.getType(), listDTO.getDateType()) && StrUtil.equals(obj.getCode(), listDTO.getStartDate().toString()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setStartDateName(startDateName);
            //截单日期
            String endDateName = dictList.stream().filter(obj -> StrUtil.equals(obj.getType(), listDTO.getDateType()) && StrUtil.equals(obj.getCode(), listDTO.getEndDate().toString()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setEndDateName(endDateName);
       }

    }
}
