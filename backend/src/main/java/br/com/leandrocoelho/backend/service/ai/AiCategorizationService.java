package br.com.leandrocoelho.backend.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiCategorizationService {

    private final ChatClient chatClient;

    private static final String CATEGORIES_LIST = String.join(", ",
            "Alimentação", "Transporte", "Moradia", "Lazer", "Saúde",
            "Educação", "Compras", "Serviços", "Investimentos",
            "Transferência", "Receita", "Impostos", "Outros");

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
            System.err.println("Erro ao chamar Groq AI: " + e.getMessage());
            return "Outros";
        }
    }

    private String cleanResponse(String response) {
        if (response == null) return "Outros";

        String cleaned = response.trim()
                .replace(".", "")
                .replace("\"", "")
                .replace("Categoria: ", "");

        // Validação final: Se a IA alucinou uma categoria nova, forçamos "Outros"
        if (!CATEGORIES_LIST.contains(cleaned)) {
            return "Outros";
        }
        return cleaned;
    }
}
