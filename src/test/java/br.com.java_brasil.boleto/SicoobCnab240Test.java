package br.com.java_brasil.boleto;

import br.com.java_brasil.boleto.exception.BoletoException;
import br.com.java_brasil.boleto.model.*;
import br.com.java_brasil.boleto.model.enums.TipoDescontoEnum;
import br.com.java_brasil.boleto.model.enums.TipoJurosEnum;
import br.com.java_brasil.boleto.model.enums.TipoMultaEnum;
import br.com.java_brasil.boleto.service.BoletoService;
import br.com.java_brasil.boleto.service.bancos.sicoob_cnab240.ConfiguracaoSicoobCnab240;
import br.com.java_brasil.boleto.service.bancos.sicoob_cnab240.SicoobUtil;
import br.com.java_brasil.boleto.util.BoletoUtil;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class SicoobCnab240Test {

    private BoletoService boletoService;

    @BeforeEach
    public void configuraTeste() {
        Logger rootLog = Logger.getLogger("");
        rootLog.setLevel( Level.CONFIG );
        rootLog.getHandlers()[0].setLevel( Level.CONFIG );

        ConfiguracaoSicoobCnab240 configuracao = new ConfiguracaoSicoobCnab240();
        boletoService = new BoletoService(BoletoBanco.SICOOB_CNAB240, configuracao);
    }

    @Test
    @DisplayName("Testa Erro Configuracoes")
    void testaErroConfiguracoes() {
        //Não há campos na configuração para teste
    }

    @Test
    void importaRetorno() throws IOException {
        StringBuilder arquivo = new StringBuilder();
        for (String linha : Files.readAllLines(Paths.get("d:/teste/sicoobretorno.ret"))) {
            arquivo.append(linha).append(System.lineSeparator());
        }
        List<RemessaRetornoModel> remessaRetornoModels = boletoService.importarArquivoRetorno(arquivo.toString());
        System.out.println(remessaRetornoModels);
    }

    @Test
    @DisplayName("Testa Valida e Envia Boleto")
    void testaEnvioBoleto() {
        // teste Sucesso (Não implementado)
        BoletoModel boletoModel = preencheBoleto();
        Throwable exception =
                assertThrows(BoletoException.class, () -> boletoService.enviarBoleto(boletoModel));
        assertEquals("Esta função não está disponível para este banco.", exception.getMessage());
    }

    @Test
    @DisplayName("Testa Fator Data")
    void testaFatorData() {
        // teste até 21/02/2025
        String retorno = SicoobUtil.fatorData(LocalDate.of(2025, 2, 21));
        assertEquals("9999", retorno);

        // teste após 21/02/2025
        retorno = SicoobUtil.fatorData(LocalDate.of(2025, 2, 22));
        assertEquals("1000", retorno);
    }

    @Test
    @DisplayName("Testa Gera Dígito Nosso Número")
    void testaGeraDigitoNossoNumero() {
        Integer digitoGerado = SicoobUtil.geraDigitoNossoNumero(
                "4342", "1457020", "670");
        assertEquals(3, digitoGerado);
    }

    private BoletoModel preencheBoleto() {
        BoletoModel boleto = new BoletoModel();
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setAgencia("4342");
        beneficiario.setCarteira(null);
        beneficiario.setConta("59529");
        beneficiario.setDocumento("00000000000101");
        beneficiario.setNomeBeneficiario("EMPRESA DE TESTE");
        beneficiario.setDigitoAgencia("7");
        beneficiario.setPostoDaAgencia(null);
        beneficiario.setDigitoConta("2");
        beneficiario.setNossoNumero("000000670");
        beneficiario.setDigitoNossoNumero("3");
        beneficiario.setNumeroConvenio("1457020");

        Endereco enderecoBenef = new Endereco();
        enderecoBenef.setBairro("CENTRO");
        enderecoBenef.setCep("96030400");
        enderecoBenef.setCidade("PELOTAS");
        enderecoBenef.setComplemento("");
        enderecoBenef.setLogradouro("RUA OLAVO BILAC");
        enderecoBenef.setNumero("722");
        enderecoBenef.setUf("RS");
        beneficiario.setEndereco(enderecoBenef);
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
        boleto.setDataVencimento(LocalDate.of(2022, 6, 30));
        List<InformacaoModel> locaisPagamento = new ArrayList<>();
        locaisPagamento.add(new InformacaoModel("PAGÁVEL PREFERENCIALMENTE EM CANAIS DA SUA INSTITUIÇÃO FINANCEIRA"));
        boleto.setLocaisDePagamento(locaisPagamento);
        boleto.setDataEmissao(LocalDate.now());
        boleto.setNumeroDocumento("000004852");
        boleto.setEspecieDocumento("DMI");
        boleto.setAceite(false);
        boleto.setEspecieMoeda("REAL");
        List<InformacaoModel> instrucoes = new ArrayList<>();
        instrucoes.add(new InformacaoModel("PROTESTO AUTOMÁTICO APÓS 5 DIAS DO VENCIMENTO"));
        boleto.setInstrucoes(instrucoes);
        boleto.setTipoJuros(TipoJurosEnum.PERCENTUAL_MENSAL);
        boleto.setDiasJuros(1);
        boleto.setValorPercentualJuros(BigDecimal.valueOf(9.9));
        boleto.setTipoMulta(TipoMultaEnum.PERCENTUAL);
        boleto.setDiasMulta(1);
        boleto.setValorPercentualMulta(BigDecimal.valueOf(2.00));
        boleto.setTipoDesconto(TipoDescontoEnum.PERCENTUAL_DIA);
        boleto.setProtesto(true);
        boleto.setDiasProtesto(5);
        boleto.setNegativacaoAutomatica(false);
        boleto.setTipoImpressao("A");
        boleto.setEspecieMoeda("REAL");

        return boleto;
    }

}
