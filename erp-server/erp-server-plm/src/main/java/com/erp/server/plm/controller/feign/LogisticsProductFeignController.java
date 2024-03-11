package com.erp.server.plm.controller.feign;/**
 * @author Lambda
 * @Classname LogisticsController
 * @Description TODO
 * @Date 2023-11-06 12:24
 * @Created by yl
 */

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.server.plm.service.LogisticsProductService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 物流产品
 *
 * @Author yl
 * @Date 2023-11-06 12:24
 */
@RestController
@LogSystemModule("物流产品")
@RequestMapping("feign/logistics/product")
public class LogisticsProductFeignController extends BaseController {

    @Resource
    private LogisticsProductService logisticsProductService;

    /**
     * 获取到物流产品信息
     * @return
     */
    @PostMapping("/listLogisticsProduct")
    public List<LogisticsProductDTO.ProductDTO> listLogisticsProduct(@RequestBody List<String> skuIdList) {
         return logisticsProductService.listLogisticsProduct(skuIdList);
    }

    /**
     * 重算物流产品 目的国申报价
     * @return
     */
    @PostMapping("/recalDestDeclarePrice")
    public void recalDestDeclarePrice(@RequestBody List<DmpSkuCostEntity> dmpSkuCostEntityList) {
        logisticsProductService.recalDestDeclarePrice(dmpSkuCostEntityList);
    }
}
