package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.model.Comunicado;
import com.ceara_sem_fome_back.service.ComunicadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/comunicado")
public class ComunicadoController {

    @Autowired
    private ComunicadoService comunicadoService;

    @GetMapping("/listar")
    public ResponseEntity<List<Comunicado>> listar() {
        return ResponseEntity.ok(comunicadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        return comunicadoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable String id) {
        comunicadoService.deletar(id);
        return ResponseEntity.ok("Comunicado deletado com sucesso");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Comunicado comunicado) {
        if (!comunicadoService.buscarPorId(id).isPresent()) {
            return ResponseEntity.status(404).body("Comunicado não encontrado");
        }
        comunicado.setId(id);
        Comunicado atualizado = comunicadoService.atualizar(comunicado);
        return ResponseEntity.ok(atualizado);
    }

}