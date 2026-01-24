package br.com.leandrocoelho.backend.dto.request; // Ajuste se seu pacote for diferente

import com.fasterxml.jackson.annotation.JsonProperty;

public class SyncRequestDto {

    @JsonProperty("itemId")
    private String itemId;

    // Construtor vazio (Obrigatório para o Jackson funcionar sem erros)
    public SyncRequestDto() {
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}