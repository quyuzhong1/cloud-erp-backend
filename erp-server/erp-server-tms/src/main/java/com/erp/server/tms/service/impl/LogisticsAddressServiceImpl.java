package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.mapper.LogisticsAddressMapper;
import com.erp.server.tms.service.LogisticsAddressService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsAddressDTO;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 物流地址表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsAddressServiceImpl extends SuperServiceImpl<LogisticsAddressMapper, LogisticsAddressEntity> implements LogisticsAddressService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;


    @Autowired
    private SysUserFeign sysUserFeign;


    @Autowired
    @Lazy
    private LogisticsChannelService logisticsChannelService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsAddressDTO.AddDTO addDTO) {
        LogisticsAddressEntity logisticsAddressEntity = new LogisticsAddressEntity();
        BeanMapperUtils.copy(addDTO, logisticsAddressEntity);

        // 数据处理
        handleData(logisticsAddressEntity);
        log.info("开始新增物流地址单");
        boolean save = super.save(logisticsAddressEntity);
        if (!save) {
            throw new ServiceException("物流地址单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】", commonService.getUserInfo().getUserName(), "物流地址");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsAddressEntity.getId(), "新增操作");


        return new BaseResultDTO.AddDTO(logisticsAddressEntity.getId(), logisticsAddressEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsAddressDTO.UpdateDTO updateDTO) {
        LogisticsAddressEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流地址单"));
        if (old.getIsBySync()) throw new ServiceException(ApiError.ERROR_SYNC_LOGISTICS_ADDRESS_IS_NOT_EDIT);
        LogisticsAddressEntity logisticsAddressEntity = BeanMapperUtils.map(LogisticsAddressEntity.class, updateDTO);
        // 数据处理
        handleData(logisticsAddressEntity);
        log.info("编辑 开始修改物流地址单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsAddressEntity);
        if (!save) {
            throw new ServiceException("物流地址单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录物流地址单日志数据，id：【{}】", logisticsAddressEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsAddressEntity.getId(), "物流地址单");
        operateLogService.addModuleOperateLogByObj(old, logisticsAddressEntity, null, logisticsAddressEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public LogisticsAddressDTO.ViewDTO view(String id) {
        LogisticsAddressEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流地址"));
        LogisticsAddressDTO.ViewDTO view = new LogisticsAddressDTO.ViewDTO();
        BeanMapper.copy(entity, view);
        view.setTypeName(view.getType().getName());
        return view;
    }


    /**
     * 地址分页
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<LogisticsAddressDTO.PagingViewDTO> paging(PagingDTO<LogisticsAddressDTO.PagingParamDTO> dto) {
        LogisticsAddressDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<LogisticsAddressDTO.PagingViewDTO> list = pageData.getRecords();
        fillData(list);
        return new PagingVO<>(pageData);
    }


    @Override
    public Boolean exportExcel(LogisticsAddressDTO.ExportDTO dto, HttpServletResponse response) {
        List<LogisticsAddressDTO.PagingViewDTO> list = baseMapper.listExport(dto);
        fillData(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/LogisticsAddress.xlsx";
        String name = "物流地址列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, "", excelPath);
        } catch (IOException e) {
            log.error("销物流地址导出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        LogisticsAddressEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("物流地址"));
        if (entity.getIsBySync()) throw new ServiceException(ApiError.ERROR_SYNC_LOGISTICS_ADDRESS_IS_NOT_DEL);
        List<LogisticsChannelEntity> channelList= logisticsChannelService.listByAddressId(id);
        if(CollectionUtils.isNotEmpty(channelList)){
               throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_ADDRESS_EXIST,entity.getName());
        }
        this.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);

    }

    @Override
    public List<LogisticsAddressDTO.ListDTO> listByType(String type) {
        List<LogisticsAddressEntity> addressList=this.lambdaQuery().
                select(LogisticsAddressEntity::getId,LogisticsAddressEntity::getName).
                eq(LogisticsAddressEntity::getType,type).
                list();
        return BeanMapperUtils.copyList(LogisticsAddressDTO.ListDTO.class,addressList);
    }

    @Override
    public List<LogisticsAddressEntity> listByTypeAndChannelId(String type, String channelId,String shopId) {
        //根据类型和渠道ID查询地址
        List<LogisticsAddressEntity> list=baseMapper.listByTypeAndChannelId(type,channelId);
        List<LogisticsAddressEntity> shopAddressList=list.stream().filter(a->shopId.equals(a.getShopId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(shopAddressList)){
             return shopAddressList;
        }
        return list.stream().filter(a->"all".equals(a.getShopId())).collect(Collectors.toList());
    }

    @Override
    public void batchSaveOrUpdateLogisticsAddress(List<LogisticsAddressEntity> list) {
        if (CollectionUtils.isNotEmpty(list)){
            list.forEach(logisticsAddressEntity -> {
                //根据地址id 和店铺id 区分数据是否已存在
                LogisticsAddressEntity logisticsServiceAddress = getLogisticsServiceAddress(logisticsAddressEntity.getAddressId(), logisticsAddressEntity.getShopId());
                if (Objects.nonNull(logisticsServiceAddress)){
                    logisticsAddressEntity.setId(logisticsServiceAddress.getId());
                }
            });
            this.saveOrUpdateBatch(list);
        }
    }

    private LogisticsAddressEntity getLogisticsServiceAddress(String addressId,String shopId){
        if (StringUtils.isEmpty(addressId) || StringUtils.isEmpty(shopId)) return null;
        LambdaQueryWrapper<LogisticsAddressEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsAddressEntity::getAddressId, addressId);
        queryWrapper.eq(LogisticsAddressEntity::getShopId, shopId);
        List<LogisticsAddressEntity> addressEntities = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(addressEntities)){
            return addressEntities.get(0);
        }else {
            return null;
        }
    }
    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsAddressEntity entity) {
        // TODO 验证数据 & 数据赋值
        String name = entity.getName();
        String id = entity.getId();
        Integer count = this.lambdaQuery().eq(LogisticsAddressEntity::getName, name).
                ne(StringUtils.isNotBlank(id), LogisticsAddressEntity::getId, id).count();
        if (count > 0) {
           throw new ServiceException(ApiError.ERROR_LOGISTICS_ADDRESS_NAME_EXIST,name);
        }

        String country = entity.getCountry();
        DictCountryEntity countryEntity = sysUserFeign.getCountryById(country);
        if (Objects.nonNull(countryEntity)) {
            entity.setCountryName(countryEntity.getNameCn());
        }

    }


    /**
     * 填充分页数据
     */
    private void fillData(List<LogisticsAddressDTO.PagingViewDTO> list) {
        for (LogisticsAddressDTO.PagingViewDTO item : list) {
            LogisticsAddressTypeEnum typeEnums = item.getType();
            item.setTypeName(typeEnums.getName());
        }

    }
}
