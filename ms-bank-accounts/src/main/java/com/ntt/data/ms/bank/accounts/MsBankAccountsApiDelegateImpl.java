package com.ntt.data.ms.bank.accounts;

/*
import com.ntt.data.ms.bank.accounts.api.CuentasApiDelegate;
import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import com.ntt.data.ms.bank.accounts.entity.Currency;
import com.ntt.data.ms.bank.accounts.entity.AccountType;
import com.ntt.data.ms.bank.accounts.model.CuentaRequest;
import com.ntt.data.ms.bank.accounts.model.CuentaResponse;
import com.ntt.data.ms.bank.accounts.model.Movimiento;
import com.ntt.data.ms.bank.accounts.service.BankAccountService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springfox.documentation.annotations.ApiIgnore;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Component
public class MsBankAccountsApiDelegateImpl implements CuentasApiDelegate {
    @Autowired
    private BankAccountService cuentaService;

    @Override
    public Mono<ResponseEntity<CuentaResponse>> crearCuentasBancarias(Mono<CuentaRequest> cuentaRequest, ServerWebExchange exchange) {
        return cuentaRequest
                .map(this::mapToCuenta) // Convertir CuentaRequest a BankAccount
                .flatMap(cuentaService::create) // Llamar al servicio
                .map(this::mapToCuentaResponse) // Convertir BankAccount a CuentaResponse
                .map(ResponseEntity::ok); // Envolver en ResponseEntity
    }

    private CuentaResponse mapToCuentaResponse(BankAccount cuenta) {
        CuentaResponse response = new CuentaResponse();

        // Mapear atributos
        response.setTipo(cuenta.getTipo().name()); // Convertir Enum a String
        response.setClienteId(cuenta.getClienteId().toHexString()); // Convertir ObjectId a String
        response.setSaldo(cuenta.getSaldo());
        response.setMoneda(String.valueOf(cuenta.getMoneda()));
        response.setFechaApertura(OffsetDateTime.from(cuenta.getFechaApertura()));
        response.setComisionMantenimiento(cuenta.getComisionMantenimiento());
        response.setLimiteMovimientos(cuenta.getLimiteMovimientos());

        // Manejar listas (evitar nulls)
        response.setMovimientos(
                cuenta.getMovimientos() != null
                        ? cuenta.getMovimientos().stream()
                        .map(this::convertirMovimiento) // Método que convierte la entidad en modelo
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        );
        response.setTitulares(cuenta.getTitulares() != null ? new ArrayList<>(cuenta.getTitulares()) : new ArrayList<>());
        response.setFirmantesAutorizados(cuenta.getFirmantesAutorizados() != null ? new ArrayList<>(cuenta.getFirmantesAutorizados()) : new ArrayList<>());

        return response;
    }

    private Movimiento convertirMovimiento(com.ntt.data.ms.bank.accounts.entity.Movimiento entityMovimiento) {
        Movimiento modelMovimiento = new Movimiento();
        modelMovimiento.setMonto(entityMovimiento.getMonto());
        modelMovimiento.setFecha(OffsetDateTime.from(entityMovimiento.getFecha()));
        // Agregar más campos si es necesario
        return modelMovimiento;
    }


    private BankAccount mapToCuenta(CuentaRequest cuentaRequest) {
        BankAccount cuenta = new BankAccount();

        // Convertir clienteId (String) a ObjectId
        cuenta.setClienteId(new ObjectId(cuentaRequest.getClienteId()));

        // Mapear los demás atributos
        cuenta.setTipo(AccountType.valueOf(cuentaRequest.getTipo().toUpperCase()));
        cuenta.setMoneda(Currency.valueOf(cuentaRequest.getMoneda().toUpperCase()));
        return cuenta;
    }


    @Override
    public Mono<ResponseEntity<Flux<CuentaResponse>>> listarCuentasBancarias(@ApiIgnore ServerWebExchange exchange) {
        return cuentaService.getAll()
                .map(this::mapToCuentaResponse) // Convertir BankAccount a CuentaResponse
                .collectList() // Convertimos el Flux en una lista temporalmente
                .map(lista -> ResponseEntity.ok(Flux.fromIterable(lista))); // Convertimos la lista de vuelta a Flux y la envolvemos en ResponseEntity
    }
}

 */
