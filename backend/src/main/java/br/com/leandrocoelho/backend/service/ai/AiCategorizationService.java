package br.com.leandrocoelho.backend.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiCategorizationService {

    private final ChatClient chatClient;

    private static final String CATEGORIES_LIST = String.join(", ",
            "Alimentação", "Transporte", "Moradia", "Lazer", "Saúde",
            "Educação", "Compras", "Serviços", "Investimentos",
            "Transferência", "Receita", "Impostos","Pet", "Outros");

    public AiCategorizationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String categorize(String description, String merchantName) {
        // Se não tiver dados, desiste logo
        if (description == null && merchantName == null) return "Outros";

        // Prompt Otimizado para o Llama 3
        String prompt = String.format("""
                Atue como um classificador financeiro.
                Analise a transação abaixo e escolha a MELHOR categoria desta lista: [%s].

                Dados da Transação:
                - Descrição: "%s"
                - Estabelecimento: "%s"

                Regras:
                1. Retorne APENAS a palavra da categoria. Sem frases. Sem pontos.
                2. Se for ambíguo, use o bom senso (Ex: Uber -> Transporte, Uber Eats -> Alimentação).
                3. Se impossível determinar, retorne "Outros".
                """, CATEGORIES_LIST, description, merchantName);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return cleanResponse(response);
        } catch (Exception e) {
            // Em caso de erro (sem internet, chave inválida), fallback seguro
            log.error("Erro ao chamar Groq AI: " + e.getMessage());
            return "Outros";
        }
    }

    public Map<String, String> categorizeBatch(Map<String, String> transactionsContext) {
        // Monta a lista visualmente para o prompt
        String itemsList = transactionsContext.entrySet().stream()
                .map(e -> String.format("- ID: %s | Dados: %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            Atue como um classificador financeiro especialista.
            Sua tarefa é analisar uma lista de transações e definir a categoria correta para CADA uma.
            
            Categorias permitidas: %s.
            
            Regras de Classificação:
            1. Use a 'Loja' e a 'Desc' para identificar o gasto.
            2. Se for 'Amazon', 'Mercado Livre', verifique se parece compra ou assinatura.
            3. 'Uber' e '99' -> Transporte. 'Uber Eats' -> Alimentação.
            4. Se não souber, use "Outros".
            
            Formato de Saída (JSON Puro):
            Retorne APENAS um objeto JSON onde a chave é o ID e o valor é a Categoria.
            NÃO escreva introduções como "Aqui está o JSON".
            
            Exemplo de Saída:
            {
              "id_123": "Alimentação",
              "id_456": "Transporte"
            }

            Itens para classificar:
            %s
            """, CATEGORIES_LIST, itemsList);

        try {
            String response = chatClient.prompt().user(prompt).call().content();

            // PASSO CRÍTICO: Limpeza da resposta antes de tentar converter
            String cleanJson = cleanResponse(response);

            return parseJsonToMap(cleanJson);

        } catch (Exception e) {
            log.error("Erro no batch do Groq: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, String> parseJsonToMap(String json){
        Map<String, String> result = new HashMap<>();

        String clean = json.replace("{", "").replace("}", "").replace("\n", "").trim();

        if(clean.isBlank()) return result;
        String [] pairs = clean.split(",");
        for (String pair: pairs){
            String[] parts = pair.split(":");
            if (parts.length == 2){
                String key = parts[0].trim().replace("\"", "");
                String value = parts[1].trim().replace("\"", "");

                if(!CATEGORIES_LIST.contains(value)) value = "Outros";
                result.put(key, value);
            }
        }
        return result;

    }

    private String cleanResponse(String response) {
        if (response == null) return "{}";

        String cleaned = response.replaceAll("```json", "").replaceAll("```", "");

        // Remove qualquer texto antes do primeiro '{' ou depois do último '}'
        int firstBrace = cleaned.indexOf("{");
        int lastBrace = cleaned.lastIndexOf("}");

        if (firstBrace != -1 && lastBrace != -1) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned.trim();
    }
}
