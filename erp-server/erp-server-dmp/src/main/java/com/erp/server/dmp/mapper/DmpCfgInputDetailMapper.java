package com.erp.server.dmp.mapper;
import com.common.business.dto.DmpInputFeignDTO;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 外部系统接口明细 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpCfgInputDetailMapper extends BaseMapper<DmpCfgInputDetailEntity> {


    /**
     * 根据系统代号和业务代号查询新中台任务
     * @param systemCodeList 系统代号(不分大小写)
     * @param billTypeList 业务类型(对应)
     * @param nextLevelIdList 下一级ID(店铺ID/授权ID)
     * @return 可执行的任务列表-明细维度
     */
    List<DmpInoutDTO.ListDTO> listBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList);
    /**
     * 查询配置明细信息
     * @author will 
     * @date 2025/11/7 16:52
     * @param cfgOptionDTO 
     * @return DmpCfgInputDetailEntity
     */
    DmpCfgInputDetailEntity getDmpCfgInputDetailByOption(@Param("cfgOptionDTO") DmpInputFeignDTO.CfgOptionDTO cfgOptionDTO);
}
