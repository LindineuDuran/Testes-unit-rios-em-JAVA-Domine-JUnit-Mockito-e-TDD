package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroException;
import br.ce.wcaquino.runners.ParalleRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ParalleRunner.class)
public class CalculadoraTest
{
    private Calculadora calc;

    @Before
    public void setup()
    {
        calc = new Calculadora();
        System.out.println("iniciando...");
    }

    @After
    public void tearDown()
    {
        System.out.println("finalizando...");
    }

    @Test
    public void deveSomarDoisValores()
    {
        //cenário
        int a = 5;
        int b = 3;

        //ação
        int resultado = calc.somar(a,b);

        //verificação
        Assert.assertEquals(8, resultado);
    }

    @Test
    public void deveSubtrairDoisValores()
    {
        //cenário
        int a = 8;
        int b = 5;

        //ação
        int resultado = calc.subtrair(a,b);

        //verificação
        Assert.assertEquals(3, resultado);
    }

    @Test
    public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException
    {
        //cenário
        int a = 6;
        int b = 3;

        //ação
        int resultado = calc.dividir(a,b);

        //verificação
        Assert.assertEquals(2, resultado);
    }

    @Test(expected = NaoPodeDividirPorZeroException.class)
    public void deveLancarExcecaoAoDividirPorZero() throws NaoPodeDividirPorZeroException
    {
        int a = 10;
        int b = 0;

        calc.dividir(a, b);
    }
}
