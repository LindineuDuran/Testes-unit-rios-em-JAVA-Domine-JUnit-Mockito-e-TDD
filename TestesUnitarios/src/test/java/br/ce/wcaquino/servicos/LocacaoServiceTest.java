package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.util.*;

import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
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
    public void deveAlugarUmFilme() throws Exception
    {
        Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

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
    public void deveAlugarVariosFilmesComDescontoPorQuantidade() throws Exception
    {
        //cenário
        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmesLocar = Arrays.asList(new Filme("Filme 1", 3, 5.00),
                                                new Filme("Filme 2", 1, 4.00),
                                                new Filme("Filme 3", 2, 5.00));

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(12.75)));
        error.checkThat(locacao.getFilmes().size(), is(equalTo(3)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class) //Forma Elegante
    public void naoDeveAlugarFilmeSemEstoque() throws Exception
    {
        //cenário
        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmesLocar = Arrays.asList(new Filme("Filme 1", 0, 5.00));

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);
    }

    @Test
    public void deveAlugarListaDeFilmesParaOsQueTemEstoque() throws Exception
    {
        //cenário
        Usuario usuario = new Usuario("Usuario 1");

        List<Filme> filmesLocar = Arrays.asList(new Filme("Filme 1", 3, 5.00),
                                                new Filme("Filme 2", 0, 4.00),
                                                new Filme("Filme 3", 2, 5.00),
                                                new Filme("Filme 4", 0, 4.00));

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(10.0)));
        error.checkThat(locacao.getFilmes().size(), is(equalTo(2)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class) //Forma Elegante
    public void naoDeveAlugarFilmesSeNenhumTiverEstoque() throws Exception
    {
        //cenário
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmesLocar = Arrays.asList(new Filme("Filme 1", 0, 5.00),
                                                new Filme("Filme 2", 0, 4.00),
                                                new Filme("Filme 3", 0, 5.00),
                                                new Filme("Filme 4", 0, 4.00));

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);
    }

    @Test //Forma Robusta
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException
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
    public void naoDeveAlugarFilmeSeListaDeFilmesVazia() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");

        exception.expect(LocadoraException.class);
        exception.expectMessage("Lista de Filmes vazia");

        //acao
        service.alugarFilme(usuario, null);
    }

    @Test
    public void devePagar75PctNoTerceiroFilme() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0), new Filme("Filme 3", 2, 4.0));

        //acao
        Locacao resultado = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(11.0));
    }

    @Test
    public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0), new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0));

        //acao
        Locacao resultado = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(13.0));
    }

    @Test
    public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
                new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0),
                new Filme("Filme 5", 2, 4.0));

        //acao
        Locacao resultado = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void devePagar0PctNoFilme6() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
                new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0),
                new Filme("Filme 5", 2, 4.0), new Filme("Filme 6", 2, 4.0));

        //acao
        Locacao resultado = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void naoDeveDevolverFilmeNoDomingo() throws FilmeSemEstoqueException, LocadoraException
    {
        Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
        
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1", 2, 4.0));

        //acao
        Locacao retorno = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(retorno.getDataRetorno(), caiNumaSegunda());
    }
}
