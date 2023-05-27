package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LocacaoServiceTest
{
    private LocacaoService service;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup()
    {
        service = new LocacaoService();
    }

    @Test
    public void testeLocacaoDeUmFilme() throws Exception
    {
        //cenário
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 3, 5.00);

        List<Filme> filmesLocar = new ArrayList<>();
        filmesLocar.add(filme);

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(5.0)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
    }

    @Test
    public void testeLocacaoDeVariosFilmes() throws Exception
    {
        //cenário
        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmesLocar = Arrays.asList(new Filme("Filme 1", 3, 5.00),
                                                new Filme("Filme 2", 1, 4.00),
                                                new Filme("Filme 3", 2, 5.00));

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(14.0)));
        error.checkThat(locacao.getFilmes().size(), is(equalTo(3)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class) //Forma Elegante
    public void testeLocacaoDeVariosFilmesAlgunsSemEstoque() throws Exception
    {
        //cenário
        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmesLocar = Arrays.asList(new Filme("Filme 1", 3, 5.00),
                                                new Filme("Filme 2", 0, 4.00),
                                                new Filme("Filme 3", 2, 5.00),
                                                new Filme("Filme 4", 0, 4.00));

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);
    }

    @Test(expected = FilmeSemEstoqueException.class) //Forma Elegante
    public void testeLocacaoDeUmFilmeSemEstoque() throws Exception
    {
        //cenário
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmesLocar = Arrays.asList(new Filme("Filme 2", 0, 4.00));

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);
    }

    @Test //Forma Robusta
    public void testeLocacaoUsuarioVazio() throws FilmeSemEstoqueException
    {
        //cenario
        List<Filme> filmesLocar = Arrays.asList(new Filme("Filme 2", 1, 4.00));

        //acao
        try
        {
            service.alugarFilme(null, filmesLocar);
            Assert.fail();
        }
        catch (LocadoraException e)
        {
            assertThat(e.getMessage(), is("Usuário vazio"));
        }
    }

    @Test //Forma Nova
    public void testeLocacaoListaDeFilmesVazia() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");

        exception.expect(LocadoraException.class);
        exception.expectMessage("Lista de Filmes vazia");

        //acao
        service.alugarFilme(usuario, null);
    }
}
