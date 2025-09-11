package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.tms.aliexpress.model.channel.response.ChannelResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jim
 * @ClassName AmazonLogisticsHandlerImpl
 * @description: 亚马逊物流接口开发
 * @date 2023年12月25日
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.AMAZON)
public class AmazonLogisticsHandlerImpl extends AbstractLogisticsHandler {

    public static final String FEIA = "17FEIA";
    public static final String LION = "360lion";
    public static final String PX = "4PX";
    public static final String A_1 = "A-1";
    public static final String AAA_COOPER = "AAA Cooper";
    public static final String ABF = "ABF";
    public static final String AFL_FEDEX = "AFL/Fedex";
    public static final String ALLJOY = "ALLJOY";
    public static final String AMAUK = "AMAUK";
    public static final String AMAZON_HORIZON = "AMAZON HORIZON";
    public static final String AMAZON_SHIPPING = "Amazon Shipping";
    public static final String AMZL = "AMZL";
    public static final String AMZL_UK = "AMZL_UK";
    public static final String ANDERE = "Andere";
    public static final String ANJUN = "Anjun";
    public static final String AO = "AO";
    public static final String AO_DEUTSCHLAND = "AO Deutschland";
    public static final String APC = "APC";
    public static final String APC_OVERNIGHT = "APC Overnight";
    public static final String APC_POSTAL_LOGISTICS = "APC POSTAL LOGISTICS";
    public static final String APG_E_COMMERCE = "APG eCommerce";
    public static final String ARAMEX = "Aramex";
    public static final String ARAS = "ARAS";
    public static final String ARAS_KARGO = "Aras Kargo";
    public static final String ARCO_SPEDIZIONI = "Arco Spedizioni";
    public static final String ARKAS = "Arkas";
    public static final String ARROW_XL = "Arrow XL";
    public static final String ASENDIA = "Asendia";
    public static final String ASGARD = "Asgard";
    public static final String ASSETT = "Assett";
    public static final String AT_POST = "AT POST";
    public static final String ATS = "ATS";
    public static final String AUSSIE_POST = "AUSSIE_POST";
    public static final String AUSTRALIA_POST = "Australia Post";
    public static final String AUSTRALIA_POST_ARTICLE_ID = "Australia Post-ArticleID";
    public static final String AUSTRALIA_POST_CONSIGNMENT = "Australia Post-Consignment";
    public static final String B_2_C = "B2C";
    public static final String B_2_C_EUROPE = "B2C Europe";
    public static final String B_2_C_SHIP = "B2CShip";
    public static final String BALNAK = "Balnak";
    public static final String BARTOLINI = "Bartolini";
    public static final String BEIJING_QUANFENG_EXPRESS = "Beijing Quanfeng Express";
    public static final String BEST_BUY = "Best Buy";
    public static final String BEST_EXPRESS = "Best Express";
    public static final String BETTER_TRUCKS = "Better Trucks";
    public static final String BJS = "BJS";
    public static final String BLOWHORN = "Blowhorn";
    public static final String BLUE_PACKAGE = "Blue Package";
    public static final String BLUE_DART = "BlueDart";
    public static final String CODE = "Bo臒azi莽i";
    public static final String CODE1 = "Bombax";
    public static final String CODE2 = "Bombino Express";
    public static final String CODE3 = "BPOST";
    public static final String BR_1_EXPRESS = "BR1 Express";
    public static final String BRT = "BRT";
    public static final String BUYLOGIC = "Buylogic";
    public static final String CODE4 = "Canada Post";
    public static final String CODE5 = "Canpar";
    public static final String CODE6 = "CargoLine";
    public static final String CARIBOU = "Caribou";
    public static final String CART_2_INDIA = "Cart2India";
    public static final String CBL = "CBL";
    public static final String CDC = "CDC";
    public static final String CELERITAS = "CELERITAS";

    /**
     * 渠道查询
     *
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        List<LogisticsSaleChannelEntity> entityList = new ArrayList<>();
      return success(entityList);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.AMAZON;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
    /**
     * 授权判断
     *
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        return success("授权成功");
    }
}
