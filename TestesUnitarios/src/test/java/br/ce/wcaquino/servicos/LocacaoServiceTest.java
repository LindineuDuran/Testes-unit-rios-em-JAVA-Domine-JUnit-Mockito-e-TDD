package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.runners.ParalleRunner;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.LocacaoBuilder.umaLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.*;
import static br.ce.wcaquino.utils.DataUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class LocacaoServiceTest
{
	@InjectMocks @Spy
	private LocacaoService service;

	@Mock
	private SPCService spc;

	@Mock
	private LocacaoDAO dao;

	@Mock
	private EmailService email;

	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		System.out.println("iniciando 2...");
	}

	@After
	public void tearDown()
	{
		System.out.println("finalizando 2...");
	}

	@Test
	public void deveAlugarFilme() throws Exception
	{
		//cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().comValor(5.0).agora());

		Mockito.doReturn(DataUtils.obterData(28, 4, 2017)).when(service).obterData();

		//acao
		Locacao locacao = service.alugarFilme(usuario, filmes);

		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(28, 4, 2017)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 4, 2017)), is(true));
	}

	@Test
	public void deveAlugarVariosFilmesComDescontoPorQuantidade() throws Exception
	{
		//cenário
		Usuario usuario = umUsuario().agora();

		List<Filme> filmesLocar = Arrays.asList(umFilme().agora(), umFilme().agora(), umFilme().agora());

		//ação
		Locacao locacao = service.alugarFilme(usuario, filmesLocar);
		Date dataLocacao = obterData(17, 6, 2023); //Data é em um sábado
		locacao.setDataLocacao(dataLocacao);

		Date dataRetorno = obterDataComDiferencaDias(obterData(17, 6, 2023), 2);
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

		List<Filme> filmesLocar = Arrays.asList(umFilme().agora(), umFilme().semEstoque().agora(), umFilme().agora(), umFilme().semEstoque().agora());

		//ação
		Locacao locacao = service.alugarFilme(usuario, filmesLocar);
		Date dataLocacao = obterData(17, 6, 2023); //Data é em um sábado
		locacao.setDataLocacao(dataLocacao);

		Date dataRetorno = obterDataComDiferencaDias(obterData(17, 6, 2023), 2);
		locacao.setDataRetorno(dataRetorno); //Data é na segunda-feira

		//verificação
		error.checkThat(locacao.getValor(), is(equalTo(8.0)));
		error.checkThat(locacao.getFilmes().size(), is(equalTo(2)));
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(dataLocacao, 2)), is(true));
	}

	@Test(expected = FilmeSemEstoqueException.class) //Forma Elegante
	public void naoDeveAlugarFilmesSeNenhumTiverEstoque() throws Exception
	{
		//cenário
		Usuario usuario = umUsuario().agora();
		List<Filme> filmesLocar = Arrays.asList(umFilme().semEstoque().agora(), umFilme().semEstoque().agora(), umFilme().semEstoque().agora(), umFilme().semEstoque().agora());

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
		List<Filme> filmes = Arrays.asList(umFilme().agora(), umFilme().agora(), umFilme().agora());

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
		List<Filme> filmes = Arrays.asList(umFilme().agora(), umFilme().agora(), umFilme().agora(), umFilme().agora());

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
		List<Filme> filmes = Arrays.asList(umFilme().agora(), umFilme().agora(), umFilme().agora(), umFilme().agora(), umFilme().agora());

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
		List<Filme> filmes = Arrays.asList(umFilme().agora(), umFilme().agora(), umFilme().agora(), umFilme().agora(), umFilme().agora(), umFilme().agora());

		//acao
		Locacao resultado = service.alugarFilme(usuario, filmes);

		//verificacao
		assertThat(resultado.getValor(), is(14.0));
	}

	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception
	{
		//cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		Mockito.doReturn(DataUtils.obterData(29, 4, 2017)).when(service).obterData();

		//acao
		Locacao retorno = service.alugarFilme(usuario, filmes);

		//verificacao
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}

	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception
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
		Usuario usuario2 = umUsuario().comNome("Usuário em dia").agora();
		Usuario usuario3 = umUsuario().comNome("Outro atrasado").agora();
		List<Locacao> locacoes = Arrays.asList(umaLocacao().atrasada().comUsuario(usuario).agora(), umaLocacao().comUsuario(usuario2).agora(), umaLocacao().atrasada().comUsuario(usuario3).agora());

		when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

		//acao
		service.notificarAtraso();

		//verificacao
		verify(email).notificarAtraso(usuario);
		verify(email).notificarAtraso(usuario3);
		verify(email, never()).notificarAtraso(usuario2);
		verifyNoMoreInteractions(email);
	}

	@Test
	public void deveTratarErroNoSPC() throws Exception
	{
		//cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		when(spc.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catatrófica!"));

		//verificacao
		exception.expect(LocadoraException.class);
		exception.expectMessage("Problemas com SPC, tente novamente");

		//acao
		service.alugarFilme(usuario, filmes);
	}

	@Test
	public void deveProrrogarUmaLocacao()
	{
		//cenario
		Locacao locacao = umaLocacao().agora();

		//acao
		service.prorrogarLocacao(locacao, 3);

		//verificacao
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();

		assertThat(locacaoRetornada.getValor(), is(12.0));
		assertThat(locacaoRetornada.getDataLocacao(), ehHoje());
		assertThat(locacaoRetornada.getDataRetorno(), ehHojeComDiferencaDias(3));

	}

	@Test
	public void deveCalcularValorLocacao() throws Exception
	{
		//cenario
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		//acao
		Class<LocacaoService> clazz = LocacaoService.class;
		Method metodo = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
		metodo.setAccessible(true);
		Double valor = (Double) metodo.invoke(service, filmes);

		//verificacao
		Assert.assertThat(valor, is(4.0));
	}
}
