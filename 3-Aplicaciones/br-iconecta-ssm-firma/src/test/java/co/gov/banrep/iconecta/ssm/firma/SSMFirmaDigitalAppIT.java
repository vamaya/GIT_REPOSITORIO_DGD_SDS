package co.gov.banrep.iconecta.ssm.firma;

//import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import co.gov.banrep.iconecta.ssm.firma.enums.Events;
import co.gov.banrep.iconecta.ssm.firma.enums.States;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class SSMFirmaDigitalAppIT {
	
	@Before
	public void setup() throws Exception{
		stateMachine = stateMachineFactory.getStateMachine();
		
				
		for (int i = 0; i < 10; i++) {
			if (stateMachine.getState() != null) {
				break;
			} else {
				Thread.sleep(200);
			}
		}
	}
	
	/*
	@Configuration
	static class ContextConfiguration {
		@Bean	
		public DataSource dataSource() throws Exception{
			OracleDataSource dataSource = new OracleDataSource();
			dataSource.setUser("username");
			try {
				dataSource.setPassword(new String(Extractor.obtener("cuenta", "endpoint", "tipo")));
			} catch (CAPUPMException e) {
				throw new Exception("Error obteniendo contraseña", e);
			}
			dataSource.setURL("url");
			dataSource.setImplicitCachingEnabled(true);
			dataSource.setFastConnectionFailoverEnabled(true);
			return dataSource;
		}	
	}
		
	*/
	
	@Autowired
	private StateMachineFactory<States, Events> stateMachineFactory;
	
		
	private StateMachine<States, Events> stateMachine;
	
	
	
	@Test
	public void testInitial() throws Exception {
		StateMachineTestPlan<States, Events> plan =
				StateMachineTestPlanBuilder.<States, Events>builder()
					.stateMachine(stateMachine)
					.step()
						.expectStates(States.FASE_INICIAL,States.LISTO)
						.and()					
					.build();
		plan.test();
	}
	
	@Test
	public void testObtenerDocCS() throws Exception {
		StateMachineTestPlan<States, Events> plan =
				StateMachineTestPlanBuilder.<States, Events>builder()
					.stateMachine(stateMachine)
					.step()
						.expectStates(States.FASE_INICIAL,States.LISTO)
						.and()
					.step()
						.sendEvent(Events.INICIAR)
						.expectStates(States.FASE_INICIAL,States.OBTENER_DOCUMENTO_CS)
						.and()					
					.build();
		plan.test();
	}
	
	@Test
	public void testIniciarCircuito() throws Exception {
		StateMachineTestPlan<States, Events> plan =
				StateMachineTestPlanBuilder.<States, Events>builder()
					.stateMachine(stateMachine)
					.step()
						.expectStates(States.FASE_INICIAL,States.LISTO)
						.and()
					.step()
						.sendEvent(Events.INICIAR)
						.expectStates(States.FASE_INICIAL,States.OBTENER_DOCUMENTO_CS)
						.and()
					.step()
						.sendEvent(Events.DOCUMENTO_OBTENIDO_CS)
						.expectStates(States.FASE_INICIAL,States.INICIAR_CIRCUITO_PF)
						.and()
					.build();
		plan.test();
	}
	
	@Test
	public void testEsperandoRespuestaPF() throws Exception {
		StateMachineTestPlan<States, Events> plan =
				StateMachineTestPlanBuilder.<States, Events>builder()
					.stateMachine(stateMachine)
					.step()
						.expectStates(States.FASE_INICIAL,States.LISTO)
						.and()
					.step()
						.sendEvent(Events.INICIAR)
						.expectStates(States.FASE_INICIAL,States.OBTENER_DOCUMENTO_CS)
						.and()
					.step()
						.sendEvent(Events.DOCUMENTO_OBTENIDO_CS)
						.expectStates(States.FASE_INICIAL,States.INICIAR_CIRCUITO_PF)
						.and()
					.step()
						.sendEvent(Events.CIRCUITO_INICIADO)
						.expectStates(States.FASE_INICIAL,States.ESPERANDO_RESPUESTA_PF)
						.and()
					.build();
		plan.test();
	}
	
	@Test
	public void testRespuestaPF() throws Exception {
		StateMachineTestPlan<States, Events> plan =
				StateMachineTestPlanBuilder.<States, Events>builder()
					.stateMachine(stateMachine)
					.step()
						.expectStates(States.FASE_INICIAL,States.LISTO)
						.and()
					.step()
						.sendEvent(Events.INICIAR)
						.expectStates(States.FASE_INICIAL,States.OBTENER_DOCUMENTO_CS)
						.and()
					.step()
						.sendEvent(Events.DOCUMENTO_OBTENIDO_CS)
						.expectStates(States.FASE_INICIAL,States.INICIAR_CIRCUITO_PF)
						.and()
					.step()
						.sendEvent(Events.CIRCUITO_INICIADO)
						.expectStates(States.FASE_INICIAL,States.ESPERANDO_RESPUESTA_PF)
						.and()
					.step()
						.sendEvent(Events.RESPUESTA_PF_EXITOSA)
						.expectStates(States.FASE_FINAL, States.OBTENER_DOCUMENTO_PF)
						.and()
					.build();
		plan.test();
	}

}
