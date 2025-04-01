package skytales.Payments.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import skytales.Payments.web.dto.StockChange;
import skytales.Payments.service.PaymentService;

@RestController
@RequestMapping("api/payments")
public class

StockController {

    private final PaymentService paymentService;

    public StockController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


}
