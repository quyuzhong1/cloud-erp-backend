package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.server.dmp.mapper.ThirdShopMapper;
import com.erp.server.dmp.service.ThirdShopService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.ThirdShopDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 第三方系统店铺表 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Slf4j
@Service
public class ThirdShopServiceImpl extends SuperServiceImpl<ThirdShopMapper, ThirdShopEntity> implements ThirdShopService {
//    @Autowired
//    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdShopDTO.AddDTO addDTO) {
        ThirdShopEntity thirdShopEntity = new ThirdShopEntity();
        BeanMapperUtils.copy(addDTO, thirdShopEntity);

        // 数据处理
        handleData(thirdShopEntity);

        log.info("开始新增第三方系统店铺单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        thirdShopEntity.setCode(code);
        boolean save = super.save(thirdShopEntity);
        if(!save) {
            throw new ServiceException("第三方系统店铺单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "第三方系统店铺单" , thirdShopEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, thirdShopEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(thirdShopEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdShopDTO.UpdateDTO updateDTO) {
        ThirdShopEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "第三方系统店铺单"));
        ThirdShopEntity thirdShopEntity =  BeanMapperUtils.map(ThirdShopEntity.class, updateDTO);

        // 数据处理
        handleData(thirdShopEntity);
        log.info("编辑 开始修改第三方系统店铺单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(thirdShopEntity);
        if(!save) {
            throw new ServiceException("第三方系统店铺单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录第三方系统店铺单日志数据，单号：【{}】", thirdShopEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thirdShopEntity.getCode(), "第三方系统店铺单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, thirdShopEntity, null, thirdShopEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ThirdShopDTO.PageDTO> paging(PagingDTO<ThirdShopDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());

        IPage<ThirdShopDTO.PageDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return null;
    }

    @Override
    public PagingVO<ThirdShopDTO.PageSelectDTO> pagingSelect(PagingDTO<ThirdShopDTO.SelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ThirdShopDTO.PageSelectDTO> pageData = this.baseMapper.pagingSelect(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO<>(pageData);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdShopEntity thirdShopEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
