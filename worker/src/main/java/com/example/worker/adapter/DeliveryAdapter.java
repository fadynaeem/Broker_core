package com.example.worker.adapter;

import com.example.shared.model.DeliveryResult;
import com.example.shared.event.PaymentEvent;
public interface DeliveryAdapter {
    DeliveryResult deliver(PaymentEvent message);
}
