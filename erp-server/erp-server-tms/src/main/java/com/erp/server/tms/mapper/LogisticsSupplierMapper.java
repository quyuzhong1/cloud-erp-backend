package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物理商表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsSupplierMapper extends BaseMapper<LogisticsSupplierEntity> {

    /**
     * 获取到tab页数据
     * @param permissionSql
     * @return
     */
    List<LogisticsSupplierDTO.TabListDTO> tabList(@Param("permissionSql") String permissionSql);
}
