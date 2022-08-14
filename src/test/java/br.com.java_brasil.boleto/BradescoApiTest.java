package br.com.java_brasil.boleto;

import br.com.java_brasil.boleto.exception.BoletoException;
import br.com.java_brasil.boleto.model.*;
import br.com.java_brasil.boleto.model.enums.AmbienteEnum;
import br.com.java_brasil.boleto.service.BoletoService;
import br.com.java_brasil.boleto.service.bancos.bradesco_api.ConfiguracaoBradescoAPI;
import br.com.java_brasil.boleto.util.ValidaUtils;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class BradescoApiTest {

    private BoletoService boletoService;

    @BeforeEach
    public void configuraTeste() {
        Logger rootLog = Logger.getLogger("");
        rootLog.setLevel( Level.CONFIG );
        rootLog.getHandlers()[0].setLevel( Level.CONFIG );

        ConfiguracaoBradescoAPI configuracao = new ConfiguracaoBradescoAPI();
        configuracao.setClientId("9c228ae2-6277-4a8c-a26b-51223a0aaa09");
        configuracao.setCpfCnpj("38052160005701");
        configuracao.setAmbiente(AmbienteEnum.HOMOLOGACAO);
        configuracao.setCaminhoCertificado("d:/teste/bradesco.pem");
        boletoService = new BoletoService(BoletoBanco.BRADESCO_API, configuracao);
    }

    @Test
    @DisplayName("Testa Erro Configuracoes")
    void testaErroConfiguracoes() {
        ConfiguracaoBradescoAPI configuracao = (ConfiguracaoBradescoAPI) boletoService.getConfiguracao();
        configuracao.setClientId(null);
        Throwable exception =
                assertThrows(BoletoException.class, () -> ValidaUtils.validaConfiguracao(configuracao));
        assertEquals("Campo clientId não pode estar vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Testa Impressão Boleto")
    void testeImprimirBoleto() {
        // Model Null
        assertThrows(NullPointerException.class, () -> boletoService.imprimirBoletoBanco(null));

        // teste Sucesso (Não implementado)
        Throwable exception =
                assertThrows(BoletoException.class, () -> boletoService.imprimirBoletoBanco(new BoletoModel()));
        assertEquals("Não implementado!", exception.getMessage());

    }

    @Test
    @DisplayName("Testa Valida e Envia Boleto")
    void testaEnvioBoleto() {
        BoletoModel boletoModel = preencheBoleto();
        ValidaUtils.validaBoletoModel(boletoModel, this.boletoService.getConfiguracao().camposObrigatoriosBoleto());
        BoletoModel retorno = boletoService.enviarBoleto(boletoModel);
        System.out.println(retorno.getCodRetorno() + " - " + retorno.getMensagemRetorno());
        System.out.println(retorno.getCodigoBarras());
//
//        byte[] bytes = boletoService.imprimirBoleto(retorno);
        // SALVAR PDF

    }

    private BoletoModel preencheBoleto() {
        BoletoModel boleto = new BoletoModel();
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setAgencia("3995");
        beneficiario.setDigitoAgencia("0");
        beneficiario.setDocumento("38052160005701");
        beneficiario.setConta("75557");
        beneficiario.setDigitoConta("5");
        beneficiario.setCarteira("9");
        beneficiario.setNossoNumero("2336835");
        boleto.setBeneficiario(beneficiario);

        Pagador pagador = new Pagador();
        pagador.setNome("SAMUEL BORGES DE OLIVEIRA");
        pagador.setDocumento("01713390108"); // <- PIX
        pagador.setCodigo("999");
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Endereco Teste");
        endereco.setNumero("0");
        endereco.setBairro("Centro");
        endereco.setComplemento("Qd 0 Lote 0");
        endereco.setCep("75120683");
        endereco.setCidade("ANAPOLIS");
        endereco.setUf("GO");
        pagador.setEndereco(endereco);
        boleto.setPagador(pagador);

        boleto.setValorBoleto(BigDecimal.TEN);
        boleto.setDataVencimento(LocalDate.of(2022, 5, 30));

        return boleto;
    }

}
