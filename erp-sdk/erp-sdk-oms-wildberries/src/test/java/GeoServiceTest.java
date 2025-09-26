import com.sdk.oms.wildberries.dto.GeoResponse;
import com.sdk.oms.wildberries.service.GeoService;
import com.sdk.oms.wildberries.service.WildberriesSDKService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author zdy
 * @ClassName GeoServiceTest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={GeoService.class})
public class GeoServiceTest {
    @Resource
    private GeoService geoService;
    @Test
    public void getGeo() {
        BigDecimal longitude = new BigDecimal("13.4142133");
        BigDecimal latitude = new BigDecimal("52.5432379");
        GeoResponse geo = geoService.getGeo(longitude, latitude);
        System.out.println(geo);
    }
}
