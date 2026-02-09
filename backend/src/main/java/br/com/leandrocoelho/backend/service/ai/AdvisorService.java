package br.com.leandrocoelho.backend.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdvisorService {

    private final ChatClient chatClient;
    private final FinancialAdvisorContextService financialAdvisorContextService;

    @Value("${app.ai.models.advisor}")
    private String advisorModel;

    public AdvisorService(ChatClient.Builder chatClientBuilder, FinancialAdvisorContextService financialAdvisorContextService) {
        this.chatClient = chatClientBuilder.build();
        this.financialAdvisorContextService = financialAdvisorContextService;
    }

    private static final String SYSTEM_PROMPT =
"""
        Você é um Personal Financial Advisor (CFP) de elite.
        Sua missão é otimizar a saúde financeira do usuário, não apenas responder "sim" ou "não".
        
        ESTRATÉGIA DE ANÁLISE:
        1. **Analise os Gastos por Categoria**: Se o usuário perguntar sobre comprar algo supérfluo e a categoria "Lazer" ou "Restaurantes" já for alta, ALERTE-O sobre o desequilíbrio.
        2. **Contexto Recente**: Olhe as 'Últimas Movimentações'. Se ele acabou de gastar muito ontem, sugira segurar a onda hoje.
        3. **Regra 50/30/20**: Tente guiar o usuário para 50% Fixos, 30% Variáveis/Lazer, 20% Investimentos.
        4. **Patrimônio**: Se ele tiver muitos investimentos mas pouco caixa, explique o custo de oportunidade de desinvestir (impostos, perda de juros).
        
        TOM DE VOZ:
        - Profissional, direto, mas empático.
        - Use formatação Markdown (negrito, listas) para facilitar a leitura.
        - Se a situação for ruim, seja franco: "Sua renda está 80% comprometida, pare de gastar agora."
        """;

    public String getAdvice(UUID userId, String userQuestion){
        String financialContext = financialAdvisorContextService.buildAdvisorContext(userId);

        String userText =
                        """
                        CONTEXTO DO USUÁRIO:
                        {context}
                        
                        PERGUNTA DO USUÁRIO:
                        {question}
                        """;

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(u -> u.text(userText
)
                        .param("context",financialContext)
                        .param("question",userQuestion)
                )
                .options(OpenAiChatOptions.builder()
                        .model(advisorModel)
                        .temperature(0.6)
                        .build())
                .call()
                .content();
    }
}
