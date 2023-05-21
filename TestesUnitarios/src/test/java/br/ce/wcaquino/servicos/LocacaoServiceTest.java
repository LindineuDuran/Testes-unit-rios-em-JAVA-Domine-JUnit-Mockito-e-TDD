package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class LocacaoServiceTest
{
    @Test
    public void teste()
    {
        //cenário
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 3, 5.00);

        //ação
        Locacao locacaoTest = service.alugarFilme(usuario, filme);

        //verificação
        Assert.assertTrue(locacaoTest.getValor() == 5.00);
        Assert.assertTrue(DataUtils.isMesmaData(locacaoTest.getDataLocacao(), new Date()));
        Assert.assertTrue(DataUtils.isMesmaData(locacaoTest.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)));
    }
}
