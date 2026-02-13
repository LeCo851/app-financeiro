package br.com.leandrocoelho.backend.service.importation;

import br.com.leandrocoelho.backend.model.Account;
import br.com.leandrocoelho.backend.model.Transaction;
import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.model.enums.TransactionSource;
import br.com.leandrocoelho.backend.model.enums.TransactionType;
import com.webcohesion.ofx4j.domain.data.ResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.ResponseMessage;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.common.TransactionList;
import com.webcohesion.ofx4j.domain.data.creditcard.CreditCardStatementResponseTransaction;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OfxParserService {

    public List<Transaction> parse(InputStream fileStream, User user, Account account) {
        try {
            AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<>(ResponseEnvelope.class);
            ResponseEnvelope envelope = unmarshaller.unmarshal(fileStream);

            // 1. CORREÇÃO: SortedSet usa .first() e não .getFirst() ou .get(0)
            if (envelope.getMessageSets() == null || envelope.getMessageSets().isEmpty()) {
                throw new RuntimeException("Arquivo OFX vazio ou inválido (sem MessageSets).");
            }

            // PEGA O PRIMEIRO CONJUNTO DE MENSAGENS
            TransactionList ofxTransactions = getTransactionList(envelope);

            if (ofxTransactions == null) return new ArrayList<>();

            List<Transaction> transactions = new ArrayList<>();
            // Itera sobre as transações do OFX
            for (com.webcohesion.ofx4j.domain.data.common.Transaction ofxTx : ofxTransactions.getTransactions()) {
                transactions.add(mapToEntity(ofxTx, user, account));
            }

            return transactions;

        } catch (Exception e) {
            log.error("Erro ao ler arquivo OFX", e);
            throw new RuntimeException("Falha ao processar arquivo OFX: " + e.getMessage());
        }
    }

    private static TransactionList getTransactionList(ResponseEnvelope envelope) {
        ResponseMessageSet messageSet = envelope.getMessageSets().first();

        if (messageSet.getResponseMessages() == null || messageSet.getResponseMessages().isEmpty()) {
            throw new RuntimeException("Arquivo OFX sem respostas de banco.");
        }

        // A lista de respostas é uma List, então aqui o .get(0) funciona
        ResponseMessage responseMessage = messageSet.getResponseMessages().get(0);

        // 2. Identifica se é Conta Corrente ou Cartão de Crédito
        TransactionList ofxTransactions = null;

        // Tenta ler como Conta Bancária (Checking/Savings)
        if (responseMessage instanceof BankStatementResponseTransaction) {
            BankStatementResponseTransaction bankTx = (BankStatementResponseTransaction) responseMessage;
            ofxTransactions = bankTx.getMessage().getTransactionList();
        }
        // Tenta ler como Cartão de Crédito
        else if (responseMessage instanceof CreditCardStatementResponseTransaction) {
            CreditCardStatementResponseTransaction ccTx = (CreditCardStatementResponseTransaction) responseMessage;
            ofxTransactions = ccTx.getMessage().getTransactionList();
        } else {
            throw new RuntimeException("Tipo de extrato OFX não suportado (apenas Banco ou Cartão).");
        }
        return ofxTransactions;
    }

    private Transaction mapToEntity(com.webcohesion.ofx4j.domain.data.common.Transaction ofxTx, User user, Account account) {
        BigDecimal amount = BigDecimal.valueOf(ofxTx.getAmount());
        TransactionType type = amount.compareTo(BigDecimal.ZERO) >= 0 ? TransactionType.INCOME : TransactionType.EXPENSE;

        return Transaction.builder()
                .user(user)
                .account(account)
                .description(ofxTx.getMemo() != null ? ofxTx.getMemo() : "Sem descrição")
                .originalDescription(ofxTx.getMemo())
                .amount(amount.abs()) // Armazenamos o valor absoluto, o tipo define se é entrada ou saída
                .date(ZonedDateTime.ofInstant(ofxTx.getDatePosted().toInstant(), ZoneId.systemDefault()))
                .type(type)
                .status("POSTED") // OFX geralmente são transações já efetivadas
                .source(TransactionSource.OFX)
                .transactionHash(ofxTx.getId()) // Usamos o ID do OFX como hash para evitar duplicidade
                .build();
    }
}
