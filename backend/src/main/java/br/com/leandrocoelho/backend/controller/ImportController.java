package br.com.leandrocoelho.backend.controller;

import br.com.leandrocoelho.backend.model.User;
import br.com.leandrocoelho.backend.service.TransactionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions/import")
@RequiredArgsConstructor
public class ImportController {

    private final TransactionImportService importService;

    @PostMapping("/ofx")
    public ResponseEntity<?> uploadOfx(
            @AuthenticationPrincipal User user, // Ou pegue o ID do contexto
            @RequestParam("accountId") UUID accountId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo inválido");
        }

        int count = importService.importOfxFile(user.getId(), accountId, file);

        return ResponseEntity.ok(Map.of(
                "message", "Importação realizada com sucesso",
                "importedCount", count
        ));
    }
}