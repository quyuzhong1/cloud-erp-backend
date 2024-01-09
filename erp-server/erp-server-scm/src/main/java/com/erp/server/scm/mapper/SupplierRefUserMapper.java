package com.erp.server.scm.mapper;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.scm.vo.SupplierRefUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-01-05
 */
@Mapper
public interface SupplierRefUserMapper extends BaseMapper<SupplierRefUserEntity> {

    List<SupplierRefUserVO> getUserIdsBySupplierIds(@Param("supplierIds") List<String> supplierIds, @Param("isSuper") Boolean isSuper);
}
