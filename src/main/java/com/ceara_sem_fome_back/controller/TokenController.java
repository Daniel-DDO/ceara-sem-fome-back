package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.service.CadastroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
public class TokenController {

    // [ALTERADO] Injeta o novo serviço de cadastro
    @Autowired
    private CadastroService cadastroService;

    /**
     * Endpoint final que o usuário clica no e-mail de verificação.
     * Delega ao CadastroService a tarefa de validar o token e criar o beneficiário.
     */
    @GetMapping("/confirmar-cadastro")
    public ResponseEntity<String> confirmarCadastro(@RequestParam String token) {
        // [ALTERADO] Chama o método do serviço correto
        boolean sucesso = cadastroService.verificarEFinalizarCadastro(token);

        String logoCearaSemFome = "https://www.ceara.gov.br/wp-content/uploads/2024/01/logo-cesf-e-cegov-e1704803051849-600x239.png";
        String logoGovernoCeara = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fe/Bras%C3%A3o_do_Cear%C3%A1.svg/500px-Bras%C3%A3o_do_Cear%C3%A1.svg.png";

        if (sucesso) {
            String htmlSuccess = 
                "<html>"
                + "<head><meta charset=\"UTF-8\"><title>Cadastro Concluído</title></head>"
                + "<body style=\"font-family: Arial, sans-serif; text-align: center; margin: 0; padding: 0; background: linear-gradient(to bottom, #E8F5E9, #F5F5F5);\">"
                + "<div style=\"padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: 20px auto; padding: 30px; background-color: #fff; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">"
                + "<div style=\"text-align: left; padding-bottom: 20px; border-bottom: 1px solid #eee;\">"
                + "<img src=\"" + logoCearaSemFome + "\" alt=\"Ceará sem Fome\" style=\"height: 40px; margin-right: 20px;\">"
                + "<img src=\"" + logoGovernoCeara + "\" alt=\"Governo do Estado do Ceará\" style=\"height: 40px;\">"
                + "</div>"
                + "<h1 style=\"color: #333; font-size: 28px; margin-top: 30px;\">Cadastro Realizado com Sucesso!</h1>"
                + "<h2 style=\"color: #28a745; font-size: 22px; font-weight: bold; margin: 15px 0;\">Sua conta foi ativada.</h2>"
                + "<p style=\"font-size: 16px; color: #555;\">Você já pode fechar esta página e acessar o aplicativo.</p>"
                + "</div></div></body></html>";
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlSuccess);
        } else {
            String htmlError = 
                "<html>"
                + "<head><meta charset=\"UTF-8\"><title>Erro no Cadastro</title></head>"
                + "<body style=\"font-family: Arial, sans-serif; text-align: center; margin: 0; padding: 0; background: linear-gradient(to bottom, #fde8e8, #F5F5F5);\">"
                + "<div style=\"padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: 20px auto; padding: 30px; background-color: #fff; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">"
                + "<div style=\"text-align: left; padding-bottom: 20px; border-bottom: 1px solid #eee;\">"
                + "<img src=\"" + logoCearaSemFome + "\" alt=\"Ceará sem Fome\" style=\"height: 40px; margin-right: 20px;\">"
                + "<img src=\"" + logoGovernoCeara + "\" alt=\"Governo do Estado do Ceará\" style=\"height: 40px;\">"
                + "</div>"
                + "<h1 style=\"color: #dc3545; font-size: 28px; margin-top: 30px;\">Erro no Cadastro</h1>"
                + "<h2 style=\"font-size: 22px; font-weight: bold; color: #555;\">Token Inválido ou Expirado.</h2>"
                + "<p style=\"font-size: 16px; color: #555;\">O link de ativação não é mais válido. Por favor, tente se cadastrar novamente.</p>"
                + "</div></div></body></html>";
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_HTML).body(htmlError);
        }
    }
}

