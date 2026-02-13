package br.com.leandrocoelho.backend.service;

import br.com.leandrocoelho.backend.model.Account;
import br.com.leandrocoelho.backend.model.Category;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.repository.AccountRepository;
import br.com.leandrocoelho.backend.repository.UserRepository;
import br.com.leandrocoelho.backend.service.importation.OfxParserService;
import br.com.leandrocoelho.backend.service.rule.TransactionClassifier; // Sua IA
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionImportService {

    private final OfxParserService ofxParserService;
    private final CoreTransactionService coreTransactionService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionClassifier classifier; // <--- IA para categorizar

    @Transactional
    public int importOfxFile(UUID userId, UUID accountId, MultipartFile file) {
        log.info("Iniciando importação OFX para usuário {} e conta {}", userId, accountId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        try {
            // 1. Parseia o arquivo
            List<Transaction> transactions = ofxParserService.parse(file.getInputStream(), user, account);

            if (transactions.isEmpty()) return 0;

            // 2. Passa a IA em cada transação (Categorização)
            transactions.forEach(tx -> {
                // A IA define Tipo (Cred/Deb) e tenta achar a Categoria
                // Aqui estou simplificando, mas você pode usar seu coreCategoryService.resolveBatch se quiser
                // tx.setCategory(classifier.predictCategory(tx.getDescription()));
            });

            // 3. Salva usando o CORE (que tem a proteção de duplicidade e Range de datas!)
            List<Transaction> saved = coreTransactionService.saveTransactionsBatch(transactions);

            log.info("Importação concluída. {} transações processadas/salvas.", saved.size());
            return saved.size();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo enviado: " + e.getMessage());
        }
    }
}