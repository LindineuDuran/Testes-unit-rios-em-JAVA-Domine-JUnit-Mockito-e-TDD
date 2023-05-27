package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

public class LocacaoService
{
    public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException
	{
        if(usuario == null) { throw new LocadoraException("Usuário vazio"); }

        if(filmes == null || filmes.isEmpty()) { throw new LocadoraException("Lista de Filmes vazia"); }

        List<Filme> filmesLocar = new ArrayList<>();
        filmes.forEach( f -> { if(f.getEstoque() != 0) {  filmesLocar.add(f); } });

        if(filmesLocar.size() < filmes.size()) {throw new FilmeSemEstoqueException();}

        Locacao locacao = getLocacao(usuario, filmesLocar);


        //Salvando a locacao...
        //TODO adicionar método para salvar

        return locacao;
    }

    private static Locacao getLocacao(Usuario usuario, List<Filme> filmes)
    {
        Locacao locacao = new Locacao();
        locacao.setFilmes(filmes);
        locacao.setUsuario(usuario);
        locacao.setDataLocacao(new Date());

        Double[] valorLocacao = new Double[1];
        valorLocacao[0] = 0.0;
        filmes.forEach(f -> { valorLocacao[0] += f.getPrecoLocacao();});

        locacao.setValor(valorLocacao[0]);

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