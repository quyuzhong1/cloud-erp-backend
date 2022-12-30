package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.ReturnOrderFilterDTO;
import com.erp.model.bi.vo.DateReturnOrderVO;
import com.erp.model.bi.vo.ReturnOrderAnalyseTableVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.server.bi.mapper.BiReturnOrderAnalyseMapper;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.BiReturnOrderAnalyseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 退货分析相关/一级模块
 *
 * @Author Luo_WG
 * @Date 2022/12/16 11:07
 **/
@Service
public class BiReturnOrderAnalyseServiceImpl extends ServiceImpl<BiReturnOrderAnalyseMapper, DmpOrderInfoEntity> implements BiReturnOrderAnalyseService {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    /**
     * 退货分析-品类
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    @Override
    public PagingVO<ReturnOrderAnalyseTableVO> returnOrderAnalByCategoryPaging(PagingDTO<BiFilterDTO> biFilterDTO) {
        biFilterDTO.getParams().setParam(biFilterDTO.getParam());
        Page query = new Page(biFilterDTO.getCurrPage(), biFilterDTO.getPageSize());
        IPage<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoIPage = baseMapper.returnOrderAnalByCategoryPaging(query, biFilterDTO.getParams());
        return new PagingVO(returnOrderAnalyseTableVoIPage);
    }

    /**
     * 退货分析-店铺
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    @Override
    public PagingVO<ReturnOrderAnalyseTableVO> returnOrderAnalByShopPaging(PagingDTO<BiFilterDTO> biFilterDTO) {
        biFilterDTO.getParams().setParam(biFilterDTO.getParam());
        Page query = new Page(biFilterDTO.getCurrPage(), biFilterDTO.getPageSize());
        IPage<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoIPage = baseMapper.returnOrderAnalByShopPaging(query, biFilterDTO.getParams());
        return new PagingVO(returnOrderAnalyseTableVoIPage);
    }

    /**
     * 退货分析-平台
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    @Override
    public List<ReturnOrderAnalyseTableVO> returnOrderAnalByPlatformPaging(BiFilterDTO biFilterDTO) {
        List<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVOIPage = baseMapper.returnOrderAnalByPlatformPaging(biFilterDTO);
        return returnOrderAnalyseTableVOIPage;
    }

    /**
     * 退货分析-平台
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    @Override
    public PagingVO<ReturnOrderAnalyseTableVO> returnOrderAnalByDeptPaging(PagingDTO<BiFilterDTO> biFilterDTO) {
        biFilterDTO.getParams().setParam(biFilterDTO.getParam());
        Page query = new Page(biFilterDTO.getCurrPage(), biFilterDTO.getPageSize());
        IPage<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoIPage = baseMapper.returnOrderAnalByDeptPaging(query, biFilterDTO.getParams());
        return new PagingVO(returnOrderAnalyseTableVoIPage);
    }

    /**
     * 退货分析-日期
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    @Override
    public PagingVO<DateReturnOrderVO> returnOrderAnalByDate(PagingDTO<ReturnOrderFilterDTO> biFilterDTO) {
        biFilterDTO.getParams().setParam(biFilterDTO.getParam());
        Page query = new Page(biFilterDTO.getCurrPage(), biFilterDTO.getPageSize());
        IPage<DateReturnOrderVO> dateReturnOrderVOIPage = null;
        switch (biFilterDTO.getParams().getDateType()) {
            case "DAY":
                dateReturnOrderVOIPage = baseMapper.returnOrderAnalByDateDay(query, biFilterDTO.getParams());
                break;
            case "WEEK":
                dateReturnOrderVOIPage = baseMapper.returnOrderAnalByDateWeek(query, biFilterDTO.getParams());
                break;
            case "MONTH":
                dateReturnOrderVOIPage = baseMapper.returnOrderAnalByDateMonth(query, biFilterDTO.getParams());
                break;
            case "QUARTER":
                dateReturnOrderVOIPage = baseMapper.returnOrderAnalByDateQuarter(query, biFilterDTO.getParams());
                break;
            case "YEAR":
                dateReturnOrderVOIPage = baseMapper.returnOrderAnalByDateYear(query, biFilterDTO.getParams());
                break;
            default:
                dateReturnOrderVOIPage = baseMapper.returnOrderAnalByDateDay(query, biFilterDTO.getParams());
                break;
        }
        return new PagingVO(dateReturnOrderVOIPage);
    }


}
