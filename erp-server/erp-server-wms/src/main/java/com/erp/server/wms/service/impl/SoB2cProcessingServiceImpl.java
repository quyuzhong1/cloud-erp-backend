package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.model.wms.entity.SoB2cProcessingEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.SoB2cProcessingMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoB2cProcessingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_B2C_PROCESSING;

/**
 * <p>
 * B2C虚拟仓订单跟踪 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@Service
public class SoB2cProcessingServiceImpl extends SuperServiceImpl<SoB2cProcessingMapper, SoB2cProcessingEntity> implements SoB2cProcessingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cProcessingDTO.AddDTO addDTO) {
        SoB2cProcessingEntity soB2cProcessingEntity = new SoB2cProcessingEntity();
        BeanMapperUtils.copy(addDTO, soB2cProcessingEntity);

        // 数据处理
        handleData(soB2cProcessingEntity);

        log.info("开始新增B2C虚拟仓订单跟踪");
        boolean save = super.save(soB2cProcessingEntity);
        if(!save) {
            throw new ServiceException("B2C虚拟仓订单跟踪保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C虚拟仓订单跟踪" , soB2cProcessingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cProcessingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cProcessingEntity.getId(), soB2cProcessingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cProcessingDTO.UpdateDTO updateDTO) {
        SoB2cProcessingEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C虚拟仓订单跟踪"));
        SoB2cProcessingEntity soB2cProcessingEntity =  BeanMapperUtils.map(SoB2cProcessingEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cProcessingEntity);
        log.info("编辑 开始修改B2C虚拟仓订单跟踪数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cProcessingEntity);
        if(!save) {
            throw new ServiceException("B2C虚拟仓订单跟踪保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2C虚拟仓订单跟踪日志数据，id：【{}】", soB2cProcessingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cProcessingEntity.getId(), "B2C虚拟仓订单跟踪");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cProcessingEntity, null, soB2cProcessingEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoB2cProcessingDTO.ListDTO> paging(PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<SoB2cProcessingDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(SoB2cProcessingDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("B2C虚拟仓列表信息", EXPORT_WMS_SO_B2C_PROCESSING.getCode(), dto);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cProcessingEntity soB2cProcessingEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:24
     * @param list
     */
    private void fillPageData(List<SoB2cProcessingDTO.ListDTO> list) {
        // TODO 验证数据 & 数据赋值
    }
}
