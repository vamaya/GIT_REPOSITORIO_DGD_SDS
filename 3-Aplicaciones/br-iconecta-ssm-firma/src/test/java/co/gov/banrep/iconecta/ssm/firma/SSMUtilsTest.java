package co.gov.banrep.iconecta.ssm.firma;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import co.gov.banrep.iconecta.ssm.firma.utils.SSMUtils;

//@RunWith(SpringRunner.class)
@RunWith(SpringRunner.class)
//@SpringBootTest
public class SSMUtilsTest {
	
	/*
	@Test
	public void loadContext(){		
	}
	*/
	
	@Test
	public void testCargarValoresCadenaCS() {
		List<Long> listaEsperada = new ArrayList<Long>();
		listaEsperada.add(1000000l);
		listaEsperada.add(1000001l);
		listaEsperada.add(1000002l);
		
		List<Long> listaActual = new ArrayList<Long>();
		String str = "{1000000,1000001,1000002}";
		
		SSMUtils.cargarValoresCadenaCS(str, listaActual);
		
		assertEquals(listaEsperada, listaActual);
	}

	@Test
	public void testToStringFromList() {
		String valorEsperado = "1000000 1000001 1000002 ";
		List<Long> lista = new ArrayList<>();
		lista.add(1000000l);
		lista.add(1000001l);
		lista.add(1000002l);

		String valorActual = SSMUtils.toStringFromList(lista);
		assertEquals(valorEsperado, valorActual);
	}

	@Test
	public void testToStringFromMap() {
		String valorEsperado = "{(Llave1-Valor1), (Llave2-Valor2), (Llave3-Valor3), }";
		Map<String, Object> mapa = new HashMap<>();
		mapa.put("Llave1", "Valor1");
		mapa.put("Llave2", "Valor2");
		mapa.put("Llave3", "Valor3");
		
		String valorActual = SSMUtils.toStringFromMap(mapa);
		assertEquals(valorEsperado, valorActual);
	}

	@Test
	public void testGetRaizLog() {
		//String valorEsperado = "Maquina [3883081] - FINALIZADO - ";
	}

	@Test
	public void testCargarIdsDocumentosPF() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCargarMetadatosDocumentoSsm() {
		//fail("Not yet implemented");
	}

}
