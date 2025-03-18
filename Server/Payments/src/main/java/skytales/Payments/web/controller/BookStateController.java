package skytales.Payments.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import skytales.Payments.web.dto.StockChange;
import skytales.Payments.model.BookState;
import skytales.Payments.service.PaymentService;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/payments")
public class

BookStateController {

    private final PaymentService paymentService;
    private final BookState bookState;

    public BookStateController(PaymentService paymentService, BookState bookState) {
        this.paymentService = paymentService;
        this.bookState = bookState;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateBookState(@RequestBody StockChange stockChange) {

        paymentService.addBookToState(stockChange.id(), stockChange.quantity());
        return ResponseEntity.ok("Book state updated successfully");
    }

    @GetMapping("/check_local_state")
    public ResponseEntity<Map<UUID, BookState.BookDetails>> getBookState() {
        return ResponseEntity.ok(paymentService.getBookState());
    }
}
