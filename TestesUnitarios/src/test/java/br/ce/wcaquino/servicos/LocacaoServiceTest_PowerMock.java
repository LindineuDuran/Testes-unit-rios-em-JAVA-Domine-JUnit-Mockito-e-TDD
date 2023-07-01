package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static java.util.Calendar.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocacaoService.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class LocacaoServiceTest_PowerMock
{
	@InjectMocks
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
		service = PowerMockito.spy(service);
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
		PowerMockito.verifyStatic(Mockito.times(2));
		Calendar.getInstance();
	}

	@Test
	public void deveAlugarFilmeSemCalcularValor() throws Exception
	{
		//cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		PowerMockito.doReturn(1.0).when(service,"calcularValorLocacao", filmes);

		//acao
		Locacao locacao = service.alugarFilme(usuario, filmes);

		//verificacao
		Assert.assertThat(locacao.getValor(), is(1.0));
		PowerMockito.verifyPrivate(service).invoke("calcularValorLocacao", filmes);
	}

	@Test
	public void deveCalcularValorLocacao() throws Exception
	{
		//cenario
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		//acao
		Double valor = (Double) Whitebox.invokeMethod(service,"calcularValorLocacao", filmes);

		//verificacao
		Assert.assertThat(valor, is(4.0));
	}
}
