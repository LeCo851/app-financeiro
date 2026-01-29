package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.repository.TransactionRepository;
import br.com.leandrocoelho.backend.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoreTransactionService {

    private final TransactionRepository repository;

    @Transactional(readOnly = true)
    public List<Transaction> listMyTransactions(){

        UUID userId = UserContext.getCurrentUserId();
        return  repository.findByUser_IdOrderByDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listTransactionsByUser(UUID userId) {
        return repository.findByUser_IdOrderByDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listTransactionsByUserAndMonth(UUID userId, Integer year, Integer month) {
        if (year == null || month == null) {
            return listTransactionsByUser(userId);
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        ZonedDateTime startZoned = start.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endZoned = end.atTime(23, 59, 59).atZone(ZoneId.systemDefault());

        return repository.findByUser_IdAndDateBetweenOrderByDateDesc(userId, startZoned, endZoned);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(UUID userId) {
        // Saldo até o momento atual (ZonedDateTime.now())
        return repository.calculateBalanceUntilDate(userId, ZonedDateTime.now());
    }

    @Transactional
    public Transaction createTransaction(Transaction newTransaction){

        if(newTransaction.getUser() == null){
            throw new IllegalArgumentException("A transação deve estar vinculada a um usuário");
        }

        UUID userId = newTransaction.getUser().getId();

        if(newTransaction.getSource() == null){
            newTransaction.setSource(TransactionSource.MANUAL);
        }

        if (newTransaction.getPluggyTransactionId() != null) {
            // 1. Busca no banco para ver se já existe
            var existingOpt = repository.findByPluggyTransactionId(newTransaction.getPluggyTransactionId());

            if (existingOpt.isPresent()) {
                // --- CENÁRIO: ATUALIZAÇÃO (UPDATE) ---
                Transaction existing = existingOpt.get();

                // Atualizamos apenas os campos que podem mudar vindo do banco
                existing.setDescription(newTransaction.getDescription());
                existing.setAmount(newTransaction.getAmount());
                existing.setDate(newTransaction.getDate()); // Data pode mudar (ajuste de fuso ou compensação)
                existing.setStatus(newTransaction.getStatus()); // Ex: PENDING -> POSTED
                existing.setCategory(newTransaction.getCategory()); // Categoria pode ter vindo nova

                // Campos ricos (se você estiver preenchendo)
                existing.setMerchantName(newTransaction.getMerchantName());
                existing.setPaymentMethod(newTransaction.getPaymentMethod());

                // O Hibernate percebe que 'existing' tem um ID e faz um UPDATE ao salvar
                return repository.save(existing);
            }
            // Se não entrou no if, segue o fluxo para criar uma NOVA (INSERT)
        }

        if(newTransaction.getTransactionHash() == null){
            newTransaction.setTransactionHash(UUID.randomUUID().toString());
        }

        if(newTransaction.getTransactionHash() != null){
            boolean exists = repository.existsByUser_IdAndTransactionHash(userId, newTransaction.getTransactionHash());
            if(exists){
                throw new IllegalStateException("Transação manual duplicada detectada (Hash).");
            }
        }
        return repository.save(newTransaction);
    }
}
