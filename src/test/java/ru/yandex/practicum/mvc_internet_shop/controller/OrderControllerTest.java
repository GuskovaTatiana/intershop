package ru.yandex.practicum.mvc_internet_shop.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mvc_internet_shop.model.enums.OrderStatus;
import ru.yandex.practicum.mvc_internet_shop.service.OrderService;
import ru.yandex.practicum.mvc_internet_shop.utils.TestDataUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @MockitoBean
    private OrderService orderService;
    @Autowired
    private MockMvc mockMvc;

    private TestDataUtils testData = new TestDataUtils();

    //Оформление заказа
    @Test
    void createOrder_shouldReturnHtmlWithOrderInfo() throws Exception  {
        Mockito.when(orderService.addNewOrder()).thenReturn(testData.getOrder(1, OrderStatus.CLOSED, testData.getListProduct().getContent()));
        mockMvc.perform(post("/orders" ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1"));
    }

    //Получение списка заказов getOrders
    @Test
    void getOrders_shouldReturnHtmlWithListOrder() throws Exception  {
        Mockito.when(orderService.findAllCompletedOrder()).thenReturn(testData.getListOrder());
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(xpath("//div[@id='order-list']/div").nodeCount(3));
    }

    //Получение заказа по идентификатору getOrders
    @Test
    void getOrderById_shouldReturnHtmlWithOrderInfo() throws Exception  {
        Mockito.when(orderService.findById(any())).thenReturn(testData.getOrder(1, OrderStatus.CLOSED, testData.getListProduct().getContent()));
        mockMvc.perform(get("/orders/{orderId}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(xpath("//div[@id='order-title']/a").string("Заказ № 1"));
    }
}
