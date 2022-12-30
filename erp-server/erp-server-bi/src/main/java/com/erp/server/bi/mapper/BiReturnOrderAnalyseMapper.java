package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.DateReturnOrderVO;
import com.erp.model.bi.vo.ReturnOrderAnalyseTableVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BiReturnOrderAnalyseMapper extends BaseMapper<DmpOrderInfoEntity> {

    /**
     * 退货分析-类别
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<ReturnOrderAnalyseTableVO> returnOrderAnalByCategoryPaging(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 退货分析-店铺
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<ReturnOrderAnalyseTableVO> returnOrderAnalByShopPaging(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 退货分析-平台
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<ReturnOrderAnalyseTableVO> returnOrderAnalByPlatformPaging(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 退货分析-事业部
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<ReturnOrderAnalyseTableVO> returnOrderAnalByDeptPaging(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 退货分析-日期-年
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<DateReturnOrderVO> returnOrderAnalByDateYear(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 退货分析-日期-季度
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<DateReturnOrderVO> returnOrderAnalByDateQuarter(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 退货分析-日期-月
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<DateReturnOrderVO> returnOrderAnalByDateMonth(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 退货分析-日期-周
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<DateReturnOrderVO> returnOrderAnalByDateWeek(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 退货分析-日期-日
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<DateReturnOrderVO> returnOrderAnalByDateDay(@Param("params") BiFilterDTO biFilterDTO);
}
