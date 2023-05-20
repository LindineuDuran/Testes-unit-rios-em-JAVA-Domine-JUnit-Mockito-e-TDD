package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Date;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService
{
    public Locacao alugarFilme(Usuario usuario, Filme filme)
	{
        Locacao locacao = getLocacao(usuario, filme);

        //Salvando a locacao...
        //TODO adicionar método para salvar

        return locacao;
    }

    public static void main(String[] args)
	{
        //cenário
        LocacaoService service = new LocacaoService();
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme = new Filme("Filme 1", 3, 10.00);
        Locacao locacaoRef = getLocacao(usuario, filme);

        //ação
		Locacao locacaoTest = service.alugarFilme(usuario, filme);

        //verificação
        System.out.println(locacaoTest.getValor() == locacaoRef.getValor());
        System.out.println(DataUtils.isMesmaData(locacaoTest.getDataLocacao(), new Date()));
        System.out.println(DataUtils.isMesmaData(locacaoTest.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)));

	}

    private static Locacao getDataEntrega(Locacao locacao)
    {
        Date dataEntrega = new Date();
        dataEntrega = adicionarDias(dataEntrega, 1);
        locacao.setDataRetorno(dataEntrega);

        return locacao;
    }

    private static Locacao getLocacao(Usuario usuario, Filme filme)
    {
        Locacao locacao = new Locacao();
        locacao.setFilme(filme);
        locacao.setUsuario(usuario);
        locacao.setDataLocacao(new Date());
        locacao.setValor(filme.getPrecoLocacao());

        //Entrega no dia seguinte
        locacao = getDataEntrega(locacao);
        return locacao;
    }
}