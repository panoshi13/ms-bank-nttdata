package com.ntt.data.ms.client.controller;

/*
import com.ntt.data.ms.client.dto.ProductDTO;
import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;

    // Crear un cliente
    @PostMapping
    public Mono<Customer> createClient(@RequestBody Customer customer) {
        return customerService.create(customer);
    }


    // Obtener todos los clientes
    @GetMapping
    public Flux<Customer> getAllClients() {
        return customerService.findAll();
    }

    // Obtener un cliente
    @GetMapping("/{id}")
    public Mono<Customer> getCustomerById(@PathVariable String id) {
        return customerService.findById(id);
    }

    // Actualzar un cliente
    @PutMapping
    public Mono<Customer> updateClient(@RequestBody Customer customer) {
        return customerService.update(customer);
    }

    // Eliminar un cliente por su id
    @DeleteMapping("/{id}")
    public Mono<String> deleteClient(@PathVariable String id) {
        return customerService.delete(id);
    }

    @GetMapping("/{clientId}/products/{productId}/movements")
    public Mono<ProductDTO> getMovement(@PathVariable String clientId, @PathVariable String productId){
        return customerService.getMovement(clientId,productId);
    }

}

 */