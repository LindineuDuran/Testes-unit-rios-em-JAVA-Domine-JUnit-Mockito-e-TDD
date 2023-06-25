package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static br.ce.wcaquino.builders.LocacaoBuilder.umaLocacao;
import static br.ce.wcaquino.utils.DataUtils.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LocacaoServiceTest
{
    private LocacaoService service;
    private SPCService spc;
    private LocacaoDAO dao;
    private EmailService email;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup()
    {
        service = new LocacaoService();
        dao = Mockito.mock(LocacaoDAO.class);
        service.setLocacaoDAO(dao);

        spc = Mockito.mock(SPCService.class);
        service.setSPCService(spc);

        email = Mockito.mock(EmailService.class);
        service.setEmailService(email);
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
        Date dataLocacao = obterData (17,6,2023); //Data é em um sábado
        locacao.setDataLocacao(dataLocacao);

        Date dataRetorno = obterDataComDiferencaDias(obterData (17,6,2023),2);
        locacao.setDataRetorno(dataRetorno); //Data é na segunda-feira

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(11.00)));
        error.checkThat(locacao.getFilmes().size(), is(equalTo(3)));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(dataLocacao, 2)), is(true));
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
        Date dataLocacao = obterData (17,6,2023); //Data é em um sábado
        locacao.setDataLocacao(dataLocacao);

        Date dataRetorno = obterDataComDiferencaDias(obterData (17,6,2023),2);
        locacao.setDataRetorno(dataRetorno); //Data é na segunda-feira

        //verificação
        error.checkThat(locacao.getValor(), is(equalTo(8.0)));
        error.checkThat(locacao.getFilmes().size(), is(equalTo(2)));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(dataLocacao,2)), is(true));
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

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws FilmeSemEstoqueException
    {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora());

        when(spc.possuiNegativacao(usuario)).thenReturn(true);

        //acao
        try
        {
            service.alugarFilme(usuario, filmes);
            Assert.fail();
        }
        catch (LocadoraException e)
        {
            Assert.assertThat(e.getMessage(), is("Usuário Negativado"));
        }

        //verificacao
        verify(spc).possuiNegativacao(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas()
    {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Locacao> locacoes = Arrays.asList(umaLocacao().atrasada().comUsuario(usuario).agora());

        when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

        //acao
        service.notificarAtraso();

        //verificacao
        verify(email).notificarAtraso(usuario);
    }
}
