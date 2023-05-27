package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LocacaoServiceTest
{
    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testeLocacao() throws Exception
    {
        //cenário
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 3, 5.00);

        //ação
        Locacao locacao = service.alugarFilme(usuario, filme);

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(5.0)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
    }

    @Test(expected = Exception.class) //Método Elegante
    public void testeLocacaoFilmeSemEstoque() throws Exception
    {
        //cenário
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 2", 0, 4.00);

        //ação
        service.alugarFilme(usuario, filme);
    }

    @Test //Método Robusto
    public void testeLocacaoFilmeSemEstoque2()
    {
        //cenário
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 2", 0, 4.00);

        //ação
        try
        {
            service.alugarFilme(usuario, filme);
            Assert.fail("Deveria ter lançado uma exceção");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), is("Filme sem estoque"));
        }
    }

    @Test //Método Novo - Melhor que Robusto
    public void testeLocacaoFilmeSemEstoque3() throws Exception
    {
        //cenário
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 2", 0, 4.00);

        exception.expect(Exception.class);
        exception.expectMessage("Filme sem estoque");

        //acao
        service.alugarFilme(usuario, filme);
    }
}
