package com.erp.server.sys.service.impl;


import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeEntity;
import com.erp.server.sys.mapper.CfgNoticeMapper;
import com.erp.server.sys.service.CfgNoticeDetailService;
import com.erp.server.sys.service.CfgNoticeService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 通知配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-02-13
 */
@Slf4j
@Service
public class CfgNoticeServiceImpl extends SuperServiceImpl<CfgNoticeMapper, CfgNoticeEntity> implements CfgNoticeService {
    @Resource
    private CfgNoticeDetailService cfgNoticeDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgNoticeDTO.AddDTO addDTO) {
        CfgNoticeEntity cfgNoticeEntity = new CfgNoticeEntity();
        BeanMapperUtils.copy(addDTO, cfgNoticeEntity);

        // 数据处理
        handleData(cfgNoticeEntity);

        log.info("开始新增通知配置单");
        boolean save = super.save(cfgNoticeEntity);
        if(!save) {
            throw new ServiceException("通知配置单保存失败");
        }
        //新增通知对象信息
        cfgNoticeDetailService.addOrUpdateNoticeObjectList(addDTO.getNoticeObjectDTOList(), cfgNoticeEntity.getId());
        //新增通知对象信息
        cfgNoticeDetailService.addNOrUpdateoticeTimeList(addDTO.getNoticeTimeDTOList(), cfgNoticeEntity.getId());

        return new BaseResultDTO.AddDTO(cfgNoticeEntity.getId(), cfgNoticeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgNoticeDTO.UpdateDTO addOrUpdateDTO) {
        CfgNoticeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "通知配置单"));
        CfgNoticeEntity cfgNoticeEntity =  BeanMapperUtils.map(CfgNoticeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgNoticeEntity);
        log.info("编辑 开始修改通知配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgNoticeEntity);
        if(!save) {
            throw new ServiceException("通知配置单保存失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgNoticeDTO.ListDTO> paging(PagingDTO<CfgNoticeDTO.SearchParamDTO> pagingDTO) {
        CfgNoticeDTO.SearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<CfgNoticeDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        List<CfgNoticeDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        doOpHandlePaging(records);
        return new PagingVO(pageData);
    }

    @Override
    public CfgNoticeDTO.ViewDTO view(String id) {
        CfgNoticeEntity cfgNoticeEntity = super.getById(id);
        if(cfgNoticeEntity == null) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "通知配置单");
        }
        CfgNoticeDTO.ViewDTO viewDTO = BeanMapperUtils.map(CfgNoticeDTO.ViewDTO.class, cfgNoticeEntity);
        // TODO 数据赋值处理
        return viewDTO;
    }

    @Override
    public void updateDisabled(CfgNoticeDTO.UpdateDisabledDTO dto) {
        CfgNoticeEntity cfgNoticeEntity = super.getById(dto.getId());
        if (ObjUtil.isEmpty(cfgNoticeEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "通知配置单");
        }
        cfgNoticeEntity.setDisabled(dto.getDisabled());
        boolean update = super.updateById(cfgNoticeEntity);
        if(!update) {
            throw new ServiceException("通知配置单保存失败");
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgNoticeEntity cfgNoticeEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页查询
     * @Auther will
     * @Date 2025/2/13 14:57
     * @param records
     */
    private void doOpHandlePaging(List<CfgNoticeDTO.ListDTO> records) {
        records.forEach(item -> {
            // TODO 数据赋值处理
        });
    }
}
