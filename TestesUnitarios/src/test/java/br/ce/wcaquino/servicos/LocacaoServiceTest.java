package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.daos.LocacaoDAOFake;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
//import buildermaster.BuilderMaster;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.util.*;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.*;
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
        LocacaoDAO dao = new LocacaoDAOFake();
        service.setLocacaoDAO(dao);
    }

    @Test
    public void deveAlugarUmFilme() throws Exception
    {
        Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenário
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora());

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmes);

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(4.0)));
        error.checkThat(locacao.getDataLocacao(), ehHoje());
        error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
    }

    @Test
    public void deveAlugarVariosFilmesComDescontoPorQuantidade() throws Exception
    {
        //cenário
        Usuario usuario = umUsuario().agora();

        List<Filme> filmesLocar = Arrays.asList(umFilme().agora(), umFilme().agora(), umFilme().agora());

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);
        locacao.setDataLocacao(new Date(2023,6,17)); //Data é em um sábado

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(11.00)));
        error.checkThat(locacao.getFilmes().size(), is(equalTo(3)));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(2)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class) //Forma Elegante
    public void naoDeveAlugarFilmeSemEstoque() throws Exception
    {
        //cenário
        Usuario usuario = umUsuario().agora();

        List<Filme> filmes = Arrays.asList(umFilmeSemEstoque().agora());

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmes);
    }

    @Test
    public void deveAlugarListaDeFilmesParaOsQueTemEstoque() throws Exception
    {
        //cenário
        Usuario usuario = umUsuario().agora();

        List<Filme> filmesLocar = Arrays.asList(umFilme().agora(),
                                                umFilme().semEstoque().agora(),
                                                umFilme().agora(),
                                                umFilme().semEstoque().agora());

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);
        locacao.setDataLocacao(new Date(2023,6,17)); //Data é em um sábado

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(8.0)));
        error.checkThat(locacao.getFilmes().size(), is(equalTo(2)));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(2)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class) //Forma Elegante
    public void naoDeveAlugarFilmesSeNenhumTiverEstoque() throws Exception
    {
        //cenário
        Usuario usuario = umUsuario().agora();
        List<Filme> filmesLocar = Arrays.asList(umFilme().semEstoque().agora(),
                                                umFilme().semEstoque().agora(),
                                                umFilme().semEstoque().agora(),
                                                umFilme().semEstoque().agora());

        //ação
        Locacao locacao = service.alugarFilme(usuario, filmesLocar);
    }

    @Test //Forma Robusta
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException
    {
        //cenario
        List<Filme> filmes = Arrays.asList(umFilme().agora());

        //acao
        try
        {
            service.alugarFilme(null, filmes);
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
        Usuario usuario = umUsuario().agora();

        exception.expect(LocadoraException.class);
        exception.expectMessage("Lista de Filmes vazia");

        //acao
        service.alugarFilme(usuario, null);
    }

    @Test
    public void devePagar75PctNoTerceiroFilme() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora());

        //acao
        Locacao resultado = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(11.0));
    }

    @Test
    public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora());

        //acao
        Locacao resultado = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(13.0));
    }

    @Test
    public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora());

        //acao
        Locacao resultado = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void devePagar0PctNoFilme6() throws FilmeSemEstoqueException, LocadoraException
    {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora(),
                                           umFilme().agora());

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
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora());

        //acao
        Locacao retorno = service.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(retorno.getDataRetorno(), caiNumaSegunda());
    }

    /*public static void main(String[] args)
    {
        new BuilderMaster().gerarCodigoClasse(Locacao.class);
    }*/
}
