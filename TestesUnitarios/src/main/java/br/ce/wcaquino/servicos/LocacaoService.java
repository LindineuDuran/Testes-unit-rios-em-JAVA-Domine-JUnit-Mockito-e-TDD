package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;

import java.util.Date;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

public class LocacaoService
{
    public Locacao alugarFilme(Usuario usuario, Filme filme)
	{
        Locacao locacao = getLocacao(usuario, filme);

        //Salvando a locacao...
        //TODO adicionar método para salvar

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

    private static Locacao getDataEntrega(Locacao locacao)
    {
        Date dataEntrega = new Date();
        dataEntrega = adicionarDias(dataEntrega, 1);
        locacao.setDataRetorno(dataEntrega);

        return locacao;
    }
}