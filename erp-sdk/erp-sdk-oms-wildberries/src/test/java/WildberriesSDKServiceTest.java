import cn.hutool.json.JSONUtil;
import com.common.business.utils.PdfUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.sdk.oms.wildberries.constant.WildberriesConstant;
import com.sdk.oms.wildberries.dto.*;
import com.sdk.oms.wildberries.service.WildberriesSDKService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

/**
 * @author zdy
 * @ClassName WildberriesSDKServiceTest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={WildberriesSDKService.class})
public class WildberriesSDKServiceTest {

    @Resource
    private WildberriesSDKService wildberriesSDKService;
    @Test
    public void shopCheck() {
        WildberriesResponse response = wildberriesSDKService.checkToken(WildberriesConstant.TOKEN);
        System.out.println(JSONUtil.toJsonStr(response));
    }
    @Test
    public void getProductCategory() {
        String response = wildberriesSDKService.getProductCategory(WildberriesConstant.TOKEN);
//        {"data":[{"name":"Игрушки","id":7,"isVisible":true},{"name":"Спортивная обувь","id":3497,"isVisible":true},{"name":"Обувь","id":2,"isVisible":true},{"name":"Рукоделие","id":1284,"isVisible":true},{"name":"Техника для кухни","id":1521,"isVisible":true},{"name":"Белье","id":4,"isVisible":true},{"name":"Красота","id":49,"isVisible":true},{"name":"Хранение вещей","id":1598,"isVisible":true},{"name":"Ноутбуки и компьютеры","id":6238,"isVisible":true},{"name":"Электроника","id":479,"isVisible":true},{"name":"Периферия и аксессуары","id":6237,"isVisible":true},{"name":"Строительные инструменты","id":1162,"isVisible":true},{"name":"Книжная продукция и диски","id":786,"isVisible":true},{"name":"Посуда и инвентарь","id":1590,"isVisible":true},{"name":"Для праздника","id":257,"isVisible":true},{"name":"Телевизоры и аудиотехника","id":6260,"isVisible":true},{"name":"Хозяйственные товары","id":1077,"isVisible":true},{"name":"Страхование","id":5300,"isVisible":false},{"name":"Электротранспорт","id":2207,"isVisible":true},{"name":"Канцелярские товары","id":571,"isVisible":true},{"name":"Ювелирные украшения","id":204,"isVisible":true},{"name":"Бижутерия","id":200,"isVisible":true},{"name":"Транспортные средства","id":8242,"isVisible":false},{"name":"Товары для малышей","id":883,"isVisible":true},{"name":"Аксессуары для малышей","id":4823,"isVisible":true},{"name":"Аксессуары для волос","id":1465,"isVisible":true},{"name":"Сантехника","id":2051,"isVisible":true},{"name":"Профессиональные музыкальные инструменты","id":3324,"isVisible":true},{"name":"Бытовая техника","id":657,"isVisible":true},{"name":"Строительные материалы","id":2050,"isVisible":true},{"name":"Отделочные материалы","id":8555,"isVisible":true},{"name":"Оргтехника","id":858,"isVisible":true},{"name":"Спортивная одежда","id":1513,"isVisible":true},{"name":"Одежда для малышей","id":4607,"isVisible":true},{"name":"Продукты","id":3109,"isVisible":true},{"name":"Здоровье","id":4268,"isVisible":true},{"name":"Белье для малышей","id":4735,"isVisible":true},{"name":"Товары для животных","id":723,"isVisible":true},{"name":"Одежда","id":1,"isVisible":true},{"name":"Головные уборы","id":6,"isVisible":true},{"name":"Аксессуары","id":3,"isVisible":true},{"name":"Спортивный товар","id":239,"isVisible":true},{"name":"Автомобильные товары","id":760,"isVisible":true},{"name":"Алкогольная продукция","id":6967,"isVisible":true},{"name":"Крупная бытовая техника","id":5495,"isVisible":true},{"name":"Товары для курения","id":6249,"isVisible":true},{"name":"Игровые консоли и игры","id":6256,"isVisible":true},{"name":"Товары для взрослых","id":5038,"isVisible":true},{"name":"Фарма","id":6371,"isVisible":true},{"name":"Умный дом и безопасность","id":6259,"isVisible":true},{"name":"Спортивные аксессуары","id":1605,"isVisible":true},{"name":"Детское питание","id":2638,"isVisible":true},{"name":"Автоэлектроника","id":6240,"isVisible":true},{"name":"Смартфоны и аксессуары","id":6258,"isVisible":true},{"name":"Товары для отдыха и кемпинга","id":6945,"isVisible":true},{"name":"Сетевое оборудование","id":6257,"isVisible":true},{"name":"Запчасти для авто","id":6731,"isVisible":true},{"name":"Фото и Видеотехника","id":6261,"isVisible":true},{"name":"Все для садоводства","id":6943,"isVisible":true},{"name":"Спортивное питание и косметика","id":1081,"isVisible":true},{"name":"Шторы и аксессуары","id":1616,"isVisible":true},{"name":"Садовая техника","id":2240,"isVisible":true},{"name":"Торговое оборудование","id":2479,"isVisible":true},{"name":"Дом","id":195,"isVisible":true},{"name":"Мебель","id":7580,"isVisible":true},{"name":"Спецодежда и СИЗы","id":6119,"isVisible":true},{"name":"Аксессуары для обуви","id":739,"isVisible":true},{"name":"Садовые инструменты","id":6946,"isVisible":true},{"name":"Восстановленные","id":2304,"isVisible":false},{"name":"Автомобили","id":8895,"isVisible":true}],"error":false,"errorText":"","additionalErrors":null}
    }
    @Test
    public void getProductSubject() {
        String response = wildberriesSDKService.getProductSubject(WildberriesConstant.TOKEN);
//        {"data":[{"subjectID":397,"parentID":786,"subjectName":"Журналы","parentName":"Книжная продукция и диски"},{"subjectID":3806,"parentID":786,"subjectName":"Книги родословные","parentName":"Книжная продукция и диски"},{"subjectID":1134,"parentID":786,"subjectName":"Музыкальные диски","parentName":"Книжная продукция и диски"},{"subjectID":2022,"parentID":786,"subjectName":"Обучающие диски","parentName":"Книжная продукция и диски"},{"subjectID":8241,"parentID":8895,"subjectName":"Автомобили новые","parentName":"Автомобили"},{"subjectID":840,"parentID":786,"subjectName":"Наборы карточек","parentName":"Книжная продукция и диски"},{"subjectID":1046,"parentID":786,"subjectName":"Плакаты","parentName":"Книжная продукция и диски"},{"subjectID":8212,"parentID":786,"subjectName":"Прописи","parentName":"Книжная продукция и диски"},{"subjectID":54,"parentID":204,"subjectName":"Ювелирные кольца","parentName":"Ювелирные украшения"},{"subjectID":3100,"parentID":3109,"subjectName":"Печенье","parentName":"Продукты"},{"subjectID":3110,"parentID":3109,"subjectName":"Хлебцы","parentName":"Продукты"},{"subjectID":3391,"parentID":3109,"subjectName":"Хлебные палочки","parentName":"Продукты"},{"subjectID":2964,"parentID":786,"subjectName":"Блокноты творческие","parentName":"Книжная продукция и диски"},{"subjectID":8827,"parentID":8895,"subjectName":"Автомобили с пробегом","parentName":"Автомобили"},{"subjectID":3133,"parentID":3109,"subjectName":"Сухарики","parentName":"Продукты"},{"subjectID":3360,"parentID":3109,"subjectName":"Рахат-лукум","parentName":"Продукты"},{"subjectID":2080,"parentID":786,"subjectName":"Правовые акты","parentName":"Книжная продукция и диски"},{"subjectID":3733,"parentID":786,"subjectName":"Ноты","parentName":"Книжная продукция и диски"},{"subjectID":7081,"parentID":786,"subjectName":"Проекты домов","parentName":"Книжная продукция и диски"},{"subjectID":105,"parentID":2,"subjectName":"Кроссовки","parentName":"Обувь"},{"subjectID":2018,"parentID":786,"subjectName":"Атласы учебные","parentName":"Книжная продукция и диски"},{"subjectID":3456,"parentID":786,"subjectName":"Букинистические комплекты","parentName":"Книжная продукция и диски"},{"subjectID":4961,"parentID":786,"subjectName":"Книги-комиксы","parentName":"Книжная продукция и диски"},{"subjectID":8477,"parentID":786,"subjectName":"Наглядные пособия","parentName":"Книжная продукция и диски"},{"subjectID":1313,"parentID":6260,"subjectName":"MP3 плееры","parentName":"Телевизоры и аудиотехника"},{"subjectID":5805,"parentID":786,"subjectName":"Иностранные книги","parentName":"Книжная продукция и диски"},{"subjectID":6710,"parentID":786,"subjectName":"Интеллект-карты","parentName":"Книжная продукция и диски"},{"subjectID":744,"parentID":786,"subjectName":"Календари","parentName":"Книжная продукция и диски"},{"subjectID":4146,"parentID":786,"subjectName":"Карты Таро","parentName":"Книжная продукция и диски"},{"subjectID":5322,"parentID":786,"subjectName":"Книги коллекционные","parentName":"Книжная продукция и диски"},{"subjectID":8384,"parentID":786,"subjectName":"Контурные карты","parentName":"Книжная продукция и диски"},{"subjectID":8261,"parentID":786,"subjectName":"Метафорические ассоциативные карты","parentName":"Книжная продукция и диски"},{"subjectID":338,"parentID":786,"subjectName":"Раскраски","parentName":"Книжная продукция и диски"},{"subjectID":2076,"parentID":786,"subjectName":"Словари","parentName":"Книжная продукция и диски"},{"subjectID":3101,"parentID":3109,"subjectName":"Вафли","parentName":"Продукты"},{"subjectID":5768,"parentID":786,"subjectName":"Аудиокниги CD","parentName":"Книжная продукция и диски"},{"subjectID":3455,"parentID":786,"subjectName":"Букинистические книги","parentName":"Книжная продукция и диски"},{"subjectID":8524,"parentID":786,"subjectName":"Карты Оракул","parentName":"Книжная продукция и диски"},{"subjectID":2337,"parentID":786,"subjectName":"Медиа на USB-накопителе","parentName":"Книжная продукция и диски"},{"subjectID":3647,"parentID":786,"subjectName":"Карты географические","parentName":"Книжная продукция и диски"},{"subjectID":381,"parentID":786,"subjectName":"Книги","parentName":"Книжная продукция и диски"}],"error":false,"errorText":"","additionalErrors":null}
    }
    @Test
    public void getProductCharacteristic() {
        String response = wildberriesSDKService.getProductCharacteristic(WildberriesConstant.TOKEN, 397);
//        {"data":[{"charcID":15000019,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Электронная версия","required":false,"unitName":"","maxCount":0,"popular":false,"charcType":1},{"charcID":15001137,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Дата регистрации сертификата/декларации","required":false,"unitName":"","maxCount":1,"popular":false,"charcType":1},{"charcID":15001650,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"ИКПУ","required":false,"unitName":"","maxCount":1,"popular":false,"charcType":1},{"charcID":88952,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Вес товара с упаковкой (г)","required":false,"unitName":"г","maxCount":0,"popular":false,"charcType":4},{"charcID":90673,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Ширина предмета","required":false,"unitName":"см","maxCount":0,"popular":false,"charcType":4},{"charcID":189099,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Год выпуска","required":false,"unitName":"","maxCount":3,"popular":false,"charcType":1},{"charcID":378533,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Комплектация","required":false,"unitName":"","maxCount":12,"popular":false,"charcType":1},{"charcID":14177453,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"SKU","required":false,"unitName":"","maxCount":0,"popular":false,"charcType":1},{"charcID":18269,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Вид бумаги","required":false,"unitName":"","maxCount":3,"popular":false,"charcType":1},{"charcID":239573,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"ISBN","required":false,"unitName":"","maxCount":3,"popular":false,"charcType":1},{"charcID":90630,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Высота предмета","required":false,"unitName":"см","maxCount":0,"popular":false,"charcType":4},{"charcID":14177451,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Страна производства","required":false,"unitName":"","maxCount":1,"popular":false,"charcType":1},{"charcID":15001135,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Номер декларации соответствия","required":false,"unitName":"","maxCount":1,"popular":false,"charcType":1},{"charcID":15001405,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Ставка НДС","required":false,"unitName":"","maxCount":1,"popular":false,"charcType":1},{"charcID":15001706,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Код упаковки","required":false,"unitName":"","maxCount":1,"popular":false,"charcType":1},{"charcID":19717,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Возрастные ограничения","required":false,"unitName":"","maxCount":3,"popular":false,"charcType":1},{"charcID":62431,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Жанры/тематика","required":false,"unitName":"","maxCount":3,"popular":false,"charcType":1},{"charcID":90702,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Количество предметов в упаковке (шт.)","required":false,"unitName":"шт.","maxCount":0,"popular":false,"charcType":4},{"charcID":15001136,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Номер сертификата соответствия","required":false,"unitName":"","maxCount":1,"popular":false,"charcType":1},{"charcID":15001138,"subjectName":"Журналы","subjectID":397,"parentName":"","name":"Дата окончания действия сертификата/декларации","required":false,"unitName":"","maxCount":1,"popular":false,"charcType":1}],"error":false,"errorText":"","additionalErrors":null}
    }

    @Test
    public void createProduct() {
//        CreateProductRequest request = CreateProductRequest.builder().build();
//        List<CreateProductRequest> requestList = Collections.singletonList(request);
        String requestStr = "[{\"subjectID\":105,\"variants\":[{\"vendorCode\":\"АртикулПродавца\",\"wholesale\":{\"enabled\":true,\"quantum\":211},\"title\":\"Наименование товара\",\"description\":\"Описание товара\",\"brand\":\"Бренд\",\"dimensions\":{\"length\":12,\"width\":7,\"height\":5,\"weightBrutto\":1.242},\"characteristics\":[{\"id\":12,\"value\":[\"Turkish flag\"]},{\"id\":25471,\"value\":1200},{\"id\":14177449,\"value\":[\"red\"]}],\"sizes\":[{\"techSize\":\"S\",\"wbSize\":\"42\",\"price\":5000,\"skus\":[\"L01\"]}]}]}]";
        List<CreateProductRequest> requestList = JSONUtil.toList(requestStr, CreateProductRequest.class);
        String response = wildberriesSDKService.createProduct(WildberriesConstant.TOKEN, requestList);

    }

    @Test
    public void getSkuList() {
        SkuRequest skuRequest = SkuRequest.builder()
                .settings(SkuRequest.Setting.builder()
                        .cursor(SkuRequest.Cursor.builder().limit(100).build())
                        .filter(SkuRequest.Filter.builder().withPhoto(-1).build())
                        .sort(SkuRequest.Sort.builder().ascending(Boolean.FALSE).build())
                        .build())
                .build();
        System.out.println(JSONUtil.toJsonStr(skuRequest));
        wildberriesSDKService.getSkuList(WildberriesConstant.TOKEN, skuRequest);
    }

    @Test
    public void listProductError() {
        ProductErrorRequest request = ProductErrorRequest.builder()
                .cursor(ProductErrorRequest.Cursor.builder().limit(100).build())
                .order(ProductErrorRequest.Order.builder().ascending(Boolean.FALSE).build())
                .build();
        System.out.println(JSONUtil.toJsonStr(request));
        String response = wildberriesSDKService.listProductError(WildberriesConstant.TOKEN, request);
        System.out.println(response);
    }
    @Test
    public void getTagList() {
        String response = wildberriesSDKService.getTagList(WildberriesConstant.TOKEN);
//        System.out.println(response);
    }

    @Test
    public void getOrderList() {
        Calendar specifiedTime = Calendar.getInstance();
        specifiedTime.set(2025, Calendar.OCTOBER, 01, 0, 0, 0);
        long dateFrom = specifiedTime.getTimeInMillis() / 1000;
        System.out.println("dateFrom："+ dateFrom);//1759248000

        specifiedTime.set(2025, Calendar.OCTOBER, 31, 0, 0, 0);
        long dateTo = specifiedTime.getTimeInMillis() / 1000;
        System.out.println("dateTo："+ dateTo);//1761840000

        OrderRequest orderRequest = OrderRequest.builder().limit(100).next(4017309858L).dateFrom(dateFrom).dateTo(dateTo).build();
        System.out.println(JSONUtil.toJsonStr(orderRequest));
        wildberriesSDKService.getOrderList(WildberriesConstant.TOKEN, orderRequest);
    }
    @Test
    public void getOrderNewList() {
        Calendar specifiedTime = Calendar.getInstance();
        specifiedTime.set(2025, Calendar.NOVEMBER, 01, 0, 0, 0);
        long dateFrom = specifiedTime.getTimeInMillis() / 1000;
        System.out.println("dateFrom："+ dateFrom);

        specifiedTime.set(2025, Calendar.NOVEMBER, 25, 0, 0, 0);
        long dateTo = specifiedTime.getTimeInMillis() / 1000;

        OrderRequest orderRequest = OrderRequest.builder().limit(100).next(0L).dateFrom(dateFrom).dateTo(dateTo).build();
        System.out.println(JSONUtil.toJsonStr(orderRequest));
        wildberriesSDKService.getOrderNewList(WildberriesConstant.TOKEN, orderRequest);
    }
    @Test
    public void getOrderStatus() {
        OrderStatusRequest request = OrderStatusRequest.builder().orders(Arrays.asList(8224L)).build();
        OrderStatusResponse orderStatus = wildberriesSDKService.getOrderStatus(WildberriesConstant.TOKEN, request);
        System.out.println(JSONUtil.toJsonStr(orderStatus));
    }
    @Test
    public void createOrder() {
        List<CreateOrderRequest.Order> orders = new ArrayList<>();
        CreateOrderRequest.Order order = CreateOrderRequest.Order.builder().build();
        order.setAmount(1);
        order.setSku("2850");
        orders.add(order);
        CreateOrderRequest request = CreateOrderRequest.builder().orders(orders).build();
        String supply = wildberriesSDKService.createOrder(WildberriesConstant.TOKEN, request);
        System.out.println(JSONUtil.toJsonStr(supply));
        //{"id":"WB-GI-SAND-7946"}
    }
    @Test
    public void createSupply() {
        CreateSupplyRequest request = CreateSupplyRequest.builder().name("XSDD251010000001").build();
        CreateSupplyResponse supply = wildberriesSDKService.createSupply(WildberriesConstant.TOKEN, request);
        System.out.println(JSONUtil.toJsonStr(supply));
        //{"id":"WB-GI-SAND-7946"}
    }

    @Test
    public void addBoxToSupply() {
        String supplyId = "WB-GI-SAND-7946";
        AddBoxToSupplyRequest request = AddBoxToSupplyRequest.builder().amount(1).build();
        AddBoxToSupplyResponse addBoxToSupplyResponse = wildberriesSDKService.addBoxToSupply(WildberriesConstant.TOKEN, supplyId, request);
        System.out.println(JSONUtil.toJsonStr(addBoxToSupplyResponse));
        //{"trbxIds":["WB-TRBX-SAND-7947"]}
    }

    @Test
    public void addOrderToSupply() {
        String supplyId = "WB-GI-SAND-7946";
        AddOrderToSupplyRequest request = AddOrderToSupplyRequest.builder().supplyId(supplyId).orderId(3916460244L).build();
        AddOrderToSupplyResponse addOrderToSupplyResponse = wildberriesSDKService.addOrderToSupply(WildberriesConstant.TOKEN, request);
        System.out.println(JSONUtil.toJsonStr(addOrderToSupplyResponse));
        //{"trbxIds":["WB-TRBX-SAND-7947"]}
    }
    @Test
    public void getOrderLabel() {
        OrderLabelRequest request = OrderLabelRequest.builder().orders(Arrays.asList(3893097985L))
                .width(58)
                .height(40)
                .type("png")
                .build();
        OrderLabelResponse orderLabel = wildberriesSDKService.getOrderLabel(WildberriesConstant.TOKEN, request);
        String pdfBase64 = null;
        try {
            pdfBase64 = PdfUtil.ImageToPdfBase64(orderLabel.getStickers().get(0).getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("===============================");
        System.out.println("data:application/pdf;base64," + pdfBase64);
    }
    @Test
    public void getCrossOrderLabel() {
        OrderLabelRequest request = OrderLabelRequest.builder().orders(Arrays.asList(3916460244L))
                .width(58)
                .height(40)
                .type("png")
                .build();
        CrossOrderLabelResponse crossOrderLabel = wildberriesSDKService.getCrossOrderLabel(WildberriesConstant.TOKEN, request);
//        String pdfBase64 = null;
//        try {
//            pdfBase64 = PdfUtil.ImageToPdfBase64(orderLabel.getStickers().get(0).getFile());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(pdfBase64);
    }
    @Test
    public void getSupplyLabel() {
        String supplyId = "WB-GI-182686515";
        SupplyLabelResponse supplyLabel = wildberriesSDKService.getSupplyLabel(WildberriesConstant.TOKEN, supplyId);
        System.out.println(JSONUtil.toJsonStr(supplyLabel));
        String pdfBase64 = null;
        try {
            pdfBase64 = PdfUtil.ImageToPdfBase64(supplyLabel.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("data:application/pdf;base64," + pdfBase64);
        //村粗到fastdfs 服务器
    }

    @Test
    public void getWarehouse() {
        String warehouse = wildberriesSDKService.getWarehouse(WildberriesConstant.TOKEN);
        System.out.println(JSONUtil.toJsonStr(warehouse));
    }
    @Test
    public void createWarehouse() {
        CreateWarehouseRequest request = CreateWarehouseRequest.builder().name("测试仓库").officeId(1).build();
        String warehouse = wildberriesSDKService.createWarehouse(WildberriesConstant.TOKEN, request);
        System.out.println(JSONUtil.toJsonStr(warehouse));
        //{"id":8173}
    }
    @Test
    public void updateInventory() {
        UpdateInventoryRequest request = UpdateInventoryRequest.builder().stocks(Collections.singletonList(UpdateInventoryRequest.Stock.builder().sku("88005553535").amount(1000).build())).build();
        String warehouse = wildberriesSDKService.updateInventory(WildberriesConstant.TOKEN, "8173", request);
        System.out.println(JSONUtil.toJsonStr(warehouse));
        //{"id":8173}
    }
    @Test
    public void getInventory() {
        GetInventoryRequest request = GetInventoryRequest.builder().skus(Collections.singletonList("88005553535")).build();
        String warehouse = wildberriesSDKService.getInventory(WildberriesConstant.TOKEN, "8173", request);
        System.out.println(JSONUtil.toJsonStr(warehouse));
        //{"id":8173}
    }
    @Test
    public void getOffices() {
        String warehouse = wildberriesSDKService.getOffices(WildberriesConstant.TOKEN);
        System.out.println(JSONUtil.toJsonStr(warehouse));
    }
    @Test
    public void getOfficeForPass() {
        String warehouse = wildberriesSDKService.getOfficeForPass(WildberriesConstant.TOKEN);
        System.out.println(JSONUtil.toJsonStr(warehouse));
    }

}
