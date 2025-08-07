package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThirdShopDTO;
import com.erp.model.sys.dto.DictPartitionDTO;
import com.erp.model.sys.entity.DictPartitionEntity;
import com.erp.server.sys.mapper.DictPartitionMapper;
import com.erp.server.sys.service.DictPartitionService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 分区表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
 */
@Slf4j
@Service
public class DictPartitionServiceImpl extends SuperServiceImpl<DictPartitionMapper, DictPartitionEntity> implements DictPartitionService {
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictPartitionDTO.AddDTO addDTO) {
        DictPartitionEntity dictPartitionEntity = new DictPartitionEntity();
        BeanMapperUtils.copy(addDTO, dictPartitionEntity);

        // 数据处理
        handleData(dictPartitionEntity);

        log.info("开始新增分区单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        dictPartitionEntity.setCode(code);
        boolean save = super.save(dictPartitionEntity);
        if(!save) {
            throw new ServiceException("分区单保存失败");
        }
        return new BaseResultDTO.AddDTO(dictPartitionEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictPartitionDTO.UpdateDTO updateDTO) {
        DictPartitionEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "分区单"));
        DictPartitionEntity dictPartitionEntity =  BeanMapperUtils.map(DictPartitionEntity.class, updateDTO);

        // 数据处理
        handleData(dictPartitionEntity);
        log.info("编辑 开始修改分区单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dictPartitionEntity);
        if(!save) {
            throw new ServiceException("分区单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DictPartitionDTO.DictDTO> pagingSelect(PagingDTO<DictPartitionDTO.SelectDTO> dto) {
        Page<DictPartitionDTO.ViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<DictPartitionDTO.DictDTO> pageData = this.baseMapper.pagingSelect(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public List<DictPartitionDTO.DictDTO> dropDown(DictPartitionDTO.SelectDTO dto) {
        return baseMapper.dropDown(dto);
    }

    @Override
    public List<DictPartitionEntity> listByAdvanceQuery(AdvanceQueryContainer advanceQueryContainer) {
        return baseMapper.listByAdvanceQuery(advanceQueryContainer);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(DictPartitionEntity dictPartitionEntity) {
    }
}
