package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.WmsDataCompareTempEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 数据对比对比加工临时表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
@Mapper
public interface WmsDataCompareTempMapper extends BaseMapper<WmsDataCompareTempEntity> {
	void batchInsertWmsDataCompareTemp(@Param("list")List<WmsDataCompareTempEntity> list);
	void deleteData(@Param("taskId")String taskId);
}
