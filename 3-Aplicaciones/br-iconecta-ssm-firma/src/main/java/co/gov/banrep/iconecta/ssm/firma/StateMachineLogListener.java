package co.gov.banrep.iconecta.ssm.firma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

import co.gov.banrep.iconecta.ssm.firma.enums.Events;
import co.gov.banrep.iconecta.ssm.firma.enums.States;

/**
 * Escucha los cambios de eventos en la Maquina de Estados y los registra
 * en el Log
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
public class StateMachineLogListener extends StateMachineListenerAdapter<States, Events>{
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void stateContext(StateContext<States, Events> stateContext){
		if(stateContext.getStage() == Stage.STATE_ENTRY){
			log.info("Maquina [" + stateContext.getStateMachine().getId() + "] - ENTRA: " + stateContext.getTarget().getId());
		}else if(stateContext.getStage() == Stage.STATE_EXIT){
			log.info("Maquina [" + stateContext.getStateMachine().getId() + "] - SALE: " + stateContext.getSource().getId());
		}
	}

}
