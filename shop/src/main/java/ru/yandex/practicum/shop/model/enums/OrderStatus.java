package ru.yandex.practicum.shop.model.enums;

public enum OrderStatus {
    /**
     * Заказ создан, для корзины
     * */
    CREATE,
    /**
     * В обработке, после нажатия кнопки, оформить заказ
     * */
    IN_PROGRESS,

    /**
     * Завершен, устанавливается шедулером
     * */
    CLOSED
}
