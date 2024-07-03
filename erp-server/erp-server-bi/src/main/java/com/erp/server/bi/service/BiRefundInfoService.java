package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.dto.DmpRefundInfoDTO;
import com.erp.model.dmp.dto.DmpRefundInfoSearchDTO;
import com.erp.model.dmp.entity.BiRefundInfoEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
 * 退款列表服务类
 */
public interface BiRefundInfoService extends IService<BiRefundInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 9:29
     * @param dto
     * @return PagingVO<DmpRefundInfoDTO>
     */
    PagingVO<DmpRefundInfoDTO> paging(PagingDTO<DmpRefundInfoSearchDTO> dto);
    /**
     * @description: 导出
     * @author Will
     * @date: 2022/12/15 10:37
     * @param dto
     * @param response
     */
    void exportExcel(DmpRefundInfoSearchDTO dto, HttpServletResponse response);

    /**
     * @description: 根据退款单号查询
     * @author Will
     * @date: 2022/12/16 11:35
     * @param refundId
     * @return DmpRefundInfoEntity
     */
    BiRefundInfoEntity getByRefundId(String refundId);

    /**
     * @description: 导入
     * @author Will
     * @date: 2023/1/4 16:39
     * @param excelFile
     * @param importType
     * @param response
     * @return Boolean
     */
    Boolean importOrderFile(MultipartFile excelFile, Integer importType, HttpServletResponse response);


    /**
     * 获取到退款金额
     * @param dto
     * @return
     */
    BigDecimal getRefundOrderAmount(BiFilterDTO dto);

    /**
     * 获取年度的退款金额
     * @param dto
     * @param yearStr
     * @return
     */
    BigDecimal getYearRefundOrderAmount(BiFilterDTO dto, String yearStr);
}
